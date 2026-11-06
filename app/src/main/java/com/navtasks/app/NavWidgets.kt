package com.navtasks.app

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.RemoteViews
import java.time.LocalDate

private const val WIDGET_TOGGLE = "com.navtasks.app.WIDGET_TOGGLE"

open class NavBaseWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        NavWidgetUpdater.updateAll(context, TaskStore(context))
    }

    override fun onAppWidgetOptionsChanged(
        context: Context, manager: AppWidgetManager, id: Int, newOptions: Bundle
    ) {
        super.onAppWidgetOptionsChanged(context, manager, id, newOptions)
        NavWidgetUpdater.updateAll(context, TaskStore(context))
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == WIDGET_TOGGLE) {
            if (intent.getStringExtra("profileId") != ProfileStore(context).activeId) return
            val key = intent.getStringExtra("runKey") ?: return
            val store = TaskStore(context)
            val run = store.runs[key] ?: return
            if (run.checklist.isEmpty()) store.setSimpleComplete(key, !run.completed)
            NavWidgetUpdater.updateAll(context, store)
            NavNotifications.reschedule(context, store, NavSettings(context))
        }
    }
}

class NavCompactWidgetProvider : NavBaseWidgetProvider()
class NavWideWidgetProvider : NavBaseWidgetProvider()

object NavWidgetUpdater {
    private fun togglePending(context: Context, runKey: String): PendingIntent {
        val profileId = ProfileStore(context).activeId
        val intent = Intent(context, NavCompactWidgetProvider::class.java).apply {
            action = WIDGET_TOGGLE
            data = Uri.parse("navtasks://widget/" + Uri.encode(profileId) + "/" + Uri.encode(runKey))
            putExtra("profileId", profileId)
            putExtra("runKey", runKey)
        }
        return PendingIntent.getBroadcast(context, ("widget:" + profileId + ":" + runKey).hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun darkWidget(context: Context, settings: NavSettings) = when (settings.theme) {
        UiTheme.DARK -> true
        UiTheme.LIGHT -> false
        UiTheme.SYSTEM -> context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
            Configuration.UI_MODE_NIGHT_YES
    }

    fun updateAll(context: Context, store: TaskStore = TaskStore(context)) {
        val manager = AppWidgetManager.getInstance(context)
        val settings = NavSettings(context)
        val rows = store.forDate(LocalDate.now())
        val completed = rows.count { it.second.completed }
        val pending = rows.filterNot { it.second.completed }
        val missedCount = store.missed().size
        val dark = darkWidget(context, settings)
        val language = settings.language
        val textColor = if (dark) 0xFFF2F8FF.toInt() else 0xFF193147.toInt()
        val muted = if (dark) 0xFFAFC4D8.toInt() else 0xFF5E7489.toInt()
        val accent = if (dark) 0xFF69D4BE.toInt() else 0xFF218E87.toInt()

        // Never distribute spare widget height between task rows. The row
        // group is centred as one block in the XML; each task stays compact.
        val rowIdsCompact = listOf(
            Triple(R.id.nav_widget_row_one, R.id.nav_widget_task_one, R.id.nav_widget_check_one),
            Triple(R.id.nav_widget_row_two, R.id.nav_widget_task_two, R.id.nav_widget_check_two),
        )
        val rowIdsWide = rowIdsCompact + listOf(
            Triple(R.id.nav_widget_row_three, R.id.nav_widget_task_three, R.id.nav_widget_check_three),
            Triple(R.id.nav_widget_row_four, R.id.nav_widget_task_four, R.id.nav_widget_check_four),
        )
        for (wide in listOf(false, true)) {
            val component = ComponentName(context,
                if (wide) NavWideWidgetProvider::class.java else NavCompactWidgetProvider::class.java)
            val ids = manager.getAppWidgetIds(component)
            val rowIds = if (wide) rowIdsWide else rowIdsCompact
            for (id in ids) {
                val height = manager.getAppWidgetOptions(id)
                    .getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT,
                        if (wide) 120 else 110)
                val capacity = if (wide) when {
                    height >= 192 -> 4
                    height >= 154 -> 3
                    else -> 2
                } else if (height >= 147) 2 else 1
                val count = if (pending.isEmpty()) 1 else minOf(capacity, pending.size)

                val view = RemoteViews(context.packageName,
                    if (wide) R.layout.navtasks_widget_wide else R.layout.navtasks_widget_compact)
                view.setInt(R.id.nav_widget_root, "setBackgroundResource",
                    if (dark) R.drawable.navtasks_widget_bg else R.drawable.navtasks_widget_bg_light)
                view.setTextViewText(R.id.nav_widget_title, localized("Сегодня", "Today", language))
                view.setTextViewText(R.id.nav_widget_progress, "$completed/${rows.size}")
                view.setTextColor(R.id.nav_widget_title, textColor)
                view.setTextColor(R.id.nav_widget_progress, accent)
                view.setProgressBar(R.id.nav_widget_bar, rows.size.coerceAtLeast(1), completed, false)
                view.setOnClickPendingIntent(R.id.nav_widget_root, NavNotifications.openTask(context))

                for ((index, idsForRow) in rowIds.withIndex()) {
                    val (rowId, nameId, checkId) = idsForRow
                    if (index >= count) {
                        view.setViewVisibility(rowId, View.GONE)
                        continue
                    }
                    view.setViewVisibility(rowId, View.VISIBLE)
                    val pair = pending.getOrNull(index)
                    if (pair == null) {
                        view.setTextViewText(nameId,
                            if (rows.isEmpty()) localized("Нет задач на сегодня", "No tasks today", language)
                            else localized("Всё выполнено", "All done", language))
                        view.setTextViewText(checkId, "✓")
                        view.setOnClickPendingIntent(nameId, NavNotifications.openTask(context))
                        view.setOnClickPendingIntent(checkId, NavNotifications.openTask(context))
                    } else {
                        val (task, run) = pair
                        view.setTextViewText(nameId, task.title)
                        // Simple tasks toggle; a checklist always opens its editor.
                        view.setTextViewText(checkId, if (run.checklist.isEmpty()) "○" else "☷")
                        val action = if (run.checklist.isEmpty()) togglePending(context, run.key)
                            else NavNotifications.openTask(context, run.key)
                        view.setOnClickPendingIntent(checkId, action)
                        view.setOnClickPendingIntent(nameId, action)
                    }
                    view.setTextColor(nameId, textColor)
                    view.setTextColor(checkId, accent)
                }

                val status = when {
                    rows.isEmpty() -> localized("Добавьте первую задачу", "Add your first task", language)
                    pending.isEmpty() -> localized("Все задачи выполнены", "All tasks completed", language)
                    else -> localized("Осталось: ${pending.size}", "Remaining: ${pending.size}", language)
                }
                val hidden = (pending.size - count).coerceAtLeast(0)
                val secondary = when {
                    missedCount > 0 -> localized("Пропущено: $missedCount", "Missed: $missedCount", language)
                    hidden > 0 -> localized("Ещё $hidden", "$hidden more", language)
                    else -> ""
                }
                view.setTextViewText(R.id.nav_widget_status, status)
                view.setTextViewText(R.id.nav_widget_secondary, secondary)
                view.setTextColor(R.id.nav_widget_status, muted)
                view.setTextColor(R.id.nav_widget_secondary,
                    if (missedCount > 0) 0xFFF0B493.toInt() else muted)
                view.setViewVisibility(R.id.nav_widget_secondary,
                    if (secondary.isEmpty()) View.GONE else View.VISIBLE)
                manager.updateAppWidget(id, view)
            }
        }
    }
}
