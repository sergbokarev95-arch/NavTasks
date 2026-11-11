package com.navtasks.app

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NavStableUpdaterPolicyTest {
    private fun raw(
        available: Boolean = true,
        code: Int = 170,
        floor: Int = 165,
        name: String = "1.0.0",
    ) = JSONObject()
        .put("schemaVersion", 1)
        .put("channel", "stable")
        .put("available", available)
        .put("applicationId", "com.navtasks.app.glass")
        .put("versionName", name)
        .put("versionCode", code)
        .put("minSdk", 31)
        .put("minSupportedVersionCode", floor)
        .put("apk", JSONObject()
            .put("url", "https://github.com/sergbokarev95-arch/NavTasks-Updates/releases/download/v$name/NavTasks_$name.apk")
            .put("sizeBytes", 35_000_000)
            .put("sha256", "a".repeat(64))
            .put("signerSha256", "b".repeat(64)))
        .put("releaseNotes", JSONObject().put("ru", "Изменения").put("en", "Changes"))

    @Test fun inactiveStableChannelDoesNotOfferRCOrShowError() {
        assertNull(navParseStableManifest("""{"schemaVersion":1,"channel":"stable","available":false}"""))
    }

    @Test fun strictStableMetadataRejectsBetaRedirectsAndInvalidSignature() {
        val invalid = listOf(
            raw(name = "0.4.142-rc1"),
            raw().put("channel", "beta"),
            raw().put("applicationId", "com.fake.app"),
            raw().put("minSupportedVersionCode", 171),
            raw().put("minSdk", 45),
            raw().put("apk", raw().getJSONObject("apk").put("url", "https://evil.example/apk")),
            raw().put("apk", raw().getJSONObject("apk").put("sha256", "invalid")),
            raw().put("releaseNotes", JSONObject().put("ru", "").put("en", "Test")),
        )
        for (json in invalid) assertTrue(runCatching { navParseStableManifest(json.toString()) }.isFailure)
    }

    @Test fun offerOnlyNewCompatibleNonDismissedStableRelease() {
        val info = requireNotNull(navParseStableManifest(raw().toString()))
        assertEquals(NavUpdateDecision.NEW,
            navDecideStableUpdate(167, 36, info, 0, false))
        assertEquals(NavUpdateDecision.DISMISSED,
            navDecideStableUpdate(167, 36, info, 170, false))
        assertEquals(NavUpdateDecision.NEW,
            navDecideStableUpdate(167, 36, info, 170, true))
        assertEquals(NavUpdateDecision.INCOMPATIBLE,
            navDecideStableUpdate(164, 36, info, 0, false))
        assertEquals(NavUpdateDecision.CURRENT_OR_OLD,
            navDecideStableUpdate(170, 36, info, 0, false))
        assertEquals(NavUpdateDecision.CURRENT_OR_OLD,
            navDecideStableUpdate(171, 36, info, 0, false))
    }

    @Test fun automaticChecksThrottledAndOfflineRetryNotLockedOut() {
        val t = 1_700_000_000_000L
        assertTrue(navUpdateCheckIsDue(t, 0, 0))
        assertEquals(false, navUpdateCheckIsDue(t + 60_000, 0, t))
        assertTrue(navUpdateCheckIsDue(t + 6 * 60_000, 0, t))
        assertEquals(false, navUpdateCheckIsDue(t + 60_000, t, 0))
        assertTrue(navUpdateCheckIsDue(t + 86_400_001L, t, t))
    }
}
