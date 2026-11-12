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
    val workOrders: List<WorkOrderEntry> = emptyList(),
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
        .put("workOrders", JSONArray().apply { snapshot.workOrders.forEach { put(it.toJson()) } })
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
        // Old profile files omit workOrders. Treat those as an empty AMOS register.
        val ws = data.optJSONArray("workOrders") ?: JSONArray()
        require(ts.length() <= 2500 && hs.length() <= 100_000 && ss.length() <= 250 && ms.length() <= 1000 && ws.length() <= 1500) {
            "Profile has too many records"
        }
        val templates = NavStorageRead.templates(ts.toString())
        val runs = NavStorageRead.runs(hs.toString()).values.toList()
        val sections = NavStorageRead.sections(ss.toString())
        val services = (0 until ms.length()).map { ServiceEntry.fromJson(ms.getJSONObject(it)) }
        val workOrders = (0 until ws.length()).map { WorkOrderEntry.fromJson(ws.getJSONObject(it)) }
        validateWorkOrders(workOrders)
        require(services.map { it.id }.toSet().size == services.size) { "Duplicate services" }
        require(sections.sumOf { it.entries.size } <= 25000) { "Too many notes" }
        require(templates.map { it.id }.toSet().size == templates.size) { "Duplicate tasks" }
        require(runs.map { it.key }.toSet().size == runs.size) { "Duplicate history entries" }
        val known = templates.map { it.id }.toSet()
        require(runs.all { it.taskId in known }) { "History has missing task references" }
        if (profile.kind == NavProfileKind.PERSONAL) {
            require(templates.isEmpty() && runs.isEmpty() && services.isEmpty() && workOrders.isEmpty()) {
                "Personal profile must contain notes only"
            }
        }
        val lastVisit = data.optString("lastVisit", LocalDate.now().toString())
        require(runCatching { LocalDate.parse(lastVisit) }.isSuccess) { "Invalid last visit date" }
        return ProfileSnapshot(profile, templates, runs, sections, lastVisit, services, workOrders)
    }
}


/**
 * The profile index is only an address book. Individual task/handover/AMOS/Services
 * data lives in different preferences and must NEVER be cleared on parse failures.
 */
internal data class NavProfileIndexResult(
    val items: List<NavProfile>,
    val raw: String,
    val fromBackup: Boolean,
)

internal fun navValidateProfileIndex(raw: String?): List<NavProfile> {
    require(!raw.isNullOrBlank() && raw.length <= 512_000) { "Missing or oversized profile index" }
    val arr = JSONArray(raw)
    require(arr.length() in 1..1000) { "Empty or oversized profile index" }
    val result = (0 until arr.length()).map { NavProfile.fromJson(arr.getJSONObject(it)) }
    require(result.map { it.id }.toSet().size == result.size) { "Duplicate profile IDs" }
    return result
}

internal fun navChooseProfileIndex(primary: String?, backup: String?): NavProfileIndexResult {
    val current = runCatching { navValidateProfileIndex(primary) }.getOrNull()
    if (current != null) return NavProfileIndexResult(current, primary!!, false)
    val rescued = try {
        navValidateProfileIndex(backup)
    } catch (error: Exception) {
        // Never silently substitute default profiles or write an empty index.
        throw IllegalStateException(
            "Profile index is damaged; no verified backup. Existing profile data was not erased.",
            error,
        )
    }
    return NavProfileIndexResult(rescued, backup!!, true)
}

/**
 * Choose a valid remaining profile, preferring another vessel and finally
 * Personal. This works even if the original default ship was deleted.
 */
internal fun profileAfterDeletion(
    existing: List<NavProfile>,
    deletedId: String,
    currentActiveId: String,
): String {
    val remaining = existing.filterNot { it.id == deletedId }
    require(remaining.isNotEmpty()) { "At least one profile must remain" }
    return currentActiveId.takeIf { id -> remaining.any { it.id == id } }
        ?: remaining.firstOrNull { it.kind == NavProfileKind.VESSEL }?.id
        ?: remaining.first().id
}

/**
 * Profile index is separate from individual profile files.
 * v0.3.0 data is copied, not removed, into the default ship profile exactly once.
 */
