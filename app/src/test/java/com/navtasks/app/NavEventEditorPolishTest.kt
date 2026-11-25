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
        assertTrue(ui.contains("Modifier.weight(1f).height(44.dp)"))
        assertTrue(screen.contains("NavEventSchedulePanel(p)"))
        assertTrue(screen.contains("NavEventOptionGrid(navTaskRepeatChoices(lang)"))
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
