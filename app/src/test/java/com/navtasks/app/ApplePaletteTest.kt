package com.navtasks.app

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class ApplePaletteTest {
    @Test fun dayColorsUseAppleAccentAndNotesBackground() {
        val p = navPalette(false)
        assertEquals(Color(0xFF0088FF), p.blue)
        assertEquals(Color(0xFFF4F5F7), p.background)
        assertEquals(p.background, p.backgroundEnd)
        assertEquals(Color(0xFFFF8D28), p.systemOrange)
        assertEquals(Color(0xFFC55300), p.warningText)
    }

    @Test fun nightRemainsTrueBlackWithAppleAccent() {
        val p = navPalette(true)
        assertEquals(Color.Black, p.background)
        assertEquals(Color.Black, p.backgroundEnd)
        assertEquals(Color(0xFF0091FF), p.blue)
        assertEquals(Color(0xFFFF9230), p.systemOrange)
        assertEquals(Color(0xFFFF9230), p.warningText)
    }
}
