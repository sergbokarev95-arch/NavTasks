package com.navtasks.app

import androidx.compose.ui.graphics.Color

/**
 * Opaque, static UI surfaces. These are NOT optical Liquid Glass.
 * Real refraction remains solely in the floating controls/dock.
 *
 * Ordinary card materials are opaque so the Liquid Glass dock can capture
 * readable content below it. Day #02 keeps all original light card gradients;
 * Night #09 replaces navy cards with flat neutral charcoal tones and quiet
 * neutral-gray borders. NAVIGATION/SELECTED and action handlers are untouched.
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
            Color(0xFF1D2126) to Color(0xFF1D2126)
        } else {
            Color(0xFFE8F5FC) to Color(0xFFFFFFFF)
        }
        GlassRole.TASK -> if (p.dark) {
            Color(0xFF282D33) to Color(0xFF282D33)
        } else {
            Color(0xFFE5F3FC) to Color(0xFFFFFFFF)
        }
        GlassRole.NESTED_TASK -> if (p.dark) {
            Color(0xFF282D33) to Color(0xFF282D33)
        } else {
            Color(0xFFE6EEFA) to Color(0xFFF9FCFF)
        }
        GlassRole.CONTROL -> if (p.dark) {
            Color(0xFF282D33) to Color(0xFF282D33)
        } else {
            Color(0xFFEBF4FA) to Color(0xFFFDFEFF)
        }
        // These are deliberately translucent control states, or the old
        // dock fallback. They are not static background cards.
        GlassRole.NAVIGATION, GlassRole.SELECTED -> return null
    }
    // Night #09 replaces the old bright blue/steel contour with one matte
    // neutral stroke. Day border gradients stay byte-for-byte unchanged.
    val top = if (p.dark) Color(0xFF40464D) else Color(0xFFFCFFFF)
    val middle = if (p.dark) Color(0xFF40464D) else Color(0xFFB9CDDE)
    val bottom = if (p.dark) Color(0xFF40464D) else Color(0xFFB0C7D9)
    return NavSolidCardTokens(colors.first, colors.second, top, middle, bottom)
}
