package com.navtasks.app

import java.io.ByteArrayOutputStream
import java.io.Closeable
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import org.json.JSONObject

/**
 * Portable OFFLINE original-manual archive. Uses streaming ZIP; PDFs and DOCX
 * retain byte-for-byte fidelity. Does NOT contain vessel equipment, tasks, AMOS,
 * Services, notifications or user profile credentials.
 *
 * Import is staged into a private temp directory; caller explicitly commits
 * the complete catalog only AFTER all hashes and document packages pass.
 */
internal object ReferenceManualArchive {
    const val extension = ".navlib"
    private const val metadataName = "manuals.json"
    private const val metadataMax = 8_000_000
    private const val maxDocBytes = 250_000_000L
    private const val maxAggregateBytes = 1_000_000_000L

    internal fun blobName(manual: ReferenceManualDocument): String {
        manual.validate()
        val suffix = if (manual.mimeType == ReferenceManualMime.PDF) "pdf" else "docx"
        return "originals/${manual.sha256}.$suffix"
    }

    private fun sha256(): MessageDigest = MessageDigest.getInstance("SHA-256")
    private fun hex(digest: ByteArray): String =
        digest.joinToString("") { "%02x".format(it.toInt() and 0xFF) }

    fun export(
        catalog: ReferenceManualCatalog,
        fileFor: (ReferenceManualDocument) -> File,
        output: OutputStream,
    ) {
        catalog.validate()
        val metadata = catalog.toJson().toString().toByteArray(StandardCharsets.UTF_8)
        require(metadata.size <= metadataMax)
        val unique = catalog.documents.distinctBy(::blobName)
        var aggregate = 0L
        // Close zip at the end: a failed export must be deleted by SAF caller,
        // never reported as a valid backup.
        ZipOutputStream(output).use { zip ->
            zip.putNextEntry(ZipEntry(metadataName))
            zip.write(metadata)
            zip.closeEntry()
            for (manual in unique) {
                val file = fileFor(manual)
                require(file.isFile && file.length() == manual.bytes) {
                    "Missing or incomplete original manual"
                }
                val checksum = sha256()
                var count = 0L
                zip.putNextEntry(ZipEntry(blobName(manual)))
                file.inputStream().use { source ->
                    val buffer = ByteArray(64 * 1024)
                    while (true) {
                        val n = source.read(buffer)
                        if (n < 0) break
                        if (n == 0) continue
                        count += n
                        aggregate += n
                        require(count <= maxDocBytes &&
                            aggregate <= maxAggregateBytes) { "Manual archive limit exceeded" }
                        checksum.update(buffer, 0, n)
                        zip.write(buffer, 0, n)
                    }
                }
                zip.closeEntry()
                require(count == manual.bytes && hex(checksum.digest()) == manual.sha256) {
                    "Original manual checksum changed during export"
                }
            }
        }
    }

    /**
     * A process crash can leave a large, otherwise inaccessible staging directory.
     * Only remove OUR precisely named archive staging folders older than seven
     * days, never recently opened previews or unrelated application files.
     */
    internal fun cleanupAbandonedStages(
        parentDirectory: File, nowMillis: Long = System.currentTimeMillis(),
    ): Int {
        val cutoff = nowMillis - 7L * 24 * 60 * 60 * 1000
        var cleaned = 0
        for (candidate in parentDirectory.listFiles().orEmpty()) {
            if (candidate.isDirectory &&
                candidate.name.startsWith("reference-manuals-") &&
                candidate.name.endsWith(".staging") &&
                candidate.lastModified() < cutoff &&
                candidate.deleteRecursively()
            ) cleaned++
        }
        return cleaned
    }

