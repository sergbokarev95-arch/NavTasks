package com.navtasks.app

import com.mohamedrejeb.richeditor.model.RichTextState
import androidx.compose.ui.text.TextRange
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Synthetic old stored HTML only. Do not use or overwrite any actual Handover notes.
 * Ensure a selection-only change does not destroy text or existing formatted spans.
 */
class NavRichEditorCompatGateTest {
    @Test fun oldRichTextNotesSurviveImportExportWithTextIntact() {
        val cases = listOf(
            "<p>ECDIS &amp; Navtex notes</p>",
            "<p>Первая строка с <strong>жирным</strong> текстом</p>",
            "<p>Радар: <em>курсив</em> и <u>подчёркивание</u></p>",
            "<p><span style=\"color: #0071e3\">Цветной текст</span></p>",
            "<p><span style=\"background-color: #ffd86b\">Маркер</span></p>",
        )
        for (html in cases) {
            val first = RichTextState()
            first.setHtml(html)
            val before = first.annotatedString.text
            val beforeStyles = first.annotatedString.spanStyles
            val stored = first.toHtml()
            val reloaded = RichTextState()
            reloaded.setHtml(stored)
            assertEquals("Round-trip changed text for $html", before, reloaded.annotatedString.text)
            assertTrue("Unexpectedly empty HTML after round trip", stored.isNotBlank())
            if (html.contains("<strong") || html.contains("<em") ||
                html.contains("<u>") || html.contains("<span")) {
                assertTrue("Formatted input lost all spans before saving: $html",
                    beforeStyles.isNotEmpty())
                assertEquals("Formatting changed on HTML round-trip: $html",
                    beforeStyles, reloaded.annotatedString.spanStyles)
            }
        }
    }

    @Test fun selectionHandleMovementMustNotLoseTextOrMarkup() {
        val note = RichTextState()
        note.setHtml("<p>Начало <strong>выделение</strong> конец</p>")
        val originalText = note.annotatedString.text
        val originalHtml = note.toHtml()
        val length = originalText.length
        note.selection = TextRange(1, length - 1)
        note.selection = TextRange(3, length - 1)
        note.selection = TextRange(0, length)
        assertEquals(originalText, note.annotatedString.text)
        assertEquals(originalHtml, note.toHtml())
    }
}
