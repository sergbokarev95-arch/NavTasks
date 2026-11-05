package com.navtasks.app

import android.app.DatePickerDialog
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import dev.chrisbanes.haze.hazeSource
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.abs
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
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
    true, Color.Black, Color.Black, Color(0xE021344F),
    Color(0xE01A304B), Color(0xE021415E), Color(0xFF36536B),
    Color(0xFFF2F8FF), Color(0xFFAFC4D8), Color(0xFF44C4FF), Color(0xFF68CE77),
) else NavPalette(
    false, Color(0xFFEAF3FA), Color(0xFFF9FBFF), Color(0xF9FFFFFF),
    Color(0xFFF8FCFF), Color(0xFFE1F0FA), Color(0xFFC9DFED),
    Color(0xFF172B42), Color(0xFF60788F), Color(0xFF267BAF), Color(0xFF60C569),
)

/** HONOR-style success green, with readable contrast for text in Day mode. */
val NavPalette.successText: Color
    get() = if (dark) mint else Color(0xFF2B8951)

fun localized(ru: String, en: String, lang: UiLanguage) = if (lang == UiLanguage.RU) ru else en

fun navDate(date: LocalDate, lang: UiLanguage, includeYear: Boolean = false): String {
    val pattern = if (lang == UiLanguage.RU) {
        if (includeYear) "d MMMM yyyy" else "d MMMM"
    } else if (includeYear) "MMM d, yyyy" else "MMM d"
    return date.format(DateTimeFormatter.ofPattern(pattern, if (lang == UiLanguage.RU) Locale("ru") else Locale.ENGLISH))
}


/**
 * Select adjacent primary tab on a horizontal swipe. Child widgets (the
 * calendar pager and the 7-day scroller) own gestures they consume.
 * A vertical movement, short swipe, or system-edge gesture never changes tabs.
 */
internal fun tabAfterHorizontalSwipe(
    current: Int,
    deltaX: Float,
    deltaY: Float,
    minDistance: Float,
    consumedByChild: Boolean,
): Int {
    if (consumedByChild || abs(deltaX) < minDistance ||
        abs(deltaX) < abs(deltaY) * 1.35f
    ) return current
    return (current + if (deltaX < 0f) 1 else -1).coerceIn(0, 3)
}

private fun Modifier.primaryTabSwipe(
    tab: Int,
    onSelect: (Int) -> Unit,
): Modifier = pointerInput(tab) {
    val edgeGuard = 28.dp.toPx()
    val travel = 78.dp.toPx()
    awaitEachGesture {
        // Watch the Final pass instead of claiming the child's swipe.
        // This preserves the calendar's month/day swipe and the week strip.
        val first = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Final)
        val inContent = first.position.x > edgeGuard &&
            first.position.x < size.width - edgeGuard
        var dx = 0f
        var dy = 0f
        var claimedByChild = false
        while (true) {
            val event = awaitPointerEvent(pass = PointerEventPass.Final)
            val change = event.changes.firstOrNull { it.id == first.id } ?: break
            if (!change.pressed) break
            dx += change.position.x - change.previousPosition.x
            dy += change.position.y - change.previousPosition.y
            if (change.isConsumed) claimedByChild = true
        }
        if (inContent) {
            val next = tabAfterHorizontalSwipe(tab, dx, dy, travel, claimedByChild)
            if (next != tab) onSelect(next)
        }
    }
}

