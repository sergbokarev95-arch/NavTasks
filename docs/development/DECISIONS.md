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
