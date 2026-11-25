package com.navtasks.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
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

/** Archived AMOS and service cycles appear inside Today's Completed stack.
 * These are read-only snapshots; tapping Go to opens the owning Work module.
 */
@Composable
internal fun WorkAgendaCompletedTodaySection(
    completedAmos: List<WorkOrderHistoryRecord>,
    completedServices: List<ServiceAgendaCompletion>,
    p: NavPalette,
    lang: UiLanguage,
    onAmosHistory: (String) -> Unit,
    onServices: (String) -> Unit,
) {
    if (completedAmos.isEmpty() && completedServices.isEmpty()) return
    Text(
        localized("Работы", "Work", lang), color = p.muted, fontSize = 13.sp,
        modifier = Modifier.padding(start = 4.dp, top = 6.dp, bottom = 2.dp),
    )
    completedAmos.forEach { record ->
        key("completed-amos-" + record.order.id + "-" + record.completion.id) {
            WorkAgendaTodayCard(
                title = record.order.title,
                category = "AMOS",
                nextServiceDate = null,
                completedAt = record.completion.completedAt,
                p = p, lang = lang,
                onNavigate = { onAmosHistory(record.order.id) },
            )
        }
        Spacer(Modifier.height(6.dp))
    }
    completedServices.forEachIndexed { index, record ->
        key("completed-service-" + record.service.id + "-" + index) {
            WorkAgendaTodayCard(
                title = record.completion.equipment.ifBlank { record.service.equipment },
                category = localized("Сервис", "Maintenance", lang),
                nextServiceDate = record.service.nextDate.takeIf {
                    record.service.completedAt == null
                },
                completedAt = record.completion.completedAt,
                p = p, lang = lang,
                onNavigate = { onServices(record.service.id) },
            )
        }
        Spacer(Modifier.height(6.dp))
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
    completedAt: LocalDate? = null,
    p: NavPalette,
    lang: UiLanguage,
    onNavigate: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(17.dp))
            .background(NavTodayVisualTokens.cardSurface(p.dark), RoundedCornerShape(17.dp))
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(
            Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(category, color = p.text, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(title, color = p.text, fontSize = 14.sp,
                maxLines = 2, overflow = TextOverflow.Ellipsis)
            completedAt?.let { done ->
                Text(
                    localized("Выполнено: ", "Completed: ", lang) + navDate(done, lang, true),
                    color = p.successText, fontSize = 11.sp,
                )
            }
            nextServiceDate?.let { due ->
                Text(localized("Следующий сервис: ", "Next service: ", lang) +
                    navDate(due, lang, true),
                    color = p.muted, fontSize = 11.sp)
            }
        }
        Box(
            Modifier.then(
                if (p.dark) Modifier.clip(RoundedCornerShape(12.dp))
                    .background(NavTodayVisualTokens.workNavigateNightSurface(), RoundedCornerShape(12.dp))
                    .border(1.dp, NavTodayVisualTokens.workNavigateNightRim(), RoundedCornerShape(12.dp))
                else Modifier.navGlass(p, radius = 12.dp, role = GlassRole.CONTROL)
            ).glassClickable(onClick = onNavigate)
                .heightIn(min = 44.dp)
                .padding(horizontal = 12.dp, vertical = 11.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(localized("Перейти", "Go to", lang),
                color = NavTodayVisualTokens.accentInk(p.dark), fontSize = 12.sp)
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
    Text(localized("Работы", "Work", lang), color = p.text, fontSize = 14.sp,
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
        Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
            .background(NavTodayVisualTokens.cardSurface(p.dark))
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Text(category, color = p.text, fontSize = 11.sp)
        Text(title, color = p.text, fontSize = 14.sp)
        Text(navDate(date, lang, true), color = p.muted, fontSize = 12.sp)
        secondary?.let { Text(it, color = p.muted, fontSize = 11.sp) }
    }
}

/** The current requisition is independent of the maintenance schedule. */
@Composable
internal fun ServiceRequestNumberField(
    number: String, p: NavPalette, lang: UiLanguage, onChange: (String) -> Unit,
    neutralStyle: Boolean = true,
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(NavEventLayout.innerGap)) {
        NavGlassInput(value = number, onValueChange = { onChange(it.take(100)) },
            p = p, modifier = Modifier.weight(1f),
            placeholder = localized("Номер из AMOS", "Number from AMOS", lang),
            keyboardOptions = NavIdentifierKeyboard, neutralStyle = neutralStyle)
        if (number.isNotEmpty()) {
            NavEventHeaderAction("x", localized("Очистить номер заявки", "Clear request number", lang),
                p, true) { onChange("") }
        }
    }
}

@Composable
internal fun ServiceRequestDialog(
    service: ServiceEntry, p: NavPalette, lang: UiLanguage,
    onDismiss: () -> Unit, neutralStyle: Boolean = true,
    onSave: (String, LocalDate?) -> Unit,
) {
    var number by remember(service.id) { mutableStateOf(service.requestNumber) }
    var date by remember(service.id) { mutableStateOf(service.effectiveRequestDate) }
    var pickingDate by remember(service.id) { mutableStateOf(false) }
    val dirty = number.trim() != service.requestNumber.trim() || date != service.effectiveRequestDate
    NavGlassAlertDialog(
        p = p, neutralSurface = neutralStyle, neutralActions = neutralStyle,
        onDismissRequest = onDismiss,
        title = { Text(localized("Заявка AMOS", "AMOS request", lang)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(service.equipment, color = p.muted, fontSize = 13.sp)
                Text(localized("Номер созданной заявки", "Created request number", lang),
                    color = p.muted, fontSize = 12.sp)
                ServiceRequestNumberField(number, p, lang, onChange = { value ->
                    if (value.isBlank()) date = null
                    else if (number.isBlank() && date == null) date = LocalDate.now()
                    number = value
                }, neutralStyle = neutralStyle)
                if (number.isNotBlank()) {
                    NavEventValueRow(localized("Дата заявки AMOS", "AMOS request date", lang),
                        date?.let { navDate(it, lang, true) }
                            ?: localized("Указать", "Set date", lang), p, chevron = true,
                        valueAccent = date != null) { pickingDate = true }
                    Text(localized("Дата заявки не влияет на сроки обслуживания.",
                        "Request date does not affect maintenance due dates.", lang),
                        color = p.muted, fontSize = 11.sp)
                }
            }
        },
        confirmButton = {
            TextButton(enabled = dirty, onClick = { onSave(number.trim(), date) }) {
                Text(localized("Сохранить", "Save", lang),
                    color = if (dirty) NavTodayVisualTokens.accentInk(p.dark) else p.muted)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(localized("Отмена", "Cancel", lang), color = p.text)
            }
        },
    )
    if (pickingDate) NavDateWheelDialog(initial = date ?: LocalDate.now(), p = p, lang = lang,
        onDismiss = { pickingDate = false },
        onSelected = { date = it; pickingDate = false })
}
