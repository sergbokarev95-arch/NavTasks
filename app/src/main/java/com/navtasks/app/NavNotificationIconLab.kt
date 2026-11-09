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
 * Only notification-icon rendering experiments. Actual reminders, groups,
 * schedules, and all app data are untouched. Exactly ONE preview notification
 * is sent at a time to prevent MagicOS automatically grouping the variants.
 */
internal object NavNotificationIconLab {
    private const val CHANNEL = "navtasks-icon-compare-v1"
    private const val TAG = "navtasks-icon-compare"
    private const val FIRST_ID = 27110
    private const val BRAND_BLUE = 0xFF0071E3.toInt()

    internal enum class Variant {
        STANDARD, COLORED_SMALL, COLOR_TILE, FULL_COLOR_SMALL, LAUNCHER_SMALL
    }

    internal fun smallIconFor(variant: Variant): Int = when (variant) {
        Variant.COLORED_SMALL -> R.drawable.navtasks_notification_colored_small
        Variant.FULL_COLOR_SMALL -> R.drawable.navtasks_notification_full_small
        // An isolated experiment to see whether MagicOS uses the adaptive
        // launcher artwork when this resource is supplied as SmallIcon.
        Variant.LAUNCHER_SMALL -> R.mipmap.ic_launcher
        Variant.STANDARD, Variant.COLOR_TILE -> R.drawable.navtasks_notification_small
    }

    internal fun hasLargeIcon(variant: Variant): Boolean =
        variant == Variant.COLOR_TILE

    private fun title(variant: Variant): String = when (variant) {
        Variant.STANDARD -> "A · Системная иконка"
        Variant.COLORED_SMALL -> "B · Синий контур"
        Variant.COLOR_TILE -> "C · Большая цветная"
        Variant.FULL_COLOR_SMALL -> "D · Полный цвет SmallIcon"
        Variant.LAUNCHER_SMALL -> "E · Иконка приложения"
    }

    private fun detail(variant: Variant): String = when (variant) {
        Variant.STANDARD -> "Чистая монохромная иконка Android"
        Variant.COLORED_SMALL -> "Цветной синий контур на прозрачном фоне"
        Variant.COLOR_TILE -> "LargeIcon (уже проверено — справа)"
        Variant.FULL_COLOR_SMALL -> "Белая плитка, серые карточки, синий знак"
        Variant.LAUNCHER_SMALL -> "Адаптивная иконка NavTasks без LargeIcon"
    }

    fun show(context: Context, variant: Variant): Boolean {
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
        // Clean up both old A/B/C IDs and the current single preview.
        clear(context)
        return runCatching {
            val builder = Notification.Builder(context, CHANNEL)
                .setSmallIcon(smallIconFor(variant))
                .setContentTitle(title(variant))
                .setContentText(detail(variant))
                .setColor(BRAND_BLUE)
                .setAutoCancel(true)
                .setShowWhen(false)
            if (hasLargeIcon(variant)) {
                builder.setLargeIcon(Icon.createWithResource(
                    context, R.drawable.navtasks_notification_color_tile,
                ))
            }
            manager.notify(TAG, FIRST_ID, builder.build())
        }.isSuccess
    }

    // Kept for source/API compatibility with earlier tests/call sites.
    fun show(context: Context): Boolean = show(context, Variant.STANDARD)

    fun clear(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        for (offset in 0 until 5) manager.cancel(TAG, FIRST_ID + offset)
    }
}
