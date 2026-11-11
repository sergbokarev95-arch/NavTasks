package com.navtasks.app

import android.os.Build
import org.json.JSONObject

/** Isolated manual-only test channel. NEVER used by stable automatic checks. */
internal const val NAV_TEST_FEED =
    "https://raw.githubusercontent.com/sergbokarev95-arch/NavTasks-Updates/main/channels/test.json"

internal fun navParseTestFeed(
    raw: String, installedCode: Int, expectedPackage: String,
    deviceSdk: Int = Build.VERSION.SDK_INT,
): NavUpdateChecker.Candidate? {
    val obj = JSONObject(raw)
    if (obj.getInt("schemaVersion") != 1 || obj.getString("channel") != "test" ||
        !obj.getBoolean("available")) return null
    if (expectedPackage != "com.navtasks.app.glass" ||
        obj.getString("applicationId") != expectedPackage) return null
    val name = obj.getString("versionName")
    if (!Regex("(0|[1-9][0-9]*)\\.(0|[1-9][0-9]*)\\.(0|[1-9][0-9]*)-test1").matches(name))
        return null
    val code = obj.getInt("versionCode")
    val floor = obj.getInt("minSupportedVersionCode")
    if (!navUpdateSupportsDirectUpgrade(installedCode.toLong(), code.toLong(), floor.toLong()))
        return null
    if (obj.getInt("minSdk") > deviceSdk) return null
    val baseVersion = name.removeSuffix("-test1")
    val expectedUrl =
        "https://github.com/sergbokarev95-arch/NavTasks-Updates/releases/download/test-v${baseVersion}/NavTasks_${name}.apk"
    val apk = obj.getJSONObject("apk")
    if (apk.getString("url") != expectedUrl) return null
    val hash = apk.getString("sha256")
    val cert = apk.getString("signerSha256")
    val size = apk.getLong("sizeBytes")
    if (!Regex("[0-9a-fA-F]{64}").matches(hash) ||
        !Regex("[0-9a-fA-F]{64}").matches(cert) || !navUpdateValidSize(size)) return null
    val notes = obj.getJSONObject("releaseNotes")
    val ru = notes.optString("ru").trim()
    val en = notes.optString("en").trim()
    if (ru.isBlank() || en.isBlank() || ru.length > 4000 || en.length > 4000)
        return null
    return NavUpdateChecker.Candidate(name, code, floor, ru, en, expectedUrl,
        hash.lowercase(), size, cert.lowercase())
}
