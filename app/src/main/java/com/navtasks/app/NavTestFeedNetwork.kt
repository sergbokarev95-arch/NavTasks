package com.navtasks.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONException
import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.net.URI
import java.net.URL
import javax.net.ssl.SSLException

/** TEST feed only; the installed application's stable checks are deliberately untouched. */
internal class NavTestHttpStatus(val status: Int) : IOException("HTTP $status")
internal class NavTestUnsafeRedirect : IOException("Unsafe test feed redirect")

/** Reject any redirect that leaves the single hard-pinned read-only TEST feed. */
internal fun navTestFeedAllowedUrl(url: String): Boolean = runCatching {
    val u = URI(url)
    u.scheme.equals("https", ignoreCase = true) &&
        u.host.equals("raw.githubusercontent.com", ignoreCase = true) &&
        u.port == -1 && u.userInfo == null && u.fragment == null &&
        u.path == "/sergbokarev95-arch/NavTasks-Updates/main/channels/test.json"
}.getOrDefault(false)

/** Safe, localized error classification; do not display exception details or private URLs. */
internal fun navTestFeedErrorCode(error: Exception): String = when (error) {
    is SocketTimeoutException -> "test_timeout"
    is UnknownHostException -> "test_dns"
    is SSLException -> "test_tls"
    is NavTestHttpStatus -> "test_http_${error.status}"
    is NavTestUnsafeRedirect -> "test_redirect"
    is JSONException -> "test_bad_feed"
    is SecurityException -> "test_security"
    is IOException -> "test_network"
    else -> "test_error"
}

internal fun navTestFeedRetryable(error: IOException): Boolean =
    when (error) {
        is NavTestUnsafeRedirect, is SSLException -> false
        is NavTestHttpStatus -> error.status == 429 || error.status in 500..599
        else -> true
}

/**
 * Bounded retry on weak maritime networks. Response stays tiny (<=32 KiB);
 * no data-store writes, no startup polls, and never switches to stable.
 */
internal object NavTestFeedNetwork {
    private fun readOnce(): String {
        var url = NAV_TEST_FEED
        repeat(4) { redirect ->
            if (!navTestFeedAllowedUrl(url)) throw NavTestUnsafeRedirect()
            val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = 12_000
                readTimeout = 20_000
                instanceFollowRedirects = false
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", "NavTasks-Android-Test-Update-Check")
                setRequestProperty("Accept-Encoding", "identity")
            }
            try {
                when (val status = connection.responseCode) {
                    HttpURLConnection.HTTP_OK -> {
                        val content = connection.inputStream.bufferedReader(Charsets.UTF_8).use {
                            it.readText().take(32769)
                        }
                        if (content.length > 32768) throw IOException("Response too large")
                        return content
                    }
                    HttpURLConnection.HTTP_MOVED_PERM,
                    HttpURLConnection.HTTP_MOVED_TEMP,
                    HttpURLConnection.HTTP_SEE_OTHER, 307, 308 -> {
                        if (redirect == 3) throw NavTestUnsafeRedirect()
                        val location = connection.getHeaderField("Location")
                            ?: throw NavTestUnsafeRedirect()
                        val next = URL(URL(url), location).toString()
                        if (!navTestFeedAllowedUrl(next)) throw NavTestUnsafeRedirect()
                        url = next
                    }
                    else -> throw NavTestHttpStatus(status)
                }
            } finally {
                connection.disconnect()
            }
        }
        throw NavTestUnsafeRedirect()
    }

    suspend fun read(): String = withContext(Dispatchers.IO) {
        for (attempt in 0..2) {
            try {
                return@withContext readOnce()
            } catch (error: IOException) {
                if (attempt == 2 || !navTestFeedRetryable(error)) throw error
                delay(750L * (attempt + 1))
            }
        }
        throw IOException("Test feed retry limit reached")
    }
}
