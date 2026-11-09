package com.navtasks.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/** Presentation-only nav contract: no task data migration to Reference. */
class ReferenceNavigationTest {
    @Test fun fourthDockTabIsReferenceNotTasks() {
        assertEquals(4, NavLiquidDockTabs.items.size)
        assertEquals("reference", NavLiquidDockTabs.items[3].glyph)
        assertEquals("Справочник", NavLiquidDockTabs.items[3].label)
        assertNotEquals("tasks", NavLiquidDockTabs.items[3].glyph)
    }

    @Test fun referenceDoesNotReuseStoredTasksTabScrollState() {
        val reference = primaryTabResetStateKey("vessel", 3, 0)
        assertEquals("vessel/main-tab-reference/dock-0", reference)
        assertNotEquals("vessel/main-tab-3/dock-0", reference)
        assertEquals("vessel/main-tab-works/dock-0",
            primaryTabResetStateKey("vessel", 1, 0))
    }

    @Test fun allFourTaskFrequencyChoicesStayAvailable() {
        assertEquals(4, Frequency.entries.size)
        assertEquals(
            listOf(Frequency.DAILY, Frequency.WEEKLY, Frequency.MONTHLY, Frequency.EXTRA),
            Frequency.entries.toList(),
        )
    }
}
