package com.navtasks.app

import dev.chrisbanes.haze.hazeSource
import com.kyant.backdrop.backdrops.layerBackdrop

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.io.IOException
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.blur
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Personal-only material: nested notes must remain distinct from their section. */
internal object NavPersonalVisualTokens {
    const val headerTopDp = 9f

    fun cardSurface(dark: Boolean, nested: Boolean): Color =
        if (nested) {
            if (dark) Color(0xFF2A2A2C) else Color(0xFFF2F2F5)
        } else NavTodayVisualTokens.cardSurface(dark)
}

internal fun Modifier.personalNoteCard(
    p: NavPalette, radius: androidx.compose.ui.unit.Dp, nested: Boolean = false,
): Modifier {
    val shape = RoundedCornerShape(radius)
    return clip(shape).background(NavPersonalVisualTokens.cardSurface(p.dark, nested), shape)
}

/** Active notes stay at the top. Resolved/struck entries remain visible at the end. */
internal fun orderedHandoverEntries(entries: List<HandoverEntry>): List<HandoverEntry> =
    entries.filter { it.mark == HandoverMark.OPEN } +
        entries.filter { it.mark != HandoverMark.OPEN }

/** Handover is a notebook: legacy completion states are not shown or reordered. */
internal fun handoverNotebookEntries(entries: List<HandoverEntry>): List<HandoverEntry> = entries

/** Exactly one Handover section can be open; tapping it a second time closes it. */
internal fun nextExpandedHandoverSection(current: String?, tapped: String): String? =
    if (current == tapped) null else tapped

/** Compact Handover entries are UI-only: no task state or saved note data changes. */
internal fun nextExpandedHandoverEntry(current: String?, tapped: String): String? =
    if (current == tapped) null else tapped

/** Keep Personal notes fully visible; search opens note matches automatically. */
internal fun handoverEntryShowsNotes(
    personalMode: Boolean, expandedEntryId: String?, entry: HandoverEntry, query: String,
): Boolean = personalMode || expandedEntryId == entry.id ||
    (query.isNotBlank() && navSearchRanges(entry.notes, query).isNotEmpty())

