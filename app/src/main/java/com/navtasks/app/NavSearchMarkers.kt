package com.navtasks.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Only begin filtering and marking after two entered characters. */
internal fun navEffectiveSearchQuery(input: String): String =
    input.trim().takeIf { it.length >= 2 }.orEmpty()

/** Case-insensitive, non-overlapping textual occurrences. */
internal fun navSearchRanges(text: String, query: String): List<IntRange> {
    val token = query.trim()
    if (token.length < 2) return emptyList()
    val result = mutableListOf<IntRange>()
    var from = 0
    while (from < text.length) {
        val at = text.indexOf(token, startIndex = from, ignoreCase = true)
        if (at < 0) break
        result.add(at until at + token.length)
        from = at + token.length // deliberate non-overlapping marker spans
    }
    return result
}

/**
 * Positive delta scrolls list content upward; never requests scrolling on an
 * ancestor Window or the search-field container. Keep the match inside the
 * LazyColumn viewport with a margin for comfortable reading.
 */
internal fun navSearchListScrollDelta(
    matchY: Float, top: Float, bottom: Float, margin: Float,
): Float {
    val safeTop = top + margin
    val safeBottom = (bottom - margin).coerceAtLeast(safeTop)
    return when {
        matchY < safeTop -> matchY - safeTop
        matchY > safeBottom -> matchY - safeBottom
        else -> 0f
    }
}

/** Handover arrows deliberately align each hit near the top, even if already visible.
 * The fixed search bar is outside the LazyColumn and must never be moved.
 * Unlike other search screens, merely keeping the hit inside the viewport is not enough.
 */
internal fun navHandoverSearchTopScrollDelta(
    matchY: Float, top: Float, margin: Float,
): Float {
    val delta = matchY - (top + margin)
    return if (kotlin.math.abs(delta) <= 6f) 0f else delta
}

internal fun navSearchNextIndex(current: Int, count: Int, delta: Int): Int =
    if (count <= 0) 0 else Math.floorMod(current.coerceIn(0, count - 1) + delta, count)

internal fun navSearchAnnotatedText(
    text: String, query: String, p: NavPalette, focused: Boolean = false,
    activeMatchStart: Int? = null,
): AnnotatedString = buildAnnotatedString {
    append(text)
    for (range in navSearchRanges(text, query)) {
        val strong = range.first == activeMatchStart ||
            (activeMatchStart == null && focused)
        addStyle(SpanStyle(
            background = if (strong) p.systemOrange else
                p.systemOrange.copy(alpha = if (p.dark) .46f else .33f),
            color = if (strong) Color.Black else p.text,
            fontWeight = FontWeight.SemiBold,
        ), range.first, range.last + 1)
    }
}

/** Search-only visual marking. Original rich HTML stays untouched in storage. */
@Composable
internal fun NavSearchMarkedText(
    value: String,
    query: String,
    p: NavPalette,
    modifier: Modifier = Modifier,
    color: Color = p.text,
    fontSize: TextUnit = 14.sp,
    fontWeight: FontWeight? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    focused: Boolean = false,
    activeMatchStart: Int? = null,
    onTextLayout: (TextLayoutResult) -> Unit = {},
) {
    Text(
        text = navSearchAnnotatedText(value, query, p, focused, activeMatchStart),
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontWeight = fontWeight,
        maxLines = maxLines,
        overflow = overflow,
        onTextLayout = onTextLayout,
    )
}

/** Simple semantic controls; the same shared palette works in Day and Night. */
@Composable
internal fun NavSearchMatchControls(
    count: Int,
    current: Int,
    p: NavPalette,
    lang: UiLanguage,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        Text(
            text = "${if (count > 0) current.coerceIn(0, count - 1) + 1 else 0} / $count",
            color = p.warningText, fontSize = 11.sp,
        )
        Spacer(Modifier.width(2.dp))
        NavSearchNavArrow(up = true, enabled = count > 0, p = p,
            label = localized("Предыдущее совпадение", "Previous result", lang),
            onClick = onPrevious)
        NavSearchNavArrow(up = false, enabled = count > 0, p = p,
            label = localized("Следующее совпадение", "Next result", lang),
            onClick = onNext)
    }
}

@Composable
private fun NavSearchNavArrow(
    up: Boolean, enabled: Boolean, p: NavPalette,
    label: String, onClick: () -> Unit,
) {
    val base = Modifier.size(32.dp).semantics { contentDescription = label }
    Box(
        base.then(if (enabled) Modifier.glassClickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        NavGlassGlyph("chevron-down", if (enabled) p.warningText else p.muted,
            Modifier.size(17.dp).rotate(if (up) 180f else 0f))
    }
}


/** Every matching text fragment is an independent result. */
internal enum class NavHandoverField { SECTION_TITLE, ENTRY_TITLE, ENTRY_NOTES }
internal data class NavHandoverSearchHit(
    val sectionId: String,
    val entryId: String? = null,
    val field: NavHandoverField = NavHandoverField.SECTION_TITLE,
    val start: Int = 0,
)
internal fun navHandoverSearchHits(
    sections: List<HandoverSection>, query: String,
): List<NavHandoverSearchHit> {
    val token = navEffectiveSearchQuery(query)
    if (token.isEmpty()) return emptyList()
    return buildList {
        for (section in sections) {
            for (range in navSearchRanges(section.title, token))
                add(NavHandoverSearchHit(section.id, field = NavHandoverField.SECTION_TITLE,
                    start = range.first))
            for (entry in section.entries) {
                for (range in navSearchRanges(entry.title, token))
                    add(NavHandoverSearchHit(section.id, entry.id,
                        NavHandoverField.ENTRY_TITLE, range.first))
                for (range in navSearchRanges(entry.notes, token))
                    add(NavHandoverSearchHit(section.id, entry.id,
                        NavHandoverField.ENTRY_NOTES, range.first))
            }
        }
    }
}

internal enum class NavReferenceField { TITLE, SUBTITLE, FOOTER }
internal data class NavReferenceSearchRow(
    val id: String, val title: String, val subtitle: String, val footer: String,
)
internal data class NavReferenceSearchHit(
    val sectionId: String, val field: NavReferenceField, val start: Int,
)
internal fun navReferenceSearchHits(
    rows: List<NavReferenceSearchRow>, query: String,
): List<NavReferenceSearchHit> {
    val token = navEffectiveSearchQuery(query)
    if (token.isEmpty()) return emptyList()
    return buildList {
        for (row in rows) {
            for (range in navSearchRanges(row.title, token))
                add(NavReferenceSearchHit(row.id, NavReferenceField.TITLE, range.first))
            for (range in navSearchRanges(row.subtitle, token))
                add(NavReferenceSearchHit(row.id, NavReferenceField.SUBTITLE, range.first))
            for (range in navSearchRanges(row.footer, token))
                add(NavReferenceSearchHit(row.id, NavReferenceField.FOOTER, range.first))
        }
    }
}
