package com.navtasks.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Selected CAP only: no modification to the dock's optical physics or routing. */
class NavDockNightGraphiteTest {
    @Test fun primaryNightSelectionUsesNeutralGraphiteAndPreservesDay() {
        val dock = File("src/main/java/com/navtasks/app/NavLiquidGlassDock.kt").readText()
        assertTrue(dock.contains("Color(0xFF3A3A3C).copy(alpha = .93f)"))
        assertTrue(dock.contains("drawRect(resting)"))
        assertFalse(dock.contains("Color(0xFF3F4B58).copy(alpha = .82f)"))
        assertFalse(dock.contains("Color(0xFF75B8F0).copy(alpha = .13f)"))
        assertTrue(dock.contains("else Color(0xFFD7D8DA).copy(alpha = .87f)"))
        assertFalse(dock.contains("glassLabLensMatte"))
        assertTrue(dock.contains("NavLiquidDockTabs.activeInk(dark)"))
        assertTrue(dock.contains("detectHorizontalDragGestures("))
        assertTrue(dock.contains("NavLiquidDockOptics.selection.blurDp"))
        assertTrue(dock.contains("navLiquidGlassRim(dark)"))
    }

    @Test fun fallbackNightCapsuleHasNoCyanButDayAndDockMaterialRemainUnchanged() {
        val fallback = File("src/main/java/com/navtasks/app/NavGlass.kt").readText()
        assertTrue(fallback.contains("if (p.dark) Color(0xE83A3A3C) else Color(0xB2D7D8DA)"))
        assertTrue(fallback.contains("if (p.dark) Color(0xA357575A) else Color(0xC4FAFAFB)"))
        assertTrue(fallback.contains("if (p.dark) Color(0xC7E0E0E2)"))
        assertTrue(fallback.contains("val activeInk = NavLiquidDockTabs.activeInk(p.dark)"))
    }
}
