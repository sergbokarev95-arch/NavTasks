package com.navtasks.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class WorkOrderRecurrenceTest {
    private val due = LocalDate.of(2026, 11, 30)
    private fun order(
        repeatDays: Int = 0,
        repeatMonths: Int = 0,
        checklist: List<CheckItem> = emptyList(),
        checkedItemIds: Set<String> = emptySet(),
    ) = WorkOrderEntry(
        id = "work", reference = "AMOS-971", title = "Periodic radio inspection",
        dueDate = due, createdAt = due.minusMonths(1),
        repeatDays = repeatDays, repeatMonths = repeatMonths,
        checklist = checklist, checkedItemIds = checkedItemIds,
    )

    @Test fun oneOffWorkOrderKeepsLegacyCompletionSemantics() {
        val old = order()
        val new = workOrderComplete(old, LocalDate.of(2026, 11, 28))
        assertTrue(new.completed)
        assertEquals(LocalDate.of(2026, 11, 28), new.completedAt)
        assertTrue(new.executionHistory.isEmpty())
        assertEquals(due, new.dueDate)
        assertEquals(old, workOrderReopen(new))
    }

    @Test fun weekRepeatStaysAnchoredToOriginalStartDate() {
        val work = order(repeatDays = 7)
        val finished = workOrderComplete(work, LocalDate.of(2026, 11, 26))
        assertFalse(finished.completed)
        assertEquals(LocalDate.of(2026, 12, 7), finished.dueDate)
        assertEquals(1, finished.executionHistory.size)
        assertEquals(due, finished.executionHistory.single().dueDate)
        assertEquals(LocalDate.of(2026, 11, 26),
            finished.executionHistory.single().completedAt)
        assertEquals(1, workOrderHistoryRecords(listOf(finished)).size)
    }

    @Test fun repeatPreservesCompletedChecklistAndClearsCurrentProgress() {
        val item = CheckItem(id = "gmdss", title = "Inspect battery", comment = "Journal note")
        val work = order(
            repeatMonths = 3, checklist = listOf(item),
            checkedItemIds = setOf(item.id),
        )
        val finished = workOrderComplete(work, due)
        assertTrue(finished.checkedItemIds.isEmpty())
        assertFalse(workOrderCanComplete(finished))
        val archived = finished.executionHistory.single()
        assertEquals("Journal note", archived.checklist.single().comment)
        assertEquals(setOf("gmdss"), archived.checkedItemIds)
        val revised = finished.copy(checklist = listOf(item.copy(comment = "New wording")))
        assertEquals("Journal note", revised.executionHistory.single().checklist.single().comment)
    }

    @Test fun monthlyCyclesStayOnTheOriginalDayAfterFebruary() {
        val jan = order(repeatMonths = 1).copy(
            dueDate = LocalDate.of(2026, 1, 31),
            startDate = LocalDate.of(2026, 1, 31),
        )
        val first = workOrderComplete(jan, LocalDate.of(2026, 1, 31))
        assertEquals(LocalDate.of(2026, 2, 28), first.dueDate)
        val second = workOrderComplete(first, LocalDate.of(2026, 2, 28))
        assertEquals(LocalDate.of(2026, 3, 31), second.dueDate)
    }

    @Test fun calendarMonthRepeatRespectsLeapDay() {
        val leap = order(repeatMonths = 12).copy(dueDate = LocalDate.of(2024, 2, 29))
        val next = workOrderComplete(leap, LocalDate.of(2024, 2, 29))
        assertEquals(LocalDate.of(2025, 2, 28), next.dueDate)
    }

    @Test fun recurringHistoryRetainsDistinctCycles() {
        val work = order(repeatMonths = 1)
        val one = workOrderComplete(work, due)
        val two = workOrderComplete(one, LocalDate.of(2026, 12, 31))
        assertEquals(2, two.executionHistory.size)
        assertEquals(LocalDate.of(2027, 1, 30), two.dueDate)
        val archives = workOrderHistoryRecords(listOf(two))
        assertEquals(2, archives.size)
        assertTrue(archives[0].canUndo)
        assertFalse(archives[1].canUndo)
        assertEquals(LocalDate.of(2026, 12, 31), archives[0].completion.completedAt)
    }

    @Test fun undoOnlyLastCycleWithoutDestroyingCurrentProgress() {
        val item = CheckItem(id = "a", title = "Verify log")
        val current = workOrderComplete(order(
            repeatMonths = 1, checklist = listOf(item), checkedItemIds = setOf("a"),
        ), due)
        val startedNext = workOrderCheck(current, "a", true)
        assertTrue(runCatching { workOrderReopen(startedNext) }.isFailure)
        val rolledBack = workOrderReopen(current)
        assertEquals(due, rolledBack.dueDate)
        assertTrue(rolledBack.executionHistory.isEmpty())
        assertEquals(setOf("a"), rolledBack.checkedItemIds)
    }

    @Test fun invalidDoubleRecurrenceAndExcessHistoryAreRejected() {
        assertTrue(runCatching { order(7, 1) }.isFailure)
        assertTrue(runCatching { order(repeatMonths = 1201) }.isFailure)
        assertTrue(runCatching { order(repeatDays = 3651) }.isFailure)
    }

    @Test fun existingOneOffHistoryRemainsVisible() {
        val legacy = order().copy(completedAt = due)
        val archive = workOrderHistoryRecords(listOf(legacy)).single()
        assertTrue(archive.canUndo)
        assertEquals(due, archive.completion.completedAt)
    }
}
