package com.navtasks.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowInsetsController
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity() {
    private val taskStore by lazy { TaskStore(this) }
    private val appSettings by lazy { NavSettings(this) }
    private var requestedRunKey by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedRunKey = intent.getStringExtra("runKey")
        setContent {
            // Sync status/navigation icon foregrounds to the actual selected app
            // theme, not only to Android's global Day/Night setting.
            val dark = when (appSettings.theme) {
                UiTheme.DARK -> true
                UiTheme.LIGHT -> false
                UiTheme.SYSTEM -> isSystemInDarkTheme()
            }
            SideEffect {
                val flags = WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or
                    WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                window.insetsController?.setSystemBarsAppearance(
                    if (dark) 0 else flags,
                    flags,
                )
            }
            NavTasksApp(taskStore, appSettings, requestedRunKey) { requestedRunKey = null }
        }
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 7001)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        requestedRunKey = intent.getStringExtra("runKey")
    }

    override fun onResume() {
        super.onResume()
        taskStore.refresh()
        NavNotifications.reschedule(this, taskStore, appSettings)
        NavWidgetUpdater.updateAll(this, taskStore)
    }
}
