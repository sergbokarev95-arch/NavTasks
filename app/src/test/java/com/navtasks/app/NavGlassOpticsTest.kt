package com.navtasks.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Capability-only regression tests; optical quality must be checked on real hardware. */
class NavGlassOpticsTest {
    @Test fun olderAndroidKeepsExistingHazeFallback() {
        assertFalse(canRenderNavGlassRefraction(31, 900f, 120f))
        assertFalse(canRenderNavGlassRefraction(32, 900f, 120f))
    }

    @Test fun android13AndNewerUseOpticsOnlyForValidDockLayers() {
        assertTrue(canRenderNavGlassRefraction(33, 900f, 120f))
        assertTrue(canRenderNavGlassRefraction(36, 900f, 120f))
        assertFalse(canRenderNavGlassRefraction(36, 0f, 120f))
        assertFalse(canRenderNavGlassRefraction(36, 900f, 0f))
        assertFalse(canRenderNavGlassRefraction(36, Float.NaN, 120f))
        assertFalse(canRenderNavGlassRefraction(36, Float.POSITIVE_INFINITY, 120f))
        assertFalse(canRenderNavGlassRefraction(36, 900f, Float.NaN))
    }
}