    /**
     * Caller must close the returned staging handle. zip entry paths are
     * canonical, based ONLY on validated stored digests, never display names.
     */
    fun stage(input: InputStream, parentDirectory: File): StagedManualArchive {
        require(parentDirectory.mkdirs() || parentDirectory.isDirectory)
        cleanupAbandonedStages(parentDirectory)
        val stage = File.createTempFile("reference-manuals-", ".staging", parentDirectory)
        require(stage.delete() && stage.mkdir()) { "Could not prepare archive staging" }
        try {
            val files = mutableMapOf<String, File>()
            val catalog = ZipInputStream(input).use { zip ->
                val first = zip.nextEntry ?: error("Missing manual archive metadata")
                require(!first.isDirectory && first.name == metadataName)
                val metadata = readBounded(zip, metadataMax.toLong())
                zip.closeEntry()
                val parsed = ReferenceManualCatalog.fromJson(JSONObject(
                    String(metadata, StandardCharsets.UTF_8)))
                val expected = parsed.documents.groupBy(::blobName)
                var aggregate = 0L
                while (true) {
                    val entry = zip.nextEntry ?: break
                    require(!entry.isDirectory && entry.name in expected &&
                        entry.name !in files) { "Unrecognized or duplicate manual entry" }
                    val reference = expected.getValue(entry.name).first()
                    val suffix = if (reference.mimeType == ReferenceManualMime.PDF) "pdf" else "docx"
                    val temporary = File.createTempFile("manual-", ".$suffix", stage)
                    val checksum = sha256()
                    var count = 0L
                    temporary.outputStream().use { target ->
                        val buffer = ByteArray(64 * 1024)
                        while (true) {
                            val n = zip.read(buffer)
                            if (n < 0) break
                            if (n == 0) continue
                            count += n
                            aggregate += n
                            require(count <= maxDocBytes &&
                                aggregate <= maxAggregateBytes) {
                                "Reference archive decompression limit exceeded"
                            }
                            checksum.update(buffer, 0, n)
                            target.write(buffer, 0, n)
                        }
                    }
                    require(count == reference.bytes &&
                        hex(checksum.digest()) == reference.sha256) {
                        "Manual original corrupted or missing bytes"
                    }
                    validateReferenceManualSignature(temporary, reference.mimeType)
                    files[entry.name] = temporary
                    zip.closeEntry()
                }
                require(files.keys == expected.keys) { "Missing original manuals in archive" }
                parsed
            }
            return StagedManualArchive(catalog, files.toMap(), stage)
        } catch (cause: Throwable) {
            stage.deleteRecursively()
            throw cause
        }
    }

    private fun readBounded(input: InputStream, limit: Long): ByteArray {
        val bytes = ByteArrayOutputStream()
        val buffer = ByteArray(8192)
        var count = 0L
        while (true) {
            val n = input.read(buffer)
            if (n < 0) break
            count += n
            require(count <= limit) { "Reference archive metadata too large" }
            bytes.write(buffer, 0, n)
        }
        return bytes.toByteArray()
    }
}

internal class StagedManualArchive(
    val catalog: ReferenceManualCatalog,
    val originals: Map<String, File>,
    private val directory: File,
) : Closeable {
    override fun close() {
        directory.deleteRecursively()
    }
}

internal fun referenceMergeManualCatalogCopies(
    local: ReferenceManualCatalog,
    incoming: ReferenceManualCatalog,
): ReferenceManualCatalog {
    local.validate()
    incoming.validate()
    val folderIds = incoming.folders.associate { it.id to navReferenceId() }
    val folders = incoming.folders.map { old ->
        old.copy(
            id = folderIds.getValue(old.id),
            parentId = old.parentId?.let { folderIds.getValue(it) },
        )
    }
    val documents = incoming.documents.map { doc ->
        doc.copy(
            id = navReferenceId(),
            folderId = doc.folderId?.let { folderIds.getValue(it) },
        )
    }
    val excerpts = incoming.excerpts.map { excerpt ->
        excerpt.copy(
            id = navReferenceId(),
            folderId = excerpt.folderId?.let { folderIds.getValue(it) },
        )
    }
    return local.copy(
        folders = local.folders + folders,
        documents = local.documents + documents,
        excerpts = local.excerpts + excerpts,
    ).also { it.validate() }
}
