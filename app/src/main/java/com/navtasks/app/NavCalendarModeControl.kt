package com.navtasks.app

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate

/** Only existing functional views are shown. Week/Agenda need dedicated data/UI QA. */
internal val navCalendarAvailableLevels = listOf(0, 1, 2)

internal fun navCalendarWeekDates(selected: LocalDate): List<LocalDate> {
    val monday = selected.minusDays((selected.dayOfWeek.value - 1).toLong())
    return (0..6).map { monday.plusDays(it.toLong()) }
}

/**
 * Compact system-style segmented control scoped to Calendar.
 * Do not reuse the optical Liquid Glass dock lens or mutate task state.
 */
@Composable
internal fun NavCalendarModeControl(
    level: Int, p: NavPalette, lang: UiLanguage,
    onSelect: (Int) -> Unit,
) {
    val options = listOf(
        localized("Год", "Year", lang),
        localized("Месяц", "Month", lang),
        localized("День", "Day", lang),
    )
    val outer = RoundedCornerShape(24.dp)
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp)
            .clip(outer)
            .background(if (p.dark) Color(0xFF242428) else Color(0xFFF8F9FB), outer)
            .border(1.dp, p.border.copy(alpha = if (p.dark) 0.55f else 0.7f), outer)
            .padding(4.dp)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        options.forEachIndexed { index, label ->
            val selected = level == navCalendarAvailableLevels[index]
            val fill by animateColorAsState(
                targetValue = if (selected) {
                    if (p.dark) Color(0xFF404044) else Color(0xFFE4E6E9)
                } else Color.Transparent,
                animationSpec = tween(170), label = "calendarSegmentFill",
            )
            val shape = RoundedCornerShape(19.dp)
            Box(
                modifier = Modifier.weight(1f).height(42.dp)
                    .clip(shape).background(fill, shape)
                    .selectable(
                        selected = selected, role = Role.Tab,
                        onClick = { onSelect(navCalendarAvailableLevels[index]) },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label, fontSize = 14.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (selected) p.text else p.muted,
                    maxLines = 1,
                )
            }
        }
    }
}
