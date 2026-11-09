package com.navtasks.app

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.util.UUID

/** Immutable dated snapshot of a completed equipment service. */
data class ServiceCompletion(
    val scheduledDate: LocalDate?,
    val completedAt: LocalDate,
    val requestNumber: String = "",
    val equipment: String = "",
    val comment: String = "",
    val commentHtml: String = "",
    val intervalMonths: Int = 0,
) {
    fun toJson(): JSONObject = JSONObject()
        .put("scheduledDate", scheduledDate?.toString() ?: JSONObject.NULL)
        .put("completedAt", completedAt.toString())
        .put("requestNumber", requestNumber)
        .put("equipment", equipment)
        .put("comment", comment)
        .put("commentHtml", commentHtml)
        .put("intervalMonths", intervalMonths)

    companion object {
        fun fromJson(json: JSONObject): ServiceCompletion = ServiceCompletion(
            scheduledDate = json.optString("scheduledDate", "").takeUnless {
                it.isBlank() || it == "null"
            }?.let(LocalDate::parse),
            completedAt = LocalDate.parse(json.getString("completedAt")),
            requestNumber = json.optString("requestNumber", ""),
            equipment = json.optString("equipment", ""),
            comment = json.optString("comment", ""),
            commentHtml = json.optString("commentHtml", ""),
            intervalMonths = json.optInt("intervalMonths", 0),
        )
    }
}

/**
 * Small, entirely local maintenance record. Each completed service remains
 * archived with the requisition number submitted for that specific cycle.
 */
data class ServiceEntry(
    val id: String = UUID.randomUUID().toString(),
    val equipment: String,
    val lastDate: LocalDate,
    val nextDate: LocalDate?,
    val comment: String = "",
    val commentHtml: String = "",
    val periodMonths: Int = 0,
    val remindMonths: Int = 0,
    /** AMOS requisition created for this service; empty means not requested. */
    val requestNumber: String = "",
    val serviceHistory: List<ServiceCompletion> = emptyList(),
    /** One-off services have no next cycle after completion. */
    val completedAt: LocalDate? = null,
    /** Removed from active equipment without erasing completed cycles. */
    val closedAt: LocalDate? = null,
    /** Unplanned work has an AMOS request, not a fabricated next-due date. */
    val unplanned: Boolean = false,
) {
    init {
        require(id.isNotBlank() && id.length <= 100)
        require(equipment.isNotBlank() && equipment.length <= 200)
        require(comment.length <= 6000 && commentHtml.length <= 24000)
        if (unplanned) {
            require(nextDate == null) { "Unplanned work cannot have a scheduled due date" }
            require(periodMonths == 0 && remindMonths == 0) {
                "Unplanned requests cannot create automatic service cycles or reminders"
            }
        } else {
            require(nextDate != null && nextDate.isAfter(lastDate)) {
                "Next service date must follow the last date"
            }
        }
        require(periodMonths in 0..1200) { "Service interval must be 0–1200 months" }
        require(remindMonths in 0..3) { "Lead time exceeds three months" }
        require(requestNumber.length <= 100) { "Request number too long" }
        require(serviceHistory.size <= 500) { "Too many completed services" }
        require(serviceHistory.all {
            it.requestNumber.length <= 100 && it.equipment.length <= 200 &&
                it.comment.length <= 6000 && it.intervalMonths in 0..1200
        })
    }

    fun toJson(): JSONObject = JSONObject()
        .put("id", id)
        .put("equipment", equipment)
        .put("lastDate", lastDate.toString())
        .put("nextDate", nextDate?.toString() ?: JSONObject.NULL)
        .put("unplanned", unplanned)
        .put("comment", comment).put("commentHtml", commentHtml)
        .put("periodMonths", periodMonths)
        .put("remindMonths", remindMonths)
        .put("requestNumber", requestNumber)
        .put("completedAt", completedAt?.toString() ?: JSONObject.NULL)
        .put("closedAt", closedAt?.toString() ?: JSONObject.NULL)
        .put("serviceHistory", JSONArray().apply {
            serviceHistory.forEach { put(it.toJson()) }
        })

    companion object {
        fun fromJson(json: JSONObject): ServiceEntry = ServiceEntry(
            id = json.getString("id"),
            equipment = json.getString("equipment"),
            lastDate = LocalDate.parse(json.getString("lastDate")),
            nextDate = json.optString("nextDate", "").takeUnless {
                it.isBlank() || it == "null"
            }?.let(LocalDate::parse),
            comment = json.optString("comment", ""),
            commentHtml = json.optString("commentHtml", ""),
            periodMonths = json.optInt("periodMonths", 0),
            remindMonths = json.optInt("remindMonths", 0),
            requestNumber = json.optString("requestNumber", ""),
            serviceHistory = (json.optJSONArray("serviceHistory") ?: JSONArray()).let { items ->
                require(items.length() <= 500)
                (0 until items.length()).map {
                    ServiceCompletion.fromJson(items.getJSONObject(it))
                }
            },
            completedAt = json.optString("completedAt", "").takeUnless {
                it.isBlank() || it == "null"
            }?.let(LocalDate::parse),
            closedAt = json.optString("closedAt", "").takeUnless {
                it.isBlank() || it == "null"
            }?.let(LocalDate::parse),
            // Missing in every pre-0.4.50 profile/export means planned.
            unplanned = json.optBoolean("unplanned", false),
        )
    }
}

