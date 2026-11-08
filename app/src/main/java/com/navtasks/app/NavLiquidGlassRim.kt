package com.navtasks.app

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * The ONLY user-requested optical change: soften a white-white outline in
 * Day without altering the shader, refraction, lens, highlight or Night.
 * All circular actions and the dock use this one brush.
 */
internal fun navLiquidGlassRim(dark: Boolean): Brush =
    Brush.verticalGradient(
        listOf(
            if (dark) Color.White.copy(alpha = .36f)
                else Color(0xFF9AAEC0).copy(alpha = .48f),
            Color(0xFF8FAFCB).copy(alpha = if (dark) .12f else .18f),
            if (dark) Color.White.copy(alpha = .17f)
                else Color(0xFF91A7BB).copy(alpha = .34f),
        )
    )
