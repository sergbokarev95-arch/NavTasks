package com.navtasks.app

import android.app.DatePickerDialog
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import java.io.IOException
import android.app.AlarmManager
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.abs

/** The same swipe gesture changes the visible unit at the current calendar zoom. */
internal data class CalendarSwipePosition(val year: Int, val month: Int, val day: LocalDate)

internal fun calendarSwipePosition(
    level: Int, year: Int, month: Int, day: LocalDate, step: Int,
): CalendarSwipePosition = when (level) {
    0 -> CalendarSwipePosition(year + step, month, day)
    1 -> YearMonth.of(year, month).plusMonths(step.toLong()).let {
        CalendarSwipePosition(it.year, it.monthValue, day)
    }
    else -> day.plusDays(step.toLong()).let { CalendarSwipePosition(it.year, it.monthValue, it) }
}

@Composable
fun PlanScreenV2(store: TaskStore, today: LocalDate, p: NavPalette, lang: UiLanguage, onEdit: (TaskTemplate) -> Unit) {
    var level by rememberSaveable { mutableIntStateOf(1) } // 0 year, 1 month, 2 day
    var year by rememberSaveable { mutableIntStateOf(today.year) }
    var month by rememberSaveable { mutableIntStateOf(today.monthValue) }
    var selectedDay by rememberSaveable { mutableStateOf(today) }
    // Day and year are zoomed-in/out subpages of Plan, not separate Android tasks.
    // Intercept both system edge swipes and the Back button before leaving Plan.
    BackHandler(enabled = level != 1) { level = 1 }
    val locale = if (lang == UiLanguage.RU) Locale("ru") else Locale.ENGLISH
    fun moveBySwipe(step: Int) {
        val next = calendarSwipePosition(level, year, month, selectedDay, step)
        year = next.year
        month = next.month
        selectedDay = next.day
    }
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { level = 0 }) { Text("$year", color = p.blue) }
            Text("/", color = p.muted)
            TextButton(onClick = { level = 1 }) {
                Text(YearMonth.of(year, month).month.getDisplayName(TextStyle.FULL_STANDALONE, locale).replaceFirstChar { it.uppercase() }, color = p.blue)
            }
            Spacer(Modifier.weight(1f))
            TextButton(onClick = { year = today.year; month = today.monthValue; selectedDay = today; level = 2 }) {
                Text(localized("Сегодня", "Today", lang), color = p.mint)
            }
        }
        Crossfade(
            targetState = level,
            modifier = Modifier.fillMaxSize().pointerInput(level, year, month, selectedDay) {
                var horizontalDistance = 0f
                detectHorizontalDragGestures(
                    onDragStart = { horizontalDistance = 0f },
                    onHorizontalDrag = { change, dragAmount ->
                        horizontalDistance += dragAmount
                        change.consume()
                    },
                    onDragEnd = {
                        if (abs(horizontalDistance) >= 58.dp.toPx()) {
                            moveBySwipe(if (horizontalDistance < 0f) 1 else -1)
                        }
                    },
                    onDragCancel = { horizontalDistance = 0f },
                )
            },
            animationSpec = tween(220),
            label = "calendarZoom",
        ) { zoom ->
            when (zoom) {
                0 -> Column {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { year-- }) { Text("‹", fontSize = 26.sp) }
                        Text("$year", color = p.text, fontSize = 24.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        TextButton(onClick = { year++ }) { Text("›", fontSize = 26.sp) }
                    }
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 11.dp, end = 11.dp, top = 11.dp, bottom = 125.dp),
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                        verticalArrangement = Arrangement.spacedBy(7.dp),
                    ) {
                        items((1..12).toList()) { m ->
                            Column(
                                Modifier.fillMaxWidth().navGlass(p, radius = 13.dp,
                                    role = if (year == today.year && m == today.monthValue) GlassRole.NESTED_TASK else GlassRole.PANEL)
                                    .glassClickable { month = m; level = 1 }.padding(7.dp),
                            ) {
                                Text(YearMonth.of(year, m).month.getDisplayName(TextStyle.SHORT_STANDALONE, locale),
                                    color = p.text, fontSize = 12.sp, fontWeight = FontWeight.Medium,
                                    modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                                Spacer(Modifier.height(5.dp))
                                monthGridDates(year, m).chunked(7).forEach { week ->
                                    Row(Modifier.fillMaxWidth()) {
                                        week.forEach { date ->
                                            val isInMonth = date.monthValue == m && date.year == year
                                            val mark = store.dayCounts(date).state(date, today)
                                            val markColor = when (mark) {
                                                CalendarDayState.COMPLETE -> p.mint
                                                CalendarDayState.PARTIAL -> Color(0xFFE9B35A)
                                                CalendarDayState.INCOMPLETE -> Color(0xFFE88B83)
                                                CalendarDayState.SCHEDULED -> p.blue
                                                else -> p.muted
                                            }
                                            Text("${date.dayOfMonth}",
                                                color = when {
                                                    date == today -> p.blue
                                                    !isInMonth -> p.muted.copy(alpha = 0.40f)
                                                    mark == CalendarDayState.EMPTY -> p.muted
                                                    else -> markColor
                                                }, fontSize = 8.sp, lineHeight = 12.sp, textAlign = TextAlign.Center,
                                                modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> Column {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp), verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = {
                            if (month == 1) { month = 12; year-- } else month--
                        }) { Text("‹", fontSize = 27.sp) }
                        Text("${YearMonth.of(year, month).month.getDisplayName(TextStyle.FULL_STANDALONE, locale).replaceFirstChar { it.uppercase() }} $year",
                            modifier = Modifier.weight(1f), color = p.text, fontSize = 19.sp, textAlign = TextAlign.Center)
                        TextButton(onClick = {
                            if (month == 12) { month = 1; year++ } else month++
                        }) { Text("›", fontSize = 27.sp) }
                    }
                    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                        (1..7).forEach { dow ->
                            Text(DayOfWeek.of(dow).getDisplayName(TextStyle.SHORT, locale),
                                color = p.muted, fontSize = 11.sp, textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f).padding(vertical = 7.dp))
                        }
                    }
                    monthGridDates(year, month).chunked(7).forEach { week ->
                        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            week.forEach { date ->
                                val isToday = date == today
                                val inMonth = date.year == year && date.monthValue == month
                                val status = store.dayCounts(date).state(date, today)
                                val statusColor = when (status) {
                                    CalendarDayState.COMPLETE -> p.mint
                                    CalendarDayState.PARTIAL -> Color(0xFFE7B25B)
                                    CalendarDayState.INCOMPLETE -> Color(0xFFE98881)
                                    CalendarDayState.SCHEDULED -> p.blue
                                    CalendarDayState.EMPTY -> p.border
                                }
                                val shape = RoundedCornerShape(10.dp)
                                val cellColor = when {
                                    isToday -> p.blue.copy(alpha = if (p.dark) 0.88f else 0.25f)
                                    status == CalendarDayState.COMPLETE -> p.mint.copy(alpha = if (p.dark) 0.13f else 0.09f)
                                    status == CalendarDayState.PARTIAL -> statusColor.copy(alpha = 0.11f)
                                    status == CalendarDayState.INCOMPLETE -> statusColor.copy(alpha = 0.08f)
                                    else -> if (p.dark) Color.White.copy(alpha = 0.035f) else Color.White.copy(alpha = 0.10f)
                                }
                                val edge = when {
                                    isToday -> p.blue
                                    !inMonth -> p.border.copy(alpha = 0.20f)
                                    status == CalendarDayState.EMPTY -> p.border.copy(alpha = 0.48f)
                                    else -> statusColor.copy(alpha = 0.85f)
                                }
                                Column(
                                    Modifier.weight(1f).height(54.dp)
                                        .clip(shape).background(cellColor, shape)
                                        .border(if (isToday) 2.dp else 1.dp, edge, shape)
                                        .glassClickable { selectedDay = date; year = date.year; month = date.monthValue; level = 2 },
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                ) {
                                    Text("${date.dayOfMonth}", color = when {
                                        isToday -> if (p.dark) Color(0xFF092C43) else p.text
                                        !inMonth -> p.muted.copy(alpha = 0.52f)
                                        else -> p.text
                                    }, fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal, fontSize = 14.sp)
                                    if (status != CalendarDayState.EMPTY) {
                                        Box(Modifier.padding(top = 4.dp).size(5.dp)
                                            .background(if (isToday) (if (p.dark) Color(0xFF092C43) else p.blue) else statusColor.copy(alpha = if (inMonth) 1f else 0.45f), CircleShape))
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(3.dp))
                    }
                    Row(Modifier.fillMaxWidth().padding(horizontal = 19.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        listOf(
                            p.mint to localized("Все выполнено", "All done", lang),
                            Color(0xFFE7B25B) to localized("Часть", "Partial", lang),
                            Color(0xFFE98881) to localized("Пропущено", "Missed", lang),
                        ).forEach { (color, title) ->
                            Row(verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                Box(Modifier.size(6.dp).background(color, CircleShape))
                                Text(title, color = p.muted, fontSize = 10.sp)
                            }
                        }
                    }
                }
                else -> Column {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { level = 1 }) { Text("‹", fontSize = 25.sp) }
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(navDate(selectedDay, lang, true), color = p.text, fontSize = 19.sp)
                            Text(selectedDay.dayOfWeek.getDisplayName(TextStyle.FULL, locale), color = p.muted, fontSize = 12.sp)
                        }
                        TextButton(onClick = { level = 0 }) { Text("▦", fontSize = 21.sp) }
                    }
                    val rows = store.forDate(selectedDay)
                    LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 125.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (rows.isEmpty()) item {
                            NavPanel(p) { Text(localized("Задач на этот день нет", "No tasks for this day", lang), color = p.muted) }
                        }
                        items(rows, key = { it.second.key }) { (task, run) ->
                            PlanReadOnlyTile(task, run, store, p, lang, onEdit)
                        }
                    }
                }
            }
        }
    }
}

