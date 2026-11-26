package com.navtasks.app

import java.io.File
import org.junit.Assert.*
import org.junit.Test

class NavReferenceGlassActionsTest {
    private fun source(name: String) = File("src/main/java/com/navtasks/app/$name.kt").readText()

    @Test fun referenceUsesSharedOpticsAndSeparateBackdrop() {
        val action = source("ReferenceLiquidGlassAction")
        assertTrue(action.contains("Build.VERSION.SDK_INT >= 33"))
        assertTrue(action.contains("Modifier.matchParentSize().layerBackdrop(backdrop)"))
        assertTrue(action.contains("NavGlassBackdrop(p)"))
        assertTrue(action.contains("NavLiquidGlassCircleButton(p, backdrop, kind, onClick)"))
        assertTrue(action.contains("NavGlassCircleButton(p, kind, onClick)"))
        assertTrue(action.contains("NavLiquidGlassActionTokens.compactDiameterDp.dp"))
        assertTrue(action.contains("contentDescription = label"))
        assertFalse(action.contains("personalNoteCard("))
        for (name in listOf("ReferenceHubUi", "ReferenceEquipmentUi", "ReferenceManualLibraryUi")) {
            val ui = source(name)
            assertTrue(name, ui.contains("ReferenceLiquidGlassAction(p, kind, label, onClick)"))
            assertFalse(name, ui.contains("NavEventHeaderAction(kind, label, p, true, onClick)"))
            assertFalse(name, ui.contains("NavGlassCircleButton(p, \"back\")"))
        }
    }

    @Test fun allExistingReferenceActionRoutesRemain() {
        val hub = source("ReferenceHubUi")
        val equipment = source("ReferenceEquipmentUi")
        val manual = source("ReferenceManualLibraryUi")
        assertTrue(hub.contains("searchOpen = !searchOpen"))
        assertTrue(hub.contains("{ leaveExchange() }"))
        assertTrue(equipment.contains("{ navigateBack() }"))
        assertTrue(equipment.contains("{ addMenu = true }"))
        assertTrue(equipment.contains("searchOpen = !searchOpen"))
        assertTrue(equipment.contains("{ attachmentPreview = null }"))
        assertTrue(manual.contains("{ navigateBack() }"))
        assertTrue(manual.contains("{ if (!busy) menuOpen = true }"))
        assertTrue(manual.contains("showSearch = !showSearch"))
        assertTrue(manual.contains("{ documentId = null }"))
    }
}
