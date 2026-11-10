package com.navtasks.app

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.util.UUID

enum class HandoverMark { OPEN, COMPLETED, STRUCK }

data class HandoverEntry(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val notes: String = "",
    val notesHtml: String = "",
    val mark: HandoverMark = HandoverMark.OPEN,
    val completedDate: LocalDate? = null,
) {
    fun toJson() = JSONObject()
        .put("id", id).put("title", title).put("notes", notes)
        .put("notesHtml", notesHtml)
        .put("mark", mark.name).put("completedDate", completedDate?.toString() ?: "")

    companion object {
        fun fromJson(json: JSONObject): HandoverEntry {
            val mark = HandoverMark.entries.firstOrNull { it.name == json.optString("mark") }
                ?: HandoverMark.OPEN
            return HandoverEntry(
                id = json.optString("id").takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString(),
                title = json.optString("title").trim().take(200),
                notes = json.optString("notes").take(4000),
                notesHtml = json.optString("notesHtml", "").take(24000),
                mark = mark,
                completedDate = runCatching { LocalDate.parse(json.optString("completedDate")) }.getOrNull(),
            )
        }
    }
}

data class HandoverSection(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val entries: List<HandoverEntry> = emptyList(),
) {
    fun toJson() = JSONObject()
        .put("id", id).put("title", title)
        .put("entries", JSONArray().apply { entries.forEach { put(it.toJson()) } })

    companion object {
        fun fromJson(json: JSONObject): HandoverSection {
            val arr = json.optJSONArray("entries") ?: JSONArray()
            return HandoverSection(
                id = json.optString("id").takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString(),
                title = json.optString("title").trim().take(120),
                entries = (0 until arr.length()).mapNotNull {
                    arr.optJSONObject(it)?.let(HandoverEntry::fromJson)
                }.filter { it.title.isNotBlank() },
            )
        }
    }
}

/** The handover notebook is independent of task occurrences, reminders and settings. */
class HandoverStore(context: Context, private val profiles: ProfileStore = ProfileStore(context)) {
    private var prefs = profiles.data(profiles.activeId)

    var storageIssue: String? by mutableStateOf(null)
        private set

    var sections: List<HandoverSection> by mutableStateOf(load())
        private set

    init {
        // Persist empty built-in notebook sections too, so the complete profile
        // export contains the structure even before the first entry is written.
        if (!prefs.contains("sections")) commit(sections)
    }

    fun switchProfile() {
        prefs = profiles.data(profiles.activeId)
        sections = load()
        if (!prefs.contains("sections")) commit(sections)
    }

    private fun load(): List<HandoverSection> {
        if (!prefs.contains("sections")) {
            storageIssue = null
            return if (profiles.active.kind == NavProfileKind.PERSONAL) emptyList()
                else listOf(HandoverSection(title = "Navigation"), HandoverSection(title = "Medicines"))
        }
        val decoded = runCatching {
            NavStorageRead.sections(prefs.getString("sections", "[]") ?: "[]")
        }
        storageIssue = if (decoded.isFailure)
            "Не удалось прочитать Handover этого профиля. Запись заблокирована. Восстановите полный профиль из копии."
        else null
        return decoded.getOrDefault(emptyList())
    }

    private fun commit(value: List<HandoverSection>) {
        ensureNavStorageWritable(storageIssue)
        val json = JSONArray().apply { value.forEach { put(it.toJson()) } }.toString()
        check(prefs.edit().putString("sections", json).commit()) {
            "Не удалось сохранить Handover"
        }
        sections = value
    }

    fun saveSection(title: String, existingId: String? = null) {
        val clean = title.trim().take(120)
        if (clean.isBlank()) return
        if (existingId == null) commit(sections + HandoverSection(title = clean))
        else commit(sections.map { if (it.id == existingId) it.copy(title = clean) else it })
    }

    fun deleteSection(id: String) = commit(sections.filterNot { it.id == id })

