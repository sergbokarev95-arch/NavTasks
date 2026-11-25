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
        assertTrue(ui.contains("TextButton(onClick = onExport, modifier = Modifier.heightIn(min = 48.dp))"))
        assertFalse(ui.contains("ReferenceCenteredDelete(localized(\"Удалить папку\""))
        assertTrue(ui.contains("maxLines = if (equipment != null && procedure == null) 3 else 1"))
        assertTrue(ui.contains("onEdit = { equipmentEditor = equipment }"))
        assertTrue(ui.contains("onAdd = { newProcedure = true }"))
        val details = ui.substringAfter("private fun ReferenceEquipmentDetails(").substringBefore("private fun ReferenceProcedureReader(")
        assertTrue(details.contains("Box(Modifier.personalNoteCard(p, 22.dp))"))
        assertTrue(details.contains("ReferenceEquipmentHeaderAction(p, \"edit\", localized(\"Изменить\", \"Edit\", lang), onEdit)"))
        assertTrue(details.contains("ReferenceEquipmentHeaderAction(p, \"plus\", localized(\"Добавить процедуру\", \"Add procedure\", lang), onAdd)"))
        assertTrue(ui.contains("neutralSurface = true, neutralActions = true"))
        assertTrue(ui.contains("restoreStagedEquipmentOriginals("))
    }
    @Test fun editorsUseDirtySaveAndRetainAttachments() {
        val ui = File("src/main/java/com/navtasks/app/ReferenceEquipmentEditorUi.kt").readText()
        assertTrue(ui.contains("ModalBottomSheet("))
        assertTrue(ui.contains("Box(Modifier.personalNoteCard(p, 14.dp, nested = true))"))
        assertTrue(ui.contains("TextButton(onClick = onDelete, modifier = Modifier.heightIn(min = 44.dp))"))
        assertTrue(ui.contains("currentFields != initialFields"))
        assertTrue(ui.contains("accent = true, onClick = onSave"))
        assertTrue(ui.contains("attachmentSha256 = original?.attachmentSha256"))
        assertTrue(ui.contains("attachmentMimeType = original?.attachmentMimeType"))
        assertTrue(ui.contains("attachmentFilename = original?.attachmentFilename.orEmpty()"))
    }
}
