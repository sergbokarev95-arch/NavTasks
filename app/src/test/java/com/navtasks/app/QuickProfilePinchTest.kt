package com.navtasks.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuickProfilePinchTest {
    @Test fun deliberateTwoFingerPinchOpens() {
        assertTrue(isQuickProfilePinch(240f, 146f, 100f, 52f))
        assertTrue(isQuickProfilePinch(190f, 120f, 100f, 52f))
    }

    @Test fun scrollingSlightContractionAndSmallStartDoNotTrigger() {
        assertFalse(isQuickProfilePinch(240f, 215f, 100f, 52f))
        assertFalse(isQuickProfilePinch(80f, 10f, 100f, 52f))
        assertFalse(isQuickProfilePinch(240f, 193f, 100f, 52f))
        assertFalse(isQuickProfilePinch(240f, 260f, 100f, 52f))
        assertFalse(isQuickProfilePinch(240f, 185f, 100f, 52f))
    }
}
