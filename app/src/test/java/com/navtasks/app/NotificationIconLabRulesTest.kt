package com.navtasks.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationIconLabRulesTest {
    @Test fun nativeNotificationStyleKeepsMonochromeSmallIcon() {
        val variant = NavNotificationIconLab.Variant.STANDARD
        assertEquals(R.drawable.navtasks_notification_small,
            NavNotificationIconLab.smallIconFor(variant))
        assertFalse(NavNotificationIconLab.hasLargeIcon(variant))
    }
    @Test fun coloredSmallVariantNeverAddsLargeIcon() {
        val variant = NavNotificationIconLab.Variant.COLORED_SMALL
        assertEquals(R.drawable.navtasks_notification_colored_small,
            NavNotificationIconLab.smallIconFor(variant))
        assertFalse(NavNotificationIconLab.hasLargeIcon(variant))
    }
    @Test fun standaloneDUsesColoredSmallIconWithoutAddingLargeIcon() {
        assertEquals(R.drawable.navtasks_notification_tile_small,
            NavNotificationIconLab.dSmallIcon())
    }

    @Test fun onlyColorTileVariantAddsLargeIcon() {
        val variant = NavNotificationIconLab.Variant.COLOR_TILE
        assertEquals(R.drawable.navtasks_notification_small,
            NavNotificationIconLab.smallIconFor(variant))
        assertTrue(NavNotificationIconLab.hasLargeIcon(variant))
    }
}
