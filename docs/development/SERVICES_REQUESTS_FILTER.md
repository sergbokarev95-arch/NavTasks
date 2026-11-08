# Services: current AMOS applications filter (user-approved semantics)

User request: keep `Работы` as all active planned/unplanned services. Add a third segmented button `Заявки` between `Работы` and `Закрытые`; this view shows ONLY services for which an AMOS requisition is already registered. Planned entries without requisition are hidden in Requests view but remain in Works; unplanned with requisition appear in both.

- Storage unchanged: `ServiceEntry.requestNumber` is authoritative and already trimmed by the editor/action; no new JSON field, migration or separate AMOS module.
- Exact pure predicate `currentServiceRequests`: `closedAt == null && completedAt == null && requestNumber.isNotBlank()`. Current planned and unplanned both included, historical numbers in `serviceHistory` excluded, completed/closed excluded.
- Service list filters remain the same for ALL/SOON/OVERDUE counters, and existing search works on whichever tab is selected. Top counters select Works; bottom three buttons select Works / Requests / Closed and reset the overview filter to ALL.
- Request counter is live, changes instantly when user enters or removes AMOS number or completes/closes/restores record. Empty request list shows `Текущих заявок AMOS пока нет.`.
- All visible service cards and handlers (Edit, Serviced, Delete, enter AMOS, history) are the SAME components. No business logic, date calculation, reminder, AMOS Work Orders, profile/backup changes.
- Base: approved glass + faster check 0.4.66 signed source `38919f6348e17237bf66a0102815ba3caf992f75`. Working source backup `backup/navtasks-0.4.66-before-service-requests`. Feature `feature/service-requests-filter-0.4.68-beta`.
- Same-ID signed in-place 0.4.68-beta versionCode93 (above optional recovery 0.4.67 code92), with small ZIP and same stable Beta signing key; never uninstall, export FULL profile first.
- Verified signed recovery should use exact 0.4.66 visual source with higher versionCode94 (prefer the accepted Liquid Glass UI over old stable Haze), not the old Haze. Physical HONOR acceptance remains required.
