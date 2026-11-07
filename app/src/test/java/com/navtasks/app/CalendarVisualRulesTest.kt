package com.navtasks.app

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
