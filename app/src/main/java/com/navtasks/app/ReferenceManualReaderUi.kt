package com.navtasks.app

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.graphics.pdf.PdfRenderer
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
internal fun ReferenceManualReader(
    document: ReferenceManualDocument,
    store: ReferenceManualStore,
    p: NavPalette,
    lang: UiLanguage,
) {
    val original = remember(document.sha256, document.bytes) {
        runCatching { store.findOriginal(document) }
    }
    if (original.isFailure) {
        NavPanel(p) {
            Text(localized(
                "Оригинальный файл отсутствует или неполный. Восстанови архив библиотеки.",
                "Original manual is missing or incomplete. Restore your library backup.", lang),
                color = p.systemRed)
        }
        return
    }
    when (document.mimeType) {
        ReferenceManualMime.PDF ->
            ReferencePdfReader(original.getOrThrow(), p, lang)
        ReferenceManualMime.DOCX ->
            ReferenceDocxReader(original.getOrThrow(), p, lang)
    }
}

/** Android's built-in offline PDF rasterizer works on our minSdk 31. */
private data class ReferencePdfRaster(val bitmap: Bitmap, val pages: Int)
private data class ReferencePdfHit(val page: Int)
private fun readReferencePdfPage(file: File, index: Int, targetWidth: Int): ReferencePdfRaster {
    PdfRenderer(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)).use { pdf ->
        require(pdf.pageCount in 1..20000) { "Unsupported PDF page count" }
        pdf.openPage(index.coerceIn(0, pdf.pageCount - 1)).use { page ->
            val width = targetWidth.coerceIn(480, 1800)
            val height = (width.toFloat() * page.height / page.width)
                .toInt().coerceIn(1, 3600)
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.eraseColor(AndroidColor.WHITE)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            return ReferencePdfRaster(bitmap, pdf.pageCount)
        }
    }
}

/** API 35+ supports text extraction/search. Older devices retain offline page reading. */
private fun searchReferencePdf(file: File, query: String): List<ReferencePdfHit> {
    if (Build.VERSION.SDK_INT < 35) return emptyList()
    val results = ArrayList<ReferencePdfHit>()
    PdfRenderer(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)).use { pdf ->
        for (pageIndex in 0 until pdf.pageCount) {
            pdf.openPage(pageIndex).use { page ->
                val found = page.searchText(query)
                repeat(found.size) { results += ReferencePdfHit(pageIndex) }
            }
            require(results.size <= 100000) { "Too many matching words" }
        }
    }
    return results
}

@Composable
private fun ReferencePdfReader(file: File, p: NavPalette, lang: UiLanguage) {
    val scope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val baseWidth = with(density) { configuration.screenWidthDp.dp.roundToPx() }
    var currentPage by remember(file.path) { mutableIntStateOf(0) }
    var zoom by remember(file.path) { mutableFloatStateOf(1f) }
    var query by remember(file.path) { mutableStateOf("") }
    var results by remember(file.path) { mutableStateOf<List<ReferencePdfHit>>(emptyList()) }
    var matchIndex by remember(file.path) { mutableIntStateOf(0) }
    var searching by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    val screenWidth = configuration.screenWidthDp.dp
    val raster by produceState<Result<ReferencePdfRaster>?>(null, file.path, currentPage, zoom, baseWidth) {
        value = null
        value = withContext(Dispatchers.IO) {
            runCatching { readReferencePdfPage(file, currentPage,
                (baseWidth * zoom).toInt()) }
        }
    }
    val pages = raster?.getOrNull()?.pages ?: 0

    Column(Modifier.fillMaxSize().padding(horizontal = 14.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = { currentPage = (currentPage - 1).coerceAtLeast(0) },
                enabled = currentPage > 0) {
                Text("‹", color = p.blueText, fontSize = 24.sp)
            }
            Text(localized("Стр. ", "Page ", lang) +
                "${currentPage + 1} / ${pages.takeIf { it > 0 } ?: "…"}",
                color = p.text, fontSize = 13.sp,
                modifier = Modifier.weight(1f).padding(top = 12.dp))
            TextButton(onClick = { currentPage = (currentPage + 1).coerceAtMost(pages - 1) },
                enabled = pages > 0 && currentPage < pages - 1) {
                Text("›", color = p.blueText, fontSize = 24.sp)
            }
            TextButton(onClick = { zoom = (zoom - .25f).coerceAtLeast(1f) }) {
                Text("−", color = p.blueText, fontSize = 20.sp)
            }
            TextButton(onClick = { zoom = (zoom + .25f).coerceAtMost(2.5f) }) {
                Text("+", color = p.blueText, fontSize = 20.sp)
            }
        }
        if (Build.VERSION.SDK_INT >= 35) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                Box(Modifier.weight(1f)) {
                    NavGlassInput(query, {
                        query = it.take(120)
                        results = emptyList()
                        matchIndex = 0
                    }, p, placeholder = localized("Поиск по PDF", "Search PDF", lang),
                        modifier = Modifier.fillMaxWidth())
                }
                TextButton(enabled = query.trim().length >= 2 && !searching,
                    onClick = {
                        scope.launch {
                            searching = true
                            val found = withContext(Dispatchers.IO) {
                                runCatching { searchReferencePdf(file, query.trim()) }
                            }
                            searching = false
                            found.onSuccess {
                                results = it
                                matchIndex = 0
                                if (it.isNotEmpty()) currentPage = it.first().page
                                message = if (it.isEmpty())
                                    localized("Совпадений нет.", "No matches.", lang) else null
                            }.onFailure { message = it.localizedMessage }
                        }
                    }) { Text(localized("Найти", "Find", lang), color = p.blueText) }
            }
            if (searching) LinearProgressIndicator(Modifier.fillMaxWidth(), color = p.blueText)
            if (results.isNotEmpty()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(localized("Совпадение ", "Match ", lang) +
                        "${matchIndex + 1}/${results.size}",
                        color = p.muted, fontSize = 12.sp,
                        modifier = Modifier.weight(1f).padding(top = 9.dp))
                    TextButton(onClick = {
                        matchIndex = (matchIndex - 1 + results.size) % results.size
                        currentPage = results[matchIndex].page
                    }) { Text("↑", color = p.blueText) }
                    TextButton(onClick = {
                        matchIndex = (matchIndex + 1) % results.size
                        currentPage = results[matchIndex].page
                    }) { Text("↓", color = p.blueText) }
                }
            }
        } else {
            Text(localized(
                "Просмотр страниц доступен офлайн. Поиск внутри PDF поддерживается на Android 15+.",
                "Pages work offline; PDF text search requires Android 15+.", lang),
                color = p.muted, fontSize = 11.sp)
        }
        if (message != null) {
            Text(message.orEmpty(), color = p.muted, fontSize = 12.sp)
        }
        val data = raster
        when {
            data == null ->
                CircularProgressIndicator(color = p.blueText)
            data.isFailure -> Text(data.exceptionOrNull()?.localizedMessage ?:
                localized("PDF не удалось прочитать.", "Cannot open PDF.", lang),
                color = p.systemRed, fontSize = 13.sp)
            else -> {
                // Render only ONE page at a time, bounding memory even for huge SIRE manuals.
                val bitmap = data.getOrThrow().bitmap
                val vertical = rememberScrollState()
                val horizontal = rememberScrollState()
                Box(Modifier.weight(1f).fillMaxWidth().verticalScroll(vertical)
                    .horizontalScroll(horizontal)) {
                    Image(bitmap.asImageBitmap(), contentDescription = localized(
                        "Страница документа", "Document page", lang),
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier.width(screenWidth * zoom).padding(bottom = 105.dp))
                }
            }
        }
    }
}

