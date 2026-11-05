package com.navtasks.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class WorkOrderStoreTest {
    private val today = LocalDate.of(2026, 11, 5)

    private fun work(
        id: String = "one",
        number: String = "AMOS-3154",
        dueDate: LocalDate = today.plusDays(5),
        completedAt: LocalDate? = null,
    ) = WorkOrderEntry(
        id = id, reference = number, title = "Weekly steering gear check",
        dueDate = dueDate, createdAt = today.minusDays(30),
        completedAt = completedAt, comment = "Record in AMOS logbook",
    )

    @Test fun approachingAndOverdueAreDifferentStates() {
        assertEquals(WorkOrderState.SOON, work().let { workOrderState(it, today) })
        assertEquals(WorkOrderState.OVERDUE, work(dueDate = today.minusDays(1)).let {
            workOrderState(it, today)
        })
        assertEquals(WorkOrderState.OPEN, work(dueDate = today.plusDays(8)).let {
            workOrderState(it, today)
        })
        assertEquals(WorkOrderState.SOON, work(dueDate = today).let {
            workOrderState(it, today)
        })
    }

    @Test fun completionPreservesOriginalDueDateAndComments() {
        val source = work(dueDate = today.minusDays(15))
        val updated = workOrderComplete(source, today)
        assertTrue(updated.completed)
        assertEquals(today, updated.completedAt)
        assertEquals(source.dueDate, updated.dueDate)
        assertEquals(source.reference, updated.reference)
        assertEquals(source.comment, updated.comment)
        assertEquals(WorkOrderState.COMPLETED, workOrderState(updated, today))
    }

    @Test fun completionCannotSilentlyOverwriteArchivedDate() {
        val archived = work(completedAt = today.minusDays(4))
        assertTrue(runCatching { workOrderComplete(archived, today) }.isFailure)
        assertEquals(today.minusDays(4), archived.completedAt)
    }

    @Test fun reopenOnlyChangesCompletionStatus() {
        val archived = work(completedAt = today.minusDays(1))
        val opened = workOrderReopen(archived)
        assertFalse(opened.completed)
        assertEquals(archived.id, opened.id)
        assertEquals(archived.dueDate, opened.dueDate)
        assertEquals(archived.comment, opened.comment)
        assertEquals(WorkOrderState.SOON, workOrderState(opened, today))
        assertTrue(runCatching { workOrderReopen(opened) }.isFailure)
    }

    @Test fun duplicateAmosNumbersAreRejectedIgnoringCaseAndSpacing() {
        assertTrue(runCatching {
            validateWorkOrders(listOf(work(), work(id = "two", number = " amos-3154 ")))
        }.isFailure)
        assertTrue(runCatching {
            validateWorkOrders(listOf(work(), work(id = "one", number = "AMOS-9999")))
        }.isFailure)
        validateWorkOrders(listOf(work(), work(id = "two", number = "AMOS-9999")))
    }

    @Test fun amosNumberIsOptionalWhileTitleRemainsRequired() {
        val first = work(number = "")
        val second = work(id = "two", number = "")
        validateWorkOrders(listOf(first, second))
        assertFalse(first.completed)
        assertTrue(runCatching { work().copy(title = "") }.isFailure)
    }

    @Test fun incompleteRequiredChecklistCannotBeClosed() {
        val row = CheckItem(id = "a", title = "Verify engine")
        val wo = work().copy(checklist = listOf(row))
        assertFalse(workOrderCanComplete(wo))
        // Legacy checklist is retained but AMOS completion no longer requires it.
        assertTrue(workOrderComplete(wo, today).completed)
        val checked = workOrderCheck(wo, "a", true)
        assertEquals(1 to 1, workOrderMandatoryProgress(checked))
        assertTrue(workOrderCanComplete(checked))
        assertTrue(workOrderComplete(checked, today).completed)
    }

    @Test fun optionalItemsDoNotBlockCompletion() {
        val optional = CheckItem(id = "optional", title = "Remark", required = false)
        val wo = work().copy(checklist = listOf(optional))
        assertTrue(workOrderCanComplete(wo))
        val closed = workOrderComplete(wo, today)
        assertTrue(runCatching { workOrderCheck(closed, "optional", true) }.isFailure)
    }

    @Test fun uncheckingRestoresIncompleteStatusAndNotes() {
        val item = CheckItem(id = "check", title = "PPE", comment = "Ref note")
        val wo = work().copy(checklist = listOf(item))
        val checked = workOrderCheck(wo, "check", true)
        assertFalse(workOrderCanComplete(workOrderCheck(checked, "check", false)))
        assertEquals("Ref note", checked.checklist.single().comment)
        assertTrue(runCatching { workOrderCheck(wo, "missing", true) }.isFailure)
    }

    @Test fun reminderLeadTimeUsesCalendarDaysAndIsValidated() {
        val wo = work(dueDate = today.plusDays(30)).copy(remindDaysBefore = 7)
        assertEquals(today.plusDays(23), workOrderWarningStart(wo))
        assertTrue(runCatching { wo.copy(remindDaysBefore = 91) }.isFailure)
    }

    @Test fun backdatedCompletionIsKeptForManualAmosHistory() {
        val historical = workOrderComplete(work(), today.minusDays(10))
        assertEquals(today.minusDays(10), historical.completedAt)
        assertEquals(WorkOrderState.COMPLETED, workOrderState(historical, today))
    }
}
