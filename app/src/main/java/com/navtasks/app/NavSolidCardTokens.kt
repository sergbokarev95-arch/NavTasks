package com.navtasks.app

import androidx.compose.ui.graphics.Color

/**
 * Opaque static surfaces. Floating Liquid Glass optics are separate and
 * must NOT inherit these tokens.
 *
 * Day #03: a quiet HONOR Calendar-style #F2F3F7 page with truly white
 * cards and fields. No cyan/blue horizontal gradients or blue-tinted rims.
 * Night #09 retains the existing matte graphite surfaces, byte-for-byte.
 */
internal data class NavSolidCardTokens(
    val left: Color,
    val right: Color,
    val borderTop: Color,
    val borderMid: Color,
    val borderBottom: Color,
)

internal fun navSolidCardTokens(p: NavPalette, role: GlassRole): NavSolidCardTokens? {
    // NAVIGATION belongs to the optical dock/fallback; SELECTED retains the
    // semantic active-blue state, not the normal card background.
    if (role == GlassRole.NAVIGATION || role == GlassRole.SELECTED) return null

    if (!p.dark) {
        return NavSolidCardTokens(
            left = Color.White,
            right = Color.White,
            borderTop = Color(0xFFF8F9FB),
            borderMid = Color(0xFFE0E2E8),
            borderBottom = Color(0xFFE0E2E8),
        )
    }

    val surface = if (role == GlassRole.PANEL)
        Color(0xFF1D2126) else Color(0xFF282D33)
    val rim = Color(0xFF40464D)
    return NavSolidCardTokens(
        left = surface,
        right = surface,
        borderTop = rim,
        borderMid = rim,
        borderBottom = rim,
    )
}
