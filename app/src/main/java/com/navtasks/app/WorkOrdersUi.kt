package com.navtasks.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.time.LocalDate
import java.util.UUID

/** Manual offline AMOS register. No synchronization with the actual ship AMOS. */
@Composable
fun WorkOrdersScreen(
    workOrders: WorkOrderStore,
    tasks: TaskStore,
    settings: NavSettings,
    p: NavPalette,
    lang: UiLanguage,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    fun reschedule() { NavNotifications.reschedule(context, tasks, settings) }
    var showHistory by rememberSaveable { mutableStateOf(false) }
    var editing by remember { mutableStateOf<WorkOrderEntry?>(null) }
    var editorOpen by remember { mutableStateOf(false) }
    var completeTarget by remember { mutableStateOf<WorkOrderEntry?>(null) }
    var completionDate by remember { mutableStateOf(LocalDate.now()) }
    var completionDatePicker by remember { mutableStateOf(false) }
    var reopenTarget by remember { mutableStateOf<WorkOrderEntry?>(null) }
    var deleteTarget by remember { mutableStateOf<WorkOrderEntry?>(null) }
    var feedback by remember { mutableStateOf<String?>(null) }

    val today = LocalDate.now()
    val openOrders = workOrders.entries.filterNot { it.completed }.sortedWith(
        compareBy<WorkOrderEntry> { it.dueDate }.thenBy { it.reference })
    val history = workOrderHistoryRecords(workOrders.entries)
    val overdue = openOrders.count { it.dueDate.isBefore(today) }
    val soon = openOrders.count { workOrderState(it, today) == WorkOrderState.SOON }

    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NavGlassCircleButton(p, "back", onBack)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("NAVTASKS", color = p.blue, fontSize = 12.sp)
                Text("AMOS · Work Orders", color = p.text, fontSize = 22.sp)
            }
            NavGlassCircleButton(p, "plus") {
                editing = null
                editorOpen = true
            }
        }
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, top = 6.dp, end = 16.dp, bottom = 42.dp),
            verticalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            item {
                NavPanel(p) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        WorkOrderCount(localized("Открыто", "Open", lang), openOrders.size, p.blue, p)
                        WorkOrderCount(localized("Скоро", "Soon", lang), soon,
                            if (soon > 0) Color(0xFFE6AD72) else p.muted, p)
                        WorkOrderCount(localized("Просрочено", "Overdue", lang), overdue,
                            if (overdue > 0) Color(0xFFE98B84) else p.muted, p)
                    }
                    Text(
                        localized(
                            "Ручной учёт номеров AMOS. Данные хранятся только на телефоне.",
                            "Manual AMOS register. Data is stored locally on this phone.",
                            lang,
                        ),
                        color = p.muted, fontSize = 11.sp,
                    )
                }
            }
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(9.dp),
                ) {
                    WorkOrderChip(
                        localized("Открытые · ${openOrders.size}", "Open · ${openOrders.size}", lang),
                        !showHistory, p, Modifier.weight(1f),
                    ) { showHistory = false }
                    WorkOrderChip(
                        localized("История · ${history.size}", "History · ${history.size}", lang),
                        showHistory, p, Modifier.weight(1f),
                    ) { showHistory = true }
                }
            }
            feedback?.let { msg ->
                item { Text(msg, color = p.muted, fontSize = 12.sp) }
            }
            val visible = if (showHistory) history else openOrders
            if (visible.isEmpty()) {
                item {
                    NavPanel(p) {
                        Text(
                            if (showHistory)
                                localized("Завершённых работ пока нет.", "No completed work orders yet.", lang)
                            else localized("Работ пока нет. Добавь запись кнопкой +.",
                                "No open work orders. Add one with +.", lang),
                            color = p.muted, fontSize = 13.sp,
                        )
                    }
                }
            }
            if (showHistory) {
                items(history, key = {
                    it.order.id + "@" + (if (it.order.completed) "oneoff" else it.completion.id)
                }) { archive ->
                    WorkOrderHistoryCard(archive, p, lang) {
                        reopenTarget = archive.order
                    }
                }
            } else {
                items(openOrders, key = { it.id }) { order ->
                    WorkOrderCard(
                        order, today, p, lang,
                        onEdit = { editing = order; editorOpen = true },
                        onComplete = {
                            if (workOrderCanComplete(order)) {
                                completeTarget = order
                                completionDate = LocalDate.now()
                            } else {
                                val (done, total) = workOrderMandatoryProgress(order)
                                feedback = localized(
                                    "Отметь обязательные пункты: $done/$total.",
                                    "Complete required checklist items first: $done/$total.", lang)
                            }
                        },
                        onToggleCheck = { id, checked ->
                            runCatching { workOrders.setCheck(order.id, id, checked) }
                                .onFailure { feedback = it.localizedMessage }
                        },
                        onReopen = { reopenTarget = order },
                        onDelete = { deleteTarget = order },
                    )
                }
            }
        }
    }

    if (editorOpen) {
        WorkOrderEditorDialog(
            existing = editing, p = p, lang = lang,
            onDismiss = { editorOpen = false },
            onSave = { candidate ->
                runCatching { workOrders.upsert(candidate); reschedule() }
                    .exceptionOrNull()?.localizedMessage
            },
        )
    }

    completeTarget?.let { order ->
        NavGlassAlertDialog(
            p = p,
            onDismissRequest = { completeTarget = null; completionDatePicker = false },
            title = { Text(localized("Завершить работу?", "Complete work order?", lang)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(order.reference + " · " + order.title, color = p.text, fontSize = 14.sp)
                    if (order.recurring) {
                        Text(localized(
                            "Текущий чек-лист сохранится в истории. Следующий срок рассчитывается от фактической даты.",
                            "Current checklist is archived. Next due is calculated from completion date.",
                            lang,
                        ), color = p.muted, fontSize = 12.sp)
                    }
                    Text(localized("Дата выполнения", "Completion date", lang),
                        color = p.muted, fontSize = 12.sp)
                    WorkOrderChip(navDate(completionDate, lang, true), false, p) {
                        completionDatePicker = true
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    runCatching { workOrders.complete(order.id, completionDate); reschedule() }
                        .onSuccess { completeTarget = null; feedback = null }
                        .onFailure { feedback = it.localizedMessage; completeTarget = null }
                }) {
                    Text(localized("Завершить", "Complete", lang))
                }
            },
            dismissButton = {
                TextButton(onClick = { completeTarget = null }) {
                    Text(localized("Отмена", "Cancel", lang))
                }
            },
        )
        if (completionDatePicker) {
            NavDateWheelDialog(
                initial = completionDate, p = p, lang = lang,
                onDismiss = { completionDatePicker = false },
                onSelected = { completionDate = it; completionDatePicker = false },
            )
        }
    }

    reopenTarget?.let { order ->
        WorkOrderConfirm(
            p = p, lang = lang,
            title = localized("Вернуть в открытые?", "Reopen work order?", lang),
            description = order.reference + " · " + order.title,
            onCancel = { reopenTarget = null },
            onConfirm = {
                runCatching { workOrders.reopen(order.id); reschedule() }
                    .onFailure { feedback = it.localizedMessage }
                reopenTarget = null
            },
        )
    }
    deleteTarget?.let { order ->
        WorkOrderConfirm(
            p = p, lang = lang,
            title = localized("Удалить запись AMOS?", "Delete AMOS record?", lang),
            description = order.reference + " · " + order.title +
                "\n" + localized("Это удалит и сохранённую дату завершения.",
                    "The saved completion date will also be deleted.", lang),
            onCancel = { deleteTarget = null },
            onConfirm = {
                runCatching { workOrders.delete(order.id); reschedule() }
                    .onFailure { feedback = it.localizedMessage }
                deleteTarget = null
            },
        )
    }
}

