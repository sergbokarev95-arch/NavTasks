package com.navtasks.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NavPersonalAndSearchViewportFixTest {
    @Test fun notesOnlyPersonalNeverLoadsOrMaterializesTaskRuns() {
        assertFalse(navProfileNeedsTaskStorage(NavProfileKind.PERSONAL))
        assertTrue(navProfileNeedsTaskStorage(NavProfileKind.VESSEL))
    }

    @Test fun scrollingIsLimitedToTheLazyListAndKeepsVisibleMatchesStill() {
        assertEquals(0f, navSearchListScrollDelta(180f, 100f, 700f, 42f), 0.001f)
        assertEquals(-30f, navSearchListScrollDelta(112f, 100f, 700f, 42f), 0.001f)
        assertEquals(62f, navSearchListScrollDelta(720f, 100f, 700f, 42f), 0.001f)
    }

    @Test fun shortViewportStillProducesFiniteScroll() {
        assertEquals(15f, navSearchListScrollDelta(160f, 100f, 170f, 45f), 0.001f)
    }
}
