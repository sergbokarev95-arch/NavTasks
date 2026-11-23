package com.navtasks.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

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
 * A 48dp choice row shown in the existing editor.
 * The committed choice is read from TaskEditorScreen's unchanged local state.
 */
@Composable
internal fun NavTaskChoiceField(
    selected: String,
    options: List<Pair<String, String>>,
    p: NavPalette,
    lang: UiLanguage,
    onOpen: () -> Unit,
) {
    val currentLabel = options.firstOrNull { it.first == selected }?.second
        ?: localized("Выбрать", "Choose", lang)
    Row(
        Modifier.fillMaxWidth()
            .navGlass(p, radius = 15.dp, role = GlassRole.CONTROL, elevation = 0.dp)
            .glassClickable(onClick = onOpen)
            .heightIn(min = 48.dp)
            .padding(start = 15.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(currentLabel, modifier = Modifier.weight(1f), fontSize = 15.sp,
            color = p.text, maxLines = 1)
        NavGlassGlyph("chevron-right", p.muted, Modifier.size(17.dp))
    }
}

/**
 * System-backed frosted choice dialog. The tap first changes a draft; Cancel
 * never mutates the parent editor, while Select commits exactly one old key.
 * The dialog's own spring/fade transitions do not affect scheduler state.
 */
@Composable
internal fun NavTaskChoiceDialog(
    title: String,
    selected: String,
    options: List<Pair<String, String>>,
    p: NavPalette,
    lang: UiLanguage,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
) {
    var draft by remember(selected, options) { mutableStateOf(selected) }
    var closing by remember { mutableStateOf(false) }
    var confirm by remember { mutableStateOf(false) }
    val visibility = remember { MutableTransitionState(false) }
    LaunchedEffect(Unit) { visibility.targetState = true }

    fun close(accept: Boolean) {
        if (closing) return
        confirm = accept
        closing = true
        visibility.targetState = false
    }
    LaunchedEffect(closing, visibility.currentState, visibility.isIdle) {
        if (closing && visibility.isIdle && !visibility.currentState) {
            if (confirm) onSelect(draft) else onDismiss()
        }
    }
    val frosted = navPopupFrostAvailable()
    Dialog(
        onDismissRequest = { close(false) },
        properties = DialogProperties(dismissOnBackPress = true,
            dismissOnClickOutside = true),
    ) {
        NavApplyDialogFrost(frosted)
        AnimatedVisibility(
            visibleState = visibility,
            enter = fadeIn(tween(170)) + scaleIn(
                initialScale = 0.97f, animationSpec = spring(
                    dampingRatio = 1f, stiffness = 550f)),
            exit = fadeOut(tween(125)) + scaleOut(
                targetScale = 0.985f, animationSpec = tween(125)),
        ) {
            Column(
                Modifier.fillMaxWidth().widthIn(max = 410.dp)
                    .navPopupGlassSurface(p, frosted = frosted, radius = 28.dp)
                    .padding(horizontal = 18.dp, vertical = 17.dp),
            ) {
                Text(title, color = p.text, fontSize = 21.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 5.dp, bottom = 12.dp))

                Column(
                    Modifier.fillMaxWidth().heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState())
                        .selectableGroup(),
                ) {
                    options.forEachIndexed { index, (key, label) ->
                        Row(
                            Modifier.fillMaxWidth().heightIn(min = 53.dp)
                                .selectable(
                                    selected = draft == key,
                                    enabled = !closing,
                                    role = Role.RadioButton,
                                    onClick = { draft = key },
                                ).padding(horizontal = 10.dp),
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
                            HorizontalDivider(
                                thickness = .6.dp,
                                color = p.border.copy(alpha = if (p.dark) .54f else .68f),
                                modifier = Modifier.padding(horizontal = 10.dp),
                            )
                        }
                    }
                }
                Spacer(Modifier.height(13.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(Modifier.weight(1f)
                        .navGlass(p, radius = 18.dp, role = GlassRole.CONTROL, elevation = 0.dp),
                    ) {
                        TextButton(onClick = { close(false) }, enabled = !closing,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 45.dp)) {
                            Text(localized("Отмена", "Cancel", lang), color = p.muted)
                        }
                    }
                    Box(Modifier.weight(1f)
                        .navGlass(p, radius = 18.dp, role = GlassRole.CONTROL, elevation = 0.dp),
                    ) {
                        TextButton(onClick = { close(true) }, enabled = !closing,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 45.dp)) {
                            Text(localized("Выбрать", "Select", lang), color = p.blueText)
                        }
                    }
                }
            }
        }
    }
}
