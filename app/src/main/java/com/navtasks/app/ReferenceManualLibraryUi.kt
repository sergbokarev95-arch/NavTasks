package com.navtasks.app

import android.provider.OpenableColumns
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.draw.blur
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

/** Shared between ALL NavTasks profiles. Never invokes TaskStore or work scheduling. */
@Composable
internal fun ReferenceManualLibraryScreen(
    sectionId: String,
    p: NavPalette,
    lang: UiLanguage,
    onBack: () -> Unit,
    initialQuery: String = "",
) {
    val context = LocalContext.current
    val store = remember(context) { ReferenceManualStore(context) }
    val loaded = remember(store) { runCatching { store.read() } }
    var catalog by remember(store) { mutableStateOf(loaded.getOrNull()) }
    val scope = rememberCoroutineScope()
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    var selectedSearchResult by remember { mutableIntStateOf(0) }
    var busy by remember { mutableStateOf(false) }
    var feedback by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf(false) }
    var folderId by rememberSaveable(sectionId) { mutableStateOf<String?>(null) }
    var documentId by rememberSaveable(sectionId) { mutableStateOf<String?>(null) }
    var excerptId by rememberSaveable(sectionId) { mutableStateOf<String?>(null) }
    var search by rememberSaveable(sectionId) { mutableStateOf(initialQuery) }
    var showSearch by rememberSaveable(sectionId) { mutableStateOf(initialQuery.isNotBlank()) }
    var menuOpen by remember { mutableStateOf(false) }
    var folderEdit by remember { mutableStateOf<ReferenceManualFolder?>(null) }
    var newFolder by remember { mutableStateOf(false) }
    var newExcerpt by remember { mutableStateOf(false) }
    var editingExcerpt by remember { mutableStateOf<ReferenceManualExcerpt?>(null) }
    var deleteExcerpt by remember { mutableStateOf<ReferenceManualExcerpt?>(null) }
    var folderName by remember { mutableStateOf("") }
    var deleteDocument by remember { mutableStateOf<ReferenceManualDocument?>(null) }
    var deleteFolder by remember { mutableStateOf<ReferenceManualFolder?>(null) }
    var stagedImport by remember { mutableStateOf<StagedManualArchive?>(null) }
    // During restore, its coroutine owns the staged files until the copy finishes.
    // Disposal of the dialog must not delete originals while they are being read.
    var restoringArchive by remember { mutableStateOf(false) }

    DisposableEffect(stagedImport) {
        val stage = stagedImport
        onDispose { if (!restoringArchive) stage?.close() }
    }

    fun finish(result: Result<ReferenceManualCatalog>, success: String) {
        busy = false
        result.onSuccess {
            catalog = it
            error = false
            feedback = success
        }.onFailure {
            error = true
            feedback = it.localizedMessage ?: localized(
                "Не удалось выполнить действие.", "Operation failed.", lang)
        }
    }

    val addDocument = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null && !busy && catalog != null) {
            scope.launch {
                busy = true
                val activeFolder = folderId
                val result = withContext(Dispatchers.IO) {
                    runCatching {
                        val name = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            if (cursor.moveToFirst() && index >= 0) cursor.getString(index) else null
                        } ?: throw IOException("Could not read document name")
                        val cleanName = name.substringAfterLast('/').substringAfterLast('\\').trim()
                        require(cleanName.isNotBlank() && cleanName.length <= 240)
                        val mime = when {
                            cleanName.endsWith(".pdf", ignoreCase = true) ->
                                ReferenceManualMime.PDF
                            cleanName.endsWith(".docx", ignoreCase = true) ->
                                ReferenceManualMime.DOCX
                            else -> throw IllegalArgumentException("Only PDF and DOCX are supported")
                        }
                        context.contentResolver.openInputStream(uri)?.use { source ->
                            store.importOriginal(
                                source,
                                cleanName.substringBeforeLast('.').ifBlank { cleanName },
                                cleanName, mime, sectionId, activeFolder,
                            )
                        } ?: throw IOException("Could not open document")
                        store.read()
                    }
                }
                finish(result, localized("Документ сохранён в офлайн-библиотеке.",
                    "Document saved in the offline library.", lang))
            }
        }
    }

    val saveArchive = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream"),
    ) { uri ->
        if (uri != null && !busy && catalog != null) {
            scope.launch {
                busy = true
                val result = withContext(Dispatchers.IO) {
                    runCatching {
                        context.contentResolver.openOutputStream(uri, "w")?.use { output ->
                            store.exportArchive(output)
                        } ?: throw IOException("Could not write library archive")
                        store.read()
                    }
                }
                finish(result, localized("Архив библиотеки записан с оригиналами файлов.",
                    "Full manual archive exported with original files.", lang))
            }
        }
    }

    val openArchive = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null && !busy && catalog != null) {
            scope.launch {
                busy = true
                val result = withContext(Dispatchers.IO) {
                    runCatching {
                        context.contentResolver.openInputStream(uri)?.use { source ->
                            ReferenceManualArchive.stage(
                                source, java.io.File(context.noBackupFilesDir, "navtasks-reference/staging"))
                        } ?: throw IOException("Could not open library archive")
                    }
                }
                busy = false
                result.onSuccess { stagedImport = it; error = false; feedback = null }
                    .onFailure { error = true; feedback = it.localizedMessage }
            }
        }
    }

    val current = catalog
    val activeFolder = current?.folders?.firstOrNull { it.id == folderId }
    val selectedDocument = current?.documents?.firstOrNull { it.id == documentId }
    val selectedExcerpt = current?.excerpts?.firstOrNull { it.id == excerptId }
    val q = if (showSearch) navEffectiveSearchQuery(search) else ""
    val folders = current?.folders.orEmpty().filter {
        if (q.isBlank()) it.parentId == folderId && it.sectionId == sectionId
        else it.sectionId == sectionId && it.title.contains(q, ignoreCase = true)
    }.sortedBy { it.title.lowercase() }
    val documents = current?.documents.orEmpty().filter {
        if (q.isBlank()) it.folderId == folderId && it.sectionId == sectionId
        else it.sectionId == sectionId && (
            it.title.contains(q, ignoreCase = true) ||
            it.originalFilename.contains(q, ignoreCase = true) ||
            it.notes.contains(q, ignoreCase = true))
    }.sortedBy { it.title.lowercase() }

    val excerpts = current?.excerpts.orEmpty().filter {
        if (q.isBlank()) it.folderId == folderId && it.sectionId == sectionId
        else it.sectionId == sectionId && referenceExcerptMatches(it, q)
    }.sortedBy { it.title.lowercase() }
    val totalSearchResults = if (q.isBlank()) 0 else
        folders.size + documents.size + excerpts.size
    LaunchedEffect(q, folderId) { selectedSearchResult = 0 }
    fun moveSearchResult(direction: Int) {
        if (totalSearchResults <= 0) return
        selectedSearchResult = navSearchNextIndex(
            selectedSearchResult, totalSearchResults, direction)
        scope.launch { listState.animateScrollToItem(selectedSearchResult) }
    }

    fun navigateBack() {
        if (busy || restoringArchive) return
        when {
            documentId != null -> documentId = null
            excerptId != null -> excerptId = null
            showSearch -> { showSearch = false; search = "" }
            folderId != null -> folderId = activeFolder?.parentId
            else -> onBack()
        }
    }
    BackHandler { navigateBack() }

    val modalOpen = menuOpen || newFolder || newExcerpt || deleteFolder != null ||
        deleteDocument != null || deleteExcerpt != null || stagedImport != null
    Column(Modifier.fillMaxSize().blur(if (modalOpen) 4.dp else 0.dp)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically) {
            ReferenceManualHeaderAction(p, "back", localized("Назад", "Back", lang)) { navigateBack() }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(selectedExcerpt?.title ?: selectedDocument?.title ?: activeFolder?.title ?: when (sectionId) {
                    NavReferenceSections.SIRE -> "SIRE 2.0"
                    NavReferenceSections.VIQ7 -> "VIQ7"
                    else -> localized("Мои материалы", "My materials", lang)
                }, color = p.text, fontSize = 20.sp, maxLines = 1,
                    overflow = TextOverflow.Ellipsis)
            }
            if (selectedExcerpt != null) {
                ReferenceManualHeaderAction(p, "edit", localized("Изменить", "Edit", lang)) {
                    if (!busy) { editingExcerpt = selectedExcerpt; newExcerpt = true }
                }
            } else if (selectedDocument == null && current != null) {
                Box {
                    ReferenceManualHeaderAction(p, "plus", localized("Добавить", "Add", lang)) { if (!busy) menuOpen = true }
                    NavPopoverMenu(p, expanded = menuOpen,
                        onDismissRequest = { menuOpen = false }, neutralStyle = true) {
                        NavPopoverAction(localized("Добавить PDF / Word",
                            "Import PDF / Word", lang), p) {
                            menuOpen = false
                            addDocument.launch(arrayOf(
                                ReferenceManualMime.PDF, ReferenceManualMime.DOCX))
                        }
                        NavPopoverAction(localized("Новая выдержка", "New excerpt", lang), p) {
                            menuOpen = false
                            editingExcerpt = null
                            newExcerpt = true
                        }
                        NavPopoverAction(localized("Новая папка", "New folder", lang), p) {
                            menuOpen = false; folderName = ""; folderEdit = null; newFolder = true
                        }
                        NavPopoverDivider(p)
                        NavPopoverAction(localized("Сохранить всю библиотеку",
                            "Back up entire library", lang), p) {
                            menuOpen = false
                            saveArchive.launch("NavTasks_Manuals.navlib")
                        }
                        NavPopoverAction(localized("Восстановить библиотеку",
                            "Restore entire library", lang), p) {
                            menuOpen = false; openArchive.launch(arrayOf("*/*"))
                        }
                    }
                }
                Spacer(Modifier.width(7.dp))
                ReferenceManualHeaderAction(p, "search", localized("Поиск", "Search", lang)) {
                    if (!busy) {
                        showSearch = !showSearch
                        if (!showSearch) search = ""
                    }
                }
            }
        }

        if (busy) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                color = NavTodayVisualTokens.accentInk(p.dark))
        }
        if (feedback != null) {
            Text(feedback.orEmpty(), color = if (error) p.systemRed else NavTodayVisualTokens.accentInk(p.dark),
                modifier = Modifier.padding(horizontal = 17.dp, vertical = 6.dp),
                fontSize = 12.sp)
        }

        if (selectedDocument != null) {
            // The PDF/DOCX viewer owns a separate full-screen dialog window,
            // above the application's toolbar and floating dock.
            Spacer(Modifier.weight(1f))
        } else if (selectedExcerpt != null) {
            ReferenceManualExcerptReader(
                selectedExcerpt, p, lang,
                onRemove = { deleteExcerpt = selectedExcerpt },
            )
        } else {
            if (current == null) {
                ReferenceManualPanel(p) {
                    Text(localized(
                        "Каталог требует проверки. Импорт заблокирован; данные не удалены.",
                        "Manual catalog needs inspection. Import is locked; data was preserved.",
                        lang), color = p.systemRed)
                }
            } else {
                NavAnimatedSearchField(
                    searchOpen = showSearch, value = search, onValueChange = { search = it },
                    onDismiss = { showSearch = false; search = "" },
                    p = p, lang = lang,
                    placeholder = localized("Поиск по названиям документов",
                        "Search document titles", lang),
                    neutralStyle = true, highContrastField = true,
                    resultCount = totalSearchResults,
                    currentResult = selectedSearchResult,
                    onPreviousResult = { moveSearchResult(-1) },
                    onNextResult = { moveSearchResult(1) },
                )
                Box(Modifier.padding(horizontal = 16.dp)) { NavEventDivider(p) }
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth()
                        .revealSearchOnSettledTopPull(listState, showSearch || busy || modalOpen) {
                            showSearch = true
                        }
                        .dismissSearchImeOnResultsGesture(showSearch),
                    state = listState,
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp,
                        top = 8.dp, bottom = 125.dp),
                    verticalArrangement = Arrangement.spacedBy(11.dp),
                ) {
                    if (folders.isEmpty() && documents.isEmpty() && excerpts.isEmpty()) item {
                        ReferenceManualPanel(p) {
                            Text(if (q.isNotBlank())
                                localized("Ничего не найдено.", "No matching documents.", lang)
                            else localized("Нажми «+», чтобы импортировать мануал или создать папку.",
                                "Tap + to import a manual or create a folder.", lang),
                                color = p.muted, fontSize = 13.sp)
                        }
                    }
                    items(folders, key = { "folder-" + it.id }) { item ->
                        ReferenceManualListRow(
                            item.title, localized("Папка", "Folder", lang), p,
                            query = q, glyph = "equipment-folder", isFolder = true,
                            onClick = { folderId = item.id; showSearch = false; search = "" },
                            onEdit = {
                                if (!busy) { folderName = item.title; folderEdit = item; newFolder = true }
                            },
                        )
                    }
                    items(documents, key = { "document-" + it.id }) { doc ->
                        ReferenceManualListRow(
                            doc.title,
                            if (doc.mimeType == ReferenceManualMime.PDF) "PDF"
                                else "Word DOCX",
                            p, query = q, glyph = "reference", onClick = {
                                if (doc.mimeType == ReferenceManualMime.PDF &&
                                    navNativePdfSupported()) {
                                    runCatching {
                                        context.startActivity(ReferencePdfActivity.intent(context, doc))
                                    }.onFailure {
                                        error = true
                                        feedback = it.localizedMessage
                                        documentId = doc.id // Reliable existing PdfRenderer fallback.
                                    }
                                } else documentId = doc.id
                            },
                            onRemove = { deleteDocument = doc },
                        )
                    }
                    items(excerpts, key = { "excerpt-" + it.id }) { excerpt ->
                        ReferenceManualListRow(
                            excerpt.title,
                            if (q.isBlank())
                                localized("Выдержка", "Excerpt", lang)
                            else listOf(excerpt.text, excerpt.sourceTitle)
                                .firstOrNull { it.contains(q, ignoreCase = true) }
                                ?.take(110)
                                ?: localized("Выдержка", "Excerpt", lang),
                            p, query = q, glyph = "work-order", onClick = { excerptId = excerpt.id },
                        )
                    }
                }
            }
        }
    }

    selectedDocument?.let { document ->
        Dialog(
            onDismissRequest = { documentId = null },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnClickOutside = false,
                decorFitsSystemWindows = false,
            ),
        ) {
            Column(
                Modifier.fillMaxSize()
                    .background(p.background)
                    .safeDrawingPadding(),
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(
                        start = 12.dp, end = 12.dp, top = 5.dp, bottom = 6.dp
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ReferenceManualHeaderAction(p, "back", localized("Назад", "Back", lang)) { documentId = null }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(localized("Чтение документа", "Document reader", lang),
                            fontSize = 12.sp, color = NavTodayVisualTokens.accentInk(p.dark))
                        Text(document.title, color = p.text, fontSize = 17.sp,
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    ReferenceManualOpenWithButton(
                        document = document, store = store, p = p, lang = lang)
                }
                ReferenceManualReader(
                    document = document,
                    store = store, p = p, lang = lang,
                )
            }
        }
    }

    if (newExcerpt) {
        ReferenceManualExcerptEditor(
            original = editingExcerpt, sectionId = sectionId,
            folderId = folderId, p = p, lang = lang,
            onDismiss = { newExcerpt = false; editingExcerpt = null },
            onSave = { excerpt ->
                scope.launch {
                    busy = true
                    val result = withContext(Dispatchers.IO) {
                        runCatching { store.saveExcerpt(excerpt); store.read() }
                    }
                    finish(result, localized("Выдержка сохранена.",
                        "Excerpt saved.", lang))
                    if (result.isSuccess) {
                        newExcerpt = false
                        editingExcerpt = null
                    }
                }
            },
        )
    }
    if (deleteExcerpt != null) {
        val excerpt = deleteExcerpt!!
        NavGlassAlertDialog(p, neutralSurface = true, neutralActions = true, onDismissRequest = { deleteExcerpt = null },
            title = { Text(localized("Удалить выдержку?", "Delete excerpt?", lang)) },
            text = { Text(localized("Удаление нельзя отменить.",
                "This action cannot be undone.", lang)) },
            confirmButton = {
                TextButton(enabled = !busy, onClick = {
                    scope.launch {
                        busy = true
                        val result = withContext(Dispatchers.IO) {
                            runCatching { store.removeExcerpt(excerpt.id); store.read() }
                        }
                        finish(result, localized("Выдержка удалена.",
                            "Excerpt deleted.", lang))
                        if (result.isSuccess) {
                            deleteExcerpt = null
                            excerptId = null
                        }
                    }
                }) { Text(localized("Удалить", "Delete", lang), color = p.systemRed) }
            },
            dismissButton = { TextButton(onClick = { deleteExcerpt = null }) {
                Text(localized("Отмена", "Cancel", lang), color = p.muted)
            } },
        )
    }

    if (newFolder) {
        NavGlassAlertDialog(p, neutralSurface = true, neutralActions = true, onDismissRequest = { newFolder = false },
            title = { Text(localized(if (folderEdit == null) "Новая папка" else "Название папки",
                if (folderEdit == null) "New folder" else "Rename folder", lang)) },
            text = {
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ReferenceField(localized("Название", "Name", lang), folderName,
                        { folderName = it.take(120) }, p)
                    folderEdit?.let { item ->
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Box(Modifier.personalNoteCard(p, 14.dp, nested = true)) {
                                TextButton(enabled = !busy, onClick = {
                                    newFolder = false
                                    deleteFolder = item
                                }, modifier = Modifier.heightIn(min = 44.dp)) {
                                    Text(localized("Удалить папку", "Delete folder", lang), color = p.systemRed)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                val canSave = !busy && folderName.isNotBlank() &&
                    folderName.trim() != folderEdit?.title.orEmpty().trim()
                TextButton(enabled = canSave, onClick = {
                    val folder = (folderEdit ?: ReferenceManualFolder(
                        title = folderName.trim(), sectionId = sectionId,
                        parentId = folderId)).copy(title = folderName.trim())
                    scope.launch {
                        busy = true
                        val result = withContext(Dispatchers.IO) {
                            runCatching { store.saveFolder(folder); store.read() }
                        }
                        finish(result, localized("Папка сохранена.", "Folder saved.", lang))
                        if (result.isSuccess) newFolder = false
                    }
                }) { Text(localized("Сохранить", "Save", lang), color = NavTodayVisualTokens.accentInk(p.dark)) }
            },
            dismissButton = { TextButton(onClick = { newFolder = false }) {
                Text(localized("Отмена", "Cancel", lang), color = p.muted)
            } },
        )
    }
    if (deleteFolder != null) {
        val item = deleteFolder!!
        NavGlassAlertDialog(p, neutralSurface = true, neutralActions = true, onDismissRequest = { deleteFolder = null },
            title = { Text(localized("Удалить пустую папку?", "Delete empty folder?", lang)) },
            text = { Text(localized(
                "Папку с документами или вложенными папками удалить нельзя.",
                "Folders containing files or subfolders cannot be deleted.", lang)) },
            confirmButton = { TextButton(onClick = {
                scope.launch {
                    busy = true
                    val result = withContext(Dispatchers.IO) {
                        runCatching { store.deleteEmptyFolder(item.id); store.read() }
                    }
                    finish(result, localized("Папка удалена.", "Folder deleted.", lang))
                    if (result.isSuccess) {
                        if (folderId == item.id) folderId = item.parentId
                        deleteFolder = null
                    }
                }
            }) { Text(localized("Удалить", "Delete", lang), color = p.systemRed) } },
            dismissButton = { TextButton(onClick = { deleteFolder = null }) {
                Text(localized("Отмена", "Cancel", lang), color = p.muted)
            } },
        )
    }
    if (deleteDocument != null) {
        val item = deleteDocument!!
        NavGlassAlertDialog(p, neutralSurface = true, neutralActions = true, onDismissRequest = { deleteDocument = null },
            title = { Text(localized("Убрать документ из библиотеки?",
                "Remove document from library?", lang)) },
            text = { Text(localized(
                "Запись будет удалена из каталога. Ранее экспортированные архивы не затрагиваются.",
                "This document entry will be removed; earlier backups are not changed.", lang)) },
            confirmButton = { TextButton(onClick = {
                scope.launch {
                    busy = true
                    val result = withContext(Dispatchers.IO) {
                        runCatching { store.removeDocument(item.id); store.read() }
                    }
                    finish(result, localized("Документ удалён из каталога.",
                        "Document removed.", lang))
                    if (result.isSuccess) deleteDocument = null
                }
            }) { Text(localized("Удалить", "Delete", lang), color = p.systemRed) } },
            dismissButton = { TextButton(onClick = { deleteDocument = null }) {
                Text(localized("Отмена", "Cancel", lang), color = p.muted)
            } },
        )
    }
    stagedImport?.let { staged ->
        NavGlassAlertDialog(p, neutralSurface = true, neutralActions = true, onDismissRequest = {
            if (!busy && !restoringArchive) stagedImport = null
        }, title = {
            Text(localized("Восстановить мануалы", "Restore manuals", lang))
        }, text = {
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text(localized("Документов: ", "Documents: ", lang) +
                    staged.catalog.documents.size, color = p.text)
                Text(localized("Папок: ", "Folders: ", lang) +
                    staged.catalog.folders.size, color = p.text)
                Text(localized(
                    "Будут добавлены копии, оригиналы PDF/DOCX проверены. " +
                        "Существующие материалы не удаляются.",
                    "Copies will be added with verified original PDF/DOCX files. " +
                        "Existing materials are preserved.", lang),
                    color = p.muted, fontSize = 12.sp)
            }
        }, confirmButton = {
            TextButton(enabled = !busy, onClick = {
                restoringArchive = true
                busy = true
                scope.launch {
                    try {
                        val result = withContext(Dispatchers.IO) {
                            runCatching { store.restoreArchive(staged); store.read() }
                        }
                        finish(result, localized("Мануалы восстановлены.",
                            "Manual library restored.", lang))
                    } finally {
                        // Cleanup must run even if the screen is disposed mid-restore.
                        withContext(NonCancellable + Dispatchers.IO) { staged.close() }
                        stagedImport = null
                        restoringArchive = false
                        busy = false
                    }
                }
            }) { Text(localized("Добавить копии", "Add copies", lang), color = NavTodayVisualTokens.accentInk(p.dark)) }
        }, dismissButton = {
            TextButton(enabled = !busy && !restoringArchive,
                onClick = { stagedImport = null }) {
                Text(localized("Отмена", "Cancel", lang), color = p.muted)
            }
        })
    }
}

@Composable
private fun ReferenceManualListRow(
    title: String, subtitle: String, p: NavPalette,
    query: String = "", glyph: String = "reference", isFolder: Boolean = false,
    onClick: () -> Unit, onRemove: (() -> Unit)? = null, onEdit: (() -> Unit)? = null,
) {
    Row(Modifier.fillMaxWidth().personalNoteCard(p, NavEventLayout.cardRadius)
        .padding(horizontal = 13.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Row(Modifier.weight(1f).glassClickable(onClick).padding(vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(42.dp).personalNoteCard(p, 12.dp, nested = true),
                contentAlignment = Alignment.Center) {
                NavGlassGlyph(glyph, if (isFolder) NavTodayVisualTokens.accentInk(p.dark) else p.text,
                    Modifier.size(25.dp))
            }
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                NavSearchMarkedText(title, query, p, color = p.text, fontSize = 15.sp,
                    maxLines = 2, overflow = TextOverflow.Ellipsis)
                NavSearchMarkedText(subtitle, query, p,
                    color = if (isFolder) NavTodayVisualTokens.accentInk(p.dark) else p.muted,
                    fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            NavGlassGlyph("chevron-right", p.muted, Modifier.size(17.dp))
        }
        if (onEdit != null) {
            Spacer(Modifier.width(6.dp))
            Box(Modifier.size(42.dp).glassClickable(onEdit), contentAlignment = Alignment.Center) {
                NavGlassGlyph("edit", p.muted, Modifier.size(20.dp))
            }
        }
        if (onRemove != null) {
            Spacer(Modifier.width(6.dp))
            Box(Modifier.size(44.dp).glassClickable(onRemove), contentAlignment = Alignment.Center) {
                NavGlassGlyph("x", p.systemRed, Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun ReferenceManualHeaderAction(p: NavPalette, kind: String, label: String, onClick: () -> Unit) {
    Box(Modifier.personalNoteCard(p, 22.dp)) {
        NavEventHeaderAction(kind, label, p, true, onClick)
    }
}

@Composable
internal fun ReferenceManualPanel(p: NavPalette, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth().personalNoteCard(p, NavEventLayout.cardRadius).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp), content = content)
}
