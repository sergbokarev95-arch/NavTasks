package com.navtasks.app

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class NavPopupFrostTest {
    @Test fun subtleBlurAndNeutralMaterialMatchReference() {
        assertEquals(10f, NavPopupFrostTokens.blurRadiusDp, 0f)
        assertEquals(.90f, NavPopupFrostTokens.tintAlpha, 0f)
        assertEquals(Color(0xFF222222), NavPopupFrostTokens.fill(true))
        assertEquals(Color(0xFFF6F7F9), NavPopupFrostTokens.fill(false))
        assertTrue(NavPopupFrostTokens.rimWidthDp < 1f)
        assertTrue(NavPopupFrostTokens.divider(true).alpha < .1f)
        assertTrue(NavPopupFrostTokens.divider(false).alpha < .1f)
    }

    @Test fun sourceIsSeparateAndOnlyActiveWhileMenuIsOpen() {
        val root = "src/main/java/com/navtasks/app/"
        val frost = File(root + "NavPopupFrost.kt").readText()
        val menu = File(root + "NavOverlayUi.kt").readText()
        val activity = File(root + "MainActivity.kt").readText()
        assertTrue(activity.contains("NavPopoverBackdropHost {"))
        assertTrue(frost.contains("if (capture.openMenus > 0) Modifier.hazeSource(capture.haze)"))
        assertTrue(frost.contains("fallbackTint = HazeTint(fill)"))
        assertTrue(menu.contains(".navPopoverFrost(capture, p.dark)"))
        assertTrue(menu.contains("containerColor = Color.Transparent"))
        assertTrue(menu.contains("if (expanded && capture != null) capture.openMenus++"))
        assertTrue(menu.contains("if (solidSurface) Modifier.navDialogSurface(p)"))
        assertTrue(menu.contains("Modifier.heightIn(min = 48.dp)"))
    }
}
