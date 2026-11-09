package com.navtasks.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Reference tab is intentionally a polished EMPTY shell until after mobile
 * v1.0.0. No inspection data, PDFs, document storage, permissions or routes.
 * VIQ7 is historical; SIRE 2.0 is the current inspection reference program.
 */
@Composable
internal fun ReferenceHubScreen(
    p: NavPalette,
    lang: UiLanguage,
    smoothReturnTick: Int = 0,
) {
    val listState = rememberLazyListState()
    LaunchedEffect(smoothReturnTick) {
        if (smoothReturnTick > 0) listState.animateScrollToItem(0)
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(
            start = 16.dp, top = 6.dp, end = 16.dp, bottom = 125.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            NavPanel(p) {
                Text(
                    localized("Материалы для работы", "Working references", lang),
                    color = p.text, fontSize = 17.sp,
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    localized(
                        "Справочник будет постепенно наполняться после релиза 1.0.0.",
                        "Reference materials will be added after version 1.0.0.",
                        lang,
                    ),
                    color = p.muted, fontSize = 12.sp,
                )
            }
        }
        item {
            ReferencePlaceholderTile(
                p, "SIRE 2.0",
                localized("Подготовка к инспекциям", "Inspection preparation", lang),
                localized("Материалы будут добавлены позже", "Content coming later", lang),
            )
        }
        item {
            ReferencePlaceholderTile(
                p, "VIQ7",
                localized("Архивный справочник", "Historical reference", lang),
                localized("Материалы будут добавлены позже", "Content coming later", lang),
            )
        }
    }
}

/** Deliberately non-interactive: no dead-end chevrons or fake navigation. */
@Composable
private fun ReferencePlaceholderTile(
    p: NavPalette,
    title: String,
    subtitle: String,
    footer: String,
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
            Text(title, color = p.text, fontSize = 16.sp)
            Text(subtitle, color = p.muted, fontSize = 12.sp)
            Spacer(Modifier.height(5.dp))
            Text(footer, color = p.muted, fontSize = 11.sp)
        }
    }
}
