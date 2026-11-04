package com.navtasks.app

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class FrequencyScheduleChangeTest {
    private fun date(value: String) = LocalDate.parse(value)
    private fun weekly() = TaskTemplate(
        id = "gmdss", title = "GMDSS", frequency = Frequency.WEEKLY,
        startDate = date("2026-10-01"), dayOfWeek = 1, dayOfMonth = 5,
    )

    @Test fun dailyToWeeklyChangesTodayAndFollowingDaysWithoutRewritingPast() {
        val original = weekly().copy(frequency = Frequency.DAILY)
        val today = date("2026-11-05") // Thursday
        val past = TaskRun(original.id, date("2026-11-04"), emptyList(), completed = true)
        val current = TaskRun(original.id, today, emptyList())
        val future = TaskRun(original.id, today.plusDays(1), emptyList())
        val existing = listOf(past, current, future).associateBy { it.key }
        val edited = original.copy(frequency = Frequency.WEEKLY, dayOfWeek = 6)
        val result = editCalendarFrequencyScoped(original, edited, existing,
            CalendarEditScope.THIS_AND_FUTURE, today)!!

        assertSame(past, result.runs[past.key])
        assertNull(result.runs[current.key])
        assertNull(result.runs[future.key])
        assertEquals(date("2026-11-07"), result.runs["gmdss@2026-11-07"]?.dueDate)
        assertEquals(Frequency.DAILY, TaskSchedule.effectiveFrequency(result.template, past.dueDate))
        assertEquals(Frequency.WEEKLY, TaskSchedule.effectiveFrequency(result.template, today))
        assertEquals(Frequency.DAILY, result.template.frequency) // historical base
        assertTrue(TaskSchedule.isCalendarDue(result.template, date("2026-11-04")))
        assertFalse(TaskSchedule.isCalendarDue(result.template, today))
        assertTrue(TaskSchedule.isCalendarDue(result.template, date("2026-11-07")))
        assertTrue(TaskSchedule.isCalendarDue(result.template, date("2026-11-14")))
        assertFalse(TaskSchedule.isCalendarDue(result.template, date("2026-11-08")))
    }

    @Test fun weeklyToMonthlyCarriesTodayChecklistChecksAndNotes() {
        val task = weekly().copy(dayOfWeek = 4) // Thursday 5 Nov
        val items = listOf(
            CheckItem(id = "pv", title = "PV", comment = "GMDSS journal"),
            CheckItem(id = "kv", title = "KV"),
        )
        val run = TaskRun(task.id, date("2026-11-05"), items,
            checkedIds = setOf("pv"))
        val result = editCalendarFrequencyScoped(
            task, task.copy(frequency = Frequency.MONTHLY, dayOfMonth = 10),
            mapOf(run.key to run), CalendarEditScope.THIS_AND_FUTURE,
            date("2026-11-05"),
        )!!
        val moved = result.runs["gmdss@2026-11-10"]!!
        assertEquals(setOf("pv"), moved.checkedIds)
        assertEquals("GMDSS journal", moved.checklist.first().comment)
        assertEquals(Frequency.MONTHLY, TaskSchedule.effectiveFrequency(result.template, date("2026-11-10")))
        assertEquals(date("2026-12-10"),
            TaskSchedule.nextCalendarDate(result.template, date("2026-11-11")))
        assertFalse(TaskSchedule.isCalendarDue(result.template, date("2026-11-12")))
        assertTrue(TaskSchedule.isCalendarDue(result.template, date("2026-11-10")))
    }

    @Test fun onlyOneOccurrenceChangesCategoryEvenIfDateStaysSame() {
        val task = weekly().copy(dayOfWeek = 4) // Thursday
        val today = date("2026-11-05")
        val run = TaskRun(task.id, today, emptyList())
        val result = editCalendarFrequencyScoped(task,
            task.copy(frequency = Frequency.DAILY),
            mapOf(run.key to run), CalendarEditScope.ONLY_THIS, today)!!
        val changed = result.runs[run.key]!!
        assertEquals(today, changed.displayDate)
        assertEquals(Frequency.DAILY, changed.frequencyOverride)
        assertEquals(Frequency.DAILY,
            TaskSchedule.occurrenceFrequency(result.template, changed))
        assertEquals(Frequency.WEEKLY,
            TaskSchedule.effectiveFrequency(result.template, today))
        assertEquals(Frequency.WEEKLY,
            TaskSchedule.effectiveFrequency(result.template, today.plusDays(7)))
    }

    @Test fun onlyOneOccurrenceMovesWithoutChangingFutureFrequency() {
        val task = weekly()
        val today = date("2026-11-05")
        val next = TaskRun(task.id, date("2026-11-09"), emptyList())
        val newRule = task.copy(frequency = Frequency.MONTHLY, dayOfMonth = 7)
        val result = editCalendarFrequencyScoped(task, newRule,
            mapOf(next.key to next), CalendarEditScope.ONLY_THIS, today)!!
        assertEquals(date("2026-11-07"), result.runs[next.key]?.displayDate)
        assertEquals(Frequency.MONTHLY, result.runs[next.key]?.frequencyOverride)
        assertTrue(TaskSchedule.isCalendarDue(result.template, date("2026-11-09")))
        assertEquals(Frequency.WEEKLY, result.template.frequency)
        assertTrue(result.template.calendarRevisions.isEmpty())
    }

    @Test fun completedTodayIsProtectedAndNewDailyStartsTomorrow() {
        val task = weekly().copy(dayOfWeek = 4)
        val today = date("2026-11-05")
        val completed = TaskRun(task.id, today, emptyList(), completed = true)
        val existing = mapOf(completed.key to completed)
        val preview = calendarFrequencyChangePreview(task,
            task.copy(frequency = Frequency.DAILY), existing, today)!!
        assertEquals(date("2026-11-06"), preview.newDate)
        assertEquals(date("2026-11-12"), preview.selectedRun.dueDate)
        val change = editCalendarFrequencyScoped(
            task, task.copy(frequency = Frequency.DAILY),
            existing, CalendarEditScope.THIS_AND_FUTURE, today)!!
        assertSame(completed, change.runs[completed.key])
        assertEquals(date("2026-11-06"), change.runs["gmdss@2026-11-06"]?.dueDate)
        assertFalse(TaskSchedule.isCalendarDue(change.template, today))
        assertTrue(TaskSchedule.isCalendarDue(change.template, date("2026-11-07")))
    }

    @Test fun afterCompletionAndOneOffAreNotSilentlyRewritten() {
        val task = weekly()
        val afterCompletion = task.copy(scheduleMode = ScheduleMode.AFTER_COMPLETION)
        assertNull(calendarFrequencyChangePreview(afterCompletion,
            afterCompletion.copy(frequency = Frequency.MONTHLY), emptyMap(),
            date("2026-11-05")))
        val once = task.copy(frequency = Frequency.EXTRA)
        assertNull(calendarFrequencyChangePreview(once,
            once.copy(frequency = Frequency.WEEKLY), emptyMap(),
            date("2026-11-05")))
        assertNull(calendarFrequencyChangePreview(task,
            task.copy(frequency = Frequency.EXTRA), emptyMap(),
            date("2026-11-05")))
    }

    @Test fun repeatedFrequencyChangesKeepEarlierCalendarRules() {
        val original = weekly()
        val first = editCalendarFrequencyScoped(original,
            original.copy(frequency = Frequency.MONTHLY, dayOfMonth = 5),
            emptyMap(), CalendarEditScope.THIS_AND_FUTURE, date("2026-11-02"))!!
        val monthly = first.template
        assertEquals(Frequency.WEEKLY,
            TaskSchedule.effectiveFrequency(monthly, date("2026-10-26")))
        assertEquals(Frequency.MONTHLY,
            TaskSchedule.effectiveFrequency(monthly, date("2026-11-05")))
        assertTrue(TaskSchedule.isCalendarDue(monthly, date("2026-11-05")))
        val second = editCalendarFrequencyScoped(monthly,
            monthly.copy(frequency = Frequency.DAILY), first.runs,
            CalendarEditScope.THIS_AND_FUTURE, date("2026-11-10"))!!
        assertEquals(Frequency.WEEKLY,
            TaskSchedule.effectiveFrequency(second.template, date("2026-10-26")))
        assertEquals(Frequency.MONTHLY,
            TaskSchedule.effectiveFrequency(second.template, date("2026-11-05")))
        assertEquals(Frequency.DAILY,
            TaskSchedule.effectiveFrequency(second.template, date("2026-11-10")))
        assertTrue(TaskSchedule.isCalendarDue(second.template, date("2026-11-11")))
        assertEquals(2, second.template.calendarRevisions.size)
    }

    @Test fun frequencyChangeThenWeekdayChangeRetainsBaseHistory() {
        val initial = weekly()
        val first = editCalendarFrequencyScoped(initial,
            initial.copy(frequency = Frequency.MONTHLY, dayOfMonth = 9),
            emptyMap(), CalendarEditScope.THIS_AND_FUTURE, date("2026-11-02"))!!
        val month = first.template
        val edited = month.copy(frequency = Frequency.MONTHLY, dayOfMonth = 13)
        val second = editCalendarDayScoped(month, edited, first.runs,
            CalendarEditScope.THIS_AND_FUTURE, date("2026-11-10"))!!
        assertEquals(Frequency.WEEKLY, second.template.frequency)
        assertEquals(Frequency.WEEKLY,
            TaskSchedule.effectiveFrequency(second.template, date("2026-10-26")))
        assertEquals(Frequency.MONTHLY,
            TaskSchedule.effectiveFrequency(second.template, date("2026-11-13")))
        assertTrue(TaskSchedule.isCalendarDue(second.template, date("2026-11-13")))
        assertFalse(TaskSchedule.isCalendarDue(second.template, date("2026-11-09")))
    }

    @Test fun todayGroupingUsesSingleOccurrenceCategoryOverride() {
        val task = weekly()
        val today = date("2026-11-05")
        val moved = TaskRun(task.id, today, emptyList(),
            frequencyOverride = Frequency.MONTHLY)
        val grouped = groupTodayByFrequency(listOf(task to moved))
        assertEquals(Frequency.MONTHLY, grouped.single().first)
        assertEquals(moved.key, grouped.single().second.single().second.key)
    }

    @Test fun existingRevisionWithoutFrequencyRetainsBaseRule() {
        val task = weekly().copy(calendarRevisions = listOf(
            CalendarScheduleRevision(date("2026-11-01"), date("2026-11-03"),
                dayOfWeek = 2, dayOfMonth = 15)))
        assertEquals(Frequency.WEEKLY,
            TaskSchedule.effectiveFrequency(task, date("2026-11-04")))
        assertTrue(TaskSchedule.isCalendarDue(task, date("2026-11-10")))
        assertFalse(TaskSchedule.isCalendarDue(task, date("2026-11-11")))
    }
}
