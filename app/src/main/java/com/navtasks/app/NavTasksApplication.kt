package com.navtasks.app

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build

/**
 * CONFIGURATION_CHANGED cannot be received by manifest-only widget providers.
 * Refresh placed widgets when system night mode changes while our process lives.
 * The platform's periodic APPWIDGET_UPDATE is a bounded backup for cold process.
 * Never creates, completes, or alters task records.
 */
class NavTasksApplication : Application() {
    private val configurationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_CONFIGURATION_CHANGED) {
                NavWidgetUpdater.updateAll(applicationContext)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        val filter = IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED)
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(configurationReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            registerReceiver(configurationReceiver, filter)
        }
    }
}
