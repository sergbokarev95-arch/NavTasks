package com.navtasks.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

/** Shared profile-only card material; nested dialog rows are slightly lifted for contrast. */
internal object NavProfileVisualTokens {
    fun cardSurface(dark: Boolean, nested: Boolean): Color =
        if (dark && nested) Color(0xFF2A2A2C) else NavTodayVisualTokens.cardSurface(dark)
}

internal fun Modifier.profileCardStyle(
    p: NavPalette, selected: Boolean, radius: Dp, nested: Boolean = false,
): Modifier {
    val shape = RoundedCornerShape(radius)
    return clip(shape)
        .background(NavProfileVisualTokens.cardSurface(p.dark, nested), shape)
        .then(if (selected) Modifier.border(
            NavTodayVisualTokens.selectedDateRimDp.dp,
            NavTodayVisualTokens.accentInk(p.dark), shape,
        ) else Modifier)
}

/** Physically meaningful two-finger pinch thresholds; pure/JVM-testable. */
internal fun isQuickProfilePinch(
    initialSpan: Float,
    currentSpan: Float,
    minimumInitialSpan: Float,
    minimumContraction: Float,
): Boolean =
    initialSpan >= minimumInitialSpan &&
    initialSpan - currentSpan >= minimumContraction &&
    currentSpan <= initialSpan * 0.75f

/**
 * Observes but NEVER consumes pointer changes; preserves one-finger scrolling,
 * tab swipes, edge Back, and child touch events.
 */
internal fun Modifier.quickProfilePinchGesture(
    enabled: Boolean,
    onDetected: () -> Unit,
): Modifier = if (!enabled) this else this.pointerInput(enabled) {
    val minimumInitialSpan = 96.dp.toPx()
    val minimumContraction = 48.dp.toPx()
    awaitEachGesture {
        awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
        var initialSpan: Float? = null
        var trackedIds: Set<androidx.compose.ui.input.pointer.PointerId>? = null
        while (true) {
            val pressed = awaitPointerEvent(pass = PointerEventPass.Initial)
                .changes.filter { it.pressed }
            if (pressed.isEmpty() || pressed.size > 2) break
            if (pressed.size != 2) {
                initialSpan = null
                trackedIds = null
                continue
            }
            val ids = pressed.map { it.id }.toSet()
            val span = (pressed[0].position - pressed[1].position).getDistance()
            if (ids != trackedIds) {
                trackedIds = ids
                initialSpan = span
                continue
            }
            if (isQuickProfilePinch(
                    initialSpan ?: span, span, minimumInitialSpan, minimumContraction
                )
            ) {
                onDetected()
                break
            }
        }
    }
}

/**
 * Quick switcher, NOT profile management. Calls ProfileStore + four existing
 * data-store switches through the owning root callback.
 */
@Composable
internal fun NavQuickProfilePicker(
    profiles: List<NavProfile>,
    activeId: String,
    p: NavPalette,
    lang: UiLanguage,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
) {
    val frost = navPopupFrostAvailable()
    Dialog(onDismissRequest = onDismiss) {
        NavApplyDialogFrost(frost, radius = 26.dp)
        Column(
            Modifier.fillMaxWidth().widthIn(max = 380.dp)
                .navPopupGlassSurface(p, frosted = frost, radius = 26.dp)
                .padding(horizontal = 18.dp, vertical = 19.dp),
            verticalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            Text(
                localized("Переключить профиль", "Switch profile", lang),
                color = p.text, fontSize = 21.sp,
            )
            Text(
                localized("Выбери профиль одним нажатием", "Choose a profile with one tap", lang),
                color = p.muted, fontSize = 13.sp,
            )
            Column(
                Modifier.fillMaxWidth().heightIn(max = 360.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                profiles.forEach { profile ->
                    val selected = profile.id == activeId
                    Row(
                        Modifier.fillMaxWidth()
                            .profileCardStyle(p, selected, radius = 16.dp, nested = true)
                            .clickable { onSelect(profile.id) }
                            .padding(horizontal = 14.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                profile.title, color = p.text, fontSize = 16.sp,
                                maxLines = 1, overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                if (profile.kind == NavProfileKind.PERSONAL)
                                    localized("Личное", "Personal", lang)
                                else localized("Судовой профиль", "Vessel profile", lang),
                                color = p.muted, fontSize = 12.sp,
                            )
                        }
                        if (selected) NavGlassGlyph("check", NavTodayVisualTokens.accentInk(p.dark), Modifier.size(23.dp))
                    }
                }
            }
        }
    }
}
