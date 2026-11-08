package com.navtasks.app

import org.junit.Assert.assertEquals
import org.junit.Test

class NavLiquidGlassAllActionsTest {
    @Test fun floatButtonsKeepApprovedSizes() {
        assertEquals(54f, NavLiquidGlassActionTokens.addDiameterDp, 0f)
        assertEquals(44f, NavLiquidGlassActionTokens.compactDiameterDp, 0f)
    }
    @Test fun exactShaderParametersRemainFrozen() {
        assertEquals(2f, NavLiquidDockOptics.bar.blurDp, 0f)
        assertEquals(12f, NavLiquidDockOptics.bar.refractionHeightDp, 0f)
        assertEquals(36f, NavLiquidDockOptics.bar.refractionAmountDp, 0f)
        assertEquals(1.5f, NavLiquidDockOptics.selection.blurDp, 0f)
        assertEquals(10f, NavLiquidDockOptics.selection.refractionHeightDp, 0f)
        assertEquals(18f, NavLiquidDockOptics.selection.refractionAmountDp, 0f)
        assertEquals(1.30f, NavLiquidDockTabs.movingScale, 0f)
    }
}
