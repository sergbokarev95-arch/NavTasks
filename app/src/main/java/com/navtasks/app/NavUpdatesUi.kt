package com.navtasks.app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.DateFormat
import java.util.Date

/** Reuse the application's existing glass popup, not a new dialog system. */
@Composable
internal fun NavUpdateOffer(
    update: NavUpdateController,
    p: NavPalette,
    lang: UiLanguage,
) {
    val candidate = update.latest ?: return
    if (!update.showOffer) return
    val busy = update.state == NavUpdateState.DOWNLOADING
    val ru = lang == UiLanguage.RU
    NavGlassAlertDialog(
        p = p,
        onDismissRequest = { if (!busy) update.later() },
        title = { Text(localized("Доступно обновление", "Update available", lang)) },
        text = {
            Column {
                Text(
                    localized("Установлено: ", "Installed: ", lang) + update.currentVersionName,
                    color = p.muted, fontSize = 13.sp,
                )
                Text(
                    localized("Доступно: ", "Available: ", lang) + candidate.versionName,
                    color = p.text, fontWeight = FontWeight.Medium, fontSize = 15.sp,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    if (ru) candidate.notesRu else candidate.notesEn,
                    color = p.muted, fontSize = 13.sp,
                )
                if (busy) {
                    Spacer(Modifier.height(14.dp))
                    LinearProgressIndicator(
                        progress = { update.progressPercent / 100f },
                        modifier = Modifier.fillMaxWidth(),
                        color = p.blue,
                    )
                    Text(
                        localized("Загрузка: ", "Downloading: ", lang) +
                            "${update.progressPercent}%",
                        color = p.muted, fontSize = 12.sp,
                    )
                }
                when (update.state) {
                    NavUpdateState.ERROR -> Text(
                        localized(
                            "Не удалось загрузить или проверить APK. Установленная версия и данные не изменены.",
                            "APK download or verification failed. Installed app and data are unchanged.",
                            lang,
                        ),
                        color = p.warningText, fontSize = 12.sp,
                    )
                    NavUpdateState.AWAITING_INSTALL_PERMISSION -> Text(
                        localized(
                            "Разреши установку из NavTasks в системных настройках Android, затем вернись сюда.",
                            "Allow installation from NavTasks in Android settings, then return.",
                            lang,
                        ), color = p.muted, fontSize = 12.sp,
                    )
                    NavUpdateState.INSTALL_READY -> Text(
                        localized(
                            "Файл проверен. Подтверди установку в стандартном окне Android.",
                            "APK verified. Confirm installation in the Android system installer.",
                            lang,
                        ), color = p.muted, fontSize = 12.sp,
                    )
                    else -> Unit
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (update.state == NavUpdateState.INSTALL_READY ||
                        update.state == NavUpdateState.AWAITING_INSTALL_PERMISSION
                    ) update.requestSystemInstaller()
                    else update.downloadAndInstall()
                },
                enabled = !busy,
            ) {
                Text(
                    when (update.state) {
                        NavUpdateState.ERROR -> localized("Повторить", "Retry", lang)
                        NavUpdateState.INSTALL_READY -> localized("Установить", "Install", lang)
                        NavUpdateState.AWAITING_INSTALL_PERMISSION ->
                            localized("Разрешение", "Permission", lang)
                        else -> localized("Обновить", "Update", lang)
                    },
                    color = if (busy) p.muted else p.blueText,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = { update.later() }, enabled = !busy) {
                Text(localized("Позже", "Later", lang), color = p.muted)
            }
        },
    )
}

/** Displayed only in Settings, so offline/manual failures never block app start. */
@Composable
internal fun NavUpdateSettingsPanel(
    update: NavUpdateController,
    p: NavPalette,
    lang: UiLanguage,
) {
    NavPanel(p) {
        Text(localized("Обновления", "Updates", lang), color = p.text,
            fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Text(
            localized("Установлена версия ", "Installed version ", lang) +
                update.currentVersionName,
            color = p.muted, fontSize = 13.sp,
        )
        Text(
            if (update.lastSuccessfulCheckMs > 0)
                localized("Последняя проверка: ", "Last checked: ", lang) +
                    DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                        .format(Date(update.lastSuccessfulCheckMs))
            else localized("Проверок пока не было", "Not checked yet", lang),
            color = p.muted, fontSize = 12.sp,
        )
        val available = update.latest
        if (available != null) {
            Text(
                localized("В канале доступна: ", "Channel version: ", lang) +
                    available.versionName,
                color = p.text, fontSize = 13.sp,
            )
            Text(if (lang == UiLanguage.RU) available.notesRu else available.notesEn,
                color = p.muted, fontSize = 12.sp)
        }
        when (update.state) {
            NavUpdateState.OFFLINE -> Text(
                localized("Нет интернета. Основные функции работают офлайн.",
                    "No internet. All core features work offline.", lang),
                color = p.muted, fontSize = 12.sp,
            )
            NavUpdateState.ERROR -> Text(
                localized("Не удалось проверить обновления. Повтори позже.",
                    "Could not check updates. Try again later.", lang),
                color = p.warningText, fontSize = 12.sp,
            )
            NavUpdateState.NO_UPDATE -> Text(
                localized("Новых стабильных обновлений нет.",
                    "No new stable updates.", lang),
                color = p.muted, fontSize = 12.sp,
            )
            NavUpdateState.INCOMPATIBLE -> Text(
                localized("Для этой установленной версии автоматический переход не подтверждён.",
                    "Direct upgrade from this installed version is not verified.", lang),
                color = p.warningText, fontSize = 12.sp,
            )
            NavUpdateState.CHECKING -> Text(
                localized("Проверяем GitHub Releases…", "Checking GitHub Releases…", lang),
                color = p.muted, fontSize = 12.sp,
            )
            else -> Unit
        }
        OutlinedButton(
            onClick = { update.checkManually() },
            modifier = Modifier.fillMaxWidth(),
            enabled = update.state != NavUpdateState.CHECKING &&
                update.state != NavUpdateState.DOWNLOADING,
        ) {
            Text(localized("Проверить обновления", "Check for updates", lang))
        }
    }
}
