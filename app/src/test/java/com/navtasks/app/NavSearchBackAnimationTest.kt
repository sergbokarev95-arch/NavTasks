package com.navtasks.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NavSearchBackAnimationTest {
    @Test fun firstBackWithVisibleKeyboardBelongsToIme() {
        assertFalse(shouldHandleSearchBack(searchOpen = true, imeVisible = true))
    }

    @Test fun secondBackClosesSearchWithoutNavigatingAway() {
        assertTrue(shouldHandleSearchBack(searchOpen = true, imeVisible = false))
    }

    @Test fun whenSearchIsClosedParentCanHandleBack() {
        assertFalse(shouldHandleSearchBack(searchOpen = false, imeVisible = false))
        assertFalse(shouldHandleSearchBack(searchOpen = false, imeVisible = true))
    }

    @Test fun searchTransitionsAreSoftAndDoNotOvershoot() {
        assertTrue(NavSearchTransition.showHeightMs in 220..320)
        assertTrue(NavSearchTransition.hideHeightMs in 180..300)
        assertTrue(NavSearchTransition.showFadeMs <= NavSearchTransition.showHeightMs)
        assertTrue(NavSearchTransition.hideFadeMs <= NavSearchTransition.hideHeightMs)
    }
}
