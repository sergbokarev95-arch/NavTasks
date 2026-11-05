package com.navtasks.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
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
    p: NavPalette,
    lang: UiLanguage,
    onBack: () -> Unit,
) {
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
    val history = workOrders.entries.filter { it.completed }.sortedWith(
        compareByDescending<WorkOrderEntry> { it.completedAt }.thenBy { it.reference })
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
            items(visible, key = { it.id }) { order ->
                WorkOrderCard(
                    order, today, p, lang,
                    onEdit = { editing = order; editorOpen = true },
                    onComplete = {
                        completeTarget = order
                        completionDate = LocalDate.now()
                    },
                    onReopen = { reopenTarget = order },
                    onDelete = { deleteTarget = order },
                )
            }
        }
    }

    if (editorOpen) {
        WorkOrderEditorDialog(
            existing = editing, p = p, lang = lang,
            onDismiss = { editorOpen = false },
            onSave = { candidate ->
                runCatching { workOrders.upsert(candidate) }.exceptionOrNull()?.localizedMessage
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
                    Text(localized("Дата выполнения", "Completion date", lang),
                        color = p.muted, fontSize = 12.sp)
                    WorkOrderChip(navDate(completionDate, lang, true), false, p) {
                        completionDatePicker = true
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    runCatching { workOrders.complete(order.id, completionDate) }
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
                runCatching { workOrders.reopen(order.id) }
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
                runCatching { workOrders.delete(order.id) }
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
    onReopen: () -> Unit, onDelete: () -> Unit,
) {
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
        if (order.comment.isNotBlank()) {
            Text(order.comment, color = p.muted, fontSize = 12.sp,
                maxLines = 2, overflow = TextOverflow.Ellipsis)
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
                        WorkOrderEntry(
                            id = existing?.id ?: UUID.randomUUID().toString(),
                            reference = reference.trim(),
                            title = title.trim(),
                            dueDate = dueDate,
                            createdAt = existing?.createdAt ?: LocalDate.now(),
                            completedAt = existing?.completedAt,
                            comment = comment.trim(),
                        )
                    }.getOrElse {
                        error = localized(
                            "Укажи номер AMOS и название работы.",
                            "Enter an AMOS number and job title.", lang)
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
