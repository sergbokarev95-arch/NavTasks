package com.navtasks.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pure presentation/catalog edits. Existing scheduling models are never constructed. */
class ReferenceEquipmentInteractionTest {
    private val source = ReferenceSource("Device manual", 20, "3.4", "2026")
    private fun procedure() = ReferenceProcedure(
        id = "weekly-check", title = "Check sensor",
        intervalDescription = "Every month", excerpt = "Sample source requirement",
        source = source, instructions = "Inspect sensor and record result externally",
    )

    @Test fun createRenameAndDeleteNestedFolderWithoutTouchingEquipment() {
        var catalog = ReferenceEquipmentCatalog()
        catalog = referenceSaveFolder(catalog, ReferenceFolder("safety", "Safety"))
        catalog = referenceSaveFolder(catalog, ReferenceFolder("cargo", "Cargo", "safety"))
        assertEquals(2, catalog.folders.size)
        assertTrue(runCatching { referenceDeleteEmptyFolder(catalog, "safety") }.isFailure)
        catalog = referenceSaveFolder(catalog, ReferenceFolder("cargo", "Cargo operations", "safety"))
        assertEquals("Cargo operations", catalog.folders.first { it.id == "cargo" }.title)
        catalog = referenceDeleteEmptyFolder(catalog, "cargo")
        assertEquals(listOf("safety"), catalog.folders.map { it.id })
        catalog = referenceDeleteEmptyFolder(catalog, "safety")
        assertTrue(catalog.folders.isEmpty())
    }

    @Test fun equipmentAndProcedureEditsRetainStableIdentifiers() {
        var catalog = ReferenceEquipmentCatalog()
        val equipment = ReferenceEquipment(id = "detector-1", title = "Gas detector")
        catalog = referenceSaveEquipment(catalog, equipment)
        val withProcedure = referenceSaveProcedure(equipment, procedure())
        catalog = referenceSaveEquipment(catalog, withProcedure)
        assertEquals(1, catalog.equipment.size)
        assertEquals("weekly-check", catalog.equipment.single().procedures.single().id)
        val changed = referenceSaveProcedure(withProcedure, procedure().copy(title = "Sensor inspection"))
        catalog = referenceSaveEquipment(catalog, changed)
        assertEquals(1, catalog.equipment.single().procedures.size)
        assertEquals("Sensor inspection", catalog.equipment.single().procedures.single().title)
        catalog = referenceSaveEquipment(catalog,
            referenceRemoveProcedure(catalog.equipment.single(), "weekly-check"))
        assertTrue(catalog.equipment.single().procedures.isEmpty())
        catalog = referenceDeleteEquipment(catalog, "detector-1")
        assertTrue(catalog.equipment.isEmpty())
    }

    @Test fun searchFindsTextInManualsAndProcedureWithoutScheduling() {
        val item = ReferenceEquipment(id = "detector-1", title = "Gas detector",
            manufacturer = "Test", model = "X5",
            procedures = listOf(procedure()))
        val catalog = ReferenceEquipmentCatalog(equipment = listOf(item))
        assertEquals(listOf(item), referenceEquipmentVisible(catalog, null, "source requirement"))
        assertEquals(listOf(item), referenceEquipmentVisible(catalog, null, "device manual"))
        assertEquals(listOf(item), referenceEquipmentVisible(catalog, null, "sensor"))
        assertTrue(referenceEquipmentSearchPreview(item, "source requirement").contains("source requirement"))
        assertFalse(referenceMatchesEquipment(item, "nothing-found"))
        assertEquals(listOf(item), referenceEquipmentVisible(catalog, null, ""))
    }

    @Test fun importingIsAlwaysManualAndNeverCopiesAnySchedulerIds() {
        val original = ReferenceEquipment(id = "existing", title = "Gas detector",
            procedures = listOf(procedure()))
        val catalog = ReferenceEquipmentCatalog(equipment = listOf(original))
        val newCatalog = referenceImportEquipment(catalog, original, targetFolderId = null)
        assertEquals(2, newCatalog.equipment.size)
        assertEquals(2, newCatalog.equipment.map { it.id }.distinct().size)
        assertEquals(2, newCatalog.equipment.flatMap { it.procedures }.map { it.id }.distinct().size)
        val raw = newCatalog.toJson().toString()
        for (forbidden in listOf("runKey", "workOrderId", "taskId", "serviceId", "deadline", "notification")) {
            assertFalse(forbidden, raw.contains(forbidden))
        }
        assertEquals(1, catalog.equipment.size) // caller explicitly persists, no side effects
    }

    @Test fun searchFindsNestedFolderTitlesAcrossHierarchy() {
        val catalog = ReferenceEquipmentCatalog(folders = listOf(
            ReferenceFolder("root", "Navigation"),
            ReferenceFolder("child", "Gas detection", "root"),
            ReferenceFolder("other", "Engine"),
        ))
        assertEquals(listOf("other", "root"), referenceVisibleFolders(catalog, null, "").map { it.id })
        assertEquals(listOf("child"), referenceVisibleFolders(catalog, "root", "").map { it.id })
        assertEquals(listOf("child"), referenceVisibleFolders(catalog, null, "DETECT").map { it.id })
        assertTrue(referenceVisibleFolders(catalog, null, "unknown").isEmpty())
    }

    @Test fun folderCycleEditIsRejected() {
        val initial = ReferenceEquipmentCatalog(folders = listOf(
            ReferenceFolder("one", "One"), ReferenceFolder("two", "Two", "one")))
        assertTrue(runCatching {
            referenceSaveFolder(initial, ReferenceFolder("one", "One", "two"))
        }.isFailure)
    }
}
