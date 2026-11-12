package com.navtasks.app

import org.json.JSONArray
import org.json.JSONObject

/**
 * Shared offline Reference library. Manual originals are shared across profiles;
 * installed equipment remains in its own vessel's independent catalog.
 *
 * NO task IDs, due dates, completion state, AMOS, Services or notifications.
 */
internal object ReferenceManualMime {
    const val PDF = "application/pdf"
    const val DOCX =
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    val allowed = setOf(PDF, DOCX)
}

internal data class ReferenceManualFolder(
    val id: String = navReferenceId(),
    val title: String,
    val sectionId: String = NavReferenceSections.PERSONAL,
    val parentId: String? = null,
) {
    fun validate() {
        referenceSafeId(id)
        referenceSafeId(sectionId)
        require(title.isNotBlank() && title.length <= 120)
        if (parentId != null) referenceSafeId(parentId)
        require(id != parentId)
    }

    fun toJson(): JSONObject {
        validate()
        return JSONObject().put("id", id).put("title", title)
            .put("sectionId", sectionId)
            .put("parentId", parentId ?: JSONObject.NULL)
    }

    companion object {
        fun fromJson(obj: JSONObject): ReferenceManualFolder {
            require(obj.length() == 4)
            return ReferenceManualFolder(
                id = obj.getString("id"),
                title = obj.getString("title"),
                sectionId = obj.getString("sectionId"),
                parentId = if (obj.isNull("parentId")) null else obj.getString("parentId"),
            ).also { it.validate() }
        }
    }
}

internal data class ReferenceManualDocument(
    val id: String = navReferenceId(),
    val title: String,
    val sectionId: String = NavReferenceSections.PERSONAL,
    val folderId: String? = null,
    val originalFilename: String,
    val mimeType: String,
    /** SHA-256 of the ORIGINAL file bytes, used as the private storage key. */
    val sha256: String,
    val bytes: Long,
    val notes: String = "",
) {
    fun validate() {
        referenceSafeId(id)
        referenceSafeId(sectionId)
        require(title.isNotBlank() && title.length <= 200)
        require(originalFilename.isNotBlank() && originalFilename.length <= 240)
        // Never accept a path as the display filename; no arbitrary filesystem paths.
        require(!originalFilename.contains('/') && !originalFilename.contains('\\') &&
            originalFilename != "." && originalFilename != "..")
        require(mimeType in ReferenceManualMime.allowed)
        require(sha256.matches(Regex("[0-9a-f]{64}")))
        require(bytes in 1..250_000_000)
        require(notes.length <= 12000)
        if (folderId != null) referenceSafeId(folderId)
    }

    fun toJson(): JSONObject {
        validate()
        return JSONObject().put("id", id).put("title", title)
            .put("sectionId", sectionId)
            .put("folderId", folderId ?: JSONObject.NULL)
            .put("originalFilename", originalFilename)
            .put("mimeType", mimeType)
            .put("sha256", sha256).put("bytes", bytes).put("notes", notes)
    }

    companion object {
        fun fromJson(obj: JSONObject): ReferenceManualDocument {
            require(obj.length() == 9)
            return ReferenceManualDocument(
                id = obj.getString("id"),
                title = obj.getString("title"),
                sectionId = obj.getString("sectionId"),
                folderId = if (obj.isNull("folderId")) null else obj.getString("folderId"),
                originalFilename = obj.getString("originalFilename"),
                mimeType = obj.getString("mimeType"),
                sha256 = obj.getString("sha256"),
                bytes = obj.getLong("bytes"),
                notes = obj.getString("notes"),
            ).also { it.validate() }
        }
    }
}

