package com.navtasks.app

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.Shadow
import com.kyant.shapes.Capsule

/**
 * First optically rendered action controls: '+' and Calendar, placed as
 * siblings of the recorded scene, exactly like the proven Liquid Glass dock.
 * No new capture and no change to the original action handlers.
 */
@Composable
fun NavLiquidGlassActionButton(
    p: NavPalette,
    backdrop: Backdrop,
    glyph: String,
    onClick: () -> Unit
) {
    val density = LocalDensity.current
    val sheen = Brush.verticalGradient(
        if (p.dark) listOf(
            Color.White.copy(alpha = .075f),
            Color(0xFFA0BED7).copy(alpha = .022f),
            Color.Black.copy(alpha = .065f)
        ) else listOf(
            Color.White.copy(alpha = .15f),
            Color.White.copy(alpha = .025f),
            Color(0xFF96B2C8).copy(alpha = .035f)
        )
    )
    Box(
        Modifier.size(54.dp)
            .drawBackdrop(
                backdrop = backdrop,
                shape = { Capsule() },
                effects = {
                    vibrancy()
                    blur(with(density) { NavLiquidDockOptics.bar.blurDp.dp.toPx() })
                    lens(
                        with(density) { NavLiquidDockOptics.selection.refractionHeightDp.dp.toPx() },
                        with(density) { NavLiquidDockOptics.selection.refractionAmountDp.dp.toPx() }
                    )
                },
                highlight = { Highlight.Default.copy(alpha = .72f) },
                shadow = { Shadow(alpha = .18f) },
                onDrawSurface = {
                    drawRect(if (p.dark) Color(0xFF14263A).copy(alpha = .20f)
                        else Color.White.copy(alpha = .11f))
                    drawRect(sheen)
                }
            )
            .border(.75.dp, Brush.verticalGradient(
                listOf(
                    Color.White.copy(alpha = if (p.dark) .36f else .70f),
                    Color(0xFF7C9BAC).copy(alpha = .18f),
                    Color.White.copy(alpha = if (p.dark) .16f else .45f)
                )
            ), CircleShape)
            .glassClickable(onClick),
        contentAlignment = Alignment.Center
    ) {
        NavGlassGlyph(glyph, p.blue,
            Modifier.size(if (glyph == "calendar") 26.dp else 28.dp))
    }
}
