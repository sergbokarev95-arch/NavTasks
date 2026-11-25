package com.navtasks.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Reuses NavTasks glass dialogs; all entered fields are standalone reference text. */
@Composable
internal fun ReferenceFolderEditor(
    original: ReferenceFolder?,
    parentId: String?,
    p: NavPalette,
    lang: UiLanguage,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)? = null,
    onSave: (ReferenceFolder) -> Unit,
) {
    var name by remember(original?.id) { mutableStateOf(original?.title.orEmpty()) }
    val canSave = name.isNotBlank() && name.trim() != original?.title.orEmpty().trim()
    NavGlassAlertDialog(p, onDismissRequest = onDismiss, neutralSurface = true, neutralActions = true,
        title = { Text(localized(if (original == null) "Новая папка" else "Название папки",
            if (original == null) "New folder" else "Rename folder", lang)) },
        text = {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BasicTextField(value = name, onValueChange = { name = it.take(120) },
                    singleLine = true, textStyle = TextStyle(color = p.text, fontSize = 15.sp),
                    cursorBrush = SolidColor(NavTodayVisualTokens.accentInk(p.dark)),
                    modifier = Modifier.fillMaxWidth().personalNoteCard(p, 12.dp, nested = true),
                    decorationBox = { inner ->
                        Box(Modifier.fillMaxWidth().heightIn(min = 44.dp).padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart) {
                            if (name.isEmpty()) Text(localized("Название", "Name", lang),
                                color = p.muted, fontSize = 15.sp)
                            inner()
                        }
                    })
                if (onDelete != null) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Box(Modifier.personalNoteCard(p, 14.dp, nested = true)) {
                        TextButton(onClick = onDelete, modifier = Modifier.heightIn(min = 44.dp)) {
                            Text(localized("Удалить папку", "Delete folder", lang), color = p.systemRed)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(enabled = canSave, onClick = {
                onSave((original ?: ReferenceFolder(title = name.trim(), parentId = parentId))
                    .copy(title = name.trim()))
            }) { Text(localized("Сохранить", "Save", lang), color = if (canSave)
                NavTodayVisualTokens.accentInk(p.dark) else p.muted) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(localized("Отмена", "Cancel", lang), color = p.text)
            }
        },
    )
}

@Composable
internal fun ReferenceEquipmentEditor(
    original: ReferenceEquipment?,
    folderId: String?,
    p: NavPalette,
    lang: UiLanguage,
    onDismiss: () -> Unit,
    onSave: (ReferenceEquipment) -> Unit,
) {
    var title by remember(original?.id) { mutableStateOf(original?.title.orEmpty()) }
    var maker by remember(original?.id) { mutableStateOf(original?.manufacturer.orEmpty()) }
    var model by remember(original?.id) { mutableStateOf(original?.model.orEmpty()) }
    var serial by remember(original?.id) { mutableStateOf(original?.serialNumber.orEmpty()) }
    var location by remember(original?.id) { mutableStateOf(original?.location.orEmpty()) }
    var notes by remember(original?.id) { mutableStateOf(original?.notes.orEmpty()) }
    val currentFields = listOf(title.trim(), maker.trim(), model.trim(), serial.trim(), location.trim(), notes)
    val initialFields = remember(original?.id) {
        listOf(original?.title.orEmpty().trim(), original?.manufacturer.orEmpty().trim(),
            original?.model.orEmpty().trim(), original?.serialNumber.orEmpty().trim(),
            original?.location.orEmpty().trim(), original?.notes.orEmpty())
    }
    val canSave = title.isNotBlank() && currentFields != initialFields
    ReferenceEditorSheet(p, lang, localized(if (original == null) "Новое оборудование" else "Редактировать оборудование",
        if (original == null) "New equipment" else "Edit equipment", lang), canSave, onDismiss, onSave = {
            val draft = (original ?: ReferenceEquipment(title = title.trim(), folderId = folderId))
                .copy(title = title.trim(), manufacturer = maker.trim(),
                    model = model.trim(), serialNumber = serial.trim(),
                    location = location.trim(), notes = notes)
            onSave(draft)
    }) {
        ReferenceField(localized("Название *", "Name *", lang), title,
            { title = it.take(160) }, p)
        ReferenceField(localized("Производитель", "Manufacturer", lang), maker,
            { maker = it.take(160) }, p)
        ReferenceField(localized("Модель", "Model", lang), model,
            { model = it.take(160) }, p)
        ReferenceField(localized("Серийный номер", "Serial number", lang), serial,
            { serial = it.take(160) }, p)
        ReferenceField(localized("Расположение", "Location", lang), location,
            { location = it.take(160) }, p)
        ReferenceField(localized("Описание", "Description", lang), notes,
            { notes = it.take(12000) }, p, multiline = true)
    }
}

@Composable
internal fun ReferenceProcedureEditor(
    original: ReferenceProcedure?,
    p: NavPalette,
    lang: UiLanguage,
    onDismiss: () -> Unit,
    onSave: (ReferenceProcedure) -> Unit,
) {
    var title by remember(original?.id) { mutableStateOf(original?.title.orEmpty()) }
    var interval by remember(original?.id) { mutableStateOf(original?.intervalDescription.orEmpty()) }
    var manual by remember(original?.id) { mutableStateOf(original?.source?.documentTitle.orEmpty()) }
    var section by remember(original?.id) { mutableStateOf(original?.source?.section.orEmpty()) }
    var page by remember(original?.id) {
        mutableStateOf(original?.source?.page?.toString().orEmpty())
    }
    var edition by remember(original?.id) { mutableStateOf(original?.source?.edition.orEmpty()) }
    var excerpt by remember(original?.id) { mutableStateOf(original?.excerpt.orEmpty()) }
    var steps by remember(original?.id) { mutableStateOf(original?.instructions.orEmpty()) }
    val validPage = page.isBlank() || (page.toIntOrNull()?.let { it in 1..100000 } == true)

    val currentFields = listOf(title.trim(), interval.trim(), manual.trim(), section.trim(),
        page.toIntOrNull(), edition.trim(), excerpt.trim(), steps)
    val initialFields = remember(original?.id) { listOf(original?.title.orEmpty().trim(),
        original?.intervalDescription.orEmpty().trim(), original?.source?.documentTitle.orEmpty().trim(),
        original?.source?.section.orEmpty().trim(), original?.source?.page,
        original?.source?.edition.orEmpty().trim(), original?.excerpt.orEmpty().trim(),
        original?.instructions.orEmpty()) }
    val canSave = title.isNotBlank() && manual.isNotBlank() && excerpt.isNotBlank() && validPage &&
        currentFields != initialFields
    ReferenceEditorSheet(p, lang, localized(if (original == null) "Новая процедура" else "Редактировать процедуру",
        if (original == null) "New procedure" else "Edit procedure", lang), canSave, onDismiss, onSave = {
            val draft = ReferenceProcedure(
                id = original?.id ?: navReferenceId(),
                title = title.trim(),
                intervalDescription = interval.trim(),
                excerpt = excerpt.trim(),
                source = ReferenceSource(
                    documentTitle = manual.trim(),
                    page = page.toIntOrNull(),
                    section = section.trim(),
                    edition = edition.trim(),
                ),
                instructions = steps,
                // Editing textual instructions must not silently detach a manual.
                attachmentSha256 = original?.attachmentSha256,
                attachmentMimeType = original?.attachmentMimeType,
                attachmentFilename = original?.attachmentFilename.orEmpty(),
            )
            onSave(draft)
    }) {
        Text(localized("Информационная запись. Сроки и напоминания здесь не создаются.",
            "Reference only. No scheduled tasks or reminders are created.", lang),
            color = p.muted, fontSize = 12.sp)
        ReferenceField(localized("Процедура *", "Procedure *", lang), title,
            { title = it.take(160) }, p)
        ReferenceField(localized("Периодичность (по мануалу)", "Interval (from manual)", lang),
            interval, { interval = it.take(120) }, p)
        ReferenceField(localized("Название источника *", "Source document *", lang),
            manual, { manual = it.take(200) }, p)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.weight(1f)) {
                ReferenceField(localized("Раздел", "Section", lang), section,
                    { section = it.take(160) }, p)
            }
            Box(Modifier.weight(.45f)) {
                ReferenceField(localized("Стр.", "Page", lang), page,
                    { value -> page = value.filter(Char::isDigit).take(6) }, p)
            }
        }
        ReferenceField(localized("Редакция документа", "Document edition", lang),
            edition, { edition = it.take(100) }, p)
        ReferenceField(localized("Выдержка из мануала *", "Manual excerpt *", lang),
            excerpt, { excerpt = it.take(20000) }, p, multiline = true)
        ReferenceField(localized("Как выполняется / примечания", "Steps / notes", lang),
            steps, { steps = it.take(20000) }, p, multiline = true)
        Text(localized("* Обязательные поля", "* Required fields", lang),
            color = p.muted, fontSize = 11.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReferenceEditorSheet(
    p: NavPalette, lang: UiLanguage, heading: String, canSave: Boolean,
    onDismiss: () -> Unit, onSave: () -> Unit, content: @Composable ColumnScope.() -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = navEditorSheetSurface(p), contentColor = p.text,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = { Box(Modifier.padding(top = 8.dp, bottom = 4.dp)
            .size(width = 42.dp, height = 4.dp)
            .background(p.muted.copy(alpha = .58f), RoundedCornerShape(4.dp))) }) {
        Column(Modifier.fillMaxWidth().fillMaxHeight(.94f).imePadding()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically) {
                NavEventHeaderAction("x", localized("Отмена", "Cancel", lang), p, true, onDismiss)
                Text(heading, color = p.text, fontSize = 19.sp, textAlign = TextAlign.Center,
                    maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                NavEventHeaderAction("check", localized("Сохранить", "Save", lang), p, canSave,
                    accent = true, onClick = onSave)
            }
            Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(horizontal = NavEventLayout.screenInset),
                verticalArrangement = Arrangement.spacedBy(NavEventLayout.sectionGap)) {
                content()
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun ReferenceField(
    label: String, value: String, onValueChange: (String) -> Unit,
    p: NavPalette, multiline: Boolean = false,
) {
    Column(Modifier.fillMaxWidth().personalNoteCard(p, NavEventLayout.cardRadius)
        .padding(horizontal = 14.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, color = p.muted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        BasicTextField(value = value, onValueChange = onValueChange,
            singleLine = !multiline, minLines = if (multiline) 3 else 1,
            maxLines = if (multiline) 8 else 1, keyboardOptions = NavSentenceKeyboard,
            textStyle = TextStyle(color = p.text, fontSize = 15.sp, lineHeight = 21.sp),
            cursorBrush = SolidColor(NavTodayVisualTokens.accentInk(p.dark)),
            modifier = Modifier.fillMaxWidth().heightIn(min = if (multiline) 104.dp else 44.dp)
                .personalNoteCard(p, 12.dp, nested = true),
            decorationBox = { inner ->
                Box(Modifier.fillMaxWidth().heightIn(min = if (multiline) 104.dp else 44.dp)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                    contentAlignment = if (multiline) Alignment.TopStart else Alignment.CenterStart) {
                    inner()
                }
            })
    }
}
