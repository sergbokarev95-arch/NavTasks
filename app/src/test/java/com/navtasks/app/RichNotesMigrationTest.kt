package com.navtasks.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class RichNotesMigrationTest {
    private val day = LocalDate.of(2026, 11, 8)
    private val oldDay = day.minusDays(2)

    @Test fun legacyNotesRemainPlainAndHaveNoExtraHtml() {
        val old = CheckItem(title = "GMDSS", comment = "Record test result")
        assertEquals("", old.commentHtml)
        assertEquals("Record test result", old.comment)
        assertEquals("Record test result", CheckItem.fromJson(old.toJson()).comment)
        assertEquals("", CheckItem.fromJson(old.toJson()).commentHtml)
    }

    @Test fun checklistHtmlAndPlainTextRoundTripTogether() {
        val old = CheckItem(title = "Daily Test", comment = "Radio check",
            commentHtml = "<p><b>Radio</b> check</p>")
        val after = CheckItem.fromJson(old.toJson())
        assertEquals(old.comment, after.comment)
        assertEquals(old.commentHtml, after.commentHtml)
    }

    @Test fun serviceKeepsPlainHistoryAndRichFormattingAfterCompletion() {
        val old = ServiceEntry(equipment = "Radar", lastDate = oldDay,
            nextDate = day.plusMonths(6), periodMonths = 6,
            comment = "Inspect antenna", commentHtml = "<p><b>Inspect</b> antenna</p>")
        val after = completeServiceCycle(old, day)
        assertEquals(old.commentHtml, after.commentHtml)
        assertEquals(old.comment, after.serviceHistory.single().comment)
        assertEquals(old.commentHtml, after.serviceHistory.single().commentHtml)
        val restored = ServiceEntry.fromJson(after.toJson())
        assertEquals(old.commentHtml, restored.commentHtml)
        assertEquals(old.commentHtml, restored.serviceHistory.single().commentHtml)
    }

    @Test fun amosAndHandoverStyledNotesSurviveFullJsonRoundTrip() {
        val work = WorkOrderEntry(title = "Radar check", dueDate = day,
            comment = "Calibration required", commentHtml = "<p><u>Calibration</u> required</p>")
        assertEquals(work.commentHtml, WorkOrderEntry.fromJson(work.toJson()).commentHtml)
        val note = HandoverEntry(title = "ECDIS handover", notes = "License due",
            notesHtml = "<p><i>License</i> due</p>")
        assertEquals(note.notesHtml, HandoverEntry.fromJson(note.toJson()).notesHtml)
        assertEquals(note.notes, HandoverEntry.fromJson(note.toJson()).notes)
    }

    @Test fun editorInputEscapesHtmlAndRespectsBothLimits() {
        assertEquals("<p>Compass &amp; radar</p>", navEscapeNoteHtml("Compass & radar"))
        assertEquals("<p>&lt;test&gt;</p>", navEscapeNoteHtml("<test>"))
        assertTrue(navRichNoteAllowed("Log", "<p>Log</p>", 4000))
        assertFalse(navRichNoteAllowed("ABCDE", "<p>ABCDE</p>", 4))
        assertFalse(navRichNoteAllowed("A", "X".repeat(24001), 4000))
    }
}
