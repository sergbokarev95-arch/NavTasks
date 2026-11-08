package com.navtasks.app

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Day/night semantic accents are shared by the Today 7-day strip,
 * the full Plan month grid, and the note reference indicator.
 */
class AppleWeekendAndReferenceTest {
    @Test fun weekendUsesExactlyAppleRedWithoutOldPinkTones() {
        val day = navPalette(false)
        val night = navPalette(true)
        assertEquals(Color(0xFFFF3B30), calendarWeekendInk(day))
        assertEquals(Color(0xFFFF453A), calendarWeekendInk(night))
        assertEquals(day.systemRed, calendarWeekendInk(day))
        assertEquals(night.systemRed, calendarWeekendInk(night))
    }

    @Test fun noteAccentKeepsAppleOrangeWhileSelectionRemainsBlue() {
        for (dark in listOf(false, true)) {
            val p = navPalette(dark)
            assertEquals(
                if (dark) Color(0xFFFF9230) else Color(0xFFFF8D28),
                p.systemOrange,
            )
            assertEquals(Color(0xFF0071E3), p.selectionFill)
            assertEquals(Color.White, p.selectionContent)
        }
    }
}
