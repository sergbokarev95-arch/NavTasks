# NavTasks — Current State

Reviewed 2026-11-07 against GitHub main and successful signed 0.4.56 release; subsequent documentation-only commits may advance main HEAD.

- Repository: sergbokarev95-arch/NavTasks (private); default and current integration branch: main.
- Current signed and published release is v0.4.56 (versionCode 78), following successful CI and release workflow. v0.4.55 (versionCode 77) remains the previous released baseline. Debug variant adds -beta and .glass.
- Historical milestones: 0.4.46 due-only AMOS/Services; 0.4.48 notification/widget update; 0.4.50 planned/unplanned Services; 0.4.51 informational AMOS request date. Current 0.4.52 adjusts only edge-to-edge drawing.
- Last pre-documentation main commit: 211c0ac — publication workflow for 0.4.46.
- Current release workflow .github/workflows/publish-0.4.54-apk.yml pinned to 7e04c01a67b5979ae2b02dbf9ad1be6ccf21d16f. Earlier signed release workflows are retained for provenance.
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

## 0.4.54 — weekend tint in Today week strip (released, 2026-11-07)
- After 0.4.53 calendar update, user pointed out Today seven-day strip still lacked weekend distinction. Correct: Today has its own private DayChip in NavTasksUi.kt and was not covered by the Plan month-grid change.
- Code changes: Today DayChip accepts a weekend flag from the same calendarIsWeekend(date) helper as Plan. Both screens use shared calendarWeekendInk(p), so dark/light muted-coral hues cannot drift. Weekend labels and dates are tinted whether or not the day is selected; selected glass/pill, scroll and data logic remain unchanged. Date number increased 15sp -> 16sp.
- Pure CalendarVisualRulesTest confirms shared palette weekend colors and weekend detection. No schedule, history, task behavior, notifications, widgets, profile schemas, or AMOS changes.
- Source 7e04c01a67b5979ae2b02dbf9ad1be6ccf21d16f, publisher workflow 8ff1fc48a05c00a5635e3da8ca9ce330467f8cb0. Main CI https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42366929632 SUCCESS and signed publisher CI https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42367156700 SUCCESS.
- Published https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.54-glass-beta; APK SHA-256 f3b2ab0a8c74836469875ba8e0c25dd711ea3cf58f67ccee4c1f5a2325cb63ac. Physical HONOR verification is still outstanding.

## 0.4.55 — align floating Add buttons on right (released, 2026-11-07)
- User requested “плюсики должны быть с правой части экрана”. Source audit found Add at BottomStart in Tasks (NavTasksUi.kt), Services (ServicesUi.kt) and AMOS (WorkOrdersUi.kt), while the Today calendar action was already BottomEnd.
- Exact UI-only change: use BottomEnd and end=19dp instead of BottomStart/start=19dp in these three overlays. Preserve navigationBarsPadding(), existing bottom offsets (Tasks 83dp above dock; Services/AMOS 18dp), glass component, click handlers, scrolling, screen layout and all business logic.
- Build versionName 0.4.55, versionCode 77; no data schema or migrations.
- Immutable source 401564269c574271b1e3c22c356989fe2d94eb58; publisher workflow commit aa4ed6b8f4140e7f78364819a8a8f1998e0ab279.
- Main CI https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42369659340 SUCCESS (unit tests, APK). Signed publisher CI https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42369873080 SUCCESS (repeated tests, signed APK/ZIP, publication).
- Release https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.55-glass-beta; APK SHA-256 156b24d04dcbb496e8480ce463423e430651d40fe412e42b2f9202ace07229be. Actual HONOR visual/tap acceptance remains outstanding.

## 0.4.56 Handover Add placement (2026-11-07, released)
- User provided HONOR screenshot showing the Handover (+) still at bottom left, overlooked in 0.4.55. Source audit found a separate HandoverUi.kt floating Add wrapper, not covered by the previous three fixes.
- Minimal fix: the one Handover Add wrapper is now BottomEnd / end=19dp instead of BottomStart / start=19dp. Preserve the same NavGlassAddButton, openSectionEditor(null) handler, navigationBarsPadding(), and bottom offset: 83dp when embedded in main dock, 18dp when standalone.
- Build version 0.4.56 / versionCode 78; no data, profile, business logic, task, schedule or widget changes.
- Immutable source commit 612ba1ba215e8c1871a654acda2304f5c44c678c; publication workflow commit 49ae8ce49b6dfc90cfd094a17c07bfd815faf2a0.
- Main CI https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42373593478 SUCCESS (unit tests and APK). Publisher CI https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42373854191 SUCCESS (repeated tests, stable-signed APK, ZIP and release).
- Release https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.56-glass-beta; APK SHA-256 f01d6f88729a9227ee7813e281f733c711cfab42b621f9f9a557eb3e244cb69b.
- Physical HONOR test of final alignment and section-creation tap remains outstanding.

## Liquid Glass architecture audit (2026-11-07, documentation only)
- Reviewed NavGlass.kt + NavTasksUi.kt at f6cb1eb0d9a4a11072f11cc964a4d534416cb9bc versus Telegram Android master 3de37c8cfb039452be7398909461da6288babf15. Audit: docs/development/GLASS_TECH_AUDIT.md.
- Current Haze 1.7.1 genuinely blurs the background; it does not have Telegram-style AGSL refraction. Multiple tinted fill layers can make the dock appear matte. No NavTasks application source changed, no version bump; latest signed release remains 0.4.56.
- Proposed Stage 0 device baseline and Stage 1 material opacity/edge review are **not yet approved implementation**; Stage 2 optional API33+ shader would require separately tested, license-safe code.
