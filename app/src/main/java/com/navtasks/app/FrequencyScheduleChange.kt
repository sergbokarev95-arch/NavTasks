package com.navtasks.app

import java.time.LocalDate

/** Only calendar-based Daily / Weekly / Monthly series are handled here. */
internal data class FrequencyChangePreview(
    val referenceDate: LocalDate,
    val selectedRun: TaskRun,
    val newDate: LocalDate,
    val oldFrequency: Frequency,
    val newFrequency: Frequency,
)

internal data class FrequencyChangeResult(
    val template: TaskTemplate,
    val runs: Map<String, TaskRun>,
)

private val supportedCalendarFrequencies =
    setOf(Frequency.DAILY, Frequency.WEEKLY, Frequency.MONTHLY)

/**
 * An edit to a template's frequency must not reinterpret any earlier due dates.
 * Select today's unfinished occurrence first; otherwise the nearest unfinished
 * occurrence of the old schedule. The new date follows the requested cadence.
 */
internal fun calendarFrequencyChangePreview(
    original: TaskTemplate,
    edited: TaskTemplate,
    existing: Map<String, TaskRun>,
    today: LocalDate,
): FrequencyChangePreview? {
    val originalFrequency = TaskSchedule.effectiveFrequency(original, today)
    if (original.id != edited.id ||
        original.scheduleMode != ScheduleMode.CALENDAR ||
        edited.scheduleMode != ScheduleMode.CALENDAR ||
        original.startDate != edited.startDate ||
        !original.enabled || !edited.enabled ||
        originalFrequency !in supportedCalendarFrequencies ||
        edited.frequency !in supportedCalendarFrequencies ||
        originalFrequency == edited.frequency
    ) return null

    // A frequency switch is an explicit new rule; don't inherit old revisions.
    val candidate = edited.copy(calendarRevisions = emptyList())
    var newDate = TaskSchedule.nextCalendarDate(candidate, today) ?: return null
    // An already completed or removed occurrence at the target date is immutable.
    // Start the new cadence from the next possible date instead of duplicating it.
    var targetAttempts = 0
    while (targetAttempts++ < 40) {
        val occupied = existing[original.id + "@" + newDate]
        if (occupied == null || (!occupied.completed && !occupied.skipped)) break
        newDate = TaskSchedule.nextCalendarDate(candidate, newDate.plusDays(1))
            ?: return null
    }
    if (targetAttempts > 40) return null

    val eligible = existing.values.filter {
        it.taskId == original.id && !it.completed && !it.skipped &&
            !it.displayDate.isBefore(today)
    }
    val onToday = eligible.filter { it.displayDate == today }
        .minWithOrNull(compareBy<TaskRun> { if (it.dueDate == today) 0 else 1 }
            .thenBy { it.dueDate })

    var nextDue = TaskSchedule.nextCalendarDate(original, today)
    var nextScheduled: TaskRun? = null
    var attempts = 0
    while (nextScheduled == null && nextDue != null && attempts++ < 120) {
        val due = nextDue ?: break
        val saved = existing[original.id + "@" + due]
        if (saved == null) {
            nextScheduled = TaskRun(original.id, due, original.checklist)
        } else if (!saved.completed && !saved.skipped &&
            !saved.displayDate.isBefore(today)) {
            nextScheduled = saved
        }
        if (nextScheduled == null) {
            nextDue = TaskSchedule.nextCalendarDate(original, due.plusDays(1))
        }
    }

    val selected = onToday ?: listOfNotNull(
        nextScheduled, eligible.minByOrNull { it.displayDate },
    ).minByOrNull { it.displayDate } ?: return null

    return FrequencyChangePreview(today, selected, newDate,
        originalFrequency, edited.frequency)
}

/**
 * Pure transaction: preserve the historical template frequency, all completed
 * runs and checklist progress. Unfinished ordinary future occurrences follow
 * the new rule only when the user explicitly chooses THIS_AND_FUTURE.
 */
internal fun editCalendarFrequencyScoped(
    original: TaskTemplate,
    edited: TaskTemplate,
    existing: Map<String, TaskRun>,
    scope: CalendarEditScope,
    today: LocalDate,
): FrequencyChangeResult? {
    val preview = calendarFrequencyChangePreview(original, edited, existing, today)
        ?: return null
    val selected = preview.selectedRun
    val target = preview.newDate
    val base = edited.copy(
        frequency = original.frequency,
        dayOfWeek = original.dayOfWeek,
        dayOfMonth = original.dayOfMonth,
        calendarRevisions = original.calendarRevisions,
    )
    val updated = existing.toMutableMap()

    if (scope == CalendarEditScope.ONLY_THIS) {
        // An occurrence may remain on the same date but change its visible
        // category. Store the override on the run, not the recurring template.
        updated[selected.key] = selected.copy(
            postponedTo = if (target != selected.displayDate) target else selected.postponedTo,
            autoTransferred = false,
            frequencyOverride = edited.frequency,
        )
        return FrequencyChangeResult(base, updated)
    }

    val revision = CalendarScheduleRevision(
        effectiveFrom = today,
        firstDate = target,
        dayOfWeek = edited.dayOfWeek,
        dayOfMonth = edited.dayOfMonth,
        frequency = edited.frequency,
    )
    val task = base.copy(
        calendarRevisions = original.calendarRevisions
            .filter { it.effectiveFrom.isBefore(today) } + revision,
    )

    for ((key, run) in existing) {
        // Preserve historical completion, skipped records and independent
        // manual transfers. Only ordinary pending future runs are replaced.
        if (run.taskId == original.id && !run.completed && !run.skipped &&
            !run.transferred && !run.dueDate.isBefore(today)
        ) updated.remove(key)
    }
    updated.remove(selected.key)
    if (selected.dueDate.isBefore(today)) {
        // Leave a tombstone at the earlier key to prevent re-materialization.
        updated[selected.key] = selected.copy(
            skipped = true, postponedTo = null, autoTransferred = false,
        )
    }
    if (target == selected.displayDate) {
        updated[selected.key] = selected.copy(frequencyOverride = null)
    } else {
        val targetKey = original.id + "@" + target
        if (targetKey in updated) {
            // Don't overwrite completed/skipped records at the destination.
            updated[selected.key] = selected.copy(
                postponedTo = target, skipped = false, autoTransferred = false,
                frequencyOverride = null,
            )
        } else {
            val moved = selected.copy(
                dueDate = target, postponedTo = null, autoTransferred = false,
                frequencyOverride = null,
            )
            updated[moved.key] = moved
        }
    }
    return FrequencyChangeResult(task, updated)
}
