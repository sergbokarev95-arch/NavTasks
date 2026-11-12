package com.navtasks.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URI
import java.net.URL
import javax.net.ssl.SSLException

/** Stable feed is pinned independently of the manual-only TEST channel. */
internal const val NAV_STABLE_FEED =
    "https://raw.githubusercontent.com/sergbokarev95-arch/NavTasks-Updates/main/channels/stable.json"

internal class NavStableHttpStatus(val status: Int) : IOException("HTTP $status")
internal class NavStableUnsafeRedirect : IOException("Unsafe stable feed redirect")

internal fun navStableFeedAllowedUrl(value: String): Boolean = runCatching {
    val url = URI(value)
    url.scheme == "https" &&
        url.host == "raw.githubusercontent.com" &&
        url.port == -1 && url.userInfo == null && url.fragment == null &&
        url.rawQuery == null &&
        url.path == "/sergbokarev95-arch/NavTasks-Updates/main/channels/stable.json"
}.getOrDefault(false)

internal fun navStableFeedRetryable(error: IOException): Boolean = when (error) {
    is NavStableUnsafeRedirect, is SSLException -> false
    is NavStableHttpStatus -> error.status == 429 || error.status in 500..599
    else -> true
}

/** Bounded, asynchronous weak-network retries. Never touches the TEST feed or APK installer. */
internal object NavStableFeedNetwork {
    private fun readOnce(): String {
        var address = NAV_STABLE_FEED
        repeat(4) { hop ->
            if (!navStableFeedAllowedUrl(address)) throw NavStableUnsafeRedirect()
            val connection = (URL(address).openConnection() as HttpURLConnection).apply {
                connectTimeout = 10_000
                readTimeout = 15_000
                instanceFollowRedirects = false
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Accept-Encoding", "identity")
                setRequestProperty("User-Agent", "NavTasks-Android-Update-Check")
            }
            try {
                when (val status = connection.responseCode) {
                    HttpURLConnection.HTTP_OK -> {
                        val content = connection.inputStream.bufferedReader(Charsets.UTF_8).use {
                            it.readText().take(32769)
                        }
                        if (content.length > 32768) throw IOException("Update feed too large")
                        return content
                    }
                    HttpURLConnection.HTTP_MOVED_PERM,
                    HttpURLConnection.HTTP_MOVED_TEMP,
                    HttpURLConnection.HTTP_SEE_OTHER, 307, 308 -> {
                        if (hop == 3) throw NavStableUnsafeRedirect()
                        val location = connection.getHeaderField("Location")
                            ?: throw NavStableUnsafeRedirect()
                        val next = URL(URL(address), location).toString()
                        if (!navStableFeedAllowedUrl(next)) throw NavStableUnsafeRedirect()
                        address = next
                    }
                    else -> throw NavStableHttpStatus(status)
                }
            } finally {
                connection.disconnect()
            }
        }
        throw NavStableUnsafeRedirect()
    }

    suspend fun read(): String = withContext(Dispatchers.IO) {
        for (attempt in 0..2) {
            try {
                return@withContext readOnce()
            } catch (error: IOException) {
                if (attempt == 2 || !navStableFeedRetryable(error)) throw error
                delay(750L * (attempt + 1))
            }
        }
        throw IOException("Stable feed retry limit reached")
    }
}
