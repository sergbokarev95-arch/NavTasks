package com.navtasks.app

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class ApplePaletteTest {
    @Test fun dayColorsUseHONORNeutralWhiteWithUnchangedAppleAccents() {
        val p = navPalette(false)
        assertEquals(Color(0xFF0088FF), p.blue)
        assertEquals(Color(0xFFF2F3F7), p.background)
        assertEquals(p.background, p.backgroundEnd)
        assertEquals(Color.White, p.panel)
        assertEquals(Color.White, p.task)
        assertEquals(Color.White, p.nestedTask)
        assertEquals(Color(0xFFE0E2E8), p.border)
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
        assertEquals(Color(0xFF1D2126), p.panel)
        assertEquals(Color(0xFF282D33), p.task)
        assertEquals(Color(0xFF40464D), p.border)
    }
}
