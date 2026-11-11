package com.navtasks.app

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant

/** Manual checks bypass the disabled automatic offer preference. */
internal fun navUpdateMayCheck(manual: Boolean, autoOffers: Boolean) = manual || autoOffers
internal fun navUpdateMayOffer(autoOffers: Boolean, deferred: Boolean) = autoOffers && !deferred

/**
 * Stage 3A: read-only stable feed discovery. Does not download or install APKs.
 * Network errors are deliberately silent on automatic checks; offline remains normal.
 */
internal object NavUpdateChecker {
    private const val FEED =
        "https://raw.githubusercontent.com/sergbokarev95-arch/NavTasks-Updates/main/channels/stable.json"
    private const val PREFS = "navtasks-update-check"
    private const val CHECK_INTERVAL_MS = 24L * 60L * 60L * 1000L

    data class Candidate(
        val versionName: String,
        val versionCode: Int,
        val minSupportedVersionCode: Int,
        val notesRu: String,
        val notesEn: String,
        val apkUrl: String,
        val sha256: String,
        val sizeBytes: Long,
        val signerSha256: String,
    )

    var candidate by mutableStateOf<Candidate?>(null)
        private set
    // Never participates in automatic update offers.
    var testCandidate by mutableStateOf<Candidate?>(null)
        private set
    var checking by mutableStateOf(false)
        private set
    var lastCheckedAt by mutableStateOf(0L)
        private set
    var lastManualMessage by mutableStateOf<String?>(null)
        private set

    fun parseStableFeed(raw: String, installedCode: Int, expectedPackage: String): Candidate? {
        val obj = JSONObject(raw)
        if (obj.getInt("schemaVersion") != 1 || obj.getString("channel") != "stable") return null
        if (!obj.getBoolean("available")) return null
        if (obj.getString("applicationId") != expectedPackage ||
            expectedPackage != "com.navtasks.app.glass") return null
        val name = obj.getString("versionName")
        if (!Regex("(0|[1-9][0-9]*)\\.(0|[1-9][0-9]*)\\.(0|[1-9][0-9]*)").matches(name)) return null
        val code = obj.getInt("versionCode")
        val floor = obj.getInt("minSupportedVersionCode")
        if (code <= installedCode || floor > installedCode || floor <= 0 || floor > code) return null
        if (obj.getInt("minSdk") > android.os.Build.VERSION.SDK_INT) return null
        val apk = obj.getJSONObject("apk")
        val expectedUrl =
            "https://github.com/sergbokarev95-arch/NavTasks-Updates/releases/download/v$name/NavTasks_$name.apk"
        if (apk.getString("url") != expectedUrl) return null
        val hash = apk.getString("sha256")
        val signer = apk.getString("signerSha256")
        if (!Regex("[0-9a-fA-F]{64}").matches(hash) ||
            !Regex("[0-9a-fA-F]{64}").matches(signer) ||
            apk.getLong("sizeBytes") <= 0) return null
        val notes = obj.getJSONObject("releaseNotes")
        return Candidate(name, code, floor, notes.optString("ru").take(4000),
            notes.optString("en").take(4000), expectedUrl, hash.lowercase(),
            apk.getLong("sizeBytes"), signer.lowercase())
    }

    suspend fun check(context: Context, manual: Boolean = false) {
        val appContext = context.applicationContext
        if (!navUpdateMayCheck(manual, NavSettings(appContext).automaticUpdateOffers)) return
        if (checking) return
        val prefs = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()
        val lastAttempt = prefs.getLong("last_attempt", 0L)
        if (!manual && now >= lastAttempt && now - lastAttempt < CHECK_INTERVAL_MS) return
        checking = true
        if (manual) lastManualMessage = null
        try {
            val code = appContext.packageManager.getPackageInfo(appContext.packageName, 0).longVersionCode
            val result = withContext(Dispatchers.IO) {
                val connection = (URL(FEED).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 3500
                    readTimeout = 3500
                    instanceFollowRedirects = false
                    setRequestProperty("Accept", "application/json")
                    setRequestProperty("User-Agent", "NavTasks-Android-Update-Check")
                }
                try {
                    if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                        throw java.io.IOException("Update channel unavailable")
                    }
                    val body = connection.inputStream.bufferedReader(Charsets.UTF_8).use {
                        it.readText().take(32769)
                    }
                    if (body.length > 32768) throw java.io.IOException("Update feed too large")
                    parseStableFeed(body, code.toInt(), appContext.packageName)
                } finally {
                    connection.disconnect()
                }
            }
            candidate = result
            lastCheckedAt = System.currentTimeMillis()
            prefs.edit().putLong("last_success", lastCheckedAt)
                .putLong("last_attempt", lastCheckedAt).apply()
            if (manual) lastManualMessage = if (result == null) "up_to_date" else "available"
        } catch (_: Exception) {
            // Never show an error at startup, even if the ship has no internet.
            if (manual) lastManualMessage = "offline"
        } finally {
            // Failed offline checks must NOT suppress the next online check.
            checking = false
        }
    }

    /** Explicit manual-only check; never modifies the stable candidate or last-success throttle. */
    suspend fun checkTest(context: Context): Boolean {
        if (checking) return false
        checking = true
        lastManualMessage = null
        val appContext = context.applicationContext
        return try {
            val installed = appContext.packageManager
                .getPackageInfo(appContext.packageName, 0).longVersionCode.toInt()
            val body = withContext(Dispatchers.IO) {
                val connection = (URL(NAV_TEST_FEED).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 3500
                    readTimeout = 3500
                    instanceFollowRedirects = false
                    setRequestProperty("Accept", "application/json")
                    setRequestProperty("User-Agent", "NavTasks-Android-Test-Update-Check")
                }
                try {
                    if (connection.responseCode != HttpURLConnection.HTTP_OK)
                        throw java.io.IOException("Test feed unavailable")
                    val data = connection.inputStream.bufferedReader(Charsets.UTF_8)
                        .use { it.readText().take(32769) }
                    if (data.length > 32768) throw java.io.IOException("Test feed too large")
                    data
                } finally {
                    connection.disconnect()
                }
            }
            testCandidate = navParseTestFeed(body, installed, appContext.packageName)
            lastManualMessage = if (testCandidate == null) "test_none" else "test_available"
            testCandidate != null
        } catch (_: Exception) {
            testCandidate = null
            lastManualMessage = "test_offline"
            false
        } finally {
            checking = false
        }
    }

    /** Suppress the exact release chosen by Later, not a future update. */
    fun defer(context: Context, code: Int) {
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putInt("dismissed_code", code).apply()
    }

    fun shouldOfferAutomatically(context: Context, update: Candidate,
                                 autoOffers: Boolean): Boolean =
        navUpdateMayOffer(autoOffers, context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getInt("dismissed_code", -1) == update.versionCode)

    fun lastSuccess(context: Context): Long =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getLong("last_success", 0L)
}
