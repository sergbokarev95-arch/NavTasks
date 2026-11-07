# NavTasks — Next Tasks

## Current stage — 0.4.47 source ready for CI; no device validation yet

User approved the design on 2026-11-07.

- Notification: full-color original installed app launcher icon as LargeIcon and layered-card monochrome NavTasks glyph as Android SmallIcon. Android/HONOR controls which notification surfaces display LargeIcon; test on phone.
- Widgets: localized date, progress, compact pending-task list; wide two-column layout only at >=350dp, otherwise preserve readable single-column rows. Titles open tasks; simple toggles and checklist actions unchanged.
- Missed tasks: red Today count only for backlog > 0; neutral at zero. Widget red pill hidden at zero and linked to Today missed section.
- AMOS/Service: 0.4.46 due-date-only Today logic unchanged, WorkAgendaTest already covers Nov 7/13; confirm on HONOR.
- Next: run GitHub Actions unit tests/build, fix compile or regression failures, then publish stable-signed APK/ZIP from pinned tested commit. Do NOT claim the device UI is tested until checked on HONOR.
- After verification update CURRENT_STATE, NEXT_TASKS, DECISIONS, CHANGELOG_DEV and HANDOFF with actual commit, CI run, release/tag and outstanding device checks.

No data/profile schema migration, recurrence rewrite or unrelated screen changes are authorized.