    fun saveEntry(sectionId: String, title: String, notes: String, existingId: String? = null, notesHtml: String = "") {
        val clean = title.trim().take(200)
        if (clean.isBlank()) return
        commit(sections.map { section ->
            if (section.id != sectionId) section else {
                val entry = section.entries.firstOrNull { it.id == existingId }
                val updated = (entry ?: HandoverEntry(title = clean)).copy(
                    title = clean, notes = notes.trim().take(4000),
                    notesHtml = notesHtml.take(24000),
                )
                section.copy(entries = if (entry == null) section.entries + updated
                    else section.entries.map { if (it.id == existingId) updated else it })
            }
        })
    }

    fun setMark(sectionId: String, entryId: String, mark: HandoverMark, date: LocalDate? = null) {
        commit(sections.map { section ->
            if (section.id != sectionId) section else section.copy(entries = section.entries.map {
                if (it.id == entryId) it.copy(
                    mark = mark,
                    completedDate = if (mark == HandoverMark.COMPLETED) date else null,
                ) else it
            })
        })
    }

    fun deleteEntry(sectionId: String, entryId: String) {
        commit(sections.map { section ->
            if (section.id == sectionId) section.copy(entries = section.entries.filterNot { it.id == entryId })
            else section
        })
    }

    /** Merge incoming notebook without replacing any local edits or completion dates. */
    fun mergeSections(incoming: List<HandoverSection>): Pair<Int, Int> {
        val merged = sections.toMutableList()
        var addedSections = 0
        var addedEntries = 0
        incoming.forEach { imported ->
            if (imported.title.isBlank()) return@forEach
            val index = merged.indexOfFirst {
                it.id == imported.id || it.title.equals(imported.title, ignoreCase = true)
            }
            if (index < 0) {
                merged += imported.copy(id = UUID.randomUUID().toString(), entries = imported.entries.map {
                    it.copy(id = UUID.randomUUID().toString())
                })
                addedSections++
                addedEntries += imported.entries.size
            } else {
                val local = merged[index]
                val additions = imported.entries.filter { remote ->
                    local.entries.none { item ->
                        item.id == remote.id || (item.title.equals(remote.title, ignoreCase = true) &&
                            item.notes == remote.notes && item.notesHtml == remote.notesHtml)
                    }
                }.map { it.copy(id = UUID.randomUUID().toString()) }
                merged[index] = local.copy(entries = local.entries + additions)
                addedEntries += additions.size
            }
        }
        if (addedSections > 0 || addedEntries > 0) commit(merged)
        return addedSections to addedEntries
    }
}

data class HandoverPackage(val tasks: List<TaskTemplate>, val sections: List<HandoverSection>)

/** Shareable shipboard transfer containing task templates and handover notes, not private history. */
object HandoverTransfer {
    private const val FORMAT = "navtasks-handover-v1"

    fun export(tasks: List<TaskTemplate>, sections: List<HandoverSection>): String = JSONObject()
        .put("format", FORMAT).put("version", 1).put("created", LocalDate.now().toString())
        .put("tasks", JSONArray().apply { tasks.forEach { put(it.toJson()) } })
        .put("sections", JSONArray().apply { sections.forEach { put(it.toJson()) } })
        .toString(2)

    fun parse(raw: String): HandoverPackage {
        require(raw.length <= 2_000_000) { "File exceeds 2 MB" }
        val root = JSONObject(raw)
        require(root.optString("format") == FORMAT && root.optInt("version") == 1) {
            "Unsupported Handover file format"
        }
        val tasks = root.optJSONArray("tasks") ?: throw IllegalArgumentException("Tasks missing")
        val sections = root.optJSONArray("sections") ?: throw IllegalArgumentException("Sections missing")
        require(tasks.length() <= 1500 && sections.length() <= 150) { "File contains too many items" }
        val decodedSections = (0 until sections.length()).mapNotNull {
            sections.optJSONObject(it)?.let(HandoverSection::fromJson)
        }.filter { it.title.isNotBlank() }
        require(decodedSections.sumOf { it.entries.size } <= 10_000) { "Too many Handover entries" }
        return HandoverPackage(
            tasks = (0 until tasks.length()).mapNotNull { tasks.optJSONObject(it)?.let(TaskTemplate::fromJson) }
                .filter { it.title.isNotBlank() },
            sections = decodedSections,
        )
    }
}
