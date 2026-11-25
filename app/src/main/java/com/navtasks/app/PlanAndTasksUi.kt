package com.navtasks.app

import kotlinx.coroutines.launch

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
import androidx.compose.foundation.gestures.DraggableState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.draw.blur
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
import java.time.temporal.IsoFields
import java.util.Locale
import kotlinx.coroutines.delay
import kotlin.math.abs

/** Weekend styling in the month grid is not a task status. */
internal fun calendarIsWeekend(date: LocalDate): Boolean =
    date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY

/** One Apple system red for Saturdays/Sundays in Plan and Today's date strip.
 * Keep status dots independent: completed=green, partial=orange, missed=red.
 */
internal fun calendarWeekendInk(p: NavPalette): Color = p.systemRed

/** Preserve existing status-dot meaning independently of its layout position. */
internal fun calendarHasOutcomeDot(state: CalendarDayState, hasWork: Boolean): Boolean =
    // Planned work remains accessible in Day view, but has no mystery blue dot.
    state == CalendarDayState.COMPLETE || state == CalendarDayState.PARTIAL ||
        state == CalendarDayState.INCOMPLETE

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
        Row(
            Modifier.fillMaxWidth().padding(start = 19.dp, end = 16.dp, top = 6.dp, bottom = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val periodTitle = when (level) {
                0 -> "$year"
                1 -> YearMonth.of(year, month).month
                    .getDisplayName(TextStyle.FULL_STANDALONE, locale)
                    .replaceFirstChar { it.uppercase() } + " $year"
                else -> navDate(selectedDay, lang, true)
            }
            Text(periodTitle, color = p.text, fontSize = 23.sp,
                fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            if (level == 0) {
                // No duplicate year header below the switch: navigation sits
                // beside the current year, freeing space for all 12 months.
                NavGlassIconAction(
                    "chevron-left", p.text,
                    localized("Предыдущий год", "Previous year", lang),
                ) { year-- }
                NavGlassIconAction(
                    "chevron-right", p.text,
                    localized("Следующий год", "Next year", lang),
                ) { year++ }
            }
            TextButton(onClick = {
                year = today.year
                month = today.monthValue
                selectedDay = today
                level = 2
            }) {
                Text(localized("Сегодня", "Today", lang), color = NavTodayVisualTokens.accentInk(p.dark))
            }
        }
        NavCalendarModeControl(level, p, lang, onSelect = { level = it })
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
                0 -> BoxWithConstraints(
                    Modifier.fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(25.dp))
                        .background(NavTodayVisualTokens.cardSurface(p.dark)),
                ) {
                    // Four equal-height rows ALWAYS fit the remaining viewport.
                    // This deliberately replaces LazyVerticalGrid: a scrollable
                    // grid hid October–December on the user's HONOR.
                    val compact = maxHeight < 470.dp
                    Column(
                        Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(if (compact) 1.dp else 3.dp),
                    ) {
                        navCalendarYearRows.forEach { months ->
                            Row(
                                Modifier.fillMaxWidth().weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                months.forEach { m ->
                                    Column(
                                        Modifier.weight(1f).fillMaxHeight()
                                            .clip(RoundedCornerShape(10.dp))
                                            .glassClickable(pressScale = 1.01f) {
                                                month = m
                                                level = 1
                                            }
                                            .padding(horizontal = 2.dp, vertical = 1.dp),
                                    ) {
                                        Text(
                                            YearMonth.of(year, m).month
                                                .getDisplayName(TextStyle.FULL_STANDALONE, locale)
                                                .replaceFirstChar { it.uppercase() },
                                            color = if (year == today.year && m == today.monthValue)
                                                p.systemRed else p.text,
                                            fontSize = if (compact) 12.sp else 15.sp,
                                            lineHeight = if (compact) 14.sp else 18.sp,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                        )
                                        Spacer(Modifier.height(if (compact) 1.dp else 3.dp))
                                        Row(Modifier.fillMaxWidth().height(12.dp)) {
                                            (1..7).forEach { dow ->
                                                Text(
                                                    DayOfWeek.of(dow).getDisplayName(
                                                        TextStyle.NARROW, locale),
                                                    color = p.muted.copy(alpha = .82f),
                                                    fontSize = 8.sp, lineHeight = 10.sp,
                                                    textAlign = TextAlign.Center,
                                                    maxLines = 1,
                                                    modifier = Modifier.weight(1f),
                                                )
                                            }
                                        }
                                        // Use six weighted week slots even in a five-week
                                        // month: identical alignment across all 12 tiles.
                                        monthGridDates(year, m).chunked(7).forEach { week ->
                                            Row(Modifier.fillMaxWidth().weight(1f)) {
                                                week.forEach { date ->
                                                    val inMonth = date.monthValue == m && date.year == year
                                                    val state = store.dayCounts(date).state(date, today)
                                                    val ink = when (state) {
                                                        CalendarDayState.COMPLETE -> p.mint
                                                        CalendarDayState.PARTIAL -> p.systemOrange
                                                        CalendarDayState.INCOMPLETE -> p.systemRed
                                                        else -> p.text
                                                    }
                                                    Box(
                                                        Modifier.weight(1f).fillMaxHeight(),
                                                        contentAlignment = Alignment.Center,
                                                    ) {
                                                        if (inMonth) {
                                                            if (date == today) {
                                                                Box(
                                                                    Modifier.size(if (compact) 13.dp else 18.dp)
                                                                        .background(p.systemRed, CircleShape),
                                                                    contentAlignment = Alignment.Center,
                                                                ) {
                                                                    Text(
                                                                        "${date.dayOfMonth}",
                                                                        fontSize = if (compact) 8.sp else 10.sp,
                                                                        lineHeight = if (compact) 9.sp else 11.sp,
                                                                        color = Color.White,
                                                                        maxLines = 1,
                                                                    )
                                                                }
                                                            } else {
                                                                Text(
                                                                    "${date.dayOfMonth}",
                                                                    color = ink, fontSize = if (compact) 9.sp else 10.sp,
                                                                    lineHeight = if (compact) 10.sp else 12.sp,
                                                                    maxLines = 1,
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> Column {
                    Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(25.dp))
                        .background(NavTodayVisualTokens.cardSurface(p.dark))) {
                    val weekendInk = calendarWeekendInk(p)
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 0.dp),
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        // Same reserved space as the ISO week numbers below.
                        Spacer(Modifier.width(calendarMonthWeekNumberGutter))
                        (1..7).forEach { dow ->
                            Text(DayOfWeek.of(dow).getDisplayName(TextStyle.SHORT, locale),
                                color = if (dow >= 6) weekendInk else p.muted,
                                fontSize = 11.sp, textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f).padding(vertical = 7.dp))
                        }
                    }
                    monthGridDates(year, month).chunked(7).forEach { week ->
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 0.dp),
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            // ISO week of the Monday displayed at the start of this row.
                            // No click handler: week numbers are an informational label.
                            Text(
                                calendarIsoWeekNumber(week.first()).toString(),
                                modifier = Modifier.width(calendarMonthWeekNumberGutter).height(56.dp)
                                    .wrapContentHeight(Alignment.CenterVertically),
                                color = p.muted.copy(alpha = 0.72f),
                                fontSize = 11.sp,
                                maxLines = 1,
                                textAlign = TextAlign.Center,
                            )
                            week.forEach { date ->
                                val isToday = date == today
                                val inMonth = date.year == year && date.monthValue == month
                                val status = store.dayCounts(date).state(date, today)
                                val hasWork = workAgendaExistsOnDay(
                                    date, workOrders.entries, services.entries)
                                val statusColor = when (status) {
                                    CalendarDayState.COMPLETE -> p.mint
                                    CalendarDayState.PARTIAL -> p.systemOrange
                                    CalendarDayState.INCOMPLETE -> p.systemRed
                                    CalendarDayState.SCHEDULED -> p.border
                                    CalendarDayState.EMPTY -> p.border
                                }
                                // Clean calendar typography: the date is the only
                                // highlighted surface; task outcomes remain colored dots.
                                Box(
                                    Modifier.weight(1f).height(56.dp)
                                        .glassClickable {
                                            selectedDay = date
                                            year = date.year
                                            month = date.monthValue
                                            level = 2
                                        },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Box(
                                        Modifier.size(45.dp).background(
                                            if (isToday) p.systemRed else Color.Transparent,
                                            CircleShape,
                                        ),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            "${date.dayOfMonth}",
                                            color = when {
                                                isToday -> Color.White
                                                !inMonth -> p.muted.copy(alpha = 0.32f)
                                                calendarIsWeekend(date) -> weekendInk
                                                else -> p.text
                                            },
                                            fontWeight = if (isToday) FontWeight.SemiBold
                                                else FontWeight.Medium,
                                            fontSize = 20.sp,
                                        )
                                        if (calendarHasOutcomeDot(status, hasWork)) {
                                            Box(
                                                Modifier.align(Alignment.BottomCenter)
                                                    .padding(bottom = 4.dp).size(5.dp)
                                                    .background(
                                                        if (isToday) Color.White else
                                                            statusColor.copy(alpha = if (inMonth) 1f else 0.35f),
                                                        CircleShape,
                                                    ),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(3.dp))
                    }
                    Row(Modifier.fillMaxWidth().padding(horizontal = 7.dp, vertical = 11.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        listOf(
                            p.mint to localized("Все выполнено", "All done", lang),
                            p.systemOrange to localized("Часть", "Partial", lang),
                            p.systemRed to localized("Пропущено", "Missed", lang),
                        ).forEach { (color, title) ->
                            Row(verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                Box(Modifier.size(6.dp).background(color, CircleShape))
                                Text(title, color = p.muted, fontSize = 10.sp)
                            }
                        }
                    }
                    }
                }
                else -> Column {
                    // Keep the week visible in Day mode, like the reference
                    // calendar, without changing task recurrence or completion.
                    val visibleWeek = navCalendarWeekDates(selectedDay)
                    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 5.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(NavTodayVisualTokens.cardSurface(p.dark))
                        .padding(vertical = 6.dp)) {
                        visibleWeek.forEach { date ->
                            Column(
                                Modifier.weight(1f).glassClickable {
                                    selectedDay = date
                                    year = date.year
                                    month = date.monthValue
                                },
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    date.dayOfWeek.getDisplayName(TextStyle.SHORT, locale),
                                    color = if (calendarIsWeekend(date)) p.muted
                                        else p.text,
                                    fontSize = 11.sp, maxLines = 1,
                                )
                                Spacer(Modifier.height(7.dp))
                                Box(
                                    Modifier.size(45.dp).background(
                                        if (selectedDay == date) p.systemRed
                                        else Color.Transparent, CircleShape,
                                    ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        "${date.dayOfMonth}", fontSize = 19.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = when {
                                            selectedDay == date -> Color.White
                                            calendarIsWeekend(date) -> p.muted
                                            else -> p.text
                                        },
                                    )
                                    val status = store.dayCounts(date).state(date, today)
                                    val mark = when (status) {
                                        CalendarDayState.COMPLETE -> p.mint
                                        CalendarDayState.PARTIAL -> p.systemOrange
                                        CalendarDayState.INCOMPLETE -> p.systemRed
                                        else -> null
                                    }
                                    if (mark != null) {
                                        Box(Modifier.align(Alignment.BottomCenter)
                                            .padding(bottom = 3.dp).size(4.dp)
                                            .background(if (selectedDay == date) Color.White else mark,
                                                CircleShape))
                                    }
                                }
                            }
                        }
                    }
                    val rows = store.forDate(selectedDay)
                    val plannedWorks = workAgendaExistsOnDay(
                        selectedDay, workOrders.entries, services.entries)
                    LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (rows.isEmpty() && !plannedWorks) item {
                            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                                .background(NavTodayVisualTokens.cardSurface(p.dark)).padding(14.dp)) {
                                Text(localized("Задач на этот день нет", "No tasks for this day", lang), color = p.muted)
                            }
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
        Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
            .background(NavTodayVisualTokens.cardSurface(p.dark))
            .padding(horizontal = 14.dp, vertical = 8.dp),
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
                "more", p.text, localized("Действия с задачей", "Task actions", lang),
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
            Text(localized("Удалить", "Remove", lang), color = p.systemRed)
        } },
        dismissButton = { TextButton(onClick = { deleteDialog = false }) {
            Text(localized("Отмена", "Cancel", lang))
        } },
    )
}

/** ISO-8601 calendar week: Monday start, with correct year-boundary week numbering. */
internal fun calendarIsoWeekNumber(date: LocalDate): Int =
    date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)

/** Fixed leading gutter in Month mode; header and rows stay perfectly aligned. */
internal val calendarMonthWeekNumberGutter = 26.dp

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
    smoothReturnTick: Int = 0,
) {
    val listState = rememberLazyListState()
    LaunchedEffect(smoothReturnTick) {
        if (smoothReturnTick > 0) listState.animateScrollToItem(0)
    }
    val labels = listOf(
        localized("День", "Daily", lang), localized("Неделя", "Weekly", lang),
        localized("Месяц", "Monthly", lang), localized("Разовые", "Extra", lang),
    )
    val filtered = store.templates.filter {
        TaskSchedule.effectiveFrequency(it, LocalDate.now()) == filter
    }.sortedBy { if (it.enabled) 0 else 1 }
    Column(Modifier.fillMaxSize()) {
        NavCalendarSegmentedControl(
            labels = labels,
            activeIndex = Frequency.entries.indexOf(filter).coerceAtLeast(0),
            p = p,
            modifier = Modifier.padding(horizontal = NavEventLayout.screenInset, vertical = 3.dp),
        ) { index -> onFilter(Frequency.entries[index]) }
        // Same inline section treatment as Today: label, count, trailing rule.
        Row(
            Modifier.fillMaxWidth()
                .padding(horizontal = NavEventLayout.screenInset)
                .padding(top = 8.dp, bottom = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(localized("Список задач", "Task list", lang), color = p.text, fontSize = 13.sp)
            Spacer(Modifier.width(7.dp))
            Text(filtered.size.toString(), color = p.muted, fontSize = 11.sp)
            Spacer(Modifier.width(12.dp))
            Box(Modifier.weight(1f).height(1.dp).background(p.border.copy(alpha = 0.58f)))
        }
        // Keep the same lazy list, stable item keys and editing navigation.
        LazyColumn(
            Modifier.weight(1f), state = listState,
            contentPadding = PaddingValues(start = NavEventLayout.screenInset, end = NavEventLayout.screenInset,
                top = 5.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(NavEventLayout.sectionGap),
        ) {
            if (filtered.isEmpty()) item {
                Box(Modifier.fillMaxWidth().personalNoteCard(p, NavEventLayout.cardRadius)
                    .padding(NavEventLayout.cardInset)) {
                    Text(localized("Задач пока нет", "No tasks yet", lang), color = p.muted)
                }
            }
            itemsIndexed(filtered, key = { _, item -> item.id }) { _, task ->
                Row(Modifier.fillMaxWidth().personalNoteCard(p, NavEventLayout.cardRadius)
                    .glassClickable { onEdit(task) }
                    .heightIn(min = 60.dp)
                    .padding(horizontal = NavEventLayout.cardInset, vertical = NavEventLayout.innerGap),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(NavEventLayout.innerGap)) {
                    // Templates are not executable, so no fake empty checkbox or checkbox icon here.
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(task.title, color = if (task.enabled) p.text else p.muted, fontSize = 15.sp,
                            textDecoration = if (task.enabled) androidx.compose.ui.text.style.TextDecoration.None
                                else androidx.compose.ui.text.style.TextDecoration.LineThrough)
                        Text(
                            when {
                                TaskSchedule.effectiveFrequency(task, LocalDate.now()) == Frequency.EXTRA -> navDate(task.startDate, lang)
                                task.scheduleMode == ScheduleMode.AFTER_COMPLETION -> localized("От выполнения", "After completion", lang)
                                TaskSchedule.effectiveFrequency(task, LocalDate.now()) == Frequency.WEEKLY -> DayOfWeek.of(effectiveCalendarWeekday(task, LocalDate.now())).getDisplayName(
                                    TextStyle.FULL, if (lang == UiLanguage.RU) Locale("ru") else Locale.ENGLISH)
                                TaskSchedule.effectiveFrequency(task, LocalDate.now()) == Frequency.MONTHLY -> "${effectiveCalendarMonthDay(task, LocalDate.now())}"
                                else -> localized("Каждый день", "Every day", lang)
                            }, color = p.muted, fontSize = 12.sp,
                        )
                    }
                    if (task.checklist.isNotEmpty()) Text("${task.checklist.size}",
                        color = if (task.enabled) NavTodayVisualTokens.accentInk(p.dark) else p.muted, fontSize = 12.sp)
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
                Text(label, color = if (active) p.selectionContent else p.text, fontSize = 12.sp)
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
    sheetMode: Boolean = false,
    sheetDragState: DraggableState? = null,
    onSheetDragStopped: (Float) -> Unit = {},
) {
    val p = palette
    val state = remember(initial?.id) { TaskEditorState(initial) }
    val editorBackdrop = if (!sheetMode && android.os.Build.VERSION.SDK_INT >= 33)
        com.kyant.backdrop.backdrops.rememberLayerBackdrop() else null
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().then(
            if (editorBackdrop != null) Modifier.layerBackdrop(editorBackdrop) else Modifier
        ).then(if (sheetMode) Modifier.padding(top = 13.dp) else Modifier.statusBarsPadding())
            .navigationBarsPadding()) {
            state.EditorHeader(p, lang, store, sheetMode, sheetDragState, onSheetDragStopped,
                editorBackdrop != null, onBack, onSave)
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = NavEventLayout.screenInset),
                verticalArrangement = Arrangement.spacedBy(NavEventLayout.sectionGap)) {
                NavEventTitle(state.title, { state.title = it }, p, lang)
                state.EditorSchedule(p, lang)
                state.EditorReminders(p, lang)
                state.EditorChecklist(p, lang)
                state.EditorOther(p, lang)
                Spacer(Modifier.height(NavEventLayout.innerGap))
            }
        }
        if (!sheetMode && editorBackdrop != null) {
            Box(Modifier.align(Alignment.TopStart).statusBarsPadding()
                .padding(start = 14.dp, top = 10.dp)) {
                NavLiquidGlassCircleButton(p, editorBackdrop, "back", onBack)
            }
        }
    }
    state.EditorDeletionDialogs(p, lang, store, onBack)
    state.EditorScopeDialogs(p, lang, store, onBack)
    state.EditorWheels(p, lang)
    state.EditorChoiceDialogs(p, lang)
    state.EditorChecklistNote(p, lang)
}

private class TaskEditorState(val initial: TaskTemplate?) {
    var title: String by mutableStateOf(initial?.title.orEmpty())
    var frequency: Frequency by mutableStateOf(initial?.let {
        TaskSchedule.effectiveFrequency(it, LocalDate.now())
    } ?: Frequency.EXTRA)
    var mode: ScheduleMode by mutableStateOf(initial?.scheduleMode ?: ScheduleMode.CALENDAR)
    var date: LocalDate by mutableStateOf(initial?.startDate ?: LocalDate.now())
    var weekDay: Int by mutableIntStateOf(initial?.let { effectiveCalendarWeekday(it, LocalDate.now()) }
        ?: LocalDate.now().dayOfWeek.value)
    var monthDay: String by mutableStateOf((initial?.let { effectiveCalendarMonthDay(it, LocalDate.now()) }
        ?: LocalDate.now().dayOfMonth).toString())
    var fixedMonthDay: String by mutableStateOf(run {
        val selected = initial?.let { effectiveCalendarMonthDay(it, LocalDate.now()) }
            ?: LocalDate.now().dayOfMonth
        (if (selected == 31) date.dayOfMonth.coerceAtMost(30) else selected).toString()
    })
    var interval: String by mutableStateOf((initial?.intervalCount ?: 1).toString())
    var missedPolicy: MissedPolicy by mutableStateOf(initial?.missedPolicy ?: MissedPolicy.HISTORY)
    var time: LocalTime? by mutableStateOf(initial?.deadlineTime)
    var windowStart: LocalTime? by mutableStateOf(initial?.windowStartTime)
    var pickingStart: Boolean by mutableStateOf(false)
    var showRepeat: Boolean by mutableStateOf(false)
    var customRepeat: Boolean by mutableStateOf(initial?.let {
        val effective = TaskSchedule.effectiveFrequency(it, LocalDate.now())
        it.scheduleMode == ScheduleMode.AFTER_COMPLETION ||
            (effective == Frequency.WEEKLY && effectiveCalendarWeekday(it, LocalDate.now()) != it.startDate.dayOfWeek.value) ||
            (effective == Frequency.MONTHLY && effectiveCalendarMonthDay(it, LocalDate.now()) != it.startDate.dayOfMonth)
    } ?: false)
    var otherSettings: Boolean by mutableStateOf(false)
    var showReminder: Boolean by mutableStateOf(false)
    var showCustomReminder: Boolean by mutableStateOf(false)
    var customMinutes: String by mutableStateOf("60")
    var showDateWheel: Boolean by mutableStateOf(false)
    var showTimeWheel: Boolean by mutableStateOf(false)
    var reminders: List<Int> by mutableStateOf(initial?.let { SmartReminderTiming.offsets(it) } ?: listOf(60))
    val notified: Boolean get() = reminders.isNotEmpty()
    val reminder: Int get() = reminders.firstOrNull() ?: initial?.remindMinutesBefore ?: 60
    fun addReminder(minutes: Int) {
        if (minutes in 0..10080 && minutes !in reminders && reminders.size < 9)
            reminders = reminders + minutes
    }
    var checklist: List<CheckItem> by mutableStateOf(initial?.checklist ?: emptyList())
    var editingChecklistNote: String? by mutableStateOf(null)
    var enabled: Boolean by mutableStateOf(initial?.enabled ?: true)
    var showTaskDeletionOptions: Boolean by mutableStateOf(false)
    var confirmPermanentTaskDeletion: Boolean by mutableStateOf(false)
    var taskDeletionError: String? by mutableStateOf(null)
    var pendingCalendarDayEdit: Pair<TaskTemplate, CalendarDayEditPreview>? by mutableStateOf(null)
    var pendingFrequencyEdit: Pair<TaskTemplate, FrequencyChangePreview>? by mutableStateOf(null)
    var unsupportedFrequencyEdit: Boolean by mutableStateOf(false)
    val validWindow: Boolean get() = windowStart == null || time?.let { end -> windowStart?.isBefore(end) } == true
    val valid: Boolean get() = title.isNotBlank() && (monthDay.toIntOrNull() ?: 0) in 1..31 &&
        (interval.toIntOrNull() ?: 0) > 0 && validWindow
}

@Composable
private fun TaskEditorState.EditorHeader(p: NavPalette, lang: UiLanguage, store: TaskStore, sheetMode: Boolean, sheetDragState: DraggableState?, onSheetDragStopped: (Float) -> Unit, hasEditorBackdrop: Boolean, onBack: () -> Unit, onSave: (TaskTemplate) -> Unit) {
    val initial = this.initial
        Row(
    Modifier.fillMaxWidth()
        .then(if (sheetMode && sheetDragState != null) Modifier.draggable(
            state = sheetDragState,
            orientation = Orientation.Vertical,
            onDragStopped = { velocity -> onSheetDragStopped(velocity) },
        ) else Modifier)
        .padding(horizontal = 14.dp, vertical = 10.dp),
    verticalAlignment = Alignment.CenterVertically,
        ) {
    if (!sheetMode) {
        if (hasEditorBackdrop) Spacer(Modifier.size(44.dp))
        else NavGlassCircleButton(p, "back", onClick = onBack)
        Spacer(Modifier.width(12.dp))
    }
    if (sheetMode) {
        NavEventHeaderAction("x", localized("Отмена", "Cancel", lang), p, true, onBack)
    }
    Text(localized(if (initial == null) "Новая задача" else "Редактировать задачу",
        if (initial == null) "New task" else "Edit task", lang),
        modifier = Modifier.weight(1f), color = p.text, fontSize = 20.sp, textAlign = TextAlign.Center)
    NavEventHeaderAction(
        "check", localized("Сохранить", "Save", lang), p, valid,
        onClick = {
        val edited = (initial ?: TaskTemplate(title = title.trim(), frequency = frequency)).copy(
            title = title.trim(), frequency = frequency,
            scheduleMode = if (frequency == Frequency.EXTRA) ScheduleMode.CALENDAR else mode,
            startDate = date, dayOfWeek = weekDay, dayOfMonth = monthDay.toInt(),
            intervalCount = interval.toInt(), missedPolicy = missedPolicy,
            completionMode = CompletionMode.AUTOMATIC,
            deadlineTime = time, notificationsEnabled = notified,
            remindMinutesBefore = reminder,
            windowStartTime = if (time != null) windowStart else null,
            additionalReminderMinutes = reminders.drop(1),
            offsetReminderKeys = initial == null || initial.offsetReminderKeys ||
                reminders != SmartReminderTiming.offsets(initial),
            legacyReminderOffset = initial?.let {
                if (it.offsetReminderKeys) it.legacyReminderOffset
                else if (it.notificationsEnabled && reminders != SmartReminderTiming.offsets(it))
                    it.remindMinutesBefore else null
            },
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
        },
    )
        }
}

@Composable
private fun TaskEditorState.EditorSchedule(p: NavPalette, lang: UiLanguage) {
    NavEventSchedulePanel(p) {
        NavEventSwitchRow(localized("Весь день", "All day", lang), time == null, p) { allDay ->
            if (allDay) { time = null; windowStart = null }
            else time = LocalTime.of(18, 0)
        }
        NavEventDivider(p)
        NavEventValueRow(
            localized("Дата начала", "Start date", lang),
            navDate(date, lang, true), p,
        ) { showDateWheel = true }
        if (time != null) {
            NavEventDivider(p)
            NavEventSwitchRow(localized("Временной интервал", "Time window", lang), windowStart != null, p) { useWindow ->
                if (useWindow) {
                    if (time == LocalTime.MIDNIGHT) time = LocalTime.of(1, 0)
                    val end = time ?: LocalTime.of(18, 0)
                    windowStart = end.minusHours(1).takeIf { it.isBefore(end) } ?: LocalTime.MIDNIGHT
                } else windowStart = null
            }
            if (windowStart != null) {
                NavEventDivider(p)
                NavEventValueRow(localized("Начало", "Start", lang), windowStart.toString(), p) {
                    pickingStart = true; showTimeWheel = true
                }
            }
            NavEventDivider(p)
            NavEventValueRow(
                if (windowStart == null) localized("Выполнить к", "Due by", lang)
                else localized("Конец", "End", lang),
                time.toString(), p,
            ) { pickingStart = false; showTimeWheel = true }
            if (!validWindow) Text(localized("Начало должно быть раньше окончания в тот же день.",
                "Start must precede the end on the same day.", lang), color = p.dangerText, fontSize = 12.sp)
        }
        NavEventDivider(p)
        NavEventValueRow(localized("Повторять", "Repeat", lang),
            navEventRepeatLabel(frequency, mode, customRepeat, lang), p, chevron = true) {
            showRepeat = true
        }
        if (customRepeat && frequency != Frequency.EXTRA) {
            NavEventDivider(p)
            Spacer(Modifier.height(NavEventLayout.groupGap))
            NavEventOptionGrid(navEventRepeatChoices(lang).filter { it.first != "custom" },
                frequency.name, p) { key ->
                val chosen = Frequency.valueOf(key)
                if (frequency != chosen) {
                    weekDay = date.dayOfWeek.value
                    monthDay = date.dayOfMonth.toString()
                }
                frequency = chosen
                if (chosen == Frequency.EXTRA) { mode = ScheduleMode.CALENDAR; customRepeat = false }
            }
            Spacer(Modifier.height(NavEventLayout.groupGap))
            NavEventOptionGrid(navTaskRepeatChoices(lang), mode.name, p) { mode = ScheduleMode.valueOf(it) }
            if (frequency == Frequency.WEEKLY && mode == ScheduleMode.CALENDAR) {
                Spacer(Modifier.height(NavEventLayout.groupGap))
                FormLabel("День недели", "Weekday", lang, p)
                Spacer(Modifier.height(NavEventLayout.innerGap))
                val loc = if (lang == UiLanguage.RU) Locale("ru") else Locale.ENGLISH
                NavEventOptionGrid((1..7).map { n -> "$n" to DayOfWeek.of(n).getDisplayName(TextStyle.SHORT, loc) },
                    "$weekDay", p, columns = 4) { weekDay = it.toInt() }
            }
            if (frequency == Frequency.MONTHLY && mode == ScheduleMode.CALENDAR) {
                Spacer(Modifier.height(NavEventLayout.groupGap))
                FormLabel("Когда выполнять каждый месяц", "Monthly execution date", lang, p)
                Spacer(Modifier.height(NavEventLayout.innerGap))
                NavEventOptionGrid(listOf("fixed" to localized("В конкретное число", "Specific day", lang),
                    "end" to localized("В последний день", "Last day", lang)),
                    if (monthDay == "31") "end" else "fixed", p) { choice ->
                    if (choice == "end") {
                        if ((monthDay.toIntOrNull() ?: 0) in 1..30) fixedMonthDay = monthDay
                        monthDay = "31"
                    } else monthDay = fixedMonthDay.takeIf { (it.toIntOrNull() ?: 0) in 1..30 } ?: "1"
                }
                Spacer(Modifier.height(NavEventLayout.innerGap))
                if (monthDay != "31") NavGlassInput(monthDay, { monthDay = it.take(2); fixedMonthDay = monthDay }, p,
                    modifier = Modifier.fillMaxWidth(), neutralStyle = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                val upcoming = monthlySchedulePreview(date, monthDay.toIntOrNull() ?: 0)
                if (upcoming.isNotEmpty()) Text(upcoming.joinToString("  ·  ") { navDate(it, lang, true) },
                    color = p.muted, fontSize = 12.sp)
            }
            if (frequency != Frequency.EXTRA && mode == ScheduleMode.AFTER_COMPLETION) {
                Spacer(Modifier.height(NavEventLayout.groupGap))
                val unit = when (frequency) {
                    Frequency.DAILY -> localized("дней", "days", lang)
                    Frequency.WEEKLY -> localized("недель", "weeks", lang)
                    else -> localized("месяцев", "months", lang)
                }
                Text(localized("Интервал от выполнения", "Interval after completion", lang) + " ($unit)", color = p.muted, fontSize = 12.sp)
                NavGlassInput(interval, { interval = it }, p, modifier = Modifier.fillMaxWidth(),
                    neutralStyle = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }
            Spacer(Modifier.height(NavEventLayout.innerGap))
        }
    }
}

@Composable
private fun TaskEditorState.EditorReminders(p: NavPalette, lang: UiLanguage) {
    NavEventSchedulePanel(p) {
        reminders.forEach { minutes ->
            Row(Modifier.fillMaxWidth().heightIn(min = NavEventRowHeight), verticalAlignment = Alignment.CenterVertically) {
                Text(navEventReminderLabel(minutes, lang), color = p.text, fontSize = 15.sp, modifier = Modifier.weight(1f))
                NavEventHeaderAction("x", localized("Удалить напоминание", "Remove reminder", lang), p, true) {
                    reminders = reminders.filterNot { it == minutes }
                }
            }
            NavEventDivider(p)
        }
        if (reminders.size < 9) NavEventActionRow(
            localized("Добавить напоминание", "Add reminder", lang), p) { showReminder = true }
        if (reminders.isNotEmpty() && (time == null || windowStart != null)) {
            Text(if (time == null)
                localized("От основного времени в настройках", "Relative to the default time in Settings", lang)
                else localized("От времени начала", "Relative to the start time", lang),
                color = p.muted, fontSize = 11.sp,
                modifier = Modifier.padding(bottom = NavEventLayout.innerGap))
        }
    }
}

@Composable
private fun TaskEditorState.EditorChecklist(p: NavPalette, lang: UiLanguage) {
    NavEventSchedulePanel(p) {
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
            neutralStyle = true,
            compactEventLayout = true,
        )
    }
}

@Composable
private fun TaskEditorState.EditorOther(p: NavPalette, lang: UiLanguage) {
    val initial = this.initial
    NavEventSchedulePanel(p) {
        NavEventValueRow(localized("Другие настройки", "Other settings", lang),
            "", p, chevron = true, expanded = otherSettings) { otherSettings = !otherSettings }
        if (otherSettings) {
            NavEventDivider(p)
            Spacer(Modifier.height(NavEventLayout.groupGap))
            Text(localized("Если пропущена", "When overdue", lang), color = p.text, fontSize = 15.sp)
            Spacer(Modifier.height(NavEventLayout.innerGap))
            NavEventOptionGrid(listOf(
            MissedPolicy.HISTORY.name to localized("В пропущенные", "Keep in Missed", lang),
            MissedPolicy.CARRY.name to localized("Автоперенос", "Auto move", lang),
            MissedPolicy.ASK.name to localized("Спросить", "Ask me", lang),
            MissedPolicy.ARCHIVE.name to localized("В историю", "Archive", lang),
            ), missedPolicy.name, p) { missedPolicy = MissedPolicy.valueOf(it) }
            Spacer(Modifier.height(NavEventLayout.innerGap))
        }
    }
    NavEventSchedulePanel(p) {
        NavEventSwitchRow(localized("Задача активна", "Task enabled", lang), enabled, p) { enabled = it }
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
                color = p.systemRed, fontSize = 14.sp)
        }
        taskDeletionError?.let { error ->
            Text(error, color = p.dangerText, fontSize = 12.sp)
        }
    }
}

@Composable
private fun TaskEditorState.EditorDeletionDialogs(p: NavPalette, lang: UiLanguage, store: TaskStore, onBack: () -> Unit) {
    val initial = this.initial
    if (showTaskDeletionOptions && initial != null) {
        NavGlassAlertDialog(
    p = p, neutralActions = true, neutralSurface = true,
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
                        color = NavTodayVisualTokens.accentInk(p.dark),
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
                    color = p.systemRed)
            }
        }
    },
    confirmButton = {},
    dismissButton = {
        TextButton(onClick = { showTaskDeletionOptions = false }) {
            Text(localized("Отмена", "Cancel", lang), color = p.text)
        }
    },
        )
    }
    if (confirmPermanentTaskDeletion && initial != null) {
        NavGlassAlertDialog(
    p = p, neutralActions = true, neutralSurface = true,
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
                color = p.systemRed)
        }
    },
    dismissButton = {
        TextButton(onClick = { confirmPermanentTaskDeletion = false }) {
            Text(localized("Отмена", "Cancel", lang), color = p.text)
        }
    },
        )
    }
}

