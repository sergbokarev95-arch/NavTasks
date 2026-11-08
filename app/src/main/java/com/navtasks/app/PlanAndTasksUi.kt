package com.navtasks.app

import com.kyant.backdrop.backdrops.layerBackdrop

import android.app.DatePickerDialog
import androidx.activity.compose.BackHandler
import android.app.AlarmManager
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.Crossfade
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.lazy.itemsIndexed
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
import kotlinx.coroutines.delay
import kotlin.math.abs

/** Weekend styling in the month grid is not a task status. */
internal fun calendarIsWeekend(date: LocalDate): Boolean =
    date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY

/** Shared warm weekend ink for both Plan and the Today week strip. */
internal fun calendarWeekendInk(p: NavPalette): Color =
    if (p.dark) Color(0xFFE5A4A0) else Color(0xFFB36563)

/** Preserve existing status-dot meaning independently of its layout position. */
internal fun calendarHasOutcomeDot(state: CalendarDayState, hasWork: Boolean): Boolean =
    hasWork || (state != CalendarDayState.EMPTY && state != CalendarDayState.SCHEDULED)

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
fun PlanScreenV2(
    store: TaskStore, today: LocalDate, p: NavPalette, lang: UiLanguage,
    onEdit: (TaskTemplate) -> Unit,
    workOrders: WorkOrderStore, services: ServiceStore,
) {
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
                Text(localized("Сегодня", "Today", lang), color = p.successText)
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
                        NavGlassIconAction(
                            "chevron-left", p.blue, localized("Предыдущий год", "Previous year", lang),
                        ) { year-- }
                        Text("$year", color = p.text, fontSize = 24.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        NavGlassIconAction(
                            "chevron-right", p.blue, localized("Следующий год", "Next year", lang),
                        ) { year++ }
                    }
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 11.dp, end = 11.dp, top = 11.dp, bottom = 28.dp),
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
                                            val workPlanned = workAgendaExistsOnDay(
                                                date, workOrders.entries, services.entries)
                                            val markColor = if (mark == CalendarDayState.EMPTY && workPlanned) p.blue else when (mark) {
                                                CalendarDayState.COMPLETE -> p.mint
                                                CalendarDayState.PARTIAL -> p.systemOrange
                                                CalendarDayState.INCOMPLETE -> Color(0xFFE88B83)
                                                CalendarDayState.SCHEDULED -> p.blue
                                                else -> p.muted
                                            }
                                            Text("${date.dayOfMonth}",
                                                color = when {
                                                    date == today -> p.blue
                                                    !isInMonth -> p.muted.copy(alpha = 0.40f)
                                                    mark == CalendarDayState.EMPTY && !workPlanned -> p.muted
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
                    val weekendInk = calendarWeekendInk(p)
                    Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp), verticalAlignment = Alignment.CenterVertically) {
                        NavGlassIconAction(
                            "chevron-left", p.blue, localized("Предыдущий месяц", "Previous month", lang),
                        ) {
                            if (month == 1) { month = 12; year-- } else month--
                        }
                        Text("${YearMonth.of(year, month).month.getDisplayName(TextStyle.FULL_STANDALONE, locale).replaceFirstChar { it.uppercase() }} $year",
                            modifier = Modifier.weight(1f), color = p.text, fontSize = 19.sp, textAlign = TextAlign.Center)
                        NavGlassIconAction(
                            "chevron-right", p.blue, localized("Следующий месяц", "Next month", lang),
                        ) {
                            if (month == 12) { month = 1; year++ } else month++
                        }
                    }
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        (1..7).forEach { dow ->
                            Text(DayOfWeek.of(dow).getDisplayName(TextStyle.SHORT, locale),
                                color = if (dow >= 6) weekendInk else p.muted,
                                fontSize = 11.sp, textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f).padding(vertical = 7.dp))
                        }
                    }
                    monthGridDates(year, month).chunked(7).forEach { week ->
                        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            week.forEach { date ->
                                val isToday = date == today
                                val inMonth = date.year == year && date.monthValue == month
                                val status = store.dayCounts(date).state(date, today)
                                val hasWork = workAgendaExistsOnDay(
                                    date, workOrders.entries, services.entries)
                                val statusColor = if (status == CalendarDayState.EMPTY && hasWork) p.blue else when (status) {
                                    CalendarDayState.COMPLETE -> p.mint
                                    CalendarDayState.PARTIAL -> p.systemOrange
                                    CalendarDayState.INCOMPLETE -> Color(0xFFE98881)
                                    CalendarDayState.SCHEDULED -> p.blue
                                    CalendarDayState.EMPTY -> p.border
                                }
                                val shape = RoundedCornerShape(10.dp)
                                // Quiet filled cells; no neon-blue outlines.
                                val cellColor = when {
                                    isToday -> p.blue.copy(alpha = if (p.dark) 0.24f else 0.14f)
                                    !inMonth -> if (p.dark) Color.White.copy(alpha = 0.018f)
                                        else p.blue.copy(alpha = 0.018f)
                                    status == CalendarDayState.COMPLETE ->
                                        p.mint.copy(alpha = if (p.dark) 0.13f else 0.09f)
                                    status == CalendarDayState.PARTIAL ->
                                        statusColor.copy(alpha = if (p.dark) 0.12f else 0.09f)
                                    status == CalendarDayState.INCOMPLETE ->
                                        statusColor.copy(alpha = if (p.dark) 0.10f else 0.07f)
                                    else -> if (p.dark) Color.White.copy(alpha = 0.075f)
                                        else p.blue.copy(alpha = 0.055f)
                                }
                                Box(
                                    Modifier.weight(1f).height(54.dp)
                                        .clip(shape).background(cellColor, shape)
                                        .glassClickable {
                                            selectedDay = date
                                            year = date.year
                                            month = date.monthValue
                                            level = 2
                                        },
                                ) {
                                    // Number stays at the exact center with or without a dot.
                                    Text(
                                        "${date.dayOfMonth}",
                                        modifier = Modifier.align(Alignment.Center),
                                        color = when {
                                            !inMonth -> p.muted.copy(alpha = 0.46f)
                                            calendarIsWeekend(date) -> weekendInk
                                            isToday -> p.blue
                                            else -> p.text
                                        },
                                        fontWeight = if (isToday) FontWeight.Medium else FontWeight.Normal,
                                        fontSize = 16.sp,
                                    )
                                    if (calendarHasOutcomeDot(status, hasWork)) {
                                        Box(
                                            Modifier.align(Alignment.BottomCenter)
                                                .padding(bottom = 6.dp).size(5.dp)
                                                .background(
                                                    statusColor.copy(alpha = if (inMonth) 1f else 0.35f),
                                                    CircleShape,
                                                ),
                                        )
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
                            p.systemOrange to localized("Часть", "Partial", lang),
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
                        NavGlassIconAction(
                            "chevron-left", p.blue, localized("К месяцу", "Back to month", lang),
                        ) { level = 1 }
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(navDate(selectedDay, lang, true), color = p.text, fontSize = 19.sp)
                            Text(selectedDay.dayOfWeek.getDisplayName(TextStyle.FULL, locale), color = p.muted, fontSize = 12.sp)
                        }
                        NavGlassIconAction(
                            "calendar", p.blue, localized("Выбрать год", "View year", lang),
                        ) { level = 0 }
                    }
                    val rows = store.forDate(selectedDay)
                    val plannedWorks = workAgendaExistsOnDay(
                        selectedDay, workOrders.entries, services.entries)
                    LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (rows.isEmpty() && !plannedWorks) item {
                            NavPanel(p) { Text(localized("Задач на этот день нет", "No tasks for this day", lang), color = p.muted) }
                        }
                        items(rows, key = { it.second.key }) { (task, run) ->
                            PlanReadOnlyTile(task, run, store, p, lang, onEdit)
                        }
                        if (plannedWorks) item(key = "planned-work") {
                            WorkAgendaCalendarRows(
                                selectedDay, workOrders.entries, services.entries, p, lang)
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
    var movingToDate by remember(run.key) { mutableStateOf<LocalDate?>(null) }
    var pendingScopedMove by remember(run.key) { mutableStateOf<LocalDate?>(null) }
    fun requestMoveDate(day: LocalDate) {
        if (day == run.displayDate) return
        if (canMoveSeries(task, run, day)) pendingScopedMove = day
        else store.postpone(run.key, day)
    }
    Row(
        Modifier.fillMaxWidth().navGlass(p, radius = 15.dp,
            role = if (run.checklist.isNotEmpty()) GlassRole.NESTED_TASK else GlassRole.TASK)
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Status icon, not a checkbox: the calendar cannot mark any task complete.
        Text(if (run.completed) "✓" else "·", color = if (run.completed) p.successText else p.muted,
            fontSize = 19.sp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(task.title, color = if (run.completed) p.muted else p.text, fontSize = 14.sp,
                textDecoration = if (run.completed) androidx.compose.ui.text.style.TextDecoration.LineThrough else null)
            Text(
                if (run.completed) localized("Выполнено", "Completed", lang)
                else if (run.transferred) localized("Перенесено", "Moved", lang)
                else localized("Запланировано", "Scheduled", lang),
                color = if (run.completed) p.successText else p.muted, fontSize = 11.sp,
            )
        }
        Box {
            NavGlassIconAction(
                "more", p.blue, localized("Действия с задачей", "Task actions", lang),
            ) { menuOpen = true }
            NavPopoverMenu(p = p, expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                NavPopoverAction(localized("Редактировать", "Edit", lang), p) {
                    menuOpen = false
                    onEdit(task)
                }
                if (!run.completed) NavPopoverAction(localized("Перенести на дату…", "Move to date…", lang), p) {
                    menuOpen = false
                    movingToDate = run.displayDate
                }
                NavPopoverDivider(p)
                NavPopoverAction(localized("Удалить из дня", "Remove from day", lang), p,
                    destructive = true) {
                    menuOpen = false
                    deleteDialog = true
                }
            }
        }
    }
    pendingScopedMove?.let { newDate ->
        MoveScopeDialog(
            p = p, lang = lang, title = task.title,
            oldDate = run.displayDate, newDate = newDate,
            onOnlyThis = {
                pendingScopedMove = null
                store.postpone(run.key, newDate)
            },
            onThisAndFuture = {
                pendingScopedMove = null
                store.postponeSeriesFrom(run.key, newDate)
            },
            onDismiss = { pendingScopedMove = null },
        )
    }
    movingToDate?.let { start ->
        NavDateWheelDialog(
            initial = start, p = p, lang = lang,
            onDismiss = { movingToDate = null },
            onSelected = { selected ->
                movingToDate = null
                requestMoveDate(selected)
            },
        )
    }
    if (deleteDialog) NavGlassAlertDialog(p = p, 
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
) {
    val labels = listOf(
        localized("День", "Daily", lang), localized("Неделя", "Weekly", lang),
        localized("Месяц", "Monthly", lang), localized("Разовые", "Extra", lang),
    )
    val filtered = store.templates.filter {
        TaskSchedule.effectiveFrequency(it, LocalDate.now()) == filter
    }
    Column(Modifier.fillMaxSize()) {
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
        // This panel is informative, not a disclosure control: tasks remain visible.
        Row(
            Modifier.fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 5.dp)
                .navGlass(p, radius = 14.dp, role = GlassRole.PANEL)
                .border(1.dp, p.warningText.copy(alpha = 0.52f), RoundedCornerShape(14.dp))
                .background(p.warningText.copy(alpha = if (p.dark) 0.10f else 0.055f),
                    RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(localized("Список задач", "Task list", lang), color = p.warningText,
                fontSize = 13.sp, modifier = Modifier.weight(1f))
            Text(filtered.size.toString() + " " + localized("задач", "tasks", lang),
                color = p.warningText, fontSize = 12.sp)
        }
        // Keep the same lazy list, stable item keys and editing navigation.
        LazyColumn(
            Modifier.weight(1f),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp,
                top = 5.dp, bottom = 180.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            if (filtered.isEmpty()) item { NavPanel(p) { Text(localized("Задач пока нет", "No tasks yet", lang), color = p.muted) } }
            itemsIndexed(filtered, key = { _, item -> item.id }) { _, task ->
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
                                TaskSchedule.effectiveFrequency(task, LocalDate.now()) == Frequency.EXTRA -> navDate(task.startDate, lang)
                                task.scheduleMode == ScheduleMode.AFTER_COMPLETION -> localized("От выполнения", "After completion", lang)
                                TaskSchedule.effectiveFrequency(task, LocalDate.now()) == Frequency.WEEKLY -> DayOfWeek.of(effectiveCalendarWeekday(task, LocalDate.now())).getDisplayName(
                                    TextStyle.FULL, if (lang == UiLanguage.RU) Locale("ru") else Locale.ENGLISH)
                                TaskSchedule.effectiveFrequency(task, LocalDate.now()) == Frequency.MONTHLY -> "${effectiveCalendarMonthDay(task, LocalDate.now())}"
                                else -> localized("Каждый день", "Every day", lang)
                            }, color = p.muted, fontSize = 11.sp,
                        )
                    }
                    if (task.checklist.isNotEmpty()) Text("${task.checklist.size}", color = p.blue, fontSize = 12.sp)
                    Spacer(Modifier.width(5.dp))
                    NavGlassGlyph("chevron-right", p.muted, Modifier.size(17.dp))
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

/** Editor-only preview; delegates to the existing calendar calculation.
 * It never creates tasks or modifies a stored schedule. */
internal fun monthlySchedulePreview(
    activeFrom: LocalDate, dayOfMonth: Int, count: Int = 3,
): List<LocalDate> {
    if (dayOfMonth !in 1..31 || count <= 0) return emptyList()
    val preview = TaskTemplate(
        title = "preview", frequency = Frequency.MONTHLY,
        scheduleMode = ScheduleMode.CALENDAR,
        startDate = activeFrom, dayOfMonth = dayOfMonth,
    )
    val upcoming = mutableListOf<LocalDate>()
    var cursor = activeFrom
    repeat(count) {
        val next = TaskSchedule.nextCalendarDate(preview, cursor) ?: return upcoming
        upcoming.add(next)
        cursor = next.plusDays(1)
    }
    return upcoming
}

@Composable
fun TaskEditorScreen(
    initial: TaskTemplate?, defaultFrequency: Frequency,
    lang: UiLanguage, palette: NavPalette,
    store: TaskStore,
    onBack: () -> Unit, onSave: (TaskTemplate) -> Unit,
) {
    val p = palette
    val context = LocalContext.current
    var title by remember(initial?.id) { mutableStateOf(initial?.title.orEmpty()) }
    var frequency by remember(initial?.id) {
        mutableStateOf(initial?.let {
            TaskSchedule.effectiveFrequency(it, LocalDate.now())
        } ?: defaultFrequency)
    }
    var mode by remember(initial?.id) { mutableStateOf(initial?.scheduleMode ?: ScheduleMode.CALENDAR) }
    var date by remember(initial?.id) { mutableStateOf(initial?.startDate ?: LocalDate.now()) }
    // A previous scoped move may have superseded the original template weekday.
    // Start from the currently effective rule, not historical base values.
    var weekDay by remember(initial?.id) {
        mutableIntStateOf(initial?.let { effectiveCalendarWeekday(it, LocalDate.now()) }
            ?: LocalDate.now().dayOfWeek.value)
    }
    var monthDay by remember(initial?.id) {
        mutableStateOf((initial?.let { effectiveCalendarMonthDay(it, LocalDate.now()) }
            ?: LocalDate.now().dayOfMonth).toString())
    }
    // Remember the fixed date if the user briefly selects "Last day of month".
    // A 31 in existing profiles already means the end of every month.
    var fixedMonthDay by remember(initial?.id) {
        val selected = initial?.let { effectiveCalendarMonthDay(it, LocalDate.now()) }
            ?: LocalDate.now().dayOfMonth
        mutableStateOf((if (selected == 31) date.dayOfMonth.coerceAtMost(30)
            else selected).toString())
    }
    var interval by remember(initial?.id) { mutableStateOf((initial?.intervalCount ?: 1).toString()) }
    var missedPolicy by remember(initial?.id) { mutableStateOf(initial?.missedPolicy ?: MissedPolicy.HISTORY) }
    var time by remember(initial?.id) { mutableStateOf(initial?.deadlineTime) }
    var showDateWheel by remember(initial?.id) { mutableStateOf(false) }
    var showTimeWheel by remember(initial?.id) { mutableStateOf(false) }
    var notified by remember(initial?.id) { mutableStateOf(initial?.notificationsEnabled ?: true) }
    var reminder by remember(initial?.id) { mutableIntStateOf(initial?.remindMinutesBefore ?: 60) }
    var checklist by remember(initial?.id) { mutableStateOf(initial?.checklist ?: emptyList()) }
    var editingChecklistNote by remember(initial?.id) { mutableStateOf<String?>(null) }
    var enabled by remember(initial?.id) { mutableStateOf(initial?.enabled ?: true) }
    var showTaskDeletionOptions by remember(initial?.id) { mutableStateOf(false) }
    var confirmPermanentTaskDeletion by remember(initial?.id) { mutableStateOf(false) }
    var taskDeletionError by remember(initial?.id) { mutableStateOf<String?>(null) }
    var pendingCalendarDayEdit by remember(initial?.id) {
        mutableStateOf<Pair<TaskTemplate, CalendarDayEditPreview>?>(null)
    }
    var pendingFrequencyEdit by remember(initial?.id) {
        mutableStateOf<Pair<TaskTemplate, FrequencyChangePreview>?>(null)
    }
    var unsupportedFrequencyEdit by remember(initial?.id) { mutableStateOf(false) }
    val valid = title.isNotBlank() && (monthDay.toIntOrNull() ?: 0) in 1..31 && (interval.toIntOrNull() ?: 0) > 0

    val editorBackdrop = if (android.os.Build.VERSION.SDK_INT >= 33)
        com.kyant.backdrop.backdrops.rememberLayerBackdrop() else null
    Box(Modifier.fillMaxSize()) {
    Column(Modifier.fillMaxSize().then(
        if (editorBackdrop != null) Modifier.layerBackdrop(editorBackdrop) else Modifier
    ).statusBarsPadding().navigationBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            if (editorBackdrop != null) Spacer(Modifier.size(44.dp))
            else NavGlassCircleButton(p, "back", onClick = onBack)
            Spacer(Modifier.width(12.dp))
            Text(localized(if (initial == null) "Новая задача" else "Редактировать задачу",
                if (initial == null) "New task" else "Edit task", lang),
                modifier = Modifier.weight(1f), color = p.text, fontSize = 21.sp)
            TextButton(enabled = valid, onClick = {
                val edited = (initial ?: TaskTemplate(title = title.trim(), frequency = frequency)).copy(
                    title = title.trim(), frequency = frequency,
                    scheduleMode = if (frequency == Frequency.EXTRA) ScheduleMode.CALENDAR else mode,
                    startDate = date, dayOfWeek = weekDay, dayOfMonth = monthDay.toInt(),
                    intervalCount = interval.toInt(), missedPolicy = missedPolicy,
                    completionMode = CompletionMode.AUTOMATIC,
                    deadlineTime = time, notificationsEnabled = notified,
                    remindMinutesBefore = reminder,
                    checklist = checklist.filter { it.title.isNotBlank() }.map { it.copy(required = true) },
                    enabled = enabled,
                )
                val effectiveOldFrequency = initial?.let {
                    TaskSchedule.effectiveFrequency(it, LocalDate.now())
                }
                if (initial != null && effectiveOldFrequency != edited.frequency) {
                    val preview = calendarFrequencyChangePreview(
                        initial, edited, store.runs, LocalDate.now(),
                    )
                    if (preview != null) pendingFrequencyEdit = edited to preview
                    else unsupportedFrequencyEdit = true
                } else {
                    val preview = initial?.let {
                        calendarDayEditPreview(it, edited, store.runs, LocalDate.now())
                    }
                    if (preview != null) {
                        pendingCalendarDayEdit = edited to preview
                    } else {
                        // Metadata changes must retain the historical frequency
                        // and the effective-dated calendar revisions.
                        onSave(initial?.let {
                            preserveHistoricalCalendarBase(it, edited, LocalDate.now())
                        } ?: edited)
                    }
                }
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
                    FormLabel("Когда выполнять каждый месяц", "Monthly execution date", lang, p)
                    Spacer(Modifier.height(5.dp))
                    OptionLine(
                        listOf(
                            "fixed" to localized("В конкретное число", "Specific day", lang),
                            "end" to localized("В последний день", "Last day", lang),
                        ),
                        if (monthDay == "31") "end" else "fixed", p,
                    ) { choice ->
                        if (choice == "end") {
                            if (monthDay.toIntOrNull() in 1..30) fixedMonthDay = monthDay
                            monthDay = "31"
                        } else {
                            monthDay = fixedMonthDay.takeIf {
                                it.toIntOrNull() in 1..30
                            } ?: "1"
                        }
                    }
                    if (monthDay != "31") {
                        Spacer(Modifier.height(10.dp))
                        FormLabel("Число месяца (1–30)", "Day of month (1–30)", lang, p)
                        OutlinedTextField(
                            monthDay, {
                                monthDay = it.take(2)
                                fixedMonthDay = monthDay
                            },
                            modifier = Modifier.fillMaxWidth(), singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        )
                        Text(
                            localized(
                                "Если такого числа нет (например, 30 февраля), работа назначается на последний день месяца.",
                                "If the date does not exist (e.g. February 30), the work is due on the last day.",
                                lang,
                            ),
                            color = p.muted, fontSize = 11.sp,
                        )
                    }
                }
                if (frequency != Frequency.EXTRA && mode == ScheduleMode.AFTER_COMPLETION) {
                    FormLabel("Интервал между выполнениями", "Interval between completions", lang, p)
                    OutlinedTextField(interval, { interval = it }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                }
                FormLabel(
                    when {
                        frequency == Frequency.EXTRA -> "Дата выполнения"
                        frequency == Frequency.MONTHLY && mode == ScheduleMode.CALENDAR ->
                            "Расписание действует с"
                        mode == ScheduleMode.AFTER_COMPLETION -> "Первое выполнение"
                        else -> "Дата начала"
                    },
                    when {
                        frequency == Frequency.EXTRA -> "Due date"
                        frequency == Frequency.MONTHLY && mode == ScheduleMode.CALENDAR ->
                            "Schedule active from"
                        mode == ScheduleMode.AFTER_COMPLETION -> "First execution"
                        else -> "Start date"
                    }, lang, p,
                )
                OutlinedButton(onClick = { showDateWheel = true },
                    modifier = Modifier.fillMaxWidth()) { Text(navDate(date, lang, true)) }
                if (frequency == Frequency.MONTHLY && mode == ScheduleMode.CALENDAR) {
                    Text(
                        localized(
                            "Это не обязательно день выполнения: первая работа появится на ближайшую расчётную дату не раньше указанной.",
                            "This is not necessarily the due date: the first occurrence is the next matching day on or after it.",
                            lang,
                        ),
                        color = p.muted, fontSize = 11.sp,
                    )
                    val upcoming = monthlySchedulePreview(date, monthDay.toIntOrNull() ?: 0)
                    if (upcoming.isNotEmpty()) {
                        Spacer(Modifier.height(7.dp))
                        FormLabel("Ближайшие выполнения", "Upcoming executions", lang, p)
                        Text(
                            upcoming.joinToString("  ·  ") { navDate(it, lang, true) },
                            color = p.blue, fontSize = 12.sp,
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                FormLabel("Срок выполнения", "Deadline", lang, p)
                OptionLine(listOf("day" to localized("До конца дня", "End of day", lang),
                    "time" to localized("Конкретное время", "Specific time", lang)),
                    if (time == null) "day" else "time", p) {
                    time = if (it == "day") null else time ?: LocalTime.of(18, 0)
                }
                if (time != null) {
                    OutlinedButton(onClick = { showTimeWheel = true },
                        modifier = Modifier.fillMaxWidth()) { Text(time.toString()) }
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
                ChecklistEditorPanel(
                    items = checklist,
                    onItemsChange = { updated ->
                        checklist = updated
                        if (editingChecklistNote != null &&
                            updated.none { it.id == editingChecklistNote }) {
                            editingChecklistNote = null
                        }
                    },
                    onEditNote = { editingChecklistNote = it },
                    p = p,
                    lang = lang,
                )
            }
            NavPanel(p) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(localized("Задача активна", "Task enabled", lang), color = p.text, modifier = Modifier.weight(1f))
                    Switch(enabled, onCheckedChange = { enabled = it }, colors = navSwitchColors(p))
                }
            }
            if (initial != null) {
                TextButton(
                    onClick = {
                        taskDeletionError = null
                        showTaskDeletionOptions = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(localized("Удалить задачу", "Delete task", lang),
                        color = p.dangerText, fontSize = 14.sp)
                }
                taskDeletionError?.let { error ->
                    Text(error, color = p.dangerText, fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(28.dp))
        }
    }
    if (editorBackdrop != null) {
        Box(Modifier.align(Alignment.TopStart).statusBarsPadding()
            .padding(start = 14.dp, top = 10.dp)) {
            NavLiquidGlassCircleButton(p, editorBackdrop, "back", onBack)
        }
    }
    }

    if (showTaskDeletionOptions && initial != null) {
        NavGlassAlertDialog(
            p = p,
            onDismissRequest = { showTaskDeletionOptions = false },
            title = {
                Text(localized("Удаление задачи", "Remove task", lang))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(initial.title, color = p.text, fontSize = 15.sp)
                    Text(localized(
                        "Выбери действие. Остановка сохраняет историю и позволяет снова включить задачу. Полное удаление уничтожает все её отметки и записи.",
                        "Choose an action. Stopping keeps history and allows reactivation. Permanent deletion erases all completions and records for this task.",
                        lang,
                    ), color = p.muted, fontSize = 12.sp)
                    if (initial.enabled) {
                        TextButton(
                            onClick = {
                                runCatching { store.stopRepeatingTask(initial.id) }
                                    .onSuccess { stopped ->
                                        if (stopped) {
                                            showTaskDeletionOptions = false
                                            onBack()
                                        } else {
                                            taskDeletionError = localized(
                                                "Задача не найдена", "Task not found", lang)
                                            showTaskDeletionOptions = false
                                        }
                                    }
                                    .onFailure {
                                        taskDeletionError = it.localizedMessage
                                        showTaskDeletionOptions = false
                                    }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                if (initial.frequency == Frequency.EXTRA)
                                    localized("Отключить задачу", "Disable task", lang)
                                else localized("Прекратить повторения", "Stop repeating", lang),
                                color = p.blue,
                            )
                        }
                    }
                    TextButton(
                        onClick = {
                            showTaskDeletionOptions = false
                            confirmPermanentTaskDeletion = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(localized("Удалить полностью", "Delete permanently", lang),
                            color = p.dangerText)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showTaskDeletionOptions = false }) {
                    Text(localized("Отмена", "Cancel", lang))
                }
            },
        )
    }
    if (confirmPermanentTaskDeletion && initial != null) {
        NavGlassAlertDialog(
            p = p,
            onDismissRequest = { confirmPermanentTaskDeletion = false },
            title = {
                Text(localized("Удалить навсегда?", "Delete permanently?", lang))
            },
            text = {
                Text(localized(
                    "Задача «" + initial.title + "» и вся её история выполнений, пропусков и переносов будут удалены без возможности восстановления. Остальные задачи останутся.",
                    "The task “" + initial.title + "” and its completion, missed and moved history will be erased permanently. Other tasks stay unchanged.",
                    lang,
                ), color = p.muted, fontSize = 13.sp)
            },
            confirmButton = {
                TextButton(onClick = {
                    runCatching { store.deleteTaskPermanently(initial.id) }
                        .onSuccess { deleted ->
                            confirmPermanentTaskDeletion = false
                            if (deleted) onBack()
                            else taskDeletionError = localized(
                                "Задача уже удалена", "Task already deleted", lang)
                        }
                        .onFailure {
                            confirmPermanentTaskDeletion = false
                            taskDeletionError = it.localizedMessage
                        }
                }) {
                    Text(localized("Удалить полностью", "Delete permanently", lang),
                        color = p.dangerText)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmPermanentTaskDeletion = false }) {
                    Text(localized("Отмена", "Cancel", lang))
                }
            },
        )
    }

    pendingFrequencyEdit?.let { (edited, preview) ->
        val oldLabel = when (preview.oldFrequency) {
            Frequency.DAILY -> localized("Ежедневно", "Daily", lang)
            Frequency.WEEKLY -> localized("Еженедельно", "Weekly", lang)
            Frequency.MONTHLY -> localized("Ежемесячно", "Monthly", lang)
            Frequency.EXTRA -> localized("Разово", "One-off", lang)
        }
        val newLabel = when (preview.newFrequency) {
            Frequency.DAILY -> localized("Ежедневно", "Daily", lang)
            Frequency.WEEKLY -> localized("Еженедельно", "Weekly", lang)
            Frequency.MONTHLY -> localized("Ежемесячно", "Monthly", lang)
            Frequency.EXTRA -> localized("Разово", "One-off", lang)
        }
        MoveScopeDialog(
            p = p, lang = lang, title = edited.title,
            oldDate = preview.selectedRun.displayDate,
            newDate = preview.newDate,
            summary = "$oldLabel → $newLabel",
            onOnlyThis = {
                pendingFrequencyEdit = null
                if (store.applyCalendarFrequencyEdit(
                    edited, CalendarEditScope.ONLY_THIS, preview.referenceDate)) onBack()
            },
            onThisAndFuture = {
                pendingFrequencyEdit = null
                if (store.applyCalendarFrequencyEdit(
                    edited, CalendarEditScope.THIS_AND_FUTURE, preview.referenceDate)) onBack()
            },
            onDismiss = { pendingFrequencyEdit = null },
        )
    }
    if (unsupportedFrequencyEdit) {
        NavGlassAlertDialog(
            p = p,
            onDismissRequest = { unsupportedFrequencyEdit = false },
            title = { Text(localized("Нужна отдельная настройка",
                "Separate scheduling required", lang)) },
            text = { Text(localized(
                "Выбор области действия пока поддерживается для повторений по календарю: " +
                    "День, Неделя и Месяц. Разовые и интервалы «От выполнения» " +
                    "нельзя безопасно изменить этим способом.",
                "Scope selection currently supports calendar-based Daily, Weekly, and Monthly " +
                    "recurrences. One-off tasks and after-completion intervals need a separate change.",
                lang,
            )) },
            confirmButton = {
                TextButton(onClick = { unsupportedFrequencyEdit = false }) {
                    Text(localized("Понятно", "OK", lang))
                }
            },
        )
    }
    pendingCalendarDayEdit?.let { (edited, preview) ->
        MoveScopeDialog(
            p = p, lang = lang, title = edited.title,
            oldDate = preview.selectedRun.displayDate,
            newDate = preview.newDate,
            onOnlyThis = {
                pendingCalendarDayEdit = null
                if (store.applyCalendarDayEdit(
                        edited, CalendarEditScope.ONLY_THIS, preview.referenceDate)) onBack()
            },
            onThisAndFuture = {
                pendingCalendarDayEdit = null
                if (store.applyCalendarDayEdit(
                        edited, CalendarEditScope.THIS_AND_FUTURE, preview.referenceDate)) onBack()
            },
            onDismiss = { pendingCalendarDayEdit = null },
        )
    }

    if (showDateWheel) {
        NavDateWheelDialog(
            initial = date, p = p, lang = lang,
            onDismiss = { showDateWheel = false },
            onSelected = { date = it; showDateWheel = false },
        )
    }
    if (showTimeWheel) {
        NavTimeWheelDialog(
            initial = time ?: LocalTime.of(18, 0), p = p, lang = lang,
            onDismiss = { showTimeWheel = false },
            onSelected = { time = it; showTimeWheel = false },
        )
    }
    editingChecklistNote?.let { itemId ->
        checklist.firstOrNull { it.id == itemId }?.let { item ->
            ChecklistReferenceDialog(
                p = p, lang = lang, title = item.title,
                note = item.comment, noteHtml = item.commentHtml, editable = true,
                onDismiss = { editingChecklistNote = null },
                onSave = { text, html ->
                    checklist = checklist.map { existing ->
                        if (existing.id == itemId) existing.copy(comment = text, commentHtml = html) else existing
                    }
                    editingChecklistNote = null
                },
            )
        }
    }
}

@Composable
fun SettingsScreen(
    settings: NavSettings, store: TaskStore, p: NavPalette, onBack: () -> Unit,
    profiles: ProfileStore, handover: HandoverStore, services: ServiceStore,
    workOrders: WorkOrderStore,
) {
    val lang = settings.language
    val context = LocalContext.current
    var showDigestWheel by remember { mutableStateOf(false) }
    fun syncSettings() {
        NavNotifications.reschedule(context, store, settings)
        NavWidgetUpdater.updateAll(context, store)
    }
    if (showDigestWheel) {
        NavTimeWheelDialog(
            initial = LocalTime.of(settings.digestHour, settings.digestMinute),
            p = p, lang = lang, onDismiss = { showDigestWheel = false },
            onSelected = {
                settings.setDigestTime(it.hour, it.minute)
                syncSettings()
                showDigestWheel = false
            },
        )
    }
    // Keep the approved Settings footer; put only the Back control outside
    // the recorded scene so its optical lens never samples itself.
    val settingsBackdrop = if (android.os.Build.VERSION.SDK_INT >= 33)
        com.kyant.backdrop.backdrops.rememberLayerBackdrop() else null
    Box(Modifier.fillMaxSize()) {
    Column(Modifier.fillMaxSize().then(
        if (settingsBackdrop != null) Modifier.layerBackdrop(settingsBackdrop) else Modifier
    ).statusBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            if (settingsBackdrop != null) Spacer(Modifier.size(44.dp))
            else NavGlassCircleButton(p, "back", onClick = onBack)
            Spacer(Modifier.width(12.dp))
            Text(localized("Настройки", "Settings", lang), color = p.text, fontSize = 23.sp)
        }
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
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
                    OutlinedButton(onClick = { showDigestWheel = true },
                        modifier = Modifier.fillMaxWidth()) {
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
            ProfileExchangePanel(profiles, store, handover, services, workOrders, p, lang)
            if (profiles.active.kind == NavProfileKind.VESSEL) {
                SelectiveExchangePanel(
                    profiles, store, handover, services, workOrders, settings, p, lang,
                )
            }
            NavPanel(p) {
                Text("NavTasks Glass 0.4.44 beta", color = p.text, fontWeight = FontWeight.Medium)
                Text(localized("Данные сохраняются на телефоне и доступны без интернета.",
                    "Data stays on this device and works offline.", lang), color = p.muted, fontSize = 12.sp)
            }
            Spacer(Modifier.height(12.dp))
            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
    }
    if (settingsBackdrop != null) {
        Box(Modifier.align(Alignment.TopStart).statusBarsPadding()
            .padding(start = 16.dp, top = 10.dp)) {
            NavLiquidGlassCircleButton(p, settingsBackdrop, "back", onBack)
        }
    }
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
