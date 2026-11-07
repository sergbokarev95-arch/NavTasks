package com.navtasks.app

/**
 * Glass Dock Preview R2 optical tuning, in dp. Backdrop records the original
 * screen content once; this controls only how the dock samples that content.
 *
 * R1 blur 2dp/1.5dp left white task titles legible beneath the dock, visually
 * competing with tab captions. Softening to 5dp/4dp reduces interference,
 * while keeping the proven refraction height and amount unchanged.
 */
internal data class NavLiquidDockLens(
    val blurDp: Float,
    val refractionHeightDp: Float,
    val refractionAmountDp: Float
)

internal object NavLiquidDockOptics {
    val bar = NavLiquidDockLens(blurDp = 5.0f, refractionHeightDp = 12.0f, refractionAmountDp = 36.0f)
    val selection = NavLiquidDockLens(blurDp = 4.0f, refractionHeightDp = 10.0f, refractionAmountDp = 18.0f)
}
