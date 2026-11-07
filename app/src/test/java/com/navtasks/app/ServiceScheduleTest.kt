package com.navtasks.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ServiceScheduleTest {
    private fun service(
        last: LocalDate = LocalDate.of(2026, 1, 31),
        next: LocalDate = LocalDate.of(2027, 1, 31),
        period: Int = 12,
        alert: Int = 3,
    ) = ServiceEntry(
        id = "test", equipment = "Ship equipment",
        lastDate = last, nextDate = next, comment = "Checklist instructions",
        periodMonths = period, remindMonths = alert,
    )

    @Test fun threeMonthWarningUsesCalendarMonths() {
        assertEquals(LocalDate.of(2026, 10, 31), service().let(::serviceWarningStart))
    }

    @Test fun eighteenMonthIntervalUsesRealCalendarAndLeapYears() {
        val item = service(
            last = LocalDate.of(2024, 2, 29),
            next = LocalDate.of(2025, 8, 29),
            period = 18,
        )
        val finished = nextServiceFromCompletion(item, LocalDate.of(2026, 8, 31))
        assertEquals(LocalDate.of(2026, 8, 31), finished.lastDate)
        assertEquals(LocalDate.of(2028, 2, 29), finished.nextDate)
        assertEquals(item.comment, finished.comment)
        assertEquals(item.id, finished.id)
    }

    @Test fun fiveYearAndThreeYearCyclesAreAvailable() {
        val fiveYears = service(period = 60)
        assertEquals(LocalDate.of(2031, 11, 1),
            nextServiceFromCompletion(fiveYears, LocalDate.of(2026, 11, 1)).nextDate)
        val threeYears = service(period = 36)
        assertEquals(LocalDate.of(2029, 11, 1),
            nextServiceFromCompletion(threeYears, LocalDate.of(2026, 11, 1)).nextDate)
    }

    @Test fun customNineAndTwentySevenMonthIntervalsAreSupported() {
        val nine = service(period = 9)
        val result = nextServiceFromCompletion(nine, LocalDate.of(2026, 1, 31))
        assertEquals(LocalDate.of(2026, 10, 31), result.nextDate)
        assertEquals(9, result.periodMonths)
        val twentySeven = service(period = 27)
        assertEquals(LocalDate.of(2029, 2, 28),
            nextServiceFromCompletion(twentySeven, LocalDate.of(2026, 11, 30)).nextDate)
    }

    @Test fun customEightYearPeriodAndReminderSurviveServiceCompletion() {
        val custom = service(period = 96, alert = 3)
        val completed = nextServiceFromCompletion(custom, LocalDate.of(2026, 11, 1))
        assertEquals(96, completed.periodMonths)
        assertEquals(3, completed.remindMonths)
        assertEquals(custom.id, completed.id)
        assertEquals(custom.comment, completed.comment)
        assertEquals(LocalDate.of(2034, 11, 1), completed.nextDate)
    }

    @Test fun customIntervalValidationRejectsOutOfRangeValues() {
        assertTrue(runCatching { service(period = 1201) }.isFailure)
        assertTrue(runCatching { service(period = -1) }.isFailure)
        assertFalse(runCatching { service(period = 1) }.isFailure)
        assertFalse(runCatching { service(period = 1200) }.isFailure)
    }

    @Test fun manualRecordDoesNotGuessNextDate() {
        val manual = service(period = 0, alert = 0)
        assertTrue(runCatching { nextServiceFromCompletion(manual, LocalDate.now()) }.isFailure)
        assertEquals(LocalDate.of(2027, 1, 31), manual.nextDate)
    }

    @Test fun actualServiceDateControlsNextDueAndArchivesAmosRequest() {
        val item = service(next = LocalDate.of(2027, 1, 16), period = 12).copy(
            requestNumber = "252-03524",
        )
        val done = completeServiceCycle(item, LocalDate.of(2026, 11, 8))
        assertEquals(LocalDate.of(2027, 11, 8), done.nextDate)
        assertEquals(LocalDate.of(2026, 11, 8), done.lastDate)
        assertEquals("", done.requestNumber)
        assertEquals(1, done.serviceHistory.size)
        val archived = done.serviceHistory.single()
        assertEquals(LocalDate.of(2027, 1, 16), archived.scheduledDate)
        assertEquals(LocalDate.of(2026, 11, 8), archived.completedAt)
        assertEquals("252-03524", archived.requestNumber)
        assertEquals(item.equipment, archived.equipment)
    }

    @Test fun eachServiceCyclePreservesPreviousRequisitionHistory() {
        val original = service(next = LocalDate.of(2026, 11, 6)).copy(
            requestNumber = "REQ-1",
        )
        val once = completeServiceCycle(original, LocalDate.of(2026, 11, 8))
        val twice = completeServiceCycle(
            once.copy(requestNumber = "REQ-2"),
            LocalDate.of(2027, 11, 9),
        )
        assertEquals(2, twice.serviceHistory.size)
        assertEquals(listOf("REQ-1", "REQ-2"),
            twice.serviceHistory.map { it.requestNumber })
        assertEquals("", twice.requestNumber)
        assertEquals(LocalDate.of(2028, 11, 9), twice.nextDate)
        assertEquals("REQ-1", original.requestNumber)
    }

    @Test fun oneOffServiceCompletesWithoutFakeNewDueDate() {
        val once = service(period = 0).copy(requestNumber = "AMOS-57")
        val result = completeServiceCycle(once, LocalDate.of(2026, 12, 25))
        assertEquals(LocalDate.of(2026, 12, 25), result.completedAt)
        assertEquals(once.nextDate, result.nextDate)
        assertEquals("AMOS-57", result.serviceHistory.single().requestNumber)
        assertEquals("", result.requestNumber)
        assertFalse(serviceVisibleOnDay(result, requireNotNull(result.nextDate)))
        assertFalse(servicePlannedOnDay(result, requireNotNull(result.nextDate)))
        assertTrue(runCatching {
            completeServiceCycle(result, LocalDate.of(2026, 12, 26))
        }.isFailure)
    }

    @Test fun unplannedRequestHasNoFakeDueDateOrAutomaticCycle() {
        val today = LocalDate.of(2026, 11, 7)
        val request = ServiceEntry(
            id = "unplanned-radar", equipment = "PC FOR ICE RADAR",
            lastDate = today, nextDate = null, comment = "Unscheduled repair",
            periodMonths = 0, remindMonths = 0, requestNumber = "AMOS-2501",
            unplanned = true,
        )
        assertEquals(null, request.nextDate)
        assertFalse(serviceVisibleOnDay(request, today))
        assertFalse(servicePlannedOnDay(request, today))
        assertFalse(servicePlannedOnDay(request, today.plusYears(1)))
        assertFalse(serviceDueSoon(request, today))
        assertTrue(filterServiceOverview(listOf(request), today, WorkOverviewFilter.SOON).isEmpty())
        assertTrue(filterServiceOverview(listOf(request), today.plusMonths(6),
            WorkOverviewFilter.OVERDUE).isEmpty())
        assertEquals(listOf(request),
            filterServiceOverview(listOf(request), today, WorkOverviewFilter.ALL))
        assertTrue(runCatching { serviceWarningStart(request) }.isFailure)
    }

    @Test fun unplannedRequestDateIsEditableButNeverBecomesDueDate() {
        val today = LocalDate.of(2026, 11, 7)
        val entry = ServiceEntry(
            id = "ice-radar-req",
            equipment = "PC FOR ICE RADAR",
            lastDate = today,
            nextDate = null,
            requestNumber = "252-03515",
            unplanned = true,
        )
        val earlier = entry.copy(lastDate = today.minusDays(15))
        val later = entry.copy(lastDate = today.plusDays(15))
        for (request in listOf(earlier, later)) {
            assertEquals(null, request.nextDate)
            assertEquals(0, request.periodMonths)
            assertEquals(0, request.remindMonths)
            assertEquals("252-03515", request.requestNumber)
            assertFalse(serviceVisibleOnDay(request, request.lastDate))
            assertFalse(servicePlannedOnDay(request, request.lastDate))
            assertFalse(serviceReminderVisibleOnDay(request, request.lastDate))
            assertFalse(serviceDueSoon(request, today))
            assertTrue(filterServiceOverview(listOf(request), today.plusYears(1),
                WorkOverviewFilter.OVERDUE).isEmpty())
            assertTrue(filterServiceOverview(listOf(request), today,
                WorkOverviewFilter.SOON).isEmpty())
            val done = completeServiceCycle(request, today.plusDays(20))
            assertEquals(request.lastDate, done.lastDate)
            assertEquals(today.plusDays(20), done.completedAt)
            assertEquals(null, done.serviceHistory.single().scheduledDate)
            assertEquals("252-03515", done.serviceHistory.single().requestNumber)
        }
    }

    @Test fun unplannedCompletionPreservesRequestAndNeverGeneratesNextDue() {
        val created = LocalDate.of(2026, 11, 7)
        val completedOn = LocalDate.of(2026, 11, 9)
        val entry = ServiceEntry(
            id = "unplanned-radar", equipment = "PC FOR ICE RADAR",
            lastDate = created, nextDate = null,
            requestNumber = "AMOS-2501", unplanned = true,
        )
        val finished = completeServiceCycle(entry, completedOn)
        assertTrue(finished.unplanned)
        assertEquals(completedOn, finished.completedAt)
        assertEquals(null, finished.nextDate)
        assertEquals("", finished.requestNumber)
        assertEquals(created, finished.lastDate)
        assertEquals("AMOS-2501", finished.serviceHistory.single().requestNumber)
        assertEquals(null, finished.serviceHistory.single().scheduledDate)
        assertEquals(completedOn, finished.serviceHistory.single().completedAt)
        assertFalse(workAgendaExistsOnDay(completedOn, emptyList(), listOf(finished)))
        assertEquals(1, serviceCompletedOnDay(listOf(finished), completedOn).size)
        assertTrue(runCatching { completeServiceCycle(finished, completedOn.plusDays(1)) }.isFailure)
    }

    @Test fun unplannedDraftRequiresRequestBeforeCompletingAndCannotHaveSchedule() {
        val today = LocalDate.of(2026, 11, 7)
        val draft = ServiceEntry(id = "unplanned-draft", equipment = "ICE RADAR",
            lastDate = today, nextDate = null, unplanned = true)
        assertTrue(runCatching { completeServiceCycle(draft, today) }.isFailure)
        assertEquals(null, draft.nextDate)
        assertTrue(runCatching { draft.copy(nextDate = today.plusDays(1)) }.isFailure)
        assertTrue(runCatching { draft.copy(periodMonths = 12) }.isFailure)
        assertTrue(runCatching { draft.copy(remindMonths = 1) }.isFailure)
        assertTrue(runCatching { nextServiceFromCompletion(draft, today) }.isFailure)
    }

    @Test fun oldServiceRecordsHaveEmptyCompletionHistoryByDefault() {
        val item = service()
        assertTrue(item.serviceHistory.isEmpty())
        assertEquals(null, item.completedAt)
        assertEquals("", item.requestNumber)
    }

    @Test fun invalidDatesOrExcessiveReminderAreRejected() {
        assertTrue(runCatching { service(
            last = LocalDate.of(2027, 1, 31),
            next = LocalDate.of(2027, 1, 31),
        ) }.isFailure)
        assertTrue(runCatching { service(alert = 4) }.isFailure)
        assertFalse(runCatching { service(period = 13) }.isFailure)
        assertFalse(runCatching { service(alert = 3, period = 60) }.isFailure)
    }
}
