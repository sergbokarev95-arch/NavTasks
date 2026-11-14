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
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    val manualStore = remember(context) { ReferenceManualStore(context) }
    val scope = rememberCoroutineScope()
    val loaded = remember(store) { runCatching { store.read() } }
    var catalog by remember(store) { mutableStateOf(loaded.getOrNull()) }
    var exportChooser by remember { mutableStateOf(false) }
    var preparedExport by remember { mutableStateOf<File?>(null) }
    var exporting by remember { mutableStateOf(false) }
    var incoming by remember { mutableStateOf<StagedReferenceEquipment?>(null) }
    var importing by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf<String?>(null) }
    var failure by remember { mutableStateOf(false) }
    val exportForCleanup = preparedExport
    DisposableEffect(exportForCleanup) { onDispose { exportForCleanup?.delete() } }
    val importForCleanup = incoming
    DisposableEffect(importForCleanup) { onDispose { importForCleanup?.close() } }

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

    // Settings MUST use the same self-contained v2 exchange as the equipment
    // card. The old ReferenceEquipmentExchange.encode() saved metadata-only
    // schemaVersion=1, silently dropping PDF/DOCX originals.
    val saveDocument = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream"),
    ) { uri ->
        val completeArchive = preparedExport
        if (uri == null || completeArchive == null) {
            preparedExport = null
        } else scope.launch {
            exporting = true
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openOutputStream(uri)?.use { output ->
                        completeArchive.inputStream().use {
                            it.copyTo(output, 64 * 1024)
                        }
                    } ?: throw IOException("Could not save equipment archive")
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        ReferenceEquipmentPortableV2.stage(input, context.cacheDir).use { staged ->
                            require(!staged.legacyMissingAttachments)
                            val required = staged.equipment.procedures.mapNotNull { procedure ->
                                procedure.attachmentSha256?.let { sha ->
                                    sha to procedure.attachmentMimeType
                                }
                            }.toSet()
                            val actual = staged.originals.map {
                                it.sha256 to it.mimeType
                            }.toSet()
                            require(actual == required) {
                                "Saved archive is missing PDF/Word originals"
                            }
                        }
                    } ?: throw IOException("Could not verify saved archive")
                }
            }
            result.onSuccess {
                failure = false
                status = localized(
                    "Оборудование и оригиналы PDF/Word сохранены в .navref v2. Передай именно новый файл.",
                    "Equipment and PDF/Word originals saved as .navref v2. Share this new file.",
                    lang)
            }.onFailure { error ->
                withContext(Dispatchers.IO) {
                    runCatching {
                        android.provider.DocumentsContract.deleteDocument(
                            context.contentResolver, uri)
                    }
                }
                failure = true
                status = localized(
                    "Не удалось сохранить полный архив с вложениями. ",
                    "Unable to save complete archive with attachments. ",
                    lang) + (error.localizedMessage ?: "")
            }
            preparedExport = null
            exporting = false
        }
    }

    fun prepareExport(item: ReferenceEquipment) {
        if (exporting) return
        exporting = true
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val temp = File.createTempFile("navtasks-equipment-", ".navref", context.cacheDir)
                    try {
                        temp.outputStream().use { output ->
                            ReferenceEquipmentPortableV2.export(
                                item, manualStore.read().documents,
                                manualStore::findOriginal, output)
                        }
                        ReferenceEquipmentPortableV2.stage(
                            temp.inputStream(), context.cacheDir).use { staged ->
                            require(!staged.legacyMissingAttachments)
                            val required = item.procedures.mapNotNull { procedure ->
                                procedure.attachmentSha256?.let { sha ->
                                    sha to procedure.attachmentMimeType
                                }
                            }.toSet()
                            val actual = staged.originals.map {
                                it.sha256 to it.mimeType
                            }.toSet()
                            require(actual == required) {
                                "An attached PDF/Word original was not included"
                            }
                        }
                        temp
                    } catch (error: Throwable) {
                        temp.delete()
                        throw error
                    }
                }
            }
            exporting = false
            result.onSuccess { ready ->
                preparedExport = ready
                val safe = item.title.replace(Regex("[^\\p{L}\\p{N}_-]"), "_")
                    .take(55).ifBlank { "Equipment" }
                saveDocument.launch("${safe}_with_documents_v2.navref")
            }.onFailure { error ->
                failure = true
                status = localized(
                    "Экспорт невозможен: оригинал PDF/Word отсутствует или повреждён. ",
                    "Export blocked: attached PDF/Word original missing or damaged. ",
                    lang) + (error.localizedMessage ?: "")
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
                    } ?: throw IOException("Could not open equipment archive")
                }
            }
            result.onSuccess {
                incoming = it
                status = null
                failure = false
            }.onFailure {
                failure = true
                status = it.localizedMessage
            }
        }
    }

    fun acceptEquipment(staged: StagedReferenceEquipment, replaceSameId: Boolean) {
        if (importing) return
        importing = true
        scope.launch {
            val originals = withContext(Dispatchers.IO) {
                runCatching {
                    for (attachment in staged.originals) {
                        val existing = manualStore.read().documents.firstOrNull {
                            it.sha256 == attachment.sha256 &&
                                it.mimeType == attachment.mimeType
                        }
                        if (existing != null &&
                            runCatching { manualStore.findOriginal(existing) }.isSuccess) continue
                        attachment.file.inputStream().use { input ->
                            val record = manualStore.importOriginal(
                                input, attachment.filename.substringBeforeLast('.'),
                                attachment.filename, attachment.mimeType)
                            require(record.sha256 == attachment.sha256) {
                                "Imported attachment differs from verified original"
                            }
                        }
                    }
                }
            }
            originals.onSuccess {
                val current = catalog
                if (current != null && save(referenceImportEquipment(
                        current, staged.equipment, targetFolderId = null,
                        replaceSameId = replaceSameId))) {
                    incoming = null
                    failure = false
                    status = localized(
                        "Оборудование и вложенные PDF/Word импортированы.",
                        "Equipment and included PDF/Word files imported.", lang)
                }
            }.onFailure { error ->
                failure = true
                status = error.localizedMessage ?: localized(
                    "Ошибка импорта PDF/Word.", "Could not import PDF/Word originals.", lang)
            }
            importing = false
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
                            exportChooser = false
                            prepareExport(item)
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
                    Text(localized("Вложенных файлов: ", "Attached originals: ", lang) +
                        staged.originals.size, color = p.muted)
                    if (staged.legacyMissingAttachments) {
                        Text(localized(
                            "Старый .navref v1: оригиналы PDF/Word отсутствуют. Запроси новый экспорт v2.",
                            "Legacy .navref v1: PDF/Word originals are missing. Request a fresh v2 export.",
                            lang), color = p.systemRed, fontSize = 12.sp)
                    }
                    Text(when (conflict) {
                        ReferenceImportConflict.SAME_ID -> localized(
                            "Запись с таким ID уже существует. Добавить копию или подтвердить замену?",
                            "An item with this ID exists. Add a copy or explicitly replace?",
                            lang)
                        ReferenceImportConflict.POSSIBLE_DUPLICATE -> localized(
                            "Похожее оборудование уже есть. Проверь перед импортом.",
                            "Similar equipment exists. Review before importing.", lang)
                        else -> localized(
                            "Будет добавлено в текущий профиль без изменения задач.",
                            "Will add to this profile without changing tasks.", lang)
                    }, color = p.muted, fontSize = 13.sp)
                    if (conflict == ReferenceImportConflict.SAME_ID) {
                        TextButton(
                            enabled = !importing,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 44.dp),
                            onClick = { acceptEquipment(staged, replaceSameId = true) },
                        ) {
                            Text(localized("Заменить существующую запись", "Replace existing item", lang),
                                color = p.systemRed, fontSize = 14.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(enabled = !importing, onClick = {
                    acceptEquipment(staged, replaceSameId = false)
                }) {
                    Text(localized("Добавить", "Add copy", lang), color = p.blueText)
                }
            },
            dismissButton = {
                TextButton(enabled = !importing, onClick = { incoming = null }) {
                    Text(localized("Отмена", "Cancel", lang), color = p.muted)
                }
                // Destructive replacement stays separately selectable in the
                // dialog body, not stacked on top of the Cancel button.
            },
        )
    }

}
