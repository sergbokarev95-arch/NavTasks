package com.navtasks.app

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.graphics.RectF
import android.graphics.pdf.PdfRenderer
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withFrameNanos
import java.io.File
import kotlin.math.ceil

/** One page at a time: no document-wide bitmaps are retained in application state. */
private data class NavPdfRaster(
    val bitmap: Bitmap,
    val pageCount: Int,
    val pageIndex: Int,
    val pageWidth: Int,
    val pageHeight: Int,
)

private data class NavPdfMatch(val pageIndex: Int, val bounds: List<RectF>)

private fun navRenderPdfPage(file: File, index: Int, requestedWidth: Int): NavPdfRaster {
    PdfRenderer(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)).use { pdf ->
        require(pdf.pageCount in 1..20000) { "Invalid PDF page count" }
        val actualIndex = index.coerceIn(0, pdf.pageCount - 1)
        pdf.openPage(actualIndex).use { page ->
            val pixel = referencePdfPixelSize(page.width, page.height, requestedWidth)
            val bitmap = Bitmap.createBitmap(pixel.width, pixel.height, Bitmap.Config.ARGB_8888)
            try {
                bitmap.eraseColor(AndroidColor.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            } catch (failure: Throwable) {
                bitmap.recycle()
                throw failure
            }
            return NavPdfRaster(bitmap, pdf.pageCount, actualIndex, page.width, page.height)
        }
    }
}

/** Android 15+: each search hit contains its actual page rectangles in PDF points. */
private suspend fun navSearchPdf(file: File, query: String): List<NavPdfMatch> {
    if (Build.VERSION.SDK_INT < 35) return emptyList()
    val hits = ArrayList<NavPdfMatch>()
    PdfRenderer(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)).use { pdf ->
        for (pageIndex in 0 until pdf.pageCount) {
            currentCoroutineContext().ensureActive()
            pdf.openPage(pageIndex).use { page ->
                for (match in page.searchText(query)) {
                    hits += NavPdfMatch(pageIndex, match.bounds.map { RectF(it) })
                    require(hits.size <= 100000) { "Too many PDF search results" }
                }
            }
        }
    }
    return hits
}

