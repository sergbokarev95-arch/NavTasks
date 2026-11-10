package com.navtasks.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
    smoothReturnTick: Int = 0,
) {
    val listState = rememberLazyListState()
    var searchOpen by rememberSaveable { mutableStateOf(false) }
    var searchText by rememberSaveable { mutableStateOf("") }
    val query = searchText.trim()
    val sireSubtitle = localized("Подготовка к инспекциям", "Inspection preparation", lang)
    val viqSubtitle = localized("Архивный справочник", "Historical reference", lang)
    val later = localized("Материалы будут добавлены позже", "Content coming later", lang)
    val showSire = referencePlaceholderMatches(query, "SIRE 2.0", sireSubtitle, later)
    val showViq = referencePlaceholderMatches(query, "VIQ7", viqSubtitle, later)
    val searchResults = if (searchOpen && query.isNotBlank()) buildList {
        if (showSire) add("sire")
        if (showViq) add("viq")
    } else emptyList()
    var searchResultIndex by rememberSaveable { mutableIntStateOf(0) }
    var searchNavigationSerial by remember { mutableIntStateOf(0) }
    LaunchedEffect(query) { searchResultIndex = 0 }
    LaunchedEffect(searchNavigationSerial) {
        if (searchNavigationSerial > 0 && searchResults.isNotEmpty()) {
            listState.animateScrollToItem(
                searchResultIndex.coerceIn(0, searchResults.lastIndex))
        }
    }

    LaunchedEffect(smoothReturnTick) {
        if (smoothReturnTick > 0) {
            listState.animateScrollToItem(0)
            searchOpen = false
            searchText = ""
        }
    }
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NavInfoSectionBanner(
                p = p,
                title = localized("Материалы для работы", "Working references", lang),
                description = localized(
                    "Справочник будет постепенно наполняться после релиза 1.0.0.",
                    "Reference materials will be added after version 1.0.0.",
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
            if (query.isNotBlank() && !showSire && !showViq) {
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
                        p, "SIRE 2.0", sireSubtitle, later, query,
                        focused = searchResults.getOrNull(searchResultIndex) == "sire",
                    )
                }
            }
            if (showViq) {
                item(key = "reference-viq") {
                    ReferencePlaceholderTile(
                        p, "VIQ7", viqSubtitle, later, query,
                        focused = searchResults.getOrNull(searchResultIndex) == "viq",
                    )
                }
            }
        }
    }
}

/** Still non-clickable; real reference content is deferred until after v1.0.0. */
@Composable
private fun ReferencePlaceholderTile(
    p: NavPalette,
    title: String,
    subtitle: String,
    footer: String,
    query: String = "",
    focused: Boolean = false,
) {
    Row(
        Modifier.fillMaxWidth()
            .navGlass(p, radius = 19.dp, role = GlassRole.TASK)
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
            NavSearchMarkedText(title, query, p, fontSize = 16.sp, focused = focused)
            NavSearchMarkedText(subtitle, query, p, color = p.muted,
                fontSize = 12.sp, focused = focused)
            Spacer(Modifier.height(5.dp))
            NavSearchMarkedText(footer, query, p, color = p.muted,
                fontSize = 11.sp, focused = focused)
        }
    }
}
