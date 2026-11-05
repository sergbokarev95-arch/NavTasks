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
) {
    init {
        require(id.isNotBlank() && id.length <= 100)
        require(reference.isNotBlank() && reference.length <= 80)
        require(title.isNotBlank() && title.length <= 200)
        require(comment.length <= 6000)
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

internal fun workOrderComplete(order: WorkOrderEntry, day: LocalDate): WorkOrderEntry {
    require(!order.completed) { "Work order already completed" }
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
