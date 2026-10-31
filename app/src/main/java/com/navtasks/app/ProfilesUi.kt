package com.navtasks.app

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.util.UUID

@Composable
fun ProfilesScreen(
    profiles: ProfileStore,
    store: TaskStore,
    handover: HandoverStore,
    p: NavPalette,
    lang: UiLanguage,
    onBack: () -> Unit,
    onSettings: () -> Unit,
) {
    var createOpen by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var editing by remember { mutableStateOf<String?>(null) }
    var deleteTarget by remember { mutableStateOf<NavProfile?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    fun activate(id: String) {
        profiles.select(id)
        store.switchProfile()
        handover.switchProfile()
        onBack()
    }

    Column(
        Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()
            .padding(horizontal = 16.dp),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NavGlassCircleButton(p, "back", onBack)
            Spacer(Modifier.width(12.dp))
            Text(localized("Профили", "Profiles", lang), color = p.text,
                fontSize = 24.sp, modifier = Modifier.weight(1f))
            NavGlassCircleButton(p, "settings", onSettings)
        }
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            Text(localized(
                "Каждое судно хранит собственные задачи, историю и передачу дел. Личное хранит только заметки.",
                "Each vessel keeps its own tasks, history and handover. Personal contains notes only.",
                lang), color = p.muted, fontSize = 13.sp)
            profiles.profiles.forEach { profile ->
                val current = profiles.activeId == profile.id
                Row(
                    Modifier.fillMaxWidth().navGlass(p, radius = 18.dp,
                        role = if (current) GlassRole.NESTED_TASK else GlassRole.PANEL)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f).glassClickable { activate(profile.id) }
                        .padding(vertical = 6.dp)) {
                        Text(profile.title, color = p.text, fontSize = 17.sp)
                        Text(
                            if (profile.kind == NavProfileKind.PERSONAL)
                                localized("Заметки", "Notes", lang)
                            else localized("Задачи · История · Handover", "Tasks · History · Handover", lang),
                            color = p.muted, fontSize = 12.sp)
                    }
                    if (current) Text("✓", color = p.mint, fontSize = 20.sp)
                    Spacer(Modifier.width(13.dp))
                    Text("✎", color = p.blue, fontSize = 23.sp,
                        modifier = Modifier.glassClickable {
                            editing = profile.id
                            name = profile.title
                        }.padding(6.dp))
                    if (profile.id != ProfileStore.DEFAULT_SHIP && profile.id != ProfileStore.PERSONAL_ID) {
                        Text("×", color = p.muted, fontSize = 23.sp,
                            modifier = Modifier.glassClickable { deleteTarget = profile }
                                .padding(7.dp))
                    }
                }
            }
            Box(
                Modifier.fillMaxWidth().navGlass(p, radius = 18.dp, role = GlassRole.CONTROL)
                    .glassClickable { name = ""; editing = null; createOpen = true }
                    .padding(16.dp), contentAlignment = Alignment.Center,
            ) {
                Text(localized("+ Добавить судно", "+ Add vessel", lang),
                    color = p.blue, fontSize = 15.sp)
            }
            if (error != null) Text(error.orEmpty(), color = Color(0xFFE47F76), fontSize = 12.sp)
            Spacer(Modifier.height(32.dp))
        }
    }

    if (createOpen || editing != null) {
        val editingId = editing
        AlertDialog(
            onDismissRequest = { createOpen = false; editing = null },
            title = { Text(if (editingId == null)
                localized("Новое судно", "New vessel", lang)
                else localized("Название профиля", "Profile name", lang)) },
            text = {
                OutlinedTextField(value = name, onValueChange = { name = it.take(100) },
                    singleLine = true, label = { Text(localized("Название", "Name", lang)) })
            },
            confirmButton = {
                TextButton(enabled = name.isNotBlank(), onClick = {
                    runCatching {
                        if (editingId == null) {
                            val pNew = profiles.createVessel(name)
                            store.switchProfile()
                            handover.switchProfile()
                            onBack()
                        } else profiles.rename(editingId, name)
                    }.onFailure { error = it.message }
                        .onSuccess { error = null; editing = null; createOpen = false }
                }) { Text(localized("Сохранить", "Save", lang)) }
            },
            dismissButton = {
                TextButton(onClick = { createOpen = false; editing = null }) {
                    Text(localized("Отмена", "Cancel", lang))
                }
            },
        )
    }
    deleteTarget?.let { victim ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text(localized("Удалить профиль?", "Delete profile?", lang)) },
            text = { Text(localized(
                "Все задачи, история и заметки профиля «" + victim.title +
                    "» будут удалены с телефона. Перед удалением лучше экспортировать профиль.",
                "All tasks, history and notes for " + victim.title +
                    " will be deleted from this phone. Export the profile first.", lang)) },
            confirmButton = {
                TextButton(onClick = {
                    profiles.delete(victim.id)
                    store.switchProfile()
                    handover.switchProfile()
                    deleteTarget = null
                }) { Text(localized("Удалить", "Delete", lang)) }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text(localized("Отмена", "Cancel", lang))
                }
            },
        )
    }
}

