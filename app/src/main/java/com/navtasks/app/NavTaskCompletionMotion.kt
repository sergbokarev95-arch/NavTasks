package com.navtasks.app

/**
 * Single-phase success feedback for all Today run completions.
 *
 * Touch -> solid mint + tick for 190ms -> simple downward fade over 250ms.
 * No double flash, vertical recoil or horizontal stretch. Persistence and
 * completion-date semantics remain owned by the original TaskStore.
 */
internal object NavTaskCompletionMotion {
    const val greenHoldMillis: Long = 190L
    const val exitMillis: Int = 250
    const val dropDistanceDp: Float = 48f
}
