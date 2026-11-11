package com.navtasks.app

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.Locale

internal enum class NavUpdateState {
    IDLE, CHECKING, NO_UPDATE, AVAILABLE, INCOMPATIBLE,
    OFFLINE, ERROR, DOWNLOADING, AWAITING_INSTALL_PERMISSION, INSTALL_READY,
}

internal class NavUpdateController(
    private val context: Context,
    private val scope: CoroutineScope,
) {
    private val prefs = context.getSharedPreferences("navtasks-update-checks", Context.MODE_PRIVATE)
    private var checkJob: Job? = null
    private var downloadJob: Job? = null
    private var lastAttemptMs = 0L
    private var downloadedFile: File? = null
    private var waitingForInstallerPermission = false

    var state by mutableStateOf(NavUpdateState.IDLE)
        private set
    var latest by mutableStateOf<NavStableUpdate?>(null)
        private set
    var showOffer by mutableStateOf(false)
        private set
    var progressPercent by mutableIntStateOf(0)
        private set
    var lastSuccessfulCheckMs by mutableLongStateOf(prefs.getLong("last_successful_check", 0))
        private set

    val currentVersionName: String get() = runCatching {
        @Suppress("DEPRECATION")
        context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty()
    }.getOrDefault("")

    val currentVersionCode: Int get() = runCatching {
        @Suppress("DEPRECATION")
        context.packageManager.getPackageInfo(context.packageName, 0).longVersionCode.toInt()
    }.getOrDefault(0)

    private fun networkAvailable(): Boolean {
        val cm = context.getSystemService(ConnectivityManager::class.java) ?: return false
        val active = cm.activeNetwork ?: return false
        return cm.getNetworkCapabilities(active)?.hasCapability(
            NetworkCapabilities.NET_CAPABILITY_INTERNET
        ) == true
    }

    fun checkAutomatically() = check(manual = false)
    fun checkManually() = check(manual = true)

    private fun check(manual: Boolean) {
        if (checkJob?.isActive == true || downloadJob?.isActive == true) return
        val now = System.currentTimeMillis()
        if (!manual && !navUpdateCheckIsDue(now, lastSuccessfulCheckMs, lastAttemptMs)) return
        if (!networkAvailable()) {
            if (manual) state = NavUpdateState.OFFLINE
            return // silent while offline; don't consume the 24h interval
        }
        lastAttemptMs = now
        checkJob = scope.launch {
            if (manual) state = NavUpdateState.CHECKING
            try {
                val raw = withContext(Dispatchers.IO) { navReadSmallFeed() }
                val candidate = navParseStableManifest(raw)
                lastSuccessfulCheckMs = System.currentTimeMillis()
                prefs.edit().putLong("last_successful_check", lastSuccessfulCheckMs).apply()
                if (candidate == null) {
                    latest = null
                    showOffer = false
                    state = NavUpdateState.NO_UPDATE
                    return@launch
                }
                val decision = navDecideStableUpdate(
                    currentVersionCode, Build.VERSION.SDK_INT, candidate,
                    prefs.getInt("dismissed_code", 0), manual,
                )
                latest = if (decision == NavUpdateDecision.NEW ||
                    decision == NavUpdateDecision.DISMISSED) candidate else null
                when (decision) {
                    NavUpdateDecision.NEW -> {
                        state = NavUpdateState.AVAILABLE
                        showOffer = true
                    }
                    NavUpdateDecision.INCOMPATIBLE -> {
                        state = if (manual) NavUpdateState.INCOMPATIBLE else NavUpdateState.IDLE
                        showOffer = false
                    }
                    NavUpdateDecision.CURRENT_OR_OLD -> {
                        showOffer = false
                        state = NavUpdateState.NO_UPDATE
                    }
                    NavUpdateDecision.DISMISSED -> {
                        showOffer = false
                        state = NavUpdateState.NO_UPDATE
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                // Never interrupt tasks, search, notifications or startup on network errors.
                if (manual) state = NavUpdateState.ERROR
            }
        }
    }

    fun later() {
        latest?.let { prefs.edit().putInt("dismissed_code", it.versionCode).apply() }
        showOffer = false
        if (state == NavUpdateState.AVAILABLE) state = NavUpdateState.IDLE
    }

    fun closeOffer() { showOffer = false }

    fun downloadAndInstall() {
        val candidate = latest ?: return
        if (downloadJob?.isActive == true || checkJob?.isActive == true) return
        showOffer = true
        progressPercent = 0
        state = NavUpdateState.DOWNLOADING
        downloadJob = scope.launch {
            try {
                val file = withContext(Dispatchers.IO) {
                    navDownloadVerifiedApk(context, candidate) { received ->
                        val percent = (received * 100L / candidate.sizeBytes).toInt().coerceIn(0, 100)
                        scope.launch { progressPercent = percent }
                    }
                }
                downloadedFile = file
                progressPercent = 100
                state = NavUpdateState.INSTALL_READY
                requestSystemInstaller()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                state = NavUpdateState.ERROR
            }
        }
    }

    fun requestSystemInstaller() {
        val candidate = latest ?: return
        val file = downloadedFile ?: return
        if (!file.isFile) { state = NavUpdateState.ERROR; return }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            !context.packageManager.canRequestPackageInstalls()
        ) {
            waitingForInstallerPermission = true
            state = NavUpdateState.AWAITING_INSTALL_PERMISSION
            runCatching {
                val intent = Intent(
                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:${context.packageName}")
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }.onFailure { state = NavUpdateState.ERROR }
            return
        }
        waitingForInstallerPermission = false
        scope.launch {
            try {
                // Re-verify after returning from Settings before making URI readable.
                withContext(Dispatchers.IO) { navVerifyDownloadedApk(context, file, candidate) }
                val uri = FileProvider.getUriForFile(
                    context, "${context.packageName}.updates.fileprovider", file
                )
                val install = Intent(Intent.ACTION_VIEW)
                    .setDataAndType(uri, "application/vnd.android.package-archive")
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(install)
                state = NavUpdateState.INSTALL_READY
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                state = NavUpdateState.ERROR
            }
        }
    }

    fun onResumed() {
        if (waitingForInstallerPermission &&
            (Build.VERSION.SDK_INT < Build.VERSION_CODES.O ||
                context.packageManager.canRequestPackageInstalls())
        ) requestSystemInstaller()
        checkAutomatically()
    }
}

/** HTTP fetch is hard bounded. An inactive feed is a normal valid response. */
private fun navReadSmallFeed(): String {
    val con = (URL(NAV_STABLE_FEED_URL).openConnection() as HttpURLConnection)
    con.connectTimeout = 5_000
    con.readTimeout = 8_000
    con.setRequestProperty("Accept", "application/json")
    con.setRequestProperty("Accept-Encoding", "identity")
    return try {
        if (con.responseCode != HttpURLConnection.HTTP_OK) throw IOException("No stable manifest")
        con.inputStream.use { input ->
            val buffer = ByteArray(8192)
            var count = 0
            val output = java.io.ByteArrayOutputStream()
            while (true) {
                val n = input.read(buffer)
                if (n == -1) break
                count += n
                if (count > 64 * 1024) throw IOException("Manifest too large")
                output.write(buffer, 0, n)
            }
            output.toString(Charsets.UTF_8.name())
        }
    } finally { con.disconnect() }
}

private val NAV_ALLOWED_DOWNLOAD_HOSTS =
    setOf("github.com", "release-assets.githubusercontent.com", "objects.githubusercontent.com")

/** Restrict all redirects to the official HTTPS GitHub asset delivery hosts. */
private fun navOpenGitHubApk(url: String): HttpURLConnection {
    var target = URL(url)
    repeat(6) {
        if (target.protocol != "https" || target.host.lowercase(Locale.ROOT) !in NAV_ALLOWED_DOWNLOAD_HOSTS ||
            target.userInfo != null || target.port != -1
        ) throw IOException("Unapproved release download host")
        val conn = target.openConnection() as HttpURLConnection
        conn.instanceFollowRedirects = false
        conn.connectTimeout = 10_000
        conn.readTimeout = 20_000
        conn.setRequestProperty("Accept-Encoding", "identity")
        when (conn.responseCode) {
            200 -> return conn
            301, 302, 303, 307, 308 -> {
                val redirect = conn.getHeaderField("Location") ?: throw IOException("Redirect missing")
                val next = URL(target, redirect)
                conn.disconnect()
                target = next
            }
            else -> {
                conn.disconnect()
                throw IOException("Release download unavailable")
            }
        }
    }
    throw IOException("Too many release redirects")
}

private fun navDigest(bytes: ByteArray): String =
    MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }

private fun navDigestFile(path: File): String {
    val md = MessageDigest.getInstance("SHA-256")
    path.inputStream().buffered().use { input ->
        val bytes = ByteArray(32 * 1024)
        while (true) {
            val n = input.read(bytes)
            if (n < 0) break
            md.update(bytes, 0, n)
        }
    }
    return md.digest().joinToString("") { "%02x".format(it) }
}

private fun navInstalledSigner(context: Context): String {
    val p = context.packageManager
    val installed = p.getPackageInfo(context.packageName, PackageManager.GET_SIGNING_CERTIFICATES)
    val cert = installed.signingInfo?.apkContentsSigners?.singleOrNull()
        ?: throw IOException("Installed signing certificate not available")
    return navDigest(cert.toByteArray())
}

internal fun navVerifyDownloadedApk(context: Context, file: File, update: NavStableUpdate) {
    if (!file.isFile || file.length() != update.sizeBytes ||
        navDigestFile(file) != update.sha256
    ) throw IOException("APK checksum mismatch")
    val pm = context.packageManager
    val info = pm.getPackageArchiveInfo(file.absolutePath, PackageManager.GET_SIGNING_CERTIFICATES)
        ?: throw IOException("Invalid APK archive")
    if (info.packageName != context.packageName ||
        info.longVersionCode != update.versionCode ||
        info.longVersionCode <= context.packageManager.getPackageInfo(
            context.packageName, 0
        ).longVersionCode
    ) throw IOException("APK package or version incompatible")
    if (info.applicationInfo != null &&
        (info.applicationInfo!!.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    ) throw IOException("Stable update cannot be debuggable")
    val signer = info.signingInfo?.apkContentsSigners?.singleOrNull()
        ?: throw IOException("Unsigned APK archive")
    val signDigest = navDigest(signer.toByteArray())
    if (signDigest != update.signerSha256 || signDigest != navInstalledSigner(context))
        throw IOException("APK certificate mismatch")
}

private fun navDownloadVerifiedApk(
    context: Context,
    update: NavStableUpdate,
    reportProgress: (Long) -> Unit,
): File {
    // Files are app-private cache, not profiles, databases, shared media or downloads.
    val folder = File(context.cacheDir, "updates").apply { mkdirs() }
    val partial = File(folder, "pending.part")
    val complete = File(folder, "pending.apk")
    partial.delete()
    complete.delete()
    val conn = navOpenGitHubApk(update.url)
    try {
        var total = 0L
        val md = MessageDigest.getInstance("SHA-256")
        conn.inputStream.use { input ->
            partial.outputStream().buffered().use { output ->
                val buffer = ByteArray(32 * 1024)
                while (true) {
                    val n = input.read(buffer)
                    if (n < 0) break
                    total += n
                    if (total > update.sizeBytes) throw IOException("Oversized APK")
                    output.write(buffer, 0, n)
                    md.update(buffer, 0, n)
                    reportProgress(total)
                }
            }
        }
        val hash = md.digest().joinToString("") { "%02x".format(it) }
        if (total != update.sizeBytes || hash != update.sha256)
            throw IOException("Incomplete or damaged update APK")
        if (!partial.renameTo(complete)) throw IOException("Could not finalize APK")
        navVerifyDownloadedApk(context, complete, update)
        return complete
    } catch (e: Exception) {
        complete.delete()
        throw e
    } finally {
        conn.disconnect()
        partial.delete()
    }
}
