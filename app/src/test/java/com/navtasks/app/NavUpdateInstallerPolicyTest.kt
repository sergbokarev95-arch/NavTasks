package com.navtasks.app

import org.junit.Assert.*
import org.junit.Test

class NavUpdateInstallerPolicyTest {
    @Test fun redirectsAreHttpsAndLimitedToGithubReleaseHosts() {
        assertTrue(navUpdateAllowedAssetHost("https://github.com/sergbokarev95-arch/NavTasks-Updates/releases/download/v1.0.0/NavTasks_1.0.0.apk"))
        assertTrue(navUpdateAllowedAssetHost("https://release-assets.githubusercontent.com/asset/file"))
        assertFalse(navUpdateAllowedAssetHost("http://github.com/foo"))
        assertFalse(navUpdateAllowedAssetHost("https://github.com.evil.example/file"))
        assertFalse(navUpdateAllowedAssetHost("https://user:pass@github.com/file"))
        assertFalse(navUpdateAllowedAssetHost("https://github.com:444/file"))
        assertFalse(navUpdateAllowedAssetHost("https://evil.com/file"))
    }

    @Test fun directUpgradeSkipsVersionsButRequiresTestedFloor() {
        assertTrue(navUpdateSupportsDirectUpgrade(165, 200, 143))
        assertTrue(navUpdateSupportsDirectUpgrade(167, 169, 167))
        assertFalse(navUpdateSupportsDirectUpgrade(165, 200, 166))
        assertFalse(navUpdateSupportsDirectUpgrade(170, 169, 143))
        assertFalse(navUpdateSupportsDirectUpgrade(169, 169, 143))
        assertFalse(navUpdateSupportsDirectUpgrade(0, 190, 1))
        assertFalse(navUpdateSupportsDirectUpgrade(165, 190, 0))
    }

    @Test fun rejectsMissingEmptyOrUnboundedApk() {
        assertFalse(navUpdateValidSize(0))
        assertFalse(navUpdateValidSize(-1))
        assertTrue(navUpdateValidSize(28_000_000))
        assertFalse(navUpdateValidSize(201L * 1024L * 1024L))
    }

    @Test fun manifestExportsOnlyUpdateCacheAndRequiresUserPermission() {
        val manifest = java.io.File("src/main/AndroidManifest.xml").readText()
        val paths = java.io.File("src/main/res/xml/navtasks_update_paths.xml").readText()
        assertTrue(manifest.contains("android.permission.REQUEST_INSTALL_PACKAGES"))
        assertTrue(manifest.contains(".NavUpdatesFileProvider"))
        assertTrue(manifest.contains("android:exported=\"false\""))
        assertTrue(manifest.contains("android:grantUriPermissions=\"true\""))
        assertTrue(paths.contains("path=\"navtasks-update/\""))
        assertFalse(paths.contains("path=\".\""))
    }
}
