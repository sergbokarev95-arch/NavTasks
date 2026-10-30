package com.navtasks.app

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class NavPalette(
    val dark: Boolean,
    val background: Color,
    val backgroundEnd: Color,
    val panel: Color,
    val task: Color,
    val nestedTask: Color,
    val border: Color,
    val text: Color,
    val muted: Color,
    val blue: Color,
    val mint: Color,
)

fun navPalette(dark: Boolean) = if (dark) NavPalette(
    true, Color(0xFF0B182A), Color(0xFF142D49), Color(0xE021344F),
    Color(0xE01A304B), Color(0xE021415E), Color(0xFF36536B),
    Color(0xFFF2F8FF), Color(0xFFAFC4D8), Color(0xFF78C4F2), Color(0xFF69D4BE),
) else NavPalette(
    false, Color(0xFFEAF3FA), Color(0xFFF9FBFF), Color(0xF9FFFFFF),
    Color(0xFFF8FCFF), Color(0xFFE1F0FA), Color(0xFFC9DFED),
    Color(0xFF172B42), Color(0xFF60788F), Color(0xFF267BAF), Color(0xFF228E80),
)

fun localized(ru: String, en: String, lang: UiLanguage) = if (lang == UiLanguage.RU) ru else en

fun navDate(date: LocalDate, lang: UiLanguage, includeYear: Boolean = false): String {
    val pattern = if (lang == UiLanguage.RU) {
        if (includeYear) "d MMMM yyyy" else "d MMMM"
    } else if (includeYear) "MMM d, yyyy" else "MMM d"
    return date.format(DateTimeFormatter.ofPattern(pattern, if (lang == UiLanguage.RU) Locale("ru") else Locale.ENGLISH))
}

