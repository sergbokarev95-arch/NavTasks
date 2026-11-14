package com.navtasks.app

import org.json.JSONArray
import org.json.JSONObject
import java.io.Closeable
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/** v2 .navref: one equipment card AND its original, SHA-verified PDF/DOCX files.
 * Existing metadata-only v1 remains readable, with an explicit missing-file warning.
 * Files are always streamed on Dispatchers.IO, never buffered into RAM.
 */
internal object ReferenceEquipmentPortableV2 {
    private const val MANIFEST = "manifest.json"
    private const val MAX_MANIFEST = 16_000_000L
    private const val MAX_SINGLE = 250_000_000L
    private const val MAX_TOTAL = 1_000_000_000L

    private data class Descriptor(val sha: String, val mime: String, val bytes: Long) {
        fun path(): String {
            require(sha.matches(Regex("[0-9a-f]{64}")))
            require(mime in ReferenceManualMime.allowed)
            require(bytes in 1..MAX_SINGLE)
            val ext = if (mime == ReferenceManualMime.PDF) "pdf" else "docx"
            return "originals/$sha.$ext"
        }
        fun json(): JSONObject = JSONObject().put("sha256", sha)
            .put("mimeType", mime).put("bytes", bytes)
    }
    private fun digest() = MessageDigest.getInstance("SHA-256")
    private fun hex(raw: ByteArray) = raw.joinToString("") {
        "%02x".format(it.toInt() and 0xff)
    }
    private fun sha(bytes: ByteArray) = hex(digest().digest(bytes))
    private fun keys(equipment: ReferenceEquipment): Set<Pair<String, String>> {
        equipment.validate()
        return equipment.procedures.mapNotNull { procedure ->
            procedure.attachmentSha256?.let { it to requireNotNull(procedure.attachmentMimeType) }
        }.toSet()
    }

    fun export(equipment: ReferenceEquipment,
        catalog: List<ReferenceManualDocument>,
        findOriginal: (ReferenceManualDocument) -> File,
        output: OutputStream,
    ) {
        val portable = equipment.copy(folderId = null, serialNumber = "")
        val docs = keys(portable).map { (hash, mime) ->
            catalog.firstOrNull { it.sha256 == hash && it.mimeType == mime }
                ?: error("An attached PDF/DOCX is missing; restore the .navlib original")
        }
        require(docs.size <= 200 && docs.sumOf { it.bytes } <= MAX_TOTAL)
        val json = portable.toJson().toString()
        val raw = json.toByteArray(StandardCharsets.UTF_8)
        require(raw.size <= MAX_MANIFEST)
        val records = docs.map { Descriptor(it.sha256, it.mimeType, it.bytes) }
        records.forEach { it.path() }
        val manifest = JSONObject()
            .put("format", ReferenceEquipmentExchange.format)
            .put("schemaVersion", 2)
            .put("equipmentJson", json)
            .put("sha256", sha(raw))
            .put("files", JSONArray().apply { records.forEach { put(it.json()) } })
            .toString().toByteArray(StandardCharsets.UTF_8)
        require(manifest.size <= MAX_MANIFEST)
        ZipOutputStream(output).use { zip ->
            zip.putNextEntry(ZipEntry(MANIFEST))
            zip.write(manifest)
            zip.closeEntry()
            docs.forEachIndexed { index, manual ->
                val descriptor = records[index]
                val original = findOriginal(manual)
                require(original.isFile && original.length() == descriptor.bytes)
                val checksum = digest()
                var read = 0L
                zip.putNextEntry(ZipEntry(descriptor.path()))
                original.inputStream().use { source ->
                    val buffer = ByteArray(64 * 1024)
                    while (true) {
                        val n = source.read(buffer)
                        if (n < 0) break
                        if (n == 0) continue
                        read += n
                        require(read <= descriptor.bytes)
                        checksum.update(buffer, 0, n)
                        zip.write(buffer, 0, n)
                    }
                }
                zip.closeEntry()
                require(read == descriptor.bytes && hex(checksum.digest()) == descriptor.sha) {
                    "Source attachment changed or is corrupted"
                }
            }
        }
    }

    /**
     * Build the complete v2 package in private cache and independently read it
     * back before copying to a user-selected document. Never report success
     * merely because a metadata-only ZIP was written.
     *
     * Caller owns the returned temporary file and must delete it in finally.
     */
    fun prepareVerifiedExport(
        equipment: ReferenceEquipment,
        catalog: List<ReferenceManualDocument>,
        findOriginal: (ReferenceManualDocument) -> File,
        cacheDir: File,
    ): Pair<File, Int> {
        require(cacheDir.isDirectory || cacheDir.mkdirs())
        val temp = File.createTempFile("navref-verified-", ".navref", cacheDir)
        try {
            temp.outputStream().use { export(equipment, catalog, findOriginal, it) }
            val count = temp.inputStream().use { source ->
                stage(source, cacheDir).use { staged ->
                    require(!staged.legacyMissingAttachments) { "Only v2 is exportable" }
                    require(staged.originals.size == keys(equipment).size) {
                        "Some original PDF/Word files were not included"
                    }
                    require(staged.equipment.procedures.size == equipment.procedures.size) {
                        "Equipment archive changed during verification"
                    }
                    staged.originals.size
                }
            }
            return temp to count
        } catch (e: Throwable) {
            temp.delete()
            throw e
        }
    }

