package com.navtasks.app

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.widget.Toast
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider

/**
 * Narrow file-sharing boundary for the explicitly selected original manual.
 * No write permission is ever granted: even an external editor must Save As
 * elsewhere and cannot change NavTasks' content-addressed original.
 */
class ReferenceManualFileProvider : FileProvider(R.xml.navtasks_reference_share_paths) {
    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor {
        if (mode != "r") throw SecurityException("NavTasks manuals are read-only")
        return super.openFile(uri, mode) ?: throw java.io.FileNotFoundException("Manual is unavailable")
    }
}

internal fun referenceExternalManualMime(mime: String): String = when (mime) {
    ReferenceManualMime.PDF -> "application/pdf"
    ReferenceManualMime.DOCX ->
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    else -> throw IllegalArgumentException("Unsupported external document format")
}

/**
 * Use a content:// URI with a single URI read grant, never a file:// URI,
 * never ACTION_EDIT and never a wildcard shared folder.
 *
 * The original display name is preserved via FileProvider's displayName API,
 * while the private stored file continues to be checksum-addressed.
 */
internal fun referenceExternalOpenChooser(
    context: Context,
    document: ReferenceManualDocument,
    store: ReferenceManualStore,
    chooserTitle: String,
): Intent {
    document.validate()
    val mime = referenceExternalManualMime(document.mimeType)
    val original = store.findOriginal(document)
    val authority = context.packageName + ".reference.fileprovider"
    val contentUri = FileProvider.getUriForFile(
        context, authority, original, document.originalFilename,
    )
    val clip = ClipData.newUri(context.contentResolver, document.originalFilename, contentUri)
    val view = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(contentUri, mime)
        clipData = clip
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    return Intent.createChooser(view, chooserTitle).apply {
        clipData = clip
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
}

/** The same action works in the standalone library and equipment attachments. */
@Composable
internal fun ReferenceManualOpenWithButton(
    document: ReferenceManualDocument,
    store: ReferenceManualStore,
    p: NavPalette,
    lang: UiLanguage,
) {
    val context = LocalContext.current
    TextButton(onClick = {
        runCatching {
            val chooser = referenceExternalOpenChooser(
                context, document, store,
                localized("Открыть через", "Open with", lang),
            )
            context.startActivity(chooser)
        }.onFailure {
            Toast.makeText(context, localized(
                "Не удалось открыть документ. Проверь наличие приложения для PDF/Word.",
                "Cannot open document. Check for an installed PDF/Word viewer.",
                lang,
            ), Toast.LENGTH_LONG).show()
        }
    }) {
        Text(localized("Открыть через", "Open with", lang),
            color = p.blueText, fontSize = 13.sp, maxLines = 1)
    }
}
