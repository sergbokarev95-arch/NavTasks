package com.navtasks.app

import org.junit.Assert.assertEquals
import org.junit.Test

class NavGlassDiagnosticsTest {
    @Test fun modesStayIndependent() {
        assertEquals(listOf(0, 1, 2), NavGlassDiagnosticModes.modes.map { it.first })
        assertEquals(3, NavGlassDiagnosticModes.modes.map { it.second }.distinct().size)
    }
    @Test fun exactLabOpticsAreNotModified() {
        assertEquals(2.0f, NavLiquidDockOptics.bar.blurDp, 0f)
        assertEquals(1.5f, NavLiquidDockOptics.selection.blurDp, 0f)
        assertEquals(36f, NavLiquidDockOptics.bar.refractionAmountDp, 0f)
        assertEquals(1.30f, NavLiquidDockTabs.movingScale, 0f)
    }
}
