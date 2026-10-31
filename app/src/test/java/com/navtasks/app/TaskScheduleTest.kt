package com.navtasks.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class TaskScheduleTest {
    @Test fun weeklyDateMovesToSelectedWeekday() {
        val task = TaskTemplate(title = "Weekly", frequency = Frequency.WEEKLY,
            dayOfWeek = 3, startDate = LocalDate.of(2026, 10, 26))
        assertFalse(TaskSchedule.isCalendarDue(task, LocalDate.of(2026, 10, 27)))
        assertTrue(TaskSchedule.isCalendarDue(task, LocalDate.of(2026, 10, 28)))
        assertEquals(LocalDate.of(2026, 10, 28),
            TaskSchedule.nextCalendarDate(task, LocalDate.of(2026, 10, 27)))
    }

    @Test fun monthly31ClampsToEndOfFebruary() {
        val task = TaskTemplate(title = "Monthly", frequency = Frequency.MONTHLY,
            dayOfMonth = 31, startDate = LocalDate.of(2026, 1, 1))
        assertTrue(TaskSchedule.isCalendarDue(task, LocalDate.of(2026, 2, 28)))
        assertEquals(LocalDate.of(2026, 2, 28),
            TaskSchedule.nextCalendarDate(task, LocalDate.of(2026, 2, 1)))
    }

    @Test fun intervalStartsFromActualCompletion() {
        val task = TaskTemplate(title = "Rolling", frequency = Frequency.WEEKLY,
            scheduleMode = ScheduleMode.AFTER_COMPLETION, intervalCount = 2,
            startDate = LocalDate.of(2026, 10, 20))
        assertEquals(LocalDate.of(2026, 11, 10),
            TaskSchedule.nextAfterCompletion(task, LocalDate.of(2026, 10, 27)))
    }

    @Test fun dailyNeverAppearsBeforeStartDate() {
        val task = TaskTemplate(title = "Daily", frequency = Frequency.DAILY,
            startDate = LocalDate.of(2026, 11, 1))
        assertFalse(TaskSchedule.isCalendarDue(task, LocalDate.of(2026, 10, 27)))
    }

    @Test fun postponingPreservesOccurrenceIdentity() {
        val run = TaskRun(taskId = "gmdss", dueDate = LocalDate.of(2026, 10, 27),
            checklist = emptyList(), postponedTo = LocalDate.of(2026, 10, 29))
        assertEquals("gmdss@2026-10-27", run.key)
        assertEquals(LocalDate.of(2026, 10, 29), run.displayDate)
        assertTrue(run.transferred)
    }

    @Test fun parentNeedsEveryNestedCheck() {
        val items = listOf(CheckItem(id = "one", title = "One"), CheckItem(id = "two", title = "Two"))
        val incomplete = TaskRun(taskId = "gmdss", dueDate = LocalDate.of(2026, 10, 28),
            checklist = items, checkedIds = setOf("one"))
        assertFalse(incomplete.requiredComplete)
        assertTrue(incomplete.copy(checkedIds = setOf("one", "two")).requiredComplete)
    }
    @Test fun russianCalendarMonthUsesNominativeButFullDateUsesGenitive() {
        val month = java.time.Month.OCTOBER.getDisplayName(java.time.format.TextStyle.FULL_STANDALONE, java.util.Locale("ru"))
        assertEquals("октябрь", month)
        assertEquals("31 октября 2026", navDate(LocalDate.of(2026, 10, 31), UiLanguage.RU, true))
    }
    @Test fun calendarSwipeMovesAcrossMonthAndYearBoundaries() {
        val anchor = LocalDate.of(2026, 10, 31)
        assertEquals(CalendarSwipePosition(2027, 1, anchor),
            calendarSwipePosition(1, 2026, 12, anchor, +1))
        assertEquals(CalendarSwipePosition(2026, 11, LocalDate.of(2026, 11, 1)),
            calendarSwipePosition(2, 2026, 10, anchor, +1))
        assertEquals(CalendarSwipePosition(2025, 10, anchor),
            calendarSwipePosition(0, 2026, 10, anchor, -1))
    }
    @Test fun calendarMonthShowsNeighbouringDatesInsteadOfBlanks() {
        val cells = monthGridDates(2026, 10)
        assertEquals(42, cells.size)
        assertEquals(LocalDate.of(2026, 9, 28), cells.first())
        assertEquals(LocalDate.of(2026, 11, 8), cells.last())
        assertEquals(1, cells.count { it == LocalDate.of(2026, 10, 31) })
    }

    @Test fun calendarDayStatesCommunicateProgress() {
        val today = LocalDate.of(2026, 10, 31)
        assertEquals(CalendarDayState.EMPTY, DayCounts(0, 0).state(today, today))
        assertEquals(CalendarDayState.SCHEDULED, DayCounts(4, 0).state(today, today))
        assertEquals(CalendarDayState.INCOMPLETE, DayCounts(4, 0).state(today.minusDays(1), today))
        assertEquals(CalendarDayState.PARTIAL, DayCounts(4, 2).state(today, today))
        assertEquals(CalendarDayState.COMPLETE, DayCounts(4, 4).state(today.minusDays(1), today))
    }
}
