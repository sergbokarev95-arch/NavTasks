package com.navtasks.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
    val initialFields = listOf(original?.title.orEmpty(), original?.text.orEmpty(),
        original?.sourceTitle.orEmpty(), original?.sourceSection.orEmpty(),
        original?.sourcePage?.toString().orEmpty(), original?.sourceEdition.orEmpty())
    val currentFields = listOf(title, text, source, section, page, edition)
    val canSave = title.isNotBlank() && text.isNotBlank() && validPage && currentFields != initialFields
    ReferenceEditorSheet(p, lang,
        localized(if (original == null) "Новая выдержка" else "Редактировать выдержку",
            if (original == null) "New excerpt" else "Edit excerpt", lang), canSave,
        onDismiss = onDismiss, onSave = {
            onSave(ReferenceManualExcerpt(
                id = original?.id ?: navReferenceId(),
                title = title.trim(),
                text = text.trim(),
                sectionId = original?.sectionId ?: sectionId,
                folderId = referenceExcerptFolderForSave(original, folderId),
                sourceTitle = source.trim(),
                sourceSection = section.trim(),
                sourcePage = page.toIntOrNull(),
                sourceEdition = edition.trim(),
            ))
        }) {
        ReferenceField(localized("Название *", "Title *", lang), title, { title = it.take(160) }, p)
        ReferenceField(localized("Текст выдержки *", "Excerpt text *", lang), text, { text = it.take(40000) }, p, multiline = true)
        ReferenceField(localized("Источник (необязательно)", "Source (optional)", lang), source, { source = it.take(220) }, p)
        ReferenceField(localized("Раздел / пункт", "Section", lang), section, { section = it.take(160) }, p)
        ReferenceField(localized("Страница", "Page", lang), page, { page = it.filter(Char::isDigit).take(6) }, p)
        ReferenceField(localized("Редакция", "Edition", lang), edition, { edition = it.take(100) }, p)
        Text(localized("Справочный текст. Не создаёт задач, сроков и уведомлений.",
            "Reference text only. No tasks, deadlines or reminders.", lang), color = p.muted, fontSize = 12.sp)
    }
}

@Composable
internal fun ReferenceManualExcerptReader(
    excerpt: ReferenceManualExcerpt, p: NavPalette, lang: UiLanguage,
    onRemove: () -> Unit,
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())
        .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 125.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (excerpt.sourceTitle.isNotBlank()) ReferenceManualPanel(p) {
            Text(localized("Источник", "Source", lang), color = NavTodayVisualTokens.accentInk(p.dark),
                fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text(excerpt.sourceTitle, color = p.text, fontSize = 15.sp)
            val details = listOfNotNull(
                excerpt.sourceSection.takeIf { it.isNotBlank() },
                excerpt.sourcePage?.let { localized("Стр. ", "Page ", lang) + it },
                excerpt.sourceEdition.takeIf { it.isNotBlank() },
            ).joinToString(" · ")
            if (details.isNotBlank()) Text(details, color = p.muted, fontSize = 12.sp)
        }
        ReferenceManualPanel(p) {
            Text(excerpt.title, color = p.text, fontSize = 17.sp,
                fontWeight = FontWeight.Medium)
            Text(excerpt.text, color = p.text, fontSize = 15.sp, lineHeight = 23.sp)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Box(Modifier.personalNoteCard(p, 14.dp)) {
                TextButton(onClick = onRemove, modifier = Modifier.heightIn(min = 44.dp)) {
                    Text(localized("Удалить выдержку", "Delete excerpt", lang), color = p.systemRed)
                }
            }
        }
    }
}
