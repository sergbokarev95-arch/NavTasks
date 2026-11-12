package com.navtasks.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class NavWidgetCapacityTest {
    @Test fun widgetsCannotCompleteTasksFromLauncher() {
        assertFalse(NAV_WIDGET_DIRECT_COMPLETE)
    }

    @Test fun countsAreClampedAndProgressIsStableOnEmptyDays() {
        val empty = widgetStats(DayCounts(0, 0), DayCounts(6, 0), missed = 0)
        assertEquals(0, empty.percentage)
        assertEquals(0, empty.remaining)
        assertFalse(empty.allCompleted)
        assertEquals("Завтра 6 задач", widgetSummaryLabel(empty, UiLanguage.RU))
        val partial = widgetStats(DayCounts(12, 10), DayCounts(6, 0), missed = 2)
        assertEquals(2, partial.remaining)
        assertEquals(83, partial.percentage)
        assertFalse(partial.allCompleted)
        assertEquals(2, partial.missed)
        val invalid = widgetStats(DayCounts(2, 99), DayCounts(-2, 0), missed = -5)
        assertEquals(2, invalid.completed)
        assertEquals(100, invalid.percentage)
        assertEquals(0, invalid.tomorrowTotal)
        assertEquals(0, invalid.missed)
    }

    @Test fun todayRemainsVisibleAfterCompletionAndTomorrowBecomesSecondary() {
        val complete = widgetStats(DayCounts(12, 12), DayCounts(6, 0), missed = 0)
        assertTrue(complete.allCompleted)
        assertEquals("Завтра 6 задач", widgetSummaryLabel(complete, UiLanguage.RU))
        assertEquals("Tomorrow: 6 tasks", widgetSummaryLabel(complete, UiLanguage.EN))
        assertEquals(
            listOf("Сегодня всё выполнено", "План на завтра"),
            widgetWideDetail(complete, emptyList(), UiLanguage.RU),
        )
        assertEquals("Всё выполнено", widgetSummaryLabel(
            widgetStats(DayCounts(12, 12), DayCounts(0, 0), 0), UiLanguage.RU))
    }

    @Test fun unfinishedTodayTakesPriorityAndWidePreviewIsReadOnly() {
        val stats = widgetStats(DayCounts(5, 3), DayCounts(17, 0), 1)
        assertEquals("Осталось 2 задачи", widgetSummaryLabel(stats, UiLanguage.RU))
        assertEquals("2 remaining", widgetSummaryLabel(stats, UiLanguage.EN))
        assertEquals(listOf("Navarea/Navtex", "Сводная корректура"),
            widgetWideDetail(stats, listOf("Navarea/Navtex", "Сводная корректура", "ECDIS"), UiLanguage.RU))
    }

    @Test fun russianCountGrammarAndNoTasksAreHandled() {
        for ((count, expected) in listOf(
            1 to "Осталось 1 задача", 2 to "Осталось 2 задачи",
            5 to "Осталось 5 задач", 11 to "Осталось 11 задач", 21 to "Осталось 21 задача",
        )) {
            val stats = widgetStats(DayCounts(count, 0), DayCounts(0, 0), 0)
            assertEquals(expected, widgetSummaryLabel(stats, UiLanguage.RU))
        }
        assertEquals("Сегодня нет задач", widgetSummaryLabel(
            widgetStats(DayCounts(0, 0), DayCounts(0, 0), 0), UiLanguage.RU))
    }

    @Test fun widgetUsesNavTasksBlueAndHidesZeroMissedBadge() {
        assertEquals(0xFF5CB8FF.toInt(), widgetAccentColor(dark = true))
        assertEquals(0xFF0071E3.toInt(), widgetAccentColor(dark = false))
        assertFalse(widgetMissedVisible(0))
        assertTrue(widgetMissedVisible(1))
        assertTrue(widgetMissedVisible(12))
    }

    @Test fun dateAlwaysUsesSelectedLanguage() {
        val day = LocalDate.of(2026, 11, 12)
        assertTrue(widgetHeaderDate(day, UiLanguage.RU).startsWith("12"))
        assertTrue(widgetHeaderDate(day, UiLanguage.EN).endsWith("12"))
    }
}
