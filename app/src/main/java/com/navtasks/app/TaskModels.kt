package com.navtasks.app

import org.json.JSONArray
import org.json.JSONObject
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters
import java.util.UUID

enum class CalendarDayState { EMPTY, SCHEDULED, INCOMPLETE, PARTIAL, COMPLETE }

data class DayCounts(val total: Int, val done: Int) {
    fun state(date: LocalDate, today: LocalDate): CalendarDayState = when {
        total == 0 -> CalendarDayState.EMPTY
        done == total -> CalendarDayState.COMPLETE
        done > 0 -> CalendarDayState.PARTIAL
        date.isBefore(today) -> CalendarDayState.INCOMPLETE
        else -> CalendarDayState.SCHEDULED
    }
}

enum class Frequency { DAILY, WEEKLY, MONTHLY, EXTRA }
enum class ScheduleMode { CALENDAR, AFTER_COMPLETION }
/** HISTORY = overdue backlog; CARRY = automatically move the current occurrence. */
enum class MissedPolicy { HISTORY, CARRY, ASK, ARCHIVE }
enum class CompletionMode { AUTOMATIC, MANUAL }

data class CheckItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val required: Boolean = true,
    /** User-authored journal wording or reference, not completion notes. */
    val comment: String = "",
) {
    fun toJson() = JSONObject()
        .put("id", id).put("title", title).put("required", required)
        .put("comment", comment)

    companion object {
        fun fromJson(json: JSONObject) = CheckItem(
            id = json.optString("id", UUID.randomUUID().toString()),
            title = json.optString("title"),
            required = json.optBoolean("required", true),
            comment = json.optString("comment", ""),
        )
    }
}

/**
 * Reusable journal note for a checklist row.
 *
 * Today's already-materialized TaskRun can contain the previous checklist snapshot.
 * If an item was recreated while editing a task, its stable ID may have changed.
 * Prefer exact ID; otherwise match only a UNIQUE name on both sides. Never
 * blindly use the row index, since this could attach the wrong journal wording
 * after a reordered checklist. Completed runs retain their archived wording.
 */
internal fun checklistReferenceComment(
    task: TaskTemplate, run: TaskRun, item: CheckItem,
): String {
    if (run.completed || run.skipped) return item.comment

    task.checklist.firstOrNull { it.id == item.id }?.let { return it.comment }

    val name = item.title.trim()
    if (name.isEmpty()) return item.comment
    val sameInRun = run.checklist.count { it.title.trim().equals(name, ignoreCase = true) }
    if (sameInRun != 1) return item.comment
    val sameInTemplate = task.checklist.filter { it.title.trim().equals(name, ignoreCase = true) }
    return if (sameInTemplate.size == 1) sameInTemplate.single().comment else item.comment
}

data class TaskTemplate(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val frequency: Frequency,
    val scheduleMode: ScheduleMode = ScheduleMode.CALENDAR,
    val startDate: LocalDate = LocalDate.now(),
    val dayOfWeek: Int = DayOfWeek.FRIDAY.value,
    val dayOfMonth: Int = 1,
    val intervalCount: Int = 1,
    val missedPolicy: MissedPolicy = MissedPolicy.HISTORY,
    val completionMode: CompletionMode = CompletionMode.AUTOMATIC,
    val deadlineTime: LocalTime? = null,
    val notificationsEnabled: Boolean = true,
    val remindMinutesBefore: Int = 60,
    val checklist: List<CheckItem> = emptyList(),
    val enabled: Boolean = true,
) {
    fun toJson() = JSONObject()
        .put("id", id)
        .put("title", title)
        .put("frequency", frequency.name)
        .put("scheduleMode", scheduleMode.name)
        .put("startDate", startDate.toString())
        .put("dayOfWeek", dayOfWeek)
        .put("dayOfMonth", dayOfMonth)
        .put("intervalCount", intervalCount)
        .put("missedPolicy", missedPolicy.name)
        .put("completionMode", completionMode.name)
        .put("deadlineTime", deadlineTime?.toString() ?: "")
        .put("notificationsEnabled", notificationsEnabled)
        .put("remindMinutesBefore", remindMinutesBefore)
        .put("enabled", enabled)
        .put("checklist", JSONArray().apply { checklist.forEach { put(it.toJson()) } })

    companion object {
        inline fun <reified T : Enum<T>> enumOrDefault(value: String, fallback: T): T =
            enumValues<T>().firstOrNull { it.name == value } ?: fallback

        fun fromJson(json: JSONObject): TaskTemplate {
            val list = json.optJSONArray("checklist") ?: JSONArray()
            return TaskTemplate(
                id = json.optString("id", UUID.randomUUID().toString()),
                title = json.optString("title"),
                frequency = enumOrDefault(json.optString("frequency"), Frequency.DAILY),
                scheduleMode = enumOrDefault(json.optString("scheduleMode"), ScheduleMode.CALENDAR),
                startDate = runCatching { LocalDate.parse(json.optString("startDate")) }.getOrDefault(LocalDate.now()),
                dayOfWeek = json.optInt("dayOfWeek", 5).coerceIn(1, 7),
                dayOfMonth = json.optInt("dayOfMonth", 1).coerceIn(1, 31),
                intervalCount = json.optInt("intervalCount", 1).coerceAtLeast(1),
                missedPolicy = enumOrDefault(json.optString("missedPolicy"), MissedPolicy.HISTORY),
                completionMode = enumOrDefault(json.optString("completionMode"), CompletionMode.AUTOMATIC),
                deadlineTime = runCatching { LocalTime.parse(json.optString("deadlineTime")) }.getOrNull(),
                notificationsEnabled = json.optBoolean("notificationsEnabled", true),
                remindMinutesBefore = json.optInt("remindMinutesBefore", 60).coerceAtLeast(0),
                enabled = json.optBoolean("enabled", true),
                checklist = (0 until list.length()).mapNotNull { list.optJSONObject(it)?.let(CheckItem::fromJson) },
            )
        }
    }
}

