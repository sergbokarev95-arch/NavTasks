package com.navtasks.app

import org.junit.Assert.assertEquals
import org.junit.Test

class PrimaryTabSwipeTest {
    @Test fun swipeLeftStepsThroughFourTabs() {
        assertEquals(1, tabAfterHorizontalSwipe(0, -160f, 5f, 78f, false))
        assertEquals(2, tabAfterHorizontalSwipe(1, -160f, 5f, 78f, false))
        assertEquals(3, tabAfterHorizontalSwipe(2, -160f, 5f, 78f, false))
        assertEquals(3, tabAfterHorizontalSwipe(3, -160f, 5f, 78f, false))
    }

    @Test fun swipeRightStepsBackAndClampsAtToday() {
        assertEquals(2, tabAfterHorizontalSwipe(3, 165f, 2f, 78f, false))
        assertEquals(1, tabAfterHorizontalSwipe(2, 165f, 2f, 78f, false))
        assertEquals(0, tabAfterHorizontalSwipe(1, 165f, 2f, 78f, false))
        assertEquals(0, tabAfterHorizontalSwipe(0, 165f, 2f, 78f, false))
    }

    @Test fun shorterIntentionalSwipesNowSwitchTabs() {
        assertEquals(1, tabAfterHorizontalSwipe(0, -52f, 3f, 48f, false))
        assertEquals(2, tabAfterHorizontalSwipe(1, -49f, 4f, 48f, false))
        assertEquals(1, tabAfterHorizontalSwipe(2, 50f, 4f, 48f, false))
        assertEquals(1, tabAfterHorizontalSwipe(1, 30f, 2f, 48f, false))
        assertEquals(1, tabAfterHorizontalSwipe(1, -65f, 57f, 48f, false))
    }

    @Test fun childGesturesAndVerticalScrollNeverSwitchTabs() {
        assertEquals(1, tabAfterHorizontalSwipe(1, -220f, 0f, 78f, true))
        assertEquals(0, tabAfterHorizontalSwipe(0, -220f, 240f, 78f, false))
        assertEquals(2, tabAfterHorizontalSwipe(2, 65f, 0f, 78f, false))
    }
}
