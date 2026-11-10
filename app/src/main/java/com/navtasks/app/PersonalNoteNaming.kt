package com.navtasks.app

/** Default titles use existing profile-scoped Handover entries, without new schema. */
internal fun nextPersonalNoteTitle(sections: List<HandoverSection>, lang: UiLanguage): String {
    val pattern = Regex("^(?:Заметка|Note)\\s+(\\d+)$", RegexOption.IGNORE_CASE)
    val max = sections.asSequence().flatMap { it.entries.asSequence() }
        .mapNotNull { pattern.matchEntire(it.title.trim())?.groupValues?.get(1)?.toIntOrNull() }
        .filter { it in 1..999_998 }.maxOrNull() ?: 0
    return localized("Заметка", "Note", lang) + " " + (max + 1)
}
