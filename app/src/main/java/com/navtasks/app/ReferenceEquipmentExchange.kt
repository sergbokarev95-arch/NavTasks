package com.navtasks.app

import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * First portable NavTasks Reference exchange format: ONE self-contained equipment card
 * with all procedures, cited excerpts and instructions. No task/work/profile data.
 *
 * A .navref ZIP includes ONLY manifest.json. Optional whole PDF/DOCX inclusion and
 * broader folder/library exports need separate validated implementation later.
 */
internal object ReferenceEquipmentExchange {
    const val extension = ".navref"
    const val format = "navtasks-reference-equipment"
    private const val version = 1
    private const val manifestName = "manifest.json"
    private const val maxArchiveBytes = 12_000_000
    private const val maxManifestBytes = 16_000_000

    private fun sha256(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes)
            .joinToString("") { "%02x".format(it.toInt() and 0xFF) }

    fun encode(equipment: ReferenceEquipment, includeSerialNumber: Boolean = false): ByteArray {
        equipment.validate()
        // Sender's local folder ID is not meaningful in recipient's catalog.
        // Serial numbers can be private: default to excluding.
        val portable = equipment.copy(
            folderId = null,
            serialNumber = if (includeSerialNumber) equipment.serialNumber else "",
        )
        val json = portable.toJson().toString()
        val jsonBytes = json.toByteArray(StandardCharsets.UTF_8)
        require(jsonBytes.size <= maxManifestBytes) { "Reference content too large" }
        val manifest = JSONObject()
            .put("format", format)
            .put("schemaVersion", version)
            .put("equipmentJson", json)
            .put("sha256", sha256(jsonBytes))
            .toString().toByteArray(StandardCharsets.UTF_8)
        require(manifest.size <= maxManifestBytes)
        return ByteArrayOutputStream().use { output ->
            ZipOutputStream(output).use { zip ->
                zip.putNextEntry(ZipEntry(manifestName))
                zip.write(manifest)
                zip.closeEntry()
            }
            output.toByteArray()
        }.also {
            require(it.size <= maxArchiveBytes) { "Reference archive too large" }
        }
    }

    fun decode(data: ByteArray): ReferenceEquipment {
        require(data.isNotEmpty() && data.size <= maxArchiveBytes) {
            "Reference archive size invalid"
        }
        val bytes = ZipInputStream(ByteArrayInputStream(data)).use { zip ->
            val entry = zip.nextEntry ?: error("Missing reference manifest")
            require(!entry.isDirectory && entry.name == manifestName) {
                "Unexpected or unsafe reference archive entry"
            }
            val output = ByteArrayOutputStream()
            val buffer = ByteArray(8192)
            while (true) {
                val read = zip.read(buffer)
                if (read < 0) break
                require(output.size() + read <= maxManifestBytes) {
                    "Reference content decompression limit exceeded"
                }
                output.write(buffer, 0, read)
            }
            zip.closeEntry()
            require(zip.nextEntry == null) { "Unexpected additional reference files" }
            output.toByteArray()
        }
        val manifest = JSONObject(String(bytes, StandardCharsets.UTF_8))
        require(manifest.length() == 4)
        require(manifest.getString("format") == format &&
            manifest.getInt("schemaVersion") == version) {
            "Unsupported reference archive version"
        }
        val equipmentJson = manifest.getString("equipmentJson")
        require(sha256(equipmentJson.toByteArray(StandardCharsets.UTF_8))
            == manifest.getString("sha256")) { "Damaged reference data" }
        return ReferenceEquipment.fromJson(JSONObject(equipmentJson))
    }
}

internal enum class ReferenceImportConflict {
    NONE, SAME_ID, POSSIBLE_DUPLICATE
}

/** Import preview reports conflicts. Never silently overwrite user equipment. */
internal fun referenceImportConflict(
    incoming: ReferenceEquipment, existing: List<ReferenceEquipment>,
): ReferenceImportConflict {
    if (existing.any { it.id == incoming.id }) return ReferenceImportConflict.SAME_ID
    if (existing.any {
        it.title.trim().equals(incoming.title.trim(), ignoreCase = true) &&
            it.model.trim().equals(incoming.model.trim(), ignoreCase = true)
    }) return ReferenceImportConflict.POSSIBLE_DUPLICATE
    return ReferenceImportConflict.NONE
}
