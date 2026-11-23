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

    @Test fun yearOverviewIsExactlyFourRowsAndTwelveDistinctMonths() {
        assertEquals(4, navCalendarYearRows.size)
        assertTrue(navCalendarYearRows.all { it.size == 3 })
        assertEquals((1..12).toList(), navCalendarYearRows.flatten())
        val calendar = File("src/main/java/com/navtasks/app/PlanAndTasksUi.kt").readText()
        val year = calendar.substringAfter("0 -> BoxWithConstraints(")
            .substringBefore("1 -> Column {")
        assertTrue(year.contains("navCalendarYearRows.forEach"))
        assertTrue(year.contains("Modifier.fillMaxWidth().weight(1f)"))
        assertTrue(year.contains("Modifier.weight(1f).fillMaxHeight()"))
        assertTrue(year.contains("val compact = maxHeight < 470.dp"))
        assertTrue(year.contains("monthGridDates(year, m).chunked(7)"))
        assertTrue(!year.contains("LazyVerticalGrid("))
    }

    @Test fun modeSwitchPreservesDockSpringAndTapOnlyLensPulse() {
        val control = File("src/main/java/com/navtasks/app/NavCalendarModeControl.kt").readText()
        assertTrue(control.contains("navLiquidGlassRim(dark)"))
        assertTrue(control.contains("stiffness = 550f"))
        assertTrue(control.contains("NavLiquidDockTabs.repeatTapScale"))
        assertTrue(control.contains("calendarGlassLensSlide"))
        assertTrue(control.contains("onSelect(navCalendarAvailableLevels[index])"))
        assertTrue(!control.contains("localized(\"Неделя\""))
        assertTrue(!control.contains("localized(\"События\""))
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
