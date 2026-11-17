package com.navtasks.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Guard against accidentally regressing to rc14, where upstream #730/#731
 * broke Android long-press selection handle dragging.
 * The actual gesture must also be manually tested on HONOR and Samsung.
 */
class NavSelectionStabilityTest {
    private val gradle = File("build.gradle.kts")
    private val editor = File("src/main/java/com/navtasks/app/NavRichNoteUi.kt")
    private val handover = File("src/main/java/com/navtasks/app/HandoverFullNoteEditor.kt")

    @Test fun stableLibraryIncludesUpstreamSelectionHandleFix() {
        val config = gradle.readText()
        assertTrue(config.contains("com.mohamedrejeb.richeditor:richeditor-compose:1.0.0\""))
        assertFalse(config.contains("richeditor-compose:1.0.0-rc14"))
    }

    @Test fun allRichNotesStillUseSingleOriginalRichTextStateAndHtmlSave() {
        val ui = editor.readText()
        assertTrue(ui.contains("rememberRichTextState()"))
        assertTrue(ui.contains("RichTextEditor("))
        assertTrue(ui.contains("state.setHtml(initialHtml.takeIf"))
        assertTrue(ui.contains("onChange(state.annotatedString.text, state.toHtml())"))
        assertTrue(ui.contains("state.toggleSpanStyle("))
        assertFalse(ui.contains("pointerInput("))
        assertFalse(ui.contains("detectDragGestures("))
        assertFalse(ui.contains("SelectionContainer {"))
        val full = handover.readText()
        assertTrue(full.contains("NavRichNoteEditor("))
        assertTrue(full.contains("onSave(title, plain, html, photos)"))
    }
}
