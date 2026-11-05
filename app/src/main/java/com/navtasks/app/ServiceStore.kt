package com.navtasks.app

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.util.UUID

/**
 * Small, entirely local maintenance record. No document blobs, certificates or
 * picture attachments. One vessel profile keeps its own records in preferences.
 */
data class ServiceEntry(
    val id: String = UUID.randomUUID().toString(),
    val equipment: String,
    val lastDate: LocalDate,
    val nextDate: LocalDate,
    val comment: String = "",
    val periodMonths: Int = 0,
    val remindMonths: Int = 0,
    /** AMOS requisition created for this service; empty means not requested. */
    val requestNumber: String = "",
) {
    init {
        require(id.isNotBlank() && id.length <= 100)
        require(equipment.isNotBlank() && equipment.length <= 200)
        require(comment.length <= 6000)
        require(nextDate.isAfter(lastDate)) { "Next service date must follow the last date" }
        require(periodMonths in 0..1200) { "Service interval must be 0–1200 months" }
        require(remindMonths in 0..3) { "Lead time exceeds three months" }
        require(requestNumber.length <= 100) { "Request number too long" }
    }

    fun toJson(): JSONObject = JSONObject()
        .put("id", id)
        .put("equipment", equipment)
        .put("lastDate", lastDate.toString())
        .put("nextDate", nextDate.toString())
        .put("comment", comment)
        .put("periodMonths", periodMonths)
        .put("remindMonths", remindMonths)
        .put("requestNumber", requestNumber)

    companion object {
        fun fromJson(json: JSONObject): ServiceEntry = ServiceEntry(
            id = json.getString("id"),
            equipment = json.getString("equipment"),
            lastDate = LocalDate.parse(json.getString("lastDate")),
            nextDate = LocalDate.parse(json.getString("nextDate")),
            comment = json.optString("comment", ""),
            periodMonths = json.optInt("periodMonths", 0),
            remindMonths = json.optInt("remindMonths", 0),
            requestNumber = json.optString("requestNumber", ""),
        )
    }
}

internal fun serviceWarningStart(entry: ServiceEntry): LocalDate =
    entry.nextDate.minusMonths(entry.remindMonths.toLong())

internal fun nextServiceFromCompletion(entry: ServiceEntry, completedAt: LocalDate): ServiceEntry {
    require(entry.periodMonths > 0) { "A recurrence interval is required to auto-calculate next date" }
    return entry.copy(
        lastDate = completedAt,
        nextDate = completedAt.plusMonths(entry.periodMonths.toLong()),
    )
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
        upsert(item.copy(requestNumber = number.trim()))
    }

    fun delete(id: String) = write(entries.filterNot { it.id == id })

    fun complete(id: String, completedAt: LocalDate) {
        val existing = entries.firstOrNull { it.id == id } ?: error("Service not found")
        upsert(nextServiceFromCompletion(existing, completedAt))
    }
}
