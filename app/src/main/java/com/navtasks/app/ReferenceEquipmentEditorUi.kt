package com.navtasks.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
    onSave: (ReferenceFolder) -> Unit,
) {
    var name by remember(original?.id) { mutableStateOf(original?.title.orEmpty()) }
    NavGlassAlertDialog(p, onDismissRequest = onDismiss,
        title = { Text(localized(if (original == null) "Новая папка" else "Название папки",
            if (original == null) "New folder" else "Rename folder", lang)) },
        text = {
            NavGlassInput(name, { name = it.take(120) }, p,
                placeholder = localized("Название", "Name", lang),
                modifier = Modifier.fillMaxWidth())
        },
        confirmButton = {
            TextButton(enabled = name.isNotBlank(), onClick = {
                onSave((original ?: ReferenceFolder(title = name.trim(), parentId = parentId))
                    .copy(title = name.trim()))
            }) { Text(localized("Сохранить", "Save", lang), color = p.blueText) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(localized("Отмена", "Cancel", lang), color = p.muted)
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
    NavGlassAlertDialog(p, onDismissRequest = onDismiss,
        title = { Text(localized(if (original == null) "Добавить оборудование" else "Оборудование",
            if (original == null) "Add equipment" else "Equipment", lang)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
        },
        confirmButton = {
            TextButton(enabled = title.isNotBlank(), onClick = {
                val draft = (original ?: ReferenceEquipment(title = title.trim(), folderId = folderId))
                    .copy(title = title.trim(), manufacturer = maker.trim(),
                        model = model.trim(), serialNumber = serial.trim(),
                        location = location.trim(), notes = notes)
                onSave(draft)
            }) { Text(localized("Сохранить", "Save", lang), color = p.blueText) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(localized("Отмена", "Cancel", lang), color = p.muted)
            }
        },
    )
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
    val validPage = page.isBlank() || page.toIntOrNull() in 1..100000
    val canSave = title.isNotBlank() && manual.isNotBlank() && excerpt.isNotBlank() && validPage

    NavGlassAlertDialog(p, onDismissRequest = onDismiss,
        title = { Text(localized(if (original == null) "Новая процедура" else "Редактор процедуры",
            if (original == null) "New procedure" else "Edit procedure", lang)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
        },
        confirmButton = {
            TextButton(enabled = canSave, onClick = {
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
                )
                onSave(draft)
            }) { Text(localized("Сохранить", "Save", lang), color = p.blueText) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(localized("Отмена", "Cancel", lang), color = p.muted)
            }
        },
    )
}

@Composable
private fun ReferenceField(
    label: String, value: String, onValueChange: (String) -> Unit,
    p: NavPalette, multiline: Boolean = false,
) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(label, color = p.muted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        if (multiline) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                keyboardOptions = NavSentenceKeyboard,
                textStyle = TextStyle(color = p.text, fontSize = 14.sp, lineHeight = 20.sp),
                cursorBrush = SolidColor(p.blue),
                minLines = 3,
                maxLines = 7,
                modifier = Modifier.fillMaxWidth().heightIn(min = 104.dp, max = 180.dp)
                    .navGlass(p, radius = 12.dp, role = GlassRole.CONTROL)
                    .padding(12.dp),
            )
        } else {
            NavGlassInput(value, onValueChange, p, modifier = Modifier.fillMaxWidth())
        }
    }
}
