package com.navtasks.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class DayConfirmationTest {
    private val today = LocalDate.of(2026, 11, 1)

    @Test fun todayIsImmediate() {
        assertFalse(requiresDayConfirmation(today, today, false))
    }
    @Test fun futureDateAlwaysRequiresConfirmation() {
        assertTrue(requiresDayConfirmation(today.plusDays(1), today, false))
        assertTrue(requiresDayConfirmation(today.plusDays(6), today, false))
    }
    @Test fun missedListIsExplicitlySeparate() {
        assertFalse(requiresDayConfirmation(today.minusDays(2), today, true))
    }
    @Test fun pastDateRequiresConfirmationOutsideMissedList() {
        assertTrue(requiresDayConfirmation(today.minusDays(1), today, false))
    }
}
