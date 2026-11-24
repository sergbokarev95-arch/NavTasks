package com.navtasks.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NavPreReleaseUiPolishTest {
    @Test fun shortTapHasVisibleDurationAndControlledSize() {
        assertTrue(NavPressFeedbackTokens.minClickPulseMs >= 100L)
        assertTrue(NavPressFeedbackTokens.pressReleaseMs > NavPressFeedbackTokens.pressRiseMs)
        assertTrue(NavPressFeedbackTokens.cardScale in 1.03f..1.04f)
        assertTrue(NavPressFeedbackTokens.floatingActionScale in 1.06f..1.08f)
    }

    @Test fun calendarStyleDockGeometryStaysConstantDuringTravel() {
        assertEquals(1f, NavLiquidDockTabs.movingScale, 0f)
        assertEquals(54f, NavLiquidGlassActionTokens.addDiameterDp, 0f)
        assertEquals(44f, NavLiquidGlassActionTokens.compactDiameterDp, 0f)
    }
}
