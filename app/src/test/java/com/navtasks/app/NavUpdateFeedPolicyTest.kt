package com.navtasks.app

import org.junit.Assert.*
import org.junit.Test

class NavUpdateFeedPolicyTest {
    // Pure contract assertions, no real network or device installation.
    private val source = java.io.File("src/main/java/com/navtasks/app/NavUpdateChecker.kt").readText()
    private val manifest = java.io.File("src/main/AndroidManifest.xml").readText()
    private val stableNetwork = java.io.File("src/main/java/com/navtasks/app/NavStableFeedNetwork.kt").readText()

    @Test fun onlyStablePinnedPublicChannelIsAccepted() {
        assertTrue(stableNetwork.contains("NavTasks-Updates/main/channels/stable.json"))
        assertTrue(stableNetwork.contains("navStableFeedAllowedUrl(address)"))
        assertTrue(source.contains("NavStableFeedNetwork.read()"))
        assertTrue(source.contains("getString(\"channel\") != \"stable\""))
        assertTrue(source.contains("getBoolean(\"available\")"))
        assertTrue(source.contains("expectedPackage != \"com.navtasks.app.glass\""))
        assertTrue(source.contains("apk.getString(\"url\") != expectedUrl"))
    }
    @Test fun skipIncompatibleOrDowngradeAndLimitNetwork() {
        assertTrue(source.contains("code <= installedCode"))
        assertTrue(source.contains("floor > installedCode"))
        assertTrue(source.contains("CHECK_INTERVAL_MS = 24L"))
        assertTrue(stableNetwork.contains("connectTimeout = 10_000"))
        assertTrue(stableNetwork.contains("readTimeout = 15_000"))
    }
    @Test fun downloadAndInstallerRequireExplicitAndroidPermission() {
        assertTrue(manifest.contains("android.permission.INTERNET"))
        assertTrue(manifest.contains("android.permission.REQUEST_INSTALL_PACKAGES"))
        assertTrue(manifest.contains(".NavUpdatesFileProvider"))
        assertFalse(source.contains("startActivity("))
        assertFalse(source.contains("FileProvider"))
    }
}
