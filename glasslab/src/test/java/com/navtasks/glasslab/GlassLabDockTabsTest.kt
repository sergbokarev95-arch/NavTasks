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
    fun movingLensUsesOneScaleForBothAxesAndExceedsDockHeight() {
        assertEquals(4, GlassLabDockTabs.items.size)
        assertEquals(1f, GlassLabDockTabs.scaleAt(0f), 0f)
        assertEquals(GlassLabDockTabs.movingScale, GlassLabDockTabs.scaleAt(1f), 0f)
        assertEquals(GlassLabDockTabs.movingScale, GlassLabDockTabs.scaleAt(2f), 0f)

        // The scaled selection must actually cross both vertical dock edges.
        val enlargedHeight = GlassLabDockTabs.restingLensHeightDp *
            GlassLabDockTabs.scaleAt(1f)
        assertTrue(enlargedHeight > GlassLabDockTabs.dockHeightDp)
        val verticalInset = (GlassLabDockTabs.dockHeightDp - enlargedHeight) / 2f
        assertTrue(verticalInset < 0f)
        assertEquals(-verticalInset,
            enlargedHeight + verticalInset - GlassLabDockTabs.dockHeightDp, .001f)
    }
}
