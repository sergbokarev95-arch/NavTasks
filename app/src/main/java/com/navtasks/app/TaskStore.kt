package com.navtasks.app

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID

/**
 * Offline-first prototype repository. All edits are persisted immediately.
 * A future migration can replace SharedPreferences with Room without changing
 * the stable template IDs or occurrence keys.
 */
class TaskStore(context: Context, private val profiles: ProfileStore = ProfileStore(context)) {
    private val appContext = context.applicationContext
    private var prefs = profiles.data(profiles.activeId)
    val profileId: String get() = profiles.activeId

    var templates: List<TaskTemplate> by mutableStateOf(emptyList())
        private set
    var runs: Map<String, TaskRun> by mutableStateOf(emptyMap())
        private set

    init { reloadProfile() }

    fun switchProfile() {
        prefs = profiles.data(profiles.activeId)
        reloadProfile()
        syncOutputs()
    }

    private fun reloadProfile() {
        templates = if (prefs.contains("templates")) {
            decodeTemplates(prefs.getString("templates", "[]").orEmpty())
        } else if (profiles.activeId == ProfileStore.DEFAULT_SHIP) {
            sampleTemplates().also { saveTemplates(it) }
        } else emptyList()
        runs = decodeRuns(prefs.getString("runs", "[]").orEmpty())
        refresh(LocalDate.now())
    }

    fun refresh(today: LocalDate = LocalDate.now(), through: LocalDate = today.plusDays(6)) {
        val lastVisit = runCatching {
            LocalDate.parse(prefs.getString("lastVisit", today.toString()).orEmpty())
        }.getOrDefault(today)
        val firstDay = if (lastVisit.isAfter(today)) today else lastVisit
        val updated = runs.toMutableMap()

        // Materialize calendar occurrences for elapsed dates. Daily checkmarks
        // belong to each date, never to the reusable template.
        var date = firstDay
        while (!date.isAfter(maxOf(today, through))) {
            for (task in templates.filter { it.scheduleMode == ScheduleMode.CALENDAR }) {
                if (!TaskSchedule.isCalendarDue(task, date)) continue
                updated.putIfAbsent(key(task.id, date), TaskRun(task.id, date, task.checklist))
            }
            date = date.plusDays(1)
        }

        // A one-off captain's assignment remains visible when its deadline has
        // passed, even if it was entered after that date.
        for (task in templates.filter { it.enabled && it.frequency == Frequency.EXTRA }) {
            if (!task.startDate.isAfter(maxOf(today, through))) {
                updated.putIfAbsent(key(task.id, task.startDate),
                    TaskRun(task.id, task.startDate, task.checklist))
            }
        }

        // Interval recurrence is based on the actual completion date.
        for (task in templates.filter { it.enabled && it.scheduleMode == ScheduleMode.AFTER_COMPLETION }) {
            val latestComplete = updated.values.filter { it.taskId == task.id && it.completed }
                .maxByOrNull { it.completedAt.orEmpty() }
            val completedOn = latestComplete?.completedAt?.let {
                runCatching { Instant.parse(it).atZone(ZoneId.systemDefault()).toLocalDate() }.getOrNull()
            }
            val nextDate = TaskSchedule.nextAfterCompletion(task, completedOn)
            if (!nextDate.isAfter(maxOf(today, through)) && task.frequency != Frequency.EXTRA) {
                updated.putIfAbsent(key(task.id, nextDate), TaskRun(task.id, nextDate, task.checklist))
            }
        }

        for ((runKey, run) in updated.toMap()) {
            val task = templates.firstOrNull { it.id == run.taskId } ?: continue
            if (run.completed || run.skipped || !isOverdue(task, run, LocalDateTime.now())) continue
            when (task.missedPolicy) {
                MissedPolicy.ARCHIVE -> updated[runKey] = run.copy(skipped = true)
                MissedPolicy.CARRY -> updated[runKey] = run.copy(
                    postponedTo = if (run.displayDate.isBefore(today)) today else today.plusDays(1),
                    autoTransferred = true,
                )
                MissedPolicy.HISTORY, MissedPolicy.ASK -> Unit
            }
        }
        runs = updated
        persistRuns()
        prefs.edit().putString("lastVisit", today.toString()).apply()
    }

