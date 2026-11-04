package com.navtasks.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Edit the task's existing checklist in place; preserve each item's stable ID,
 * comment and required flag. TaskRun snapshots are not modified.
 */
internal fun moveChecklistItem(items: List<CheckItem>, from: Int, to: Int): List<CheckItem> {
    if (from !in items.indices || to !in items.indices || from == to) return items
    return items.toMutableList().apply { add(to, removeAt(from)) }
}

@Composable
internal fun ChecklistEditorPanel(
    items: List<CheckItem>,
    onItemsChange: (List<CheckItem>) -> Unit,
    onEditNote: (String) -> Unit,
    p: NavPalette,
    lang: UiLanguage,
) {
    var focusItemId by remember { mutableStateOf<String?>(null) }
    val keyboard = LocalSoftwareKeyboardController.current

    fun appendItem() {
        val blank = items.firstOrNull { it.title.isBlank() }
        if (blank != null) {
            // An unfinished row is already available: focus it instead of duplicating it.
            focusItemId = blank.id
        } else {
            val draft = CheckItem(title = "")
            onItemsChange(items + draft)
            focusItemId = draft.id
        }
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(localized("Вложенный чек-лист", "Nested checklist", lang),
            color = p.text, fontSize = 16.sp, modifier = Modifier.weight(1f))
        Text(items.count { it.title.isNotBlank() }.toString(), color = p.blue)
    }
    Spacer(Modifier.height(9.dp))

    items.forEachIndexed { index, item ->
        key(item.id) {
            val focusRequester = remember { FocusRequester() }
            var menuOpen by remember { mutableStateOf(false) }

            LaunchedEffect(focusItemId) {
                if (focusItemId == item.id) {
                    focusRequester.requestFocus()
                    keyboard?.show()
                }
            }

            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Text(
                    (index + 1).toString() + ".",
                    color = p.muted, fontSize = 12.sp,
                    modifier = Modifier.width(23.dp),
                )
                NavGlassInput(
                    value = item.title,
                    onValueChange = { value ->
                        onItemsChange(items.map {
                            if (it.id == item.id) it.copy(title = value) else it
                        })
                    },
                    p = p,
                    placeholder = localized("Название пункта", "Checklist item", lang),
                    modifier = Modifier.weight(1f).focusRequester(focusRequester),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { appendItem() }),
                )
                ChecklistReferenceButton(p, item.comment.isNotBlank()) {
                    onEditNote(item.id)
                }
                Box {
                    Text("⋯", color = p.muted, fontSize = 21.sp,
                        modifier = Modifier.clip(CircleShape)
                            .glassClickable { menuOpen = true }
                            .padding(horizontal = 6.dp, vertical = 9.dp))
                    NavPopoverMenu(
                        p = p,
                        expanded = menuOpen,
                        onDismissRequest = { menuOpen = false },
                    ) {
                        NavPopoverAction(
                            localized("Переместить выше", "Move up", lang), p,
                            enabled = index > 0,
                        ) {
                            menuOpen = false
                            onItemsChange(moveChecklistItem(items, index, index - 1))
                        }
                        NavPopoverDivider(p)
                        NavPopoverAction(
                            localized("Переместить ниже", "Move down", lang), p,
                            enabled = index < items.lastIndex,
                        ) {
                            menuOpen = false
                            onItemsChange(moveChecklistItem(items, index, index + 1))
                        }
                        NavPopoverDivider(p)
                        NavPopoverAction(
                            localized("Удалить пункт", "Delete item", lang), p,
                            destructive = true,
                        ) {
                            menuOpen = false
                            if (focusItemId == item.id) focusItemId = null
                            onItemsChange(items.filterNot { it.id == item.id })
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }

    Box(
        Modifier.fillMaxWidth()
            .navGlass(p, radius = 13.dp, role = GlassRole.CONTROL, elevation = 0.dp)
            .glassClickable { appendItem() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Text(
            localized("+ Добавить пункт", "+ Add item", lang),
            color = p.blue, fontSize = 14.sp,
        )
    }
    Spacer(Modifier.height(8.dp))
    Text(
        localized(
            "После ввода нажми «Далее» на клавиатуре для следующего пункта. " +
                "Кнопка ? открывает комментарий, меню ⋯ позволяет менять порядок.",
            "Press Next on the keyboard to add another item. " +
                "Use ? for a note and ⋯ to reorder.",
            lang,
        ),
        color = p.muted, fontSize = 11.sp,
    )
    Spacer(Modifier.height(3.dp))
    Text(
        localized("Когда все пункты отмечены, задача завершается автоматически.",
            "All checked items automatically complete the parent task.", lang),
        color = p.muted, fontSize = 11.sp,
    )
}
