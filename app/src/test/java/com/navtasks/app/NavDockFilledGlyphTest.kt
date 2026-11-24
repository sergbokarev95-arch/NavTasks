package com.navtasks.app

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class NavDockFilledGlyphTest {
    @Test fun vividOpaqueCoralAndFourExistingDockDestinations() {
        assertEquals(Color(0xFFFF5533), NavLiquidDockTabs.activeInk(false))
        assertEquals(Color(0xFFFF5533), NavLiquidDockTabs.activeInk(true))
        assertEquals(listOf("today", "work", "handover", "reference"),
            NavLiquidDockTabs.items.map { it.glyph })
    }

    @Test fun dockIconsAreFilledWithRealCutoutsButGlobalGlyphsUnmodified() {
        val pictograms = File("src/main/java/com/navtasks/app/NavDockFilledGlyph.kt").readText()
        val dock = File("src/main/java/com/navtasks/app/NavLiquidGlassDock.kt").readText()
        val legacy = File("src/main/java/com/navtasks/app/NavGlass.kt").readText()
        for (kind in listOf("today", "work", "handover", "reference")) {
            assertTrue(pictograms.contains("\"$kind\" ->"))
        }
        assertTrue(pictograms.contains("compositingStrategy = CompositingStrategy.Offscreen"))
        assertTrue(pictograms.contains("blendMode = BlendMode.Clear"))
        assertTrue(dock.contains("NavDockFilledGlyph("))
        assertTrue(legacy.contains("NavDockFilledGlyph("))
        assertTrue(legacy.contains("fun NavGlassGlyph(kind: String"))
        assertTrue(dock.contains("NavLiquidDockOptics.selection.blurDp"))
        assertTrue(dock.contains("detectHorizontalDragGestures("))
        assertTrue(dock.contains("if (destination != selected) onSelect(destination)"))
    }
}
