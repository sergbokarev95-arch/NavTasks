package com.navtasks.app

import org.junit.Assert.*
import org.junit.Test

class NavUpdatePreferencesPolicyTest {
    @Test fun automaticAndManualCheckTruthTable() {
        assertFalse(navUpdateMayCheck(false, false))
        assertTrue(navUpdateMayCheck(true, false))
        assertTrue(navUpdateMayCheck(false, true))
        assertTrue(navUpdateMayCheck(true, true))
    }
    @Test fun deferredAndDisabledOfferTruthTable() {
        assertFalse(navUpdateMayOffer(false, false))
        assertFalse(navUpdateMayOffer(false, true))
        assertFalse(navUpdateMayOffer(true, true))
        assertTrue(navUpdateMayOffer(true, false))
    }
    @Test fun persistedSettingAndFooterPositionGuards() {
        val dir = "src/main/java/com/navtasks/app/"
        val settings = java.io.File(dir + "NavSettings.kt").readText()
        val ui = java.io.File(dir + "PlanAndTasksUi.kt").readText()
        val activity = java.io.File(dir + "MainActivity.kt").readText()
        val checker = java.io.File(dir + "NavUpdateChecker.kt").readText()
        val offer = java.io.File(dir + "NavUpdateOffer.kt").readText()
        assertTrue(settings.contains("getBoolean(\"auto_update_offers\", true)"))
        assertTrue(settings.contains("putBoolean(\"auto_update_offers\", value)"))
        val update = ui.indexOf("// This panel invokes the shared optional update flow")
        assertTrue(update > ui.indexOf("SelectiveExchangePanel("))
        assertTrue(update < ui.indexOf("Text(\"NavTasks Glass \" + actualVersion"))
        assertTrue(ui.contains("settings.updateAutomaticUpdateOffers(it)"))
        assertTrue(activity.contains("LaunchedEffect(Unit)"))
        assertTrue(activity.contains("NavUpdateChecker.checkTestAutomatically(applicationContext)"))
        assertTrue(checker.contains("NavSettings(appContext).automaticUpdateOffers"))
        assertTrue(offer.contains("settings.automaticUpdateOffers"))
    }
}
