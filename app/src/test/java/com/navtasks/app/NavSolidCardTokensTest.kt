package com.navtasks.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NavSolidCardTokensTest {
    private val staticRoles = listOf(
        GlassRole.PANEL, GlassRole.TASK,
        GlassRole.NESTED_TASK, GlassRole.CONTROL
    )

    @Test fun everyStaticRoleUsesOpaqueGradientInBothThemes() {
        for (dark in listOf(false, true)) {
            val palette = navPalette(dark)
            for (role in staticRoles) {
                val tokens = requireNotNull(navSolidCardTokens(palette, role))
                assertEquals("start/$dark/$role", 1f, tokens.left.alpha, 0f)
                assertEquals("end/$dark/$role", 1f, tokens.right.alpha, 0f)
                assertNotEquals("gradient/$dark/$role", tokens.left, tokens.right)
                assertTrue(tokens.borderTop != tokens.borderBottom)
            }
        }
    }

    @Test fun opticalStatesRetainOriginalTranslucentRendering() {
        for (dark in listOf(false, true)) {
            val p = navPalette(dark)
            assertNull(navSolidCardTokens(p, GlassRole.NAVIGATION))
            assertNull(navSolidCardTokens(p, GlassRole.SELECTED))
        }
    }

    @Test fun rolesRemainVisuallyDistinctNotCopiedIndiscriminately() {
        val p = navPalette(true)
        val task = requireNotNull(navSolidCardTokens(p, GlassRole.TASK))
        val nested = requireNotNull(navSolidCardTokens(p, GlassRole.NESTED_TASK))
        val panel = requireNotNull(navSolidCardTokens(p, GlassRole.PANEL))
        assertNotEquals(task.left, nested.left)
        assertNotEquals(task.left, panel.left)
    }
}
