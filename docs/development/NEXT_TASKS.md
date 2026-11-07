# NavTasks — Next Tasks

## Current stage — 0.4.50 published and CI-verified; HONOR acceptance pending

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

## 0.4.49 completion evidence
- Source commit 41ae92386e1459099a6c1c3c069a987c5de04203; CI 42335111965 SUCCESS.
- Release workflow commit 5dedee07a6f14d95f4730e52ee5fef62da6f8096; CI 42335349500 SUCCESS; APK/ZIP in v0.4.49-glass-beta.
- Next: verify on HONOR that empty Missed chip leaves **no gap**, 0→1 slides in red, 1→0 hides automatically, dates scroll normally in both themes, and no unexpected changes to other task flows. Do not claim device validation before the user tests it.

## New field feedback: planned vs unplanned equipment maintenance (2026-11-07)

User is testing 0.4.49 and reports a concrete issue in Services > New service: the form always requires a next maintenance date, but unplanned maintenance should be entered directly with an AMOS requisition/request number.

### Source audit (no application code changed yet)

- `app/src/main/java/com/navtasks/app/ServicesUi.kt` > `ServiceEditorDialog`: only the next-date/interval form is available. The existing request number is entered later in a separate `ServiceRequestDialog` after saving.
- `ServiceStore.kt`: `ServiceEntry` always has `nextDate`, and the constructor requires it to follow `lastDate`; `requestNumber` already exists, is saved in JSON and archived in `serviceHistory` on completion.
- `WorkAgenda.kt`, `WorksHubUi.kt`, `NavNotifications.kt`: service dates participate in Today/Calendar, Soon/Overdue, and notification scheduling. Merely hiding nextDate will create bogus deadlines.
- `ServiceScheduleTest.kt`: regression tests cover intervals, original request numbers, one-off service, and history. Existing services must remain compatible and retain planned recurrence.

### Agreed implementation — source written, verification pending (0.4.50)

1. Add an explicit Planned/Unplanned selector to the existing service editor; older records default to Planned when decoding missing new type field.
2. **Planned**: preserve all current fields and calculations (Next date, interval presets/custom, advance warning, AMOS request number added later as today).
3. **Unplanned**: show Equipment, AMOS request number, Comment (and an explicit request creation date if needed). Do **not** require a next-service date or recurrence; show an open request in Services until marked completed. Do not put it in scheduled Today/Calendar or Soon/Overdue based on a fabricated due date.
4. Preserve history, request number, completion date, deletion/restoration, and profile import/export. Keep the internal JSON backwards compatible; test conversions and unexpected switching between types with existing history.
5. No direct AMOS API/requisition submission integration exists in the repo: the number is entered/recorded in NavTasks, it does not send a new request to the external AMOS system.
6. Implemented and released in 0.4.50. Unit tests and signed publication passed, device acceptance still needed.

## 0.4.50 build acceptance

- User authorized implementation with «Собирай» on 2026-11-07.
- Source work: new Planned/Unplanned editor toggle; mandatory AMOS number on new unplanned; no fake due date; unplanned status/request date; completion archives requisition and does not create recurrence; historical planned records default to planned.
- Completed: Kotlin compilation, existing tests plus unplanned due/recurrence/history/selective-template regression tests passed in CI.
- Completed: stable-signed v0.4.50-glass-beta APK/ZIP published from immutable source 0a3467be; CI 42350648226 and 42350831779 SUCCESS.
- Device acceptance: editing planned old service preserves due/interval, new unplanned with AMOS number saves/edits, cannot save new unplanned blank number, no inaccurate calendar/soon/overdue or notifications, completion history and Today Completed correct, exchange unaffected.

### 0.4.50 release proof and next tests
- Release https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.50-glass-beta; versionCode 72, APK SHA-256 63e814be42a5a9f3af44f3551bf5024ea16ed6c00aab3ccd127023cf00e6680d.
- Verify on HONOR: older planned services still show due/period and alert; new unplanned requires AMOS request and has no next date; does not appear as overdue/soon/calendar/Today due; completion archives request without recurrence; search, full profile and selective template exchange remain intact. Capture screenshots and bugs before further changes.
