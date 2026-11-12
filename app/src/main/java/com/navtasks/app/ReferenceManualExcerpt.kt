package com.navtasks.app

import org.json.JSONObject

/**
 * User-written, standalone excerpt. A source reference is informational only.
 * No links to scheduled Tasks/Services/AMOS and no automatic reminders.
 */
internal data class ReferenceManualExcerpt(
    val id: String = navReferenceId(),
    val title: String,
    val text: String,
    val sectionId: String = NavReferenceSections.PERSONAL,
    val folderId: String? = null,
    val sourceTitle: String = "",
    val sourceSection: String = "",
    val sourcePage: Int? = null,
    val sourceEdition: String = "",
) {
    fun validate() {
        referenceSafeId(id)
        referenceSafeId(sectionId)
        if (folderId != null) referenceSafeId(folderId)
        require(title.isNotBlank() && title.length <= 160)
        require(text.isNotBlank() && text.length <= 40000)
        require(sourceTitle.length <= 220 && sourceSection.length <= 160)
        require(sourceEdition.length <= 100)
        require(sourcePage == null || sourcePage in 1..100000)
    }

    fun toJson(): JSONObject {
        validate()
        return JSONObject().put("id", id).put("title", title)
            .put("text", text).put("sectionId", sectionId)
            .put("folderId", folderId ?: JSONObject.NULL)
            .put("sourceTitle", sourceTitle)
            .put("sourceSection", sourceSection)
            .put("sourcePage", sourcePage ?: JSONObject.NULL)
            .put("sourceEdition", sourceEdition)
    }

    companion object {
        fun fromJson(obj: JSONObject): ReferenceManualExcerpt {
            require(obj.length() == 9) { "Unknown Reference excerpt fields" }
            return ReferenceManualExcerpt(
                id = obj.getString("id"),
                title = obj.getString("title"),
                text = obj.getString("text"),
                sectionId = obj.getString("sectionId"),
                folderId = if (obj.isNull("folderId")) null else obj.getString("folderId"),
                sourceTitle = obj.getString("sourceTitle"),
                sourceSection = obj.getString("sourceSection"),
                sourcePage = if (obj.isNull("sourcePage")) null else obj.getInt("sourcePage"),
                sourceEdition = obj.getString("sourceEdition"),
            ).also { it.validate() }
        }
    }
}

internal fun referenceExcerptMatches(excerpt: ReferenceManualExcerpt, query: String): Boolean {
    val needle = navEffectiveSearchQuery(query)
    return needle.isEmpty() || listOf(
        excerpt.title, excerpt.text, excerpt.sourceTitle,
        excerpt.sourceSection, excerpt.sourceEdition,
    ).any { it.contains(needle, ignoreCase = true) }
}
