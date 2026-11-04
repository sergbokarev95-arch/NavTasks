package com.navtasks.app

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier

/** Anchored task occurrence actions; never opens a full-screen or bottom sheet. */
@Composable
internal fun RunTaskPopover(
    p: NavPalette,
    lang: UiLanguage,
    expanded: Boolean,
    available: Boolean,
    onDismiss: () -> Unit,
    onPostponeDays: (Long) -> Unit,
    onChooseDate: () -> Unit,
    onCloseWithReason: () -> Unit,
) {
    NavPopoverMenu(p = p, expanded = expanded, onDismissRequest = onDismiss) {
        if (available) {
            NavPopoverAction(localized("Завтра", "Tomorrow", lang), p) {
                onDismiss()
                onPostponeDays(1L)
            }
            NavPopoverDivider(p)
            NavPopoverAction(localized("Через 3 дня", "In 3 days", lang), p) {
                onDismiss()
                onPostponeDays(3L)
            }
            NavPopoverDivider(p)
            NavPopoverAction(localized("Через неделю", "In a week", lang), p) {
                onDismiss()
                onPostponeDays(7L)
            }
            NavPopoverDivider(p)
            NavPopoverAction(localized("Выбрать дату…", "Choose date…", lang), p) {
                onDismiss()
                onChooseDate()
            }
            NavPopoverDivider(p)
            NavPopoverAction(
                localized("Закрыть с причиной…", "Close with reason…", lang),
                p, destructive = true,
                onClick = {
                    onDismiss()
                    onCloseWithReason()
                },
            )
        } else {
            Text(localized("Задача выполнена", "Task completed", lang),
                modifier = Modifier.padding(18.dp), color = p.muted, fontSize = 14.sp)
        }
    }
}
