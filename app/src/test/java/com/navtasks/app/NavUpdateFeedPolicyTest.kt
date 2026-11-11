package com.navtasks.app

import org.junit.Assert.*
import org.junit.Test

class NavUpdateFeedPolicyTest {
    // Pure contract assertions, no real network or device installation.
    private val source = java.io.File("src/main/java/com/navtasks/app/NavUpdateChecker.kt").readText()
    private val manifest = java.io.File("src/main/AndroidManifest.xml").readText()

    @Test fun onlyStablePinnedPublicChannelIsAccepted() {
        assertTrue(source.contains("NavTasks-Updates/main/channels/stable.json"))
        assertTrue(source.contains("getString(\"channel\") != \"stable\""))
        assertTrue(source.contains("getBoolean(\"available\")"))
        assertTrue(source.contains("expectedPackage != \"com.navtasks.app.glass\""))
        assertTrue(source.contains("apk.getString(\"url\") != expectedUrl"))
    }
    @Test fun skipIncompatibleOrDowngradeAndLimitNetwork() {
        assertTrue(source.contains("code <= installedCode"))
        assertTrue(source.contains("floor > installedCode"))
        assertTrue(source.contains("CHECK_INTERVAL_MS = 24L"))
        assertTrue(source.contains("connectTimeout = 3500"))
        assertTrue(source.contains("readTimeout = 3500"))
    }
    @Test fun noInstallPermissionsInReadOnlyStage() {
        assertTrue(manifest.contains("android.permission.INTERNET"))
        assertFalse(manifest.contains("REQUEST_INSTALL_PACKAGES"))
        assertFalse(source.contains("startActivity("))
        assertFalse(source.contains("FileProvider"))
    }
}
