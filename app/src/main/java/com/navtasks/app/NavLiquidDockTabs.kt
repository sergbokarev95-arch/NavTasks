package com.navtasks.app

/** Production NavTasks 0.4.59 four-tab order, labels and glyph identifiers. */
internal data class NavLiquidDockTab(val glyph: String, val label: String)

internal object NavLiquidDockTabs {
    // Resting selection stays 52dp inside a 56dp bar.
    // During flight it grows uniformly: 52dp * 1.30 = 67.6dp,
    // roughly 5.8dp above and below the dock.
    const val dockHeightDp = 56f
    const val restingLensHeightDp = 52f
    const val movingScale = 1.30f
    const val heldScale = 1.15f

    // Movement keeps the accepted Lab 1.30x lens. A stationary long press
    // expands it to 1.15x until release; no idle enlargement or timing delays.
    fun stretchTarget(moving: Boolean, pressed: Boolean): Float = when {
        moving -> 1f
        pressed -> (heldScale - 1f) / (movingScale - 1f)
        else -> 0f
    }

    fun scaleAt(progress: Float): Float =
        1f + (movingScale - 1f) * progress.coerceIn(0f, 1f)

    // Press begins before clickable emits onClick, which is dispatched upon
    // pointer release. Keep the destination stable in between; a canceled
    // press (no click) returns to the current selected tab.
    fun lensTargetIndex(selected: Int, pressedTab: Int, clickedTab: Int): Int {
        val last = items.lastIndex
        val destination = when {
            pressedTab in 0..last -> pressedTab
            clickedTab in 0..last -> clickedTab
            else -> selected
        }
        return destination.coerceIn(0, last)
    }

    val items = listOf(
        NavLiquidDockTab("today", "Сегодня"),
        NavLiquidDockTab("work", "Работы"),
        NavLiquidDockTab("handover", "Handover"),
        NavLiquidDockTab("tasks", "Задачи")
    )
}