/** Calendar is a planning surface: read statuses, move/remove an occurrence or edit its template.
 * It never completes an occurrence. */
@Composable
private fun PlanReadOnlyTile(
    task: TaskTemplate, run: TaskRun, store: TaskStore, p: NavPalette,
    lang: UiLanguage, onEdit: (TaskTemplate) -> Unit,
) {
    val context = LocalContext.current
    var menuOpen by remember(run.key) { mutableStateOf(false) }
    var deleteDialog by remember(run.key) { mutableStateOf(false) }
    Row(
        Modifier.fillMaxWidth().navGlass(p, radius = 15.dp,
            role = if (run.checklist.isNotEmpty()) GlassRole.NESTED_TASK else GlassRole.TASK)
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Status icon, not a checkbox: the calendar cannot mark any task complete.
        Text(if (run.completed) "✓" else "·", color = if (run.completed) p.mint else p.muted,
            fontSize = 19.sp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(task.title, color = if (run.completed) p.muted else p.text, fontSize = 14.sp,
                textDecoration = if (run.completed) androidx.compose.ui.text.style.TextDecoration.LineThrough else null)
            Text(
                if (run.completed) localized("Выполнено", "Completed", lang)
                else if (run.transferred) localized("Перенесено", "Moved", lang)
                else localized("Запланировано", "Scheduled", lang),
                color = if (run.completed) p.mint else p.muted, fontSize = 11.sp,
            )
        }
        Box {
            Text("⋯", color = p.blue, fontSize = 22.sp,
                modifier = Modifier.glassClickable { menuOpen = true }.padding(horizontal = 9.dp, vertical = 6.dp))
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(text = { Text(localized("Редактировать", "Edit", lang)) }, onClick = {
                    menuOpen = false
                    onEdit(task)
                })
                if (!run.completed) DropdownMenuItem(text = { Text(localized("Перенести на дату…", "Move to date…", lang)) },
                    onClick = {
                        menuOpen = false
                        val start = run.displayDate
                        DatePickerDialog(context, { _, y, m, d ->
                            store.postpone(run.key, LocalDate.of(y, m + 1, d))
                        }, start.year, start.monthValue - 1, start.dayOfMonth).show()
                    })
                DropdownMenuItem(text = { Text(localized("Удалить из дня", "Remove from day", lang)) }, onClick = {
                    menuOpen = false
                    deleteDialog = true
                })
            }
        }
    }
    if (deleteDialog) AlertDialog(
        onDismissRequest = { deleteDialog = false },
        title = { Text(localized("Убрать задачу из календаря?", "Remove this task from the calendar?", lang)) },
        text = { Text(localized("Удаляется только это выполнение. Шаблон повторяющейся задачи останется.",
            "Only this occurrence is removed. The recurring task template remains.", lang)) },
        confirmButton = { TextButton(onClick = { store.skip(run.key); deleteDialog = false }) {
            Text(localized("Удалить", "Remove", lang))
        } },
        dismissButton = { TextButton(onClick = { deleteDialog = false }) {
            Text(localized("Отмена", "Cancel", lang))
        } },
    )
}

