package com.navtasks.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/** Dock navigation restarts only its destination's UI state, not business records. */
class NavDockResetKeyTest {
    @Test fun eachTabHasItsOwnSnapshotKey() {
        val p = "vessel-test"
        val keys = (0..3).map { primaryTabResetStateKey(p, it, 0) }
        assertEquals(4, keys.distinct().size)
        assertEquals("vessel-test/main-tab-works/dock-0", keys[1])
    }

    @Test fun retappingActiveTabGetsFreshPageButOtherTabsKeepTheirKey() {
        val p = "vessel-test"
        assertNotEquals(primaryTabResetStateKey(p, 0, 0), primaryTabResetStateKey(p, 0, 1))
        assertEquals(primaryTabResetStateKey(p, 2, 0), primaryTabResetStateKey(p, 2, 0))
        assertNotEquals(primaryTabResetStateKey(p, 0, 1), primaryTabResetStateKey(p, 2, 1))
    }

    @Test fun anotherProfileCanNeverRestorePreviousVesselScroll() {
        assertNotEquals(
            primaryTabResetStateKey("ship-a", 0, 5),
            primaryTabResetStateKey("ship-b", 0, 5),
        )
    }
}
