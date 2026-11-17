package com.navtasks.app

import java.time.LocalDate

/** Read-only export model; never mutates Handover notes or completion marks. */
internal enum class HandoverExportKind { TITLE, META, SECTION, ENTRY, NOTE, PHOTO }
internal data class HandoverExportLine(val kind: HandoverExportKind, val text: String, val photo: HandoverPhoto? = null)

internal fun handoverExportLines(
    sections: List<HandoverSection>, onDate: LocalDate = LocalDate.now(),
): List<HandoverExportLine> {
    require(sections.size <= 250 && sections.sumOf { it.entries.size } <= 25_000)
    val lines = mutableListOf(
        HandoverExportLine(HandoverExportKind.TITLE, "Handover"),
        HandoverExportLine(HandoverExportKind.META, "NavTasks · " + onDate.toString()))
    var totalCharacters = 0
    sections.forEach { section ->
        lines += HandoverExportLine(HandoverExportKind.SECTION, section.title)
        section.entries.forEach { entry ->
            val suffix = when (entry.mark) {
                HandoverMark.OPEN -> ""
                HandoverMark.COMPLETED -> " [Completed" +
                    (entry.completedDate?.let { ": " + it } ?: "") + "]"
                HandoverMark.STRUCK -> " [Struck]"
            }
            lines += HandoverExportLine(HandoverExportKind.ENTRY, entry.title + suffix)
            entry.notes.split('\n').forEach {
                totalCharacters += it.length
                lines += HandoverExportLine(HandoverExportKind.NOTE, it)
            }
            // Photos are emitted directly after THIS entry's notes, never globally.
            entry.photos.forEach { photo ->
                lines += HandoverExportLine(HandoverExportKind.PHOTO, "", photo)
            }
        }
    }
    require(totalCharacters <= 8_000_000) { "Handover document exceeds size limit" }
    return lines
}
