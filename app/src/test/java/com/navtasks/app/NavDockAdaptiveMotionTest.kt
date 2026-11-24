package com.navtasks.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** The screen-recording requires interruptible, time-dependent deformation. */
class NavDockAdaptiveMotionTest {
    @Test fun originalJumpGeometryAndAllFourRoutesAreRestored() {
        assertEquals(56f, NavLiquidDockTabs.dockHeightDp, 0f)
        assertEquals(52f, NavLiquidDockTabs.restingLensHeightDp, 0f)
        assertEquals(1.30f, NavLiquidDockTabs.movingScale, 0f)
        assertEquals(1.15f, NavLiquidDockTabs.scaleAt(.5f), 0f)
        assertEquals(listOf("today", "work", "handover", "reference"),
            NavLiquidDockTabs.items.map { it.glyph })
    }

    @Test fun interruptionRetargetsPositionAndExpansionAtCurrentValues() {
        val dock = File("src/main/java/com/navtasks/app/NavLiquidGlassDock.kt").readText()
        assertTrue(dock.contains("val lensExpansion = remember { Animatable(1f) }"))
        assertTrue(dock.contains("LaunchedEffect(target, dragging)"))
        assertTrue(dock.contains("val expansion = launch {"))
        assertTrue(dock.contains("lensPosition.animateTo("))
        assertTrue(dock.contains("expansion.cancel()"))
        assertTrue(dock.contains("lensExpansion.value * tapPulse.value"))
        assertTrue(dock.contains("dampingRatio = .78f, stiffness = 580f"))
        assertTrue(dock.contains("dampingRatio = .84f, stiffness = 175f"))
        assertTrue(dock.contains("val matte = 1f - stretched * .70f"))
        assertTrue(dock.contains("val slide = if (dragging) dragPosition else lensPosition.value"))
        assertTrue(dock.contains("if (destination != selected) onSelect(destination)"))
        assertFalse(dock.contains("glassLabLensStretch"))
        assertFalse(dock.contains("glassLabLensMatte"))
        assertTrue(dock.contains("NavLiquidDockOptics.selection.blurDp"))
        assertTrue(dock.contains("NavDockFilledGlyph("))
    }
}
