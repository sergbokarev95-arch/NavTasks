package com.navtasks.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import java.time.LocalDate
import org.junit.Test

class NavWidgetCapacityTest {
    @Test fun shortWidgetsNeverForceFourRowsIntoRestrictedHeight() {
        assertEquals(2, widgetTaskCapacity(110))
        assertEquals(2, widgetTaskCapacity(139))
    }

    @Test fun tallHomeWidgetsUseSpareSpaceForActualTasks() {
        assertEquals(3, widgetTaskCapacity(140))
        assertEquals(3, widgetTaskCapacity(169))
        assertEquals(4, widgetTaskCapacity(170))
        assertEquals(4, widgetTaskCapacity(250))
    }

    @Test fun readableWideLayoutNeedsSufficientHorizontalSpace() {
        assertFalse(widgetUsesColumns(300))
        assertFalse(widgetUsesColumns(349))
        assertTrue(widgetUsesColumns(350))
        assertTrue(widgetUsesColumns(410))
    }

    @Test fun widgetAccentMatchesNavTasksSystemBlueInBothThemes() {
        assertEquals(0xFF5CB8FF.toInt(), widgetAccentColor(dark = true))
        assertEquals(0xFF0071E3.toInt(), widgetAccentColor(dark = false))
    }

    @Test fun noRedMissedPillWhenCountIsZero() {
        assertFalse(widgetMissedVisible(0))
        assertTrue(widgetMissedVisible(1))
        assertTrue(widgetMissedVisible(12))
    }

    @Test fun widgetHeaderShowsLocalizedDate() {
        val day = LocalDate.of(2026, 11, 7)
        assertTrue(widgetHeaderDate(day, UiLanguage.RU).startsWith("7"))
        assertTrue(widgetHeaderDate(day, UiLanguage.EN).endsWith("7"))
    }

    @Test fun footerCountsTasksBeyondVisibleRowsWithoutGoingNegative() {
        assertEquals(7, widgetHiddenTaskCount(10, 3))
        assertEquals(6, widgetHiddenTaskCount(10, 4))
        assertEquals(0, widgetHiddenTaskCount(2, 4))
        assertEquals(0, widgetHiddenTaskCount(0, 2))
    }
}
