package com.navtasks.app

import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

/**
 * Work > Routine tasks. Reuses the exact existing TasksScreenV2 and TaskEditor.
 * This is presentation-only: TaskStore, TaskRun, recurrence, due reminders,
 * profiles and export/import continue on their original code paths.
 *
 * The old Tasks dock tab remains during this isolated migration step, so
 * existing navigation is never abruptly removed before Reference is introduced.
 */
@Composable
internal fun RoutinesModuleScreen(
    store: TaskStore,
    filter: Frequency,
    onFilter: (Frequency) -> Unit,
    p: NavPalette,
    lang: UiLanguage,
    onBack: () -> Unit,
    onEdit: (TaskTemplate) -> Unit,
    onAdd: () -> Unit,
) {
    val backdrop = if (Build.VERSION.SDK_INT >= 33) rememberLayerBackdrop() else null
    val hazeState = remember { HazeState() }
    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize()
                .then(if (backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier)
                .statusBarsPadding().navigationBarsPadding()
                .hazeSource(hazeState),
        ) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (backdrop != null) Spacer(Modifier.size(44.dp))
                else NavGlassCircleButton(p, "back", onBack)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    NavBrandLabel(p)
                    Text(localized("Рутинные задачи", "Routine tasks", lang),
                        color = p.text, fontSize = 23.sp)
                }
            }
            Box(Modifier.fillMaxWidth().weight(1f)) {
                TasksScreenV2(
                    store = store, filter = filter, onFilter = onFilter,
                    p = p, lang = lang, onEdit = onEdit,
                )
            }
        }
        if (backdrop != null) {
            Box(
                Modifier.align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(start = 16.dp, top = 9.dp)
            ) {
                NavLiquidGlassCircleButton(p, backdrop, "back", onBack)
            }
        }
        Box(
            Modifier.align(Alignment.BottomEnd).navigationBarsPadding()
                .padding(end = 19.dp, bottom = 24.dp),
        ) {
            if (backdrop != null) {
                NavLiquidGlassAddButton(p, backdrop, onAdd)
            } else {
                NavGlassAddButton(p, hazeState, onAdd)
            }
        }
    }
}
