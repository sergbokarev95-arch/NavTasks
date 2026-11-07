# NavTasks — Current State

Reviewed 2026-11-07 against GitHub main and successful signed 0.4.53 release; subsequent documentation-only commits may advance main HEAD.

- Repository: sergbokarev95-arch/NavTasks (private); default and current integration branch: main.
- Current source stage is 0.4.54 (versionCode 76), pending CI and signed release. Last published and CI-verified release remains 0.4.53 (versionCode 75). Debug variant adds -beta and .glass.
- Historical milestones: 0.4.46 due-only AMOS/Services; 0.4.48 notification/widget update; 0.4.50 planned/unplanned Services; 0.4.51 informational AMOS request date. Current 0.4.52 adjusts only edge-to-edge drawing.
- Last pre-documentation main commit: 211c0ac — publication workflow for 0.4.46.
- Current release workflow .github/workflows/publish-0.4.53-apk.yml pinned to 81cce3e29b74454f6329a124f9589570a9cb2722. Earlier signed release workflows are retained for provenance.
- GitHub Actions Build NavTasks Android APK run 42254823995 completed successfully on 2026-11-06. CI runs unit tests and assembles debug APK. Physical-device validation is not established by CI.
- Main modules: Today, Plan/calendar, Tasks and recurrence, profiles, import/export, handover, Services, AMOS work orders, Works agenda, notifications and two home widgets. Implementation is Kotlin/Jetpack Compose, with RemoteViews XML widgets.
- Notification implementation: app/src/main/java/com/navtasks/app/NavNotifications.kt; icon app/src/main/res/drawable/navtasks_notification.xml.
- Widget implementation: app/src/main/java/com/navtasks/app/NavWidgets.kt and app/src/main/res/layout/navtasks_widget_{compact,wide}.xml.
- The repository README.md and CI_README.md contain legacy 0.2-era statements; use Gradle, current workflow and this document for current state.
- On 2026-11-07 user approved original-color notification icon, adaptive widgets and restrained red Missed chips. 0.4.48 CI and release build passed; HONOR/Samsung on-device visual testing remains outstanding.

- 0.4.47 source touches notification icon/builder, widgets and Today presentation only; no profile or task data schema changes. Verify the GitHub main head and the next passing CI run before installing.

- 0.4.48 correction: wide widget no longer contains unsupported android.widget.Space; uses supported LinearLayout padding. The 0.4.47 release is marked Superseded/do not install; 0.4.48 is published and preferred.

## Verified 0.4.48 delivery
- Source commit: 2968dcce377f531b4bc3a57166cb8e0b62d1e1de.
- Publish-workflow commit: 0b77bce58613dbf828b17b8e6dc5ae1a081ecb34.
- Main CI: https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42332014351 — success (RemoteViews guard, unit tests, APK).
- Publisher CI: https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42332255873 — success (unit tests, stable-signed APK, ZIP, releases).
- Release: https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.48-glass-beta
- Asset SHA-256 (APK): 55f1b97fb7c31fefc990ef3904efb4bf617aaf06eecc0538275e914728b52c04.
- Device testing has NOT been completed.

## 0.4.49 Today Missed chip (released)
- User requested on 2026-11-07: the Today Missed tile must occupy **zero width at count=0** and appear as red only at count>0; weekday strip expands left automatically.
- Implementation: conditional AnimatedVisibility with 180 ms horizontal expansion/shrinkage; includes both 105 dp tile and 7 dp gap so there is no residual spacing. An empty backlog closes the Missed view automatically.
- Scope: NavTasksUi.kt, existing Today grouping unit-test file, version bump. No change to home widgets, notification logic, storage, profiles, AMOS, or schedules.
- CI and signed release completed successfully; HONOR device verification remains pending.

## Verified 0.4.49 delivery
- Source commit: 41ae92386e1459099a6c1c3c069a987c5de04203.
- Publisher-workflow commit: 5dedee07a6f14d95f4730e52ee5fef62da6f8096.
- Main CI (unit tests, safe RemoteViews and APK): https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42335111965 — SUCCESS.
- Signed release CI: https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42335349500 — SUCCESS, APK+ZIP published.
- Latest release: https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.49-glass-beta.
- Published APK SHA-256: 08123ef5d64a6b7d6f1febe3725a8fe7902b47ea0ff50fc85f4b90585b5103a5.
- Not yet visually verified on HONOR/Samsung.

## 0.4.50 — planned/unplanned maintenance (released, 2026-11-07)

- Base: main 0fa21503afcc32b9ae356388705dbf121d6d4efb (v0.4.49 remains the previous verified release).
- New source version: 0.4.50 / versionCode 72. **CI unit tests and signed release verified; on-device validation pending.**
- ServiceEntry now has nullable nextDate and optional unplanned flag; missing flag in older profile JSON defaults to planned. Scheduled records retain required due date/intervals. Unplanned records cannot have a due date, cycle, or advance reminder.
- Unplanned request date is retained in the existing lastDate field as an explicit request-date interpretation for that mode; no fake nextDate is created. Service history has an optional scheduledDate for archived unscheduled work.
- Services editor shows Planned/Unplanned, requires AMOS request number for new unplanned records, hides due/recurrence/reminder; completed unscheduled work archives the actual request number without starting a cycle.
- Date-based Today, Calendar, Soon, Overdue, Work Hub counters and service notifications explicitly exclude unplanned requests; completed unplanned work remains visible in completion history. Full profile JSON remains backward-compatible, and selective template export continues to scrub the vessel's actual request number.
- GitHub CI verified unit tests and signed APK. Physical HONOR/Samsung acceptance is outstanding.

