package com.navtasks.app

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ReferenceManualFileValidationTest {
    @Test fun pdfRequiresRealSignatureNotJustFilename() {
        val pdf = File.createTempFile("reference-pdf-", ".pdf")
        try {
            pdf.writeBytes("%PDF-1.7\n1 0 obj\n".toByteArray())
            validateReferenceManualSignature(pdf, ReferenceManualMime.PDF)
            pdf.writeBytes("This is a fake pdf".toByteArray())
            assertTrue(runCatching {
                validateReferenceManualSignature(pdf, ReferenceManualMime.PDF)
            }.isFailure)
        } finally { pdf.delete() }
    }

    private fun createDocx(entries: List<String>): File {
        val file = File.createTempFile("reference-docx-", ".docx")
        ZipOutputStream(file.outputStream()).use { zip ->
            entries.forEach { name ->
                zip.putNextEntry(ZipEntry(name))
                zip.write("<xml/>".toByteArray())
                zip.closeEntry()
            }
        }
        return file
    }

    @Test fun docxRequiresWordDocumentAndContentTypes() {
        val good = createDocx(listOf("[Content_Types].xml", "word/document.xml"))
        val bad = createDocx(listOf("[Content_Types].xml", "random-file.xml"))
        try {
            validateReferenceManualSignature(good, ReferenceManualMime.DOCX)
            assertTrue(runCatching {
                validateReferenceManualSignature(bad, ReferenceManualMime.DOCX)
            }.isFailure)
        } finally {
            good.delete()
            bad.delete()
        }
    }

    @Test fun unsafeDocxEntryNamesAreRejected() {
        val pathTraversal = createDocx(listOf(
            "[Content_Types].xml", "word/document.xml", "../bad.txt"))
        try {
            assertTrue(runCatching {
                validateReferenceManualSignature(pathTraversal, ReferenceManualMime.DOCX)
            }.isFailure)
        } finally { pathTraversal.delete() }
    }
}
