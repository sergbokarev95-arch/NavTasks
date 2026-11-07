# NavTasks — Next Tasks

## Current stage — 0.4.51 request-date picker published and CI-verified; HONOR acceptance pending

- User reported on 2026-11-07 that unplanned maintenance requisitions may have been created on a different date from entry into NavTasks. **Request date is informational only**, never a due date.
- Implementation scope: ServicesUi.kt (Unplanned editor picker + persist user-selected date), ServiceScheduleTest.kt (regression), app/build.gradle.kts (versionName 0.4.51, versionCode 73). Existing 0.4.50 model uses lastDate as request date for Unplanned; no data-format changes.
- Default new unplanned date = today; editing an existing unplanned record must show and preserve its saved date until user changes it.
- Planned service editor must remain exactly as before; no new reminders/overdue/calendar scheduling from request date.
- Complete: unit tests and signed APK/ZIP build passed. Main CI 42354150215 and publisher CI 42354348234 both SUCCESS, released from immutable 651948cb0c280cd63a418d0054515f4d83102ea9.
- On-device next: verify picker opens on AMOS request date, backdated date saves and reopens, card displays selected date, completion date remains separate, overdue/soon/calendar unaltered; profile backup recommended before installing.

## Prior release
- v0.4.50-glass-beta: Planned/Unplanned Services, immutable source 0a3467be369d889774b653d111917d7a6606a520; previous published baseline.
- Existing notification/widget/AMOS improvements and their acceptance checks remain tracked in Git history and the current-state changelog.

## On-device next step (0.4.51)
- Open a new Unplanned maintenance request, change “Дата заявки AMOS” to an earlier date, save and reopen; the selected date must persist and show on the service card.
- Edit an existing Unplanned request from 0.4.50 and verify it opens at its original saved date; change it without altering AMOS number, comment or completion date.
- Confirm request date has no effect on planned due date, recurrence, Soon/Overdue, Calendar or reminders. Planned maintenance editor unchanged.
- Release https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.51-glass-beta; APK SHA-256 ea58622fad66204e812dfdaea6ed7e8546ed98e672c6764481dd91f35ae8e4c3.
