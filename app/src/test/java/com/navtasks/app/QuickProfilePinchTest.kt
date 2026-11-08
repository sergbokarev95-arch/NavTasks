package com.navtasks.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuickProfilePinchTest {
    @Test fun deliberateTwoFingerContractionIsRecognized() {
        assertTrue(isQuickProfilePinch(180f, 125f, 96f, 48f))
        assertTrue(isQuickProfilePinch(160f, 105f, 96f, 48f))
    }

    @Test fun scrollingZoomOutAndIncidentalTouchAreIgnored() {
        assertFalse(isQuickProfilePinch(140f, 140f, 96f, 48f))
        assertFalse(isQuickProfilePinch(140f, 180f, 96f, 48f))
        assertFalse(isQuickProfilePinch(70f, 14f, 96f, 48f))
        assertFalse(isQuickProfilePinch(180f, 151f, 96f, 48f))
        assertFalse(isQuickProfilePinch(300f, 235f, 96f, 48f))
    }
}
