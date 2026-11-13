package com.navtasks.app

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.ext.SdkExtensions
import android.view.View
import android.view.WindowInsetsController
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.pdf.PdfDocument
import androidx.pdf.viewer.fragment.PdfViewerFragment
import java.io.IOException

/**
 * Isolated, fullscreen AndroidX PDF reader. TaskStore, Services, AMOS and
 * shared Reference manual bytes are NEVER modified by the viewer.
 *
 * Devices without S SDK extension13 keep the existing Compose PdfRenderer
 * fallback in ReferenceManualLibraryUi.
 */
internal fun navNativePdfSupported(): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
        SdkExtensions.getExtensionVersion(Build.VERSION_CODES.S) >= 13

internal class NavReferencePdfFragment : PdfViewerFragment() {
    var onReady: (() -> Unit)? = null
    var onFailure: ((Throwable) -> Unit)? = null

    override fun onLoadDocumentSuccess(document: PdfDocument) {
        super.onLoadDocumentSuccess(document)
        onReady?.invoke()
    }

    override fun onLoadDocumentError(error: Throwable) {
        super.onLoadDocumentError(error)
        onFailure?.invoke(error)
    }

    override fun onRequestImmersiveMode(enterImmersive: Boolean) {
        super.onRequestImmersiveMode(enterImmersive)
        activity?.findViewById<View>(R.id.nav_pdf_toolbar)?.visibility =
            if (enterImmersive) View.GONE else View.VISIBLE
    }
}

class ReferencePdfActivity : AppCompatActivity() {
    companion object {
        private const val EXTRA_MANUAL_SHA = "manual_sha256"

        internal fun intent(context: Context, manual: ReferenceManualDocument): Intent {
            manual.validate()
            require(manual.mimeType == ReferenceManualMime.PDF)
            return Intent(context, ReferencePdfActivity::class.java)
                .putExtra(EXTRA_MANUAL_SHA, manual.sha256)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        delegate.localNightMode = when (NavSettings(this).theme) {
            UiTheme.DARK -> AppCompatDelegate.MODE_NIGHT_YES
            UiTheme.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            UiTheme.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.navtasks_reference_pdf)

        val night = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
            Configuration.UI_MODE_NIGHT_YES
        val foreground = Color.parseColor(if (night) "#F4F6FA" else "#20252E")
        val bg = Color.parseColor(if (night) "#000000" else "#E6E8EC")
        val accent = Color.parseColor(if (night) "#5CB8FF" else "#0071E3")
        window.statusBarColor = bg
        window.navigationBarColor = bg
        val flags = WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or
            WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
        window.insetsController?.setSystemBarsAppearance(if (night) 0 else flags, flags)
        findViewById<View>(R.id.nav_pdf_root).setBackgroundColor(bg)
        findViewById<View>(R.id.nav_pdf_toolbar).setBackgroundColor(bg)
        val title = findViewById<TextView>(R.id.nav_pdf_title)
        title.setTextColor(foreground)
        val back = findViewById<TextView>(R.id.nav_pdf_back)
        back.setTextColor(accent)
        back.setOnClickListener { finish() }
        val search = findViewById<ImageButton>(R.id.nav_pdf_search)
        search.setColorFilter(accent)
        search.isEnabled = false

        // The external Intent carries a digest, NEVER an arbitrary file path.
        val result = runCatching {
            check(navNativePdfSupported()) { "Android PDF extension 13+ is required" }
            val sha = intent.getStringExtra(EXTRA_MANUAL_SHA)
                ?: throw IOException("Missing manual ID")
            require(sha.matches(Regex("[0-9a-f]{64}"))) { "Invalid manual ID" }
            val store = ReferenceManualStore(this)
            val document = store.read().documents.firstOrNull {
                it.sha256 == sha && it.mimeType == ReferenceManualMime.PDF
            } ?: throw IOException("PDF not found in offline Reference library")
            val file = store.findOriginal(document)
            title.text = document.title
            file
        }
        result.onFailure { failure ->
            title.text = failure.localizedMessage ?: "PDF unavailable"
            search.visibility = View.GONE
            return
        }
        val file = result.getOrThrow()
        val tag = "navtasks-reference-pdf-viewer"
        val fragment = supportFragmentManager.findFragmentByTag(tag)
            as? NavReferencePdfFragment ?: NavReferencePdfFragment().also {
            supportFragmentManager.beginTransaction()
                .replace(R.id.nav_pdf_container, it, tag).commitNow()
        }
        fragment.onReady = { search.isEnabled = true }
        fragment.onFailure = { throwable ->
            title.text = throwable.localizedMessage ?: "Unable to read PDF"
            search.isEnabled = false
        }
        search.setOnClickListener {
            if (search.isEnabled) fragment.isTextSearchActive = true
        }
        fragment.documentUri = Uri.fromFile(file)
    }
}
