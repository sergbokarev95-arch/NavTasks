package com.navtasks.app

import android.app.DatePickerDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.IOException
import java.time.LocalDate

/** Active notes stay at the top. Resolved/struck entries remain visible at the end. */
internal fun orderedHandoverEntries(entries: List<HandoverEntry>): List<HandoverEntry> =
    entries.filter { it.mark == HandoverMark.OPEN } +
        entries.filter { it.mark != HandoverMark.OPEN }

@Composable
fun HandoverScreen(
    handover: HandoverStore,
    tasks: TaskStore,
    p: NavPalette,
    lang: UiLanguage,
    onBack: () -> Unit,
    personalMode: Boolean = false,
) {
    val context = LocalContext.current
    var collapsed by remember { mutableStateOf(emptySet<String>()) }
    var editSectionId by remember { mutableStateOf<String?>(null) }
    var addSection by remember { mutableStateOf(false) }
    var sectionName by remember { mutableStateOf("") }
    var editEntry by remember { mutableStateOf<Pair<String, String?>?>(null) }
    var entryTitle by remember { mutableStateOf("") }
    var entryNotes by remember { mutableStateOf("") }
    var selectedEntry by remember { mutableStateOf<Pair<String, String>?>(null) }
    var confirmDelete by remember { mutableStateOf<Pair<String, String?>?>(null) }
    var pendingImport by remember { mutableStateOf<HandoverPackage?>(null) }
    var feedback by remember { mutableStateOf<String?>(null) }

    fun openSectionEditor(section: HandoverSection?) {
        editSectionId = section?.id
        sectionName = section?.title.orEmpty()
        addSection = true
    }

    fun openEntryEditor(sectionId: String, entry: HandoverEntry?) {
        editEntry = sectionId to entry?.id
        entryTitle = entry?.title.orEmpty()
        entryNotes = entry?.notes.orEmpty()
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        if (uri != null) {
            runCatching {
                val stream = context.contentResolver.openOutputStream(uri)
                    ?: throw IOException("Cannot create file")
                stream.bufferedWriter(Charsets.UTF_8).use {
                    it.write(HandoverTransfer.export(tasks.templates, handover.sections))
                }
            }.onSuccess {
                feedback = localized("Пакет передачи дел сохранён.", "Handover package saved.", lang)
            }.onFailure {
                feedback = it.localizedMessage ?: localized("Ошибка экспорта", "Export failed", lang)
            }
        }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching {
                val input = context.contentResolver.openInputStream(uri)
                    ?: throw IOException("Cannot open file")
                val text = input.bufferedReader(Charsets.UTF_8).use { reader ->
                    val out = StringBuilder()
                    val buffer = CharArray(4096)
                    while (true) {
                        val count = reader.read(buffer)
                        if (count < 0) break
                        if (out.length + count > 2_000_000) throw IOException("File exceeds 2 MB")
                        out.append(buffer, 0, count)
                    }
                    out.toString()
                }
                HandoverTransfer.parse(text)
            }.onSuccess {
                pendingImport = it
                feedback = null
            }.onFailure {
                feedback = it.localizedMessage ?: localized("Ошибка импорта", "Import failed", lang)
            }
        }
    }

    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NavGlassCircleButton(p, "back", onClick = onBack)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(if (personalMode) localized("ЛИЧНОЕ", "PERSONAL", lang) else "HANDOVER",
                    color = p.blue, fontSize = 13.sp, letterSpacing = 1.8.sp)
                Text(if (personalMode) localized("Заметки", "Notes", lang)
                    else localized("Передача дел", "Handover notes", lang),
                    color = p.text, fontSize = 22.sp)
            }
            NavGlassCircleButton(p, "plus") { openSectionEditor(null) }
        }
        if (!personalMode) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 17.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            Box(
                Modifier.weight(1f).navGlass(p, radius = 15.dp, role = GlassRole.CONTROL)
                    .glassClickable { exportLauncher.launch("NavTasks_Handover_${LocalDate.now()}.json") }
                    .padding(vertical = 12.dp), contentAlignment = Alignment.Center,
            ) { Text(localized("Экспорт пакета", "Export package", lang), color = p.blue, fontSize = 13.sp) }
            Box(
                Modifier.weight(1f).navGlass(p, radius = 15.dp, role = GlassRole.CONTROL)
                    .glassClickable { importLauncher.launch(arrayOf("application/json", "text/plain", "application/octet-stream")) }
                    .padding(vertical = 12.dp), contentAlignment = Alignment.Center,
            ) { Text(localized("Импорт пакета", "Import package", lang), color = p.blue, fontSize = 13.sp) }
        }
        Text(
            localized("В пакете: задачи и заметки передачи дел. История личных отметок и настройки не передаются.",
                "Package contains tasks and handover notes, not private task history or settings.", lang),
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 3.dp),
            color = p.muted, fontSize = 11.sp,
        )
        }
        feedback?.let {
            Text(it, modifier = Modifier.padding(horizontal = 20.dp, vertical = 5.dp),
                color = p.mint, fontSize = 12.sp)
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (handover.sections.isEmpty()) {
                item {
                    NavPanel(p) {
                        Text(localized("Пока нет разделов. Нажми +, чтобы создать первый.",
                            "No sections yet. Tap + to create one.", lang), color = p.muted)
                    }
                }
            }
            items(handover.sections, key = { it.id }) { section ->
                Column(
                    Modifier.fillMaxWidth().navGlass(p, radius = 19.dp, role = GlassRole.PANEL)
                        .animateContentSize().padding(11.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            section.title,
                            Modifier.weight(1f).glassClickable {
                                collapsed = if (section.id in collapsed) collapsed - section.id
                                    else collapsed + section.id
                            }.padding(vertical = 6.dp),
                            color = p.text, fontSize = 17.sp, fontWeight = FontWeight.Medium,
                        )
                        Text("${section.entries.count { it.mark == HandoverMark.COMPLETED }}/${section.entries.size}",
                            color = p.muted, fontSize = 12.sp)
                        Spacer(Modifier.width(12.dp))
                        Text("✎", Modifier.glassClickable { openSectionEditor(section) }.padding(7.dp),
                            color = p.muted, fontSize = 19.sp)
                        Text("+", Modifier.glassClickable { openEntryEditor(section.id, null) }.padding(horizontal = 10.dp, vertical = 5.dp),
                            color = p.blue, fontSize = 24.sp)
                    }
                    if (section.id !in collapsed) {
                        if (section.entries.isEmpty()) {
                            Text(localized("Нажми +, чтобы добавить пункт", "Tap + to add an item", lang),
                                color = p.muted, fontSize = 12.sp,
                                modifier = Modifier.padding(start = 5.dp, bottom = 7.dp))
                        }
                        orderedHandoverEntries(section.entries).forEach { entry ->
                            val finished = entry.mark != HandoverMark.OPEN
                            Row(
                                Modifier.fillMaxWidth()
                                    .navGlass(p, radius = 13.dp,
                                        role = if (finished) GlassRole.CONTROL else GlassRole.TASK)
                                    .glassClickable { selectedEntry = section.id to entry.id }
                                    .padding(horizontal = 10.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(if (entry.mark == HandoverMark.COMPLETED) "✓" else if (finished) "–" else "○",
                                    color = if (finished) p.mint else p.blue, fontSize = 22.sp)
                                Spacer(Modifier.width(11.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(entry.title, color = if (finished) p.muted else p.text,
                                        fontSize = 14.sp,
                                        textDecoration = if (finished) TextDecoration.LineThrough else TextDecoration.None)
                                    if (entry.notes.isNotBlank()) {
                                        Text(entry.notes, color = p.muted, fontSize = 11.sp,
                                            maxLines = 2)
                                    }
                                    if (entry.mark == HandoverMark.COMPLETED) {
                                        Text(entry.completedDate?.let {
                                            localized("Завершено: $it", "Completed: $it", lang)
                                        } ?: localized("Завершено без даты", "Completed without date", lang),
                                            color = p.mint, fontSize = 10.sp)
                                    } else if (entry.mark == HandoverMark.STRUCK) {
                                        Text(localized("Вычеркнуто", "Struck out", lang),
                                            color = p.muted, fontSize = 10.sp)
                                    }
                                }
                                Text("›", color = p.muted, fontSize = 23.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    if (addSection) {
        AlertDialog(
            onDismissRequest = { addSection = false },
            title = { Text(if (editSectionId == null) localized("Новый раздел", "New section", lang)
                else localized("Редактировать раздел", "Edit section", lang)) },
            text = {
                OutlinedTextField(sectionName, onValueChange = { sectionName = it.take(120) },
                    label = { Text(localized("Название", "Title", lang)) }, singleLine = true)
            },
            confirmButton = {
                TextButton(onClick = {
                    handover.saveSection(sectionName, editSectionId)
                    addSection = false
                }, enabled = sectionName.isNotBlank()) {
                    Text(localized("Сохранить", "Save", lang))
                }
            },
            dismissButton = {
                Row {
                    if (editSectionId != null) {
                        TextButton(onClick = {
                            confirmDelete = editSectionId.orEmpty() to null
                            addSection = false
                        }) { Text(localized("Удалить", "Delete", lang), color = Color(0xFFCF6259)) }
                    }
                    TextButton(onClick = { addSection = false }) {
                        Text(localized("Отмена", "Cancel", lang))
                    }
                }
            },
        )
    }

    editEntry?.let { (sectionId, entryId) ->
        AlertDialog(
            onDismissRequest = { editEntry = null },
            title = { Text(if (entryId == null) localized("Новый пункт", "New item", lang)
                else localized("Редактировать пункт", "Edit item", lang)) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(entryTitle, onValueChange = { entryTitle = it.take(200) },
                        label = { Text(localized("Название", "Title", lang)) }, singleLine = true)
                    OutlinedTextField(entryNotes, onValueChange = { entryNotes = it.take(4000) },
                        label = { Text(localized("Комментарий", "Notes", lang)) },
                        minLines = 2, maxLines = 5)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    handover.saveEntry(sectionId, entryTitle, entryNotes, entryId)
                    editEntry = null
                }, enabled = entryTitle.isNotBlank()) {
                    Text(localized("Сохранить", "Save", lang))
                }
            },
            dismissButton = {
                Row {
                    if (entryId != null) {
                        TextButton(onClick = {
                            confirmDelete = sectionId to entryId
                            editEntry = null
                        }) { Text(localized("Удалить", "Delete", lang), color = Color(0xFFCF6259)) }
                    }
                    TextButton(onClick = { editEntry = null }) { Text(localized("Отмена", "Cancel", lang)) }
                }
            },
        )
    }

    selectedEntry?.let { (sectionId, entryId) ->
        val item = handover.sections.firstOrNull { it.id == sectionId }?.entries?.firstOrNull { it.id == entryId }
        if (item != null) {
            AlertDialog(
                onDismissRequest = { selectedEntry = null },
                title = { Text(item.title) },
                text = {
                    Column(Modifier.verticalScroll(rememberScrollState()).heightIn(max = 380.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        if (item.notes.isNotBlank()) Text(item.notes, color = p.muted, fontSize = 13.sp)
                        if (item.mark == HandoverMark.COMPLETED) {
                            Text(item.completedDate?.toString() ?: localized("Дата не указана", "No date", lang),
                                color = p.mint, fontSize = 13.sp)
                        }
                        TextButton(onClick = {
                            val day = item.completedDate ?: LocalDate.now()
                            DatePickerDialog(context, { _, year, month, date ->
                                handover.setMark(sectionId, entryId, HandoverMark.COMPLETED,
                                    LocalDate.of(year, month + 1, date))
                            }, day.year, day.monthValue - 1, day.dayOfMonth).show()
                            selectedEntry = null
                        }) { Text(localized("✓ Завершить с датой", "✓ Complete with date", lang)) }
                        TextButton(onClick = {
                            handover.setMark(sectionId, entryId, HandoverMark.COMPLETED)
                            selectedEntry = null
                        }) { Text(localized("✓ Завершить без даты", "✓ Complete without date", lang)) }
                        TextButton(onClick = {
                            handover.setMark(sectionId, entryId, HandoverMark.STRUCK)
                            selectedEntry = null
                        }) { Text(localized("Вычеркнуть", "Strike out", lang)) }
                        if (item.mark != HandoverMark.OPEN) {
                            TextButton(onClick = {
                                handover.setMark(sectionId, entryId, HandoverMark.OPEN)
                                selectedEntry = null
                            }) { Text(localized("Вернуть в работу", "Reopen", lang)) }
                        }
                        TextButton(onClick = {
                            openEntryEditor(sectionId, item)
                            selectedEntry = null
                        }) { Text(localized("Редактировать", "Edit", lang)) }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { selectedEntry = null }) {
                        Text(localized("Закрыть", "Close", lang))
                    }
                },
            )
        }
    }

    confirmDelete?.let { (sectionId, entryId) ->
        AlertDialog(
            onDismissRequest = { confirmDelete = null },
            title = { Text(localized("Удалить без возможности восстановления?", "Delete permanently?", lang)) },
            text = { Text(localized("Пункт или раздел будет удалён из передачи дел.",
                "The item or section will be removed from Handover.", lang)) },
            confirmButton = {
                TextButton(onClick = {
                    if (entryId == null) handover.deleteSection(sectionId)
                    else handover.deleteEntry(sectionId, entryId)
                    confirmDelete = null
                }) { Text(localized("Удалить", "Delete", lang), color = Color(0xFFCF6259)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = null }) {
                    Text(localized("Отмена", "Cancel", lang))
                }
            },
        )
    }

    pendingImport?.let { pack ->
        AlertDialog(
            onDismissRequest = { pendingImport = null },
            title = { Text(localized("Импорт передачи дел", "Import Handover", lang)) },
            text = { Text(localized(
                "В файле ${pack.sections.size} разделов, ${pack.sections.sumOf { it.entries.size }} пунктов и ${pack.tasks.size} шаблонов задач. Добавить без замены существующих данных?",
                "File contains ${pack.sections.size} sections, ${pack.sections.sumOf { it.entries.size }} notes and ${pack.tasks.size} task templates. Merge without replacing existing data?", lang)) },
            confirmButton = {
                TextButton(onClick = {
                    val merged = handover.mergeSections(pack.sections)
                    val newTasks = tasks.importTemplates(pack.tasks)
                    pendingImport = null
                    feedback = localized("Добавлено: ${merged.first} разделов, ${merged.second} пунктов, $newTasks задач.",
                        "Added ${merged.first} sections, ${merged.second} notes and $newTasks tasks.", lang)
                }) { Text(localized("Импортировать", "Import", lang)) }
            },
            dismissButton = {
                TextButton(onClick = { pendingImport = null }) {
                    Text(localized("Отмена", "Cancel", lang))
                }
            },
        )
    }
}
