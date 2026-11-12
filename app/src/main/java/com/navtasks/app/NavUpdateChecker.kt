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
            val body = NavStableFeedNetwork.read()
            val result = parseStableFeed(body, code.toInt(), appContext.packageName)
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

    /** QA-only automatic test feed; independent 24h successful-check throttle. */
    suspend fun checkTestAutomatically(context: Context): Boolean {
        if (checking) return false
        val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()
        val last = prefs.getLong("last_test_auto_success", 0L)
        if (now >= last && now - last < CHECK_INTERVAL_MS) return false
        val found = checkTest(context, automatic = true)
        if (lastTestCheckSucceeded) {
            prefs.edit().putLong("last_test_auto_success", System.currentTimeMillis()).apply()
        }
        return found
    }

    private var lastTestCheckSucceeded = false

    /** Manual TEST checks always bypass the startup throttle. */
    suspend fun checkTest(context: Context, automatic: Boolean = false): Boolean {
        if (checking) return false
        checking = true
        if (!automatic) lastManualMessage = null
        lastTestCheckSucceeded = false
        val appContext = context.applicationContext
        return try {
            val installed = appContext.packageManager
                .getPackageInfo(appContext.packageName, 0).longVersionCode.toInt()
            // Retries are test-only; stable feed checks and throttling remain untouched.
            val body = NavTestFeedNetwork.read()
            testCandidate = navParseTestFeed(body, installed, appContext.packageName)
            lastTestCheckSucceeded = true
            if (!automatic) lastManualMessage =
                if (testCandidate == null) "test_none" else "test_available"
            testCandidate != null
        } catch (error: Exception) {
            testCandidate = null
            if (!automatic) lastManualMessage = navTestFeedErrorCode(error)
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

    /** Per-version Later for automatic TEST QA, independent from stable Later. */
    fun deferTest(context: Context, code: Int) {
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putInt("dismissed_test_code", code).apply()
    }

    fun shouldOfferTestAutomatically(context: Context, update: Candidate,
                                     autoOffers: Boolean): Boolean =
        navUpdateMayOffer(autoOffers, context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getInt("dismissed_test_code", -1) == update.versionCode)

    fun shouldOfferAutomatically(context: Context, update: Candidate,
                                 autoOffers: Boolean): Boolean =
        navUpdateMayOffer(autoOffers, context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getInt("dismissed_code", -1) == update.versionCode)

    fun lastSuccess(context: Context): Long =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getLong("last_success", 0L)
}