@Composable
internal fun ReferencePdfReader(file: File, p: NavPalette, lang: UiLanguage) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    var page by remember(file.path) { mutableIntStateOf(0) }
    var zoom by remember(file.path) { mutableFloatStateOf(1f) }
    var query by remember(file.path) { mutableStateOf("") }
    var matches by remember(file.path) { mutableStateOf<List<NavPdfMatch>>(emptyList()) }
    var selected by remember(file.path) { mutableIntStateOf(0) }
    var busy by remember(file.path) { mutableStateOf(false) }
    var message by remember(file.path) { mutableStateOf<String?>(null) }
    var searchJob by remember(file.path) { mutableStateOf<Job?>(null) }
    DisposableEffect(file.path) {
        onDispose { searchJob?.cancel() }
    }

    val vertical = rememberScrollState()
    val horizontal = rememberScrollState()
    val active = matches.getOrNull(selected)

    Column(Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val availableWidth = maxWidth
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                TextButton(enabled = page > 0, onClick = { page-- }) {
                    Text("‹", color = p.blueText, fontSize = 25.sp)
                }
                Text(localized("Стр. ", "Page ", lang) + "${page + 1}",
                    color = p.text, fontSize = 14.sp,
                    modifier = Modifier.weight(1f))
                TextButton(onClick = {
                    zoom = (zoom / 1.4f).coerceAtLeast(1f)
                }) { Text("−", color = p.blueText, fontSize = 23.sp) }
                Text("${(zoom * 100).toInt()}%", color = p.muted, fontSize = 12.sp)
                TextButton(onClick = {
                    zoom = (zoom * 1.4f).coerceAtMost(4f)
                }) { Text("+", color = p.blueText, fontSize = 23.sp) }
                TextButton(onClick = { page++ }) {
                    Text("›", color = p.blueText, fontSize = 25.sp)
                }
            }
        }
        if (Build.VERSION.SDK_INT >= 35) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f)) {
                    NavGlassInput(query, { changed ->
                        searchJob?.cancel()
                        query = changed.take(120)
                        matches = emptyList()
                        selected = 0
                        busy = false
                        message = null
                    }, p, modifier = Modifier.fillMaxWidth(),
                        placeholder = localized("Поиск по документу", "Find in document", lang))
                }
                TextButton(enabled = query.trim().length >= 2 && !busy,
                    onClick = {
                        val requested = query.trim()
                        searchJob?.cancel()
                        busy = true
                        searchJob = scope.launch {
                            try {
                                val found = withContext(Dispatchers.IO) {
                                    navSearchPdf(file, requested)
                                }
                                if (query.trim() != requested) return@launch
                                matches = found
                                selected = 0
                                if (found.isNotEmpty()) page = found.first().pageIndex
                                message = if (found.isEmpty())
                                    localized("Совпадений нет", "No matches", lang) else null
                            } catch (exception: CancellationException) {
                                throw exception
                            } catch (exception: Exception) {
                                if (query.trim() == requested)
                                    message = exception.localizedMessage
                            } finally {
                                if (query.trim() == requested) busy = false
                            }
                        }
                    }) { Text(localized("Найти", "Find", lang), color = p.blueText) }
            }
            if (busy) LinearProgressIndicator(Modifier.fillMaxWidth(), color = p.blueText)
            if (matches.isNotEmpty()) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(localized("Найдено ", "Found ", lang) +
                        "${selected + 1} / ${matches.size}",
                        color = p.muted, fontSize = 13.sp,
                        modifier = Modifier.weight(1f))
                    TextButton(onClick = {
                        selected = (selected - 1 + matches.size) % matches.size
                        page = matches[selected].pageIndex
                    }) { Text("↑", color = p.blueText, fontSize = 19.sp) }
                    TextButton(onClick = {
                        selected = (selected + 1) % matches.size
                        page = matches[selected].pageIndex
                    }) { Text("↓", color = p.blueText, fontSize = 19.sp) }
                }
            }
        }
        if (message != null) {
            Text(message.orEmpty(), color = p.systemRed, fontSize = 12.sp)
        }

        // Fit-to-width at 100%; the viewport itself determines pixel size.
        BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
            val fitWidth = maxWidth
            val fitPixels = with(density) { fitWidth.roundToPx() }
            val zoomBucket = ceil(zoom * 2).toInt() / 2f
            val loaded by produceState<Result<NavPdfRaster>?>(null,
                file.path, page, zoomBucket, fitPixels) {
                value = null
                value = withContext(Dispatchers.IO) {
                    runCatching {
                        navRenderPdfPage(file, page, (fitPixels * zoomBucket).toInt())
                    }
                }
            }
            val render = loaded?.getOrNull()?.takeIf { it.pageIndex == page }
            val pageCount = render?.pageCount ?: 0
            // A page-change button never points to a nonexistent page.
            LaunchedEffect(pageCount, page) {
                if (pageCount > 0 && page >= pageCount) page = pageCount - 1
            }
            when {
                loaded == null -> CircularProgressIndicator(
                    color = p.blueText, modifier = Modifier.align(Alignment.Center))
                render == null -> Text(
                    loaded?.exceptionOrNull()?.localizedMessage ?:
                        localized("Не удалось открыть страницу PDF", "Cannot open PDF page", lang),
                    color = p.systemRed, modifier = Modifier.padding(16.dp))
                else -> {
                    val imageWidth = fitWidth * zoom
                    val ratio = render.bitmap.height.toFloat() / render.bitmap.width
                    val imageHeight = imageWidth * ratio
                    val viewportHeightPx = with(density) { maxHeight.toPx() }
                    val viewportWidthPx = with(density) { maxWidth.toPx() }
                    val imageWidthPx = with(density) { imageWidth.toPx() }
                    val imageHeightPx = with(density) { imageHeight.toPx() }

                    LaunchedEffect(page, selected, matches, zoom, render.bitmap) {
                        // Wait until new document/page dimensions and scroll ranges are committed.
                        withFrameNanos { }
                        val chosen = matches.getOrNull(selected)?.takeIf { it.pageIndex == page }
                        val rectangle = chosen?.bounds?.firstOrNull()
                        if (rectangle == null) {
                            vertical.scrollTo(0)
                            horizontal.scrollTo(0)
                        } else {
                            val targetY = (rectangle.centerY() * imageHeightPx /
                                render.pageHeight - viewportHeightPx * .35f).toInt()
                            val targetX = (rectangle.centerX() * imageWidthPx /
                                render.pageWidth - viewportWidthPx * .5f).toInt()
                            vertical.animateScrollTo(targetY.coerceIn(0, vertical.maxValue))
                            horizontal.animateScrollTo(targetX.coerceIn(0, horizontal.maxValue))
                        }
                    }
                    Box(Modifier.fillMaxSize()
                        .pointerInput(file.path) {
                            awaitEachGesture {
                                awaitFirstDown(requireUnconsumed = false)
                                do {
                                    val event = awaitPointerEvent(PointerEventPass.Initial)
                                    if (event.changes.count { it.pressed } >= 2) {
                                        zoom = (zoom * event.calculateZoom()).coerceIn(1f, 4f)
                                        event.changes.forEach { it.consume() }
                                    }
                                } while (event.changes.any { it.pressed })
                            }
                        }
                        .verticalScroll(vertical)
                        .horizontalScroll(horizontal)) {
                        Box(Modifier.width(imageWidth).height(imageHeight)) {
                            Image(render.bitmap.asImageBitmap(),
                                contentDescription = localized("Страница PDF", "PDF page", lang),
                                contentScale = ContentScale.FillBounds,
                                modifier = Modifier.matchParentSize())
                            val pageHits = matches.withIndex().filter {
                                it.value.pageIndex == page
                            }
                            if (pageHits.isNotEmpty()) {
                                Canvas(Modifier.matchParentSize()) {
                                    pageHits.forEach { indexed ->
                                        indexed.value.bounds.forEach { rectangle ->
                                            val r = referencePdfRectPixels(
                                                rectangle.left, rectangle.top,
                                                rectangle.right, rectangle.bottom,
                                                render.pageWidth, render.pageHeight,
                                                size.width, size.height,
                                            )
                                            val width = (r[2] - r[0]).coerceAtLeast(1f)
                                            val height = (r[3] - r[1]).coerceAtLeast(1f)
                                            val chosen = indexed.index == selected
                                            drawRect(
                                                color = if (chosen)
                                                    p.systemOrange.copy(alpha = .58f)
                                                else p.blue.copy(alpha = .23f),
                                                topLeft = Offset(r[0], r[1]),
                                                size = Size(width, height),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
