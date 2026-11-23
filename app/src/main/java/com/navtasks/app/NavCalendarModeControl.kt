package com.navtasks.app

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import kotlin.math.abs

/** Exactly the three working modes. No inert Week or Events tabs. */
internal val navCalendarAvailableLevels = listOf(0, 1, 2)

/** Fixed four-row, three-column year: all months are visible without scrolling. */
internal val navCalendarYearRows: List<List<Int>> = (1..12).chunked(3)

/** Compact control footprint, shared by all three calendar views. */
internal val navCalendarModeHeight = 44.dp
internal val navCalendarModeLensHeight = 40.dp

internal fun navCalendarWeekDates(selected: LocalDate): List<LocalDate> {
    val monday = selected.minusDays((selected.dayOfWeek.value - 1).toLong())
    return (0..6).map { monday.plusDays(it.toLong()) }
}

/**
 * A lighter sibling of our accepted Liquid Glass dock: same shared rim,
 * critically damped 550f selection spring, graphite resting lens, subtle
 * travel expansion and tap-only haptic pulse (never hold-to-enlarge).
 *
 * It deliberately doesn't sample the dock's backdrop: the Calendar lives
 * INSIDE the captured Plan surface, so doing so could recursively refract
 * its own selected segment. The real bottom dock renderer stays untouched.
 */
@Composable
internal fun NavCalendarModeControl(
    level: Int,
    p: NavPalette,
    lang: UiLanguage,
    onSelect: (Int) -> Unit,
) {
    val labels = listOf(
        localized("Год", "Year", lang),
        localized("Месяц", "Month", lang),
        localized("День", "Day", lang),
    )
    val activeIndex = navCalendarAvailableLevels.indexOf(level).coerceAtLeast(0)
    val view = LocalView.current
    val dark = p.dark
    val activeBlue = if (dark) Color(0xFF329CFF) else Color(0xFF087DFA)
    val inactiveInk = if (dark) Color(0xFFAFC4D8) else Color(0xFF5F768D)
    val shape = RoundedCornerShape(30.dp)
    val shell = Brush.verticalGradient(
        if (dark) listOf(Color(0xE926303C), Color(0xEB1D2935), Color(0xED17232F))
        else listOf(Color(0xF8EDF3F8), Color(0xFAE5EDF3), Color(0xF5DAE5ED))
    )

    BoxWithConstraints(
        Modifier.fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 3.dp)
            .height(navCalendarModeHeight),
    ) {
        // No clip on the parent: the moving lens rises a few pixels while flying.
        Box(
            Modifier.matchParentSize().clip(shape)
                .background(shell, shape)
                .border(.75.dp, navLiquidGlassRim(dark), shape),
        )

        val cellWidth = maxWidth / navCalendarAvailableLevels.size
        val target = activeIndex.toFloat()
        val slide by animateFloatAsState(
            targetValue = target,
            animationSpec = spring(
                dampingRatio = 1f,
                stiffness = 550f,
                visibilityThreshold = .001f,
            ),
            label = "calendarGlassLensSlide",
        )
        val moving = abs(slide - target) > .015f
        val matte by animateFloatAsState(
            targetValue = if (moving) 0f else 1f,
            animationSpec = spring(dampingRatio = 1f, stiffness = 700f),
            label = "calendarGlassLensMatte",
        )
        val stretch by animateFloatAsState(
            targetValue = if (moving) 1f else 0f,
            animationSpec = spring(dampingRatio = 1f, stiffness = 600f),
            label = "calendarGlassLensStretch",
        )
        var repeatedTapSerial by remember(activeIndex) { mutableIntStateOf(0) }
        val tapPulse = remember(activeIndex) { Animatable(1f) }
        LaunchedEffect(activeIndex, repeatedTapSerial) {
            if (repeatedTapSerial > 0) {
                tapPulse.animateTo(NavLiquidDockTabs.repeatTapScale, tween(85))
                tapPulse.animateTo(1f, tween(155))
            }
        }
        // A little quieter than the floating dock's 1.30x lens while preserving
        // the EXACT spring/pulse timing and neutral settled material.
        val scale = (1f + .12f * stretch) * tapPulse.value
        val lensWidth = (cellWidth - 4.dp) * scale
        val lensHeight = navCalendarModeLensHeight * scale
        val lensX = (cellWidth * (slide + .5f) - lensWidth / 2f)
            .coerceIn(2.dp, (maxWidth - 2.dp - lensWidth).coerceAtLeast(2.dp))
        val lensY = (navCalendarModeHeight - lensHeight) / 2f
        val travel = if (dark) Color(0x66528BAF) else Color(0x66FFFFFF)
        val resting = if (dark) Color(0xE53F4B58) else Color(0xEED7D8DA)
        val lensFace = lerp(travel, resting, matte.coerceIn(0f, 1f))
        val lensShape = RoundedCornerShape(22.dp)
        Box(
            Modifier.offset(x = lensX, y = lensY)
                .size(width = lensWidth, height = lensHeight)
                .clip(lensShape)
                .background(
                    Brush.verticalGradient(
                        listOf(lensFace.copy(alpha = lensFace.alpha.coerceAtMost(.97f)),
                            lensFace),
                    ), lensShape,
                )
                .border(
                    .75.dp,
                    Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = if (dark) .36f else .62f),
                            Color.White.copy(alpha = .07f)),
                    ), lensShape,
                ),
        )

        Row(
            Modifier.fillMaxSize().padding(2.dp).selectableGroup(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            labels.forEachIndexed { index, label ->
                val selected = index == activeIndex
                val interaction = remember { MutableInteractionSource() }
                val pressed by interaction.collectIsPressedAsState()
                val textScale by animateFloatAsState(
                    targetValue = if (pressed) .94f else 1f,
                    animationSpec = spring(dampingRatio = 1f, stiffness = 1600f),
                    label = "calendarGlassTabPress",
                )
                Box(
                    Modifier.weight(1f).fillMaxHeight()
                        .clip(RoundedCornerShape(20.dp))
                        .selectable(
                            selected = selected,
                            role = Role.Tab,
                            interactionSource = interaction,
                            indication = null,
                            onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                if (selected) repeatedTapSerial++
                                else onSelect(navCalendarAvailableLevels[index])
                            },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        label,
                        color = if (selected) activeBlue else inactiveInk,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                        fontSize = 13.sp,
                        maxLines = 1,
                        modifier = Modifier.graphicsLayer {
                            scaleX = textScale
                            scaleY = textScale
                        },
                    )
                }
            }
        }
    }
}