/**
 * Planned maintenance: compute the next due date from the ACTUAL last service,
 * never from an unrelated default date or an invented yesterday value.
 * Keep manually selected due dates in ServiceEntry unchanged until edited.
 */
internal fun calculatedNextServiceDate(lastServiceDate: LocalDate, periodMonths: Int): LocalDate {
    require(periodMonths in 1..1200) { "A recurring service needs a valid interval" }
    return lastServiceDate.plusMonths(periodMonths.toLong())
}

internal fun serviceWarningStart(entry: ServiceEntry): LocalDate {
    require(!entry.unplanned) { "Unplanned requests have no due-date warnings" }
    return requireNotNull(entry.nextDate).minusMonths(entry.remindMonths.toLong())
}

/**
 * Complete an equipment service. The AMOS request belongs to the archived
 * execution and is cleared only after successful saving of the new cycle.
 */
internal fun completeServiceCycle(entry: ServiceEntry, on: LocalDate): ServiceEntry {
    require(entry.completedAt == null) { "One-off service is already completed" }
    require(!entry.unplanned || entry.requestNumber.isNotBlank()) {
        "An unplanned service requires its AMOS request number before completion"
    }
    require(entry.serviceHistory.size < 500) { "Service history limit reached" }
    val snapshot = ServiceCompletion(
        scheduledDate = if (entry.unplanned) null else entry.nextDate,
        completedAt = on,
        requestNumber = entry.requestNumber,
        equipment = entry.equipment,
        comment = entry.comment,
        commentHtml = entry.commentHtml,
        intervalMonths = entry.periodMonths,
    )
    val history = entry.serviceHistory + snapshot
    return if (entry.periodMonths > 0) {
        entry.copy(
            lastDate = on,
            nextDate = on.plusMonths(entry.periodMonths.toLong()),
            completedAt = null,
            requestNumber = "",
            serviceHistory = history,
        )
    } else {
        // A one-off has no next due date. Preserve its original scheduled date
        // for history, but exclude the completed record from active reminders.
        entry.copy(
            completedAt = on,
            requestNumber = "",
            serviceHistory = history,
        )
    }
}

internal fun nextServiceFromCompletion(entry: ServiceEntry, completedAt: LocalDate): ServiceEntry {
    require(!entry.unplanned && entry.periodMonths > 0) {
        "A planned recurrence interval is required to auto-calculate next date"
    }
    return completeServiceCycle(entry, completedAt)
}

/** Writing services is atomic within the same profile preference file. */
class ServiceStore(context: Context, private val profiles: ProfileStore) {
    private var prefs = profiles.data(profiles.activeId)
    var entries: List<ServiceEntry> by mutableStateOf(load())
        private set

    private fun load(): List<ServiceEntry> {
        val array = JSONArray(prefs.getString("services", "[]"))
        require(array.length() <= 1000) { "Too many service records" }
        return (0 until array.length()).map { ServiceEntry.fromJson(array.getJSONObject(it)) }
    }

    fun switchProfile() {
        prefs = profiles.data(profiles.activeId)
        entries = load()
    }

    private fun write(next: List<ServiceEntry>) {
        require(profiles.active.kind == NavProfileKind.VESSEL) { "Personal profile is notes-only" }
        require(next.size <= 1000) { "Too many service records" }
        require(next.map { it.id }.toSet().size == next.size)
        val json = JSONArray().apply { next.forEach { put(it.toJson()) } }.toString()
        check(prefs.edit().putString("services", json).commit()) { "Could not save services" }
        entries = next
    }

    fun upsert(item: ServiceEntry) {
        val current = entries.indexOfFirst { it.id == item.id }
        write(if (current >= 0) entries.toMutableList().apply { set(current, item) }
            else entries + item)
    }

    fun setRequestNumber(id: String, number: String) {
        val item = entries.firstOrNull { it.id == id } ?: error("Service not found")
        val normalized = number.trim()
        if (item.unplanned && item.completedAt == null) {
            require(normalized.isNotBlank()) {
                "Unplanned maintenance requires an AMOS request number"
            }
        }
        upsert(item.copy(requestNumber = normalized))
    }

    /** First delete only closes this record; history is retained. */
    fun delete(id: String) {
        val entry = entries.firstOrNull { it.id == id } ?: error("Service not found")
        require(entry.closedAt == null) { "Service already closed" }
        upsert(entry.copy(closedAt = LocalDate.now()))
    }

    fun restore(id: String) {
        val entry = entries.firstOrNull { it.id == id } ?: error("Service not found")
        require(entry.closedAt != null) { "Service is not closed" }
        upsert(entry.copy(closedAt = null))
    }

    fun deletePermanently(id: String) {
        require(entries.any { it.id == id && it.closedAt != null }) {
            "Only closed services can be permanently deleted"
        }
        write(entries.filterNot { it.id == id })
    }

    fun complete(id: String, completedAt: LocalDate) {
        val existing = entries.firstOrNull { it.id == id } ?: error("Service not found")
        upsert(completeServiceCycle(existing, completedAt))
    }
}
