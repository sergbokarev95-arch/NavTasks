package com.navtasks.app

import dev.chrisbanes.haze.hazeSource
import com.kyant.backdrop.backdrops.layerBackdrop

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.activity.compose.BackHandler
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.draw.blur
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID

@Composable
fun ServicesScreen(
    services: ServiceStore, tasks: TaskStore, settings: NavSettings,
    p: NavPalette, lang: UiLanguage, onBack: () -> Unit,
    focusServiceId: String? = null,
) {
    val context = LocalContext.current
    val now = LocalDate.now()
    var showEditor by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<ServiceEntry?>(null) }
    var confirmingDelete by remember { mutableStateOf<ServiceEntry?>(null) }
    var confirmingPurge by remember { mutableStateOf<ServiceEntry?>(null) }
    var selectedView by rememberSaveable { mutableStateOf(ServiceListView.WORKS) }
    var searchOpen by rememberSaveable { mutableStateOf(false) }
    var searchText by rememberSaveable { mutableStateOf("") }
    val activeQuery = if (searchOpen) navEffectiveSearchQuery(searchText) else ""
    val hazeState = remember { dev.chrisbanes.haze.HazeState() }
    // Page content only: floating buttons stay sibling to the recorder.
    val actionBackdrop = if (android.os.Build.VERSION.SDK_INT >= 33)
        com.kyant.backdrop.backdrops.rememberLayerBackdrop() else null
    var requestTarget by remember { mutableStateOf<ServiceEntry?>(null) }
    var confirmingComplete by remember { mutableStateOf<ServiceEntry?>(null) }
    var completionDate by remember { mutableStateOf(LocalDate.now()) }
    var completionDatePicker by remember { mutableStateOf(false) }
    var viewingHistory by remember { mutableStateOf<ServiceEntry?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var selectedFilter by rememberSaveable { mutableStateOf(WorkOverviewFilter.ALL) }
    val activeServices = services.entries.filter { it.closedAt == null }.sortedWith(
        compareBy<ServiceEntry> { it.completedAt != null }
            .thenBy { if (it.unplanned) LocalDate.MIN else it.nextDate ?: LocalDate.MAX })
    val closedServices = services.entries.filter { it.closedAt != null }
        .sortedByDescending { it.closedAt }
    // A request is a CURRENT uncompleted service with an AMOS reference:
    // both planned and unplanned, never historical or closed records.
    val activeRequests = currentServiceRequests(activeServices)
    val listState = rememberLazyListState()
    val filteredServices = searchServiceTitles(
        filterServiceOverview(
            if (selectedView == ServiceListView.REQUESTS) activeRequests else activeServices,
            now, selectedFilter,
        ), activeQuery)
    val filteredClosed = searchServiceTitles(closedServices, activeQuery)
    val visibleResults = if (selectedView == ServiceListView.CLOSED)
        filteredClosed else filteredServices
    val searchResults = if (activeQuery.isNotBlank())
        visibleResults else emptyList()
    var searchResultIndex by rememberSaveable { mutableIntStateOf(0) }
    var searchNavigationSerial by remember { mutableIntStateOf(0) }
    LaunchedEffect(searchText, selectedView, selectedFilter) { searchResultIndex = 0 }
    LaunchedEffect(searchNavigationSerial) {
        if (searchNavigationSerial > 0 && searchResults.isNotEmpty()) {
            listState.animateScrollToItem(
                2 + (if (message != null) 1 else 0) +
                    searchResultIndex.coerceIn(0, searchResults.lastIndex),
            )
        }
    }
    LaunchedEffect(focusServiceId) {
        if (focusServiceId != null) {
            searchText = ""
            searchOpen = false
            selectedView = ServiceListView.WORKS
            selectedFilter = WorkOverviewFilter.ALL
            withFrameNanos { }
            val index = activeServices.indexOfFirst { it.id == focusServiceId }
            if (index >= 0) listState.scrollToItem(2 + index)
        }
    }
    val overdue = filterServiceOverview(activeServices, now, WorkOverviewFilter.OVERDUE).size
    val soon = filterServiceOverview(activeServices, now, WorkOverviewFilter.SOON).size

    fun reschedule() { NavNotifications.reschedule(context, tasks, settings) }

    val modalOpen = showEditor || requestTarget != null || confirmingDelete != null ||
        confirmingPurge != null || confirmingComplete != null || viewingHistory != null
    Box(Modifier.fillMaxSize()) {
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
            else NavGlassCircleButton(p, "back", onClick = onBack)
            Spacer(Modifier.width(12.dp))
            Text(localized("Сервисы", "Maintenance", lang), color = p.text,
                fontSize = 24.sp, modifier = Modifier.weight(1f), maxLines = 1,
                overflow = TextOverflow.Ellipsis)
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
            placeholder = localized("Поиск по названию оборудования",
                "Search equipment names", lang),
            neutralStyle = true, highContrastField = true,
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
            contentPadding = PaddingValues(NavEventLayout.screenInset, 6.dp, NavEventLayout.screenInset,
                navFloatingActionBottomPadding(embeddedInTab = false, edgeToEdgeStandalone = true)),
            verticalArrangement = Arrangement.spacedBy(NavEventLayout.sectionGap),
        ) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(NavEventLayout.innerGap)) {
                        ServiceCount(
                            localized("Всего", "All", lang), activeServices.size, p.text, p,
                            selected = selectedView == ServiceListView.WORKS &&
                                selectedFilter == WorkOverviewFilter.ALL,
                            modifier = Modifier.weight(1f),
                        ) { selectedView = ServiceListView.WORKS; selectedFilter = WorkOverviewFilter.ALL }
                        ServiceCount(
                            localized("Скоро", "Soon", lang), soon,
                            if (soon > 0) p.systemOrange else p.muted, p,
                            selected = selectedView == ServiceListView.WORKS &&
                                selectedFilter == WorkOverviewFilter.SOON,
                            modifier = Modifier.weight(1f),
                        ) { selectedView = ServiceListView.WORKS; selectedFilter = WorkOverviewFilter.SOON }
                        ServiceCount(
                            localized("Просрочены", "Overdue", lang), overdue,
                            if (overdue > 0) p.dangerText else p.muted, p,
                            selected = selectedView == ServiceListView.WORKS &&
                                selectedFilter == WorkOverviewFilter.OVERDUE,
                            modifier = Modifier.weight(1f),
                        ) { selectedView = ServiceListView.WORKS; selectedFilter = WorkOverviewFilter.OVERDUE }
                }
            }
            item {
                NavCalendarSegmentedControl(
                    labels = listOf(
                        localized("Работы · ${activeServices.size}", "Work · ${activeServices.size}", lang),
                        localized("Заявки · ${activeRequests.size}", "Requests · ${activeRequests.size}", lang),
                        localized("Закрытые · ${closedServices.size}", "Closed · ${closedServices.size}", lang),
                    ),
                    activeIndex = when (selectedView) {
                        ServiceListView.WORKS -> 0
                        ServiceListView.REQUESTS -> 1
                        ServiceListView.CLOSED -> 2
                    }, p = p,
                ) { index ->
                    selectedView = when (index) {
                        1 -> ServiceListView.REQUESTS
                        2 -> ServiceListView.CLOSED
                        else -> ServiceListView.WORKS
                    }
                    selectedFilter = WorkOverviewFilter.ALL
                }
            }
            message?.let { msg ->
                item { Text(msg, color = p.muted, fontSize = 12.sp) }
            }
            if (visibleResults.isEmpty()) {
                item {
                    ServicePanel(p) {
                        Text(
                            if (activeQuery.isNotBlank())
                                localized("Нет работ с таким названием.",
                                    "No maintenance names match.", lang)
                            else if (selectedView == ServiceListView.CLOSED)
                                localized("Закрытых сервисов пока нет.", "No closed services yet.", lang)
                            else if (selectedView == ServiceListView.REQUESTS)
                                localized("Текущих заявок AMOS пока нет.",
                                    "No current AMOS requests.", lang)
                            else if (selectedFilter == WorkOverviewFilter.ALL)
                                localized("Нет сервисов. Добавь оборудование кнопкой +.",
                                    "No records. Add equipment with +.", lang)
                            else localized("В этом фильтре сервисов нет.",
                                "No maintenance matches this filter.", lang),
                            color = p.muted,
                        )
                    }
                }
            }
            if (selectedView == ServiceListView.CLOSED) {
                items(filteredClosed, key = { it.id }) { service ->
                    ServiceClosedCard(service, p, lang,
                        searchQuery = activeQuery,
                        focusedSearchResult = searchResults.getOrNull(searchResultIndex)?.id == service.id,
                        onHistory = { viewingHistory = service },
                        onRestore = {
                            runCatching { services.restore(service.id); reschedule() }
                                .onFailure { message = it.localizedMessage }
                        },
                        onPurge = { confirmingPurge = service },
                    )
                }
            } else {
                items(filteredServices, key = { it.id }) { service ->
                val completed = service.completedAt != null
                val due = service.nextDate
                val late = !completed && !service.unplanned && due?.isBefore(now) == true
                val approaching = serviceDueSoon(service, now)
                val accent = if (completed) p.successText
                    else if (service.unplanned) NavTodayVisualTokens.accentInk(p.dark)
                    else if (late) p.dangerText
                    else if (approaching) p.systemOrange else p.muted
                Column(
                    Modifier.fillMaxWidth().personalNoteCard(p, NavEventLayout.cardRadius)
                        .glassClickable { viewingHistory = service }
                        .padding(horizontal = NavEventLayout.cardInset, vertical = NavEventLayout.groupGap),
                    verticalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(7.dp).background(accent, RoundedCornerShape(5.dp)))
                        Spacer(Modifier.width(10.dp))
                        NavSearchMarkedText(
                            value = service.equipment, query = activeQuery,
                            p = p, fontSize = 16.sp, modifier = Modifier.weight(1f),
                            maxLines = 2, overflow = TextOverflow.Ellipsis,
                            focused = searchResults.getOrNull(searchResultIndex)?.id == service.id,
                        )
                        NavGlassGlyph("chevron-right", p.muted, Modifier.size(17.dp))
                    }
                    if (!service.unplanned) Text(
                        localized("Последнее обслуживание: ", "Last serviced: ", lang) +
                            navDate(service.serviceHistory.lastOrNull()?.completedAt
                                ?: service.lastDate, lang, true),
                        color = p.successText, fontSize = 12.sp,
                    )
                    if (!completed) service.effectiveRequestDate?.let { date ->
                        Text(localized("Заявка от: ", "Request dated: ", lang) +
                            navDate(date, lang, true), color = p.muted, fontSize = 12.sp)
                    }
                    if (!completed) {
                        if (!service.unplanned && due != null) {
                            Text(localized("Следующее обслуживание: ", "Next service: ", lang) +
                                navDate(due, lang, true),
                                color = accent, fontSize = 12.sp)
                        }
                        ServiceAmosRequestButton(
                            hasRequest = service.requestNumber.isNotBlank(),
                            label = if (service.requestNumber.isBlank())
                                localized("Указать заявку AMOS", "Enter AMOS request", lang)
                            else localized("Заявка AMOS: ", "AMOS request: ", lang) +
                                service.requestNumber,
                            p = p, modifier = Modifier.fillMaxWidth(),
                        ) { requestTarget = service }
                    }
                    if (service.comment.isNotBlank()) {
                        NavRichNoteText(service.comment, service.commentHtml, p, fontSize = 12.sp,
                            modifier = Modifier.fillMaxWidth())
                    }
                    Text(
                        if (completed) localized("Завершено", "Completed", lang)
                        else if (service.unplanned) {
                            if (service.requestNumber.isNotBlank())
                                localized("Внеплановая · заявка оформлена", "Unplanned · request filed", lang)
                            else localized("Внеплановая · укажи номер заявки",
                                "Unplanned · add request number", lang)
                        } else if (late) localized("Просрочено", "Overdue", lang)
                        else if (approaching && due != null)
                            localized("Осталось " + ChronoUnit.DAYS.between(now, due) + " дн.",
                                ChronoUnit.DAYS.between(now, due).toString() + " days left", lang)
                        else localized("Запланировано", "Scheduled", lang),
                        color = accent, fontSize = 12.sp,
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        ServiceOption(
                            localized("Изменить", "Edit", lang), p, false,
                            modifier = Modifier.weight(1f),
                        ) { selected = service; showEditor = true }
                        if (!completed) {
                            ServiceOption(localized("Выполнено", "Completed", lang), p, false,
                                modifier = Modifier.weight(1f)) {
                                if (service.unplanned && service.requestNumber.isBlank()) {
                                    requestTarget = service
                                    message = localized("Сначала укажи номер заявки AMOS.",
                                        "Enter the AMOS request number first.", lang)
                                } else {
                                    confirmingComplete = service
                                    completionDate = LocalDate.now()
                                    completionDatePicker = false
                                }
                            }
                        }
                        ServiceOption(localized("Удалить", "Delete", lang), p, false,
                            modifier = Modifier.weight(1f), destructive = true) {
                            confirmingDelete = service
                        }
                    }
                }
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
            selected = null
            showEditor = true
            }
        } else {
            NavGlassAddButton(p, hazeState) {
            selected = null
            showEditor = true
            }
        }
    }
    } // End blurred maintenance page and floating actions.
        ServiceEditorOverlay(
            showEditor, selected, p, lang,
            onDismiss = { showEditor = false },
            onSave = {
                runCatching { services.upsert(it); reschedule() }
                    .onSuccess {
                        showEditor = false
                        message = localized("Сервис сохранён", "Saved", lang)
                    }
                    .onFailure { e -> message = e.localizedMessage }
            },
        )
    } // Sheet remains outside the blurred page.
    BackHandler(enabled = showEditor) { showEditor = false }
    requestTarget?.let { entry ->
        ServiceRequestDialog(
            entry, p, lang, onDismiss = { requestTarget = null }, neutralStyle = true,
            onSave = { number, date ->
                runCatching { services.setRequest(entry.id, number, date) }
                    .onSuccess { requestTarget = null; message = null }
                    .onFailure { message = it.localizedMessage }
            },
        )
    }
    confirmingDelete?.let { entry ->
        ServiceConfirmDialog(
            p, localized("Переместить в закрытые?", "Move to Closed?", lang),
            entry.equipment + "\n" + localized(
                "Сервис и история сохранятся. Запись можно будет восстановить.",
                "The service and full history are kept and can be restored.", lang),
            lang, onDismiss = { confirmingDelete = null },
        ) {
            runCatching { services.delete(entry.id); reschedule() }
                .onFailure { message = it.localizedMessage }
            confirmingDelete = null
        }
    }
    confirmingPurge?.let { entry ->
        ServiceConfirmDialog(
            p, localized("Удалить навсегда?", "Permanently delete?", lang),
            entry.equipment + "\n" + localized(
                "Сервис и ВСЯ история обслуживания будут уничтожены без восстановления.",
                "The service and ALL maintenance history will be permanently erased.", lang),
            lang, onDismiss = { confirmingPurge = null }, destructive = true,
        ) {
            runCatching { services.deletePermanently(entry.id); reschedule() }
                .onFailure { message = it.localizedMessage }
            confirmingPurge = null
        }
    }
    confirmingComplete?.let { entry ->
        Dialog(onDismissRequest = {
            confirmingComplete = null
            completionDatePicker = false
        }) {
            Column(
                Modifier.fillMaxWidth().personalNoteCard(p, 26.dp).padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(localized("Сервис выполнен?", "Service completed?", lang),
                    color = p.text, fontSize = 20.sp)
                Text(entry.equipment, color = p.muted, fontSize = 14.sp)
                Text(localized("Дата выполнения", "Actual service date", lang),
                    color = p.muted, fontSize = 12.sp)
                Row(Modifier.fillMaxWidth().personalNoteCard(p, 12.dp, nested = true)
                    .border(1.dp, NavTodayVisualTokens.checklistInsetRim(p.dark), RoundedCornerShape(12.dp))
                    .glassClickable { completionDatePicker = true }
                    .heightIn(min = NavEventRowHeight).padding(horizontal = NavEventLayout.cardInset),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(NavEventLayout.innerGap)) {
                    NavGlassGlyph("calendar", NavTodayVisualTokens.accentInk(p.dark), Modifier.size(20.dp))
                    Text(navDate(completionDate, lang, true), color = p.text,
                        fontSize = 14.sp, modifier = Modifier.weight(1f))
                    NavGlassGlyph("chevron-right", p.muted, Modifier.size(17.dp))
                }
                if (entry.unplanned) {
                    Text(localized(
                        "Внеплановая заявка будет завершена без следующего срока.",
                        "The unplanned request will be completed without another due date.", lang),
                        color = p.muted, fontSize = 12.sp)
                } else if (entry.periodMonths > 0) {
                    Text(
                        localized("Следующий сервис: ", "Next service: ", lang) +
                            navDate(completionDate.plusMonths(entry.periodMonths.toLong()),
                                lang, true),
                        color = NavTodayVisualTokens.accentInk(p.dark), fontSize = 13.sp,
                    )
                } else {
                    Text(localized("Без повтора — запись перейдёт в завершённые.",
                        "One-off service — the record will be marked completed.", lang),
                        color = p.muted, fontSize = 12.sp)
                }
                if (entry.requestNumber.isNotBlank()) {
                    Text(localized(
                        "Заявка AMOS " + entry.requestNumber +
                            " сохранится в истории. Для нового цикла номер очистится.",
                        "AMOS request " + entry.requestNumber +
                            " will be archived. The next cycle starts without a request number.",
                        lang,
                    ), color = p.muted, fontSize = 12.sp)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    ServiceOption(localized("Отмена", "Cancel", lang), p, false) {
                        confirmingComplete = null
                        completionDatePicker = false
                    }
                    Spacer(Modifier.width(8.dp))
                    ServiceOption(localized("Подтвердить", "Confirm", lang), p, true) {
                        runCatching {
                            services.complete(entry.id, completionDate)
                            reschedule()
                        }.onFailure { message = it.localizedMessage }
                            .onSuccess { message = null }
                        confirmingComplete = null
                        completionDatePicker = false
                    }
                }
            }
        }
        if (completionDatePicker) {
            NavDateWheelDialog(
                initial = completionDate,
                p = p, lang = lang,
                onDismiss = { completionDatePicker = false },
                onSelected = { selectedDate ->
                    completionDate = selectedDate
                    completionDatePicker = false
                },
            )
        }
    }
    viewingHistory?.let { entry ->
        NavGlassAlertDialog(
            p = p, neutralActions = true, neutralSurface = true,
            onDismissRequest = { viewingHistory = null },
            title = { Text(localized("История обслуживания", "Service history", lang)) },
            text = {
                Column(
                    Modifier.heightIn(max = 375.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(entry.equipment, color = p.text, fontSize = 15.sp)
                    if (entry.serviceHistory.isEmpty()) {
                        Text(localized(
                            "Обслуживаний пока нет. После «Выполнено» здесь появится история.",
                            "No services recorded yet. History appears here after marking completed.",
                            lang,
                        ), color = p.muted, fontSize = 13.sp)
                    }
                    entry.serviceHistory.asReversed().forEach { item ->
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(item.equipment, color = p.text, fontSize = 14.sp)
                            if (item.scheduledDate != null) {
                                Text(
                                    localized("По графику: ", "Scheduled: ", lang) +
                                        navDate(item.scheduledDate, lang, true),
                                    color = p.muted, fontSize = 12.sp,
                                )
                            } else {
                                Text(localized("Внеплановое обслуживание",
                                    "Unplanned maintenance", lang),
                                    color = p.muted, fontSize = 12.sp)
                            }
                            Text(
                                localized("Выполнено: ", "Completed: ", lang) +
                                    navDate(item.completedAt, lang, true),
                                color = p.successText, fontSize = 12.sp,
                            )
                            if (item.requestNumber.isNotBlank()) {
                                Text(
                                    localized("Заявка AMOS: ", "AMOS request: ", lang) +
                                        item.requestNumber,
                                    color = NavTodayVisualTokens.accentInk(p.dark), fontSize = 12.sp,
                                )
                                item.requestDate?.let { date ->
                                    Text(localized("Дата заявки: ", "Request date: ", lang) +
                                        navDate(date, lang, true), color = p.muted, fontSize = 12.sp)
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

/** Explicitly removed maintenance records retain the entire execution journal. */
@Composable
private fun ServiceClosedCard(
    entry: ServiceEntry, p: NavPalette, lang: UiLanguage,
    searchQuery: String = "", focusedSearchResult: Boolean = false,
    onHistory: () -> Unit, onRestore: () -> Unit, onPurge: () -> Unit,
) {
    Column(
        Modifier.fillMaxWidth().personalNoteCard(p, NavEventLayout.cardRadius)
            .padding(horizontal = NavEventLayout.cardInset, vertical = NavEventLayout.groupGap),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Column(
            Modifier.fillMaxWidth().glassClickable(onClick = onHistory)
                .padding(vertical = 3.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            NavSearchMarkedText(entry.equipment, searchQuery, p, fontSize = 15.sp,
                focused = focusedSearchResult)
            entry.closedAt?.let {
                Text(localized("Закрыт: ", "Closed: ", lang) +
                    navDate(it, lang, true), color = p.muted, fontSize = 12.sp)
            }
            Text(
                localized("История обслуживаний: ", "Service history: ", lang) +
                    entry.serviceHistory.size,
                color = p.muted, fontSize = 12.sp,
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ServiceOption(localized("Восстановить", "Restore", lang), p, false,
                modifier = Modifier.weight(1f), onClick = onRestore)
            ServiceOption(localized("Удалить навсегда", "Delete forever", lang), p, false,
                modifier = Modifier.weight(1f), destructive = true, onClick = onPurge)
        }
    }
}

@Composable
private fun ServiceCount(
    label: String, number: Int, color: Color, p: NavPalette,
    selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit,
) {
    Column(
        modifier.personalNoteCard(p, NavEventLayout.cardRadius)
            .then(if (selected) Modifier.border(1.5.dp, NavTodayVisualTokens.accentInk(p.dark),
                RoundedCornerShape(NavEventLayout.cardRadius)) else Modifier)
            .glassClickable(onClick = onClick)
            .heightIn(min = 66.dp)
            .padding(horizontal = 6.dp, vertical = NavEventLayout.innerGap),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(number.toString(), color = color, fontSize = 21.sp, fontWeight = FontWeight.Medium)
        Text(label, color = if (selected) NavTodayVisualTokens.accentInk(p.dark) else p.muted, fontSize = 11.sp,
            maxLines = 1)
    }
}

/** Same bottom-up presentation and handle behavior as the routine event editor. */
@Composable
private fun BoxScope.ServiceEditorOverlay(
    visible: Boolean, existing: ServiceEntry?, p: NavPalette, lang: UiLanguage,
    onDismiss: () -> Unit, onSave: (ServiceEntry) -> Unit,
) {
    var dragPx by remember { mutableFloatStateOf(0f) }
    val scope = rememberCoroutineScope()
    val returnJob = remember { mutableStateOf<Job?>(null) }
    val dragState = rememberDraggableState { delta ->
        returnJob.value?.cancel()
        returnJob.value = null
        dragPx = (dragPx + delta).coerceIn(0f, 1600f)
    }
    val dismissDistance = with(LocalDensity.current) { 110.dp.toPx() }
    val dragStopped: (Float) -> Unit = { velocity ->
        if (dragPx >= dismissDistance || velocity > 1150f) onDismiss()
        else {
            val offset = dragPx
            returnJob.value = scope.launch {
                Animatable(offset).animateTo(0f, spring(dampingRatio = .96f, stiffness = 680f)) {
                    dragPx = value
                }
                returnJob.value = null
            }
        }
    }
    LaunchedEffect(visible) {
        if (!visible) delay(250)
        returnJob.value?.cancel()
        returnJob.value = null
        dragPx = 0f
    }
    AnimatedVisibility(
        visible = visible, modifier = Modifier.fillMaxSize(),
        enter = fadeIn(tween(150)), exit = fadeOut(tween(175)),
        label = "serviceEditorScrim",
    ) {
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = .36f))
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                /* Preserve draft; dismiss via header, handle or Back. */
            })
    }
    AnimatedVisibility(
        visible = visible, modifier = Modifier.align(Alignment.BottomCenter),
        enter = slideInVertically(initialOffsetY = { it },
            animationSpec = spring(dampingRatio = .92f, stiffness = 620f)) + fadeIn(tween(115)),
        exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(220)) + fadeOut(tween(140)),
        label = "serviceEditorBottomSheet",
    ) {
        val shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        Box(Modifier.fillMaxWidth().fillMaxHeight(.94f)
            .offset { IntOffset(0, dragPx.roundToInt()) }
            .shadow(16.dp, shape).clip(shape)
            .background(navEditorSheetSurface(p), shape)
            .border(1.dp, p.border.copy(alpha = .65f), shape)) {
            ServiceEditorSheet(existing, p, lang, onDismiss, onSave)
            Box(Modifier.align(Alignment.TopCenter).size(width = 112.dp, height = 30.dp)
                .draggable(state = dragState, orientation = Orientation.Vertical,
                    onDragStopped = { velocity -> dragStopped(velocity) }),
                contentAlignment = Alignment.TopCenter) {
                Box(Modifier.padding(top = 8.dp).size(width = 42.dp, height = 4.dp)
                    .background(p.muted.copy(alpha = .58f), CircleShape))
            }
        }
    }
}


@Composable
private fun ServiceEditorSheet(
    existing: ServiceEntry?, p: NavPalette, lang: UiLanguage,
    onDismiss: () -> Unit, onSave: (ServiceEntry) -> Unit,
) {
    var equipment by remember(existing?.id) { mutableStateOf(existing?.equipment.orEmpty()) }
    var unplanned by remember(existing?.id) { mutableStateOf(existing?.unplanned ?: false) }
    var requestNumber by remember(existing?.id) {
        mutableStateOf(existing?.requestNumber.orEmpty())
    }
    // Informational date only: never used as a service deadline or reminder.
    var requestDate by remember(existing?.id) {
        mutableStateOf(existing?.effectiveRequestDate ?: existing?.takeIf { it.unplanned }?.lastDate ?: LocalDate.now())
    }
    // Planned equipment has a real last-service date, independently editable.
    // Legacy entries keep both stored dates as-is until the user edits them.
    var lastServiceDate by remember(existing?.id) {
        mutableStateOf(existing?.takeUnless { it.unplanned }?.lastDate ?: LocalDate.now())
    }
    var next by remember(existing?.id) {
        mutableStateOf(existing?.nextDate ?: LocalDate.now().plusMonths(12))
    }
    var note by remember(existing?.id) { mutableStateOf(existing?.comment.orEmpty()) }
    var noteHtml by remember(existing?.id) { mutableStateOf(existing?.commentHtml.orEmpty()) }
    var period by remember(existing?.id) { mutableIntStateOf(existing?.periodMonths ?: 12) }
    val presetIntervals = listOf(0, 6, 12, 18, 36, 60)
    var customSelected by remember(existing?.id) {
        mutableStateOf(existing?.periodMonths?.let { it !in presetIntervals } == true)
    }
    var customYears by remember(existing?.id) {
        mutableStateOf((if (customSelected) period / 12 else 0).toString())
    }
    var customMonths by remember(existing?.id) {
        mutableStateOf((if (customSelected) period % 12 else 9).toString())
    }
    var alert by remember(existing?.id) { mutableIntStateOf(existing?.remindMonths ?: 0) }
    var error by remember { mutableStateOf<String?>(null) }
    var pickingDate by remember(existing?.id) { mutableStateOf<String?>(null) }

    var repeatOpen by remember(existing?.id) { mutableStateOf(false) }
    var reminderOpen by remember(existing?.id) { mutableStateOf(false) }
    val intervals = presetIntervals.map { months -> months.toString() to
        if (months == 0) localized("Без повтора", "One-off", lang)
        else localized("$months мес.", "$months mo", lang) } +
        ("custom" to localized("Настроить", "Customize", lang))
    val reminders = listOf(0, 1, 2, 3).map { months -> months.toString() to
        if (months == 0) localized("Нет", "None", lang)
        else localized("За $months мес.", "$months mo before", lang) }
    val repeatLabel = if (customSelected) localized("$period мес.", "$period mo", lang)
        else intervals.firstOrNull { it.first == period.toString() }?.second ?: "$period"
    val reminderLabel = reminders.firstOrNull { it.first == alert.toString() }?.second ?: "$alert"
    val years = customYears.toIntOrNull()
    val months = customMonths.toIntOrNull()
    var initialNote by remember(existing?.id) {
        mutableStateOf(existing?.comment.orEmpty() to existing?.commentHtml.orEmpty())
    }
    val originalFields = remember(existing?.id) {
        listOf(equipment.trim(), unplanned, requestNumber.trim(), requestDate, lastServiceDate,
            next, period, alert, customSelected, if (customSelected) customYears else "",
            if (customSelected) customMonths else "")
    }
    val dirty = listOf(equipment.trim(), unplanned, requestNumber.trim(), requestDate, lastServiceDate,
        next, period, alert, customSelected, if (customSelected) customYears else "",
        if (customSelected) customMonths else "") != originalFields || (note to noteHtml) != initialNote
    val valid = equipment.isNotBlank() && (unplanned || (next.isAfter(lastServiceDate) &&
        (!customSelected || (years != null && months != null && years >= 0 &&
            months in 0..11 && years * 12 + months in 1..1200))))
    Column(Modifier.fillMaxSize().padding(top = 13.dp).navigationBarsPadding().imePadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically) {
            NavEventHeaderAction("x", localized("Отмена", "Cancel", lang), p, true, onDismiss)
            Text(if (existing == null) localized("Новый сервис", "New service", lang)
                else localized("Редактировать сервис", "Edit service", lang),
                modifier = Modifier.weight(1f), color = p.text, fontSize = 20.sp,
                textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
            NavEventHeaderAction("check", localized("Сохранить", "Save", lang), p, valid && dirty, accent = true) {
                    runCatching {
                        require(existing?.serviceHistory.isNullOrEmpty() ||
                            existing?.unplanned == unplanned) {
                            "Cannot change the type of a service with archived history"
                        }
                        require(navRichNoteAllowed(note, noteHtml, 6000))
                        val total = if (unplanned) 0 else if (customSelected) {
                            val years = customYears.toIntOrNull() ?: -1
                            val months = customMonths.toIntOrNull() ?: -1
                            require(years >= 0 && months in 0..11 && years * 12 + months in 1..1200)
                            years * 12 + months
                        } else period
                        ServiceEntry(
                            id = existing?.id ?: UUID.randomUUID().toString(),
                            equipment = equipment.trim(),
                            // A planned service uses the actual user-selected previous
                            // maintenance date; never fabricate "next date - 1 day".
                            // Unplanned keeps its informational AMOS request date.
                            lastDate = if (unplanned) requestDate else lastServiceDate,
                            nextDate = if (unplanned) null else next,
                            comment = note.trim(),
                            commentHtml = noteHtml,
                            periodMonths = total,
                            remindMonths = if (unplanned) 0 else alert,
                            requestNumber = if (unplanned) requestNumber.trim()
                                else existing?.requestNumber.orEmpty(),
                            requestDate = if (unplanned) requestDate.takeIf { requestNumber.isNotBlank() }
                                else existing?.effectiveRequestDate,
                            closedAt = existing?.closedAt,
                            serviceHistory = existing?.serviceHistory.orEmpty(),
                            completedAt = existing?.completedAt,
                            unplanned = unplanned,
                        )
                    }.onSuccess { onSave(it) }
                        .onFailure {
                            error = localized(
                                "Проверь название, даты и параметры обслуживания.",
                                "Check equipment name, dates and service parameters.", lang)
                        }
            }
        }
        Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())
            .padding(horizontal = NavEventLayout.screenInset),
            verticalArrangement = Arrangement.spacedBy(NavEventLayout.sectionGap)) {
            NavCalendarSegmentedControl(
                labels = listOf(localized("Плановое", "Planned", lang), localized("Внеплановое", "Unplanned", lang)),
                activeIndex = if (unplanned) 1 else 0, p = p,
            ) { index -> if (existing?.serviceHistory.isNullOrEmpty()) unplanned = index == 1 }
            if (existing?.serviceHistory?.isNotEmpty() == true) {
                Text(localized("Тип закреплён: у записи уже есть история обслуживания.",
                    "Type is fixed because this record already has service history.", lang),
                    color = p.muted, fontSize = 11.sp)
            }
            ServiceInput(equipment, { equipment = it.take(200) },
                localized("Название оборудования", "Equipment name", lang), p, false)
            if (unplanned) {
                NavEventSchedulePanel(p) {
                    Text(localized("Номер заявки AMOS", "AMOS request number", lang),
                        color = p.text, fontSize = 15.sp, modifier = Modifier.padding(vertical = 8.dp))
                    ServiceRequestNumberField(requestNumber, p, lang, onChange = { value ->
                        if (requestNumber.isBlank() && value.isNotBlank()) requestDate = LocalDate.now()
                        requestNumber = value
                    })
                    Spacer(Modifier.height(NavEventLayout.innerGap))
                    if (requestNumber.isNotBlank()) NavEventValueRow(localized("Дата заявки AMOS", "AMOS request date", lang),
                        navDate(requestDate, lang, true), p) { pickingDate = "request" }
                    Text(localized("Только для информации — не влияет на сроки и уведомления.",
                        "For reference only — no effect on due dates or reminders.", lang),
                        color = p.muted, fontSize = 11.sp, modifier = Modifier.padding(bottom = 8.dp))
                }
            } else {
                NavEventSchedulePanel(p) {
                    NavEventValueRow(localized("Последнее обслуживание", "Last service", lang),
                        navDate(lastServiceDate, lang, true), p) { pickingDate = "last" }
                    NavEventDivider(p)
                    NavEventValueRow(localized("Следующее обслуживание", "Next service", lang),
                        navDate(next, lang, true), p) { pickingDate = "next" }
                    NavEventDivider(p)
                    NavEventValueRow(localized("Периодичность", "Interval", lang), repeatLabel, p,
                        chevron = true, valueAccent = period > 0) { repeatOpen = true }
                if (customSelected) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        Column(Modifier.weight(1f)) {
                            Text(localized("Лет", "Years", lang), color = p.muted, fontSize = 11.sp)
                            ServiceNumberInput(customYears, { value ->
                                customYears = value
                                val years = value.toIntOrNull()
                                val months = customMonths.toIntOrNull()
                                if (years != null && months != null) {
                                    val total = years * 12 + months
                                    if (total in 1..1200) {

                                        period = total
                                        next = calculatedNextServiceDate(lastServiceDate, total)
                                    }
                                }
                            }, p)
                        }
                        Column(Modifier.weight(1f)) {
                            Text(localized("Месяцев", "Months", lang), color = p.muted, fontSize = 11.sp)
                            ServiceNumberInput(customMonths, { value ->
                                customMonths = value
                                val years = customYears.toIntOrNull()
                                val months = value.toIntOrNull()
                                if (years != null && months != null) {
                                    val total = years * 12 + months
                                    if (total in 1..1200) {

                                        period = total
                                        next = calculatedNextServiceDate(lastServiceDate, total)
                                    }
                                }
                            }, p)
                        }
                    }
                    val years = customYears.toIntOrNull()
                    val months = customMonths.toIntOrNull()
                    val total = if (years == null || months == null) 0 else years * 12 + months
                    Text(
                        if (total in 1..1200)
                            localized("Интервал: $total мес.", "Interval: $total months", lang)
                        else localized("Укажи от 1 до 1200 месяцев суммарно.",
                            "Enter a total between 1 and 1200 months.", lang),
                        color = if (total in 1..1200) p.muted else p.dangerText,
                        fontSize = 11.sp,
                    )
                }

                    if (period > 0) Text(localized(
                        "«Выполнено» пересчитает срок от фактической даты выполнения.",
                        "Completion calculates next due from the actual completion date.", lang),
                        color = p.muted, fontSize = 11.sp, modifier = Modifier.padding(vertical = 8.dp))
                }
                NavEventSchedulePanel(p) {
                    NavEventValueRow(localized("Предупредить заранее", "Remind before due", lang),
                        reminderLabel, p, chevron = true, valueAccent = alert > 0) { reminderOpen = true }
                }
            }
            NavEventSchedulePanel(p) {
                Text(localized("Комментарий", "Comment", lang), color = p.text, fontSize = 15.sp,
                    modifier = Modifier.padding(vertical = NavEventLayout.innerGap))
                NavRichNoteEditor(initialPlain = existing?.comment.orEmpty(), initialHtml = existing?.commentHtml.orEmpty(),
                    entryKey = existing?.id ?: "new-service", p = p, lang = lang,
                    maxCharacters = 6000, neutralStyle = true, amosCommentField = true,
                    placeholder = localized("Работы, расходники, примечания…", "Details and remarks…", lang),
                    onChange = { text, html -> note = text; noteHtml = html },
                    onInitialized = { text, html -> initialNote = text to html; note = text; noteHtml = html })
                Spacer(Modifier.height(NavEventLayout.innerGap))
            }
            error?.let { Text(it, color = p.dangerText, fontSize = 12.sp) }
            Spacer(Modifier.height(NavEventLayout.innerGap))
        }
    }
    if (repeatOpen) NavEventChoiceDialog(
        localized("Периодичность", "Interval", lang), intervals,
        if (customSelected) "custom" else period.toString(), p, lang, confirmRequired = false,
        onDismiss = { repeatOpen = false }, onSelect = { key ->
            repeatOpen = false
            if (key == "custom") {
                if (!customSelected) {
                    val candidate = (customYears.toIntOrNull() ?: 0) * 12 + (customMonths.toIntOrNull() ?: 0)
                    if (candidate in 1..1200) {
                        period = candidate
                        next = calculatedNextServiceDate(lastServiceDate, candidate)
                    }
                }
                customSelected = true
            } else {
                customSelected = false
                period = key.toInt()
                if (period > 0) next = calculatedNextServiceDate(lastServiceDate, period)
            }
        })
    if (reminderOpen) NavEventChoiceDialog(
        localized("Предупредить заранее", "Remind before due", lang), reminders, alert.toString(), p, lang,
        confirmRequired = false, onDismiss = { reminderOpen = false },
        onSelect = { key -> alert = key.toInt(); reminderOpen = false })
    if (pickingDate != null) {
        val forRequest = pickingDate == "request"
        val forLastService = pickingDate == "last"
        NavDateWheelDialog(
            initial = when {
                forRequest -> requestDate
                forLastService -> lastServiceDate
                else -> next
            }, p = p, lang = lang,
            onDismiss = { pickingDate = null },
            onSelected = { chosen ->
                when {
                    forRequest -> requestDate = chosen
                    forLastService -> {
                        lastServiceDate = chosen
                        if (period > 0) next = calculatedNextServiceDate(chosen, period)
                    }
                    else -> next = chosen // Keep explicit manual due-date override.
                }
                pickingDate = null
            },
        )
    }
}

@Composable
private fun ServiceInput(
    value: String, onChange: (String) -> Unit, hint: String, p: NavPalette,
    multiline: Boolean, identifier: Boolean = false,
) {
    BasicTextField(
        value = value, onValueChange = onChange, singleLine = !multiline,
        maxLines = if (multiline) 5 else 1,
        keyboardOptions = if (identifier) NavIdentifierKeyboard else NavSentenceKeyboard,
        textStyle = TextStyle(color = p.text, fontSize = 14.sp),
        cursorBrush = SolidColor(NavTodayVisualTokens.accentInk(p.dark)),
        modifier = Modifier.fillMaxWidth().height(if (multiline) 86.dp else if (identifier) 48.dp else NavEventRowHeight + NavEventLayout.cardVertical * 2)
            .personalNoteCard(p, if (identifier) 12.dp else NavEventLayout.cardRadius, nested = identifier),
        decorationBox = { inner ->
            Box(Modifier.fillMaxSize().padding(12.dp),
                contentAlignment = if (multiline) Alignment.TopStart else Alignment.CenterStart) {
                if (value.isEmpty()) Text(hint, color = p.muted, fontSize = 14.sp)
                inner()
            }
        },
    )
}

@Composable
private fun ServiceNumberInput(
    value: String,
    onChange: (String) -> Unit,
    p: NavPalette,
) {
    BasicTextField(
        value = value,
        onValueChange = { entered ->
            if (entered.length <= 3 && entered.all(Char::isDigit)) onChange(entered)
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        textStyle = TextStyle(color = p.text, fontSize = 14.sp),
        cursorBrush = SolidColor(NavTodayVisualTokens.accentInk(p.dark)),
        modifier = Modifier.fillMaxWidth().height(44.dp)
            .personalNoteCard(p, 11.dp, nested = true),
        decorationBox = { field ->
            Box(Modifier.fillMaxSize().padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart) {
                field()
            }
        },
    )
}

@Composable
private fun ServiceOption(
    label: String, p: NavPalette, selected: Boolean,
    modifier: Modifier = Modifier, compact: Boolean = false,
    destructive: Boolean = false, onClick: () -> Unit,
) {
    Box(modifier.personalNoteCard(p, 12.dp, nested = true)
        .then(if (selected) Modifier.border(1.5.dp, NavTodayVisualTokens.accentInk(p.dark),
            RoundedCornerShape(12.dp)) else Modifier)
        .glassClickable(onClick = onClick)
        .heightIn(min = 44.dp)
        .padding(horizontal = if (compact) 5.dp else 10.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center) {
        Text(label, color = if (destructive) p.systemRed
            else if (selected) NavTodayVisualTokens.accentInk(p.dark) else p.text,
            fontSize = if (compact) 11.sp else 12.sp,
            maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/**
 * Only the AMOS request badge inside Services uses the chosen warm accent.
 * Preserve the distinct warm request badge and the glass dock.
 */
internal data class ServiceAmosBadgeColors(
    val fill: Color, val outline: Color, val text: Color,
)

internal fun serviceAmosBadgeColors(dark: Boolean): ServiceAmosBadgeColors =
    if (dark) ServiceAmosBadgeColors(
        fill = Color(0xFF403227),
        outline = Color(0xFF74583C),
        text = Color(0xFFFFBF79),
    ) else ServiceAmosBadgeColors(
        fill = Color(0xFFFFF1DE),
        outline = Color(0xFFF4C48B),
        text = Color(0xFFAE5800),
    )

/** Warm fill is reserved for records that already have an AMOS number. */
internal fun serviceAmosBadgeFill(dark: Boolean, hasRequest: Boolean): Color =
    if (hasRequest) serviceAmosBadgeColors(dark).fill else Color.Transparent

@Composable
private fun ServiceAmosRequestButton(
    hasRequest: Boolean, label: String, p: NavPalette,
    modifier: Modifier = Modifier, onClick: () -> Unit,
) {
    val colors = serviceAmosBadgeColors(p.dark)
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier
            .clip(shape)
            // Empty AMOS request is an outline-only action, not a filled badge.
            .background(serviceAmosBadgeFill(p.dark, hasRequest), shape)
            .border(1.dp, colors.outline, shape)
            .glassClickable(onClick = onClick)
            .heightIn(min = 44.dp)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            // Empty request: orange outline is the only accent; text stays neutral.
            label, color = if (hasRequest) colors.text else p.text, fontSize = 12.sp,
            maxLines = 1, overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ServiceConfirmDialog(
    p: NavPalette, title: String, detail: String, lang: UiLanguage,
    onDismiss: () -> Unit, destructive: Boolean = false, onConfirmed: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().personalNoteCard(p, 26.dp)
            .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(title, color = p.text, fontSize = 20.sp)
            Text(detail, color = p.muted, fontSize = 14.sp)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                ServiceOption(localized("Отмена", "Cancel", lang), p, false, onClick = onDismiss)
                Spacer(Modifier.width(8.dp))
                ServiceOption(localized("Подтвердить", "Confirm", lang), p, true, destructive = destructive, onClick = onConfirmed)
            }
        }
    }
}

@Composable
private fun ServicePanel(p: NavPalette, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth().personalNoteCard(p, NavEventLayout.cardRadius)
        .padding(NavEventLayout.cardInset), verticalArrangement = Arrangement.spacedBy(NavEventLayout.innerGap), content = content)
}
