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
        assertEquals("REQ-2026-91", nextServiceFromCompletion(entry, due).requestNumber)
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

    @Test fun calendarDetectsScheduledWorkEvenWithoutOrdinaryTasks() {
        val entry = amos(start = now.plusDays(2))
        assertTrue(workAgendaExistsOnDay(now.plusDays(2), listOf(entry), emptyList()))
        assertFalse(workAgendaExistsOnDay(now, listOf(entry), emptyList()))
    }
}
