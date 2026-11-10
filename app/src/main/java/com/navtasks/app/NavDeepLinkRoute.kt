package com.navtasks.app

/**
 * Immutable tap payload, shared by cold-start and onNewIntent entry points.
 * The serial distinguishes repeated taps on an identical PendingIntent.
 */
data class NavOpenRequest(
    val serial: Long,
    val profileId: String?,
    val runKey: String?,
    val workOrderId: String?,
    val openMissed: Boolean,
)

internal enum class NavOpenDestination { TODAY, TASK, AMOS, MISSED }

/** Reject stale/deleted profile IDs rather than accidentally opening another ship's record. */
internal fun resolveNavProfileTarget(
    activeId: String,
    availableIds: Collection<String>,
    requestedId: String?,
): String? = when {
    requestedId == null -> activeId.takeIf { it in availableIds } // Pre-profile legacy intents.
    requestedId in availableIds -> requestedId
    else -> null
}

/** One destination per tap; never execute two pending intents against different modules. */
internal fun navOpenDestination(request: NavOpenRequest): NavOpenDestination = when {
    !request.workOrderId.isNullOrBlank() -> NavOpenDestination.AMOS
    !request.runKey.isNullOrBlank() -> NavOpenDestination.TASK
    request.openMissed -> NavOpenDestination.MISSED
    else -> NavOpenDestination.TODAY
}
