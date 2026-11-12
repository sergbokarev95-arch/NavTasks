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
}
