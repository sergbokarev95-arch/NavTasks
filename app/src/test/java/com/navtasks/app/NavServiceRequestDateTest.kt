package com.navtasks.app

import java.time.LocalDate
import java.io.File
import org.junit.Assert.*
import org.junit.Test

class NavServiceRequestDateTest {
    private val last = LocalDate.of(2026, 1, 1)
    private val due = last.plusYears(1)
    private val filed = LocalDate.of(2026, 11, 26)
    private val planned = ServiceEntry(equipment = "Radar", lastDate = last,
        nextDate = due, periodMonths = 12, remindMonths = 2)

    @Test fun requestDateIsIndependentAndArchived() {
        val entry = withServiceRequest(planned, " 252-03524 ", filed)
        assertEquals(last, entry.lastDate)
        assertEquals(due, entry.nextDate)
        assertEquals(2, entry.remindMonths)
        assertEquals(filed, ServiceEntry.fromJson(entry.toJson()).effectiveRequestDate)
        val done = completeServiceCycle(entry, due)
        assertEquals(filed, done.serviceHistory.single().requestDate)
        assertEquals(filed, ServiceEntry.fromJson(done.toJson()).serviceHistory.single().requestDate)
        assertNull(done.effectiveRequestDate)
        assertNull(done.requestDate)
        assertEquals(due.plusYears(1), done.nextDate)
    }

    @Test fun legacyAndClearDoNotInventDatesOrEraseHistory() {
        val old = planned.copy(requestNumber = "OLD")
        val json = old.toJson().apply { remove("requestDate") }
        assertNull(ServiceEntry.fromJson(json).effectiveRequestDate)
        val unplanned = ServiceEntry(equipment = "Radar", lastDate = last,
            nextDate = null, unplanned = true, requestNumber = "OLD")
        assertEquals(last, ServiceEntry.fromJson(unplanned.toJson().apply {
            remove("requestDate")
        }).effectiveRequestDate)
        val draft = withServiceRequest(unplanned, "", filed)
        assertTrue(draft.unplanned)
        assertNull(draft.nextDate)
        assertNull(draft.effectiveRequestDate)
        assertFalse(runCatching { completeServiceCycle(draft, filed) }.isSuccess)
        val cleared = withServiceRequest(planned, "", filed)
        assertNull(cleared.requestDate)
        assertEquals(due, cleared.nextDate)
        val done = completeServiceCycle(withServiceRequest(planned, "OLD", filed), due)
        assertEquals(done.serviceHistory, withServiceRequest(done, "", null).serviceHistory)
    }

    @Test fun editorsRequireValidChangedFieldsAndRichInitializationIsNotAnEdit() {
        val root = "src/main/java/com/navtasks/app/"
        for (name in listOf("PlanAndTasksUi.kt", "WorkOrdersUi.kt", "ServicesUi.kt")) {
            assertTrue(File(root + name).readText().contains("valid && dirty, accent = true"))
        }
        assertTrue(File(root + "NavRichNoteUi.kt").readText().contains("onInitialized?.invoke"))
    }
}
