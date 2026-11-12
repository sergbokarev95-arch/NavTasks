package com.navtasks.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class ReferenceManualArchiveTest {
    private fun digest(data: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(data)
            .joinToString("") { "%02x".format(it.toInt() and 0xFF) }

    private fun pdf(): ByteArray = "%PDF-1.7\n1 0 obj\n<< /Type /Catalog >>\n".toByteArray()

    private fun docx(): ByteArray {
        val out = ByteArrayOutputStream()
        ZipOutputStream(out).use { zip ->
            for (part in listOf("[Content_Types].xml", "word/document.xml")) {
                zip.putNextEntry(ZipEntry(part))
                zip.write("<xml/>".toByteArray())
                zip.closeEntry()
            }
        }
        return out.toByteArray()
    }

    @Test fun shareAllOriginalDocumentsWithoutReformattingOrTaskData() {
        val root = java.nio.file.Files.createTempDirectory("navref-manual-").toFile()
        try {
            val pdf = pdf()
            val docx = docx()
            val pdfRef = ReferenceManualDocument(
                id = "sire-doc", title = "SIRE 2.0", sectionId = NavReferenceSections.SIRE,
                folderId = "sire-chapter", originalFilename = "SIRE.pdf",
                mimeType = ReferenceManualMime.PDF, sha256 = digest(pdf), bytes = pdf.size.toLong(),
            )
            val docxRef = ReferenceManualDocument(
                id = "private-doc", title = "User notes", originalFilename = "note.docx",
                mimeType = ReferenceManualMime.DOCX, sha256 = digest(docx),
                bytes = docx.size.toLong(),
            )
            val catalog = ReferenceManualCatalog(
                folders = listOf(ReferenceManualFolder(
                    "sire-chapter", "Navigation", NavReferenceSections.SIRE)),
                documents = listOf(pdfRef, docxRef),
            )
            val pdfFile = File(root, "pdf-original").apply { writeBytes(pdf) }
            val docxFile = File(root, "docx-original").apply { writeBytes(docx) }
            val output = ByteArrayOutputStream()
            ReferenceManualArchive.export(catalog, {
                if (it.id == pdfRef.id) pdfFile else docxFile
            }, output)
            val imported = ReferenceManualArchive.stage(
                ByteArrayInputStream(output.toByteArray()), root)
            imported.use {
                assertEquals(catalog, it.catalog)
                assertTrue(it.originals.getValue(
                    ReferenceManualArchive.blobName(pdfRef)).readBytes().contentEquals(pdf))
                assertTrue(it.originals.getValue(
                    ReferenceManualArchive.blobName(docxRef)).readBytes().contentEquals(docx))
            }
            val merged = referenceMergeManualCatalogCopies(catalog, catalog)
            assertEquals(4, merged.documents.size)
            assertEquals(2, merged.folders.size)
            assertEquals(4, merged.documents.map { it.id }.toSet().size)
            assertEquals(2, merged.documents.filter { it.sectionId == NavReferenceSections.SIRE }
                .map { it.folderId }.toSet().size)
            assertFalse(merged.toJson().toString().contains("taskId"))
        } finally { root.deleteRecursively() }
    }

    private fun writeArchive(entries: List<Pair<String, ByteArray>>): ByteArray {
        val out = ByteArrayOutputStream()
        ZipOutputStream(out).use { zip ->
            for ((name, body) in entries) {
                zip.putNextEntry(ZipEntry(name))
                zip.write(body)
                zip.closeEntry()
            }
        }
        return out.toByteArray()
    }

    @Test fun missingOrForgedOriginalIsRejectedAndStagingRemoved() {
        val root = java.nio.file.Files.createTempDirectory("navref-fraud-").toFile()
        try {
            val data = pdf()
            val doc = ReferenceManualDocument(
                id = "doc", title = "Manual", originalFilename = "manual.pdf",
                mimeType = ReferenceManualMime.PDF, sha256 = digest(data), bytes = data.size.toLong(),
            )
            val catalog = ReferenceManualCatalog(documents = listOf(doc))
            val metadata = catalog.toJson().toString().toByteArray()
            assertTrue(runCatching {
                ReferenceManualArchive.stage(ByteArrayInputStream(writeArchive(
                    listOf("manuals.json" to metadata))), root)
            }.isFailure)
            assertTrue(runCatching {
                ReferenceManualArchive.stage(ByteArrayInputStream(writeArchive(
                    listOf(
                        "manuals.json" to metadata,
                        ReferenceManualArchive.blobName(doc) to "NOT A PDF".toByteArray(),
                    ))), root)
            }.isFailure)
            assertTrue(runCatching {
                ReferenceManualArchive.stage(ByteArrayInputStream(writeArchive(
                    listOf("manuals.json" to metadata,
                        "../outside.txt" to data))), root)
            }.isFailure)
            assertTrue(root.listFiles().orEmpty().isEmpty())
        } finally { root.deleteRecursively() }
    }

    @Test fun abandonedStagingCleanupKeepsRecentAndUnrelatedFiles() {
        val root = java.nio.file.Files.createTempDirectory("navref-stage-cleanup-").toFile()
        try {
            val now = 1_900_000_000_000L
            val old = File(root, "reference-manuals-old.staging").apply {
                mkdirs()
                File(this, "temporary.pdf").writeText("orphaned")
                setLastModified(now - 8L * 24 * 60 * 60 * 1000)
            }
            val active = File(root, "reference-manuals-active.staging").apply {
                mkdirs()
                File(this, "temporary.pdf").writeText("keep")
                setLastModified(now - 60_000)
            }
            val unrelated = File(root, "important-user-data").apply {
                mkdirs()
                setLastModified(now - 12L * 24 * 60 * 60 * 1000)
            }
            assertEquals(1, ReferenceManualArchive.cleanupAbandonedStages(root, now))
            assertFalse(old.exists())
            assertTrue(active.isDirectory)
            assertTrue(File(active, "temporary.pdf").exists())
            assertTrue(unrelated.isDirectory)
        } finally {
            root.deleteRecursively()
        }
    }

    @Test fun wrongZipEntryOrderOrUnexpectedFilesFailClosed() {
        val root = java.nio.file.Files.createTempDirectory("navref-order-").toFile()
        try {
            assertTrue(runCatching {
                ReferenceManualArchive.stage(ByteArrayInputStream(writeArchive(
                    listOf("arbitrary.txt" to "wrong".toByteArray()))), root)
            }.isFailure)
            val emptyCatalog = ReferenceManualCatalog().toJson().toString().toByteArray()
            assertTrue(runCatching {
                ReferenceManualArchive.stage(ByteArrayInputStream(writeArchive(
                    listOf("manuals.json" to emptyCatalog,
                        "unexpected.pdf" to pdf()))), root)
            }.isFailure)
        } finally { root.deleteRecursively() }
    }
}
