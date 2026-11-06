package com.navtasks.app

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
    var showClosed by rememberSaveable { mutableStateOf(false) }
    var requestTarget by remember { mutableStateOf<ServiceEntry?>(null) }
    var confirmingComplete by remember { mutableStateOf<ServiceEntry?>(null) }
    var completionDate by remember { mutableStateOf(LocalDate.now()) }
    var completionDatePicker by remember { mutableStateOf(false) }
    var viewingHistory by remember { mutableStateOf<ServiceEntry?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var selectedFilter by rememberSaveable { mutableStateOf(WorkOverviewFilter.ALL) }
    val activeServices = services.entries.filter { it.closedAt == null }.sortedWith(
        compareBy<ServiceEntry> { it.completedAt != null }.thenBy { it.nextDate })
    val closedServices = services.entries.filter { it.closedAt != null }
        .sortedByDescending { it.closedAt }
    val listState = rememberLazyListState()
    val filteredServices = filterServiceOverview(activeServices, now, selectedFilter)
    LaunchedEffect(focusServiceId) {
        if (focusServiceId != null) {
            showClosed = false
            selectedFilter = WorkOverviewFilter.ALL
            withFrameNanos { }
            val index = activeServices.indexOfFirst { it.id == focusServiceId }
            if (index >= 0) listState.scrollToItem(2 + index)
        }
    }
    val overdue = filterServiceOverview(activeServices, now, WorkOverviewFilter.OVERDUE).size
    val soon = filterServiceOverview(activeServices, now, WorkOverviewFilter.SOON).size

    fun reschedule() { NavNotifications.reschedule(context, tasks, settings) }

    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NavGlassCircleButton(p, "back", onClick = onBack)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("NAVTASKS", color = p.blue, fontSize = 12.sp)
                Text(localized("Сервисы", "Maintenance", lang), color = p.text, fontSize = 23.sp)
            }
            NavGlassCircleButton(p, "plus") { selected = null; showEditor = true }
        }
        LazyColumn(
            Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(16.dp, 6.dp, 16.dp, 36.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                NavPanel(p) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ServiceCount(
                            localized("Всего", "All", lang), activeServices.size, p.blue, p,
                            selected = !showClosed && selectedFilter == WorkOverviewFilter.ALL,
                            modifier = Modifier.weight(1f),
                        ) { showClosed = false; selectedFilter = WorkOverviewFilter.ALL }
                        ServiceCount(
                            localized("Скоро", "Soon", lang), soon,
                            if (soon > 0) p.warningText else p.muted, p,
                            selected = !showClosed && selectedFilter == WorkOverviewFilter.SOON,
                            modifier = Modifier.weight(1f),
                        ) { showClosed = false; selectedFilter = WorkOverviewFilter.SOON }
                        ServiceCount(
                            localized("Просрочены", "Overdue", lang), overdue,
                            if (overdue > 0) p.dangerText else p.muted, p,
                            selected = !showClosed && selectedFilter == WorkOverviewFilter.OVERDUE,
                            modifier = Modifier.weight(1f),
                        ) { showClosed = false; selectedFilter = WorkOverviewFilter.OVERDUE }
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
                        p, !showClosed, modifier = Modifier.weight(1f),
                    ) { showClosed = false }
                    ServiceOption(
                        localized("Закрытые · ${closedServices.size}", "Closed · ${closedServices.size}", lang),
                        p, showClosed, modifier = Modifier.weight(1f),
                    ) { showClosed = true }
                }
            }
            message?.let { msg ->
                item { Text(msg, color = p.muted, fontSize = 12.sp) }
            }
            val visible = if (showClosed) closedServices else filteredServices
            if (visible.isEmpty()) {
                item {
                    NavPanel(p) {
                        Text(
                            if (showClosed)
                                localized("Закрытых сервисов пока нет.", "No closed services yet.", lang)
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
            if (showClosed) {
                items(closedServices, key = { it.id }) { service ->
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
                val late = !completed && service.nextDate.isBefore(now)
                val approaching = serviceDueSoon(service, now)
                val accent = if (completed) p.successText
                    else if (late) p.dangerText
                    else if (approaching) p.warningText else p.successText
                Column(
                    Modifier.fillMaxWidth().navGlass(p, radius = 18.dp, role = GlassRole.TASK)
                        .glassClickable { viewingHistory = service }
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(9.dp),
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
                        localized("Последнее обслуживание: ", "Last serviced: ", lang) +
                            navDate(service.serviceHistory.lastOrNull()?.completedAt
                                ?: service.lastDate, lang, true),
                        color = p.successText, fontSize = 12.sp,
                    )
                    if (!completed) {
                        Text(localized("Следующее обслуживание: ", "Next service: ", lang) +
                            navDate(service.nextDate, lang, true),
                            color = accent, fontSize = 12.sp)
                        ServiceOption(
                            if (service.requestNumber.isBlank())
                                localized("Заявка создана", "Request created", lang)
                            else localized("Заявка AMOS: ", "AMOS request: ", lang) +
                                service.requestNumber,
                            p, service.requestNumber.isNotBlank(), Modifier.fillMaxWidth(),
                        ) { requestTarget = service }
                    }
                    if (service.comment.isNotBlank()) {
                        Text(service.comment, color = p.muted, fontSize = 12.sp, maxLines = 2,
                            overflow = TextOverflow.Ellipsis)
                    }
                    Text(
                        if (completed) localized("Завершено", "Completed", lang)
                        else if (late) localized("Просрочено", "Overdue", lang)
                        else if (approaching)
                            localized("Осталось " + ChronoUnit.DAYS.between(now, service.nextDate) + " дн.",
                                ChronoUnit.DAYS.between(now, service.nextDate).toString() + " days left", lang)
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
                            modifier = Modifier.weight(1f)) {
                            confirmingDelete = service
                        }
                    }
                }
                }
            }
        }
    }
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
                Text(navDate(completionDate, lang, true), color = p.blue, fontSize = 13.sp)
                if (entry.periodMonths > 0) {
                    Text(
                        localized("Следующий сервис: ", "Next service: ", lang) +
                            navDate(completionDate.plusMonths(entry.periodMonths.toLong()),
                                lang, true),
                        color = p.blue, fontSize = 13.sp,
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
                            Text(
                                localized("По графику: ", "Scheduled: ", lang) +
                                    navDate(item.scheduledDate, lang, true),
                                color = p.muted, fontSize = 12.sp,
                            )
                            Text(
                                localized("Проведено: ", "Completed: ", lang) +
                                    navDate(item.completedAt, lang, true),
                                color = p.successText, fontSize = 12.sp,
                            )
                            if (item.requestNumber.isNotBlank()) {
                                Text(
                                    localized("Заявка AMOS: ", "AMOS request: ", lang) +
                                        item.requestNumber,
                                    color = p.blue, fontSize = 12.sp,
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
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
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
                modifier = Modifier.weight(1f), onClick = onPurge)
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
        Text(number.toString(), color = color, fontSize = 22.sp)
        Text(label, color = if (selected) p.blue else p.muted, fontSize = 11.sp,
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
    var last by remember(existing?.id) { mutableStateOf(existing?.lastDate ?: LocalDate.now()) }
    var next by remember(existing?.id) { mutableStateOf(existing?.nextDate ?: LocalDate.now().plusMonths(12)) }
    var note by remember(existing?.id) { mutableStateOf(existing?.comment.orEmpty()) }
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
                Text(localized("Оборудование", "Equipment", lang), color = p.muted, fontSize = 12.sp)
                ServiceInput(equipment, { equipment = it.take(200) },
                    localized("Название", "Name", lang), p, false)
                Text(localized("Следующее обслуживание", "Next service date", lang),
                    color = p.muted, fontSize = 12.sp)
                ServiceOption(navDate(next, lang, true), p, false, Modifier.fillMaxWidth()) {
                    pickingDate = "next"
                }
                Text(localized("Комментарий", "Comment", lang), color = p.muted, fontSize = 12.sp)
                ServiceInput(note, { note = it.take(6000) },
                    localized("Работы, расходники, примечания…", "Details and remarks…", lang), p, true)
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
                error?.let { Text(it, color = p.dangerText, fontSize = 12.sp) }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                ServiceOption(localized("Отмена", "Cancel", lang), p, false, onClick = onDismiss)
                Spacer(Modifier.width(8.dp))
                ServiceOption(localized("Сохранить", "Save", lang), p, true) {
                    runCatching {
                        val total = if (customSelected) {
                            val years = customYears.toIntOrNull() ?: -1
                            val months = customMonths.toIntOrNull() ?: -1
                            require(years >= 0 && months in 0..11 && years * 12 + months in 1..1200)
                            years * 12 + months
                        } else period
                        ServiceEntry(
                            id = existing?.id ?: UUID.randomUUID().toString(),
                            equipment = equipment.trim(),
                            lastDate = existing?.lastDate?.takeIf { it.isBefore(next) }
                                ?: next.minusDays(1),
                            nextDate = next,
                            comment = note.trim(),
                            periodMonths = total,
                            remindMonths = alert,
                            requestNumber = existing?.requestNumber.orEmpty(),
                            serviceHistory = existing?.serviceHistory.orEmpty(),
                            completedAt = existing?.completedAt,
                        )
                    }.onSuccess { onSave(it) }
                        .onFailure {
                            error = localized(
                                "Проверь название и даты. Следующий срок должен быть позже последнего.",
                                "Check name and dates. Next due must follow the last date.", lang)
                        }
                }
            }
        }
    }
    if (pickingDate != null) {
        NavDateWheelDialog(
            initial = next, p = p, lang = lang,
            onDismiss = { pickingDate = null },
            onSelected = { chosen -> next = chosen; pickingDate = null },
        )
    }
}

@Composable
private fun ServiceInput(
    value: String, onChange: (String) -> Unit, hint: String, p: NavPalette, multiline: Boolean,
) {
    BasicTextField(
        value = value, onValueChange = onChange, singleLine = !multiline,
        maxLines = if (multiline) 5 else 1,
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
    modifier: Modifier = Modifier, onClick: () -> Unit,
) {
    Box(modifier.navGlass(p, radius = 12.dp,
        role = if (selected) GlassRole.SELECTED else GlassRole.CONTROL)
        .glassClickable(onClick = onClick)
        .heightIn(min = 44.dp)
        .padding(horizontal = 10.dp, vertical = 10.dp), contentAlignment = Alignment.Center) {
        Text(label, color = if (selected) p.blue else p.text, fontSize = 12.sp,
            maxLines = 1, overflow = TextOverflow.Ellipsis)
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
