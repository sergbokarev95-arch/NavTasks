package com.navtasks.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ReferenceDocxReaderTest {
    private fun makeDocx(body: String): File {
        val file = File.createTempFile("navtasks-docx-", ".docx")
        ZipOutputStream(file.outputStream()).use { zip ->
            for ((part, bytes) in listOf(
                "[Content_Types].xml" to "<Types/>",
                "word/document.xml" to body,
            )) {
                zip.putNextEntry(ZipEntry(part))
                zip.write(bytes.toByteArray(Charsets.UTF_8))
                zip.closeEntry()
            }
        }
        return file
    }

    @Test fun preservesWordParagraphsAndHeadingsOffline() {
        val file = makeDocx(
            """<?xml version="1.0" encoding="UTF-8"?>
            <w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
              <w:body>
                <w:p><w:pPr><w:pStyle w:val="Heading1"/></w:pPr>
                    <w:r><w:t>SIRE 2.0</w:t></w:r></w:p>
                <w:p><w:r><w:t>First paragraph</w:t></w:r>
                    <w:r><w:tab/><w:t>continued</w:t></w:r></w:p>
                <w:p><w:r><w:t>Второй абзац</w:t></w:r></w:p>
              </w:body>
            </w:document>""".trimIndent())
        try {
            val lines = extractReferenceDocxText(file)
            assertEquals(3, lines.size)
            assertEquals("SIRE 2.0", lines[0].text)
            assertTrue(lines[0].heading)
            assertEquals("First paragraph    continued", lines[1].text)
            assertEquals("Второй абзац", lines[2].text)
        } finally { file.delete() }
    }

    @Test fun unsafeExternalDoctypeIsRejected() {
        val file = makeDocx(
            """<?xml version="1.0"?>
               <!DOCTYPE foo [<!ENTITY xxe SYSTEM "file:///etc/passwd">]>
               <w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
                 <w:body><w:p><w:r><w:t>&xxe;</w:t></w:r></w:p></w:body>
               </w:document>""".trimIndent())
        try {
            assertTrue(runCatching { extractReferenceDocxText(file) }.isFailure)
        } finally { file.delete() }
    }
}
