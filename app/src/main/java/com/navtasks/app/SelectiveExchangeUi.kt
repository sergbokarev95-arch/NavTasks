package com.navtasks.app

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.IOException
import java.time.LocalDate

private enum class ShareGroup { TASKS, WORK, HANDOVER }
private enum class ShareWork { AMOS, SERVICES }

@Composable
private fun ShareOption(
    label: String, selected: Boolean, p: NavPalette,
    modifier: Modifier = Modifier, onClick: () -> Unit,
) {
    Box(modifier.navGlass(p, radius = 12.dp,
        role = if (selected) GlassRole.SELECTED else GlassRole.CONTROL)
        .glassClickable(onClick = onClick).heightIn(min = 44.dp)
        .padding(horizontal = 8.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center) {
        Text(label, color = if (selected) p.selectionContent else p.text, fontSize = 12.sp,
            maxLines = 1)
    }
}

@Composable
internal fun SelectiveExchangePanel(
    profiles: ProfileStore, tasks: TaskStore, handover: HandoverStore,
    services: ServiceStore, workOrders: WorkOrderStore, settings: NavSettings,
    p: NavPalette, lang: UiLanguage,
) {
    val context = LocalContext.current
    var choosing by remember { mutableStateOf(false) }
    var group by remember { mutableStateOf(ShareGroup.TASKS) }
    var work by remember { mutableStateOf(ShareWork.AMOS) }
    var frequency by remember { mutableStateOf(Frequency.DAILY) }
    var selected by remember { mutableStateOf<Set<String>>(emptySet()) }
    var exporting by remember { mutableStateOf<SelectiveBundle?>(null) }
    var pending by remember { mutableStateOf<SelectiveBundle?>(null) }
    var feedback by remember { mutableStateOf<String?>(null) }
    var failed by remember { mutableStateOf(false) }

    val options: List<Pair<String, String>> = when (group) {
        ShareGroup.TASKS -> tasks.templates.filter {
            TaskSchedule.effectiveFrequency(it, LocalDate.now()) == frequency
        }.map { it.id to it.title }
        ShareGroup.WORK -> when (work) {
            ShareWork.AMOS -> workOrders.entries.filter { it.closedAt == null }
                .map { it.id to it.title }
            ShareWork.SERVICES -> services.entries.filter { it.closedAt == null }
                .map { it.id to it.equipment }
        }
        ShareGroup.HANDOVER -> handover.sections.map {
            it.id to (it.title + " · " + it.entries.size)
        }
    }
    val shownIds = options.map { it.first }.toSet()
    val checked = selected.intersect(shownIds)
    fun selectedBundle(): SelectiveBundle = when (group) {
        ShareGroup.TASKS -> SelectiveBundle(
            tasks = tasks.templates.filter { it.id in checked })
        ShareGroup.HANDOVER -> SelectiveBundle(
            sections = handover.sections.filter { it.id in checked })
        ShareGroup.WORK -> when (work) {
            ShareWork.AMOS -> SelectiveBundle(
                workOrders = workOrders.entries.filter { it.id in checked })
            ShareWork.SERVICES -> SelectiveBundle(
                services = services.entries.filter { it.id in checked })
        }
    }

    val exportFile = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        val bundle = exporting
        if (uri != null && bundle != null) {
            runCatching {
                context.contentResolver.openOutputStream(uri)
                    ?.bufferedWriter(Charsets.UTF_8)?.use {
                        it.write(SelectiveTransfer.export(bundle))
                    } ?: throw IOException("Cannot create file")
            }.onSuccess {
                feedback = localized("Выбранные записи сохранены в файл.",
                    "Selected records exported.", lang)
                failed = false
                choosing = false
                selected = emptySet()
            }.onFailure {
                feedback = it.localizedMessage
                failed = true
            }
        }
        exporting = null
    }
    val importFile = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.openInputStream(uri)
                    ?.bufferedReader(Charsets.UTF_8)?.use { reader ->
                        val buffer = CharArray(4096)
                        val raw = StringBuilder()
                        while (true) {
                            val size = reader.read(buffer)
                            if (size < 0) break
                            if (raw.length + size > SelectiveTransfer.MAX_BYTES)
                                throw IOException("File exceeds 10 MB")
                            raw.append(buffer, 0, size)
                        }
                        SelectiveTransfer.parse(raw.toString())
                    } ?: throw IOException("Cannot open file")
            }.onSuccess {
                pending = it
                feedback = null
            }.onFailure {
                failed = true
                feedback = it.localizedMessage
            }
        }
    }

    NavPanel(p) {
        Text(localized("Выборочный обмен", "Selective exchange", lang),
            color = p.text, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(4.dp))
        Text(localized(
            "Передавай только выбранные задачи, работы AMOS, сервисы или разделы Handover. История выполнений не передаётся.",
            "Share only selected tasks, AMOS, maintenance or Handover sections. Completion history is excluded.",
            lang,
        ), color = p.muted, fontSize = 12.sp)
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            ShareOption(localized("Экспорт", "Export", lang), false, p,
                Modifier.weight(1f)) {
                group = ShareGroup.TASKS
                selected = emptySet()
                choosing = true
            }
            ShareOption(localized("Импорт", "Import", lang), false, p,
                Modifier.weight(1f)) {
                importFile.launch(arrayOf("application/json", "text/plain",
                    "application/octet-stream"))
            }
        }
        Text(localized(
            "Импорт добавляет отсутствующие записи, не заменяя существующие данные и отметки.",
            "Import adds missing records without replacing local data or completed marks.",
            lang,
        ), color = p.muted, fontSize = 11.sp)
        feedback?.let {
            Text(it, color = if (failed) p.dangerText else p.successText,
                fontSize = 12.sp)
        }
    }

    if (choosing) NavGlassAlertDialog(
        p = p, onDismissRequest = { choosing = false },
        title = { Text(localized("Что отправить?", "What to share?", lang)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    ShareOption(localized("Задачи", "Tasks", lang),
                        group == ShareGroup.TASKS, p, Modifier.weight(1f)) {
                        group = ShareGroup.TASKS; selected = emptySet()
                    }
                    ShareOption(localized("Работы", "Work", lang),
                        group == ShareGroup.WORK, p, Modifier.weight(1f)) {
                        group = ShareGroup.WORK; selected = emptySet()
                    }
                    ShareOption("Handover", group == ShareGroup.HANDOVER,
                        p, Modifier.weight(1f)) {
                        group = ShareGroup.HANDOVER; selected = emptySet()
                    }
                }
                if (group == ShareGroup.TASKS) Row(
                    Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Frequency.entries.forEach { f ->
                        ShareOption(when (f) {
                            Frequency.DAILY -> localized("День", "Day", lang)
                            Frequency.WEEKLY -> localized("Неделя", "Week", lang)
                            Frequency.MONTHLY -> localized("Месяц", "Month", lang)
                            Frequency.EXTRA -> localized("Разовые", "Once", lang)
                        }, frequency == f, p, Modifier.weight(1f)) {
                            frequency = f; selected = emptySet()
                        }
                    }
                }
                if (group == ShareGroup.WORK) Row(
                    Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    ShareOption("AMOS", work == ShareWork.AMOS, p, Modifier.weight(1f)) {
                        work = ShareWork.AMOS; selected = emptySet()
                    }
                    ShareOption(localized("Сервисы", "Services", lang),
                        work == ShareWork.SERVICES, p, Modifier.weight(1f)) {
                        work = ShareWork.SERVICES; selected = emptySet()
                    }
                }
                Text(localized("Выбрано: ", "Selected: ", lang) + checked.size,
                    color = p.blue, fontSize = 12.sp)
                if (options.isEmpty()) Text(
                    localized("В этой категории пока пусто.", "No records in this category.", lang),
                    color = p.muted, fontSize = 12.sp)
                else {
                    Text(
                        if (checked.size == options.size)
                            localized("Снять выделение", "Clear selection", lang)
                        else localized("Выбрать все", "Select all", lang),
                        modifier = Modifier.glassClickable {
                            selected = if (checked.size == options.size)
                                emptySet() else shownIds
                        }.padding(vertical = 6.dp),
                        color = p.blue, fontSize = 12.sp,
                    )
                    options.forEach { (id, label) ->
                        Row(
                            Modifier.fillMaxWidth()
                                .navGlass(p, radius = 12.dp, role = GlassRole.CONTROL)
                                .glassClickable {
                                    selected = if (id in selected) selected - id
                                        else selected + id
                                }.padding(horizontal = 12.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            NavGlassGlyph(if (id in selected) "check" else "plus",
                                if (id in selected) p.successText else p.muted,
                                Modifier.size(19.dp))
                            Spacer(Modifier.width(10.dp))
                            Text(label, color = p.text, fontSize = 13.sp,
                                modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(enabled = checked.isNotEmpty(), onClick = {
                exporting = selectedBundle()
                exportFile.launch("NavTasks_Selected_" +
                    group.name.lowercase() + "_" + LocalDate.now() + ".json")
            }) {
                Text(localized("Экспорт", "Export", lang) + " · " + checked.size)
            }
        },
        dismissButton = {
            TextButton(onClick = { choosing = false }) {
                Text(localized("Отмена", "Cancel", lang))
            }
        },
    )
    pending?.let { bundle ->
        val inspection = runCatching {
            mergeSelective(profiles.snapshot(profiles.activeId), bundle)
        }
        NavGlassAlertDialog(
            p = p, onDismissRequest = { pending = null },
            title = { Text(localized("Импорт записей", "Import selected items", lang)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Text(localized(
                        "Файл: " + bundle.tasks.size + " задач, " +
                            bundle.workOrders.size + " AMOS, " +
                            bundle.services.size + " сервисов, " +
                            bundle.sections.size + " разделов Handover.",
                        "File: " + bundle.tasks.size + " tasks, " +
                            bundle.workOrders.size + " AMOS, " +
                            bundle.services.size + " services, " +
                            bundle.sections.size + " Handover sections.",
                        lang,
                    ), color = p.text, fontSize = 13.sp)
                    inspection.onSuccess { result ->
                        val n = result.added
                        Text(localized(
                            "Добавится: " + n.tasks + " задач, " + n.amos + " AMOS, " +
                                n.services + " сервисов, " + n.sections +
                                " разделов и " + n.handoverEntries + " пунктов Handover.",
                            "Add: " + n.tasks + " tasks, " + n.amos + " AMOS, " +
                                n.services + " services, " + n.sections +
                                " sections and " + n.handoverEntries + " Handover items.",
                            lang,
                        ), color = p.successText, fontSize = 13.sp)
                    }.onFailure {
                        Text(it.localizedMessage ?: "Invalid import",
                            color = p.dangerText, fontSize = 13.sp)
                    }
                    Text(localized(
                        "История выполнения не импортируется. Плановые даты новых работ рекомендуется проверить.",
                        "Completion history is excluded. Please review the new work due dates.",
                        lang,
                    ), color = p.muted, fontSize = 12.sp)
                }
            },
            confirmButton = {
                TextButton(enabled = inspection.getOrNull()?.added?.total?.let { it > 0 } == true,
                    onClick = {
                        runCatching {
                            val added = profiles.mergeSelected(bundle)
                            tasks.switchProfile()
                            handover.switchProfile()
                            services.switchProfile()
                            workOrders.switchProfile()
                            NavNotifications.reschedule(context, tasks, settings)
                            added
                        }.onSuccess { n ->
                            feedback = localized(
                                "Добавлено: " + n.tasks + " задач, " + n.amos + " AMOS, " +
                                    n.services + " сервисов, " + n.handoverEntries + " пунктов Handover.",
                                "Added: " + n.tasks + " tasks, " + n.amos + " AMOS, " +
                                    n.services + " services, " + n.handoverEntries + " Handover items.",
                                lang,
                            )
                            failed = false
                            pending = null
                        }.onFailure {
                            feedback = it.localizedMessage
                            failed = true
                            pending = null
                        }
                    },
                ) { Text(localized("Добавить", "Add", lang)) }
            },
            dismissButton = {
                TextButton(onClick = { pending = null }) {
                    Text(localized("Отмена", "Cancel", lang))
                }
            },
        )
    }
}
