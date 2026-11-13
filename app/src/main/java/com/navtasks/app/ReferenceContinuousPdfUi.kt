package com.navtasks.app

import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.ceil

/**
 * Keep the HONOR-accepted page renderer (with search and hit highlighting)
 * available at all times. Continuous PDF is a standalone opt-in-equivalent
 * view mode on the same original file; it never changes storage or profiles.
 */
@Composable
internal fun ReferencePdfReader(file: File, p: NavPalette, lang: UiLanguage) {
    var continuous by remember(file.path) { mutableStateOf(true) }
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.End) {
            TextButton(onClick = { continuous = !continuous }) {
                Text(
                    if (continuous)
                        localized("Постранично / поиск", "Single page / search", lang)
                    else
                        localized("Лента страниц", "Continuous pages", lang),
                    color = p.blueText, fontSize = 13.sp,
                )
            }
        }
        Box(Modifier.weight(1f)) {
            if (continuous) ReferenceContinuousPdfReader(file, p, lang)
            else ReferencePdfPagedReader(file, p, lang)
        }
    }
}

/**
 * LazyColumn composes only viewport-adjacent pages. Each page owns exactly
 * one bounded PdfRenderer bitmap and releases it when it leaves composition.
 * In contrast to the rejected native AndroidX beta, this uses the existing
 * Android framework PdfRenderer and never restores a PDF Fragment.
 */
@Composable
private fun ReferenceContinuousPdfReader(file: File, p: NavPalette, lang: UiLanguage) {
    val density = LocalDensity.current
    val pageCountResult by produceState<Result<Int>?>(null, file.path) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                PdfRenderer(ParcelFileDescriptor.open(
                    file, ParcelFileDescriptor.MODE_READ_ONLY,
                )).use { renderer ->
                    require(renderer.pageCount in 1..20_000) { "Invalid PDF page count" }
                    renderer.pageCount
                }
            }
        }
    }
    val count = pageCountResult?.getOrNull()
    var zoom by remember(file.path) { mutableFloatStateOf(1f) }
    var rasterZoom by remember(file.path) { mutableFloatStateOf(1f) }
    val listState = rememberLazyListState()
    val horizontal = rememberScrollState()

    Column(Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                localized("Стр. ", "Page ", lang) +
                    (if (count == null) "…" else
                        (listState.firstVisibleItemIndex + 1).coerceIn(1, count)
                            .toString() + " / " + count),
                modifier = Modifier.weight(1f),
                color = p.text, fontSize = 13.sp,
            )
            TextButton(onClick = {
                zoom = (zoom / 1.4f).coerceAtLeast(1f)
                rasterZoom = zoom
            }) { Text("−", color = p.blueText, fontSize = 22.sp) }
            Text(((zoom * 100f).toInt()).toString() + "%", color = p.muted, fontSize = 12.sp)
            TextButton(onClick = {
                zoom = (zoom * 1.4f).coerceAtMost(4f)
                rasterZoom = zoom
            }) { Text("+", color = p.blueText, fontSize = 22.sp) }
        }
        when {
            pageCountResult == null -> Box(Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = p.blueText)
            }
            count == null -> Text(
                pageCountResult?.exceptionOrNull()?.localizedMessage ?:
                    localized("Не удалось открыть PDF", "Cannot open PDF", lang),
                color = p.systemRed, modifier = Modifier.padding(16.dp),
            )
            else -> BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
                val fitWidth = maxWidth
                val fitPixels = with(density) { fitWidth.roundToPx() }.coerceAtLeast(1)
                val zoomBucket = ceil(rasterZoom * 2f) / 2f
                val rasterPixels = (fitPixels * zoomBucket).toInt().coerceAtLeast(1)
                val documentWidth = fitWidth * zoom

                Box(Modifier.fillMaxSize()
                    .pointerInput(file.path) {
                        awaitEachGesture {
                            awaitFirstDown(requireUnconsumed = false)
                            do {
                                val event = awaitPointerEvent(PointerEventPass.Initial)
                                if (event.changes.count { it.pressed } >= 2) {
                                    val previous = zoom
                                    val next = (previous * event.calculateZoom()).coerceIn(1f, 4f)
                                    if (kotlin.math.abs(next - previous) > .001f) {
                                        val finger = event.calculateCentroid(useCurrent = true)
                                        val oldX = horizontal.value
                                        val row = listState.firstVisibleItemIndex
                                        val oldY = listState.firstVisibleItemScrollOffset
                                        zoom = next
                                        // Let the layout grow before clamping the new scroll.
                                        withFrameNanos { }
                                        horizontal.scrollTo(referencePdfFocalScroll(
                                            oldX, finger.x, previous, next, horizontal.maxValue))
                                        listState.scrollToItem(row, referencePdfFocalScroll(
                                            oldY, finger.y, previous, next, 10_000_000))
                                        event.changes.forEach { it.consume() }
                                    }
                                }
                            } while (event.changes.any { it.pressed })
                            // Only rerender once fingers are lifted, not per pinch frame.
                            rasterZoom = zoom
                        }
                    }
                    .horizontalScroll(horizontal)) {
                    LazyColumn(
                        modifier = Modifier.width(documentWidth).fillMaxHeight(),
                        state = listState,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 20.dp),
                    ) {
                        items(count = count, key = { it }) { index ->
                            ReferenceContinuousPdfPage(
                                file = file,
                                index = index,
                                rasterPixels = rasterPixels,
                                documentWidth = documentWidth,
                                p = p,
                                lang = lang,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReferenceContinuousPdfPage(
    file: File,
    index: Int,
    rasterPixels: Int,
    documentWidth: androidx.compose.ui.unit.Dp,
    p: NavPalette,
    lang: UiLanguage,
) {
    // Each visible page stays under 2.4 Mpx / approximately 9.6 MB ARGB.
    // The old single-page renderer retains its independent 7 Mpx budget.
    val result by produceState<Result<NavPdfRaster>?>(null,
        file.path, index, rasterPixels) {
        value = null
        value = withContext(Dispatchers.IO) {
            runCatching {
                navRenderPdfPage(
                    file, index, rasterPixels, maxPixels = 2_400_000L,
                )
            }
        }
    }
    val page = result?.getOrNull()?.takeIf { it.pageIndex == index }
    DisposableEffect(page) {
        onDispose {
            if (page != null && !page.bitmap.isRecycled) page.bitmap.recycle()
        }
    }

    when {
        result == null -> Box(
            Modifier.width(documentWidth).height(documentWidth * 1.41f),
            contentAlignment = Alignment.Center,
        ) { CircularProgressIndicator(color = p.blueText) }
        page == null -> Box(
            Modifier.width(documentWidth).padding(16.dp),
        ) {
            Text(
                localized("Не удалось открыть страницу ", "Cannot open page ", lang) +
                    (index + 1) + ": " +
                    (result?.exceptionOrNull()?.localizedMessage ?: ""),
                color = p.systemRed, fontSize = 12.sp,
            )
        }
        else -> {
            val ratio = page.bitmap.height.toFloat() / page.bitmap.width
            Image(
                bitmap = page.bitmap.asImageBitmap(),
                contentDescription = localized("Страница PDF ", "PDF page ", lang) + (index + 1),
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.width(documentWidth).height(documentWidth * ratio),
            )
        }
    }
}
