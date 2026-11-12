package com.navtasks.app

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Android sends MY_PACKAGE_REPLACED only after an installed APK was replaced.
 * Do not show a misleading success message on first install or mere download.
 * This preference is app-global and does not read/write any user profile data.
 */
internal fun navReceiptMatches(
    pendingCode: Long,
    pendingName: String?,
    installedCode: Long,
    installedName: String,
): Boolean = pendingCode > 0L && pendingCode == installedCode &&
    !pendingName.isNullOrBlank() && pendingName == installedName

internal object NavUpdateReceipt {
    private const val PREFS = "navtasks-update-receipt-v1"
    private const val CODE = "pending_version_code"
    private const val NAME = "pending_version_name"

    fun recordPackageReplaced(context: Context): Boolean = runCatching {
        val app = context.applicationContext
        val version = app.packageManager.getPackageInfo(app.packageName, 0)
        val name = version.versionName.orEmpty()
        if (version.longVersionCode <= 0L || name.isBlank()) return@runCatching false
        app.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putLong(CODE, version.longVersionCode).putString(NAME, name).commit()
    }.getOrDefault(false)

    fun pending(context: Context): String? = runCatching {
        val app = context.applicationContext
        val prefs = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val version = app.packageManager.getPackageInfo(app.packageName, 0)
        val installedName = version.versionName.orEmpty()
        if (navReceiptMatches(
                prefs.getLong(CODE, -1L), prefs.getString(NAME, null),
                version.longVersionCode, installedName,
            )
        ) installedName else null
    }.getOrNull()

    /** Only user acknowledgment clears the pending receipt; a crash keeps it. */
    fun acknowledge(context: Context, version: String): Boolean {
        if (pending(context) != version) return false
        return context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().remove(CODE).remove(NAME).commit()
    }
}

@Composable
internal fun NavUpdateInstalledNotice(
    version: String,
    dark: Boolean,
    language: UiLanguage,
    onAcknowledge: () -> Unit,
) {
    val p = navPalette(dark)
    Dialog(
        onDismissRequest = {}, // Must be explicitly acknowledged before clearing.
        properties = DialogProperties(dismissOnClickOutside = false, dismissOnBackPress = false),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            color = p.panel,
            contentColor = p.text,
            border = BorderStroke(1.dp, p.border),
            shadowElevation = 12.dp,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    localized("NavTasks обновлён", "NavTasks updated", language),
                    color = p.text, fontSize = 20.sp, fontWeight = FontWeight.SemiBold,
                )
                Text(
                    localized(
                        "Успешно установлена версия $version.",
                        "Version $version has been installed successfully.",
                        language,
                    ),
                    color = p.muted, fontSize = 14.sp,
                )
                Button(
                    onClick = onAcknowledge,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = p.blue, contentColor = Color.White,
                    ),
                ) {
                    Text(localized("Готово", "Done", language))
                }
            }
        }
    }
}