@Composable
private fun TaskEditorState.EditorScopeDialogs(p: NavPalette, lang: UiLanguage, store: TaskStore, onBack: () -> Unit) {
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
    p = p, lang = lang, title = edited.title, neutralStyle = true,
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
    p = p, neutralActions = true, neutralSurface = true,
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
            Text(localized("Понятно", "OK", lang), color = NavTodayVisualTokens.accentInk(p.dark))
        }
    },
        )
    }
    pendingCalendarDayEdit?.let { (edited, preview) ->
        MoveScopeDialog(
    p = p, lang = lang, title = edited.title, neutralStyle = true,
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
}

@Composable
private fun TaskEditorState.EditorWheels(p: NavPalette, lang: UiLanguage) {
    val initial = this.initial
    if (showDateWheel) {
        NavDateWheelDialog(
    initial = date, p = p, lang = lang,
    onDismiss = { showDateWheel = false },
    onSelected = {
        date = it
        if (initial == null && !customRepeat) {
            weekDay = it.dayOfWeek.value
            monthDay = it.dayOfMonth.toString()
        }
        showDateWheel = false
    },
        )
    }
    if (showTimeWheel) {
        NavTimeWheelDialog(
    initial = (if (pickingStart) windowStart else time) ?: LocalTime.of(18, 0), p = p, lang = lang,
    onDismiss = { showTimeWheel = false },
    onSelected = { if (pickingStart) windowStart = it else time = it; showTimeWheel = false },
        )
    }
}

@Composable
private fun TaskEditorState.EditorChoiceDialogs(p: NavPalette, lang: UiLanguage) {
    if (showRepeat) NavEventChoiceDialog(
        localized("Повторять", "Repeat", lang), navEventRepeatChoices(lang),
        if (customRepeat) "custom" else frequency.name, p, lang, confirmRequired = false,
        onDismiss = { showRepeat = false },
        onSelect = { key ->
    showRepeat = false
    if (key == "custom") {
        if (frequency == Frequency.EXTRA) frequency = Frequency.DAILY
        customRepeat = true
    } else {
        val chosen = Frequency.valueOf(key)
        if (chosen != frequency || mode != ScheduleMode.CALENDAR) {
            weekDay = date.dayOfWeek.value
            monthDay = date.dayOfMonth.toString()
        }
        frequency = chosen
        mode = ScheduleMode.CALENDAR
        customRepeat = false
    }
        },
    )
    if (showReminder) NavEventChoiceDialog(
        localized("Добавить напоминание", "Add reminder", lang),
        navEventReminderChoices(lang), null, p, lang,
        onDismiss = { showReminder = false },
        onSelect = { key ->
    showReminder = false
    when (key) {
        "none" -> reminders = emptyList()
        "custom" -> showCustomReminder = true
        else -> key.toIntOrNull()?.let { addReminder(it) }
    }
        },
    )
    if (showCustomReminder) NavGlassAlertDialog(
        p = p, neutralActions = true, neutralSurface = true,
        onDismissRequest = { showCustomReminder = false },
        title = { Text(localized("За сколько минут", "Minutes before", lang)) },
        text = {
    Column {
        NavGlassInput(customMinutes, { customMinutes = it.filter(Char::isDigit).take(5) }, p,
            neutralStyle = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
        Text(localized("От 0 до 10080 минут (7 дней)", "0–10080 minutes (7 days)", lang), color = p.muted, fontSize = 12.sp)
    }
        },
        confirmButton = { TextButton(enabled = (customMinutes.toIntOrNull() ?: -1) in 0..10080, onClick = {
    customMinutes.toIntOrNull()?.let { addReminder(it) }; showCustomReminder = false
        }) { Text(localized("OK", "OK", lang), color = NavTodayVisualTokens.accentInk(p.dark)) } },
        dismissButton = { TextButton(onClick = { showCustomReminder = false }) { Text(localized("Отмена", "Cancel", lang), color = p.text) } },
    )
}

@Composable
private fun TaskEditorState.EditorChecklistNote(p: NavPalette, lang: UiLanguage) {
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
    var showReminderWheel by remember { mutableStateOf(false) }
    val actualVersion = remember(context) {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty()
        }.getOrDefault("")
    }
    fun syncSettings() {
        NavNotifications.reschedule(context, store, settings)
        NavWidgetUpdater.updateAll(context, store)
    }
    if (showReminderWheel) {
        NavTimeWheelDialog(
            initial = LocalTime.of(settings.reminderHour, settings.reminderMinute),
            p = p, lang = lang, onDismiss = { showReminderWheel = false },
            onSelected = {
                settings.setReminderTime(it.hour, it.minute)
                syncSettings()
                showReminderWheel = false
            },
        )
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
                if (settings.notificationsEnabled) {
                    OutlinedButton(onClick = { showReminderWheel = true },
                        modifier = Modifier.fillMaxWidth()) {
                        Text(localized("Основное время напоминаний", "Default reminder time", lang) +
                            "  %02d:%02d".format(settings.reminderHour, settings.reminderMinute))
                    }
                    Text(localized(
                        "Для задач «До конца дня» уведомление отсчитывается от этого времени. " +
                            "Задачи с указанным часом используют своё время; срок «До конца дня» остаётся до 23:59.",
                        "Tasks due by end of day use this reminder clock. " +
                            "Tasks with a specific hour use their own time; end-of-day deadlines remain unchanged.",
                        lang), color = p.muted, fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(localized("Объединять одновременные напоминания",
                        "Group simultaneous reminders", lang),
                        color = p.text, modifier = Modifier.weight(1f), fontSize = 14.sp)
                    Switch(settings.groupSimultaneousReminders,
                        onCheckedChange = {
                            settings.updateReminderGrouping(it)
                            syncSettings()
                        },
                        enabled = settings.notificationsEnabled, colors = navSwitchColors(p))
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
                        Text(localized("Время сводки пропусков", "Missed-task digest time", lang) +
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
            // Reference equipment/manual exchange now belongs to the independent
            // Reference tab. Settings intentionally keeps only Full Profile Transfer
            // and Selective Exchange; never merge Reference data into those flows.
            // This panel invokes the shared optional update flow. No app-data writes.
            NavPanel(p) {
                Text(localized("Обновления", "Updates", lang),
                    color = p.text, fontWeight = FontWeight.Medium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(localized("Предлагать обновления", "Offer updates automatically", lang),
                        color = p.text, modifier = Modifier.weight(1f), fontSize = 14.sp)
                    Switch(settings.automaticUpdateOffers,
                        onCheckedChange = { settings.updateAutomaticUpdateOffers(it) },
                        colors = navSwitchColors(p))
                }
                Text(localized("Если выключено, при запуске не проверяем обновления. Ручная проверка доступна всегда.",
                    "When off, startup checks are disabled. Manual checking remains available.", lang),
                    color = p.muted, fontSize = 12.sp)
                Text(localized("Установлена версия", "Installed version", lang) +
                    ": " + actualVersion, color = p.muted, fontSize = 13.sp)
                val lastSuccess = NavUpdateChecker.lastSuccess(context)
                Text(
                    localized("Последняя проверка", "Last check", lang) + ": " +
                        if (lastSuccess == 0L) localized("ещё не было", "never", lang)
                        else java.text.DateFormat.getDateTimeInstance(
                            java.text.DateFormat.SHORT, java.text.DateFormat.SHORT
                        ).format(java.util.Date(lastSuccess)),
                    color = p.muted, fontSize = 12.sp
                )
                val update = NavUpdateChecker.candidate
                if (update != null) {
                    Text(localized("Доступна версия", "Available version", lang) +
                        " " + update.versionName, color = p.blue)
                    Text(if (lang == UiLanguage.RU) update.notesRu else update.notesEn,
                        color = p.muted, fontSize = 13.sp)
                    OutlinedButton(
                        onClick = { NavUpdateFlow.open() },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(localized("Обновить", "Update", lang))
                    }
                }
                val manualResult = NavUpdateChecker.lastManualMessage
                if (manualResult != null) {
                    Text(when (manualResult) {
                        "available" -> localized("Обновление найдено", "Update found", lang)
                        "offline" -> localized("Нет соединения. Попробуйте позже.",
                            "No connection. Try again later.", lang)
                        "test_available" -> localized("Найдена тестовая версия", "Test update found", lang)
                        "test_none" -> localized("Тестовых обновлений нет", "No test updates available", lang)
                        "test_offline" -> localized("Тестовый канал недоступен",
                            "Test channel unavailable", lang)
                        "test_timeout" -> localized("Тестовый канал: время ожидания истекло. Повторите проверку.",
                            "Test channel timed out. Check again.", lang)
                        "test_dns" -> localized("Тестовый канал: не удалось найти сервер.",
                            "Test channel: server address unavailable.", lang)
                        "test_tls" -> localized("Тестовый канал: ошибка защищённого соединения.",
                            "Test channel: secure connection failed.", lang)
                        "test_redirect" -> localized("Тестовый канал: небезопасное перенаправление.",
                            "Test channel: unsafe redirect.", lang)
                        "test_bad_feed" -> localized("Тестовый канал: неверные данные ответа.",
                            "Test channel: invalid response data.", lang)
                        "test_network" -> localized("Тестовый канал: ошибка соединения. Повторите позже.",
                            "Test channel: connection failed. Try again.", lang)
                        "test_security" -> localized("Тестовый канал: доступ запрещён системой.",
                            "Test channel: access blocked by system.", lang)
                        else -> if (manualResult.startsWith("test_http_"))
                            localized("Тестовый канал: ответ сервера HTTP ${manualResult.removePrefix("test_http_")}.",
                                "Test channel: server returned HTTP ${manualResult.removePrefix("test_http_")}.", lang)
                        else if (manualResult == "test_error")
                            localized("Тестовый канал: внутренняя ошибка проверки.",
                                "Test channel: unexpected checker error.", lang)
                        else localized("Новых совместимых версий нет",
                            "No compatible updates found", lang)
                    }, color = p.muted, fontSize = 12.sp)
                }
                val scope = rememberCoroutineScope()
                OutlinedButton(
                    onClick = { scope.launch { NavUpdateChecker.check(context, manual = true) } },
                    enabled = !NavUpdateChecker.checking,
                    modifier = Modifier.fillMaxWidth(),
                    border = androidx.compose.foundation.BorderStroke(1.dp, p.blue),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = p.blueText),
                ) {
                    Text(if (NavUpdateChecker.checking)
                        localized("Проверяем…", "Checking…", lang)
                        else localized("Проверить обновления", "Check for updates", lang))
                }
                // QA-only control absent from production 1.0.x.
                if (NavUpdateQa.isQaBuild(context)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(localized("Тестировать предложение при запуске",
                            "Test startup update offer", lang),
                            color = p.text, modifier = Modifier.weight(1f), fontSize = 14.sp)
                        Switch(settings.qaAutomaticTestOffers,
                            onCheckedChange = { settings.updateQaAutomaticTestOffers(it) },
                            enabled = settings.automaticUpdateOffers,
                            colors = navSwitchColors(p))
                    }
                    Text(localized("Только RC: при следующем запуске проверяется тестовый канал.",
                        "RC only: next launch checks the test feed.", lang),
                        color = p.muted, fontSize = 12.sp)
                }
                // TEST discovery stays available for the original signed RC15 QA build,
                // but is hidden from every ordinary production 1.0.x Settings screen.
                if (NavUpdateQa.isQaBuild(context)) {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                if (NavUpdateChecker.checkTest(context)) NavUpdateFlow.openTest()
                            }
                        },
                        enabled = !NavUpdateChecker.checking,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(localized("Проверить тестовый канал", "Check test channel", lang))
                    }
                    Text(localized("Тестовые версии устанавливаются только по вашему выбору.",
                        "Test builds install only after your approval.", lang),
                        color = p.muted, fontSize = 12.sp)
                }
            }
            NavPanel(p) {
                Text("NavTasks " + actualVersion, color = p.text, fontWeight = FontWeight.Medium)
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
    checkedTrackColor = p.blue,
    checkedThumbColor = Color.White,
    checkedBorderColor = p.blue,
    uncheckedTrackColor = p.panel,
    uncheckedThumbColor = p.muted,
    uncheckedBorderColor = p.border,
)
