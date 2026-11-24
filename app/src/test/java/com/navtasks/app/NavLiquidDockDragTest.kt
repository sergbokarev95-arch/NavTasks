package com.navtasks.app

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Gesture-preview and color contracts; physical motion is verified on HONOR. */
class NavLiquidDockDragTest {
    @Test fun aDraggedLensClampsInsideTheFourRealTabs() {
        assertEquals(0f, NavLiquidDockTabs.clampedPosition(-6f), 0f)
        assertEquals(1.37f, NavLiquidDockTabs.clampedPosition(1.37f), 0f)
        assertEquals(3f, NavLiquidDockTabs.clampedPosition(9f), 0f)
        assertEquals(0f, NavLiquidDockTabs.clampedPosition(Float.NaN), 0f)
    }

    @Test fun fingerUpChoosesOnlyTheNearestValidDestination() {
        assertEquals(0, NavLiquidDockTabs.destinationForDrag(-5f))
        assertEquals(0, NavLiquidDockTabs.destinationForDrag(.49f))
        assertEquals(1, NavLiquidDockTabs.destinationForDrag(.51f))
        assertEquals(2, NavLiquidDockTabs.destinationForDrag(1.68f))
        assertEquals(3, NavLiquidDockTabs.destinationForDrag(2.7f))
        assertEquals(3, NavLiquidDockTabs.destinationForDrag(40f))
    }

    @Test fun activeInkIsCoralNotBlueAndNightIsLegible() {
        assertEquals(Color(0xFFFF5C32), NavLiquidDockTabs.activeInk(false))
        assertEquals(Color(0xFFFF7857), NavLiquidDockTabs.activeInk(true))
        assertEquals(4, NavLiquidDockTabs.items.size)
    }

    @Test fun directDragMovesLensButNeverNavigatesUntilFingerUp() {
        val dock = File("src/main/java/com/navtasks/app/NavLiquidGlassDock.kt").readText()
        val legacy = File("src/main/java/com/navtasks/app/NavGlass.kt").readText()
        assertTrue(dock.contains("detectHorizontalDragGestures("))
        assertTrue(dock.contains("val slide = if (dragging) dragPosition else lensPosition.value"))
        assertTrue(dock.contains("lensPosition.snapTo(landing)"))
        assertTrue(dock.contains("if (destination != selected) onSelect(destination)"))
        assertTrue(dock.contains("onDragCancel = {"))
        assertTrue(dock.contains("dragPosition + delta / cellWidthPx"))
        assertTrue(dock.contains("val lensFocus = (1f - abs(slide - index.toFloat()))"))
        assertTrue(dock.contains("navLiquidGlassRim(dark)"))
        assertTrue(dock.contains("NavLiquidDockOptics.selection.blurDp"))
        assertFalse(dock.contains("val activeBlue ="))
        assertTrue(legacy.contains("val activeInk = NavLiquidDockTabs.activeInk(p.dark)"))
    }
}
