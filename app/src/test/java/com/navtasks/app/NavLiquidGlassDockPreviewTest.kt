package com.navtasks.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Guard the accepted 0.1.5 dock geometry and positional model. */
class NavLiquidGlassDockPreviewTest {
    @Test fun tabOrderAndScale() {
        assertEquals(
            listOf("today", "work", "handover", "tasks"),
            NavLiquidDockTabs.items.map { it.glyph }
        )
        assertEquals(1f, NavLiquidDockTabs.scaleAt(0f), 0f)
        assertEquals(1.30f, NavLiquidDockTabs.scaleAt(1f), 0.0001f)
        assertEquals(1.30f, NavLiquidDockTabs.scaleAt(2f), 0.0001f)
    }

    @Test fun lensActuallyExtendsAboveAndBelowBar() {
        val expanded = NavLiquidDockTabs.restingLensHeightDp *
            NavLiquidDockTabs.scaleAt(1f)
        assertTrue(expanded > NavLiquidDockTabs.dockHeightDp)
        val symmetricOverflow = (expanded - NavLiquidDockTabs.dockHeightDp) / 2f
        assertEquals(5.8f, symmetricOverflow, .01f)
        assertEquals(5f, NavLiquidDockOptics.bar.blurDp, 0f)
        assertEquals(4f, NavLiquidDockOptics.selection.blurDp, 0f)
        // Distortion geometry must stay exactly as in user-approved Lab 0.1.5.
        assertEquals(12f, NavLiquidDockOptics.bar.refractionHeightDp, 0f)
        assertEquals(10f, NavLiquidDockOptics.selection.refractionHeightDp, 0f)
        assertEquals(18f, NavLiquidDockOptics.selection.refractionAmountDp, 0f)
        assertEquals(36f, NavLiquidDockOptics.bar.refractionAmountDp, 0f)
    }
}
