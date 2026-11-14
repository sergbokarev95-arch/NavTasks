package com.navtasks.app

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException

/** Only edits the independent catalog passed by ReferenceHubScreen. */
@Composable
internal fun ReferenceEquipmentScreen(
    catalog: ReferenceEquipmentCatalog,
    p: NavPalette,
    lang: UiLanguage,
    onBack: () -> Unit,
    persist: (ReferenceEquipmentCatalog) -> Boolean,
    initialQuery: String = "",
) {
    val context = LocalContext.current
    val manualStore = remember(context) { ReferenceManualStore(context) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var folderId by rememberSaveable { mutableStateOf<String?>(null) }
    var equipmentId by rememberSaveable { mutableStateOf<String?>(null) }
    var procedureId by rememberSaveable { mutableStateOf<String?>(null) }
    var searchOpen by rememberSaveable { mutableStateOf(initialQuery.isNotBlank()) }
    var searchText by rememberSaveable { mutableStateOf(initialQuery) }
    val query = if (searchOpen) navEffectiveSearchQuery(searchText) else ""
    var searchIndex by rememberSaveable { mutableIntStateOf(0) }
    var searchSerial by remember { mutableIntStateOf(0) }
    val folder = catalog.folders.firstOrNull { it.id == folderId }
    val equipment = catalog.equipment.firstOrNull { it.id == equipmentId }
    val procedure = equipment?.procedures?.firstOrNull { it.id == procedureId }
    val matches = remember(catalog, folderId, query) {
        referenceEquipmentVisible(catalog, folderId, query)
    }
    val folders = remember(catalog, folderId, query) {
        referenceVisibleFolders(catalog, folderId, query)
    }
    val totalSearchMatches = if (query.isBlank()) 0 else folders.size + matches.size
    var addMenu by remember { mutableStateOf(false) }
    var folderEditor by remember { mutableStateOf<ReferenceFolder?>(null) }
    var newFolder by remember { mutableStateOf(false) }
    var equipmentEditor by remember { mutableStateOf<ReferenceEquipment?>(null) }
    var newEquipment by remember { mutableStateOf(false) }
    var procedureEditor by remember { mutableStateOf<ReferenceProcedure?>(null) }
    var newProcedure by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<Pair<String, String>?>(null) }
    var imported by remember { mutableStateOf<StagedReferenceEquipment?>(null) }
    val stagedForDisposal = imported
    DisposableEffect(stagedForDisposal) {
        onDispose { stagedForDisposal?.close() }
    }
    var importing by remember { mutableStateOf(false) }
    var preparedExport by remember { mutableStateOf<java.io.File?>(null) }
    var exporting by remember { mutableStateOf(false) }
    val exportForDisposal = preparedExport
    DisposableEffect(exportForDisposal) {
        onDispose { exportForDisposal?.delete() }
    }
    var feedback by remember { mutableStateOf<String?>(null) }
    var failure by remember { mutableStateOf(false) }
    var attaching by remember { mutableStateOf(false) }
    var attachmentOwnerId by remember { mutableStateOf<String?>(null) }
    var attachmentProcedureId by remember { mutableStateOf<String?>(null) }
    var attachmentPreview by remember { mutableStateOf<ReferenceManualDocument?>(null) }

    fun navigateBack() {
        when {
            procedureId != null -> procedureId = null
            equipmentId != null -> equipmentId = null
            searchOpen -> { searchOpen = false; searchText = "" }
            folderId != null -> folderId = folder?.parentId
            else -> onBack()
        }
    }
    BackHandler { navigateBack() }

    fun submit(next: ReferenceEquipmentCatalog): Boolean {
        return if (runCatching { next.validate() }.isFailure) {
            failure = true
            feedback = localized("Недопустимые данные справочника.", "Invalid reference data.", lang)
            false
        } else {
            val saved = persist(next)
            if (!saved) {
                failure = true
                feedback = localized("Не удалось сохранить. Данные не изменены.",
                    "Could not save. Your data was not changed.", lang)
            } else {
                failure = false
                feedback = null
            }
            saved
        }
    }

    val attachDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        val ownerId = attachmentOwnerId
        val procId = attachmentProcedureId
        if (uri != null && ownerId != null && procId != null && !attaching) {
            scope.launch {
                attaching = true
                val importedFile = withContext(Dispatchers.IO) {
                    runCatching {
                        val filename = context.contentResolver.query(
                            uri, null, null, null, null,
                        )?.use { cursor ->
                            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            if (cursor.moveToFirst() && index >= 0)
                                cursor.getString(index) else null
                        } ?: throw IOException("Cannot read document name")
                        val safeFilename = filename.substringAfterLast('/')
                            .substringAfterLast('\\').trim()
                        require(safeFilename.isNotBlank() && safeFilename.length <= 240)
                        val mime = when {
                            safeFilename.endsWith(".pdf", ignoreCase = true) ->
                                ReferenceManualMime.PDF
                            safeFilename.endsWith(".docx", ignoreCase = true) ->
                                ReferenceManualMime.DOCX
                            else -> throw IllegalArgumentException("Only PDF/DOCX can be attached")
                        }
                        context.contentResolver.openInputStream(uri)?.use { input ->
                            manualStore.importOriginal(
                                input, safeFilename.substringBeforeLast('.'),
                                safeFilename, mime, NavReferenceSections.PERSONAL,
                            )
                        } ?: throw IOException("Cannot open selected document")
                    }
                }
                importedFile.onSuccess { doc ->
                    val currentOwner = catalog.equipment.firstOrNull { it.id == ownerId }
                    val currentProcedure = currentOwner?.procedures?.firstOrNull {
                        it.id == procId
                    }
                    if (currentOwner != null && currentProcedure != null &&
                        submit(referenceSaveEquipment(catalog,
                            referenceSaveProcedure(currentOwner, currentProcedure.copy(
                                attachmentSha256 = doc.sha256,
                                attachmentMimeType = doc.mimeType,
                                attachmentFilename = doc.originalFilename,
                            ))))) {
                        failure = false
                        feedback = localized(
                            "Документ прикреплён. Оригинал сохранён в «Моих материалах».",
                            "Document attached. Original saved in My Materials.", lang)
                    } else if (currentProcedure == null) {
                        failure = true
                        feedback = localized("Процедура больше не существует.",
                            "Procedure no longer exists.", lang)
                    }
                }.onFailure {
                    failure = true
                    feedback = it.localizedMessage ?: localized("Не удалось прикрепить файл.",
                        "Could not attach file.", lang)
                }
                attaching = false
                attachmentOwnerId = null
                attachmentProcedureId = null
            }
        }
    }

    // CreateDocument can create a new (initially empty) SAF file as soon as the
    // chooser finishes. Prepare and verify the COMPLETE v2 archive first:
    // otherwise a missing PDF can leave only a manifest at the destination.
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream"),
    ) { uri ->
        val verified = preparedExport
        if (uri == null || verified == null) {
            preparedExport = null
        } else scope.launch {
            exporting = true
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openOutputStream(uri, "wt")?.use { output ->
                        verified.inputStream().use { it.copyTo(output, 64 * 1024) }
                    } ?: throw IOException("Cannot save equipment package")
                    // Validate the SAF provider's FINAL bytes too. This prevents
                    // reporting success if a cloud/file provider truncated them.
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        ReferenceEquipmentPortableV2.stage(input, context.cacheDir).use {
                            require(!it.legacyMissingAttachments) {
                                "Saved an incomplete legacy equipment package"
                            }
                            require(it.equipment.procedures.count { p ->
                                p.attachmentSha256 != null
                            } >= it.originals.size)
                        }
                    } ?: throw IOException("Cannot verify the saved equipment package")
                }
            }
            result.onSuccess {
                failure = false
                feedback = localized(
                    "Оборудование сохранено в .navref v2 вместе с оригиналами PDF/Word. Отправляй именно новый файл.",
                    "Saved .navref v2 including the original PDF/Word files. Share this new file.",
                    lang)
            }.onFailure { error ->
                // Only the document just created by our SAF picker is a cleanup
                // candidate. Never delete an unrelated file from the library.
                withContext(Dispatchers.IO) {
                    runCatching {
                        android.provider.DocumentsContract.deleteDocument(
                            context.contentResolver, uri)
                    }
                }
                failure = true
                feedback = localized(
                    "Не удалось сохранить ПОЛНЫЙ архив. Проверь оригиналы PDF/Word в «Моих материалах». ",
                    "Unable to save the COMPLETE package. Check the PDF/Word originals in My Materials. ",
                    lang) + (error.localizedMessage ?: "")
            }
            preparedExport = null
            exporting = false
        }
    }

    fun prepareEquipmentExport(item: ReferenceEquipment) {
        if (exporting) return
        exporting = true
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val stagedFile = java.io.File.createTempFile(
                        "navtasks-equipment-", ".navref", context.cacheDir)
                    try {
                        stagedFile.outputStream().use { output ->
                            ReferenceEquipmentPortableV2.export(
                                item, manualStore.read().documents,
                                manualStore::findOriginal, output)
                        }
                        ReferenceEquipmentPortableV2.stage(
                            stagedFile.inputStream(), context.cacheDir).use { staged ->
                            require(!staged.legacyMissingAttachments)
                            require(staged.originals.size ==
                                item.procedures.mapNotNull { it.attachmentSha256 }.distinct().size) {
                                "Not every attached PDF/Word is included"
                            }
                            require(staged.equipment.id == item.id)
                        }
                        stagedFile
                    } catch (error: Throwable) {
                        stagedFile.delete()
                        throw error
                    }
                }
            }
            exporting = false
            result.onSuccess { ready ->
                preparedExport = ready
                val safe = item.title.replace(Regex("[^\\p{L}\\p{N}_-]"), "_")
                    .take(55).ifBlank { "Equipment" }
                // Distinct name prevents confusion with a legacy v1 file
                // still sitting in the Downloads folder.
                exportLauncher.launch("${safe}_with_documents_v2.navref")
            }.onFailure { error ->
                failure = true
                feedback = localized(
                    "Экспорт отменён: один из прикреплённых PDF/Word недоступен или повреждён. ",
                    "Export cancelled: an attached PDF/Word original is missing or damaged. ",
                    lang) + (error.localizedMessage ?: "")
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) scope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        ReferenceEquipmentPortableV2.stage(input, context.cacheDir)
                    } ?: throw IOException("Cannot open equipment package")
                }
            }
            result.onSuccess {
                imported = it
                failure = false
                feedback = null
            }.onFailure {
                failure = true
                feedback = it.localizedMessage ?: localized(
                    "Неверный или повреждённый файл.", "Invalid or corrupted file.", lang)
            }
        }
    }

    LaunchedEffect(query) { searchIndex = 0 }
    LaunchedEffect(searchSerial) {
        if (searchSerial > 0 && totalSearchMatches > 0 && equipment == null) {
            listState.animateScrollToItem(
                searchIndex.coerceIn(0, totalSearchMatches - 1))
        }
    }
    val sectionTitle = when {
        procedure != null -> procedure.title
        equipment != null -> equipment.title
        folder != null -> folder.title
        else -> localized("Оборудование", "Equipment", lang)
    }
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NavGlassCircleButton(p, "back") { navigateBack() }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(localized("Справочник", "Reference", lang),
                    color = p.blueText, fontSize = 13.sp)
                Text(sectionTitle, color = p.text, fontSize = 20.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (procedure == null) {
                Box {
                    NavGlassCircleButton(p, "plus") { addMenu = true }
                    NavPopoverMenu(p, expanded = addMenu, onDismissRequest = { addMenu = false }) {
                        if (equipment == null) {
                            NavPopoverAction(localized("Добавить папку", "New folder", lang), p) {
                                addMenu = false
                                newFolder = true
                            }
                            NavPopoverAction(localized("Добавить оборудование", "Add equipment", lang), p) {
                                addMenu = false
                                newEquipment = true
                            }
                            NavPopoverDivider(p)
                            NavPopoverAction(localized("Импорт оборудования", "Import equipment", lang), p) {
                                addMenu = false
                                importLauncher.launch(arrayOf("*/*"))
                            }
                        } else {
                            NavPopoverAction(localized("Добавить процедуру", "Add procedure", lang), p) {
                                addMenu = false
                                newProcedure = true
                            }
                            NavPopoverAction(localized("Редактировать", "Edit equipment", lang), p) {
                                addMenu = false
                                equipmentEditor = equipment
                            }
                            NavPopoverAction(localized("Экспортировать", "Export equipment", lang), p) {
                                addMenu = false
                                prepareEquipmentExport(equipment)
                            }
                        }
                    }
                }
            } else {
                NavGlassCircleButton(p, "edit") { procedureEditor = procedure }
            }
        }
        if (feedback != null) {
            Text(feedback.orEmpty(), modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 6.dp),
                color = if (failure) p.systemRed else p.blueText, fontSize = 12.sp)
        }
        when {
            procedure != null -> {
                ReferenceProcedureReader(
                    procedure, p, lang,
                    attaching = attaching,
                    onEdit = { procedureEditor = procedure },
                    onDelete = { deleteTarget = "procedure" to procedure.id },
                    onAttach = {
                        if (!attaching) {
                            val owner = equipment
                            if (owner != null) {
                                attachmentOwnerId = owner.id
                                attachmentProcedureId = procedure.id
                                attachDocumentLauncher.launch(arrayOf(
                                    ReferenceManualMime.PDF, ReferenceManualMime.DOCX))
                            }
                        }
                    },
                    onOpenAttachment = {
                        val result = runCatching {
                            manualStore.read().documents.firstOrNull { manual ->
                                manual.sha256 == procedure.attachmentSha256 &&
                                    manual.mimeType == procedure.attachmentMimeType
                            } ?: throw IOException("Manual original is missing. Restore .navlib backup.")
                        }
                        result.onSuccess { manual ->
                            failure = false
                            feedback = null
                            if (manual.mimeType == ReferenceManualMime.PDF &&
                                navNativePdfSupported()) {
                                runCatching {
                                    context.startActivity(ReferencePdfActivity.intent(context, manual))
                                }.onFailure {
                                    attachmentPreview = manual
                                    failure = true
                                    feedback = it.localizedMessage
                                }
                            } else attachmentPreview = manual
                        }.onFailure { failure = true; feedback = it.localizedMessage }
                    },
                    onDetach = {
                        val owner = equipment
                        if (owner != null && submit(referenceSaveEquipment(catalog,
                                referenceSaveProcedure(owner, procedure.copy(
                                    attachmentSha256 = null,
                                    attachmentMimeType = null,
                                    attachmentFilename = "",
                                ))))) {
                            feedback = localized("Связь с документом убрана. Сам файл сохранён.",
                                "Attachment link removed; original file retained.", lang)
                        }
                    },
                )
            }
            equipment != null -> {
                ReferenceEquipmentDetails(
                    equipment, p, lang,
                    onOpenProcedure = { procedureId = it },
                    onEdit = { equipmentEditor = equipment },
                    onDelete = { deleteTarget = "equipment" to equipment.id },
                    onExport = {
                        prepareEquipmentExport(equipment)
                    },
                    onAddProcedure = { newProcedure = true },
                )
            }
            else -> {
                if (folder != null) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Text(localized("Раздел оборудования", "Equipment folder", lang),
                            color = p.muted, fontSize = 12.sp, modifier = Modifier.weight(1f))
                        TextButton(onClick = { folderEditor = folder }) {
                            Text(localized("Переименовать", "Rename", lang),
                                color = p.blueText, fontSize = 12.sp)
                        }
                        TextButton(onClick = { deleteTarget = "folder" to folder.id }) {
                            Text(localized("Удалить", "Delete", lang),
                                color = p.systemRed, fontSize = 12.sp)
                        }
                    }
                }
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.weight(1f)) {
                        NavInfoSectionBanner(p,
                            title = localized("Оборудование и инструкции",
                                "Equipment and procedures", lang),
                            description = localized("Только справочные материалы. Без задач и уведомлений.",
                                "Reference only. No tasks or reminders.", lang),
                            modifier = Modifier.fillMaxWidth())
                    }
                    Spacer(Modifier.width(8.dp))
                    NavGlassCircleButton(p, "search") {
                        searchOpen = !searchOpen
                        if (!searchOpen) searchText = ""
                    }
                }
                NavAnimatedSearchField(
                    searchOpen = searchOpen,
                    value = searchText, onValueChange = { searchText = it },
                    onDismiss = { searchOpen = false; searchText = "" },
                    p = p, lang = lang,
                    placeholder = localized("Поиск по оборудованию и инструкциям",
                        "Search equipment and procedures", lang),
                    resultCount = totalSearchMatches,
                    currentResult = searchIndex,
                    onPreviousResult = {
                        searchIndex = navSearchNextIndex(searchIndex, totalSearchMatches, -1)
                        searchSerial++
                    },
                    onNextResult = {
                        searchIndex = navSearchNextIndex(searchIndex, totalSearchMatches, 1)
                        searchSerial++
                    },
                )
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                        .dismissSearchImeOnResultsGesture(searchOpen)
                        .revealSearchOnSettledTopPull(listState, searchOpen) {
                            searchOpen = true
                        },
                    state = listState,
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp,
                        top = 6.dp, bottom = 125.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (folders.isEmpty() && matches.isEmpty()) {
                        item {
                            NavPanel(p) {
                                Text(if (query.isBlank())
                                    localized("Пока пусто. Нажми «+», чтобы добавить папку или оборудование.",
                                        "No equipment yet. Tap + to add a folder or device.", lang)
                                else localized("Ничего не найдено.", "No matches found.", lang),
                                    color = p.muted, fontSize = 13.sp)
                            }
                        }
                    }
                    items(folders, key = { "folder-" + it.id }) { item ->
                        ReferenceBrowseRow(item.title, localized("Папка", "Folder", lang),
                            p, query, onClick = {
                                folderId = item.id
                                searchText = ""; searchOpen = false
                            }, onEdit = { folderEditor = item })
                    }
                    items(matches, key = { "equipment-" + it.id }) { item ->
                        val preview = referenceEquipmentSearchPreview(item, query)
                        ReferenceBrowseRow(item.title, preview, p, query,
                            onClick = { equipmentId = item.id; searchOpen = false },
                            onEdit = { equipmentEditor = item })
                    }
                }
            }
        }
    }

    if (newFolder || folderEditor != null) {
        ReferenceFolderEditor(folderEditor, folderId, p, lang,
            onDismiss = { newFolder = false; folderEditor = null },
            onSave = { updated ->
                if (submit(referenceSaveFolder(catalog, updated))) {
                    newFolder = false; folderEditor = null
                }
            })
    }
    if (newEquipment || equipmentEditor != null) {
        ReferenceEquipmentEditor(equipmentEditor, folderId, p, lang,
            onDismiss = { newEquipment = false; equipmentEditor = null },
            onSave = { updated ->
                if (submit(referenceSaveEquipment(catalog, updated))) {
                    newEquipment = false; equipmentEditor = null
                }
            })
    }
    attachmentPreview?.let { manual ->
        Dialog(
            onDismissRequest = { attachmentPreview = null },
            properties = DialogProperties(usePlatformDefaultWidth = false,
                dismissOnClickOutside = false, decorFitsSystemWindows = false),
        ) {
            Column(Modifier.fillMaxSize().background(p.background).safeDrawingPadding()) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    NavGlassCircleButton(p, "back") { attachmentPreview = null }
                    Spacer(Modifier.width(12.dp))
                    Text(manual.originalFilename, color = p.text,
                        fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f))
                    ReferenceManualOpenWithButton(
                        document = manual, store = manualStore, p = p, lang = lang)
                }
                ReferenceManualReader(manual, manualStore, p, lang)
            }
        }
    }

    if (newProcedure || procedureEditor != null) {
        ReferenceProcedureEditor(procedureEditor, p, lang,
            onDismiss = { newProcedure = false; procedureEditor = null },
            onSave = { updated ->
                val owner = equipment
                if (owner != null && submit(referenceSaveEquipment(catalog,
                        referenceSaveProcedure(owner, updated)))) {
                    newProcedure = false; procedureEditor = null
                }
            })
    }
    deleteTarget?.let { (kind, id) ->
        NavGlassAlertDialog(p, onDismissRequest = { deleteTarget = null },
            title = { Text(localized("Удалить запись?", "Delete item?", lang)) },
            text = { Text(localized(
                "Удаление нельзя отменить. Сначала экспортируй нужные материалы.",
                "Deletion cannot be undone. Export important materials first.", lang)) },
            confirmButton = {
                TextButton(onClick = {
                    val updated = runCatching {
                        when (kind) {
                            "procedure" -> {
                                val owner = equipment ?: error("Equipment is missing")
                                referenceSaveEquipment(catalog, referenceRemoveProcedure(owner, id))
                            }
                            "equipment" -> referenceDeleteEquipment(catalog, id)
                            "folder" -> referenceDeleteEmptyFolder(catalog, id)
                            else -> error("Unknown reference delete operation")
                        }
                    }.onFailure {
                        failure = true; feedback = it.localizedMessage
                    }.getOrNull()
                    if (updated != null && submit(updated)) {
                        if (kind == "procedure") procedureId = null
                        if (kind == "equipment") equipmentId = null
                        if (kind == "folder") folderId = folder?.parentId
                        deleteTarget = null
                    }
                }) { Text(localized("Удалить", "Delete", lang), color = p.systemRed) }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text(localized("Отмена", "Cancel", lang), color = p.muted)
                }
            })
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
                            val title = attachment.filename.substringBeforeLast('.')
                            val record = manualStore.importOriginal(input, title,
                                attachment.filename, attachment.mimeType)
                            require(record.sha256 == attachment.sha256) {
                                "Imported attachment differs from verified archive"
                            }
                        }
                    }
                }
            }
            originals.onSuccess {
                if (submit(referenceImportEquipment(catalog, staged.equipment,
                        folderId, replaceSameId = replaceSameId))) {
                    imported = null
                    feedback = localized(
                        "Оборудование и вложенные документы добавлены.",
                        "Equipment and attached documents imported.", lang)
                }
            }.onFailure {
                failure = true
                feedback = it.localizedMessage ?: localized(
                    "Не удалось восстановить вложения.", "Could not restore attachments.", lang)
            }
            importing = false
        }
    }

    imported?.let { staged ->
        val candidate = staged.equipment
        val conflict = referenceImportConflict(candidate, catalog.equipment)
        NavGlassAlertDialog(p, onDismissRequest = { if (!importing) imported = null },
            title = { Text(localized("Импорт оборудования", "Import equipment", lang)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Text(candidate.title, color = p.text, fontWeight = FontWeight.Medium)
                    Text(localized("Процедур: ", "Procedures: ", lang) +
                        candidate.procedures.size, color = p.muted)
                    Text(localized("Документов внутри файла: ", "Files included: ", lang) +
                        staged.originals.size, color = p.muted, fontSize = 12.sp)
                    if (staged.legacyMissingAttachments) Text(localized(
                        "Старый формат: PDF/Word в этом файле нет. Попроси отправить оборудование заново из обновлённой версии.",
                        "Legacy file: the PDF/Word original was not included. Request a new export.",
                        lang), color = p.systemRed, fontSize = 12.sp)
                    Text(when (conflict) {
                        ReferenceImportConflict.SAME_ID -> localized(
                            "Запись уже существует: можно добавить копию или явно заменить.",
                            "Existing record: add a copy or explicitly replace.", lang)
                        ReferenceImportConflict.POSSIBLE_DUPLICATE -> localized(
                            "Оборудование с таким именем и моделью уже существует.",
                            "Equipment with this name and model already exists.", lang)
                        else -> localized("Существующие записи не будут затронуты.",
                            "Existing records will not be changed.", lang)
                    }, color = p.muted, fontSize = 13.sp)
                }
            },
            confirmButton = {
                TextButton(enabled = !importing, onClick = {
                    acceptEquipment(staged, replaceSameId = false)
                }) { Text(localized("Добавить копию", "Add copy", lang), color = p.blueText) }
            },
            dismissButton = {
                TextButton(enabled = !importing, onClick = { imported = null }) {
                    Text(localized("Отмена", "Cancel", lang), color = p.muted)
                }
                if (conflict == ReferenceImportConflict.SAME_ID) {
                    TextButton(enabled = !importing, onClick = {
                        acceptEquipment(staged, replaceSameId = true)
                    }) { Text(localized("Заменить", "Replace", lang), color = p.systemRed) }
                }
            },
        )
    }
}