@Composable
private fun WorkOrderCount(label: String, number: Int, color: Color, p: NavPalette) {
    Column {
        Text(number.toString(), color = color, fontSize = 21.sp)
        Text(label, color = p.muted, fontSize = 11.sp)
    }
}

@Composable
private fun WorkOrderCard(
    order: WorkOrderEntry, today: LocalDate, p: NavPalette, lang: UiLanguage,
    onEdit: () -> Unit, onComplete: () -> Unit,
    onToggleCheck: (String, Boolean) -> Unit,
    onReopen: () -> Unit, onDelete: () -> Unit,
) {
    var selectedNote by remember(order.id) { mutableStateOf<CheckItem?>(null) }
    val status = workOrderState(order, today)
    val accent = when (status) {
        WorkOrderState.COMPLETED -> p.successText
        WorkOrderState.OVERDUE -> Color(0xFFE98B84)
        WorkOrderState.SOON -> Color(0xFFE6AD72)
        WorkOrderState.OPEN -> p.blue
    }
    Column(
        Modifier.fillMaxWidth().navGlass(p, radius = 18.dp, role = GlassRole.TASK)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(
                Modifier.weight(1f).glassClickable(onClick = onEdit),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(order.reference, color = p.blue, fontSize = 12.sp)
                Text(order.title, color = p.text, fontSize = 15.sp,
                    maxLines = 3, overflow = TextOverflow.Ellipsis)
            }
            NavGlassGlyph("chevron-right", p.muted, Modifier.size(17.dp))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                localized("Срок: ", "Due: ", lang) + navDate(order.dueDate, lang, true),
                color = if (order.completed) p.muted else accent,
                fontSize = 12.sp,
            )
        }
        if (order.completedAt != null) {
            Text(localized("Выполнено: ", "Completed: ", lang) +
                navDate(order.completedAt, lang, true),
                color = p.successText, fontSize = 12.sp)
        }
        if (order.recurring) {
            Text(localized("Повторяется после выполнения", "Repeats after completion", lang),
                color = p.muted, fontSize = 11.sp)
        }
        if (order.comment.isNotBlank()) {
            Text(order.comment, color = p.muted, fontSize = 12.sp,
                maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        if (order.checklist.isNotEmpty()) {
            val (done, total) = workOrderMandatoryProgress(order)
            Text(localized("Чек-лист $done/$total", "Checklist $done/$total", lang),
                color = p.blue, fontSize = 12.sp)
            order.checklist.forEach { item ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    val checked = item.id in order.checkedItemIds
                    if (order.completed) {
                        Text(if (checked) "✓" else "○", color = if (checked) p.successText else p.muted,
                            fontSize = 18.sp, modifier = Modifier.width(29.dp))
                    } else {
                        RoundCheck(checked, p, { onToggleCheck(item.id, !checked) })
                    }
                    Text(item.title, color = if (checked) p.muted else p.text,
                        fontSize = 12.sp, modifier = Modifier.weight(1f),
                        maxLines = 2, overflow = TextOverflow.Ellipsis)
                    if (item.comment.isNotBlank()) {
                        ChecklistReferenceButton(p, true) { selectedNote = item }
                    }
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            Text(
                when (status) {
                    WorkOrderState.COMPLETED -> localized("Завершено", "Completed", lang)
                    WorkOrderState.OVERDUE -> localized("Просрочено", "Overdue", lang)
                    WorkOrderState.SOON -> localized("Срок близко", "Due soon", lang)
                    WorkOrderState.OPEN -> localized("Открыто", "Open", lang)
                },
                color = accent, fontSize = 11.sp, modifier = Modifier.weight(1f),
            )
            WorkOrderChip(localized("Изменить", "Edit", lang), false, p, onClick = onEdit)
            WorkOrderChip(
                if (order.completed) localized("Вернуть", "Reopen", lang)
                else localized("Готово", "Done", lang),
                false, p, onClick = if (order.completed) onReopen else onComplete,
            )
            WorkOrderChip(localized("Удалить", "Delete", lang), false, p, onClick = onDelete)
        }
    }
    selectedNote?.let { item ->
        ChecklistReferenceDialog(p, lang, item.title, item.comment, editable = false,
            onDismiss = { selectedNote = null })
    }
}

@Composable
private fun WorkOrderChip(
    label: String, selected: Boolean, p: NavPalette,
    modifier: Modifier = Modifier, onClick: () -> Unit,
) {
    Box(
        modifier.navGlass(p, radius = 12.dp,
            role = if (selected) GlassRole.SELECTED else GlassRole.CONTROL,
            elevation = 0.dp)
            .glassClickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = if (selected) p.blue else p.text,
            fontSize = 11.sp, maxLines = 1)
    }
}

