package com.navtasks.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class WorkAgendaTest {
    private val now = LocalDate.of(2026, 11, 5)
    private fun amos(
        start: LocalDate = now,
        repeatMonths: Int = 0,
        remindDays: Int = 0,
    ) = WorkOrderEntry(
        id = "w1", title = "Portable Iridium phone",
        dueDate = start, startDate = start, createdAt = now,
        repeatMonths = repeatMonths, remindDaysBefore = remindDays,
    )
    private fun service(next: LocalDate, remindMonths: Int = 3) = ServiceEntry(
        id = "s1", equipment = "Gyro compass",
        lastDate = next.minusYears(1), nextDate = next,
        periodMonths = 12, remindMonths = remindMonths,
        requestNumber = "REQ-2026-91",
    )

    @Test fun newAmosStartsTodayWithoutRequiringWorkNumberOrChecklist() {
        val entry = amos()
        assertEquals("", entry.reference)
        assertTrue(amosVisibleOnDay(entry, now))
        assertTrue(amosPlannedOnDay(entry, now))
        assertFalse(amosPlannedOnDay(entry, now.plusDays(1)))
        assertTrue(workOrderComplete(entry, now).completed)
    }

    @Test fun tomorrowWorkIsVisibleAsUpcomingEvenWithoutNotification() {
        val entry = amos(start = now.plusDays(1))
        assertTrue(amosVisibleOnDay(entry, now))
        assertTrue(amosVisibleOnDay(entry, now.plusDays(1)))
        assertTrue(amosPlannedOnDay(entry, now.plusDays(1)))
        val distant = amos(start = now.plusDays(9))
        assertFalse(amosVisibleOnDay(distant, now))
    }

    @Test fun amosLeadTimeShowsFutureWorkInToday() {
        val entry = amos(start = now.plusDays(7), remindDays = 7)
        assertTrue(amosVisibleOnDay(entry, now))
        assertFalse(amosPlannedOnDay(entry, now))
        assertTrue(amosPlannedOnDay(entry, now.plusDays(7)))
    }

    @Test fun recurringCalendarUsesStartAnchorAcrossMonths() {
        val entry = amos(start = LocalDate.of(2026, 1, 31), repeatMonths = 1)
        assertTrue(amosPlannedOnDay(entry, LocalDate.of(2026, 2, 28)))
        assertTrue(amosPlannedOnDay(entry, LocalDate.of(2026, 3, 31)))
        assertFalse(amosPlannedOnDay(entry, LocalDate.of(2026, 3, 28)))
    }

    @Test fun serviceIsVisibleFromThreeMonthsBeforeAndForecastInCalendar() {
        val due = LocalDate.of(2027, 1, 16)
        val entry = service(due)
        assertTrue(serviceVisibleOnDay(entry, now))
        assertTrue(servicePlannedOnDay(entry, due))
        assertTrue(servicePlannedOnDay(entry, due.plusYears(1)))
        assertFalse(servicePlannedOnDay(entry, due.minusDays(1)))
        assertEquals("REQ-2026-91", entry.requestNumber)
        val nextCycle = nextServiceFromCompletion(entry, due)
        assertEquals("", nextCycle.requestNumber)
        assertEquals("REQ-2026-91", nextCycle.serviceHistory.single().requestNumber)
        assertEquals(due, nextCycle.serviceHistory.single().scheduledDate)
    }

    @Test fun maintenanceRequestHidesOnlyTodayReminderNotCalendarOrRecord() {
        val due = now.plusDays(12)
        val service = service(due)
        assertTrue(serviceVisibleOnDay(service, now))
        assertFalse(serviceReminderVisibleOnDay(service, now))
        assertTrue(servicePlannedOnDay(service, due))
        assertTrue(workAgendaExistsOnDay(due, emptyList(), listOf(service)))

        val withoutRequest = service.copy(requestNumber = "")
        assertTrue(serviceReminderVisibleOnDay(withoutRequest, now))
        assertEquals("REQ-2026-91", service.requestNumber)
    }

    @Test fun serviceRequestMatchingIsWhitespacedSafe() {
        val entry = service(now.plusDays(15)).copy(requestNumber = " ")
        assertTrue(serviceReminderVisibleOnDay(entry, now))
        assertFalse(serviceReminderVisibleOnDay(entry.copy(requestNumber = "AMOS-123"), now))
    }

    @Test fun completedOneOffServiceIsHiddenFromAgenda() {
        val finished = completeServiceCycle(
            service(now.plusDays(1)).copy(periodMonths = 0),
            now,
        )
        assertFalse(serviceReminderVisibleOnDay(finished, now.plusDays(1)))
        assertFalse(servicePlannedOnDay(finished, now.plusDays(1)))
        assertFalse(workAgendaExistsOnDay(
            now.plusDays(1), emptyList(), listOf(finished)))
    }

    @Test fun recurringAmosCompletionMovesToTodayCompletedAndKeepsNextCycle() {
        val planned = amos(repeatMonths = 1)
        val closed = workOrderComplete(planned, now)
        assertFalse(closed.completed)
        assertEquals(now.plusMonths(1), closed.dueDate)
        assertFalse(amosVisibleOnDay(closed, now))
        assertEquals(1, amosCompletedOnDay(listOf(closed), now).size)
        assertEquals(now, amosCompletedOnDay(listOf(closed), now).single().completion.completedAt)
        assertTrue(amosVisibleOnDay(closed, closed.dueDate.minusDays(1)))
        assertTrue(amosCompletedOnDay(listOf(closed), now.plusDays(1)).isEmpty())
    }

    @Test fun oneOffAmosCompletionIsShownOnlyOnItsActualCompletionDay() {
        val planned = amos(start = now.minusDays(2))
        val closed = workOrderComplete(planned, now)
        assertTrue(closed.completed)
        assertFalse(amosVisibleOnDay(closed, now))
        assertEquals(1, amosCompletedOnDay(listOf(closed), now).size)
        assertTrue(amosCompletedOnDay(listOf(closed), now.minusDays(2)).isEmpty())
    }

    @Test fun serviceCompletionAppearsInCompletedAndNotAgainAsTodayReminder() {
        val planned = service(now).copy(periodMonths = 1, requestNumber = "")
        val closed = completeServiceCycle(planned, now)
        assertEquals(now.plusMonths(1), closed.nextDate)
        assertEquals(1, serviceCompletedOnDay(listOf(closed), now).size)
        assertFalse(serviceReminderVisibleOnDay(closed, now))
        assertTrue(serviceReminderVisibleOnDay(closed, now.plusDays(1)))
        assertTrue(serviceCompletedOnDay(listOf(closed), now.plusDays(1)).isEmpty())
    }

    @Test fun amosOverviewCountersAndRowsMatchTheSameFilters() {
        val overdue = amos(start = now.minusDays(1)).copy(id = "overdue")
        val soon = amos(start = now.plusDays(3)).copy(id = "soon")
        val distant = amos(start = now.plusDays(15)).copy(id = "distant")
        val archived = workOrderComplete(
            amos(start = now.minusDays(5)).copy(id = "archived"), now)
        val orders = listOf(overdue, soon, distant, archived)
        assertEquals(3, filterAmosOverview(orders, now, WorkOverviewFilter.ALL).size)
        assertEquals(listOf("soon"),
            filterAmosOverview(orders, now, WorkOverviewFilter.SOON).map { it.id })
        assertEquals(listOf("overdue"),
            filterAmosOverview(orders, now, WorkOverviewFilter.OVERDUE).map { it.id })
    }

    @Test fun serviceOverviewCountersUseSameSoonAndOverdueDefinitionsAsCards() {
        val late = service(now.minusDays(1)).copy(id = "late")
        val soon = service(now.plusDays(20)).copy(id = "soon")
        val far = service(now.plusMonths(8), 0).copy(id = "far")
        val all = listOf(late, soon, far)
        assertEquals(3, filterServiceOverview(all, now, WorkOverviewFilter.ALL).size)
        assertEquals(listOf("soon"),
            filterServiceOverview(all, now, WorkOverviewFilter.SOON).map { it.id })
        assertEquals(listOf("late"),
            filterServiceOverview(all, now, WorkOverviewFilter.OVERDUE).map { it.id })
        assertFalse(serviceDueSoon(late, now))
    }

    @Test fun calendarDetectsScheduledWorkEvenWithoutOrdinaryTasks() {
        val entry = amos(start = now.plusDays(2))
        assertTrue(workAgendaExistsOnDay(now.plusDays(2), listOf(entry), emptyList()))
        assertFalse(workAgendaExistsOnDay(now, listOf(entry), emptyList()))
    }
}
