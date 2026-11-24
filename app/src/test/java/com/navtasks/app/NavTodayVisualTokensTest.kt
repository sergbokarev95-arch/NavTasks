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