/** Full profile import/export, separate from the existing template-only transfer. */
@Composable
fun ProfileExchangePanel(
    profiles: ProfileStore,
    store: TaskStore,
    handover: HandoverStore,
    p: NavPalette,
    lang: UiLanguage,
) {
    val context = LocalContext.current
    var selectedId by rememberSaveable { mutableStateOf(profiles.activeId) }
    var selectorExpanded by remember { mutableStateOf(false) }
    if (profiles.profiles.none { it.id == selectedId }) selectedId = profiles.activeId
    var pending by remember { mutableStateOf<ProfileSnapshot?>(null) }
    var feedback by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf(false) }
    val saveFile = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            runCatching {
                val json = ProfileTransfer.export(profiles.snapshot(selectedId))
                context.contentResolver.openOutputStream(uri)?.bufferedWriter(Charsets.UTF_8)?.use {
                    it.write(json)
                } ?: throw IllegalStateException("Unable to open output file")
            }.onSuccess {
                error = false
                feedback = localized("Профиль экспортирован со всей историей.",
                    "Full profile exported with history.", lang)
            }.onFailure {
                error = true; feedback = it.localizedMessage
            }
        }
    }
    val openFile = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            runCatching {
                val reader = context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)
                    ?: throw IllegalStateException("Unable to open the file")
                reader.use {
                    val result = StringBuilder()
                    val buf = CharArray(8192)
                    while (true) {
                        val read = it.read(buf)
                        if (read < 0) break
                        if (result.length + read > ProfileTransfer.MAX_BYTES)
                            throw IllegalStateException("File exceeds 10 MB")
                        result.append(buf, 0, read)
                    }
                    ProfileTransfer.parse(result.toString())
                }
            }.onSuccess {
                pending = it; feedback = null
            }.onFailure {
                error = true; feedback = it.localizedMessage
            }
        }
    }

    NavPanel(p) {
        Text(localized("Полный обмен профилями", "Full profile transfer", lang),
            color = p.text, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        Text(localized(
            "Выбери профиль для экспорта. Файл содержит расписания, все отметки по датам, пропуски, переносы и Handover.",
            "Choose the profile to export. It contains all schedules, dated completions, missed and moved tasks, plus Handover.",
            lang), color = p.muted, fontSize = 12.sp)
        Text(localized("Профиль для экспорта", "Profile to export", lang),
            color = p.muted, fontSize = 12.sp)
        Box(Modifier.fillMaxWidth()) {
            Row(
                Modifier.fillMaxWidth()
                    .navGlass(p, radius = 14.dp, role = GlassRole.CONTROL)
                    .glassClickable { selectorExpanded = true }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    profiles.profiles.first { it.id == selectedId }.title,
                    color = p.text, fontSize = 14.sp,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                )
                Spacer(Modifier.width(8.dp))
                NavGlassGlyph("chevron-down", p.blue, Modifier.size(17.dp))
            }
            DropdownMenu(
                expanded = selectorExpanded,
                onDismissRequest = { selectorExpanded = false },
                modifier = Modifier.background(
                    if (p.dark) Color(0xFF203348) else Color(0xFFF3F8FC),
                ),
            ) {
                profiles.profiles.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item.title, color = p.text, fontSize = 14.sp) },
                        leadingIcon = {
                            if (item.id == selectedId)
                                Text("✓", color = p.mint, fontSize = 16.sp)
                            else Spacer(Modifier.width(14.dp))
                        },
                        onClick = {
                            selectedId = item.id
                            selectorExpanded = false
                        },
                    )
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(9.dp),
            modifier = Modifier.fillMaxWidth()) {
            Box(Modifier.weight(1f).navGlass(p, radius = 13.dp, role = GlassRole.CONTROL)
                .glassClickable {
                    val name = profiles.profiles.first { it.id == selectedId }.title
                        .replace(Regex("[^a-zA-Z0-9а-яА-Я_-]"), "_").take(40)
                    saveFile.launch("NavTasks_" + name + "_" + LocalDate.now() + ".json")
                }.padding(vertical = 13.dp), contentAlignment = Alignment.Center) {
                Text(localized("Экспорт", "Export", lang), color = p.blue, fontSize = 13.sp)
            }
            Box(Modifier.weight(1f).navGlass(p, radius = 13.dp, role = GlassRole.CONTROL)
                .glassClickable {
                    openFile.launch(arrayOf("application/json", "text/plain", "application/octet-stream"))
                }.padding(vertical = 13.dp), contentAlignment = Alignment.Center) {
                Text(localized("Импорт", "Import", lang), color = p.blue, fontSize = 13.sp)
            }
        }
        Text(localized(
            "При импорте одинаковый профиль ПОЛНОСТЬЮ заменяется, без объединения данных. Перед заменой потребуется подтверждение.",
            "Import completely REPLACES a matching profile, never merges its contents. Confirmation is required.",
            lang), color = p.muted, fontSize = 11.sp)
        feedback?.let { Text(it, color = if (error) Color(0xFFE47F76) else p.mint,
            fontSize = 12.sp) }
    }
    pending?.let { incoming ->
        val matched = profiles.findMatch(incoming.profile)
        val title = incoming.profile.title
        AlertDialog(
            onDismissRequest = { pending = null },
            title = { Text(if (matched == null)
                localized("Импортировать профиль?", "Import profile?", lang)
                else localized("Заменить существующий профиль?", "Replace existing profile?", lang)) },
            text = {
                Column {
                    Text(title + " · " + incoming.templates.size + " " +
                        localized("задач", "tasks", lang) + " · " + incoming.runs.size + " " +
                        localized("отметок", "records", lang) + " · " +
                        incoming.sections.size + " " + localized("разделов", "sections", lang))
                    if (matched != null) {
                        Spacer(Modifier.height(12.dp))
                        Text(localized(
                            "ВНИМАНИЕ: все текущие задачи, записи и история профиля «" +
                                matched.title + "» будут удалены и заменены содержимым файла.",
                            "WARNING: all existing tasks, history and notes in " +
                                matched.title + " will be erased and replaced with the file.",
                            lang), color = Color(0xFFD37B69))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    runCatching {
                        val result = profiles.importReplace(incoming)
                        store.switchProfile()
                        handover.switchProfile()
                        selectedId = result.id
                    }.onSuccess {
                        feedback = localized("Профиль импортирован полностью.",
                            "Complete profile imported.", lang)
                        error = false
                        pending = null
                    }.onFailure {
                        feedback = it.localizedMessage
                        error = true
                        pending = null
                    }
                }) {
                    Text(if (matched != null)
                        localized("Заменить", "Replace", lang)
                        else localized("Импортировать", "Import", lang))
                }
            },
            dismissButton = {
                TextButton(onClick = { pending = null }) { Text(localized("Отмена", "Cancel", lang)) }
            },
        )
    }
}
