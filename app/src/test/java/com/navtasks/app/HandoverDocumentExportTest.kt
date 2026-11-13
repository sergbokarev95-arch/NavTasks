package com.navtasks.app

import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.util.zip.ZipInputStream
import javax.xml.parsers.DocumentBuilderFactory

class HandoverDocumentExportTest {
    private val sample = listOf(HandoverSection(title = "Navigation",
        entries = listOf(
            HandoverEntry(title = "ECDIS", notes = "Primar & Unio <charts>\nNext correction",
                mark = HandoverMark.COMPLETED,
                completedDate = LocalDate.of(2026, 11, 14)),
            HandoverEntry(title = "Radar X-band", notes = "Check antenna"),
        )))

    @Test fun wordDocumentHasAllSectionsNotesAndValidXml() {
        val bytes = ByteArrayOutputStream().also {
            HandoverDocxExport.export(sample, it)
        }.toByteArray()
        val parts = mutableMapOf<String, ByteArray>()
        ZipInputStream(bytes.inputStream()).use { zip ->
            while (true) {
                val e = zip.nextEntry ?: break
                parts[e.name] = zip.readBytes()
                zip.closeEntry()
            }
        }
        for (name in listOf("[Content_Types].xml", "_rels/.rels",
            "word/_rels/document.xml.rels", "word/styles.xml", "word/document.xml")) {
            assertTrue("Missing $name", name in parts)
            DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(requireNotNull(parts[name]).inputStream())
        }
        val text = parts.getValue("word/document.xml").toString(Charsets.UTF_8)
        assertTrue(text.contains("Navigation"))
        assertTrue(text.contains("ECDIS"))
        assertTrue(text.contains("Primar &amp; Unio &lt;charts&gt;"))
        assertTrue(text.contains("Next correction"))
        assertTrue(text.contains("Completed: 2026-11-14"))
        assertTrue(text.contains("Radar X-band"))
        assertFalse(text.contains("taskId"))
    }

    @Test fun handoverDocumentPreviewNeverChangesSourceNotes() {
        val copy = sample.map { it.copy(entries = it.entries.toList()) }
        val lines = handoverExportLines(sample, LocalDate.of(2026, 11, 14))
        assertEquals("Handover", lines.first().text)
        assertTrue(lines.any { it.text == "Check antenna" })
        assertTrue(lines.any { it.text == "Next correction" })
        assertEquals(copy, sample)
        assertTrue(java.io.File("src/main/java/com/navtasks/app/HandoverPdfExport.kt")
            .readText().contains("PdfDocument()"))
    }
}
