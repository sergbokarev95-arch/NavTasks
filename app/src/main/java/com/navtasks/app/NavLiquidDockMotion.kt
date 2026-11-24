package com.navtasks.app

/**
 * One responsive spring for horizontal travel plus a SHORT overlapping
 * deformation envelope. The envelope is not queued after a landing.
 *
 * A slow/single tap has a clearly visible expansion. Repeated taps during
 * an unfinished movement reuse the current scale without forcing the full
 * growth cycle again. All dimensions remain relative to the accepted dock.
 */
internal object NavLiquidDockMotion {
    const val positionStiffness = 860f
    const val positionDamping = .78f
    const val expansionRiseMillis = 65
    const val expansionFallMillis = 105
    const val rapidRetargetWindowMillis = 180L

    fun peakScaleForTap(distanceSlots: Float, rapidRetarget: Boolean): Float {
        if (!distanceSlots.isFinite() || distanceSlots <= .01f) return 1f
        // A one-tab tap visibly grows to 1.27x; two or more may reach 1.30x.
        // Fast retargets shouldn't repeatedly restart a large enlargement.
        return if (rapidRetarget) 1.10f
            else (1.24f + .03f * distanceSlots.coerceIn(0f, 2f))
                .coerceAtMost(NavLiquidDockTabs.movingScale)
    }

    fun lensMatteFromScale(scale: Float): Float {
        if (!scale.isFinite()) return 1f
        val fraction = ((scale - 1f) /
            (NavLiquidDockTabs.movingScale - 1f)).coerceIn(0f, 1f)
        return 1f - .70f * fraction
    }
}
