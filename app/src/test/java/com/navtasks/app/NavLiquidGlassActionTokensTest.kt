package com.navtasks.app

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertTrue
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Test

class NavLiquidGlassActionTokensTest {
    @Test fun coralMatchesDockInBothThemesButOtherActionsRetainTheirInk() {
        for (dark in listOf(false, true)) {
            val p = navPalette(dark)
            for (kind in listOf("plus", "calendar")) {
                assertEquals(Color(0xFFFF5533), NavLiquidGlassActionTokens.glyphInk(kind, dark, p.blue))
                assertEquals(NavLiquidDockTabs.activeInk(dark), NavLiquidGlassActionTokens.glyphInk(kind, dark, p.text))
            }
            for (kind in listOf("back", "search", "settings", "edit", "delete")) {
                assertEquals(p.text, NavLiquidGlassActionTokens.glyphInk(kind, dark, p.text))
            }
        }
    }

    @Test fun allGlassRoutesUseSameActionInk() {
        val root = "src/main/java/com/navtasks/app/"
        val primary = File(root + "NavLiquidGlassActionButton.kt").readText()
        val fallback = File(root + "NavGlass.kt").readText()
        val handover = File(root + "HandoverUi.kt").readText()
        assertTrue(primary.contains("glyphInk(kind, dark, glyphColor)"))
        assertTrue(fallback.contains("glyphInk(kind, p.dark, p.text)"))
        assertTrue(fallback.contains("glyphInk(\"plus\", p.dark, p.blue)"))
        assertTrue(fallback.contains("glyphInk(\"calendar\", p.dark, p.blue)"))
        assertTrue(handover.contains("glyphInk(\"plus\", p.dark, p.blue)"))
    }

    @Test fun addKeepsExistingCircleAndTapSize() {
        assertEquals(54f, NavLiquidGlassActionTokens.addDiameterDp, 0f)
    }

    @Test fun floatingButtonSamplesExactlyTheDockMaterial() {
        assertEquals(2f, NavLiquidDockOptics.bar.blurDp, 0f)
        assertEquals(12f, NavLiquidDockOptics.bar.refractionHeightDp, 0f)
        assertEquals(36f, NavLiquidDockOptics.bar.refractionAmountDp, 0f)
    }
}