@Composable
private fun WorkOrderEditorDialog(
    existing: WorkOrderEntry?,
    p: NavPalette,
    lang: UiLanguage,
    onDismiss: () -> Unit,
    onSave: (WorkOrderEntry) -> String?,
) {
    var reference by remember(existing?.id) { mutableStateOf(existing?.reference.orEmpty()) }
    var title by remember(existing?.id) { mutableStateOf(existing?.title.orEmpty()) }
    var dueDate by remember(existing?.id) {
        mutableStateOf(existing?.dueDate ?: LocalDate.now().plusDays(7))
    }
    var comment by remember(existing?.id) { mutableStateOf(existing?.comment.orEmpty()) }
    var checklist by remember(existing?.id) { mutableStateOf(existing?.checklist.orEmpty()) }
    var remindDays by remember(existing?.id) { mutableIntStateOf(existing?.remindDaysBefore ?: 0) }
    var repeatDays by remember(existing?.id) { mutableIntStateOf(existing?.repeatDays ?: 0) }
    var repeatMonths by remember(existing?.id) { mutableIntStateOf(existing?.repeatMonths ?: 0) }
    val presetMonths = listOf(0, 1, 3, 6, 12, 18, 36, 60)
    var customInterval by remember(existing?.id) {
        mutableStateOf(repeatDays == 0 && repeatMonths !in presetMonths)
    }
    var customMonths by remember(existing?.id) {
        mutableStateOf((if (repeatMonths > 0) repeatMonths else 9).toString())
    }
    var noteItemId by remember(existing?.id) { mutableStateOf<String?>(null) }
    var pickingDate by remember(existing?.id) { mutableStateOf(false) }
    var error by remember(existing?.id) { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier.fillMaxWidth().navDialogSurface(p).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                if (existing == null) localized("Новая работа AMOS", "New AMOS work order", lang)
                else localized("Редактировать AMOS", "Edit AMOS work order", lang),
                color = p.text, fontSize = 20.sp,
            )
            Column(
                Modifier.fillMaxWidth().heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(localized("Номер Work Order", "Work order number", lang),
                    color = p.muted, fontSize = 12.sp)
                NavGlassInput(reference, { reference = it.take(80) }, p,
                    placeholder = "WO-0001", modifier = Modifier.fillMaxWidth())
                Text(localized("Название работы", "Job title", lang),
                    color = p.muted, fontSize = 12.sp)
                NavGlassInput(title, { title = it.take(200) }, p,
                    placeholder = localized("Описание задания", "Work description", lang),
                    modifier = Modifier.fillMaxWidth())
                Text(localized("Срок выполнения", "Due date", lang),
                    color = p.muted, fontSize = 12.sp)
                WorkOrderChip(navDate(dueDate, lang, true), false, p,
                    Modifier.fillMaxWidth()) { pickingDate = true }
                Text(localized("Повторение", "Recurrence", lang),
                    color = p.muted, fontSize = 12.sp)
                androidx.compose.foundation.layout.FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    val presets = listOf(
                        Triple(0, 0, localized("Разовая", "One-off", lang)),
                        Triple(7, 0, localized("Неделя", "Weekly", lang)),
                        Triple(0, 1, localized("1 мес.", "1 month", lang)),
                        Triple(0, 3, localized("3 мес.", "3 months", lang)),
                        Triple(0, 6, localized("6 мес.", "6 months", lang)),
                        Triple(0, 12, localized("12 мес.", "12 months", lang)),
                        Triple(0, 18, localized("18 мес.", "18 months", lang)),
                        Triple(0, 36, localized("36 мес.", "36 months", lang)),
                        Triple(0, 60, localized("60 мес.", "60 months", lang)),
                    )
                    presets.forEach { (days, months, label) ->
                        WorkOrderChip(label,
                            !customInterval && repeatDays == days && repeatMonths == months, p,
                        ) {
                            customInterval = false
                            repeatDays = days
                            repeatMonths = months
                        }
                    }
                    WorkOrderChip(localized("Свой срок", "Custom months", lang), customInterval, p) {
                        customInterval = true
                        repeatDays = 0
                        repeatMonths = customMonths.toIntOrNull()?.takeIf { it in 1..1200 } ?: 9
                    }
                }
                if (customInterval) {
                    Text(localized("Месяцев (1–1200)", "Months (1–1200)", lang),
                        color = p.muted, fontSize = 11.sp)
                    NavGlassInput(
                        value = customMonths,
                        onValueChange = { value ->
                            if (value.length <= 4 && value.all(Char::isDigit)) {
                                customMonths = value
                                value.toIntOrNull()?.takeIf { it in 1..1200 }?.let {
                                    repeatMonths = it
                                }
                            }
                        },
                        p = p, modifier = Modifier.fillMaxWidth(),
                        placeholder = "9",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                }
                Text(localized(
                    "Для повторяющейся работы следующий срок считается от даты выполнения. История остаётся.",
                    "The next cycle is due after the actual completion date. History is retained.",
                    lang,
                ), color = p.muted, fontSize = 11.sp)
                Text(localized("Предупредить заранее", "Remind before due", lang),
                    color = p.muted, fontSize = 12.sp)
                androidx.compose.foundation.layout.FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(0, 1, 3, 7, 14, 30, 90).forEach { days ->
                        WorkOrderChip(
                            if (days == 0) localized("Нет", "Off", lang)
                            else localized("$days дн.", "$days days", lang),
                            remindDays == days, p,
                        ) { remindDays = days }
                    }
                }
                if (existing?.completedAt == null) {
                    ChecklistEditorPanel(
                        items = checklist,
                        onItemsChange = { checklist = it },
                        onEditNote = { noteItemId = it },
                        p = p, lang = lang,
                        completionHint = localized(
                            "Для AMOS отметь обязательные пункты, затем нажми «Готово» в работе.",
                            "For AMOS, check required items, then tap Done on the work order.", lang),
                    )
                } else if (checklist.isNotEmpty()) {
                    Text(localized("Чек-лист завершённой работы доступен только для просмотра.",
                            "Archived checklist is read-only. Reopen to edit.", lang),
                        color = p.muted, fontSize = 12.sp)
                }
                Text(localized("Комментарий", "Notes", lang),
                    color = p.muted, fontSize = 12.sp)
                BasicTextField(
                    value = comment,
                    onValueChange = { comment = it.take(6000) },
                    singleLine = false,
                    maxLines = 6,
                    textStyle = TextStyle(color = p.text, fontSize = 14.sp),
                    cursorBrush = SolidColor(p.blue),
                    modifier = Modifier.fillMaxWidth().height(90.dp)
                        .navGlass(p, radius = 12.dp, role = GlassRole.CONTROL),
                    decorationBox = { input ->
                        Box(Modifier.fillMaxSize().padding(12.dp),
                            contentAlignment = Alignment.TopStart) {
                            if (comment.isBlank()) {
                                Text(localized("Детали, замечания…", "Details and remarks…", lang),
                                    color = p.muted, fontSize = 12.sp)
                            }
                            input()
                        }
                    },
                )
                if (existing?.completedAt != null) {
                    Text(
                        localized("Завершено: ", "Completed: ", lang) +
                            navDate(existing.completedAt, lang, true),
                        color = p.successText, fontSize = 12.sp)
                }
                error?.let { Text(it, color = Color(0xFFE98B84), fontSize = 12.sp) }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically) {
                WorkOrderChip(localized("Отмена", "Cancel", lang), false, p, onClick = onDismiss)
                Spacer(Modifier.width(8.dp))
                WorkOrderChip(localized("Сохранить", "Save", lang), true, p) {
                    val candidate = runCatching {
                        if (customInterval) {
                            require(customMonths.toIntOrNull()?.let { it in 1..1200 } == true) {
                                "Choose 1 to 1200 months"
                            }
                        }
                        WorkOrderEntry(
                            id = existing?.id ?: UUID.randomUUID().toString(),
                            reference = reference.trim(),
                            title = title.trim(),
                            dueDate = dueDate,
                            createdAt = existing?.createdAt ?: LocalDate.now(),
                            completedAt = existing?.completedAt,
                            comment = comment.trim(),
                            checklist = checklist.filter { it.title.isNotBlank() },
                            checkedItemIds = existing?.checkedItemIds.orEmpty().intersect(
                                checklist.filter { it.title.isNotBlank() }.map { it.id }.toSet()),
                            remindDaysBefore = remindDays,
                            repeatDays = if (customInterval) 0 else repeatDays,
                            repeatMonths = if (customInterval)
                                customMonths.toInt() else repeatMonths,
                            executionHistory = existing?.executionHistory.orEmpty(),
                        )
                    }.getOrElse {
                        error = localized(
                            "Проверь номер, название работы и интервал повторения.",
                            "Check AMOS number, title and recurrence interval.", lang)
                        return@WorkOrderChip
                    }
                    val issue = onSave(candidate)
                    if (issue == null) onDismiss() else error = issue
                }
            }
        }
    }
    if (pickingDate) {
        NavDateWheelDialog(
            initial = dueDate, p = p, lang = lang,
            onDismiss = { pickingDate = false },
            onSelected = { dueDate = it; pickingDate = false },
        )
    }
    noteItemId?.let { id ->
        val item = checklist.firstOrNull { it.id == id }
        if (item != null) {
            ChecklistReferenceDialog(
                p = p, lang = lang, title = item.title, note = item.comment, editable = true,
                onDismiss = { noteItemId = null },
                onSave = { note ->
                    checklist = checklist.map { if (it.id == id) it.copy(comment = note) else it }
                    noteItemId = null
                },
            )
        }
    }
}

