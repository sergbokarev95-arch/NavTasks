package com.navtasks.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Approved optical material stays untouched; selected motion now follows HONOR Calendar. */
class NavLiquidGlassParityTest {
    @Test fun materialIsByteForByteNumericallyEqualToApprovedLab() {
        assertEquals(2.0f, NavLiquidDockOptics.bar.blurDp, 0f)
        assertEquals(12.0f, NavLiquidDockOptics.bar.refractionHeightDp, 0f)
        assertEquals(36.0f, NavLiquidDockOptics.bar.refractionAmountDp, 0f)
        assertEquals(1.5f, NavLiquidDockOptics.selection.blurDp, 0f)
        assertEquals(10.0f, NavLiquidDockOptics.selection.refractionHeightDp, 0f)
        assertEquals(18.0f, NavLiquidDockOptics.selection.refractionAmountDp, 0f)
    }
    @Test fun selectedLensAndTabOrderMatchLab() {
        assertEquals(56f, NavLiquidDockTabs.dockHeightDp, 0f)
        assertEquals(52f, NavLiquidDockTabs.restingLensHeightDp, 0f)
        assertEquals(1f, NavLiquidDockTabs.movingScale, 0f)
        assertEquals(1f, NavLiquidDockTabs.scaleAt(0f), 0f)
        assertEquals(1f, NavLiquidDockTabs.scaleAt(1f), 0f)
        assertEquals(listOf("today","work","handover","reference"), NavLiquidDockTabs.items.map{it.glyph})
        assertTrue(NavLiquidDockTabs.restingLensHeightDp <= NavLiquidDockTabs.dockHeightDp)
    }
}
