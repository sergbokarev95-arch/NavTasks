package com.navtasks.app

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

/** Android alarm and notification delivery. All dates use the phone's zone. */
object NavNotifications {
    private const val ACTION_ALARM = "com.navtasks.app.ALARM"
    private const val ACTION_TASK = "com.navtasks.app.TASK_ACTION"
    private const val REMINDER = "reminder"
    private const val MISSED = "missed"
    private const val DIGEST = "digest"
    // Independent midnight horizon refresh, separate from missed-task digest.
    private const val ROLL = "roll"
    private const val SERVICE = "service"
    private const val AMOS = "amos"
    private const val CHANNEL_TASKS = "navtasks-reminders"
    private const val CHANNEL_MISSED = "navtasks-missed"

    private fun manager(context: Context) = context.getSystemService(NotificationManager::class.java)

    /** Original installed app icon, not a surrogate checkmark, in notification cards. */
    private fun appIconBitmap(context: Context): Bitmap {
        val size = (64f * context.resources.displayMetrics.density).toInt().coerceAtLeast(64)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val drawable = context.packageManager.getApplicationIcon(context.applicationInfo)
        drawable.setBounds(0, 0, size, size)
        drawable.draw(Canvas(bitmap))
        return bitmap
    }

    private fun notificationBuilder(context: Context, channel: String): Notification.Builder =
        Notification.Builder(context, channel)
            .setSmallIcon(R.drawable.navtasks_notification)
            .setLargeIcon(appIconBitmap(context))

    fun createChannels(context: Context) {
        manager(context).createNotificationChannel(NotificationChannel(
            CHANNEL_TASKS, "NavTasks reminders", NotificationManager.IMPORTANCE_DEFAULT,
        ))
        manager(context).createNotificationChannel(NotificationChannel(
            CHANNEL_MISSED, "NavTasks missed tasks", NotificationManager.IMPORTANCE_DEFAULT,
        ))
    }

