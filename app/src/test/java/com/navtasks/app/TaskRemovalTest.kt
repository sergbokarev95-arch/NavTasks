package com.navtasks.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class TaskRemovalTest {
    private val day = LocalDate.of(2026, 11, 6)

    private fun task(id: String, enabled: Boolean = true) = TaskTemplate(
        id = id, title = "Bridge routine " + id,
        frequency = Frequency.DAILY,
        startDate = day.minusDays(9),
        enabled = enabled,
    )

    @Test fun stopRecurrenceKeepsTemplateAndAllowsReactivation() {
        val original = task("bridge")
        val stopped = taskStopRepeating(original)
        assertEquals(original.id, stopped.id)
        assertEquals(original.title, stopped.title)
        assertEquals(original.startDate, stopped.startDate)
        assertFalse(stopped.enabled)
        assertFalse(TaskSchedule.isCalendarDue(stopped, day))
        assertTrue(TaskSchedule.isCalendarDue(stopped.copy(enabled = true), day))
        assertTrue(original.enabled)
    }

    @Test fun permanentDeletionOnlyRemovesSelectedTemplateAndItsOwnRuns() {
        val a = task("a")
        val b = task("b")
        val aComplete = TaskRun(a.id, day.minusDays(2), emptyList(),
            completed = true)
        val aMoved = TaskRun(a.id, day.minusDays(1), emptyList(),
            postponedTo = day.plusDays(3))
        val bComplete = TaskRun(b.id, day.minusDays(1), emptyList(),
            completed = true)
        val saved = mapOf(
            aComplete.key to aComplete,
            aMoved.key to aMoved,
            bComplete.key to bComplete,
        )

        val deleted = taskDeletePermanently(listOf(a, b), saved, a.id)!!
        assertEquals(listOf(b), deleted.templates)
        assertEquals(mapOf(bComplete.key to bComplete), deleted.runs)
        assertEquals(3, saved.size)
        assertTrue(saved.containsKey(aComplete.key))
    }

    @Test fun stoppedTaskHistoryRemainsAvailableForExport() {
        val task = task("a")
        val completion = TaskRun(task.id, day.minusDays(1), emptyList(),
            completed = true)
        val runs = mapOf(completion.key to completion)
        val stopped = taskStopRepeating(task)
        assertFalse(stopped.enabled)
        assertEquals(completion, runs[completion.key])
        assertNull(taskDeletePermanently(listOf(stopped), runs, "unknown"))
    }

    @Test fun deleteOneOffAndDisabledTasksIndependently() {
        val once = task("once").copy(frequency = Frequency.EXTRA)
        val disabled = taskStopRepeating(task("archived"))
        val gone = taskDeletePermanently(listOf(once, disabled), emptyMap(), "once")!!
        assertEquals(listOf(disabled), gone.templates)
        assertTrue(gone.runs.isEmpty())
    }
}
