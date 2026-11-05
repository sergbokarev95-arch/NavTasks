package com.navtasks.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
 * The entry point for ship work. Maintenance continues to use the existing
 * ServiceStore and ServicesScreen unchanged. AMOS and other work are clearly
 * marked as upcoming; do not create pretend records or mutate task schedules.
 */
@Composable
fun WorksHubScreen(
    services: ServiceStore,
    p: NavPalette,
    lang: UiLanguage,
    onBack: () -> Unit,
    onServices: () -> Unit,
) {
    val today = LocalDate.now()
    val overdue = services.entries.count { it.nextDate.isBefore(today) }
    val approaching = services.entries.count {
        !it.nextDate.isBefore(today) &&
            (it.nextDate <= today.plusDays(30) ||
                (it.remindMonths > 0 && !today.isBefore(serviceWarningStart(it))))
    }

    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NavGlassCircleButton(p, "back", onBack)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("NAVTASKS", color = p.blue, fontSize = 12.sp)
                Text(localized("Работы", "Work", lang), color = p.text, fontSize = 23.sp)
            }
        }
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, top = 6.dp, end = 16.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                NavPanel(p) {
                    Text(localized("Судовые работы", "Vessel work", lang),
                        color = p.text, fontSize = 17.sp)
                    Spacer(Modifier.height(5.dp))
                    Text(
                        localized(
                            "Единый раздел для работ AMOS, обслуживания оборудования и прочих задач.",
                            "One place for AMOS work orders, equipment maintenance and other work.",
                            lang,
                        ),
                        color = p.muted, fontSize = 12.sp,
                    )
                }
            }
            item {
                WorkHubTile(
                    p = p,
                    title = "AMOS · Work Orders",
                    description = localized(
                        "Судовые задания из AMOS. Ручной журнал добавим следующим этапом.",
                        "AMOS work orders. A manual work log is the next step.",
                        lang,
                    ),
                    secondary = localized("В разработке", "Coming next", lang),
                    icon = "tasks",
                    onClick = null,
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
                        "${services.entries.size} записей · ${overdue} просрочено · ${approaching} скоро",
                        "${services.entries.size} records · ${overdue} overdue · ${approaching} soon",
                        lang,
                    ),
                    icon = "settings",
                    onClick = onServices,
                )
            }
            item {
                WorkHubTile(
                    p = p,
                    title = localized("Прочие работы", "Other work", lang),
                    description = localized(
                        "Разовые работы вне AMOS и графика обслуживания.",
                        "One-off jobs outside AMOS and equipment servicing.",
                        lang,
                    ),
                    secondary = localized("В разработке", "Coming next", lang),
                    icon = "plus",
                    onClick = null,
                )
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
