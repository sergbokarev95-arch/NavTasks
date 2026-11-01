package com.navtasks.app

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.util.UUID

enum class NavProfileKind { PERSONAL, VESSEL }

data class NavProfile(
    val id: String,
    val title: String,
    val kind: NavProfileKind,
) {
    fun toJson() = JSONObject()
        .put("id", id).put("title", title).put("kind", kind.name)

    companion object {
        fun fromJson(json: JSONObject): NavProfile {
            val id = json.getString("id").trim()
            require(id.matches(Regex("[a-zA-Z0-9_-]{1,80}"))) { "Invalid profile ID" }
            val name = json.getString("title").trim()
            require(name.isNotBlank() && name.length <= 100) { "Invalid profile name" }
            val kind = NavProfileKind.valueOf(json.getString("kind"))
            return NavProfile(id, name, kind)
        }
    }
}

data class ProfileSnapshot(
    val profile: NavProfile,
    val templates: List<TaskTemplate>,
    val runs: List<TaskRun>,
    val sections: List<HandoverSection>,
    val lastVisit: String,
    val services: List<ServiceEntry> = emptyList(),
)

object ProfileTransfer {
    const val FORMAT = "navtasks-full-profile-v1"
    const val MAX_BYTES = 10_000_000

    fun export(snapshot: ProfileSnapshot): String = JSONObject()
        .put("format", FORMAT)
        .put("schemaVersion", 1)
        .put("profile", snapshot.profile.toJson())
        .put("tasks", JSONArray().apply { snapshot.templates.forEach { put(it.toJson()) } })
        .put("history", JSONArray().apply { snapshot.runs.forEach { put(it.toJson()) } })
        .put("handover", JSONArray().apply { snapshot.sections.forEach { put(it.toJson()) } })
        .put("services", JSONArray().apply { snapshot.services.forEach { put(it.toJson()) } })
        .put("lastVisit", snapshot.lastVisit)
        .toString(2)

    fun parse(raw: String): ProfileSnapshot {
        require(raw.length in 1..MAX_BYTES) { "Profile file exceeds 10 MB" }
        val data = JSONObject(raw)
        require(data.optString("format") == FORMAT && data.optInt("schemaVersion") == 1) {
            "Unsupported profile format"
        }
        val profile = NavProfile.fromJson(data.getJSONObject("profile"))
        val ts = data.getJSONArray("tasks")
        val hs = data.getJSONArray("history")
        val ss = data.getJSONArray("handover")
        val ms = data.optJSONArray("services") ?: JSONArray()
        require(ts.length() <= 2500 && hs.length() <= 100_000 && ss.length() <= 250 && ms.length() <= 1000) {
            "Profile has too many records"
        }
        val templates = (0 until ts.length()).map {
            TaskTemplate.fromJson(ts.getJSONObject(it))
        }
        val runs = (0 until hs.length()).map { TaskRun.fromJson(hs.getJSONObject(it)) }
        val sections = (0 until ss.length()).map {
            HandoverSection.fromJson(ss.getJSONObject(it))
        }
        val services = (0 until ms.length()).map { ServiceEntry.fromJson(ms.getJSONObject(it)) }
        require(services.map { it.id }.toSet().size == services.size) { "Duplicate services" }
        require(sections.sumOf { it.entries.size } <= 25000) { "Too many notes" }
        require(templates.map { it.id }.toSet().size == templates.size) { "Duplicate tasks" }
        require(runs.map { it.key }.toSet().size == runs.size) { "Duplicate history entries" }
        val known = templates.map { it.id }.toSet()
        require(runs.all { it.taskId in known }) { "History has missing task references" }
        if (profile.kind == NavProfileKind.PERSONAL) {
            require(templates.isEmpty() && runs.isEmpty() && services.isEmpty()) {
                "Personal profile must contain notes only"
            }
        }
        val lastVisit = data.optString("lastVisit", LocalDate.now().toString())
        require(runCatching { LocalDate.parse(lastVisit) }.isSuccess) { "Invalid last visit date" }
        return ProfileSnapshot(profile, templates, runs, sections, lastVisit, services)
    }
}

