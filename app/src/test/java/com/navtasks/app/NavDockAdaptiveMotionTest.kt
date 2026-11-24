package com.navtasks.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Regression: v16 appeared to have NO enlargement on actual HONOR hardware. */
class NavDockAdaptiveMotionTest {
    @Test fun visibleOneTabJumpAndAdaptiveRapidRetargetArePreserved() {
        assertEquals(1f, NavLiquidDockMotion.peakScaleForTap(0f, false), 0f)
        assertEquals(1.27f, NavLiquidDockMotion.peakScaleForTap(1f, false), .0001f)
        assertEquals(1.30f, NavLiquidDockMotion.peakScaleForTap(2f, false), .0001f)
        assertEquals(1.10f, NavLiquidDockMotion.peakScaleForTap(2f, true), .0001f)
        assertEquals(1f, NavLiquidDockMotion.peakScaleForTap(Float.NaN, false), 0f)
        assertEquals(56f, NavLiquidDockTabs.dockHeightDp, 0f)
        assertEquals(52f, NavLiquidDockTabs.restingLensHeightDp, 0f)
        assertEquals(1.30f, NavLiquidDockTabs.movingScale, 0f)
    }

    @Test fun matteAndGrowthShareRealAnimatedScale() {
        assertEquals(1f, NavLiquidDockMotion.lensMatteFromScale(1f), 0f)
        assertEquals(.30f, NavLiquidDockMotion.lensMatteFromScale(1.30f), .0001f)
        assertTrue(NavLiquidDockMotion.lensMatteFromScale(1.17f) < 1f)
        assertEquals(1f, NavLiquidDockMotion.lensMatteFromScale(Float.NaN), 0f)
    }

    @Test fun sizeShrinksDuringJumpNotInAnotherPhaseAfterLanding() {
        val dock = File("src/main/java/com/navtasks/app/NavLiquidGlassDock.kt").readText()
        assertTrue(dock.contains("val lensExpansion = remember { Animatable(1f) }"))
        assertTrue(dock.contains("LaunchedEffect(target, dragging)"))
        assertTrue(dock.contains("kotlinx.coroutines.coroutineScope {"))
        assertTrue(dock.contains("launch {"))
        assertTrue(dock.contains("NavLiquidDockMotion.peakScaleForTap(distance, rapid)"))
        assertTrue(dock.contains("NavLiquidDockMotion.expansionRiseMillis"))
        assertTrue(dock.contains("NavLiquidDockMotion.expansionFallMillis"))
        assertTrue(dock.contains("NavLiquidDockMotion.lensMatteFromScale(lensExpansion.value)"))
        assertTrue(dock.contains("val lensScale = lensExpansion.value * tapPulse.value"))
        assertTrue(dock.contains("lensPosition.animateTo("))
        assertTrue(dock.contains("if (destination != selected) onSelect(destination)"))
        assertFalse(dock.contains("val motionSpeed ="))
        assertFalse(dock.contains("lensPosition.velocity"))
        assertFalse(dock.contains("val lensExpansion = remember { Animatable(1f) }\n        LaunchedEffect(target, dragging)"))
        assertTrue(dock.contains("NavLiquidDockOptics.selection.blurDp"))
        assertTrue(dock.contains("NavDockFilledGlyph("))
    }
}
