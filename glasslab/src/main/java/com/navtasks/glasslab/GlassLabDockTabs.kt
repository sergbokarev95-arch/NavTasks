package com.navtasks.glasslab

/** Production NavTasks 0.4.59 four-tab order, labels and glyph identifiers. */
internal data class GlassLabDockTab(val glyph: String, val label: String)

internal object GlassLabDockTabs {
    const val maxStretchDp = 28f
    val items = listOf(
        GlassLabDockTab("today", "Сегодня"),
        GlassLabDockTab("work", "Работы"),
        GlassLabDockTab("handover", "Handover"),
        GlassLabDockTab("tasks", "Задачи")
    )
}
