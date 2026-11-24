package com.navtasks.app

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test
import java.io.File

class NavProfileVisualTokensTest {
    @Test fun profileCardsShareNeutralPaletteAndCoralSelection() {
        assertEquals(Color.White, NavProfileVisualTokens.cardSurface(false, false))
        assertEquals(Color.White, NavProfileVisualTokens.cardSurface(false, true))
        assertEquals(Color(0xFF202022), NavProfileVisualTokens.cardSurface(true, false))
        assertEquals(Color(0xFF2A2A2C), NavProfileVisualTokens.cardSurface(true, true))
        val root = "src/main/java/com/navtasks/app/"
        val screen = File(root + "ProfilesUi.kt").readText().substringBefore("fun ProfileExchangePanel(")
        assertFalse(screen.contains("onSettings"))
        assertTrue(screen.contains("profileCardStyle(p, current"))
        assertTrue(screen.contains("profileCardStyle(p, selected = false"))
        assertTrue(screen.contains("NavTodayVisualTokens.accentInk(p.dark)"))
        assertTrue(screen.contains("color = p.systemRed"))
        assertTrue(screen.contains("profiles.select(id)"))
        assertTrue(screen.contains("profiles.delete(victim.id)"))
        val quick = File(root + "NavQuickProfileUi.kt").readText()
        assertTrue(quick.contains("NavTodayVisualTokens.selectedDateRimDp.dp"))
        assertTrue(quick.contains("if (selected) Modifier.border("))
        assertTrue(quick.contains("profileCardStyle(p, selected, radius = 16.dp, nested = true)"))
        assertFalse(quick.contains("p.selectionFill"))
        assertTrue(quick.contains("clickable { onSelect(profile.id) }"))
        assertTrue(quick.contains("NavApplyDialogFrost(frost, radius = 26.dp)"))
    }
}
