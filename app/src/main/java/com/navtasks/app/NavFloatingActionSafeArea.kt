package com.navtasks.app

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.calculateBottomPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Scrollable pages are captured under the floating Liquid Glass controls,
 * rather than being laid out above a solid Scaffold bottom bar.
 *
 * Reserve enough *scrollable end space* to lift the final tappable card above
 * the actual control (button bottom offset + its full 54dp diameter + a 20dp
 * visual/touch gap). The dock tabs draw edge-to-edge, so add Android's actual
 * bottom gesture inset. Standalone Handover already uses navigationBarsPadding
 * on its parent; counting the inset twice there would create excess space.
 *
 * Only list padding changes. Floating button positions and optics are frozen.
 */
internal object NavFloatingActionSafeArea {
    const val actionDiameterDp = 54f
    const val tabButtonBottomDp = 83f
    const val standaloneButtonBottomDp = 18f
    const val touchGapDp = 20f

    fun listBottomPaddingDp(navigationBarInsetDp: Float, embeddedInTab: Boolean): Float {
        val bottomOffset = if (embeddedInTab) tabButtonBottomDp else standaloneButtonBottomDp
        val unconsumedInset = if (embeddedInTab) navigationBarInsetDp.coerceAtLeast(0f) else 0f
        return bottomOffset + actionDiameterDp + touchGapDp + unconsumedInset
    }
}

@Composable
internal fun navFloatingActionBottomPadding(embeddedInTab: Boolean): Dp {
    val systemBottomInsetDp = WindowInsets.navigationBars
        .asPaddingValues().calculateBottomPadding().value
    return NavFloatingActionSafeArea
        .listBottomPaddingDp(systemBottomInsetDp, embeddedInTab).dp
}
