package com.navtasks.app

import com.kyant.backdrop.backdrops.layerBackdrop

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate

/**
 * The entry point for ship work. AMOS uses an independent WorkOrderStore.
 * Maintenance continues to use ServiceStore/ServicesScreen unchanged.
 * Routine tasks reuse the existing TasksScreenV2 and TaskStore through
 * a presentation-only route; no storage or recurrence migration is involved.
 * Inspections remain upcoming. Nothing here mutates task history.
 */
@Composable
fun WorksHubScreen(
    services: ServiceStore,
    workOrders: WorkOrderStore,
    p: NavPalette,
    lang: UiLanguage,
    taskCount: Int,
    onBack: () -> Unit,
    onTasks: () -> Unit,
    onServices: () -> Unit,
    onAmos: () -> Unit,
    embeddedInTab: Boolean = false,
    smoothReturnTick: Int = 0,
) {
    val listState = rememberLazyListState()
    LaunchedEffect(smoothReturnTick) {
        if (smoothReturnTick > 0) listState.animateScrollToItem(0)
    }
    val today = LocalDate.now()
    val activeAmos = workOrders.entries.filter {
        it.closedAt == null && !it.completed
    }
    val activeServices = services.entries.filter { it.closedAt == null }
    val overdueAmos = activeAmos.count { it.dueDate.isBefore(today) }
    // Unplanned requests have no due date and must never inflate overdue/soon counters.
    val overdue = filterServiceOverview(activeServices, today, WorkOverviewFilter.OVERDUE).size
    val approaching = filterServiceOverview(activeServices, today, WorkOverviewFilter.SOON).size

    val workBackdrop = if (!embeddedInTab && android.os.Build.VERSION.SDK_INT >= 33)
        com.kyant.backdrop.backdrops.rememberLayerBackdrop() else null
    Box(Modifier.fillMaxSize()) {
    Column(
        Modifier.fillMaxSize().then(
            if (embeddedInTab) Modifier else Modifier.statusBarsPadding().navigationBarsPadding()
        ).then(if (workBackdrop != null) Modifier.layerBackdrop(workBackdrop) else Modifier),
    ) {
        if (!embeddedInTab) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (workBackdrop != null) Spacer(Modifier.size(44.dp))
                else NavGlassCircleButton(p, "back", onBack)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    NavBrandLabel(p)
                    Text(localized("Работы", "Work", lang), color = p.text, fontSize = 23.sp)
                }
            }
        }
        LazyColumn(
            Modifier.fillMaxSize(), state = listState,
            contentPadding = PaddingValues(start = 16.dp, top = 6.dp, end = 16.dp, bottom = if (embeddedInTab) 125.dp else 40.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                NavInfoSectionBanner(
                    p = p,
                    title = localized("Судовые работы", "Vessel work", lang),
                    description = localized(
                        "Рутинные задачи, работы AMOS и обслуживание оборудования — в одном разделе.",
                        "Routine tasks, AMOS work orders and equipment maintenance in one place.",
                        lang,
                    ),
                )
            }
            item {
                WorkHubTile(
                    p = p,
                    title = localized("Рутинные задачи", "Routine tasks", lang),
                    description = localized(
                        "Ежедневные, еженедельные, ежемесячные и разовые задачи.",
                        "Daily, weekly, monthly and one-off tasks.",
                        lang,
                    ),
                    secondary = localized(
                        "$taskCount задач", "$taskCount tasks", lang,
                    ),
                    icon = "tasks",
                    onClick = onTasks,
                )
            }
            item {
                WorkHubTile(
                    p = p,
                    title = "AMOS · Work Orders",
                    description = localized(
                        "Судовые работы AMOS: дата начала, повторения и история.",
                        "AMOS jobs: start dates, recurrence and history.",
                        lang,
                    ),
                    secondary = localized(
                        "${activeAmos.size} открыто · ${overdueAmos} просрочено",
                        "${activeAmos.size} open · ${overdueAmos} overdue", lang),
                    icon = "tasks",
                    onClick = onAmos,
                )
            }
            item {
                WorkHubTile(
                    p = p,
                    title = localized("Сервис оборудования", "Equipment maintenance", lang),
                    description = localized(
                        "Сроки обслуживания, интервалы, предупреждения и комментарии.",
                        "Due dates, intervals, reminders and service notes.",
                        lang,
                    ),
                    secondary = localized(
                        "${activeServices.size} записей · ${overdue} просрочено · ${approaching} скоро",
                        "${activeServices.size} records · ${overdue} overdue · ${approaching} soon",
                        lang,
                    ),
                    icon = "settings",
                    onClick = onServices,
                )
            }
        }
    }
    if (workBackdrop != null) {
        Box(Modifier.align(Alignment.TopStart).statusBarsPadding()
            .padding(start = 16.dp, top = 9.dp)) {
            NavLiquidGlassCircleButton(p, workBackdrop, "back", onBack)
        }
    }
    }
}

@Composable
private fun WorkHubTile(
    p: NavPalette,
    title: String,
    description: String,
    secondary: String,
    icon: String,
    onClick: (() -> Unit)?,
) {
    val surface = Modifier.fillMaxWidth().navGlass(p, radius = 19.dp, role = GlassRole.TASK)
    val touch = if (onClick != null) surface.glassClickable(onClick = onClick) else surface
    Row(
        touch.padding(horizontal = 16.dp, vertical = 17.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            Modifier.size(39.dp).navGlass(p, radius = 13.dp, role = GlassRole.CONTROL),
            contentAlignment = Alignment.Center,
        ) {
            NavGlassGlyph(icon, if (onClick != null) p.blue else p.muted,
                Modifier.size(22.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(title, color = p.text, fontSize = 16.sp,
                maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(5.dp))
            Text(description, color = p.muted, fontSize = 12.sp)
            Spacer(Modifier.height(11.dp))
            Text(secondary, color = if (onClick != null) p.blue else p.muted,
                fontSize = 11.sp)
        }
        if (onClick != null) {
            NavGlassGlyph("chevron-right", p.muted, Modifier.size(17.dp))
        }
    }
}
