package com.navtasks.app

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class NavNotificationRolloverTest {
    private val day = LocalDate.of(2026, 11, 10)

    @Test fun onlyOpenPlannedServiceCanSendReminder() {
        val open = ServiceEntry(equipment = "Fire pump", lastDate = day.minusMonths(60),
            nextDate = day.plusDays(2), periodMonths = 60, remindMonths = 1)
        assertTrue(activeServiceReminderRecord(open))
        assertFalse(activeServiceReminderRecord(open.copy(closedAt = day)))
        assertFalse(activeServiceReminderRecord(open.copy(completedAt = day)))
        assertFalse(activeServiceReminderRecord(open.copy(unplanned = true,
            nextDate = null, periodMonths = 0, remindMonths = 0, requestNumber = "1001")))
    }

    @Test fun archivedAmosNeverSendsScheduledOrGroupedReminder() {
        val open = WorkOrderEntry(title = "Test radio", dueDate = day.plusDays(3),
            remindDaysBefore = 3)
        assertTrue(activeAmosReminderRecord(open))
        assertFalse(activeAmosReminderRecord(open.copy(closedAt = day)))
        assertFalse(activeAmosReminderRecord(open.copy(completedAt = day)))
    }

    @Test fun rollRunsEveryDayEvenAcrossMonthAndYearBoundary() {
        assertEquals(LocalDateTime.of(2026, 11, 11, 0, 5),
            nextNavWidgetRefresh(LocalDateTime.of(2026, 11, 10, 23, 59)))
        assertEquals(LocalDateTime.of(2027, 1, 1, 0, 5),
            nextNavWidgetRefresh(LocalDateTime.of(2026, 12, 31, 0, 6)))
    }

    @Test fun edgeToEdgeServiceAndAmosRequireActualSystemBottomInset() {
        val none = NavFloatingActionSafeArea.listBottomPaddingDp(0f, false, true)
        val withInset = NavFloatingActionSafeArea.listBottomPaddingDp(34f, false, true)
        assertEquals(92f, none, 0.001f)
        assertEquals(126f, withInset, 0.001f)
        assertEquals(92f,
            NavFloatingActionSafeArea.listBottomPaddingDp(34f, false), 0.001f)
        assertTrue(withInset > 106f)
    }
}
