package com.navtasks.app

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.util.UUID

/** Manually entered AMOS reference, not a live connection to the vessel AMOS system. */
data class WorkOrderEntry(
    val id: String = UUID.randomUUID().toString(),
    val reference: String,
    val title: String,
    val dueDate: LocalDate,
    val createdAt: LocalDate = LocalDate.now(),
    val completedAt: LocalDate? = null,
    val comment: String = "",
    val checklist: List<CheckItem> = emptyList(),
    val checkedItemIds: Set<String> = emptySet(),
    val remindDaysBefore: Int = 0,
) {
    init {
        require(id.isNotBlank() && id.length <= 100)
        require(reference.isNotBlank() && reference.length <= 80)
        require(title.isNotBlank() && title.length <= 200)
        require(comment.length <= 6000)
        require(remindDaysBefore in 0..90)
        require(checklist.size <= 80)
        require(checklist.map { it.id }.toSet().size == checklist.size)
        require(checklist.all { it.id.isNotBlank() && it.title.isNotBlank() &&
            it.title.length <= 200 && it.comment.length <= 6000 })
        require(checkedItemIds.all { id -> checklist.any { it.id == id } })
    }

    val completed: Boolean get() = completedAt != null

    fun toJson(): JSONObject = JSONObject()
        .put("id", id)
        .put("reference", reference)
        .put("title", title)
        .put("dueDate", dueDate.toString())
        .put("createdAt", createdAt.toString())
        .put("completedAt", completedAt?.toString() ?: JSONObject.NULL)
        .put("comment", comment)
        .put("checklist", JSONArray().apply { checklist.forEach { put(it.toJson()) } })
        .put("checkedItemIds", JSONArray().apply { checkedItemIds.forEach { put(it) } })
        .put("remindDaysBefore", remindDaysBefore)

    companion object {
        fun fromJson(json: JSONObject): WorkOrderEntry {
            val rawCompleted = json.optString("completedAt", "")
            return WorkOrderEntry(
                id = json.getString("id"),
                reference = json.getString("reference"),
                title = json.getString("title"),
                dueDate = LocalDate.parse(json.getString("dueDate")),
                createdAt = LocalDate.parse(json.getString("createdAt")),
                completedAt = rawCompleted.takeUnless { it.isBlank() || it == "null" }
                    ?.let(LocalDate::parse),
                comment = json.optString("comment", ""),
                checklist = (json.optJSONArray("checklist") ?: JSONArray()).let { a ->
                    (0 until a.length()).map { CheckItem.fromJson(a.getJSONObject(it)) }
                },
                checkedItemIds = (json.optJSONArray("checkedItemIds") ?: JSONArray()).let { a ->
                    (0 until a.length()).map { a.getString(it) }.toSet()
                },
                remindDaysBefore = json.optInt("remindDaysBefore", 0),
            )
        }
    }
}

enum class WorkOrderState { OPEN, SOON, OVERDUE, COMPLETED }

internal fun workOrderState(order: WorkOrderEntry, today: LocalDate): WorkOrderState = when {
    order.completed -> WorkOrderState.COMPLETED
    order.dueDate.isBefore(today) -> WorkOrderState.OVERDUE
    !order.dueDate.isAfter(today.plusDays(7)) -> WorkOrderState.SOON
    else -> WorkOrderState.OPEN
}

internal fun workOrderMandatoryProgress(order: WorkOrderEntry): Pair<Int, Int> {
    val required = order.checklist.filter { it.required }
    return required.count { it.id in order.checkedItemIds } to required.size
}

internal fun workOrderCanComplete(order: WorkOrderEntry): Boolean {
    val (done, required) = workOrderMandatoryProgress(order)
    return done == required
}

internal fun workOrderCheck(order: WorkOrderEntry, checkId: String, checked: Boolean): WorkOrderEntry {
    require(!order.completed) { "Completed work orders are archived" }
    require(order.checklist.any { it.id == checkId }) { "Unknown checklist item" }
    return order.copy(checkedItemIds = if (checked) order.checkedItemIds + checkId
        else order.checkedItemIds - checkId)
}

internal fun workOrderWarningStart(order: WorkOrderEntry): LocalDate =
    order.dueDate.minusDays(order.remindDaysBefore.toLong())

internal fun workOrderComplete(order: WorkOrderEntry, day: LocalDate): WorkOrderEntry {
    require(!order.completed) { "Work order already completed" }
    require(workOrderCanComplete(order)) { "Required AMOS checklist items are not complete" }
    return order.copy(completedAt = day)
}

internal fun workOrderReopen(order: WorkOrderEntry): WorkOrderEntry {
    require(order.completed) { "Work order is already open" }
    return order.copy(completedAt = null)
}

internal fun validateWorkOrders(items: List<WorkOrderEntry>) {
    require(items.size <= 1500) { "Too many AMOS work orders" }
    require(items.map { it.id }.toSet().size == items.size) { "Duplicate work order IDs" }
    require(items.map { it.reference.trim().uppercase() }.toSet().size == items.size) {
        "Duplicate AMOS work order number"
    }
}

/** Saved independently per vessel profile; never modifies Tasks or ServiceStore. */
class WorkOrderStore(context: Context, private val profiles: ProfileStore) {
    private var prefs = profiles.data(profiles.activeId)

    var entries: List<WorkOrderEntry> by mutableStateOf(load())
        private set

    private fun load(): List<WorkOrderEntry> {
        val raw = JSONArray(prefs.getString("workOrders", "[]"))
        require(raw.length() <= 1500) { "Too many AMOS work orders" }
        val decoded = (0 until raw.length()).map { WorkOrderEntry.fromJson(raw.getJSONObject(it)) }
        validateWorkOrders(decoded)
        return decoded
    }

    fun switchProfile() {
        val nextPrefs = profiles.data(profiles.activeId)
        // Decode the newly selected profile before switching away from current data.
        val raw = JSONArray(nextPrefs.getString("workOrders", "[]"))
        require(raw.length() <= 1500)
        val decoded = (0 until raw.length()).map { WorkOrderEntry.fromJson(raw.getJSONObject(it)) }
        validateWorkOrders(decoded)
        prefs = nextPrefs
        entries = decoded
    }

    private fun write(next: List<WorkOrderEntry>) {
        require(profiles.active.kind == NavProfileKind.VESSEL) { "Personal profile is notes-only" }
        validateWorkOrders(next)
        val json = JSONArray().apply { next.forEach { put(it.toJson()) } }.toString()
        check(prefs.edit().putString("workOrders", json).commit()) {
            "Could not save AMOS work orders"
        }
        entries = next
    }

    fun upsert(entry: WorkOrderEntry) {
        val index = entries.indexOfFirst { it.id == entry.id }
        write(if (index < 0) entries + entry
            else entries.toMutableList().apply { set(index, entry) })
    }

    fun setCheck(id: String, checkId: String, checked: Boolean) {
        val entry = entries.firstOrNull { it.id == id } ?: error("Work order not found")
        upsert(workOrderCheck(entry, checkId, checked))
    }

    fun complete(id: String, on: LocalDate) {
        val entry = entries.firstOrNull { it.id == id } ?: error("Work order not found")
        upsert(workOrderComplete(entry, on))
    }

    fun reopen(id: String) {
        val entry = entries.firstOrNull { it.id == id } ?: error("Work order not found")
        upsert(workOrderReopen(entry))
    }

    fun delete(id: String) {
        require(entries.any { it.id == id }) { "Work order not found" }
        write(entries.filterNot { it.id == id })
    }
}
