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
 * Portable backup for one vessel's ENTIRE equipment catalog: folders,
 * equipment and all cited procedures. No tasks, AMOS, Services, reminders,
 * profile preferences or PDF/DOCX binaries.
 *
 * Do not call this a "full Reference library backup" before manual binaries
 * and user-written document excerpts are included by a newer archive version.
 */
internal object ReferenceCatalogArchive {
    const val extension = ".navref"
    const val format = "navtasks-reference-catalog"
    private const val version = 1
    const val maxArchiveBytes = 16_000_000
    private const val maxCatalogBytes = 24_000_000
    private const val maxManifestBytes = 12_000

    private fun digest(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes)
            .joinToString("") { "%02x".format(it.toInt() and 0xFF) }

    fun encode(catalog: ReferenceEquipmentCatalog): ByteArray {
        catalog.validate()
        val raw = catalog.toJson().toString().toByteArray(StandardCharsets.UTF_8)
        require(raw.size <= maxCatalogBytes) { "Equipment catalog exceeds backup limit" }
        val manifest = JSONObject()
            .put("format", format)
            .put("schemaVersion", version)
            .put("catalogSize", raw.size)
            .put("sha256", digest(raw))
            .toString().toByteArray(StandardCharsets.UTF_8)
        require(manifest.size <= maxManifestBytes)
        return ByteArrayOutputStream().use { bytes ->
            ZipOutputStream(bytes).use { zip ->
                zip.putNextEntry(ZipEntry("manifest.json"))
                zip.write(manifest)
                zip.closeEntry()
                zip.putNextEntry(ZipEntry("catalog.json"))
                zip.write(raw)
                zip.closeEntry()
            }
            bytes.toByteArray()
        }.also {
            require(it.isNotEmpty() && it.size <= maxArchiveBytes) {
                "Equipment archive exceeds size limit"
            }
        }
    }

    fun decode(bytes: ByteArray): ReferenceEquipmentCatalog {
        require(bytes.isNotEmpty() && bytes.size <= maxArchiveBytes) {
            "Equipment archive exceeds size limit"
        }
        val (manifestBytes, catalogBytes) = ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
            fun readEntry(expected: String, maxSize: Int): ByteArray {
                val entry = zip.nextEntry ?: error("Missing $expected")
                require(!entry.isDirectory && entry.name == expected) {
                    "Unexpected or unsafe archive entry"
                }
                val output = ByteArrayOutputStream()
                val buffer = ByteArray(8192)
                while (true) {
                    val n = zip.read(buffer)
                    if (n < 0) break
                    require(output.size() + n <= maxSize) {
                        "Decompressed Reference entry exceeds size limit"
                    }
                    output.write(buffer, 0, n)
                }
                zip.closeEntry()
                return output.toByteArray()
            }
            val meta = readEntry("manifest.json", maxManifestBytes)
            val data = readEntry("catalog.json", maxCatalogBytes)
            require(zip.nextEntry == null) { "Unexpected files in Reference catalog" }
            meta to data
        }
        val manifest = JSONObject(String(manifestBytes, StandardCharsets.UTF_8))
        require(manifest.length() == 4)
        require(manifest.getString("format") == format &&
            manifest.getInt("schemaVersion") == version) {
            "Unsupported Reference catalog backup format"
        }
        require(manifest.getInt("catalogSize") == catalogBytes.size &&
            manifest.getString("sha256") == digest(catalogBytes)) {
            "Reference catalog backup checksum mismatch"
        }
        return ReferenceEquipmentCatalog.fromJson(JSONObject(
            String(catalogBytes, StandardCharsets.UTF_8)))
    }
}

internal data class ReferenceCatalogImportSummary(
    val folders: Int,
    val equipment: Int,
    val procedures: Int,
    val similarEquipment: Int,
)

internal fun referenceCatalogImportSummary(
    local: ReferenceEquipmentCatalog, imported: ReferenceEquipmentCatalog,
): ReferenceCatalogImportSummary {
    local.validate()
    imported.validate()
    return ReferenceCatalogImportSummary(
        folders = imported.folders.size,
        equipment = imported.equipment.size,
        procedures = imported.equipment.sumOf { it.procedures.size },
        similarEquipment = imported.equipment.count { incoming ->
            local.equipment.any { existing ->
                existing.title.trim().equals(incoming.title.trim(), true) &&
                    existing.model.trim().equals(incoming.model.trim(), true)
            }
        },
    )
}

/** Non-destructive merge: fresh IDs for every folder, device and procedure. */
internal fun referenceMergeCatalogCopies(
    local: ReferenceEquipmentCatalog, imported: ReferenceEquipmentCatalog,
): ReferenceEquipmentCatalog {
    local.validate()
    imported.validate()
    val folderIds = imported.folders.associate { it.id to navReferenceId() }
    val newFolders = imported.folders.map { original ->
        original.copy(
            id = folderIds.getValue(original.id),
            parentId = original.parentId?.let(folderIds::getValue),
        )
    }
    val newEquipment = imported.equipment.map { item ->
        item.copy(
            id = navReferenceId(),
            folderId = item.folderId?.let(folderIds::getValue),
            procedures = item.procedures.map { it.copy(id = navReferenceId()) },
        )
    }
    return local.copy(
        folders = local.folders + newFolders,
        equipment = local.equipment + newEquipment,
    ).also { it.validate() }
}
