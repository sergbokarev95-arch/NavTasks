package com.navtasks.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NavFloatingActionSafeAreaTest {
    @Test fun todayAndEmbeddedHandoverClearBothBottomButtonsAndSystemGestures() {
        for (inset in listOf(0f, 16f, 24f, 34f, 48f)) {
            val bottomPadding = NavFloatingActionSafeArea.listBottomPaddingDp(inset, true)
            val buttonTopFromScreenBottom = inset +
                NavFloatingActionSafeArea.tabButtonBottomDp +
                NavFloatingActionSafeArea.actionDiameterDp
            assertEquals(buttonTopFromScreenBottom + 20f, bottomPadding, 0.001f)
            assertTrue(bottomPadding > buttonTopFromScreenBottom)
        }
    }

    @Test fun standaloneHandoverDoesNotDoubleCountConsumedNavigationInset() {
        val noInset = NavFloatingActionSafeArea.listBottomPaddingDp(0f, false)
        val largeInset = NavFloatingActionSafeArea.listBottomPaddingDp(48f, false)
        assertEquals(92f, noInset, 0.001f)
        assertEquals(noInset, largeInset, 0.001f)
    }

    @Test fun negativeInsetsAreSafelyClampedAndActionGeometryIsUnmodified() {
        assertEquals(157f,
            NavFloatingActionSafeArea.listBottomPaddingDp(-12f, true), 0.001f)
        assertEquals(54f, NavFloatingActionSafeArea.actionDiameterDp, 0f)
        assertEquals(83f, NavFloatingActionSafeArea.tabButtonBottomDp, 0f)
        assertEquals(18f, NavFloatingActionSafeArea.standaloneButtonBottomDp, 0f)
    }

    @Test fun fullScreenModulesAlreadyMeetStandaloneClearance() {
        val needed = NavFloatingActionSafeArea.listBottomPaddingDp(30f, false)
        assertTrue(106f >= needed) // Services and AMOS contentPadding
        assertTrue(180f >= needed) // Routine tasks contentPadding
    }
}
