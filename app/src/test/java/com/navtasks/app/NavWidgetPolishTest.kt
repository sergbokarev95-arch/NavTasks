package com.navtasks.app

import org.junit.Assert.*
import org.junit.Test
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Strict pre-1.0.0 widget/reader QA gate: style must never alter task semantics.
 * Real launcher theme switching still requires HONOR and second-device testing.
 */
class NavWidgetPolishTest {
    private fun source(path: String): String = File("src/main/$path").readText()

    @Test fun bothWidgetTreesUseOnlyRemoteViewsFriendlyElements() {
        for (name in listOf("compact", "wide")) {
            val path = "res/layout/navtasks_widget_${name}.xml"
            val xml = File("src/main/$path")
            val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xml)
            val classes = listOf("LinearLayout", "TextView", "ProgressBar")
            val tags = doc.getElementsByTagName("*")
            for (i in 0 until tags.length) {
                assertTrue(tags.item(i).nodeName, tags.item(i).nodeName in classes)
            }
            val src = xml.readText()
            assertTrue(src.contains("@+id/nav_widget_title"))
            assertTrue(src.contains("@+id/nav_widget_progress"))
            assertTrue(src.contains("@+id/nav_widget_bar"))
            assertTrue(src.contains("@+id/nav_widget_secondary"))
            assertFalse(src.contains("<Space"))
            assertFalse(src.contains("onClick="))
        }
        assertTrue(source("res/layout/navtasks_widget_wide.xml")
            .contains("@+id/nav_widget_detail_caption"))
    }

    @Test fun systemModeRefreshSurvivesProcessAndDoesNotRequireReadding() {
        val manifest = source("AndroidManifest.xml")
        val app = source("java/com/navtasks/app/NavTasksApplication.kt")
        val updater = source("java/com/navtasks/app/NavWidgets.kt")
        assertTrue(manifest.contains("android:name=\".NavTasksApplication\""))
        assertTrue(app.contains("registerReceiver(configurationReceiver, filter"))
        assertTrue(app.contains("Intent.ACTION_CONFIGURATION_CHANGED"))
        assertTrue(updater.contains("R.drawable.navtasks_widget_bg_dark"))
        assertTrue(updater.contains("R.drawable.navtasks_widget_bg_light"))
        assertTrue(updater.contains("setBackgroundResource"))
        assertTrue(updater.contains("setColorInt(viewId, \"setTextColor\""))
        assertTrue(updater.contains("NavNotifications.openTask(context)"))
        assertFalse(updater.contains("WIDGET_TOGGLE"))
        for (name in listOf("compact", "wide")) {
            val info = source("res/xml/navtasks_widget_${name}_info.xml")
            assertTrue(info.contains("android:updatePeriodMillis=\"1800000\""))
        }
    }

    @Test fun missedPillAndReadableTextHaveNoStateMutation() {
        val widget = source("java/com/navtasks/app/NavWidgets.kt")
        assertTrue(widget.contains("widgetMissedVisible(stats.missed)"))
        assertTrue(widget.contains("R.drawable.navtasks_widget_missed_dark"))
        assertTrue(widget.contains("R.drawable.navtasks_widget_missed_light"))
        assertTrue(widget.contains("widgetWideCaption(stats, language)"))
        assertFalse(NAV_WIDGET_DIRECT_COMPLETE)
        assertFalse(widget.contains("setSimpleComplete("))
        assertFalse(widget.contains("postpone("))
    }

    @Test fun externalReaderActionUsesExistingOpaqueBlueButtonWithReadOnlyGrant() {
        val reader = source("java/com/navtasks/app/ReferenceManualExternalOpen.kt")
        assertTrue(reader.contains("ButtonDefaults.buttonColors"))
        assertTrue(reader.contains("containerColor = p.selectionFill"))
        assertTrue(reader.contains("contentColor = p.selectionContent"))
        assertTrue(reader.contains("Intent.ACTION_VIEW"))
        assertTrue(reader.contains("FLAG_GRANT_READ_URI_PERMISSION"))
        assertFalse(reader.contains("FLAG_GRANT_WRITE_URI_PERMISSION"))
        assertFalse(reader.contains("Intent.ACTION_EDIT"))
    }
}
