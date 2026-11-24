package com.navtasks.app

import kotlin.math.abs

/**
 * HONOR Calendar-style responsive dock optics.
 *
 * The lens expands ONLY while it is physically moving. The same spring that
 * changes horizontal position provides velocity for width/height and glass
 * contrast; there is no grow-then-shrink animation, separate scale coroutine,
 * landing delay or timer to interrupt on rapid consecutive taps.
 *
 * Velocity is in dock tab slots per second, NOT physical pixels, so motion
 * looks consistent across screen sizes and density settings.
 */
internal object NavLiquidDockMotion {
    const val positionStiffness = 980f
    const val positionDamping = .78f
    const val fullExpansionVelocity = 19f

    fun motionFraction(velocitySlotsPerSecond: Float): Float {
        if (!velocitySlotsPerSecond.isFinite()) return 0f
        return (abs(velocitySlotsPerSecond) / fullExpansionVelocity).coerceIn(0f, 1f)
    }

    fun lensScale(velocitySlotsPerSecond: Float): Float =
        1f + (NavLiquidDockTabs.movingScale - 1f) * motionFraction(velocitySlotsPerSecond)

    fun lensMatte(velocitySlotsPerSecond: Float): Float =
        1f - .70f * motionFraction(velocitySlotsPerSecond)
}
