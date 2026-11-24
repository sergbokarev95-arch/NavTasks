package com.navtasks.app

import org.junit.Assert.*
import org.junit.Test
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException
import org.json.JSONException

class NavTestFeedNetworkPolicyTest {
    @Test fun readOnlyTestFeedRedirectsCannotEscapePinnedPublicPath() {
        assertTrue(navTestFeedAllowedUrl(NAV_TEST_FEED))
        assertFalse(navTestFeedAllowedUrl(NAV_TEST_FEED.replace("https:", "http:")))
        assertFalse(navTestFeedAllowedUrl(NAV_TEST_FEED.replace("raw.githubusercontent.com",
            "raw.githubusercontent.com.attacker.example")))
        assertFalse(navTestFeedAllowedUrl("https://github.com/sergbokarev95-arch/NavTasks-Updates/main/channels/test.json"))
        assertFalse(navTestFeedAllowedUrl(NAV_TEST_FEED.replace("test.json", "stable.json")))
        assertFalse(navTestFeedAllowedUrl(NAV_TEST_FEED + "#fragment"))
        assertFalse(navTestFeedAllowedUrl("https://evil@raw.githubusercontent.com/sergbokarev95-arch/NavTasks-Updates/main/channels/test.json"))
        assertFalse(navTestFeedAllowedUrl("https://raw.githubusercontent.com:8443/sergbokarev95-arch/NavTasks-Updates/main/channels/test.json"))
    }

    @Test fun transientNetworkFailuresRetryButTamperingAndClientErrorsDoNot() {
        assertTrue(navTestFeedRetryable(SocketTimeoutException()))
        assertTrue(navTestFeedRetryable(UnknownHostException()))
        assertTrue(navTestFeedRetryable(NavTestHttpStatus(429)))
        assertTrue(navTestFeedRetryable(NavTestHttpStatus(503)))
        assertFalse(navTestFeedRetryable(NavTestHttpStatus(404)))
        assertFalse(navTestFeedRetryable(NavTestHttpStatus(403)))
        assertFalse(navTestFeedRetryable(NavTestUnsafeRedirect()))
        assertFalse(navTestFeedRetryable(SSLException("invalid cert")))
    }

    @Test fun failureLabelsDistinguishTimeoutServerAndBadFeed() {
        assertEquals("test_timeout", navTestFeedErrorCode(SocketTimeoutException()))
        assertEquals("test_dns", navTestFeedErrorCode(UnknownHostException()))
        assertEquals("test_tls", navTestFeedErrorCode(SSLException("bad TLS")))
        assertEquals("test_http_429", navTestFeedErrorCode(NavTestHttpStatus(429)))
        assertEquals("test_http_403", navTestFeedErrorCode(NavTestHttpStatus(403)))
        assertEquals("test_redirect", navTestFeedErrorCode(NavTestUnsafeRedirect()))
        assertEquals("test_bad_feed", navTestFeedErrorCode(JSONException("bad")))
        assertEquals("test_network", navTestFeedErrorCode(IOException("disconnect")))
    }

    @Test fun boundedTestTransportDoesNotModifyStablePipeline() {
        val dir = "src/main/java/com/navtasks/app/"
        val checker = java.io.File(dir + "NavUpdateChecker.kt").readText()
        val transport = java.io.File(dir + "NavTestFeedNetwork.kt").readText()
        val settings = java.io.File(dir + "PlanAndTasksUi.kt").readText()
        val app = java.io.File("build.gradle.kts").readText()
        assertTrue(checker.contains("val body = NavTestFeedNetwork.read()"))
        assertTrue(checker.contains("val body = NavStableFeedNetwork.read()"))
        assertTrue(java.io.File(dir + "NavStableFeedNetwork.kt").readText().contains("NAV_STABLE_FEED"))
        assertTrue(checker.contains("parseStableFeed(body, code.toInt(), appContext.packageName)"))
        assertTrue(checker.contains("lastManualMessage = navTestFeedErrorCode(error)"))
        assertTrue(transport.contains("readTimeout = 20_000"))
        assertTrue(transport.contains("connectTimeout = 12_000"))
        assertTrue(transport.contains("for (attempt in 0..2)"))
        assertTrue(settings.contains("\"test_timeout\""))
        assertTrue(settings.contains("\"test_bad_feed\""))
        assertTrue(app.contains("versionCode = 284"))
        assertFalse(app.contains("versionNameSuffix = \"-rc1\""))
    }
}
