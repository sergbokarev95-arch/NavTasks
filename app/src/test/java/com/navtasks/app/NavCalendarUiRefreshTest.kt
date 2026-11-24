package com.navtasks.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.time.LocalDate
import androidx.compose.ui.unit.dp

/** Calendar UI Refresh: visual-only navigation, task recurrence untouched. */
class NavCalendarUiRefreshTest {
    @Test fun yearMonthDayAreOnlyFunctionalSegmentOptions() {
        assertEquals(listOf(0, 1, 2), navCalendarAvailableLevels)
    }

    @Test fun yearOverviewIsExactlyFourRowsAndTwelveDistinctMonths() {
        assertEquals(4, navCalendarYearRows.size)
        assertTrue(navCalendarYearRows.all { it.size == 3 })
        assertEquals((1..12).toList(), navCalendarYearRows.flatten())
        val calendar = File("src/main/java/com/navtasks/app/PlanAndTasksUi.kt").readText()
        val year = calendar.substringAfter("0 -> BoxWithConstraints(")
            .substringBefore("1 -> Column {")
        assertTrue(year.contains("navCalendarYearRows.forEach"))
        assertTrue(year.contains("Modifier.fillMaxWidth().weight(1f)"))
        assertTrue(year.contains("Modifier.weight(1f).fillMaxHeight()"))
        assertTrue(year.contains("val compact = maxHeight < 470.dp"))
        assertTrue(year.contains("monthGridDates(year, m).chunked(7)"))
        assertTrue(!year.contains("LazyVerticalGrid("))
    }

    @Test fun modeSwitchPreservesDockSpringAndTapOnlyLensPulse() {
        val control = File("src/main/java/com/navtasks/app/NavCalendarModeControl.kt").readText()
        assertTrue(control.contains("navLiquidGlassRim(dark)"))
        assertTrue(control.contains("stiffness = NavLiquidDockMotion.positionStiffness"))
        assertTrue(control.contains("NavLiquidDockTabs.repeatTapScale"))
        assertTrue(control.contains("calendarGlassLensSlide"))
        assertTrue(control.contains("onSelect(navCalendarAvailableLevels[index])"))
        assertTrue(!control.contains("localized(\"Неделя\""))
        assertTrue(!control.contains("localized(\"События\""))
    }

    @Test fun calendarSwitchMatchesExactYearCardInBothThemes() {
        assertEquals(NavTodayVisualTokens.cardSurface(true), navCalendarModeSurface(navPalette(true)))
        assertEquals(NavTodayVisualTokens.cardSurface(false), navCalendarModeSurface(navPalette(false)))
        assertEquals(androidx.compose.ui.graphics.Color(0xFF3A3A3C),
            navCalendarLensResting(true))
        assertEquals(androidx.compose.ui.graphics.Color(0xFFE7E7E9),
            navCalendarLensResting(false))
        val control = File("src/main/java/com/navtasks/app/NavCalendarModeControl.kt")
            .readText()
        val calendar = File("src/main/java/com/navtasks/app/PlanAndTasksUi.kt")
            .readText()
        assertTrue(control.contains(".background(navCalendarModeSurface(p), shape)"))
        assertTrue(calendar.contains(".background(NavTodayVisualTokens.cardSurface(p.dark))"))
        assertTrue(control.contains("val inactiveInk = p.muted"))
        assertTrue(control.contains("val activeInk = NavLiquidDockTabs.activeInk(dark)"))
        // Neither inactive surfaces nor the glass lens contain the old blue hues.
        assertTrue(!control.contains("0x66528BAF"))
        assertTrue(!control.contains("0xE926303C"))
        assertTrue(!control.contains("0xF8EDF3F8"))
    }

    @Test fun calendarRedesignKeepsSemanticStatusColorsAndUsesDockMotion() {
        val calendar = File("src/main/java/com/navtasks/app/PlanAndTasksUi.kt").readText()
            .substringAfter("fun PlanScreenV2(").substringBefore("/** ISO-8601 calendar week")
        val control = File("src/main/java/com/navtasks/app/NavCalendarModeControl.kt").readText()
        assertTrue(control.contains("NavLiquidDockMotion.landingSquashScale"))
        assertTrue(control.contains("NavLiquidDockMotion.peakScaleForTap"))
        assertTrue(calendar.contains("\"chevron-left\", p.text"))
        assertTrue(calendar.contains("\"more\", p.text"))
        assertTrue(calendar.contains("CalendarDayState.INCOMPLETE -> p.systemRed"))
        assertTrue(calendar.contains("CalendarDayState.COMPLETE -> p.mint"))
    }

    @Test fun onlyTopPeriodHeaderDisplaysFullDate() {
        val ui = File("src/main/java/com/navtasks/app/PlanAndTasksUi.kt").readText()
        val calendar = ui.substringAfter("fun PlanScreenV2(").substringBefore("private fun PlanReadOnlyTile(")
        val month = calendar.substringAfter("1 -> Column {").substringBefore("else -> Column {")
        val day = calendar.substringAfter("else -> Column {")
        assertTrue(calendar.contains("Text(periodTitle,"))
        assertTrue(!month.contains("Text(\"\${YearMonth.of(year, month)"))
        assertTrue(!day.contains("Text(navDate(selectedDay"))
        assertTrue(month.contains("Previous month") && month.contains("Next month"))
        assertTrue(day.contains("Back to month") && day.contains("View year"))
    }

