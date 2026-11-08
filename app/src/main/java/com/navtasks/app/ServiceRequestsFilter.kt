package com.navtasks.app

/** Service list views select a subset; they never modify the service records. */
internal enum class ServiceListView { WORKS, REQUESTS, CLOSED }

/**
 * AMOS requests that are still current, irrespective of planned / unplanned.
 * The prior-cycle numbers in serviceHistory are intentionally NOT included.
 * Closed records and completed one-offs are omitted, even if legacy data
 * accidentally retains a request number.
 */
internal fun currentServiceRequests(entries: List<ServiceEntry>): List<ServiceEntry> =
    entries.filter {
        it.closedAt == null &&
            it.completedAt == null &&
            it.requestNumber.isNotBlank()
    }
