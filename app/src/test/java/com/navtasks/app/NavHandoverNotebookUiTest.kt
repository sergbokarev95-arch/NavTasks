package com.navtasks.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class NavHandoverNotebookUiTest {
    @Test fun legacyMarksRetainedAndNotebookOrderUnchanged() {
        val first = HandoverEntry(id = "one", title = "ECDIS",
            notes = "Chart updates", mark = HandoverMark.COMPLETED,
            completedDate = LocalDate.of(2026, 11, 10))
        val second = HandoverEntry(id = "two", title = "Radar", mark = HandoverMark.OPEN)
        val third = HandoverEntry(id = "three", title = "Gyro", mark = HandoverMark.STRUCK)
        val source = listOf(first, second, third)
        val displayed = handoverNotebookEntries(source)
        assertEquals(listOf("one", "two", "three"), displayed.map { it.id })
        assertEquals(source, displayed)
        assertEquals(HandoverMark.COMPLETED, displayed.first().mark)
        assertEquals(first.completedDate, displayed.first().completedDate)
    }

    @Test fun smallSpringScalesDoNotAffectGlassLensConstants() {
        assertTrue(NavPressFeedbackTokens.cardScale in 1.005f..1.04f)
        assertTrue(NavPressFeedbackTokens.floatingActionScale in 1.03f..1.08f)
        assertEquals(36f, NavLiquidDockOptics.bar.refractionAmountDp, 0.01f)
    }
}
