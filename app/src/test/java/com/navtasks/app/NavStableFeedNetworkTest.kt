package com.navtasks.app

import java.io.IOException
import java.net.SocketTimeoutException
import javax.net.ssl.SSLException
import org.junit.Assert.*
import org.junit.Test

class NavStableFeedNetworkTest {
    @Test fun onlyExactPinnedStableFeedIsAllowed() {
        assertTrue(navStableFeedAllowedUrl(NAV_STABLE_FEED))
        for (url in listOf(
            "http://raw.githubusercontent.com/sergbokarev95-arch/NavTasks-Updates/main/channels/stable.json",
            "https://raw.githubusercontent.com/sergbokarev95-arch/NavTasks-Updates/main/channels/test.json",
            "https://raw.githubusercontent.com/sergbokarev95-arch/NavTasks-Updates/main/channels/stable.json?redirect=1",
            "https://raw.githubusercontent.com/sergbokarev95-arch/NavTasks-Updates/main/channels/stable.json#fragment",
            "https://raw.githubusercontent.com.evil.example/sergbokarev95-arch/NavTasks-Updates/main/channels/stable.json",
            "https://raw.githubusercontent.com:444/sergbokarev95-arch/NavTasks-Updates/main/channels/stable.json",
            "https://evil.example/stable.json"
        )) assertFalse(url, navStableFeedAllowedUrl(url))
    }

    @Test fun retryOnlyTransientNetworkErrors() {
        assertTrue(navStableFeedRetryable(SocketTimeoutException()))
        assertTrue(navStableFeedRetryable(IOException("Connection reset")))
        assertTrue(navStableFeedRetryable(NavStableHttpStatus(429)))
        assertTrue(navStableFeedRetryable(NavStableHttpStatus(503)))
        for (code in listOf(400, 401, 403, 404, 410)) {
            assertFalse(navStableFeedRetryable(NavStableHttpStatus(code)))
        }
        assertFalse(navStableFeedRetryable(NavStableUnsafeRedirect()))
        assertFalse(navStableFeedRetryable(SSLException("Untrusted TLS")))
    }

    @Test fun autoOfferPreferenceAndLaterStillGatePrompt() {
        assertFalse(navUpdateMayCheck(manual = false, autoOffers = false))
        assertTrue(navUpdateMayCheck(manual = true, autoOffers = false))
        assertTrue(navUpdateMayCheck(manual = false, autoOffers = true))
        assertTrue(navUpdateMayOffer(autoOffers = true, deferred = false))
        assertFalse(navUpdateMayOffer(autoOffers = true, deferred = true))
        assertFalse(navUpdateMayOffer(autoOffers = false, deferred = false))
    }
}
