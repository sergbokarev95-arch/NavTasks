package com.navtasks.app

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Source-of-truth pixel checks for user-selected 02 (Day) and 09 (Night).
 * Do not silently reintroduce navy gradients on dark static cards.
 * Keep the existing translucent navigation lens and solid blue selection separate.
 */
class NavSolidCardTokensTest {
    private val staticRoles = listOf(
        GlassRole.PANEL, GlassRole.TASK, GlassRole.NESTED_TASK, GlassRole.CONTROL,
    )

    @Test fun day02KeepsOriginalCardGradientsAndNoOpticalChanges() {
        val p = navPalette(false)
        assertEquals(Color(0xFFE6E8EC), p.background)
        val panel = requireNotNull(navSolidCardTokens(p, GlassRole.PANEL))
        val task = requireNotNull(navSolidCardTokens(p, GlassRole.TASK))
        assertEquals(Color(0xFFE8F5FC), panel.left)
        assertEquals(Color.White, panel.right)
        assertEquals(Color(0xFFE5F3FC), task.left)
        assertEquals(Color.White, task.right)
        for (role in staticRoles) {
            val t = requireNotNull(navSolidCardTokens(p, role))
            assertEquals(1f, t.left.alpha, 0f)
            assertEquals(1f, t.right.alpha, 0f)
            assertNotEquals(t.left, t.right)
            assertNotEquals(t.borderTop, t.borderBottom)
        }
    }

    @Test fun night09HasOnlyNeutralFlatCardsAndOneQuietBorder() {
        val p = navPalette(true)
        assertEquals(Color.Black, p.background)
        for (role in staticRoles) {
            val t = requireNotNull(navSolidCardTokens(p, role))
            val expected = if (role == GlassRole.PANEL)
                Color(0xFF1D2126) else Color(0xFF282D33)
            assertEquals("night fill/$role", expected, t.left)
            assertEquals("night no-gradient/$role", expected, t.right)
            assertEquals("night neutral border top/$role",
                Color(0xFF40464D), t.borderTop)
            assertEquals("night neutral border middle/$role",
                Color(0xFF40464D), t.borderMid)
            assertEquals("night neutral border bottom/$role",
                Color(0xFF40464D), t.borderBottom)
        }
        assertNotEquals(
            navSolidCardTokens(p, GlassRole.PANEL)?.left,
            navSolidCardTokens(p, GlassRole.TASK)?.left
        )
    }

    @Test fun opticalDockAndActiveBlueRemainIndependentOfStaticCards() {
        for (dark in listOf(false, true)) {
            val p = navPalette(dark)
            assertNull(navSolidCardTokens(p, GlassRole.NAVIGATION))
            assertNull(navSolidCardTokens(p, GlassRole.SELECTED))
            assertEquals(Color(0xFF0071E3), p.selectionFill)
            assertEquals(Color.White, p.selectionContent)
        }
    }
}
