package com.navtasks.app

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.os.Build

/**
 * HONOR/MagicOS rendering experiment. These temporary samples use their OWN
 * notification channel and IDs. No scheduling, task data, user preferences,
 * notification delivery markers, or real reminder icons are modified.
 *
 * Android's SmallIcon always remains required. Version C intentionally adds
 * a LargeIcon ONLY to test whether MagicOS shows it as the desired left tile;
 * the platform may add a right-hand monochrome small badge as a consequence.
 * Do not enable C on actual reminders until the owner verifies the device.
 */
internal object NavNotificationIconLab {
    private const val CHANNEL = "navtasks-icon-compare-v1"
    private const val TAG = "navtasks-icon-compare"
    private const val FIRST_ID = 27110
    private const val BRAND_BLUE = 0xFF0071E3.toInt()

    internal enum class Variant {
        STANDARD, COLORED_SMALL, COLOR_TILE
    }

    internal fun smallIconFor(variant: Variant): Int =
        if (variant == Variant.COLORED_SMALL) R.drawable.navtasks_notification_colored_small
        else R.drawable.navtasks_notification_small

    internal fun hasLargeIcon(variant: Variant): Boolean =
        variant == Variant.COLOR_TILE

    fun show(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= 33 &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED) return false

        val manager = context.getSystemService(NotificationManager::class.java)
        if (!manager.areNotificationsEnabled()) return false
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL, "NavTasks icon comparison",
                NotificationManager.IMPORTANCE_LOW,
            ),
        )
        val titles = listOf(
            "A · Стандартный Android",
            "B · Цветной контур",
            "C · Цветной фирменный",
        )
        val details = listOf(
            "Системная монохромная пиктограмма",
            "Синие контуры на прозрачном фоне",
            "Белая плитка, серые карточки, синий знак",
        )
        Variant.entries.forEachIndexed { index, variant ->
            val notification = Notification.Builder(context, CHANNEL)
                .setSmallIcon(smallIconFor(variant))
                .setContentTitle(titles[index])
                .setContentText(details[index])
                .setColor(BRAND_BLUE)
                .setAutoCancel(true)
                .setShowWhen(false)
                                .apply {
                    if (hasLargeIcon(variant)) {
                        setLargeIcon(Icon.createWithResource(
                            context, R.drawable.navtasks_notification_color_tile))
                    }
                }
                .build()
            manager.notify(TAG, FIRST_ID + index, notification)
        }
        return true
    }

    fun clear(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        Variant.entries.forEachIndexed { index, _ ->
            manager.cancel(TAG, FIRST_ID + index)
        }
    }
}
