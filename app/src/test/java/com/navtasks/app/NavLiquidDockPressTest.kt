package com.navtasks.app

import org.junit.Assert.assertEquals
import org.junit.Test

/** Press state affects the lens only. No tab, schedule or store logic changes. */
class NavLiquidDockPressTest {
    private fun eq(expected: Float, actual: Float) =
        assertEquals(expected, actual, 0.0001f)

    @Test fun idleLensRetainsTheApprovedDockGeometry() {
        eq(0f, NavLiquidDockTabs.stretchTarget(moving = false, pressed = false))
        eq(1f, NavLiquidDockTabs.scaleAt(NavLiquidDockTabs.stretchTarget(false, false)))
        eq(56f, NavLiquidDockTabs.dockHeightDp)
        eq(52f, NavLiquidDockTabs.restingLensHeightDp)
    }

    @Test fun holdingAnyTabGrowsLensAndKeepsItGrown() {
        val held = NavLiquidDockTabs.stretchTarget(false, true)
        eq(1.15f, NavLiquidDockTabs.scaleAt(held))
        eq(held, NavLiquidDockTabs.stretchTarget(false, true))
    }

    @Test fun travelStillUsesAcceptedGlassLabOnePointThree() {
        eq(1.30f, NavLiquidDockTabs.scaleAt(
            NavLiquidDockTabs.stretchTarget(moving = true, pressed = true)))
        eq(1.30f, NavLiquidDockTabs.scaleAt(
            NavLiquidDockTabs.stretchTarget(moving = true, pressed = false)))
    }

    @Test fun releaseRestoresMatteRestingLens() {
        eq(1f, NavLiquidDockTabs.scaleAt(NavLiquidDockTabs.stretchTarget(false, false)))
        eq(1f, NavLiquidDockTabs.scaleAt(-3f))
        eq(1.30f, NavLiquidDockTabs.scaleAt(3f))
    }
}
