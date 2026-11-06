package com.navtasks.app

import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDate
import java.util.Locale
import java.util.UUID

/**
 * Portable, selectively chosen definitions. It is intentionally NOT a full-profile
 * backup: completion runs, service journals and handover checkmarks do not travel.
 */
internal data class SelectiveBundle(
    val tasks: List<TaskTemplate> = emptyList(),
    val workOrders: List<WorkOrderEntry> = emptyList(),
    val services: List<ServiceEntry> = emptyList(),
    val sections: List<HandoverSection> = emptyList(),
) {
    val records: Int get() = tasks.size + workOrders.size + services.size + sections.size
}

internal data class SelectiveImportCounts(
    val tasks: Int = 0,
    val amos: Int = 0,
    val services: Int = 0,
    val sections: Int = 0,
    val handoverEntries: Int = 0,
) {
    val total: Int get() = tasks + amos + services + sections + handoverEntries
}

internal data class SelectiveMergeResult(
    val snapshot: ProfileSnapshot,
    val added: SelectiveImportCounts,
)

/** No user-specific completion information is exported with a template. */
internal object SelectiveTransfer {
    const val FORMAT = "navtasks-selective-v1"
    const val MAX_BYTES = 10_000_000
    private const val VERSION = 1

    private fun stripWork(item: WorkOrderEntry) = item.copy(
        completedAt = null, checkedItemIds = emptySet(),
        executionHistory = emptyList(), closedAt = null,
    )

    private fun stripService(item: ServiceEntry) = item.copy(
        completedAt = null, closedAt = null, requestNumber = "",
        serviceHistory = emptyList(),
    )

    private fun stripHandover(section: HandoverSection) = section.copy(
        entries = section.entries.map {
            it.copy(mark = HandoverMark.OPEN, completedDate = null)
        },
    )

    fun clean(bundle: SelectiveBundle) = bundle.copy(
        workOrders = bundle.workOrders.map(::stripWork),
        services = bundle.services.map(::stripService),
        sections = bundle.sections.map(::stripHandover),
    )

    fun export(bundle: SelectiveBundle): String {
        val selected = clean(bundle)
        require(selected.records > 0) { "Select at least one item" }
        require(selected.tasks.size <= 2500 && selected.workOrders.size <= 1500 &&
            selected.services.size <= 1000 && selected.sections.size <= 250) {
            "Too many records"
        }
        val encoded = JSONObject()
            .put("format", FORMAT)
            .put("version", VERSION)
            .put("created", Instant.now().toString())
            .put("tasks", JSONArray().apply { selected.tasks.forEach { put(it.toJson()) } })
            .put("workOrders", JSONArray().apply { selected.workOrders.forEach { put(it.toJson()) } })
            .put("services", JSONArray().apply { selected.services.forEach { put(it.toJson()) } })
            .put("sections", JSONArray().apply { selected.sections.forEach { put(it.toJson()) } })
            .toString(2)
        require(encoded.length <= MAX_BYTES) { "Selected file exceeds 10 MB" }
        return encoded
    }

    /** Older task-only and Handover packages remain importable. */
    fun parse(raw: String): SelectiveBundle {
        require(raw.isNotBlank() && raw.length <= MAX_BYTES) { "File exceeds 10 MB" }
        val json = JSONObject(raw)
        return when (json.optString("format")) {
            "navtasks-template-bundle" ->
                SelectiveBundle(tasks = NavTaskTransfer.parse(raw))
            "navtasks-handover-v1" -> HandoverTransfer.parse(raw).let {
                clean(SelectiveBundle(tasks = it.tasks, sections = it.sections))
            }
            FORMAT -> {
                require(json.optInt("version") == VERSION) { "Unsupported bundle version" }
                val taskList = json.optJSONArray("tasks") ?: JSONArray()
                val workList = json.optJSONArray("workOrders") ?: JSONArray()
                val serviceList = json.optJSONArray("services") ?: JSONArray()
                val noteList = json.optJSONArray("sections") ?: JSONArray()
                require(taskList.length() <= 2500 && workList.length() <= 1500 &&
                    serviceList.length() <= 1000 && noteList.length() <= 250) {
                    "Too many records"
                }
                val tasks = (0 until taskList.length()).map {
                    val obj = taskList.getJSONObject(it)
                    require(obj.optString("title").trim().length in 1..200) {
                        "Invalid task title"
                    }
                    require(Frequency.entries.any { f -> f.name == obj.optString("frequency") }) {
                        "Invalid task frequency"
                    }
                    val checks = obj.optJSONArray("checklist") ?: JSONArray()
                    require(checks.length() <= 200) { "Too many checklist entries" }
                    TaskTemplate.fromJson(obj)
                }
                val orders = (0 until workList.length()).map {
                    WorkOrderEntry.fromJson(workList.getJSONObject(it))
                }
                val services = (0 until serviceList.length()).map {
                    ServiceEntry.fromJson(serviceList.getJSONObject(it))
                }
                val sections = (0 until noteList.length()).map {
                    HandoverSection.fromJson(noteList.getJSONObject(it))
                }
                require(sections.sumOf { it.entries.size } <= 25000) {
                    "Too many Handover entries"
                }
                clean(SelectiveBundle(tasks, orders, services, sections))
            }
            else -> error("Not a NavTasks selective import file")
        }
    }
}