@Composable
private fun ReferenceDocxReader(file: File, p: NavPalette, lang: UiLanguage) {
    var query by remember(file.path) { mutableStateOf("") }
    var currentResult by remember { mutableIntStateOf(0) }
    val listState = rememberLazyListState()
    val paragraphs by produceState<Result<List<ReferenceDocxParagraph>>?>(null, file.path) {
        value = withContext(Dispatchers.IO) {
            runCatching { extractReferenceDocxText(file) }
        }
    }
    val data = paragraphs
    when {
        data == null -> CircularProgressIndicator(color = p.blueText)
        data.isFailure -> Text(data.exceptionOrNull()?.localizedMessage ?: "DOCX error",
            color = p.systemRed, modifier = Modifier.padding(16.dp))
        else -> {
            val rows = data.getOrThrow()
            val needle = navEffectiveSearchQuery(query)
            val hits = remember(rows, needle) {
                if (needle.isEmpty()) emptyList() else rows.indices.filter {
                    rows[it].text.contains(needle, ignoreCase = true)
                }
            }
            val scope = rememberCoroutineScope()
            Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                Text(localized(
                    "Упрощённый режим чтения Word. Сложная вёрстка и изображения не воспроизводятся.",
                    "Simplified Word reader. Complex layouts and images are not shown.", lang),
                    color = p.muted, fontSize = 11.sp,
                    modifier = Modifier.padding(bottom = 8.dp))
                NavGlassInput(query, { query = it.take(120); currentResult = 0 }, p,
                    placeholder = localized("Поиск по тексту Word", "Search Word text", lang),
                    modifier = Modifier.fillMaxWidth())
                if (hits.isNotEmpty()) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("${currentResult + 1}/${hits.size}", color = p.muted,
                            modifier = Modifier.weight(1f).padding(top = 10.dp))
                        TextButton(onClick = {
                            currentResult = (currentResult - 1 + hits.size) % hits.size
                            scope.launch { listState.animateScrollToItem(hits[currentResult]) }
                        }) { Text("↑", color = p.blueText) }
                        TextButton(onClick = {
                            currentResult = (currentResult + 1) % hits.size
                            scope.launch { listState.animateScrollToItem(hits[currentResult]) }
                        }) { Text("↓", color = p.blueText) }
                    }
                }
                LazyColumn(state = listState, contentPadding = PaddingValues(bottom = 125.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    itemsIndexed(rows) { _, paragraph ->
                        NavSearchMarkedText(paragraph.text, needle, p,
                            color = p.text, fontSize = if (paragraph.heading) 18.sp else 14.sp)
                    }
                }
            }
        }
    }
}
