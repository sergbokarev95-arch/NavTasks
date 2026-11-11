package com.navtasks.app

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File

/** The Settings button can open the same dialog even after "Later". */
internal object NavUpdateFlow {
    var explicitlyOpened by mutableStateOf(false)
        private set
    fun open() { explicitlyOpened = true }
    fun close() { explicitlyOpened = false }
}

/** Never install without an explicit press and the Android system installer. */
@Composable
internal fun NavUpdateOffer(
    settings: NavSettings, dark: Boolean, context: Context,
) {
    val update = NavUpdateChecker.candidate
    val scope = rememberCoroutineScope()
    val p = navPalette(dark)
    val lang = settings.language
    val key = update?.versionCode ?: -1
    var hiddenVersion by remember { mutableIntStateOf(-1) }
    var percent by remember(key) { mutableIntStateOf(0) }
    var downloading by remember(key) { mutableStateOf(false) }
    var installing by remember(key) { mutableStateOf(false) }
    var verifiedApk by remember(key) { mutableStateOf<File?>(null) }
    var error by remember(key) { mutableStateOf<String?>(null) }
    var permissionNeeded by remember(key) { mutableStateOf(false) }
    var installerOpened by remember(key) { mutableStateOf(false) }
    var job by remember(key) { mutableStateOf<Job?>(null) }
    if (update == null ||
        (hiddenVersion == update.versionCode && !NavUpdateFlow.explicitlyOpened) ||
        (!NavUpdateFlow.explicitlyOpened &&
            !NavUpdateChecker.shouldOfferAutomatically(context, update, settings.automaticUpdateOffers))
    ) return

    fun later() {
        job?.cancel()
        NavUpdateChecker.defer(context, update.versionCode)
        hiddenVersion = update.versionCode
        NavUpdateFlow.close()
    }

    Dialog(
        onDismissRequest = { if (downloading || installing) job?.cancel() else later() },
        properties = DialogProperties(dismissOnClickOutside = false),
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
                Modifier.fillMaxWidth().heightIn(max = 550.dp)
                    .verticalScroll(rememberScrollState()).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(localized("Доступно обновление", "Update available", lang),
                    color = p.text, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                val installed = remember(context) {
                    context.packageManager.getPackageInfo(context.packageName, 0)
                        .versionName.orEmpty()
                }
                Text(
                    localized("Установлено", "Installed", lang) + ": " + installed +
                        "   ·   " + localized("Доступно", "Available", lang) +
                        ": " + update.versionName,
                    color = p.muted, fontSize = 12.sp,
                )
                Text(localized("Что нового", "What's new", lang),
                    color = p.text, fontWeight = FontWeight.Medium)
                Text(if (lang == UiLanguage.RU) update.notesRu else update.notesEn,
                    color = p.muted, fontSize = 13.sp)

                if (downloading) {
                    Text(localized("Загрузка APK", "Downloading APK", lang) +
                        " — " + percent + "%", color = p.text)
                    LinearProgressIndicator(progress = { percent / 100f },
                        modifier = Modifier.fillMaxWidth(), color = p.blue, trackColor = p.border)
                    OutlinedButton(onClick = { job?.cancel() },
                        modifier = Modifier.fillMaxWidth()) {
                        Text(localized("Отменить загрузку", "Cancel download", lang))
                    }
                } else if (installing) {
                    Text(localized("Проверка APK…", "Verifying APK…", lang),
                        color = p.muted, fontSize = 13.sp)
                } else {
                    if (error != null) {
                        Text(when (error) {
                            "download" -> localized(
                                "Скачивание или проверка APK завершились ошибкой. Повтори попытку.",
                                "APK download or verification failed. Try again.", lang)
                            "install" -> localized(
                                "Не удалось открыть установщик. Текущая версия не изменилась.",
                                "Cannot open installer. Installed app is unchanged.", lang)
                            else -> localized("Загрузка отменена.", "Download canceled.", lang)
                        }, color = p.warningText, fontSize = 12.sp)
                    }
                    if (permissionNeeded) {
                        Text(localized(
                            "Разреши установку из NavTasks в Android, вернись и нажми «Установить».",
                            "Allow installs from NavTasks in Android, then tap Install again.",
                            lang), color = p.muted, fontSize = 12.sp)
                    }
                    if (installerOpened) {
                        Text(localized(
                            "Системный установщик открыт. Результат ещё не подтверждён.",
                            "System installer opened. Installation is not yet confirmed.",
                            lang), color = p.muted, fontSize = 12.sp)
                    }

                    if (verifiedApk == null) {
                        Button(
                            onClick = {
                                percent = 0
                                error = null
                                permissionNeeded = false
                                installerOpened = false
                                job = scope.launch {
                                    downloading = true
                                    try {
                                        verifiedApk = NavUpdateInstaller.download(context, update) {
                                            value -> percent = value
                                        }
                                    } catch (_: CancellationException) {
                                        error = "cancel"
                                    } catch (_: Exception) {
                                        error = "download"
                                    } finally {
                                        downloading = false
                                        job = null
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = p.blue, contentColor = Color.White),
                        ) { Text(localized("Обновить", "Update", lang)) }
                    } else {
                        Button(
                            onClick = {
                                val apk = verifiedApk ?: return@Button
                                if (!context.packageManager.canRequestPackageInstalls()) {
                                    permissionNeeded = true
                                    runCatching {
                                        NavUpdateInstaller.askForInstallPermission(context)
                                    }.onFailure { error = "install" }
                                } else {
                                    permissionNeeded = false
                                    error = null
                                    job = scope.launch {
                                        installing = true
                                        try {
                                            NavUpdateInstaller.install(context, apk, update)
                                            installerOpened = true
                                        } catch (_: CancellationException) {
                                            error = "cancel"
                                        } catch (_: Exception) {
                                            error = "install"
                                            verifiedApk = null
                                        } finally {
                                            installing = false
                                            job = null
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = p.blue, contentColor = Color.White),
                        ) { Text(localized("Установить", "Install", lang)) }
                    }
                    OutlinedButton(onClick = { later() },
                        modifier = Modifier.fillMaxWidth()) {
                        Text(localized("Позже", "Later", lang))
                    }
                }
                Text(localized(
                    "Обновление добровольное. Отмена не удаляет ваши данные.",
                    "Updates are optional. Canceling does not delete your data.",
                    lang), color = p.muted, fontSize = 11.sp)
            }
        }
    }
}
