package com.navtasks.app

import org.junit.Assert.*
import org.junit.Test

class NavAutomaticOfferQaTest {
    @Test fun qaGateNeverEnablesProduction1x() {
        assertTrue(NavUpdateQa.isQaVersionName("0.4.160-rc15"))
        for (name in listOf("1.0.0", "1.0.1", "0.4.158-test2",
            "0.4.160-rc14", "0.4.160-rc15-extra")) {
            assertFalse(NavUpdateQa.isQaVersionName(name))
        }
    }

    @Test fun automaticToggleAndLaterDoNotDisableManual() {
        assertFalse(navUpdateMayCheck(false, false))
        assertTrue(navUpdateMayCheck(true, false))
        assertTrue(navUpdateMayOffer(true, false))
        assertFalse(navUpdateMayOffer(true, true))
        assertFalse(navUpdateMayOffer(false, false))
        val dir = "src/main/java/com/navtasks/app/"
        val settings = java.io.File(dir+"NavSettings.kt").readText()
        val checker = java.io.File(dir+"NavUpdateChecker.kt").readText()
        val activity = java.io.File(dir+"MainActivity.kt").readText()
        val offer = java.io.File(dir+"NavUpdateOffer.kt").readText()
        val ui = java.io.File(dir+"PlanAndTasksUi.kt").readText()
        assertTrue(settings.contains("getBoolean(\"qa_auto_test_offers\", false)"))
        assertTrue(settings.contains("putBoolean(\"qa_auto_test_offers\", value)"))
        assertTrue(checker.contains("\"last_test_auto_success\""))
        assertTrue(checker.contains("\"dismissed_test_code\""))
        assertTrue(checker.contains("if (!automatic) lastManualMessage"))
        assertTrue(activity.contains("if (appSettings.automaticUpdateOffers)"))
        assertTrue(activity.contains("NavUpdateFlow.openTestAutomatically()"))
        assertTrue(offer.contains("NavUpdateChecker.shouldOfferTestAutomatically"))
        assertTrue(offer.contains("NavUpdateChecker.deferTest(context, update.versionCode)"))
        assertTrue(ui.contains("if (NavUpdateQa.isQaBuild(context))"))
        assertTrue(ui.contains("NavUpdateChecker.checkTest(context)"))
    }

    @Test fun pinnedTest3ParserPreservesSecurityGuards() {
        val parser = java.io.File("src/main/java/com/navtasks/app/NavTestUpdateFeed.kt").readText()
        assertTrue(parser.contains("-test(1|2|3)"))
        assertTrue(parser.contains("apk.getString(\"url\") != expectedUrl"))
        assertTrue(parser.contains("navUpdateSupportsDirectUpgrade"))
    }
}
