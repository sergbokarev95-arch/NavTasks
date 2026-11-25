package com.navtasks.app

import java.time.LocalDate
import java.time.LocalTime
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class NavEventEditorModelTest {
    private val day = LocalDate.of(2026, 11, 25)
    private val clock = LocalTime.of(13, 0)
    private val original = TaskTemplate(id = "one", title = "Test", frequency = Frequency.DAILY,
        startDate = day, deadlineTime = LocalTime.of(12, 0), remindMinutesBefore = 60,
        checklist = listOf(CheckItem(id = "check", title = "Log", comment = "Keep")))
    private val run = TaskRun(original.id, day, original.checklist)

    @Test fun oldJsonStillLoadsWithExactOriginalDeadlineAndSingleReminder() {
        val json = original.toJson()
        for (key in listOf("windowStartTime", "additionalReminderMinutes", "offsetReminderKeys", "legacyReminderOffset")) json.remove(key)
        val loaded = TaskTemplate.fromJson(JSONObject(json.toString()))
        assertEquals(original, loaded)
        assertEquals(listOf(60), SmartReminderTiming.offsets(loaded))
        assertEquals(run.key, SmartReminderTiming.alarmKey(loaded, run, 60))
        assertEquals(day.atTime(11, 0), SmartReminderTiming.trigger(loaded, run, clock))
    }

    @Test fun newWindowAndMultipleOffsetsRoundTripWithChecklistAndRevisionData() {
        val task = original.copy(windowStartTime = LocalTime.of(8, 0), additionalReminderMinutes = listOf(0, 1440, 10080),
            offsetReminderKeys = true, legacyReminderOffset = 60,
            calendarRevisions = listOf(CalendarScheduleRevision(day, day, 3, 25, Frequency.WEEKLY)))
        assertEquals(task, TaskTemplate.fromJson(JSONObject(task.toJson().toString())))
        assertEquals(day.atTime(7, 0), SmartReminderTiming.trigger(task, run, clock, 60))
        assertEquals(day.atTime(8, 0), SmartReminderTiming.trigger(task, run, clock, 0))
        assertEquals(day.minusWeeks(1).atTime(8, 0), SmartReminderTiming.trigger(task, run, clock, 10080))
        assertFalse(SmartReminderTiming.atDeadline(task))
        assertEquals(LocalTime.of(12, 0), task.deadlineTime)
    }

    @Test fun separateAlarmKeysSurviveRemovingPrimaryAndRejectRemovedOffsets() {
        val task = original.copy(offsetReminderKeys = true, additionalReminderMinutes = listOf(15, 0),
            legacyReminderOffset = 60)
        val key = SmartReminderTiming.alarmKey(task, run, 15)
        assertEquals("${run.key}|15", key)
        assertEquals(15, SmartReminderTiming.alarmOffset(task, key))
        assertNull(SmartReminderTiming.alarmOffset(task, run.key))
        assertEquals(run.key, SmartReminderTiming.deliveryAlarmKey(task, run, 60))
        val changed = task.copy(remindMinutesBefore = 15, additionalReminderMinutes = listOf(0))
        assertEquals(key, SmartReminderTiming.alarmKey(changed, run, 15))
        assertNull(SmartReminderTiming.alarmOffset(changed, "${run.key}|60"))
        assertNull(SmartReminderTiming.alarmOffset(changed.copy(notificationsEnabled = false), key))
        assertTrue(SmartReminderTiming.atDeadline(changed))
    }

    @Test fun invalidImportedWindowDoesNotChangeDeadline() {
        val json = original.toJson().put("windowStartTime", "18:00")
        val task = TaskTemplate.fromJson(json)
        assertNull(task.windowStartTime)
        assertEquals(original.deadlineTime, task.deadlineTime)
    }

    @Test fun repeatChoicesMapDirectlyToExistingRoutineCategories() {
        val choices = navEventRepeatChoices(UiLanguage.RU)
        assertEquals(listOf("EXTRA", "DAILY", "WEEKLY", "MONTHLY", "custom"), choices.map { it.first })
        assertEquals(listOf("none", "0", "5", "10", "15", "30", "60", "120", "1440", "2880", "10080", "custom"),
            navEventReminderChoices(UiLanguage.RU).map { it.first })
    }

    @Test fun renamingEffectiveRevisedTaskDoesNotRewriteHistoricalBaseFrequency() {
        val task = original.copy(calendarRevisions = listOf(CalendarScheduleRevision(day, day, 3, 25, Frequency.WEEKLY)))
        val edited = task.copy(title = "New title", frequency = Frequency.WEEKLY, dayOfWeek = 3)
        val saved = preserveHistoricalCalendarBase(task, edited, day)
        assertEquals(Frequency.DAILY, saved.frequency)
        assertEquals(Frequency.WEEKLY, TaskSchedule.effectiveFrequency(saved, day))
        assertEquals(task.calendarRevisions, saved.calendarRevisions)
    }
}
