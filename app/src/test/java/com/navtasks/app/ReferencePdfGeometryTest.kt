package com.navtasks.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReferencePdfGeometryTest {
    @Test fun fittedPdfPageIsNotCroppedAndKeepsAspect() {
        val pixel = referencePdfPixelSize(
            pageWidth = 595, pageHeight = 842, requestedWidth = 1080,
        )
        assertEquals(1080, pixel.width)
        assertTrue(pixel.height in 1527..1530)
        assertTrue(pixel.width.toLong() * pixel.height <= 7_000_000L)
    }

    @Test fun bigOrUnusualPagesRemainWithinBitmapMemoryLimits() {
        for ((w, h) in listOf(
            500 to 800, 9000 to 20000, 30 to 200000,
            200000 to 30, 900 to 900
        )) {
            val pixel = referencePdfPixelSize(w, h, 10000)
            assertTrue(pixel.width in 1..2400)
            assertTrue(pixel.height in 1..4096)
            assertTrue(pixel.width.toLong() * pixel.height <= 7_000_000L)
        }
        assertTrue(runCatching { referencePdfPixelSize(0, 100, 720) }.isFailure)
        assertTrue(runCatching { referencePdfPixelSize(100, 0, 720) }.isFailure)
    }

    @Test fun searchBoundsAreMappedFromPdfPointsToDisplayedImagePixels() {
        val rect = referencePdfRectPixels(
            100f, 200f, 180f, 230f,
            pageWidthPoints = 600, pageHeightPoints = 900,
            outputWidthPixels = 1200f, outputHeightPixels = 1800f,
        )
        assertEquals(200f, rect[0], 0.001f)
        assertEquals(400f, rect[1], 0.001f)
        assertEquals(360f, rect[2], 0.001f)
        assertEquals(460f, rect[3], 0.001f)
    }

    @Test fun searchBoxesStayCorrectWhenPageIsZoomed() {
        val normal = referencePdfRectPixels(
            100f, 200f, 180f, 230f, 600, 900, 600f, 900f,
        )
        val doubled = referencePdfRectPixels(
            100f, 200f, 180f, 230f, 600, 900, 1200f, 1800f,
        )
        normal.indices.forEach { i ->
            assertEquals(normal[i] * 2f, doubled[i], 0.001f)
        }
    }

    @Test fun pinchZoomAnchorsHorizontalAndVerticalScrollToFingers() {
        // Point x=160 at 100% stays at x=160 when document zooms to 200%.
        assertEquals(260, referencePdfFocalScroll(
            oldScroll = 50, focalPx = 160f, previousZoom = 1f,
            nextZoom = 2f, maxScroll = 1000))
        // The same point stays anchored while zooming out.
        assertEquals(50, referencePdfFocalScroll(
            oldScroll = 260, focalPx = 160f, previousZoom = 2f,
            nextZoom = 1f, maxScroll = 1000))
        assertEquals(0, referencePdfFocalScroll(0, 20f, 2f, 1f, 0))
        assertEquals(150, referencePdfFocalScroll(200, 100f, 2f, 1f, 150))
    }

    @Test fun continuousPdfZoomUsesPageUnderFingersNotTopVisiblePage() {
        val visible = listOf(
            Triple(9, -330, 650), Triple(10, 328, 850), Triple(11, 1186, 700))
        assertEquals(9, referencePdfFocalItemIndex(180f, visible))
        assertEquals(10, referencePdfFocalItemIndex(350f, visible))
        assertEquals(10, referencePdfFocalItemIndex(1010f, visible))
        assertEquals(9, referencePdfFocalItemIndex(-20f, visible))
        assertEquals(null, referencePdfFocalItemIndex(200f, emptyList()))
        // Page 10 begins at y=328, finger y=400; twofold zoom must
        // retain the same page at the touched point, not navigate to page 9.
        assertEquals(-256, referencePdfFocalItemOffset(
            itemTopPx = 328, focalYPx = 400f, previousZoom = 1f, nextZoom = 2f))
        // A partly scrolled page should stay at the same point after zoom.
        assertEquals(520, referencePdfFocalItemOffset(
            itemTopPx = -180, focalYPx = 160f, previousZoom = 1f, nextZoom = 2f))
    }

    @Test fun invalidZoomFactorsAreRejected() {
        assertTrue(runCatching {
            referencePdfFocalScroll(0, 100f, 0f, 1f, 1000)
        }.isFailure)
        assertTrue(runCatching {
            referencePdfFocalItemOffset(0, 100f, 1f, 0f)
        }.isFailure)
    }

}
