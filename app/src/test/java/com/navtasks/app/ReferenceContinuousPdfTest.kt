package com.navtasks.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** QA6 release gate: vertical pages must not replace the accepted PDF path. */
class ReferenceContinuousPdfTest {
    private val src = File("src/main/java/com/navtasks/app")

    @Test fun pagedPdfSearchAndQuarantinedNativeViewerRemainAvailable() {
        val paged = src.resolve("ReferenceFullscreenPdfUi.kt").readText()
        val native = src.resolve("ReferencePdfActivity.kt").readText()
        val continuous = src.resolve("ReferenceContinuousPdfUi.kt").readText()
        assertTrue(paged.contains("fun ReferencePdfPagedReader("))
        assertTrue(paged.contains("navSearchPdf(file, requested)"))
        assertTrue(paged.contains("referencePdfRectPixels("))
        assertTrue(native.contains("NAV_NATIVE_PDF_QA_APPROVED = false"))
        assertTrue(continuous.contains("ReferencePdfPagedReader(file, p, lang)"))
    }

    @Test fun newViewerUsesLazyFrameworkPagesWithExplicitMemoryCeiling() {
        val source = src.resolve("ReferenceContinuousPdfUi.kt").readText()
        assertTrue(source.contains("LazyColumn("))
        assertTrue(source.contains("items(count = count, key = { it })"))
        assertTrue(source.contains("maxPixels = 2_400_000L"))
        assertTrue(source.contains("ParcelFileDescriptor.MODE_READ_ONLY"))
        assertFalse(source.contains("androidx.pdf"))
        val page = referencePdfPixelSize(595, 842, 4000, maxPixels = 2_400_000L)
        assertTrue(page.width.toLong() * page.height <= 2_400_000L)
        assertTrue(page.width > 0 && page.height > 0)
    }

    @Test fun existingPdfDefaultStillGetsItsOwnMemoryBudget() {
        assertEquals(
            referencePdfPixelSize(595, 842, 2400),
            referencePdfPixelSize(595, 842, 2400, maxPixels = 7_000_000L),
        )
    }
}
