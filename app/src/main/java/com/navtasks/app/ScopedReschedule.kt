package com.navtasks.app

import java.time.LocalDate

internal data class ScopedCalendarMoveResult(
    val template: TaskTemplate,
    val runs: Map<String, TaskRun>,
)

/**
 * A scoped change keeps the template ID and historical completed runs.
 * Only unfinished occurrences from the selected display date onward are
 * regenerated, while the selected checklist's partial progress is carried.
 * This is intentionally limited to CALENDAR repetition.
 */
internal fun scopedCalendarMove(
    task: TaskTemplate,
    existing: Map<String, TaskRun>,
    selected: TaskRun,
    newDate: LocalDate,
): ScopedCalendarMoveResult? {
    if (task.scheduleMode != ScheduleMode.CALENDAR ||
        TaskSchedule.effectiveFrequency(task, selected.displayDate) == Frequency.EXTRA ||
        !task.enabled || selected.taskId != task.id ||
        selected.completed || selected.skipped ||
        newDate.isBefore(selected.displayDate) ||
        newDate == selected.displayDate
    ) return null

    val effectiveFrom = selected.displayDate
    val revision = CalendarScheduleRevision(
        effectiveFrom = effectiveFrom,
        firstDate = newDate,
        dayOfWeek = newDate.dayOfWeek.value,
        dayOfMonth = newDate.dayOfMonth,
        frequency = TaskSchedule.effectiveFrequency(task, effectiveFrom),
    )
    val changed = task.copy(
        calendarRevisions = task.calendarRevisions
            .filter { it.effectiveFrom.isBefore(effectiveFrom) } + revision,
    )

    val updated = existing.toMutableMap()
    for ((key, run) in existing) {
        if (run.taskId == task.id && !run.completed && !run.skipped &&
            (run.dueDate >= effectiveFrom || key == selected.key)
        ) {
            updated.remove(key)
        }
    }

    // If an older occurrence was already transferred onto the selected day,
    // keep a tombstone at its original key. Otherwise refresh() would recreate
    // that older occurrence under the previous calendar rule.
    if (selected.dueDate.isBefore(effectiveFrom)) {
        updated[selected.key] = selected.copy(
            skipped = true, postponedTo = null, autoTransferred = false,
        )
    }

    // Preserve the selected occurrence's partially completed checklist.
    // If a distinct completed occurrence already occupies the target date,
    // retain its history and transfer the selected run without a key collision.
    val newKey = selected.taskId + "@" + newDate
    if (newKey in updated) {
        // Keep both records: one historical completion and one transfer.
        // A transferred older key is safe because refresh() uses putIfAbsent.
        updated[selected.key] = selected.copy(
            postponedTo = newDate, autoTransferred = false, skipped = false,
        )
    } else {
        val moved = selected.copy(
            dueDate = newDate, postponedTo = null, autoTransferred = false,
        )
        updated[moved.key] = moved
    }

    return ScopedCalendarMoveResult(changed, updated)
}

internal fun canMoveSeries(task: TaskTemplate, run: TaskRun, newDate: LocalDate): Boolean =
    task.scheduleMode == ScheduleMode.CALENDAR &&
        TaskSchedule.effectiveFrequency(task, run.displayDate) != Frequency.EXTRA &&
        task.enabled &&
        !run.completed && !run.skipped &&
        newDate.isAfter(run.displayDate)
