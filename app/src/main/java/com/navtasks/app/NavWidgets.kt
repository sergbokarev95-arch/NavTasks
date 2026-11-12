package com.navtasks.app

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.RemoteViews
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Widgets display statistics only. Completion remains exclusively inside NavTasks. */
internal const val NAV_WIDGET_DIRECT_COMPLETE = false
internal fun widgetMissedVisible(count: Int): Boolean = count > 0
internal fun widgetAccentColor(dark: Boolean): Int =
    if (dark) 0xFF5CB8FF.toInt() else 0xFF0071E3.toInt()

internal data class NavWidgetStats(
    val total: Int,
    val completed: Int,
    val tomorrowTotal: Int,
    val missed: Int,
) {
    val remaining: Int get() = (total - completed).coerceAtLeast(0)
    val allCompleted: Boolean get() = total > 0 && remaining == 0
    val percentage: Int get() = if (total == 0) 0
        else (completed.coerceIn(0, total) * 100 / total)
}

internal fun widgetStats(today: DayCounts, tomorrow: DayCounts, missed: Int): NavWidgetStats =
    NavWidgetStats(
        total = today.total.coerceAtLeast(0),
        completed = today.done.coerceIn(0, today.total.coerceAtLeast(0)),
        tomorrowTotal = tomorrow.total.coerceAtLeast(0),
        missed = missed.coerceAtLeast(0),
    )

internal fun widgetHeaderDate(date: LocalDate, language: UiLanguage): String {
    val locale = if (language == UiLanguage.RU) Locale("ru") else Locale.ENGLISH
    return date.format(DateTimeFormatter.ofPattern(
        if (language == UiLanguage.RU) "d MMM" else "MMM d", locale))
}

private fun widgetRuTaskCount(value: Int): String {
    val v = value.coerceAtLeast(0)
    val word = when {
        v % 100 in 11..14 -> "задач"
        v % 10 == 1 -> "задача"
        v % 10 in 2..4 -> "задачи"
        else -> "задач"
    }
    return "$v $word"
}

internal fun widgetSummaryLabel(stats: NavWidgetStats, language: UiLanguage): String = when {
    stats.remaining > 0 -> if (language == UiLanguage.RU)
        "Осталось ${widgetRuTaskCount(stats.remaining)}" else
        "${stats.remaining} remaining"
    stats.tomorrowTotal > 0 -> if (language == UiLanguage.RU)
        "Завтра ${widgetRuTaskCount(stats.tomorrowTotal)}" else
        "Tomorrow: ${stats.tomorrowTotal} tasks"
    stats.allCompleted -> localized("Всё выполнено", "All done", language)
    else -> localized("Сегодня нет задач", "No tasks today", language)
}

internal fun widgetWideDetail(
    stats: NavWidgetStats, pendingTitles: List<String>, language: UiLanguage,
): List<String> = when {
    stats.remaining > 0 -> pendingTitles.take(2)
    stats.tomorrowTotal > 0 -> listOf(
        localized("Сегодня всё выполнено", "All done today", language),
        localized("План на завтра", "Tomorrow's plan", language),
    )
    stats.allCompleted -> listOf(localized("Сегодня всё выполнено", "All done today", language))
    else -> listOf(localized("Свободный день", "No tasks today", language))
}

open class NavBaseWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        NavWidgetUpdater.updateAll(context, TaskStore(context))
    }

    override fun onAppWidgetOptionsChanged(
        context: Context, manager: AppWidgetManager, id: Int, newOptions: Bundle,
    ) {
        super.onAppWidgetOptionsChanged(context, manager, id, newOptions)
        NavWidgetUpdater.updateAll(context, TaskStore(context))
    }

    // Intentionally NO WIDGET_TOGGLE receiver. The launcher cannot alter task state.
}

class NavCompactWidgetProvider : NavBaseWidgetProvider()
class NavWideWidgetProvider : NavBaseWidgetProvider()

