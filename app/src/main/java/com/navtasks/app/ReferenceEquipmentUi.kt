package com.navtasks.app

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.BackHandler
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
    val folders = if (query.isBlank()) catalog.folders
        .filter { it.parentId == folderId }.sortedBy { it.title.lowercase() } else emptyList()
    var addMenu by remember { mutableStateOf(false) }
    var folderEditor by remember { mutableStateOf<ReferenceFolder?>(null) }
    var newFolder by remember { mutableStateOf(false) }
    var equipmentEditor by remember { mutableStateOf<ReferenceEquipment?>(null) }
    var newEquipment by remember { mutableStateOf(false) }
    var procedureEditor by remember { mutableStateOf<ReferenceProcedure?>(null) }
    var newProcedure by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<Pair<String, String>?>(null) }
    var imported by remember { mutableStateOf<ReferenceEquipment?>(null) }
    var exportingId by remember { mutableStateOf<String?>(null) }
    var feedback by remember { mutableStateOf<String?>(null) }
    var failure by remember { mutableStateOf(false) }

    fun navigateBack() {
        when {
            procedureId != null -> procedureId = null
            equipmentId != null -> equipmentId = null
            folderId != null -> folderId = folder?.parentId
            searchOpen -> { searchOpen = false; searchText = "" }
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

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream"),
    ) { uri ->
        val target = catalog.equipment.firstOrNull { it.id == exportingId }
        if (uri != null && target != null) {
            runCatching {
                val bytes = ReferenceEquipmentExchange.encode(target)
                context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
                    ?: throw IOException("Cannot create file")
            }.onSuccess {
                failure = false
                feedback = localized("Карточка оборудования экспортирована.",
                    "Equipment reference exported.", lang)
            }.onFailure {
                failure = true; feedback = it.localizedMessage
            }
        }
        exportingId = null
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            runCatching {
                val bytes = context.contentResolver.openInputStream(uri)?.use { input ->
                    val out = ByteArrayOutputStream()
                    val buffer = ByteArray(8192)
                    while (true) {
                        val n = input.read(buffer)
                        if (n < 0) break
                        require(out.size() + n <= 12_000_000) { "Reference file exceeds 12 MB" }
                        out.write(buffer, 0, n)
                    }
                    out.toByteArray()
                } ?: throw IOException("Cannot read file")
                ReferenceEquipmentExchange.decode(bytes)
            }.onSuccess {
                imported = it
                failure = false
                feedback = null
            }.onFailure {
                failure = true
                feedback = it.localizedMessage
            }
        }
    }

    LaunchedEffect(query) { searchIndex = 0 }
    LaunchedEffect(searchSerial) {
        if (searchSerial > 0 && matches.isNotEmpty() && equipment == null) {
            listState.animateScrollToItem(
                (if (query.isBlank()) folders.size else 0) +
                    searchIndex.coerceIn(0, matches.lastIndex))
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
                                exportingId = equipment.id
                                val safe = equipment.title.replace(
                                    Regex("[^\\p{L}\\p{N}_-]"), "_").take(60)
                                exportLauncher.launch("$safe.navref")
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
                    onEdit = { procedureEditor = procedure },
                    onDelete = { deleteTarget = "procedure" to procedure.id },
                )
            }
            equipment != null -> {
                ReferenceEquipmentDetails(
                    equipment, p, lang,
                    onOpenProcedure = { procedureId = it },
                    onEdit = { equipmentEditor = equipment },
                    onDelete = { deleteTarget = "equipment" to equipment.id },
                    onExport = {
                        exportingId = equipment.id
                        exportLauncher.launch(
                            equipment.title.replace(Regex("[^\\p{L}\\p{N}_-]"), "_")
                                .take(60) + ".navref")
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
                    resultCount = if (query.isBlank()) 0 else matches.size,
                    currentResult = searchIndex,
                    onPreviousResult = {
                        searchIndex = navSearchNextIndex(searchIndex, matches.size, -1)
                        searchSerial++
                    },
                    onNextResult = {
                        searchIndex = navSearchNextIndex(searchIndex, matches.size, 1)
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
    imported?.let { candidate ->
        val conflict = referenceImportConflict(candidate, catalog.equipment)
        NavGlassAlertDialog(p, onDismissRequest = { imported = null },
            title = { Text(localized("Импорт оборудования", "Import equipment", lang)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Text(candidate.title, color = p.text, fontWeight = FontWeight.Medium)
                    Text(localized("Процедур: ", "Procedures: ", lang) +
                        candidate.procedures.size, color = p.muted)
                    Text(when (conflict) {
                        ReferenceImportConflict.SAME_ID -> localized(
                            "Такая запись уже есть. Можно создать копию или явно заменить её.",
                            "This item already exists. Add a copy or explicitly replace it.", lang)
                        ReferenceImportConflict.POSSIBLE_DUPLICATE -> localized(
                            "Похоже, оборудование с таким именем и моделью уже существует.",
                            "Similar equipment is already in this profile.", lang)
                        else -> localized("Существующие записи не будут затронуты.",
                            "Existing records will not be changed.", lang)
                    }, color = p.muted, fontSize = 13.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (submit(referenceImportEquipment(catalog, candidate, folderId))) {
                        imported = null
                        feedback = localized("Оборудование добавлено в справочник.",
                            "Equipment added to Reference.", lang)
                    }
                }) { Text(localized("Добавить копию", "Add copy", lang), color = p.blueText) }
            },
            dismissButton = {
                TextButton(onClick = { imported = null }) {
                    Text(localized("Отмена", "Cancel", lang), color = p.muted)
                }
                if (conflict == ReferenceImportConflict.SAME_ID) {
                    TextButton(onClick = {
                        if (submit(referenceImportEquipment(catalog, candidate, folderId,
                                replaceSameId = true))) {
                            imported = null
                        }
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
    onEdit: () -> Unit, onDelete: () -> Unit,
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
