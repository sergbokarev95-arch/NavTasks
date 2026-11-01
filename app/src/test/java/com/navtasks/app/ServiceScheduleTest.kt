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

    @Test fun customEightYearPeriodHasSameJsonFieldAndIsPreserved() {
        val custom = service(period = 96, alert = 3)
        val archived = custom.toJson()
        assertEquals(96, archived.getInt("periodMonths"))
        assertEquals(3, archived.getInt("remindMonths"))
        assertEquals(LocalDate.of(2034, 11, 1),
            nextServiceFromCompletion(custom, LocalDate.of(2026, 11, 1)).nextDate)
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
