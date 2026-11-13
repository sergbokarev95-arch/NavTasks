package com.navtasks.app

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * Information-only reference catalog. Reference never tracks or links tasks,
 * AMOS/Services, scheduling, completion, due dates or notifications.
 * Only SIRE and VIQ are pre-created manual categories; user adds all content.
 */
internal object NavReferenceSections {
    const val SIRE = "sire-2"
    const val VIQ7 = "viq7"
    const val EQUIPMENT = "equipment"
    const val PERSONAL = "my-materials"
}

internal fun navReferenceId(): String = UUID.randomUUID().toString()

private val referenceIdPattern = Regex("[A-Za-z0-9_-]{1,80}")

internal fun referenceSafeId(value: String) {
    require(referenceIdPattern.matches(value)) { "Invalid reference identifier" }
}

private fun JSONObject.requireReferenceFields(allowed: Set<String>) {
    val keys = keys()
    while (keys.hasNext()) {
        require(keys.next() in allowed) { "Unsupported reference field" }
    }
}

internal data class ReferenceSource(
    val documentTitle: String,
    val page: Int? = null,
    val section: String = "",
    val edition: String = "",
) {
    fun validate() {
        require(documentTitle.isNotBlank() && documentTitle.length <= 200)
        require(page == null || page in 1..100000)
        require(section.length <= 160 && edition.length <= 100)
    }

    fun toJson(): JSONObject {
        validate()
        return JSONObject()
            .put("documentTitle", documentTitle)
            .put("page", page ?: JSONObject.NULL)
            .put("section", section)
            .put("edition", edition)
    }

    companion object {
        fun fromJson(value: JSONObject): ReferenceSource {
            value.requireReferenceFields(setOf("documentTitle", "page", "section", "edition"))
            val pageValue = if (value.isNull("page")) null else value.getInt("page")
            return ReferenceSource(
                documentTitle = value.getString("documentTitle"),
                page = pageValue,
                section = value.optString("section"),
                edition = value.optString("edition"),
            ).also { it.validate() }
        }
    }
}

internal data class ReferenceProcedure(
    val id: String = navReferenceId(),
    val title: String,
    /** Reference text only, e.g. "every 3 months"; never a scheduled interval. */
    val intervalDescription: String = "",
    val excerpt: String,
    val source: ReferenceSource,
    val instructions: String = "",
    // A shared ORIGINAL manual stored by SHA-256; equipment remains vessel-scoped.
    // Storing a content hash (rather than document ID) survives .navlib ID remapping.
    val attachmentSha256: String? = null,
    val attachmentMimeType: String? = null,
    val attachmentFilename: String = "",
) {
    fun validate() {
        referenceSafeId(id)
        require(title.isNotBlank() && title.length <= 160)
        require(intervalDescription.length <= 120)
        require(excerpt.isNotBlank() && excerpt.length <= 20000)
        require(instructions.length <= 20000)
        require((attachmentSha256 == null) == (attachmentMimeType == null)) {
            "Incomplete manual attachment reference"
        }
        if (attachmentSha256 != null) {
            require(attachmentSha256.matches(Regex("[0-9a-f]{64}")))
            require(attachmentMimeType in ReferenceManualMime.allowed)
            require(attachmentFilename.isNotBlank() && attachmentFilename.length <= 240)
            require('/' !in attachmentFilename && '\\' !in attachmentFilename)
        } else require(attachmentFilename.isEmpty())
        source.validate()
    }

    fun toJson(): JSONObject {
        validate()
        return JSONObject()
            .put("id", id).put("title", title)
            .put("intervalDescription", intervalDescription)
            .put("excerpt", excerpt).put("source", source.toJson())
            .put("instructions", instructions)
            .put("attachmentSha256", attachmentSha256 ?: JSONObject.NULL)
            .put("attachmentMimeType", attachmentMimeType ?: JSONObject.NULL)
            .put("attachmentFilename", attachmentFilename)
    }

    companion object {
        fun fromJson(value: JSONObject): ReferenceProcedure {
            value.requireReferenceFields(setOf(
                "id", "title", "intervalDescription", "excerpt", "source", "instructions",
                "attachmentSha256", "attachmentMimeType", "attachmentFilename",
            ))
            return ReferenceProcedure(
                id = value.getString("id"),
                title = value.getString("title"),
                intervalDescription = value.optString("intervalDescription"),
                excerpt = value.getString("excerpt"),
                source = ReferenceSource.fromJson(value.getJSONObject("source")),
                instructions = value.optString("instructions"),
                attachmentSha256 = if (value.isNull("attachmentSha256")) null
                    else value.getString("attachmentSha256"),
                attachmentMimeType = if (value.isNull("attachmentMimeType")) null
                    else value.getString("attachmentMimeType"),
                attachmentFilename = value.optString("attachmentFilename"),
            ).also { it.validate() }
        }
    }
}