@Composable
fun HandoverScreen(
    handover: HandoverStore,
    tasks: TaskStore,
    p: NavPalette,
    lang: UiLanguage,
    onBack: () -> Unit,
    personalMode: Boolean = false,
    embeddedInTab: Boolean = false,
    smoothReturnTick: Int = 0,
    externalAddRequest: Int = 0,
    onExternalAddHandled: () -> Unit = {},
) {
    val context = LocalContext.current
    val exportScope = rememberCoroutineScope()
    var exportOpen by remember { mutableStateOf(false) }
    var exportSnapshot by remember { mutableStateOf<List<HandoverSection>?>(null) }
    var exportFeedback by remember { mutableStateOf<String?>(null) }
    var exportFailed by remember { mutableStateOf(false) }
    var exportRunning by remember { mutableStateOf(false) }

    fun exportTo(uri: android.net.Uri?, isWord: Boolean) {
        val snapshot = exportSnapshot
        exportSnapshot = null
        if (uri == null || snapshot == null) return
        exportScope.launch {
            exportRunning = true
            val outcome = withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openOutputStream(uri)?.use { stream ->
                        if (isWord) HandoverDocxExport.export(snapshot, stream)
                        else HandoverPdfExport.export(snapshot, stream)
                    } ?: throw IOException("Unable to save Handover")
                }
            }
            outcome.onSuccess {
                exportFailed = false
                exportFeedback = localized(
                    "Handover сохранён. Файл можно передать на компьютер или iPhone.",
                    "Handover saved. The file can be sent to a PC or iPhone.", lang)
            }.onFailure {
                exportFailed = true
                exportFeedback = it.localizedMessage ?: "Handover export failed"
            }
            exportRunning = false
        }
    }
    val wordExport = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document")) { uri ->
        exportTo(uri, isWord = true)
    }
    val pdfExport = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        exportTo(uri, isWord = false)
    }
    val listState = rememberLazyListState()
    val matchMarginPx = with(LocalDensity.current) { 42.dp.toPx() }
    var listViewportCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val hazeState = remember { dev.chrisbanes.haze.HazeState() }
    // Page content only: floating buttons stay sibling to the recorder.
    val actionBackdrop = if (android.os.Build.VERSION.SDK_INT >= 33)
        com.kyant.backdrop.backdrops.rememberLayerBackdrop() else null
    var searchOpen by rememberSaveable { mutableStateOf(false) }
    var searchText by rememberSaveable { mutableStateOf("") }
    val query = if (searchOpen) navEffectiveSearchQuery(searchText) else ""
    val shownSections = searchHandoverSections(
        handover.sections, if (searchOpen) query else "")
    val searchResults = if (searchOpen && query.isNotEmpty())
        navHandoverSearchHits(shownSections, query) else emptyList()
    var searchResultIndex by rememberSaveable { mutableIntStateOf(0) }
    var searchNavigationSerial by remember { mutableIntStateOf(0) }
    var entryRevealSerial by remember { mutableIntStateOf(0) }
    val focusedSearchHit = searchResults.getOrNull(searchResultIndex)
    LaunchedEffect(query) { searchResultIndex = 0 }
    LaunchedEffect(searchNavigationSerial) {
        if (searchNavigationSerial > 0) {
            val hit = searchResults.getOrNull(searchResultIndex)
            if (hit != null) {
                val sectionIndex = shownSections.indexOfFirst { it.id == hit.sectionId }
                if (sectionIndex >= 0) {
                    // Avoid jumping to the section top for each repeated
                    // occurrence in the same long note.
                    if (listState.firstVisibleItemIndex != sectionIndex)
                        listState.animateScrollToItem(sectionIndex)
                    entryRevealSerial = searchNavigationSerial
                }
            }
        }
    }
    // A compact accordion: all sections start closed; each profile keeps
    // its current open section when the activity is recreated.
    var expandedSectionId by rememberSaveable { mutableStateOf<String?>(null) }
    var expandedEntryId by rememberSaveable { mutableStateOf<String?>(null) }
    var editSectionId by remember { mutableStateOf<String?>(null) }
    var addSection by remember { mutableStateOf(false) }
    var sectionName by remember { mutableStateOf("") }
    var editEntry by remember { mutableStateOf<Pair<String, String?>?>(null) }
    var entryTitle by remember { mutableStateOf("") }
    var entryNotes by remember { mutableStateOf("") }
    var entryNotesHtml by remember { mutableStateOf("") }
    var confirmDelete by remember { mutableStateOf<Pair<String, String?>?>(null) }
    LaunchedEffect(smoothReturnTick) {
        if (smoothReturnTick > 0) {
            listState.animateScrollToItem(0)
            // A fresh Handover starts with its sections folded.
            expandedSectionId = null
            expandedEntryId = null
            searchOpen = false
            searchText = ""
        }
    }

    fun openSectionEditor(section: HandoverSection?) {
        editSectionId = section?.id
        sectionName = section?.title.orEmpty()
        addSection = true
    }

    LaunchedEffect(externalAddRequest) {
        if (embeddedInTab && externalAddRequest > 0) {
            openSectionEditor(null)
            onExternalAddHandled()
        }
    }

    fun openEntryEditor(sectionId: String, entry: HandoverEntry?) {
        editEntry = sectionId to entry?.id
        entryTitle = entry?.title.orEmpty()
        entryNotes = entry?.notes.orEmpty()
        entryNotesHtml = entry?.notesHtml.orEmpty()
    }

    val modalOpen = addSection || confirmDelete != null || exportOpen || exportFeedback != null
    val headerTop = if (personalMode) NavPersonalVisualTokens.headerTopDp.dp else 8.dp
    Box(Modifier.fillMaxSize()) {
    Box(Modifier.fillMaxSize().blur(if (!personalMode && modalOpen) 4.dp else 0.dp)) {
    Column(
        Modifier.fillMaxSize().hazeSource(hazeState)
            .then(if (embeddedInTab) Modifier else Modifier.statusBarsPadding().navigationBarsPadding())
            .then(if (actionBackdrop != null) Modifier.layerBackdrop(actionBackdrop) else Modifier)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = headerTop),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (embeddedInTab) {
                Column(Modifier.weight(1f)) {
                    Text(localized("Разделы передачи дел", "Handover sections", lang),
                        color = p.text, fontSize = 14.sp, maxLines = 2)
                    Text(localized("Разделов: ${handover.sections.size}", "Sections: ${handover.sections.size}", lang),
                        color = p.muted, fontSize = 12.sp)
                }
                HandoverExportAction(p, lang, enabled = !exportRunning) { exportOpen = true }
                Spacer(Modifier.width(8.dp))
            } else {
                if (actionBackdrop != null) Spacer(Modifier.size(44.dp))
                else NavGlassCircleButton(p, "back", onClick = onBack)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    if (personalMode) {
                        Text(localized("Личное", "Personal", lang),
                            color = p.text, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                    Text(if (personalMode) localized("Заметки", "Notes", lang)
                        else localized("Передача дел", "Handover notes", lang),
                        color = p.text, fontSize = 22.sp)
                }
            }
            if (!embeddedInTab && !personalMode) {
                HandoverExportAction(p, lang, enabled = !exportRunning) { exportOpen = true }
                Spacer(Modifier.width(8.dp))
            }
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
            placeholder = if (personalMode) localized("Поиск по заметкам", "Search notes", lang)
                else localized("Поиск по всему Handover", "Search all Handover", lang),
            neutralStyle = true, highContrastField = !personalMode,
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
        if (!personalMode) Box(Modifier.padding(horizontal = 16.dp)) { NavEventDivider(p) }
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize()
                .onGloballyPositioned { listViewportCoordinates = it }
                .revealSearchOnSettledTopPull(listState, searchOpen) { searchOpen = true }
                .dismissSearchImeOnResultsGesture(searchOpen),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp, top = 10.dp,
                // Keep the final section/entry entirely above the glass Add
                // button (including the navigation gesture inset in dock tabs).
                bottom = navFloatingActionBottomPadding(embeddedInTab),
            ),
            verticalArrangement = Arrangement.spacedBy(if (personalMode) 12.dp else NavEventLayout.sectionGap),
        ) {
            if (shownSections.isEmpty()) {
                item {
                    Column(Modifier.fillMaxWidth().then(if (personalMode) Modifier.personalNoteCard(p, 19.dp)
                        else Modifier.personalNoteCard(p, NavEventLayout.cardRadius)).padding(14.dp)) {
                        Text(
                            if (query.isNotBlank() && searchOpen)
                                localized("Совпадений не найдено.", "No matches found.", lang)
                            else localized("Пока нет разделов. Нажми +, чтобы создать первый.",
                                "No sections yet. Tap + to create one.", lang),
                            color = p.muted,
                        )
                    }
                }
            }
            items(shownSections, key = { it.id }) { section ->
                val expanded = (searchOpen && query.isNotBlank()) || expandedSectionId == section.id
                val chevronRotation by animateFloatAsState(
                    targetValue = if (expanded) 180f else 0f,
                    animationSpec = spring(dampingRatio = 1f, stiffness = 550f),
                    label = "handoverSectionChevron",
                )
                Column(
                    Modifier.fillMaxWidth().then(if (personalMode) Modifier.personalNoteCard(p, 19.dp)
                        else Modifier.personalNoteCard(p, NavEventLayout.cardRadius))
                        .padding(9.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Row(
                            Modifier.weight(1f)
                                .glassClickable {
                                    expandedSectionId = nextExpandedHandoverSection(
                                        expandedSectionId, section.id,
                                    )
                                }
                                .padding(vertical = 10.dp, horizontal = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            NavSearchMarkedText(
                                value = section.title,
                                query = if (searchOpen) query else "",
                                p = p, modifier = Modifier.weight(1f),
                                color = p.text, fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1, overflow = TextOverflow.Ellipsis,
                                activeMatchStart = focusedSearchHit?.takeIf {
                                    it.sectionId == section.id &&
                                        it.field == NavHandoverField.SECTION_TITLE
                                }?.start,
                            )
                            Spacer(Modifier.width(7.dp))
                            Text(
                                "${section.entries.size}",
                                color = if (personalMode) p.text else if (expanded) NavTodayVisualTokens.accentInk(p.dark) else p.muted, fontSize = 12.sp,
                            )
                            Spacer(Modifier.width(5.dp))
                            NavGlassGlyph(
                                "chevron-down", if (personalMode) p.text else p.muted,
                                Modifier.size(17.dp).rotate(chevronRotation),
                            )
                        }
                        Spacer(Modifier.width(5.dp))
                        Box(
                            Modifier.size(44.dp)
                                .glassClickable { openSectionEditor(section) },
                            contentAlignment = Alignment.Center,
                        ) {
                            NavGlassGlyph("edit", p.text, Modifier.size(21.dp))
                        }
                        Box(
                            Modifier.size(44.dp)
                                .glassClickable { openEntryEditor(section.id, null) },
                            contentAlignment = Alignment.Center,
                        ) {
                            NavGlassGlyph("plus", NavLiquidGlassActionTokens.glyphInk("plus", p.dark, p.blue), Modifier.size(23.dp))
                        }
                    }
                    AnimatedVisibility(
                        visible = expanded,
                        enter = expandVertically(
                            spring(dampingRatio = 1f, stiffness = 550f),
                        ) + fadeIn(spring(dampingRatio = 1f, stiffness = 550f)),
                        exit = shrinkVertically(
                            spring(dampingRatio = 1f, stiffness = 550f),
                        ) + fadeOut(spring(dampingRatio = 1f, stiffness = 550f)),
                    ) {
                        Column(
                            Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Spacer(Modifier.height(1.dp))
                            if (section.entries.isEmpty()) {
                                Text(
                                    localized("Нажми +, чтобы добавить пункт", "Tap + to add an item", lang),
                                    color = p.muted, fontSize = 12.sp,
                                    modifier = Modifier.padding(start = 5.dp, bottom = 7.dp),
                                )
                            }
                            handoverNotebookEntries(section.entries).forEach { entry ->
                                val focusedEntryHit = focusedSearchHit?.takeIf {
                                    it.sectionId == section.id && it.entryId == entry.id
                                }
                                val entryExpanded = handoverEntryShowsNotes(
                                    personalMode, expandedEntryId, entry, query,
                                )
                                val entryChevronRotation by animateFloatAsState(
                                    targetValue = if (entryExpanded) 180f else 0f,
                                    animationSpec = spring(dampingRatio = 1f, stiffness = 550f),
                                    label = "handoverEntryChevron",
                                )
                                var titleCoordinates by remember(section.id, entry.id) {
                                    mutableStateOf<LayoutCoordinates?>(null)
                                }
                                var noteCoordinates by remember(section.id, entry.id) {
                                    mutableStateOf<LayoutCoordinates?>(null)
                                }
                                var noteLayout by remember(section.id, entry.id, query) {
                                    mutableStateOf<TextLayoutResult?>(null)
                                }
                                var handledSearchSerial by remember(section.id, entry.id) {
                                    mutableIntStateOf(0)
                                }
                                // Only the LazyColumn may scroll to a match.
                                // BringIntoViewRequester used to propagate scrolling
                                // to the Android window and push the fixed search bar
                                // above the top edge while the keyboard was open.
                                LaunchedEffect(
                                    entryRevealSerial, focusedEntryHit, noteLayout,
                                    titleCoordinates, noteCoordinates, listViewportCoordinates,
                                ) {
                                    val hit = focusedEntryHit
                                    if (entryRevealSerial > 0 && hit != null &&
                                        handledSearchSerial != entryRevealSerial
                                    ) {
                                        val target = when (hit.field) {
                                            NavHandoverField.ENTRY_TITLE -> titleCoordinates
                                            NavHandoverField.ENTRY_NOTES -> noteCoordinates
                                            else -> null
                                        }
                                        val viewport = listViewportCoordinates
                                        val measured = if (hit.field == NavHandoverField.ENTRY_NOTES)
                                            noteLayout else null
                                        if (target != null && target.isAttached &&
                                            viewport != null && viewport.isAttached &&
                                            (hit.field != NavHandoverField.ENTRY_NOTES || measured != null)
                                        ) {
                                            withFrameNanos { }
                                            val localY = if (measured != null && entry.notes.isNotEmpty())
                                                measured.getBoundingBox(
                                                    hit.start.coerceIn(0, entry.notes.length - 1),
                                                ).top else 0f
                                            val wordY = target.positionInWindow().y + localY
                                            val viewportTop = viewport.positionInWindow().y
                                            // Keep the active occurrence near the top of
                                            // the list viewport, even when it was already
                                            // visible lower down. Only the LazyColumn scrolls;
                                            // the search field remains fixed above it.
                                            val targetTop = viewportTop + matchMarginPx
                                            val delta = wordY - targetTop
                                            handledSearchSerial = entryRevealSerial
                                            if (delta != 0f) listState.animateScrollBy(delta)
                                        }
                                    }
                                }
                                Column(
                                    Modifier.fillMaxWidth()
                                        .then(if (personalMode) Modifier.personalNoteCard(p, 13.dp, nested = true)
                                            else Modifier.personalNoteCard(p, 15.dp, nested = true))
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalArrangement = Arrangement.spacedBy(2.dp),
                                ) {
                                    Row(
                                        Modifier.fillMaxWidth()
                                            .glassClickable {
                                                if (personalMode) openEntryEditor(section.id, entry)
                                                else expandedEntryId = nextExpandedHandoverEntry(
                                                    expandedEntryId, entry.id,
                                                )
                                            }
                                            .padding(vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        NavSearchMarkedText(
                                            entry.title, query, p,
                                            modifier = Modifier.weight(1f)
                                                .onGloballyPositioned { titleCoordinates = it },
                                            color = p.text, fontSize = 15.sp,
                                            fontWeight = FontWeight.Medium,
                                            activeMatchStart = focusedEntryHit?.takeIf {
                                                it.field == NavHandoverField.ENTRY_TITLE
                                            }?.start,
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        NavGlassGlyph(
                                            if (personalMode) "chevron-right" else "chevron-down",
                                            if (personalMode) p.text else p.muted.copy(alpha = 0.72f),
                                            Modifier.size(17.dp)
                                                .rotate(if (personalMode) 0f else entryChevronRotation),
                                        )
                                    }
                                    AnimatedVisibility(
                                        visible = entryExpanded,
                                        enter = expandVertically(
                                            spring(dampingRatio = 1f, stiffness = 550f),
                                        ) + fadeIn(spring(dampingRatio = 1f, stiffness = 550f)),
                                        exit = shrinkVertically(
                                            spring(dampingRatio = 1f, stiffness = 550f),
                                        ) + fadeOut(spring(dampingRatio = 1f, stiffness = 550f)),
                                    ) {
                                        Column(
                                            Modifier.fillMaxWidth()
                                                .glassClickable { openEntryEditor(section.id, entry) }
                                                .padding(top = 6.dp, bottom = 4.dp),
                                        ) {
                                            if (entry.notes.isBlank() && entry.photos.isEmpty()) {
                                                Text(
                                                    localized("Нажми, чтобы добавить описание",
                                                        "Tap to add a description", lang),
                                                    color = p.muted, fontSize = 12.sp,
                                                )
                                            } else if (query.isNotEmpty() &&
                                                navSearchRanges(entry.notes, query).isNotEmpty()
                                            ) {
                                                // Mark search text only; never mutate saved HTML.
                                                NavSearchMarkedText(
                                                    entry.notes, query, p,
                                                    modifier = Modifier.fillMaxWidth()
                                                        .onGloballyPositioned {
                                                            noteCoordinates = it
                                                        },
                                                    color = p.muted, fontSize = 12.sp,
                                                    activeMatchStart = focusedEntryHit?.takeIf {
                                                        it.field == NavHandoverField.ENTRY_NOTES
                                                    }?.start,
                                                    onTextLayout = { noteLayout = it },
                                                )
                                            } else {
                                                NavRichNoteText(
                                                    entry.notes, entry.notesHtml, p,
                                                    fontSize = 12.sp,
                                                    modifier = Modifier.fillMaxWidth(),
                                                )
                                            }
                                            if (entry.photos.isNotEmpty()) {
                                                Spacer(Modifier.height(8.dp))
                                                HandoverPhotoStrip(entry.photos, p, lang)
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
    if (actionBackdrop != null) {
        if (!embeddedInTab) {
            Box(Modifier.align(Alignment.TopStart).statusBarsPadding()
                .padding(start = 16.dp, top = headerTop)) {
                NavLiquidGlassCircleButton(p, actionBackdrop, "back", onBack)
            }
        }
        Box(Modifier.align(Alignment.TopEnd)
            .then(if (embeddedInTab) Modifier else Modifier.statusBarsPadding())
            .padding(end = 16.dp, top = headerTop)) {
            NavLiquidGlassCircleButton(p, actionBackdrop, "search") {
                searchOpen = !searchOpen
                if (!searchOpen) searchText = ""
            }
        }
    }
    // Embedded Handover uses the already captured parent glass backdrop.
    // Standalone Personal keeps its own FAB, without changing note storage.
    if (!embeddedInTab) {
        Box(
            Modifier.align(Alignment.BottomEnd).navigationBarsPadding()
                .padding(end = 19.dp, bottom = 18.dp),
        ) {
            if (actionBackdrop != null) {
                NavLiquidGlassAddButton(p, actionBackdrop) { openSectionEditor(null) }
            } else {
                NavGlassAddButton(p, hazeState) { openSectionEditor(null) }
            }
        }
    }
    } // Blurred page content and floating actions.
    } // Dialogs remain outside the blurred page.

    if (exportOpen && !personalMode) {
        NavGlassAlertDialog(p = p, neutralSurface = true, neutralActions = true,
            onDismissRequest = { exportOpen = false },
            solidSurface = true,
            title = { Text(localized("Экспорт Handover", "Export Handover", lang)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(localized(
                        "Создать документ для передачи сменщику. Все разделы и записи будут доступны без NavTasks.",
                        "Create a document readable without NavTasks, including all sections and notes.",
                        lang))
                    HandoverExportOption("Word (.docx)", p) {
                        exportSnapshot = handover.sections.toList()
                        exportOpen = false
                        wordExport.launch("NavTasks_Handover_" + LocalDate.now() + ".docx")
                    }
                    HandoverExportOption("PDF", p) {
                        exportSnapshot = handover.sections.toList()
                        exportOpen = false
                        pdfExport.launch("NavTasks_Handover_" + LocalDate.now() + ".pdf")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { exportOpen = false }) {
                    Text(localized("Отмена", "Cancel", lang), color = p.text)
                }
            })
    }
    if (exportFeedback != null) {
        NavGlassAlertDialog(p = p, neutralSurface = true, neutralActions = true,
            onDismissRequest = { exportFeedback = null },
            solidSurface = true,
            title = { Text(localized("Экспорт Handover", "Handover export", lang)) },
            text = { Text(exportFeedback.orEmpty(),
                color = if (exportFailed) p.systemRed else p.text) },
            confirmButton = {
                TextButton(onClick = { exportFeedback = null }) {
                    Text("OK", color = NavTodayVisualTokens.accentInk(p.dark))
                }
            })
    }

    if (addSection) {
        HandoverGlassDialog(
            p = p, personalMode = personalMode,
            onDismissRequest = { addSection = false },
            title = { Text(if (editSectionId == null) localized("Новый раздел", "New section", lang)
                else localized("Редактировать раздел", "Edit section", lang)) },
            text = {
                HandoverGlassInput(
                    value = sectionName, onValueChange = { sectionName = it.take(120) },
                    p = p, placeholder = localized("Название", "Title", lang),
                    modifier = Modifier.fillMaxWidth(), personalMode = personalMode
                )
            },
            confirmButton = {
                HandoverDialogAction(localized("Сохранить", "Save", lang), p,
                    enabled = sectionName.isNotBlank() && (personalMode ||
                        sectionName.trim() != handover.sections.firstOrNull { it.id == editSectionId }?.title.orEmpty().trim()),
                    personalMode = personalMode, accent = true) {
                    handover.saveSection(sectionName, editSectionId)
                    addSection = false
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (editSectionId != null) {
                        HandoverDialogAction(localized("Удалить", "Delete", lang),
                            p, destructive = true, personalMode = personalMode) {
                            confirmDelete = editSectionId.orEmpty() to null
                            addSection = false
                        }
                    }
                    HandoverDialogAction(localized("Отмена", "Cancel", lang), p, personalMode = personalMode) {
                        addSection = false
                    }
                }
            },
        )
    }

    editEntry?.let { (sectionId, entryId) ->
        val section = handover.sections.firstOrNull { it.id == sectionId }
        val item = section?.entries?.firstOrNull { it.id == entryId }
        // Shared editor for both Personal notes and vessel Handover; stores untouched.
        Dialog(
            onDismissRequest = { editEntry = null },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false,
            ),
        ) {
            HandoverFullNoteEditor(
                p = p, lang = lang, personalMode = personalMode,
                sectionTitle = section?.title.orEmpty(),
                entryKey = "handover:" + sectionId + ":" + (entryId ?: "new"),
                initialTitle = item?.title.orEmpty(),
                initialPlain = item?.notes.orEmpty(),
                initialHtml = item?.notesHtml.orEmpty(),
                initialPhotos = item?.photos.orEmpty(),
                onCancel = { editEntry = null },
                onDelete = if (entryId != null) ({
                    editEntry = null
                    confirmDelete = sectionId to entryId
                }) else null,
                onSave = { title, plain, html, photos ->
                    // If storage is full, leave the editor and its draft photos
                    // untouched. Never claim success after a failed commit.
                    runCatching {
                        val savedTitle = if (personalMode && title.isBlank())
                            nextPersonalNoteTitle(handover.sections, lang) else title
                        handover.saveEntry(sectionId, savedTitle, plain, entryId, html, photos)
                        editEntry = null
                    }.isSuccess
                },
            )
        }
    }

    confirmDelete?.let { (sectionId, entryId) ->
        HandoverGlassDialog(
            p = p, personalMode = personalMode,
            onDismissRequest = { confirmDelete = null },
            title = { Text(localized("Удалить без возможности восстановления?", "Delete permanently?", lang)) },
            text = { Text(localized("Пункт или раздел будет удалён из передачи дел.",
                "The item or section will be removed from Handover.", lang)) },
            confirmButton = {
                HandoverDialogAction(localized("Удалить", "Delete", lang), p,
                    destructive = true, personalMode = personalMode) {
                    if (entryId == null) handover.deleteSection(sectionId)
                    else handover.deleteEntry(sectionId, entryId)
                    confirmDelete = null
                }
            },
            dismissButton = {
                HandoverDialogAction(localized("Отмена", "Cancel", lang), p, personalMode = personalMode) {
                    confirmDelete = null
                }
            },
        )
    }


}

/** The same glass language as NavTasks cards. Avoids lavender Material alert surfaces. */
@Composable
private fun HandoverGlassDialog(
    p: NavPalette, personalMode: Boolean,
    onDismissRequest: () -> Unit,
    title: @Composable () -> Unit,
    text: @Composable () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit = {},
) {
    val frost = navPopupFrostAvailable()
    Dialog(onDismissRequest = onDismissRequest) {
        if (personalMode) NavApplyDialogFrost(frost)
        Column(
            Modifier.fillMaxWidth()
                .then(if (personalMode) Modifier.navPopupGlassSurface(p, frosted = frost)
                    else Modifier.personalNoteCard(p, 26.dp))
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp),
        ) {
            CompositionLocalProvider(LocalContentColor provides p.text) {
                Box(Modifier.fillMaxWidth()) {
                    ProvideTextStyle(TextStyle(color = p.text, fontSize = 20.sp)) { title() }
                }
            }
            CompositionLocalProvider(LocalContentColor provides p.muted) {
                Box(Modifier.fillMaxWidth().heightIn(max = 440.dp)) { text() }
            }
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                dismissButton()
                confirmButton()
            }
        }
    }
}

/** Separate, minimum 44dp, optically consistent actions; no merged Delete/Cancel pill. */
@Composable
private fun HandoverDialogAction(
    label: String, p: NavPalette,
    destructive: Boolean = false, enabled: Boolean = true,
    personalMode: Boolean = false,
    accent: Boolean = false,
    onClick: () -> Unit,
) {
    Box(Modifier.personalNoteCard(p, 12.dp, nested = true)) {
        TextButton(
            onClick = onClick, enabled = enabled,
            modifier = Modifier.heightIn(min = 44.dp),
            contentPadding = PaddingValues(horizontal = 9.dp, vertical = 4.dp),
        ) {
            Text(label,
                color = if (!enabled) p.muted
                    else if (destructive) p.systemRed
                    else if (personalMode || accent) NavTodayVisualTokens.accentInk(p.dark) else p.text,
                fontSize = 13.sp)
        }
    }
}

/** Compact glass text field used by both Handover edit dialogs. */
@Composable
private fun HandoverGlassInput(
    value: String,
    onValueChange: (String) -> Unit,
    p: NavPalette,
    placeholder: String,
    modifier: Modifier = Modifier,
    multiline: Boolean = false,
    personalMode: Boolean = false,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = !multiline,
        maxLines = if (multiline) 5 else 1,
        keyboardOptions = NavSentenceKeyboard,
        textStyle = TextStyle(color = p.text, fontSize = 14.sp),
        cursorBrush = SolidColor(NavTodayVisualTokens.accentInk(p.dark)),
        modifier = modifier.height(if (multiline) 100.dp else 46.dp)
            .personalNoteCard(p, 12.dp, nested = true),
        decorationBox = { inner ->
            Box(
                Modifier.fillMaxSize().padding(horizontal = 13.dp, vertical = if (multiline) 12.dp else 0.dp),
                contentAlignment = if (multiline) Alignment.TopStart else Alignment.CenterStart,
            ) {
                if (value.isBlank()) {
                    Text(placeholder, color = p.muted, fontSize = 13.sp)
                }
                inner()
            }
        },
    )
}

/** Export all sections, never just the currently visible search results. */
@Composable
private fun HandoverExportAction(p: NavPalette, lang: UiLanguage, enabled: Boolean, onClick: () -> Unit) {
    TextButton(onClick = onClick, enabled = enabled,
        modifier = Modifier.heightIn(min = 44.dp).personalNoteCard(p, 18.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)) {
        Text(localized("Экспорт", "Export", lang),
            color = if (enabled) NavTodayVisualTokens.accentInk(p.dark) else p.muted,
            fontSize = 13.sp, maxLines = 1)
    }
}

@Composable
private fun HandoverExportOption(label: String, p: NavPalette, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().personalNoteCard(p, 16.dp, nested = true)
        .glassClickable(onClick = onClick).heightIn(min = 48.dp).padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = p.text, fontSize = 15.sp, modifier = Modifier.weight(1f))
        NavGlassGlyph("chevron-right", NavTodayVisualTokens.accentInk(p.dark), Modifier.size(17.dp))
    }
}
