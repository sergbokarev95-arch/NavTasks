package com.navtasks.app

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.ColorStateList
import android.os.Build
import android.net.Uri
import android.view.View
import android.widget.RemoteViews
import java.time.LocalDate

private const val WIDGET_TOGGLE = "com.navtasks.app.WIDGET_TOGGLE"

/** Keep visible task count and the overflow label in sync across widget sizes. */
internal fun widgetOverflowCount(pendingCount: Int, wide: Boolean): Int =
    (pendingCount - if (wide) 3 else 2).coerceAtLeast(0)


open class NavBaseWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
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
        val accent = if (dark) 0xFF80DCC4.toInt() else 0xFF166E6B.toInt()

        for (wide in listOf(false, true)) {
            val component = ComponentName(context,
                if (wide) NavWideWidgetProvider::class.java else NavCompactWidgetProvider::class.java)
            val ids = manager.getAppWidgetIds(component)
            if (ids.isEmpty()) continue
            val view = RemoteViews(context.packageName,
                if (wide) R.layout.navtasks_widget_wide else R.layout.navtasks_widget_compact)
            view.setInt(R.id.nav_widget_root, "setBackgroundResource",
                if (dark) R.drawable.navtasks_widget_bg else R.drawable.navtasks_widget_bg_light)
            view.setTextViewText(R.id.nav_widget_title, localized("Сегодня", "Today", language))
            view.setTextViewText(R.id.nav_widget_progress, "$completed/${rows.size}")
            view.setTextColor(R.id.nav_widget_title, textColor)
            view.setTextColor(R.id.nav_widget_progress, accent)
            view.setProgressBar(R.id.nav_widget_bar, rows.size.coerceAtLeast(1), completed, false)
            // API 31+ RemoteViews supports ProgressBar tint changes at apply time.
            // Keep the XML-defined fallback for older Samsung/Android devices.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                view.setColorStateList(
                    R.id.nav_widget_bar, "setProgressTintList",
                    ColorStateList.valueOf(accent),
                )
                view.setColorStateList(
                    R.id.nav_widget_bar, "setProgressBackgroundTintList",
                    ColorStateList.valueOf(if (dark) 0xFF41566A.toInt() else 0xFFBED4DD.toInt()),
                )
            }
            view.setTextViewText(R.id.nav_widget_missed, localized("Пропущено: $missedCount", "Missed: $missedCount", language))
            view.setTextColor(R.id.nav_widget_missed, if (missedCount == 0) muted else 0xFFF0B493.toInt())
            view.setOnClickPendingIntent(R.id.nav_widget_root, NavNotifications.openTask(context))

            // RemoteViews has no intrinsic scrolling. Keep a fixed, compact number
            // of pending tasks and show an honest overflow counter for the rest.
            fun renderTask(index: Int, rowId: Int, nameId: Int, checkId: Int) {
                val pair = pending.getOrNull(index)
                if (pair == null && (index > 0 || pending.isNotEmpty())) {
                    view.setViewVisibility(rowId, View.GONE)
                    return
                }
                view.setViewVisibility(rowId, View.VISIBLE)
                if (pair == null) {
                    view.setTextViewText(
                        nameId,
                        if (rows.isEmpty()) localized("На сегодня задач нет", "No tasks today", language)
                        else localized("Все задачи выполнены", "All tasks completed", language),
                    )
                    view.setTextViewText(checkId, if (rows.isEmpty()) "·" else "✓")
                    val open = NavNotifications.openTask(context)
                    view.setOnClickPendingIntent(nameId, open)
                    view.setOnClickPendingIntent(checkId, open)
                } else {
                    val (task, run) = pair
                    view.setTextViewText(nameId, task.title)
                    view.setTextViewText(checkId, if (run.checklist.isEmpty()) "◯" else "☷")
                    // Task title opens its details. Only the check control toggles a
                    // checklist-free task: avoiding accidental completions on title tap.
                    view.setOnClickPendingIntent(nameId, NavNotifications.openTask(context, run.key))
                    val action = if (run.checklist.isEmpty()) togglePending(context, run.key)
                        else NavNotifications.openTask(context, run.key)
                    view.setOnClickPendingIntent(checkId, action)
                }
                view.setTextColor(nameId, textColor)
                view.setTextColor(checkId, accent)
            }

            renderTask(0, R.id.nav_widget_row_one,
                R.id.nav_widget_task_one, R.id.nav_widget_check_one)
            renderTask(1, R.id.nav_widget_row_two,
                R.id.nav_widget_task_two, R.id.nav_widget_check_two)
            if (wide) {
                renderTask(2, R.id.nav_widget_row_three,
                    R.id.nav_widget_task_three, R.id.nav_widget_check_three)
            }

            val remaining = widgetOverflowCount(pending.size, wide)
            view.setTextViewText(
                R.id.nav_widget_more,
                if (remaining > 0) localized("+$remaining ещё", "+$remaining more", language) else "",
            )
            view.setTextColor(R.id.nav_widget_more, muted)
            view.setOnClickPendingIntent(R.id.nav_widget_more, NavNotifications.openTask(context))
            manager.updateAppWidget(ids, view)
        }
    }
}