@Composable
fun NavTasksApp(
    store: TaskStore,
    settings: NavSettings,
    profiles: ProfileStore,
    requestedRunKey: String? = null,
    onRequestHandled: () -> Unit = {},
    requestedWorkOrderId: String? = null,
    onWorkOrderRequestHandled: () -> Unit = {},
) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    // Four tabs: 0=Today, 1=Plan, 2=Handover, 3=Tasks.
    // Kept across activity recreation so Android's Back gesture retraces navigation.
    var tabHistory by rememberSaveable { mutableStateOf("") }
    // Keep the selected calendar zoom/day and tab-specific scroll state intact
    // when Back closes Settings, the editor, or returns to a previous tab.
    val tabStateHolder = rememberSaveableStateHolder()
    var showSettings by rememberSaveable { mutableStateOf(false) }
    var showProfiles by rememberSaveable { mutableStateOf(false) }
    var showServices by rememberSaveable { mutableStateOf(false) }
    var showWorks by rememberSaveable { mutableStateOf(false) }
    var showWorkOrders by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val handoverStore = remember(context, profiles) { HandoverStore(context, profiles) }
    val serviceStore = remember(context, profiles) { ServiceStore(context, profiles) }
    val workOrderStore = remember(context, profiles) { WorkOrderStore(context, profiles) }
    var todayServiceRequest by remember { mutableStateOf<ServiceEntry?>(null) }
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
    LaunchedEffect(requestedWorkOrderId) {
        if (requestedWorkOrderId != null) {
            if (workOrderStore.entries.any { it.id == requestedWorkOrderId }) {
                editorOpen = false
                showSettings = false
                showProfiles = false
                showServices = false
                showWorks = true
                showWorkOrders = true
            }
            onWorkOrderRequestHandled()
        }
    }
    LaunchedEffect(requestedRunKey) {
        if (requestedRunKey != null) {
            val target = store.runs[requestedRunKey]
            if (target != null) {
                selectedDate = target.displayDate
                tab = 0
                tabHistory = ""
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

    fun openPrimaryTab(next: Int) {
        if (next == tab) return
        tabHistory = (tabHistory + tab.toString()).takeLast(12)
        tab = next
    }

    // Android gesture navigation dispatches the system Back action here. At the root
    // of Today there is intentionally no interception so the user can leave the app.
    BackHandler(enabled = editorOpen || showSettings || showProfiles || showServices || showWorks || showWorkOrders ||
            profiles.active.kind == NavProfileKind.PERSONAL || tabHistory.isNotEmpty() || tab != 0) {
        when {
            editorOpen -> editorOpen = false
            showProfiles -> showProfiles = false
            showSettings -> showSettings = false
            showServices -> showServices = false
            showWorkOrders -> showWorkOrders = false
            showWorks -> showWorks = false
            profiles.active.kind == NavProfileKind.PERSONAL -> showProfiles = true
            tabHistory.isNotEmpty() -> {
                tab = tabHistory.last().digitToInt()
                tabHistory = tabHistory.dropLast(1)
            }
            tab != 0 -> tab = 0
        }
    }

    MaterialTheme(colorScheme = scheme) {
        Box(Modifier.fillMaxSize()) {
            NavGlassBackdrop(palette)
            if (editorOpen) {
                TaskEditorScreen(
                    initial = editorTask, defaultFrequency = templateFilter, lang = lang, palette = palette,
                    store = store,
                    onBack = { editorOpen = false },
                    onSave = { store.upsert(it); editorOpen = false },
                )
            } else if (showProfiles) {
                ProfilesScreen(profiles, store, handoverStore, serviceStore, workOrderStore, palette, lang,
                    onBack = {
                        showProfiles = false
                        tab = 0
                        tabHistory = ""
                        selectedDate = LocalDate.now()
                        templateFilter = Frequency.DAILY
                        expandedRun = null
                    },
                    onSettings = { showProfiles = false; showSettings = true },
                )
            } else if (showSettings) {
                SettingsScreen(settings, store, palette, onBack = { showSettings = false },
                    profiles = profiles, handover = handoverStore, services = serviceStore,
                    workOrders = workOrderStore)
            } else if (profiles.active.kind == NavProfileKind.PERSONAL) {
                HandoverScreen(handoverStore, store, palette, lang,
                    onBack = { showProfiles = true }, personalMode = true)
            } else if (showServices) {
                ServicesScreen(serviceStore, store, settings, palette, lang,
                    onBack = { showServices = false })
            } else if (showWorkOrders) {
                WorkOrdersScreen(workOrderStore, store, settings, palette, lang,
                    onBack = { showWorkOrders = false })
            } else if (showWorks) {
                WorksHubScreen(serviceStore, workOrderStore, palette, lang,
                    onBack = { showWorks = false },
                    onServices = { showServices = true },
                    onAmos = { showWorkOrders = true })
            } else {
                val hazeState = remember { dev.chrisbanes.haze.HazeState() }
                Box(Modifier.fillMaxSize()) {
                    // Content paints to the bottom edge. Navigation is an overlay,
                    // NOT a Scaffold bottomBar which would reserve an opaque footer.
                    Column(Modifier.fillMaxSize().statusBarsPadding().hazeSource(hazeState)
                        .primaryTabSwipe(tab, ::openPrimaryTab)) {
                        Row(
                            Modifier.fillMaxWidth().padding(start = 18.dp, end = 14.dp, top = 14.dp, bottom = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("NAVTASKS", color = palette.blue, fontSize = 17.sp,
                                        letterSpacing = 1.6.sp)
                                    Spacer(Modifier.width(9.dp))
                                    Row(
                                        Modifier.glassClickable { showProfiles = true }
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            profiles.active.title, color = palette.muted,
                                            fontSize = 12.sp, maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.widthIn(max = 116.dp),
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        NavGlassGlyph("chevron-down", palette.blue, Modifier.size(14.dp))
                                    }
                                }
                                Text(
                                    when (tab) {
                                        0 -> localized("Сегодня", "Today", lang)
                                        1 -> localized("План", "Plan", lang)
                                        2 -> "Handover"
                                        else -> localized("Задачи", "Tasks", lang)
                                    }, fontSize = 26.sp, color = palette.text, fontWeight = FontWeight.Medium,
                                )
                            }
                            NavGlassCircleButton(palette, "settings") { showSettings = true }
                        }
                        AnimatedContent(
                            targetState = tab,
                            modifier = Modifier.fillMaxWidth().weight(1f).clipToBounds(),
                            transitionSpec = {
                                val forward = targetState > initialState
                                // A short, symmetric move keeps dense task text readable.
                                // Match the dock's critically damped spring; interrupted
                                // transitions continue from their current animated values.
                                (slideInHorizontally(
                                    initialOffsetX = { if (forward) it / 5 else -it / 5 },
                                    animationSpec = spring(dampingRatio = 1f, stiffness = 550f),
                                ) + fadeIn(spring(dampingRatio = 1f, stiffness = 550f))) togetherWith
                                    (slideOutHorizontally(
                                        targetOffsetX = { if (forward) -it / 5 else it / 5 },
                                        animationSpec = spring(dampingRatio = 1f, stiffness = 550f),
                                    ) + fadeOut(spring(dampingRatio = 1f, stiffness = 550f)))
                            },
                            label = "primaryTabSlide",
                        ) { page ->
                            tabStateHolder.SaveableStateProvider(
                                profiles.activeId + "/main-tab-" + page
                            ) {
                                when (page) {
                                    0 -> TodayScreenV2(store, now, selectedDate, { selectedDate = it }, palette, lang,
                                        expandedRun, { expandedRun = it },
                                        workOrders = workOrderStore, services = serviceStore,
                                        onAmos = { showWorks = true; showWorkOrders = true },
                                        onServices = { showWorks = true; showServices = true },
                                        onRequestNumber = { todayServiceRequest = it })
                                    1 -> PlanScreenV2(store, now.toLocalDate(), palette, lang,
                                        onEdit = { editorTask = it; editorOpen = true },
                                        workOrders = workOrderStore, services = serviceStore)
                                    2 -> HandoverScreen(
                                        handoverStore, store, palette, lang,
                                        onBack = { openPrimaryTab(1) }, embeddedInTab = true,
                                    )
                                    else -> TasksScreenV2(store, templateFilter, { templateFilter = it }, palette, lang,
                                        onEdit = { editorTask = it; editorOpen = true },
                                        services = serviceStore, onWorks = { showWorks = true })
                                }
                            }
                        }
                    }
                    if (tab == 3) {
                        Box(
                            Modifier.align(Alignment.BottomStart).navigationBarsPadding()
                                .padding(start = 19.dp, bottom = 83.dp),
                        ) {
                            NavGlassAddButton(palette, hazeState) {
                                editorTask = null
                                editorOpen = true
                            }
                        }
                    }
                    NavGlassBottomBar(
                        p = palette, selected = tab, lang = lang,
                        modifier = Modifier.align(Alignment.BottomCenter),
                        hazeState = hazeState,
                        onSelect = ::openPrimaryTab,
                    )
                }

            }
        }
        todayServiceRequest?.let { service ->
            ServiceRequestDialog(
                service, palette, lang,
                onDismiss = { todayServiceRequest = null },
                onSave = { number ->
                    runCatching {
                        serviceStore.setRequestNumber(service.id, number)
                        NavNotifications.reschedule(context, store, settings)
                    }.onSuccess { todayServiceRequest = null }
                },
            )
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
fun RoundCheck(
    checked: Boolean, p: NavPalette, onClick: () -> Unit,
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource? = null,
) {
    Box(
        modifier.size(29.dp).clip(CircleShape)
            .background(if (checked) p.mint else Color.Transparent)
            .border(2.dp, if (checked) p.mint else p.blue, CircleShape)
            .glassClickable(interactionSource = interactionSource, onClick = onClick),
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
    workOrders: WorkOrderStore,
    services: ServiceStore,
    onAmos: () -> Unit,
    onServices: () -> Unit,
    onRequestNumber: (ServiceEntry) -> Unit,
) {
    var showMissed by rememberSaveable { mutableStateOf(false) }
    var showCompleted by rememberSaveable { mutableStateOf(false) }
    // Prefer stepping out of open checklists and sections before switching tabs.
    BackHandler(enabled = expandedRun != null || showMissed || showCompleted) {
        when {
            expandedRun != null -> onExpanded(null)
            showMissed -> showMissed = false
            showCompleted -> showCompleted = false
        }
    }
    val currentDay = now.toLocalDate()
    val rows = store.forDate(selectedDay)
    val incomplete = rows.filterNot { it.second.completed }
    val complete = rows.filter { it.second.completed }
    val missed = store.missed(now)
    val browsingOtherDay = !showMissed && selectedDay != currentDay
    val extraDue = groupTodayByFrequency(rows).filter { it.first != Frequency.DAILY }
    val dayWorkOrders = workOrders.entries
    val dayServices = services.entries
    val hasWork = dayWorkOrders.any { amosVisibleOnDay(it, selectedDay) } ||
        dayServices.any { serviceVisibleOnDay(it, selectedDay) }

    Column {
        NavPanel(p, Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(navDate(selectedDay, lang, true), color = p.muted, fontSize = 12.sp)
                    Text(
                        if (browsingOtherDay) localized("Прогресс выбранного дня", "Selected day's progress", lang)
                        else if (hasWork) localized("Прогресс задач", "Task progress", lang)
                        else localized("Прогресс дня", "Daily progress", lang),
                        color = p.text, fontSize = 15.sp,
                    )
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
        // The missed chip is pinned, and only the week scrolls inside card margins.
        val daysScroll = rememberLazyListState()
        LaunchedEffect(selectedDay, currentDay) {
            val index = java.time.temporal.ChronoUnit.DAYS.between(currentDay, selectedDay).toInt()
            if (index in 0..6) daysScroll.animateScrollToItem(index)
        }
        Row(
            Modifier.fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 9.dp)
                .height(62.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DayChip(
                localized("Пропущенные", "Missed", lang), "${missed.size}", showMissed,
                if (missed.isEmpty()) p.muted else Color(0xFFF0AE85), p,
                modifier = Modifier.width(105.dp).fillMaxHeight(),
            ) {
                showMissed = true
                onExpanded(null)
            }
            Spacer(Modifier.width(7.dp))
            LazyRow(
                state = daysScroll,
                modifier = Modifier.weight(1f).fillMaxHeight()
                    .clip(RoundedCornerShape(13.dp)),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(end = 2.dp),
            ) {
                items(7) { offset ->
                    val day = currentDay.plusDays(offset.toLong())
                    val label = day.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT,
                        if (lang == UiLanguage.RU) Locale("ru") else Locale.ENGLISH)
                    DayChip(
                        label, "${day.dayOfMonth}", !showMissed && selectedDay == day,
                        p.blue, p, modifier = Modifier.width(55.dp).fillMaxHeight(),
                    ) {
                        showMissed = false
                        onSelectDay(day)
                        onExpanded(null)
                    }
                }
            }
        }
        // Special weekly/monthly/one-off duties are visible immediately below the
        // date selection; badges wrap inside the same margins as the task cards.
        AnimatedVisibility(
            visible = !showMissed && extraDue.isNotEmpty(),
            enter = fadeIn(tween(180)) + expandVertically(tween(180)),
            exit = fadeOut(tween(150)) + shrinkVertically(tween(150)),
        ) {
            FlowRow(
                Modifier.fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    if (selectedDay == currentDay)
                        localized("Сегодня также:", "Also today:", lang)
                    else localized("На выбранный день:", "On selected day:", lang),
                    color = p.muted, fontSize = 12.sp,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp, end = 2.dp),
                )
                extraDue.forEach { (frequency, duties) ->
                    Box(
                        Modifier.navGlass(p, radius = 12.dp,
                            role = GlassRole.CONTROL, elevation = 0.dp)
                            .padding(horizontal = 9.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            todayFrequencyName(frequency, lang) + " · " + duties.size,
                            color = todayFrequencyColor(frequency, p), fontSize = 11.sp,
                        )
                    }
                }
            }
        }
        AnimatedVisibility(
            visible = browsingOtherDay,
            enter = fadeIn(animationSpec = tween(180)) + expandVertically(animationSpec = tween(180)),
            exit = fadeOut(animationSpec = tween(160)) + shrinkVertically(animationSpec = tween(160)),
        ) {
            val amber = if (p.dark) Color(0xFFFFC77F) else Color(0xFFAB651D)
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                    .navGlass(p, radius = 13.dp, role = GlassRole.CONTROL)
                    .border(1.dp, amber.copy(alpha = 0.65f), RoundedCornerShape(13.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                NavGlassGlyph("calendar", amber, Modifier.size(21.dp))
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        localized("НЕ СЕГОДНЯ", "NOT TODAY", lang),
                        color = amber, fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                    )
                    Text(navDate(selectedDay, lang, includeYear = true), color = p.text, fontSize = 13.sp)
                }
                Text(
                    localized("Чеки с подтверждением", "Confirm to check", lang),
                    color = amber, fontSize = 10.sp,
                )
            }
        }
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 125.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (showMissed) {
                item { Text(localized("Незакрытые пропуски", "Unfinished overdue tasks", lang),
                    color = p.muted, fontSize = 13.sp, modifier = Modifier.padding(vertical = 6.dp)) }
                if (missed.isEmpty()) item { NavPanel(p) { Text(localized("Пропущенных задач нет", "No missed tasks", lang), color = p.successText) } }
                missed.groupBy { it.second.dueDate }.forEach { (originalDate, group) ->
                    item(key = "missed-date-$originalDate") {
                        Text(navDate(originalDate, lang, true), color = p.blue, fontSize = 12.sp,
                            modifier = Modifier.padding(top = 7.dp, bottom = 2.dp))
                    }
                    groupTodayByFrequency(group).forEach { (frequency, duties) ->
                        item(key = "missed-type-" + originalDate + "-" + frequency.name) {
                            TodayFrequencyHeader(frequency, duties.size, p, lang)
                        }
                        items(duties, key = { it.second.key }) { (task, run) ->
                            RunTileV2(task, run, store, p, lang, expandedRun == run.key,
                                onToggleExpand = { onExpanded(if (expandedRun == run.key) null else run.key) },
                                overdue = true, viewingDate = run.displayDate, today = currentDay)
                        }
                    }
                }
            } else {
                if (hasWork) {
                    item(key = "today-work-agenda") {
                        WorkAgendaTodaySection(
                            day = selectedDay, today = currentDay,
                            orders = dayWorkOrders, services = dayServices,
                            p = p, lang = lang,
                            onAmos = onAmos, onServices = onServices,
                            onRequestNumber = onRequestNumber,
                        )
                    }
                }
                groupTodayByFrequency(incomplete).forEach { (frequency, duties) ->
                    item(key = "today-section-" + frequency.name) {
                        TodayFrequencyHeader(frequency, duties.size, p, lang)
                    }
                    items(duties, key = { it.second.key }) { (task, run) ->
                        RunTileV2(task, run, store, p, lang, expandedRun == run.key,
                            onToggleExpand = { onExpanded(if (expandedRun == run.key) null else run.key) },
                            viewingDate = selectedDay, today = currentDay)
                    }
                }
                if (incomplete.isEmpty() && !hasWork) item {
                    NavPanel(p) { Text(localized("На этот день всё выполнено ✓", "All done for this day ✓", lang), color = p.successText) }
                }
                if (complete.isNotEmpty()) item {
                    val completedArrow by animateFloatAsState(
                        targetValue = if (showCompleted) 180f else 0f,
                        animationSpec = spring(dampingRatio = 1f, stiffness = 550f),
                        label = "completedArrow",
                    )
                    Column(Modifier.fillMaxWidth()) {
                        Column(
                            Modifier.fillMaxWidth().navGlass(p, radius = 17.dp, role = GlassRole.PANEL).glassClickable { showCompleted = !showCompleted }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("✓", color = p.successText, fontSize = 19.sp)
                                Spacer(Modifier.width(9.dp))
                                Text(localized("Выполнено", "Completed", lang), color = p.text,
                                    modifier = Modifier.weight(1f), fontSize = 15.sp)
                                Text(complete.size.toString(), color = p.muted, fontSize = 14.sp)
                                Spacer(Modifier.width(9.dp))
                                NavGlassGlyph(
                                    "chevron-down", p.muted,
                                    Modifier.size(17.dp).rotate(completedArrow),
                                )
                            }
                        }
                        AnimatedVisibility(
                            visible = !showCompleted,
                            enter = expandVertically(
                                spring(dampingRatio = 1f, stiffness = 550f),
                                expandFrom = Alignment.Top,
                            ) + fadeIn(spring(dampingRatio = 1f, stiffness = 550f)),
                            exit = shrinkVertically(
                                spring(dampingRatio = 1f, stiffness = 550f),
                                shrinkTowards = Alignment.Top,
                            ) + fadeOut(spring(dampingRatio = 1f, stiffness = 550f)),
                        ) {
                            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(Modifier.fillMaxWidth(0.94f).height(5.dp)
                                    .background(p.border.copy(alpha = 0.7f), RoundedCornerShape(bottomStart = 9.dp, bottomEnd = 9.dp)))
                                Box(Modifier.fillMaxWidth(0.86f).height(4.dp)
                                    .background(p.border.copy(alpha = 0.45f), RoundedCornerShape(bottomStart = 9.dp, bottomEnd = 9.dp)))
                            }
                        }
                        // One height transition for the whole group: no per-row
                        // delay or empty spacing left by individually hidden rows.
                        AnimatedVisibility(
                            visible = showCompleted,
                            enter = expandVertically(
                                spring(dampingRatio = 1f, stiffness = 550f),
                                expandFrom = Alignment.Top,
                            ) + fadeIn(spring(dampingRatio = 1f, stiffness = 550f)),
                            exit = shrinkVertically(
                                spring(dampingRatio = 1f, stiffness = 550f),
                                shrinkTowards = Alignment.Top,
                            ) + fadeOut(spring(dampingRatio = 1f, stiffness = 550f)),
                        ) {
                            Column(Modifier.padding(top = 7.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                groupTodayByFrequency(complete).forEach { (frequency, duties) ->
                                    TodayFrequencyHeader(frequency, duties.size, p, lang)
                                    duties.forEach { (task, run) ->
                                        key(run.key) {
                                            RunTileV2(task, run, store, p, lang, expandedRun == run.key,
                                                onToggleExpand = { onExpanded(if (expandedRun == run.key) null else run.key) },
                                                viewingDate = selectedDay, today = currentDay)
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

/**
 * Pure presentation-only grouping: does not alter TaskRuns or their original keys.
 * Keep tasks in calendar frequency order and omit categories with no tasks.
 */
internal fun groupTodayByFrequency(
    rows: List<Pair<TaskTemplate, TaskRun>>,
): List<Pair<Frequency, List<Pair<TaskTemplate, TaskRun>>>> =
    Frequency.entries.mapNotNull { frequency ->
        val duties = rows.filter {
            TaskSchedule.occurrenceFrequency(it.first, it.second) == frequency
        }
        if (duties.isEmpty()) null else frequency to duties
    }

private fun todayFrequencyName(frequency: Frequency, lang: UiLanguage): String = when (frequency) {
    Frequency.DAILY -> localized("Ежедневные", "Daily", lang)
    Frequency.WEEKLY -> localized("Недельные", "Weekly", lang)
    Frequency.MONTHLY -> localized("Месячные", "Monthly", lang)
    Frequency.EXTRA -> localized("Разовые", "One-off", lang)
}

private fun todayFrequencyColor(frequency: Frequency, p: NavPalette): Color = when (frequency) {
    Frequency.DAILY -> p.muted
    Frequency.WEEKLY -> p.blue
    Frequency.MONTHLY -> p.mint
    Frequency.EXTRA -> if (p.dark) Color(0xFFFFC77F) else Color(0xFFAA6D30)
}

@Composable
private fun TodayFrequencyHeader(
    frequency: Frequency, count: Int, p: NavPalette, lang: UiLanguage,
) {
    val accent = todayFrequencyColor(frequency, p)
    Row(
        Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(6.dp).background(accent, CircleShape))
        Spacer(Modifier.width(8.dp))
        Text(todayFrequencyName(frequency, lang), color = accent, fontSize = 13.sp)
        Spacer(Modifier.width(7.dp))
        Text(count.toString(), color = p.muted, fontSize = 11.sp)
        Spacer(Modifier.width(12.dp))
        Box(Modifier.weight(1f).height(1.dp).background(p.border.copy(alpha = 0.58f)))
    }
}

@Composable
private fun DayChip(
    top: String, bottom: String, selected: Boolean, accent: Color, p: NavPalette,
    modifier: Modifier = Modifier, action: () -> Unit,
) {
    Column(
        modifier.navGlass(p, radius = 13.dp, role = if (selected) GlassRole.SELECTED else GlassRole.CONTROL)
            .glassClickable(onClick = action).padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(top, color = if (selected) p.blue else p.muted, fontSize = 11.sp, maxLines = 1)
        Text(bottom, color = if (selected) p.blue else p.text, fontSize = 15.sp,
            fontWeight = FontWeight.Medium)
    }
}

/** Confirm checkbox changes on another selected day; missed backlog is an explicit mode. */
internal fun requiresDayConfirmation(
    viewedDate: LocalDate, today: LocalDate, inMissedBacklog: Boolean,
): Boolean = viewedDate != today && !inMissedBacklog

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
    viewingDate: LocalDate = LocalDate.now(),
    today: LocalDate = LocalDate.now(),
) {
    val isOtherDay = requiresDayConfirmation(viewingDate, today, overdue)
    val otherDayAccent = if (p.dark) Color(0xFFFFC77F) else Color(0xFFAB651D)
    var pendingChange by remember(run.key, viewingDate) {
        mutableStateOf<Pair<String?, Boolean>?>(null)
    }
    var openedReferenceId by remember(run.key) { mutableStateOf<String?>(null) }
    var menuOpen by remember { mutableStateOf(false) }
    var postponingDate by remember(run.key) { mutableStateOf<LocalDate?>(null) }
    var pendingScopedMove by remember(run.key) { mutableStateOf<LocalDate?>(null) }
    var closeDialog by remember { mutableStateOf(false) }
    var closeReason by remember { mutableStateOf("") }
    var exiting by remember(run.key) { mutableStateOf(false) }
    var confirming by remember(run.key) { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val amount by animateFloatAsState(if (exiting) 1f else 0f, tween(330), label = "taskComplete")
    val flash = remember(run.key) { Animatable(0f) }
    // Only the header's existing action targets drive the card press.
    // Menu/checklist controls keep their own independent gestures.
    val headerInteraction = remember(run.key) { MutableInteractionSource() }
    val headerPressed by headerInteraction.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (headerPressed && !confirming && !exiting) 0.985f else 1f,
        animationSpec = spring(dampingRatio = 1f, stiffness = 1600f),
        label = "taskHeaderPress",
    )
    // Checklist headers disclose child items; completion stays on the child checks.
    val disclosureRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = spring(dampingRatio = 1f, stiffness = 550f),
        label = "checklistDisclosure",
    )
    suspend fun showConfirmation() {
        // Two distinct mint flashes while the circle displays its tick.
        flash.animateTo(1f, tween(140))
        flash.animateTo(0.22f, tween(170))
        flash.animateTo(0.85f, tween(140))
        delay(150)
    }
    fun completeSimple() {
        if (confirming || exiting) return
        confirming = true
        scope.launch {
            showConfirmation()
            exiting = true
            delay(330)
            store.setSimpleComplete(run.key, true)
            confirming = false
            exiting = false
            flash.snapTo(0f)
        }
    }
    fun applyChecklistChange(id: String, checking: Boolean) {
        if (confirming || exiting) return
        if (checking && run.checklist.all { it.id == id || it.id in run.checkedIds }) {
            confirming = true
            scope.launch {
                showConfirmation()
                exiting = true
                delay(330)
                store.setCheck(run.key, id, true)
                confirming = false
                exiting = false
                flash.snapTo(0f)
            }
        } else store.setCheck(run.key, id, checking)
    }
    fun requestSimpleChange() {
        if (confirming || exiting) return
        val newComplete = !run.completed
        if (isOtherDay) pendingChange = null to newComplete
        else if (newComplete) completeSimple()
        else store.setSimpleComplete(run.key, false)
    }
    fun requestChecklistChange(id: String, checking: Boolean) {
        if (confirming || exiting) return
        if (isOtherDay) pendingChange = id to checking
        else applyChecklistChange(id, checking)
    }
    val context = LocalContext.current
    fun requestMoveDate(target: LocalDate) {
        if (target == run.displayDate) return
        if (canMoveSeries(task, run, target)) {
            pendingScopedMove = target
        } else {
            store.postpone(run.key, target)
        }
    }
    val shape = RoundedCornerShape(15.dp)
    Column(
        Modifier.fillMaxWidth().graphicsLayer {
            alpha = 1f - amount * 0.85f
            translationY = if (amount < 0.32f) -amount / 0.32f * 10.dp.toPx()
                else -10.dp.toPx() + (amount - 0.32f) / 0.68f * 55.dp.toPx()
            scaleX = 1f + (1f - kotlin.math.abs(amount - 0.4f) / 0.6f).coerceIn(0f, 1f) * 0.045f
            scaleX *= pressScale
            scaleY = scaleX
        }.navGlass(p, radius = 15.dp,
            role = if (run.checklist.isEmpty()) GlassRole.TASK else GlassRole.NESTED_TASK)
            .then(
                if (isOtherDay) Modifier.border(
                    1.dp, otherDayAccent.copy(alpha = 0.58f), RoundedCornerShape(15.dp)
                ) else Modifier
            )
            .background(p.mint.copy(alpha = flash.value * 0.36f), RoundedCornerShape(15.dp)),
    ) {
        Row(Modifier.padding(start = 11.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically) {
            if (run.checklist.isNotEmpty()) {
                Box(
                    Modifier.size(29.dp).clip(CircleShape)
                        .glassClickable(interactionSource = headerInteraction, onClick = onToggleExpand),
                    contentAlignment = Alignment.Center,
                ) {
                    NavGlassGlyph(
                        "chevron-down",
                        if (run.completed) p.muted else p.blue,
                        Modifier.size(22.dp).rotate(disclosureRotation),
                    )
                }
            } else {
                RoundCheck(run.completed || confirming, p,
                    onClick = ::requestSimpleChange,
                    modifier = Modifier.size(27.dp), interactionSource = headerInteraction)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f).glassClickable(interactionSource = headerInteraction) {
                if (run.checklist.isNotEmpty()) onToggleExpand()
                else requestSimpleChange()
            }) {
                Text(task.title, color = if (run.completed) p.muted else p.text,
                    fontSize = 14.sp, maxLines = 2,
                    textDecoration = if (run.completed || confirming) TextDecoration.LineThrough else null)
                Spacer(Modifier.height(2.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (run.checklist.isNotEmpty()) Text("${run.checkedCount}/${run.checklist.size}", color = p.blue, fontSize = 11.sp)
                    if (run.transferred) Text(localized("Перенесено", "Moved", lang), color = p.successText, fontSize = 11.sp)
                    if (overdue) Text(navDate(run.dueDate, lang), color = Color(0xFFF0AC8F), fontSize = 11.sp)
                    if (task.deadlineTime != null) Text(task.deadlineTime.toString(), color = p.muted, fontSize = 11.sp)
                }
            }
            Box {
                Text("⋯", color = p.muted, fontSize = 20.sp,
                    modifier = Modifier.clip(CircleShape).glassClickable { menuOpen = true }.padding(6.dp))
                RunTaskPopover(
                    p = p, lang = lang, expanded = menuOpen, available = !run.completed,
                    onDismiss = { menuOpen = false },
                    onPostponeDays = { days ->
                        requestMoveDate(LocalDate.now().plusDays(days))
                    },
                    onChooseDate = { postponingDate = run.displayDate.plusDays(1) },
                    onCloseWithReason = { closeReason = ""; closeDialog = true },
                )
            }
        }
        // Let this transition alone drive the card height and neighbouring rows.
        AnimatedVisibility(
            visible = expanded && run.checklist.isNotEmpty(),
            enter = expandVertically(
                spring(dampingRatio = 1f, stiffness = 550f),
                expandFrom = Alignment.Top,
            ) + fadeIn(spring(dampingRatio = 1f, stiffness = 550f)),
            exit = shrinkVertically(
                spring(dampingRatio = 1f, stiffness = 550f),
                shrinkTowards = Alignment.Top,
            ) + fadeOut(spring(dampingRatio = 1f, stiffness = 550f)),
        ) {
            Column(Modifier.padding(start = 51.dp, end = 13.dp, bottom = 13.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                HorizontalDivider(color = p.border)
                run.checklist.forEach { item ->
                    Row(verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().navGlass(p, radius = 10.dp,
                            role = GlassRole.CONTROL, elevation = 0.dp).padding(8.dp)) {
                        RoundCheck(item.id in run.checkedIds || (confirming && run.checklist.all { it.id == item.id || it.id in run.checkedIds }), p,
                            onClick = {
                                val checking = item.id !in run.checkedIds
                                requestChecklistChange(item.id, checking)
                            },
                            modifier = Modifier.size(27.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(item.title, color = p.text, fontSize = 13.sp, modifier = Modifier.weight(1f),
                            textDecoration = if (item.id in run.checkedIds || confirming) TextDecoration.LineThrough else null)
                        val reference = checklistReferenceComment(task, run, item)
                        if (reference.isNotBlank()) {
                            Spacer(Modifier.width(5.dp))
                            ChecklistReferenceButton(p, hasNote = true) {
                                openedReferenceId = item.id
                            }
                        }
                    }
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
    postponingDate?.let { start ->
        NavDateWheelDialog(
            initial = start, p = p, lang = lang,
            onDismiss = { postponingDate = null },
            onSelected = { day ->
                postponingDate = null
                requestMoveDate(day)
            },
        )
    }
    openedReferenceId?.let { id ->
        run.checklist.firstOrNull { it.id == id }?.let { item ->
            val note = checklistReferenceComment(task, run, item)
            if (note.isNotBlank()) {
                ChecklistReferenceDialog(
                    p = p, lang = lang, title = item.title,
                    note = note, editable = false,
                    onDismiss = { openedReferenceId = null },
                )
            }
        }
    }

    pendingChange?.let { change ->
        val itemName = change.first?.let { id ->
            run.checklist.firstOrNull { it.id == id }?.title
        } ?: task.title
        Dialog(onDismissRequest = { pendingChange = null }) {
            Column(
                Modifier.fillMaxWidth()
                    .navDialogSurface(p)
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(13.dp),
            ) {
                Text(localized("Другой день", "Different day", lang),
                    color = otherDayAccent, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Text(
                    if (change.second)
                        localized("Подтвердить выполнение?", "Confirm completion?", lang)
                    else localized("Снять отметку выполнения?", "Remove completion check?", lang),
                    color = p.text, fontSize = 20.sp,
                )
                Text(
                    localized("Выбранная дата: ", "Selected date: ", lang) +
                        navDate(viewingDate, lang, includeYear = true) +
                        "\n" + itemName,
                    color = p.muted, fontSize = 14.sp,
                )
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.navGlass(p, radius = 12.dp, role = GlassRole.CONTROL)
                        .glassClickable { pendingChange = null }
                        .padding(horizontal = 15.dp, vertical = 11.dp)) {
                        Text(localized("Отмена", "Cancel", lang), color = p.muted, fontSize = 13.sp)
                    }
                    Spacer(Modifier.width(9.dp))
                    Box(Modifier.navGlass(p, radius = 12.dp, role = GlassRole.CONTROL)
                        .glassClickable {
                            pendingChange = null
                            if (change.first == null) {
                                if (change.second) completeSimple()
                                else store.setSimpleComplete(run.key, false)
                            } else applyChecklistChange(change.first!!, change.second)
                        }
                        .padding(horizontal = 15.dp, vertical = 11.dp)) {
                        Text(localized("Подтвердить", "Confirm", lang),
                            color = p.blue, fontSize = 13.sp)
                    }
                }
            }
        }
    }
    if (closeDialog) {
        NavGlassAlertDialog(
            p = p,
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
