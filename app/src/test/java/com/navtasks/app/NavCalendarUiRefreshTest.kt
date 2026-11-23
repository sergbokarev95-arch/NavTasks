package com.navtasks.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.time.LocalDate

/** Calendar UI Refresh: visual-only navigation, task recurrence untouched. */
class NavCalendarUiRefreshTest {
    @Test fun yearMonthDayAreOnlyFunctionalSegmentOptions() {
        assertEquals(listOf(0, 1, 2), navCalendarAvailableLevels)
    }

    @Test fun sevenDayStripStartsOnMondayAndCrossesMonthSafely() {
        val selected = LocalDate.of(2026, 11, 1) // Sunday
        val week = navCalendarWeekDates(selected)
        assertEquals(7, week.size)
        assertEquals(LocalDate.of(2026, 10, 26), week.first())
        assertEquals(selected, week.last())
    }

    @Test fun weekStripCrossesCalendarYearWithoutChangingDate() {
        val selected = LocalDate.of(2027, 1, 1) // Friday
        val week = navCalendarWeekDates(selected)
        assertEquals(LocalDate.of(2026, 12, 28), week.first())
        assertEquals(LocalDate.of(2027, 1, 3), week.last())
        assertTrue(selected in week)
    }

    @Test fun originalSwipePositionStillControlsYearMonthDayChanges() {
        val date = LocalDate.of(2026, 12, 31)
        val month = calendarSwipePosition(1, 2026, 12, date, 1)
        assertEquals(2027, month.year)
        assertEquals(1, month.month)
        val day = calendarSwipePosition(2, 2026, 12, date, 1)
        assertEquals(LocalDate.of(2027, 1, 1), day.day)
        assertEquals(2026, calendarSwipePosition(0, 2027, 12, date, -1).year)
    }

    @Test fun appKeepsCalendarInsideTodayAndDockUntouched() {
        val root = File("src/main/java/com/navtasks/app/NavTasksUi.kt").readText()
        val calendar = File("src/main/java/com/navtasks/app/PlanAndTasksUi.kt").readText()
        assertTrue(root.contains("if (showCalendar)"))
        assertTrue(root.contains("PlanScreenV2("))
        assertTrue(calendar.contains("NavCalendarModeControl(level, p, lang"))
        assertTrue(calendar.contains("navCalendarWeekDates(selectedDay)"))
        assertTrue(calendar.contains("PlanReadOnlyTile(task, run, store, p, lang, onEdit)"))
        assertTrue(calendar.contains("calendarHasOutcomeDot(status, hasWork)"))
    }
}
