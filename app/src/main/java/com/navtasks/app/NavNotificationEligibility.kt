package com.navtasks.app

/**
 * A closed or completed record cannot send a due reminder, even if its old
 * PendingIntent survives in AlarmManager after the user archives the record.
 * This rule is shared by scheduling, grouping and the receiver.
 */
internal fun activeServiceReminderRecord(service: ServiceEntry): Boolean =
    !service.unplanned && service.closedAt == null && service.completedAt == null

internal fun activeAmosReminderRecord(order: WorkOrderEntry): Boolean =
    order.closedAt == null && !order.completed
