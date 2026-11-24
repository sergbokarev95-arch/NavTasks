package com.navtasks.app

import android.app.Activity
import android.os.Bundle
import android.view.WindowInsetsController
import android.view.HapticFeedbackConstants
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.input.pointer.pointerInput
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
import kotlinx.coroutines.launch

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
    // Coral from the user's HONOR Calendar reference, strictly dock-local.
    // Other app accent buttons retain their existing semantic colors.
    val activeInk = NavLiquidDockTabs.activeInk(dark)
    val inactiveGlyph = if (dark) Color(0xFFEDEEF1) else Color(0xFF292C31)
    val inactiveLabel = if (dark) Color(0xFFB8BAC0) else Color(0xFF66686E)
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
        // A tiny one-shot pulse is allowed ONLY on clicking the already
        // selected tab. No press/hold state ever controls the glass lens.
        // Key by selected so switching tabs cancels any old pulse.
        var repeatedTapSerial by remember(selected) { mutableIntStateOf(0) }
        val tapPulse = remember(selected) { Animatable(1f) }
        LaunchedEffect(selected, repeatedTapSerial) {
            if (repeatedTapSerial > 0) {
                // Interruptible, restrained Apple-style feedback: no delay,
                // no steady enlargement while the finger is down.
                tapPulse.animateTo(NavLiquidDockTabs.repeatTapScale,
                    animationSpec = tween(durationMillis = 85))
                tapPulse.animateTo(1f,
                    animationSpec = tween(durationMillis = 155))
            }
        }
        val cellWidth = maxWidth / NavLiquidDockTabs.items.size
        val cellWidthPx = with(density) { cellWidth.toPx() }
        val target = selected.coerceIn(0, NavLiquidDockTabs.items.lastIndex).toFloat()
        val view = LocalView.current
        val interactionScope = rememberCoroutineScope()
        // One position drives both interactions: taps spring to their target,
        // while direct drags bypass interpolation and follow the finger.
        val lensPosition = remember { Animatable(target) }
        var dragging by remember { mutableStateOf(false) }
        var dragPosition by remember { mutableFloatStateOf(target) }
        var hapticIndex by remember { mutableIntStateOf(selected) }
        // Liquid jump: visible growth, then a brief 0.95x touchdown squash
        // and immediate return to 1x. This timeline overlaps the slightly
        // slower horizontal spring; there is no separate pause after landing.
        // New taps cancel both animations at their CURRENT position and size.
        val lensExpansion = remember { Animatable(1f) }
        var lastMotionStartMs by remember { mutableLongStateOf(0L) }
        var draggedSinceLastRelease by remember { mutableStateOf(false) }
        LaunchedEffect(target, dragging) {
            if (dragging) {
                draggedSinceLastRelease = true
                lensExpansion.animateTo(
                    NavLiquidDockTabs.movingScale,
                    animationSpec = tween(
                        durationMillis = 140,
                        easing = LinearOutSlowInEasing,
                    ),
                )
            } else {
                val justReleasedDrag = draggedSinceLastRelease
                draggedSinceLastRelease = false
                val distance = abs(lensPosition.value - target)
                if (distance > .01f) {
                    val now = android.os.SystemClock.uptimeMillis()
                    val rapid = !justReleasedDrag && lastMotionStartMs > 0L &&
                        now - lastMotionStartMs < NavLiquidDockMotion.rapidRetargetWindowMillis
                    lastMotionStartMs = now
                    kotlinx.coroutines.coroutineScope {
                        launch {
                            if (justReleasedDrag) {
                                // A physical drag already supplies its own expansion;
                                // release settles without a second artificial jump.
                                lensExpansion.animateTo(
                                    1f,
                                    animationSpec = tween(
                                        durationMillis = NavLiquidDockMotion.expansionFallMillis,
                                        easing = FastOutSlowInEasing,
                                    ),
                                )
                            } else {
                                val peak = NavLiquidDockMotion.peakScaleForTap(distance, rapid)
                                // ONE interruptible timeline, concurrent with movement.
                                // As the capsule reaches its slot, it compresses slightly
                                // below resting size then rebounds immediately to 1x.
                                // Rapid retargets intentionally skip that full landing
                                // envelope; no required replay of the same bounce.
                                lensExpansion.animateTo(
                                    targetValue = 1f,
                                    animationSpec = keyframes {
                                        durationMillis = if (rapid)
                                            NavLiquidDockMotion.rapidMotionEndMillis
                                        else NavLiquidDockMotion.normalMotionEndMillis
                                        if (rapid) {
                                            peak at NavLiquidDockMotion.rapidRiseMillis using
                                                LinearOutSlowInEasing
                                            1f at NavLiquidDockMotion.rapidMotionEndMillis using
                                                FastOutSlowInEasing
                                        } else {
                                            peak at NavLiquidDockMotion.expansionRiseMillis using
                                                LinearOutSlowInEasing
                                            1f at NavLiquidDockMotion.normalLandingStartMillis using
                                                FastOutSlowInEasing
                                            NavLiquidDockMotion.landingSquashScale at
                                                NavLiquidDockMotion.normalCompressionEndMillis using
                                                FastOutSlowInEasing
                                            1f at NavLiquidDockMotion.normalMotionEndMillis using
                                                FastOutSlowInEasing
                                        }
                                    },
                                )
                            }
                        }
                        lensPosition.animateTo(
                            target,
                            animationSpec = spring(
                                dampingRatio = NavLiquidDockMotion.positionDamping,
                                stiffness = NavLiquidDockMotion.positionStiffness,
                                visibilityThreshold = .002f,
                            ),
                        )
                    }
                } else {
                    lensExpansion.animateTo(1f, tween(durationMillis = 105))
                }
            }
        }
        val slide = if (dragging) dragPosition else lensPosition.value
        val matte = NavLiquidDockMotion.lensMatteFromScale(lensExpansion.value)
        val lensScale = lensExpansion.value * tapPulse.value
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
                    val traveling = if (dark) Color(0xFF9C9C9F).copy(alpha = .12f)
                        else Color.White.copy(alpha = .19f)
                    // Night selection follows HONOR Calendar: neutral graphite, no blue tint.
                    // Keep the existing refractive shader and geometry unchanged.
                    val resting = if (dark) Color(0xFF3A3A3C).copy(alpha = .93f)
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

        Row(
            Modifier.fillMaxSize().padding(vertical = 2.dp)
                .pointerInput(selected, cellWidthPx) {
                    // A horizontal gesture captured on the current lens is a
                    // preview: NEVER navigate across intermediate tabs.
                    // Ordinary taps remain handled by each existing selectable.
                    detectHorizontalDragGestures(
                        onDragStart = { finger ->
                            val currentCenter = (selected + .5f) * cellWidthPx
                            if (abs(finger.x - currentCenter) <= cellWidthPx * .72f) {
                                dragging = true
                                dragPosition = lensPosition.value
                                hapticIndex = selected
                            }
                        },
                        onHorizontalDrag = { change, delta ->
                            if (dragging && cellWidthPx > 0f) {
                                change.consume()
                                dragPosition = NavLiquidDockTabs.clampedPosition(
                                    dragPosition + delta / cellWidthPx,
                                )
                                val focused = NavLiquidDockTabs.destinationForDrag(dragPosition)
                                if (focused != hapticIndex) {
                                    hapticIndex = focused
                                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                }
                            }
                        },
                        onDragEnd = {
                            if (dragging) {
                                val landing = dragPosition
                                val destination = NavLiquidDockTabs.destinationForDrag(landing)
                                interactionScope.launch {
                                    // Transfer the finger's exact final position
                                    // into the spring, without a visible jump.
                                    lensPosition.snapTo(landing)
                                    dragging = false
                                    if (destination != selected) onSelect(destination)
                                }
                            }
                        },
                        onDragCancel = {
                            if (dragging) {
                                val lastFingerPosition = dragPosition
                                interactionScope.launch {
                                    lensPosition.snapTo(lastFingerPosition)
                                    dragging = false
                                    // Cancel never commits a tab or resets its page.
                                }
                            }
                        },
                    )
                }
                .selectableGroup(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavLiquidDockTabs.items.forEachIndexed { index, item ->
                val active = selected == index
                // The reference lights up the icon directly under the moving
                // lens, even before the page is committed at finger-up.
                val lensFocus = (1f - abs(slide - index.toFloat())).coerceIn(0f, 1f)
                val iconInk = lerp(inactiveGlyph, activeInk, lensFocus)
                val labelInk = lerp(inactiveLabel, activeInk, lensFocus)
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
                            // Re-tapping the selected tab must also dispatch:
                            // the parent restores Today/date/list to its root.
                            // Preserve re-tap root reset; pulse starts only
                            // after the click, never during a long hold.
                            if (active) repeatedTapSerial++
                            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                            onSelect(index)
                        },
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    NavDockFilledGlyph(
                        kind = item.glyph,
                        color = iconInk,
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
                            else -> localized("Справочник", "Reference", lang)
                        },
                        color = labelInk,
                        fontSize = 10.sp,
                        fontWeight = if (lensFocus > .75f) FontWeight.SemiBold else FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
