package com.navtasks.glasslab

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GlassLabDockTabsTest {
    @Test
    fun productionTabOrderAndTitlesRemainUnchanged() {
        assertEquals(listOf("today", "work", "handover", "tasks"),
            GlassLabDockTabs.items.map { it.glyph })
        assertEquals(listOf("Сегодня", "Работы", "Handover", "Задачи"),
            GlassLabDockTabs.items.map { it.label })
    }

    @Test
    fun springStretchStaysWithinDockCapsule() {
        assertEquals(4, GlassLabDockTabs.items.size)
        assertEquals(28f, GlassLabDockTabs.maxStretchDp, 0f)
        // The animation width is intentionally pronounced (~one third of a tab)
        // while lensX still clamps the capsule inside both dock ends.
        assertTrue(GlassLabDockTabs.maxStretchDp in 0f..32f)
    }
}
