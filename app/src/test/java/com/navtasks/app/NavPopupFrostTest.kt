package com.navtasks.app

import org.junit.Assert.assertTrue
import org.junit.Test

class NavPopupFrostTest {
    @Test fun moderateWindowBlurRadiusPreservesReadabilityAndPerformance() {
        assertTrue(NavPopupFrostTokens.blurRadiusDp in 20f..45f)
    }
}
