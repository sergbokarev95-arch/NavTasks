# NavTasks — Next Tasks

## Current stage — 0.4.49 Today chip change; source validation pending

User approved the design on 2026-11-07.

- Notification: full-color original installed app launcher icon as LargeIcon and layered-card monochrome NavTasks glyph as Android SmallIcon. Android/HONOR controls which notification surfaces display LargeIcon; test on phone.
- Widgets: localized date, progress, compact pending-task list; wide two-column layout only at >=350dp, otherwise preserve readable single-column rows. Titles open tasks; simple toggles and checklist actions unchanged.
- Missed tasks: red Today count only for backlog > 0; neutral at zero. Widget red pill hidden at zero and linked to Today missed section.
- AMOS/Service: 0.4.46 due-date-only Today logic unchanged, WorkAgendaTest already covers Nov 7/13; confirm on HONOR.
- Next: run GitHub Actions unit tests/build, fix compile or regression failures, then publish stable-signed APK/ZIP from pinned tested commit. Do NOT claim the device UI is tested until checked on HONOR.
- After HONOR testing and each later completed stage update all five documents with actual commits, test evidence and findings.

No data/profile schema migration, recurrence rewrite or unrelated screen changes are authorized.

- Pre-release regression fix: wide XML contained unsupported Space; removed in 0.4.48. An explicit CI RemoteViews guard was added. Supersede any 0.4.47 artifact; only distribute 0.4.48 once its own CI passes.

## Device acceptance checklist (not yet performed)

- Check notification drawer and lockscreen: full app identity on HONOR; status icon is monochrome, not a lone generic checkbox. OEM may place/suppress large icon.
- Today missed chip: zero is neutral; count > 0 has restrained red treatment in both light/dark themes.
- Widgets: compact and wide responsive size, accurate date/progress/remaining tasks; red missed badge appears only at positive count and opens Missed; simple completion works, checklist tap opens app.
- AMOS work scheduled for 13 Nov is not shown as a Today card before 13 Nov, while early notification still works.

## 0.4.49 approved change (2026-11-07)
- On Today, hide the entire Missed chip and its 7dp gap when backlog=0, so the date strip fills the available horizontal space.
- At >0, animate a subtle red Missed chip + red count back into the row; tapping opens Missed backlog as before.
- If the user finishes the last missed task while viewing the Missed backlog, return to the ordinary selected-day view automatically.
- Add unit regression checks for 0, negative (defensive) and positive counts; run all CI tests, build signed APK, and verify on HONOR.
- The home widgets already hide their missed pill at 0; do not modify them for this task.
