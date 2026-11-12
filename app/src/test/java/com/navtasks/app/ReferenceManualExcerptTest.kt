package com.navtasks.app

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReferenceManualExcerptTest {
    private val excerpt = ReferenceManualExcerpt(
        id = "sire-note", title = "ECDIS inspection", text = "Проверить резервный ECDIS",
        sectionId = NavReferenceSections.SIRE,
        folderId = "nav",
        sourceTitle = "SIRE 2.0", sourceSection = "Navigation",
        sourcePage = 40, sourceEdition = "2026",
    )

    private fun fullCatalog() = ReferenceManualCatalog(
        folders = listOf(ReferenceManualFolder(
            id = "nav", title = "Navigation", sectionId = NavReferenceSections.SIRE)),
        excerpts = listOf(excerpt),
    )

    @Test fun excerptRoundtripRetainsCitationAndSupportsTextSearch() {
        val archive = fullCatalog().toJson().toString()
        val restored = ReferenceManualCatalog.fromJson(JSONObject(archive))
        assertEquals(fullCatalog(), restored)
        assertEquals("SIRE 2.0", restored.excerpts.single().sourceTitle)
        assertEquals(40, restored.excerpts.single().sourcePage)
        assertTrue(referenceExcerptMatches(excerpt, "РЕЗЕРВНЫЙ"))
        assertTrue(referenceExcerptMatches(excerpt, "navigation"))
        assertFalse(referenceExcerptMatches(excerpt, "nonexistent"))
        for (forbidden in listOf("taskId", "serviceId", "workOrderId", "deadline", "notification")) {
            assertFalse(archive.contains(forbidden))
        }
    }

    @Test fun olderCatalogWithoutExcerptsStillLoads() {
        val json = JSONObject()
            .put("format", "navtasks-reference-manuals")
            .put("schemaVersion", 1)
            .put("folders", org.json.JSONArray())
            .put("documents", org.json.JSONArray())
        assertTrue(ReferenceManualCatalog.fromJson(json).excerpts.isEmpty())
        json.put("taskId", "forbidden")
        assertTrue(runCatching { ReferenceManualCatalog.fromJson(json) }.isFailure)
    }

    @Test fun copyRestoreRemapsExcerptFolderWithoutChangingOriginal() {
        val restored = referenceMergeManualCatalogCopies(
            fullCatalog(), fullCatalog())
        assertEquals(2, restored.folders.size)
        assertEquals(2, restored.excerpts.size)
        assertEquals(excerpt, restored.excerpts.first())
        assertTrue(restored.excerpts.last().id != excerpt.id)
        assertTrue(restored.excerpts.last().folderId != excerpt.folderId)
        assertEquals("Navigation", restored.folders.single {
            it.id == restored.excerpts.last().folderId
        }.title)
    }

    @Test fun emptyExcerptAndCrossSectionFolderAreRejected() {
        assertTrue(runCatching { excerpt.copy(text = "").validate() }.isFailure)
        assertTrue(runCatching { excerpt.copy(sourcePage = -1).validate() }.isFailure)
        assertTrue(runCatching {
            fullCatalog().copy(excerpts = listOf(
                excerpt.copy(sectionId = NavReferenceSections.PERSONAL)
            )).validate()
        }.isFailure)
        assertTrue(runCatching {
            ReferenceManualExcerpt.fromJson(excerpt.toJson().put("taskId", "no"))
        }.isFailure)
    }

    @Test fun sharedManualArchiveSerializesExcerptsWithoutAnyOriginalFiles() {
        val catalog = fullCatalog()
        val output = java.io.ByteArrayOutputStream()
        ReferenceManualArchive.export(catalog, { error("Should not open nonexistent files") },
            output)
        val root = java.nio.file.Files.createTempDirectory("navref-excerpt-test").toFile()
        try {
            ReferenceManualArchive.stage(output.toByteArray().inputStream(), root).use { result ->
                assertEquals(catalog.excerpts, result.catalog.excerpts)
                assertTrue(result.originals.isEmpty())
            }
        } finally { root.deleteRecursively() }
    }
}
