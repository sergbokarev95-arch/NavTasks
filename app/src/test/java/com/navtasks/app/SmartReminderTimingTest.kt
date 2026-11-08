package com.navtasks.app

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SmartReminderTimingTest {
    private val day = LocalDate.of(2026, 11, 15)
    private val reference = LocalTime.of(13, 0)
    private fun task(
        minutes: Int, hour: LocalTime? = null,
    ): TaskTemplate = TaskTemplate(
        id = "daily-reminder", title = "GMDSS",
        frequency = Frequency.DAILY,
        deadlineTime = hour,
        remindMinutesBefore = minutes,
    )
    private fun run() = TaskRun("daily-reminder", day, emptyList())

    @Test fun endOfDayUsesReferenceClockNotMidnightForReminders() {
        assertEquals(day.atTime(13, 0),
            SmartReminderTiming.trigger(task(0), run(), reference))
        assertEquals(day.atTime(12, 45),
            SmartReminderTiming.trigger(task(15), run(), reference))
        assertEquals(day.atTime(12, 0),
            SmartReminderTiming.trigger(task(60), run(), reference))
        assertEquals(day.minusDays(1).atTime(13, 0),
            SmartReminderTiming.trigger(task(1440), run(), reference))
    }

    @Test fun explicitTaskClockAlwaysOverridesDefaultReference() {
        assertEquals(day.atTime(9, 0),
            SmartReminderTiming.trigger(task(0, LocalTime.of(9, 0)), run(), reference))
        assertEquals(day.atTime(8, 45),
            SmartReminderTiming.trigger(task(15, LocalTime.of(9, 0)), run(), reference))
        assertEquals(day.atTime(8, 0),
            SmartReminderTiming.trigger(task(60, LocalTime.of(9, 0)), run(), reference))
        assertEquals(day.minusDays(1).atTime(9, 0),
            SmartReminderTiming.trigger(task(1440, LocalTime.of(9, 0)), run(), reference))
    }

    @Test fun endOfDayReminderDoesNotMakeWorkOverdueAtThirteen() {
        val normal = task(0)
        assertTrue(SmartReminderTiming.isAtOrBeforeDeadline(normal, run(), reference))
        assertTrue(day.atTime(23, 59).isBefore(day.plusDays(1).atStartOfDay()))
        // Notification scheduling is independent of the TaskStore's overdue
        // decision, which still uses the whole calendar day for default tasks.
        assertFalse(day.atTime(13, 1).toLocalDate().isBefore(day))
    }

    @Test fun postponedOccurrenceKeepsOriginalRunKeyButUsesNewDate() {
        val moved = run().copy(postponedTo = day.plusDays(2))
        assertEquals(run().key, moved.key)
        assertEquals(day.plusDays(2).atTime(12, 0),
            SmartReminderTiming.trigger(task(60), moved, reference))
    }

    @Test fun timestampsCanBeComparedAtMinutePrecision() {
        val time = LocalDateTime.of(2026, 11, 15, 12, 0)
        assertTrue(SmartReminderTiming.sameMinute(time, time.plusSeconds(35)))
        assertFalse(SmartReminderTiming.sameMinute(time, time.plusMinutes(1)))
    }
}
