package com.navtasks.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * QA3 HONOR incident: AndroidX PDF beta Activity crashed when opening
 * both library PDFs and equipment procedure attachments. Keep old reader
 * available until a real device stack trace and regression QA.
 */
class ReferencePdfSafetyGateTest {
    @Test fun betaNativeReaderIsQuarantinedUntilPhysicalAcceptance() {
        assertFalse(NAV_NATIVE_PDF_QA_APPROVED)
    }

    @Test fun bothReferenceFlowsHaveAnExistingInAppFallback() {
        val source = java.io.File("src/main/java/com/navtasks/app")
        val library = source.resolve("ReferenceManualLibraryUi.kt").readText()
        val equipment = source.resolve("ReferenceEquipmentUi.kt").readText()
        assertTrue(library.contains("navNativePdfSupported()"))
        assertTrue(library.contains("else documentId = doc.id"))
        assertTrue(equipment.contains("navNativePdfSupported()"))
        assertTrue(equipment.contains("else attachmentPreview = manual"))
    }
}
