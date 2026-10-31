package com.navtasks.app

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class ChecklistReferenceRegressionTest {
    private val date = LocalDate.of(2026, 11, 1)

    private fun template(vararg items: CheckItem) =
        TaskTemplate(title = "GMDSS Records", frequency = Frequency.DAILY, checklist = items.toList())

    private fun run(template: TaskTemplate, vararg snapshot: CheckItem) =
        TaskRun(taskId = template.id, dueDate = date, checklist = snapshot.toList())

    @Test fun todayShowsMemoAfterItemWasRecreatedWithNewId() {
        val existing = CheckItem(id = "old", title = "Daily Test")
        val current = CheckItem(id = "new", title = "Daily Test", comment = "GMDSS daily journal text")
        val t = template(current)
        assertEquals("GMDSS daily journal text",
            checklistReferenceComment(t, run(t, existing), existing))
    }

    @Test fun exactIdWinsOverName() {
        val existing = CheckItem(id = "same", title = "Daily Test")
        val current = existing.copy(comment = "Updated wording")
        val t = template(current)
        assertEquals("Updated wording",
            checklistReferenceComment(t, run(t, existing), existing))
    }

    @Test fun duplicateItemTitlesNeverGuessWrongMemo() {
        val old = CheckItem(id = "old", title = "Test", comment = "Existing")
        val a = CheckItem(id = "a", title = "Test", comment = "Note A")
        val b = CheckItem(id = "b", title = "Test", comment = "Note B")
        val t = template(a, b)
        assertEquals("Existing", checklistReferenceComment(t, run(t, old), old))
    }

    @Test fun completedRunKeepsOriginalJournalNote() {
        val old = CheckItem(id = "old", title = "Daily Test", comment = "Original")
        val new = CheckItem(id = "new", title = "Daily Test", comment = "Updated")
        val t = template(new)
        val completed = run(t, old).copy(completed = true, checkedIds = setOf("old"))
        assertEquals("Original", checklistReferenceComment(t, completed, old))
    }

    @Test fun oldItemNotInCurrentTemplateRetainsOwnMemo() {
        val old = CheckItem(id = "old", title = "Retired Check", comment = "Old note")
        val t = template(CheckItem(id = "other", title = "Different Check", comment = "New note"))
        assertEquals("Old note", checklistReferenceComment(t, run(t, old), old))
    }

    @Test fun titleMatchingIgnoresOuterSpacesAndCase() {
        val old = CheckItem(id = "old", title = " Daily Test ")
        val t = template(CheckItem(id = "new", title = "daily test", comment = "Procedure"))
        assertEquals("Procedure", checklistReferenceComment(t, run(t, old), old))
    }
}
