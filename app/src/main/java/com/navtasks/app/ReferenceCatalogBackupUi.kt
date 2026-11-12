package com.navtasks.app

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.ByteArrayOutputStream
import java.io.IOException

/**
 * Whole EQUIPMENT CATALOG backup (folders + devices + cited procedures) per vessel.
 * Do not confuse with future full library archive containing PDF/DOCX binaries.
 */
@Composable
internal fun ReferenceCatalogBackupPanel(
    p: NavPalette,
    lang: UiLanguage,
    catalog: ReferenceEquipmentCatalog,
    onRestore: (ReferenceEquipmentCatalog) -> Boolean,
) {
    val context = LocalContext.current
    var pendingImport by remember { mutableStateOf<ReferenceEquipmentCatalog?>(null) }
    var feedback by remember { mutableStateOf<String?>(null) }
    var failed by remember { mutableStateOf(false) }

    val exportFile = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream"),
    ) { uri ->
        if (uri != null) {
            runCatching {
                val bytes = ReferenceCatalogArchive.encode(catalog)
                context.contentResolver.openOutputStream(uri)?.use { output ->
                    output.write(bytes)
                } ?: throw IOException("Cannot create Reference catalog file")
            }.onSuccess {
                failed = false
                feedback = localized("Каталог оборудования сохранён в файл.",
                    "Equipment catalog exported.", lang)
            }.onFailure {
                failed = true
                feedback = it.localizedMessage
            }
        }
    }

    val importFile = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            runCatching {
                val bytes = context.contentResolver.openInputStream(uri)?.use { input ->
                    val buffer = ByteArray(8192)
                    val output = ByteArrayOutputStream()
                    while (true) {
                        val read = input.read(buffer)
                        if (read < 0) break
                        require(output.size() + read <= ReferenceCatalogArchive.maxArchiveBytes) {
                            "Equipment catalog archive exceeds size limit"
                        }
                        output.write(buffer, 0, read)
                    }
                    output.toByteArray()
                } ?: throw IOException("Cannot read Reference catalog file")
                ReferenceCatalogArchive.decode(bytes)
            }.onSuccess {
                pendingImport = it
                failed = false
                feedback = null
            }.onFailure {
                failed = true
                feedback = it.localizedMessage
            }
        }
    }

    Text(localized("Каталог оборудования целиком",
        "Complete equipment catalog", lang), color = p.text, fontSize = 14.sp)
    Text(localized(
        "Архив содержит все папки, оборудование и процедуры текущего судового профиля. " +
            "Серийные номера тоже включены. PDF и Word пока не входят в этот архив.",
        "Back up all folders, equipment and cited procedures of this vessel. " +
            "Serial numbers are included. PDF and Word files are not yet included.", lang),
        color = p.muted, fontSize = 12.sp)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
        OutlinedButton(
            onClick = { importFile.launch(arrayOf("*/*")) },
            modifier = Modifier.weight(1f),
        ) {
            Text(localized("Восстановить", "Restore", lang), color = p.blueText,
                fontSize = 12.sp)
        }
        OutlinedButton(
            onClick = { exportFile.launch("NavTasks_Equipment_Catalog.navref") },
            modifier = Modifier.weight(1f),
        ) {
            Text(localized("Сохранить архив", "Export backup", lang), color = p.blueText,
                fontSize = 12.sp)
        }
    }
    if (feedback != null) {
        Text(feedback.orEmpty(), color = if (failed) p.systemRed else p.blueText,
            fontSize = 12.sp)
    }

    pendingImport?.let { incoming ->
        val summary = remember(catalog, incoming) {
            referenceCatalogImportSummary(catalog, incoming)
        }
        NavGlassAlertDialog(
            p,
            onDismissRequest = { pendingImport = null },
            title = { Text(localized("Восстановление каталога",
                "Restore equipment catalog", lang)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Text(localized("В архиве:", "Archive contains:", lang),
                        color = p.text)
                    Text(localized("Папок: ", "Folders: ", lang) + summary.folders +
                        localized(" · Оборудования: ", " · Equipment: ", lang) +
                        summary.equipment, color = p.muted)
                    Text(localized("Процедур: ", "Procedures: ", lang) + summary.procedures,
                        color = p.muted)
                    if (summary.similarEquipment > 0) {
                        Text(localized("Похожих записей уже имеется: ",
                            "Similar existing devices: ", lang) + summary.similarEquipment,
                            color = p.systemOrange)
                    }
                    Text(localized(
                        "Будут добавлены копии с сохранением структуры папок. " +
                            "Существующие записи не заменяются и не удаляются. " +
                            "Задачи и уведомления не создаются.",
                        "Copies will be added with the folder structure preserved. " +
                            "Existing items will never be overwritten or deleted. " +
                            "No tasks or reminders are created.", lang),
                        color = p.muted, fontSize = 12.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    runCatching { referenceMergeCatalogCopies(catalog, incoming) }
                        .onSuccess { merged ->
                            if (onRestore(merged)) {
                                pendingImport = null
                                failed = false
                                feedback = localized("Каталог восстановлен без удаления существующих записей.",
                                    "Catalog added without deleting existing items.", lang)
                            } else {
                                failed = true
                                feedback = localized("Ошибка записи каталога. Исходные данные сохранены.",
                                    "Unable to write catalog. Original data was kept.", lang)
                            }
                        }
                        .onFailure {
                            failed = true
                            feedback = it.localizedMessage
                        }
                }) {
                    Text(localized("Добавить копии", "Add copies", lang), color = p.blueText)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingImport = null }) {
                    Text(localized("Отмена", "Cancel", lang), color = p.muted)
                }
            },
        )
    }
}