/**
 * Profile index is separate from individual profile files.
 * v0.3.0 data is copied, not removed, into the default ship profile exactly once.
 */
class ProfileStore(context: Context) {
    private val app = context.applicationContext
    private val index = app.getSharedPreferences("navtasks-profiles-v1", Context.MODE_PRIVATE)

    companion object {
        const val DEFAULT_SHIP = "ship-default"
        const val PERSONAL_ID = "personal"
    }

    var profiles: List<NavProfile> by mutableStateOf(emptyList())
        private set
    var activeId: String by mutableStateOf(DEFAULT_SHIP)
        private set

    val active: NavProfile get() = profiles.first { it.id == activeId }
    fun data(id: String): SharedPreferences {
        require(profiles.any { it.id == id }) { "Unknown profile" }
        return app.getSharedPreferences("navtasks-profile-" + id, Context.MODE_PRIVATE)
    }

    init {
        if (!index.getBoolean("initialized", false)) {
            val original = app.getSharedPreferences("navtasks-v1", Context.MODE_PRIVATE)
            val notes = app.getSharedPreferences("navtasks-handover-v1", Context.MODE_PRIVATE)
            val shipData = app.getSharedPreferences("navtasks-profile-" + DEFAULT_SHIP, Context.MODE_PRIVATE)
            // Copy everything in the legacy task store including lastVisit. Keep legacy untouched.
            val copy = shipData.edit()
            if (!shipData.contains("templates") && original.contains("templates"))
                copy.putString("templates", original.getString("templates", "[]"))
            if (!shipData.contains("runs") && original.contains("runs"))
                copy.putString("runs", original.getString("runs", "[]"))
            if (!shipData.contains("lastVisit") && original.contains("lastVisit"))
                copy.putString("lastVisit", original.getString("lastVisit", LocalDate.now().toString()))
            if (!shipData.contains("sections") && notes.contains("sections"))
                copy.putString("sections", notes.getString("sections", "[]"))
            check(copy.commit()) { "Unable to migrate existing NavTasks data" }
            val initial = listOf(
                NavProfile(DEFAULT_SHIP, "Основной", NavProfileKind.VESSEL),
                NavProfile(PERSONAL_ID, "Личное", NavProfileKind.PERSONAL),
            )
            check(index.edit()
                .putString("profiles", JSONArray().apply { initial.forEach { put(it.toJson()) } }.toString())
                .putString("activeId", DEFAULT_SHIP).putBoolean("initialized", true)
                .commit()) { "Unable to initialize profiles" }
        }
        val list = runCatching {
            val arr = JSONArray(index.getString("profiles", "[]"))
            (0 until arr.length()).map { NavProfile.fromJson(arr.getJSONObject(it)) }
        }.getOrDefault(emptyList())
        profiles = if (list.isEmpty()) listOf(
            NavProfile(DEFAULT_SHIP, "Основной", NavProfileKind.VESSEL),
            NavProfile(PERSONAL_ID, "Личное", NavProfileKind.PERSONAL),
        ) else list
        activeId = index.getString("activeId", DEFAULT_SHIP)
            .takeIf { value -> profiles.any { it.id == value } } ?: profiles.first().id
    }

    private fun saveIndex(value: List<NavProfile> = profiles, active: String = activeId) {
        val encoded = JSONArray().apply { value.forEach { put(it.toJson()) } }.toString()
        check(index.edit().putString("profiles", encoded).putString("activeId", active).commit()) {
            "Could not save profiles"
        }
        profiles = value
        activeId = active
    }

    fun select(id: String) {
        require(profiles.any { it.id == id }) { "Unknown profile" }
        saveIndex(active = id)
    }

