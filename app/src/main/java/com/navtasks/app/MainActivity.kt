package com.navtasks.app

import android.Manifest
import android.content.Intent
import android.content.res.Configuration
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.WindowInsetsController
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity() {
    private val profileStore by lazy { ProfileStore(this) }
    private val taskStore by lazy { TaskStore(this, profileStore) }
    private val appSettings by lazy { NavSettings(this) }
    private var installedUpdateNotice by mutableStateOf<String?>(null)
    // One request object prevents profile and record routing from racing.
    // A serial guarantees repeated taps on the same notification are handled.
    private var nextOpenSerial = 0L
    private var openRequest by mutableStateOf<NavOpenRequest?>(null)

    private fun receiveOpenIntent(incoming: Intent) {
        val profileId = incoming.getStringExtra("profileId")
        val runKey = incoming.getStringExtra("runKey")
        val workOrderId = incoming.getStringExtra("workOrderId")
        val openMissed = incoming.getBooleanExtra("openMissed", false)
        openRequest = if (profileId != null || runKey != null ||
            workOrderId != null || openMissed) {
            NavOpenRequest(++nextOpenSerial, profileId, runKey, workOrderId, openMissed)
        } else null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // The native splash is already navy, and the Activity window is given
        // the actual saved app theme before Compose's first frame.
        val savedDark = when (appSettings.theme) {
            UiTheme.DARK -> true
            UiTheme.LIGHT -> false
            UiTheme.SYSTEM -> (resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        }
        window.setBackgroundDrawable(ColorDrawable(
            Color.parseColor(if (savedDark) "#000000" else "#E6E8EC")
        ))
        receiveOpenIntent(intent)
        installedUpdateNotice = NavUpdateReceipt.pending(this)
        setContent {
            // Sync status/navigation icon foregrounds to the actual selected app
            // theme, not only to Android's global Day/Night setting.
            val dark = when (appSettings.theme) {
                UiTheme.DARK -> true
                UiTheme.LIGHT -> false
                UiTheme.SYSTEM -> isSystemInDarkTheme()
            }
            SideEffect {
                // Keep the pre-Compose background in sync when Night/Day is
                // changed in Settings, so window exposures never flash white.
                window.setBackgroundDrawable(ColorDrawable(
                    Color.parseColor(if (dark) "#000000" else "#E6E8EC")
                ))
                @Suppress("DEPRECATION")
                window.statusBarColor = Color.parseColor(
                    if (dark) "#000000" else "#E6E8EC")
                @Suppress("DEPRECATION")
                window.navigationBarColor = Color.parseColor(
                    if (dark) "#000000" else "#E6E8EC")
                val flags = WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or
                    WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                window.insetsController?.setSystemBarsAppearance(
                    if (dark) 0 else flags,
                    flags,
                )
            }
            // Refresh already-placed launcher widgets whenever NavTasks theme
            // or system Day/Night actually changes while the app is active.
            // The RemoteViews palette still uses host-resolved colors for
            // configuration changes happening while the app is not open.
            LaunchedEffect(appSettings.theme, dark) {
                NavWidgetUpdater.updateAll(this@MainActivity, taskStore)
            }
            // Startup check only: toggling settings does not produce intrusive popups.
            LaunchedEffect(Unit) {
                if (appSettings.automaticUpdateOffers) {
                    if (NavUpdateQa.isQaBuild(applicationContext) &&
                        appSettings.qaAutomaticTestOffers) {
                        if (NavUpdateChecker.checkTestAutomatically(applicationContext)) {
                            NavUpdateFlow.openTestAutomatically()
                        }
                    } else {
                        NavUpdateChecker.check(applicationContext, manual = false)
                    }
                }
            }
            NavTasksApp(
                store = taskStore, settings = appSettings, profiles = profileStore,
                openRequest = openRequest,
                onOpenRequestHandled = { handledSerial ->
                    if (openRequest?.serial == handledSerial) openRequest = null
                },
            )
            val receipt = installedUpdateNotice
            if (receipt != null) {
                NavUpdateInstalledNotice(
                    version = receipt, dark = dark, language = appSettings.language,
                    onAcknowledge = {
                        if (NavUpdateReceipt.acknowledge(this, receipt)) {
                            installedUpdateNotice = null
                        }
                    },
                )
            } else {
                NavUpdateOffer(settings = appSettings, dark = dark, context = this)
            }
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
        receiveOpenIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        taskStore.refresh()
        NavNotifications.reschedule(this, taskStore, appSettings)
        NavWidgetUpdater.updateAll(this, taskStore)
    }
}
