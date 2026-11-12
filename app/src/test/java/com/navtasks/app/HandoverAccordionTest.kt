package com.navtasks.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HandoverAccordionTest {
    @Test fun everySectionStartsClosed() {
        assertNull(nextExpandedHandoverSection("deck", "deck"))
    }

    @Test fun nestedNotesStartClosedAndRetapClosesOnlyTheSelectedEntry() {
        assertEquals("ecdis", nextExpandedHandoverEntry(null, "ecdis"))
        assertNull(nextExpandedHandoverEntry("ecdis", "ecdis"))
        assertEquals("radar", nextExpandedHandoverEntry("ecdis", "radar"))
    }

    @Test fun openingAnotherSectionClosesThePreviousOne() {
        assertEquals("engine", nextExpandedHandoverSection("deck", "engine"))
        assertEquals("deck", nextExpandedHandoverSection(null, "deck"))
    }
}
