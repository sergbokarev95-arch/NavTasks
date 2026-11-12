package com.navtasks.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HandoverAccordionTest {
    @Test fun everySectionStartsClosed() {
        assertNull(nextExpandedHandoverSection("deck", "deck"))
    }

    @Test fun openingAnotherSectionClosesThePreviousOne() {
        assertEquals("engine", nextExpandedHandoverSection("deck", "engine"))
        assertEquals("deck", nextExpandedHandoverSection(null, "deck"))
    }
    @Test fun vesselEntriesAreClosedUntilTapped() {
        val note = HandoverEntry(id = "radar", title = "X-Band", notes = "Radar notes")
        assertEquals(false, handoverEntryShowsNotes(false, null, note, ""))
        assertEquals("radar", nextExpandedHandoverEntry(null, "radar"))
        assertEquals(true, handoverEntryShowsNotes(false, "radar", note, ""))
        assertNull(nextExpandedHandoverEntry("radar", "radar"))
    }

    @Test fun searchingNotesOpensOnlyMatchingVesselEntry() {
        val radar = HandoverEntry(id = "radar", title = "X-band", notes = "Signal failure")
        val ecdis = HandoverEntry(id = "ecdis", title = "ECDIS", notes = "Chart update")
        assertEquals(true, handoverEntryShowsNotes(false, null, radar, "SIGNAL"))
        assertEquals(false, handoverEntryShowsNotes(false, null, ecdis, "SIGNAL"))
        assertEquals(false, handoverEntryShowsNotes(false, null, radar, "s"))
    }

    @Test fun personalNotesStayExpanded() {
        val note = HandoverEntry(id = "personal", title = "Note", notes = "Private")
        assertEquals(true, handoverEntryShowsNotes(true, null, note, ""))
        assertEquals(true, handoverEntryShowsNotes(true, null, note, "missing"))
    }

}
