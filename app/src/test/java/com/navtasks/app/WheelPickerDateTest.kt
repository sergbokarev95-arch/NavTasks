package com.navtasks.app
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class WheelPickerDateTest {
    @Test fun leapDayClampsToShorterFebruary() {
        assertEquals(LocalDate.of(2025, 2, 28), wheelDate(2025, 2, 29))
        assertEquals(LocalDate.of(2024, 2, 29), wheelDate(2024, 2, 29))
    }
    @Test fun day31ClampsToThirtyDayMonths() {
        assertEquals(LocalDate.of(2026, 4, 30), wheelDate(2026, 4, 31))
        assertEquals(LocalDate.of(2026, 1, 31), wheelDate(2026, 1, 31))
    }
}
