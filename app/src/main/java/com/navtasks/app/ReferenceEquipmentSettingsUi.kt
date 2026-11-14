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
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Independent Reference import/export panel, opened from the Reference hub. */
@Composable
internal fun ReferenceEquipmentSettingsPanel(
    profileId: String,
    p: NavPalette,
    lang: UiLanguage,
) {
    val context = LocalContext.current
    val store = remember(context, profileId) { ReferenceEquipmentStore(context, profileId) }
    val manualStore = remember(context) { ReferenceManualStore(context) }
    val scope = rememberCoroutineScope()
    val loaded = remember(store) { runCatching { store.read() } }
    var catalog by remember(store) { mutableStateOf(loaded.getOrNull()) }
    var exportChooser by remember { mutableStateOf(false) }
    var selectedExport by remember { mutableStateOf<ReferenceEquipment?>(null) }
    var incoming by remember { mutableStateOf<StagedReferenceEquipment?>(null) }
    val stagedForDisposal = incoming
    DisposableEffect(stagedForDisposal) {
        onDispose { stagedForDisposal?.close() }
    }
    var importing by remember { mutableStateOf(false) }
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

    // This hub panel uses the SAME v2 packaging and SHA-verified import as the
    // Equipment screen. Never fall back to manifest-only schema v1 here.
    val saveDocument = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream"),
    ) { uri ->
        val item = selectedExport
        selectedExport = null
        if (uri != null && item != null) scope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val (verified, count) = ReferenceEquipmentPortableV2.prepareVerifiedExport(
                        item, manualStore.read().documents,
                        manualStore::findOriginal, context.cacheDir)
                    try {
                        context.contentResolver.openOutputStream(uri)?.use { output ->
                            verified.inputStream().use { input -> input.copyTo(output) }
                        } ?: throw IOException("Cannot open equipment export file")
                        count
                    } finally {
                        verified.delete()
                    }
                }
            }
            result.onSuccess { count ->
                failure = false
                status = localized(
                    "Оборудование сохранено (v2). Оригиналов PDF/Word: $count.",
                    "Equipment saved (v2). Original PDF/Word files: $count.", lang)
            }.onFailure {
                failure = true
                status = it.localizedMessage ?: localized(
                    "Экспорт не выполнен: не удалось включить вложение.",
                    "Export failed: could not include the attachment.", lang)
            }
        }
    }

    val openDocument = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) scope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        ReferenceEquipmentPortableV2.stage(input, context.cacheDir)
                    } ?: throw IOException("Cannot open equipment archive")
                }
            }
            result.onSuccess { staged ->
                incoming = staged
                failure = false
                status = null
            }.onFailure {
                failure = true
                status = it.localizedMessage ?: localized(
                    "Не удалось прочитать оборудование и вложения.",
                    "Could not read equipment and its attachments.", lang)
            }
        }
    }

    fun confirmImport(staged: StagedReferenceEquipment, replaceSameId: Boolean) {
        if (importing) return
        val current = catalog ?: return
        importing = true
        scope.launch {
            val outcome = withContext(Dispatchers.IO) {
                runCatching { restoreStagedEquipmentOriginals(staged, manualStore) }
            }
            outcome.onSuccess {
                if (save(referenceImportEquipment(
                        current, staged.equipment, targetFolderId = null,
                        replaceSameId = replaceSameId))) {
                    incoming = null
                    failure = false
                    status = localized(
                        "Оборудование импортировано. Документов: ${staged.originals.size}.",
                        "Equipment imported. Documents: ${staged.originals.size}.", lang)
                }
            }.onFailure {
                failure = true
                status = it.localizedMessage ?: localized(
                    "Не удалось восстановить PDF/Word.", "Could not restore PDF/Word.", lang)
            }
            importing = false
        }
    }

    NavPanel(p) {
        Text(localized("Обмен оборудованием", "Equipment exchange", lang),
            color = p.text, fontSize = 16.sp)
        Text(localized(
            "Карточки оборудования передаются вместе с прикреплёнными PDF и Word. " +
                "Профили, задачи, AMOS и Services не передаются.",
            "Equipment cards include attached PDF and Word originals. " +
                "Profiles, tasks, AMOS and Services are not included.", lang),
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
            HorizontalDivider(
                color = p.border.copy(alpha = .4f),
                modifier = Modifier.padding(vertical = 6.dp),
            )
            ReferenceCatalogBackupPanel(
                p = p, lang = lang, catalog = catalog!!,
                onRestore = { restored -> save(restored) },
            )
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
    incoming?.let { staged ->
        val candidate = staged.equipment
        val conflict = referenceImportConflict(candidate, catalog?.equipment.orEmpty())
        NavGlassAlertDialog(p, onDismissRequest = { if (!importing) incoming = null },
            title = { Text(localized("Проверка импорта", "Review import", lang)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Text(candidate.title, color = p.text)
                    Text(localized("Процедур: ", "Procedures: ", lang) +
                        candidate.procedures.size, color = p.muted)
                    Text(localized("Оригиналов PDF/Word: ", "Original PDF/Word files: ", lang) +
                        staged.originals.size, color = p.muted)
                    if (staged.legacyMissingAttachments) {
                        Text(localized(
                            "Старый файл v1 содержит только описание, без PDF/Word. " +
                                "Попроси отправить новый .navref из обновлённого NavTasks.",
                            "This old v1 file contains descriptions only, no PDF/Word. " +
                                "Ask for a new .navref from the updated NavTasks.", lang),
                            color = p.systemRed, fontSize = 12.sp)
                    }
                    Text(when (conflict) {
                        ReferenceImportConflict.SAME_ID -> localized(
                            "Эта запись уже есть. Можно добавить копию или заменить после подтверждения.",
                            "This item exists. Add a copy or explicitly replace.", lang)
                        ReferenceImportConflict.POSSIBLE_DUPLICATE -> localized(
                            "Похожее оборудование уже существует. Проверь перед импортом.",
                            "Similar equipment already exists; review before import.", lang)
                        else -> localized(
                            "Будет добавлено в текущий профиль. Задачи не создаются.",
                            "Will be added to this profile without creating tasks.", lang)
                    }, color = p.muted, fontSize = 13.sp)
                }
            },
            confirmButton = {
                TextButton(enabled = !importing, onClick = {
                    confirmImport(staged, replaceSameId = false)
                }) { Text(
                    if (staged.legacyMissingAttachments)
                        localized("Без PDF", "Without PDF", lang)
                    else localized("Добавить копию", "Add copy", lang),
                    color = p.blueText) }
            },
            dismissButton = {
                TextButton(enabled = !importing, onClick = { incoming = null }) {
                    Text(localized("Отмена", "Cancel", lang), color = p.muted)
                }
                if (conflict == ReferenceImportConflict.SAME_ID &&
                    !staged.legacyMissingAttachments) {
                    TextButton(enabled = !importing, onClick = {
                        confirmImport(staged, replaceSameId = true)
                    }) { Text(localized("Заменить", "Replace", lang), color = p.systemRed) }
                }
            },
        )
    }
}
