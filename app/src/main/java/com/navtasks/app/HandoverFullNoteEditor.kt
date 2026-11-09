package com.navtasks.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** One fullscreen Notes-style editor for both Personal and vessel Handover.
 * Original profile-scoped HandoverStore fields and completion state stay intact.
 * Only explicit Save persists; Back discards the draft.
 */
@Composable
internal fun HandoverFullNoteEditor(
    p: NavPalette, lang: UiLanguage, personalMode: Boolean,
    sectionTitle: String, entryKey: String,
    initialTitle: String, initialPlain: String, initialHtml: String,
    onSave: (String, String, String) -> Unit, onCancel: () -> Unit,
) {
    var title by remember(entryKey) { mutableStateOf(initialTitle) }
    var plain by remember(entryKey) { mutableStateOf(initialPlain) }
    var html by remember(entryKey) { mutableStateOf(initialHtml) }
    val canSave = title.isNotBlank() && navRichNoteAllowed(plain, html, 4000)
    Column(
        Modifier.fillMaxSize().background(p.background)
            .statusBarsPadding().navigationBarsPadding().imePadding()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NavGlassCircleButton(p, "back", onClick = onCancel)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    if (personalMode) localized("Личное", "Personal", lang)
                    else localized("Передача дел", "Handover", lang),
                    color = p.blueText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                )
                if (sectionTitle.isNotEmpty()) {
                    Text(sectionTitle, color = p.muted, fontSize = 12.sp, maxLines = 1)
                }
            }
            Box(Modifier.navGlass(p, radius = 13.dp,
                role = if (canSave) GlassRole.SELECTED else GlassRole.CONTROL)) {
                TextButton(
                    enabled = canSave,
                    onClick = { onSave(title, plain, html) },
                    modifier = Modifier.heightIn(min = 44.dp),
                    contentPadding = PaddingValues(horizontal = 15.dp, vertical = 6.dp),
                ) {
                    Text(
                        localized("Сохранить", "Save", lang),
                        color = if (canSave) p.selectionContent else p.muted,
                        fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
        BasicTextField(
            value = title, onValueChange = { title = it.take(200) },
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                .semantics {
                    contentDescription = localized("Название заметки", "Note title", lang)
                },
            singleLine = false, maxLines = 3,
            keyboardOptions = NavTitleKeyboard,
            textStyle = TextStyle(
                color = p.text, fontSize = 24.sp, lineHeight = 29.sp,
                fontWeight = FontWeight.Bold,
            ),
            cursorBrush = SolidColor(p.blue),
            decorationBox = { inner ->
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.TopStart) {
                    if (title.isEmpty()) Text(
                        localized("Название", "Title", lang),
                        color = p.muted, fontSize = 24.sp, fontWeight = FontWeight.Bold,
                    )
                    inner()
                }
            },
        )
        Box(Modifier.fillMaxWidth().weight(1f).padding(bottom = 8.dp)) {
            NavRichNoteEditor(
                initialPlain = initialPlain, initialHtml = initialHtml,
                entryKey = entryKey, p = p, lang = lang,
                placeholder = localized("Текст заметки", "Write a note", lang),
                maxCharacters = 4000, fullPage = true,
                onChange = { updatedPlain, updatedHtml ->
                    plain = updatedPlain
                    html = updatedHtml
                },
            )
        }
    }
}
