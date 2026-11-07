# NavTasks — Next Tasks

## Current stage — 0.4.51 request-date picker; CI and signed release pending

- User reported on 2026-11-07 that unplanned maintenance requisitions may have been created on a different date from entry into NavTasks. **Request date is informational only**, never a due date.
- Implementation scope: ServicesUi.kt (Unplanned editor picker + persist user-selected date), ServiceScheduleTest.kt (regression), app/build.gradle.kts (versionName 0.4.51, versionCode 73). Existing 0.4.50 model uses lastDate as request date for Unplanned; no data-format changes.
- Default new unplanned date = today; editing an existing unplanned record must show and preserve its saved date until user changes it.
- Planned service editor must remain exactly as before; no new reminders/overdue/calendar scheduling from request date.
- Run all unit tests and CI APK build; pin signed 0.4.51 release to tested commit. Update all five docs with resulting evidence.
- On-device next: verify picker opens on AMOS request date, backdated date saves and reopens, card displays selected date, completion date remains separate, overdue/soon/calendar unaltered; profile backup recommended before installing.

## Prior release
- v0.4.50-glass-beta: Planned/Unplanned Services, immutable source 0a3467be369d889774b653d111917d7a6606a520; latest published pending 0.4.51 validation.
- Existing notification/widget/AMOS improvements and their acceptance checks remain tracked in Git history and the current-state changelog.
