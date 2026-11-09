package com.navtasks.app

import dev.chrisbanes.haze.hazeSource
import com.kyant.backdrop.backdrops.layerBackdrop

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
    focusWorkOrderId: String? = null,
    focusHistoryWorkOrderId: String? = null,
) {
    val context = LocalContext.current
    fun reschedule() { NavNotifications.reschedule(context, tasks, settings) }
    var showClosed by rememberSaveable { mutableStateOf(false) }
    var searchOpen by rememberSaveable { mutableStateOf(false) }
    var searchText by rememberSaveable { mutableStateOf("") }
    val hazeState = remember { dev.chrisbanes.haze.HazeState() }
    // Page content only: floating buttons stay sibling to the recorder.
    val actionBackdrop = if (android.os.Build.VERSION.SDK_INT >= 33)
        com.kyant.backdrop.backdrops.rememberLayerBackdrop() else null
    var selectedFilter by rememberSaveable { mutableStateOf(WorkOverviewFilter.ALL) }
    var completionNotice by remember { mutableStateOf<String?>(null) }
    var editing by remember { mutableStateOf<WorkOrderEntry?>(null) }
    var editorOpen by remember { mutableStateOf(false) }
    var completeTarget by remember { mutableStateOf<WorkOrderEntry?>(null) }
    var completionDate by remember { mutableStateOf(LocalDate.now()) }
    var completionDatePicker by remember { mutableStateOf(false) }
    var chooseShiftAfterCompletion by remember { mutableStateOf(false) }
    var reopenTarget by remember { mutableStateOf<WorkOrderEntry?>(null) }
    var deleteTarget by remember { mutableStateOf<WorkOrderEntry?>(null) }
    var purgeTarget by remember { mutableStateOf<WorkOrderEntry?>(null) }
    var viewingHistory by remember { mutableStateOf<WorkOrderEntry?>(null) }
    var feedback by remember { mutableStateOf<String?>(null) }

    val today = LocalDate.now()
    val activeOrders = workOrders.entries.filter { it.closedAt == null }.sortedWith(
        compareBy<WorkOrderEntry> { it.completed }.thenBy { it.dueDate }.thenBy { it.reference })
    val closedOrders = workOrders.entries.filter { it.closedAt != null }
        .sortedByDescending { it.closedAt }
    val listState = rememberLazyListState()
    val filteredOrders = searchAmosTitles(
        filterAmosOverview(activeOrders, today, selectedFilter), searchText)
    val filteredClosed = searchAmosTitles(closedOrders, searchText)
    LaunchedEffect(focusWorkOrderId, focusHistoryWorkOrderId) {
        if (focusWorkOrderId != null || focusHistoryWorkOrderId != null) {
            searchText = ""
            searchOpen = false
        }
        when {
            focusHistoryWorkOrderId != null -> {
                showClosed = false
                selectedFilter = WorkOverviewFilter.ALL
                viewingHistory = workOrders.entries.firstOrNull {
                    it.id == focusHistoryWorkOrderId
                }
            }
            focusWorkOrderId != null -> {
                showClosed = false
                selectedFilter = WorkOverviewFilter.ALL
                withFrameNanos { }
                val index = activeOrders.indexOfFirst { it.id == focusWorkOrderId }
                if (index >= 0) listState.scrollToItem(2 + index)
            }
        }
    }
    val overdue = filterAmosOverview(activeOrders, today, WorkOverviewFilter.OVERDUE).size
    val soon = filterAmosOverview(activeOrders, today, WorkOverviewFilter.SOON).size

    fun finalizeCompletion(shiftFuture: Boolean) {
        val order = completeTarget ?: return
        runCatching {
            workOrders.complete(order.id, completionDate, shiftFuture)
            reschedule()
        }.onSuccess {
            val next = workOrders.entries.firstOrNull { it.id == order.id }
            completionNotice = if (next?.recurring == true) {
                localized("Работа выполнена: ", "Work completed: ", lang) +
                    navDate(completionDate, lang, true) +
                    localized(" · Следующее выполнение: ", " · Next due: ", lang) +
                    navDate(next.dueDate, lang, true)
            } else {
                localized("Работа выполнена: ", "Work completed: ", lang) +
                    navDate(completionDate, lang, true) +
                    localized(" · История доступна в карточке", " · History available in card", lang)
            }
            completeTarget = null
            chooseShiftAfterCompletion = false
            completionDatePicker = false
            feedback = null
        }.onFailure {
            completionNotice = null
            completeTarget = null
            chooseShiftAfterCompletion = false
            completionDatePicker = false
            feedback = it.localizedMessage
        }
    }

    Box(Modifier.fillMaxSize()) {
    // Edge-to-edge scroll content, matching Today; keep only the Add FAB inset.
    Column(Modifier.fillMaxSize().statusBarsPadding()
        .hazeSource(hazeState)
        .then(if (actionBackdrop != null) Modifier.layerBackdrop(actionBackdrop) else Modifier)) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (actionBackdrop != null) Spacer(Modifier.size(44.dp))
            else NavGlassCircleButton(p, "back", onBack)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                NavBrandLabel(p)
                Text("AMOS · Work Orders", color = p.text, fontSize = 22.sp)
            }
            if (actionBackdrop != null) Spacer(Modifier.size(44.dp))
            else NavGlassCircleButton(p, "search") {
                searchOpen = !searchOpen
                if (!searchOpen) searchText = ""
            }
        }
        if (searchOpen) {
            NavGlassSearchField(
                searchText, { searchText = it }, p, lang,
                placeholder = localized("Поиск по названию AMOS", "Search AMOS titles", lang),
            )
        }
        LazyColumn(
            Modifier.fillMaxSize().dismissSearchImeOnResultsGesture(searchOpen),
            state = listState,
            contentPadding = PaddingValues(start = 16.dp, top = 6.dp, end = 16.dp, bottom = 106.dp),
            verticalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            item {
                NavPanel(p) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        WorkOrderCount(
                            localized("Все", "All", lang), activeOrders.size, p.blue, p,
                            selected = !showClosed && selectedFilter == WorkOverviewFilter.ALL,
                            modifier = Modifier.weight(1f),
                        ) { showClosed = false; selectedFilter = WorkOverviewFilter.ALL }
                        WorkOrderCount(
                            localized("Скоро", "Soon", lang), soon,
                            if (soon > 0) p.warningText else p.muted, p,
                            selected = !showClosed && selectedFilter == WorkOverviewFilter.SOON,
                            modifier = Modifier.weight(1f),
                        ) { showClosed = false; selectedFilter = WorkOverviewFilter.SOON }
                        WorkOrderCount(
                            localized("Просрочено", "Overdue", lang), overdue,
                            if (overdue > 0) p.dangerText else p.muted, p,
                            selected = !showClosed && selectedFilter == WorkOverviewFilter.OVERDUE,
                            modifier = Modifier.weight(1f),
                        ) { showClosed = false; selectedFilter = WorkOverviewFilter.OVERDUE }
                    }
                    Text(
                        localized(
                            "Судовые работы AMOS. Даты и история сохраняются на телефоне.",
                            "AMOS work and history are stored locally on this phone.",
                            lang,
                        ),
                        color = p.muted, fontSize = 11.sp,
                    )
                }
            }
            completionNotice?.let { notice ->
                item(key = "completion-notice") {
                    NavPanel(p) {
                        Text(notice, color = p.successText, fontSize = 13.sp)
                    }
                }
            }
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(9.dp),
                ) {
                    WorkOrderChip(
                        localized("Работы · ${activeOrders.size}", "Work · ${activeOrders.size}", lang),
                        !showClosed, p, Modifier.weight(1f),
                    ) { showClosed = false }
                    WorkOrderChip(
                        localized("Закрытые · ${closedOrders.size}", "Closed · ${closedOrders.size}", lang),
                        showClosed, p, Modifier.weight(1f),
                    ) { showClosed = true }
                }
            }
            feedback?.let { msg ->
                item { Text(msg, color = p.dangerText, fontSize = 12.sp) }
            }
            val visible = if (showClosed) filteredClosed else filteredOrders
            if (visible.isEmpty()) {
                item {
                    NavPanel(p) {
                        Text(
                            if (searchText.isNotBlank())
                                localized("Работы с таким названием не найдены.",
                                    "No work orders match this title.", lang)
                            else if (showClosed)
                                localized("Закрытых работ пока нет.", "No closed work orders yet.", lang)
                            else if (selectedFilter != WorkOverviewFilter.ALL)
                                localized("В этом фильтре работ нет.", "No work orders match this filter.", lang)
                            else localized("Работ пока нет. Добавь запись кнопкой +.",
                                "No open work orders. Add one with +.", lang),
                            color = p.muted, fontSize = 13.sp,
                        )
                    }
                }
            }
            if (showClosed) {
                items(filteredClosed, key = { it.id }) { order ->
                    WorkOrderClosedCard(
                        order, p, lang,
                        onHistory = { viewingHistory = order },
                        onRestore = {
                            runCatching { workOrders.restore(order.id); reschedule() }
                                .onFailure { feedback = it.localizedMessage }
                        },
                        onPurge = { purgeTarget = order },
                    )
                }
            } else {
                items(filteredOrders, key = { it.id }) { order ->
                    WorkOrderCard(
                        order, today, p, lang,
                        onHistory = { viewingHistory = order },
                        onEdit = { editing = order; editorOpen = true },
                        onComplete = {
                            completeTarget = order
                            completionDate = LocalDate.now()
                            chooseShiftAfterCompletion = false
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
    if (actionBackdrop != null) {
        Box(Modifier.align(Alignment.TopStart).statusBarsPadding()
            .padding(start = 16.dp, top = 9.dp)) {
            NavLiquidGlassCircleButton(p, actionBackdrop, "back", onBack)
        }
        Box(Modifier.align(Alignment.TopEnd).statusBarsPadding()
            .padding(end = 16.dp, top = 9.dp)) {
            NavLiquidGlassCircleButton(p, actionBackdrop, "search") {
                searchOpen = !searchOpen
                if (!searchOpen) searchText = ""
            }
        }
    }
    Box(
        Modifier.align(Alignment.BottomEnd).navigationBarsPadding()
            .padding(end = 19.dp, bottom = 18.dp),
    ) {
        if (actionBackdrop != null) {
            NavLiquidGlassAddButton(p, actionBackdrop) {
            editing = null
            editorOpen = true
            }
        } else {
            NavGlassAddButton(p, hazeState) {
            editing = null
            editorOpen = true
            }
        }
    }
    } // End work screen; floating Add retains its separate glass overlay.

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

    completeTarget?.takeUnless { chooseShiftAfterCompletion }?.let { order ->
        NavGlassAlertDialog(
            p = p,
            onDismissRequest = {
                completeTarget = null
                completionDatePicker = false
                chooseShiftAfterCompletion = false
            },
            title = { Text(localized("Завершить работу?", "Complete work order?", lang)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(order.title, color = p.text, fontSize = 14.sp)
                    if (order.recurring) {
                        Text(localized(
                            "Выполнение сохранится в истории. Повторения идут от даты начала работы.",
                            "Completion is saved to history. Recurrence follows the original start date.",
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
                    if (order.recurring && completionDate != order.dueDate) {
                        completionDatePicker = false
                        chooseShiftAfterCompletion = true
                    } else {
                        finalizeCompletion(false)
                    }
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

    if (chooseShiftAfterCompletion) {
        completeTarget?.let { order ->
            NavGlassAlertDialog(
                p = p,
                onDismissRequest = { chooseShiftAfterCompletion = false },
                title = {
                    Text(localized("Сместить график AMOS?", "Shift AMOS schedule?", lang))
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                        Text(order.title, color = p.text, fontSize = 14.sp)
                        Text(
                            localized("По графику: ", "Scheduled: ", lang) +
                                navDate(order.dueDate, lang, true),
                            color = p.muted, fontSize = 12.sp,
                        )
                        Text(
                            localized("Выполнено: ", "Completed: ", lang) +
                                navDate(completionDate, lang, true),
                            color = p.text, fontSize = 12.sp,
                        )
                        Text(
                            localized(
                                "Сместить — будущие повторения пойдут от выбранной даты. " +
                                    "Оставить — прежние дни останутся без изменений. " +
                                    "Фактическая дата выполнения сохранится в истории в обоих случаях.",
                                "Shift moves future repetitions to the selected date. " +
                                    "Keep preserves the original schedule. " +
                                    "The actual completion date is archived either way.",
                                lang,
                            ), color = p.muted, fontSize = 12.sp,
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { finalizeCompletion(true) }) {
                        Text(localized("Сместить график", "Shift schedule", lang))
                    }
                },
                dismissButton = {
                    Column(horizontalAlignment = Alignment.End) {
                        TextButton(onClick = { finalizeCompletion(false) }) {
                            Text(localized("Оставить график", "Keep schedule", lang))
                        }
                        TextButton(onClick = { chooseShiftAfterCompletion = false }) {
                            Text(localized("Назад", "Back", lang))
                        }
                    }
                },
            )
        }
    }

    reopenTarget?.let { order ->
        WorkOrderConfirm(
            p = p, lang = lang,
            title = localized("Вернуть в открытые?", "Reopen work order?", lang),
            description = order.title,
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
            title = localized("Переместить в закрытые?", "Move to Closed?", lang),
            description = order.title + "\n" + localized(
                "Запись и вся история сохранятся. Из раздела «Закрытые» её можно восстановить.",
                "The work order and its history are kept. You can restore them from Closed.",
                lang,
            ),
            onCancel = { deleteTarget = null },
            onConfirm = {
                runCatching { workOrders.delete(order.id); reschedule() }
                    .onFailure { feedback = it.localizedMessage }
                deleteTarget = null
            },
        )
    }
    purgeTarget?.let { order ->
        WorkOrderConfirm(
            p = p, lang = lang,
            title = localized("Удалить навсегда?", "Permanently delete?", lang),
            description = order.title + "\n" + localized(
                "Запись, график и ВСЯ история выполнения будут удалены без возможности восстановления.",
                "The work order, schedule and ALL completion history will be erased permanently.",
                lang,
            ),
            onCancel = { purgeTarget = null },
            onConfirm = {
                runCatching { workOrders.deletePermanently(order.id); reschedule() }
                    .onFailure { feedback = it.localizedMessage }
                purgeTarget = null
            },
        )
    }
    viewingHistory?.let { entry ->
        val current = workOrders.entries.firstOrNull { it.id == entry.id } ?: entry
        val history = workOrderHistoryRecords(listOf(current))
        NavGlassAlertDialog(
            p = p, onDismissRequest = { viewingHistory = null },
            title = { Text(localized("История AMOS", "AMOS history", lang)) },
            text = {
                Column(
                    Modifier.heightIn(max = 400.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(current.title, color = p.text, fontSize = 15.sp)
                    if (history.isEmpty()) {
                        Text(localized(
                            "Выполнений пока нет. После «Готово» здесь появится дата и история.",
                            "No completions yet. Dates and history appear here after Done.",
                            lang,
                        ), color = p.muted, fontSize = 13.sp)
                    } else {
                        history.forEach { record ->
                            key(record.completion.id) {
                                WorkOrderHistoryCard(record, p, lang) {
                                    viewingHistory = null
                                    reopenTarget = current
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewingHistory = null }) {
                    Text(localized("Закрыть", "Close", lang))
                }
            },
        )
    }
}

/** Closed means explicitly removed, not completed. Preserves all cycles. */
@Composable
private fun WorkOrderClosedCard(
    order: WorkOrderEntry, p: NavPalette, lang: UiLanguage,
    onHistory: () -> Unit, onRestore: () -> Unit, onPurge: () -> Unit,
) {
    Column(
        Modifier.fillMaxWidth().navGlass(p, radius = 18.dp, role = GlassRole.TASK)
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Column(Modifier.fillMaxWidth().glassClickable(onClick = onHistory)
            .padding(vertical = 3.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text("AMOS", color = p.blueText, fontSize = 11.sp)
            Text(order.title, color = p.text, fontSize = 15.sp)
            order.closedAt?.let {
                Text(localized("Закрыта: ", "Closed: ", lang) +
                    navDate(it, lang, true), color = p.muted, fontSize = 12.sp)
            }
            Text(
                localized("История выполнений: ", "Completions: ", lang) +
                    workOrderHistoryRecords(listOf(order)).size,
                color = p.muted, fontSize = 12.sp,
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            WorkOrderChip(localized("Восстановить", "Restore", lang), false, p,
                modifier = Modifier.weight(1f), onClick = onRestore)
            WorkOrderChip(localized("Удалить навсегда", "Delete forever", lang), false, p,
                modifier = Modifier.weight(1f), destructive = true, onClick = onPurge)
        }
    }
}

@Composable
private fun WorkOrderCount(
    label: String, number: Int, color: Color, p: NavPalette,
    selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit,
) {
    Column(
        modifier.navGlass(p, radius = 14.dp,
            role = if (selected) GlassRole.SELECTED else GlassRole.CONTROL)
            .glassClickable(onClick = onClick)
            .heightIn(min = 75.dp)
            .padding(horizontal = 8.dp, vertical = 9.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(number.toString(), color = if (selected) p.selectionContent else color, fontSize = 21.sp)
        Text(label, color = if (selected) p.selectionContent else p.muted, fontSize = 11.sp,
            maxLines = 1)
    }
}

@Composable
private fun WorkOrderCard(
    order: WorkOrderEntry, today: LocalDate, p: NavPalette, lang: UiLanguage,
    onHistory: () -> Unit, onEdit: () -> Unit, onComplete: () -> Unit,
    onToggleCheck: (String, Boolean) -> Unit,
    onReopen: () -> Unit, onDelete: () -> Unit,
) {
    var selectedNote by remember(order.id) { mutableStateOf<CheckItem?>(null) }
    val status = workOrderState(order, today)
    val accent = when (status) {
        WorkOrderState.COMPLETED -> p.successText
        WorkOrderState.OVERDUE -> p.dangerText
        WorkOrderState.SOON -> p.warningText
        WorkOrderState.OPEN -> p.blue
    }
    Column(
        Modifier.fillMaxWidth().navGlass(p, radius = 18.dp, role = GlassRole.TASK)
            .glassClickable(onClick = onHistory)
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(
                Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text("AMOS", color = p.blueText, fontSize = 12.sp)
                Text(order.title, color = p.text, fontSize = 15.sp,
                    maxLines = 3, overflow = TextOverflow.Ellipsis)
            }
            NavGlassGlyph("chevron-right", p.muted, Modifier.size(17.dp))
        }
        val latestCompleted = order.executionHistory.lastOrNull()?.completedAt
            ?: order.completedAt
        Text(
            if (latestCompleted == null)
                localized("Последнее выполнение: ещё не выполнялась",
                    "Last completed: not yet completed", lang)
            else localized("Последнее выполнение: ", "Last completed: ", lang) +
                navDate(latestCompleted, lang, true),
            color = if (latestCompleted == null) p.muted else p.successText,
            fontSize = 12.sp,
        )
        if (!order.completed) {
            Text(
                localized(
                    if (order.recurring) "Следующее выполнение: " else "Плановая дата: ",
                    if (order.recurring) "Next due: " else "Scheduled date: ", lang,
                ) + navDate(order.dueDate, lang, true),
                color = accent, fontSize = 12.sp,
            )
        }
        if (order.comment.isNotBlank()) {
            NavRichNoteText(order.comment, order.commentHtml, p, fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth())
        }
        Text(
            when (status) {
                WorkOrderState.COMPLETED -> localized("Завершено", "Completed", lang)
                WorkOrderState.OVERDUE -> localized("Просрочено", "Overdue", lang)
                WorkOrderState.SOON -> localized("Срок близко", "Due soon", lang)
                WorkOrderState.OPEN -> localized("Открыто", "Open", lang)
            },
            color = accent, fontSize = 12.sp,
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            WorkOrderChip(localized("Изменить", "Edit", lang), false, p,
                modifier = Modifier.weight(1f), onClick = onEdit)
            WorkOrderChip(
                if (order.completed) localized("Вернуть", "Reopen", lang)
                else localized("Готово", "Done", lang),
                false, p, modifier = Modifier.weight(1f),
                onClick = if (order.completed) onReopen else onComplete,
            )
            WorkOrderChip(localized("Удалить", "Delete", lang), false, p,
                modifier = Modifier.weight(1f), destructive = true, onClick = onDelete)
        }
    }
    selectedNote?.let { item ->
        ChecklistReferenceDialog(p, lang, item.title, item.comment,
            noteHtml = item.commentHtml, editable = false,
            onDismiss = { selectedNote = null })
    }
}

@Composable
private fun WorkOrderChip(
    label: String, selected: Boolean, p: NavPalette,
    modifier: Modifier = Modifier, destructive: Boolean = false,
    onClick: () -> Unit,
) {
    Box(
        modifier.navGlass(p, radius = 12.dp,
            role = if (selected) GlassRole.SELECTED else GlassRole.CONTROL,
            elevation = 0.dp)
            .glassClickable(onClick = onClick)
            .heightIn(min = 44.dp)
            .padding(horizontal = 9.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = if (selected) p.selectionContent
            else if (destructive) p.systemRed else p.text,
            fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
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
    var title by remember(existing?.id) { mutableStateOf(existing?.title.orEmpty()) }
    var dueDate by remember(existing?.id) {
        mutableStateOf(existing?.startDate ?: existing?.dueDate ?: LocalDate.now())
    }
    var comment by remember(existing?.id) { mutableStateOf(existing?.comment.orEmpty()) }
    var commentHtml by remember(existing?.id) { mutableStateOf(existing?.commentHtml.orEmpty()) }
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
                Text(localized("Название работы", "Job title", lang),
                    color = p.muted, fontSize = 12.sp)
                NavGlassInput(title, { title = it.take(200) }, p,
                    placeholder = localized("Описание задания", "Work description", lang),
                    modifier = Modifier.fillMaxWidth())
                Text(localized("Дата начала работы", "Work start date", lang),
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
                    "Повторения отсчитываются от выбранной даты начала, независимо от даты выполнения.",
                    "Repeats follow the selected start date, not the completion date.",
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
                Text(localized("Комментарий", "Notes", lang),
                    color = p.muted, fontSize = 12.sp)
                NavRichNoteEditor(
                    initialPlain = existing?.comment.orEmpty(),
                    initialHtml = existing?.commentHtml.orEmpty(),
                    entryKey = existing?.id ?: "new-amos",
                    p = p, lang = lang, maxCharacters = 6000,
                    placeholder = localized("Детали, замечания…", "Details and remarks…", lang),
                    onChange = { text, html -> comment = text; commentHtml = html },
                )
                if (existing?.completedAt != null) {
                    Text(
                        localized("Завершено: ", "Completed: ", lang) +
                            navDate(existing.completedAt, lang, true),
                        color = p.successText, fontSize = 12.sp)
                }
                error?.let { Text(it, color = p.dangerText, fontSize = 12.sp) }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically) {
                WorkOrderChip(localized("Отмена", "Cancel", lang), false, p, onClick = onDismiss)
                Spacer(Modifier.width(8.dp))
                WorkOrderChip(localized("Сохранить", "Save", lang), true, p) {
                    val candidate = runCatching {
                        require(navRichNoteAllowed(comment, commentHtml, 6000))
                        if (customInterval) {
                            require(customMonths.toIntOrNull()?.let { it in 1..1200 } == true) {
                                "Choose 1 to 1200 months"
                            }
                        }
                        WorkOrderEntry(
                            id = existing?.id ?: UUID.randomUUID().toString(),
                            reference = existing?.reference.orEmpty(),
                            title = title.trim(),
                            dueDate = if (existing != null &&
                                    dueDate == (existing.startDate ?: existing.dueDate) &&
                                    existing.repeatDays == repeatDays &&
                                    existing.repeatMonths == repeatMonths) existing.dueDate
                                else dueDate,
                            startDate = dueDate,
                            createdAt = existing?.createdAt ?: LocalDate.now(),
                            completedAt = existing?.completedAt,
                            comment = comment.trim(),
                            commentHtml = commentHtml,
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
                            "Проверь название работы, дату начала и периодичность.",
                            "Check work title, start date and recurrence.", lang)
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
                p = p, lang = lang, title = item.title, note = item.comment,
                noteHtml = item.commentHtml, editable = true,
                onDismiss = { noteItemId = null },
                onSave = { note, html ->
                    checklist = checklist.map { if (it.id == id) it.copy(comment = note, commentHtml = html) else it }
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
        Text("AMOS", color = p.blueText, fontSize = 12.sp)
        Text(archive.order.title, color = p.text, fontSize = 15.sp)
        Text(localized("Срок: ", "Due: ", lang) +
            navDate(completion.dueDate, lang, true),
            color = p.muted, fontSize = 12.sp)
        Text(localized("Выполнено: ", "Completed: ", lang) +
            navDate(completion.completedAt, lang, true),
            color = p.successText, fontSize = 12.sp)
        if (archive.canUndo) {
            WorkOrderChip(
                localized("Отменить выполнение", "Undo completion", lang), false, p,
                onClick = onReopen,
            )
        }
    }
}
