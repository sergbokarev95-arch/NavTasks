package com.navtasks.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class ChecklistEditorOrderTest {
    private val original = listOf(
        CheckItem(id = "one", title = "GMDSS PV", comment = "Journal note PV"),
        CheckItem(id = "two", title = "GMDSS KV", comment = "Journal note KV"),
        CheckItem(id = "three", title = "GMDSS Log", comment = "Journal note Log"),
    )

    @Test fun movingDownPreservesIdsAndNotes() {
        val moved = moveChecklistItem(original, 0, 1)
        assertEquals(listOf("two", "one", "three"), moved.map { it.id })
        assertEquals("Journal note PV", moved[1].comment)
        assertEquals("Journal note KV", moved[0].comment)
        assertEquals(listOf("one", "two", "three"), original.map { it.id })
    }

    @Test fun movingUpPreservesItems() {
        val moved = moveChecklistItem(original, 2, 1)
        assertEquals(listOf("one", "three", "two"), moved.map { it.id })
        assertEquals(original.toSet(), moved.toSet())
    }

    @Test fun outOfRangeMovesLeaveExistingListUntouched() {
        assertSame(original, moveChecklistItem(original, -1, 1))
        assertSame(original, moveChecklistItem(original, 0, 3))
        assertSame(original, moveChecklistItem(original, 1, 1))
    }
}
