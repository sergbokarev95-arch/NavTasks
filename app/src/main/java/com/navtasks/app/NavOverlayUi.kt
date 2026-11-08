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
import androidx.compose.ui.unit.Dp
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
                if (p.dark) Color(0xFF4A5058)
                else Color(0xFFBDC5CE),
                shape,
            ),
        shape = shape,
        // A native DropdownMenu is rendered in a separate popup window;
        // use the same neutral palette, but do not mistake transparency for
        // actual refracting Liquid Glass from the page's separate backdrop.
        containerColor = if (p.dark) Color(0xFF25292E) else Color(0xFFF9FAFC),
        tonalElevation = 0.dp,
        shadowElevation = 8.dp,
        content = content,
    )
}

@Composable
fun NavPopoverAction(
    label: String,
    p: NavPalette,
    destructive: Boolean = false,
    selected: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit,
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
            { NavGlassGlyph("check", p.blue, Modifier.size(17.dp)) }
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

/** Consistent neutral glass surface for all modal dialogs. Content/layout stays in each caller. */
fun Modifier.navDialogSurface(p: NavPalette, radius: Dp = 24.dp): Modifier {
    val shape = RoundedCornerShape(radius)
    return clip(shape)
        // Large modal editors follow the Night09 matte graphite materials.
        // Opaque backgrounds avoid the former muddy reddish/gray alpha blend.
        .background(if (p.dark) Color(0xFF25292E) else Color(0xFFF9FAFC), shape)
        .border(1.dp,
            if (p.dark) Color(0xFF454B53)
            else Color(0xFFC4CBD3),
            shape)
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
    Dialog(onDismissRequest = onDismissRequest) {
        Column(
            Modifier.fillMaxWidth().widthIn(max = 420.dp)
                .navDialogSurface(p)
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
