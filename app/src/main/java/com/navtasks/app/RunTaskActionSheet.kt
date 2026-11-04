package com.navtasks.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

/**
 * A neutral, grouped action sheet for a task occurrence.
 * Actions still use the existing TaskStore handlers; only presentation changes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RunTaskActionSheet(
    p: NavPalette,
    lang: UiLanguage,
    taskTitle: String,
    available: Boolean,
    onDismiss: () -> Unit,
    onPostponeDays: (Long) -> Unit,
    onChooseDate: () -> Unit,
    onCloseWithReason: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var closing by remember { mutableStateOf(false) }
    val background = if (p.dark) Color(0xFF192A3B) else Color(0xFFF5F8FB)
    val groupBackground = if (p.dark) Color(0xFF25384C) else Color.White
    val groupOutline = p.border.copy(alpha = if (p.dark) 0.72f else 0.85f)
    val groupShape = RoundedCornerShape(18.dp)

    fun perform(action: () -> Unit) {
        if (closing) return
        closing = true
        scope.launch {
            sheetState.hide()
            onDismiss()
            action()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = background,
        contentColor = p.text,
        tonalElevation = 0.dp,
        scrimColor = Color.Black.copy(alpha = if (p.dark) 0.52f else 0.28f),
        dragHandle = {
            Box(
                Modifier.padding(top = 12.dp, bottom = 8.dp)
                    .width(36.dp).height(4.dp)
                    .background(p.muted.copy(alpha = 0.44f), CircleShape),
            )
        },
    ) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 18.dp).padding(bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                localized("Действия с задачей", "Task actions", lang),
                color = p.muted, fontSize = 12.sp,
            )
            Text(
                taskTitle, color = p.text, fontSize = 18.sp,
                fontWeight = FontWeight.Medium, maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(4.dp))
            if (available) {
                Text(
                    localized("ПЕРЕНЕСТИ", "POSTPONE", lang),
                    color = p.muted, fontSize = 11.sp,
                )
                Column(
                    Modifier.fillMaxWidth().clip(groupShape)
                        .background(groupBackground, groupShape)
                        .border(1.dp, groupOutline, groupShape),
                ) {
                    ActionSheetEntry(localized("Завтра", "Tomorrow", lang), p) {
                        perform { onPostponeDays(1L) }
                    }
                    SheetSeparator(p)
                    ActionSheetEntry(localized("Через 3 дня", "In 3 days", lang), p) {
                        perform { onPostponeDays(3L) }
                    }
                    SheetSeparator(p)
                    ActionSheetEntry(localized("Через неделю", "In a week", lang), p) {
                        perform { onPostponeDays(7L) }
                    }
                    SheetSeparator(p)
                    ActionSheetEntry(
                        localized("Выбрать дату…", "Choose date…", lang),
                        p, showChevron = true,
                    ) {
                        perform(onChooseDate)
                    }
                }
                Spacer(Modifier.height(2.dp))
                Column(
                    Modifier.fillMaxWidth().clip(groupShape)
                        .background(groupBackground, groupShape)
                        .border(1.dp, groupOutline, groupShape),
                ) {
                    ActionSheetEntry(
                        localized("Закрыть с причиной…", "Close with reason…", lang),
                        p, showChevron = true,
                    ) {
                        perform(onCloseWithReason)
                    }
                }
            } else {
                Text(
                    localized("Для выполненной задачи нет доступных действий.",
                        "No actions are available for this completed task.", lang),
                    color = p.muted, fontSize = 13.sp,
                )
            }
            Spacer(Modifier.height(3.dp))
            Box(
                Modifier.fillMaxWidth().clip(groupShape)
                    .background(groupBackground, groupShape)
                    .border(1.dp, groupOutline, groupShape)
                    .glassClickable { perform {} }
                    .padding(vertical = 15.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(localized("Отмена", "Cancel", lang),
                    color = p.blue, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun ActionSheetEntry(
    label: String,
    p: NavPalette,
    showChevron: Boolean = false,
    onClick: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 49.dp)
            .glassClickable(onClick = onClick)
            .padding(horizontal = 17.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = p.text, fontSize = 15.sp, modifier = Modifier.weight(1f))
        if (showChevron) {
            NavGlassGlyph("chevron-right", p.muted, Modifier.size(16.dp))
        }
    }
}

@Composable
private fun SheetSeparator(p: NavPalette) {
    HorizontalDivider(
        modifier = Modifier.padding(start = 17.dp),
        color = p.border.copy(alpha = 0.60f),
        thickness = 0.6.dp,
    )
}
