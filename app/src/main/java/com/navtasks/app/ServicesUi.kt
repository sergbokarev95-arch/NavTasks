package com.navtasks.app

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
) {
    val context = LocalContext.current
    val now = LocalDate.now()
    var showEditor by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<ServiceEntry?>(null) }
    var confirmingDelete by remember { mutableStateOf<ServiceEntry?>(null) }
    var confirmingComplete by remember { mutableStateOf<ServiceEntry?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    val sorted = services.entries.sortedBy { it.nextDate }
    val overdue = sorted.count { it.nextDate.isBefore(now) }
    val soon = sorted.count { !it.nextDate.isBefore(now) && (
        it.nextDate <= now.plusDays(30) ||
        (it.remindMonths > 0 && !now.isBefore(serviceWarningStart(it)))
    ) }

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
            contentPadding = PaddingValues(16.dp, 6.dp, 16.dp, 36.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                NavPanel(p) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        ServiceCount(localized("Всего", "All", lang), sorted.size, p.blue, p)
                        ServiceCount(localized("Скоро", "Soon", lang), soon,
                            if (soon > 0) Color(0xFFE6AD72) else p.muted, p)
                        ServiceCount(localized("Просрочены", "Overdue", lang), overdue,
                            if (overdue > 0) Color(0xFFE98B84) else p.muted, p)
                    }
                    Text(localized("Оборудование, даты, комментарии. Без вложений.",
                        "Equipment, dates and notes. No attachments.", lang),
                        color = p.muted, fontSize = 11.sp)
                }
            }
            message?.let { msg ->
                item { Text(msg, color = p.muted, fontSize = 12.sp) }
            }
            if (sorted.isEmpty()) {
                item {
                    NavPanel(p) {
                        Text(localized("Нет сервисов. Добавь оборудование кнопкой +.",
                            "No records. Add equipment with +.", lang), color = p.muted)
                    }
                }
            }
            items(sorted, key = { it.id }) { service ->
                val late = service.nextDate.isBefore(now)
                val approaching = !late && (service.nextDate <= now.plusDays(30) ||
                    (service.remindMonths > 0 && !now.isBefore(serviceWarningStart(service))))
                val accent = if (late) Color(0xFFE98B84) else if (approaching)
                    Color(0xFFE6AD72) else p.mint
                Column(
                    Modifier.fillMaxWidth().navGlass(p, radius = 18.dp, role = GlassRole.TASK)
                        .glassClickable { selected = service; showEditor = true }
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
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column(Modifier.weight(1f)) {
                            Text(localized("Последний", "Last", lang), color = p.muted, fontSize = 11.sp)
                            Text(navDate(service.lastDate, lang, true), color = p.text, fontSize = 12.sp)
                        }
                        Column(Modifier.weight(1f)) {
                            Text(localized("Следующий", "Next", lang), color = p.muted, fontSize = 11.sp)
                            Text(navDate(service.nextDate, lang, true), color = accent, fontSize = 12.sp)
                        }
                    }
                    if (service.comment.isNotBlank()) {
                        Text(service.comment, color = p.muted, fontSize = 12.sp, maxLines = 2,
                            overflow = TextOverflow.Ellipsis)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            if (late) localized("Просрочено", "Overdue", lang)
                            else if (approaching)
                                localized("Осталось " + ChronoUnit.DAYS.between(now, service.nextDate) + " дн.",
                                    ChronoUnit.DAYS.between(now, service.nextDate).toString() + " days left", lang)
                            else localized("Запланировано", "Scheduled", lang),
                            color = accent, fontSize = 11.sp, modifier = Modifier.weight(1f),
                        )
                        if (service.periodMonths > 0) {
                            ServiceOption(localized("Проведено", "Serviced", lang), p, false) {
                                confirmingComplete = service
                            }
                        }
                        Spacer(Modifier.width(6.dp))
                        ServiceOption(localized("Удалить", "Delete", lang), p, false) {
                            confirmingDelete = service
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
    confirmingDelete?.let { entry ->
        ServiceConfirmDialog(
            p, localized("Удалить сервис?", "Delete service?", lang),
            entry.equipment, lang, onDismiss = { confirmingDelete = null },
        ) {
            runCatching { services.delete(entry.id); reschedule() }
                .onFailure { message = it.localizedMessage }
            confirmingDelete = null
        }
    }
    confirmingComplete?.let { entry ->
        val finished = LocalDate.now()
        val next = nextServiceFromCompletion(entry, finished)
        ServiceConfirmDialog(
            p, localized("Сервис выполнен?", "Service completed?", lang),
            entry.equipment + "\n" + localized("Следующий: ", "Next: ", lang) +
                navDate(next.nextDate, lang, true),
            lang, onDismiss = { confirmingComplete = null },
        ) {
            runCatching { services.complete(entry.id, finished); reschedule() }
                .onFailure { message = it.localizedMessage }
            confirmingComplete = null
        }
    }
}

@Composable
private fun ServiceCount(label: String, number: Int, color: Color, p: NavPalette) {
    Column {
        Text(number.toString(), color = color, fontSize = 22.sp)
        Text(label, color = p.muted, fontSize = 11.sp)
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
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text(localized("Последний", "Last", lang), color = p.muted, fontSize = 11.sp)
                        ServiceOption(navDate(last, lang, true), p, false, Modifier.fillMaxWidth()) {
                            pickingDate = "last"
                        }
                    }
                    Column(Modifier.weight(1f)) {
                        Text(localized("Следующий", "Next", lang), color = p.muted, fontSize = 11.sp)
                        ServiceOption(navDate(next, lang, true), p, false, Modifier.fillMaxWidth()) {
                            pickingDate = "next"
                        }
                    }
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
                            if (period > 0 && months > 0 && next == last.plusMonths(period.toLong()))
                                next = last.plusMonths(months.toLong())
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
                                if (period > 0 && next == last.plusMonths(period.toLong()))
                                    next = last.plusMonths(candidate.toLong())
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
                                        if (period > 0 && next == last.plusMonths(period.toLong()))
                                            next = last.plusMonths(total.toLong())
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
                                        if (period > 0 && next == last.plusMonths(period.toLong()))
                                            next = last.plusMonths(total.toLong())
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
                        color = if (total in 1..1200) p.muted else Color(0xFFE98B84),
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
                error?.let { Text(it, color = Color(0xFFE98B84), fontSize = 12.sp) }
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
                        ServiceEntry(existing?.id ?: UUID.randomUUID().toString(),
                            equipment.trim(), last, next, note.trim(), total, alert)
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
    pickingDate?.let { field ->
        NavDateWheelDialog(
            initial = if (field == "last") last else next,
            p = p, lang = lang,
            onDismiss = { pickingDate = null },
            onSelected = { chosen ->
                if (field == "last") {
                    if (period > 0 && next == last.plusMonths(period.toLong())) {
                        next = chosen.plusMonths(period.toLong())
                    }
                    last = chosen
                } else {
                    next = chosen
                }
                pickingDate = null
            },
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
        modifier = Modifier.fillMaxWidth().height(43.dp)
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
        .padding(horizontal = 11.dp, vertical = 10.dp), contentAlignment = Alignment.Center) {
        Text(label, color = if (selected) p.blue else p.text, fontSize = 12.sp, maxLines = 1)
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