    fun forDate(day: LocalDate): List<Pair<TaskTemplate, TaskRun>> {
        if (day.isAfter(LocalDate.now().plusDays(6))) materializeSingleDay(day)
        return runs.values.mapNotNull { run ->
            val task = templates.firstOrNull { it.id == run.taskId && it.enabled } ?: return@mapNotNull null
            if (run.skipped || run.displayDate != day) null else task to run
        }.sortedWith(
            compareBy<Pair<TaskTemplate, TaskRun>> { if (it.second.completed) 1 else 0 }
                .thenBy { TaskSchedule.occurrenceFrequency(it.first, it.second).ordinal }
                .thenBy { it.first.title.lowercase() }
        )
    }

    /** Calendar display-only summary: never writes occurrences or mutates progress. */
    fun dayCounts(day: LocalDate): DayCounts {
        var total = 0
        var done = 0
        val enabled = templates.filter { it.enabled }.associateBy { it.id }
        // Recorded occurrences include one-off tasks, moved tasks and after-completion tasks.
        runs.values.forEach { run ->
            if (!run.skipped && run.displayDate == day && run.taskId in enabled) {
                total++
                if (run.completed) done++
            }
        }
        // Future calendar dates may not yet have a materialized TaskRun.
        // Skip a scheduled occurrence that was already transferred away or removed.
        enabled.values.forEach { task ->
            if (task.scheduleMode == ScheduleMode.CALENDAR &&
                TaskSchedule.isCalendarDue(task, day) &&
                key(task.id, day) !in runs
            ) total++
        }
        return DayCounts(total, done)
    }

    fun visible(today: LocalDate = LocalDate.now()) = forDate(today)
    fun completionCount(today: LocalDate = LocalDate.now()) = forDate(today).count { it.second.completed }

    fun isOverdue(task: TaskTemplate, run: TaskRun, now: LocalDateTime = LocalDateTime.now()): Boolean {
        if (run.completed || run.skipped) return false
        if (run.displayDate.isBefore(now.toLocalDate())) return true
        return run.displayDate == now.toLocalDate() &&
            task.deadlineTime?.let { !now.toLocalTime().isBefore(it) } == true
    }

    fun missed(now: LocalDateTime = LocalDateTime.now()): List<Pair<TaskTemplate, TaskRun>> =
        runs.values.mapNotNull { run ->
            val task = templates.firstOrNull { it.id == run.taskId && it.enabled }
            if (task != null && isOverdue(task, run, now)) task to run else null
        }.sortedWith(compareBy<Pair<TaskTemplate, TaskRun>> { it.second.dueDate }.thenBy { it.first.title })

    fun setSimpleComplete(runKey: String, complete: Boolean) {
        val run = runs[runKey] ?: return
        if (run.checklist.isNotEmpty()) return
        replaceRun(run.copy(completed = complete, completedAt = if (complete) Instant.now().toString() else null))
        refresh()
        syncOutputs()
    }

    fun setCheck(runKey: String, itemId: String, complete: Boolean) {
        val run = runs[runKey] ?: return
        if (run.checklist.none { it.id == itemId }) return
        val nextChecked = if (complete) run.checkedIds + itemId else run.checkedIds - itemId
        val completed = run.checklist.isNotEmpty() && run.checklist.all { it.id in nextChecked }
        replaceRun(run.copy(
            checkedIds = nextChecked,
            completed = completed,
            completedAt = if (completed) run.completedAt ?: Instant.now().toString() else null,
        ))
        refresh()
        syncOutputs()
    }

    fun confirmComplete(runKey: String) {
        val run = runs[runKey] ?: return
        if (!run.requiredComplete) return
        replaceRun(run.copy(completed = true, completedAt = Instant.now().toString()))
    }

    fun skip(runKey: String) {
        val run = runs[runKey] ?: return
        replaceRun(run.copy(skipped = true))
        syncOutputs()
    }

