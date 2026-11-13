package com.navtasks.app

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReferenceProcedureAttachmentTest {
    private val digest = "a".repeat(64)
    private fun baseline() = ReferenceProcedure(
        id = "procedure-radar",
        title = "Radar check",
        intervalDescription = "1 month",
        excerpt = "Check equipment as described in manual",
        source = ReferenceSource(documentTitle = "Maker Manual", page = 250),
        instructions = "Record results",
    )

    @Test fun legacyProcedureWithoutAttachmentStillLoads() {
        val raw = baseline().toJson()
        raw.remove("attachmentSha256")
        raw.remove("attachmentMimeType")
        raw.remove("attachmentFilename")
        assertEquals(baseline(), ReferenceProcedure.fromJson(raw))
    }

    @Test fun attachedPdfSurvivesProcedureJsonAndEquipmentArchive() {
        val attached = baseline().copy(
            attachmentSha256 = digest, attachmentMimeType = ReferenceManualMime.PDF,
            attachmentFilename = "Safety manual.pdf",
        )
        assertEquals(attached, ReferenceProcedure.fromJson(attached.toJson()))
        val equipment = ReferenceEquipment(
            id = "nav-radar", title = "Radar", procedures = listOf(attached),
        )
        val portable = ReferenceEquipmentExchange.decode(
            ReferenceEquipmentExchange.encode(equipment))
        assertEquals(digest, portable.procedures.single().attachmentSha256)
        assertEquals("Safety manual.pdf", portable.procedures.single().attachmentFilename)
        assertEquals("Maker Manual", portable.procedures.single().source.documentTitle)
    }

    @Test fun attachmentValidationRejectsPartialOrUnsafeReferences() {
        assertTrue(runCatching {
            baseline().copy(attachmentSha256 = digest).validate()
        }.isFailure)
        assertTrue(runCatching {
            baseline().copy(attachmentMimeType = ReferenceManualMime.PDF).validate()
        }.isFailure)
        assertTrue(runCatching {
            baseline().copy(attachmentSha256 = "abc",
                attachmentMimeType = ReferenceManualMime.PDF,
                attachmentFilename = "manual.pdf").validate()
        }.isFailure)
        assertTrue(runCatching {
            baseline().copy(attachmentSha256 = digest,
                attachmentMimeType = ReferenceManualMime.PDF,
                attachmentFilename = "../manual.pdf").validate()
        }.isFailure)
    }

    @Test fun contentHashSurvivesDocumentIdRemappingDuringLibraryRestore() {
        val document = ReferenceManualDocument(id = "original-id",
            title = "PDF source", originalFilename = "manual.pdf",
            mimeType = ReferenceManualMime.PDF, sha256 = digest, bytes = 100)
        val original = ReferenceManualCatalog(documents = listOf(document))
        val copied = referenceMergeManualCatalogCopies(
            ReferenceManualCatalog(), original)
        assertEquals(digest, copied.documents.single().sha256)
        assertFalse(document.id == copied.documents.single().id)
        assertTrue(copied.documents.any { it.sha256 == digest })
    }
}
