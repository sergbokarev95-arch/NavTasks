package com.navtasks.app

import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.WindowManager
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.hazeEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindowProvider

/** Neutral HONOR-reference popup material; no changes to dock optics or global palette. */
internal object NavPopupFrostTokens {
    const val blurRadiusDp = 10f
    const val screenBlurRadiusDp = 1.5f
    const val menuBlurRadiusDp = 8f
    const val menuTintAlpha = .96f
    const val screenBlurAnimationMillis = 150
    const val tintAlpha = .90f
    const val rimWidthDp = .6f

    fun fill(dark: Boolean): Color =
        if (dark) Color(0xFF222222) else Color(0xFFF6F7F9)

    // Menu-specific tones: native dialog surfaces keep their Beta25 palette.
    fun menuFill(dark: Boolean): Color =
        if (dark) Color(0xFF222222) else Color(0xFFF5F6F8)

    fun menuRim(dark: Boolean): Color =
        if (dark) Color(0xFF424242) else Color.White

    fun menuShadowDp(dark: Boolean): Float = if (dark) 5f else 12f

    fun rim(dark: Boolean): Color =
        if (dark) Color(0xFF4A4A4A).copy(alpha = .65f) else Color.White.copy(alpha = .88f)

    fun divider(dark: Boolean): Color =
        if (dark) Color.White.copy(alpha = .075f) else Color.Black.copy(alpha = .075f)
}

/**
 * Dedicated source for popup windows. Do not share the dock's HazeState:
 * Haze 1.7.1 resolves coordinates across windows independently for this source.
 * Record the Activity content only while an anchored menu is open.
 */
internal class NavPopoverCapture {
    val haze = HazeState()
    var openMenus by mutableIntStateOf(0)
}

internal val LocalNavPopoverCapture = compositionLocalOf<NavPopoverCapture?> { null }

@Composable
internal fun NavPopoverBackdropHost(content: @Composable () -> Unit) {
    val capture = remember { NavPopoverCapture() }
    val screenBlur by animateDpAsState(
        targetValue = if (capture.openMenus > 0) NavPopupFrostTokens.screenBlurRadiusDp.dp else 0.dp,
        animationSpec = tween(NavPopupFrostTokens.screenBlurAnimationMillis),
        label = "navPopoverScreenBlur",
    )
    CompositionLocalProvider(LocalNavPopoverCapture provides capture) {
        // The outer blur affects only Activity content, including floating dock/buttons.
        // DropdownMenu is a separate popup window: its text and hit targets stay sharp.
        // Capture the original page inside the display blur so popup material is unchanged.
        Box(Modifier.fillMaxSize().blur(screenBlur, BlurredEdgeTreatment.Rectangle).then(
            if (capture.openMenus > 0) Modifier.hazeSource(capture.haze) else Modifier
        )) {
            content()
        }
    }
}

/** Day retains a trace of frost. Night uses an opaque graphite surface, not a second blur layer. */
internal fun Modifier.navPopoverFrost(capture: NavPopoverCapture?, dark: Boolean): Modifier {
    val fill = NavPopupFrostTokens.menuFill(dark)
    if (dark || capture == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.S)
        return background(fill)
    return this.background(fill).hazeEffect(
        state = capture.haze,
        style = HazeStyle(
            backgroundColor = fill,
            blurRadius = NavPopupFrostTokens.menuBlurRadiusDp.dp,
            tints = listOf(HazeTint(fill.copy(alpha = NavPopupFrostTokens.menuTintAlpha))),
            fallbackTint = HazeTint(fill),
            noiseFactor = 0f,
        ),
    )
}

/**
 * System window-background blur samples the ACTUAL Activity behind a dialog,
 * avoiding the incorrect coordinates when Backdrop is drawn in a different window.
 * This does not emulate our dock lens/refraction: the soft rim is visual only.
 */
@Composable
internal fun navPopupFrostAvailable(): Boolean {
    val context = LocalContext.current
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
        runCatching {
            context.getSystemService(WindowManager::class.java)?.isCrossWindowBlurEnabled == true
        }.getOrDefault(false)
}

@Composable
internal fun NavApplyDialogFrost(enabled: Boolean, radius: Dp = 24.dp) {
    if (!enabled) return
    val view = LocalView.current
    val density = LocalDensity.current
    DisposableEffect(view, enabled, radius) {
        val window = (view.parent as? DialogWindowProvider)?.window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val radiusPx = with(density) { radius.toPx() }
            window?.setBackgroundDrawable(GradientDrawable().apply {
                setColor(android.graphics.Color.TRANSPARENT)
                cornerRadius = radiusPx
            })
            window?.setBackgroundBlurRadius(with(density) {
                NavPopupFrostTokens.blurRadiusDp.dp.roundToPx()
            })
        }
        onDispose {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                window?.setBackgroundBlurRadius(0)
            }
        }
    }
}

/**
 * High-contrast frosted card if Android window blur is active. Opaque fallback
 * is mandatory when the manufacturer or battery saver disables background blur.
 */
internal fun Modifier.navPopupGlassSurface(
    p: NavPalette,
    frosted: Boolean,
    radius: Dp = 24.dp,
): Modifier {
    val shape = RoundedCornerShape(radius)
    val base = NavPopupFrostTokens.fill(p.dark)
    val fill = if (frosted) base.copy(alpha = NavPopupFrostTokens.tintAlpha) else base
    return this.clip(shape).background(fill, shape)
        .border(NavPopupFrostTokens.rimWidthDp.dp, NavPopupFrostTokens.rim(p.dark), shape)
}