    fun closeWithReason(runKey: String, reason: String) {
        val run = runs[runKey] ?: return
        if (reason.isBlank()) return
        replaceRun(run.copy(skipped = true, closedReason = reason.trim()))
        syncOutputs()
    }

    fun postpone(runKey: String, newDate: LocalDate) {
        val run = runs[runKey] ?: return
        if (run.completed || run.skipped) return
        replaceRun(run.copy(postponedTo = newDate, autoTransferred = false))
        refresh()
        syncOutputs()
    }

    /**
     * Reschedule this occurrence and the remaining calendar series.
     * Completed history and unrelated templates are preserved.
     */
    fun postponeSeriesFrom(runKey: String, newDate: LocalDate): Boolean {
        val selected = runs[runKey] ?: return false
        val task = templates.firstOrNull { it.id == selected.taskId } ?: return false
        val result = scopedCalendarMove(task, runs, selected, newDate) ?: return false

        templates = templates.map { if (it.id == task.id) result.template else it }
        saveTemplates(templates)
        runs = result.runs
        persistRuns()
        refresh()
        syncOutputs()
        return true
    }

    /**
     * Atomic edit of calendar weekday / month day with an explicit scope.
     * The original template and run keys remain available for historical dates.
     */
    internal fun applyCalendarDayEdit(
        edited: TaskTemplate,
        scope: CalendarEditScope,
        today: LocalDate = LocalDate.now(),
    ): Boolean {
        require(profiles.active.kind == NavProfileKind.VESSEL) { "Personal profile is notes-only" }
        val original = templates.firstOrNull { it.id == edited.id } ?: return false
        val result = editCalendarDayScoped(original, edited, runs, scope, today) ?: return false
        templates = templates.map { if (it.id == edited.id) result.template else it }
        runs = result.runs
        saveTemplates(templates)
        persistRuns()
        refresh(today)
        syncOutputs()
        return true
    }

    /**
     * Apply the chosen scope to a calendar frequency change, saving the
     * template and occurrence history in the same local transaction.
     */
    internal fun applyCalendarFrequencyEdit(
        edited: TaskTemplate,
        scope: CalendarEditScope,
        today: LocalDate = LocalDate.now(),
    ): Boolean {
        require(profiles.active.kind == NavProfileKind.VESSEL) { "Personal profile is notes-only" }
        val original = templates.firstOrNull { it.id == edited.id } ?: return false
        val change = editCalendarFrequencyScoped(original, edited, runs, scope, today) ?: return false
        templates = templates.map { if (it.id == edited.id) change.template else it }
        runs = change.runs
        saveTemplates(templates)
        persistRuns()
        refresh(today)
        syncOutputs()
        return true
    }

    private fun materializeSingleDay(day: LocalDate) {
        val updated = runs.toMutableMap()
        for (task in templates.filter { it.scheduleMode == ScheduleMode.CALENDAR }) {
            if (TaskSchedule.isCalendarDue(task, day)) {
                updated.putIfAbsent(key(task.id, day), TaskRun(task.id, day, task.checklist))
            }
        }
        for (task in templates.filter { it.enabled && it.scheduleMode == ScheduleMode.AFTER_COMPLETION }) {
            val latest = updated.values.filter { it.taskId == task.id && it.completed }
                .maxByOrNull { it.completedAt.orEmpty() }
            val completedOn = latest?.completedAt?.let {
                runCatching { Instant.parse(it).atZone(ZoneId.systemDefault()).toLocalDate() }.getOrNull()
            }
            if (task.frequency != Frequency.EXTRA && TaskSchedule.nextAfterCompletion(task, completedOn) == day) {
                updated.putIfAbsent(key(task.id, day), TaskRun(task.id, day, task.checklist))
            }
        }
        if (updated.size != runs.size) { runs = updated; persistRuns() }
    }

    fun upsert(task: TaskTemplate) {
        require(profiles.active.kind == NavProfileKind.VESSEL) { "Personal profile is notes-only" }
        templates = if (templates.any { it.id == task.id }) {
            templates.map { if (it.id == task.id) task else it }
        } else templates + task
        saveTemplates(templates)
        runs = runs.filterValues {
            it.taskId != task.id || it.completed ||
                !it.dueDate.isAfter(LocalDate.now()) ||
                it.skipped || it.transferred || it.frequencyOverride != null ||
                it.checkedIds.isNotEmpty()
        }
        persistRuns()
        refresh()
        syncOutputs()
    }

