package com.navtasks.app

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class NavTodayVisualTokensTest {
    @Test fun referenceRingAndCardMatchBothThemes() {
        assertEquals(22f, NavTodayVisualTokens.checkDiameterDp, 0f)
        assertEquals(1.6f, NavTodayVisualTokens.checkStrokeDp, 0f)
        assertEquals(15f, NavTodayVisualTokens.checkedGlyphDp, 0f)
        assertEquals(20f, NavTodayVisualTokens.cardRadiusDp, 0f)
        assertEquals(Color(0xFFA0A0A2), NavTodayVisualTokens.uncheckedInk(false))
        assertEquals(Color(0xFF6B6B6D), NavTodayVisualTokens.uncheckedInk(true))
        assertEquals(Color.White, NavTodayVisualTokens.cardSurface(false))
        assertEquals(Color(0xFF202022), NavTodayVisualTokens.cardSurface(true))
    }

    @Test fun neutralChecklistAndWorkCardsPreserveOtherDayWarnings() {
        val root = "src/main/java/com/navtasks/app/"
        val ui = File(root + "NavTasksUi.kt").readText()
        val banner = ui.substringAfter("visible = browsingOtherDay,").substringBefore("LazyColumn(")
        assertTrue(banner.contains("NavTodayVisualTokens.cardSurface(p.dark)"))
        assertTrue(banner.contains("amber.copy(alpha = 0.65f)"))
        assertTrue(banner.contains("color = amber"))
        val row = ui.substringAfter("fun RunTileV2(")
        assertTrue(row.contains("if (run.completed) p.muted else p.text,\n                        Modifier.size(22.dp).rotate(disclosureRotation)"))
        assertTrue(row.contains("SpanStyle(color = if (run.completed) p.muted else p.text, fontSize = 11.sp)"))
        assertTrue(row.contains("otherDayAccent.copy(alpha = 0.58f)"))
        val work = File(root + "WorkAgendaUi.kt").readText()
            .substringAfter("private fun WorkAgendaTodayCard(").substringBefore("internal fun WorkAgendaCalendarRows(")
        assertTrue(work.contains("NavTodayVisualTokens.cardSurface(p.dark)"))
        assertTrue(work.contains("Text(category, color = p.text, fontSize = 13.sp, fontWeight = FontWeight.Bold)"))
        assertTrue(work.contains("color = NavTodayVisualTokens.accentInk(p.dark), fontSize = 12.sp"))
        assertTrue(work.contains("glassClickable(onClick = onNavigate)"))
    }

    @Test fun todayCoralTrialKeepsNeutralDateFillAndFrequencyInk() {
        assertEquals(Color(0xFFFF5533), NavTodayVisualTokens.accentInk(false))
        assertEquals(Color(0xFFFF5533), NavTodayVisualTokens.accentInk(true))
        assertEquals(1.5f, NavTodayVisualTokens.selectedDateRimDp, 0f)
        assertEquals(Color(0xFF272729), NavTodayVisualTokens.workNavigateNightSurface())
        assertEquals(Color(0xFF38383A), NavTodayVisualTokens.workNavigateNightRim())
        val root = "src/main/java/com/navtasks/app/"
        val ui = File(root + "NavTasksUi.kt").readText()
        val day = ui.substringAfter("private fun DayChip(").substringBefore("internal fun todayMissedChipVisible(")
        assertTrue(day.contains("background(NavTodayVisualTokens.cardSurface(p.dark), shape)"))
        assertTrue(day.contains("if (selected) Modifier.border("))
        assertTrue(day.contains("NavTodayVisualTokens.accentInk(p.dark), shape"))
        assertTrue(!day.contains("GlassRole.SELECTED"))
        assertTrue(day.contains("if (weekend) calendarWeekendInk(p) else p.text"))
        val today = ui.substringAfter("private fun TodayScreenV2(").substringBefore("internal fun groupTodayByFrequency(")
        assertTrue(today.contains("color = NavTodayVisualTokens.accentInk(p.dark), fontSize = 23.sp"))
        val badges = today.substringAfter("extraDue.forEach").substringBefore("visible = browsingOtherDay")
        assertTrue(badges.contains("NavTodayVisualTokens.cardSurface(p.dark)"))
        assertTrue(badges.contains("color = todayFrequencyColor(frequency, p)"))
        val work = File(root + "WorkAgendaUi.kt").readText()
            .substringAfter("private fun WorkAgendaTodayCard(").substringBefore("internal fun WorkAgendaCalendarRows(")
        assertTrue(work.contains("if (p.dark) Modifier.clip"))
        assertTrue(work.contains("NavTodayVisualTokens.workNavigateNightSurface()"))
        assertTrue(work.contains("else Modifier.navGlass(p, radius = 12.dp, role = GlassRole.CONTROL)"))
    }

    @Test fun todayKeepsTapTargetsGreenConfirmationAndScopedDateWarning() {
        val ui = File("src/main/java/com/navtasks/app/NavTasksUi.kt").readText()
        val check = ui.substringAfter("fun RoundCheck(").substringBefore("private fun TodayScreenV2(")
        assertTrue(check.contains("modifier.size(44.dp).clip(CircleShape)"))
        assertTrue(check.contains("NavTodayVisualTokens.checkDiameterDp.dp"))
        assertTrue(check.contains("if (checked) p.mint else NavTodayVisualTokens.uncheckedInk(p.dark)"))
        val row = ui.substringAfter("fun RunTileV2(")
        assertTrue(row.contains("NavTodayVisualTokens.cardSurface(p.dark)"))
        assertTrue(row.contains("otherDayAccent.copy(alpha = 0.58f)"))
        assertTrue(row.contains("flash.snapTo(1f)"))
        assertTrue(row.contains("delay(NavTaskCompletionMotion.greenHoldMillis)"))
        assertTrue(row.contains("store.setSimpleComplete(run.key, true)"))
        assertTrue(row.contains("store.setCheck(run.key, id, true)"))
    }
}
