package com.navtasks.app

import com.kyant.backdrop.backdrops.layerBackdrop

import androidx.compose.foundation.layout.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
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
            contentPadding = PaddingValues(start = NavEventLayout.screenInset, top = 6.dp, end = NavEventLayout.screenInset, bottom = if (embeddedInTab) 125.dp else 40.dp),
            verticalArrangement = Arrangement.spacedBy(NavEventLayout.sectionGap),
        ) {
            item {
                WorkHubTile(
                    p = p,
                    title = localized("Рутинные задачи", "Routine tasks", lang),
                    description = localized(
                        "Ежедневные, еженедельные, ежемесячные и разовые.",
                        "Daily, weekly, monthly and one-off tasks.",
                        lang,
                    ),
                    stats = listOf(WorkHubStat(localized(
                        "$taskCount задач", "$taskCount tasks", lang,
                    ))),
                    icon = "tasks",
                    onClick = onTasks,
                )
            }
            item {
                WorkHubTile(
                    p = p,
                    title = "AMOS / By Crew",
                    description = localized(
                        "Работы AMOS и проверки экипажем.",
                        "AMOS work orders and crew checks.",
                        lang,
                    ),
                    stats = listOf(
                        WorkHubStat(localized("${activeAmos.size} открыто", "${activeAmos.size} open", lang)),
                        WorkHubStat(localized("$overdueAmos просрочено", "$overdueAmos overdue", lang),
                            if (overdueAmos > 0) WorkHubTone.OVERDUE else WorkHubTone.NORMAL),
                    ),
                    icon = "work-order",
                    onClick = onAmos,
                )
            }
            item {
                WorkHubTile(
                    p = p,
                    title = localized("Сервис оборудования", "Equipment maintenance", lang),
                    description = localized(
                        "Сроки, интервалы и обслуживание.",
                        "Due dates, intervals and maintenance.",
                        lang,
                    ),
                    stats = listOf(
                        WorkHubStat(localized("${activeServices.size} записей", "${activeServices.size} records", lang)),
                        WorkHubStat(localized("$overdue просрочено", "$overdue overdue", lang),
                            if (overdue > 0) WorkHubTone.OVERDUE else WorkHubTone.NORMAL),
                        WorkHubStat(localized("$approaching скоро", "$approaching soon", lang),
                            if (approaching > 0) WorkHubTone.SOON else WorkHubTone.NORMAL),
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

internal enum class WorkHubTone { NORMAL, OVERDUE, SOON }
internal data class WorkHubStat(val label: String, val tone: WorkHubTone = WorkHubTone.NORMAL)

/** The three destinations share the matte Today/Personal surfaces and spacing.
 * Counts are presentation only; a zero overdue/soon count is never a warning.
 */
@Composable
private fun WorkHubTile(
    p: NavPalette,
    title: String,
    description: String,
    stats: List<WorkHubStat>,
    icon: String,
    onClick: (() -> Unit)?,
) {
    val surface = Modifier.fillMaxWidth().personalNoteCard(p, NavEventLayout.cardRadius)
    val touch = if (onClick != null) surface.glassClickable(onClick = onClick) else surface
    Row(
        touch.heightIn(min = 96.dp)
            .padding(horizontal = NavEventLayout.cardInset, vertical = NavEventLayout.groupGap),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(NavEventLayout.groupGap),
    ) {
        Box(
            Modifier.size(42.dp).personalNoteCard(p, 13.dp, nested = true),
            contentAlignment = Alignment.Center,
        ) {
            NavGlassGlyph(icon, if (onClick != null) NavTodayVisualTokens.accentInk(p.dark) else p.muted,
                Modifier.size(23.dp))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, color = p.text, fontSize = 16.sp, fontWeight = FontWeight.Medium,
                maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(description, color = p.muted, fontSize = 12.sp, lineHeight = 17.sp)
            Text(buildAnnotatedString {
                stats.forEachIndexed { index, stat ->
                    if (index > 0) withStyle(SpanStyle(color = p.muted)) { append(" · ") }
                    val ink = when (stat.tone) {
                        WorkHubTone.NORMAL -> p.muted
                        WorkHubTone.OVERDUE -> p.dangerText
                        WorkHubTone.SOON -> p.systemOrange
                    }
                    withStyle(SpanStyle(color = ink)) { append(stat.label) }
                }
            }, fontSize = 12.sp, lineHeight = 17.sp)
        }
        if (onClick != null) {
            NavGlassGlyph("chevron-right", p.muted, Modifier.size(17.dp))
        }
    }
}
