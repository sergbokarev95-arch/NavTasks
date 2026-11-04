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

/**
 * A future calendar rule begins on effectiveFrom but never schedules a new
 * occurrence before firstDate. Earlier completed dates retain the old rule.
 */
data class CalendarScheduleRevision(
    val effectiveFrom: LocalDate,
    val firstDate: LocalDate,
    val dayOfWeek: Int,
    val dayOfMonth: Int,
    /** Null for historical revisions written before frequency-change support. */
    val frequency: Frequency? = null,
) {
    fun toJson(): JSONObject = JSONObject()
        .put("effectiveFrom", effectiveFrom.toString())
        .put("firstDate", firstDate.toString())
        .put("dayOfWeek", dayOfWeek)
        .put("dayOfMonth", dayOfMonth)
        .put("frequency", frequency?.name ?: "")

    companion object {
        fun fromJson(json: JSONObject): CalendarScheduleRevision? = runCatching {
            CalendarScheduleRevision(
                effectiveFrom = LocalDate.parse(json.getString("effectiveFrom")),
                firstDate = LocalDate.parse(json.getString("firstDate")),
                dayOfWeek = json.optInt("dayOfWeek", 1).coerceIn(1, 7),
                dayOfMonth = json.optInt("dayOfMonth", 1).coerceIn(1, 31),
                frequency = Frequency.entries.firstOrNull {
                    it.name == json.optString("frequency")
                },
            )
        }.getOrNull()
    }
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
    val calendarRevisions: List<CalendarScheduleRevision> = emptyList(),
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
        .put("calendarRevisions", JSONArray().apply {
            calendarRevisions.forEach { put(it.toJson()) }
        })

    companion object {
        inline fun <reified T : Enum<T>> enumOrDefault(value: String, fallback: T): T =
            enumValues<T>().firstOrNull { it.name == value } ?: fallback

        fun fromJson(json: JSONObject): TaskTemplate {
            val list = json.optJSONArray("checklist") ?: JSONArray()
            val revisions = json.optJSONArray("calendarRevisions") ?: JSONArray()
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
                calendarRevisions = (0 until revisions.length())
                    .mapNotNull { revisions.optJSONObject(it)?.let(CalendarScheduleRevision::fromJson) }
                    .sortedBy { it.effectiveFrom },
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
    /** A one-time category change does not alter the recurring template. */
    val frequencyOverride: Frequency? = null,
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
        .put("frequencyOverride", frequencyOverride?.name ?: "")

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
                frequencyOverride = Frequency.entries.firstOrNull {
                    it.name == json.optString("frequencyOverride")
                },
            )
        }
    }
}

/** Pure calendar calculations. Local dates use the phone's system time zone. */
object TaskSchedule {
    private fun activeRevision(task: TaskTemplate, date: LocalDate): CalendarScheduleRevision? =
        task.calendarRevisions.filter { !it.effectiveFrom.isAfter(date) }
            .maxByOrNull { it.effectiveFrom }

    /** Historical dates use the original frequency; later dates use dated revisions. */
    fun effectiveFrequency(task: TaskTemplate, date: LocalDate): Frequency =
        activeRevision(task, date)?.frequency ?: task.frequency

    fun occurrenceFrequency(task: TaskTemplate, run: TaskRun): Frequency =
        run.frequencyOverride ?: effectiveFrequency(task, run.displayDate)

    fun isCalendarDue(task: TaskTemplate, date: LocalDate): Boolean {
        if (!task.enabled || date.isBefore(task.startDate)) return false
        val revision = activeRevision(task, date)
        if (revision != null && date.isBefore(revision.firstDate)) return false
        val first = revision?.firstDate ?: task.startDate
        if (date.isBefore(first)) return false
        val weekday = revision?.dayOfWeek ?: task.dayOfWeek
        val monthDay = revision?.dayOfMonth ?: task.dayOfMonth
        return when (effectiveFrequency(task, date)) {
            Frequency.DAILY -> true
            Frequency.WEEKLY -> date.dayOfWeek.value == weekday
            Frequency.MONTHLY -> date.dayOfMonth == minOf(monthDay, date.lengthOfMonth())
            Frequency.EXTRA -> date == task.startDate
        }
    }

    private fun nextOnRule(
        task: TaskTemplate,
        start: LocalDate,
        rule: CalendarScheduleRevision?,
    ): LocalDate? {
        val first = rule?.firstDate ?: task.startDate
        val cursor = maxOf(start, first)
        val weekday = rule?.dayOfWeek ?: task.dayOfWeek
        val monthDay = rule?.dayOfMonth ?: task.dayOfMonth
        val frequency = rule?.frequency ?: task.frequency
        return when (frequency) {
            Frequency.DAILY -> cursor
            Frequency.WEEKLY -> cursor.with(TemporalAdjusters.nextOrSame(DayOfWeek.of(weekday)))
            Frequency.MONTHLY -> {
                val ym = YearMonth.from(cursor)
                val candidate = ym.atDay(minOf(monthDay, ym.lengthOfMonth()))
                if (!candidate.isBefore(cursor)) candidate else {
                    val next = ym.plusMonths(1)
                    next.atDay(minOf(monthDay, next.lengthOfMonth()))
                }
            }
            Frequency.EXTRA -> task.startDate.takeUnless { it.isBefore(start) }
        }
    }

    fun nextCalendarDate(task: TaskTemplate, from: LocalDate): LocalDate? {
        // Walk the finite schedule segments instead of scanning individual days.
        val revisions = task.calendarRevisions.sortedBy { it.effectiveFrom }
        var cursor = maxOf(from, task.startDate)
        for (segment in 0..revisions.size) {
            val rule = activeRevision(task, cursor)
            val candidate = nextOnRule(task, cursor, rule)
            val boundary = revisions.firstOrNull { it.effectiveFrom.isAfter(cursor) }?.effectiveFrom
            if (candidate != null && (boundary == null || candidate.isBefore(boundary))) {
                return candidate
            }
            if (boundary == null) return null
            cursor = boundary
        }
        return null
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
