package com.navtasks.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

/** Small floating popup anchored to its button. */
@Composable
fun NavPopoverMenu(
    p: NavPalette,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(22.dp)
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier.widthIn(min = 215.dp, max = 310.dp)
            .border(
                1.dp,
                if (p.dark) Color.White.copy(alpha = 0.16f)
                else Color(0xFF9BA8B4).copy(alpha = 0.34f),
                shape,
            ),
        shape = shape,
        containerColor = if (p.dark) Color(0xF52E2F32) else Color(0xF9F8FAFC),
        tonalElevation = 0.dp,
        shadowElevation = 14.dp,
        content = content,
    )
}

@Composable
fun NavPopoverAction(
    label: String,
    p: NavPalette,
    onClick: () -> Unit,
    destructive: Boolean = false,
    selected: Boolean = false,
    enabled: Boolean = true,
) {
    DropdownMenuItem(
        text = {
            Text(label,
                fontSize = 15.sp,
                color = if (destructive) {
                    if (p.dark) Color(0xFFFF9790) else Color(0xFFB7423C)
                } else p.text,
            )
        },
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.heightIn(min = 48.dp),
        trailingIcon = if (selected) {
            { Text("✓", color = p.blue, fontSize = 16.sp) }
        } else null,
        contentPadding = PaddingValues(horizontal = 17.dp),
    )
}

@Composable
fun NavPopoverDivider(p: NavPalette) {
    HorizontalDivider(
        color = p.border.copy(alpha = if (p.dark) 0.44f else 0.66f),
        thickness = 0.6.dp,
        modifier = Modifier.padding(horizontal = 13.dp),
    )
}

/** Neutral glass confirmation dialog; callbacks and validation remain in callers. */
@Composable
fun NavGlassAlertDialog(
    p: NavPalette,
    onDismissRequest: () -> Unit,
    title: @Composable () -> Unit,
    text: @Composable () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit = {},
) {
    val shape = RoundedCornerShape(25.dp)
    Dialog(onDismissRequest = onDismissRequest) {
        Column(
            Modifier.fillMaxWidth().widthIn(max = 420.dp)
                .clip(shape)
                .background(
                    if (p.dark) Color(0xF42A2D32) else Color(0xFAF8FAFD),
                    shape,
                )
                .border(
                    1.dp,
                    if (p.dark) Color.White.copy(alpha = 0.19f)
                    else Color(0xFFB8C6D2).copy(alpha = 0.68f),
                    shape,
                )
                .padding(horizontal = 20.dp, vertical = 19.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            CompositionLocalProvider(LocalContentColor provides p.text) {
                ProvideTextStyle(TextStyle(fontSize = 20.sp, lineHeight = 25.sp, color = p.text)) {
                    title()
                }
            }
            CompositionLocalProvider(LocalContentColor provides p.muted) {
                ProvideTextStyle(TextStyle(fontSize = 14.sp, lineHeight = 20.sp, color = p.muted)) {
                    Box(Modifier.fillMaxWidth().heightIn(max = 460.dp)
                        .verticalScroll(rememberScrollState())) {
                        text()
                    }
                }
            }
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.navGlass(p, radius = 13.dp, role = GlassRole.CONTROL)) {
                    dismissButton()
                }
                Spacer(Modifier.width(8.dp))
                Box(Modifier.navGlass(p, radius = 13.dp, role = GlassRole.CONTROL)) {
                    confirmButton()
                }
            }
        }
    }
}
