package com.navtasks.app

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

/** Canonical Apple destructive hue, independent of the more readable status red. */
class AppleSystemRedTest {
    @Test fun destructiveActionsUseAppleSystemRedForEachTheme() {
        val day = navPalette(false)
        val night = navPalette(true)
        assertEquals(Color(0xFFFF3B30), day.systemRed)
        assertEquals(Color(0xFFFF453A), night.systemRed)
        assertEquals(Color(0xFFC9342D), day.dangerText)
        assertEquals(Color(0xFFFF5C52), night.dangerText)
    }

    @Test fun selectedAppleBlueIsUnchangedByDestructiveHues() {
        for (dark in listOf(false, true)) {
            val p = navPalette(dark)
            assertEquals(Color(0xFF0071E3), p.selectionFill)
            assertEquals(Color.White, p.selectionContent)
        }
    }
}
