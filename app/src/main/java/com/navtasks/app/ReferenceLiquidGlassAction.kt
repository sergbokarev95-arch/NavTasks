package com.navtasks.app

import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop

/**
 * Reference headers and the equipment action row sit on the plain page
 * background, never on a card. Record that identical day/night scene in a
 * separate sibling layer so an inline optical button cannot sample itself.
 * Keeping the source local also works in the independent document dialog
 * and keeps existing menu anchors and scrolling layout intact.
 */
@Composable
internal fun ReferenceLiquidGlassAction(
    p: NavPalette, kind: String, label: String, onClick: () -> Unit,
) {
    val backdrop = if (Build.VERSION.SDK_INT >= 33) rememberLayerBackdrop() else null
    Box(
        Modifier.size(NavLiquidGlassActionTokens.compactDiameterDp.dp)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        if (backdrop != null) {
            Box(Modifier.matchParentSize().layerBackdrop(backdrop)) {
                NavGlassBackdrop(p)
            }
            NavLiquidGlassCircleButton(p, backdrop, kind, onClick)
        } else {
            NavGlassCircleButton(p, kind, onClick)
        }
    }
}
