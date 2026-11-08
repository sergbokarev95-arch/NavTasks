package com.navtasks.app

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.pow

/** Selection surfaces use a deeper Apple blue, so 11sp white labels stay legible. */
class AppleBlueSelectionTest {
    private fun linear(c: Float): Double {
        val v = c.toDouble()
        return if (v <= .04045) v / 12.92 else ((v + .055) / 1.055).pow(2.4)
    }

    private fun luminance(c: Color): Double =
        .2126 * linear(c.red) + .7152 * linear(c.green) + .0722 * linear(c.blue)

    private fun contrast(a: Color, b: Color): Double {
        val aa = luminance(a)
        val bb = luminance(b)
        return (maxOf(aa, bb) + .05) / (minOf(aa, bb) + .05)
    }

    @Test fun selectedControlsAreReadableInBothThemes() {
        for (dark in listOf(false, true)) {
            val p = navPalette(dark)
            assertEquals(Color(0xFF0071E3), p.selectionFill)
            assertEquals(Color.White, p.selectionContent)
            assertTrue("Selected text contrast fails in dark=$dark",
                contrast(p.selectionFill, p.selectionContent) >= 4.5)
        }
    }

    @Test fun ordinaryAppleBlueAndOrangeStayStableAcrossCardPalette() {
        val day = navPalette(false)
        val night = navPalette(true)
        assertEquals(Color(0xFF0088FF), day.blue)
        assertEquals(Color(0xFF0091FF), night.blue)
        assertEquals(Color(0xFFE6E8EC), day.background)
        assertEquals(Color.Black, night.background)
        assertEquals(Color(0xFFFF8D28), day.systemOrange)
        assertEquals(Color(0xFFFF9230), night.systemOrange)
    }
}
