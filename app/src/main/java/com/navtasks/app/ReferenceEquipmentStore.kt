package com.navtasks.app

import android.content.Context
import android.util.AtomicFile
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.nio.charset.StandardCharsets

/**
 * Independent equipment reference data, scoped per VESSEL profile.
 * No TaskStore/Services/AMOS calls, no scheduling and no task links.
 * Original PDF/DOCX manuals will reside in a separate shared library store.
 */
internal data class ReferenceEquipmentCatalog(
    val folders: List<ReferenceFolder> = emptyList(),
    val equipment: List<ReferenceEquipment> = emptyList(),
) {
    fun validate() {
        require(equipment.size <= 3000)
        validateReferenceFolders(folders)
        val folderIds = folders.map { it.id }.toSet()
        require(equipment.map { it.id }.distinct().size == equipment.size)
        equipment.forEach { item ->
            item.validate()
            require(item.folderId == null || item.folderId in folderIds) {
                "Missing reference equipment folder"
            }
        }
    }

    fun toJson(): JSONObject {
        validate()
        return JSONObject()
            .put("format", "navtasks-reference-equipment-v1")
            .put("schemaVersion", 1)
            .put("folders", JSONArray().apply {
                folders.forEach { f ->
                    put(JSONObject()
                        .put("id", f.id)
                        .put("title", f.title)
                        .put("parentId", f.parentId ?: JSONObject.NULL))
                }
            })
            .put("equipment", JSONArray().apply {
                equipment.forEach { put(it.toJson()) }
            })
    }

    companion object {
        fun fromJson(raw: JSONObject): ReferenceEquipmentCatalog {
            require(raw.length() == 4)
            require(raw.getString("format") == "navtasks-reference-equipment-v1")
            require(raw.getInt("schemaVersion") == 1)
            val folderArray = raw.getJSONArray("folders")
            val equipmentArray = raw.getJSONArray("equipment")
            require(folderArray.length() <= 5000 && equipmentArray.length() <= 3000)
            return ReferenceEquipmentCatalog(
                folders = (0 until folderArray.length()).map { i ->
                    val folder = folderArray.getJSONObject(i)
                    require(folder.length() == 3)
                    ReferenceFolder(
                        id = folder.getString("id"),
                        title = folder.getString("title"),
                        parentId = if (folder.isNull("parentId")) null
                            else folder.getString("parentId"),
                    )
                },
                equipment = (0 until equipmentArray.length()).map { i ->
                    ReferenceEquipment.fromJson(equipmentArray.getJSONObject(i))
                },
            ).also { it.validate() }
        }
    }
}

/**
 * Atomic per-profile JSON. On parsing failure fail CLOSED: never overwrite a
 * damaged file with an empty catalog. This store is not yet wired into UI.
 */
internal class ReferenceEquipmentStore(
    context: Context,
    private val profileId: String,
) {
    private val file: File

    init {
        referenceSafeId(profileId)
        file = File(context.applicationContext.filesDir,
            "navtasks-reference/v1/vessel/$profileId.json")
    }

    private val atomic: AtomicFile by lazy { AtomicFile(file) }

    fun read(): ReferenceEquipmentCatalog {
        val content = try {
            atomic.openRead().use { input ->
                val data = ByteArrayOutputStream()
                val buffer = ByteArray(8192)
                while (true) {
                    val n = input.read(buffer)
                    if (n < 0) break
                    require(data.size() + n <= 24_000_000) {
                        "Reference equipment catalog too large"
                    }
                    data.write(buffer, 0, n)
                }
                data.toByteArray()
            }
        } catch (_: FileNotFoundException) {
            return ReferenceEquipmentCatalog()
        }
        return ReferenceEquipmentCatalog.fromJson(JSONObject(String(
            content, StandardCharsets.UTF_8)))
    }

    fun write(updated: ReferenceEquipmentCatalog) {
        updated.validate()
        // If existing JSON is damaged, read() throws and no write is attempted.
        read()
        val content = updated.toJson().toString().toByteArray(StandardCharsets.UTF_8)
        require(content.size <= 24_000_000) { "Reference equipment catalog too large" }
        require(file.parentFile?.mkdirs() == true || file.parentFile?.isDirectory == true) {
            "Unable to create reference storage directory"
        }
        val stream = atomic.startWrite()
        try {
            stream.write(content)
            atomic.finishWrite(stream)
        } catch (cause: Throwable) {
            atomic.failWrite(stream)
            throw cause
        }
    }
}
