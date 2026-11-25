package com.navtasks.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

/** Small, non-destructive reference button: it never touches check/uncheck state. */
@Composable
fun ChecklistReferenceButton(
    p: NavPalette,
    hasNote: Boolean,
    onClick: () -> Unit,
) {
    // Reference/help is informational, not a primary blue action.
    val accent = if (hasNote) NavTodayVisualTokens.accentInk(p.dark) else p.muted
    Box(
        Modifier.size(44.dp).clip(CircleShape)
            .glassClickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier.size(33.dp)
                .clip(CircleShape)
                .background(NavTodayVisualTokens.checklistInsetSurface(p.dark))
                .border(1.dp, NavTodayVisualTokens.checklistInsetRim(p.dark), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text("?", color = accent, fontSize = 17.sp, fontWeight = FontWeight.Medium)
        }
    }
}

/** Edit during template creation or view/copy during a scheduled check. */
@Composable
fun ChecklistReferenceDialog(
    p: NavPalette,
    lang: UiLanguage,
    title: String,
    note: String,
    noteHtml: String = "",
    editable: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit = { _, _ -> },
) {
    var draft by remember(title, note) { mutableStateOf(note) }
    var draftHtml by remember(title, noteHtml) { mutableStateOf(noteHtml) }
    var copied by remember(title, note) { mutableStateOf(false) }
    val clipboard = LocalClipboardManager.current
    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .background(NavTodayVisualTokens.cardSurface(p.dark))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                localized(
                    if (editable) "Комментарий к проверке" else "Памятка к проверке",
                    if (editable) "Edit checklist note" else "Checklist reference", lang,
                ),
                color = NavTodayVisualTokens.accentInk(p.dark), fontSize = 12.sp,
            )
            Text(title, color = p.text, fontSize = 20.sp, maxLines = 3)
            if (editable) {
                Text(
                    localized(
                        "Например, готовая запись для журнала ГМССБ. Текст сохранится для следующих проверок.",
                        "For example, journal wording. This note is reused for future checks.", lang,
                    ),
                    color = p.muted, fontSize = 12.sp,
                )
                NavRichNoteEditor(
                    initialPlain = note, initialHtml = noteHtml,
                    entryKey = title + ":" + note.hashCode(),
                    p = p, lang = lang, maxCharacters = 6000, neutralStyle = true,
                    placeholder = localized("Текст подсказки…", "Reference note…", lang),
                    onChange = { text, html -> draft = text; draftHtml = html },
                )
            } else {
                SelectionContainer {
                    Column(
                        Modifier.fillMaxWidth().heightIn(max = 340.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(NavTodayVisualTokens.checklistInsetSurface(p.dark))
                            .verticalScroll(rememberScrollState())
                            .padding(14.dp),
                    ) {
                        NavRichNoteText(note, noteHtml, p, fontSize = 14.sp)
                    }
                }
            }
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
            ) {
                if (!editable && copied)
                    Text(localized("Скопировано", "Copied", lang), color = p.mint,
                        fontSize = 11.sp, modifier = Modifier.weight(1f))
                ChecklistNoteAction(
                    p,
                    if (editable) localized("Отмена", "Cancel", lang)
                    else localized("Закрыть", "Close", lang),
                    accent = false,
                    onClick = onDismiss,
                )
                Spacer(Modifier.width(8.dp))
                ChecklistNoteAction(
                    p,
                    if (editable) localized("Сохранить", "Save", lang)
                    else localized("Копировать", "Copy", lang),
                    accent = true,
                    onClick = {
                        if (editable) {
                            if (navRichNoteAllowed(draft, draftHtml, 6000))
                                onSave(draft.trim(), draftHtml)
                        }
                        else {
                            clipboard.setText(AnnotatedString(note))
                            copied = true
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun ChecklistNoteAction(
    p: NavPalette,
    label: String,
    accent: Boolean,
    onClick: () -> Unit,
) {
    Box(
        Modifier.clip(RoundedCornerShape(12.dp))
            .background(NavTodayVisualTokens.checklistInsetSurface(p.dark))
            .glassClickable(onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = if (accent) NavTodayVisualTokens.accentInk(p.dark) else p.text, fontSize = 13.sp)
    }
}