data class TaskRun(
    val taskId: String,
    val dueDate: LocalDate,
    // Keep a snapshot of checklist items, so editing a template never rewrites history.
    val checklist: List<CheckItem>,
    val checkedIds: Set<String> = emptySet(),
    val completed: Boolean = false,
    val completedAt: String? = null,
    val skipped: Boolean = false,
    /** The scheduled occurrence key stays unchanged when this run is postponed. */
    val postponedTo: LocalDate? = null,
    val autoTransferred: Boolean = false,
    val closedReason: String? = null,
) {
    val key: String get() = "${taskId}@${dueDate}"
    val displayDate: LocalDate get() = postponedTo ?: dueDate
    val transferred: Boolean get() = postponedTo != null
    val checkedCount: Int get() = checklist.count { it.id in checkedIds }
    val requiredComplete: Boolean get() =
        checklist.isNotEmpty() && checklist.all { it.id in checkedIds }
    fun toJson() = JSONObject()
        .put("taskId", taskId).put("dueDate", dueDate.toString())
        .put("checklist", JSONArray().apply { checklist.forEach { put(it.toJson()) } })
        .put("checkedIds", JSONArray().apply { checkedIds.forEach { put(it) } })
        .put("completed", completed).put("completedAt", completedAt)
        .put("skipped", skipped)
        .put("postponedTo", postponedTo?.toString() ?: "")
        .put("autoTransferred", autoTransferred)
        .put("closedReason", closedReason ?: "")

    companion object {
        fun fromJson(json: JSONObject): TaskRun {
            val items = json.optJSONArray("checklist") ?: JSONArray()
            val checked = json.optJSONArray("checkedIds") ?: JSONArray()
            return TaskRun(
                taskId = json.getString("taskId"),
                dueDate = LocalDate.parse(json.getString("dueDate")),
                checklist = (0 until items.length()).mapNotNull { items.optJSONObject(it)?.let(CheckItem::fromJson) },
                checkedIds = (0 until checked.length()).mapNotNull { checked.optString(it).takeIf(String::isNotEmpty) }.toSet(),
                completed = json.optBoolean("completed"),
                completedAt = json.optString("completedAt").takeIf { it.isNotEmpty() },
                skipped = json.optBoolean("skipped"),
                postponedTo = runCatching { LocalDate.parse(json.optString("postponedTo")) }.getOrNull(),
                autoTransferred = json.optBoolean("autoTransferred", false),
                closedReason = json.optString("closedReason").takeIf { it.isNotBlank() },
            )
        }
    }
}

/** Pure calendar calculations. Local dates use the phone's system time zone. */
object TaskSchedule {
    fun isCalendarDue(task: TaskTemplate, date: LocalDate): Boolean {
        if (!task.enabled || date.isBefore(task.startDate)) return false
        return when (task.frequency) {
            Frequency.DAILY -> true
            Frequency.WEEKLY -> date.dayOfWeek.value == task.dayOfWeek
            Frequency.MONTHLY -> date.dayOfMonth == minOf(task.dayOfMonth, date.lengthOfMonth())
            Frequency.EXTRA -> date == task.startDate
        }
    }

    fun nextCalendarDate(task: TaskTemplate, from: LocalDate): LocalDate? {
        val date = maxOf(from, task.startDate)
        return when (task.frequency) {
            Frequency.DAILY -> date
            Frequency.WEEKLY -> date.with(TemporalAdjusters.nextOrSame(DayOfWeek.of(task.dayOfWeek)))
            Frequency.MONTHLY -> {
                val thisMonth = YearMonth.from(date)
                val candidate = thisMonth.atDay(minOf(task.dayOfMonth, thisMonth.lengthOfMonth()))
                if (!candidate.isBefore(date)) candidate else {
                    val next = thisMonth.plusMonths(1)
                    next.atDay(minOf(task.dayOfMonth, next.lengthOfMonth()))
                }
            }
            Frequency.EXTRA -> task.startDate.takeUnless { it.isBefore(from) }
        }
    }

    fun nextAfterCompletion(task: TaskTemplate, completedOn: LocalDate?): LocalDate {
        val base = completedOn ?: task.startDate
        if (completedOn == null) return task.startDate
        val count = task.intervalCount.toLong().coerceAtLeast(1)
        return when (task.frequency) {
            Frequency.DAILY -> base.plusDays(count)
            Frequency.WEEKLY -> base.plusWeeks(count)
            Frequency.MONTHLY -> base.plusMonths(count)
            Frequency.EXTRA -> base
        }
    }
}
