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
}
