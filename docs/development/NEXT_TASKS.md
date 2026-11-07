# NavTasks — Next Tasks

## Current stage: 0.4.54 published and CI-verified; HONOR day/night checks pending

User sent HONOR screenshots 2026-11-07 showing a straight cutoff line above system navigation in Services and AMOS, unlike Today. User explicitly requested removal.

### Scoped fix

- ServicesUi.kt and WorkOrdersUi.kt: remove only navigationBarsPadding() on root scrolling Column. Retain statusBarsPadding() and hazeSource() and the navigationBarsPadding() on the floating + buttons.
- Preserve LazyColumn bottom contentPadding 106dp so last cards/buttons are scrollable to a tappable position; content can draw behind the system gesture bar.
- Version 0.4.52 / versionCode 74; no data schema changes or business logic changes.
- Completed: main CI 42356909189 and signed publisher CI 42357102513 SUCCESS. Release https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.52-glass-beta, immutable source d38aa26ddeb8ac3fe5f568dddb1c551463fc5a4d, published APK SHA-256 e05d0691f0d0320c288e6b466507adc160cad2c13fd83422656daf9726eea3fb.

### HONOR acceptance

- Services and AMOS: scroll cards to the bottom; the container should fill behind the gesture region without a horizontal cutoff line.
- Floating + stays above Android system navigation; bottom buttons on last card can scroll above the gesture area and remain tappable.
- Test day/night and expanded/collapsed/search views; Android back gesture and scrolling unaffected.
- Preserve other screens and all previous 0.4.51 request-date validation; screenshots from user needed to confirm device result.

## Previous release

- 0.4.51 signed: https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.51-glass-beta; source 651948cb0c280cd63a418d0054515f4d83102ea9; CI tests/build passed.

## 0.4.52 release evidence
- Source commit d38aa26ddeb8ac3fe5f568dddb1c551463fc5a4d; signed publisher workflow commit f448c0f5225c6415d3cf414b1edcf2dff2a3bd8f.
- Main CI https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42356909189 SUCCESS; release CI https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42357102513 SUCCESS.
- Release https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.52-glass-beta; APK SHA-256 e05d0691f0d0320c288e6b466507adc160cad2c13fd83422656daf9726eea3fb. Hardware HONOR display and interaction acceptance NOT yet confirmed.
- Follow up after screenshots: look for bottom-edge transition, last-card tappability, floating Add, dark/day mode and edge Back gesture before closing this stage.

## Current user feedback — monthly Plan calendar (2026-11-07)
- In night-mode calendar, cyan outlines on every day are too harsh; use soft filled tiles without bright outlines, preserving a distinct but subtle Today.
- Saturdays/Sundays need visual separation: muted coral weekday labels and in-month date numerals, not aggressive red background; avoid confusing this with missed-task status.
- Numerals should be a little larger (14sp→16sp) and always stay dead-center whether a bottom status dot is shown or not.
- Keep status dots, their existing color meanings, month navigation, swipe, year/day zoom, and task-business logic unchanged.
- Implemented in PlanAndTasksUi.kt; CalendarVisualRulesTest.kt added to assert weekend and dot semantics. Version 0.4.53 / versionCode 75.
- Completed CI and signed publication: https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.53-glass-beta, source 81cce3e29b74454f6329a124f9589570a9cb2722; CI 42365740578 and publisher 42365940441 SUCCESS. Device visual check still outstanding.

## 0.4.53 completion evidence and current acceptance
- Immutable source commit: 81cce3e29b74454f6329a124f9589570a9cb2722; publisher workflow commit: e556ab3b4993d7a877e8885bf6d5045bae136505.
- Main CI https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42365740578 SUCCESS (unit tests and APK).
- Publisher CI https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42365940441 SUCCESS (repeated tests, stable-signed APK and ZIP).
- Release https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.53-glass-beta, APK SHA-256 c7dd50a87bd36e9efaf7ddfcdc4ef55c40dff7be645314e536f7b373e16d7b73. On-device HONOR layout checks still pending.
- On HONOR: compare month view in day/night, with and without status dots; check Saturdays/Sundays colored subtly, all numerals center-aligned, no bright month-date borders, and Today is still clearly visible. Confirm day selection/swipe and marker meaning unchanged.
- Other open visual checks: 0.4.52 edge-to-edge Services/AMOS, notification icon and home widget appearance; no user feedback has conclusively closed all of them.

## 0.4.54 — Apply weekend accent to Today (2026-11-07)
- User asked «А почему в сегодня не поменял?» with HONOR screenshot: previous 0.4.53 had updated only Plan, leaving the Today seven-day strip untouched.
- Fix implemented in NavTasksUi.kt: Today DayChip receives calendarIsWeekend(day). Both top day label and date number use exactly the same muted coral ink in night/day themes as Plan (shared calendarWeekendInk(p)).
- Keep selected day blue glass fill/contrast, chip geometry, non-weekend colors, seven-day scrolling, missed-count animation and click behavior unchanged. Increase Today date number 15 -> 16sp.
- Tiny PlanAndTasksUi.kt refactor only exposes the shared weekend palette; Plan appearance must not change. Regression CalendarVisualRulesTest includes the two palette colors.
- Completed: main CI https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42366929632 SUCCESS; stable-signed publisher CI https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42367156700 SUCCESS. Release https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.54-glass-beta, source 7e04c01a67b5979ae2b02dbf9ad1be6ccf21d16f, APK SHA-256 f3b2ab0a8c74836469875ba8e0c25dd711ea3cf58f67ccee4c1f5a2325cb63ac.
- HONOR acceptance: Saturday/Sunday coral in Today including selected weekend, non-weekdays retain existing color; Plan visuals unchanged; check RU/EN and day/night. No phone verification yet.

## Latest evidence and next device acceptance
- 0.4.54 source commit 7e04c01a67b5979ae2b02dbf9ad1be6ccf21d16f; signed release workflow commit 8ff1fc48a05c00a5635e3da8ca9ce330467f8cb0; APK versionCode 76.
- CI 42366929632 SUCCESS, publisher 42367156700 SUCCESS; latest release https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.54-glass-beta; SHA-256 f3b2ab0a8c74836469875ba8e0c25dd711ea3cf58f67ccee4c1f5a2325cb63ac.
- User HONOR acceptance pending: Today Sat/Sun coral labels/numerals with selected and unselected states (RU/EN, dark/light); other days and selected glass unchanged, horizontal scrolling and Missed chip intact. Confirm Plan calendar remains the 0.4.53 design.
- Earlier open checks include 0.4.52 Services/AMOS lower edge, icon in notification, widgets.
