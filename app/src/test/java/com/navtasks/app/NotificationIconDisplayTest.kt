package com.navtasks.app

import org.junit.Assert.assertEquals
import org.junit.Test

/** Restrict the full-color Android SmallIcon experiment to HONOR/MagicOS. */
class NotificationIconDisplayTest {
    @Test fun honorUsesFullColorAdaptiveSmallIcon() {
        for (m in listOf("HONOR", "honor", "Honor")) {
            assertEquals(R.drawable.navtasks_notification_adaptive,
                NavNotifications.smallIconForManufacturer(m))
        }
    }

    @Test fun otherPhonesKeepOriginalMonochromeSmallIcon() {
        for (m in listOf("samsung", "Google", "Xiaomi", "HUAWEI", "")) {
            assertEquals(R.drawable.navtasks_notification_small,
                NavNotifications.smallIconForManufacturer(m))
        }
    }
}
