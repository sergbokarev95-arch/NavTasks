package com.navtasks.app

import androidx.compose.ui.graphics.Color

/** Today task rows only; colors sampled approximately from the owner's HONOR reference. */
internal object NavTodayVisualTokens {
    const val checkDiameterDp = 22f
    const val checkStrokeDp = 1.6f
    const val checkedGlyphDp = 15f
    const val cardRadiusDp = 20f

    fun uncheckedInk(dark: Boolean): Color =
        if (dark) Color(0xFF6B6B6D) else Color(0xFFA0A0A2)

    fun cardSurface(dark: Boolean): Color =
        if (dark) Color(0xFF202022) else Color.White
}
