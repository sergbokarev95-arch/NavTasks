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

    @Test fun manualRecordDoesNotGuessNextDate() {
        val manual = service(period = 0, alert = 0)
        assertTrue(runCatching { nextServiceFromCompletion(manual, LocalDate.now()) }.isFailure)
        assertEquals(LocalDate.of(2027, 1, 31), manual.nextDate)
    }

    @Test fun invalidDatesOrExcessiveReminderAreRejected() {
        assertTrue(runCatching { service(
            last = LocalDate.of(2027, 1, 31),
            next = LocalDate.of(2027, 1, 31),
        ) }.isFailure)
        assertTrue(runCatching { service(alert = 4) }.isFailure)
        assertTrue(runCatching { service(period = 13) }.isFailure)
        assertFalse(runCatching { service(alert = 3, period = 60) }.isFailure)
    }
}