@Composable
private fun WorkOrderConfirm(
    p: NavPalette, lang: UiLanguage,
    title: String, description: String,
    onCancel: () -> Unit, onConfirm: () -> Unit,
) {
    NavGlassAlertDialog(
        p = p, onDismissRequest = onCancel,
        title = { Text(title) },
        text = { Text(description, color = p.muted, fontSize = 14.sp) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(localized("Подтвердить", "Confirm", lang))
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text(localized("Отмена", "Cancel", lang))
            }
        },
    )
}

/** The archived occurrence is a snapshot; later checklist edits cannot rewrite it. */
@Composable
private fun WorkOrderHistoryCard(
    archive: WorkOrderHistoryRecord,
    p: NavPalette,
    lang: UiLanguage,
    onReopen: () -> Unit,
) {
    val completion = archive.completion
    Column(
        Modifier.fillMaxWidth().navGlass(p, radius = 18.dp, role = GlassRole.TASK)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(archive.order.reference, color = p.blue, fontSize = 12.sp)
        Text(archive.order.title, color = p.text, fontSize = 15.sp)
        Text(localized("Срок: ", "Due: ", lang) +
            navDate(completion.dueDate, lang, true),
            color = p.muted, fontSize = 12.sp)
        Text(localized("Выполнено: ", "Completed: ", lang) +
            navDate(completion.completedAt, lang, true),
            color = p.successText, fontSize = 12.sp)
        if (completion.checklist.isNotEmpty()) {
            Text(localized("Чек-лист: ", "Checklist: ", lang) +
                completion.checkedItemIds.size + "/" + completion.checklist.size,
                color = p.muted, fontSize = 12.sp)
        }
        if (archive.canUndo) {
            WorkOrderChip(
                localized("Отменить выполнение", "Undo completion", lang), false, p,
                onClick = onReopen,
            )
        }
    }
}
