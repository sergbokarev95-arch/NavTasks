package com.navtasks.app

import android.graphics.RenderEffect as AndroidRenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.asComposeRenderEffect

/** Bound the optical bend to avoid 0.4.57's moving-text shimmer. */
internal const val NAV_DOCK_OPTICAL_RIM_DP = 5f
internal const val NAV_DOCK_OPTICAL_BEND_DP = 1.2f

/**
 * Pure capability gate. Older Android versions keep the existing Haze glass,
 * and invalid / zero-sized render layers never attempt to allocate a shader.
 */
internal fun canRenderNavGlassRefraction(api: Int, widthPx: Float, heightPx: Float): Boolean =
    api >= Build.VERSION_CODES.TIRAMISU &&
        widthPx.isFinite() && heightPx.isFinite() && widthPx >= 40f && heightPx >= 24f

/**
 * Original, local capsule optics for the small DOCK BACKDROP layer, not its
 * icons, touch targets, selected-tab Canvas, or any app content.
 *
 * The input is the already-blurred Haze material, not an image from another
 * application or the system wallpaper. Near the perimeter we bend sampling
 * coordinates toward the inside of the capsule. This makes content transitions
 * visibly bend through its curved glass rim without a coloured overlay.
 *
 * Keep this independent of Telegram's GPL implementation. Never compile or
 * apply this shader on Android < 13 (API 33).
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
internal fun createNavDockRefraction(
    widthPx: Float,
    heightPx: Float,
    density: Float,
): RenderEffect? {
    if (!canRenderNavGlassRefraction(Build.VERSION.SDK_INT, widthPx, heightPx)) return null
    return try {
        val shader = RuntimeShader(DOCK_OPTICS_AGSL)
        shader.setFloatUniform("viewport", widthPx, heightPx)
        shader.setFloatUniform("rimWidth", NAV_DOCK_OPTICAL_RIM_DP * density.coerceAtLeast(0.5f))
        shader.setFloatUniform("bendAmount", NAV_DOCK_OPTICAL_BEND_DP * density.coerceAtLeast(0.5f))
        AndroidRenderEffect.createRuntimeShaderEffect(shader, "blurredImage")
            .asComposeRenderEffect()
    } catch (_: RuntimeException) {
        // The underlying Haze layer remains visible on unsupported GPU drivers.
        null
    }
}

/**
 * Capsule-shaped refraction, independently authored for NavTasks.
 * Signed distance to a pill and its local gradient define an inward sampling
 * bend in a thin peripheral band (5dp, up to 1.2dp displacement).
 * This limits the moving-letter highlight distortion observed in 0.4.57.
 * Center pixels stay untouched for legibility.
 */
private const val DOCK_OPTICS_AGSL = """
uniform shader blurredImage;
uniform float2 viewport;
uniform float rimWidth;
uniform float bendAmount;

float capsuleDistance(float2 xy) {
    float2 halfSize = viewport * 0.5;
    float radius = halfSize.y;
    float2 q = abs(xy - halfSize) - (halfSize - radius);
    return length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - radius;
}

half4 main(float2 xy) {
    float dist = capsuleDistance(xy);
    float innerRise = smoothstep(-rimWidth, -1.5, dist);
    float outerFall = 1.0 - smoothstep(-1.5, 0.0, dist);
    float edgeBand = innerRise * outerFall;

    float2 dx = float2(1.0, 0.0);
    float2 dy = float2(0.0, 1.0);
    float2 slope = float2(
        capsuleDistance(xy + dx) - capsuleDistance(xy - dx),
        capsuleDistance(xy + dy) - capsuleDistance(xy - dy)
    );
    float2 normal = slope / max(length(slope), 0.001);
    float2 shifted = xy - normal * bendAmount * edgeBand;
    float2 sampleAt = clamp(shifted, float2(0.5), viewport - float2(0.5));

    return blurredImage.eval(sampleAt);
}
""";
