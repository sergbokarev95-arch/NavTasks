package com.navtasks.app

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

/**
 * The Task editor has EXACTLY four existing frequencies, not a new task model.
 * Mirrors the accepted Year/Month/Day calendar switch without modifying it.
 * All changes stay in editor-local state until the existing Save action.
 */
@Composable
internal fun NavTaskFrequencyControl(
    selected: Frequency,
    p: NavPalette,
    lang: UiLanguage,
    onSelect: (Frequency) -> Unit,
) {
    val frequencies = Frequency.entries
    val labels = navTaskFrequencyChoices(lang).map { it.second }
    val selectedIndex = frequencies.indexOf(selected).coerceAtLeast(0)
    val view = LocalView.current
    val shape = RoundedCornerShape(26.dp)

    BoxWithConstraints(Modifier.fillMaxWidth().height(navCalendarModeHeight)) {
        Box(
            Modifier.matchParentSize().clip(shape)
                .background(navCalendarModeSurface(p), shape)
                .border(.75.dp, navCalendarRim(p.dark), shape),
        )

        val cellWidth = maxWidth / frequencies.size
        val destination = selectedIndex.toFloat()
        val slide by animateFloatAsState(
            targetValue = destination,
            animationSpec = spring(dampingRatio = 1f, stiffness = 550f),
            label = "taskFrequencyLensSlide",
        )
        val isMoving = abs(slide - destination) > .015f
        val matte by animateFloatAsState(
            targetValue = if (isMoving) 0f else 1f,
            animationSpec = spring(dampingRatio = 1f, stiffness = 700f),
            label = "taskFrequencyLensMatte",
        )
        val lensShape = RoundedCornerShape(22.dp)
        val resting = navCalendarLensResting(p.dark)
        val traveling = navCalendarLensTraveling(p.dark)
        val face = androidx.compose.ui.graphics.lerp(
            traveling, resting, matte.coerceIn(0f, 1f),
        )
        val width = cellWidth - 4.dp
        val x = (cellWidth * (slide + .5f) - width / 2f)
            .coerceIn(2.dp, (maxWidth - 2.dp - width).coerceAtLeast(2.dp))
        Box(
            Modifier.offset(x = x, y = (navCalendarModeHeight - navCalendarModeLensHeight) / 2)
                .size(width = width, height = navCalendarModeLensHeight)
                .clip(lensShape)
                .background(face, lensShape)
                .border(.75.dp, navCalendarRim(p.dark), lensShape),
        )
        Row(
            Modifier.fillMaxSize().padding(2.dp).selectableGroup(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            labels.forEachIndexed { i, label ->
                val isSelected = i == selectedIndex
                Box(
                    Modifier.weight(1f).fillMaxHeight()
                        .clip(RoundedCornerShape(20.dp))
                        .selectable(selected = isSelected, role = Role.Tab, onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                            if (!isSelected) onSelect(frequencies[i])
                        }),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        label,
                        fontSize = 12.sp,
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                        color = if (isSelected) p.blueText else p.muted,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    )
                }
            }
        }
    }
}
