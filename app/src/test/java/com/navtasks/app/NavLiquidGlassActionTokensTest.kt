package com.navtasks.app

import org.junit.Assert.assertEquals
import org.junit.Test

class NavLiquidGlassActionTokensTest {
    @Test fun addKeepsExistingCircleAndTapSize() {
        assertEquals(54f, NavLiquidGlassActionTokens.addDiameterDp, 0f)
    }

    @Test fun floatingButtonSamplesExactlyTheDockMaterial() {
        assertEquals(2f, NavLiquidDockOptics.bar.blurDp, 0f)
        assertEquals(12f, NavLiquidDockOptics.bar.refractionHeightDp, 0f)
        assertEquals(36f, NavLiquidDockOptics.bar.refractionAmountDp, 0f)
    }
}
