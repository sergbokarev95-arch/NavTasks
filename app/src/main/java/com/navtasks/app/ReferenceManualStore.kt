package com.navtasks.app

import android.content.Context
import android.util.AtomicFile
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.zip.ZipFile

/**
 * Private shared storage of ORIGINAL manual binaries, independent of vessel profiles.
 * Import must be invoked from Dispatchers.IO (never on the Compose UI thread).
 * Do not expose import UI until a binary-inclusive export/restore format is available.
 */
internal class ReferenceManualStore(context: Context) {
    private val base = File(context.applicationContext.filesDir, "navtasks-reference/v1/manuals")
    private val originals = File(base, "originals")
    private val atomicCatalog = AtomicFile(File(base, "catalog.json"))
    private val catalogLimit = 8_000_000
    private val fileLimit = 250_000_000L

    fun read(): ReferenceManualCatalog {
        val content = try {
            atomicCatalog.openRead().use { input ->
                val output = ByteArrayOutputStream()
                val buffer = ByteArray(8192)
                while (true) {
                    val n = input.read(buffer)
                    if (n < 0) break
                    require(output.size() + n <= catalogLimit) { "Manual catalog too large" }
                    output.write(buffer, 0, n)
                }
                output.toByteArray()
            }
        } catch (_: FileNotFoundException) {
            return ReferenceManualCatalog()
        }
        return ReferenceManualCatalog.fromJson(JSONObject(
            String(content, StandardCharsets.UTF_8)))
    }

    private fun write(updated: ReferenceManualCatalog) {
        updated.validate()
        read() // Fail closed: do not overwrite an unreadable catalog.
        val content = updated.toJson().toString().toByteArray(StandardCharsets.UTF_8)
        require(content.size <= catalogLimit)
        require(base.mkdirs() || base.isDirectory) { "Could not create manuals storage" }
        val stream = atomicCatalog.startWrite()
        try {
            stream.write(content)
            atomicCatalog.finishWrite(stream)
        } catch (cause: Throwable) {
            atomicCatalog.failWrite(stream)
            throw cause
        }
    }

    fun findOriginal(document: ReferenceManualDocument): File {
        document.validate()
        val extension = when (document.mimeType) {
            ReferenceManualMime.PDF -> "pdf"
            ReferenceManualMime.DOCX -> "docx"
            else -> throw IllegalArgumentException("Unsupported manual type")
        }
        return File(originals, "${document.sha256}.$extension").also {
            require(it.isFile && it.length() == document.bytes) {
                "Original manual is missing or incomplete"
            }
        }
    }

    /** Streams a complete shared manual backup including ORIGINAL PDF/DOCX bytes. */
    fun exportArchive(output: java.io.OutputStream) {
        val snapshot = read()
        ReferenceManualArchive.export(snapshot, ::findOriginal, output)
    }

    /**
     * Restore a previously stage-validated archive, adding copies instead of
     * overwriting existing user folders and document records.
     *
     * Runs on Dispatchers.IO. Files are committed before the atomic catalog;
     * if metadata writing fails the old catalog remains intact, leaving only
     * harmless unreferenced blobs. The staging handle is closed by the caller.
     */
    fun restoreArchive(staged: StagedManualArchive) {
        val previous = read()
        val merged = referenceMergeManualCatalogCopies(previous, staged.catalog)
        require(originals.mkdirs() || originals.isDirectory)
        val unique = staged.catalog.documents.distinctBy(ReferenceManualArchive::blobName)
        for (manual in unique) {
            val archiveKey = ReferenceManualArchive.blobName(manual)
            val stagedFile = staged.originals[archiveKey]
                ?: throw IllegalArgumentException("Missing verified original in staging")
            require(stagedFile.isFile && stagedFile.length() == manual.bytes)
            val suffix = if (manual.mimeType == ReferenceManualMime.PDF) "pdf" else "docx"
            val target = File(originals, "${manual.sha256}.$suffix")
            if (target.exists()) {
                // Never silently overwrite a known original. Verify that an
                // existing identical-name blob is byte-for-byte the same.
                require(target.isFile && target.length() == manual.bytes &&
                    sha256File(target) == manual.sha256) {
                    "Existing original manual does not match its checksum"
                }
                continue
            }
            val temporary = File.createTempFile("verified-import-", ".part", originals)
            try {
                stagedFile.inputStream().use { source ->
                    temporary.outputStream().use { dest -> source.copyTo(dest, 64 * 1024) }
                }
                require(temporary.length() == manual.bytes &&
                    sha256File(temporary) == manual.sha256) {
                    "Restored original manual checksum mismatch"
                }
                check(temporary.renameTo(target)) { "Could not commit restored original" }
            } finally {
                temporary.delete()
            }
        }
        write(merged)
    }

