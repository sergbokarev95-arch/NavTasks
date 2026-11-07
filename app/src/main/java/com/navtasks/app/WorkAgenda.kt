package com.navtasks.app

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Cross-module agenda for AMOS and equipment maintenance.
 * These functions never create TaskRuns, modify task frequency, or write data.
 */
internal fun amosVisibleOnDay(work: WorkOrderEntry, day: LocalDate): Boolean {
    if (work.closedAt != null || work.completed ||
        work.executionHistory.any { it.completedAt == day }) return false
    // Today is a due-date agenda, not an early-warning list.
    // remindDaysBefore is exclusively for notification scheduling and AMOS's
    // Soon filter; it must not make a future work order appear today.
    return day == work.dueDate
}

internal fun serviceVisibleOnDay(service: ServiceEntry, day: LocalDate): Boolean =
    !service.unplanned && service.closedAt == null && service.completedAt == null &&
        day == service.nextDate

/** A registered AMOS service request suppresses the outstanding Today card. */
internal fun serviceReminderVisibleOnDay(service: ServiceEntry, day: LocalDate): Boolean =
    service.requestNumber.isBlank() && serviceVisibleOnDay(service, day) &&
        service.serviceHistory.none { it.completedAt == day }

/** Fixed calendar cycle anchored at the manually selected AMOS start date. */
internal fun amosPlannedOnDay(work: WorkOrderEntry, date: LocalDate): Boolean {
    if (work.closedAt != null) return false
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
    if (service.unplanned || service.closedAt != null || service.completedAt != null)
        return false
    val due = service.nextDate ?: return false
    if (date.isBefore(due)) return false
    if (date == due) return true
    if (service.periodMonths == 0) return false
    val months = ChronoUnit.MONTHS.between(due.withDayOfMonth(1),
        date.withDayOfMonth(1))
    return months >= 0 && months % service.periodMonths == 0L &&
        due.plusMonths(months) == date
}

internal fun workAgendaExistsOnDay(
    day: LocalDate, works: List<WorkOrderEntry>, services: List<ServiceEntry>,
): Boolean = works.any { amosPlannedOnDay(it, day) } ||
    services.any { servicePlannedOnDay(it, day) }


/**
 * Overview counters and rows use identical conditions. These are presentation
 * filters only; neither schedules nor persisted work records are modified.
 */
internal enum class WorkOverviewFilter { ALL, SOON, OVERDUE }

internal fun filterAmosOverview(
    orders: List<WorkOrderEntry>, today: LocalDate, filter: WorkOverviewFilter,
): List<WorkOrderEntry> = orders.filter { order ->
    when (filter) {
        WorkOverviewFilter.ALL -> order.closedAt == null
        WorkOverviewFilter.SOON -> order.closedAt == null &&
            workOrderState(order, today) == WorkOrderState.SOON
        WorkOverviewFilter.OVERDUE -> order.closedAt == null &&
            workOrderState(order, today) == WorkOrderState.OVERDUE
    }
}

internal fun serviceDueSoon(service: ServiceEntry, today: LocalDate): Boolean {
    if (service.unplanned || service.closedAt != null || service.completedAt != null)
        return false
    val due = service.nextDate ?: return false
    return !due.isBefore(today) &&
        (due <= today.plusDays(30) ||
            (service.remindMonths > 0 && !today.isBefore(serviceWarningStart(service))))
}

internal fun filterServiceOverview(
    services: List<ServiceEntry>, today: LocalDate, filter: WorkOverviewFilter,
): List<ServiceEntry> = services.filter { entry ->
    when (filter) {
        WorkOverviewFilter.ALL -> entry.closedAt == null
        WorkOverviewFilter.SOON -> serviceDueSoon(entry, today)
        WorkOverviewFilter.OVERDUE ->
            !entry.unplanned && entry.closedAt == null && entry.completedAt == null &&
                entry.nextDate?.isBefore(today) == true
    }
}

/** One completion belongs to its actual execution date, not the next schedule. */
internal fun amosCompletedOnDay(
    orders: List<WorkOrderEntry>, day: LocalDate,
): List<WorkOrderHistoryRecord> =
    orders.asSequence()
        .filter { work ->
            work.closedAt == null &&
                (work.completedAt == day || work.executionHistory.any { it.completedAt == day })
        }
        .flatMap { workOrderHistoryRecords(listOf(it)).asSequence() }
        .filter { it.completion.completedAt == day }
        .sortedByDescending { it.completion.completedAt }
        .toList()

internal data class ServiceAgendaCompletion(
    val service: ServiceEntry,
    val completion: ServiceCompletion,
)

internal fun serviceCompletedOnDay(
    services: List<ServiceEntry>, day: LocalDate,
): List<ServiceAgendaCompletion> = services.flatMap { service ->
    service.serviceHistory.filter {
        service.closedAt == null && it.completedAt == day
    }.map {
        ServiceAgendaCompletion(service, it)
    }
}
