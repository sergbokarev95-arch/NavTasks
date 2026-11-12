package com.navtasks.app

import androidx.compose.foundation.layout.*
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

/** Only the two existing reference placeholders can be searched before 1.0.0. */
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
    val manualStore = remember(context) { ReferenceManualStore(context) }
    var manualSection by rememberSaveable { mutableStateOf<String?>(null) }
    var manualSearch by rememberSaveable { mutableStateOf("") }
    val manualSnapshot = remember(manualSection) { runCatching { manualStore.read() } }
    val manuals = manualSnapshot.getOrNull()
    val listState = rememberLazyListState()
    var searchOpen by rememberSaveable { mutableStateOf(false) }
    var searchText by rememberSaveable { mutableStateOf("") }
    val query = if (searchOpen) navEffectiveSearchQuery(searchText) else ""
    val sireSubtitle = localized("Подготовка к инспекциям", "Inspection preparation", lang)
    val viqSubtitle = localized("Архивный справочник", "Historical reference", lang)
    val later = localized("Материалы будут добавлены позже", "Content coming later", lang)
    val manualMatches = if (query.isBlank()) emptyList()
        else manuals?.let { referenceManualSearch(it, query) }.orEmpty()
    val sireHits = manualMatches.count { it.sectionId == NavReferenceSections.SIRE }
    val viqHits = manualMatches.count { it.sectionId == NavReferenceSections.VIQ7 }
    val personalHits = manualMatches.count { it.sectionId == NavReferenceSections.PERSONAL }
    val showSire = referencePlaceholderMatches(query, "SIRE 2.0", sireSubtitle) || sireHits > 0
    val showViq = referencePlaceholderMatches(query, "VIQ7", viqSubtitle) || viqHits > 0
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
        } + manualMatches.map { file ->
            NavReferenceSearchHit(
                when (file.sectionId) {
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
                if (showSire) add("sire")
                if (showViq) add("viq")
                if (showEquipment) add("equipment")
                if (showPersonal) add("personal")
            }
            val index = visible.indexOf(hit.sectionId)
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
            manualSection = null
            manualSearch = ""
        }
    }
    if (manualSection != null) {
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
    } else Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NavInfoSectionBanner(
                p = p,
                title = localized("Материалы для работы", "Working references", lang),
                description = localized(
                    "Мануалы PDF/Word и инструкции к оборудованию. Всё доступно офлайн.",
                    "PDF/Word manuals and equipment procedures. Available offline.",
                    lang,
                ),
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(8.dp))
            NavGlassCircleButton(p, "search") {
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
        LazyColumn(
            modifier = Modifier.weight(1f)
                .revealSearchOnSettledTopPull(listState, searchOpen) { searchOpen = true }
                .dismissSearchImeOnResultsGesture(searchOpen),
            state = listState,
            contentPadding = PaddingValues(
                start = 16.dp, top = 6.dp, end = 16.dp, bottom = 125.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (query.isNotBlank() && !showSire && !showViq &&
                !showEquipment && !showPersonal) {
                item {
                    NavPanel(p) {
                        Text(localized("Разделов по запросу не найдено.",
                            "No reference sections match the search.", lang),
                            color = p.muted, fontSize = 13.sp)
                    }
                }
            }
            if (showSire) {
                item(key = "reference-sire") {
                    ReferencePlaceholderTile(
                        p, "SIRE 2.0", sireSubtitle,
                        localized("Документов: ", "Documents: ", lang) +
                            manuals?.documents.orEmpty().count { it.sectionId == NavReferenceSections.SIRE },
                        query,
                        activeHit = focusedSearchHit?.takeIf { it.sectionId == "sire" },
                        onClick = {
                            manualSection = NavReferenceSections.SIRE
                            manualSearch = query
                        },
                    )
                }
            }
            if (showViq) {
                item(key = "reference-viq") {
                    ReferencePlaceholderTile(
                        p, "VIQ7", viqSubtitle,
                        localized("Документов: ", "Documents: ", lang) +
                            manuals?.documents.orEmpty().count { it.sectionId == NavReferenceSections.VIQ7 },
                        query,
                        activeHit = focusedSearchHit?.takeIf { it.sectionId == "viq" },
                        onClick = {
                            manualSection = NavReferenceSections.VIQ7
                            manualSearch = query
                        },
                    )
                }
            }
            if (showEquipment) {
                item(key = "reference-equipment") {
                    if (catalog == null) {
                        NavPanel(p) {
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
                        NavPanel(p) {
                            Text(localized("Библиотека требует проверки. Данные не перезаписаны.",
                                "Manual library needs inspection. Data was preserved.", lang),
                                color = p.systemRed, fontSize = 13.sp)
                        }
                    } else {
                        ReferencePlaceholderTile(
                            p, personalName,
                            localized("Свои PDF, Word и папки", "Your PDF, Word and folders", lang),
                            localized("Документов: ", "Documents: ", lang) +
                                manuals.documents.count {
                                    it.sectionId == NavReferenceSections.PERSONAL
                                }, query,
                            activeHit = focusedSearchHit?.takeIf { it.sectionId == "personal" },
                            onClick = {
                                manualSection = NavReferenceSections.PERSONAL
                                manualSearch = query
                            },
                        )
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
    onClick: (() -> Unit)? = null,
) {
    Row(
        Modifier.fillMaxWidth()
            .navGlass(p, radius = 19.dp, role = GlassRole.TASK)
            .then(if (onClick != null) Modifier.glassClickable(onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            Modifier.size(39.dp).navGlass(p, radius = 13.dp, role = GlassRole.CONTROL),
            contentAlignment = Alignment.Center,
        ) {
            NavGlassGlyph("reference", p.muted, Modifier.size(22.dp))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            NavSearchMarkedText(title, query, p, fontSize = 16.sp,
                activeMatchStart = activeHit?.takeIf {
                    it.field == NavReferenceField.TITLE }?.start)
            NavSearchMarkedText(subtitle, query, p, color = p.muted,
                fontSize = 12.sp, activeMatchStart = activeHit?.takeIf {
                    it.field == NavReferenceField.SUBTITLE }?.start)
            Spacer(Modifier.height(5.dp))
            NavSearchMarkedText(footer, query, p, color = p.muted,
                fontSize = 11.sp, activeMatchStart = activeHit?.takeIf {
                    it.field == NavReferenceField.FOOTER }?.start)
        }
    }
}
