package com.navtasks.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NavTopSearchGestureTest {
    private fun opens(
        top: Boolean = true, rested: Boolean = true,
        firstDown: Boolean = true, endTop: Boolean = true,
        dx: Float = 10f, dy: Float = 100f,
        duration: Long = 280L, multi: Boolean = false,
    ) = shouldRevealSearchOnPull(
        beganAtTop = top, restedBeforeDown = rested,
        firstMoveWasDown = firstDown, endedAtTop = endTop,
        dxPx = dx, dyPx = dy, elapsedMs = duration,
        multiTouch = multi, minPullPx = 90f,
    )

    @Test fun deliberateDownwardSwipeAtSettledTopOpensSearch() {
        assertTrue(opens())
        assertTrue(opens(dx = -30f, dy = 120f, duration = 380L))
    }
    @Test fun continuingAListScrollOrStoppingMomentumNeverOpens() {
        assertFalse(opens(top = false))
        assertFalse(opens(rested = false))
        assertFalse(opens(endTop = false))
    }
    @Test fun upDownReversalAndDiagonalOverswipeAreRejected() {
        assertFalse(opens(firstDown = false))
        assertFalse(opens(dy = -130f))
        assertFalse(opens(dx = 75f, dy = 100f))
        assertFalse(opens(dx = 75f, dy = 70f))
    }
    @Test fun quickFlickShortDragAndMultitouchAreRejected() {
        assertFalse(opens(duration = NavTopSearchGesture.minGestureMs - 1L))
        assertFalse(opens(dy = 89f))
        assertFalse(opens(multi = true))
    }
    @Test fun guardConstantsAreDeliberateAndAboveTouchSlop() {
        assertTrue(NavTopSearchGesture.minRestMs >= 200L)
        assertTrue(NavTopSearchGesture.minPullDp >= 80f)
        assertTrue(NavTopSearchGesture.minGestureMs >= 120L)
    }
}
