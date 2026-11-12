package com.navtasks.app

import org.junit.Assert.*
import org.junit.Test

class NavTestUpdateFeedTest {
    private fun feed(
        channel: String = "test", available: Boolean = true,
        code: Int = 176, floor: Int = 174,
        url: String = "https://github.com/sergbokarev95-arch/NavTasks-Updates/releases/download/test-v0.4.152/NavTasks_0.4.152-test1.apk",
    ): String = """{
        "schemaVersion":1,"channel":"$channel","available":$available,
        "applicationId":"com.navtasks.app.glass","versionName":"0.4.152-test1",
        "versionCode":$code,"minSupportedVersionCode":$floor,"minSdk":31,
        "apk":{"url":"$url","sizeBytes":28500000,
            "sha256":"${"a".repeat(64)}","signerSha256":"${"b".repeat(64)}"},
        "releaseNotes":{"ru":"Тестовая версия","en":"Test version"}
    }""".trimIndent()

    @Test fun manualTestCandidateIsNeverStableAndRequiresExactPinnedUrl() {
        assertNotNull(navParseTestFeed(feed(), 174, "com.navtasks.app.glass", 36))
        assertNull(navParseTestFeed(feed(channel="stable"),174,"com.navtasks.app.glass",36))
        assertNull(navParseTestFeed(feed(available=false),174,"com.navtasks.app.glass",36))
        assertNull(navParseTestFeed(feed(url="https://evil.invalid/app.apk"),174,"com.navtasks.app.glass",36))
        assertNull(navParseTestFeed(feed(),174,"com.other.application",36))
    }

    @Test fun directUpdateRequiresNewerCodeAndCompatibleTestedFloor() {
        assertNull(navParseTestFeed(feed(code=174),174,"com.navtasks.app.glass",36))
        assertNull(navParseTestFeed(feed(floor=175),174,"com.navtasks.app.glass",36))
        assertNull(navParseTestFeed(feed(),174,"com.navtasks.app.glass",30))
        assertNotNull(navParseTestFeed(feed(),175,"com.navtasks.app.glass",36))
    }

    @Test fun secondTestReleaseAcceptedOnlyForMatchingExactUrlAndCompatibleFloor() {
        val test2 = feed(code = 180, floor = 178)
            .replace("0.4.152-test1", "0.4.158-test2")
            .replace("test-v0.4.152", "test-v0.4.158")
        assertNotNull(navParseTestFeed(test2, 178, "com.navtasks.app.glass", 36))
        assertNull(navParseTestFeed(test2, 176, "com.navtasks.app.glass", 36))
        assertNull(navParseTestFeed(test2.replace("test-v0.4.158", "test-v0.4.152"),
            178, "com.navtasks.app.glass", 36))
        assertNull(navParseTestFeed(test2.replace("0.4.158-test2", "0.4.158-test3"),
            178, "com.navtasks.app.glass", 36))
    }

    @Test fun testDoesNotAlterStableAutomaticFlow() {
        val dir = "src/main/java/com/navtasks/app/"
        val checker = java.io.File(dir+"NavUpdateChecker.kt").readText()
        val offer = java.io.File(dir+"NavUpdateOffer.kt").readText()
        val settings = java.io.File(dir+"PlanAndTasksUi.kt").readText()
        assertTrue(checker.contains("NavStableFeedNetwork.read()"))
        assertTrue(checker.contains("NavTestFeedNetwork.read()"))
        val network = java.io.File(dir+"NavTestFeedNetwork.kt").readText()
        assertTrue(network.contains("URL(url).openConnection()"))
        assertTrue(network.contains("NAV_TEST_FEED"))
        assertTrue(checker.contains("var testCandidate"))
        assertTrue(offer.contains("if (testChannel) NavUpdateChecker.testCandidate else NavUpdateChecker.candidate"))
        assertTrue(offer.contains("if (!testChannel) NavUpdateChecker.defer(context, update.versionCode)"))
        assertTrue(settings.contains("NavUpdateChecker.checkTest(context)"))
    }
}
