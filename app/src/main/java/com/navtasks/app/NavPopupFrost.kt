package com.navtasks.app

import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindowProvider

/** Only native floating dialogs. Existing Liquid Glass dock shader is untouched. */
internal object NavPopupFrostTokens {
    const val blurRadiusDp = 32f
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
    if (!frosted) return navDialogSurface(p, radius)
    val shape = RoundedCornerShape(radius)
    val fill = if (p.dark) Color(0xE025292E) else Color(0xD9F9FAFC)
    val rim = Brush.verticalGradient(
        if (p.dark) listOf(
            Color.White.copy(alpha = .51f),
            Color(0xFF6F7E8D).copy(alpha = .42f),
            Color(0xFF485767).copy(alpha = .52f),
        ) else listOf(
            Color.White.copy(alpha = .88f),
            Color(0xFFBAC8D4).copy(alpha = .68f),
            Color(0xFF91A8B9).copy(alpha = .52f),
        )
    )
    return this.clip(shape).background(fill, shape).border(1.dp, rim, shape)
}
