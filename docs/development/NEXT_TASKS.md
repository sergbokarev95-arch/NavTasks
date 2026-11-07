# NavTasks — Next Tasks

## Current stage: analysis / awaiting design approval

1. Notification icon: replace checkmark-centric small notification glyph with a recognizable monochrome NavTasks brand mark. Current drawable is a rounded square with a large checkmark; all inspected notification builders reference R.drawable.navtasks_notification. Preserve Android small-icon tint behavior. **Not implemented.**
2. Widgets: improve information hierarchy, fit, useful daily summary, overdue/missed visibility, click targets and resizing across compact/wide layouts. Current RemoteViews shows 2–4 pending tasks based on height, progress fraction, overflow text and optional missed count in footer. **Not implemented.**
3. Missed tasks: propose a restrained red count/pill only when missedCount > 0; verify legibility and no overflow in narrow layouts. **Not implemented.**
4. AMOS and Services Today: regression-test that only due-date items appear in Today; early alerts and Soon remain independent. 0.4.46 source change claims fix; **device verification pending**.
5. After approval: small scoped code change, tests, signed CI build, device checks, GitHub APK/ZIP publication; update all docs together with code changes.

## Constraints

No rewrite, no data/profile schema migration unless explicitly approved, preserve recurrence, reminders, task history, settings, profile import/export and existing interactions. User must approve proposed notification/widget design before code edits.
