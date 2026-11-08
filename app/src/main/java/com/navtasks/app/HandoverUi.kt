package com.navtasks.app

import dev.chrisbanes.haze.hazeSource
import com.kyant.backdrop.backdrops.layerBackdrop

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate

/** Active notes stay at the top. Resolved/struck entries remain visible at the end. */
internal fun orderedHandoverEntries(entries: List<HandoverEntry>): List<HandoverEntry> =
    entries.filter { it.mark == HandoverMark.OPEN } +
        entries.filter { it.mark != HandoverMark.OPEN }

/** Exactly one Handover section can be open; tapping it a second time closes it. */
internal fun nextExpandedHandoverSection(current: String?, tapped: String): String? =
    if (current == tapped) null else tapped

@Composable
fun HandoverScreen(
    handover: HandoverStore,
    tasks: TaskStore,
    p: NavPalette,
    lang: UiLanguage,
    onBack: () -> Unit,
    personalMode: Boolean = false,
    embeddedInTab: Boolean = false,
) {
    val hazeState = remember { dev.chrisbanes.haze.HazeState() }
    // Page content only: floating buttons stay sibling to the recorder.
    val actionBackdrop = if (android.os.Build.VERSION.SDK_INT >= 33)
        com.kyant.backdrop.backdrops.rememberLayerBackdrop() else null
    var searchOpen by rememberSaveable { mutableStateOf(false) }
    var searchText by rememberSaveable { mutableStateOf("") }
    val query = searchText.trim()
    val shownSections = searchHandoverSections(
        handover.sections, if (searchOpen) query else "")
    // A compact accordion: all sections start closed; each profile keeps
    // its current open section when the activity is recreated.
    var expandedSectionId by rememberSaveable { mutableStateOf<String?>(null) }
    var editSectionId by remember { mutableStateOf<String?>(null) }
    var addSection by remember { mutableStateOf(false) }
    var sectionName by remember { mutableStateOf("") }
    var editEntry by remember { mutableStateOf<Pair<String, String?>?>(null) }
    var entryTitle by remember { mutableStateOf("") }
    var entryNotes by remember { mutableStateOf("") }
    var entryNotesHtml by remember { mutableStateOf("") }
    var selectedEntry by remember { mutableStateOf<Pair<String, String>?>(null) }
    var completingWithDate by remember { mutableStateOf<Triple<String, String, LocalDate>?>(null) }
    var confirmDelete by remember { mutableStateOf<Pair<String, String?>?>(null) }

    fun openSectionEditor(section: HandoverSection?) {
        editSectionId = section?.id
        sectionName = section?.title.orEmpty()
        addSection = true
    }

    fun openEntryEditor(sectionId: String, entry: HandoverEntry?) {
        editEntry = sectionId to entry?.id
        entryTitle = entry?.title.orEmpty()
        entryNotes = entry?.notes.orEmpty()
        entryNotesHtml = entry?.notesHtml.orEmpty()
    }

    Box(Modifier.fillMaxSize()) {
    Column(
        Modifier.fillMaxSize().hazeSource(hazeState)
            .then(if (embeddedInTab) Modifier else Modifier.statusBarsPadding().navigationBarsPadding())
            .then(if (actionBackdrop != null) Modifier.layerBackdrop(actionBackdrop) else Modifier)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (embeddedInTab) {
                Text(localized("Разделы передачи дел", "Handover sections", lang),
                    color = p.text, fontSize = 16.sp, modifier = Modifier.weight(1f))
            } else {
                if (actionBackdrop != null) Spacer(Modifier.size(44.dp))
                else NavGlassCircleButton(p, "back", onClick = onBack)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(if (personalMode) localized("ЛИЧНОЕ", "PERSONAL", lang) else "HANDOVER",
                        color = p.blue, fontSize = 13.sp, letterSpacing = 1.8.sp)
                    Text(if (personalMode) localized("Заметки", "Notes", lang)
                        else localized("Передача дел", "Handover notes", lang),
                        color = p.text, fontSize = 22.sp)
                }
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
                placeholder = localized("Поиск по всему Handover", "Search all Handover", lang),
            )
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp, top = 10.dp,
                bottom = if (embeddedInTab) 126.dp else 32.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (shownSections.isEmpty()) {
                item {
                    NavPanel(p) {
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
                    Modifier.fillMaxWidth().navGlass(p, radius = 19.dp, role = GlassRole.PANEL)
                        .padding(11.dp),
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
                            Text(
                                section.title, Modifier.weight(1f),
                                color = p.text, fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1, overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(Modifier.width(7.dp))
                            Text(
                                "${section.entries.count { it.mark == HandoverMark.COMPLETED }}/${section.entries.size}",
                                color = p.muted, fontSize = 12.sp,
                            )
                            Spacer(Modifier.width(5.dp))
                            NavGlassGlyph(
                                "chevron-down", p.muted,
                                Modifier.size(17.dp).rotate(chevronRotation),
                            )
                        }
                        Spacer(Modifier.width(5.dp))
                        Box(
                            Modifier.size(44.dp)
                                .glassClickable { openSectionEditor(section) },
                            contentAlignment = Alignment.Center,
                        ) {
                            NavGlassGlyph("edit", p.muted, Modifier.size(21.dp))
                        }
                        Box(
                            Modifier.size(44.dp)
                                .glassClickable { openEntryEditor(section.id, null) },
                            contentAlignment = Alignment.Center,
                        ) {
                            NavGlassGlyph("plus", p.blue, Modifier.size(23.dp))
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
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Spacer(Modifier.height(1.dp))
                            if (section.entries.isEmpty()) {
                                Text(
                                    localized("Нажми +, чтобы добавить пункт", "Tap + to add an item", lang),
                                    color = p.muted, fontSize = 12.sp,
                                    modifier = Modifier.padding(start = 5.dp, bottom = 7.dp),
                                )
                            }
                            orderedHandoverEntries(section.entries).forEach { entry ->
                            val finished = entry.mark != HandoverMark.OPEN
                            Row(
                                Modifier.fillMaxWidth()
                                    .navGlass(p, radius = 13.dp,
                                        role = if (finished) GlassRole.CONTROL else GlassRole.TASK)
                                    .glassClickable { selectedEntry = section.id to entry.id }
                                    .padding(horizontal = 10.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(if (entry.mark == HandoverMark.COMPLETED) "✓" else if (finished) "–" else "○",
                                    color = if (finished) p.successText else p.blue, fontSize = 22.sp)
                                Spacer(Modifier.width(11.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(entry.title, color = if (finished) p.muted else p.text,
                                        fontSize = 14.sp,
                                        textDecoration = if (finished) TextDecoration.LineThrough else TextDecoration.None)
                                    if (entry.notes.isNotBlank()) {
                                        NavRichNoteText(entry.notes, entry.notesHtml, p,
                                            fontSize = 11.sp, modifier = Modifier.heightIn(max = 46.dp))
                                    }
                                    if (entry.mark == HandoverMark.COMPLETED) {
                                        Text(entry.completedDate?.let {
                                            localized("Завершено: $it", "Completed: $it", lang)
                                        } ?: localized("Завершено без даты", "Completed without date", lang),
                                            color = p.successText, fontSize = 11.sp)
                                    } else if (entry.mark == HandoverMark.STRUCK) {
                                        Text(localized("Вычеркнуто", "Struck out", lang),
                                            color = p.muted, fontSize = 11.sp)
                                    }
                                }
                                NavGlassGlyph("chevron-right", p.muted, Modifier.size(17.dp))
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
                .padding(start = 16.dp, top = 8.dp)) {
                NavLiquidGlassCircleButton(p, actionBackdrop, "back", onBack)
            }
        }
        Box(Modifier.align(Alignment.TopEnd)
            .then(if (embeddedInTab) Modifier else Modifier.statusBarsPadding())
            .padding(end = 16.dp, top = 8.dp)) {
            NavLiquidGlassCircleButton(p, actionBackdrop, "search") {
                searchOpen = !searchOpen
                if (!searchOpen) searchText = ""
            }
        }
    }
    Box(
        Modifier.align(Alignment.BottomEnd)
            .navigationBarsPadding()
            .padding(end = 19.dp, bottom = if (embeddedInTab) 83.dp else 18.dp),
    ) {
        if (actionBackdrop != null) {
            NavLiquidGlassAddButton(p, actionBackdrop) { openSectionEditor(null) }
        } else {
            NavGlassAddButton(p, hazeState) { openSectionEditor(null) }
        }
    }
    } // Content and floating Add button share the same glass source.

    if (addSection) {
        HandoverGlassDialog(
            p = p, 
            onDismissRequest = { addSection = false },
            title = { Text(if (editSectionId == null) localized("Новый раздел", "New section", lang)
                else localized("Редактировать раздел", "Edit section", lang)) },
            text = {
                HandoverGlassInput(
                    value = sectionName, onValueChange = { sectionName = it.take(120) },
                    p = p, placeholder = localized("Название", "Title", lang),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    handover.saveSection(sectionName, editSectionId)
                    addSection = false
                }, enabled = sectionName.isNotBlank()) {
                    Text(localized("Сохранить", "Save", lang))
                }
            },
            dismissButton = {
                Row {
                    if (editSectionId != null) {
                        TextButton(onClick = {
                            confirmDelete = editSectionId.orEmpty() to null
                            addSection = false
                        }) { Text(localized("Удалить", "Delete", lang), color = p.dangerText) }
                    }
                    TextButton(onClick = { addSection = false }) {
                        Text(localized("Отмена", "Cancel", lang))
                    }
                }
            },
        )
    }

    editEntry?.let { (sectionId, entryId) ->
        HandoverGlassDialog(
            p = p, 
            onDismissRequest = { editEntry = null },
            title = { Text(if (entryId == null) localized("Новый пункт", "New item", lang)
                else localized("Редактировать пункт", "Edit item", lang)) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    HandoverGlassInput(
                        value = entryTitle, onValueChange = { entryTitle = it.take(200) },
                        p = p, placeholder = localized("Название", "Title", lang),
                        modifier = Modifier.fillMaxWidth()
                    )
                    NavRichNoteEditor(
                        initialPlain = handover.sections.firstOrNull { it.id == sectionId }
                            ?.entries?.firstOrNull { it.id == entryId }?.notes.orEmpty(),
                        initialHtml = handover.sections.firstOrNull { it.id == sectionId }
                            ?.entries?.firstOrNull { it.id == entryId }?.notesHtml.orEmpty(),
                        entryKey = "handover:" + sectionId + ":" + (entryId ?: "new"),
                        p = p, lang = lang, maxCharacters = 4000,
                        placeholder = localized("Комментарий", "Notes", lang),
                        onChange = { plain, html -> entryNotes = plain; entryNotesHtml = html },
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    handover.saveEntry(sectionId, entryTitle, entryNotes, entryId, entryNotesHtml)
                    editEntry = null
                }, enabled = entryTitle.isNotBlank() &&
                    navRichNoteAllowed(entryNotes, entryNotesHtml, 4000)) {
                    Text(localized("Сохранить", "Save", lang))
                }
            },
            dismissButton = {
                Row {
                    if (entryId != null) {
                        TextButton(onClick = {
                            confirmDelete = sectionId to entryId
                            editEntry = null
                        }) { Text(localized("Удалить", "Delete", lang), color = p.dangerText) }
                    }
                    TextButton(onClick = { editEntry = null }) { Text(localized("Отмена", "Cancel", lang)) }
                }
            },
        )
    }

    completingWithDate?.let { (sectionId, entryId, start) ->
        NavDateWheelDialog(
            initial = start, p = p, lang = lang,
            onDismiss = { completingWithDate = null },
            onSelected = { day ->
                handover.setMark(sectionId, entryId, HandoverMark.COMPLETED, day)
                completingWithDate = null
            },
        )
    }
    selectedEntry?.let { (sectionId, entryId) ->
        val item = handover.sections.firstOrNull { it.id == sectionId }?.entries?.firstOrNull { it.id == entryId }
        if (item != null) {
            HandoverGlassDialog(
            p = p, 
                onDismissRequest = { selectedEntry = null },
                title = {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            item.title, modifier = Modifier.weight(1f),
                            maxLines = 2, overflow = TextOverflow.Ellipsis,
                        )
                        IconButton(
                            onClick = {
                                openEntryEditor(sectionId, item)
                                selectedEntry = null
                            },
                            modifier = Modifier.size(44.dp).semantics {
                                contentDescription = localized("Редактировать пункт", "Edit item", lang)
                            },
                        ) {
                            NavGlassGlyph("edit", p.blue, Modifier.size(21.dp))
                        }
                    }
                },
                text = {
                    Column(Modifier.verticalScroll(rememberScrollState()).heightIn(max = 380.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        if (item.notes.isNotBlank()) NavRichNoteText(item.notes, item.notesHtml, p,
                            fontSize = 13.sp)
                        if (item.mark == HandoverMark.COMPLETED) {
                            Text(item.completedDate?.toString() ?: localized("Дата не указана", "No date", lang),
                                color = p.successText, fontSize = 13.sp)
                        }
                        TextButton(onClick = {
                            completingWithDate = Triple(
                                sectionId, entryId, item.completedDate ?: LocalDate.now(),
                            )
                            selectedEntry = null
                        }) { Text(localized("✓ Завершить с датой", "✓ Complete with date", lang)) }
                        TextButton(onClick = {
                            handover.setMark(sectionId, entryId, HandoverMark.COMPLETED)
                            selectedEntry = null
                        }) { Text(localized("✓ Завершить без даты", "✓ Complete without date", lang)) }
                        TextButton(onClick = {
                            handover.setMark(sectionId, entryId, HandoverMark.STRUCK)
                            selectedEntry = null
                        }) { Text(localized("Вычеркнуть", "Strike out", lang)) }
                        if (item.mark != HandoverMark.OPEN) {
                            TextButton(onClick = {
                                handover.setMark(sectionId, entryId, HandoverMark.OPEN)
                                selectedEntry = null
                            }) { Text(localized("Вернуть в работу", "Reopen", lang)) }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { selectedEntry = null }) {
                        Text(localized("Закрыть", "Close", lang))
                    }
                },
            )
        }
    }

    confirmDelete?.let { (sectionId, entryId) ->
        HandoverGlassDialog(
            p = p, 
            onDismissRequest = { confirmDelete = null },
            title = { Text(localized("Удалить без возможности восстановления?", "Delete permanently?", lang)) },
            text = { Text(localized("Пункт или раздел будет удалён из передачи дел.",
                "The item or section will be removed from Handover.", lang)) },
            confirmButton = {
                TextButton(onClick = {
                    if (entryId == null) handover.deleteSection(sectionId)
                    else handover.deleteEntry(sectionId, entryId)
                    confirmDelete = null
                }) { Text(localized("Удалить", "Delete", lang), color = p.dangerText) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = null }) {
                    Text(localized("Отмена", "Cancel", lang))
                }
            },
        )
    }


}

/** The same glass language as NavTasks cards. Avoids lavender Material alert surfaces. */
@Composable
private fun HandoverGlassDialog(
    p: NavPalette,
    onDismissRequest: () -> Unit,
    title: @Composable () -> Unit,
    text: @Composable () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit = {},
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Column(
            Modifier.fillMaxWidth()
                .navDialogSurface(p)
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
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.navGlass(p, radius = 14.dp, role = GlassRole.CONTROL)) {
                    dismissButton()
                }
                Spacer(Modifier.width(7.dp))
                Box(Modifier.navGlass(p, radius = 14.dp, role = GlassRole.CONTROL)) {
                    confirmButton()
                }
            }
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
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = !multiline,
        maxLines = if (multiline) 5 else 1,
        keyboardOptions = NavSentenceKeyboard,
        textStyle = TextStyle(color = p.text, fontSize = 14.sp),
        cursorBrush = SolidColor(p.blue),
        modifier = modifier.height(if (multiline) 100.dp else 46.dp)
            .navGlass(p, radius = 12.dp, role = GlassRole.CONTROL),
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
