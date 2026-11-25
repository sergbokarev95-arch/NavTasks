package com.navtasks.app

import androidx.compose.ui.graphics.Color

/** Today task rows only; colors sampled approximately from the owner's HONOR reference. */
internal object NavTodayVisualTokens {
    const val checkDiameterDp = 22f
    const val checkStrokeDp = 1.6f
    const val checkedGlyphDp = 15f
    const val cardRadiusDp = 20f

    const val selectedDateRimDp = 1.5f

    // Reuse the approved coral exactly, without modifying the dock.
    fun accentInk(dark: Boolean): Color = NavLiquidDockTabs.activeInk(dark)

    fun workNavigateNightSurface(): Color = Color(0xFF272729)
    fun workNavigateNightRim(): Color = Color(0xFF38383A)

    // Scoped checklist surfaces: neutral graphite inside the matte parent card.
    fun checklistInsetSurface(dark: Boolean): Color =
        if (dark) Color(0xFF272729) else Color(0xFFF2F3F7)

    fun checklistInsetRim(dark: Boolean): Color =
        if (dark) Color(0xFF38383A) else Color(0xFFE0E2E8)

    fun uncheckedInk(dark: Boolean): Color =
        if (dark) Color(0xFF6B6B6D) else Color(0xFFA0A0A2)

    fun cardSurface(dark: Boolean): Color =
        if (dark) Color(0xFF202022) else Color.White
}
