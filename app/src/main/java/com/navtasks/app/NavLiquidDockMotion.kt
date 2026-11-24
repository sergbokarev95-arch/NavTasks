package com.navtasks.app

/**
 * One responsive spring for horizontal travel plus a SHORT overlapping
 * deformation envelope. Touchdown briefly dips below 1x and rebounds;
 * the compression starts during the tail of the horizontal hop, with no
 * separate post-landing delay or queued animation.
 *
 * A slow/single tap has a clearly visible expansion. Repeated taps during
 * an unfinished movement reuse the current scale without forcing the full
 * growth cycle again. All dimensions remain relative to the accepted dock.
 */
internal object NavLiquidDockMotion {
    // Beta19: subtly slower, more damped travel without losing interruptibility.
    const val positionStiffness = 660f
    const val positionDamping = .86f

    // One continuous scale envelope runs concurrently with horizontal travel.
    // The capsule crosses its natural 1.0x size near touchdown, compresses
    // to 0.85x as it settles, then immediately springs visually back to 1.0x.
    const val expansionRiseMillis = 86
    const val expansionFallMillis = 100
    const val landingCompressMillis = 62
    const val landingRecoverMillis = 96
    const val landingSquashScale = .85f

    // Very rapid retargets remain light: never force a complete landing pulse.
    const val rapidRiseMillis = 50
    const val rapidRecoverMillis = 126
    const val rapidRetargetWindowMillis = 180L

    const val normalLandingStartMillis = expansionRiseMillis + expansionFallMillis
    const val normalCompressionEndMillis = normalLandingStartMillis + landingCompressMillis
    const val normalMotionEndMillis = normalCompressionEndMillis + landingRecoverMillis
    const val rapidMotionEndMillis = rapidRiseMillis + rapidRecoverMillis

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
