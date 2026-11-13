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
        for (name in listOf("compact", "wide", "compact_light", "compact_dark", "wide_light", "wide_dark")) {
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
        // A different layout id is selected for both sizes and both palettes:
        // no single cached root drawable can outlive a day/night change.
        for (layout in listOf("compact_dark", "compact_light", "wide_dark", "wide_light")) {
            assertTrue(updater.contains("R.layout.navtasks_widget_${layout}"))
            val source = source("res/layout/navtasks_widget_${layout}.xml")
            assertTrue(source.contains("@drawable/navtasks_widget_bg_" +
                if (layout.endsWith("dark")) "dark" else "light"))
        }
        assertTrue(updater.contains("manager.updateAppWidget(id, view)"))
        assertTrue(updater.contains("setTextColor(viewId, if (dark) darkColor else light)"))
        assertFalse(updater.contains("setColorInt(viewId, \"setTextColor\""))
        assertFalse(updater.contains("\"setBackgroundResource\""))
        assertTrue(updater.contains("NavNotifications.openTask(context)"))
        assertFalse(updater.contains("intent.action == \"WIDGET_TOGGLE\""))
        for (name in listOf("compact", "wide")) {
            val info = source("res/xml/navtasks_widget_${name}_info.xml")
            assertTrue(info.contains("android:updatePeriodMillis=\"1800000\""))
        }
    }

    @Test fun missedPillAndReadableTextHaveNoStateMutation() {
        val widget = source("java/com/navtasks/app/NavWidgets.kt")
        assertTrue(widget.contains("widgetMissedVisible(stats.missed)"))
        assertTrue(source("res/layout/navtasks_widget_wide_dark.xml")
            .contains("@drawable/navtasks_widget_missed_dark"))
        assertTrue(source("res/layout/navtasks_widget_wide_light.xml")
            .contains("@drawable/navtasks_widget_missed_light"))
        assertTrue(source("res/layout/navtasks_widget_compact_dark.xml")
            .contains("@drawable/navtasks_widget_missed_dark"))
        assertTrue(source("res/layout/navtasks_widget_compact_light.xml")
            .contains("@drawable/navtasks_widget_missed_light"))
        assertTrue(widget.contains("widgetWideCaption(stats, language)"))
        assertFalse(NAV_WIDGET_DIRECT_COMPLETE)
        assertFalse(widget.contains("setSimpleComplete("))
        assertFalse(widget.contains("postpone("))
    }

    @Test fun widePercentIsReadableBesideTheProgressBarInBothThemes() {
        val dark = source("res/drawable/navtasks_widget_bg_dark.xml")
        assertTrue(dark.contains("#E0000000"))
        assertFalse(dark.contains("#E7282D36"))
        for (mode in listOf("light", "dark")) {
            val wide = source("res/layout/navtasks_widget_wide_${mode}.xml")
            val headerEnd = wide.indexOf("<!-- Percentage belongs")
            val percentIndex = wide.indexOf("@+id/nav_widget_percent")
            val barIndex = wide.indexOf("@+id/nav_widget_bar")
            assertTrue(headerEnd > 0)
            assertTrue(percentIndex > barIndex && barIndex > headerEnd)
            assertTrue(wide.substring(percentIndex).contains("android:textSize=\"16sp\""))
            assertTrue(wide.substring(0, headerEnd).contains("@+id/nav_widget_date"))
            assertFalse(wide.substring(0, headerEnd).contains("@+id/nav_widget_percent"))
        }
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
