package com.navtasks.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ServiceRequestsFilterTest {
    private val last = LocalDate.of(2026, 8, 1)
    private val next = LocalDate.of(2027, 8, 1)

    private fun planned(id: String, request: String = "") = ServiceEntry(
        id = id,
        equipment = "Radar $id",
        lastDate = last,
        nextDate = next,
        periodMonths = 12,
        requestNumber = request,
    )

    private fun unplanned(id: String, request: String) = ServiceEntry(
        id = id,
        equipment = "Unplanned $id",
        lastDate = last,
        nextDate = null,
        periodMonths = 0,
        remindMonths = 0,
        unplanned = true,
        requestNumber = request,
    )

    @Test fun bothPlannedAndUnplannedRequestsAreVisible() {
        val rows = listOf(
            planned("planned-with", "252-03515"),
            planned("planned-without"),
            unplanned("unplanned", "252-03513"),
        )
        assertEquals(listOf("planned-with", "unplanned"),
            currentServiceRequests(rows).map { it.id })
        assertEquals(3, rows.size) // WORKS is never affected
    }

    @Test fun closeCompletionAndWhitespaceDoNotCreateCurrentRequests() {
        val rows = listOf(
            planned("whitespace", "   "),
            planned("closed", "252-01001").copy(closedAt = last),
            unplanned("done", "252-01002").copy(completedAt = next),
            planned("active", " 252-03511 "),
        )
        assertEquals(listOf("active"), currentServiceRequests(rows).map { it.id })
    }

    @Test fun completedCycleKeepsHistoryButLeavesRequestList() {
        val before = planned("planned", "252-03515")
        val after = completeServiceCycle(before, LocalDate.of(2026, 11, 8))
        assertEquals("", after.requestNumber)
        assertEquals("252-03515", after.serviceHistory.single().requestNumber)
        assertTrue(currentServiceRequests(listOf(after)).isEmpty())
        assertFalse(currentServiceRequests(listOf(before)).isEmpty())
    }

    @Test fun restoredOpenRequestIsVisibleAgain() {
        val original = planned("planned", "252-03515")
        val archived = original.copy(closedAt = last)
        assertTrue(currentServiceRequests(listOf(archived)).isEmpty())
        assertEquals(1, currentServiceRequests(listOf(archived.copy(closedAt = null))).size)
    }
}