class ProfileStore(context: Context) {
    private val app = context.applicationContext
    private val index = app.getSharedPreferences("navtasks-profiles-v1", Context.MODE_PRIVATE)
    // Independent address-book safety copy, not a duplicate of personal task data.
    private val safeIndex = app.getSharedPreferences("navtasks-profiles-safety-v1", Context.MODE_PRIVATE)

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
        // Recover even if Android lost the index's initialized flag: never replace
        // existing profile IDs with initial defaults when a valid index exists.
        val present = runCatching { index.getString("profiles", null) }.getOrNull()
        val safety = runCatching { safeIndex.getString("profiles", null) }.getOrNull()
        if (!index.getBoolean("initialized", false) && present == null && safety == null) {
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
        val verified = navChooseProfileIndex(
            runCatching { index.getString("profiles", null) }.getOrNull(),
            runCatching { safeIndex.getString("profiles", null) }.getOrNull(),
        )
        profiles = verified.items
        activeId = runCatching { index.getString("activeId", DEFAULT_SHIP) }.getOrNull()
            .takeIf { value -> profiles.any { it.id == value } }
            ?: runCatching { safeIndex.getString("activeId", DEFAULT_SHIP) }.getOrNull()
                .takeIf { value -> profiles.any { it.id == value } }
            ?: profiles.first().id
        if (verified.fromBackup || !index.getBoolean("initialized", false)) {
            // Only a validated previously saved list may repair the primary index.
            check(index.edit().putString("profiles", verified.raw)
                .putString("activeId", activeId).putBoolean("initialized", true)
                .commit()) { "Cannot recover validated profile index" }
        }
        // First launch on an existing installation seeds the independent copy.
        // Backing up only the validated IDs never touches actual profile records.
        safeIndex.edit().putString("profiles", verified.raw)
            .putString("activeId", activeId).commit()
    }

    private fun saveIndex(value: List<NavProfile> = profiles, active: String = activeId) {
        require(value.isNotEmpty() && value.map { it.id }.toSet().size == value.size &&
            value.any { it.id == active }) { "Invalid profile index update" }
        val encoded = JSONArray().apply { value.forEach { put(it.toJson()) } }.toString()
        // Stage the complete validated list in a separate SharedPreferences file
        // BEFORE changing the main index. A failed safety write aborts the edit.
        check(safeIndex.edit().putString("profiles", encoded)
            .putString("activeId", active).commit()) { "Could not back up profile index" }
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
        // Personal is our permanent safe fallback. Any vessel, including the
        // migrated original "ship-default", can be deleted on user confirmation.
        require(id != PERSONAL_ID) { "Personal profile cannot be deleted" }
        require(profiles.any { it.id == id && it.kind == NavProfileKind.VESSEL }) {
            "Unknown vessel profile"
        }
        val remaining = profiles.filterNot { it.id == id }
        val newActive = profileAfterDeletion(profiles, id, activeId)
        // First remove the profile from the index so it cannot accidentally
        // become active again after a restart.
        saveIndex(remaining, newActive)
        check(app.getSharedPreferences("navtasks-profile-" + id, Context.MODE_PRIVATE)
            .edit().clear().commit()) { "Unable to clear deleted profile data" }
    }

    fun findMatch(incoming: NavProfile): NavProfile? =
        profiles.firstOrNull { it.id == incoming.id && it.kind == incoming.kind }
            ?: profiles.firstOrNull {
                it.kind == incoming.kind && it.title.equals(incoming.title, ignoreCase = true)
            }

    fun snapshot(id: String): ProfileSnapshot {
        val profile = profiles.first { it.id == id }
        val prefs = data(id)
        val tasks = NavStorageRead.templates(prefs.getString("templates", "[]") ?: "[]")
        val runs = NavStorageRead.runs(prefs.getString("runs", "[]") ?: "[]")
        val notes = NavStorageRead.sections(prefs.getString("sections", "[]") ?: "[]")
        val services = JSONArray(prefs.getString("services", "[]"))
        val workOrders = JSONArray(prefs.getString("workOrders", "[]"))
        return ProfileSnapshot(
            profile,
            tasks, runs.values.toList(), notes,
            prefs.getString("lastVisit", LocalDate.now().toString()) ?: LocalDate.now().toString(),
            (0 until services.length()).map { ServiceEntry.fromJson(services.getJSONObject(it)) },
            (0 until workOrders.length()).map { WorkOrderEntry.fromJson(workOrders.getJSONObject(it)) },
        )
    }

    /**
     * Atomic append-only selective import. Does not touch TaskRun history,
     * lastVisit, profile identity, or application-wide settings.
     */
    internal fun mergeSelected(bundle: SelectiveBundle): SelectiveImportCounts {
        require(active.kind == NavProfileKind.VESSEL) { "Select a vessel profile" }
        require(bundle.records > 0) { "File contains no records" }
        val merged = mergeSelective(snapshot(activeId), bundle)
        if (merged.added.total == 0) return merged.added
        val data = merged.snapshot
        val encodedTasks = JSONArray().apply {
            data.templates.forEach { put(it.toJson()) }
        }.toString()
        val encodedHandover = JSONArray().apply {
            data.sections.forEach { put(it.toJson()) }
        }.toString()
        val encodedServices = JSONArray().apply {
            data.services.forEach { put(it.toJson()) }
        }.toString()
        val encodedAmos = JSONArray().apply {
            data.workOrders.forEach { put(it.toJson()) }
        }.toString()
        check(this.data(activeId).edit()
            .putString("templates", encodedTasks)
            .putString("sections", encodedHandover)
            .putString("services", encodedServices)
            .putString("workOrders", encodedAmos)
            .commit()) { "Selective import could not be saved" }
        return merged.added
    }

    /** Replace ALL tasks, history, notes, services and AMOS as one preference transaction. */
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
            .putString("workOrders", JSONArray().apply { incoming.workOrders.forEach { put(it.toJson()) } }.toString())
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
