package com.navtasks.app
import java.io.File
import org.junit.Assert.*
import org.junit.Test
class NavHandoverRedesignTest {
    @Test fun exportIsPinnedAndUsesAllSections() {
        val ui = File("src/main/java/com/navtasks/app/HandoverUi.kt").readText()
        assertFalse(ui.contains("NavInfoSectionBanner("))
        assertFalse(ui.contains("item(key = \"handover-export\")"))
        assertTrue(ui.contains("handover.sections.size"))
        assertTrue(ui.contains("HandoverExportAction(p, lang"))
        assertTrue(ui.indexOf("HandoverExportAction(p, lang") < ui.indexOf("LazyColumn("))
        assertTrue(ui.contains("NavEventDivider(p)"))
        assertTrue(ui.contains("highContrastField = !personalMode"))
        assertEquals(2, Regex("exportSnapshot = handover.sections.toList").findAll(ui).count())
        assertTrue(ui.contains("wordExport.launch("))
        assertTrue(ui.contains("pdfExport.launch("))
        assertTrue(ui.contains("handoverNotebookEntries(section.entries)"))
        assertTrue(ui.contains("handover.saveEntry(sectionId, savedTitle, plain, entryId, html, photos)"))
    }
    @Test fun vesselNotesUseCoralDirtySaveWithoutChangingPhotoHandling() {
        val ui = File("src/main/java/com/navtasks/app/HandoverFullNoteEditor.kt").readText()
        assertTrue(ui.contains("(personalMode || dirty)"))
        assertTrue(ui.contains("p, canSave, accent = true"))
        assertTrue(ui.contains("amosCommentField = !personalMode"))
        assertTrue(ui.contains("onInitialized ="))
        assertTrue(ui.contains("photos != initialPhotos"))
        assertTrue(ui.contains("HandoverPhotoCodec.fromUri(context, uri)"))
        assertTrue(ui.contains("onSave(title, plain, html, photos)"))
    }
}
