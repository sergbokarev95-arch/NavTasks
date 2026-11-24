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
import androidx.compose.ui.unit.Dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.Shadow
import com.kyant.shapes.Capsule

/**
 * Single exact Backdrop optical recipe for all floating circular actions.
 * The only visual material change is the DAY rim brush, shared with the dock.
 * Do not edit blur/lens/vibrancy/highlight/shadow values or task semantics.
 */
internal object NavLiquidGlassActionTokens {
    const val addDiameterDp = 54f
    const val compactDiameterDp = 44f

    // One trial accent, shared by all standalone glass create/calendar actions.
    // Semantic colors and the accepted dock remain independent of this choice.
    fun glyphInk(kind: String, dark: Boolean, normal: Color): Color =
        if (kind == "plus" || kind == "calendar") NavLiquidDockTabs.activeInk(dark) else normal
}

@Composable
fun NavLiquidGlassAddButton(p: NavPalette, backdrop: Backdrop, onClick: () -> Unit) =
    NavLiquidGlassRoundButton(
        p, backdrop, "plus", 54.dp, 28.dp, p.blue, onClick,
    )

@Composable
fun NavLiquidGlassCalendarButton(p: NavPalette, backdrop: Backdrop, onClick: () -> Unit) =
    NavLiquidGlassRoundButton(
        p, backdrop, "calendar", 54.dp, 26.dp, p.blue, onClick,
    )

@Composable
fun NavLiquidGlassCircleButton(
    p: NavPalette,
    backdrop: Backdrop,
    kind: String,
    onClick: () -> Unit,
) = NavLiquidGlassRoundButton(
    p, backdrop, kind, 44.dp, 25.dp, p.text, onClick,
)

@Composable
internal fun NavLiquidGlassRoundButton(
    p: NavPalette,
    backdrop: Backdrop,
    kind: String,
    diameter: Dp,
    glyphSize: Dp,
    glyphColor: Color,
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
    val rim = navLiquidGlassRim(dark)

    Box(
        Modifier.size(diameter)
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
            .glassClickable(
                onClick = onClick, pressScale = NavPressFeedbackTokens.floatingActionScale),
        contentAlignment = Alignment.Center,
    ) {
        NavGlassGlyph(kind, NavLiquidGlassActionTokens.glyphInk(kind, dark, glyphColor), Modifier.size(glyphSize))
    }
}
