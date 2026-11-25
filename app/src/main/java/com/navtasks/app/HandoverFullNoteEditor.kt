package com.navtasks.app

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
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
    initialPhotos: List<HandoverPhoto> = emptyList(),
    onSave: (String, String, String, List<HandoverPhoto>) -> Boolean,
    onCancel: () -> Unit,
    onDelete: (() -> Unit)? = null,
) {
    var title by remember(entryKey) { mutableStateOf(initialTitle) }
    var plain by remember(entryKey) { mutableStateOf(initialPlain) }
    var html by remember(entryKey) { mutableStateOf(initialHtml) }
    var photos by remember(entryKey) { mutableStateOf(initialPhotos) }
    var photoLoading by remember(entryKey) { mutableStateOf(false) }
    var photoError by remember(entryKey) { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents(),
    ) { uris ->
        if (uris.isNotEmpty() && !photoLoading) {
            val selected = uris.distinct().take(
                (HandoverPhoto.MAX_PER_ENTRY - photos.size).coerceAtLeast(0))
            scope.launch {
                photoLoading = true
                photoError = null
                try {
                    // Never save an incomplete photo list on partial decoding failure.
                    val converted = selected.map { uri ->
                        HandoverPhotoCodec.fromUri(context, uri)
                    }
                    photos = photos + converted
                } catch (e: Exception) {
                    photoError = e.localizedMessage ?: "Cannot attach photo"
                } finally {
                    photoLoading = false
                }
            }
        }
    }
    var saveError by remember(entryKey) { mutableStateOf(false) }
    var originalBody by remember(entryKey) { mutableStateOf(initialPlain to initialHtml) }
    val dirty = title.trim() != initialTitle.trim() || (plain to html) != originalBody || photos != initialPhotos
    // Only Personal notes may be saved without a title when they have a body.
    val canSave = (personalMode || dirty) && !photoLoading && navRichNoteAllowed(plain, html, 4000) &&
        (title.isNotBlank() || (personalMode && (plain.isNotBlank() || photos.isNotEmpty())))
    Column(
        Modifier.fillMaxSize().background(p.background)
            .statusBarsPadding().navigationBarsPadding().imePadding()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(top = if (personalMode) NavPersonalVisualTokens.headerTopDp.dp else 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (personalMode) NavGlassCircleButton(p, "back", onClick = onCancel)
            else NavEventHeaderAction("back", localized("Назад", "Back", lang), p, true, onCancel)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    if (personalMode) localized("Личное", "Personal", lang)
                    else localized("Передача дел", "Handover", lang),
                    color = p.text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                )
                if (sectionTitle.isNotEmpty()) {
                    Text(sectionTitle, color = p.muted, fontSize = 12.sp, maxLines = 1)
                }
            }
            if (personalMode) Box(Modifier.personalNoteCard(p, 13.dp)) {
                TextButton(
                    enabled = canSave,
                    onClick = {
                        saveError = !onSave(title, plain, html, photos)
                    },
                    modifier = Modifier.heightIn(min = 44.dp),
                    contentPadding = PaddingValues(horizontal = 15.dp, vertical = 6.dp),
                ) {
                    Text(
                        localized("Сохранить", "Save", lang),
                        color = if (!canSave) p.muted else if (personalMode)
                            NavTodayVisualTokens.accentInk(p.dark) else p.selectionContent,
                        fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                    )
                }
            } else NavEventHeaderAction("check", localized("Сохранить", "Save", lang),
                p, canSave, accent = true) {
                saveError = !onSave(title, plain, html, photos)
            }
        }
        BasicTextField(
            value = title, onValueChange = { title = it.take(200) },
            modifier = Modifier.fillMaxWidth().heightIn(min = 38.dp)
                .semantics {
                    contentDescription = localized("Название заметки", "Note title", lang)
                },
            singleLine = false, maxLines = 3,
            keyboardOptions = NavTitleKeyboard,
            textStyle = TextStyle(
                color = p.text, fontSize = 24.sp, lineHeight = 29.sp,
                fontWeight = FontWeight.Bold,
            ),
            cursorBrush = SolidColor(NavTodayVisualTokens.accentInk(p.dark)),
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
        Box(Modifier.fillMaxWidth().weight(1f).padding(bottom = 2.dp)) {
            NavRichNoteEditor(
                initialPlain = initialPlain, initialHtml = initialHtml,
                entryKey = entryKey, p = p, lang = lang,
                placeholder = localized("Текст заметки", "Write a note", lang),
                maxCharacters = 4000, fullPage = true, neutralStyle = true,
                amosCommentField = !personalMode,
                onInitialized = { text, normalizedHtml ->
                    if (!personalMode) {
                        originalBody = text to normalizedHtml
                        plain = text
                        html = normalizedHtml
                    }
                },
                onChange = { updatedPlain, updatedHtml ->
                    plain = updatedPlain
                    html = updatedHtml
                },
            )
        }
        if (!personalMode) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                TextButton(
                    onClick = { photoPicker.launch("image/*") },
                    enabled = !photoLoading && photos.size < HandoverPhoto.MAX_PER_ENTRY,
                ) {
                    Text(localized("Прикрепить фото", "Attach photos", lang),
                        color = NavTodayVisualTokens.accentInk(p.dark), fontSize = 14.sp)
                }
                Spacer(Modifier.weight(1f))
                Text(photos.size.toString() + " / " + HandoverPhoto.MAX_PER_ENTRY,
                    color = p.muted, fontSize = 12.sp)
            }
            if (photoLoading) {
                Text(localized("Обработка фото…", "Processing photos…", lang),
                    color = p.muted, fontSize = 12.sp)
            }
            if (photoError != null || saveError) {
                Text(
                    photoError ?: localized("Не удалось сохранить фото. Проверь свободное место.",
                        "Could not save photos. Check available storage.", lang),
                    color = p.warningText, fontSize = 12.sp,
                )
            }
        }
        HandoverPhotoStrip(
            photos = photos, p = p, lang = lang,
            onRemove = if (personalMode) null else { id ->
                photos = photos.filterNot { it.id == id }
                photoError = null
                saveError = false
            },
        )
        // Deletion belongs in the bottom center, away from title and Save.
        if (onDelete != null) {
            Box(
                Modifier.fillMaxWidth().padding(bottom = 4.dp),
                contentAlignment = Alignment.Center,
            ) {
                TextButton(onClick = onDelete, modifier = Modifier.heightIn(min = 44.dp)) {
                    Text(localized("Удалить заметку", "Delete note", lang),
                        color = p.systemRed, fontSize = 13.sp)
                }
            }
        }
    }
}