@Composable
private fun ReferenceBrowseRow(
    title: String, subtitle: String,
    p: NavPalette, query: String, onClick: () -> Unit, onEdit: () -> Unit,
) {
    Row(Modifier.fillMaxWidth().navGlass(p, radius = 17.dp, role = GlassRole.TASK)
        .padding(horizontal = 13.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Row(Modifier.weight(1f).glassClickable(onClick).padding(vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically) {
            NavGlassGlyph("reference", p.blueText, Modifier.size(25.dp))
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                NavSearchMarkedText(title, query, p, fontSize = 15.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                NavSearchMarkedText(subtitle, query, p, color = p.muted,
                    fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            NavGlassGlyph("chevron-right", p.muted, Modifier.size(17.dp))
        }
        Spacer(Modifier.width(6.dp))
        Box(Modifier.size(42.dp).glassClickable(onEdit),
            contentAlignment = Alignment.Center) {
            NavGlassGlyph("edit", p.muted, Modifier.size(20.dp))
        }
    }
}

@Composable
private fun ReferenceEquipmentDetails(
    equipment: ReferenceEquipment, p: NavPalette, lang: UiLanguage,
    onOpenProcedure: (String) -> Unit, onEdit: () -> Unit,
    onDelete: () -> Unit, onExport: () -> Unit, onAddProcedure: () -> Unit,
) {
    LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        .let { PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 125.dp) },
        verticalArrangement = Arrangement.spacedBy(11.dp)) {
        item {
            NavPanel(p) {
                if (equipment.manufacturer.isNotBlank() || equipment.model.isNotBlank())
                    Text(listOf(equipment.manufacturer, equipment.model)
                        .filter { it.isNotBlank() }.joinToString(" · "),
                        color = p.text, fontSize = 15.sp)
                if (equipment.location.isNotBlank())
                    Text(localized("Расположение: ", "Location: ", lang) + equipment.location,
                        color = p.muted, fontSize = 13.sp)
                if (equipment.serialNumber.isNotBlank())
                    Text(localized("Серийный номер: ", "Serial: ", lang) + equipment.serialNumber,
                        color = p.muted, fontSize = 12.sp)
                if (equipment.notes.isNotBlank())
                    Text(equipment.notes, color = p.text, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    TextButton(onClick = onEdit) {
                        Text(localized("Изменить", "Edit", lang), color = p.blueText)
                    }
                    TextButton(onClick = onExport) {
                        Text(localized("Экспорт", "Export", lang), color = p.blueText)
                    }
                    TextButton(onClick = onDelete) {
                        Text(localized("Удалить", "Delete", lang), color = p.systemRed)
                    }
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(localized("Процедуры и выдержки", "Procedures and excerpts", lang),
                    color = p.text, fontSize = 17.sp, fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f))
                TextButton(onClick = onAddProcedure) {
                    Text(localized("+ Добавить", "+ Add", lang), color = p.blueText)
                }
            }
        }
        if (equipment.procedures.isEmpty()) item {
            NavPanel(p) {
                Text(localized("Добавь выдержку из мануала, чтобы сохранить порядок работ.",
                    "Add a cited manual excerpt to describe the procedure.", lang),
                    color = p.muted, fontSize = 13.sp)
            }
        }
        items(equipment.procedures, key = { it.id }) { procedure ->
            Column(Modifier.fillMaxWidth()
                .navGlass(p, radius = 17.dp, role = GlassRole.TASK)
                .glassClickable { onOpenProcedure(procedure.id) }
                .padding(15.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(procedure.title, color = p.text, fontSize = 15.sp,
                        modifier = Modifier.weight(1f))
                    NavGlassGlyph("chevron-right", p.muted, Modifier.size(18.dp))
                }
                if (procedure.intervalDescription.isNotBlank())
                    Text(procedure.intervalDescription, color = p.blueText, fontSize = 12.sp)
                Text(procedure.source.documentTitle +
                    (procedure.source.page?.let { " · p. $it" } ?: ""),
                    color = p.muted, fontSize = 12.sp, maxLines = 2)
            }
        }
    }
}

@Composable
private fun ReferenceProcedureReader(
    procedure: ReferenceProcedure, p: NavPalette, lang: UiLanguage,
    attaching: Boolean,
    onEdit: () -> Unit, onDelete: () -> Unit,
    onAttach: () -> Unit, onOpenAttachment: () -> Unit, onDetach: () -> Unit,
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())
        .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 125.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        NavPanel(p) {
            if (procedure.intervalDescription.isNotBlank())
                Text(localized("По инструкции: ", "As specified: ", lang) +
                    procedure.intervalDescription, color = p.blueText, fontSize = 13.sp)
            Text(localized("Источник", "Source", lang), color = p.muted,
                fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Text(procedure.source.documentTitle, color = p.text, fontSize = 15.sp)
            val sourceDetails = listOfNotNull(
                procedure.source.section.takeIf { it.isNotBlank() }?.let {
                    localized("Раздел ", "Section ", lang) + it },
                procedure.source.page?.let { localized("Стр. ", "Page ", lang) + it },
                procedure.source.edition.takeIf { it.isNotBlank() },
            ).joinToString(" · ")
            if (sourceDetails.isNotBlank())
                Text(sourceDetails, color = p.muted, fontSize = 12.sp)
        }
        NavPanel(p) {
            Text(localized("Выдержка из мануала", "Manual excerpt", lang),
                color = p.blueText, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text(procedure.excerpt, color = p.text, fontSize = 15.sp, lineHeight = 23.sp)
        }
        if (procedure.instructions.isNotBlank()) NavPanel(p) {
            Text(localized("Порядок выполнения / примечания", "Steps / notes", lang),
                color = p.blueText, fontSize = 13.sp)
            Text(procedure.instructions, color = p.text, fontSize = 14.sp, lineHeight = 22.sp)
        }
        NavPanel(p) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NavGlassGlyph("paperclip", p.blueText, Modifier.size(22.dp))
                Text(localized("Прикреплённый документ", "Attached document", lang),
                    color = p.blueText, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
            if (procedure.attachmentSha256 == null) {
                Text(localized(
                    "PDF или Word будет храниться офлайн вместе с библиотекой мануалов.",
                    "PDF or Word will be saved offline in the shared manual library.", lang),
                    color = p.muted, fontSize = 12.sp)
                TextButton(enabled = !attaching, onClick = onAttach) {
                    Text(localized("Прикрепить файл", "Attach file", lang),
                        color = p.blueText)
                }
            } else {
                Text(procedure.attachmentFilename, color = p.text, fontSize = 14.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = onOpenAttachment) {
                        Text(localized("Открыть", "Open", lang), color = p.blueText)
                    }
                    TextButton(enabled = !attaching, onClick = onAttach) {
                        Text(localized("Заменить", "Replace", lang), color = p.blueText)
                    }
                    TextButton(enabled = !attaching, onClick = onDetach) {
                        Text(localized("Убрать", "Unlink", lang), color = p.systemRed)
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            TextButton(onClick = onEdit) {
                Text(localized("Редактировать", "Edit", lang), color = p.blueText)
            }
            TextButton(onClick = onDelete) {
                Text(localized("Удалить", "Delete", lang), color = p.systemRed)
            }
        }
    }
}
