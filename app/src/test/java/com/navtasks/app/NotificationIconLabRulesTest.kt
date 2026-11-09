package com.navtasks.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationIconLabRulesTest {
    @Test fun nativeIsMonochromeWithoutLargeIcon() {
        assertEquals(R.drawable.navtasks_notification_small,
            NavNotificationIconLab.smallIconFor(NavNotificationIconLab.Variant.STANDARD))
        assertFalse(NavNotificationIconLab.hasLargeIcon(NavNotificationIconLab.Variant.STANDARD))
    }

    @Test fun blueOutlineSmallHasNoLargeIcon() {
        assertEquals(R.drawable.navtasks_notification_colored_small,
            NavNotificationIconLab.smallIconFor(NavNotificationIconLab.Variant.COLORED_SMALL))
        assertFalse(NavNotificationIconLab.hasLargeIcon(NavNotificationIconLab.Variant.COLORED_SMALL))
    }

    @Test fun brandedFullColorSmallUsesDrawableOnly() {
        assertEquals(R.drawable.navtasks_notification_full_small,
            NavNotificationIconLab.smallIconFor(NavNotificationIconLab.Variant.FULL_COLOR_SMALL))
        assertFalse(NavNotificationIconLab.hasLargeIcon(NavNotificationIconLab.Variant.FULL_COLOR_SMALL))
    }

    @Test fun launcherAdaptiveUsesMipmapAndNoLargeIcon() {
        assertEquals(R.mipmap.ic_launcher,
            NavNotificationIconLab.smallIconFor(NavNotificationIconLab.Variant.LAUNCHER_SMALL))
        assertFalse(NavNotificationIconLab.hasLargeIcon(NavNotificationIconLab.Variant.LAUNCHER_SMALL))
    }

    @Test fun priorColorTileWasOnlyVariantWithLargeIcon() {
        assertEquals(R.drawable.navtasks_notification_small,
            NavNotificationIconLab.smallIconFor(NavNotificationIconLab.Variant.COLOR_TILE))
        assertTrue(NavNotificationIconLab.hasLargeIcon(NavNotificationIconLab.Variant.COLOR_TILE))
    }
}
