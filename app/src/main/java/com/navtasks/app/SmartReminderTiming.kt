package com.navtasks.app

import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Notification time and task deadline are distinct concepts.
 *
 * A deadline without an explicit hour is still the end of its calendar day.
 * The configured reference clock controls when its reminder is sent, not when
 * the work becomes overdue. Explicit task hours always override that clock.
 */
internal object SmartReminderTiming {
    fun trigger(task: TaskTemplate, run: TaskRun, reference: LocalTime,
        minutes: Int = task.remindMinutesBefore): LocalDateTime =
        run.displayDate.atTime(task.windowStartTime ?: task.deadlineTime ?: reference)
            .minusMinutes(minutes.toLong().coerceAtLeast(0))

    fun offsets(task: TaskTemplate): List<Int> =
        if (!task.notificationsEnabled) emptyList()
        else (listOf(task.remindMinutesBefore) + task.additionalReminderMinutes)
            .filter { it >= 0 }.distinct()

    fun alarmKey(task: TaskTemplate, run: TaskRun, minutes: Int): String =
        if (!task.offsetReminderKeys && minutes == task.remindMinutesBefore) run.key
        else "${run.key}|$minutes"

    fun deliveryAlarmKey(task: TaskTemplate, run: TaskRun, minutes: Int): String =
        if (task.legacyReminderOffset == minutes) run.key else alarmKey(task, run, minutes)

    fun alarmOffset(task: TaskTemplate, key: String): Int? {
        val encoded = '|' in key
        val offset = if (encoded) key.substringAfterLast('|').toIntOrNull()
            else task.remindMinutesBefore
        return offset?.takeIf { it in offsets(task) &&
            encoded == (task.offsetReminderKeys || it != task.remindMinutesBefore) }
    }

    fun atDeadline(task: TaskTemplate): Boolean =
        task.deadlineTime != null && task.windowStartTime == null && 0 in offsets(task)


    fun isAtOrBeforeDeadline(
        task: TaskTemplate, run: TaskRun, reference: LocalTime,
    ): Boolean = trigger(task, run, reference) <=
        (task.deadlineTime?.let { run.displayDate.atTime(it) }
            ?: run.displayDate.plusDays(1).atStartOfDay())

    fun sameMinute(a: LocalDateTime, b: LocalDateTime): Boolean =
        a.withSecond(0).withNano(0) == b.withSecond(0).withNano(0)
}

/** One device-local midnight rollover regardless of notifications preference. */
internal fun nextNavWidgetRefresh(now: LocalDateTime): LocalDateTime =
    now.toLocalDate().plusDays(1).atTime(0, 5)
