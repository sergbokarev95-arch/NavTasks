package com.navtasks.app

import androidx.compose.foundation.background
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
 * The exact optical recipe of the accepted dock, now on ONE floating create
 * action only. The button is outside the captured layer, sharing its Backdrop.
 * Glyph and touch target are drawn on top, never sampled by the lens.
 */
internal object NavLiquidGlassActionTokens {
    const val addDiameterDp = 54f
}

@Composable
fun NavLiquidGlassAddButton(
    p: NavPalette,
    backdrop: Backdrop,
    onClick: () -> Unit,
) {
    val density = LocalDensity.current
    val dark = p.dark
    val ambient = if (dark) Color(0xFF111E2E).copy(alpha = .15f)
        else Color.White.copy(alpha = .12f)
    val sheen = Brush.verticalGradient(
        if (dark) listOf(
            Color.White.copy(alpha = .08f),
            Color(0xFF9FCBED).copy(alpha = .025f),
            Color.Black.copy(alpha = .045f),
        ) else listOf(
            Color.White.copy(alpha = .12f),
            Color.White.copy(alpha = .035f),
            Color(0xFF8CACCB).copy(alpha = .035f),
        )
    )
    val rim = Brush.verticalGradient(
        listOf(
            Color.White.copy(alpha = if (dark) .36f else .49f),
            Color(0xFF8FAFCB).copy(alpha = .12f),
            Color.White.copy(alpha = if (dark) .17f else .28f),
        )
    )

    Box(
        Modifier.size(NavLiquidGlassActionTokens.addDiameterDp.dp)
            .drawBackdrop(
                backdrop = backdrop,
                shape = { Capsule() }, // 54 x 54: geometrically a circle
                effects = {
                    vibrancy()
                    blur(with(density) { NavLiquidDockOptics.bar.blurDp.dp.toPx() })
                    lens(
                        with(density) { NavLiquidDockOptics.bar.refractionHeightDp.dp.toPx() },
                        with(density) { NavLiquidDockOptics.bar.refractionAmountDp.dp.toPx() }
                    )
                },
                highlight = { Highlight.Default.copy(alpha = .85f) },
                shadow = { Shadow(alpha = .22f) },
                onDrawSurface = {
                    drawRect(ambient)
                    drawRect(sheen)
                }
            )
            .border(.75.dp, rim, CircleShape)
            .glassClickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        NavGlassGlyph("plus", p.blue, Modifier.size(28.dp))
    }
}
