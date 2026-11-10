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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A result is one visible card (or a Handover section header), not every
 * repeated occurrence within a note. Text within each matching result may
 * contain multiple highlighted spans.
 */
internal fun navSearchRanges(text: String, query: String): List<IntRange> {
    val token = query.trim()
    if (token.isEmpty()) return emptyList()
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

internal fun navSearchNextIndex(current: Int, count: Int, delta: Int): Int =
    if (count <= 0) 0 else Math.floorMod(current.coerceIn(0, count - 1) + delta, count)

internal fun navSearchAnnotatedText(
    text: String, query: String, p: NavPalette, focused: Boolean = false,
): AnnotatedString = buildAnnotatedString {
    append(text)
    val background = if (focused) p.systemOrange else
        p.systemOrange.copy(alpha = if (p.dark) .46f else .33f)
    for (range in navSearchRanges(text, query)) {
        addStyle(SpanStyle(
            background = background,
            color = if (focused) Color.Black else p.text,
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
) {
    Text(
        text = navSearchAnnotatedText(value, query, p, focused),
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontWeight = fontWeight,
        maxLines = maxLines,
        overflow = overflow,
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
        Modifier.fillMaxWidth().padding(start = 16.dp, end = 18.dp, bottom = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
    ) {
        Text(
            text = "${if (count > 0) current.coerceIn(0, count - 1) + 1 else 0} / $count",
            color = p.warningText, fontSize = 12.sp,
        )
        Spacer(Modifier.width(8.dp))
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
    val base = Modifier.size(40.dp).semantics { contentDescription = label }
    Box(
        base.then(if (enabled) Modifier.glassClickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        NavGlassGlyph("chevron-down", if (enabled) p.warningText else p.muted,
            Modifier.size(20.dp).rotate(if (up) 180f else 0f))
    }
}
