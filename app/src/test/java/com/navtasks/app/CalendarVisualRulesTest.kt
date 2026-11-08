package com.navtasks.app

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/** Pure month-grid presentation regressions; scheduling and status model stay unchanged. */
class CalendarVisualRulesTest {
    @Test fun saturdayAndSundayAreWeekendsInEveryMonth() {
        assertTrue(calendarIsWeekend(LocalDate.of(2026, 11, 7)))  // Saturday
        assertTrue(calendarIsWeekend(LocalDate.of(2026, 12, 6)))  // Sunday
        assertFalse(calendarIsWeekend(LocalDate.of(2026, 12, 4))) // Friday
        assertFalse(calendarIsWeekend(LocalDate.of(2026, 12, 7))) // Monday
    }

    @Test fun todayAndPlanUseAppleSystemRedOnWeekends() {
        // Today calls calendarWeekendInk as well as Plan, avoiding theme drift.
        // No muted-pink leftovers from pre-0.4.84 presentation.
        assertEquals(Color(0xFFFF453A), calendarWeekendInk(navPalette(true)))
        assertEquals(Color(0xFFFF3B30), calendarWeekendInk(navPalette(false)))
    }

    @Test fun aStatusDotStillMarksOutcomesButNotPlainScheduledDays() {
        assertFalse(calendarHasOutcomeDot(CalendarDayState.EMPTY, false))
        assertFalse(calendarHasOutcomeDot(CalendarDayState.SCHEDULED, false))
        assertTrue(calendarHasOutcomeDot(CalendarDayState.INCOMPLETE, false))
        assertTrue(calendarHasOutcomeDot(CalendarDayState.PARTIAL, false))
        assertTrue(calendarHasOutcomeDot(CalendarDayState.COMPLETE, false))
        assertTrue(calendarHasOutcomeDot(CalendarDayState.EMPTY, true))
        assertTrue(calendarHasOutcomeDot(CalendarDayState.SCHEDULED, true))
    }
}
