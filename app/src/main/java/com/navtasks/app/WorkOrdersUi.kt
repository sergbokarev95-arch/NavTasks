package com.navtasks.app

import dev.chrisbanes.haze.hazeSource
import com.kyant.backdrop.backdrops.layerBackdrop

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.border
import androidx.compose.ui.draw.blur
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
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
    val activeQuery = if (searchOpen) navEffectiveSearchQuery(searchText) else ""
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
        filterAmosOverview(activeOrders, today, selectedFilter), activeQuery)
    val filteredClosed = searchAmosTitles(closedOrders, activeQuery)
    val visibleResults = if (showClosed) filteredClosed else filteredOrders
    val searchResults = if (activeQuery.isNotBlank())
        visibleResults else emptyList()
    var searchResultIndex by rememberSaveable { mutableIntStateOf(0) }
    var searchNavigationSerial by remember { mutableIntStateOf(0) }
    LaunchedEffect(searchText, showClosed, selectedFilter) {
        searchResultIndex = 0
    }
    LaunchedEffect(searchNavigationSerial) {
        if (searchNavigationSerial > 0 && searchResults.isNotEmpty()) {
            val pos = searchResultIndex.coerceIn(0, searchResults.lastIndex)
            // Counts, tabs and transient banners occupy the first LazyColumn items.
            listState.animateScrollToItem(
                2 + (if (completionNotice != null) 1 else 0) +
                    (if (feedback != null) 1 else 0) + pos,
            )
        }
    }
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

    // Dialogs are separate windows: blur only the page beneath them.
    val modalOpen = editorOpen || completeTarget != null || chooseShiftAfterCompletion ||
        reopenTarget != null || deleteTarget != null || purgeTarget != null || viewingHistory != null
    Box(Modifier.fillMaxSize().blur(if (modalOpen) 4.dp else 0.dp)) {
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
            Text("AMOS / By Crew", modifier = Modifier.weight(1f),
                color = p.text, fontSize = 24.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (actionBackdrop != null) Spacer(Modifier.size(44.dp))
            else NavGlassCircleButton(p, "search") {
                searchOpen = !searchOpen
                if (!searchOpen) searchText = ""
            }
        }
        NavAnimatedSearchField(
            searchOpen = searchOpen,
            value = searchText,
            onValueChange = { searchText = it },
            onDismiss = { searchOpen = false; searchText = "" },
            p = p, lang = lang,
            placeholder = localized("Поиск по работам", "Search work names", lang),
            neutralStyle = true,
            highContrastField = true,
            resultCount = searchResults.size,
            currentResult = searchResultIndex,
            onPreviousResult = {
                searchResultIndex = navSearchNextIndex(searchResultIndex, searchResults.size, -1)
                searchNavigationSerial++
            },
            onNextResult = {
                searchResultIndex = navSearchNextIndex(searchResultIndex, searchResults.size, 1)
                searchNavigationSerial++
            },
        )
        LazyColumn(
            Modifier.fillMaxSize()
                .revealSearchOnSettledTopPull(listState, searchOpen) { searchOpen = true }
                .dismissSearchImeOnResultsGesture(searchOpen),
            state = listState,
            contentPadding = PaddingValues(start = NavEventLayout.screenInset, top = 6.dp, end = NavEventLayout.screenInset,
                bottom = navFloatingActionBottomPadding(embeddedInTab = false, edgeToEdgeStandalone = true)),
            verticalArrangement = Arrangement.spacedBy(NavEventLayout.sectionGap),
        ) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(NavEventLayout.innerGap)) {
                    WorkOrderCount(
                        localized("Все", "All", lang), activeOrders.size, p.text, p,
                        selected = !showClosed && selectedFilter == WorkOverviewFilter.ALL,
                        modifier = Modifier.weight(1f),
                    ) { showClosed = false; selectedFilter = WorkOverviewFilter.ALL }
                    WorkOrderCount(
                        localized("Скоро", "Soon", lang), soon,
                        if (soon > 0) p.systemOrange else p.muted, p,
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
            }
            completionNotice?.let { notice ->
                item(key = "completion-notice") {
                    NavAmosPanel(p) {
                        Text(notice, color = p.successText, fontSize = 13.sp)
                    }
                }
            }
            item {
                NavCalendarSegmentedControl(
                    labels = listOf(
                        localized("Работы · ${activeOrders.size}", "Work · ${activeOrders.size}", lang),
                        localized("Закрытые · ${closedOrders.size}", "Closed · ${closedOrders.size}", lang),
                    ),
                    activeIndex = if (showClosed) 1 else 0, p = p,
                ) { index -> showClosed = index == 1 }
            }
            feedback?.let { msg ->
                item { Text(msg, color = p.dangerText, fontSize = 12.sp) }
            }
            if (visibleResults.isEmpty()) {
                item {
                    NavAmosPanel(p) {
                        Text(
                            if (activeQuery.isNotBlank())
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
                        searchQuery = activeQuery,
                        focusedSearchResult = searchResults.getOrNull(searchResultIndex)?.id == order.id,
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
                        searchQuery = activeQuery,
                        focusedSearchResult = searchResults.getOrNull(searchResultIndex)?.id == order.id,
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
            p = p, neutralActions = true, neutralSurface = true,
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
                    WorkOrderChip(navDate(completionDate, lang, true), false, p,
                        modifier = Modifier.fillMaxWidth(), outlined = true) {
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
                    Text(localized("Завершить", "Complete", lang), color = NavTodayVisualTokens.accentInk(p.dark))
                }
            },
            dismissButton = {
                TextButton(onClick = { completeTarget = null }) {
                    Text(localized("Отмена", "Cancel", lang), color = p.text)
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
                p = p, neutralActions = true, neutralSurface = true,
                onDismissRequest = { chooseShiftAfterCompletion = false },
                title = {
                    Text(localized("Сместить график работы?", "Shift work schedule?", lang))
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
                        Text(localized("Сместить график", "Shift schedule", lang), color = NavTodayVisualTokens.accentInk(p.dark))
                    }
                },
                dismissButton = {
                    Column(horizontalAlignment = Alignment.End) {
                        TextButton(onClick = { finalizeCompletion(false) }) {
                            Text(localized("Оставить график", "Keep schedule", lang), color = p.text)
                        }
                        TextButton(onClick = { chooseShiftAfterCompletion = false }) {
                            Text(localized("Назад", "Back", lang), color = p.text)
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
            destructive = true,
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
            p = p, neutralActions = true, neutralSurface = true, onDismissRequest = { viewingHistory = null },
            title = { Text(localized("История работы", "Work history", lang)) },
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
                    Text(localized("Закрыть", "Close", lang), color = NavTodayVisualTokens.accentInk(p.dark))
                }
            },
        )
    }
}

@Composable
private fun NavAmosPanel(p: NavPalette, content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxWidth().personalNoteCard(p, NavEventLayout.cardRadius)
            .padding(NavEventLayout.cardInset),
        verticalArrangement = Arrangement.spacedBy(NavEventLayout.innerGap),
        content = content,
    )
}

/** Closed means explicitly removed, not completed. Preserves all cycles. */
@Composable
private fun WorkOrderClosedCard(
    order: WorkOrderEntry, p: NavPalette, lang: UiLanguage,
    searchQuery: String = "", focusedSearchResult: Boolean = false,
    onHistory: () -> Unit, onRestore: () -> Unit, onPurge: () -> Unit,
) {
    Column(
        Modifier.fillMaxWidth().personalNoteCard(p, NavEventLayout.cardRadius)
            .padding(horizontal = NavEventLayout.cardInset, vertical = NavEventLayout.groupGap),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Column(Modifier.fillMaxWidth().glassClickable(onClick = onHistory)
            .padding(vertical = 3.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(localized("Работа", "Work", lang), color = p.muted, fontSize = 11.sp)
            NavSearchMarkedText(order.title, searchQuery, p, fontSize = 15.sp,
                focused = focusedSearchResult)
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
        modifier.personalNoteCard(p, NavEventLayout.cardRadius)
            .then(if (selected) Modifier.border(1.5.dp,
                NavTodayVisualTokens.accentInk(p.dark), RoundedCornerShape(NavEventLayout.cardRadius))
                else Modifier)
            .glassClickable(onClick = onClick)
            .heightIn(min = 66.dp)
            .padding(horizontal = 6.dp, vertical = NavEventLayout.innerGap),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(number.toString(), color = color, fontSize = 21.sp, fontWeight = FontWeight.Medium)
        Text(label, color = if (selected) NavTodayVisualTokens.accentInk(p.dark) else p.muted, fontSize = 11.sp,
            maxLines = 1)
    }
}

@Composable
private fun WorkOrderCard(
    order: WorkOrderEntry, today: LocalDate, p: NavPalette, lang: UiLanguage,
    searchQuery: String = "", focusedSearchResult: Boolean = false,
    onHistory: () -> Unit, onEdit: () -> Unit, onComplete: () -> Unit,
    onToggleCheck: (String, Boolean) -> Unit,
    onReopen: () -> Unit, onDelete: () -> Unit,
) {
    var selectedNote by remember(order.id) { mutableStateOf<CheckItem?>(null) }
    val status = workOrderState(order, today)
    val accent = when (status) {
        WorkOrderState.COMPLETED -> p.successText
        WorkOrderState.OVERDUE -> p.dangerText
        WorkOrderState.SOON -> p.systemOrange
        WorkOrderState.OPEN -> p.muted
    }
    Column(
        Modifier.fillMaxWidth().personalNoteCard(p, NavEventLayout.cardRadius)
            .glassClickable(onClick = onHistory)
            .padding(horizontal = NavEventLayout.cardInset, vertical = NavEventLayout.groupGap),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(
                Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(localized("Работа", "Work", lang), color = p.muted, fontSize = 12.sp)
                NavSearchMarkedText(
                    value = order.title, query = searchQuery, p = p,
                    fontSize = 15.sp, maxLines = 3, overflow = TextOverflow.Ellipsis,
                    focused = focusedSearchResult,
                )
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
    outlined: Boolean = false,
    onClick: () -> Unit,
) {
    Box(
        modifier.personalNoteCard(p, 12.dp, nested = true)
            .then(if (selected) Modifier.border(1.5.dp,
                NavTodayVisualTokens.accentInk(p.dark), RoundedCornerShape(12.dp))
                else if (outlined) Modifier.border(1.dp, NavTodayVisualTokens.checklistInsetRim(p.dark), RoundedCornerShape(12.dp))
                else Modifier)
            .glassClickable(onClick = onClick)
            .heightIn(min = NavEventLayout.choiceHeight)
            .padding(horizontal = 9.dp, vertical = NavEventLayout.innerGap),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = if (selected) NavTodayVisualTokens.accentInk(p.dark)
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
            Modifier.fillMaxWidth().personalNoteCard(p, 26.dp)
                .padding(NavEventLayout.cardInset),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                if (existing == null) localized("Новая работа", "New work item", lang)
                else localized("Редактировать работу", "Edit work item", lang),
                color = p.text, fontSize = 20.sp,
            )
            Column(
                Modifier.fillMaxWidth().weight(1f, fill = false).heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(localized("Название работы", "Job title", lang),
                    color = p.muted, fontSize = 12.sp)
                NavGlassInput(title, { title = it.take(200) }, p,
                    placeholder = localized("Описание задания", "Work description", lang),
                    modifier = Modifier.fillMaxWidth(), neutralStyle = true)
                Text(localized("Дата начала работы", "Work start date", lang),
                    color = p.muted, fontSize = 12.sp)
                WorkOrderChip(navDate(dueDate, lang, true), false, p,
                    Modifier.fillMaxWidth(), outlined = true) { pickingDate = true }
                Text(localized("Повторение", "Recurrence", lang),
                    color = p.muted, fontSize = 12.sp)
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
                NavEventOptionGrid(
                    options = presets.map { (days, months, label) -> "$days:$months" to label } +
                        ("custom" to localized("Свой срок", "Custom months", lang)),
                    selected = if (customInterval) "custom" else "$repeatDays:$repeatMonths",
                    p = p, columns = 3,
                ) { key ->
                    if (key == "custom") {
                        customInterval = true
                        repeatDays = 0
                        repeatMonths = customMonths.toIntOrNull()?.takeIf { it in 1..1200 } ?: 9
                    } else {
                        presets.firstOrNull { (days, months, _) -> "$days:$months" == key }?.let { (days, months, _) ->
                            customInterval = false
                            repeatDays = days
                            repeatMonths = months
                        }
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
                        placeholder = "9", neutralStyle = true,
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
                NavEventOptionGrid(
                    options = listOf(0, 1, 3, 7, 14, 30, 90).map { days ->
                        days.toString() to if (days == 0) localized("Нет", "Off", lang)
                            else localized("$days дн.", "$days days", lang)
                    },
                    selected = remindDays.toString(), p = p, columns = 4,
                ) { key -> remindDays = key.toInt() }
                Text(localized("Комментарий", "Notes", lang),
                    color = p.muted, fontSize = 12.sp)
                Column(Modifier.fillMaxWidth().personalNoteCard(p, 14.dp, nested = true)
                    .padding(NavEventLayout.innerGap)) {
                    NavRichNoteEditor(
                        initialPlain = existing?.comment.orEmpty(),
                        initialHtml = existing?.commentHtml.orEmpty(),
                        entryKey = existing?.id ?: "new-amos",
                        p = p, lang = lang, maxCharacters = 6000, neutralStyle = true,
                        placeholder = localized("Детали, замечания…", "Details and remarks…", lang),
                        onChange = { text, html -> comment = text; commentHtml = html },
                    )
                }
                if (existing?.completedAt != null) {
                    Text(
                        localized("Завершено: ", "Completed: ", lang) +
                            navDate(existing.completedAt, lang, true),
                        color = p.successText, fontSize = 12.sp)
                }
                error?.let { Text(it, color = p.dangerText, fontSize = 12.sp) }
            }
            NavEventDivider(p)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(NavEventLayout.innerGap),
                verticalAlignment = Alignment.CenterVertically) {
                WorkOrderChip(localized("Отмена", "Cancel", lang), false, p,
                    modifier = Modifier.weight(1f), outlined = true, onClick = onDismiss)
                WorkOrderChip(localized("Сохранить", "Save", lang), true, p,
                    modifier = Modifier.weight(1f)) {
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
    destructive: Boolean = false,
) {
    NavGlassAlertDialog(
        p = p, neutralActions = true, neutralSurface = true, onDismissRequest = onCancel,
        title = { Text(title) },
        text = { Text(description, color = p.muted, fontSize = 14.sp) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(localized("Подтвердить", "Confirm", lang), color = if (destructive) p.systemRed else NavTodayVisualTokens.accentInk(p.dark))
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text(localized("Отмена", "Cancel", lang), color = p.text)
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
        Modifier.fillMaxWidth().personalNoteCard(p, NavEventLayout.cardRadius, nested = true)
            .padding(NavEventLayout.cardInset),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(localized("Работа", "Work", lang), color = p.muted, fontSize = 12.sp)
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
