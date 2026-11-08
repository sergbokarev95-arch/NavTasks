package com.navtasks.app

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class UiLanguage { RU, EN }
enum class UiTheme { SYSTEM, LIGHT, DARK }

class NavSettings(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("navtasks-settings", Context.MODE_PRIVATE)

    var language by mutableStateOf(runCatching {
        UiLanguage.valueOf(prefs.getString("language", "RU") ?: "RU")
    }.getOrDefault(UiLanguage.RU))
        private set
    var theme by mutableStateOf(runCatching {
        UiTheme.valueOf(prefs.getString("theme", "SYSTEM") ?: "SYSTEM")
    }.getOrDefault(UiTheme.SYSTEM))
        private set
    var notificationsEnabled by mutableStateOf(prefs.getBoolean("notifications", true))
        private set
    var dailyDigestEnabled by mutableStateOf(prefs.getBoolean("digest", true))
        private set
    var digestHour by mutableStateOf(prefs.getInt("digest_hour", 8))
        private set
    var digestMinute by mutableStateOf(prefs.getInt("digest_minute", 0))
        private set

    // Migration: previous "digest time" was the only clock exposed in Settings.
    // Seed the new independent task reminder clock from that exact user choice.
    var reminderHour by mutableStateOf(
        prefs.getInt("reminder_hour", prefs.getInt("digest_hour", 13)).coerceIn(0, 23)
    )
        private set
    var reminderMinute by mutableStateOf(
        prefs.getInt("reminder_minute", prefs.getInt("digest_minute", 0)).coerceIn(0, 59)
    )
        private set
    var groupSimultaneousReminders by mutableStateOf(prefs.getBoolean("group_reminders", true))
        private set

    fun updateLanguage(value: UiLanguage) {
        language = value
        prefs.edit().putString("language", value.name).apply()
    }

    fun updateTheme(value: UiTheme) {
        theme = value
        prefs.edit().putString("theme", value.name).apply()
    }

    fun setNotifications(value: Boolean) {
        notificationsEnabled = value
        prefs.edit().putBoolean("notifications", value).apply()
    }

    fun setDigest(value: Boolean) {
        dailyDigestEnabled = value
        prefs.edit().putBoolean("digest", value).apply()
    }

    fun setReminderTime(hour: Int, minute: Int) {
        reminderHour = hour.coerceIn(0, 23)
        reminderMinute = minute.coerceIn(0, 59)
        prefs.edit().putInt("reminder_hour", reminderHour)
            .putInt("reminder_minute", reminderMinute).apply()
    }

    fun setGroupSimultaneousReminders(value: Boolean) {
        groupSimultaneousReminders = value
        prefs.edit().putBoolean("group_reminders", value).apply()
    }

    fun setDigestTime(hour: Int, minute: Int) {
        digestHour = hour.coerceIn(0, 23)
        digestMinute = minute.coerceIn(0, 59)
        prefs.edit().putInt("digest_hour", digestHour).putInt("digest_minute", digestMinute).apply()
    }
}
