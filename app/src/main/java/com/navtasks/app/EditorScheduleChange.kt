package com.navtasks.app

import java.time.LocalDate

/** Scope for editing the weekday or day of month of a calendar task. */
internal enum class CalendarEditScope { ONLY_THIS, THIS_AND_FUTURE }

internal data class CalendarDayEditPreview(
    val referenceDate: LocalDate,
    val selectedRun: TaskRun,
    val newDate: LocalDate,
)

internal data class CalendarDayEditResult(
    val template: TaskTemplate,
    val runs: Map<String, TaskRun>,
)

private fun lastEffectiveRule(task: TaskTemplate, date: LocalDate): CalendarScheduleRevision? =
    task.calendarRevisions.filter { !it.effectiveFrom.isAfter(date) }
        .maxByOrNull { it.effectiveFrom }

internal fun effectiveCalendarWeekday(task: TaskTemplate, date: LocalDate): Int =
    lastEffectiveRule(task, date)?.dayOfWeek ?: task.dayOfWeek

internal fun effectiveCalendarMonthDay(task: TaskTemplate, date: LocalDate): Int =
    lastEffectiveRule(task, date)?.dayOfMonth ?: task.dayOfMonth

/**
 * This is intentionally limited to changing WEEKLY weekday or MONTHLY month day.
 * Frequency/mode/startDate changes need separate history-aware rules.
 */
internal fun calendarDayEditPreview(
    original: TaskTemplate,
    edited: TaskTemplate,
    existing: Map<String, TaskRun>,
    today: LocalDate,
): CalendarDayEditPreview? {
    if (original.id != edited.id ||
        TaskSchedule.effectiveFrequency(original, today) != edited.frequency ||
        original.scheduleMode != ScheduleMode.CALENDAR ||
        edited.scheduleMode != ScheduleMode.CALENDAR ||
        original.startDate != edited.startDate ||
        !original.enabled || !edited.enabled
    ) return null

    val changed = when (TaskSchedule.effectiveFrequency(original, today)) {
        Frequency.WEEKLY ->
            effectiveCalendarWeekday(original, today) != edited.dayOfWeek
        Frequency.MONTHLY ->
            effectiveCalendarMonthDay(original, today) != edited.dayOfMonth
        else -> false
    }
    if (!changed) return null

    // Compute the first date of the new rule without applying any old revisions.
    val candidate = original.copy(
        frequency = edited.frequency,
        dayOfWeek = edited.dayOfWeek,
        dayOfMonth = edited.dayOfMonth,
        calendarRevisions = emptyList(),
    )
    val targetDate = TaskSchedule.nextCalendarDate(candidate, today) ?: return null

    val eligible = existing.values.filter {
        it.taskId == original.id && !it.completed && !it.skipped &&
            !it.displayDate.isBefore(today)
    }
    // The user's immediate concern: an occurrence already visible TODAY
    // must be selected, even if it originally came from an overdue date.
    val onToday = eligible.filter { it.displayDate == today }
        .minWithOrNull(compareBy<TaskRun> { if (it.dueDate == today) 0 else 1 }
            .thenBy { it.dueDate })
    // Today may already be completed or explicitly removed. Find the NEXT
    // unfinished scheduled occurrence instead of editing the completed one.
    var nextDue = TaskSchedule.nextCalendarDate(original, today)
    var scheduled: TaskRun? = null
    var attempts = 0
    while (scheduled == null && nextDue != null && attempts < 120) {
        attempts++
        val due = nextDue ?: break
        val key = original.id + "@" + due
        val saved = existing[key]
        if (saved == null) {
            scheduled = TaskRun(original.id, due, original.checklist)
        } else if (!saved.completed && !saved.skipped &&
            !saved.displayDate.isBefore(today)) {
            scheduled = saved
        }
        if (scheduled == null) {
            nextDue = TaskSchedule.nextCalendarDate(original, due.plusDays(1))
        }
    }
    val selected = onToday ?: listOfNotNull(scheduled, eligible.minByOrNull { it.displayDate })
        .minByOrNull { it.displayDate } ?: return null

    return CalendarDayEditPreview(today, selected, targetDate)
}

