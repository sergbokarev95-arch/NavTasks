package com.navtasks.app

import android.app.DatePickerDialog
import com.kyant.backdrop.backdrops.layerBackdrop
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
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.shrinkHorizontally
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
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.abs
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import kotlinx.coroutines.Job
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
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
    // Night #09 Smoke Slate: true black canvas, solid neutral graphite surfaces.
    true, Color.Black, Color.Black, Color(0xFF1D2126),
    Color(0xFF282D33), Color(0xFF282D33), Color(0xFF40464D),
    Color(0xFFF6F7F8), Color(0xFFAAAFB5), Color(0xFF0091FF), Color(0xFF68CE77),
) else NavPalette(
    // Day #03 Neutral White: match HONOR Calendar's neutral gray canvas
    // (#F2F3F7), opaque white cards, and quiet neutral outlines.
    // Selection and semantic accent colors are unchanged.
    false, Color(0xFFF2F3F7), Color(0xFFF2F3F7), Color.White,
    Color.White, Color.White, Color(0xFFE0E2E8),
    Color(0xFF1C1C1E), Color(0xFF707076), Color(0xFF0088FF), Color(0xFF60C569),
)

/** HONOR-style success green, with readable contrast for text in Day mode. */
val NavPalette.successText: Color
    get() = if (dark) mint else Color(0xFF24834B)

/** Apple HIG semantic orange; foreground gets increased-contrast orange in Day. */
val NavPalette.systemOrange: Color
    get() = if (dark) Color(0xFFFF9230) else Color(0xFFFF8D28)

val NavPalette.warningText: Color
    get() = if (dark) systemOrange else Color(0xFFBE5300)

/** High-contrast Apple-blue variant for small readable text; system blue stays on icons. */
val NavPalette.blueText: Color
    get() = if (dark) Color(0xFF5CB8FF) else Color(0xFF0071E3)

/**
 * Opaque Apple-blue selection in both themes. Slightly deeper than system blue
 * so white 11sp labels pass WCAG AA. Not used by the Liquid Glass dock.
 */
val NavPalette.selectionFill: Color
    get() = Color(0xFF0071E3)

val NavPalette.selectionContent: Color
    get() = Color.White

/** Exact Apple semantic systemRed; use for destructive action labels and icons. */
val NavPalette.systemRed: Color
    get() = if (dark) Color(0xFFFF453A) else Color(0xFFFF3B30)

/** Higher-contrast light-mode red for small overdue/error status text. */
val NavPalette.dangerText: Color
    get() = if (dark) Color(0xFFFF5C52) else Color(0xFFC9342D)

/** A dock tap resets only its target tab; other tabs still restore via Back/swipe. */
internal fun primaryTabResetStateKey(profileId: String, tab: Int, epoch: Int): String =
    profileId + "/main-tab-" + (when (tab) {
        1 -> "works"
        3 -> "reference"
        else -> tab.toString()
    }) + "/dock-" + epoch

/** One prominent, consistent NavTasks wordmark, independent of the app's business screens. */
@Composable
fun NavBrandLabel(p: NavPalette, fontSize: androidx.compose.ui.unit.TextUnit = 19.sp) {
    Text(
        "NavTasks",
        color = p.blueText,
        fontSize = fontSize,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.35).sp,
        maxLines = 1,
    )
}

fun localized(ru: String, en: String, lang: UiLanguage) = if (lang == UiLanguage.RU) ru else en

fun navDate(date: LocalDate, lang: UiLanguage, includeYear: Boolean = false): String {
    val pattern = if (lang == UiLanguage.RU) {
        if (includeYear) "d MMMM yyyy" else "d MMMM"
    } else if (includeYear) "MMM d, yyyy" else "MMM d"
    return date.format(DateTimeFormatter.ofPattern(pattern, if (lang == UiLanguage.RU) Locale("ru") else Locale.ENGLISH))
}


