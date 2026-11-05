package com.navtasks.app

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Cross-module agenda for AMOS and equipment maintenance.
 * These functions never create TaskRuns, modify task frequency, or write data.
 */
internal fun amosVisibleOnDay(work: WorkOrderEntry, day: LocalDate): Boolean {
    if (work.completed) return false
    // The current occurrence is kept visible while approaching and overdue.
    val firstVisible = work.dueDate.minusDays(work.remindDaysBefore.toLong())
    return !day.isBefore(firstVisible)
}

internal fun serviceVisibleOnDay(service: ServiceEntry, day: LocalDate): Boolean =
    !day.isBefore(serviceWarningStart(service))

/** Fixed calendar cycle anchored at the manually selected AMOS start date. */
internal fun amosPlannedOnDay(work: WorkOrderEntry, date: LocalDate): Boolean {
    val start = work.startDate ?: work.dueDate
    if (date.isBefore(start)) return false
    if (!work.recurring) return date == start
    if (work.repeatDays > 0) {
        return ChronoUnit.DAYS.between(start, date) % work.repeatDays == 0L
    }
    val months = ChronoUnit.MONTHS.between(start.withDayOfMonth(1),
        date.withDayOfMonth(1))
    if (months < 0 || months % work.repeatMonths != 0L) return false
    return start.plusMonths(months) == date
}

/** Service calendar markers are informative, including projected future servicing. */
internal fun servicePlannedOnDay(service: ServiceEntry, date: LocalDate): Boolean {
    if (date.isBefore(service.nextDate)) return false
    if (date == service.nextDate) return true
    if (service.periodMonths == 0) return false
    val months = ChronoUnit.MONTHS.between(service.nextDate.withDayOfMonth(1),
        date.withDayOfMonth(1))
    return months >= 0 && months % service.periodMonths == 0L &&
        service.nextDate.plusMonths(months) == date
}

internal fun workAgendaExistsOnDay(
    day: LocalDate, works: List<WorkOrderEntry>, services: List<ServiceEntry>,
): Boolean = works.any { amosPlannedOnDay(it, day) } ||
    services.any { servicePlannedOnDay(it, day) }
