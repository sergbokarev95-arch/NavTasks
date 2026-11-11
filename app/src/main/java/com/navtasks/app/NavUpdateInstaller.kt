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
     * Download one complete APK. A failed, canceled or tampered transfer never
     * leaves a trusted "ready" file. No profile/store migration takes place.
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
        partial.delete()
        apk.delete()
        var ok = false
        try {
            val sha = MessageDigest.getInstance("SHA-256")
            var bytes = 0L
            var lastPercent = -1
            var current = update.apkUrl
            var connection: HttpURLConnection? = null
            try {
                for (redirect in 0..5) {
                    if (!navUpdateAllowedAssetHost(current)) throw IOException("Forbidden redirect")
                    val opened = URL(current).openConnection() as HttpURLConnection
                    connection = opened
                    opened.connectTimeout = 12_000
                    opened.readTimeout = 15_000
                    opened.instanceFollowRedirects = false
                    opened.setRequestProperty("User-Agent", "NavTasks-Android-Updater")
                    opened.setRequestProperty("Accept-Encoding", "identity")
                    when (opened.responseCode) {
                        HttpURLConnection.HTTP_MOVED_PERM,
                        HttpURLConnection.HTTP_MOVED_TEMP,
                        HttpURLConnection.HTTP_SEE_OTHER,
                        307, 308 -> {
                            val location = opened.getHeaderField("Location")
                                ?: throw IOException("Redirect without location")
                            current = URL(URL(current), location).toString()
                            opened.disconnect()
                            connection = null
                            if (redirect == 5) throw IOException("Too many redirects")
                        }
                        HttpURLConnection.HTTP_OK -> break
                        else -> throw IOException("APK download HTTP " + opened.responseCode)
                    }
                }
                val active = connection ?: throw IOException("Missing APK response")
                if (active.responseCode != HttpURLConnection.HTTP_OK)
                    throw IOException("APK response not successful")
                partial.outputStream().buffered().use { output ->
                    active.inputStream.use { input ->
                        val chunk = ByteArray(64 * 1024)
                        while (true) {
                            currentCoroutineContext().ensureActive()
                            val len = input.read(chunk)
                            if (len < 0) break
                            bytes += len
                            if (bytes > update.sizeBytes ||
                                bytes > 200L * 1024L * 1024L)
                                throw IOException("APK larger than expected")
                            output.write(chunk, 0, len)
                            sha.update(chunk, 0, len)
                            val percent = ((bytes * 100L) / update.sizeBytes).toInt()
                            if (percent != lastPercent) {
                                lastPercent = percent
                                withContext(Dispatchers.Main) { onProgress(percent) }
                            }
                        }
                    }
                }
            } finally {
                connection?.disconnect()
            }
            if (bytes != update.sizeBytes ||
                !digestHex(sha.digest()).equals(update.sha256, ignoreCase = true)
            ) throw IOException("APK integrity check failed")
            if (!partial.renameTo(apk)) throw IOException("Cannot finalize APK cache")
            verifyArchive(context, apk, update)
            ok = true
            withContext(Dispatchers.Main) { onProgress(100) }
            apk
        } catch (cancelled: CancellationException) {
            throw cancelled
        } finally {
            partial.delete()
            if (!ok) apk.delete()
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