    @Test fun compactGlassControlReturnsHeightToYearOverview() {
        assertEquals(44.dp, navCalendarModeHeight)
        assertEquals(40.dp, navCalendarModeLensHeight)
        val calendar = File("src/main/java/com/navtasks/app/PlanAndTasksUi.kt").readText()
        val year = calendar.substringAfter("0 -> BoxWithConstraints(")
            .substringBefore("1 -> Column {")
        // Calendar text increases without reintroducing year scrolling.
        assertTrue(year.contains("fontSize = if (compact) 12.sp else 15.sp"))
        assertTrue(year.contains("fontSize = if (compact) 9.sp else 10.sp"))
        assertTrue(year.contains("val compact = maxHeight < 470.dp"))
        assertTrue(!year.contains("LazyVerticalGrid("))
    }

    @Test fun november2026ShowsWeek44Through49BesideMonthRows() {
        val rows = monthGridDates(2026, 11).chunked(7)
        assertEquals(6, rows.size)
        assertTrue(rows.all { it.size == 7 && it.first().dayOfWeek.value == 1 })
        assertEquals(listOf(44, 45, 46, 47, 48, 49),
            rows.map { calendarIsoWeekNumber(it.first()) })
    }

    @Test fun isoWeekNumbersHandleYearTransitionAndLeapYears() {
        // Friday Jan 1 2021 belongs to 2020's ISO week 53.
        assertEquals(53, calendarIsoWeekNumber(LocalDate.of(2020, 12, 28)))
        assertEquals(53, calendarIsoWeekNumber(LocalDate.of(2021, 1, 1)))
        assertEquals(1, calendarIsoWeekNumber(LocalDate.of(2021, 1, 4)))
        // Dec 30 2024 is ISO week 1 of the following ISO week-based year.
        assertEquals(1, calendarIsoWeekNumber(LocalDate.of(2024, 12, 30)))
        assertEquals(9, calendarIsoWeekNumber(LocalDate.of(2024, 2, 29)))
    }

    @Test fun monthWeekGutterDoesNotAffectYearOrDayModes() {
        assertEquals(26.dp, calendarMonthWeekNumberGutter)
        val calendar = File("src/main/java/com/navtasks/app/PlanAndTasksUi.kt").readText()
        val year = calendar.substringAfter("0 -> BoxWithConstraints(")
            .substringBefore("1 -> Column {")
        val month = calendar.substringAfter("1 -> Column {")
            .substringBefore("else -> Column {")
        val day = calendar.substringAfter("else -> Column {")
            .substringBefore("/** ISO-8601 calendar week")
        assertTrue(month.contains("Spacer(Modifier.width(calendarMonthWeekNumberGutter))"))
        assertTrue(month.contains("calendarIsoWeekNumber(week.first()).toString()"))
        assertTrue(month.contains("Modifier.width(calendarMonthWeekNumberGutter).height(56.dp)"))
        assertTrue(!year.contains("calendarIsoWeekNumber("))
        assertTrue(!day.contains("calendarIsoWeekNumber("))
        assertTrue(month.contains("week.forEach { date ->"))
    }

    @Test fun sevenDayStripStartsOnMondayAndCrossesMonthSafely() {
        val selected = LocalDate.of(2026, 11, 1) // Sunday
        val week = navCalendarWeekDates(selected)
        assertEquals(7, week.size)
        assertEquals(LocalDate.of(2026, 10, 26), week.first())
        assertEquals(selected, week.last())
    }

    @Test fun weekStripCrossesCalendarYearWithoutChangingDate() {
        val selected = LocalDate.of(2027, 1, 1) // Friday
        val week = navCalendarWeekDates(selected)
        assertEquals(LocalDate.of(2026, 12, 28), week.first())
        assertEquals(LocalDate.of(2027, 1, 3), week.last())
        assertTrue(selected in week)
    }

    @Test fun originalSwipePositionStillControlsYearMonthDayChanges() {
        val date = LocalDate.of(2026, 12, 31)
        val month = calendarSwipePosition(1, 2026, 12, date, 1)
        assertEquals(2027, month.year)
        assertEquals(1, month.month)
        val day = calendarSwipePosition(2, 2026, 12, date, 1)
        assertEquals(LocalDate.of(2027, 1, 1), day.day)
        assertEquals(2026, calendarSwipePosition(0, 2027, 12, date, -1).year)
    }

    @Test fun appKeepsCalendarInsideTodayAndDockUntouched() {
        val root = File("src/main/java/com/navtasks/app/NavTasksUi.kt").readText()
        val calendar = File("src/main/java/com/navtasks/app/PlanAndTasksUi.kt").readText()
        assertTrue(root.contains("if (showCalendar)"))
        assertTrue(root.contains("PlanScreenV2("))
        assertTrue(calendar.contains("NavCalendarModeControl(level, p, lang"))
        assertTrue(calendar.contains("navCalendarWeekDates(selectedDay)"))
        assertTrue(calendar.contains("PlanReadOnlyTile(task, run, store, p, lang, onEdit)"))
        assertTrue(calendar.contains("calendarHasOutcomeDot(status, hasWork)"))
    }
}
