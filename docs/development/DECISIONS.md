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
