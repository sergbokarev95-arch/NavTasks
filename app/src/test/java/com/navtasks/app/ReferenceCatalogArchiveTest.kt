package com.navtasks.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class ReferenceCatalogArchiveTest {
    private fun catalog() = ReferenceEquipmentCatalog(
        folders = listOf(
            ReferenceFolder("safety", "Safety"),
            ReferenceFolder("gas", "Gas detection", "safety"),
        ),
        equipment = listOf(ReferenceEquipment(
            id = "detector", title = "Газоанализатор", model = "X3",
            serialNumber = "SERIAL-SAVED-IN-EXPLICIT-BACKUP",
            folderId = "gas",
            procedures = listOf(ReferenceProcedure(
                id = "calibration",
                title = "Годовая калибровка",
                intervalDescription = "По мануалу: ежегодно",
                excerpt = "Источник требует проверки в указанных условиях.",
                source = ReferenceSource("Gas detector manual", 51, "4.2", "Rev3"),
                instructions = "См. условия и меры безопасности.",
            )),
        )),
    )

    @Test fun backupPreservesCompleteTreeSourcesAndEquipment() {
        val original = catalog()
        val restored = ReferenceCatalogArchive.decode(ReferenceCatalogArchive.encode(original))
        assertEquals(original, restored)
        assertEquals("gas", restored.equipment.single().folderId)
        assertEquals("safety", restored.folders.first { it.id == "gas" }.parentId)
        assertEquals(51, restored.equipment.single().procedures.single().source.page)
        assertEquals("SERIAL-SAVED-IN-EXPLICIT-BACKUP", restored.equipment.single().serialNumber)
        val raw = restored.toJson().toString()
        for (forbidden in listOf("taskId", "workOrderId", "serviceId", "runKey",
            "deadline", "notification", "completionState")) {
            assertFalse(forbidden, raw.contains(forbidden))
        }
    }

    @Test fun mergedBackupNeverReplacesExistingDataOrBreaksNestedPaths() {
        val original = catalog()
        val merged = referenceMergeCatalogCopies(original, original)
        assertEquals(4, merged.folders.size)
        assertEquals(2, merged.equipment.size)
        assertEquals(original.folders, merged.folders.take(2))
        assertEquals(original.equipment.first(), merged.equipment.first())
        assertEquals(2, merged.equipment.map { it.id }.toSet().size)
        assertEquals(2, merged.equipment.flatMap { it.procedures }.map { it.id }.toSet().size)
        val importedDevice = merged.equipment.last()
        val importedFolder = merged.folders.first { it.id == importedDevice.folderId }
        assertEquals("Gas detection", importedFolder.title)
        assertTrue(merged.folders.any { it.id == importedFolder.parentId && it.title == "Safety" })
        assertEquals(1, referenceCatalogImportSummary(original, original).similarEquipment)
        assertEquals(2, referenceCatalogImportSummary(original, original).folders)
    }

    @Test fun independentBlankCatalogCanRecoverAllUserRecords() {
        val recovered = referenceMergeCatalogCopies(ReferenceEquipmentCatalog(),
            ReferenceCatalogArchive.decode(ReferenceCatalogArchive.encode(catalog())))
        assertEquals(2, recovered.folders.size)
        assertEquals(1, recovered.equipment.size)
        recovered.validate()
    }

    private fun makeZip(entries: List<Pair<String, ByteArray>>): ByteArray =
        ByteArrayOutputStream().use { output ->
            ZipOutputStream(output).use { zip ->
                entries.forEach { (name, content) ->
                    zip.putNextEntry(ZipEntry(name))
                    zip.write(content)
                    zip.closeEntry()
                }
            }
            output.toByteArray()
        }

    @Test fun zipSlipAndUnknownExtraFilesRejected() {
        val original = ReferenceCatalogArchive.encode(catalog())
        val contents = ZipInputStream(original.inputStream()).use { zip ->
            val all = mutableListOf<Pair<String, ByteArray>>()
            while (true) {
                val entry = zip.nextEntry ?: break
                all += entry.name to zip.readBytes()
                zip.closeEntry()
            }
            all
        }
        assertTrue(runCatching { ReferenceCatalogArchive.decode(
            makeZip(listOf("../manifest.json" to contents[0].second,
                "catalog.json" to contents[1].second))
        ) }.isFailure)
        assertTrue(runCatching { ReferenceCatalogArchive.decode(
            makeZip(contents + ("../../anything" to "bad".toByteArray()))
        ) }.isFailure)
    }

    @Test fun checksumTamperingAndMalformedBytesRejected() {
        val original = ReferenceCatalogArchive.encode(catalog())
        val entries = ZipInputStream(original.inputStream()).use { zip ->
            val all = mutableListOf<Pair<String, ByteArray>>()
            while (true) {
                val entry = zip.nextEntry ?: break
                all += entry.name to zip.readBytes()
                zip.closeEntry()
            }
            all
        }
        assertTrue(runCatching {
            ReferenceCatalogArchive.decode(makeZip(listOf(
                entries[0], "catalog.json" to "{\"invalid\":true}".toByteArray())))
        }.isFailure)
        assertTrue(runCatching {
            ReferenceCatalogArchive.decode(byteArrayOf(0, 1, 2))
        }.isFailure)
    }

    @Test fun compressedBombRefusedBeforeMemoryExpansion() {
        val bomb = "X".repeat(24_000_050).toByteArray()
        val raw = makeZip(listOf(
            "manifest.json" to "{}".toByteArray(),
            "catalog.json" to bomb,
        ))
        assertTrue(runCatching { ReferenceCatalogArchive.decode(raw) }.isFailure)
    }
}
