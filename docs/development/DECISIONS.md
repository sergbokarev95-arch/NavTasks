# NavTasks — Durable Decisions

- Preserve the existing Android Kotlin/Jetpack Compose application. Make minimal, reviewable changes; do not recreate the project.
- User data, profiles, schedules, recurring task semantics, task completion/history, settings and exchange formats must remain compatible.
- Use Apple Human Interface principles as design guidance: hierarchy, readable typography, consistent spacing, subtle surfaces, day/night contrast and restrained Liquid Glass. Android platform limitations still apply.
- Widgets use Android RemoteViews, not arbitrary Compose/Haze backdrop blur; do not claim native Liquid Glass for home-screen widgets.
- A widget task title opens the task; it must not silently complete it. A simple task's separate control can toggle completion; checklist tasks open in-app.
- AMOS/Services cards in Today appear on the actual scheduled due date only. Early reminders and Soon/Overdue screens are separate; never surface a future AMOS due card early because an early notification is configured.
- AMOS Today card stays compact, links directly to the work order; editing/completion happens in Works. Services with an open work-order reference must not be duplicated in Today (prior user decision; verify implementation when touching this logic).
- Notification small icon must be a high-contrast monochrome glyph recognizable as NavTasks, not a launcher glass image.
- For closing a work item on a different date, preserve the distinction between actual completion date and whether future occurrences retain or shift their schedule.
- No unverified APK may be described as device-tested. Build, unit tests and on-device checks are distinct milestones.
- Every completed stage must update docs/development files in GitHub alongside the implementation. Record exact commit IDs and CI links when available.

- 2026-11-07 approval: the notification should show the actual installed NavTasks color launcher icon (Android LargeIcon); small status-bar glyph is a monochrome translation of the existing layered-card brand, not a generic checkmark. Android OEM may suppress LargeIcon in some contexts.
- 2026-11-07 approval: Missed is neutral when zero, restrained red glass with red count in Today when positive; widgets show a red pill only with missed tasks and open the missed backlog on tap.
- 2026-11-07 approval: widget has localized date and progress; show pending task titles and adapt layout for width and height rather than stretching rows. Preserve task-click behavior.

- Widgets are Android RemoteViews. Use only its supported layout/view classes; do not put android.widget.Space or arbitrary Compose/custom views inside widget XML. Use layout padding or margins for gaps. Include a CI guard for known forbidden classes.

- 2026-11-07: version 0.4.48 is the first deliverable of this notification/widget update; v0.4.47 was automatically published then superseded due to unsupported widget XML. Do not recommend v0.4.47.

- 2026-11-07 user-approved clarification: Today Missed is completely absent (including its reserved horizontal gap) when backlog count is zero; the dates occupy that space. When count becomes positive, show restrained red glass with animated horizontal appearance; do not change data, overdue semantics, or the widget's existing zero-count behavior.

- 0.4.49 shipped on 2026-11-07: the Today missed chip has no zero state (hidden entirely), while the home widget missed badge retains its already-implemented zero-hide behavior. The date strip reclaims the width with an AnimatedVisibility horizontal transition.

- 2026-11-07 (0.4.50): equipment maintenance has two distinct record modes: planned (schedule, optional recurrence, reminders) and unplanned (AMOS requisition, no scheduled next due). This is different from a planned one-off with period=0. Existing JSON with no unplanned flag means Planned.
- Unplanned requests must not contribute to date-based Today/Calendar/Soon/Overdue or scheduled service notifications. Actual completion appears in history; request number is archived, and no next cycle is generated.
- Reuse current local record/profile storage. For an unplanned record nextDate is nullable; the existing lastDate is interpreted as the request creation date in that mode. Do not fabricate a next due date.
- New unplanned records require a manually entered AMOS requisition number; NavTasks does not create the request in external AMOS. The selective-template exchange removes original request numbers, leaving an unplanned draft that must have a new number before completion.
- Prevent changing service type when service completion history exists to avoid reinterpretation of archived records.

- v0.4.50 was signed and released 2026-11-07, source 0a3467be; previous release 0.4.49 remains available. GitHub CI is not physical-device acceptance.

- 2026-11-07 (0.4.51): “Дата заявки AMOS” for an unplanned service is manually selectable, defaulting to the current day for new entries. It is historical/reference metadata, not a due/completion date; must not create reminders, recurrence, scheduled Today/Calendar entries, Soon/Overdue status, or affect actual completion date. Reuse the existing Unplanned lastDate field; old profiles must remain compatible.

- 0.4.51 signed release 2026-11-07, source 651948cb0c280cd63a418d0054515f4d83102ea9: unplanned AMOS request date remains informational; it is not a deadline or the service-completion date. No change to profile JSON schema.

- 2026-11-07 (0.4.52 design): for standalone Services and AMOS pages, the scroll container must be edge-to-edge like Today. No navigationBarsPadding() on the full scrolling Column; the status-bar inset stays. Keep safe navigationBarsPadding() on the bottom-left floating Add control and 106dp internal LazyColumn trailing content padding for accessible card buttons. Do not introduce opaque reserved footer/bottomBar.

- 0.4.52 (2026-11-07): signed release ships edge-to-edge standalone Services and AMOS scroll containers. Maintain the decision not to re-add root navigationBarsPadding() when editing these pages; keep safe inset on floating Add buttons. Source d38aa26ddeb8ac3fe5f568dddb1c551463fc5a4d; physical HONOR verification pending.
