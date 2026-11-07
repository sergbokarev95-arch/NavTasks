# NavTasks — Next Tasks

## Current stage: 0.4.53 monthly-calendar visual update; CI and release pending

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
- Next: CI test+build, stable-signed GitHub APK/ZIP from immutable source, update all docs with actual commit/runs/assets, then receive HONOR screenshots in both themes.
