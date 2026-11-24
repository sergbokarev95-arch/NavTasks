package com.navtasks.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Beta20: slower continuous landing with the same 15% compression. */
class NavDockLandingMotionTest {
    @Test fun deeper85PercentSquashRecoversSmoothlyAfterTouchdown() {
        assertEquals(.85f, NavLiquidDockMotion.landingSquashScale, 0f)
        assertTrue(NavLiquidDockMotion.landingSquashScale < 1f)
        assertTrue(NavLiquidDockMotion.landingSquashScale > .80f)
        assertTrue(NavLiquidDockMotion.normalLandingStartMillis >
            NavLiquidDockMotion.expansionRiseMillis)
        assertTrue(NavLiquidDockMotion.normalCompressionEndMillis >
            NavLiquidDockMotion.normalLandingStartMillis)
        assertEquals(NavLiquidDockMotion.normalCompressionEndMillis +
            NavLiquidDockMotion.landingRecoverMillis,
            NavLiquidDockMotion.normalMotionEndMillis)
        assertEquals(432, NavLiquidDockMotion.normalMotionEndMillis)
        assertTrue(NavLiquidDockMotion.landingRecoverMillis <= 125)
        assertTrue(NavLiquidDockMotion.normalMotionEndMillis <= 450)
        assertTrue(NavLiquidDockMotion.normalMotionEndMillis >= 410)
    }

    @Test fun movementIsSlightlySlowerButRetainsOriginalJumpRange() {
        assertTrue(NavLiquidDockMotion.positionStiffness == 420f)
        assertTrue(NavLiquidDockMotion.positionStiffness >= 400f)
        assertTrue(NavLiquidDockMotion.positionStiffness < 660f)
        assertTrue(NavLiquidDockMotion.positionDamping >= .85f)
        assertEquals(1.27f, NavLiquidDockMotion.peakScaleForTap(1f, false), .0001f)
        assertEquals(1.30f, NavLiquidDockMotion.peakScaleForTap(2f, false), .0001f)
        assertEquals(52f, NavLiquidDockTabs.restingLensHeightDp, 0f)
        assertEquals(1.30f, NavLiquidDockTabs.movingScale, 0f)
    }

    @Test fun fullSquashDoesNotReplayOnRapidRetargetOrDrag() {
        assertEquals(176, NavLiquidDockMotion.rapidMotionEndMillis)
        assertEquals(100, NavLiquidDockMotion.dragReleaseMillis)
        assertEquals(225L, NavLiquidDockMotion.rapidRetargetWindowMillis)
        assertTrue(NavLiquidDockMotion.rapidMotionEndMillis <
            NavLiquidDockMotion.normalMotionEndMillis)
        assertEquals(1.10f, NavLiquidDockMotion.peakScaleForTap(1f, true), .0001f)
        val dock = File("src/main/java/com/navtasks/app/NavLiquidGlassDock.kt").readText()
        assertTrue(dock.contains("animationSpec = keyframes {"))
        assertTrue(dock.contains("NavLiquidDockMotion.landingSquashScale at"))
        assertTrue(dock.contains("NavLiquidDockMotion.normalCompressionEndMillis"))
        assertTrue(dock.contains("NavLiquidDockMotion.normalMotionEndMillis"))
        assertTrue(dock.contains("if (rapid) {"))
        assertTrue(dock.contains("if (justReleasedDrag) {"))
        assertTrue(dock.contains("kotlinx.coroutines.coroutineScope {"))
        assertTrue(dock.contains("lensPosition.animateTo("))
        assertTrue(dock.contains("LaunchedEffect(target, dragging)"))
        assertFalse(dock.contains("lensPosition.velocity"))
        assertTrue(dock.contains("NavLiquidDockOptics.selection.blurDp"))
        assertTrue(dock.contains("NavDockFilledGlyph("))
        assertTrue(dock.contains("if (destination != selected) onSelect(destination)"))
    }
}
