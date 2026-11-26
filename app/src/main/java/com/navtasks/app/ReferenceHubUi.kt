package com.navtasks.app

import androidx.activity.compose.BackHandler
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.disabled
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Inspections are a visible preview until the online library is ready.
 * This guard also covers a restored navigation state from an older APK.
 */
internal fun referenceLibraryRouteAvailable(sectionId: String?): Boolean =
    sectionId == NavReferenceSections.PERSONAL

/** Keep search across the existing SIRE/VIQ7 headings, equipment and personal sources. */
internal fun referencePlaceholderMatches(query: String, vararg fields: String): Boolean {
    val needle = query.trim()
    return needle.isEmpty() || fields.any { it.contains(needle, ignoreCase = true) }
}

@Composable
internal fun ReferenceHubScreen(
    p: NavPalette,
    lang: UiLanguage,
    profileId: String,
    smoothReturnTick: Int = 0,
) {
    val context = LocalContext.current
    val equipmentStore = remember(context, profileId) {
        ReferenceEquipmentStore(context, profileId)
    }
    // A corrupted catalog must never be reinterpreted as an empty one.
    val loaded = remember(equipmentStore) { runCatching { equipmentStore.read() } }
    var catalog by remember(equipmentStore) { mutableStateOf(loaded.getOrNull()) }
    var equipmentOpen by rememberSaveable(profileId) { mutableStateOf(false) }
    var equipmentSearch by rememberSaveable(profileId) { mutableStateOf("") }
    var inspectionsOpen by rememberSaveable(profileId) { mutableStateOf(false) }
    var exchangeOpen by rememberSaveable(profileId) { mutableStateOf(false) }
    val manualStore = remember(context) { ReferenceManualStore(context) }
    var manualSection by rememberSaveable { mutableStateOf<String?>(null) }
    var manualSearch by rememberSaveable { mutableStateOf("") }
    // Refresh manual metadata when returning from a Reference import.
    val manualSnapshot = remember(manualSection, exchangeOpen) { runCatching { manualStore.read() } }
    val manuals = manualSnapshot.getOrNull()
    val listState = rememberLazyListState()
    var searchOpen by rememberSaveable { mutableStateOf(false) }
    var searchText by rememberSaveable { mutableStateOf("") }
    val query = if (searchOpen) navEffectiveSearchQuery(searchText) else ""
    val inspectionsName = localized("Инспекции", "Inspections", lang)
    val inspectionsSubtitle = localized("SIRE 2.0 и VIQ7", "SIRE 2.0 and VIQ7", lang)
    val sireSubtitle = localized("Подготовка к инспекциям", "Inspection preparation", lang)
    val viqSubtitle = localized("Архивный справочник", "Historical reference", lang)
    val later = localized("В разработке", "In development", lang)
    val manualMatches = if (query.isBlank()) emptyList()
        else manuals?.let { referenceManualSearch(it, query) }.orEmpty()
            .filter { referenceLibraryRouteAvailable(it.sectionId) }
    val excerptMatches = if (query.isBlank()) emptyList()
        else manuals?.excerpts?.filter {
            referenceLibraryRouteAvailable(it.sectionId) && referenceExcerptMatches(it, query)
        }.orEmpty()
    val personalHits = manualMatches.count { it.sectionId == NavReferenceSections.PERSONAL } +
        excerptMatches.count { it.sectionId == NavReferenceSections.PERSONAL }
    val showSire = referencePlaceholderMatches(query, "SIRE 2.0", sireSubtitle, later)
    val showViq = referencePlaceholderMatches(query, "VIQ7", viqSubtitle, later)
    val inspectionsMatch = referencePlaceholderMatches(query,
        "Инспекции", "Inspections", inspectionsSubtitle)
    val showInspections = inspectionsMatch || showSire || showViq
    val personalName = localized("Мои материалы", "My materials", lang)
    val showPersonal = referencePlaceholderMatches(query, personalName,
        localized("Свои PDF, Word и папки", "Your PDF, Word and folders", lang)) ||
        personalHits > 0
    val equipmentSubtitle = localized("Процедуры и выдержки из мануалов",
        "Equipment, procedures and manual excerpts", lang)
    val equipmentMatches = if (query.isBlank()) emptyList()
        else catalog?.equipment?.filter { referenceMatchesEquipment(it, query) }.orEmpty()
    val showEquipment = query.isBlank() ||
        referencePlaceholderMatches(query, "Оборудование", "Equipment", equipmentSubtitle) ||
        equipmentMatches.isNotEmpty()
    val searchResults = navReferenceSearchHits(
        listOf(
            NavReferenceSearchRow("inspections", inspectionsName, inspectionsSubtitle, ""),
            NavReferenceSearchRow("sire", "SIRE 2.0", sireSubtitle, later),
            NavReferenceSearchRow("viq", "VIQ7", viqSubtitle, later),
            NavReferenceSearchRow("equipment", localized("Оборудование", "Equipment", lang),
                equipmentSubtitle, ""),
            NavReferenceSearchRow("personal", personalName,
                localized("Мануалы и файлы", "Manuals and files", lang), ""),
        ), query,
    ) + if (query.isBlank()) emptyList() else
        equipmentMatches.map {
            NavReferenceSearchHit("equipment", NavReferenceField.TITLE, 0)
        } + (manualMatches.map { it.sectionId } +
            excerptMatches.map { it.sectionId }).map { section ->
            NavReferenceSearchHit(
                when (section) {
                    NavReferenceSections.SIRE -> "sire"
                    NavReferenceSections.VIQ7 -> "viq"
                    else -> "personal"
                }, NavReferenceField.TITLE, 0)
        }
    var searchResultIndex by rememberSaveable { mutableIntStateOf(0) }
    val focusedSearchHit = searchResults.getOrNull(searchResultIndex)
    var searchNavigationSerial by remember { mutableIntStateOf(0) }
    LaunchedEffect(query) { searchResultIndex = 0 }
    LaunchedEffect(searchNavigationSerial) {
        if (searchNavigationSerial > 0 && searchResults.isNotEmpty()) {
            val hit = searchResults[searchResultIndex.coerceIn(0, searchResults.lastIndex)]
            val visible = buildList {
                if (showInspections) add("inspections")
                if (showEquipment) add("equipment")
                if (showPersonal) add("personal")
            }
            val rootSection = if (hit.sectionId == "sire" || hit.sectionId == "viq")
                "inspections" else hit.sectionId
            val index = visible.indexOf(rootSection)
            if (index >= 0) listState.animateScrollToItem(index)
        }
    }

    LaunchedEffect(smoothReturnTick) {
        if (smoothReturnTick > 0) {
            listState.animateScrollToItem(0)
            searchOpen = false
            searchText = ""
            equipmentOpen = false
            equipmentSearch = ""
            inspectionsOpen = false
            exchangeOpen = false
            manualSection = null
            manualSearch = ""
        }
    }
    // An upgrade may restore SIRE/VIQ7 as the current destination. Redirect
    // to the inspections overview without opening the catalog or touching files.
    LaunchedEffect(manualSection) {
        if (manualSection != null && !referenceLibraryRouteAvailable(manualSection)) {
            inspectionsOpen = true
            manualSection = null
            manualSearch = ""
        }
    }
    if (referenceLibraryRouteAvailable(manualSection)) {
        ReferenceManualLibraryScreen(
            sectionId = manualSection!!, p = p, lang = lang,
            initialQuery = manualSearch,
            onBack = { manualSection = null; manualSearch = "" },
        )
    } else if (equipmentOpen && catalog != null) {
        ReferenceEquipmentScreen(
            catalog = catalog!!, p = p, lang = lang,
            initialQuery = equipmentSearch,
            onBack = { equipmentOpen = false; equipmentSearch = "" },
            persist = { updated ->
                runCatching { equipmentStore.write(updated) }
                    .onSuccess { catalog = updated }.isSuccess
            },
        )
    } else if (exchangeOpen) {
        fun leaveExchange() {
            // Imported equipment becomes immediately visible on the main Reference hub.
            // Preserve the old catalog on read failure; never reset storage to empty.
            catalog = runCatching { equipmentStore.read() }.getOrNull()
            exchangeOpen = false
        }
        BackHandler { leaveExchange() }
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically) {
                ReferenceHubAction(p, "back", localized("Назад", "Back", lang)) { leaveExchange() }
                Spacer(Modifier.width(12.dp))
                Text(localized("Импорт и экспорт", "Import and export", lang),
                    color = p.text, fontSize = 21.sp)
            }
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                // All v2 PDF/DOCX SHA validation and conflict safeguards are unchanged.
                ReferenceEquipmentSettingsPanel(profileId = profileId, p = p, lang = lang)
                Spacer(Modifier.height(125.dp))
            }
        }
    } else if (inspectionsOpen || (manualSection != null && !referenceLibraryRouteAvailable(manualSection))) {
        BackHandler { inspectionsOpen = false; manualSection = null; manualSearch = "" }
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically) {
                ReferenceHubAction(p, "back", localized("Назад", "Back", lang)) { inspectionsOpen = false; manualSection = null; manualSearch = "" }
                Spacer(Modifier.width(12.dp))
                Text(inspectionsName, color = p.text, fontSize = 21.sp)
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp, top = 8.dp, end = 16.dp, bottom = 125.dp),
                verticalArrangement = Arrangement.spacedBy(NavEventLayout.sectionGap),
            ) {
                if (showSire || inspectionsMatch) {
                    item(key = "inspection-sire") {
                        ReferencePlaceholderTile(
                            p, "SIRE 2.0", sireSubtitle, later, query,
                            activeHit = focusedSearchHit?.takeIf { it.sectionId == "sire" },
                            unavailable = true,
                            onClick = null,
                        )
                    }
                }
                if (showViq || inspectionsMatch) {
                    item(key = "inspection-viq") {
                        ReferencePlaceholderTile(
                            p, "VIQ7", viqSubtitle, later, query,
                            activeHit = focusedSearchHit?.takeIf { it.sectionId == "viq" },
                            unavailable = true,
                            onClick = null,
                        )
                    }
                }
            }
        }
    } else Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(localized("Материалы для работы", "Working references", lang),
                    color = p.text, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Text(localized("Оборудование и мои материалы доступны офлайн.",
                    "Equipment and my materials are available offline.", lang),
                    color = p.muted, fontSize = 12.sp)
                Text(localized("Онлайн-библиотека — в разработке", "Online library — in development", lang),
                    color = NavTodayVisualTokens.accentInk(p.dark), fontSize = 12.sp)
            }
            Spacer(Modifier.width(8.dp))
            ReferenceHubAction(p, "search", localized("Поиск", "Search", lang)) {
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
            placeholder = localized("Поиск по справочнику", "Search references", lang),
            neutralStyle = true, highContrastField = true,
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
        Box(Modifier.padding(horizontal = 16.dp)) { NavEventDivider(p) }
        LazyColumn(
            modifier = Modifier.weight(1f)
                .revealSearchOnSettledTopPull(listState, searchOpen) { searchOpen = true }
                .dismissSearchImeOnResultsGesture(searchOpen),
            state = listState,
            contentPadding = PaddingValues(
                start = 16.dp, top = 6.dp, end = 16.dp, bottom = 125.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(NavEventLayout.sectionGap),
        ) {
            if (query.isNotBlank() && !showInspections &&
                !showEquipment && !showPersonal) {
                item {
                    Column(Modifier.fillMaxWidth().personalNoteCard(p, NavEventLayout.cardRadius).padding(14.dp)) {
                        Text(localized("Разделов по запросу не найдено.",
                            "No reference sections match the search.", lang),
                            color = p.muted, fontSize = 13.sp)
                    }
                }
            }
            if (showInspections) {
                item(key = "reference-inspections") {
                    ReferencePlaceholderTile(
                        p, inspectionsName, inspectionsSubtitle,
                        later,
                        query,
                        activeHit = focusedSearchHit?.takeIf {
                            it.sectionId == "inspections"
                        },
                        glyph = "check", statusAccent = true,
                        onClick = { inspectionsOpen = true },
                    )
                }
            }
            if (showEquipment) {
                item(key = "reference-equipment") {
                    if (catalog == null) {
                        Column(Modifier.fillMaxWidth().personalNoteCard(p, NavEventLayout.cardRadius).padding(14.dp)) {
                            Text(localized("Справочник оборудования недоступен: данные требуют проверки. Ничего не перезаписано.",
                                "Equipment storage needs inspection. No data was overwritten.", lang),
                                color = p.systemRed, fontSize = 13.sp)
                        }
                    } else {
                        ReferencePlaceholderTile(
                            p, localized("Оборудование", "Equipment", lang),
                            equipmentSubtitle,
                            if (equipmentMatches.isNotEmpty())
                                localized("Найдено: ", "Found: ", lang) + equipmentMatches.size
                            else localized("Свои папки, оборудование и процедуры",
                                "Your folders, equipment and procedures", lang),
                            query,
                            activeHit = focusedSearchHit?.takeIf { it.sectionId == "equipment" },
                            glyph = "work-order",
                            onClick = {
                                equipmentSearch = query
                                equipmentOpen = true
                            },
                        )
                    }
                }
            }
            if (showPersonal) {
                item(key = "reference-personal") {
                    if (manuals == null) {
                        Column(Modifier.fillMaxWidth().personalNoteCard(p, NavEventLayout.cardRadius).padding(14.dp)) {
                            Text(localized("Библиотека требует проверки. Данные не перезаписаны.",
                                "Manual library needs inspection. Data was preserved.", lang),
                                color = p.systemRed, fontSize = 13.sp)
                        }
                    } else {
                        ReferencePlaceholderTile(
                            p, personalName,
                            localized("Свои PDF, Word и папки", "Your PDF, Word and folders", lang),
                            localized("Документов: ", "Documents: ", lang) +
                                (manuals.documents.count {
                                    it.sectionId == NavReferenceSections.PERSONAL
                                } + manuals.excerpts.count {
                                    it.sectionId == NavReferenceSections.PERSONAL
                                }), query,
                            activeHit = focusedSearchHit?.takeIf { it.sectionId == "personal" },
                            onClick = {
                                manualSection = NavReferenceSections.PERSONAL
                                manualSearch = query
                            },
                        )
                    }
                }
            }
            // Keep the existing independent equipment exchange entry point below the three categories.
            if (query.isBlank()) {
                item(key = "reference-exchange") {
                    Box(
                        Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        TextButton(
                            onClick = { exchangeOpen = true },
                            modifier = Modifier.widthIn(min = 174.dp).heightIn(min = 46.dp)
                                .personalNoteCard(p, 18.dp),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                        ) {
                            Text(localized("Импорт и экспорт", "Import and export", lang),
                                color = NavTodayVisualTokens.accentInk(p.dark), fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

/** Reference category tiles open independent manual and equipment catalogs. */
@Composable
private fun ReferencePlaceholderTile(
    p: NavPalette,
    title: String,
    subtitle: String,
    footer: String,
    query: String = "",
    activeHit: NavReferenceSearchHit? = null,
    glyph: String = "reference",
    unavailable: Boolean = false,
    statusAccent: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    Row(
        Modifier.fillMaxWidth()
            .personalNoteCard(p, NavEventLayout.cardRadius)
            .then(if (!unavailable && onClick != null) Modifier.glassClickable(onClick)
                else if (unavailable) Modifier.semantics { disabled() } else Modifier)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(42.dp).personalNoteCard(p, 13.dp, nested = true),
            contentAlignment = Alignment.Center,
        ) {
            NavGlassGlyph(glyph, p.text, Modifier.size(22.dp))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            NavSearchMarkedText(title, query, p, fontSize = 16.sp,
                activeMatchStart = activeHit?.takeIf {
                    it.field == NavReferenceField.TITLE }?.start)
            NavSearchMarkedText(subtitle, query, p, color = p.muted,
                fontSize = 12.sp, activeMatchStart = activeHit?.takeIf {
                    it.field == NavReferenceField.SUBTITLE }?.start)
            NavSearchMarkedText(footer, query, p,
                color = if (unavailable || statusAccent) NavTodayVisualTokens.accentInk(p.dark) else p.muted,
                fontSize = 11.sp, activeMatchStart = activeHit?.takeIf {
                    it.field == NavReferenceField.FOOTER }?.start)
        }
        if (!unavailable && onClick != null) {
            NavGlassGlyph("chevron-right", p.muted, Modifier.size(17.dp))
        }
    }
}

@Composable
private fun ReferenceHubAction(p: NavPalette, kind: String, label: String, onClick: () -> Unit) {
    ReferenceLiquidGlassAction(p, kind, label, onClick)
}
