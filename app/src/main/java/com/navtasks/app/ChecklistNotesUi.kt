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
    val accent = if (hasNote) p.blue else p.muted
    Box(
        Modifier.size(33.dp)
            .navGlass(p, radius = 17.dp, role = GlassRole.CONTROL)
            .border(1.dp, accent.copy(alpha = if (hasNote) 0.65f else 0.25f), CircleShape)
            .clip(CircleShape)
            .glassClickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text("?", color = accent, fontSize = 17.sp, fontWeight = FontWeight.Medium)
    }
}

/** Edit during template creation or view/copy during a scheduled check. */
@Composable
fun ChecklistReferenceDialog(
    p: NavPalette,
    lang: UiLanguage,
    title: String,
    note: String,
    editable: Boolean,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit = {},
) {
    var draft by remember(title, note) { mutableStateOf(note) }
    var copied by remember(title, note) { mutableStateOf(false) }
    val clipboard = LocalClipboardManager.current
    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier.fillMaxWidth()
                .navDialogSurface(p)
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                localized(
                    if (editable) "Комментарий к проверке" else "Памятка к проверке",
                    if (editable) "Edit checklist note" else "Checklist reference", lang,
                ),
                color = p.blue, fontSize = 12.sp,
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
                BasicTextField(
                    value = draft,
                    onValueChange = { draft = it.take(6000) },
                    textStyle = TextStyle(color = p.text, fontSize = 14.sp, lineHeight = 21.sp),
                    cursorBrush = SolidColor(p.blue),
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth()
                        .heightIn(min = 120.dp, max = 225.dp)
                        .navGlass(p, radius = 13.dp, role = GlassRole.CONTROL),
                    decorationBox = { inner ->
                        Box(
                            Modifier.fillMaxWidth().padding(13.dp),
                            contentAlignment = Alignment.TopStart,
                        ) {
                            if (draft.isEmpty())
                                Text(
                                    localized("Текст подсказки…", "Reference note…", lang),
                                    color = p.muted, fontSize = 13.sp,
                                )
                            inner()
                        }
                    },
                )
            } else {
                SelectionContainer {
                    Column(
                        Modifier.fillMaxWidth().heightIn(max = 340.dp)
                            .navGlass(p, radius = 13.dp, role = GlassRole.CONTROL)
                            .verticalScroll(rememberScrollState())
                            .padding(14.dp),
                    ) {
                        Text(note, color = p.text, fontSize = 14.sp, lineHeight = 21.sp)
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
                        if (editable) onSave(draft.trim())
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
        Modifier.navGlass(p, radius = 12.dp, role = GlassRole.CONTROL)
            .glassClickable(onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = if (accent) p.blue else p.muted, fontSize = 13.sp)
    }
}
