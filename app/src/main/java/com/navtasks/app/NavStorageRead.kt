package com.navtasks.app

import org.json.JSONArray
import java.time.LocalDate

/**
 * Strict decoding of persisted user data. On damage it is safer to lock
 * writes than silently reinterpret history as an empty JSON array.
 * Optional fields from older valid versions remain supported.
 */
internal object NavStorageRead {
    fun templates(raw: String): List<TaskTemplate> {
        val arr = JSONArray(raw)
        require(arr.length() <= 2500) { "Too many task templates" }
        val items = (0 until arr.length()).map { index ->
            val obj = arr.getJSONObject(index)
            require(obj.optString("id").isNotBlank() && obj.optString("title").isNotBlank()) {
                "Task ID or title is missing"
            }
            if (obj.has("startDate")) LocalDate.parse(obj.getString("startDate"))
            TaskTemplate.fromJson(obj)
        }
        require(items.map { it.id }.toSet().size == items.size) { "Duplicate task IDs" }
        return items
    }

    fun runs(raw: String): Map<String, TaskRun> {
        val arr = JSONArray(raw)
        require(arr.length() <= 100_000) { "Too many task history entries" }
        val items = (0 until arr.length()).map { TaskRun.fromJson(arr.getJSONObject(it)) }
        require(items.map { it.key }.toSet().size == items.size) {
            "Duplicate dated task keys"
        }
        return items.associateBy { it.key }
    }

    fun sections(raw: String): List<HandoverSection> {
        val arr = JSONArray(raw)
        require(arr.length() <= 250) { "Too many Handover sections" }
        var totalNotes = 0
        val result = (0 until arr.length()).map { index ->
            val obj = arr.getJSONObject(index)
            require(obj.optString("title").isNotBlank()) { "Untitled Handover section" }
            val entries = if (obj.has("entries")) obj.getJSONArray("entries") else JSONArray()
            totalNotes += entries.length()
            require(totalNotes <= 25_000) { "Too many Handover entries" }
            for (i in 0 until entries.length()) {
                val item = entries.getJSONObject(i)
                require(item.optString("title").isNotBlank()) { "Untitled Handover entry" }
            }
            HandoverSection.fromJson(obj)
        }
        require(result.map { it.id }.toSet().size == result.size) {
            "Duplicate Handover section IDs"
        }
        return result
    }
}

internal fun ensureNavStorageWritable(issue: String?) {
    check(issue == null) {
        "Данные профиля не прочитаны. Запись заблокирована до восстановления полного профиля."
    }
}
