package com.navtasks.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate

/**
 * AMOS and maintenance are shown beside tasks without creating duplicate TaskRuns.
 * The normal task progress, missed queue and task editing remain untouched.
 */
@Composable
internal fun WorkAgendaTodaySection(
    day: LocalDate,
    orders: List<WorkOrderEntry>,
    services: List<ServiceEntry>,
    p: NavPalette,
    lang: UiLanguage,
    onAmos: (String) -> Unit,
    onServices: (String) -> Unit,
) {
    val ordersVisible = orders.filter { amosVisibleOnDay(it, day) }
        .sortedBy { it.dueDate }
    val servicesVisible = services.filter { serviceReminderVisibleOnDay(it, day) }
        .sortedBy { it.nextDate }
    if (ordersVisible.isEmpty() && servicesVisible.isEmpty()) return

    Text(localized("Работы", "Work", lang), color = p.text, fontSize = 15.sp,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))

    ordersVisible.forEach { order ->
        key("amos-" + order.id) {
            WorkAgendaTodayCard(
                title = order.title,
                category = "AMOS",
                nextServiceDate = null,
                p = p, lang = lang,
                onNavigate = { onAmos(order.id) },
            )
        }
        Spacer(Modifier.height(8.dp))
    }
    servicesVisible.forEach { service ->
        key("service-" + service.id) {
            WorkAgendaTodayCard(
                title = service.equipment,
                category = localized("Сервис", "Maintenance", lang),
                nextServiceDate = service.nextDate,
                p = p, lang = lang,
                onNavigate = { onServices(service.id) },
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}

/**
 * Read-only reminder: only the explicit navigation button is interactive.
 * Work completion, requests and editing live in the appropriate Works module.
 */
@Composable
private fun WorkAgendaTodayCard(
    title: String,
    category: String,
    nextServiceDate: LocalDate?,
    p: NavPalette,
    lang: UiLanguage,
    onNavigate: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().navGlass(p, radius = 17.dp, role = GlassRole.TASK)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(
            Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(category, color = p.blue, fontSize = 11.sp)
            Text(title, color = p.text, fontSize = 14.sp,
                maxLines = 2, overflow = TextOverflow.Ellipsis)
            nextServiceDate?.let { due ->
                Text(localized("Следующий сервис: ", "Next service: ", lang) +
                    navDate(due, lang, true),
                    color = p.muted, fontSize = 11.sp)
            }
        }
        Box(
            Modifier.navGlass(p, radius = 12.dp, role = GlassRole.CONTROL)
                .glassClickable(onClick = onNavigate)
                .heightIn(min = 44.dp)
                .padding(horizontal = 12.dp, vertical = 11.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(localized("Перейти", "Go to", lang),
                color = p.blue, fontSize = 12.sp)
        }
    }
}

@Composable
internal fun WorkAgendaCalendarRows(
    day: LocalDate,
    orders: List<WorkOrderEntry>,
    services: List<ServiceEntry>,
    p: NavPalette,
    lang: UiLanguage,
) {
    val plannedOrders = orders.filter { amosPlannedOnDay(it, day) }
    val plannedServices = services.filter { servicePlannedOnDay(it, day) }
    if (plannedOrders.isEmpty() && plannedServices.isEmpty()) return
    Text(localized("Работы", "Work", lang), color = p.blue, fontSize = 14.sp,
        modifier = Modifier.padding(top = 8.dp, bottom = 3.dp))
    plannedOrders.forEach { entry ->
        WorkAgendaCalendarRow(
            "AMOS", entry.title, day, p, lang,
            if (entry.recurring) localized("Повторение", "Recurring", lang) else null)
        Spacer(Modifier.height(7.dp))
    }
    plannedServices.forEach { service ->
        WorkAgendaCalendarRow(
            localized("Сервис", "Maintenance", lang), service.equipment,
            day, p, lang,
            service.requestNumber.takeIf { it.isNotBlank() }?.let {
                localized("Заявка: ", "Request: ", lang) + it
            },
        )
        Spacer(Modifier.height(7.dp))
    }
}

@Composable
private fun WorkAgendaCalendarRow(
    category: String, title: String, date: LocalDate,
    p: NavPalette, lang: UiLanguage, secondary: String?,
) {
    Column(
        Modifier.fillMaxWidth().navGlass(p, radius = 16.dp, role = GlassRole.TASK)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Text(category, color = p.blue, fontSize = 11.sp)
        Text(title, color = p.text, fontSize = 14.sp)
        Text(navDate(date, lang, true), color = p.muted, fontSize = 12.sp)
        secondary?.let { Text(it, color = p.muted, fontSize = 11.sp) }
    }
}

/** Service requisition number is separate from the equipment service record. */
@Composable
internal fun ServiceRequestDialog(
    service: ServiceEntry,
    p: NavPalette,
    lang: UiLanguage,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    var number by remember(service.id) { mutableStateOf(service.requestNumber) }
    NavGlassAlertDialog(
        p = p,
        onDismissRequest = onDismiss,
        title = { Text(localized("Заявка AMOS", "AMOS request", lang)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(service.equipment, color = p.muted, fontSize = 13.sp)
                Text(localized("Номер созданной заявки", "Created request number", lang),
                    color = p.muted, fontSize = 12.sp)
                NavGlassInput(
                    value = number,
                    onValueChange = { number = it.take(100) },
                    p = p,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = localized("Номер из AMOS", "Number from AMOS", lang),
                )
                if (service.requestNumber.isNotBlank()) {
                    Text(localized("Очисти поле, чтобы убрать номер заявки.",
                        "Clear the field to remove the request number.", lang),
                        color = p.muted, fontSize = 11.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(number.trim()) }) {
                Text(localized("Сохранить", "Save", lang))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(localized("Отмена", "Cancel", lang))
            }
        },
    )
}
