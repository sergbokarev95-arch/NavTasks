package com.navtasks.app

/** Title-only AMOS search: comments, checklists and AMOS references are excluded. */
internal fun searchAmosTitles(items: List<WorkOrderEntry>, input: String): List<WorkOrderEntry> =
    items.filter { it.title.contains(input.trim(), ignoreCase = true) }

/** Title-only equipment search: request numbers and service notes are excluded. */
internal fun searchServiceTitles(items: List<ServiceEntry>, input: String): List<ServiceEntry> =
    items.filter { it.equipment.contains(input.trim(), ignoreCase = true) }

/** Search every Handover category plus every entry's name and notes. */
internal fun searchHandoverSections(
    sections: List<HandoverSection>, input: String,
): List<HandoverSection> {
    val query = input.trim()
    if (query.isBlank()) return sections
    return sections.mapNotNull { section ->
        if (section.title.contains(query, ignoreCase = true)) section
        else section.copy(entries = section.entries.filter {
            it.title.contains(query, ignoreCase = true) ||
                it.notes.contains(query, ignoreCase = true)
        }).takeIf { it.entries.isNotEmpty() }
    }
}
