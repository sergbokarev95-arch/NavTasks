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
        assertEquals(Color(0xFFBE5300), p.warningText)
        assertEquals(Color(0xFF0071E3), p.blueText)
    }

    @Test fun nightRemainsTrueBlackWithAppleAccent() {
        val p = navPalette(true)
        assertEquals(Color.Black, p.background)
        assertEquals(Color.Black, p.backgroundEnd)
        assertEquals(Color(0xFF0091FF), p.blue)
        assertEquals(Color(0xFFFF9230), p.systemOrange)
        assertEquals(Color(0xFFFF9230), p.warningText)
        assertEquals(Color(0xFF5CB8FF), p.blueText)
    }
}
