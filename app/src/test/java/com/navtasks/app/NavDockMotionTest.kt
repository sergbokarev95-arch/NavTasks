package com.navtasks.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class NavDockMotionTest {
    @Test fun lensGeometryDoesNotPulseDuringPageNavigation() {
        assertEquals(56f, NavLiquidDockTabs.dockHeightDp, 0f)
        assertEquals(52f, NavLiquidDockTabs.restingLensHeightDp, 0f)
        assertEquals(1f, NavLiquidDockTabs.movingScale, 0f)
        assertEquals(1f, NavLiquidDockTabs.scaleAt(.5f), 0f)
        assertTrue(NavLiquidDockTabs.repeatTapScale <= 1.025f)
    }

    @Test fun oneInterruptiblePositionSpringNoLaggingVisualSprings() {
        val dock = File("src/main/java/com/navtasks/app/NavLiquidGlassDock.kt").readText()
        assertTrue(dock.contains("val lensPosition = remember { Animatable(target) }"))
        assertTrue(dock.contains("dampingRatio = .95f, stiffness = 850f"))
        assertTrue(dock.contains("val lensScale = tapPulse.value"))
        assertTrue(dock.contains("val slide = if (dragging) dragPosition else lensPosition.value"))
        assertTrue(dock.contains("drawRect(resting)"))
        assertTrue(dock.contains("if (destination != selected) onSelect(destination)"))
        assertFalse(dock.contains("glassLabLensStretch"))
        assertFalse(dock.contains("glassLabLensMatte"))
        assertFalse(dock.contains("lerp(traveling, resting, matte"))
        assertTrue(dock.contains("NavLiquidDockOptics.selection.blurDp"))
    }
}
