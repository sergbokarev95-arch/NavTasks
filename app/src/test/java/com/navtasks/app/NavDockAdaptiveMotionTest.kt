package com.navtasks.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** User's 486876.mp4 vs HONOR Calendar 486877.mp4: no pause after landing. */
class NavDockAdaptiveMotionTest {
    @Test fun originalJumpGeometryAndAllFourRoutesRemain() {
        assertEquals(56f, NavLiquidDockTabs.dockHeightDp, 0f)
        assertEquals(52f, NavLiquidDockTabs.restingLensHeightDp, 0f)
        assertEquals(1.30f, NavLiquidDockTabs.movingScale, 0f)
        assertEquals(listOf("today", "work", "handover", "reference"),
            NavLiquidDockTabs.items.map { it.glyph })
    }

    @Test fun scaleImmediatelyTracksSpeedAndReturnsToRestWithZeroVelocity() {
        assertEquals(1f, NavLiquidDockMotion.lensScale(0f), 0f)
        assertEquals(1f, NavLiquidDockMotion.lensScale(Float.NaN), 0f)
        assertEquals(1f, NavLiquidDockMotion.lensScale(Float.POSITIVE_INFINITY), 0f)
        assertEquals(1f, NavLiquidDockMotion.lensScale(Float.NEGATIVE_INFINITY), 0f)
        val low = NavLiquidDockMotion.lensScale(5f)
        val medium = NavLiquidDockMotion.lensScale(11f)
        val full = NavLiquidDockMotion.lensScale(24f)
        assertTrue(low > 1f)
        assertTrue(medium > low)
        assertTrue(full > medium)
        assertEquals(1.30f, full, .0001f)
        assertEquals(medium, NavLiquidDockMotion.lensScale(-11f), 0f)
        // No delayed shrink animation: visual state returns in the SAME frame
        // that physical velocity reaches zero.
        assertEquals(1f, NavLiquidDockMotion.lensScale(0f), 0f)
        assertEquals(1f, NavLiquidDockMotion.lensMatte(0f), 0f)
        assertTrue(NavLiquidDockMotion.lensMatte(20f) < 1f)
    }

    @Test fun retargetingUsesCurrentSpringVelocityAndNoSecondaryExpansionJob() {
        val dock = File("src/main/java/com/navtasks/app/NavLiquidGlassDock.kt").readText()
        assertTrue(dock.contains("val lensPosition = remember { Animatable(target) }"))
        assertTrue(dock.contains("LaunchedEffect(target, dragging)"))
        assertTrue(dock.contains("lensPosition.animateTo("))
        assertTrue(dock.contains("NavLiquidDockMotion.positionStiffness"))
        assertTrue(dock.contains("else lensPosition.velocity"))
        assertTrue(dock.contains("NavLiquidDockMotion.lensScale(motionSpeed)"))
        assertTrue(dock.contains("NavLiquidDockMotion.lensMatte(motionSpeed)"))
        assertTrue(dock.contains("val slide = if (dragging) dragPosition else lensPosition.value"))
        assertTrue(dock.contains("if (destination != selected) onSelect(destination)"))
        assertFalse(dock.contains("val lensExpansion ="))
        assertFalse(dock.contains("lensExpansion.animateTo("))
        assertFalse(dock.contains("glassLabLensStretch"))
        assertFalse(dock.contains("glassLabLensMatte"))
        assertTrue(dock.contains("NavLiquidDockOptics.selection.blurDp"))
        assertTrue(dock.contains("NavDockFilledGlyph("))
    }
}
