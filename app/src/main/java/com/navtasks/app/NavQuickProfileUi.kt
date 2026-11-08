package com.navtasks.app

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.sqrt

/**
 * Pinch-in must be intentional, not a two-finger scroll or the beginning
 * of a one-finger primary-tab swipe. Thresholds are pure and unit-tested.
 */
internal fun isQuickProfilePinch(
    initial: Float, current: Float,
    minimumInitial: Float, minimumContraction: Float
): Boolean = initial >= minimumInitial &&
    initial - current >= minimumContraction &&
    current <= initial * .75f

/**
 * Observes gestures on the parent while leaving all one-finger interactions
 * untouched. No pointer changes are consumed until the pinch is recognized.
 * Disabled on editor, calendar and full-screen management routes.
 */
fun Modifier.navQuickProfilePinch(enabled: Boolean, onPinch: () -> Unit): Modifier {
    if (!enabled) return this
    return pointerInput(enabled) {
        val minInitial = 100.dp.toPx()
        val minContraction = 52.dp.toPx()
        awaitEachGesture {
            awaitFirstDown(requireUnconsumed = false)
            var initialDistance: Float? = null
            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                val pressed = event.changes.filter { it.pressed }
                if (pressed.size > 2 || pressed.isEmpty()) break
                if (initialDistance != null && pressed.size != 2) break
                if (pressed.size == 2) {
                    val dx = pressed[0].position.x - pressed[1].position.x
                    val dy = pressed[0].position.y - pressed[1].position.y
                    val current = sqrt(dx * dx + dy * dy)
                    val start = initialDistance ?: current.also { initialDistance = it }
                    if (isQuickProfilePinch(start, current, minInitial, minContraction)) {
                        event.changes.forEach { it.consume() }
                        onPinch()
                        break
                    }
                }
            }
        }
    }
}

/**
 * Quick selector uses the same ProfileStore IDs and activation path as
 * ProfilesScreen; it never creates, merges or deletes profile data.
 * The parent blurs the underlying screen while this panel remains crisp.
 */
@Composable
fun NavQuickProfilePicker(
    profiles: List<NavProfile>,
    activeId: String,
    p: NavPalette,
    lang: UiLanguage,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Box(
            Modifier.fillMaxSize()
                .background(if (p.dark) Color.Black.copy(alpha = .40f)
                    else Color(0xFF203447).copy(alpha = .24f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null, onClick = onDismiss
                )
        )
        Column(
            Modifier.fillMaxWidth().widthIn(max = 410.dp)
                .padding(horizontal = 24.dp)
                .navDialogSurface(p, radius = 28.dp)
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(localized("Быстрый выбор профиля", "Switch profile", lang),
                        color = p.text, fontSize = 19.sp, fontWeight = FontWeight.Medium)
                    Text(localized("Сведение двух пальцев", "Two-finger pinch", lang),
                        color = p.muted, fontSize = 12.sp)
                }
                Box(
                    Modifier.size(38.dp).navGlass(p, radius = 19.dp, role = GlassRole.CONTROL)
                        .glassClickable(onDismiss),
                    contentAlignment = Alignment.Center
                ) {
                    NavGlassGlyph("x", p.muted, Modifier.size(20.dp))
                }
            }
            Column(
                Modifier.fillMaxWidth().heightIn(max = 440.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                profiles.forEach { profile ->
                    val selected = profile.id == activeId
                    val shape = RoundedCornerShape(17.dp)
                    Row(
                        Modifier.fillMaxWidth()
                            .clip(shape)
                            .navGlass(p, radius = 17.dp,
                                role = if (selected) GlassRole.NESTED_TASK else GlassRole.PANEL)
                            .glassClickable { onSelect(profile.id) }
                            .padding(horizontal = 15.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(profile.title, color = if (selected) p.blue else p.text,
                                fontSize = 16.sp, maxLines = 1,
                                overflow = TextOverflow.Ellipsis)
                            Text(
                                if (profile.kind == NavProfileKind.PERSONAL)
                                    localized("Личное", "Personal", lang)
                                else localized("Судовой профиль", "Vessel profile", lang),
                                color = p.muted, fontSize = 12.sp
                            )
                        }
                        if (selected) NavGlassGlyph("check", p.blue, Modifier.size(21.dp))
                    }
                }
            }
        }
    }
}
