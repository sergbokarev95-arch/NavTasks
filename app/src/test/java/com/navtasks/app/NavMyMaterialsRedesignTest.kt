package com.navtasks.app

import java.io.File
import org.junit.Assert.*
import org.junit.Test

class NavMyMaterialsRedesignTest {
    private fun source(name: String) = File("src/main/java/com/navtasks/app/$name.kt").readText()

    @Test fun libraryUsesSharedSearchAndNeutralCards() {
        val ui = source("ReferenceManualLibraryUi")
        assertTrue(ui.contains("revealSearchOnSettledTopPull(listState, showSearch || busy || modalOpen)"))
        assertTrue(ui.contains("dismissSearchImeOnResultsGesture(showSearch)"))
        assertTrue(ui.contains("neutralStyle = true, highContrastField = true"))
        assertTrue(ui.contains("neutralSurface = true, neutralActions = true"))
        assertTrue(ui.contains("personalNoteCard(p, NavEventLayout.cardRadius)"))
        assertFalse(ui.contains("p.blueText"))
        assertTrue(ui.contains("glyph = \"equipment-folder\", isFolder = true"))
        assertTrue(ui.contains("onEdit = {"))
        assertTrue(ui.contains("store.deleteEmptyFolder(item.id)"))
        assertTrue(ui.contains("if (folderId == item.id) folderId = item.parentId"))
        assertFalse(ui.contains("deleteFolder = activeFolder"))
    }

    @Test fun excerptUsesSharedEditorAndRetainsOriginalFolder() {
        val ui = source("ReferenceManualExcerptUi")
        assertTrue(ui.contains("ReferenceEditorSheet(p, lang"))
        assertTrue(ui.contains("currentFields != initialFields"))
        assertTrue(ui.contains("referenceExcerptFolderForSave(original, folderId)"))
        assertTrue(ui.contains("sourcePage = page.toIntOrNull()"))
        assertFalse(ui.contains("p.blueText"))
        assertTrue(source("ReferenceHubUi").contains("Онлайн-библиотека — в разработке"))
    }
}