/** Six complete weeks, including dimmed trailing/leading dates of neighbouring months. */
internal fun monthGridDates(year: Int, month: Int): List<LocalDate> {
    val first = LocalDate.of(year, month, 1)
    val start = first.minusDays((first.dayOfWeek.value - 1).toLong())
    return (0 until 42).map { start.plusDays(it.toLong()) }
}

@Composable
fun TasksScreenV2(
    store: TaskStore, filter: Frequency, onFilter: (Frequency) -> Unit,
    p: NavPalette, lang: UiLanguage,
    onEdit: (TaskTemplate) -> Unit,
    onHandover: () -> Unit,
) {
    val labels = listOf(
        localized("День", "Daily", lang), localized("Неделя", "Weekly", lang),
        localized("Месяц", "Monthly", lang), localized("Разовые", "Extra", lang),
    )
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                .navGlass(p, radius = 17.dp, role = GlassRole.PANEL)
                .glassClickable(onClick = onHandover)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NavGlassGlyph("tasks", p.blue, Modifier.size(23.dp))
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text("Handover", color = p.text, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                Text(localized("Передача дел и заметки для сменщика", "Notes and tasks for your relief officer", lang),
                    color = p.muted, fontSize = 11.sp)
            }
            Text("›", color = p.blue, fontSize = 24.sp)
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 13.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Frequency.entries.forEachIndexed { i, freq ->
                Box(Modifier.weight(1f).navGlass(p, radius = 14.dp,
                    role = if (filter == freq) GlassRole.SELECTED else GlassRole.CONTROL, elevation = 0.dp)
                    .glassClickable { onFilter(freq) }.padding(vertical = 11.dp),
                    contentAlignment = Alignment.Center) {
                    Text(labels[i], color = if (filter == freq) p.blue else p.text, fontSize = 11.sp, maxLines = 1)
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 17.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Text("${store.templates.count { it.frequency == filter }} ${localized("задач", "tasks", lang)}",
                color = p.muted, fontSize = 13.sp)
        }
        LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 5.dp, bottom = 180.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)) {
            val filtered = store.templates.filter { it.frequency == filter }
            if (filtered.isEmpty()) item { NavPanel(p) { Text(localized("Задач пока нет", "No tasks yet", lang), color = p.muted) } }
            items(filtered, key = { it.id }) { task ->
                Row(Modifier.fillMaxWidth().navGlass(p, radius = 17.dp,
                    role = if (task.checklist.isNotEmpty()) GlassRole.NESTED_TASK else GlassRole.TASK)
                    .glassClickable { onEdit(task) }
                    .padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
                    // Templates are not executable, so no fake empty checkbox or checkbox icon here.
                    Spacer(Modifier.width(1.dp))
                    Column(Modifier.weight(1f)) {
                        Text(task.title, color = if (task.enabled) p.text else p.muted, fontSize = 14.sp)
                        Text(
                            when {
                                task.frequency == Frequency.EXTRA -> navDate(task.startDate, lang)
                                task.scheduleMode == ScheduleMode.AFTER_COMPLETION -> localized("От выполнения", "After completion", lang)
                                task.frequency == Frequency.WEEKLY -> DayOfWeek.of(task.dayOfWeek).getDisplayName(
                                    TextStyle.FULL, if (lang == UiLanguage.RU) Locale("ru") else Locale.ENGLISH)
                                task.frequency == Frequency.MONTHLY -> "${task.dayOfMonth}"
                                else -> localized("Каждый день", "Every day", lang)
                            }, color = p.muted, fontSize = 11.sp,
                        )
                    }
                    if (task.checklist.isNotEmpty()) Text("${task.checklist.size}", color = p.blue, fontSize = 12.sp)
                    Spacer(Modifier.width(5.dp))
                    Text("›", color = p.muted, fontSize = 24.sp)
                }
            }
        }
    }
}

