package com.navtasks.app

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class ScopedRescheduleTest {
    private fun day(date: String) = LocalDate.parse(date)

    @Test fun weeklySeriesChangesOnlyFromSelectedDay() {
        val t = TaskTemplate(id = "gmdss", title = "GMDSS Weekly",
            frequency = Frequency.WEEKLY, startDate = day("2026-10-27"), dayOfWeek = 2)
        val past = TaskRun(t.id, day("2026-10-27"), emptyList(), completed = true)
        val selected = TaskRun(t.id, day("2026-11-03"), emptyList())
        val next = TaskRun(t.id, day("2026-11-10"), emptyList())
        val changed = scopedCalendarMove(
            t, mapOf(past.key to past, selected.key to selected, next.key to next),
            selected, day("2026-11-06"),
        )!!
        assertSame(past, changed.runs[past.key])
        assertFalse(TaskSchedule.isCalendarDue(changed.template, day("2026-11-03")))
        assertFalse(TaskSchedule.isCalendarDue(changed.template, day("2026-11-10")))
        assertTrue(TaskSchedule.isCalendarDue(changed.template, day("2026-10-27")))
        assertTrue(TaskSchedule.isCalendarDue(changed.template, day("2026-11-06")))
        assertTrue(TaskSchedule.isCalendarDue(changed.template, day("2026-11-13")))
        assertEquals(day("2026-11-06"),
            TaskSchedule.nextCalendarDate(changed.template, day("2026-11-03")))
        assertFalse(changed.runs.containsKey(selected.key))
        assertFalse(changed.runs.containsKey(next.key))
        assertEquals(day("2026-11-06"), changed.runs["gmdss@2026-11-06"]?.dueDate)
    }

    @Test fun postponingTodaySeriesCarriesPartialChecklist() {
        val check = listOf(CheckItem(id = "pv", title = "PV", comment = "journal"),
            CheckItem(id = "kv", title = "KV"))
        val t = TaskTemplate(id = "t", title = "Every day", frequency = Frequency.DAILY,
            startDate = day("2026-11-01"), checklist = check)
        val today = TaskRun(t.id, day("2026-11-05"), check, checkedIds = setOf("pv"))
        val tomorrow = TaskRun(t.id, day("2026-11-06"), check)
        val changed = scopedCalendarMove(t, mapOf(today.key to today, tomorrow.key to tomorrow),
            today, day("2026-11-07"))!!
        val moved = changed.runs["t@2026-11-07"]!!
        assertEquals(setOf("pv"), moved.checkedIds)
        assertEquals("journal", moved.checklist.first().comment)
        assertFalse(TaskSchedule.isCalendarDue(changed.template, day("2026-11-05")))
        assertFalse(TaskSchedule.isCalendarDue(changed.template, day("2026-11-06")))
        assertTrue(TaskSchedule.isCalendarDue(changed.template, day("2026-11-07")))
        assertTrue(TaskSchedule.isCalendarDue(changed.template, day("2026-11-08")))
    }

    @Test fun monthlyEndOfMonthRetainsEarlierHistory() {
        val t = TaskTemplate(id = "m", title = "Monthly", frequency = Frequency.MONTHLY,
            startDate = day("2026-01-01"), dayOfMonth = 31)
        val selected = TaskRun(t.id, day("2026-01-31"), emptyList())
        val changed = scopedCalendarMove(t, mapOf(selected.key to selected),
            selected, day("2026-02-28"))!!.template
        assertFalse(TaskSchedule.isCalendarDue(changed, day("2026-01-31")))
        assertTrue(TaskSchedule.isCalendarDue(changed, day("2026-02-28")))
        assertTrue(TaskSchedule.isCalendarDue(changed, day("2026-03-28")))
        assertFalse(TaskSchedule.isCalendarDue(changed, day("2026-03-31")))
    }

    @Test fun earlierHistorySurvivesLaterOverride() {
        val t = TaskTemplate(id = "weekly", title = "Weekly",
            frequency = Frequency.WEEKLY, dayOfWeek = 2,
            startDate = day("2026-10-20"))
        val first = scopedCalendarMove(t,
            mapOf("weekly@2026-11-03" to TaskRun("weekly", day("2026-11-03"), emptyList())),
            TaskRun("weekly", day("2026-11-03"), emptyList()), day("2026-11-06"))!!
        val nextRun = TaskRun("weekly", day("2026-11-13"), emptyList())
        val second = scopedCalendarMove(first.template,
            first.runs + (nextRun.key to nextRun),
            nextRun, day("2026-11-16"))!!
        assertTrue(TaskSchedule.isCalendarDue(second.template, day("2026-10-27")))
        assertTrue(TaskSchedule.isCalendarDue(second.template, day("2026-11-06")))
        assertTrue(TaskSchedule.isCalendarDue(second.template, day("2026-11-09")) == false)
        assertTrue(TaskSchedule.isCalendarDue(second.template, day("2026-11-16")))
        assertTrue(TaskSchedule.isCalendarDue(second.template, day("2026-11-23")))
    }

    @Test fun completedOccurrenceAndNonCalendarSeriesCannotBeChanged() {
        val task = TaskTemplate(id = "a", title = "Weekly", frequency = Frequency.WEEKLY)
        val complete = TaskRun(task.id, day("2026-11-06"), emptyList(), completed = true)
        assertNull(scopedCalendarMove(task, mapOf(complete.key to complete),
            complete, day("2026-11-13")))
        val rolling = task.copy(scheduleMode = ScheduleMode.AFTER_COMPLETION)
        val pending = complete.copy(completed = false)
        assertFalse(canMoveSeries(rolling, pending, day("2026-11-13")))
        assertNull(scopedCalendarMove(rolling, mapOf(pending.key to pending),
            pending, day("2026-11-13")))
    }

    @Test fun existingCompletedTargetIsNeverOverwritten() {
        val task = TaskTemplate(id = "z", title = "Weekly", frequency = Frequency.WEEKLY,
            dayOfWeek = 2, startDate = day("2026-10-01"))
        val selected = TaskRun("z", day("2026-11-03"), emptyList())
        val completed = TaskRun("z", day("2026-11-06"), emptyList(), completed = true)
        val result = scopedCalendarMove(task,
            mapOf(selected.key to selected, completed.key to completed),
            selected, day("2026-11-06"))!!
        assertSame(completed, result.runs[completed.key])
        assertEquals(day("2026-11-06"), result.runs[selected.key]?.displayDate)
    }
    @Test fun earlierMovedOccurrenceKeepsTombstoneSoRefreshCannotRecreateIt() {
        val task = TaskTemplate(id = "q", title = "Daily", frequency = Frequency.DAILY,
            startDate = day("2026-11-01"))
        val carried = TaskRun("q", day("2026-11-03"), emptyList(),
            postponedTo = day("2026-11-05"))
        val result = scopedCalendarMove(task, mapOf(carried.key to carried),
            carried, day("2026-11-07"))!!
        assertTrue(result.runs[carried.key]?.skipped == true)
        assertEquals(day("2026-11-07"), result.runs["q@2026-11-07"]?.displayDate)
        assertTrue(TaskSchedule.isCalendarDue(result.template, day("2026-11-03")))
        assertFalse(TaskSchedule.isCalendarDue(result.template, day("2026-11-05")))
    }

}
