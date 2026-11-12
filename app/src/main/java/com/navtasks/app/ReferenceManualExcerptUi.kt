package com.navtasks.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun ReferenceManualExcerptEditor(
    original: ReferenceManualExcerpt?,
    sectionId: String,
    folderId: String?,
    p: NavPalette,
    lang: UiLanguage,
    onDismiss: () -> Unit,
    onSave: (ReferenceManualExcerpt) -> Unit,
) {
    var title by remember(original?.id) { mutableStateOf(original?.title.orEmpty()) }
    var text by remember(original?.id) { mutableStateOf(original?.text.orEmpty()) }
    var source by remember(original?.id) { mutableStateOf(original?.sourceTitle.orEmpty()) }
    var section by remember(original?.id) { mutableStateOf(original?.sourceSection.orEmpty()) }
    var page by remember(original?.id) {
        mutableStateOf(original?.sourcePage?.toString().orEmpty())
    }
    var edition by remember(original?.id) { mutableStateOf(original?.sourceEdition.orEmpty()) }
    val validPage = page.isBlank() || (page.toIntOrNull()?.let { it in 1..100000 } == true)
    NavGlassAlertDialog(p, onDismissRequest = onDismiss,
        title = { Text(localized(if (original == null) "Новая выдержка" else "Редактировать выдержку",
            if (original == null) "New excerpt" else "Edit excerpt", lang)) },
        text = {
            Column(Modifier.heightIn(max = 440.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text(localized(
                    "Справочный текст. Не создаёт задач, сроков и уведомлений.",
                    "Reference text only. No tasks, deadlines or reminders.", lang),
                    color = p.muted, fontSize = 12.sp)
                ReferenceExcerptTextField(
                    localized("Название *", "Title *", lang), title,
                    { title = it.take(160) }, p)
                ReferenceExcerptTextField(
                    localized("Текст выдержки *", "Excerpt text *", lang), text,
                    { text = it.take(40000) }, p, multi = true)
                ReferenceExcerptTextField(
                    localized("Источник (необязательно)", "Source (optional)", lang), source,
                    { source = it.take(220) }, p)
                ReferenceExcerptTextField(localized("Раздел / пункт", "Section", lang),
                    section, { section = it.take(160) }, p)
                ReferenceExcerptTextField(localized("Страница", "Page", lang),
                    page, { page = it.filter(Char::isDigit).take(6) }, p)
                ReferenceExcerptTextField(localized("Редакция", "Edition", lang),
                    edition, { edition = it.take(100) }, p)
            }
        },
        confirmButton = {
            TextButton(enabled = title.isNotBlank() && text.isNotBlank() && validPage,
                onClick = {
                    onSave(ReferenceManualExcerpt(
                        id = original?.id ?: navReferenceId(),
                        title = title.trim(),
                        text = text.trim(),
                        sectionId = original?.sectionId ?: sectionId,
                        folderId = original?.folderId ?: folderId,
                        sourceTitle = source.trim(),
                        sourceSection = section.trim(),
                        sourcePage = page.toIntOrNull(),
                        sourceEdition = edition.trim(),
                    ))
                }) { Text(localized("Сохранить", "Save", lang), color = p.blueText) }
        },
        dismissButton = { TextButton(onClick = onDismiss) {
            Text(localized("Отмена", "Cancel", lang), color = p.muted)
        } },
    )
}

@Composable
private fun ReferenceExcerptTextField(
    title: String, value: String, update: (String) -> Unit,
    p: NavPalette, multi: Boolean = false,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, color = p.muted, fontSize = 12.sp)
        if (multi) {
            BasicTextField(value = value, onValueChange = update,
                textStyle = TextStyle(color = p.text, fontSize = 14.sp, lineHeight = 21.sp),
                cursorBrush = SolidColor(p.blue),
                keyboardOptions = NavSentenceKeyboard,
                minLines = 5, maxLines = 10,
                modifier = Modifier.fillMaxWidth()
                    .navGlass(p, radius = 12.dp, role = GlassRole.CONTROL)
                    .padding(12.dp))
        } else {
            NavGlassInput(value, update, p, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
internal fun ReferenceManualExcerptReader(
    excerpt: ReferenceManualExcerpt, p: NavPalette, lang: UiLanguage,
    onEdit: () -> Unit, onRemove: () -> Unit,
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())
        .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 125.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (excerpt.sourceTitle.isNotBlank()) NavPanel(p) {
            Text(localized("Источник", "Source", lang), color = p.blueText,
                fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text(excerpt.sourceTitle, color = p.text, fontSize = 15.sp)
            val details = listOfNotNull(
                excerpt.sourceSection.takeIf { it.isNotBlank() },
                excerpt.sourcePage?.let { localized("Стр. ", "Page ", lang) + it },
                excerpt.sourceEdition.takeIf { it.isNotBlank() },
            ).joinToString(" · ")
            if (details.isNotBlank()) Text(details, color = p.muted, fontSize = 12.sp)
        }
        NavPanel(p) {
            Text(excerpt.title, color = p.text, fontSize = 17.sp,
                fontWeight = FontWeight.Medium)
            Text(excerpt.text, color = p.text, fontSize = 15.sp, lineHeight = 23.sp)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = onEdit) {
                Text(localized("Редактировать", "Edit", lang), color = p.blueText)
            }
            TextButton(onClick = onRemove) {
                Text(localized("Удалить", "Delete", lang), color = p.systemRed)
            }
        }
    }
}