object NavWidgetUpdater {
    private fun darkWidget(context: Context, settings: NavSettings) = when (settings.theme) {
        UiTheme.DARK -> true
        UiTheme.LIGHT -> false
        UiTheme.SYSTEM -> context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
            Configuration.UI_MODE_NIGHT_YES
    }

    fun updateAll(context: Context, store: TaskStore = TaskStore(context)) {
        val manager = AppWidgetManager.getInstance(context)
        val today = LocalDate.now()
        // dayCounts is read-only, including unmaterialized future calendar events:
        // tomorrow's number must not create tasks or modify schedule persistence.
        val stats = widgetStats(
            today = store.dayCounts(today),
            tomorrow = store.dayCounts(today.plusDays(1)),
            missed = store.missed().size,
        )
        val pendingTitles = store.forDate(today)
            .filterNot { it.second.completed }
            .map { it.first.title }
        val settings = NavSettings(context)
        val dark = darkWidget(context, settings)
        val language = settings.language
        val foreground = if (dark) 0xFFF4F6FA.toInt() else 0xFF20252E.toInt()
        val muted = if (dark) 0xFFB1BDCE.toInt() else 0xFF697383.toInt()
        val accent = widgetAccentColor(dark)
        val warning = if (dark) 0xFFFF9C96.toInt() else 0xFFBD4238.toInt()
        val detail = widgetWideDetail(stats, pendingTitles, language)

        for (wide in listOf(false, true)) {
            val component = ComponentName(context,
                if (wide) NavWideWidgetProvider::class.java else NavCompactWidgetProvider::class.java)
            val layoutId = if (wide) R.layout.navtasks_widget_wide else R.layout.navtasks_widget_compact
            for (id in manager.getAppWidgetIds(component)) {
                val view = RemoteViews(context.packageName, layoutId)
                view.setInt(R.id.nav_widget_root, "setBackgroundResource",
                    if (dark) R.drawable.navtasks_widget_bg else R.drawable.navtasks_widget_bg_light)
                view.setTextViewText(R.id.nav_widget_title,
                    localized("Сегодня", "Today", language))
                view.setTextViewText(R.id.nav_widget_date, widgetHeaderDate(today, language))
                view.setTextViewText(R.id.nav_widget_progress, "${stats.completed}/${stats.total}")
                view.setTextViewText(R.id.nav_widget_status, widgetSummaryLabel(stats, language))
                view.setTextColor(R.id.nav_widget_title, foreground)
                view.setTextColor(R.id.nav_widget_date, muted)
                view.setTextColor(R.id.nav_widget_progress, foreground)
                view.setTextColor(R.id.nav_widget_status, if (stats.remaining == 0) accent else muted)
                view.setProgressBar(R.id.nav_widget_bar, stats.total.coerceAtLeast(1),
                    stats.completed, false)
                view.setTextViewText(R.id.nav_widget_secondary, localized(
                    "Пропущено: ${stats.missed}", "Missed: ${stats.missed}", language))
                view.setTextColor(R.id.nav_widget_secondary, warning)
                view.setViewVisibility(R.id.nav_widget_secondary,
                    if (widgetMissedVisible(stats.missed)) View.VISIBLE else View.GONE)
                view.setOnClickPendingIntent(R.id.nav_widget_root,
                    NavNotifications.openTask(context))
                // NO row clicks, checkboxes or completion PendingIntents in either size.
                if (wide) {
                    view.setTextViewText(R.id.nav_widget_detail_one, detail.getOrNull(0) ?: "")
                    view.setTextViewText(R.id.nav_widget_detail_two, detail.getOrNull(1) ?: "")
                    view.setTextColor(R.id.nav_widget_detail_one, foreground)
                    view.setTextColor(R.id.nav_widget_detail_two, muted)
                    view.setTextViewText(R.id.nav_widget_percent, "${stats.percentage}%")
                    view.setTextColor(R.id.nav_widget_percent, accent)
                }
                manager.updateAppWidget(id, view)
            }
        }
    }
}
