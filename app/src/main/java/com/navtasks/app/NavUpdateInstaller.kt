package com.navtasks.app

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.security.MessageDigest

/**
 * GitHub's first-party asset hosts. The initial URL is ALSO pinned by
 * NavUpdateChecker.parseStableFeed to the exact public release asset.
 */
internal fun navUpdateAllowedAssetHost(url: String): Boolean = runCatching {
    val u = URI(url)
    u.scheme == "https" && u.userInfo == null && u.port == -1 &&
        u.host in setOf(
            "github.com",
            "release-assets.githubusercontent.com",
            "objects.githubusercontent.com",
        )
}.getOrDefault(false)

internal fun navUpdateSupportsDirectUpgrade(
    installedCode: Long, targetCode: Long, minimumSupportedCode: Long,
): Boolean = installedCode > 0 && targetCode > installedCode &&
    minimumSupportedCode > 0 && minimumSupportedCode <= installedCode

internal fun navUpdateValidSize(sizeBytes: Long): Boolean =
    sizeBytes in 1..(200L * 1024L * 1024L)

/**
 * A resumed response must identify the exact requested byte range of this
 * signed full APK. Never append HTTP 200 to an existing partial.
 */
internal enum class NavUpdateTransferMode { RESTART, APPEND }

internal fun navUpdateTransferMode(
    httpStatus: Int, offset: Long, totalSize: Long, contentRange: String?,
): NavUpdateTransferMode {
    if (httpStatus == HttpURLConnection.HTTP_OK) return NavUpdateTransferMode.RESTART
    if (httpStatus != HttpURLConnection.HTTP_PARTIAL || offset <= 0 ||
        !navUpdateValidSize(totalSize) || offset >= totalSize)
        throw IOException("Unexpected APK response")
    val m = Regex("bytes ([0-9]+)-([0-9]+)/([0-9]+)")
        .matchEntire(contentRange?.trim().orEmpty())
        ?: throw IOException("Invalid partial Content-Range")
    val from = m.groupValues[1].toLongOrNull()
    val end = m.groupValues[2].toLongOrNull()
    val size = m.groupValues[3].toLongOrNull()
    if (from != offset || size != totalSize || end == null ||
        end < offset || end >= totalSize)
        throw IOException("Mismatched partial Content-Range")
    return NavUpdateTransferMode.APPEND
}

internal fun navUpdateResumeIdentity(update: NavUpdateChecker.Candidate): String =
    "${update.versionCode}|${update.versionName}|${update.sizeBytes}|" +
        "${update.sha256.lowercase()}|${update.apkUrl}"

/** A private cache subdirectory is the ONLY FileProvider-exported location. */
internal class NavUpdatesFileProvider : FileProvider()

internal object NavUpdateInstaller {
    private const val MIME = "application/vnd.android.package-archive"
    private const val DIRECTORY = "navtasks-update"

    private fun folder(context: Context): File =
        File(context.applicationContext.cacheDir, DIRECTORY).apply {
            if (!isDirectory && !mkdirs()) throw IOException("Cannot create update cache")
        }

    private fun archiveFile(context: Context, update: NavUpdateChecker.Candidate): File =
        File(folder(context), "NavTasks_" + update.versionName + ".apk")

    private fun digestHex(bytes: ByteArray): String =
        bytes.joinToString("") { "%02x".format(it.toInt() and 0xff) }

