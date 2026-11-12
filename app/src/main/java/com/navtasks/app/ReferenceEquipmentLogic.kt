package com.navtasks.app

/** All transformations are confined to reference equipment; there are no scheduler calls. */
internal fun referenceSaveEquipment(
    catalog: ReferenceEquipmentCatalog, updated: ReferenceEquipment,
): ReferenceEquipmentCatalog {
    updated.validate()
    val previous = catalog.equipment.indexOfFirst { it.id == updated.id }
    val records = catalog.equipment.toMutableList()
    if (previous < 0) records.add(updated) else records[previous] = updated
    return catalog.copy(equipment = records).also { it.validate() }
}

internal fun referenceDeleteEquipment(
    catalog: ReferenceEquipmentCatalog, equipmentId: String,
): ReferenceEquipmentCatalog = catalog.copy(
    equipment = catalog.equipment.filterNot { it.id == equipmentId },
).also { it.validate() }

internal fun referenceSaveFolder(
    catalog: ReferenceEquipmentCatalog, updated: ReferenceFolder,
): ReferenceEquipmentCatalog {
    updated.validate()
    val previous = catalog.folders.indexOfFirst { it.id == updated.id }
    val folders = catalog.folders.toMutableList()
    if (previous < 0) folders.add(updated) else folders[previous] = updated
    return catalog.copy(folders = folders).also { it.validate() }
}

/** Never delete a folder if it still contains equipment or nested folders. */
internal fun referenceDeleteEmptyFolder(
    catalog: ReferenceEquipmentCatalog, folderId: String,
): ReferenceEquipmentCatalog {
    require(catalog.folders.none { it.parentId == folderId }) { "Folder contains subfolders" }
    require(catalog.equipment.none { it.folderId == folderId }) { "Folder contains equipment" }
    return catalog.copy(folders = catalog.folders.filterNot { it.id == folderId })
        .also { it.validate() }
}

internal fun referenceSaveProcedure(
    equipment: ReferenceEquipment, updated: ReferenceProcedure,
): ReferenceEquipment {
    updated.validate()
    val index = equipment.procedures.indexOfFirst { it.id == updated.id }
    val procedures = equipment.procedures.toMutableList()
    if (index < 0) procedures.add(updated) else procedures[index] = updated
    return equipment.copy(procedures = procedures).also { it.validate() }
}

internal fun referenceRemoveProcedure(
    equipment: ReferenceEquipment, procedureId: String,
): ReferenceEquipment = equipment.copy(
    procedures = equipment.procedures.filterNot { it.id == procedureId },
).also { it.validate() }

/** A search matches equipment metadata, full procedure text, and cited manuals. */
internal fun referenceMatchesEquipment(item: ReferenceEquipment, query: String): Boolean {
    val needle = navEffectiveSearchQuery(query)
    if (needle.isEmpty()) return true
    val fields = buildList {
        add(item.title)
        add(item.manufacturer)
        add(item.model)
        add(item.serialNumber)
        add(item.location)
        add(item.notes)
        item.procedures.forEach { p ->
            add(p.title)
            add(p.intervalDescription)
            add(p.excerpt)
            add(p.instructions)
            add(p.source.documentTitle)
            add(p.source.section)
            add(p.source.edition)
        }
    }
    return fields.any { it.contains(needle, ignoreCase = true) }
}

/** Short context for a global search result; whole procedure text remains in the reader. */
internal fun referenceEquipmentSearchPreview(
    item: ReferenceEquipment, query: String,
): String {
    val needle = navEffectiveSearchQuery(query)
    if (needle.isEmpty()) {
        val caption = listOf(item.manufacturer, item.model).filter { it.isNotBlank() }
            .joinToString(" · ")
        return caption.ifBlank { "${item.procedures.size} процедур" }
    }
    val texts = buildList {
        add(item.model)
        add(item.location)
        add(item.notes)
        item.procedures.forEach { p ->
            add(p.title)
            add(p.intervalDescription)
            add(p.source.documentTitle)
            add(p.excerpt)
            add(p.instructions)
        }
    }
    val matching = texts.firstOrNull { it.contains(needle, ignoreCase = true) }
        ?: return "${item.procedures.size} процедур"
    val offset = matching.indexOf(needle, ignoreCase = true)
    val start = (offset - 28).coerceAtLeast(0)
    val end = (offset + needle.length + 65).coerceAtMost(matching.length)
    return (if (start > 0) "…" else "") + matching.substring(start, end)
        .replace("\n", " ") + if (end < matching.length) "…" else ""
}

/** Folder name search walks all user folders; browsing stays within the open folder. */
internal fun referenceVisibleFolders(
    catalog: ReferenceEquipmentCatalog, parentId: String?, query: String,
): List<ReferenceFolder> {
    val needle = navEffectiveSearchQuery(query)
    return catalog.folders.filter {
        if (needle.isEmpty()) it.parentId == parentId
        else it.title.contains(needle, ignoreCase = true)
    }.sortedBy { it.title.lowercase() }
}

/** Empty query is a browse state; active search searches all folders, not just open one. */
internal fun referenceEquipmentVisible(
    catalog: ReferenceEquipmentCatalog, folderId: String?, query: String,
): List<ReferenceEquipment> =
    catalog.equipment.filter {
        if (navEffectiveSearchQuery(query).isEmpty()) it.folderId == folderId
        else referenceMatchesEquipment(it, query)
    }.sortedBy { it.title.lowercase() }

/**
 * Importing a shared equipment card never moves its originating folder
 * and never creates a task. Caller chooses add-copy or explicit replace.
 */
internal fun referenceImportEquipment(
    catalog: ReferenceEquipmentCatalog,
    incoming: ReferenceEquipment,
    targetFolderId: String?,
    replaceSameId: Boolean = false,
): ReferenceEquipmentCatalog {
    val local = if (replaceSameId) incoming.copy(folderId = targetFolderId)
        else incoming.copy(
            id = navReferenceId(),
            folderId = targetFolderId,
            procedures = incoming.procedures.map { it.copy(id = navReferenceId()) },
        )
    return referenceSaveEquipment(catalog, local)
}