    /** Stage and verify everything before allowing an import confirmation. */
    fun stage(input: InputStream, cache: File): StagedReferenceEquipment {
        require(cache.mkdirs() || cache.isDirectory)
        val dir = File.createTempFile("reference-equipment-", ".staging", cache)
        require(dir.delete() && dir.mkdir())
        try {
            lateinit var equipment: ReferenceEquipment
            val originals = mutableListOf<StagedReferenceOriginal>()
            var legacyMissing = false
            ZipInputStream(input).use { zip ->
                val first = zip.nextEntry ?: error("Missing equipment manifest")
                require(!first.isDirectory && first.name == MANIFEST)
                val manifest = JSONObject(String(readBounded(zip, MAX_MANIFEST),
                    StandardCharsets.UTF_8))
                zip.closeEntry()
                val version = manifest.getInt("schemaVersion")
                val expectedFieldCount = if (version == 1) 4 else 5
                require(manifest.getString("format") == ReferenceEquipmentExchange.format &&
                    version in 1..2 && manifest.length() == expectedFieldCount)
                val raw = manifest.getString("equipmentJson")
                require(sha(raw.toByteArray(StandardCharsets.UTF_8)) ==
                    manifest.getString("sha256")) { "Equipment metadata checksum mismatch" }
                equipment = ReferenceEquipment.fromJson(JSONObject(raw))
                val wanted = keys(equipment)
                if (version == 1) {
                    require(zip.nextEntry == null)
                    legacyMissing = wanted.isNotEmpty()
                } else {
                    val array = manifest.getJSONArray("files")
                    require(array.length() <= 200)
                    val expected = linkedMapOf<String, Descriptor>()
                    var declared = 0L
                    for (index in 0 until array.length()) {
                        val item = array.getJSONObject(index)
                        require(item.length() == 3)
                        val descriptor = Descriptor(item.getString("sha256"),
                            item.getString("mimeType"), item.getLong("bytes"))
                        val path = descriptor.path()
                        require(expected.put(path, descriptor) == null)
                        declared += descriptor.bytes
                        require(declared <= MAX_TOTAL)
                    }
                    require(expected.values.map { it.sha to it.mime }.toSet() == wanted) {
                        "Equipment and attached files do not match"
                    }
                    var total = 0L
                    while (true) {
                        val entry = zip.nextEntry ?: break
                        require(!entry.isDirectory && entry.name in expected &&
                            originals.none { it.archiveName == entry.name }) {
                            "Unexpected or duplicate attached file"
                        }
                        val descriptor = expected.getValue(entry.name)
                        val suffix = if (descriptor.mime == ReferenceManualMime.PDF)
                            ".pdf" else ".docx"
                        val temp = File.createTempFile("attached-", suffix, dir)
                        var length = 0L
                        val checksum = digest()
                        temp.outputStream().use { target ->
                            val buffer = ByteArray(64 * 1024)
                            while (true) {
                                val n = zip.read(buffer)
                                if (n < 0) break
                                if (n == 0) continue
                                length += n
                                total += n
                                require(length <= descriptor.bytes && total <= MAX_TOTAL) {
                                    "Attachment too large"
                                }
                                checksum.update(buffer, 0, n)
                                target.write(buffer, 0, n)
                            }
                        }
                        require(length == descriptor.bytes &&
                            hex(checksum.digest()) == descriptor.sha) {
                            "Attachment checksum mismatch"
                        }
                        validateReferenceManualSignature(temp, descriptor.mime)
                        val fileName = equipment.procedures.first {
                            it.attachmentSha256 == descriptor.sha &&
                                it.attachmentMimeType == descriptor.mime
                        }.attachmentFilename
                        originals += StagedReferenceOriginal(
                            entry.name, descriptor.sha, descriptor.mime, fileName, temp)
                        zip.closeEntry()
                    }
                    require(originals.map { it.archiveName }.toSet() == expected.keys) {
                        "Missing attached PDF/DOCX"
                    }
                }
            }
            return StagedReferenceEquipment(equipment, originals, legacyMissing, dir)
        } catch (failure: Throwable) {
            dir.deleteRecursively()
            throw failure
        }
    }
    private fun readBounded(input: InputStream, max: Long): ByteArray {
        val out = java.io.ByteArrayOutputStream()
        val buffer = ByteArray(8192)
        var nTotal = 0L
        while (true) {
            val n = input.read(buffer)
            if (n < 0) break
            if (n == 0) continue
            nTotal += n
            require(nTotal <= max) { "Equipment manifest too large" }
            out.write(buffer, 0, n)
        }
        return out.toByteArray()
    }
}

internal data class StagedReferenceOriginal(
    val archiveName: String,
    val sha256: String,
    val mimeType: String,
    val filename: String,
    val file: File,
)

internal class StagedReferenceEquipment(
    val equipment: ReferenceEquipment,
    val originals: List<StagedReferenceOriginal>,
    val legacyMissingAttachments: Boolean,
    private val directory: File,
) : Closeable {
    override fun close() { directory.deleteRecursively() }
}
