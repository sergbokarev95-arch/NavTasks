package com.navtasks.app

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.pow

/** Readability regression tests for critical status text on day/night panels. */
class UiStatusContrastTest {
    private fun linear(component: Float): Double {
        val c = component.toDouble()
        return if (c <= 0.04045) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
    }

    private fun luminance(color: Color): Double =
        0.2126 * linear(color.red) + 0.7152 * linear(color.green) +
            0.0722 * linear(color.blue)

    private fun contrast(a: Color, b: Color): Double {
        val la = luminance(a)
        val lb = luminance(b)
        return (maxOf(la, lb) + 0.05) / (minOf(la, lb) + 0.05)
    }

    @Test fun dayStatusTextIsReadableOnLightGlass() {
        val p = navPalette(false)
        val surface = Color(0xFFF9FBFF)
        assertTrue(contrast(p.blue, surface) >= 3.0)
        assertTrue(contrast(p.blueText, surface) >= 4.5)
        assertTrue(contrast(p.muted, surface) >= 4.5)
        assertTrue(contrast(p.successText, surface) >= 4.5)
        assertTrue(contrast(p.warningText, surface) >= 4.5)
        assertTrue(contrast(p.dangerText, surface) >= 4.5)
    }

    @Test fun nightStatusTextIsReadableOnDarkGlass() {
        val p = navPalette(true)
        val surface = Color(0xFF282D33)
        assertTrue(contrast(p.blue, surface) >= 3.0)
        assertTrue(contrast(p.blueText, surface) >= 4.5)
        assertTrue(contrast(p.muted, surface) >= 4.5)
        assertTrue(contrast(p.successText, surface) >= 4.5)
        assertTrue(contrast(p.warningText, surface) >= 4.5)
        assertTrue(contrast(p.dangerText, surface) >= 4.5)
    }
}
