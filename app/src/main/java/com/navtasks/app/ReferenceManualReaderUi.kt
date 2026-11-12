package com.navtasks.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
