package com.navtasks.app

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReferenceManualModelsTest {
    private fun sire(): ReferenceManualDocument = ReferenceManualDocument(
        id = "sire-pdf",
        title = "SIRE 2.0",
        sectionId = NavReferenceSections.SIRE,
        folderId = "sire-chapter",
        originalFilename = "SIRE_2.0.pdf",
        mimeType = ReferenceManualMime.PDF,
        sha256 = "a".repeat(64),
        bytes = 25_000_000L,
        notes = "Inspection questions and vessel guidance",
    )

    @Test fun nestedSharedManualsRoundTripAndKeepSections() {
        val original = ReferenceManualCatalog(
            folders = listOf(
                ReferenceManualFolder("sire-root", "Inspection", NavReferenceSections.SIRE),
                ReferenceManualFolder("sire-chapter", "Navigation",
                    NavReferenceSections.SIRE, "sire-root"),
                ReferenceManualFolder("own-root", "My documents", NavReferenceSections.PERSONAL),
            ),
            documents = listOf(sire(), ReferenceManualDocument(
                id = "private-doc", title = "Device instructions",
                folderId = "own-root", originalFilename = "device.docx",
                mimeType = ReferenceManualMime.DOCX, sha256 = "b".repeat(64),
                bytes = 1_000_000L,
            )),
        )
        val parsed = ReferenceManualCatalog.fromJson(JSONObject(original.toJson().toString()))
        assertEquals(original, parsed)
        assertEquals(listOf(sire()), referenceManualSearch(original, "inspection"))
        assertEquals(listOf(sire()), referenceManualSearch(original, "SIRE"))
        for (forbidden in listOf("taskId", "workOrderId", "serviceId", "deadline", "notification")) {
            assertFalse(parsed.toJson().toString().contains(forbidden))
        }
    }

    @Test fun forgedPathsAndUnsupportedMimeAreRejected() {
        assertTrue(runCatching { sire().copy(originalFilename = "../secret.pdf").validate() }.isFailure)
        assertTrue(runCatching { sire().copy(originalFilename = "abc\\temp.pdf").validate() }.isFailure)
        assertTrue(runCatching { sire().copy(sha256 = "bad").validate() }.isFailure)
        assertTrue(runCatching { sire().copy(mimeType = "application/msword").validate() }.isFailure)
        assertTrue(runCatching { sire().copy(bytes = 0).validate() }.isFailure)
        assertTrue(runCatching { sire().copy(bytes = 250_000_001).validate() }.isFailure)
    }

    @Test fun brokenHierarchyAndCrossSectionMovesAreRejected() {
        val folder = ReferenceManualFolder("a", "A", NavReferenceSections.SIRE, "b")
        val other = ReferenceManualFolder("b", "B", NavReferenceSections.SIRE, "a")
        assertTrue(runCatching { ReferenceManualCatalog(
            folders = listOf(folder, other)).validate() }.isFailure)

        val cross = ReferenceManualCatalog(
            folders = listOf(ReferenceManualFolder("own", "Own", NavReferenceSections.PERSONAL)),
            documents = listOf(sire().copy(folderId = "own")),
        )
        assertTrue(runCatching { cross.validate() }.isFailure)
    }

    @Test fun schemaDoesNotAcceptAdditionalTaskFields() {
        val original = ReferenceManualCatalog(
            folders = listOf(ReferenceManualFolder("sire-chapter", "Navigation",
                NavReferenceSections.SIRE)),
            documents = listOf(sire()),
        )
        val json = original.toJson()
        json.put("taskIds", "forbidden")
        assertTrue(runCatching { ReferenceManualCatalog.fromJson(json) }.isFailure)
        val forged = sire().toJson().put("notificationInterval", 30)
        assertTrue(runCatching { ReferenceManualDocument.fromJson(forged) }.isFailure)
    }
}
