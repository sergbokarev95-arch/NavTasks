package com.navtasks.app

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class ReferenceEquipmentExchangeTest {
    private fun sample(): ReferenceEquipment = ReferenceEquipment(
        id = "gas-detector-1",
        title = "Газоанализатор",
        manufacturer = "Test Manufacturer",
        model = "GD-1",
        serialNumber = "PRIVATE-SERIAL",
        location = "Cargo control room",
        folderId = "ship-safety",
        procedures = listOf(
            ReferenceProcedure(
                id = "gas-check",
                title = "Проверка",
                intervalDescription = "раз в месяц",
                excerpt = "Проверяйте согласно пункту 4.1 руководства.",
                source = ReferenceSource("Gas detector manual", 41, "4.1", "Rev 2"),
                instructions = "Проверьте показания согласно инструкции.",
            ),
            ReferenceProcedure(
                id = "gas-calibration",
                title = "Калибровка",
                intervalDescription = "ежегодно",
                excerpt = "Требование о ежегодной калибровке (пример).",
                source = ReferenceSource("Gas detector manual", 62),
            ),
        ),
    )

    @Test fun oneEquipmentExportsWithInstructionsAndCitationWithoutSerialByDefault() {
        val original = sample()
        val portable = ReferenceEquipmentExchange.decode(
            ReferenceEquipmentExchange.encode(original))
        assertEquals(original.title, portable.title)
        assertEquals("", portable.serialNumber)
        assertEquals(null, portable.folderId)
        assertEquals(2, portable.procedures.size)
        assertEquals("4.1", portable.procedures.first().source.section)
        assertEquals(41, portable.procedures.first().source.page)
        assertEquals(original.procedures.first().excerpt, portable.procedures.first().excerpt)
        assertEquals(original.procedures.first().instructions, portable.procedures.first().instructions)
        assertEquals("PRIVATE-SERIAL", original.serialNumber) // no mutation to sender
        assertFalse(portable.toJson().toString().contains("taskId"))
        assertFalse(portable.toJson().toString().contains("workOrderId"))
        assertFalse(portable.toJson().toString().contains("schedule"))
    }

    @Test fun exportingSerialNumberRequiresOptIn() {
        val equipped = ReferenceEquipmentExchange.decode(
            ReferenceEquipmentExchange.encode(sample(), includeSerialNumber = true))
        assertEquals("PRIVATE-SERIAL", equipped.serialNumber)
    }

    @Test fun importReportsDuplicatesWithoutOverwritingAnything() {
        val input = sample()
        assertEquals(ReferenceImportConflict.NONE, referenceImportConflict(input, emptyList()))
        assertEquals(ReferenceImportConflict.SAME_ID, referenceImportConflict(input, listOf(input)))
        val local = input.copy(id = "other-id")
        assertEquals(ReferenceImportConflict.POSSIBLE_DUPLICATE,
            referenceImportConflict(input, listOf(local)))
    }

    @Test fun missingExcerptOrSourceIsRejectedInsteadOfCreatingFakeSchedule() {
        val original = sample()
        assertTrue(runCatching {
            original.copy(procedures = listOf(original.procedures.first().copy(excerpt = "")))
                .validate()
        }.isFailure)
        assertTrue(runCatching {
            original.copy(procedures = listOf(original.procedures.first().copy(
                source = ReferenceSource("")))).validate()
        }.isFailure)
        assertTrue(runCatching {
            ReferenceProcedure.fromJson(JSONObject()
                .put("id", "test")
                .put("title", "fake")
                .put("excerpt", "manual text")
                .put("source", ReferenceSource("manual").toJson())
                .put("taskId", "not-allowed"))
        }.isFailure)
    }

    @Test fun nestedFoldersAreFlexibleButCyclesAndMissingParentsRejected() {
        validateReferenceFolders(listOf(
            ReferenceFolder("a", "Safety"),
            ReferenceFolder("b", "Cargo", "a"),
            ReferenceFolder("c", "Gas detectors", "b"),
        ))
        assertTrue(runCatching { validateReferenceFolders(listOf(
            ReferenceFolder("a", "A", "b"),
            ReferenceFolder("b", "B", "a"),
        )) }.isFailure)
        assertTrue(runCatching { validateReferenceFolders(listOf(
            ReferenceFolder("a", "A", "missing"),
        )) }.isFailure)
    }

    private fun archiveWith(name: String, content: String): ByteArray =
        ByteArrayOutputStream().use { bytes ->
            ZipOutputStream(bytes).use { zip ->
                zip.putNextEntry(ZipEntry(name))
                zip.write(content.toByteArray(Charsets.UTF_8))
                zip.closeEntry()
            }
            bytes.toByteArray()
        }

    @Test fun unsafePathsUnknownEntriesAndInvalidArchivesAreRejected() {
        val archive = ReferenceEquipmentExchange.encode(sample())
        assertTrue(runCatching { ReferenceEquipmentExchange.decode(byteArrayOf(1, 2, 3)) }.isFailure)
        assertTrue(runCatching { ReferenceEquipmentExchange.decode(archiveWith(
            "../manifest.json", "{}")) }.isFailure)
        assertTrue(runCatching { ReferenceEquipmentExchange.decode(archiveWith(
            "other.json", "{}")) }.isFailure)
    }

    @Test fun modifiedManifestFailsDigestValidation() {
        val source = ReferenceEquipmentExchange.encode(sample())
        val extracted = ZipInputStream(source.inputStream()).use { zip ->
            assertEquals("manifest.json", zip.nextEntry.name)
            zip.readBytes().toString(Charsets.UTF_8)
        }
        val modified = JSONObject(extracted).put("sha256", "a".repeat(64)).toString()
        assertTrue(runCatching {
            ReferenceEquipmentExchange.decode(archiveWith("manifest.json", modified))
        }.isFailure)
    }

    @Test fun giantZipEntryRejectedBeforeUnboundedAllocation() {
        val enormous = "x".repeat(16_000_010)
        assertTrue(runCatching {
            ReferenceEquipmentExchange.decode(archiveWith("manifest.json", enormous))
        }.isFailure)
    }
}
