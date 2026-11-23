package com.navtasks.app

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.window.PopupPositionProvider

/**
 * Use the SAME keys and values already supported by TaskEditorScreen.
 * These are display options, never a new scheduling / notification model.
 */
internal fun navTaskFrequencyChoices(lang: UiLanguage): List<Pair<String, String>> =
    Frequency.entries.map { frequency ->
        frequency.name to when (frequency) {
            Frequency.DAILY -> localized("День", "Daily", lang)
            Frequency.WEEKLY -> localized("Неделя", "Weekly", lang)
            Frequency.MONTHLY -> localized("Месяц", "Monthly", lang)
            Frequency.EXTRA -> localized("Разовая", "One-off", lang)
        }
    }

internal fun navTaskRepeatChoices(lang: UiLanguage): List<Pair<String, String>> = listOf(
    ScheduleMode.CALENDAR.name to localized("По расписанию", "By schedule", lang),
    ScheduleMode.AFTER_COMPLETION.name to localized("От выполнения", "From completion", lang),
)

internal fun navTaskReminderChoices(lang: UiLanguage): List<Pair<String, String>> = listOf(
    "0" to localized("В срок", "At deadline", lang),
    "15" to localized("За 15 минут", "15 minutes before", lang),
    "60" to localized("За час", "1 hour before", lang),
    "1440" to localized("За день", "1 day before", lang),
)

/**
 * The popup lives in the SAME layout as the tapped field, so Compose provides
 * the actual field bounds to PopupPositionProvider, including after scrolling.
 */
@Composable
internal fun NavTaskChoiceField(
    title: String,
    selected: String,
    options: List<Pair<String, String>>,
    p: NavPalette,
    lang: UiLanguage,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val currentLabel = options.firstOrNull { it.first == selected }?.second
        ?: localized("Выбрать", "Choose", lang)
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val fieldWidth = maxWidth
        Row(
            Modifier.fillMaxWidth()
                .navGlass(p, radius = 15.dp, role = GlassRole.CONTROL, elevation = 0.dp)
                .glassClickable { expanded = true }
                .heightIn(min = 48.dp)
                .padding(start = 15.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(currentLabel, modifier = Modifier.weight(1f), fontSize = 15.sp,
                color = p.text, maxLines = 1)
            NavGlassGlyph("chevron-right", p.muted, Modifier.size(17.dp))
        }
        if (expanded) {
            NavTaskChoiceDialog(
                title = title, selected = selected, options = options,
                p = p, lang = lang, popupWidth = fieldWidth,
                onDismiss = { expanded = false },
                onSelect = { key ->
                    onSelect(key)
                    expanded = false
                },
            )
        }
    }
}

/** Places the menu against the actual tapped control, never at a screen edge. */
private class NavTaskChoicePositionProvider(
    private val gapPx: Int,
    private val onPlacement: (Boolean) -> Unit,
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val below = windowSize.height - anchorBounds.bottom
        val above = anchorBounds.top
        val placeAbove = below < popupContentSize.height + gapPx && above > below
        onPlacement(placeAbove)
        val maxX = (windowSize.width - popupContentSize.width - gapPx).coerceAtLeast(gapPx)
        val x = anchorBounds.left.coerceIn(gapPx, maxX)
        val preferredY = if (placeAbove) {
            anchorBounds.top - popupContentSize.height - gapPx
        } else {
            anchorBounds.bottom + gapPx
        }
        val maxY = (windowSize.height - popupContentSize.height - gapPx)
            .coerceAtLeast(gapPx)
        return IntOffset(x, preferredY.coerceIn(gapPx, maxY))
    }
}

/**
 * OPAQUE, field-anchored choice list. A small alpha/scale transition changes
 * only drawing (never layout), preventing the moving/right-edge Dialog glitch.
 * Cancel, Back, or outside touch never commits the temporary selection.
 */
@Composable
internal fun NavTaskChoiceDialog(
    title: String,
    selected: String,
    options: List<Pair<String, String>>,
    p: NavPalette,
    lang: UiLanguage,
    popupWidth: Dp,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
) {
    var draft by remember(selected, options) { mutableStateOf(selected) }
    var closing by remember { mutableStateOf(false) }
    var confirm by remember { mutableStateOf(false) }
    var above by remember { mutableStateOf(false) }
    val motion = remember { Animatable(0f) }
    val gapPx = with(LocalDensity.current) { 8.dp.roundToPx() }
    val placement = remember(gapPx) {
        NavTaskChoicePositionProvider(gapPx) { above = it }
    }

    fun close(accept: Boolean) {
        if (closing) return
        confirm = accept
        closing = true
    }
    LaunchedEffect(closing) {
        if (closing) {
            motion.animateTo(0f, tween(115))
            if (confirm) onSelect(draft) else onDismiss()
        } else {
            // Same restrained, critically damped emergence shown in the
            // reference: expand vertically from the touched field without
            // sliding a separate Android window across the screen.
            motion.animateTo(1f, spring(dampingRatio = .92f, stiffness = 780f))
        }
    }
    val surface = if (p.dark) Color(0xFF23272C) else Color(0xFFF8F9FA)
    val control = if (p.dark) Color(0xFF32373D) else Color(0xFFE8EAED)
    val shape = RoundedCornerShape(24.dp)

    Popup(
        popupPositionProvider = placement,
        onDismissRequest = { close(false) },
        properties = PopupProperties(
            focusable = true,
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        ),
    ) {
        Column(
            Modifier.width(popupWidth.coerceAtMost(410.dp))
                .graphicsLayer {
                    val progress = motion.value.coerceIn(0f, 1f)
                    alpha = progress
                    scaleX = .96f + .04f * progress
                    scaleY = .62f + .38f * progress
                    transformOrigin = TransformOrigin(.5f, if (above) 1f else 0f)
                }
                .shadow(12.dp, shape)
                .clip(shape)
                .background(surface, shape)
                .border(1.dp, p.border, shape)
                .padding(horizontal = 16.dp, vertical = 15.dp),
        ) {
            Text(title, color = p.text, fontSize = 19.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 4.dp, bottom = 10.dp))
            Column(
                Modifier.fillMaxWidth().heightIn(max = 320.dp)
                    .verticalScroll(rememberScrollState())
                    .selectableGroup(),
            ) {
                options.forEachIndexed { index, (key, label) ->
                    Row(
                        Modifier.fillMaxWidth().heightIn(min = 48.dp)
                            .selectable(
                                selected = draft == key,
                                enabled = !closing,
                                role = Role.RadioButton,
                                onClick = { draft = key },
                            )
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(label, color = p.text, fontSize = 15.sp,
                            modifier = Modifier.weight(1f))
                        RadioButton(
                            selected = draft == key,
                            onClick = null,
                            colors = RadioButtonDefaults.colors(
                                selectedColor = p.blueText,
                                unselectedColor = p.muted,
                            ),
                        )
                    }
                    if (index < options.lastIndex) {
                        HorizontalDivider(thickness = .6.dp, color = p.border,
                            modifier = Modifier.padding(horizontal = 8.dp))
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                Box(
                    Modifier.weight(1f).heightIn(min = 46.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .background(control)
                        .clickable(enabled = !closing) { close(false) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(localized("Отмена", "Cancel", lang), color = p.muted, fontSize = 14.sp)
                }
                Box(
                    Modifier.weight(1f).heightIn(min = 46.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .background(control)
                        .clickable(enabled = !closing) { close(true) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(localized("Выбрать", "Select", lang),
                        color = p.blueText, fontSize = 14.sp)
                }
            }
        }
    }
}