    /**
     * One complete, signed APK with opt-in Range resume.
     *
     * A .part file is NEVER trusted as an APK: full SHA-256 + installed signer
     * are revalidated before handing the finished file to Android. Cache is
     * device-private and may be deleted by Android at any time. Cancel and
     * transient failures retain a bounded partial, while corrupt candidates
     * are discarded. No profile or business data is read or modified.
     */
    suspend fun download(
        context: Context,
        update: NavUpdateChecker.Candidate,
        onProgress: suspend (Int) -> Unit,
    ): File = withContext(Dispatchers.IO) {
        if (!navUpdateAllowedAssetHost(update.apkUrl) ||
            !navUpdateValidSize(update.sizeBytes) ||
            !navUpdateSupportsDirectUpgrade(
                installedCode(context), update.versionCode.toLong(),
                update.minSupportedVersionCode.toLong(),
            )
        ) throw IOException("Incompatible or invalid update")

        val apk = archiveFile(context, update)
        val partial = File(apk.parentFile, apk.name + ".part")
        val identity = File(apk.parentFile, apk.name + ".part.key")
        val expected = navUpdateResumeIdentity(update)
        // A ready APK from a previous attempt is reusable ONLY after revalidation.
        if (apk.isFile) {
            try {
                verifyArchive(context, apk, update)
                withContext(Dispatchers.Main) { onProgress(100) }
                return@withContext apk
            } catch (_: Exception) {
                apk.delete()
            }
        }
        val stale = !partial.isFile || !identity.isFile ||
            identity.length() > 2048L ||
            runCatching { identity.readText() }.getOrNull() != expected ||
            partial.length() > update.sizeBytes
        if (stale) {
            partial.delete()
            identity.delete()
        }
        if (!identity.isFile) identity.writeText(expected)
        var verified = false
        try {
            for (attempt in 0..2) {
                currentCoroutineContext().ensureActive()
                val existing = if (partial.isFile) partial.length() else 0L
                withContext(Dispatchers.Main) {
                    onProgress(((existing * 100L) / update.sizeBytes).toInt())
                }
                if (existing == update.sizeBytes) break
                var current = update.apkUrl
                var active: HttpURLConnection? = null
                try {
                    for (redirect in 0..5) {
                        if (!navUpdateAllowedAssetHost(current))
                            throw IOException("Forbidden APK redirect")
                        val opened = URL(current).openConnection() as HttpURLConnection
                        active = opened
                        opened.connectTimeout = 15_000
                        opened.readTimeout = 30_000
                        opened.instanceFollowRedirects = false
                        opened.setRequestProperty("Accept-Encoding", "identity")
                        opened.setRequestProperty("User-Agent", "NavTasks-Android-Updater")
                        if (existing > 0) {
                            opened.setRequestProperty("Range", "bytes=${existing}-")
                        }
                        when (val status = opened.responseCode) {
                            HttpURLConnection.HTTP_MOVED_PERM,
                            HttpURLConnection.HTTP_MOVED_TEMP,
                            HttpURLConnection.HTTP_SEE_OTHER, 307, 308 -> {
                                val location = opened.getHeaderField("Location")
                                    ?: throw IOException("APK redirect missing target")
                                current = URL(URL(current), location).toString()
                                opened.disconnect()
                                active = null
                                if (redirect == 5) throw IOException("APK redirect limit")
                            }
                            HttpURLConnection.HTTP_OK,
                            HttpURLConnection.HTTP_PARTIAL -> break
                            else -> throw IOException("APK HTTP $status")
                        }
                    }
                    val connection = active ?: throw IOException("No APK response")
                    if (!connection.contentEncoding.isNullOrBlank() &&
                        connection.contentEncoding != "identity")
                        throw IOException("Compressed APK response refused")
                    val mode = navUpdateTransferMode(
                        connection.responseCode, existing, update.sizeBytes,
                        connection.getHeaderField("Content-Range"),
                    )
                    if (mode == NavUpdateTransferMode.RESTART && existing > 0) {
                        // Server ignored Range. Reset, never append a full response.
                        if (!partial.delete()) throw IOException("Cannot reset partial APK")
                        withContext(Dispatchers.Main) { onProgress(0) }
                    }
                    val append = mode == NavUpdateTransferMode.APPEND
                    var bytes = if (append) existing else 0L
                    java.io.FileOutputStream(partial, append).buffered().use { output ->
                        connection.inputStream.use { input ->
                            val chunk = ByteArray(64 * 1024)
                            var lastPercent = -1
                            while (true) {
                                currentCoroutineContext().ensureActive()
                                val n = input.read(chunk)
                                if (n < 0) break
                                bytes += n
                                if (bytes > update.sizeBytes)
                                    throw IOException("APK exceeds expected size")
                                output.write(chunk, 0, n)
                                val percent = ((bytes * 100L) / update.sizeBytes).toInt()
                                if (percent != lastPercent) {
                                    lastPercent = percent
                                    withContext(Dispatchers.Main) { onProgress(percent) }
                                }
                            }
                        }
                    }
                    if (bytes < update.sizeBytes)
                        throw IOException("APK transfer interrupted")
                } catch (e: IOException) {
                    // Keep a bounded prefix for the next attempt or user retry.
                    if (partial.length() > update.sizeBytes) {
                        partial.delete()
                        identity.delete()
                    }
                    if (attempt == 2) throw e
                    kotlinx.coroutines.delay(1_000L * (attempt + 1))
                } finally {
                    active?.disconnect()
                }
            }
            if (!partial.isFile || partial.length() != update.sizeBytes)
                throw IOException("Incomplete APK after retries")
            if (!partial.renameTo(apk)) throw IOException("Cannot finalize APK cache")
            try {
                verifyArchive(context, apk, update)
            } catch (e: Exception) {
                // Integrity/signature errors MUST NOT retain the corrupt candidate.
                apk.delete()
                partial.delete()
                identity.delete()
                throw e
            }
            verified = true
            withContext(Dispatchers.Main) { onProgress(100) }
            apk
        } catch (cancelled: CancellationException) {
            // Safe resumable prefix remains in private cache until next explicit tap.
            throw cancelled
        } finally {
            if (verified) {
                partial.delete()
                identity.delete()
            } else {
                apk.delete()
            }
        }
    }

