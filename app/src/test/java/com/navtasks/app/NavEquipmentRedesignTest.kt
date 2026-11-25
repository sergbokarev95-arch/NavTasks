package com.navtasks.app
import java.io.File
import org.junit.Assert.*
import org.junit.Test
class NavEquipmentRedesignTest {
    @Test fun equipmentUsesSharedStyleAndFolderDeleteFromRename() {
        val ui = File("src/main/java/com/navtasks/app/ReferenceEquipmentUi.kt").readText()
        assertFalse(ui.contains("NavInfoSectionBanner("))
        assertTrue(ui.contains("neutralStyle = true, highContrastField = true"))
        assertTrue(ui.contains("onDelete = folderEditor?.let"))
        assertTrue(ui.contains("referenceDeleteFolderPreservingContents(catalog, id)"))
        assertFalse(ui.contains("ReferenceCompactAction("))
        assertTrue(ui.contains("ReferenceCenteredDelete("))
        assertTrue(ui.contains("isFolder = true"))
        assertTrue(ui.contains("isFolder = false"))
        assertTrue(ui.contains("if (isFolder) \"equipment-folder\" else \"work-order\""))
        assertFalse(ui.contains("onAddProcedure:"))
        assertFalse(ui.contains("onEdit = { procedureEditor = procedure }"))
        assertFalse(ui.contains("NavPopoverAction(localized(\"Добавить процедуру\""))
        assertFalse(ui.contains("Text(localized(\"+ Добавить\""))
        assertTrue(ui.contains("TextButton(onClick = onExport)"))
        assertTrue(ui.contains("neutralSurface = true, neutralActions = true"))
        assertTrue(ui.contains("restoreStagedEquipmentOriginals("))
    }
    @Test fun editorsUseDirtySaveAndRetainAttachments() {
        val ui = File("src/main/java/com/navtasks/app/ReferenceEquipmentEditorUi.kt").readText()
        assertTrue(ui.contains("ModalBottomSheet("))
        assertTrue(ui.contains("currentFields != initialFields"))
        assertTrue(ui.contains("accent = true, onClick = onSave"))
        assertTrue(ui.contains("attachmentSha256 = original?.attachmentSha256"))
        assertTrue(ui.contains("attachmentMimeType = original?.attachmentMimeType"))
        assertTrue(ui.contains("attachmentFilename = original?.attachmentFilename.orEmpty()"))
    }
}