private fun uniqueLabel(value: String): String =
    value.trim().lowercase(Locale.ROOT)

private fun mergeNotebook(
    existing: List<HandoverSection>,
    imported: List<HandoverSection>,
): Triple<List<HandoverSection>, Int, Int> {
    val result = existing.toMutableList()
    var newSections = 0
    var newEntries = 0
    imported.forEach { source ->
        if (source.title.isBlank()) return@forEach
        val index = result.indexOfFirst {
            uniqueLabel(it.title) == uniqueLabel(source.title)
        }
        if (index < 0) {
            result += source.copy(id = UUID.randomUUID().toString(),
                entries = source.entries.map {
                    it.copy(id = UUID.randomUUID().toString(),
                        mark = HandoverMark.OPEN, completedDate = null)
                })
            newSections++
            newEntries += source.entries.size
        } else {
            val local = result[index]
            val known = local.entries.map {
                uniqueLabel(it.title) to it.notes
            }.toMutableSet()
            val additions = source.entries.filter {
                known.add(uniqueLabel(it.title) to it.notes)
            }.map {
                it.copy(id = UUID.randomUUID().toString(),
                    mark = HandoverMark.OPEN, completedDate = null)
            }
            result[index] = local.copy(entries = local.entries + additions)
            newEntries += additions.size
        }
    }
    return Triple(result, newSections, newEntries)
}

/**
 * Preflight everything in memory; ProfileStore commits the resulting four
 * collections together. Existing records, histories and due dates stay intact.
 */
internal fun mergeSelective(
    existing: ProfileSnapshot, incoming: SelectiveBundle,
): SelectiveMergeResult {
    require(existing.profile.kind == NavProfileKind.VESSEL) {
        "Only vessel profiles can import work and tasks"
    }
    val source = SelectiveTransfer.clean(incoming)
    val newTasks = NavTaskTransfer.newTemplates(existing.templates, source.tasks)

    val workNames = existing.workOrders.map { uniqueLabel(it.title) }.toMutableSet()
    val workRefs = existing.workOrders.map { uniqueLabel(it.reference) }
        .filter { it.isNotBlank() }.toMutableSet()
    val newWork = source.workOrders.filter { item ->
        val label = uniqueLabel(item.title)
        val ref = uniqueLabel(item.reference)
        if (label in workNames || (ref.isNotBlank() && ref in workRefs)) false
        else {
            workNames.add(label)
            if (ref.isNotBlank()) workRefs.add(ref)
            true
        }
    }.map { item ->
        item.copy(id = UUID.randomUUID().toString(), completedAt = null,
            closedAt = null, createdAt = LocalDate.now(),
            checkedItemIds = emptySet(), executionHistory = emptyList(),
            checklist = item.checklist.map { it.copy(id = UUID.randomUUID().toString()) })
    }
    validateWorkOrders(existing.workOrders + newWork)

    val serviceNames = existing.services.map { uniqueLabel(it.equipment) }.toMutableSet()
    val newServices = source.services.filter {
        serviceNames.add(uniqueLabel(it.equipment))
    }.map { item ->
        item.copy(id = UUID.randomUUID().toString(), closedAt = null,
            completedAt = null, requestNumber = "", serviceHistory = emptyList())
    }
    require(existing.services.size + newServices.size <= 1000) { "Too many services" }
    require(existing.templates.size + newTasks.size <= 2500) { "Too many tasks" }

    val (notebook, addedSections, addedEntries) =
        mergeNotebook(existing.sections, source.sections)
    require(notebook.size <= 250 && notebook.sumOf { it.entries.size } <= 25000) {
        "Too many Handover notes"
    }
    return SelectiveMergeResult(
        snapshot = existing.copy(
            templates = existing.templates + newTasks,
            sections = notebook,
            services = existing.services + newServices,
            workOrders = existing.workOrders + newWork,
        ),
        added = SelectiveImportCounts(
            tasks = newTasks.size, amos = newWork.size,
            services = newServices.size, sections = addedSections,
            handoverEntries = addedEntries,
        ),
    )
}
