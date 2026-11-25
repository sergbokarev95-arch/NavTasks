package com.navtasks.app

import java.io.File
import org.junit.Assert.*
import org.junit.Test

class NavSettingsRedesignTest {
    private fun source(name: String) = File("src/main/java/com/navtasks/app/$name.kt").readText()

    @Test fun settingsUseSharedNeutralChoicesAndSwitches() {
        val ui = source("PlanAndTasksUi").substringAfter("fun SettingsScreen(")
        assertTrue(ui.contains("NavEventOptionGrid(listOf("))
        assertTrue(ui.contains("columns = 2"))
        assertTrue(ui.contains("columns = 3"))
        assertTrue(ui.contains("navEditorSwitchColors(p)"))
        assertTrue(ui.contains("NavSettingsPanel(p)"))
        assertTrue(ui.contains("NavEventValueRow("))
        assertFalse(ui.contains("OutlinedButton("))
        assertFalse(ui.contains("p.blue"))
        assertTrue(ui.contains("settings.setReminderTime(it.hour, it.minute)"))
        assertTrue(ui.contains("settings.setDigestTime(it.hour, it.minute)"))
        assertTrue(ui.contains("NavUpdateChecker.check(context, manual = true)"))
        assertTrue(ui.contains("settings.updateAutomaticUpdateOffers(it)"))
    }

    @Test fun profileAndSelectiveExchangeKeepSafetyAndNeutralStyle() {
        val profiles = source("ProfilesUi").substringAfter("fun ProfileExchangePanel(")
        val selective = source("SelectiveExchangeUi")
        for (ui in listOf(profiles, selective)) {
            assertTrue(ui.contains("NavSettingsPanel(p)"))
            assertTrue(ui.contains("neutralSurface = true, neutralActions = true"))
            assertFalse(ui.contains("p.blue"))
        }
        assertTrue(profiles.contains("profiles.importReplace(incoming)"))
        assertTrue(profiles.contains("profiles.delete(victim.id)"))
        assertTrue(profiles.contains("selectedProfile.kind == NavProfileKind.VESSEL"))
        assertTrue(selective.contains("profiles.mergeSelected(bundle)"))
        assertTrue(selective.contains("checked.isNotEmpty()"))
    }
}
