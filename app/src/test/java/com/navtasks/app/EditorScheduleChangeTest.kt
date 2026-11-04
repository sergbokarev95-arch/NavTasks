package com.navtasks.app

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class EditorScheduleChangeTest {
    private fun day(s: String): LocalDate = LocalDate.parse(s)

    private fun weeklyTask(): TaskTemplate = TaskTemplate(
        id = "gmdss",
        title = "GMDSS Weekly",
        frequency = Frequency.WEEKLY,
        startDate = day("2026-10-01"),
        dayOfWeek = 1, // Monday
    )

    @Test fun changingWeekdayMovesAlreadyMaterializedTodayForOnlyThis() {
        val old = weeklyTask()
        val today = day("2026-11-02") // Monday
        val checklist = listOf(CheckItem(id = "pv", title = "PV", comment = "Journal note"))
        val now = TaskRun(old.id, today, checklist, checkedIds = setOf("pv"))
        val future = TaskRun(old.id, day("2026-11-09"), emptyList())
        val completed = TaskRun(old.id, day("2026-10-26"), emptyList(), completed = true)
        val existing = listOf(now, future, completed).associateBy { it.key }
        val edited = old.copy(dayOfWeek = 4) // Thursday
        val preview = calendarDayEditPreview(old, edited, existing, today)!!
        assertEquals(today, preview.selectedRun.displayDate)
        assertEquals(day("2026-11-05"), preview.newDate)

        val result = editCalendarDayScoped(old, edited, existing,
            CalendarEditScope.ONLY_THIS, today)!!
        assertEquals(day("2026-11-05"), result.runs[now.key]?.displayDate)
        assertEquals(setOf("pv"), result.runs[now.key]?.checkedIds)
        assertEquals("Journal note", result.runs[now.key]?.checklist?.first()?.comment)
        assertSame(completed, result.runs[completed.key])
        assertSame(future, result.runs[future.key])
        assertTrue(TaskSchedule.isCalendarDue(result.template, today))
        assertTrue(TaskSchedule.isCalendarDue(result.template, day("2026-11-09")))
        assertFalse(TaskSchedule.isCalendarDue(result.template, day("2026-11-05")))
    }

    @Test fun changingWeekdayForFutureRemovesOldTodayAndPreservesHistory() {
        val old = weeklyTask()
        val today = day("2026-11-02")
        val now = TaskRun(old.id, today, emptyList())
        val future = TaskRun(old.id, day("2026-11-09"), emptyList())
        val completed = TaskRun(old.id, day("2026-10-26"), emptyList(), completed = true)
        val existing = listOf(now, future, completed).associateBy { it.key }
        val edited = old.copy(dayOfWeek = 4)
        val result = editCalendarDayScoped(old, edited, existing,
            CalendarEditScope.THIS_AND_FUTURE, today)!!
        assertSame(completed, result.runs[completed.key])
        assertNull(result.runs[now.key])
        assertNull(result.runs[future.key])
        assertEquals(day("2026-11-05"), result.runs["gmdss@2026-11-05"]?.dueDate)
        assertEquals(1, result.template.calendarRevisions.size)
        assertEquals(1, result.template.dayOfWeek) // historical base stays Monday
        assertTrue(TaskSchedule.isCalendarDue(result.template, day("2026-10-26")))
        assertFalse(TaskSchedule.isCalendarDue(result.template, today))
        assertFalse(TaskSchedule.isCalendarDue(result.template, day("2026-11-09")))
        assertTrue(TaskSchedule.isCalendarDue(result.template, day("2026-11-05")))
        assertTrue(TaskSchedule.isCalendarDue(result.template, day("2026-11-12")))
    }

    @Test fun monthlyCalendarChangeUsesCurrentDateAndPreservesCompletedRuns() {
        val old = TaskTemplate(id = "monthly", title = "Monthly", frequency = Frequency.MONTHLY,
            startDate = day("2026-01-01"), dayOfMonth = 5)
        val today = day("2026-11-05")
        val earlier = TaskRun(old.id, day("2026-10-05"), emptyList(), completed = true)
        val current = TaskRun(old.id, today, emptyList())
        val result = editCalendarDayScoped(old, old.copy(dayOfMonth = 10),
            listOf(earlier, current).associateBy { it.key },
            CalendarEditScope.THIS_AND_FUTURE, today)!!
        assertSame(earlier, result.runs[earlier.key])
        assertFalse(TaskSchedule.isCalendarDue(result.template, today))
        assertTrue(TaskSchedule.isCalendarDue(result.template, day("2026-11-10")))
        assertTrue(TaskSchedule.isCalendarDue(result.template, day("2026-12-10")))
        assertTrue(TaskSchedule.isCalendarDue(result.template, day("2026-10-05")))
    }

    @Test fun editorReadsEffectiveWeekdayAfterPreviousSeriesMove() {
        val old = weeklyTask().copy(calendarRevisions = listOf(
            CalendarScheduleRevision(day("2026-10-28"), day("2026-10-30"), 5, 30)))
        assertEquals(5, effectiveCalendarWeekday(old, day("2026-11-02")))
        assertEquals(1, effectiveCalendarWeekday(old, day("2026-10-26")))
        val existing = mapOf(
            "gmdss@2026-11-06" to TaskRun(old.id, day("2026-11-06"), emptyList()))
        assertNull(calendarDayEditPreview(old, old.copy(dayOfWeek = 5), existing,
            day("2026-11-02"))) // no change: editor shows effective Friday
        val preview = calendarDayEditPreview(old, old.copy(dayOfWeek = 4), existing,
            day("2026-11-02"))!!
        assertEquals(day("2026-11-05"), preview.newDate)
        val result = editCalendarDayScoped(old, old.copy(dayOfWeek = 4), existing,
            CalendarEditScope.THIS_AND_FUTURE, day("2026-11-02"))!!
        assertEquals(1, result.template.dayOfWeek)
        assertTrue(TaskSchedule.isCalendarDue(result.template, day("2026-10-30")))
        assertTrue(TaskSchedule.isCalendarDue(result.template, day("2026-11-05")))
        assertFalse(TaskSchedule.isCalendarDue(result.template, day("2026-11-06")))
    }

    @Test fun completedTodayIsNeverMoved() {
        val old = weeklyTask()
        val now = TaskRun(old.id, day("2026-11-02"), emptyList(), completed = true)
        val next = TaskRun(old.id, day("2026-11-09"), emptyList())
        val existing = listOf(now, next).associateBy { it.key }
        val result = editCalendarDayScoped(old, old.copy(dayOfWeek = 4), existing,
            CalendarEditScope.THIS_AND_FUTURE, day("2026-11-02"))!!
        assertSame(now, result.runs[now.key])
        assertFalse(TaskSchedule.isCalendarDue(result.template, day("2026-11-02")))
        assertTrue(TaskSchedule.isCalendarDue(result.template, day("2026-11-05")))
    }

    @Test fun editingDateForNotYetMaterializedOccurrenceCreatesOnlyOneOverride() {
        val old = weeklyTask()
        val today = day("2026-11-03") // Tuesday, next Monday not materialized
        val changed = editCalendarDayScoped(old, old.copy(dayOfWeek = 5), emptyMap(),
            CalendarEditScope.ONLY_THIS, today)!!
        val originalKey = "gmdss@2026-11-09"
        assertEquals(day("2026-11-06"), changed.runs[originalKey]?.displayDate)
        assertTrue(TaskSchedule.isCalendarDue(changed.template, day("2026-11-09")))
        assertFalse(TaskSchedule.isCalendarDue(changed.template, day("2026-11-06")))
    }

    @Test fun changingFrequencyOrModeDoesNotUseUnsupportedDayEditPath() {
        val old = weeklyTask()
        assertNull(calendarDayEditPreview(old,
            old.copy(frequency = Frequency.MONTHLY, dayOfMonth = 15),
            emptyMap(), day("2026-11-02")))
        assertNull(calendarDayEditPreview(old,
            old.copy(scheduleMode = ScheduleMode.AFTER_COMPLETION, dayOfWeek = 4),
            emptyMap(), day("2026-11-02")))
    }
    @Test fun renamingTaskAfterPriorRevisionDoesNotChangePastWeekdays() {
        val old = weeklyTask().copy(calendarRevisions = listOf(
            CalendarScheduleRevision(day("2026-10-28"), day("2026-10-30"), 5, 30)))
        val inEditor = old.copy(title = "Renamed GMDSS",
            dayOfWeek = effectiveCalendarWeekday(old, day("2026-11-02")))
        val result = preserveHistoricalCalendarBase(old, inEditor, day("2026-11-02"))
        assertEquals("Renamed GMDSS", result.title)
        assertEquals(1, result.dayOfWeek)
        assertEquals(5, effectiveCalendarWeekday(result, day("2026-11-02")))
        assertTrue(TaskSchedule.isCalendarDue(result, day("2026-10-26")))
        assertTrue(TaskSchedule.isCalendarDue(result, day("2026-10-30")))
    }

    @Test fun weeklyChangeCanBringNextOccurrenceEarlierThanOldWeekday() {
        val old = weeklyTask() // Monday
        val today = day("2026-11-03") // Tuesday
        val oldNext = TaskRun(old.id, day("2026-11-09"), emptyList())
        val edited = old.copy(dayOfWeek = 5) // Friday Nov 6
        val change = editCalendarDayScoped(old, edited,
            mapOf(oldNext.key to oldNext), CalendarEditScope.THIS_AND_FUTURE, today)!!
        assertNull(change.runs[oldNext.key])
        assertEquals(day("2026-11-06"), change.runs["gmdss@2026-11-06"]?.dueDate)
        assertFalse(TaskSchedule.isCalendarDue(change.template, day("2026-11-09")))
        assertTrue(TaskSchedule.isCalendarDue(change.template, day("2026-11-06")))
    }

    @Test fun completedTodayWithoutMaterializedNextWeekKeepsTodayAndMovesNextWeek() {
        val old = weeklyTask()
        val today = day("2026-11-02")
        val complete = TaskRun(old.id, today, emptyList(), completed = true)
        val runs = mapOf(complete.key to complete)
        val edited = old.copy(dayOfWeek = 4)
        val preview = calendarDayEditPreview(old, edited, runs, today)!!
        assertEquals(day("2026-11-09"), preview.selectedRun.dueDate)
        assertEquals(day("2026-11-05"), preview.newDate)
        val result = editCalendarDayScoped(old, edited, runs,
            CalendarEditScope.THIS_AND_FUTURE, today)!!
        assertSame(complete, result.runs[complete.key])
        assertEquals(day("2026-11-05"), result.runs["gmdss@2026-11-05"]?.dueDate)
    }

    @Test fun skippedCurrentOccurrenceIsNotResurrectedByEdit() {
        val old = weeklyTask()
        val today = day("2026-11-02")
        val skipped = TaskRun(old.id, today, emptyList(), skipped = true)
        val preview = calendarDayEditPreview(old, old.copy(dayOfWeek = 4),
            mapOf(skipped.key to skipped), today)!!
        assertEquals(day("2026-11-09"), preview.selectedRun.dueDate)
    }

}
