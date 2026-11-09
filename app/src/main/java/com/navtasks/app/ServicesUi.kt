package com.navtasks.app

import dev.chrisbanes.haze.hazeSource
import com.kyant.backdrop.backdrops.layerBackdrop

import android.app.DatePickerDialog
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
        ), searchText)
    val filteredClosed = searchServiceTitles(closedServices, searchText)
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
            else NavGlassCircleButton(p, "back", onClick = onBack)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                NavBrandLabel(p)
                Text(localized("Сервисы", "Maintenance", lang), color = p.text, fontSize = 23.sp)
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
                placeholder = localized("Поиск по названию оборудования",
                    "Search equipment names", lang),
            )
        }
        LazyColumn(
            Modifier.fillMaxSize().dismissSearchImeOnResultsGesture(searchOpen),
            state = listState,
            contentPadding = PaddingValues(16.dp, 6.dp, 16.dp, 106.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                NavPanel(p) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ServiceCount(
                            localized("Всего", "All", lang), activeServices.size, p.blue, p,
                            selected = selectedView == ServiceListView.WORKS &&
                                selectedFilter == WorkOverviewFilter.ALL,
                            modifier = Modifier.weight(1f),
                        ) { selectedView = ServiceListView.WORKS; selectedFilter = WorkOverviewFilter.ALL }
                        ServiceCount(
                            localized("Скоро", "Soon", lang), soon,
                            if (soon > 0) p.warningText else p.muted, p,
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
                    Text(localized("Оборудование, даты, комментарии. Без вложений.",
                        "Equipment, dates and notes. No attachments.", lang),
                        color = p.muted, fontSize = 11.sp)
                }
            }
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(9.dp),
                ) {
                    ServiceOption(
                        localized("Работы · ${activeServices.size}", "Work · ${activeServices.size}", lang),
                        p, selectedView == ServiceListView.WORKS, modifier = Modifier.weight(1f),
                        compact = true,
                    ) {
                        selectedView = ServiceListView.WORKS
                        selectedFilter = WorkOverviewFilter.ALL
                    }
                    ServiceOption(
                        localized("Заявки · ${activeRequests.size}", "Requests · ${activeRequests.size}", lang),
                        p, selectedView == ServiceListView.REQUESTS, modifier = Modifier.weight(1f),
                        compact = true,
                    ) {
                        selectedView = ServiceListView.REQUESTS
                        selectedFilter = WorkOverviewFilter.ALL
                    }
                    ServiceOption(
                        localized("Закрытые · ${closedServices.size}", "Closed · ${closedServices.size}", lang),
                        p, selectedView == ServiceListView.CLOSED, modifier = Modifier.weight(1f),
                        compact = true,
                    ) {
                        selectedView = ServiceListView.CLOSED
                        selectedFilter = WorkOverviewFilter.ALL
                    }
                }
            }
            message?.let { msg ->
                item { Text(msg, color = p.muted, fontSize = 12.sp) }
            }
            val visible = if (selectedView == ServiceListView.CLOSED)
                filteredClosed else filteredServices
            if (visible.isEmpty()) {
                item {
                    NavPanel(p) {
                        Text(
                            if (searchText.isNotBlank())
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
                    else if (service.unplanned) p.blue
                    else if (late) p.dangerText
                    else if (approaching) p.warningText else p.successText
                Column(
                    Modifier.fillMaxWidth().navGlass(p, radius = 18.dp, role = GlassRole.TASK)
                        .glassClickable { viewingHistory = service }
                        .padding(horizontal = 14.dp, vertical = 11.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(7.dp).background(accent, RoundedCornerShape(5.dp)))
                        Spacer(Modifier.width(10.dp))
                        Text(service.equipment, color = p.text, fontSize = 16.sp,
                            modifier = Modifier.weight(1f), maxLines = 2,
                            overflow = TextOverflow.Ellipsis)
                        NavGlassGlyph("chevron-right", p.muted, Modifier.size(17.dp))
                    }
                    Text(
                        if (service.unplanned)
                            localized("Заявка от: ", "Request dated: ", lang) +
                                navDate(service.lastDate, lang, true)
                        else localized("Последнее обслуживание: ", "Last serviced: ", lang) +
                            navDate(service.serviceHistory.lastOrNull()?.completedAt
                                ?: service.lastDate, lang, true),
                        color = if (service.unplanned) p.blue else p.successText, fontSize = 12.sp,
                    )
                    if (!completed) {
                        if (!service.unplanned && due != null) {
                            Text(localized("Следующее обслуживание: ", "Next service: ", lang) +
                                navDate(due, lang, true),
                                color = accent, fontSize = 12.sp)
                        }
                        ServiceAmosRequestButton(
                            if (service.requestNumber.isBlank())
                                localized("Указать заявку AMOS", "Enter AMOS request", lang)
                            else localized("Заявка AMOS: ", "AMOS request: ", lang) +
                                service.requestNumber,
                            p, Modifier.fillMaxWidth(),
                        ) { requestTarget = service }
                    }
                    if (service.comment.isNotBlank()) {
                        NavRichNoteText(service.comment, service.commentHtml, p, fontSize = 12.sp,
                            modifier = Modifier.fillMaxWidth().heightIn(max = 50.dp))
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
                            ServiceOption(localized("Проведено", "Serviced", lang), p, false,
                                modifier = Modifier.weight(1f)) {
                                confirmingComplete = service
                                completionDate = LocalDate.now()
                                completionDatePicker = false
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
    } // End maintenance screen and floating Add overlay.
    if (showEditor) {
        ServiceEditorDialog(
            selected, p, lang,
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
    }
    requestTarget?.let { entry ->
        ServiceRequestDialog(
            entry, p, lang, onDismiss = { requestTarget = null },
            onSave = { number ->
                runCatching { services.setRequestNumber(entry.id, number) }
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
            lang, onDismiss = { confirmingPurge = null },
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
                Modifier.fillMaxWidth().navDialogSurface(p).padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(localized("Сервис проведён?", "Service completed?", lang),
                    color = p.text, fontSize = 20.sp)
                Text(entry.equipment, color = p.muted, fontSize = 14.sp)
                Text(localized("Дата проведения", "Actual service date", lang),
                    color = p.muted, fontSize = 12.sp)
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(9.dp),
                ) {
                    ServiceOption(
                        localized("Сегодня", "Today", lang), p,
                        completionDate == LocalDate.now(), Modifier.weight(1f),
                    ) { completionDate = LocalDate.now() }
                    ServiceOption(
                        localized("Выбрать дату", "Choose date", lang), p,
                        completionDate != LocalDate.now(), Modifier.weight(1f),
                    ) { completionDatePicker = true }
                }
                Text(navDate(completionDate, lang, true), color = p.blueText, fontSize = 13.sp)
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
                        color = p.blueText, fontSize = 13.sp,
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
            p = p,
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
                            "Обслуживаний пока нет. После «Проведено» здесь появится история.",
                            "No services recorded yet. History appears here after Serviced.",
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
                                localized("Проведено: ", "Completed: ", lang) +
                                    navDate(item.completedAt, lang, true),
                                color = p.successText, fontSize = 12.sp,
                            )
                            if (item.requestNumber.isNotBlank()) {
                                Text(
                                    localized("Заявка AMOS: ", "AMOS request: ", lang) +
                                        item.requestNumber,
                                    color = p.blueText, fontSize = 12.sp,
                                )
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

/** Explicitly removed maintenance records retain the entire execution journal. */
@Composable
private fun ServiceClosedCard(
    entry: ServiceEntry, p: NavPalette, lang: UiLanguage,
    onHistory: () -> Unit, onRestore: () -> Unit, onPurge: () -> Unit,
) {
    Column(
        Modifier.fillMaxWidth().navGlass(p, radius = 18.dp, role = GlassRole.TASK)
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Column(
            Modifier.fillMaxWidth().glassClickable(onClick = onHistory)
                .padding(vertical = 3.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(entry.equipment, color = p.text, fontSize = 15.sp)
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
        modifier.navGlass(p, radius = 14.dp,
            role = if (selected) GlassRole.SELECTED else GlassRole.CONTROL)
            .glassClickable(onClick = onClick)
            .heightIn(min = 75.dp)
            .padding(horizontal = 8.dp, vertical = 9.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(number.toString(), color = if (selected) p.selectionContent else color, fontSize = 22.sp)
        Text(label, color = if (selected) p.selectionContent else p.muted, fontSize = 11.sp,
            maxLines = 1)
    }
}

@Composable
private fun ServiceEditorDialog(
    existing: ServiceEntry?, p: NavPalette, lang: UiLanguage,
    onDismiss: () -> Unit, onSave: (ServiceEntry) -> Unit,
) {
    val context = LocalContext.current
    var equipment by remember(existing?.id) { mutableStateOf(existing?.equipment.orEmpty()) }
    var unplanned by remember(existing?.id) { mutableStateOf(existing?.unplanned ?: false) }
    var requestNumber by remember(existing?.id) {
        mutableStateOf(existing?.requestNumber.orEmpty())
    }
    // Informational date only: never used as a service deadline or reminder.
    var requestDate by remember(existing?.id) {
        mutableStateOf(existing?.takeIf { it.unplanned }?.lastDate ?: LocalDate.now())
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
    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier.fillMaxWidth().navDialogSurface(p)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            Text(if (existing == null) localized("Новый сервис", "New service", lang)
                else localized("Редактировать", "Edit service", lang),
                color = p.text, fontSize = 21.sp)
            Column(Modifier.fillMaxWidth().heightIn(max = 430.dp)
                .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                Text(localized("Тип обслуживания", "Maintenance type", lang),
                    color = p.muted, fontSize = 12.sp)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ServiceOption(localized("Плановое", "Planned", lang), p, !unplanned,
                        modifier = Modifier.weight(1f)) {
                        if (existing?.serviceHistory.isNullOrEmpty()) unplanned = false
                    }
                    ServiceOption(localized("Внеплановое", "Unplanned", lang), p, unplanned,
                        modifier = Modifier.weight(1f)) {
                        if (existing?.serviceHistory.isNullOrEmpty()) unplanned = true
                    }
                }
                if (existing?.serviceHistory?.isNotEmpty() == true) {
                    Text(localized("Тип закреплён: у записи уже есть история обслуживания.",
                        "Type is fixed because this record already has service history.", lang),
                        color = p.muted, fontSize = 11.sp)
                }
                Text(localized("Оборудование", "Equipment", lang), color = p.muted, fontSize = 12.sp)
                ServiceInput(equipment, { equipment = it.take(200) },
                    localized("Название", "Name", lang), p, false)
                if (unplanned) {
                    Text(localized("Номер заявки AMOS", "AMOS request number", lang),
                        color = p.muted, fontSize = 12.sp)
                    ServiceInput(requestNumber, { requestNumber = it.take(100) },
                        localized("Номер из AMOS", "Request number from AMOS", lang), p, false,
                        identifier = true)
                    Text(localized("Дата заявки AMOS", "AMOS request date", lang),
                        color = p.muted, fontSize = 12.sp)
                    ServiceOption(navDate(requestDate, lang, true), p, false,
                        Modifier.fillMaxWidth()) {
                        pickingDate = "request"
                    }
                    Text(localized("Только для информации — не влияет на сроки и уведомления.",
                        "For reference only — no effect on due dates or reminders.", lang),
                        color = p.muted, fontSize = 11.sp)
                } else {
                    Text(localized("Следующее обслуживание", "Next service date", lang),
                        color = p.muted, fontSize = 12.sp)
                    ServiceOption(navDate(next, lang, true), p, false, Modifier.fillMaxWidth()) {
                        pickingDate = "next"
                    }
                }
                Text(localized("Комментарий", "Comment", lang), color = p.muted, fontSize = 12.sp)
                NavRichNoteEditor(
                    initialPlain = existing?.comment.orEmpty(),
                    initialHtml = existing?.commentHtml.orEmpty(),
                    entryKey = existing?.id ?: "new-service", p = p, lang = lang,
                    maxCharacters = 6000,
                    placeholder = localized("Работы, расходники, примечания…",
                        "Details and remarks…", lang),
                    onChange = { text, html -> note = text; noteHtml = html },
                )
                if (!unplanned) {
                Text(localized("Периодичность", "Interval", lang), color = p.muted, fontSize = 12.sp)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    presetIntervals.forEach { months ->
                        ServiceOption(if (months == 0) localized("Без повтора", "One-off", lang)
                            else localized(months.toString() + " мес.", months.toString() + " mo", lang),
                            p, !customSelected && period == months) {

                            customSelected = false
                            period = months
                        }
                    }
                    ServiceOption(
                        localized("Своя…", "Custom…", lang), p, customSelected,
                    ) {
                        if (!customSelected) {
                            val candidate = (customYears.toIntOrNull() ?: 0) * 12 +
                                (customMonths.toIntOrNull() ?: 0)
                            if (candidate in 1..1200) {

                                period = candidate
                            }
                        }
                        customSelected = true
                    }
                }
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
                Text(localized("Предупреждение заранее", "Reminder in advance", lang),
                    color = p.muted, fontSize = 12.sp)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    listOf(0, 1, 2, 3).forEach { months ->
                        ServiceOption(if (months == 0) localized("Нет", "Off", lang)
                            else localized(months.toString() + " мес.", months.toString() + " mo", lang),
                            p, alert == months) { alert = months }
                    }
                }
                if (period > 0) {
                    Text(localized(
                        "«Проведено» пересчитает срок от фактической даты выполнения.",
                        "Serviced will calculate next due from the actual completion date.", lang),
                        color = p.muted, fontSize = 11.sp)
                }
                } // Planned schedule fields
                error?.let { Text(it, color = p.dangerText, fontSize = 12.sp) }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                ServiceOption(localized("Отмена", "Cancel", lang), p, false, onClick = onDismiss)
                Spacer(Modifier.width(8.dp))
                ServiceOption(localized("Сохранить", "Save", lang), p, true) {
                    runCatching {
                        require(existing?.serviceHistory.isNullOrEmpty() ||
                            existing?.unplanned == unplanned) {
                            "Cannot change the type of a service with archived history"
                        }
                        require(!unplanned || requestNumber.trim().isNotBlank()) {
                            "An unplanned request needs its AMOS number"
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
                            // For unplanned work, lastDate is the user-selected
                            // informational request date, never a due date.
                            lastDate = if (unplanned) requestDate
                                else existing?.lastDate?.takeIf { it.isBefore(next) }
                                    ?: next.minusDays(1),
                            nextDate = if (unplanned) null else next,
                            comment = note.trim(),
                            commentHtml = noteHtml,
                            periodMonths = total,
                            remindMonths = if (unplanned) 0 else alert,
                            requestNumber = if (unplanned) requestNumber.trim()
                                else existing?.requestNumber.orEmpty(),
                            serviceHistory = existing?.serviceHistory.orEmpty(),
                            completedAt = existing?.completedAt,
                            unplanned = unplanned,
                        )
                    }.onSuccess { onSave(it) }
                        .onFailure {
                            error = if (unplanned && requestNumber.trim().isBlank())
                                localized("Укажи номер созданной заявки AMOS.",
                                    "Enter the created AMOS request number.", lang)
                            else localized(
                                "Проверь название, даты и параметры обслуживания.",
                                "Check equipment name, dates and service parameters.", lang)
                        }
                }
            }
        }
    }
    if (pickingDate != null) {
        val forRequest = pickingDate == "request"
        NavDateWheelDialog(
            initial = if (forRequest) requestDate else next, p = p, lang = lang,
            onDismiss = { pickingDate = null },
            onSelected = { chosen ->
                if (forRequest) requestDate = chosen else next = chosen
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
        cursorBrush = SolidColor(p.blue),
        modifier = Modifier.fillMaxWidth().height(if (multiline) 86.dp else 44.dp)
            .navGlass(p, radius = 12.dp, role = GlassRole.CONTROL),
        decorationBox = { inner ->
            Box(Modifier.fillMaxSize().padding(12.dp),
                contentAlignment = if (multiline) Alignment.TopStart else Alignment.CenterStart) {
                if (value.isEmpty()) Text(hint, color = p.muted, fontSize = 12.sp)
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
        cursorBrush = SolidColor(p.blue),
        modifier = Modifier.fillMaxWidth().height(44.dp)
            .navGlass(p, radius = 11.dp, role = GlassRole.CONTROL),
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
    Box(modifier.navGlass(p, radius = 12.dp,
        role = if (selected) GlassRole.SELECTED else GlassRole.CONTROL)
        .glassClickable(onClick = onClick)
        .heightIn(min = 44.dp)
        .padding(horizontal = if (compact) 5.dp else 10.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center) {
        Text(label, color = if (selected) p.selectionContent
            else if (destructive) p.systemRed else p.text,
            fontSize = if (compact) 11.sp else 12.sp,
            maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/**
 * Only the AMOS request badge inside Services uses the chosen warm accent.
 * Keep the action/filter selection system blue and preserve the glass dock.
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

@Composable
private fun ServiceAmosRequestButton(
    label: String, p: NavPalette,
    modifier: Modifier = Modifier, onClick: () -> Unit,
) {
    val colors = serviceAmosBadgeColors(p.dark)
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier
            .clip(shape)
            .background(colors.fill, shape)
            .border(1.dp, colors.outline, shape)
            .glassClickable(onClick = onClick)
            .heightIn(min = 44.dp)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label, color = colors.text, fontSize = 12.sp,
            maxLines = 1, overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ServiceConfirmDialog(
    p: NavPalette, title: String, detail: String, lang: UiLanguage,
    onDismiss: () -> Unit, onConfirmed: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().navDialogSurface(p)
            .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(title, color = p.text, fontSize = 20.sp)
            Text(detail, color = p.muted, fontSize = 14.sp)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                ServiceOption(localized("Отмена", "Cancel", lang), p, false, onClick = onDismiss)
                Spacer(Modifier.width(8.dp))
                ServiceOption(localized("Подтвердить", "Confirm", lang), p, true, onClick = onConfirmed)
            }
        }
    }
}