/**
 * Select adjacent dock tab on a deliberate horizontal gesture.
 * Vertical scrolling, the Today seven-day strip and system edge-back gestures
 * retain priority. The standalone Calendar owns its own gestures.
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
    val activation = 48.dp.toPx()
    val axisLock = 12.dp.toPx()
    awaitEachGesture {
        // The Final pass lets scrolling children claim their own gestures first.
        // In particular, horizontal drags in Today's seven-day LazyRow do not
        // switch dock tabs. The rest of the page needs no additional swipe layer.
        val first = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Final)
        val inContent = first.position.x > edgeGuard &&
            first.position.x < size.width - edgeGuard
        var dx = 0f
        var dy = 0f
        var childClaimed = false
        var verticalGesture = false
        var switched = false
        while (true) {
            val event = awaitPointerEvent(pass = PointerEventPass.Final)
            val change = event.changes.firstOrNull { it.id == first.id } ?: break
            if (!change.pressed) break
            dx += change.position.x - change.previousPosition.x
            dy += change.position.y - change.previousPosition.y
            if (change.isConsumed) childClaimed = true
            // Once the user commits to vertical scrolling, never reinterpret
            // the remainder of that same gesture as a page swipe.
            if (!verticalGesture && abs(dy) > axisLock && abs(dy) > abs(dx) * 1.2f) {
                verticalGesture = true
            }
            if (inContent && !switched && !verticalGesture && !childClaimed) {
                val next = tabAfterHorizontalSwipe(tab, dx, dy, activation, false)
                if (next != tab) {
                    switched = true
                    // Switch while dragging rather than waiting for finger-up.
                    // The existing spring animation remains unchanged.
                    onSelect(next)
                }
            }
        }
    }
}

@Composable
fun NavTasksApp(
    store: TaskStore,
    settings: NavSettings,
    profiles: ProfileStore,
    openRequest: NavOpenRequest? = null,
    onOpenRequestHandled: (Long) -> Unit = {},
) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    // Four dock tabs: 0=Today, 1=Work, 2=Handover, 3=Reference.
    // Calendar opens as its own full-screen surface, not a fifth dock tab.
    // Kept across activity recreation so Android's Back gesture retraces navigation.
    var tabHistory by rememberSaveable { mutableStateOf("") }
    // Keep the selected calendar zoom/day and tab-specific scroll state intact
    // when Back closes Settings, the editor, or returns to a previous tab.
    val tabStateHolder = rememberSaveableStateHolder()
    // Only explicit bottom-dock taps reset transient scroll/expansion state.
    // Back, editors, full-screen Calendar and swipe gestures retain their old contract.
    var todayDockEpoch by rememberSaveable { mutableIntStateOf(0) }
    var worksDockEpoch by rememberSaveable { mutableIntStateOf(0) }
    var handoverDockEpoch by rememberSaveable { mutableIntStateOf(0) }
    // The Handover FAB is drawn by this persistent parent, not rebuilt
    // as the Handover tab enters AnimatedContent.
    var handoverAddRequest by remember { mutableIntStateOf(0) }
    var referenceDockEpoch by rememberSaveable { mutableIntStateOf(0) }
    // Active dock re-taps scroll without recreating the destination screen.
    var todayReturnTick by rememberSaveable { mutableIntStateOf(0) }
    var worksReturnTick by rememberSaveable { mutableIntStateOf(0) }
    var handoverReturnTick by rememberSaveable { mutableIntStateOf(0) }
    var referenceReturnTick by rememberSaveable { mutableIntStateOf(0) }
    // A former tab snapshot is not reused after an intentional dock reset.
    // Clear inactive generations so repeated taps do not accumulate saved UI state.
    LaunchedEffect(
        profiles.activeId, todayDockEpoch, worksDockEpoch,
        handoverDockEpoch, referenceDockEpoch,
    ) {
        listOf(todayDockEpoch, worksDockEpoch, handoverDockEpoch, referenceDockEpoch)
            .forEachIndexed { page, epoch ->
                if (epoch > 0) {
                    tabStateHolder.removeState(
                        primaryTabResetStateKey(profiles.activeId, page, epoch - 1)
                    )
                }
            }
    }
    var showSettings by rememberSaveable { mutableStateOf(false) }
    var showProfiles by rememberSaveable { mutableStateOf(false) }
    // Root-only temporary quick switch, independent of full profile management.
    var quickProfileOpen by rememberSaveable { mutableStateOf(false) }
    var showServices by rememberSaveable { mutableStateOf(false) }
    var showWorks by rememberSaveable { mutableStateOf(false) }
    // Transitional route: the Work tile opens the ORIGINAL TasksScreenV2.
    // Keep the old Tasks dock entry until the separate Reference-tab change.
    var showRoutineTasks by rememberSaveable { mutableStateOf(false) }
    var showCalendar by rememberSaveable { mutableStateOf(false) }
    var showWorkOrders by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val handoverStore = remember(context, profiles) { HandoverStore(context, profiles) }
    val serviceStore = remember(context, profiles) { ServiceStore(context, profiles) }
    val workOrderStore = remember(context, profiles) { WorkOrderStore(context, profiles) }
    // A Today reminder navigates to the exact entry in its owning module.
    var focusWorkOrderId by rememberSaveable { mutableStateOf<String?>(null) }
    var focusHistoryWorkOrderId by rememberSaveable { mutableStateOf<String?>(null) }
    var focusServiceId by rememberSaveable { mutableStateOf<String?>(null) }
    var editorOpen by remember { mutableStateOf(false) }
    var editorTask by remember { mutableStateOf<TaskTemplate?>(null) }
    var templateFilter by rememberSaveable { mutableStateOf(Frequency.DAILY) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var expandedRun by remember { mutableStateOf<String?>(null) }
    // Sent to Today only AFTER any owning-profile switch has finished.
    var pendingMissedDeepLink by remember { mutableStateOf(false) }
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    var previousToday by remember { mutableStateOf(LocalDate.now()) }
    var dismissedStorageWarning by rememberSaveable { mutableStateOf("") }

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

    fun onDockTabTap(next: Int) {
        // Re-tapping the active tab and switching via dock both open its root.
        // Never clear stored tasks, completion state, profile or scroll in dialogs.
        if (next == 0) {
            selectedDate = LocalDate.now()
            expandedRun = null
        }
        if (next == tab) {
            // Avoid remount: keep LazyColumn state alive while it animates.
            when (next) {
                0 -> todayReturnTick++
                1 -> worksReturnTick++
                2 -> handoverReturnTick++
                3 -> referenceReturnTick++
            }
        } else {
            // Cross-tab changes already use the existing horizontal transition.
            when (next) {
                0 -> todayDockEpoch++
                1 -> worksDockEpoch++
                2 -> handoverDockEpoch++
                3 -> referenceDockEpoch++
            }
        }
        openPrimaryTab(next)
    }

    // Same full store-switch contract used in ProfilesScreen, without data edits.
    fun activateQuickProfile(id: String) {
        if (id != profiles.activeId) {
            profiles.select(id)
            store.switchProfile()
            handoverStore.switchProfile()
            serviceStore.switchProfile()
            workOrderStore.switchProfile()
            tab = 0
            tabHistory = ""
            selectedDate = LocalDate.now()
            templateFilter = Frequency.DAILY
            expandedRun = null
            showRoutineTasks = false
        }
        quickProfileOpen = false
    }

    // Handle the profile and destination as ONE navigation transaction.
    // Never search the active profile for an item belonging to another ship.
    LaunchedEffect(openRequest?.serial) {
        val request = openRequest ?: return@LaunchedEffect
        val targetProfile = resolveNavProfileTarget(
            activeId = profiles.activeId,
            availableIds = profiles.profiles.map { it.id },
            requestedId = request.profileId,
        )
        if (targetProfile != null) {
            if (targetProfile != profiles.activeId) activateQuickProfile(targetProfile)
            // A notification may be opened while any nested screen is visible.
            // Close only transient navigation; never clear task or history data.
            quickProfileOpen = false
            editorOpen = false
            showSettings = false
            showProfiles = false
            showServices = false
            showWorks = false
            showRoutineTasks = false
            showCalendar = false
            showWorkOrders = false
            focusWorkOrderId = null
            focusHistoryWorkOrderId = null
            focusServiceId = null
            expandedRun = null
            pendingMissedDeepLink = false
            tabHistory = ""
            when (navOpenDestination(request)) {
                NavOpenDestination.TASK -> {
                    tab = 0
                    val run = request.runKey?.let { store.runs[it] }
                    selectedDate = run?.displayDate ?: LocalDate.now()
                    expandedRun = run?.key
                }
                NavOpenDestination.AMOS -> {
                    val orderId = request.workOrderId
                    if (orderId != null && workOrderStore.entries.any { it.id == orderId }) {
                        tab = 1
                        showWorks = true
                        focusWorkOrderId = orderId
                        showWorkOrders = true
                    } else {
                        tab = 0 // Deleted record: never open a similarly keyed foreign entry.
                        selectedDate = LocalDate.now()
                    }
                }
                NavOpenDestination.MISSED -> {
                    tab = 0
                    selectedDate = LocalDate.now()
                    pendingMissedDeepLink = true
                }
                NavOpenDestination.TODAY -> {
                    tab = 0
                    selectedDate = LocalDate.now()
                }
            }
        }
        // Deleted/unknown profile: consume stale intent without changing profile.
        onOpenRequestHandled(request.serial)
    }
    // The editor remains a normal TaskEditorScreen, but its outer presentation
    // is a drag-to-dismiss sheet. Only the handle/header owns vertical dismissal;
    // nested lists and text fields keep their original scroll/edit gestures.
    var editorSheetDragPx by remember { mutableFloatStateOf(0f) }
    val editorSheetScope = rememberCoroutineScope()
    val editorSheetReturn = remember { mutableStateOf<Job?>(null) }
    val editorSheetDragState = rememberDraggableState { delta ->
        editorSheetReturn.value?.cancel()
        editorSheetReturn.value = null
        editorSheetDragPx = (editorSheetDragPx + delta).coerceIn(0f, 1600f)
    }
    val dismissSheetDistance = with(LocalDensity.current) { 110.dp.toPx() }
    val onEditorSheetDragStopped: (Float) -> Unit = { velocity ->
        if (editorSheetDragPx >= dismissSheetDistance || velocity > 1150f) {
            editorOpen = false
        } else {
            val offset = editorSheetDragPx
            editorSheetReturn.value = editorSheetScope.launch {
                Animatable(offset).animateTo(
                    0f, spring(dampingRatio = .96f, stiffness = 680f),
                ) {
                    editorSheetDragPx = value
                }
                editorSheetReturn.value = null
            }
        }
    }
    LaunchedEffect(editorOpen) {
        if (!editorOpen) {
            // Keep the finger's last offset during the sheet's exit animation.
            delay(250)
            editorSheetReturn.value?.cancel()
            editorSheetReturn.value = null
            editorSheetDragPx = 0f
        } else {
            editorSheetReturn.value?.cancel()
            editorSheetReturn.value = null
            editorSheetDragPx = 0f
        }
    }

    val pinchEligible = !editorOpen && !showSettings && !showProfiles &&
        !showServices && !showWorks && !showRoutineTasks &&
        !showWorkOrders && !showCalendar

    // Android gesture navigation dispatches the system Back action here. At the root
    // of Today there is intentionally no interception so the user can leave the app.
    BackHandler(enabled = quickProfileOpen || editorOpen || showSettings || showProfiles || showServices || showWorks || showRoutineTasks || showWorkOrders || showCalendar ||
            profiles.active.kind == NavProfileKind.PERSONAL || tabHistory.isNotEmpty() || tab != 0) {
        when {
            quickProfileOpen -> quickProfileOpen = false
            editorOpen -> editorOpen = false
            showProfiles -> showProfiles = false
            showSettings -> showSettings = false
            showServices -> { showServices = false; focusServiceId = null }
            showWorkOrders -> {
                showWorkOrders = false
                focusWorkOrderId = null
                focusHistoryWorkOrderId = null
            }
            showRoutineTasks -> showRoutineTasks = false
            showWorks -> showWorks = false
            showCalendar -> showCalendar = false
            profiles.active.kind == NavProfileKind.PERSONAL -> showProfiles = true
            tabHistory.isNotEmpty() -> {
                tab = tabHistory.last().digitToInt()
                tabHistory = tabHistory.dropLast(1)
            }
            tab != 0 -> tab = 0
        }
    }

    MaterialTheme(colorScheme = scheme) {
        // Popup is outside the blurred app root so profile choices stay sharp.
        Box(Modifier.fillMaxSize()) {
        Box(
            Modifier.fillMaxSize()
                .quickProfilePinchGesture(
                    enabled = pinchEligible && !quickProfileOpen,
                    onDetected = { quickProfileOpen = true },
                )
                .blur(when {
                    quickProfileOpen -> 12.dp
                    editorOpen -> 4.dp // Blur underlying page ONCE, never the popup.
                    else -> 0.dp
                })
        ) {
            NavGlassBackdrop(palette)
            // The underlying page stays mounted while the editor sheet slides
            // from the Add control; this also preserves list/calendar scroll.
            if (showProfiles) {
                ProfilesScreen(profiles, store, handoverStore, serviceStore, workOrderStore, palette, lang,
                    onBack = {
                        showProfiles = false
                        showCalendar = false
                        tab = 0
                        tabHistory = ""
                        selectedDate = LocalDate.now()
                        templateFilter = Frequency.DAILY
                        expandedRun = null
                    },
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
                    onBack = { showServices = false; focusServiceId = null },
                    focusServiceId = focusServiceId)
            } else if (showWorkOrders) {
                WorkOrdersScreen(workOrderStore, store, settings, palette, lang,
                    onBack = {
                        showWorkOrders = false
                        focusWorkOrderId = null
                        focusHistoryWorkOrderId = null
                    },
                    focusWorkOrderId = focusWorkOrderId,
                    focusHistoryWorkOrderId = focusHistoryWorkOrderId)
            } else if (showCalendar) {
                // A fresh Backdrop records the Plan page; the Back button is a
                // sibling ABOVE that capture, avoiding self-refraction.
                val planBackdrop = if (android.os.Build.VERSION.SDK_INT >= 33)
                    com.kyant.backdrop.backdrops.rememberLayerBackdrop() else null
                Box(Modifier.fillMaxSize()) {
                Column(Modifier.fillMaxSize().then(
                    if (planBackdrop != null) Modifier.layerBackdrop(planBackdrop) else Modifier
                ).statusBarsPadding().navigationBarsPadding()) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (planBackdrop != null) Spacer(Modifier.size(44.dp))
                        else NavGlassCircleButton(palette, "back") { showCalendar = false }
                        Spacer(Modifier.width(12.dp))
                        Text(localized("План", "Plan", lang),
                            modifier = Modifier.weight(1f),
                            color = palette.text, fontSize = 24.sp)
                    }
                    Box(Modifier.fillMaxWidth().weight(1f)) {
                        // Reuse the former Plan tab's saveable key, preserving
                        // year/month/day when Calendar is closed and reopened.
                        tabStateHolder.SaveableStateProvider(
                            profiles.activeId + "/main-tab-1"
                        ) {
                            PlanScreenV2(
                                store, now.toLocalDate(), palette, lang,
                                onEdit = { editorTask = it; editorOpen = true },
                                workOrders = workOrderStore, services = serviceStore,
                            )
                        }
                    }
                }
                if (planBackdrop != null) {
                    Box(Modifier.align(Alignment.TopStart).statusBarsPadding()
                        .padding(start = 16.dp, top = 9.dp)) {
                        NavLiquidGlassCircleButton(palette, planBackdrop, "back") {
                            showCalendar = false
                        }
                    }
                }
                }
            } else if (showRoutineTasks) {
                // Preserve routine-list scroll/expanded child state while the
                // existing TaskEditor temporarily replaces this full-screen page.
                // Profile-specific key prevents cross-profile UI state bleed.
                tabStateHolder.SaveableStateProvider(
                    profiles.activeId + "/work-routines"
                ) {
                    RoutinesModuleScreen(
                        store = store,
                        filter = templateFilter,
                        onFilter = { templateFilter = it },
                        p = palette, lang = lang,
                        onBack = { showRoutineTasks = false },
                        onEdit = { editorTask = it; editorOpen = true },
                        onAdd = {
                            editorTask = null
                            editorOpen = true
                        },
                    )
                }
            } else if (showWorks) {
                WorksHubScreen(serviceStore, workOrderStore, palette, lang,
                    taskCount = store.templates.size,
                    onBack = { showWorks = false },
                    onTasks = { showRoutineTasks = true },
                    onServices = { focusServiceId = null; showServices = true },
                    onAmos = {
                        focusWorkOrderId = null
                        focusHistoryWorkOrderId = null
                        showWorkOrders = true
                    })
            } else {
                val hazeState = remember { dev.chrisbanes.haze.HazeState() }
                // As in the approved Lab: record the original page, not Haze's
                // output, and keep the dock outside the captured layer.
                val glassBackdrop = if (android.os.Build.VERSION.SDK_INT >= 33)
                    com.kyant.backdrop.backdrops.rememberLayerBackdrop() else null
                Box(Modifier.fillMaxSize()) {
                    Box(
                        Modifier.fillMaxSize().then(
                            if (glassBackdrop != null)
                                Modifier.layerBackdrop(glassBackdrop) else Modifier
                        )
                    ) {
                    // Content paints to the bottom edge. Navigation is an overlay,
                    // NOT a Scaffold bottomBar which would reserve an opaque footer.
                    Column(Modifier.fillMaxSize().statusBarsPadding().hazeSource(hazeState)
                        .primaryTabSwipe(tab, ::openPrimaryTab)) {
                        Row(
                            Modifier.fillMaxWidth().padding(start = 18.dp, end = 14.dp, top = 14.dp, bottom = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Row(
                                    Modifier.glassClickable { showProfiles = true }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        profiles.active.title, color = palette.text,
                                        fontSize = 16.sp, maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false),
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    NavGlassGlyph("chevron-down", palette.text, Modifier.size(14.dp))
                                }
                                Text(
                                    when (tab) {
                                        0 -> localized("Сегодня", "Today", lang)
                                        1 -> localized("Работы", "Work", lang)
                                        2 -> "Handover"
                                        else -> localized("Справочник", "Reference", lang)
                                    }, fontSize = 26.sp, color = palette.text, fontWeight = FontWeight.Medium,
                                )
                            }
                            if (glassBackdrop != null) Spacer(Modifier.size(44.dp))
                            else NavGlassCircleButton(palette, "settings") { showSettings = true }
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
                                // Every dock tap starts a fresh visible page instead
                                // of resurrecting an old scrolled SaveableStateProvider.
                                primaryTabResetStateKey(
                                    profiles.activeId, page,
                                    when (page) {
                                        0 -> todayDockEpoch
                                        1 -> worksDockEpoch
                                        2 -> handoverDockEpoch
                                        else -> referenceDockEpoch
                                    },
                                )
                            ) {
                                when (page) {
                                    0 -> TodayScreenV2(store, now, selectedDate, { selectedDate = it }, palette, lang,
                                        expandedRun, { expandedRun = it },
                                        workOrders = workOrderStore, services = serviceStore,
                                        onAmos = { id ->
                                            focusWorkOrderId = id
                                            focusHistoryWorkOrderId = null
                                            showWorks = true
                                            showWorkOrders = true
                                        },
                                        onAmosHistory = { id ->
                                            focusWorkOrderId = null
                                            focusHistoryWorkOrderId = id
                                            showWorks = true
                                            showWorkOrders = true
                                        },
                                        onServices = { id ->
                                            focusServiceId = id
                                            showWorks = true
                                            showServices = true
                                        },
                                        openMissed = pendingMissedDeepLink,
                                        onOpenMissedHandled = { pendingMissedDeepLink = false },
                                        smoothReturnTick = todayReturnTick)
                                    1 -> WorksHubScreen(
                                        serviceStore, workOrderStore, palette, lang,
                                        taskCount = store.templates.size,
                                        onBack = { openPrimaryTab(0) },
                                        onTasks = { showRoutineTasks = true },
                                        onServices = { focusServiceId = null; showServices = true },
                                        onAmos = {
                        focusWorkOrderId = null
                        focusHistoryWorkOrderId = null
                        showWorkOrders = true
                    },
                                        embeddedInTab = true,
                                        smoothReturnTick = worksReturnTick,
                                    )
                                    2 -> HandoverScreen(
                                        handoverStore, store, palette, lang,
                                        onBack = { openPrimaryTab(0) }, embeddedInTab = true,
                                        smoothReturnTick = handoverReturnTick,
                                        externalAddRequest = handoverAddRequest,
                                        onExternalAddHandled = { handoverAddRequest = 0 },
                                    )
                                    else -> ReferenceHubScreen(palette, lang,
                                        profileId = profiles.activeId,
                                        smoothReturnTick = referenceReturnTick)
                                }
                            }
                        }
                    }
                    } // End original page capture before any floating glass.
                    if (glassBackdrop != null) {
                        Box(Modifier.align(Alignment.TopEnd).statusBarsPadding()
                            .padding(end = 14.dp, top = 14.dp)) {
                            NavLiquidGlassCircleButton(palette, glassBackdrop, "settings") {
                                showSettings = true
                            }
                        }
                    }
                    if (tab == 0) {
                        // Today: Calendar on the LEFT, Add task on the RIGHT.
                        // Both reuse the approved 54dp Liquid Glass action optics,
                        // exact vertical inset and one captured backdrop.
                        Box(
                            Modifier.align(Alignment.BottomStart).navigationBarsPadding()
                                .padding(start = 19.dp, bottom = 83.dp),
                        ) {
                            if (glassBackdrop != null) {
                                NavLiquidGlassCalendarButton(palette, glassBackdrop) {
                                    showCalendar = true
                                }
                            } else {
                                NavGlassCalendarButton(palette, hazeState) {
                                    showCalendar = true
                                }
                            }
                        }
                        Box(
                            Modifier.align(Alignment.BottomEnd).navigationBarsPadding()
                                .padding(end = 19.dp, bottom = 83.dp),
                        ) {
                            if (glassBackdrop != null) {
                                NavLiquidGlassAddButton(palette, glassBackdrop) {
                                    editorTask = null
                                    templateFilter = Frequency.DAILY
                                    editorOpen = true
                                }
                            } else {
                                NavGlassAddButton(palette, hazeState) {
                                    editorTask = null
                                    templateFilter = Frequency.DAILY
                                    editorOpen = true
                                }
                            }
                        }
                    }
                    if (tab == 2) {
                        // Same ready optical source and inset as Today's action.
                        Box(
                            Modifier.align(Alignment.BottomEnd).navigationBarsPadding()
                                .padding(end = 19.dp, bottom = 83.dp),
                        ) {
                            if (glassBackdrop != null) {
                                NavLiquidGlassAddButton(palette, glassBackdrop) {
                                    handoverAddRequest++
                                }
                            } else {
                                NavGlassAddButton(palette, hazeState) {
                                    handoverAddRequest++
                                }
                            }
                        }
                    }
                    if (glassBackdrop != null) {
                        NavLiquidGlassBottomBar(
                            p = palette, selected = tab, lang = lang,
                            backdrop = glassBackdrop,
                            // Keep the laboratory's 15dp horizontal clearance and 56dp
                            // bar. Lower the whole dock by 4dp, leaving more
                            // balanced separation from Android's gesture pill.
                            modifier = Modifier.align(Alignment.BottomCenter)
                                .navigationBarsPadding()
                                .padding(horizontal = 15.dp)
                                .padding(top = 8.dp, bottom = 4.dp),
                            onSelect = ::onDockTabTap,
                        )
                    } else {
                        // Android API 31/32 keeps the original, fully tested dock.
                        NavGlassBottomBar(
                            p = palette, selected = tab, lang = lang,
                            modifier = Modifier.align(Alignment.BottomCenter),
                            hazeState = hazeState,
                            onSelect = ::onDockTabTap,
                        )
                    }
                }

            }
        }

        // Mirror the reference's bottom-up editor entrance without replacing
        // any business state or touching the four-tab Liquid Glass dock.
        AnimatedVisibility(
            visible = editorOpen,
            modifier = Modifier.fillMaxSize(),
            enter = fadeIn(tween(150)),
            exit = fadeOut(tween(175)),
            label = "taskEditorScrim",
        ) {
            Box(
                Modifier.fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.36f))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                    ) { /* Preserve unsaved editor draft; dismiss via Back. */ },
            )
        }
        AnimatedVisibility(
            visible = editorOpen,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(dampingRatio = .92f, stiffness = 620f),
            ) + fadeIn(tween(115)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(220),
            ) + fadeOut(tween(140)),
            label = "taskEditorBottomSheet",
        ) {
            val sheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            Box(
                Modifier.fillMaxWidth().fillMaxHeight(.94f)
                    .offset { IntOffset(0, editorSheetDragPx.roundToInt()) }
                    .shadow(16.dp, sheetShape)
                    .clip(sheetShape)
                    .background(palette.background, sheetShape)
                    .border(1.dp, palette.border.copy(alpha = .65f), sheetShape),
            ) {
                TaskEditorScreen(
                    initial = editorTask, defaultFrequency = templateFilter,
                    lang = lang, palette = palette, store = store,
                    onBack = { editorOpen = false },
                    onSave = { store.upsert(it); editorOpen = false },
                    sheetMode = true,
                    sheetDragState = editorSheetDragState,
                    onSheetDragStopped = onEditorSheetDragStopped,
                )
                // Broad 112x30dp hit area for the drag handle, independent of
                // the title's taps, Save button and the editor's inner scrolling.
                Box(
                    Modifier.align(Alignment.TopCenter)
                        .size(width = 112.dp, height = 30.dp)
                        .draggable(
                            state = editorSheetDragState,
                            orientation = Orientation.Vertical,
                            onDragStopped = { velocity -> onEditorSheetDragStopped(velocity) },
                        ),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    Box(
                        Modifier.padding(top = 8.dp)
                            .size(width = 42.dp, height = 4.dp)
                            .background(palette.muted.copy(alpha = .58f), CircleShape),
                    )
                }
            }
        }
        // Rendered after underlying destinations: Back closes the editor first,
        // rather than letting an underlying Calendar/Works BackHandler fire.
        BackHandler(enabled = editorOpen) { editorOpen = false }

        if (quickProfileOpen) {
            NavQuickProfilePicker(
                profiles = profiles.profiles,
                activeId = profiles.activeId,
                p = palette,
                lang = lang,
                onDismiss = { quickProfileOpen = false },
                onSelect = ::activateQuickProfile,
            )
        }
        val storageWarning = store.storageIssue ?: handoverStore.storageIssue
        val warningKey = profiles.activeId + ":" + (storageWarning ?: "")
        if (storageWarning != null && dismissedStorageWarning != warningKey) {
            NavGlassAlertDialog(
                p = palette,
                onDismissRequest = { dismissedStorageWarning = warningKey },
                title = { androidx.compose.material3.Text(localized(
                    "Данные профиля защищены", "Profile data protected", lang)) },
                text = { androidx.compose.material3.Text(storageWarning) },
                confirmButton = {
                    androidx.compose.material3.TextButton(onClick = {
                        dismissedStorageWarning = warningKey
                        showProfiles = true
                    }) {
                        androidx.compose.material3.Text(localized(
                            "Восстановить из копии", "Restore backup", lang))
                    }
                },
                dismissButton = {
                    androidx.compose.material3.TextButton(onClick = {
                        dismissedStorageWarning = warningKey
                    }) {
                        androidx.compose.material3.Text(localized(
                            "Только просмотр", "View only", lang))
                    }
                },
            )
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
fun RoundCheck(
    checked: Boolean, p: NavPalette, onClick: () -> Unit,
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource? = null,
) {
    Box(
        modifier.size(44.dp).clip(CircleShape)
            .glassClickable(interactionSource = interactionSource, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier.size(NavTodayVisualTokens.checkDiameterDp.dp)
                .background(if (checked) p.mint else Color.Transparent, CircleShape)
                .border(NavTodayVisualTokens.checkStrokeDp.dp,
                    if (checked) p.mint else NavTodayVisualTokens.uncheckedInk(p.dark), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (checked) NavGlassGlyph(
                "check", if (p.dark) Color(0xFF103F49) else Color.White,
                Modifier.size(NavTodayVisualTokens.checkedGlyphDp.dp),
            )
        }
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
    onAmos: (String) -> Unit,
    onAmosHistory: (String) -> Unit,
    onServices: (String) -> Unit,
    openMissed: Boolean = false,
    onOpenMissedHandled: () -> Unit = {},
    smoothReturnTick: Int = 0,
) {
    var showMissed by rememberSaveable { mutableStateOf(false) }
    var showCompleted by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(openMissed) {
        if (openMissed) {
            showMissed = true
            onOpenMissedHandled()
        }
    }
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
    // Exiting the last missed task must not leave the screen in an empty backlog mode.
    LaunchedEffect(showMissed, missed.size) {
        if (showMissed && !todayMissedChipVisible(missed.size)) showMissed = false
    }
    val browsingOtherDay = !showMissed && selectedDay != currentDay
    val extraDue = groupTodayByFrequency(rows).filter { it.first != Frequency.DAILY }
    val dayWorkOrders = workOrders.entries
    val dayServices = services.entries
    val completedAmosWork = amosCompletedOnDay(dayWorkOrders, selectedDay)
    val completedServiceWork = serviceCompletedOnDay(dayServices, selectedDay)
    val completedWorkCount = completedAmosWork.size + completedServiceWork.size
    val completedTotal = complete.size + completedWorkCount
    val hasWork = dayWorkOrders.any { amosVisibleOnDay(it, selectedDay) } ||
        dayServices.any { serviceReminderVisibleOnDay(it, selectedDay) }

    val daysScroll = rememberLazyListState()
    val taskListScroll = rememberLazyListState()
    // Animate both independent axes on an active-tab retap. Initial entry
    // still starts at item zero without an unnecessary scroll.
    LaunchedEffect(smoothReturnTick) {
        if (smoothReturnTick > 0) {
            showMissed = false
            showCompleted = false
            kotlinx.coroutines.coroutineScope {
                launch { daysScroll.animateScrollToItem(0) }
                launch { taskListScroll.animateScrollToItem(0) }
            }
        }
    }
    Column {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(NavTodayVisualTokens.cardSurface(p.dark), RoundedCornerShape(20.dp))
            .padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(navDate(selectedDay, lang, true), color = p.muted, fontSize = 12.sp)
                    Text(
                        if (browsingOtherDay) localized("Прогресс выбранного дня", "Selected day's progress", lang)
                        else if (hasWork || completedWorkCount > 0)
                            localized("Прогресс задач", "Task progress", lang)
                        else localized("Прогресс дня", "Daily progress", lang),
                        color = p.text, fontSize = 15.sp,
                    )
                }
                Text("${complete.size}/${rows.size}", color = NavTodayVisualTokens.accentInk(p.dark), fontSize = 23.sp,
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
        // Dates use all available width whenever there is no missed backlog.
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
            AnimatedVisibility(
                visible = todayMissedChipVisible(missed.size),
                enter = fadeIn(tween(180)) + expandHorizontally(
                    expandFrom = Alignment.Start, animationSpec = tween(180)),
                exit = fadeOut(tween(160)) + shrinkHorizontally(
                    shrinkTowards = Alignment.Start, animationSpec = tween(180)),
            ) {
                Row(Modifier.fillMaxHeight(), verticalAlignment = Alignment.CenterVertically) {
                    MissedCountChip(missed.size, showMissed, p, lang,
                        modifier = Modifier.width(105.dp).fillMaxHeight()) {
                        showMissed = true
                        onExpanded(null)
                    }
                    Spacer(Modifier.width(7.dp))
                }
            }
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
                        p, weekend = calendarIsWeekend(day),
                        modifier = Modifier.width(55.dp).fillMaxHeight(),
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
                        Modifier.clip(RoundedCornerShape(12.dp))
                            .background(NavTodayVisualTokens.cardSurface(p.dark), RoundedCornerShape(12.dp))
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
            val amber = p.warningText
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(NavTodayVisualTokens.cardSurface(p.dark), RoundedCornerShape(13.dp))
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
            state = taskListScroll,
            // Content is drawn behind floating glass. Reserve the gesture inset,
            // button offset + diameter and 20dp breathing room at list end.
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                bottom = navFloatingActionBottomPadding(embeddedInTab = true),
            ),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            if (showMissed) {
                item { Text(localized("Незакрытые пропуски", "Unfinished overdue tasks", lang),
                    color = p.muted, fontSize = 13.sp, modifier = Modifier.padding(vertical = 6.dp)) }
                if (missed.isEmpty()) item { NavPanel(p) { Text(localized("Пропущенных задач нет", "No missed tasks", lang), color = p.successText) } }
                missed.groupBy { it.second.dueDate }.forEach { (originalDate, group) ->
                    item(key = "missed-date-$originalDate") {
                        Text(navDate(originalDate, lang, true), color = p.blueText, fontSize = 12.sp,
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
                            day = selectedDay,
                            orders = dayWorkOrders, services = dayServices,
                            p = p, lang = lang,
                            onAmos = onAmos, onServices = onServices,
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
                if (incomplete.isEmpty() && !hasWork && completedTotal == 0) item {
                    NavPanel(p) { Text(localized("На этот день всё выполнено ✓", "All done for this day ✓", lang), color = p.successText) }
                }
                if (completedTotal > 0) item {
                    val completedArrow by animateFloatAsState(
                        targetValue = if (showCompleted) 180f else 0f,
                        animationSpec = spring(dampingRatio = 1f, stiffness = 550f),
                        label = "completedArrow",
                    )
                    Column(Modifier.fillMaxWidth()) {
                        Column(
                            Modifier.fillMaxWidth()
                                .navGlass(p, radius = 17.dp, role = GlassRole.PANEL)
                                .border(1.dp, p.warningText.copy(alpha = 0.52f), RoundedCornerShape(17.dp))
                                .background(p.warningText.copy(alpha = if (p.dark) 0.10f else 0.055f),
                                    RoundedCornerShape(17.dp))
                                .glassClickable { showCompleted = !showCompleted }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("✓", color = p.warningText, fontSize = 19.sp)
                                Spacer(Modifier.width(9.dp))
                                Text(localized("Выполнено", "Completed", lang), color = p.warningText,
                                    modifier = Modifier.weight(1f), fontSize = 15.sp)
                                Text(completedTotal.toString(), color = p.warningText, fontSize = 14.sp)
                                Spacer(Modifier.width(9.dp))
                                NavGlassGlyph(
                                    "chevron-down", p.warningText,
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
                                WorkAgendaCompletedTodaySection(
                                    completedAmos = completedAmosWork,
                                    completedServices = completedServiceWork,
                                    p = p, lang = lang,
                                    onAmosHistory = onAmosHistory,
                                    onServices = onServices,
                                )
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
    Frequency.EXTRA -> p.warningText
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
    top: String, bottom: String, selected: Boolean, p: NavPalette,
    weekend: Boolean = false,
    modifier: Modifier = Modifier, action: () -> Unit,
) {
    val shape = RoundedCornerShape(13.dp)
    Column(
        modifier.clip(shape)
            .background(NavTodayVisualTokens.cardSurface(p.dark), shape)
            .then(if (selected) Modifier.border(
                NavTodayVisualTokens.selectedDateRimDp.dp,
                NavTodayVisualTokens.accentInk(p.dark), shape,
            ) else Modifier)
            .glassClickable(onClick = action).padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Trial: only the selected rim is coral; the neutral surface and
        // date ink (including weekend red) are identical to unselected dates.
        val topColor = if (weekend) calendarWeekendInk(p) else p.muted
        val bottomColor = if (weekend) calendarWeekendInk(p) else p.text
        Text(top, color = topColor, fontSize = 11.sp, maxLines = 1)
        Text(bottom, color = bottomColor, fontSize = 16.sp,
            fontWeight = FontWeight.Medium)
    }
}

/** A zero backlog occupies no space in the Today date selector. */
internal fun todayMissedChipVisible(missedCount: Int): Boolean = missedCount > 0

/** Red missed-count control only while backlog > 0. */
@Composable
private fun MissedCountChip(
    count: Int, selected: Boolean, p: NavPalette, lang: UiLanguage,
    modifier: Modifier = Modifier, action: () -> Unit,
) {
    val shape = RoundedCornerShape(13.dp)
    Column(
        modifier.clip(shape)
            .background(if (p.dark) Color(0x843B1C29) else Color(0xFFFFE9E9), shape)
            .border(1.dp,
                if (p.dark) Color(0xD0E97A83)
                else if (selected) Color(0xFFCF565B) else Color(0xFFEDA8A8),
                shape)
            .glassClickable(onClick = action)
            .padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(localized("Пропущенные", "Missed", lang),
            color = p.dangerText, fontSize = 11.sp, maxLines = 1)
        Text(count.toString(), color = p.dangerText, fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold)
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
    val otherDayAccent = p.warningText
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
    val amount by animateFloatAsState(
        if (exiting) 1f else 0f,
        tween(NavTaskCompletionMotion.exitMillis),
        label = "taskComplete",
    )
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
        // ONE solid green confirmation, visible before the card starts moving.
        // This is not an animation: no flashing, bouncing or color oscillation.
        flash.snapTo(1f)
        delay(NavTaskCompletionMotion.greenHoldMillis)
    }
    fun completeSimple() {
        if (confirming || exiting) return
        confirming = true
        scope.launch {
            showConfirmation()
            exiting = true
            delay(NavTaskCompletionMotion.exitMillis.toLong())
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
                delay(NavTaskCompletionMotion.exitMillis.toLong())
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
    val shape = RoundedCornerShape(NavTodayVisualTokens.cardRadiusDp.dp)
    Column(
        Modifier.fillMaxWidth().graphicsLayer {
            // Straight down only: the old up-kick and stretch are removed.
            // Green is already fully visible from the previous hold phase.
            alpha = 1f - amount * 0.85f
            translationY = amount * NavTaskCompletionMotion.dropDistanceDp.dp.toPx()
            scaleX = pressScale
            scaleY = pressScale
        }.clip(shape)
            .background(NavTodayVisualTokens.cardSurface(p.dark), shape)
            .then(
                if (isOtherDay) Modifier.border(
                    1.dp, otherDayAccent.copy(alpha = 0.58f), shape
                ) else Modifier
            )
            .background(p.mint.copy(alpha = flash.value * 0.36f), shape),
    ) {
        // Preserve 44dp control hit targets while reducing dead vertical space.
        Row(Modifier.padding(start = 11.dp, end = 6.dp, top = 3.dp, bottom = 3.dp),
            verticalAlignment = Alignment.CenterVertically) {
            if (run.checklist.isNotEmpty()) {
                Box(
                    Modifier.size(44.dp).clip(CircleShape)
                        .glassClickable(interactionSource = headerInteraction, onClick = onToggleExpand),
                    contentAlignment = Alignment.Center,
                ) {
                    NavGlassGlyph(
                        "chevron-down",
                        if (run.completed) p.muted else p.text,
                        Modifier.size(22.dp).rotate(disclosureRotation),
                    )
                }
            } else {
                RoundCheck(run.completed || confirming, p,
                    onClick = ::requestSimpleChange,
                    interactionSource = headerInteraction)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f).glassClickable(interactionSource = headerInteraction) {
                if (run.checklist.isNotEmpty()) onToggleExpand()
                else requestSimpleChange()
            }) {
                Text(
                    buildAnnotatedString {
                        append(task.title)
                        if (run.checklist.isNotEmpty()) {
                            append("  ")
                            withStyle(SpanStyle(color = if (run.completed) p.muted else p.text, fontSize = 11.sp)) {
                                append("${run.checkedCount}/${run.checklist.size}")
                            }
                        }
                    },
                    color = if (run.completed) p.muted else p.text,
                    fontSize = 14.sp, maxLines = 2,
                    textDecoration = if (run.completed || confirming) TextDecoration.LineThrough else null,
                )
                if (run.transferred || overdue || task.deadlineTime != null) {
                    Spacer(Modifier.height(2.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (run.transferred) Text(localized("Перенесено", "Moved", lang), color = p.successText, fontSize = 11.sp)
                        if (overdue) Text(navDate(run.dueDate, lang), color = p.dangerText, fontSize = 11.sp)
                        if (task.deadlineTime != null) Text(task.deadlineTime.toString(), color = p.muted, fontSize = 11.sp)
                    }
                }
            }
            Box {
                NavGlassIconAction(
                    "more", p.muted, localized("Действия с задачей", "Task actions", lang),
                ) { menuOpen = true }
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
            Column(Modifier.padding(start = 51.dp, end = 13.dp, bottom = 9.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)) {
                HorizontalDivider(color = p.border)
                run.checklist.forEach { item ->
                    Row(verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().navGlass(p, radius = 16.dp,
                            role = GlassRole.CONTROL, elevation = 0.dp)
                            .heightIn(min = 46.dp)
                            .padding(horizontal = 7.dp, vertical = 1.dp)) {
                        RoundCheck(item.id in run.checkedIds || (confirming && run.checklist.all { it.id == item.id || it.id in run.checkedIds }), p,
                            onClick = {
                                val checking = item.id !in run.checkedIds
                                requestChecklistChange(item.id, checking)
                            },
                            modifier = Modifier)
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
                    note = note,
                    noteHtml = checklistReferenceHtml(task, run, item),
                    editable = false,
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
                            color = p.blueText, fontSize = 13.sp)
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
                OutlinedTextField(closeReason, { closeReason = it },
                    keyboardOptions = NavSentenceKeyboard, modifier = Modifier.fillMaxWidth(),
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
