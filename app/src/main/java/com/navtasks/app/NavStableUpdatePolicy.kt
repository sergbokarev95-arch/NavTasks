package com.navtasks.app

import org.json.JSONObject

/** One approved *stable* APK. Never interpret draft, beta or recovery metadata here. */
internal data class NavStableUpdate(
    val versionName: String,
    val versionCode: Int,
    val minSupportedVersionCode: Int,
    val minSdk: Int,
    val url: String,
    val sizeBytes: Long,
    val sha256: String,
    val signerSha256: String,
    val notesRu: String,
    val notesEn: String,
)

internal const val NAV_STABLE_FEED_URL =
    "https://raw.githubusercontent.com/sergbokarev95-arch/NavTasks-Updates/main/channels/stable.json"
private const val NAV_PUBLIC_REPO = "sergbokarev95-arch/NavTasks-Updates"
private val NAV_STABLE_VERSION_REGEX = Regex("(0|[1-9][0-9]*)\\.(0|[1-9][0-9]*)\\.(0|[1-9][0-9]*)")
private val NAV_SHA_REGEX = Regex("[0-9a-fA-F]{64}")

/** A validated, intentionally inactive feed returns null without a warning. */
internal fun navParseStableManifest(json: String): NavStableUpdate? {
    val obj = JSONObject(json)
    require(obj.getInt("schemaVersion") == 1)
    require(obj.getString("channel") == "stable")
    val available = obj.get("available")
    require(available is Boolean)
    if (!available) return null

    val version = obj.getString("versionName")
    require(NAV_STABLE_VERSION_REGEX.matches(version)) { "Not a stable release" }
    val code = obj.getLong("versionCode")
    val floor = obj.getLong("minSupportedVersionCode")
    val sdk = obj.getInt("minSdk")
    require(code in 1..Int.MAX_VALUE.toLong())
    require(floor in 1..code)
    require(sdk in 31..35) { "Unsupported minimum Android SDK" }
    require(obj.getString("applicationId") == "com.navtasks.app.glass")
    val apk = obj.getJSONObject("apk")
    val expectedUrl =
        "https://github.com/$NAV_PUBLIC_REPO/releases/download/v$version/NavTasks_$version.apk"
    val url = apk.getString("url")
    require(url == expectedUrl) { "APK URL is not the official stable release asset" }
    val size = apk.getLong("sizeBytes")
    require(size in 1..(250L * 1024L * 1024L))
    val hash = apk.getString("sha256")
    val signer = apk.getString("signerSha256")
    require(NAV_SHA_REGEX.matches(hash) && NAV_SHA_REGEX.matches(signer))
    val notes = obj.getJSONObject("releaseNotes")
    val ru = notes.getString("ru").trim()
    val en = notes.getString("en").trim()
    require(ru.length in 1..4000 && en.length in 1..4000)
    return NavStableUpdate(
        versionName = version, versionCode = code.toInt(),
        minSupportedVersionCode = floor.toInt(), minSdk = sdk, url = url,
        sizeBytes = size, sha256 = hash.lowercase(), signerSha256 = signer.lowercase(),
        notesRu = ru, notesEn = en,
    )
}

internal enum class NavUpdateDecision { NEW, CURRENT_OR_OLD, INCOMPATIBLE, DISMISSED }

internal fun navDecideStableUpdate(
    versionCode: Int,
    sdk: Int,
    update: NavStableUpdate,
    dismissedCode: Int,
    manual: Boolean,
): NavUpdateDecision = when {
    update.versionCode <= versionCode -> NavUpdateDecision.CURRENT_OR_OLD
    update.minSupportedVersionCode > versionCode || update.minSdk > sdk ->
        NavUpdateDecision.INCOMPATIBLE
    !manual && dismissedCode == update.versionCode -> NavUpdateDecision.DISMISSED
    else -> NavUpdateDecision.NEW
}

/** Pure policy used for a once-a-day non-blocking automatic check. */
internal fun navUpdateCheckIsDue(nowMs: Long, lastSuccessfulMs: Long, lastAttemptMs: Long): Boolean {
    val day = 24L * 60L * 60L * 1000L
    val retry = 5L * 60L * 1000L
    return (lastSuccessfulMs <= 0 || nowMs - lastSuccessfulMs >= day || nowMs < lastSuccessfulMs) &&
        (lastAttemptMs <= 0 || nowMs - lastAttemptMs >= retry || nowMs < lastAttemptMs)
}
