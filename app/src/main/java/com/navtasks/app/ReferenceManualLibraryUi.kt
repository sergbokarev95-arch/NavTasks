package com.navtasks.app

import android.provider.OpenableColumns
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
                                source, java.io.File(context.filesDir, "navtasks-reference/staging"))
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

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically) {
            NavGlassCircleButton(p, "back") { navigateBack() }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(localized("Справочник", "Reference", lang), color = p.blueText,
                    fontSize = 12.sp)
                Text(selectedDocument?.title ?: activeFolder?.title ?: when (sectionId) {
                    NavReferenceSections.SIRE -> "SIRE 2.0"
                    NavReferenceSections.VIQ7 -> "VIQ7"
                    else -> localized("Мои материалы", "My materials", lang)
                }, color = p.text, fontSize = 20.sp, maxLines = 1,
                    overflow = TextOverflow.Ellipsis)
            }
            if (selectedDocument == null && selectedExcerpt == null && current != null) {
                Box {
                    NavGlassCircleButton(p, "plus") { if (!busy) menuOpen = true }
                    NavPopoverMenu(p, expanded = menuOpen,
                        onDismissRequest = { menuOpen = false }) {
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
                NavGlassCircleButton(p, "search") {
                    if (busy) return@NavGlassCircleButton
                    showSearch = !showSearch
                    if (!showSearch) search = ""
                }
            }
        }

        if (busy) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                color = p.blueText)
        }
        if (feedback != null) {
            Text(feedback.orEmpty(), color = if (error) p.systemRed else p.blueText,
                modifier = Modifier.padding(horizontal = 17.dp, vertical = 6.dp),
                fontSize = 12.sp)
        }

        if (selectedDocument != null) {
            ReferenceManualReader(
                document = selectedDocument,
                store = store, p = p, lang = lang,
            )
        } else if (selectedExcerpt != null) {
            ReferenceManualExcerptReader(
                selectedExcerpt, p, lang,
                onEdit = { editingExcerpt = selectedExcerpt; newExcerpt = true },
                onRemove = { deleteExcerpt = selectedExcerpt },
            )
        } else {
            if (current == null) {
                NavPanel(p) {
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
                    resultCount = totalSearchResults,
                    currentResult = selectedSearchResult,
                    onPreviousResult = { moveSearchResult(-1) },
                    onNextResult = { moveSearchResult(1) },
                )
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState,
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp,
                        top = 8.dp, bottom = 125.dp),
                    verticalArrangement = Arrangement.spacedBy(11.dp),
                ) {
                    if (activeFolder != null && q.isBlank()) item {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(localized("Папка", "Folder", lang), color = p.muted,
                                fontSize = 12.sp, modifier = Modifier.weight(1f))
                            TextButton(onClick = {
                                folderName = activeFolder.title
                                folderEdit = activeFolder
                                newFolder = true
                            }) { Text(localized("Изменить", "Rename", lang), color = p.blueText) }
                            TextButton(onClick = { deleteFolder = activeFolder }) {
                                Text(localized("Удалить", "Delete", lang), color = p.systemRed)
                            }
                        }
                    }
                    if (folders.isEmpty() && documents.isEmpty() && excerpts.isEmpty()) item {
                        NavPanel(p) {
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
                            query = q,
                            onClick = { folderId = item.id; showSearch = false; search = "" },
                        )
                    }
                    items(documents, key = { "document-" + it.id }) { doc ->
                        ReferenceManualListRow(
                            doc.title,
                            if (doc.mimeType == ReferenceManualMime.PDF) "PDF"
                                else "Word DOCX",
                            p, query = q, onClick = { documentId = doc.id },
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
                            p, query = q, onClick = { excerptId = excerpt.id },
                            onRemove = { deleteExcerpt = excerpt },
                        )
                    }
                }
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
        NavGlassAlertDialog(p, onDismissRequest = { deleteExcerpt = null },
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
        NavGlassAlertDialog(p, onDismissRequest = { newFolder = false },
            title = { Text(localized("Папка справочника", "Reference folder", lang)) },
            text = {
                NavGlassInput(folderName, { folderName = it.take(120) }, p,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = localized("Название папки", "Folder name", lang))
            },
            confirmButton = {
                TextButton(enabled = !busy && folderName.isNotBlank(), onClick = {
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
                }) { Text(localized("Сохранить", "Save", lang), color = p.blueText) }
            },
            dismissButton = { TextButton(onClick = { newFolder = false }) {
                Text(localized("Отмена", "Cancel", lang), color = p.muted)
            } },
        )
    }
    if (deleteFolder != null) {
        val item = deleteFolder!!
        NavGlassAlertDialog(p, onDismissRequest = { deleteFolder = null },
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
                    if (result.isSuccess) { folderId = item.parentId; deleteFolder = null }
                }
            }) { Text(localized("Удалить", "Delete", lang), color = p.systemRed) } },
            dismissButton = { TextButton(onClick = { deleteFolder = null }) {
                Text(localized("Отмена", "Cancel", lang), color = p.muted)
            } },
        )
    }
    if (deleteDocument != null) {
        val item = deleteDocument!!
        NavGlassAlertDialog(p, onDismissRequest = { deleteDocument = null },
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
        NavGlassAlertDialog(p, onDismissRequest = {
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
            }) { Text(localized("Добавить копии", "Add copies", lang), color = p.blueText) }
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
    query: String = "", onClick: () -> Unit, onRemove: (() -> Unit)? = null,
) {
    Row(Modifier.fillMaxWidth().navGlass(p, radius = 17.dp, role = GlassRole.TASK)
        .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Row(Modifier.weight(1f).glassClickable(onClick).padding(vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically) {
            NavGlassGlyph("reference", p.blueText, Modifier.size(24.dp))
            Spacer(Modifier.width(11.dp))
            Column(Modifier.weight(1f)) {
                NavSearchMarkedText(title, query, p, color = p.text, fontSize = 15.sp,
                    maxLines = 2, overflow = TextOverflow.Ellipsis)
                NavSearchMarkedText(subtitle, query, p, color = p.muted, fontSize = 12.sp)
            }
            NavGlassGlyph("chevron-right", p.muted, Modifier.size(18.dp))
        }
        if (onRemove != null) {
            Spacer(Modifier.width(5.dp))
            TextButton(onClick = onRemove) {
                Text("×", color = p.systemRed, fontSize = 21.sp)
            }
        }
    }
}