internal data class ReferenceManualCatalog(
    val folders: List<ReferenceManualFolder> = emptyList(),
    val documents: List<ReferenceManualDocument> = emptyList(),
    val excerpts: List<ReferenceManualExcerpt> = emptyList(),
) {
    fun validate() {
        require(folders.size <= 5000 && documents.size <= 10000 && excerpts.size <= 15000)
        require(folders.map { it.id }.distinct().size == folders.size)
        require(documents.map { it.id }.distinct().size == documents.size)
        require(excerpts.map { it.id }.distinct().size == excerpts.size)
        // One hash+MIME points to exactly one immutable original byte stream;
        // repeated cards cannot silently disagree about its expected length.
        require(documents.groupBy { it.sha256 to it.mimeType }.values.all { group ->
            group.map { it.bytes }.distinct().size == 1
        }) { "Conflicting metadata for the same original manual" }
        val byId = folders.associateBy { it.id }
        folders.forEach { folder ->
            folder.validate()
            var current: String? = folder.id
            val seen = mutableSetOf<String>()
            while (current != null) {
                require(seen.add(current)) { "Cycle in Reference manual folders" }
                val parent = byId[current]?.parentId
                require(parent == null || parent in byId) { "Missing manual folder parent" }
                if (parent != null) require(byId.getValue(parent).sectionId == folder.sectionId) {
                    "Reference folder parent belongs to different section"
                }
                current = parent
            }
        }
        documents.forEach { document ->
            document.validate()
            if (document.folderId != null) {
                val folder = byId[document.folderId]
                    ?: throw IllegalArgumentException("Missing manual document folder")
                require(folder.sectionId == document.sectionId) {
                    "Reference document belongs to a different section than its folder"
                }
            }
        }
        excerpts.forEach { excerpt ->
            excerpt.validate()
            if (excerpt.folderId != null) {
                val folder = byId[excerpt.folderId]
                    ?: throw IllegalArgumentException("Missing excerpt folder")
                require(folder.sectionId == excerpt.sectionId) {
                    "Excerpt belongs to a different Reference section"
                }
            }
        }
    }

    fun toJson(): JSONObject {
        validate()
        return JSONObject()
            .put("format", "navtasks-reference-manuals")
            .put("schemaVersion", 1)
            .put("folders", JSONArray().apply { folders.forEach { put(it.toJson()) } })
            .put("documents", JSONArray().apply { documents.forEach { put(it.toJson()) } })
            .put("excerpts", JSONArray().apply { excerpts.forEach { put(it.toJson()) } })
    }

    companion object {
        fun fromJson(obj: JSONObject): ReferenceManualCatalog {
            // Accept earlier v1 catalogs (before excerpts existed) without migration.
            require(obj.length() == 4 || obj.length() == 5)
            require(obj.getString("format") == "navtasks-reference-manuals")
            require(obj.getInt("schemaVersion") == 1)
            val folders = obj.getJSONArray("folders")
            val documents = obj.getJSONArray("documents")
            val excerpts = obj.optJSONArray("excerpts") ?: JSONArray()
            require(folders.length() <= 5000 && documents.length() <= 10000 &&
                excerpts.length() <= 15000)
            return ReferenceManualCatalog(
                folders = (0 until folders.length()).map {
                    ReferenceManualFolder.fromJson(folders.getJSONObject(it))
                },
                documents = (0 until documents.length()).map {
                    ReferenceManualDocument.fromJson(documents.getJSONObject(it))
                },
                excerpts = (0 until excerpts.length()).map {
                    ReferenceManualExcerpt.fromJson(excerpts.getJSONObject(it))
                },
            ).also { it.validate() }
        }
    }
}

/** Shared manuals can be organized freely, never mutate vessel equipment records. */
internal fun referenceManualSearch(
    catalog: ReferenceManualCatalog, query: String,
): List<ReferenceManualDocument> {
    val needle = navEffectiveSearchQuery(query)
    if (needle.isEmpty()) return catalog.documents
    return catalog.documents.filter {
        it.title.contains(needle, ignoreCase = true) ||
            it.originalFilename.contains(needle, ignoreCase = true) ||
            it.notes.contains(needle, ignoreCase = true)
    }
}
