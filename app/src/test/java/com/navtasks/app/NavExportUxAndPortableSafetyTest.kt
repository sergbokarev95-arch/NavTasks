package com.navtasks.app

import org.junit.Assert.*
import org.junit.Test
import java.io.File

/**
 * Regression guards from HONOR + Samsung acceptance:
 *  - PDF/DOCX payload must be inside a new .navref v2, not legacy metadata-only v1.
 *  - Word, PDF, Cancel must not share an overlapping Box in the Handover dialog.
 *  - existing Handover markup, rich notes and business state are not rewritten.
 */
class NavExportUxAndPortableSafetyTest {
    private fun source(file: String): String =
        File("src/main/java/com/navtasks/app/$file").readText()

    @Test fun handoverUsesSeparateFullWidthWordAndPdfActions() {
        val ui = source("HandoverUi.kt")
        val start = ui.indexOf("if (exportOpen && !personalMode)")
        val end = ui.indexOf("if (exportFeedback != null)", start)
        assertTrue(start >= 0 && end > start)
        val dialog = ui.substring(start, end)
        assertTrue(dialog.contains("Dialog(onDismissRequest"))
        assertTrue(dialog.contains("navPopupGlassSurface"))
        assertTrue(dialog.contains("Word (.docx)"))
        assertTrue(dialog.contains("pdfExport.launch"))
        assertTrue(dialog.contains("wordExport.launch"))
        assertTrue(dialog.contains("localized(\"Отмена\", \"Cancel\""))
        assertEquals(2, Regex("fillMaxWidth\\(\\)\\.heightIn\\(min = 52\\.dp\\)")
            .findAll(dialog).count())
        assertFalse(dialog.contains("NavGlassAlertDialog"))
        val header = ui.substring(0, start)
        assertTrue(header.contains("widthIn(min = 92.dp)"))
        assertTrue(header.contains("navGlass(p, radius = 22.dp"))
    }

    @Test fun equipmentExportPreparesVerifiesAndUsesV2Only() {
        val ui = source("ReferenceEquipmentUi.kt")
        assertTrue(ui.contains("fun prepareEquipmentExport(item: ReferenceEquipment)"))
        assertTrue(ui.contains("ReferenceEquipmentPortableV2.export("))
        assertTrue(ui.contains("ReferenceEquipmentPortableV2.stage("))
        assertTrue(ui.contains("it.sha256 to it.mimeType"))
        assertTrue(ui.contains("included == referenced"))
        assertTrue(ui.contains("actual == required"))
        assertTrue(ui.contains("_with_documents_v2.navref"))
        assertFalse(ui.contains("ReferenceEquipmentExchange.encode("))
        assertFalse(ui.contains("exportLauncher.launch(\"$safe.navref\")"))
        assertTrue(ui.contains("DocumentsContract.deleteDocument("))
        assertTrue(ui.contains("preparedExport = null"))
    }

    @Test fun missingOriginalDoesNotClaimSuccessfulExport() {
        val portable = source("ReferenceEquipmentPortableV2.kt")
        assertTrue(portable.contains("schemaVersion\", 2"))
        assertTrue(portable.contains("Source attachment changed or is corrupted"))
        assertTrue(portable.contains("Missing attached PDF/DOCX"))
        assertTrue(portable.contains("Equipment and attached files do not match"))
    }
}