@Composable
private fun FormLabel(ru: String, en: String, lang: UiLanguage, p: NavPalette) {
    Text(localized(ru, en, lang), color = p.muted, fontSize = 12.sp)
}

@Composable
private fun OptionLine(options: List<Pair<String, String>>, selected: String, p: NavPalette, onSelect: (String) -> Unit) {
    // FlowRow keeps *all* choices visible, even on smaller Samsung/HONOR screens.
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        options.forEach { (key, label) ->
            val active = selected == key
            Box(
                Modifier.navGlass(p, radius = 13.dp,
                    role = if (active) GlassRole.SELECTED else GlassRole.CONTROL,
                    elevation = 0.dp)
                    .glassClickable { onSelect(key) }
                    .padding(horizontal = 13.dp, vertical = 11.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(label, color = if (active) p.blue else p.text, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun TaskEditorScreen(
    initial: TaskTemplate?, defaultFrequency: Frequency,
    lang: UiLanguage, palette: NavPalette,
    onBack: () -> Unit, onSave: (TaskTemplate) -> Unit,
) {
    val p = palette
    val context = LocalContext.current
    var title by remember(initial?.id) { mutableStateOf(initial?.title.orEmpty()) }
    var frequency by remember(initial?.id) { mutableStateOf(initial?.frequency ?: defaultFrequency) }
    var mode by remember(initial?.id) { mutableStateOf(initial?.scheduleMode ?: ScheduleMode.CALENDAR) }
    var date by remember(initial?.id) { mutableStateOf(initial?.startDate ?: LocalDate.now()) }
    var weekDay by remember(initial?.id) { mutableIntStateOf(initial?.dayOfWeek ?: LocalDate.now().dayOfWeek.value) }
    var monthDay by remember(initial?.id) { mutableStateOf((initial?.dayOfMonth ?: LocalDate.now().dayOfMonth).toString()) }
    var interval by remember(initial?.id) { mutableStateOf((initial?.intervalCount ?: 1).toString()) }
    var missedPolicy by remember(initial?.id) { mutableStateOf(initial?.missedPolicy ?: MissedPolicy.HISTORY) }
    var time by remember(initial?.id) { mutableStateOf(initial?.deadlineTime) }
    var notified by remember(initial?.id) { mutableStateOf(initial?.notificationsEnabled ?: true) }
    var reminder by remember(initial?.id) { mutableIntStateOf(initial?.remindMinutesBefore ?: 60) }
    var checklist by remember(initial?.id) { mutableStateOf(initial?.checklist ?: emptyList()) }
    var newItem by remember(initial?.id) { mutableStateOf("") }
    var enabled by remember(initial?.id) { mutableStateOf(initial?.enabled ?: true) }
    val valid = title.isNotBlank() && (monthDay.toIntOrNull() ?: 0) in 1..31 && (interval.toIntOrNull() ?: 0) > 0

    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            NavGlassCircleButton(p, "back", onClick = onBack)
            Spacer(Modifier.width(12.dp))
            Text(localized(if (initial == null) "Новая задача" else "Редактировать задачу",
                if (initial == null) "New task" else "Edit task", lang),
                modifier = Modifier.weight(1f), color = p.text, fontSize = 21.sp)
            TextButton(enabled = valid, onClick = {
                onSave((initial ?: TaskTemplate(title = title.trim(), frequency = frequency)).copy(
                    title = title.trim(), frequency = frequency,
                    scheduleMode = if (frequency == Frequency.EXTRA) ScheduleMode.CALENDAR else mode,
                    startDate = date, dayOfWeek = weekDay, dayOfMonth = monthDay.toInt(),
                    intervalCount = interval.toInt(), missedPolicy = missedPolicy,
                    completionMode = CompletionMode.AUTOMATIC,
                    deadlineTime = time, notificationsEnabled = notified,
                    remindMinutesBefore = reminder,
                    checklist = checklist.filter { it.title.isNotBlank() }.map { it.copy(required = true) },
                    enabled = enabled,
                ))
            }) { Text(localized("Сохранить", "Save", lang)) }
        }
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(11.dp)) {
            NavPanel(p) {
                FormLabel("Название задачи", "Task name", lang, p)
                Spacer(Modifier.height(5.dp))
                NavGlassInput(title, { title = it }, p,
                    placeholder = localized("Название", "Title", lang), modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
                FormLabel("Категория", "Category", lang, p)
                OptionLine(Frequency.entries.map { it.name to when (it) {
                    Frequency.DAILY -> localized("День", "Daily", lang)
                    Frequency.WEEKLY -> localized("Неделя", "Weekly", lang)
                    Frequency.MONTHLY -> localized("Месяц", "Monthly", lang)
                    Frequency.EXTRA -> localized("Разовые", "Extra", lang)
                } }, frequency.name, p) { frequency = Frequency.valueOf(it) }
            }
            NavPanel(p) {
                if (frequency != Frequency.EXTRA) {
                    FormLabel("Повторение", "Recurrence", lang, p)
                    OptionLine(listOf(ScheduleMode.CALENDAR.name to localized("По расписанию", "By schedule", lang),
                        ScheduleMode.AFTER_COMPLETION.name to localized("От выполнения", "From completion", lang)), mode.name, p) {
                        mode = ScheduleMode.valueOf(it)
                    }
                    Spacer(Modifier.height(10.dp))
                }
                if (frequency == Frequency.WEEKLY && mode == ScheduleMode.CALENDAR) {
                    FormLabel("День недели", "Weekday", lang, p)
                    val loc = if (lang == UiLanguage.RU) Locale("ru") else Locale.ENGLISH
                    OptionLine((1..7).map { n -> "$n" to DayOfWeek.of(n).getDisplayName(TextStyle.SHORT, loc) },
                        "$weekDay", p) { weekDay = it.toInt() }
                }
                if (frequency == Frequency.MONTHLY && mode == ScheduleMode.CALENDAR) {
                    FormLabel("Число месяца", "Day of month", lang, p)
                    OutlinedTextField(monthDay, { monthDay = it }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                }
                if (frequency != Frequency.EXTRA && mode == ScheduleMode.AFTER_COMPLETION) {
                    FormLabel("Интервал между выполнениями", "Interval between completions", lang, p)
                    OutlinedTextField(interval, { interval = it }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                }
                FormLabel(if (frequency == Frequency.EXTRA) "Дата выполнения" else "Дата начала",
                    if (frequency == Frequency.EXTRA) "Due date" else "Start date", lang, p)
                OutlinedButton(onClick = {
                    DatePickerDialog(context, { _, y, m, d -> date = LocalDate.of(y, m + 1, d) },
                        date.year, date.monthValue - 1, date.dayOfMonth).show()
                }, modifier = Modifier.fillMaxWidth()) { Text(navDate(date, lang, true)) }
                Spacer(Modifier.height(10.dp))
                FormLabel("Срок выполнения", "Deadline", lang, p)
                OptionLine(listOf("day" to localized("До конца дня", "End of day", lang),
                    "time" to localized("Конкретное время", "Specific time", lang)),
                    if (time == null) "day" else "time", p) {
                    time = if (it == "day") null else time ?: LocalTime.of(18, 0)
                }
                if (time != null) {
                    OutlinedButton(onClick = {
                        val current = time ?: LocalTime.of(18, 0)
                        TimePickerDialog(context, { _, h, m -> time = LocalTime.of(h, m) },
                            current.hour, current.minute, true).show()
                    }, modifier = Modifier.fillMaxWidth()) { Text(time.toString()) }
                }
            }
            NavPanel(p) {
                FormLabel("Если пропущена", "When overdue", lang, p)
                OptionLine(listOf(
                    MissedPolicy.HISTORY.name to localized("В пропущенные", "Keep in Missed", lang),
                    MissedPolicy.CARRY.name to localized("Автоперенос", "Auto move", lang),
                    MissedPolicy.ASK.name to localized("Спросить", "Ask me", lang),
                    MissedPolicy.ARCHIVE.name to localized("В историю", "Archive", lang),
                ), missedPolicy.name, p) { missedPolicy = MissedPolicy.valueOf(it) }
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(localized("Уведомления", "Notifications", lang), color = p.text, modifier = Modifier.weight(1f))
                    Switch(notified, onCheckedChange = { notified = it }, colors = navSwitchColors(p))
                }
                if (notified) {
                    FormLabel("Напомнить", "Remind me", lang, p)
                    OptionLine(listOf(0 to localized("В срок", "At deadline", lang),
                        15 to localized("За 15 минут", "15 minutes before", lang),
                        60 to localized("За час", "1 hour before", lang),
                        1440 to localized("За день", "1 day before", lang)).map { "${it.first}" to it.second },
                        "$reminder", p) { reminder = it.toInt() }
                }
            }
            NavPanel(p) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(localized("Вложенный чек-лист", "Nested checklist", lang), color = p.text,
                        modifier = Modifier.weight(1f), fontSize = 16.sp)
                    Text("${checklist.size}", color = p.blue)
                }
                Spacer(Modifier.height(8.dp))
                checklist.forEach { item ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        NavGlassInput(item.title, { value ->
                            checklist = checklist.map { if (it.id == item.id) it.copy(title = value) else it }
                        }, p, modifier = Modifier.weight(1f))
                        TextButton(onClick = { checklist = checklist.filterNot { it.id == item.id } }) { Text("×") }
                    }
                    Spacer(Modifier.height(5.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    NavGlassInput(newItem, { newItem = it }, p,
                        placeholder = localized("Добавить пункт", "Add item", lang), modifier = Modifier.weight(1f))
                    TextButton(onClick = {
                        if (newItem.isNotBlank()) { checklist = checklist + CheckItem(title = newItem.trim()); newItem = "" }
                    }) { Text("+") }
                }
                Text(localized("Когда все пункты отмечены, задача завершается автоматически.",
                    "All checked items automatically complete the parent task.", lang), color = p.muted, fontSize = 11.sp)
            }
            NavPanel(p) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(localized("Задача активна", "Task enabled", lang), color = p.text, modifier = Modifier.weight(1f))
                    Switch(enabled, onCheckedChange = { enabled = it }, colors = navSwitchColors(p))
                }
            }
            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
fun SettingsScreen(settings: NavSettings, store: TaskStore, p: NavPalette, onBack: () -> Unit) {
    val lang = settings.language
    val context = LocalContext.current
    var transferMessage by remember { mutableStateOf<String?>(null) }
    var transferError by remember { mutableStateOf(false) }
    var pendingImport by remember { mutableStateOf<List<TaskTemplate>?>(null) }
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        if (uri != null) {
            runCatching {
                val stream = context.contentResolver.openOutputStream(uri)
                    ?: throw IOException("Cannot create file")
                stream.bufferedWriter(Charsets.UTF_8).use { it.write(NavTaskTransfer.export(store.templates)) }
            }.onSuccess {
                transferError = false
                transferMessage = localized("Задачи экспортированы в файл.", "Tasks exported to file.", lang)
            }.onFailure {
                transferError = true
                transferMessage = it.localizedMessage ?: localized("Ошибка экспорта", "Export failed", lang)
            }
        }
    }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            runCatching {
                val stream = context.contentResolver.openInputStream(uri)
                    ?: throw IOException("Cannot read file")
                val raw = stream.bufferedReader(Charsets.UTF_8).use { reader ->
                    val buffer = CharArray(4096)
                    val result = StringBuilder()
                    while (true) {
                        val read = reader.read(buffer)
                        if (read == -1) break
                        if (result.length + read > 2_000_000) throw IOException("File exceeds 2 MB")
                        result.append(buffer, 0, read)
                    }
                    result.toString()
                }
                NavTaskTransfer.parse(raw)
            }.onSuccess { templates ->
                pendingImport = templates
                transferMessage = null
            }.onFailure {
                transferError = true
                transferMessage = it.localizedMessage ?: localized("Ошибка импорта", "Import failed", lang)
            }
        }
    }
    fun syncSettings() {
        NavNotifications.reschedule(context, store, settings)
        NavWidgetUpdater.updateAll(context, store)
    }
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            NavGlassCircleButton(p, "back", onClick = onBack)
            Spacer(Modifier.width(12.dp))
            Text(localized("Настройки", "Settings", lang), color = p.text, fontSize = 23.sp)
        }
        Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)) {
            NavPanel(p) {
                FormLabel("Язык", "Language", lang, p)
                OptionLine(listOf("RU" to "Русский", "EN" to "English"), settings.language.name, p) {
                    settings.updateLanguage(UiLanguage.valueOf(it))
                    syncSettings()
                }
                Spacer(Modifier.height(12.dp))
                FormLabel("Оформление", "Appearance", lang, p)
                OptionLine(listOf(
                    "SYSTEM" to localized("Как в системе", "Follow system", lang),
                    "LIGHT" to localized("День", "Light", lang),
                    "DARK" to localized("Ночь", "Dark", lang),
                ), settings.theme.name, p) { settings.updateTheme(UiTheme.valueOf(it)); syncSettings() }
            }
            NavPanel(p) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(localized("Уведомления", "Notifications", lang), color = p.text, modifier = Modifier.weight(1f))
                    Switch(settings.notificationsEnabled, onCheckedChange = { settings.setNotifications(it); syncSettings() }, colors = navSwitchColors(p))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(localized("Ежедневная сводка пропусков", "Daily missed-task digest", lang),
                        color = p.text, modifier = Modifier.weight(1f), fontSize = 14.sp)
                    Switch(settings.dailyDigestEnabled, onCheckedChange = { settings.setDigest(it); syncSettings() },
                        enabled = settings.notificationsEnabled, colors = navSwitchColors(p))
                }
                if (settings.notificationsEnabled && settings.dailyDigestEnabled) {
                    OutlinedButton(onClick = {
                        TimePickerDialog(context, { _, h, m -> settings.setDigestTime(h, m); syncSettings() },
                            settings.digestHour, settings.digestMinute, true).show()
                    }, modifier = Modifier.fillMaxWidth()) {
                        Text(localized("Время сводки", "Digest time", lang) +
                            "  %02d:%02d".format(settings.digestHour, settings.digestMinute))
                    }
                }
                Text(localized("Дата и время берутся только с телефона. Отдельного судового UTC нет.",
                    "Date and time follow the phone. No separate ship time zone.", lang),
                    color = p.muted, fontSize = 12.sp)
                if (Build.VERSION.SDK_INT >= 31 &&
                    !context.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()) {
                    Text(localized("Для напоминаний строго по времени разреши точные будильники в настройках Android.",
                        "For precise reminders, allow exact alarms in Android settings.", lang),
                        color = p.muted, fontSize = 12.sp)
                    OutlinedButton(onClick = {
                        context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                            Uri.parse("package:${context.packageName}")))
                    }, modifier = Modifier.fillMaxWidth()) {
                        Text(localized("Разрешить точные напоминания", "Allow exact reminders", lang))
                    }
                }
            }
            NavPanel(p) {
                Text(localized("Обмен задачами", "Share tasks", lang),
                    color = p.text, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(3.dp))
                Text(localized("Можно отправить другу файл с шаблонами задач и вложенными проверками. История выполнения и настройки телефона не передаются.",
                    "Share task templates and checklists with a friend. Completion history and device settings are not shared.", lang),
                    color = p.muted, fontSize = 12.sp)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(9.dp),
                    modifier = Modifier.fillMaxWidth()) {
                    Box(Modifier.weight(1f).navGlass(p, radius = 13.dp, role = GlassRole.CONTROL)
                        .glassClickable {
                            exportLauncher.launch("NavTasks_tasks_${LocalDate.now()}.json")
                        }.padding(vertical = 13.dp), contentAlignment = Alignment.Center) {
                        Text(localized("Экспорт", "Export", lang), color = p.blue, fontSize = 13.sp)
                    }
                    Box(Modifier.weight(1f).navGlass(p, radius = 13.dp, role = GlassRole.CONTROL)
                        .glassClickable { importLauncher.launch(arrayOf("application/json", "text/plain", "application/octet-stream")) }
                        .padding(vertical = 13.dp), contentAlignment = Alignment.Center) {
                        Text(localized("Импорт", "Import", lang), color = p.blue, fontSize = 13.sp)
                    }
                }
                Text(localized("Импорт добавляет новые задачи. Существующие задачи и отметки о выполнении не заменяются.",
                    "Import adds new tasks only. Existing tasks and completion records are never replaced.", lang),
                    color = p.muted, fontSize = 11.sp)
                if (transferMessage != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(transferMessage.orEmpty(), color = if (transferError) Color(0xFFE48E83) else p.mint,
                        fontSize = 12.sp)
                }
            }
            NavPanel(p) {
                Text("NavTasks Glass 0.3.0 beta", color = p.text, fontWeight = FontWeight.Medium)
                Text(localized("Данные сохраняются на телефоне и доступны без интернета.",
                    "Data stays on this device and works offline.", lang), color = p.muted, fontSize = 12.sp)
            }
        }
    }
    pendingImport?.let { imported ->
        val newCount = NavTaskTransfer.countNew(store.templates, imported)
        AlertDialog(
            onDismissRequest = { pendingImport = null },
            title = { Text(localized("Импорт задач", "Import tasks", lang)) },
            text = {
                Text(localized(
                    "В файле: ${imported.size}. Новых задач: $newCount. Уже существующих: ${imported.size - newCount}. Добавить новые задачи без изменения истории?",
                    "In file: ${imported.size}. New tasks: $newCount. Already existing: ${imported.size - newCount}. Add new tasks without changing history?", lang))
            },
            confirmButton = {
                TextButton(onClick = {
                    val added = store.importTemplates(imported)
                    pendingImport = null
                    transferError = false
                    transferMessage = localized("Добавлено задач: $added", "Tasks added: $added", lang)
                }) { Text(localized("Добавить", "Add", lang)) }
            },
            dismissButton = {
                TextButton(onClick = { pendingImport = null }) {
                    Text(localized("Отмена", "Cancel", lang))
                }
            },
        )
    }
}

@Composable
private fun navSwitchColors(p: NavPalette) = SwitchDefaults.colors(
    checkedTrackColor = p.blue.copy(alpha = 0.76f),
    checkedThumbColor = Color.White,
    checkedBorderColor = p.blue.copy(alpha = 0.60f),
    uncheckedTrackColor = p.panel,
    uncheckedThumbColor = p.muted,
    uncheckedBorderColor = p.border,
)
