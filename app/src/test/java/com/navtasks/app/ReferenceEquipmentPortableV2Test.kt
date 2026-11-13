package com.navtasks.app

import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.security.MessageDigest
import java.nio.file.Files
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class ReferenceEquipmentPortableV2Test {
    private fun hash(bytes: ByteArray) = MessageDigest.getInstance("SHA-256")
        .digest(bytes).joinToString("") { "%02x".format(it.toInt() and 0xff) }
    private val pdf = "%PDF-1.4\n1 0 obj\n<< >>\nendobj\n%%EOF\n"
        .toByteArray(Charsets.US_ASCII)

    private fun sample() = ReferenceEquipment(
        title = "Radar X Band", manufacturer = "Transas",
        model = "NaviRadar 4000", location = "Bridge",
        procedures = listOf(ReferenceProcedure(
            title = "Radar check", intervalDescription = "1 month",
            excerpt = "Inspect radar", source = ReferenceSource("Maker manual", 250),
            instructions = "Visual test", attachmentSha256 = hash(pdf),
            attachmentMimeType = ReferenceManualMime.PDF,
            attachmentFilename = "A.916(22).pdf"))
    )
    private fun manual() = ReferenceManualDocument(
        title = "A.916(22)", originalFilename = "A.916(22).pdf",
        mimeType = ReferenceManualMime.PDF,
        sha256 = hash(pdf), bytes = pdf.size.toLong())

    @Test fun originalPdfIsInsideEquipmentArchiveAndVerifiedBeforeImport() {
        val temp = Files.createTempDirectory("navref-test").toFile()
        try {
            val original = File(temp, "original.pdf").apply { writeBytes(pdf) }
            val bytes = ByteArrayOutputStream().also { out ->
                ReferenceEquipmentPortableV2.export(
                    sample(), listOf(manual()), { original }, out)
            }.toByteArray()
            val entries = mutableListOf<String>()
            ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
                while (true) {
                    val entry = zip.nextEntry ?: break
                    entries += entry.name
                    zip.closeEntry()
                }
            }
            assertEquals(2, entries.size)
            assertEquals("manifest.json", entries.first())
            assertEquals("originals/" + hash(pdf) + ".pdf", entries.last())
            ReferenceEquipmentPortableV2.stage(bytes.inputStream(), temp).use { staged ->
                assertEquals(sample().title, staged.equipment.title)
                assertEquals("", staged.equipment.serialNumber)
                assertNull(staged.equipment.folderId)
                assertFalse(staged.legacyMissingAttachments)
                assertEquals(1, staged.originals.size)
                assertArrayEquals(pdf, staged.originals.single().file.readBytes())
            }
        } finally {
            temp.deleteRecursively()
        }
    }

    @Test fun oldV1WithAttachmentMetadataCanImportWithExplicitMissingWarning() {
        val temp = Files.createTempDirectory("navref-v1").toFile()
        try {
            val old = ReferenceEquipmentExchange.encode(sample())
            ReferenceEquipmentPortableV2.stage(old.inputStream(), temp).use { staged ->
                assertEquals("Radar X Band", staged.equipment.title)
                assertTrue(staged.originals.isEmpty())
                assertTrue(staged.legacyMissingAttachments)
            }
        } finally { temp.deleteRecursively() }
    }

    @Test fun missingOriginalCannotClaimSuccessfulExport() {
        val tmp = Files.createTempDirectory("navref-missing").toFile()
        try {
            assertTrue(runCatching {
                ReferenceEquipmentPortableV2.export(sample(), emptyList(),
                    { File(tmp, "missing") }, ByteArrayOutputStream())
            }.isFailure)
        } finally { tmp.deleteRecursively() }
    }

    @Test fun changedAttachedBytesOrUnknownZipEntryAreRejected() {
        val temp = Files.createTempDirectory("navref-tamper").toFile()
        try {
            val original = File(temp, "a.pdf").apply { writeBytes(pdf) }
            val clean = ByteArrayOutputStream().also {
                ReferenceEquipmentPortableV2.export(sample(), listOf(manual()),
                    { original }, it)
            }.toByteArray()
            val corrupt = ByteArrayOutputStream().use { dest ->
                ZipOutputStream(dest).use { zipOut ->
                    ZipInputStream(clean.inputStream()).use { zipIn ->
                        while (true) {
                            val entry = zipIn.nextEntry ?: break
                            val contents = zipIn.readBytes()
                            zipOut.putNextEntry(ZipEntry(entry.name))
                            val changed = if (entry.name.endsWith(".pdf"))
                                contents + 0x42.toByte() else contents
                            zipOut.write(changed)
                            zipOut.closeEntry()
                        }
                    }
                }
                dest.toByteArray()
            }
            assertTrue(runCatching {
                ReferenceEquipmentPortableV2.stage(corrupt.inputStream(), temp).close()
            }.isFailure)
        } finally { temp.deleteRecursively() }
    }
}
