package com.navtasks.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class NavEventEditorPolishTest {
    @Test fun scheduleSharesRowHeightAndUsesEqualColumns() {
        val ui = File("src/main/java/com/navtasks/app/NavEventEditorUi.kt").readText()
        val screen = File("src/main/java/com/navtasks/app/PlanAndTasksUi.kt").readText()
        assertEquals(androidx.compose.ui.unit.Dp(48f), NavEventRowHeight)
        assertTrue(ui.contains("heightIn(min = NavEventRowHeight)"))
        assertTrue(ui.contains("options.chunked(columns)"))
        assertTrue(ui.contains("Modifier.weight(1f).heightIn(min = NavEventLayout.choiceHeight)"))
        assertTrue(screen.contains("NavEventSchedulePanel(p)"))
        assertTrue(screen.contains("NavEventOptionGrid(navTaskRepeatChoices(lang)"))
    }
    @Test fun compactCardsShareSpacingAndMissedGrid() {
        val screen = File("src/main/java/com/navtasks/app/PlanAndTasksUi.kt").readText()
        assertEquals(androidx.compose.ui.unit.Dp(4f), NavEventLayout.cardVertical)
        assertEquals(androidx.compose.ui.unit.Dp(10f), NavEventLayout.sectionGap)
        val reminders = screen.substringAfter("private fun TaskEditorState.EditorReminders(")
            .substringBefore("private fun TaskEditorState.EditorChecklist(")
        val other = screen.substringAfter("private fun TaskEditorState.EditorOther(")
            .substringBefore("private fun TaskEditorState.EditorDeletionDialogs(")
        assertTrue(reminders.contains("NavEventSchedulePanel(p)"))
        assertTrue(reminders.contains("NavEventActionRow("))
        assertTrue(other.contains("NavEventOptionGrid(listOf("))
        assertTrue(other.contains("NavEventLayout.innerGap"))
        assertTrue(other.contains("NavEventSwitchRow("))
    }
    @Test fun checklistEditorHasVisibleFieldWithoutChangingSave() {
        val memo = File("src/main/java/com/navtasks/app/ChecklistNotesUi.kt").readText()
        val rich = File("src/main/java/com/navtasks/app/NavRichNoteUi.kt").readText()
        assertTrue(memo.contains("checklistField = true"))
        assertTrue(memo.contains("if (title.isNotBlank())"))
        assertTrue(memo.contains("onSave(draft.trim(), draftHtml)"))
        assertTrue(rich.contains("checklistField: Boolean = false"))
        assertTrue(rich.contains("if (fullPage || checklistField) formattingTools()"))
        assertTrue(rich.contains("if (checklistField) Modifier.personalNoteCard(p, 12.dp, nested = true)"))
    }
}
