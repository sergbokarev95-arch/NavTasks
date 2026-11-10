package com.navtasks.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NavProfileDeepLinkTest {
    private val ships = listOf("ship-default", "ship-2", "personal")

    @Test fun correctShipIsSelectedBeforeRecordLookup() {
        assertEquals("ship-2", resolveNavProfileTarget("ship-default", ships, "ship-2"))
        assertEquals("personal", resolveNavProfileTarget("ship-default", ships, "personal"))
        assertEquals("ship-default", resolveNavProfileTarget("ship-default", ships, null))
    }

    @Test fun staleOrDeletedShipNeverFallsBackToAnotherShipsData() {
        assertNull(resolveNavProfileTarget("ship-default", ships, "deleted-ship"))
        assertNull(resolveNavProfileTarget("ship-default", ships, ""))
        assertNull(resolveNavProfileTarget("removed", ships, null))
    }

    @Test fun allNotificationAndWidgetDestinationsRetainIdentity() {
        val base = NavOpenRequest(1L, "ship-2", null, null, false)
        assertEquals(NavOpenDestination.TODAY, navOpenDestination(base))
        assertEquals(NavOpenDestination.TASK,
            navOpenDestination(base.copy(runKey = "task-id|2026-11-10")))
        assertEquals(NavOpenDestination.AMOS,
            navOpenDestination(base.copy(workOrderId = "amos-id")))
        assertEquals(NavOpenDestination.MISSED,
            navOpenDestination(base.copy(openMissed = true)))
        assertEquals(NavOpenDestination.AMOS,
            navOpenDestination(base.copy(workOrderId = "amos-id", runKey = "task-id", openMissed = true)))
    }

    @Test fun repeatedTapHasDistinctSerialEvenWithSameRecord() {
        val first = NavOpenRequest(10L, "ship-default", "run", null, false)
        val second = first.copy(serial = 11L)
        assertEquals(first.runKey, second.runKey)
        assertEquals(NavOpenDestination.TASK, navOpenDestination(second))
        assertEquals(true, first.serial != second.serial)
    }
}
