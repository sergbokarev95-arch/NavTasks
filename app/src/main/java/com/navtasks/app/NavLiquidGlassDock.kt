package com.navtasks.app

import android.app.Activity
import android.os.Bundle
import android.view.WindowInsetsController
import android.view.HapticFeedbackConstants
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import com.kyant.shapes.Capsule
import kotlin.math.abs

/**
 * EXACT optical/lens/dock renderer copied from accepted Glass Lab 0.1.5
 * (ea412201936a8a178c657b77bda7a3347b878f06).
 * Only production binding: NavPalette theme, language labels, NavGlassGlyph,
 * and the existing NavTasks onSelect callback. The caller supplies the
 * original lab's 15dp horizontal/8dp vertical insets.
 */
@Composable
fun NavLiquidGlassBottomBar(
    p: NavPalette,
    selected: Int,
    lang: UiLanguage,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    onSelect: (Int) -> Unit
) {
    val dark = p.dark
    val optical = true
    val density = LocalDensity.current
    val ambient = if (dark) Color(0xFF111E2E).copy(alpha = .15f)
        else Color.White.copy(alpha = .12f)
    // Exact production NavTasks active blue, inactive icon and caption colors.
    val activeBlue = if (dark) Color(0xFF329CFF) else Color(0xFF087DFA)
    val inactiveGlyph = if (dark) Color(0xFFF2F8FF).copy(alpha = .89f)
        else Color(0xFF172B42).copy(alpha = .89f)
    val inactiveLabel = if (dark) Color(0xFFAFC4D8) else Color(0xFF5F768D)
    val fallback = if (dark) Color(0xDC1D2A38) else Color(0xEAE4EDF3)
    val surfaceSheen = Brush.verticalGradient(
        if (dark) listOf(
            Color.White.copy(alpha = .08f),
            Color(0xFF9FCBED).copy(alpha = .025f),
            Color.Black.copy(alpha = .045f)
        ) else listOf(
            Color.White.copy(alpha = .12f),
            Color.White.copy(alpha = .035f),
            Color(0xFF8CACCB).copy(alpha = .035f)
        )
    )

    // Keep the proven 0.1.1 direct-scene lens for the entire 56dp navigation bar.
    // Selection, glyphs and interaction remain on top and outside this shader.
    val backing = if (optical) {
        Modifier.drawBackdrop(
            backdrop = backdrop,
            shape = { Capsule() },
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
                drawRect(surfaceSheen)
            }
        )
    } else {
        Modifier.clip(RoundedCornerShape(30.dp)).background(fallback)
    }

    val opticalEdge = if (optical) {
        Modifier.border(
            width = .75.dp,
            brush = navLiquidGlassRim(dark),
            shape = RoundedCornerShape(30.dp)
        )
    } else Modifier

    // Crucial: the refractive bar must NOT be a drawing modifier on the
    // parent of the moving lens. Its own clipping graphics layer would mask
    // the top and bottom protrusion, even with unbounded lens measurement.
    // A separate background sibling keeps backdrop capture and rim unchanged.
    BoxWithConstraints(modifier.fillMaxWidth().height(56.dp)) {
        Box(Modifier.matchParentSize().then(backing).then(opticalEdge))
        val cellWidth = maxWidth / NavLiquidDockTabs.items.size
        val target = selected.coerceIn(0, NavLiquidDockTabs.items.lastIndex).toFloat()
        // Follow the working NavTasks 0.4.59 movement: quick, interruptible spring,
        // while extra width and material settle independently of travel.
        val slide by animateFloatAsState(
            targetValue = target,
            animationSpec = spring(
                dampingRatio = 1f,
                stiffness = 550f,
                visibilityThreshold = .001f
            ),
            label = "glassLabDockSlide"
        )
        val moving = abs(slide - target) > .015f
        val matte by animateFloatAsState(
            targetValue = if (moving) 0f else 1f,
            animationSpec = spring(dampingRatio = 1f, stiffness = 700f),
            label = "glassLabLensMatte"
        )
        val stretched by animateFloatAsState(
            targetValue = if (moving) 1f else 0f,
            animationSpec = spring(dampingRatio = 1f, stiffness = 600f),
            label = "glassLabLensStretch"
        )
        // True proportional growth: both axes expand by the same factor.
        // Unlike 0.1.3's horizontal stretch, this makes the glass rise above
        // and drop below the 56dp navigation capsule while it is travelling.
        val lensScale = NavLiquidDockTabs.scaleAt(stretched)
        val lensWidth = (cellWidth - 4.dp) * lensScale
        val lensHeight = NavLiquidDockTabs.restingLensHeightDp.dp * lensScale
        val lensX = (cellWidth * (slide + .5f) - lensWidth / 2f)
            .coerceIn(2.dp, (maxWidth - 2.dp - lensWidth).coerceAtLeast(2.dp))
        val lensY = (NavLiquidDockTabs.dockHeightDp.dp - lensHeight) / 2f

        // Selection is a sibling of the opaque/refractive BACKING, not its child.
        // The outer BoxWithConstraints has no clip() / graphicsLayer clip; the
        // lens draws in true window coordinates beyond the 56dp bar.
        // Exactly one moving lens. While travelling it is optically refractive;
        // after it lands, its own face fades into neutral graphite/grey.
        val selection = if (optical) {
            Modifier.drawBackdrop(
                backdrop = backdrop,
                shape = { Capsule() },
                effects = {
                    vibrancy()
                    blur(with(density) { NavLiquidDockOptics.selection.blurDp.dp.toPx() })
                    lens(
                        with(density) { NavLiquidDockOptics.selection.refractionHeightDp.dp.toPx() },
                        with(density) { NavLiquidDockOptics.selection.refractionAmountDp.dp.toPx() }
                    )
                },
                highlight = { Highlight.Default.copy(alpha = .3f + (1f - matte) * .45f) },
                innerShadow = { InnerShadow(radius = 8.dp, alpha = .18f) },
                onDrawSurface = {
                    val traveling = if (dark) Color(0xFF75B8F0).copy(alpha = .13f)
                        else Color.White.copy(alpha = .19f)
                    // Slightly deeper neutral graphite in Night so the unchanged blue
                    // active glyph stays legible, without tinting the glass blue.
                    val resting = if (dark) Color(0xFF3F4B58).copy(alpha = .82f)
                        else Color(0xFFD7D8DA).copy(alpha = .87f)
                    drawRect(lerp(traveling, resting, matte.coerceIn(0f, 1f)))
                }
            )
        } else {
            Modifier.clip(RoundedCornerShape(26.dp))
                .background(if (dark) Color(0xFF3C5368) else Color(0xFFBDD6E7))
        }

        Box(
            Modifier.offset(x = lensX, y = lensY)
                // Explicitly allow the travelling material to render outside
                // the 56dp dock bounds; otherwise Compose constraints can
                // coerce a taller lens back to the bar's own height.
                .wrapContentSize(unbounded = true, align = Alignment.TopStart)
                .size(width = lensWidth, height = lensHeight)
                .then(selection)
        )

        val view = LocalView.current
        Row(
            Modifier.fillMaxSize().padding(vertical = 2.dp).selectableGroup(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavLiquidDockTabs.items.forEachIndexed { index, item ->
                val active = selected == index
                val interaction = remember { MutableInteractionSource() }
                val pressed by interaction.collectIsPressedAsState()
                val pressScale by animateFloatAsState(
                    targetValue = if (pressed) .94f else 1f,
                    animationSpec = spring(dampingRatio = 1f, stiffness = 1600f),
                    label = "glassLabTabPress"
                )
                Column(
                    Modifier.weight(1f).fillMaxHeight()
                        .clip(RoundedCornerShape(21.dp))
                        .selectable(
                            selected = active,
                            role = Role.Tab,
                            interactionSource = interaction,
                            indication = null
                        ) {
                            if (!active) {
                                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                onSelect(index)
                            }
                        },
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    NavGlassGlyph(
                        kind = item.glyph,
                        color = if (active) activeBlue else inactiveGlyph,
                        modifier = Modifier.size(24.dp).graphicsLayer {
                            scaleX = pressScale
                            scaleY = pressScale
                        }
                    )
                    Spacer(Modifier.height(1.dp))
                    Text(
                        text = when (index) {
                            0 -> localized("Сегодня", "Today", lang)
                            1 -> localized("Работы", "Work", lang)
                            2 -> "Handover"
                            else -> localized("Задачи", "Tasks", lang)
                        },
                        color = if (active) activeBlue else inactiveLabel,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