    private fun sha256File(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(64 * 1024)
            while (true) {
                val n = input.read(buffer)
                if (n < 0) break
                if (n > 0) digest.update(buffer, 0, n)
            }
        }
        return digest.digest().joinToString("") {
            "%02x".format(it.toInt() and 0xFF)
        }
    }

    /**
     * Streams a picked original into app-private, non-cache storage.
     * Checksum-based filenames prevent traversal, accidental replacing and duplicate bytes.
     * Crash before the final metadata commit leaves at most an orphaned file,
     * not a document card that points to a partial binary.
     */
    fun importOriginal(
        input: InputStream,
        title: String,
        originalFilename: String,
        mimeType: String,
        sectionId: String = NavReferenceSections.PERSONAL,
        folderId: String? = null,
    ): ReferenceManualDocument {
        require(mimeType in ReferenceManualMime.allowed) { "Unsupported manual file type" }
        val previous = read()
        val candidate = ReferenceManualDocument(
            title = title.trim(), sectionId = sectionId, folderId = folderId,
            originalFilename = originalFilename.trim(), mimeType = mimeType,
            sha256 = "0".repeat(64), bytes = 1,
        )
        candidate.validate()
        require(folderId == null || previous.folders.any {
            it.id == folderId && it.sectionId == sectionId
        }) { "Unknown manual folder" }
        require(originals.mkdirs() || originals.isDirectory) {
            "Cannot create manuals files directory"
        }

        val temp = File.createTempFile("incoming-", ".part", originals)
        try {
            val digest = MessageDigest.getInstance("SHA-256")
            var total = 0L
            temp.outputStream().buffered().use { output ->
                val buffer = ByteArray(64 * 1024)
                while (true) {
                    val n = input.read(buffer)
                    if (n < 0) break
                    total += n
                    require(total in 1..fileLimit) { "Manual exceeds 250 MB" }
                    digest.update(buffer, 0, n)
                    output.write(buffer, 0, n)
                }
            }
            require(total > 0)
            validateReferenceManualSignature(temp, mimeType)
            val hash = digest.digest().joinToString("") {
                "%02x".format(it.toInt() and 0xFF)
            }
            val document = candidate.copy(sha256 = hash, bytes = total)
            document.validate()
            val extension = if (mimeType == ReferenceManualMime.PDF) "pdf" else "docx"
            val final = File(originals, "$hash.$extension")
            if (!final.exists()) {
                check(temp.renameTo(final)) { "Could not store manual file" }
            } else {
                require(final.isFile && final.length() == total) {
                    "Hash matched but stored file is incomplete"
                }
            }
            val updated = previous.copy(documents = previous.documents + document)
            write(updated)
            return document
        } finally {
            // No rollback removes already stored originals; they may be referenced elsewhere.
            temp.delete()
        }
    }
}

/** Validate content without executing macros, Office scripts or external references. */
internal fun validateReferenceManualSignature(file: File, mimeType: String) {
    require(file.isFile && file.length() > 0)
    val signature = file.inputStream().use { input ->
        val bytes = ByteArray(8)
        val count = input.read(bytes)
        bytes.copyOf(count.coerceAtLeast(0))
    }
    when (mimeType) {
        ReferenceManualMime.PDF -> require(
            signature.size >= 5 && signature.copyOfRange(0, 5)
                .contentEquals("%PDF-".toByteArray(StandardCharsets.US_ASCII))
        ) { "Not a PDF file" }
        ReferenceManualMime.DOCX -> {
            require(signature.size >= 4 &&
                signature[0] == 'P'.code.toByte() &&
                signature[1] == 'K'.code.toByte() &&
                signature[2] == 3.toByte() && signature[3] == 4.toByte()
            ) { "Not a DOCX ZIP package" }
            ZipFile(file).use { zip ->
                require(zip.size() in 2..20000) { "Invalid DOCX package entry count" }
                require(zip.getEntry("[Content_Types].xml") != null &&
                    zip.getEntry("word/document.xml") != null) {
                    "Invalid DOCX Word document package"
                }
                val entries = zip.entries()
                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    require(!entry.name.startsWith("/") &&
                        !entry.name.contains("..") &&
                        !entry.name.contains('\\')) {
                        "Invalid DOCX ZIP entry path"
                    }
                    require(entry.size < 500_000_000L) {
                        "DOCX entry unexpectedly large"
                    }
                }
            }
        }
        else -> throw IllegalArgumentException("Unsupported manual type")
    }
}
