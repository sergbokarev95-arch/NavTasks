package com.navtasks.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

internal fun navEventRepeatChoices(lang: UiLanguage): List<Pair<String, String>> = listOf(
    Frequency.EXTRA.name to localized("Никогда (разовая)", "Never (one-off)", lang),
    Frequency.DAILY.name to localized("Ежедневно", "Daily", lang),
    Frequency.WEEKLY.name to localized("Еженедельно", "Weekly", lang),
    Frequency.MONTHLY.name to localized("Ежемесячно", "Monthly", lang),
    "custom" to localized("Настроить", "Customize", lang),
)

internal fun navEventRepeatLabel(frequency: Frequency, mode: ScheduleMode, custom: Boolean, lang: UiLanguage): String =
    navEventRepeatChoices(lang).first { it.first == if (custom || mode == ScheduleMode.AFTER_COMPLETION) "custom" else frequency.name }.second

internal fun navEventReminderLabel(minutes: Int, lang: UiLanguage): String = when (minutes) {
    0 -> localized("В срок / начало", "At scheduled time", lang)
    60 -> localized("За 1 час", "1 hour before", lang)
    120 -> localized("За 2 часа", "2 hours before", lang)
    1440 -> localized("За 1 день", "1 day before", lang)
    2880 -> localized("За 2 дня", "2 days before", lang)
    10080 -> localized("За 1 неделю", "1 week before", lang)
    else -> localized("За $minutes мин", "$minutes min before", lang)
}

internal fun navEventReminderChoices(lang: UiLanguage): List<Pair<String, String>> =
    listOf("none" to localized("Нет (убрать все)", "None (remove all)", lang)) +
        listOf(0, 5, 10, 15, 30, 60, 120, 1440, 2880, 10080).map { "$it" to navEventReminderLabel(it, lang) } +
        listOf("custom" to localized("Настроить", "Customize", lang))

@Composable
internal fun NavEventHeaderAction(kind: String, label: String, p: NavPalette, enabled: Boolean, onClick: () -> Unit) {
    IconButton(onClick = onClick, enabled = enabled, modifier = Modifier.size(44.dp).semantics { contentDescription = label }) {
        NavGlassGlyph(kind, if (enabled) p.text else p.muted, Modifier.size(25.dp))
    }
}

@Composable
internal fun NavEventTitle(title: String, onChange: (String) -> Unit, p: NavPalette, lang: UiLanguage) {
    BasicTextField(value = title, onValueChange = onChange, singleLine = true,
        modifier = Modifier.fillMaxWidth().personalNoteCard(p, 20.dp).heightIn(min = 52.dp),
        textStyle = TextStyle(color = p.text, fontSize = 16.sp),
        cursorBrush = SolidColor(NavTodayVisualTokens.accentInk(p.dark)),
        keyboardOptions = NavTitleKeyboard,
        decorationBox = { input ->
            Box(Modifier.fillMaxWidth().heightIn(min = 52.dp).padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart) {
                if (title.isEmpty()) Text(localized("Название задачи", "Task name", lang), color = p.muted, fontSize = 16.sp)
                input()
            }
        })
}

@Composable
internal fun NavEventDivider(p: NavPalette) {
    HorizontalDivider(thickness = .6.dp, color = NavTodayVisualTokens.checklistInsetRim(p.dark))
}

@Composable
internal fun NavEventValueRow(label: String, value: String, p: NavPalette, chevron: Boolean = false, expanded: Boolean? = null, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = 48.dp).glassClickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, color = p.text, fontSize = 15.sp, modifier = Modifier.weight(1f))
        if (value.isNotEmpty()) {
            if (chevron) Text(value, color = p.muted, fontSize = 13.sp)
            else Box(Modifier.personalNoteCard(p, 20.dp, nested = true).padding(horizontal = 14.dp, vertical = 8.dp)) {
                Text(value, color = p.text, fontSize = 13.sp)
            }
        }
        if (chevron) NavGlassGlyph(if (expanded == null) "chevron-right" else "chevron-down", p.muted,
            Modifier.size(17.dp).rotate(if (expanded == true) 180f else 0f))
    }
}

@Composable
internal fun NavEventChoiceDialog(
    title: String, options: List<Pair<String, String>>, selected: String?, p: NavPalette, lang: UiLanguage,
    confirmRequired: Boolean = true,
    onDismiss: () -> Unit, onSelect: (String) -> Unit,
) {
    var draft by remember(selected, options) { mutableStateOf(selected) }
    Dialog(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().personalNoteCard(p, 26.dp).padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, color = p.text, fontSize = 20.sp)
            Column(Modifier.fillMaxWidth().heightIn(max = 410.dp).verticalScroll(rememberScrollState())) {
                options.forEachIndexed { index, (key, label) ->
                    Row(Modifier.fillMaxWidth().heightIn(min = 48.dp)
                        .selectable(selected = draft == key, role = Role.RadioButton, onClick = { if (confirmRequired) draft = key else onSelect(key) })
                        .padding(horizontal = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(label, color = p.text, fontSize = 15.sp, modifier = Modifier.weight(1f))
                        RadioButton(selected = draft == key, onClick = null,
                            colors = RadioButtonDefaults.colors(selectedColor = NavTodayVisualTokens.accentInk(p.dark), unselectedColor = p.muted))
                    }
                    if (index < options.lastIndex) NavEventDivider(p)
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.weight(1f).personalNoteCard(p, 24.dp, nested = true)
                    .glassClickable(onClick = onDismiss).padding(13.dp), contentAlignment = Alignment.Center) {
                    Text(localized("Отмена", "Cancel", lang), color = p.text)
                }
                if (confirmRequired) Box(Modifier.weight(1f).personalNoteCard(p, 24.dp, nested = true)
                    .glassClickable { draft?.let(onSelect) }.padding(13.dp), contentAlignment = Alignment.Center) {
                    Text("OK", color = if (draft != null) NavTodayVisualTokens.accentInk(p.dark) else p.muted)
                }
            }
        }
    }
}
