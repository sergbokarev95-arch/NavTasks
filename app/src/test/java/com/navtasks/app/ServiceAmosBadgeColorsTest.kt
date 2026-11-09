package com.navtasks.app

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/** Keep AMOS request badges warm without recoloring the blue system controls. */
class ServiceAmosBadgeColorsTest {
    @Test fun chosenDayVariantIsSoftPeach() {
        val c = serviceAmosBadgeColors(false)
        assertEquals(Color(0xFFFFF1DE), c.fill)
        assertEquals(Color(0xFFF4C48B), c.outline)
        assertEquals(Color(0xFFAE5800), c.text)
    }

    @Test fun chosenNightVariantIsMutedGraphiteAmber() {
        val c = serviceAmosBadgeColors(true)
        assertEquals(Color(0xFF403227), c.fill)
        assertEquals(Color(0xFF74583C), c.outline)
        assertEquals(Color(0xFFFFBF79), c.text)
    }

    @Test fun onlyRequestBadgeUsesWarmAccentNotGlobalSelection() {
        for (dark in listOf(false, true)) {
            val request = serviceAmosBadgeColors(dark)
            val p = navPalette(dark)
            assertNotEquals(p.selectionFill, request.fill)
            assertNotEquals(p.blueText, request.text)
            assertEquals(Color(0xFF0071E3), p.selectionFill)
            assertEquals(Color.White, p.selectionContent)
        }
    }
}
