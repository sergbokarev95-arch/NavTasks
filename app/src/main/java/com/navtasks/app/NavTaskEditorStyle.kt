package com.navtasks.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** The editor is a raised sheet, not the black main screen. HONOR reference: #2C2C2C. */
internal fun navEditorSheetSurface(p: NavPalette): Color =
    if (p.dark) Color(0xFF2C2C2C) else p.background

@Composable
internal fun NavEditorPanel(p: NavPalette, content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxWidth().personalNoteCard(p, 20.dp).padding(14.dp),
        content = content,
    )
}

@Composable
internal fun NavEditorValueButton(label: String, p: NavPalette, onClick: () -> Unit) {
    Box(
        Modifier.fillMaxWidth().personalNoteCard(p, 14.dp, nested = true)
            .glassClickable(onClick = onClick).heightIn(min = 44.dp)
            .padding(horizontal = 13.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = p.text, fontSize = 14.sp)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun NavEditorOptionLine(
    options: List<Pair<String, String>>, selected: String, p: NavPalette,
    onSelect: (String) -> Unit,
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        options.forEach { (key, label) ->
            val active = selected == key
            val shape = RoundedCornerShape(13.dp)
            Box(
                Modifier.personalNoteCard(p, 13.dp, nested = true)
                    .then(if (active) Modifier.border(
                        1.5.dp, NavTodayVisualTokens.accentInk(p.dark), shape
                    ) else Modifier)
                    .glassClickable { onSelect(key) }
                    .heightIn(min = 44.dp).padding(horizontal = 13.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(label, color = if (active) NavTodayVisualTokens.accentInk(p.dark) else p.text,
                    fontSize = 12.sp)
            }
        }
    }
}

@Composable
internal fun navEditorSwitchColors(p: NavPalette) = SwitchDefaults.colors(
    checkedTrackColor = NavTodayVisualTokens.accentInk(p.dark),
    checkedThumbColor = Color.White,
    checkedBorderColor = NavTodayVisualTokens.accentInk(p.dark),
    uncheckedTrackColor = NavTodayVisualTokens.checklistInsetSurface(p.dark),
    uncheckedThumbColor = p.muted,
    uncheckedBorderColor = NavTodayVisualTokens.checklistInsetRim(p.dark),
)
