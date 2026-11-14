package com.navtasks.app

import org.junit.Assert.*
import org.junit.Test
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/** QA11 icon contract: preserve one installed APK, color layers and semantic task symbol. */
class NavLauncherIconThemeTest {
    private fun xml(path: String) = File("src/main/res/$path").readText()

    @Test fun adaptiveIconContainsDistinctNormalBackgroundAndMonochromeLayer() {
        val adaptive = xml("mipmap-anydpi-v26/ic_launcher.xml")
        assertTrue(adaptive.contains("@drawable/navtasks_icon_background"))
        assertTrue(adaptive.contains("@drawable/navtasks_icon_foreground"))
        assertTrue(adaptive.contains("@drawable/navtasks_icon_monochrome"))
        for (name in listOf("foreground", "background", "monochrome")) {
            val file = File("src/main/res/drawable/navtasks_icon_${name}.xml")
            assertTrue(file.exists())
            DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
        }
    }

    @Test fun defaultBlueBelongsOnlyToBackgroundNotCompassForeground() {
        val bg = xml("drawable/navtasks_icon_background.xml")
        val fg = xml("drawable/navtasks_icon_foreground.xml")
        assertTrue(bg.contains("#087BFA"))
        assertTrue(bg.contains("#0639C7"))
        assertFalse(bg.contains("#00000000"))
        assertFalse(fg.contains("#087BFA"))
        assertFalse(fg.contains("android:fillColor=\"#0639C7\""))
        assertFalse(fg.contains("android:fillColor=\"#23C5FF\""))
        assertTrue(fg.contains("M54,18")) // north compass marker
        assertTrue(fg.contains("#FF8E13"))
        assertTrue(fg.contains("M19,54")) // west
        assertTrue(fg.contains("M89,54")) // east
        assertTrue(fg.contains("M54,89")) // south
        assertTrue(fg.contains("#FAFCFF")) // front white card
        assertTrue(fg.contains("#C7DAF4")) // rear card
        assertTrue(fg.contains("M45,54.4 L51,60.4 L64,47.4")) // check
        assertEquals(3, Regex("android:fillColor=\"#8EA6C9\"").findAll(fg).count()) // three dots
    }

    @Test fun monochromeIsTrulySingleColorForAndroidThemedIcons() {
        val mono = xml("drawable/navtasks_icon_monochrome.xml")
        assertFalse(mono.contains("#0878F7"))
        assertFalse(mono.contains("#FF8E13"))
        assertFalse(mono.contains("#0639C7"))
        assertTrue(mono.contains("M54,18"))
        assertTrue(mono.contains("M45,54.4 L51,60.4 L64,47.4"))
    }

    @Test fun settingsFooterUsesPlainNavTasksNameWithoutLegacyGlassWord() {
        val settingsUi = File("src/main/java/com/navtasks/app/PlanAndTasksUi.kt").readText()
        assertTrue(settingsUi.contains("Text(\"NavTasks \" + actualVersion"))
        assertFalse(settingsUi.contains("Text(\"NavTasks Glass \" + actualVersion"))
    }

    @Test fun noPackageOrDataMigrationForVisualOnlyChange() {
        val gradle = File("build.gradle.kts").readText()
        assertTrue(gradle.contains("versionCode = 227"))
        assertFalse(gradle.contains("versionNameSuffix = \"-rc1\""))
        assertTrue(gradle.contains("applicationId = \"com.navtasks.app\""))
        assertTrue(gradle.contains("applicationIdSuffix = \".glass\""))
        val manifest = File("src/main/AndroidManifest.xml").readText()
        assertTrue(manifest.contains("android:icon=\"@mipmap/ic_launcher\""))
        assertTrue(manifest.contains("android:roundIcon=\"@mipmap/ic_launcher\""))
    }
}