/**
 * The editor shows the effective weekday/month day, not the historical base.
 * An unrelated edit (e.g. renaming a task) must not rewrite that base.
 */
internal fun preserveHistoricalCalendarBase(
    original: TaskTemplate,
    edited: TaskTemplate,
    today: LocalDate,
): TaskTemplate {
    if (original.id != edited.id ||
        TaskSchedule.effectiveFrequency(original, today) != edited.frequency ||
        original.scheduleMode != ScheduleMode.CALENDAR ||
        edited.scheduleMode != ScheduleMode.CALENDAR ||
        original.startDate != edited.startDate
    ) return edited

    val unchanged = when (TaskSchedule.effectiveFrequency(original, today)) {
        Frequency.WEEKLY -> edited.dayOfWeek == effectiveCalendarWeekday(original, today)
        Frequency.MONTHLY -> edited.dayOfMonth == effectiveCalendarMonthDay(original, today)
        else -> true
    }
    return if (unchanged) edited.copy(
        frequency = original.frequency,
        dayOfWeek = original.dayOfWeek,
        dayOfMonth = original.dayOfMonth,
        calendarRevisions = original.calendarRevisions,
    ) else edited
}

/**
 * Pure transaction. Never rewrites a completed/skipped occurrence, never
 * changes a template's historical base weekday/month day, and keeps all
 * checklist IDs/checkedIds on the moved occurrence.
 */
internal fun editCalendarDayScoped(
    original: TaskTemplate,
    edited: TaskTemplate,
    existing: Map<String, TaskRun>,
    scope: CalendarEditScope,
    today: LocalDate,
): CalendarDayEditResult? {
    val preview = calendarDayEditPreview(original, edited, existing, today) ?: return null
    val selected = preview.selectedRun
    val target = preview.newDate
    val newTemplateBase = edited.copy(
        frequency = original.frequency,
        dayOfWeek = original.dayOfWeek,
        dayOfMonth = original.dayOfMonth,
        calendarRevisions = original.calendarRevisions,
    )
    val updated = existing.toMutableMap()

    if (scope == CalendarEditScope.ONLY_THIS) {
        // If date is already correct, simply save other non-schedule edits.
        if (selected.displayDate != target) {
            updated[selected.key] = selected.copy(
                postponedTo = target, autoTransferred = false,
            )
        }
        return CalendarDayEditResult(newTemplateBase, updated)
    }

    // Start the new rule TODAY. In particular, don't recreate the already
    // materialized run at today's OLD due date.
    val revision = CalendarScheduleRevision(
        effectiveFrom = today,
        firstDate = target,
        dayOfWeek = edited.dayOfWeek,
        dayOfMonth = edited.dayOfMonth,
        frequency = edited.frequency,
    )
    val template = newTemplateBase.copy(
        calendarRevisions = original.calendarRevisions
            .filter { it.effectiveFrom.isBefore(today) } + revision,
    )

    for ((key, run) in existing) {
        if (run.taskId == original.id && !run.completed && !run.skipped &&
            !run.transferred && !run.dueDate.isBefore(today)
        ) {
            updated.remove(key)
        }
    }
    // The chosen occurrence is explicitly moved, even when it was carried
    // from an earlier date. Other manually transferred tasks are untouched.
    updated.remove(selected.key)
    if (selected.dueDate.isBefore(today)) {
        // Prevent refresh() from recreating its original overdue occurrence.
        updated[selected.key] = selected.copy(
            skipped = true, postponedTo = null, autoTransferred = false,
        )
    }

    if (target != selected.displayDate) {
        val targetKey = original.id + "@" + target
        if (targetKey in updated) {
            // An existing completion/deletion wins over new schedule materialization.
            // Transfer the selected occurrence under its stable original key.
            updated[selected.key] = selected.copy(
                postponedTo = target, skipped = false, autoTransferred = false,
            )
        } else {
            val moved = selected.copy(
                dueDate = target, postponedTo = null, autoTransferred = false,
            )
            updated[moved.key] = moved
        }
    } else {
        // A moved recurrence already on the selected date should not disappear.
        updated[selected.key] = selected
    }

    return CalendarDayEditResult(template, updated)
}
