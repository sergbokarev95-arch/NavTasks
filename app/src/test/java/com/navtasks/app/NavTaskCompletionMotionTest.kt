package com.navtasks.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NavTaskCompletionMotionTest {
    @Test fun solidGreenIsVisibleBeforeExit() {
        assertTrue(NavTaskCompletionMotion.greenHoldMillis in 150L..250L)
        assertEquals(190L, NavTaskCompletionMotion.greenHoldMillis)
    }
    @Test fun singleExitIsShorterThanOldAnimation() {
        assertEquals(250, NavTaskCompletionMotion.exitMillis)
        assertTrue(NavTaskCompletionMotion.greenHoldMillis +
            NavTaskCompletionMotion.exitMillis < 600L)
        assertEquals(48f, NavTaskCompletionMotion.dropDistanceDp, 0f)
    }
}
