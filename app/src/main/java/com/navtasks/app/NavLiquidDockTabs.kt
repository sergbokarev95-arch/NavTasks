package com.navtasks.app

import androidx.compose.ui.graphics.Color
import kotlin.math.roundToInt

/** Production NavTasks 0.4.59 four-tab order, labels and glyph identifiers. */
internal data class NavLiquidDockTab(val glyph: String, val label: String)

internal object NavLiquidDockTabs {
    // Resting selection stays 52dp inside a 56dp bar.
    // Calendar-style transition: the selected capsule keeps its geometry
    // while travelling; no delayed stretch/rebound overshoot.
    const val dockHeightDp = 56f
    const val restingLensHeightDp = 52f
    const val movingScale = 1.0f
    const val repeatTapScale = 1.025f

    fun scaleAt(progress: Float): Float =
        1f + (movingScale - 1f) * progress.coerceIn(0f, 1f)

    // Based on the user's HONOR Calendar screen recording. Active icon/label
    // use the same vivid saturated coral in both themes. Dock only.
    fun activeInk(dark: Boolean): Color =
        Color(0xFFFF5533)

    // A dragged lens must stay within the real four-slot dock and commit
    // exactly ONE destination only when the finger is released.
    fun clampedPosition(position: Float): Float =
        if (position.isFinite()) position.coerceIn(0f, (items.size - 1).toFloat())
        else 0f

    fun destinationForDrag(position: Float): Int =
        clampedPosition(position).roundToInt().coerceIn(0, items.lastIndex)
    val items = listOf(
        NavLiquidDockTab("today", "Сегодня"),
        NavLiquidDockTab("work", "Работы"),
        NavLiquidDockTab("handover", "Handover"),
        NavLiquidDockTab("reference", "Справочник")
    )
}
