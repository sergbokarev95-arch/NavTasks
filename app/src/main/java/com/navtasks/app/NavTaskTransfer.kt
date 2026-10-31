package com.navtasks.app

import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDate
import java.util.Locale
import java.util.UUID

/** Shareable portable file: task definitions only. Never transfers completion history or settings. */
internal object NavTaskTransfer {
    private const val FORMAT = "navtasks-template-bundle"
    private const val VERSION = 1
    private const val MAX_TEMPLATES = 1000
    private const val MAX_CHECKS = 200

    fun export(templates: List<TaskTemplate>): String = JSONObject()
        .put("format", FORMAT)
        .put("schemaVersion", VERSION)
        .put("exportedAt", Instant.now().toString())
        .put("tasks", JSONArray().apply { templates.forEach { put(it.toJson()) } })
        .toString(2)

    fun parse(raw: String): List<TaskTemplate> {
        require(raw.length <= 2_000_000) { "File is too large (2 MB limit)" }
        val root = JSONObject(raw)
        require(root.optString("format") == FORMAT) { "Not a NavTasks task file" }
        require(root.optInt("schemaVersion", -1) == VERSION) { "Unsupported NavTasks file version" }
        val tasks = root.optJSONArray("tasks") ?: error("Task list is missing")
        require(tasks.length() <= MAX_TEMPLATES) { "Too many tasks" }
        return (0 until tasks.length()).map { index ->
            val json = tasks.getJSONObject(index)
            val title = json.optString("title").trim()
            require(title.isNotBlank() && title.length <= 200) { "Invalid task name" }
            require(Frequency.entries.any { it.name == json.optString("frequency") }) { "Invalid task category" }
            require(runCatching { LocalDate.parse(json.getString("startDate")) }.isSuccess) { "Invalid task date" }
            val checklist = json.optJSONArray("checklist") ?: JSONArray()
            require(checklist.length() <= MAX_CHECKS) { "Checklist is too long" }
            for (i in 0 until checklist.length()) {
                val text = checklist.getJSONObject(i).optString("title")
                require(text.isNotBlank() && text.length <= 300) { "Invalid checklist item" }
            }
            TaskTemplate.fromJson(json).copy(title = title)
        }
    }

    private fun identity(task: TaskTemplate): String =
        "${task.frequency.name}|${task.title.trim().lowercase(Locale.ROOT)}"

    /** Safe append-only import. Same title + category is treated as already present. */
    fun countNew(existing: List<TaskTemplate>, imported: List<TaskTemplate>): Int {
        val known = existing.map(::identity).toMutableSet()
        return imported.count { known.add(identity(it)) }
    }

    fun newTemplates(existing: List<TaskTemplate>, imported: List<TaskTemplate>): List<TaskTemplate> {
        val known = existing.map(::identity).toMutableSet()
        return imported.filter { known.add(identity(it)) }.map { template ->
            template.copy(
                id = UUID.randomUUID().toString(),
                checklist = template.checklist.map { it.copy(id = UUID.randomUUID().toString()) },
            )
        }
    }
}
