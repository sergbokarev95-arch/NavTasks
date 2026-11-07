package com.navtasks.app

import androidx.compose.ui.graphics.Color

/**
 * Opaque, static UI surfaces. These are NOT optical Liquid Glass.
 * Real refraction remains solely in the floating controls/dock.
 *
 * All four ordinary roles have two fully opaque tonal anchors, which makes
 * their bright borders and gradients legible when sampled by Backdrop.
 * Existing component geometry, backgrounds for NAVIGATION/SELECTED,
 * action handlers and task-state colors are deliberately not affected.
 */
internal data class NavSolidCardTokens(
    val left: Color,
    val right: Color,
    val borderTop: Color,
    val borderMid: Color,
    val borderBottom: Color,
)

internal fun navSolidCardTokens(p: NavPalette, role: GlassRole): NavSolidCardTokens? {
    val colors = when (role) {
        GlassRole.PANEL -> if (p.dark) {
            Color(0xFF153247) to Color(0xFF10202E)
        } else {
            Color(0xFFE8F5FC) to Color(0xFFFFFFFF)
        }
        GlassRole.TASK -> if (p.dark) {
            Color(0xFF0F2D42) to Color(0xFF111F2E)
        } else {
            Color(0xFFE5F3FC) to Color(0xFFFFFFFF)
        }
        GlassRole.NESTED_TASK -> if (p.dark) {
            Color(0xFF21364E) to Color(0xFF152638)
        } else {
            Color(0xFFE6EEFA) to Color(0xFFF9FCFF)
        }
        GlassRole.CONTROL -> if (p.dark) {
            Color(0xFF1A2B3C) to Color(0xFF142330)
        } else {
            Color(0xFFEBF4FA) to Color(0xFFFDFEFF)
        }
        // These are deliberately translucent control states, or the old
        // dock fallback. They are not static background cards.
        GlassRole.NAVIGATION, GlassRole.SELECTED -> return null
    }
    val top = if (p.dark) Color(0xFF7893A7) else Color(0xFFFCFFFF)
    val middle = if (p.dark) Color(0xFF506B80) else Color(0xFFB9CDDE)
    val bottom = if (p.dark) Color(0xFF38546A) else Color(0xFFB0C7D9)
    return NavSolidCardTokens(colors.first, colors.second, top, middle, bottom)
}
