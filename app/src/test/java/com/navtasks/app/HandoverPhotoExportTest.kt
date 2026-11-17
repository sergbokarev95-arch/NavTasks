package com.navtasks.app

import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.ZipInputStream
import javax.xml.parsers.DocumentBuilderFactory

class HandoverPhotoExportTest {
    private val photo = HandoverPhoto(id = "sample", jpegBase64 = "/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAAoHBwgHBgoICAgLCgoLDhgQDg0NDh0VFhEYIx8lJCIfIiEmKzcvJik0KSEiMEExNDk7Pj4+JS5ESUM8SDc9Pjv/2wBDAQoLCw4NDhwQEBw7KCIoOzs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozv/wAARCAADAAQDASIAAhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL/8QAtRAAAgEDAwIEAwUFBAQAAAF9AQIDAAQRBRIhMUEGE1FhByJxFDKBkaEII0KxwRVS0fAkM2JyggkKFhcYGRolJicoKSo0NTY3ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqDhIWGh4iJipKTlJWWl5iZmqKjpKWmp6ipqrKztLW2t7i5usLDxMXGx8jJytLT1NXW19jZ2uHi4+Tl5ufo6erx8vP09fb3+Pn6/8QAHwEAAwEBAQEBAQEBAQAAAAAAAAECAwQFBgcICQoL/8QAtREAAgECBAQDBAcFBAQAAQJ3AAECAxEEBSExBhJBUQdhcRMiMoEIFEKRobHBCSMzUvAVYnLRChYkNOEl8RcYGRomJygpKjU2Nzg5OkNERUZHSElKU1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6goOEhYaHiImKkpOUlZaXmJmaoqOkpaanqKmqsrO0tba3uLm6wsPExcbHyMnK0tPU1dbX2Nna4uPk5ebn6Onq8vP09fb3+Pn6/9oADAMBAAIRAxEAPwDVooor4M+6P//Z",
        widthPx = 4, heightPx = 3)

    @Test fun photoBelongsToExactlyItsOwnEntryAndAppearsAfterNotes() {
        val sections = listOf(HandoverSection(title = "Navigation", entries = listOf(
            HandoverEntry(title = "ECDIS", notes = "Chart corrections", photos = listOf(photo)),
            HandoverEntry(title = "Radar", notes = "Antenna maintenance")
        )))
        val output = handoverExportLines(sections)
        val idxPhoto = output.indexOfFirst { it.kind == HandoverExportKind.PHOTO }
        val idxNote = output.indexOfFirst { it.text == "Chart corrections" }
        val idxRadar = output.indexOfFirst { it.text == "Radar" }
        assertTrue(idxNote in 0 until idxPhoto)
        assertTrue(idxPhoto < idxRadar)
        assertEquals(1, output.count { it.kind == HandoverExportKind.PHOTO })
        assertEquals("sample", output[idxPhoto].photo?.id)
    }

    @Test fun docxContainsRealEmbeddedJpegUnderCorrectHeading() {
        val sections = listOf(HandoverSection(title = "Navigation", entries = listOf(
            HandoverEntry(title = "ECDIS", notes = "Chart corrections", photos = listOf(photo)),
            HandoverEntry(title = "Radar", notes = "Second note")
        )))
        val bytes = ByteArrayOutputStream().also {
            HandoverDocxExport.export(sections, it)
        }.toByteArray()
        val parts = mutableMapOf<String, ByteArray>()
        ZipInputStream(bytes.inputStream()).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                parts[entry.name] = zip.readBytes()
                zip.closeEntry()
            }
        }
        val embedded = parts["word/media/handover_1.jpg"]
        assertNotNull(embedded)
        assertArrayEquals(photo.jpegBytes(), embedded)
        for (name in listOf("[Content_Types].xml", "word/document.xml",
            "word/_rels/document.xml.rels")) {
            DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(parts.getValue(name).inputStream())
        }
        val doc = parts.getValue("word/document.xml").toString(Charsets.UTF_8)
        val rel = parts.getValue("word/_rels/document.xml.rels").toString(Charsets.UTF_8)
        assertTrue(doc.indexOf("Chart corrections") < doc.indexOf("<w:drawing>"))
        assertTrue(doc.indexOf("<w:drawing>") < doc.indexOf("Second note"))
        assertTrue(doc.contains("r:embed=\"rId2\""))
        assertTrue(rel.contains("Id=\"rId2\""))
        assertTrue(rel.contains("Target=\"media/handover_1.jpg\""))
    }

    @Test fun savedPhotoContractAndPdfRendererRemainIndependentOfOriginalUri() {
        val ui = File("src/main/java/com/navtasks/app/HandoverUi.kt").readText()
        val store = File("src/main/java/com/navtasks/app/HandoverStore.kt").readText()
        val pdf = File("src/main/java/com/navtasks/app/HandoverPdfExport.kt").readText()
        assertTrue(ui.contains("HandoverPhotoStrip(entry.photos, p, lang)"))
        assertTrue(ui.contains("initialPhotos = item?.photos.orEmpty()"))
        assertTrue(store.contains(".put(\"photos\", JSONArray()"))
        assertTrue(store.contains("photos = photos ?: entry?.photos.orEmpty()"))
        assertTrue(pdf.contains("HandoverExportKind.PHOTO ->"))
        assertTrue(pdf.contains("canvas.drawBitmap(bitmap, null"))
        assertFalse(File("src/main/java/com/navtasks/app/HandoverPhoto.kt").readText()
            .contains("takePersistableUriPermission"))
    }
}