@Composable
fun NavTasksApp(
    store: TaskStore,
    settings: NavSettings,
    requestedRunKey: String? = null,
    onRequestHandled: () -> Unit = {},
) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var showSettings by rememberSaveable { mutableStateOf(false) }
    var editorOpen by remember { mutableStateOf(false) }
    var editorTask by remember { mutableStateOf<TaskTemplate?>(null) }
    var templateFilter by rememberSaveable { mutableStateOf(Frequency.DAILY) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var expandedRun by remember { mutableStateOf<String?>(null) }
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    var previousToday by remember { mutableStateOf(LocalDate.now()) }

    val lang = settings.language
    val dark = when (settings.theme) {
        UiTheme.DARK -> true
        UiTheme.LIGHT -> false
        UiTheme.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
    }
    val palette = navPalette(dark)

    LaunchedEffect(Unit) {
        while (true) {
            val tick = LocalDateTime.now()
            if (now.toLocalDate() != tick.toLocalDate()) store.refresh(tick.toLocalDate())
            now = tick
            delay(30_000)
        }
    }
    LaunchedEffect(now.toLocalDate()) {
        if (selectedDate == previousToday) selectedDate = now.toLocalDate()
        previousToday = now.toLocalDate()
        store.refresh(now.toLocalDate())
    }
    LaunchedEffect(requestedRunKey) {
        if (requestedRunKey != null) {
            val target = store.runs[requestedRunKey]
            if (target != null) {
                selectedDate = target.displayDate
                tab = 0
                showSettings = false
                editorOpen = false
                expandedRun = requestedRunKey
            }
            onRequestHandled()
        }
    }

    val scheme = if (dark) darkColorScheme(
        primary = palette.blue, secondary = palette.mint, background = palette.background,
        surface = palette.panel, onSurface = palette.text, onBackground = palette.text,
        surfaceVariant = palette.task, onSurfaceVariant = palette.muted,
    ) else lightColorScheme(
        primary = palette.blue, secondary = palette.mint, background = palette.background,
        surface = palette.panel, onSurface = palette.text, onBackground = palette.text,
        surfaceVariant = palette.task, onSurfaceVariant = palette.muted,
    )

    MaterialTheme(colorScheme = scheme) {
        Box(Modifier.fillMaxSize()) {
            NavGlassBackdrop(palette)
            if (editorOpen) {
                TaskEditorScreen(
                    initial = editorTask, defaultFrequency = templateFilter, lang = lang, palette = palette,
                    onBack = { editorOpen = false },
                    onSave = { store.upsert(it); editorOpen = false },
                )
            } else if (showSettings) {
                SettingsScreen(settings, store, palette, onBack = { showSettings = false })
            } else {
                Scaffold(
                    containerColor = Color.Transparent,
                    bottomBar = {
                        NavGlassBottomBar(palette, tab, lang) { tab = it }
                    },
                ) { padding ->
                    Column(Modifier.fillMaxSize().padding(padding)) {
                        Row(
                            Modifier.fillMaxWidth().padding(start = 18.dp, end = 14.dp, top = 14.dp, bottom = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text("NAVTASKS", color = palette.blue, fontSize = 10.sp, letterSpacing = 2.sp)
                                Text(
                                    when (tab) {
                                        0 -> localized("Сегодня", "Today", lang)
                                        1 -> localized("План", "Plan", lang)
                                        else -> localized("Задачи", "Tasks", lang)
                                    }, fontSize = 26.sp, color = palette.text, fontWeight = FontWeight.Medium,
                                )
                            }
                            NavGlassCircleButton(palette, "settings") { showSettings = true }
                        }
                        when (tab) {
                            0 -> TodayScreenV2(store, now, selectedDate, { selectedDate = it }, palette, lang,
                                expandedRun, { expandedRun = it })
                            1 -> PlanScreenV2(store, now.toLocalDate(), palette, lang)
                            else -> TasksScreenV2(store, templateFilter, { templateFilter = it }, palette, lang,
                                onAdd = { editorTask = null; editorOpen = true },
                                onEdit = { editorTask = it; editorOpen = true })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NavPanel(p: NavPalette, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier.navGlass(p, radius = 20.dp, role = GlassRole.PANEL).padding(14.dp),
        content = content,
    )
}

@Composable
fun RoundCheck(checked: Boolean, p: NavPalette, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier.size(29.dp).clip(CircleShape)
            .background(if (checked) p.mint else Color.Transparent)
            .border(2.dp, if (checked) p.mint else p.blue, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) Text("✓", color = if (p.dark) Color(0xFF103F49) else Color.White,
            fontWeight = FontWeight.Bold, fontSize = 17.sp)
    }
}

@Composable
private fun TodayScreenV2(
    store: TaskStore,
    now: LocalDateTime,
    selectedDay: LocalDate,
    onSelectDay: (LocalDate) -> Unit,
    p: NavPalette,
    lang: UiLanguage,
    expandedRun: String?,
    onExpanded: (String?) -> Unit,
) {
    var showMissed by rememberSaveable { mutableStateOf(false) }
    var showCompleted by rememberSaveable { mutableStateOf(false) }
    val currentDay = now.toLocalDate()
    val rows = store.forDate(selectedDay)
    val incomplete = rows.filterNot { it.second.completed }
    val complete = rows.filter { it.second.completed }
    val missed = store.missed(now)

    Column {
        NavPanel(p, Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(navDate(selectedDay, lang, true), color = p.muted, fontSize = 12.sp)
                    Text(localized("Прогресс дня", "Daily progress", lang), color = p.text, fontSize = 15.sp)
                }
                Text("${complete.size}/${rows.size}", color = p.blue, fontSize = 23.sp,
                    fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { if (rows.isEmpty()) 0f else complete.size.toFloat() / rows.size },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                color = p.mint, trackColor = p.border,
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = 9.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            DayChip(localized("Пропущенные", "Missed", lang), "${missed.size}", showMissed,
                if (missed.isEmpty()) p.muted else Color(0xFFF0AE85), p) { showMissed = true }
            (0..6).forEach { offset ->
                val day = currentDay.plusDays(offset.toLong())
                val label = day.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT,
                    if (lang == UiLanguage.RU) Locale("ru") else Locale.ENGLISH)
                DayChip(label, "${day.dayOfMonth}", !showMissed && selectedDay == day, p.blue, p) {
                    showMissed = false
                    onSelectDay(day)
                    onExpanded(null)
                }
            }
        }
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 26.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (showMissed) {
                item { Text(localized("Незакрытые пропуски", "Unfinished overdue tasks", lang),
                    color = p.muted, fontSize = 13.sp, modifier = Modifier.padding(vertical = 6.dp)) }
                if (missed.isEmpty()) item { NavPanel(p) { Text(localized("Пропущенных задач нет", "No missed tasks", lang), color = p.mint) } }
                missed.groupBy { it.second.dueDate }.forEach { (originalDate, group) ->
                    item(key = "missed-date-$originalDate") {
                        Text(navDate(originalDate, lang, true), color = p.blue, fontSize = 12.sp,
                            modifier = Modifier.padding(top = 7.dp, bottom = 2.dp))
                    }
                    items(group, key = { it.second.key }) { (task, run) ->
                        RunTileV2(task, run, store, p, lang, expandedRun == run.key,
                            onToggleExpand = { onExpanded(if (expandedRun == run.key) null else run.key) }, overdue = true)
                    }
                }
            } else {
                items(incomplete, key = { it.second.key }) { (task, run) ->
                    RunTileV2(task, run, store, p, lang, expandedRun == run.key,
                        onToggleExpand = { onExpanded(if (expandedRun == run.key) null else run.key) })
                }
                if (incomplete.isEmpty()) item {
                    NavPanel(p) { Text(localized("На этот день всё выполнено ✓", "All done for this day ✓", lang), color = p.mint) }
                }
                if (complete.isNotEmpty()) item {
                    Column(Modifier.fillMaxWidth()) {
                        Column(
                            Modifier.fillMaxWidth().navGlass(p, radius = 17.dp, role = GlassRole.PANEL).clickable { showCompleted = !showCompleted }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("✓", color = p.mint, fontSize = 19.sp)
                                Spacer(Modifier.width(9.dp))
                                Text(localized("Выполнено", "Completed", lang), color = p.text,
                                    modifier = Modifier.weight(1f), fontSize = 15.sp)
                                Text("${complete.size}  ${if (showCompleted) "⌃" else "▱"}", color = p.muted)
                            }
                        }
                        if (!showCompleted) {
                            Box(Modifier.fillMaxWidth(0.94f).align(Alignment.CenterHorizontally).height(5.dp)
                                .background(p.border.copy(alpha = 0.7f), RoundedCornerShape(bottomStart = 9.dp, bottomEnd = 9.dp)))
                            Box(Modifier.fillMaxWidth(0.86f).align(Alignment.CenterHorizontally).height(4.dp)
                                .background(p.border.copy(alpha = 0.45f), RoundedCornerShape(bottomStart = 9.dp, bottomEnd = 9.dp)))
                        }
                        Column(Modifier.padding(top = 7.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            complete.forEachIndexed { index, (task, run) ->
                                AnimatedVisibility(showCompleted,
                                    enter = expandVertically(animationSpec = tween(290, delayMillis = index * 60)) +
                                        slideInVertically(initialOffsetY = { -(index + 1) * 18 }, animationSpec = tween(290, delayMillis = index * 60)) +
                                        fadeIn(animationSpec = tween(260, delayMillis = index * 60)),
                                    exit = shrinkVertically(animationSpec = tween(250)) +
                                        slideOutVertically(targetOffsetY = { -(index + 1) * 18 }, animationSpec = tween(250)) +
                                        fadeOut(animationSpec = tween(190)),
                                ) {
                                    RunTileV2(task, run, store, p, lang, expandedRun == run.key,
                                        onToggleExpand = { onExpanded(if (expandedRun == run.key) null else run.key) })
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayChip(top: String, bottom: String, selected: Boolean, accent: Color, p: NavPalette, action: () -> Unit) {
    Column(
        Modifier.navGlass(p, radius = 13.dp, role = if (selected) GlassRole.SELECTED else GlassRole.CONTROL)
            .clickable(onClick = action).padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(top, color = if (selected) p.blue else p.muted, fontSize = 11.sp, maxLines = 1)
        Text(bottom, color = if (selected) p.blue else p.text, fontSize = 15.sp,
            fontWeight = FontWeight.Medium)
    }
}

@Composable
fun RunTileV2(
    task: TaskTemplate,
    run: TaskRun,
    store: TaskStore,
    p: NavPalette,
    lang: UiLanguage,
    expanded: Boolean,
    onToggleExpand: () -> Unit,
    overdue: Boolean = false,
) {
    var menuOpen by remember { mutableStateOf(false) }
    var closeDialog by remember { mutableStateOf(false) }
    var closeReason by remember { mutableStateOf("") }
    var exiting by remember(run.key) { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val amount by animateFloatAsState(if (exiting) 1f else 0f, tween(300), label = "taskComplete")
    val context = LocalContext.current
    val shape = RoundedCornerShape(17.dp)
    Column(
        Modifier.fillMaxWidth().graphicsLayer {
            alpha = 1f - amount * 0.85f
            translationY = if (amount < 0.32f) -amount / 0.32f * 10.dp.toPx()
                else -10.dp.toPx() + (amount - 0.32f) / 0.68f * 55.dp.toPx()
            scaleX = 1f + (1f - kotlin.math.abs(amount - 0.4f) / 0.6f).coerceIn(0f, 1f) * 0.045f
            scaleY = scaleX
        }.navGlass(p, radius = 17.dp,
            role = if (run.checklist.isEmpty()) GlassRole.TASK else GlassRole.NESTED_TASK)
            .animateContentSize(),
    ) {
        Row(Modifier.padding(start = 11.dp, end = 6.dp, top = 11.dp, bottom = 11.dp),
            verticalAlignment = Alignment.CenterVertically) {
            RoundCheck(run.completed, p, onClick = {
                if (run.checklist.isNotEmpty()) onToggleExpand()
                else if (run.completed) store.setSimpleComplete(run.key, false)
                else {
                    exiting = true
                    scope.launch { delay(270); store.setSimpleComplete(run.key, true); exiting = false }
                }
            })
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f).clickable {
                if (run.checklist.isNotEmpty()) onToggleExpand()
                else store.setSimpleComplete(run.key, !run.completed)
            }) {
                Text(task.title, color = if (run.completed) p.muted else p.text,
                    fontSize = 14.sp, maxLines = 2,
                    textDecoration = if (run.completed) TextDecoration.LineThrough else null)
                Spacer(Modifier.height(2.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (run.checklist.isNotEmpty()) Text("${run.checkedCount}/${run.checklist.size}", color = p.blue, fontSize = 11.sp)
                    if (run.transferred) Text(localized("Перенесено", "Moved", lang), color = p.mint, fontSize = 11.sp)
                    if (overdue) Text(navDate(run.dueDate, lang), color = Color(0xFFF0AC8F), fontSize = 11.sp)
                    if (task.deadlineTime != null) Text(task.deadlineTime.toString(), color = p.muted, fontSize = 11.sp)
                }
            }
            Box {
                Text("⋯", color = p.muted, fontSize = 23.sp,
                    modifier = Modifier.clip(CircleShape).clickable { menuOpen = true }.padding(8.dp))
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    if (!run.completed) {
                    listOf(
                        1L to localized("Завтра", "Tomorrow", lang),
                        3L to localized("Через 3 дня", "+3 days", lang),
                        7L to localized("Через неделю", "Next week", lang),
                    ).forEach { (days, label) ->
                        DropdownMenuItem(text = { Text(label) }, onClick = {
                            menuOpen = false
                            store.postpone(run.key, LocalDate.now().plusDays(days))
                        })
                    }
                    DropdownMenuItem(text = { Text(localized("Выбрать дату…", "Choose date…", lang)) }, onClick = {
                        menuOpen = false
                        val start = run.displayDate.plusDays(1)
                        DatePickerDialog(context, { _, y, m, d ->
                            store.postpone(run.key, LocalDate.of(y, m + 1, d))
                        }, start.year, start.monthValue - 1, start.dayOfMonth).show()
                    })
                    DropdownMenuItem(text = { Text(localized("Закрыть с причиной…", "Close with reason…", lang)) },
                        onClick = { menuOpen = false; closeReason = ""; closeDialog = true })
                    }
                }
            }
        }
        AnimatedVisibility(visible = expanded && run.checklist.isNotEmpty(),
            enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
            Column(Modifier.padding(start = 51.dp, end = 13.dp, bottom = 13.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                HorizontalDivider(color = p.border)
                run.checklist.forEach { item ->
                    Row(verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().navGlass(p, radius = 10.dp,
                            role = GlassRole.CONTROL, elevation = 0.dp).padding(8.dp)) {
                        RoundCheck(item.id in run.checkedIds, p,
                            onClick = { store.setCheck(run.key, item.id, item.id !in run.checkedIds) },
                            modifier = Modifier.size(27.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(item.title, color = p.text, fontSize = 13.sp, modifier = Modifier.weight(1f),
                            textDecoration = if (item.id in run.checkedIds) TextDecoration.LineThrough else null)
                    }
                }
            }
        }
    }
    if (closeDialog) {
        AlertDialog(
            onDismissRequest = { closeDialog = false },
            title = { Text(localized("Закрыть задачу", "Close task", lang)) },
            text = {
                OutlinedTextField(closeReason, { closeReason = it }, modifier = Modifier.fillMaxWidth(),
                    label = { Text(localized("Причина", "Reason", lang)) },
                    placeholder = { Text(localized("Например: отменено капитаном", "E.g. cancelled by Captain", lang)) })
            },
            confirmButton = {
                TextButton(enabled = closeReason.isNotBlank(), onClick = {
                    store.closeWithReason(run.key, closeReason)
                    closeDialog = false
                }) { Text(localized("Закрыть", "Close", lang)) }
            },
            dismissButton = { TextButton(onClick = { closeDialog = false }) { Text(localized("Отмена", "Cancel", lang)) } },
        )
    }
}