    /** Add only new templates, preserving local tasks and all completion history. */
    fun importTemplates(imported: List<TaskTemplate>): Int {
        require(profiles.active.kind == NavProfileKind.VESSEL) { "Personal profile is notes-only" }
        val additions = NavTaskTransfer.newTemplates(templates, imported)
        if (additions.isEmpty()) return 0
        templates = templates + additions
        saveTemplates(templates)
        refresh()
        syncOutputs()
        return additions.size
    }

    fun cloneTask(task: TaskTemplate) {
        upsert(task.copy(
            id = UUID.randomUUID().toString(),
            title = "${task.title} (копия)",
            checklist = task.checklist.map { it.copy(id = UUID.randomUUID().toString()) },
        ))
    }

    private fun replaceRun(run: TaskRun) {
        runs = runs + (run.key to run)
        persistRuns()
    }

    private fun syncOutputs() {
        NavWidgetUpdater.updateAll(appContext, this)
        NavNotifications.reschedule(appContext, this, NavSettings(appContext))
    }

    private fun persistRuns() {
        val json = JSONArray().apply { runs.values.forEach { put(it.toJson()) } }
        prefs.edit().putString("runs", json.toString()).apply()
    }

    private fun saveTemplates(value: List<TaskTemplate>) {
        prefs.edit().putString("templates", JSONArray().apply {
            value.forEach { put(it.toJson()) }
        }.toString()).apply()
    }

    private fun decodeTemplates(raw: String): List<TaskTemplate> = runCatching {
        val items = JSONArray(raw)
        (0 until items.length()).mapNotNull {
            items.optJSONObject(it)?.let(TaskTemplate::fromJson)
        }
    }.getOrDefault(emptyList())

    private fun decodeRuns(raw: String): Map<String, TaskRun> = runCatching {
        val items = JSONArray(raw)
        (0 until items.length()).mapNotNull {
            items.optJSONObject(it)?.let { json -> runCatching { TaskRun.fromJson(json) }.getOrNull() }
        }.associateBy { it.key }
    }.getOrDefault(emptyMap())

    companion object {
        fun key(taskId: String, day: LocalDate) = "${taskId}@${day}"

        private fun sampleTemplates(): List<TaskTemplate> {
            val today = LocalDate.now()
            fun daily(name: String, list: List<String> = emptyList()) = TaskTemplate(
                title = name, frequency = Frequency.DAILY, startDate = today,
                checklist = list.map { CheckItem(title = it) },
            )
            fun weekly(name: String, day: DayOfWeek, list: List<String> = emptyList()) = TaskTemplate(
                title = name, frequency = Frequency.WEEKLY, dayOfWeek = day.value,
                startDate = today, checklist = list.map { CheckItem(title = it) },
                missedPolicy = MissedPolicy.CARRY,
            )
            fun monthly(name: String) = TaskTemplate(
                title = name, frequency = Frequency.MONTHLY, dayOfMonth = today.dayOfMonth,
                startDate = today, missedPolicy = MissedPolicy.CARRY,
            )
            return listOf(
                daily("Compass Error"),
                daily("Sea Log"),
                daily("Daily ENL Report"),
                daily("Noon"),
                daily("GMDSS Records", listOf("Noon PSN", "Daily Test", "Master Sign")),
                weekly("Сводная корректура", DayOfWeek.WEDNESDAY),
                weekly("F-06 folder", DayOfWeek.FRIDAY),
                weekly("GMDSS Weekly", DayOfWeek.FRIDAY,
                    listOf("NAVAREA / NAVTEX", "F-59 A01 + AIS", "Air bubbles / Compass Error", "Gyro changed 1/2")),
                monthly("SART"),
                monthly("EPIRB"),
                monthly("Check ECDIS License"),
            )
        }
    }
}