/** Stored under the chosen vessel profile; original manuals are a shared library. */
internal data class ReferenceEquipment(
    val id: String = navReferenceId(),
    val title: String,
    val manufacturer: String = "",
    val model: String = "",
    val serialNumber: String = "",
    val location: String = "",
    val notes: String = "",
    /** A locally assigned folder; not a task group or an AMOS identifier. */
    val folderId: String? = null,
    val procedures: List<ReferenceProcedure> = emptyList(),
) {
    fun validate() {
        referenceSafeId(id)
        require(title.isNotBlank() && title.length <= 160)
        require(manufacturer.length <= 160 && model.length <= 160)
        require(serialNumber.length <= 160 && location.length <= 160)
        require(notes.length <= 12000)
        if (folderId != null) referenceSafeId(folderId)
        require(procedures.size <= 200)
        require(procedures.map { it.id }.distinct().size == procedures.size)
        procedures.forEach { it.validate() }
    }

    fun toJson(): JSONObject {
        validate()
        return JSONObject().put("id", id).put("title", title)
            .put("manufacturer", manufacturer).put("model", model)
            .put("serialNumber", serialNumber).put("location", location)
            .put("notes", notes)
            .put("folderId", folderId ?: JSONObject.NULL)
            .put("procedures", JSONArray().apply {
                procedures.forEach { put(it.toJson()) }
            })
    }

    companion object {
        fun fromJson(value: JSONObject): ReferenceEquipment {
            value.requireReferenceFields(setOf(
                "id", "title", "manufacturer", "model", "serialNumber",
                "location", "notes", "folderId", "procedures",
            ))
            val rows = value.getJSONArray("procedures")
            require(rows.length() <= 200)
            return ReferenceEquipment(
                id = value.getString("id"),
                title = value.getString("title"),
                manufacturer = value.optString("manufacturer"),
                model = value.optString("model"),
                serialNumber = value.optString("serialNumber"),
                location = value.optString("location"),
                notes = value.optString("notes"),
                folderId = if (value.isNull("folderId")) null else value.getString("folderId"),
                procedures = (0 until rows.length()).map { i ->
                    ReferenceProcedure.fromJson(rows.getJSONObject(i))
                },
            ).also { it.validate() }
        }
    }
}

/** An independent, user-defined hierarchy; cycles are rejected before saving. */
internal data class ReferenceFolder(
    val id: String = navReferenceId(),
    val title: String,
    val parentId: String? = null,
) {
    fun validate() {
        referenceSafeId(id)
        require(title.isNotBlank() && title.length <= 120)
        if (parentId != null) referenceSafeId(parentId)
        require(parentId != id)
    }
}

internal fun validateReferenceFolders(folders: List<ReferenceFolder>) {
    require(folders.size <= 5000)
    require(folders.map { it.id }.distinct().size == folders.size)
    val byId = folders.associateBy { it.id }
    folders.forEach { folder ->
        folder.validate()
        val seen = mutableSetOf<String>()
        var cursor: String? = folder.id
        while (cursor != null) {
            require(seen.add(cursor)) { "Cyclic reference folder hierarchy" }
            val parent = byId[cursor]?.parentId
            require(parent == null || parent in byId) { "Missing parent folder" }
            cursor = parent
        }
    }
}
