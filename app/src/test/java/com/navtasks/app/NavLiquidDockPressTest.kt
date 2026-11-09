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

    @Test fun quickTapKeepsDestinationBetweenReleaseAndSelection() {
        // Start Today, touch Handover: lens follows the finger immediately.
        assertEquals(2, NavLiquidDockTabs.lensTargetIndex(0, 2, -1))
        // Pointer lifted, onClick committed but parent state not yet recomposed.
        // The lens must NOT spring back toward Today for a single frame.
        assertEquals(2, NavLiquidDockTabs.lensTargetIndex(0, -1, 2))
        assertEquals(2, NavLiquidDockTabs.lensTargetIndex(2, -1, -1))
    }

    @Test fun canceledPressRestoresSelectedTabWithoutClick() {
        assertEquals(0, NavLiquidDockTabs.lensTargetIndex(0, -1, -1))
        assertEquals(3, NavLiquidDockTabs.lensTargetIndex(3, -1, -1))
    }

    @Test fun newerPressOverridesPreviousPendingClickAndRespectsEdges() {
        assertEquals(0, NavLiquidDockTabs.lensTargetIndex(1, 0, 2))
        assertEquals(3, NavLiquidDockTabs.lensTargetIndex(1, 3, 0))
        assertEquals(3, NavLiquidDockTabs.lensTargetIndex(8, -1, -1))
        assertEquals(0, NavLiquidDockTabs.lensTargetIndex(-3, -1, -1))
    }

    @Test fun releaseRestoresMatteRestingLens() {
        eq(1f, NavLiquidDockTabs.scaleAt(NavLiquidDockTabs.stretchTarget(false, false)))
        eq(1f, NavLiquidDockTabs.scaleAt(-3f))
        eq(1.30f, NavLiquidDockTabs.scaleAt(3f))
    }
}
