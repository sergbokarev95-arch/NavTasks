package com.navtasks.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class TodayFrequencyGroupingTest {
    private val date = LocalDate.of(2026, 11, 1)

    private fun row(name: String, frequency: Frequency): Pair<TaskTemplate, TaskRun> {
        val task = TaskTemplate(title = name, frequency = frequency)
        return task to TaskRun(taskId = task.id, dueDate = date, checklist = emptyList())
    }

    @Test fun categoriesHaveStableCalendarOrderAndNeverDuplicateTasks() {
        val weekly = row("GMDSS Weekly", Frequency.WEEKLY)
        val extra = row("Captain request", Frequency.EXTRA)
        val daily = row("Noon", Frequency.DAILY)
        val monthly = row("GMDSS Monthly", Frequency.MONTHLY)
        val anotherWeekly = row("Navigation weekly", Frequency.WEEKLY)
        val input = listOf(weekly, extra, daily, monthly, anotherWeekly)
        val groups = groupTodayByFrequency(input)
        assertEquals(listOf(Frequency.DAILY, Frequency.WEEKLY, Frequency.MONTHLY, Frequency.EXTRA),
            groups.map { it.first })
        assertEquals(listOf(1, 2, 1, 1), groups.map { it.second.size })
        assertEquals(input.map { it.second.key }.toSet(),
            groups.flatMap { it.second }.map { it.second.key }.toSet())
        assertSame(weekly.second, groups[1].second[0].second)
        assertSame(anotherWeekly.second, groups[1].second[1].second)
    }

    @Test fun absentSectionsAreOmittedAndDayWithOnlyDailyHasNoExtraBadges() {
        val grouped = groupTodayByFrequency(listOf(row("Noon", Frequency.DAILY)))
        assertEquals(listOf(Frequency.DAILY), grouped.map { it.first })
        assertTrue(grouped.none { it.first != Frequency.DAILY })
        assertTrue(groupTodayByFrequency(emptyList()).isEmpty())
    }

    @Test fun completedAndPendingTasksUseTheSameFrequencyPartition() {
        val weekly = row("Weekly", Frequency.WEEKLY)
        val daily = row("Daily", Frequency.DAILY)
        val completed = weekly.first to weekly.second.copy(completed = true)
        val pending = daily
        assertEquals(Frequency.WEEKLY,
            groupTodayByFrequency(listOf(completed)).single().first)
        assertEquals(Frequency.DAILY,
            groupTodayByFrequency(listOf(pending)).single().first)
        assertTrue(groupTodayByFrequency(listOf(completed)).single().second.single().second.completed)
    }
}
