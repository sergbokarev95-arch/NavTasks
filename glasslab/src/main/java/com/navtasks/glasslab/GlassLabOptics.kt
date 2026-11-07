package com.navtasks.glasslab

/**
 * Lab-only material parameters in dp. Keep the recorded scene, text/icons and
 * NavTasks production dock untouched while testing card-outline refraction.
 *
 * Lens refraction is concentrated at the rim; lowering the pre-lens blur keeps
 * one-pixel card borders visible enough to bend instead of disappearing.
 */
internal data class GlassLabLens(
    val blurDp: Float,
    val refractionHeightDp: Float,
    val refractionAmountDp: Float
)

internal object GlassLabOptics {
    val bar = GlassLabLens(blurDp = 2.0f, refractionHeightDp = 12.0f, refractionAmountDp = 36.0f)
    val selection = GlassLabLens(blurDp = 1.5f, refractionHeightDp = 10.0f, refractionAmountDp = 18.0f)
}
