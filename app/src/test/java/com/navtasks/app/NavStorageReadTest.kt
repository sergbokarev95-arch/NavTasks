package com.navtasks.app

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class NavStorageReadTest {
    @Test fun validTaskAndHistoryRoundTrip() {
        val task = TaskTemplate(title = "GMDSS Weekly", frequency = Frequency.WEEKLY)
        val run = TaskRun(task.id, LocalDate.of(2026, 11, 10), task.checklist)
        val tasks = NavStorageRead.templates("[${task.toJson()}]")
        val runs = NavStorageRead.runs("[${run.toJson()}]")
        assertEquals(task.id, tasks.single().id)
        assertEquals(run.dueDate, runs.values.single().dueDate)
    }

    @Test fun malformedOrPartialJsonDoesNotBecomeEmptySuccess() {
        for (raw in listOf("{broken", "[null]", "[34]", "[{}]",
            "[{\"taskId\":\"test\",\"dueDate\":\"bad-date\"}]")) {
            assertTrue(raw, runCatching { NavStorageRead.runs(raw) }.isFailure)
        }
        assertTrue(runCatching { NavStorageRead.templates("[null]") }.isFailure)
        assertTrue(runCatching { NavStorageRead.templates("[{}]") }.isFailure)
    }

    @Test fun duplicateTemplatesAndRunKeysAreRejected() {
        val task = TaskTemplate(title = "Daily", frequency = Frequency.DAILY)
        val run = TaskRun(task.id, LocalDate.of(2026, 11, 10), emptyList())
        assertTrue(runCatching {
            NavStorageRead.templates("[${task.toJson()},${task.toJson()}]")
        }.isFailure)
        assertTrue(runCatching {
            NavStorageRead.runs("[${run.toJson()},${run.toJson()}]")
        }.isFailure)
    }

    @Test fun completedHandoverNotesRetainFormattingAndDates() {
        val note = HandoverEntry(
            title = "Radar", notes = "Check", notesHtml = "<b>Check</b>",
            mark = HandoverMark.COMPLETED,
            completedDate = LocalDate.of(2026, 11, 9),
        )
        val sec = HandoverSection(title = "Navigation", entries = listOf(note))
        val result = NavStorageRead.sections("[${sec.toJson()}]")
        assertEquals(note.notesHtml, result.single().entries.single().notesHtml)
        assertEquals(note.completedDate, result.single().entries.single().completedDate)
    }

    @Test fun malformedNestedHandoverRecordsCannotBeSilentlyDropped() {
        for (raw in listOf("[null]", "[{}]",
            "[{\"title\":\"Notes\",\"entries\":[null]}]",
            "[{\"title\":\"Notes\",\"entries\":[{}]}]",
            "[{\"title\":\"Notes\",\"entries\":{}}]")) {
            assertTrue(raw, runCatching { NavStorageRead.sections(raw) }.isFailure)
        }
    }

    @Test fun writeProtectionPreservesOriginalCorruptBytes() {
        val original = "[{\"broken\":true}]"
        var written = original
        val issue = runCatching { NavStorageRead.runs(original) }
            .exceptionOrNull()?.message
        assertNotNull(issue)
        assertTrue(runCatching {
            ensureNavStorageWritable(issue)
            written = "[]"
        }.isFailure)
        assertEquals(original, written)
        ensureNavStorageWritable(null)
    }

    @Test fun emptyValidStorageRemainsSupported() {
        assertTrue(NavStorageRead.templates("[]").isEmpty())
        assertTrue(NavStorageRead.runs("[]").isEmpty())
        assertTrue(NavStorageRead.sections("[]").isEmpty())
    }
}