    fun createVessel(name: String): NavProfile {
        val title = name.trim().take(100)
        require(title.isNotBlank()) { "Enter a profile name" }
        require(profiles.none { it.kind == NavProfileKind.VESSEL && it.title.equals(title, true) }) {
            "A profile with this name already exists"
        }
        val created = NavProfile(UUID.randomUUID().toString(), title, NavProfileKind.VESSEL)
        saveIndex(profiles + created, created.id)
        return created
    }

    fun rename(id: String, name: String) {
        val title = name.trim().take(100)
        require(title.isNotBlank())
        require(profiles.none { it.id != id && it.title.equals(title, ignoreCase = true) && it.kind == profiles.first { p -> p.id == id }.kind }) {
            "This profile name already exists"
        }
        saveIndex(profiles.map { if (it.id == id) it.copy(title = title) else it })
    }

    fun delete(id: String) {
        require(id != DEFAULT_SHIP && id != PERSONAL_ID) { "Built-in profiles cannot be deleted" }
        require(profiles.any { it.id == id })
        val next = profiles.filterNot { it.id == id }
        saveIndex(next, if (activeId == id) DEFAULT_SHIP else activeId)
        app.getSharedPreferences("navtasks-profile-" + id, Context.MODE_PRIVATE).edit().clear().commit()
    }

    fun findMatch(incoming: NavProfile): NavProfile? =
        profiles.firstOrNull { it.id == incoming.id && it.kind == incoming.kind }
            ?: profiles.firstOrNull {
                it.kind == incoming.kind && it.title.equals(incoming.title, ignoreCase = true)
            }

    fun snapshot(id: String): ProfileSnapshot {
        val profile = profiles.first { it.id == id }
        val prefs = data(id)
        val tasks = JSONArray(prefs.getString("templates", "[]"))
        val runs = JSONArray(prefs.getString("runs", "[]"))
        val notes = JSONArray(prefs.getString("sections", "[]"))
        val services = JSONArray(prefs.getString("services", "[]"))
        return ProfileSnapshot(
            profile,
            (0 until tasks.length()).map { TaskTemplate.fromJson(tasks.getJSONObject(it)) },
            (0 until runs.length()).map { TaskRun.fromJson(runs.getJSONObject(it)) },
            (0 until notes.length()).map { HandoverSection.fromJson(notes.getJSONObject(it)) },
            prefs.getString("lastVisit", LocalDate.now().toString()) ?: LocalDate.now().toString(),
            (0 until services.length()).map { ServiceEntry.fromJson(services.getJSONObject(it)) },
        )
    }

    /** Replace ALL tasks, history, notes and services as one preference transaction. */
    fun importReplace(incoming: ProfileSnapshot): NavProfile {
        val matched = findMatch(incoming.profile)
        val target = matched ?: incoming.profile.copy(
            id = incoming.profile.id.takeIf { id -> profiles.none { it.id == id } }
                ?: UUID.randomUUID().toString()
        )
        val prefs = app.getSharedPreferences("navtasks-profile-" + target.id, Context.MODE_PRIVATE)
        val editor = prefs.edit()
            .putString("templates", JSONArray().apply { incoming.templates.forEach { put(it.toJson()) } }.toString())
            .putString("runs", JSONArray().apply { incoming.runs.forEach { put(it.toJson()) } }.toString())
            .putString("sections", JSONArray().apply { incoming.sections.forEach { put(it.toJson()) } }.toString())
            .putString("services", JSONArray().apply { incoming.services.forEach { put(it.toJson()) } }.toString())
            .putString("lastVisit", incoming.lastVisit)
        // Do not claim success or change the index if the full profile write failed.
        check(editor.commit()) { "Profile import could not be saved" }
        if (matched == null) saveIndex(profiles + target, target.id)
        else saveIndex(profiles.map {
            if (it.id == target.id) it.copy(title = incoming.profile.title) else it
        }, target.id)
        return target
    }
}
