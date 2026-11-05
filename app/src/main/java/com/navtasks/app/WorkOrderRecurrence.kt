package com.navtasks.app

import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.util.UUID

/** Immutable execution journal entry. Checklist snapshots survive later template edits. */
data class WorkOrderCompletion(
    val id: String = UUID.randomUUID().toString(),
    val dueDate: LocalDate,
    val completedAt: LocalDate,
    val checklist: List<CheckItem>,
    val checkedItemIds: Set<String>,
    /** Anchor used before this completion, needed when undoing a shifted cycle. */
    val scheduleAnchorBefore: LocalDate? = null,
) {
    init {
        require(id.isNotBlank())
        require(checklist.size <= 80)
        require(checkedItemIds.all { id -> checklist.any { it.id == id } })
    }

    fun toJson(): JSONObject = JSONObject()
        .put("id", id)
        .put("dueDate", dueDate.toString())
        .put("completedAt", completedAt.toString())
        .put("checklist", JSONArray().apply { checklist.forEach { put(it.toJson()) } })
        .put("checkedItemIds", JSONArray().apply {
            checkedItemIds.sorted().forEach { put(it) }
        })
        .put("scheduleAnchorBefore", scheduleAnchorBefore?.toString() ?: JSONObject.NULL)

    companion object {
        fun fromJson(json: JSONObject): WorkOrderCompletion {
            val list = json.optJSONArray("checklist") ?: JSONArray()
            val checked = json.optJSONArray("checkedItemIds") ?: JSONArray()
            require(list.length() <= 80)
            return WorkOrderCompletion(
                id = json.optString("id").takeUnless { it.isBlank() }
                    ?: UUID.randomUUID().toString(),
                dueDate = LocalDate.parse(json.getString("dueDate")),
                completedAt = LocalDate.parse(json.getString("completedAt")),
                checklist = (0 until list.length()).map {
                    CheckItem.fromJson(list.getJSONObject(it))
                },
                checkedItemIds = (0 until checked.length()).map { checked.getString(it) }.toSet(),
                scheduleAnchorBefore = json.optString("scheduleAnchorBefore", "").takeUnless {
                    it.isBlank() || it == "null"
                }?.let(LocalDate::parse),
            )
        }
    }
}

internal fun workOrderNextDue(entry: WorkOrderEntry, completedAt: LocalDate): LocalDate {
    require(entry.recurring) { "No recurrence interval" }
    // Fixed schedule: the repeat runs from the chosen start date, never from
    // the day the crew happened to mark the Work Order completed.
    val anchor = entry.startDate ?: entry.dueDate
    for (index in 0L..6000L) {
        // Always project from the original anchor: Jan 31 -> Feb 28 -> Mar 31.
        val due = if (entry.repeatMonths > 0)
            anchor.plusMonths(entry.repeatMonths.toLong() * index)
        else anchor.plusDays(entry.repeatDays.toLong() * index)
        if (due.isAfter(completedAt) && due.isAfter(entry.dueDate)) return due
    }
    error("AMOS recurrence exceeds supported range")
}

/** Complete one cycle without changing older dated records or checklist references. */
/**
 * Shift=true resets the repeating day/month to the selected completion date.
 * Otherwise the original schedule anchor is preserved. Either way the dated
 * occurrence is archived at its originally scheduled date.
 */
internal fun workOrderFinishCycle(
    order: WorkOrderEntry,
    completedAt: LocalDate,
    shiftFuture: Boolean = false,
): WorkOrderEntry {
    require(!order.completed)
    if (!order.recurring) return order.copy(completedAt = completedAt)
    require(order.executionHistory.size < 300) { "AMOS history limit reached" }
    val oldAnchor = order.startDate ?: order.dueDate
    val planned = if (shiftFuture) order.copy(
        startDate = completedAt, dueDate = completedAt,
    ) else order
    val nextDue = workOrderNextDue(planned, completedAt)
    val snapshot = WorkOrderCompletion(
        dueDate = order.dueDate,
        completedAt = completedAt,
        checklist = order.checklist,
        checkedItemIds = order.checkedItemIds,
        scheduleAnchorBefore = oldAnchor,
    )
    return order.copy(
        dueDate = nextDue,
        startDate = if (shiftFuture) completedAt else order.startDate,
        completedAt = null,
        checkedItemIds = emptySet(),
        executionHistory = order.executionHistory + snapshot,
    )
}

/**
 * Undo ONLY the most recently completed cycle. Future checklist progress must
 * not be silently destroyed if the newly generated cycle has already started.
 */
internal fun workOrderUndoCycle(order: WorkOrderEntry): WorkOrderEntry {
    if (order.completed) return order.copy(completedAt = null)
    require(order.recurring && order.executionHistory.isNotEmpty()) {
        "No completed AMOS cycle to reopen"
    }
    require(order.checkedItemIds.isEmpty()) {
        "Undo current checklist progress before reopening the previous cycle"
    }
    val last = order.executionHistory.last()
    return order.copy(
        dueDate = last.dueDate,
        startDate = last.scheduleAnchorBefore ?: order.startDate,
        checkedItemIds = last.checkedItemIds.intersect(order.checklist.map { it.id }.toSet()),
        executionHistory = order.executionHistory.dropLast(1),
    )
}

/** One-off archived orders and repeated executions share the same history UI. */
internal data class WorkOrderHistoryRecord(
    val order: WorkOrderEntry,
    val completion: WorkOrderCompletion,
    val canUndo: Boolean,
)

internal fun workOrderHistoryRecords(orders: List<WorkOrderEntry>): List<WorkOrderHistoryRecord> =
    orders.flatMap { order ->
        val cycles = order.executionHistory.mapIndexed { i, completion ->
            WorkOrderHistoryRecord(
                order, completion,
                canUndo = i == order.executionHistory.lastIndex && !order.completed,
            )
        }
        val once = order.completedAt?.let { done ->
            listOf(WorkOrderHistoryRecord(
                order = order,
                completion = WorkOrderCompletion(
                    dueDate = order.dueDate, completedAt = done,
                    checklist = order.checklist, checkedItemIds = order.checkedItemIds,
                ),
                canUndo = true,
            ))
        }.orEmpty()
        cycles + once
    }.sortedWith(compareByDescending<WorkOrderHistoryRecord> {
        it.completion.completedAt
    }.thenBy { it.order.reference })