### Verified 0.4.50 delivery
- Source: 0a3467be369d889774b653d111917d7a6606a520; publisher workflow: c12e0000d04aa427fa36cb600cdd29fb10216a74.
- Main CI https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42350648226 SUCCESS; signed publisher CI https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42350831779 SUCCESS.
- Release https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.50-glass-beta; APK SHA-256 63e814be42a5a9f3af44f3551bf5024ea16ed6c00aab3ccd127023cf00e6680d.
- Real HONOR/Samsung UI and data-flow testing still pending.

## 0.4.51 — editable informational AMOS request date (released, 2026-11-07)
- User feedback: an unplanned AMOS requisition can have been created on an earlier/later date than the date it was entered into NavTasks.
- ServiceEditorDialog now shows a selectable “Дата заявки AMOS” wheel date picker only for Unplanned. New drafts default to today's date; editing a saved unplanned service defaults to its existing request date.
- Chosen date remains stored as ServiceEntry.lastDate for unplanned records, as established in 0.4.50. No new profile JSON property, conversion or migration.
- No change to the planned service due date, last actual maintenance, recurrence, overdue/soon, calendar, notification scheduling, AMOS work orders, widgets or task logic.
- Unit regression covers request date before/after today and confirms no due/notification effects or false schedule.
- Immutable source 651948cb0c280cd63a418d0054515f4d83102ea9; publisher workflow 48c55cd46d37bb66df7d10fc57cab801e488f694.
- Main CI https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42354150215 SUCCESS; stable-signed publisher CI https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42354348234 SUCCESS.
- Release https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.51-glass-beta; APK SHA-256 ea58622fad66204e812dfdaea6ed7e8546ed98e672c6764481dd91f35ae8e4c3.
- HONOR device validation of UI and data flow remains pending.

## 0.4.52 — remove artificial bottom edge in Services and AMOS (released, 2026-11-07)
- User reported (HONOR screenshots) that service cards and AMOS cards have a visible bottom cutoff above the system gesture navigation area; Today draws content to the screen edge.
- Source audit verified the cause: both ServicesUi.kt and WorkOrdersUi.kt applied navigationBarsPadding() to their entire scrolling Column, unlike the root Today screen.
- Only change: drop navigationBarsPadding() from the two scrolling Column roots, retaining statusBarsPadding() and hazeSource(). Keep navigationBarsPadding() on both separate floating Add buttons so they remain outside Android's gesture area.
- Existing LazyColumn contentPadding bottom=106dp ensures last cards can scroll entirely clear of the bottom gesture area; no changes to card geometry, models, dates, notifications, AMOS logic, data, or profile format.
- Main CI https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42356909189: SUCCESS (unit tests, APK). Signed publisher https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42357102513: SUCCESS (repeated tests, signed APK, ZIP, release).
- Immutable source commit d38aa26ddeb8ac3fe5f568dddb1c551463fc5a4d; publisher workflow commit f448c0f5225c6415d3cf414b1edcf2dff2a3bd8f.
- Published https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.52-glass-beta; APK SHA-256 e05d0691f0d0320c288e6b466507adc160cad2c13fd83422656daf9726eea3fb.
- Physical HONOR day/night and gesture navigation verification remains outstanding.

## 0.4.53 — calm monthly calendar cells (released, 2026-11-07)
- User tested 0.4.52 on HONOR and reported highly saturated blue outlines around all dates in the monthly Plan calendar, uneven day-number positions when a dot is present, small numerals, and no distinction between weekends and weekdays.
- Planned targeted implementation in PlanAndTasksUi.kt, month zoom only: remove all blue calendar-cell borders; use subtle neutral/day-status fills and a restrained blue-tinted Today fill. Weekend header labels and in-month Saturday/Sunday numerals use muted coral (dark/light variants), with no change to semantic missed/completed dot colors.
- Calendar month day numerals increased 14sp → 16sp, now centered in a 54dp Box regardless of dot; marker anchored to bottom center with 6dp padding. Dot conditions unchanged. Outside-month dates remain dimmed.
- Added pure unit tests for weekend detection and status-dot inclusion. No changes to navigation, task scheduling, completion, AMOS, service data, profiles, widgets, or persisted calendar state.
- Immutable source commit: 81cce3e29b74454f6329a124f9589570a9cb2722; publisher workflow commit: e556ab3b4993d7a877e8885bf6d5045bae136505.
- Main CI https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42365740578 SUCCESS (unit tests and APK).
- Publisher CI https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42365940441 SUCCESS (repeated tests, stable-signed APK and ZIP).
- Release https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.53-glass-beta, APK SHA-256 c7dd50a87bd36e9efaf7ddfcdc4ef55c40dff7be645314e536f7b373e16d7b73. On-device HONOR layout checks still pending.

## 0.4.54 — weekend tint in Today week strip (source stage, 2026-11-07)
- After 0.4.53 calendar update, user pointed out Today seven-day strip still lacked weekend distinction. Correct: Today has its own private DayChip in NavTasksUi.kt and was not covered by the Plan month-grid change.
- Code changes: Today DayChip accepts a weekend flag from the same calendarIsWeekend(date) helper as Plan. Both screens use shared calendarWeekendInk(p), so dark/light muted-coral hues cannot drift. Weekend labels and dates are tinted whether or not the day is selected; selected glass/pill, scroll and data logic remain unchanged. Date number increased 15sp -> 16sp.
- Pure CalendarVisualRulesTest confirms shared palette weekend colors and weekend detection. No schedule, history, task behavior, notifications, widgets, profile schemas, or AMOS changes.
- Next: CI tests/build and signed GitHub APK/ZIP from immutable source, then HONOR visual verification in both themes. 0.4.53 remains published while 0.4.54 is unverified.
