package com.navtasks.app

import com.kyant.backdrop.backdrops.layerBackdrop

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
    services: ServiceStore,
    workOrders: WorkOrderStore,
    p: NavPalette,
    lang: UiLanguage,
    onBack: () -> Unit,
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
        services.switchProfile()
        workOrders.switchProfile()
        onBack()
    }

    val profileBackdrop = if (android.os.Build.VERSION.SDK_INT >= 33)
        com.kyant.backdrop.backdrops.rememberLayerBackdrop() else null
    Box(Modifier.fillMaxSize()) {
    Column(
        Modifier.fillMaxSize()
            .then(if (profileBackdrop != null) Modifier.layerBackdrop(profileBackdrop) else Modifier)
            .statusBarsPadding().navigationBarsPadding().padding(horizontal = 16.dp),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (profileBackdrop != null) Spacer(Modifier.size(44.dp))
            else NavGlassCircleButton(p, "back", onBack)
            Spacer(Modifier.width(12.dp))
            Text(localized("Профили", "Profiles", lang), color = p.text,
                fontSize = 24.sp, modifier = Modifier.weight(1f))
        }
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            Text(localized(
                "Каждое судно отдельно хранит задачи, историю, AMOS, сервисы и Handover. Личное — только заметки.",
                "Each vessel separately stores tasks, history, AMOS, maintenance and Handover. Personal contains notes only.",
                lang), color = p.muted, fontSize = 13.sp)
            profiles.profiles.forEach { profile ->
                val current = profiles.activeId == profile.id
                Row(
                    Modifier.fillMaxWidth().profileCardStyle(p, current, radius = 18.dp)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f).glassClickable { activate(profile.id) }
                        .padding(vertical = 6.dp)) {
                        Text(profile.title, color = p.text, fontSize = 17.sp,
                            maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                        Text(
                            if (profile.kind == NavProfileKind.PERSONAL)
                                localized("Заметки", "Notes", lang)
                            else localized("Задачи · AMOS · Сервисы · Handover · История",
                                    "Tasks · AMOS · Maintenance · Handover · History", lang),
                            color = p.muted, fontSize = 12.sp)
                    }
                    if (current) NavGlassGlyph("check", NavTodayVisualTokens.accentInk(p.dark), Modifier.size(19.dp))
                    Spacer(Modifier.width(8.dp))
                    NavGlassIconAction(
                        "edit", p.text, localized("Изменить профиль", "Edit profile", lang),
                    ) {
                        editing = profile.id
                        name = profile.title
                    }
                    if (profile.kind == NavProfileKind.VESSEL) {
                        Box(
                            Modifier.heightIn(min = 44.dp)
                                .glassClickable { deleteTarget = profile }
                                .padding(horizontal = 5.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(localized("Удалить", "Delete", lang),
                                color = p.systemRed, fontSize = 12.sp)
                        }
                    }
                }
            }
            Box(
                Modifier.fillMaxWidth().profileCardStyle(p, selected = false, radius = 18.dp)
                    .glassClickable { name = ""; editing = null; createOpen = true }
                    .padding(16.dp), contentAlignment = Alignment.Center,
            ) {
                Text(localized("+ Добавить профиль", "+ Add profile", lang),
                    color = NavTodayVisualTokens.accentInk(p.dark), fontSize = 15.sp)
            }
            if (error != null) Text(error.orEmpty(), color = p.dangerText, fontSize = 12.sp)
            Spacer(Modifier.height(32.dp))
        }
    }
    if (profileBackdrop != null) {
        Box(Modifier.align(Alignment.TopStart).statusBarsPadding()
            .padding(start = 16.dp, top = 9.dp)) {
            NavLiquidGlassCircleButton(p, profileBackdrop, "back", onBack)
        }
    }
    }

    if (createOpen || editing != null) {
        val editingId = editing
        NavGlassAlertDialog(p = p, neutralActions = true, 
            onDismissRequest = { createOpen = false; editing = null },
            title = { Text(if (editingId == null)
                localized("Новый профиль", "New profile", lang)
                else localized("Название профиля", "Profile name", lang)) },
            text = {
                NavGlassInput(
                    value = name,
                    onValueChange = { name = it.take(100) },
                    p = p, modifier = Modifier.fillMaxWidth(),
                    placeholder = localized("Название", "Name", lang), neutralStyle = true,
                )
            },
            confirmButton = {
                TextButton(enabled = name.isNotBlank(), onClick = {
                    runCatching {
                        if (editingId == null) {
                            val pNew = profiles.createVessel(name)
                            store.switchProfile()
                            handover.switchProfile()
                            services.switchProfile()
                            workOrders.switchProfile()
                            onBack()
                        } else profiles.rename(editingId, name)
                    }.onFailure { error = it.message }
                        .onSuccess { error = null; editing = null; createOpen = false }
                }) { Text(localized("Сохранить", "Save", lang), color = if (name.isNotBlank()) NavTodayVisualTokens.accentInk(p.dark) else p.muted) }
            },
            dismissButton = {
                TextButton(onClick = { createOpen = false; editing = null }) {
                    Text(localized("Отмена", "Cancel", lang), color = p.text)
                }
            },
        )
    }
    deleteTarget?.let { victim ->
        NavGlassAlertDialog(p = p, neutralActions = true, 
            onDismissRequest = { deleteTarget = null },
            title = { Text(localized("Удалить профиль?", "Delete profile?", lang)) },
            text = { Text(localized(
                "Все задачи, история, пропуски, Handover, сервисы и AMOS профиля «" + victim.title +
                    "» будут безвозвратно удалены. Перед удалением экспортируй профиль.",
                "All tasks, history, missed checks, Handover, services and AMOS of " + victim.title +
                    " will be permanently deleted. Export this profile first.", lang)) },
            confirmButton = {
                TextButton(onClick = {
                    runCatching {
                        profiles.delete(victim.id)
                        store.switchProfile()
                        handover.switchProfile()
                        services.switchProfile()
                        workOrders.switchProfile()
                    }.onSuccess { error = null }
                        .onFailure { error = it.localizedMessage }
                    deleteTarget = null
                }) {
                    Text(localized("Удалить навсегда", "Delete permanently", lang),
                        color = p.systemRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text(localized("Отмена", "Cancel", lang), color = p.text)
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
    services: ServiceStore,
    workOrders: WorkOrderStore,
    p: NavPalette,
    lang: UiLanguage,
) {
    val context = LocalContext.current
    var selectedId by rememberSaveable { mutableStateOf(profiles.activeId) }
    var selectorExpanded by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<NavProfile?>(null) }
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

    NavSettingsPanel(p) {
        Text(localized("Полный обмен профилями", "Full profile transfer", lang),
            color = p.text, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        Text(localized(
            "Выбери профиль для экспорта. Файл содержит расписания, все отметки по датам, пропуски, переносы, Handover, сервисы и AMOS.",
            "Choose the profile to export. It contains all schedules, dated completions, missed and moved tasks, Handover, maintenance and AMOS work orders.",
            lang), color = p.muted, fontSize = 12.sp)
        Text(localized("Профиль для экспорта", "Profile to export", lang),
            color = p.muted, fontSize = 12.sp)
        Box(Modifier.fillMaxWidth()) {
            Row(
                Modifier.fillMaxWidth()
                    .personalNoteCard(p, 13.dp, nested = true)
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
                NavGlassGlyph("chevron-down", p.muted, Modifier.size(17.dp))
            }
            NavPopoverMenu(p = p, 
                expanded = selectorExpanded,
                onDismissRequest = { selectorExpanded = false }, neutralStyle = true,
            ) {
                profiles.profiles.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item.title, color = p.text, fontSize = 14.sp) },
                        leadingIcon = {
                            if (item.id == selectedId)
                                Text("✓", color = NavTodayVisualTokens.accentInk(p.dark), fontSize = 16.sp)
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
        // Keep the profile dropdown visually independent from the actions.
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(9.dp),
            modifier = Modifier.fillMaxWidth()) {
            Box(Modifier.weight(1f).personalNoteCard(p, 13.dp, nested = true)
                .glassClickable {
                    val name = profiles.profiles.first { it.id == selectedId }.title
                        .replace(Regex("[^a-zA-Z0-9а-яА-Я_-]"), "_").take(40)
                    saveFile.launch("NavTasks_" + name + "_" + LocalDate.now() + ".json")
                }.heightIn(min = 46.dp).padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                Text(localized("Экспорт", "Export", lang), color = p.text, fontSize = 13.sp)
            }
            Box(Modifier.weight(1f).personalNoteCard(p, 13.dp, nested = true)
                .glassClickable {
                    openFile.launch(arrayOf("application/json", "text/plain", "application/octet-stream"))
                }.heightIn(min = 46.dp).padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                Text(localized("Импорт", "Import", lang), color = p.text, fontSize = 13.sp)
            }
        }
        Text(localized(
            "Если название профиля совпадает, он заменяется только после подтверждения. Профили с другим названием добавляются отдельно — существующие данные не затрагиваются.",
            "Only a matching profile name can be replaced after confirmation. Different names are added separately without changing existing data.",
            lang), color = p.muted, fontSize = 11.sp)
        val selectedProfile = profiles.profiles.first { it.id == selectedId }
        if (selectedProfile.kind == NavProfileKind.VESSEL) {
            Spacer(Modifier.height(9.dp))
            Box(
                Modifier.fillMaxWidth()
                    .personalNoteCard(p, 13.dp, nested = true)
                    .glassClickable { deleteTarget = selectedProfile }
                    .heightIn(min = 46.dp).padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(localized("Удалить выбранный профиль", "Delete selected profile", lang),
                    color = p.systemRed,
                    fontSize = 13.sp)
            }
        } else {
            Text(localized("«Личное» защищено от удаления.",
                "Personal is protected from deletion.", lang),
                color = p.muted, fontSize = 11.sp)
        }
        feedback?.let { Text(it, color = if (error) p.dangerText else p.successText,
            fontSize = 12.sp) }
    }
    deleteTarget?.let { victim ->
        NavGlassAlertDialog(p = p, neutralSurface = true, neutralActions = true, 
            onDismissRequest = { deleteTarget = null },
            title = { Text(localized("Удалить профиль «" + victim.title + "»?",
                "Delete profile " + victim.title + "?", lang)) },
            text = {
                Text(localized(
                    "Все задачи, история выполнений, пропущенные, Handover, сервисы и AMOS этого судна будут безвозвратно удалены. Это нельзя отменить. Перед удалением экспортируй профиль.",
                    "All tasks, completion history, missed entries, Handover, maintenance and AMOS records for this vessel will be permanently deleted. This cannot be undone. Export the profile first.",
                    lang))
            },
            confirmButton = {
                TextButton(onClick = {
                    runCatching {
                        profiles.delete(victim.id)
                        store.switchProfile()
                        handover.switchProfile()
                        services.switchProfile()
                        workOrders.switchProfile()
                        selectedId = profiles.activeId
                    }.onSuccess {
                        feedback = localized("Профиль удалён.", "Profile deleted.", lang)
                        error = false
                    }.onFailure {
                        feedback = it.localizedMessage ?: localized("Ошибка удаления", "Delete failed", lang)
                        error = true
                    }
                    deleteTarget = null
                }) {
                    Text(localized("Удалить навсегда", "Delete permanently", lang),
                        color = p.systemRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text(localized("Отмена", "Cancel", lang))
                }
            },
        )
    }
    pending?.let { incoming ->
        val matched = profiles.findMatch(incoming.profile)
        val title = incoming.profile.title
        NavGlassAlertDialog(p = p, neutralSurface = true, neutralActions = true, 
            onDismissRequest = { pending = null },
            title = { Text(if (matched == null)
                localized("Добавить новый профиль?", "Add a new profile?", lang)
                else localized("Заменить существующий профиль?", "Replace existing profile?", lang)) },
            text = {
                Column {
                    Text(title + " · " + incoming.templates.size + " " +
                        localized("задач", "tasks", lang) + " · " + incoming.runs.size + " " +
                        localized("отметок", "records", lang) + " · " +
                        incoming.sections.size + " " + localized("разделов", "sections", lang) +
                        " · " + incoming.services.size + " " +
                        localized("сервисов", "services", lang) + " · " +
                        incoming.workOrders.size + " AMOS")
                    if (matched != null) {
                        Spacer(Modifier.height(12.dp))
                        Text(localized(
                            "ВНИМАНИЕ: все текущие задачи, AMOS, записи и история профиля «" +
                                matched.title + "» будут удалены и заменены содержимым файла.",
                            "WARNING: all existing tasks, history and notes in " +
                                matched.title + " will be erased and replaced with the file.",
                            lang), color = p.systemRed)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    runCatching {
                        val result = profiles.importReplace(incoming)
                        store.switchProfile()
                        handover.switchProfile()
                        services.switchProfile()
                        workOrders.switchProfile()
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
                        else localized("Добавить профиль", "Add profile", lang),
                        color = if (matched != null) p.systemRed else NavTodayVisualTokens.accentInk(p.dark))
                }
            },
            dismissButton = {
                TextButton(onClick = { pending = null }) { Text(localized("Отмена", "Cancel", lang)) }
            },
        )
    }
}