    private fun pending(context: Context, kind: String, key: String): PendingIntent {
        val profileId = ProfileStore(context).activeId
        val intent = Intent(context, NavAlarmReceiver::class.java).apply {
            action = ACTION_ALARM
            data = Uri.parse("navtasks://alarm/" + kind + "/" + Uri.encode(profileId) + "/" + Uri.encode(key))
            putExtra("profileId", profileId)
            putExtra("kind", kind)
            putExtra("runKey", key)
        }
        return PendingIntent.getBroadcast(context, ("alarm:" + profileId + ":" + kind + ":" + key).hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun actionPending(context: Context, runKey: String, actionKind: String): PendingIntent {
        val profileId = ProfileStore(context).activeId
        val intent = Intent(context, NavAlarmReceiver::class.java).apply {
            action = ACTION_TASK
            data = Uri.parse("navtasks://action/" + actionKind + "/" + Uri.encode(profileId) + "/" + Uri.encode(runKey))
            putExtra("runKey", runKey)
            putExtra("profileId", profileId)
            putExtra("taskAction", actionKind)
        }
        return PendingIntent.getBroadcast(context, ("action:" + profileId + ":" + actionKind + ":" + runKey).hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    fun openTask(context: Context, runKey: String? = null): PendingIntent {
        val profileId = ProfileStore(context).activeId
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            if (runKey != null) putExtra("runKey", runKey)
            putExtra("profileId", profileId)
            data = Uri.parse("navtasks://open/" + Uri.encode(profileId) + "/" + Uri.encode(runKey ?: "today"))
        }
        return PendingIntent.getActivity(context, ("open:" + profileId + ":" + runKey).hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    /** Open the overdue backlog from the widget's red missed-count chip. */
    fun openMissed(context: Context): PendingIntent {
        val profileId = ProfileStore(context).activeId
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            data = Uri.parse("navtasks://open-missed/" + Uri.encode(profileId))
            putExtra("profileId", profileId)
            putExtra("openMissed", true)
        }
        return PendingIntent.getActivity(context, ("open-missed:" + profileId).hashCode(),
            intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun openWorkOrder(context: Context, id: String): PendingIntent {
        val profileId = ProfileStore(context).activeId
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("workOrderId", id)
            putExtra("profileId", profileId)
            data = Uri.parse("navtasks://open-amos/" + Uri.encode(profileId) + "/" + Uri.encode(id))
        }
        return PendingIntent.getActivity(context,
            ("open-amos:" + profileId + ":" + id).hashCode(),
            intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun schedule(context: Context, kind: String, key: String, whenMillis: Long) {
        if (whenMillis <= System.currentTimeMillis()) return
        val alarms = context.getSystemService(AlarmManager::class.java)
        val request = pending(context, kind, key)
        if (Build.VERSION.SDK_INT >= 31 && alarms.canScheduleExactAlarms()) {
            runCatching { alarms.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, whenMillis, request) }
                .onFailure { alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, whenMillis, request) }
        } else {
            alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, whenMillis, request)
        }
    }

    private fun dueTime(task: TaskTemplate, run: TaskRun): LocalDateTime =
        if (task.deadlineTime == null) run.displayDate.plusDays(1).atStartOfDay()
        else run.displayDate.atTime(task.deadlineTime)

    private fun referenceTime(settings: NavSettings): LocalTime =
        LocalTime.of(settings.reminderHour, settings.reminderMinute)

    private fun reminderTime(
        task: TaskTemplate, run: TaskRun, settings: NavSettings,
    ): LocalDateTime = SmartReminderTiming.trigger(task, run, referenceTime(settings))

    private fun cancelAlarm(context: Context, kind: String, key: String) {
        context.getSystemService(AlarmManager::class.java).cancel(pending(context, kind, key))
    }

    fun reschedule(context: Context, store: TaskStore, settings: NavSettings) {
        createChannels(context)
        if (!settings.notificationsEnabled) return
        val today = LocalDate.now()
        // Keep the AlarmManager queue bounded; independent ROLL extends the horizon.
        val horizon = today.plusDays(7)
        store.refresh(today, horizon)
        for (run in store.runs.values) {
            if (run.completed || run.skipped || run.displayDate.isAfter(horizon)) continue
            val task = store.templates.firstOrNull { it.id == run.taskId && it.enabled && it.notificationsEnabled }
                ?: continue
            val deadline = dueTime(task, run).atZone(ZoneId.systemDefault()).toInstant()
            // Replace the old midnight-based PendingIntent when the global
            // reference clock or the task's reminder interval changes.
            cancelAlarm(context, REMINDER, run.key)
            val reminderAt = reminderTime(task, run, settings)
                .atZone(ZoneId.systemDefault()).toInstant()
            if (SmartReminderTiming.isAtOrBeforeDeadline(task, run, referenceTime(settings)) &&
                reminderAt.isAfter(Instant.now())) {
                // "At time" (0 minutes) must schedule a real alarm.
                schedule(context, REMINDER, run.key, reminderAt.toEpochMilli())
            }
            if (deadline.isAfter(Instant.now())) schedule(context, MISSED, run.key, deadline.toEpochMilli())
        }
        // A single early warning per maintenance due date, scoped to the
        // currently selected ship. Save no photos or certificate contents.
        val profiles = ProfileStore(context)
        if (profiles.active.kind == NavProfileKind.VESSEL) {
            val notified = context.getSharedPreferences(
                "navtasks-delivered-notifications", Context.MODE_PRIVATE
            ).getStringSet("delivered", emptySet()).orEmpty()
            val current = LocalDateTime.now()
            for (service in ServiceStore(context, profiles).entries) {
                // AMOS requisitions without a planned date never schedule a due alarm.
                if (service.unplanned || service.completedAt != null ||
                    service.remindMonths == 0) continue
                val due = service.nextDate ?: continue
                if (due.isBefore(today)) continue
                val notificationKey = profiles.activeId + ":service:" +
                    service.id + ":" + due
                if (notificationKey in notified) continue
                val start = serviceWarningStart(service)
                var alarm = (if (start.isBefore(today)) today else start)
                    .atTime(referenceTime(settings))
                if (!alarm.isAfter(current)) alarm = current.plusMinutes(2)
                if (alarm.toLocalDate().isAfter(due)) continue
                schedule(context, SERVICE, service.id + "|" + due,
                    alarm.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
            }
            // One configurable early warning per open AMOS work order and due date.
            // Stale alarms from edited due dates or completed work are ignored at delivery.
            for (order in WorkOrderStore(context, profiles).entries) {
                if (order.completed || order.remindDaysBefore == 0 ||
                    order.dueDate.isBefore(today)) continue
                val key = profiles.activeId + ":amos:" + order.id + ":" +
                    order.dueDate + ":" + order.remindDaysBefore
                if (key in notified) continue
                val start = workOrderWarningStart(order)
                var alarm = (if (start.isBefore(today)) today else start)
                    .atTime(referenceTime(settings))
                if (!alarm.isAfter(current)) alarm = current.plusMinutes(2)
                if (alarm.toLocalDate().isAfter(order.dueDate)) continue
                schedule(context, AMOS,
                    order.id + "|" + order.dueDate + "|" + order.remindDaysBefore,
                    alarm.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
            }
        }
        if (settings.dailyDigestEnabled) {
            val clock = LocalTime.of(settings.digestHour, settings.digestMinute)
            var next = today.atTime(clock)
            if (!next.isAfter(LocalDateTime.now())) next = next.plusDays(1)
            schedule(context, DIGEST, "daily", next.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
        } else {
            cancelAlarm(context, DIGEST, "daily")
        }
        // Reminders must extend beyond the seven-day window even when
        // the separate daily MISSED digest is disabled.
        val nextRoll = today.plusDays(1).atTime(0, 5)
        schedule(context, ROLL, "next-day",
            nextRoll.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
    }

    private fun mayNotify(context: Context): Boolean =
        Build.VERSION.SDK_INT < 33 ||
            context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    private fun alreadyNotified(context: Context, key: String): Boolean {
        val prefs = context.getSharedPreferences("navtasks-delivered-notifications", Context.MODE_PRIVATE)
        val set = prefs.getStringSet("delivered", emptySet()).orEmpty()
        if (key in set) return true
        prefs.edit().putStringSet("delivered", set + key).apply()
        return false
    }

    fun receiveAlarm(context: Context, kind: String, runKey: String) {
        val settings = NavSettings(context)
        if (!settings.notificationsEnabled) return
        val store = TaskStore(context)
        createChannels(context)
        if (kind == ROLL) {
            reschedule(context, store, settings)
            return
        }
        if (kind == DIGEST) {
            if (settings.dailyDigestEnabled) {
                val overdue = store.missed()
                if (overdue.isNotEmpty() && mayNotify(context)) {
                    val heading = localized("Пропущенные задачи: ${overdue.size}",
                        "Missed tasks: ${overdue.size}", settings.language)
                    val details = overdue.take(4).joinToString(" · ") { it.first.title }
                    val notification = notificationBuilder(context, CHANNEL_MISSED)
                        .setContentTitle(heading).setContentText(details)
                        .setStyle(Notification.BigTextStyle().bigText(details))
                        .setContentIntent(openTask(context)).setAutoCancel(true).build()
                    manager(context).notify(12500, notification)
                }
            }
            reschedule(context, store, settings)
            return
        }
        if (kind == SERVICE) {
            val serviceId = runKey.substringBefore("|")
            val expectedDue = runKey.substringAfter("|", "")
            val profiles = ProfileStore(context)
            if (profiles.active.kind != NavProfileKind.VESSEL) return
            val service = ServiceStore(context, profiles).entries
                .firstOrNull { it.id == serviceId } ?: return
            if (service.unplanned || service.completedAt != null) return
            val due = service.nextDate ?: return
            if (due.toString() != expectedDue ||
                service.remindMonths == 0 || LocalDate.now().isBefore(serviceWarningStart(service)) ||
                LocalDate.now().isAfter(due)) return
            if (!mayNotify(context)) return
            if (!alreadyNotified(context,
                profiles.activeId + ":service:" + service.id + ":" + due)) return
            val heading = localized("Приближается сервис", "Maintenance due soon", settings.language)
            val detail = service.equipment + " · " +
                localized("до ", "due ", settings.language) + due
            val notification = notificationBuilder(context, CHANNEL_TASKS)
                .setContentTitle(heading).setContentText(detail)
                .setStyle(Notification.BigTextStyle().bigText(detail))
                .setContentIntent(openTask(context)).setAutoCancel(true).build()
            manager(context).notify(("service:" + service.id + due).hashCode(),
                notification)
            return
        }
        if (kind == AMOS) {
            val parts = runKey.split("|")
            if (parts.size != 3) return
            val profiles = ProfileStore(context)
            if (profiles.active.kind != NavProfileKind.VESSEL) return
            val order = WorkOrderStore(context, profiles).entries
                .firstOrNull { it.id == parts[0] } ?: return
            if (order.completed || order.remindDaysBefore == 0 ||
                order.dueDate.toString() != parts[1] ||
                order.remindDaysBefore.toString() != parts[2] ||
                LocalDate.now().isBefore(workOrderWarningStart(order)) ||
                LocalDate.now().isAfter(order.dueDate)) return
            if (!mayNotify(context)) return
            if (alreadyNotified(context, profiles.activeId + ":amos:" +
                    order.id + ":" + order.dueDate + ":" + order.remindDaysBefore)) return
            val detail = order.reference + " · " + order.title + " · " +
                localized("до ", "due ", settings.language) + order.dueDate
            val notification = notificationBuilder(context, CHANNEL_TASKS)
                .setContentTitle(localized("AMOS: срок работы", "AMOS: work due", settings.language))
                .setContentText(detail)
                .setStyle(Notification.BigTextStyle().bigText(detail))
                .setContentIntent(openWorkOrder(context, order.id))
                .setAutoCancel(true).build()
            manager(context).notify(("amos:" + order.id + order.dueDate).hashCode(),
                notification)
            return
        }
        val run = store.runs[runKey] ?: return
        val task = store.templates.firstOrNull { it.id == run.taskId } ?: return
        if (run.completed || run.skipped || !task.enabled || !task.notificationsEnabled) return
        val deadline = dueTime(task, run).atZone(ZoneId.systemDefault()).toInstant()
        val reminder = reminderTime(task, run, settings)
            .atZone(ZoneId.systemDefault()).toInstant()
        val now = Instant.now()
        // Ignore an obsolete alarm for a rescheduled occurrence or changed time zone.
        val originalDeadline = (if (task.deadlineTime == null) run.dueDate.plusDays(1).atStartOfDay()
            else run.dueDate.atTime(task.deadlineTime)).atZone(ZoneId.systemDefault()).toInstant()
        val autoMoved = kind == MISSED && run.autoTransferred && !now.isBefore(originalDeadline)
        if (kind == MISSED && now.isBefore(deadline) && !autoMoved) return
        if (kind == REMINDER && now.isBefore(reminder)) return
        if (kind == REMINDER && now.isAfter(deadline) &&
            task.remindMinutesBefore > 0) return
        if (kind == MISSED && !store.isOverdue(task, run) && !autoMoved) return
        if (!mayNotify(context)) return
        if (alreadyNotified(context, ProfileStore(context).activeId + ":" + kind + ":" + runKey + ":" + run.displayDate)) return

        val isMissed = kind == MISSED
        val message = if (autoMoved)
            localized("Срок прошёл. Задача автоматически перенесена.",
                "Deadline passed. Task moved automatically.", settings.language)
        else if (isMissed)
            localized("Срок прошёл. Задача добавлена в пропущенные.", "Deadline passed. Added to Missed.", settings.language)
        else localized("Скоро срок выполнения", "Due soon", settings.language)
        val builder = notificationBuilder(context, if (isMissed) CHANNEL_MISSED else CHANNEL_TASKS)
            .setContentTitle(task.title).setContentText(message)
            .setContentIntent(openTask(context, runKey)).setAutoCancel(true)
        if (task.checklist.isEmpty()) {
            builder.addAction(Notification.Action.Builder(null,
                localized("Выполнить", "Complete", settings.language),
                actionPending(context, runKey, "complete")).build())
        } else {
            builder.addAction(Notification.Action.Builder(null,
                localized("Чек-лист", "Checklist", settings.language), openTask(context, runKey)).build())
        }
        builder.addAction(Notification.Action.Builder(null,
            localized("На завтра", "Tomorrow", settings.language),
            actionPending(context, runKey, "postpone")).build())
        manager(context).notify(("$kind:$runKey").hashCode(), builder.build())
    }

    fun receiveAction(context: Context, runKey: String, action: String) {
        val store = TaskStore(context)
        when (action) {
            "complete" -> store.setSimpleComplete(runKey, true)
            "postpone" -> store.postpone(runKey, LocalDate.now().plusDays(1))
        }
        NavWidgetUpdater.updateAll(context, store)
        reschedule(context, store, NavSettings(context))
        manager(context).cancel(("$MISSED:$runKey").hashCode())
        manager(context).cancel(("$REMINDER:$runKey").hashCode())
    }

    fun handleBroadcast(context: Context, intent: Intent) {
        // Old alarms from prior versions or another vessel must never modify this profile.
        if (intent.getStringExtra("profileId") != ProfileStore(context).activeId) return
        val key = intent.getStringExtra("runKey").orEmpty()
        when (intent.action) {
            ACTION_ALARM -> receiveAlarm(context, intent.getStringExtra("kind").orEmpty(), key)
            ACTION_TASK -> receiveAction(context, key, intent.getStringExtra("taskAction").orEmpty())
        }
    }
}

class NavAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        NavNotifications.handleBroadcast(context, intent)
    }
}

class NavSystemTimeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action in listOf(Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_TIME_CHANGED,
                Intent.ACTION_TIMEZONE_CHANGED, Intent.ACTION_DATE_CHANGED, Intent.ACTION_MY_PACKAGE_REPLACED)) {
            val store = TaskStore(context)
            store.refresh()
            NavNotifications.reschedule(context, store, NavSettings(context))
            NavWidgetUpdater.updateAll(context, store)
        }
    }
}
