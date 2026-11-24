package com.navtasks.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** HONOR Calendar-like movement keeps the lens steady; only a tiny re-tap pulse remains. */
class NavLiquidDockClassicPulseTest {
    private fun eq(expected: Float, actual: Float) =
        assertEquals(expected, actual, 0.0001f)

    @Test fun dockKeepsRestingSizeThroughoutTravel() {
        eq(56f, NavLiquidDockTabs.dockHeightDp)
        eq(52f, NavLiquidDockTabs.restingLensHeightDp)
        eq(1f, NavLiquidDockTabs.scaleAt(0f))
        eq(1f, NavLiquidDockTabs.scaleAt(.5f))
        eq(1f, NavLiquidDockTabs.scaleAt(1f))
    }

    @Test fun repeatedTapFeedbackIsSmallAndReturnsToRest() {
        assertTrue(NavLiquidDockTabs.repeatTapScale > 1f)
        assertTrue(NavLiquidDockTabs.repeatTapScale < 1.10f)
        eq(1.025f, NavLiquidDockTabs.repeatTapScale)
        eq(1f, NavLiquidDockTabs.scaleAt(0f))
    }

    @Test fun geometryClampsScaleIndependentOfTap() {
        eq(1f, NavLiquidDockTabs.scaleAt(-2f))
        eq(1f, NavLiquidDockTabs.scaleAt(2f))
        assertEquals(4, NavLiquidDockTabs.items.size)
    }
}