    private fun installedCode(context: Context): Long =
        context.packageManager.getPackageInfo(context.packageName, 0).longVersionCode

    /**
     * SHA from the feed is NOT proof of developer identity. Independently
     * compare the downloaded APK signer with the installed NavTasks signer.
     * This project deliberately retains its single original signing key.
     */
    @Suppress("DEPRECATION")
    fun verifyArchive(
        context: Context, apk: File, update: NavUpdateChecker.Candidate,
    ) {
        if (apk.canonicalFile.parentFile != folder(context).canonicalFile ||
            !apk.isFile || apk.length() != update.sizeBytes ||
            !navUpdateValidSize(update.sizeBytes)) throw IOException("Invalid APK path or size")
        // Recheck the file immediately before INSTALL: another process or
        // a stale cache must not replace its verified bytes after download.
        val digest = MessageDigest.getInstance("SHA-256")
        apk.inputStream().use { input ->
            val buffer = ByteArray(64 * 1024)
            while (true) {
                val n = input.read(buffer)
                if (n < 0) break
                digest.update(buffer, 0, n)
            }
        }
        if (!digestHex(digest.digest()).equals(update.sha256, ignoreCase = true))
            throw IOException("Cached APK hash mismatch")
        val pm = context.packageManager
        val archive = pm.getPackageArchiveInfo(
            apk.absolutePath, PackageManager.GET_SIGNING_CERTIFICATES,
        ) ?: throw IOException("Android could not parse the APK")
        val installed = pm.getPackageInfo(
            context.packageName, PackageManager.GET_SIGNING_CERTIFICATES,
        )
        if (archive.packageName != context.packageName ||
            archive.versionName != update.versionName ||
            archive.longVersionCode != update.versionCode.toLong() ||
            !navUpdateSupportsDirectUpgrade(
                installed.longVersionCode, archive.longVersionCode,
                update.minSupportedVersionCode.toLong(),
            )
        ) throw IOException("APK package or version mismatch")
        val archiveApp = archive.applicationInfo ?: throw IOException("APK missing app info")
        if (archiveApp.minSdkVersion > Build.VERSION.SDK_INT ||
            archiveApp.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
        ) throw IOException("Unsupported or debug APK")
        val from = installed.signingInfo?.apkContentsSigners
            ?: throw IOException("Installed signing certificate unavailable")
        val to = archive.signingInfo?.apkContentsSigners
            ?: throw IOException("Downloaded signing certificate unavailable")
        if (from.size != 1 || to.size != 1)
            throw IOException("Unexpected signer count")
        val installedDigest = MessageDigest.getInstance("SHA-256").digest(from[0].toByteArray())
        val archiveDigest = MessageDigest.getInstance("SHA-256").digest(to[0].toByteArray())
        if (!MessageDigest.isEqual(installedDigest, archiveDigest) ||
            !digestHex(archiveDigest).equals(update.signerSha256, ignoreCase = true)
        ) throw IOException("APK signing certificate mismatch")
    }

    suspend fun install(context: Context, apk: File, update: NavUpdateChecker.Candidate) {
        withContext(Dispatchers.IO) { verifyArchive(context, apk, update) }
        if (!context.packageManager.canRequestPackageInstalls())
            throw SecurityException("UNKNOWN_SOURCES_PERMISSION_REQUIRED")
        val uri = FileProvider.getUriForFile(
            context, context.packageName + ".updates.fileprovider", apk,
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, MIME)
            clipData = ClipData.newRawUri("NavTasks update", uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            if (context !is android.app.Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun askForInstallPermission(context: Context) {
        val uri = Uri.fromParts("package", context.packageName, null)
        val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, uri)
        if (context !is android.app.Activity) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
