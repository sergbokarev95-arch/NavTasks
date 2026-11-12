package com.navtasks.app

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.ByteArrayOutputStream
import java.io.IOException

/** Import/export lives in Settings as well as the equipment-card context menu. */
@Composable
internal fun ReferenceEquipmentSettingsPanel(
    profileId: String,
    p: NavPalette,
    lang: UiLanguage,
) {
    val context = LocalContext.current
    val store = remember(context, profileId) { ReferenceEquipmentStore(context, profileId) }
    val loaded = remember(store) { runCatching { store.read() } }
    var catalog by remember(store) { mutableStateOf(loaded.getOrNull()) }
    var exportChooser by remember { mutableStateOf(false) }
    var selectedExport by remember { mutableStateOf<ReferenceEquipment?>(null) }
    var incoming by remember { mutableStateOf<ReferenceEquipment?>(null) }
    var status by remember { mutableStateOf<String?>(null) }
    var failure by remember { mutableStateOf(false) }

    fun save(updated: ReferenceEquipmentCatalog): Boolean {
        val result = runCatching { store.write(updated) }
        result.onSuccess { catalog = updated }
            .onFailure {
                failure = true
                status = it.localizedMessage ?: localized("Не удалось сохранить справочник.",
                    "Could not save reference data.", lang)
            }
        return result.isSuccess
    }

    val saveDocument = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream"),
    ) { uri ->
        val item = selectedExport
        if (uri != null && item != null) {
            runCatching {
                val bytes = ReferenceEquipmentExchange.encode(item)
                context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
                    ?: throw IOException("Cannot open output file")
            }.onSuccess {
                failure = false
                status = localized("Карточка оборудования экспортирована.",
                    "Equipment exported.", lang)
            }.onFailure {
                failure = true
                status = it.localizedMessage
            }
        }
        selectedExport = null
    }
    val openDocument = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            runCatching {
                val raw = context.contentResolver.openInputStream(uri)?.use { input ->
                    val output = ByteArrayOutputStream()
                    val buffer = ByteArray(8192)
                    while (true) {
                        val count = input.read(buffer)
                        if (count < 0) break
                        require(output.size() + count <= 12_000_000) {
                            "Reference archive exceeds 12 MB"
                        }
                        output.write(buffer, 0, count)
                    }
                    output.toByteArray()
                } ?: throw IOException("Cannot open reference archive")
                ReferenceEquipmentExchange.decode(raw)
            }.onSuccess { incoming = it; status = null; failure = false }
                .onFailure { failure = true; status = it.localizedMessage }
        }
    }

    NavPanel(p) {
        Text(localized("Справочник · Импорт и экспорт", "Reference · Import and export", lang),
            color = p.text, fontSize = 16.sp)
        Text(localized(
            "Обмен отдельными карточками оборудования, процедурами и выдержками. " +
                "Работы AMOS и Services не передаются.",
            "Share individual equipment cards, manual procedures and excerpts. " +
                "No AMOS, Services or tasks are included.", lang),
            color = p.muted, fontSize = 12.sp)
        if (catalog == null) {
            Text(localized("Каталог требует проверки. Импорт и экспорт заблокированы без удаления данных.",
                "Catalog cannot be safely read. Import and export are blocked without data loss.", lang),
                color = p.systemRed, fontSize = 12.sp)
        } else {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                OutlinedButton(
                    onClick = { openDocument.launch(arrayOf("*/*")) },
                    modifier = Modifier.weight(1f),
                ) { Text(localized("Импорт", "Import", lang), color = p.blueText) }
                OutlinedButton(
                    onClick = { exportChooser = true },
                    enabled = catalog!!.equipment.isNotEmpty(),
                    modifier = Modifier.weight(1f),
                ) { Text(localized("Экспорт", "Export", lang), color = p.blueText) }
            }
            Text(localized("Оборудования в этом профиле: ",
                "Equipment in this profile: ", lang) + catalog!!.equipment.size,
                color = p.muted, fontSize = 11.sp)
        }
        if (status != null) {
            Text(status.orEmpty(), color = if (failure) p.systemRed else p.blueText,
                fontSize = 12.sp)
        }
    }

    if (exportChooser && catalog != null) {
        NavGlassAlertDialog(p, onDismissRequest = { exportChooser = false },
            title = { Text(localized("Выбери оборудование", "Choose equipment", lang)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    catalog!!.equipment.sortedBy { it.title.lowercase() }.forEach { item ->
                        TextButton(onClick = {
                            selectedExport = item
                            exportChooser = false
                            val name = item.title.replace(
                                Regex("[^\\p{L}\\p{N}_-]"), "_").take(60)
                            saveDocument.launch("$name.navref")
                        }, modifier = Modifier.fillMaxWidth()) {
                            Text(item.title, color = p.text, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { exportChooser = false }) {
                    Text(localized("Закрыть", "Close", lang), color = p.blueText)
                }
            },
        )
    }
    incoming?.let { candidate ->
        val conflict = referenceImportConflict(candidate, catalog?.equipment.orEmpty())
        NavGlassAlertDialog(p, onDismissRequest = { incoming = null },
            title = { Text(localized("Проверка импорта", "Review import", lang)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Text(candidate.title, color = p.text)
                    Text(localized("Процедур: ", "Procedures: ", lang) +
                        candidate.procedures.size, color = p.muted)
                    Text(when (conflict) {
                        ReferenceImportConflict.SAME_ID -> localized(
                            "Эта запись уже есть. Можно добавить копию или заменить после подтверждения.",
                            "This item exists. Add a copy or explicitly replace.", lang)
                        ReferenceImportConflict.POSSIBLE_DUPLICATE -> localized(
                            "Похожее оборудование уже существует. Проверь перед импортом.",
                            "Similar equipment already exists; review before import.", lang)
                        else -> localized("Будет добавлено в текущий профиль. Задачи не создаются.",
                            "Will be added to this profile without creating tasks.", lang)
                    }, color = p.muted, fontSize = 13.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val current = catalog
                    if (current != null &&
                        save(referenceImportEquipment(current, candidate, targetFolderId = null))) {
                        incoming = null
                        failure = false
                        status = localized("Оборудование импортировано.",
                            "Equipment imported.", lang)
                    }
                }) { Text(localized("Добавить копию", "Add copy", lang), color = p.blueText) }
            },
            dismissButton = {
                TextButton(onClick = { incoming = null }) {
                    Text(localized("Отмена", "Cancel", lang), color = p.muted)
                }
                if (conflict == ReferenceImportConflict.SAME_ID) {
                    TextButton(onClick = {
                        val current = catalog
                        if (current != null &&
                            save(referenceImportEquipment(current, candidate,
                                targetFolderId = null, replaceSameId = true))) {
                            incoming = null
                        }
                    }) { Text(localized("Заменить", "Replace", lang), color = p.systemRed) }
                }
            },
        )
    }
}
