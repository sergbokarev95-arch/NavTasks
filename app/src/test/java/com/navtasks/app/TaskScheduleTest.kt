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
}
