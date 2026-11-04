package com.navtasks.app

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

/** Explicit scope selection for changing the date of a recurring occurrence. */
@Composable
internal fun MoveScopeDialog(
    p: NavPalette,
    lang: UiLanguage,
    title: String,
    oldDate: java.time.LocalDate,
    newDate: java.time.LocalDate,
    onOnlyThis: () -> Unit,
    onThisAndFuture: () -> Unit,
    onDismiss: () -> Unit,
    summary: String? = null,
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier.fillMaxWidth().navDialogSurface(p).padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(localized("Изменить расписание?", "Change schedule?", lang),
                color = p.text, fontSize = 20.sp)
            Text(title, color = p.text, fontSize = 15.sp)
            if (summary != null) {
                Text(summary, color = p.blue, fontSize = 14.sp)
            }
            Text(navDate(oldDate, lang, true) + "  →  " + navDate(newDate, lang, true),
                color = p.muted, fontSize = 13.sp)
            Spacer(Modifier.height(2.dp))
            Box(Modifier.fillMaxWidth()
                .navGlass(p, radius = 14.dp, role = GlassRole.CONTROL)
                .glassClickable(onClick = onOnlyThis)
                .padding(horizontal = 15.dp, vertical = 15.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(localized("Только это выполнение", "Only this occurrence", lang),
                        color = p.text, fontSize = 15.sp)
                    Text(localized("Остальное расписание не изменится",
                        "The rest of the schedule stays the same", lang),
                        color = p.muted, fontSize = 12.sp)
                }
            }
            Box(Modifier.fillMaxWidth()
                .navGlass(p, radius = 14.dp, role = GlassRole.CONTROL)
                .glassClickable(onClick = onThisAndFuture)
                .padding(horizontal = 15.dp, vertical = 15.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(localized("Это и все последующие", "This and future occurrences", lang),
                        color = p.blue, fontSize = 15.sp)
                    Text(localized("Новое повторение действует с выбранной даты",
                        "The new recurrence starts at the chosen occurrence", lang),
                        color = p.muted, fontSize = 12.sp)
                }
            }
            Box(Modifier.fillMaxWidth()
                .glassClickable(onClick = onDismiss)
                .padding(vertical = 9.dp), contentAlignment = Alignment.Center) {
                Text(localized("Отмена", "Cancel", lang),
                    color = p.muted, fontSize = 14.sp)
            }
        }
    }
}
