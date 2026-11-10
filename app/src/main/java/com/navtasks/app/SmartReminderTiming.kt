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
    fun trigger(task: TaskTemplate, run: TaskRun, reference: LocalTime): LocalDateTime =
        run.displayDate.atTime(task.deadlineTime ?: reference)
            .minusMinutes(task.remindMinutesBefore.toLong().coerceAtLeast(0))

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
