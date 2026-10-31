package com.navtasks.app

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class ChecklistReferenceTest {
    private val day = LocalDate.of(2026, 11, 6)

    @Test fun currentUnfinishedCheckUsesLatestTemplateReference() {
        val savedItem = CheckItem(id = "hf", title = "ПВ/КВ тест", comment = "Old wording")
        val updatedItem = savedItem.copy(comment = "New GMDSS log wording")
        val template = TaskTemplate(
            title = "ГМССБ", frequency = Frequency.WEEKLY, checklist = listOf(updatedItem),
        )
        val occurrence = TaskRun(taskId = template.id, dueDate = day, checklist = listOf(savedItem))
        assertEquals("New GMDSS log wording",
            checklistReferenceComment(template, occurrence, savedItem))
    }

    @Test fun completedCheckKeepsItsHistoricalReference() {
        val original = CheckItem(id = "hf", title = "ПВ/КВ тест", comment = "Original journal wording")
        val updated = original.copy(comment = "Newer journal wording")
        val template = TaskTemplate(
            title = "ГМССБ", frequency = Frequency.WEEKLY, checklist = listOf(updated),
        )
        val completedRun = TaskRun(
            taskId = template.id, dueDate = day,
            checklist = listOf(original), checkedIds = setOf("hf"), completed = true,
        )
        assertEquals("Original journal wording",
            checklistReferenceComment(template, completedRun, original))
    }

    @Test fun legacyCheckHasNoCommentUntilAuthorAddsOne() {
        val old = CheckItem(id = "hf", title = "ПВ/КВ тест")
        val template = TaskTemplate(
            title = "ГМССБ", frequency = Frequency.WEEKLY, checklist = listOf(old),
        )
        val occurrence = TaskRun(taskId = template.id, dueDate = day, checklist = listOf(old))
        assertEquals("", checklistReferenceComment(template, occurrence, old))
        assertEquals("new reference", old.copy(comment = "new reference").comment)
    }
}
