# NavTasks — New Chat Handoff

1. Connect GitHub account sergbokarev95-arch; open private repo sergbokarev95-arch/NavTasks.
2. Read all five docs/development/*.md files first. Confirm main HEAD, app/build.gradle.kts versionName/versionCode, recent commits, GitHub Releases, Actions and relevant feature branches. If GitHub is newer, update these documents before acting.
3. Source: app/src/main/java/com/navtasks/app/; resources: app/src/main/res/; tests: app/src/test/java/com/navtasks/app/.
4. Current stage: **v0.4.59 CI-verified signed rollback of dock rendering only; HONOR confirmation pending.** v0.4.57 and v0.4.58 optics were rejected for text shimmer. All current application modules and data stay intact; v0.4.56 is the stable dock source. User still needs to visually verify notification icon and responsive widgets on HONOR. Keep 0.4.46 AMOS due-only Today logic.
5. Build: JDK 17, Android SDK 36, Gradle 8.13; `gradle :app:testDebugUnitTest --no-daemon` then `gradle :app:assembleDebug --no-daemon`. GitHub workflow .github/workflows/android-debug.yml uses stable beta signing secrets; do not disclose or overwrite signing keys. Signed release workflows use pinned source commits and publish APK/ZIP to GitHub Releases.
6. Debug build uses applicationId suffix .glass and versionName suffix -beta. Avoid replacing production application or clearing user data.
7. Preserve profiles, task recurrence, completion history, service/AMOS scheduling, notifications, import/export, existing widgets and navigation. Use small diffs and regression tests.
8. For each completed stage: code review, tests, CI evidence, publish APK only when verified, then update CURRENT_STATE, NEXT_TASKS, DECISIONS, CHANGELOG_DEV and HANDOFF with actual commit/release/test evidence.
9. Do not claim physical HONOR/Samsung tests based solely on GitHub Actions success.

## Short continuation prompt

«Продолжай NavTasks из GitHub sergbokarev95-arch/NavTasks. Сначала прочитай docs/development/HANDOFF.md и остальные четыре документа, проверь main, последнюю версию, коммиты и CI. Работай точечно, не ломай данные и не меняй код до согласования текущей задачи. После успешного этапа обнови документацию в GitHub».

## 0.4.48 visual verification after build

- Lock screen / notification drawer: original color app icon, not generic square checkmark; status bar Android small icon remains monochrome.
- Today: missed 0 neutral; missed >0 red glyph/border/count without breaking date row or night mode.
- Widgets: narrow 180dp, wide 300dp, wide >=350dp, portrait/landscape, dark/light; list capacity, text clipping and tap behavior.
- Red missed widget pill should open Today > Missed from both cold start and running app. If it does not, fix the deep link without changes to task data.

- Do NOT distribute 0.4.47: additional code review found an unsupported Space view in the large widget XML. The corrected release target is 0.4.48 / versionCode 70 and must have its own passing CI and GitHub release. Preserve this explanation for provenance.

## Latest verified handoff (2026-11-07)

- Latest release: https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.48-glass-beta — v0.4.48-glass-beta (versionCode 70).
- Immutable released source: 2968dcce377f531b4bc3a57166cb8e0b62d1e1de. Latest publisher workflow commit: 0b77bce58613dbf828b17b8e6dc5ae1a081ecb34.
- Main test/build: https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42332014351 SUCCESS. Publisher: https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42332255873 SUCCESS.
- v0.4.47 is marked obsolete in releases due to an unsupported wide-widget Space. Never recommend installing it.
- Next action: receive HONOR screenshots/feedback on notification identity, Today Missed red glass, compact/wide widget sizes, missed deep link, and 13 Nov AMOS timing. Device result unknown until reported.
- Restart prompt: «Открой GitHub sergbokarev95-arch/NavTasks, прочитай docs/development/HANDOFF.md и CURRENT_STATE.md, проверь последнюю ветку/релиз, затем продолжи с тестов 0.4.48 на HONOR. Сохраняй все изменения и handoff в GitHub».

## 0.4.49 continuation (2026-11-07)

- User accepted: Today Missed chip must disappear entirely if missedCount=0. Existing 0.4.48 kept a neutral 0 chip; remove it and the 7dp gap only from Today, not home widget.
- Implementation touches NavTasksUi.kt and TodayFrequencyGroupingTest, bumps versionCode to 71 / versionName 0.4.49; published with successful CI (details below).
- Verify 0->1, 1->0 and returning from Missed backlog, screen width, dates scrolling, day/night, and no AMOS effects.

## Latest verified release — 0.4.49 (2026-11-07)
- Release: https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.49-glass-beta, APK/ZIP, versionCode 71, stable Beta signing.
- Immutable source: 41ae92386e1459099a6c1c3c069a987c5de04203; publication workflow commit 5dedee07a6f14d95f4730e52ee5fef62da6f8096.
- Main CI: https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42335111965 SUCCESS.
- Publisher CI: https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42335349500 SUCCESS.
- Only UI affected: Today Missed control hides at zero, 180 ms horizontal enter/exit red pill at positive count, date row reclaims space; empty Missed mode automatically closes. Tests cover zero and positive counts.
- No profile/data/AMOS/widget/notification changes in this release.
- User HONOR screenshot/test pending. Inspect the 0↔1 missed transition, day/night, date-row fill/scroll and Back navigation. Do not claim phone testing from CI alone.
- Recovery prompt: «Продолжи NavTasks из GitHub sergbokarev95-arch/NavTasks. Прочитай docs/development/HANDOFF.md и CURRENT_STATE.md; проверь main, последнее v0.4.49-glass-beta и CI. Текущая задача — проверка исчезающей плашки Пропущенные на HONOR. Работай точечно и обновляй docs/development после этапа».

## New pending finding (2026-11-07)

While testing 0.4.49, user requests `Сервисы > Новый сервис` type Planned/Unplanned. Planned has next due date and normal intervals. Unplanned needs AMOS request-number field at creation and must not be treated as a scheduled/overdue maintenance date. Current ServiceEntry forces nextDate; cross-module due/notification/calendar code uses it. See NEXT_TASKS.md for full audit/proposal. No change implemented; preserve old planned records and request-number history when modifying. Treat integration with external AMOS as out of scope unless requested explicitly.

## 0.4.50 source stage (2026-11-07)

- User approved build of Planned/Unplanned Services. Changed files: ServiceStore.kt, ServicesUi.kt, WorkAgenda.kt, WorksHubUi.kt, NavNotifications.kt, ServiceScheduleTest.kt, WorkAgendaTest.kt, SelectiveExchangeTest.kt, app/build.gradle.kts. Only Service model/presentation and its direct due/notification dependencies.
- Explicit unplanned Boolean (default false for legacy JSON); nextDate null for unplanned (no fake deadline), scheduledDate in history nullable. For unplanned lastDate is request creation date. Planned records remain unchanged semantically.
- Unplanned service creation requires AMOS requisition number; number is entered manually, not sent to external AMOS. Request persists in history after completion and does not create future dates. Selective share scrubs the number, so imported unscheduled draft needs a new number before completion.
- **CI, signed APK and release verified** (links below). Physical HONOR testing still required. 0.4.49 is the previous released baseline.
- Next: install and test 0.4.50 on HONOR for planned default vs unplanned editor, existing planned data, request validation, completion history, calendar/Today and notifications, profile export/import, then update docs.

## Latest verified handoff — 0.4.50
- Source commit 0a3467be369d889774b653d111917d7a6606a520; publisher workflow commit c12e0000d04aa427fa36cb600cdd29fb10216a74.
- Main CI https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42350648226 SUCCESS; publisher https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42350831779 SUCCESS.
- Release https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.50-glass-beta, versionCode 72, APK SHA-256 63e814be42a5a9f3af44f3551bf5024ea16ed6c00aab3ccd127023cf00e6680d.
- Next task: user tests unplanned maintenance creation, AMOS number, no fake due date, planned compatibility, completion history, no false Soon/Overdue/Calendar/notifications, data exchange. No phone tests have been performed here.
- Continue prompt: «Подключись к GitHub sergbokarev95-arch/NavTasks, прочитай docs/development/HANDOFF.md и CURRENT_STATE.md, проверь 0.4.50 и CI, продолжи точечную разработку после тестов на HONOR, сохраняй документацию в GitHub».

## New 0.4.51 date-picker change (2026-11-07)
- User request: in Services > New service > Unplanned, date of AMOS request must be editable, because request might have been written on a different day. Date is **informational only**.
- Staged files ServicesUi.kt, ServiceScheduleTest.kt, app/build.gradle.kts; versionName 0.4.51 / versionCode 73. Existing Unplanned lastDate stores the request date; no schema modification. New entries start today, existing Unplanned entries reopen at saved date. Existing NavDateWheelDialog reused.
- CI and signed GitHub release SUCCESS; see verified evidence below. Next: HONOR test, not yet performed. Do not conflate request date with maintenance due or completion date.

## Verified latest release — 0.4.51 (2026-11-07)
- Release https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.51-glass-beta; APK and compact ZIP, versionCode 73, stable Beta signing.
- Immutable source 651948cb0c280cd63a418d0054515f4d83102ea9; publisher workflow commit 48c55cd46d37bb66df7d10fc57cab801e488f694.
- Main CI https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42354150215 SUCCESS; publisher CI https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42354348234 SUCCESS.
- APK SHA-256 ea58622fad66204e812dfdaea6ed7e8546ed98e672c6764481dd91f35ae8e4c3. Since 0.4.50 only ServicesUi.kt, ServiceScheduleTest.kt and app/build.gradle.kts changed in application source; no schema migration.
- Device acceptance outstanding: selecting a past AMOS request date, storing/editing it, verifying service card and history remain correct; no deadline/notification/calendar effects. Ask for screenshots/feedback and only then close stage.
- Recovery prompt: «Открой GitHub sergbokarev95-arch/NavTasks, прочитай docs/development/HANDOFF.md и CURRENT_STATE.md, проверь версию 0.4.51, CI и релиз. Продолжи тестирование даты заявки AMOS на HONOR, работай точечно и сохраняй данные и документацию».

## Current task — 0.4.52 bottom edge (2026-11-07)

- User supplied HONOR screenshots: in Services and AMOS list cards are clipped visually above the Android gesture navigation area, unlike Today. Requested removal of the artificial bottom boundary.
- Root cause: ServicesUi.kt and WorkOrdersUi.kt both used Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().hazeSource(...)). Remove navigationBarsPadding() ONLY from the full scrolling Column, keeping it on floating Add buttons and keeping bottom 106dp LazyColumn content padding.
- Source version 0.4.52 / versionCode 74; no changes to data, business logic, notifications, AMOS schedule or other screens.
- CI and stable-signed release SUCCESS; see verified links below. On HONOR check that content extends behind gesture bar without a sharp cutoff, while + and last-card buttons stay accessible.
- 0.4.52 is now the latest tested-and-published release; 0.4.51 is previous release. Hardware acceptance pending.

## Latest verified 0.4.52 release (2026-11-07)
- Release https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.52-glass-beta (APK and compact ZIP), versionCode 74, stable Beta signing.
- Source commit d38aa26ddeb8ac3fe5f568dddb1c551463fc5a4d; signed publication workflow commit f448c0f5225c6415d3cf414b1edcf2dff2a3bd8f.
- Source CI https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42356909189 SUCCESS; signed publish https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42357102513 SUCCESS.
- APK SHA-256 e05d0691f0d0320c288e6b466507adc160cad2c13fd83422656daf9726eea3fb. No data/profile/schema changes and no changes to AMOS schedules, services models, notifications or widgets.
- Await screenshots from HONOR (day/night) of final Services and AMOS cards at the bottom and floating Add buttons; user acceptance not yet established.
- Recovery prompt: «Восстанови NavTasks из GitHub sergbokarev95-arch/NavTasks. Прочитай docs/development/HANDOFF.md, CURRENT_STATE.md и NEXT_TASKS.md, проверь main и последний релиз 0.4.52. Продолжим тест нижней границы в Сервисах и AMOS. Изменения только точечные, обновляй документацию в репозитории».

## Released 0.4.53 monthly calendar update (2026-11-07)
- User sent December 2026 dark-mode calendar screenshot with harsh neon-blue outlines, inconsistent vertical day-number centering due to status dots, and too-small same-color weekend numerals. Requested softer minimalist appearance, slightly larger digits and weekend distinction.
- Source stage 0.4.53/versionCode 75: PlanAndTasksUi.kt removes month cell borders, uses subtle fills, muted coral Sat/Sun in month and header, fixes numeral at Box center with status dot independently at bottom, increases 14→16sp. Status-dot inclusion and colors preserved; year/day views and task calculation unchanged. CalendarVisualRulesTest.kt covers weekend and status semantics.
- Source CI and signed release verified: 81cce3e29b74454f6329a124f9589570a9cb2722; publisher workflow e556ab3b4993d7a877e8885bf6d5045bae136505; main CI 42365740578 SUCCESS; publisher 42365940441 SUCCESS. https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.53-glass-beta, APK SHA-256 c7dd50a87bd36e9efaf7ddfcdc4ef55c40dff7be645314e536f7b373e16d7b73. HONOR day/night visual acceptance pending.

## Latest handoff (0.4.53)
- Stable-signed Beta release https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.53-glass-beta; versionCode 75. Immutable source 81cce3e29b74454f6329a124f9589570a9cb2722; publisher e556ab3b4993d7a877e8885bf6d5045bae136505. Main test/build https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42365740578 SUCCESS; publisher https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42365940441 SUCCESS.
- No profile/schema/schedule changes; only month grid presentation and pure tests. APK SHA-256 c7dd50a87bd36e9efaf7ddfcdc4ef55c40dff7be645314e536f7b373e16d7b73.
- Next: HONOR screenshots of Plan month with/without dots, including weekends in day and night themes. Verify calendar swipe/tap, Today highlight, no harsh borders. Continue pending 0.4.52 lower-edge acceptance separately.
- Recovery prompt: «Продолжим NavTasks из GitHub sergbokarev95-arch/NavTasks. Сначала прочитай docs/development/HANDOFF.md, CURRENT_STATE.md и NEXT_TASKS.md; сверь main и последний релиз 0.4.53. Текущая задача — проверить календарь на HONOR: выходные коралловым, без синих рамок, числа по центру, точки снизу. Меняй код точечно, обновляй docs/development после этапа».

## Current 0.4.54 task — Today weekend styling (2026-11-07)
- User noticed 0.4.53 Plan-only calendar refinements did not also color Sat/Sun inside Today 7-day horizontal chips. Today has separate NavTasksUi.kt DayChip.
- Minimal implementation under CI: pass calendarIsWeekend(day) to DayChip; render top caption and number in shared calendarWeekendInk(p), which is defined in PlanAndTasksUi.kt and used by Plan too; increase Today number 15sp to 16sp. Selected blue glass, unselected weekdays, layout, dates/scroll and missed behavior stay unchanged.
- Code changes: NavTasksUi.kt, PlanAndTasksUi.kt (extract same pre-existing color into shared pure helper), CalendarVisualRulesTest.kt, app/build.gradle.kts (0.4.54, versionCode 76). No data/schema/business-logic modifications.
- 0.4.54 tests and signed release PASSED: source 7e04c01a67b5979ae2b02dbf9ad1be6ccf21d16f, publisher 8ff1fc48a05c00a5635e3da8ca9ce330467f8cb0; CI 42366929632 SUCCESS, publisher 42367156700 SUCCESS. Released https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.54-glass-beta; APK SHA-256 f3b2ab0a8c74836469875ba8e0c25dd711ea3cf58f67ccee4c1f5a2325cb63ac. HONOR light/dark screenshot test pending.

## Latest verified handoff — NavTasks 0.4.54 (2026-11-07)
- Latest release https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.54-glass-beta, versionCode 76, same Beta signing. Source 7e04c01a67b5979ae2b02dbf9ad1be6ccf21d16f, publisher 8ff1fc48a05c00a5635e3da8ca9ce330467f8cb0.
- Main CI https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42366929632 SUCCESS; signing/publishing https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42367156700 SUCCESS. APK SHA-256 f3b2ab0a8c74836469875ba8e0c25dd711ea3cf58f67ccee4c1f5a2325cb63ac.
- Next user testing: Today Saturday/Sunday both use muted coral caption and 16sp numeral, even if day selected; selected blue glass unchanged. Compare against Plan month weekend coral in light and dark, RU/EN. Existing date strip scrolling, missed behavior, profile data unaffected. Physical device testing still needed.
- Recovery prompt: «Продолжай NavTasks из GitHub sergbokarev95-arch/NavTasks. Прочитай docs/development/HANDOFF.md, CURRENT_STATE.md и NEXT_TASKS.md. Проверь main, последний релиз 0.4.54, Actions. Следующая задача — HONOR-проверка выходных в Сегодня и Плане и дальнейшие точечные исправления; не ломай рабочую логику».

## Current stage — 0.4.55 right-side Add buttons (2026-11-07)
- User stated the + buttons should be on the right. Actual code had Tasks bottom-left above dock (NavTasksUi.kt), Services bottom-left (ServicesUi.kt), AMOS bottom-left (WorkOrdersUi.kt), with Today calendar already bottom-right.
- Source implementation strictly changes BottomStart/start 19dp -> BottomEnd/end 19dp in those three Add wrappers; keeps bottom 83dp for Tasks and 18dp for Services/AMOS, plus existing navigationBarsPadding() and callbacks. App 0.4.55/versionCode 77. No data modifications.
- CI and signed GitHub release SUCCESS, see evidence below. On HONOR verify right-side + in all three screens, proper Add editor opening, safe-area layout, day/night and gesture navigation. Physical tests not yet performed.

## Latest verified release — 0.4.55 (2026-11-07)
- Release https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.55-glass-beta, versionCode 77, signed APK+ZIP with stable Beta key. Immutable source commit 401564269c574271b1e3c22c356989fe2d94eb58; publisher workflow commit aa4ed6b8f4140e7f78364819a8a8f1998e0ab279.
- Main CI https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42369659340 SUCCESS; signed release CI https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42369873080 SUCCESS.
- APK SHA-256 156b24d04dcbb496e8480ce463423e430651d40fe412e42b2f9202ace07229be. No data/profile/schema/task/service/AMOS logic changes; only right-side Add (+) placements in Tasks, Services and AMOS.
- Next HONOR acceptance: screenshots/tap on Tasks, Services and AMOS + controls in dark/light themes. Check last-card tap/swipe and bottom safe area. Calendar icon in Today must remain right-aligned. Device test pending.
- Restart instruction: «Продолжим NavTasks через GitHub sergbokarev95-arch/NavTasks. Прочитай docs/development/HANDOFF.md, CURRENT_STATE.md, NEXT_TASKS.md, проверь main и релиз 0.4.55. Текущий этап — тест плюсов справа на HONOR. Все изменения точечные, сохраняй функциональность и обновляй документацию».

## Pending 0.4.56 (2026-11-07): Handover (+) placement
- User screenshot revealed Handover (+) stayed at lower-left after v0.4.55's Tasks/Services/AMOS right-alignment. Separate implementation: app/src/main/java/com/navtasks/app/HandoverUi.kt.
- Code updated only its floating Add Box to Alignment.BottomEnd with padding(end=19.dp); preserve navigationBarsPadding, bottom 83dp embedded / 18dp standalone, and openSectionEditor(null) action. Build version 0.4.56 / versionCode 78; docs updated.
- Main CI and signed GitHub release SUCCESS; details below. Next user tests Handover + placement and New Section dialog on HONOR.

## Latest verified handoff — v0.4.56 (2026-11-07)
- Release https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.56-glass-beta; stable Beta-signed APK/ZIP, versionCode 78. Immutable source 612ba1ba215e8c1871a654acda2304f5c44c678c, publisher workflow commit 49ae8ce49b6dfc90cfd094a17c07bfd815faf2a0.
- Main CI https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42373593478 SUCCESS; publisher https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42373854191 SUCCESS. APK sha256:f01d6f88729a9227ee7813e281f733c711cfab42b621f9f9a557eb3e244cb69b.
- Only app behavior modified since 0.4.55: Handover floating Add placement from left to right; no data or logic modifications. On-device testing still outstanding.
- Test: from Handover tab, + sits bottom-right above dock; opens 'New section' and does not overlap gesture bar, in both themes. If user confirms, close the task.
- Recovery prompt: «Продолжай NavTasks из GitHub sergbokarev95-arch/NavTasks. Прочитай docs/development/HANDOFF.md и CURRENT_STATE.md, проверь main и релиз 0.4.56. Следующий этап — HONOR-проверка кнопки + в Handover справа. Работаем точечно, не ломаем существующие функции и обновляем документацию».

## Glass rendering research (2026-11-07, no source changes)
- Read docs/development/GLASS_TECH_AUDIT.md before proposing further Liquid Glass changes. NavTasks baseline 0.4.56 uses Haze 1.7.1 blur + gradient active lens; Telegram Android master 3de37c8cfb039452be7398909461da6288babf15 uses API33+ AGSL refractive shader in an optional graphics pipeline. This was an audit only: no code or app version changed.
- Recommended first: obtain HONOR day/night screenshots over real scrolling content, then, **only with user approval**, prototype minimal opacity/edge tuning in NavGlass.kt. Do not introduce a full RenderNode/shader port or copy GPL-2.0 Telegram code. Keep all four tabs, active indicator geometry, gestures and data paths intact. Previous 0.4.56 device acceptance remains pending.

## Current 0.4.57 experiment — genuinely refractive dock (2026-11-07)
- The user wanted dock glass to be *real*, not only a translucent container, following a read-only audit saved in GLASS_TECH_AUDIT.md.
- Target version 0.4.57/versionCode 79. Code changed only: NavGlass.kt (split Haze material backdrop from glass face and icons; reduce opaque tints, soften blur, translucent pill), new NavGlassOptics.kt (independently authored AGSL API33+ edge refraction of Haze layer), NavGlassOpticsTest.kt (capability fallback), app/build.gradle.kts. No Haze dependency version change and no data or business logic changes.
- The shader is applied only to the background Box, not the Row/Canvas/icons; graphicsLayer renderEffect on the independent Haze Box. Fallback for API 31/32 or creation exception is Haze. Older signed release 0.4.56 remains the verified baseline until CI+release.
- Main CI 42376895012 and signed publisher CI 42377117640 succeeded, APK/ZIP released from source 3d2ffdeead8c607d2e6f7b43d126d18ae6593581; release https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.57-glass-beta; APK SHA-256 b8de98ae598396405ac4145e007a4ca1de629146ea14e394dd41f32200db354c. Next: HONOR light/dark screenshots over moving content, fast tab switching, frame pacing and no flicker. CI does NOT constitute visual QA.
- On any crash, severe lag or rendering glitch, revert NavGlass optics only. Do not roll back unrelated Services, AMOS, Handover, task and profile data.

## Latest signed handoff — 0.4.57 (2026-11-07)
- Release https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.57-glass-beta, versionCode 79, stable-beta signed. Immutable source commit 3d2ffdeead8c607d2e6f7b43d126d18ae6593581; publication workflow f41dc24810e0c8ce4e90b36cf72927b6cf4f8fc9; main CI https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42376895012 SUCCESS, publisher CI https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42377117640 SUCCESS.
- APK SHA-256 b8de98ae598396405ac4145e007a4ca1de629146ea14e394dd41f32200db354c, ZIP SHA-256 63430ee5388d5e6cf7548802f332f9a02c32dbcb13519f7f9ffd1925356d911c. App changes limited to NavGlass.kt, new NavGlassOptics.kt, tests and Gradle version. No data/schema changes.
- Next question is **physical glass acceptance**: on HONOR show content scrolling under dock in both themes, confirm visible optical bending, clarity, tab geometry, touch and motion; compare against 0.4.56. For black empty Today note that there are no refractive details to bend.
- Recovery: «Открой GitHub sergbokarev95-arch/NavTasks и прочитай docs/development/HANDOFF.md, CURRENT_STATE.md и GLASS_TECH_AUDIT.md. Последняя версия 0.4.57, подписанный экспериментальный док с AGSL/Haze. Проверь main и CI; дальше тест стекла на HONOR и только точечные изменения без потери данных. После правок обновляй docs/development».

## 0.4.58 source hotfix handoff (2026-11-08)
- User posted HONOR screenshot of Handover and reported *text under dock flickers/highlights* and *dock appears smaller*. This is feedback against 0.4.57; not an accepted release.
- Root cause in NavGlass.kt: 0.4.57 rendered material after 1dp x 2dp content padding (56dp dock visually becomes 52dp). Shader rim 13dp/bend 5.5dp and Haze blur only 15dp overexposed/warped scrolling letterforms.
- Source changes limited to NavGlass.kt (render Haze/face/optics at 56dp, keep original foreground inset, nav blur=30dp/modestly higher tint), NavGlassOptics.kt (rim=5dp/bend=1.2dp), NavGlassOpticsTest.kt (strength bounds) and app/build.gradle.kts (0.4.58/versionCode 80). No data/storage/logic changes or library upgrades.
- CI/test/build SUCCESS, stable Beta-signed release published at https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.58-glass-beta, source 4118106265361b3e28b2f4636f5c6c17def6801c, publisher 6abcfc23221872ab021f38d60725d88b159f6b2f. Main CI 42381839037 SUCCESS; signed publisher 42382154553 SUCCESS; APK sha256:340e1851179f1ef2a1350638df3454cfdde1d279ed6bccfc15fac82a187a1914.
- **Next:** HONOR visual test: same geometry as 0.4.56, no shimmering text during scroll, acceptable Day/Night transparency, smooth rapid tab switching. If still flickers, disable AGSL layer only in a new higher-version patch, keep Haze and app data.

## Latest verified 0.4.58 handoff (2026-11-08)
- VersionName 0.4.58, versionCode 80, stable-beta APK https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.58-glass-beta, immutable source 4118106265361b3e28b2f4636f5c6c17def6801c, publisher workflow 6abcfc23221872ab021f38d60725d88b159f6b2f.
- Main CI https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42381839037 SUCCESS; publisher CI https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42382154553 SUCCESS. APK SHA-256 340e1851179f1ef2a1350638df3454cfdde1d279ed6bccfc15fac82a187a1914; ZIP SHA-256 382e4ce9b3cb4a3b7aac244fbf009df6a7b19ed5620c561d8997ab25337eaa42.
- Real HONOR observations required: Handover scroll behind dock (no temporal shimmer), dock outer size full 56dp, no moving selection distortion, day/night and rapid taps. Do not call the shader approved until user accepts. If this fails, disable AGSL only in subsequent signed hotfix; never uninstall app to downgrade user data.
- Recovery prompt: «Продолжаем NavTasks из GitHub sergbokarev95-arch/NavTasks. Прочитай docs/development/HANDOFF.md, CURRENT_STATE.md и GLASS_TECH_AUDIT.md. Последний опубликованный релиз 0.4.58 исправляет уменьшившийся док и подсвечивающийся текст 0.4.57. Проверь HONOR-результат; если текст всё ещё мерцает — точечно отключи AGSL с сохранением Haze и всех данных».

## Immediate handoff — 0.4.59 remove failing optics experiment (2026-11-08)
- HONOR tested v0.4.58 and reported persistent animated-text shimmer/jitter below dock, which still looked like a transparent container. Both v0.4.57 and v0.4.58 are **visually rejected**. Never present them as accepted glass.
- v0.4.59 targeted source fix restores `NavGlass.kt` **exactly** from v0.4.56 stable source commit `612ba1ba215e8c1871a654acda2304f5c44c678c`; do NOT restore the entire app or downgrade version/data. Keep latest application code and modern + buttons, calendars, AMOS/service logic intact.
- New AGSL files NavGlassOptics.kt / NavGlassOpticsTest.kt remain for reference but running NavGlass does not invoke them. VersionName 0.4.59, versionCode 81, signed release published. Source b22057ad92b0f531df12217c4ecee97150395951, release https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.59-glass-beta.
- CI 42383033196 SUCCESS; publisher 42383289163 SUCCESS, publisher commit 28f6245873e0613cda7f17e9d3da7e9ab1ea7612; APK sha256:24dbfa7c439e95775e86abd8e064efdf119417903f2d2369390405b83090ab8e; ZIP sha256:454dbfd31aefe0b383d2d4ed4c1656d089fc5c079dabc2e705d8e90a1eaf1f33. Next: user tests Handover/Today under dock in day/night. If still flickers, isolate `hazeSource` rather than restarting AGSL. Authentic refraction NOT achieved.

## Latest signed v0.4.59 handoff — 2026-11-08
- Release https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.59-glass-beta; source b22057ad92b0f531df12217c4ecee97150395951; publisher 28f6245873e0613cda7f17e9d3da7e9ab1ea7612; versionCode 81; CI 42383033196 SUCCESS, 42383289163 SUCCESS. APK SHA-256 24dbfa7c439e95775e86abd8e064efdf119417903f2d2369390405b83090ab8e, ZIP SHA-256 454dbfd31aefe0b383d2d4ed4c1656d089fc5c079dabc2e705d8e90a1eaf1f33.
- Runtime NavGlass.kt is byte-for-byte the proven v0.4.56 Haze renderer; publisher workflow checks exact git object hash. AGSL experiment is dormant. No modern feature or data rollback.
- Next: confirm no shimmering while Handover/Today scroll beneath the dock on HONOR (Day/Night, fast tab switching), then only research true glass separately in an isolated GPU preview.
- Recovery prompt: «Продолжим NavTasks из GitHub sergbokarev95-arch/NavTasks. Прочитай docs/development/HANDOFF.md и CURRENT_STATE.md. Последний релиз 0.4.59 — возврат стабильного дока 0.4.56 после визуально неудачных 0.4.57 и 0.4.58. Дальше HONOR-проверка и только точечные изменения».

## 2026-11-08 update — Separate true-glass laboratory built, production untouched
- Production `main` stays NavTasks 0.4.59 with stable restored Haze dock. User wants real optical Liquid Glass akin to Telegram, but previous v0.4.57/0.4.58 custom shader attempts shimmered and were rejected.
- New experimental **branch** `experiments/liquid-glass-backdrop` contains `:glasslab` separate test app `com.navtasks.glasslab` (not a production update), using LayerBackdrop GPU scene capture + blur/lens Backdrop 1.0.6. Direct dependency shapes 1.2.0; compileSdk36; no Telegram GPL code copied. The lab is designed to test moving content with Glass ON/OFF and Day/Night toggles, four-slot 56dp dock.
- CI https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42385989762 SUCCESS; tested source `512cb355d0e1a37cba22eb79e5ada7cc32294eab`; prerelease https://github.com/sergbokarev95-arch/NavTasks/releases/tag/glasslab-v0.1.0; APK https://github.com/sergbokarev95-arch/NavTasks/releases/download/glasslab-v0.1.0/NavTasks_LiquidGlass_Lab_0.1.0.apk sha256:caeb1cbc7189e1aca707416f3f3660696dce1438fd546811099e8e7fcb24063c.
- User has NOT yet confirmed HONOR appearance/performance. Next: review Day/Night and scrolling/fast tab switching in **Lab**. If accepted, propose minimal NavGlass integration separately with 0.4.59 unchanged until then. Full details at `docs/development/GLASS_LAB.md` on the experiment branch.
- Recovery prompt: «Продолжай NavTasks из GitHub. Стабильный main 0.4.59. Для Liquid Glass открой ветку experiments/liquid-glass-backdrop и docs/development/GLASS_LAB.md. Отдельный тестовый APK Glass Lab 0.1.0 уже собран, GH Actions 42385989762 SUCCESS. Сначала получи HONOR-результат Glass ON/OFF над прокручиваемыми карточками, затем решай об интеграции. Код main не менять без проверки».


## 2026-11-08 — Liquid Glass Lab 0.1.2 signed; await HONOR lens animation verification
- User approved v0.1.1 direct Backdrop capture and card-border refraction visually (day/night screenshots). Separate lab now tests exact production four glyphs/colors and glass-to-matte transition; **not yet approved for main**.
- Experimental branch: `experiments/liquid-glass-backdrop`, immutable source `049836c2bd05bbd3378162e174a46f2ae249c240` (0.1.2 / versionCode 3); signed prerelease https://github.com/sergbokarev95-arch/NavTasks/releases/tag/glasslab-v0.1.2
- CI https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42389729911 SUCCESS; APK SHA-256 `fdeb481c91252db2c1e39f7ed02acba4541b2488f3a94bcc6d640b4f64a785c9`.
- Next: user visually tests real HONOR movement: production icons and colors, spring stretch/travel, neutral-grey lens once settled; cross-card border refraction, scroll smoothness, rapid 0↔3 switches, Day/Night.
- `main` app remains v0.4.59 / versionCode 81 with stable Haze; no production source change. Main branch docs-only updates may advance HEAD. Only after acceptance, stage isolated production integration without changing task/AMOS/profile logic.
- Lab history and rollback: backup branch `backup/glasslab-0.1.1-before-nav-animation`; see `docs/development/GLASS_LAB.md` on lab branch.


## 2026-11-08 — Glass Lab 0.1.3: 28dp expanding lens published
- User requested a much larger transient selected lens like screenshot reference; authorized first increase from 10dp to **28dp**, width only. Do not alter height or refraction while testing.
- Experiment-only source `9f3ac3c8574d539141dd828d76c9c08da28fbe6f`, branch `experiments/liquid-glass-backdrop`; CI `42390942917` SUCCESS and signed release https://github.com/sergbokarev95-arch/NavTasks/releases/tag/glasslab-v0.1.3
- APK SHA-256 `8f0b7e18cce69c007ff8a3f4e73a40da1affbdaffca3507420a72626ab6f83c9`. Separate package and signing retained; main NavTasks is still **0.4.59**, unchanged.
- Next: on HONOR test expanded travel 0↔3, dock edge containment, selected grey settled state and background border refraction, in both themes. Older 0.1.2 is backed up at `backup/glasslab-0.1.2-before-28dp-stretch`. Do not integrate until user approves.


## 2026-11-08 — Liquid Glass Lab 0.1.4 proportional lens ready for HONOR verification
- User clarified that 0.1.3's +28dp **width-only stretch** was wrong: on movement the whole active lens should grow **proportionally on both axes**, visibly protruding above and below the 56dp dock, then return to 52dp high and neutral grey on rest.
- Isolated experimental release `glasslab-v0.1.4` from commit `0ca6d8874a6c6febe86cb874b81ba0b846e087fe`. Max moving scale 1.30x on both width and height, 52dp → 67.6dp moving height, centered ±5.8dp outside dock; height measured without normal 56dp constraint by `wrapContentSize(unbounded=true)`.
- Signed APK https://github.com/sergbokarev95-arch/NavTasks/releases/download/glasslab-v0.1.4/NavTasks_LiquidGlass_Lab_0.1.4.apk; CI https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42392699540 SUCCESS. SHA-256 `7a1f9d7a372bc3d1985d7484983c830b883a63dd5b810ff8c40fd31b1e2856f0`.
- Next step: HONOR check during fast tabs and scrolling, both themes: physical protrusion top/bottom, no clipping, steady text, grey rest, unchanged icon sizes and border refraction. Only after user's approval consider production integration.
- Preserved `backup/glasslab-0.1.3-before-proportional-scale` and old prerelease. **Main NavTasks application remains 0.4.59/versionCode81, code unchanged.**


## 2026-11-08 — Glass Lab 0.1.5 fixes observed vertical cropping; HONOR retest next
- User HONOR screenshot of Lab 0.1.4 showed taller 1.30× lens still visually clipped at the 56dp dock top/bottom. Measurements alone did not prevent parent optical drawing clip.
- Lab 0.1.5 moves `drawBackdrop` backing/outline from parent to a separate background sibling. Enlarged selection (52→67.6dp) now has an unmodified parent so should render above and below dock, preserving 56dp bar, glyphs, colors, springs, card-border refraction, matte-grey resting lens.
- Source `ea412201936a8a178c657b77bda7a3347b878f06`, CI `42393384091` SUCCESS, signed https://github.com/sergbokarev95-arch/NavTasks/releases/tag/glasslab-v0.1.5. APK SHA-256 `c2e2d34e48578d189dd31238a1e04aa38137fb9d6ea022116821d7bd2c680f3f`.
- Backup `backup/glasslab-0.1.4-before-unclipped-layer`. Physical HONOR acceptance pending; DO NOT merge production. Main app v0.4.59 unchanged.


## 2026-11-08 — First full NavTasks Liquid Glass dock preview built; HONOR acceptance pending
- User approved physical appearance of standalone Glass Lab 0.1.5 including proportional 1.30x active lens protruding above/below 56dp dock; requested bringing glass into full app and eventually its controls.
- **Do not modify or merge stable main yet.** Created exact main-source backup branch `backup/navtasks-0.4.59-before-liquid-glass` at `9fcc2ee1a1a428ed4cec2a4ceae5ef1cba4c6675`; integration branch `feature/liquid-glass-dock-preview`.
- Tested full-app preview source `51f04825196093b70288bc078fe0874abf1d2723`; signed GitHub Actions `42397184202` SUCCESS (`:app:testDebugUnitTest`, `:app:assembleDebug`).
- Preview prerelease https://github.com/sergbokarev95-arch/NavTasks/releases/tag/dockpreview-v0.4.59-r1 ; signed APK https://github.com/sergbokarev95-arch/NavTasks/releases/download/dockpreview-v0.4.59-r1/NavTasks_LiquidGlass_Dock_Preview_0.4.59.apk ; SHA-256 `84f0609f7ce37d4818dcc3e4ec66fb1d8360af21d1c93b8dfd32a9ffaba87faa`.
- **Separate application** debug ID `com.navtasks.app.glasspreview` (stable debug uses `com.navtasks.app.glass`); stores/profiles NOT shared and neither installation replaces the other. Same visible launcher label/icon as NavTasks for this first preview.
- Only dock integration: dedicated `rememberLayerBackdrop` records page content, selected 1.30x refractive lens with independent backing, 4 production NavGlassGlyphs, day/night, matte grey rest. Existing `NavGlassBottomBar` stays for API31–32 and rollback; other floating buttons still use Haze. No task, Services, AMOS, Handover, data, notification, or widget logic edited.
- **Next:** user verifies new *full app* on HONOR: day/night, Today/Work/Handover/Tasks, real scrolling text/borders, 0↔3 quick taps, protrusion above/below dock, no shimmer or lag, grey rest. First-stage approval before extending same glass to '+', calendar and settings.


## 2026-11-08 — Glass Dock Preview R2: soften visible text behind the glass
- HONOR R1 screenshot: "Check ECDIS License" title remained sharply legible behind the lower dock and visually competed with navigation labels. User requested a **small additional blur**, not new lens distortions or a redesign.
- Branch `feature/liquid-glass-dock-preview`, source commit `0a5e91f2d5b10b811f4a763d922a683115b0a1cb`. Previous preview R1 source `51f04825196093b70288bc078fe0874abf1d2723` preserved as `backup/dockpreview-r1-before-soft-blur`, with unchanged old release.
- R2 changes only `NavLiquidDockOptics.bar.blurDp` 2→5dp and `selection.blurDp` 1.5→4dp. Optical refraction geometries remain bar 12/36dp and selection 10/18dp. Tab glyphs, colors, 1.30× tall animated capsule, direct content capture and business logic unchanged.
- Signed full-app preview R2 `com.navtasks.app.glasspreview`, versionCode 82/versionName `0.4.59-dock-preview2`; GitHub Actions https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42398375900 SUCCESS including full app unit tests and signed APK.
- Download https://github.com/sergbokarev95-arch/NavTasks/releases/download/dockpreview-v0.4.59-r2/NavTasks_LiquidGlass_Dock_Preview_R2.apk ; SHA256 `f8380c7b492220a79256fcc71bfe95e9e5fa217ecd5ad13eb9002c945202f236`.
- Physical HONOR assessment pending: text readability behind glass vs icons, remain crisp navigation labels, card borders still refract under scrolling, light and dark contrast and motion. **Stable main app still NavTasks 0.4.59/versionCode81; NO production code changed.**


## 2026-11-08 — v0.4.60 Exact-Lab in-place Beta CANDIDATE: real-device verification pending
- User rejected full-app Preview R1/R2's altered/muted glass; asked to transfer the already-approved standalone **Glass Lab 0.1.5** glass as faithfully as possible into the installed full NavTasks. The preview R2 blur (5dp/4dp) was not accepted.
- Isolated feature `feature/navtasks-liquid-glass-0.4.60-candidate`, tested source `c3cd12e37c91d789e54ae32e1b61d6d03f46ab79`. Ported the entire `GlassLabDock` from accepted lab source `ea412201936a8a178c657b77bda7a3347b878f06` with no changes to the actual material/lens body, except production palette, glyph/label and click callback bindings. Exact lab optics: outer blur 2dp, selection 1.5dp, refraction 12/36dp and 10/18dp, 56dp bar, 52→67.6dp moving capsule, 1.30× proportional scale, independent backing sibling, grey resting glass. Matched lab 15dp horizontal/8dp vertical insets, direct-scene capture; old Haze dock is still fallback API31/32.
- Unlike separate Preview R2, this **signed in-place update candidate** uses the same Beta `com.navtasks.app.glass` app ID and `betaStable` key as installed 0.4.59; versionCode 83, versionName `0.4.60-beta`. **BEFORE INSTALLING export a complete profile backup; never uninstall** (would erase local data). No task/AMOS/service/notification/business logic modifications.
- GitHub Actions https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42399961454 SUCCESS (all app unit tests, signed APK). Download https://github.com/sergbokarev95-arch/NavTasks/releases/download/v0.4.60-exactlab-candidate/NavTasks_Glass_0.4.60_ExactLab_Beta.apk ; SHA-256 `f360f897619c71df90eacdf52ad22db426ad17d404fabb02c270a193bd35004a`.
- **Physical HONOR acceptance NOT YET established.** Lab's colorful/high-contrast demo cards differ from live NavTasks dark content; even equal shader/material code cannot guarantee pixel-identical appearance. Test live text/card edges with scrolling, day/night, lens protrusion, repeated 0↔3 switches and legibility before merging/publishing as stable.
- `main` source remains stable 0.4.59, and `backup/navtasks-0.4.59-before-liquid-glass` preserves pre-port baseline. R1/R2 remain as separate preview releases. Next: HONOR screenshot/feedback and targeted material/scene-composition investigation only if still different.


## 2026-11-08 — Signed SAFE Haze rollback 0.4.61 prepared
- To avoid Android downgrade restrictions after testing signed in-place v0.4.60 (versionCode83), prebuilt a recovery APK using byte-stable 0.4.59 Haze sources with ONLY versionCode84/versionName0.4.61-beta.
- Isolated recovery branch `feature/navtasks-0.4.61-haze-rollback`, source `8158574bfac16ab9bc7baa648c4a7a2533cb906c`, CI https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42400505251 SUCCESS. Tested full app unit suite and aapt-verified actual `com.navtasks.app.glass` versionCode84; stable NavGlass and MainActivity blob SHA gates passed.
- Signed APK https://github.com/sergbokarev95-arch/NavTasks/releases/download/v0.4.61-haze-rollback/NavTasks_Stable_Haze_Rollback_0.4.61.apk ; SHA256 `cc2ae62b749b1873b72f3dcf8e18ffc030daae731d005e0a53e11d7b6252309a`.
- If 0.4.60 ExactLab material is rejected on HONOR, install 0.4.61 **as an update over it without uninstalling** to restore stable Haze while preserving local data. Export complete profile before *any* installation. New glass NOT merged; stable main application source 0.4.59 untouched.


## 2026-11-08 — A/B/C Liquid Glass capture diagnostics ready
- User's HONOR screenshots still show full-app glass unlike accepted Lab 0.1.5 despite exact production shader/optics. Hypotheses: differing card imagery vs Haze source/composition. No new speculative blur/material changes were made.
- Isolated branch `feature/glass-capture-diagnostics`, tested source `eb504f145b8a4e0cf8612cd9e814b8c6c6a1ab8f`, CI `42402960923` SUCCESS. Signed ZIP https://github.com/sergbokarev95-arch/NavTasks/releases/download/glasscapture-v0.4.60-diagnostic1/NavTasks_Glass_Capture_Diagnostics_0.4.60.zip, SHA256 `969f570d1feab063be5e3ac657e18e839c8767cad30d8a9fbb4893814383d9bf`, 10,865,575 bytes.
- Separate install `com.navtasks.app.glassdiagnostic`; **does not replace user data in installed 0.4.60**. Modes: ЭКРАН (live with Haze), БЕЗ HAZE (live without Haze source), ЭТАЛОН (original Glass Lab 0.1.5 reference card gradients/fonts/borders behind same dock); selector is outside recorder. Shader + optics source hash unchanged from 0.4.60.
- NEXT: user tests in dark & light at comparable scroll positions and sends screenshots / observations. Check if same live scene changes with Haze disabled; check if Lab reference matches the earlier Lab. Do NOT change production card design or optics until evidence identifies cause.
- Stable main code remains 0.4.59, current full in-place candidate 0.4.60; stable recovery Haze 0.4.61 available. Previous diagnostic CI attempt failed on workflow expression quoting, corrected before successful build.


## 2026-11-08 — Liquid Glass shared card surfaces: signed standalone Preview 1
- From 3-mode HONOR diagnostic, user confirmed Haze ON and OFF visually identical; Lab reference much richer. Chose option 1: adapt **ordinary card materials** (not business logic, geometry or all of the app to expensive shader layers) so glass refracts clearer gradients/edges.
- Isolated feature branch `feature/liquid-glass-surface-preview` source `9d210be7d0dc5d4e5f3bcaa24a49a17ad0f920f7` based on the accepted full-app 0.4.60 Exact Lab candidate. Only the common `Modifier.navGlass` and new `NavSolidCardTokens.kt` change static background materials for PANEL, TASK, NESTED_TASK, CONTROL (opaque subtle 2-stop horizontal gradients, more visible 1dp edge with vertical tone). Original NAVIGATION and SELECTED materials remain translucent; `NavLiquidGlassDock` and `NavLiquidDockOptics` source SHA unchanged. Day/Night both supported.
- Built signed preview ZIP https://github.com/sergbokarev95-arch/NavTasks/releases/download/glasssurfaces-preview1/NavTasks_LiquidGlass_Surfaces_Preview1.zip. CI `42404664493` SUCCESS, tests and package verification; ZIP size 10,856,702 bytes SHA256 `1c0df6d2c3aa28eeac80ef525cd94999f393af05a3173fcfc4d895a4c17739ac`. Separate app ID `com.navtasks.app.glasssurface` and launcher `NavTasks Glass Surface`; DOES NOT MODIFY installed 0.4.60 user data.
- NEXT user tests new surfaces in Today, Plan, Tasks, Works/AMOS, Services, Handover and Profiles in dark/day, especially task text/card borders crossing the fixed glass dock. Test overlay readability and jank. Preview has separate storage; import COPY of profile if detailed data is needed. Current main code still 0.4.59. No merge until HONOR accepted.


## 2026-11-08 — Liquid Glass opaque surfaces approved, finishing candidates signed
- HONOR screenshots of `glasssurfaces-preview1`: user likes the new dark/day static gradient card materials and rich refraction. Two final improvements requested: Night active grey capsule was too pale versus blue glyph; lower dock slightly for more balanced gesture bar spacing.
- `feature/liquid-glass-surface-preview2` built from accepted Preview1 `9d210be7d0dc5d4e5f3bcaa24a49a17ad0f920f7`. Two visuals only: Night settled selection `#616B77 @ .86` → `#3F4B58 @ .82`; dock bottom padding `8dp→4dp` (4dp lower). Day material/optics/56dp height/1.30x animation/other card gradients/logic untouched. ZIP https://github.com/sergbokarev95-arch/NavTasks/releases/download/glasssurfaces-preview2/NavTasks_LiquidGlass_Surfaces_Preview2.zip (~10.9MB), GitHub Actions `42438614953` SUCCESS, SHA256 `1dfbda02757074f76760ea9adebf0d1a6b96f01f80471d1f1aa6a28eece676d3`; installs separately as `com.navtasks.app.glasssurface`.
- Same preview code prepared as a **signed in-place Beta candidate** `0.4.62-beta` versionCode85, same `com.navtasks.app.glass` / Beta key as installed 0.4.60, only app ID/version/launcher label changed from isolated Preview2. Feature `feature/liquid-glass-surfaces-0.4.62-beta` source `7bea5101e051774d4fe82a74a2395d8d184ce27e`, CI `42438818985` SUCCESS; signed ZIP https://github.com/sergbokarev95-arch/NavTasks/releases/download/v0.4.62-liquid-glass-surfaces-candidate/NavTasks_LiquidGlass_0.4.62_Beta_Update.zip, SHA256 `c2c2246e2e7aa3afe1b7dc7ac455bb03c895d35a9739e013df3f670823aa1e1a` (~10.9MB).
- **Safe same-package signed Haze rollback v0.4.63-beta versionCode86** from exact stable Haze 0.4.59 sources; source `38988ec5bc931a11ab2a5fdb8c1d59b42f9a6532`, CI `42438923548` SUCCESS. ZIP https://github.com/sergbokarev95-arch/NavTasks/releases/download/v0.4.63-haze-rollback/NavTasks_Stable_Haze_Rollback_0.4.63.zip, SHA256 `1663e9c472ce26635a93d4171e5e9329ac6ee7db98ea2830e3339bc762036780` (~10.8MB).
- **DO NOT UNINSTALL** real NavTasks. Export FULL profile before any in-place candidate update. 0.4.62 code85 upgrades prior 0.4.60 code83 or rollback 0.4.61 code84 without losing data if signature matches; 0.4.63 code86 upgrades over 0.4.62 to restore stable Haze without uninstall. Following final releases must use versionCode ≥87.
- Physical HONOR acceptance for Preview2 tweaks and production candidate **still pending**. User specifically asked to port to main app, but `main` still only docs updated; no stable production source merge before device feedback. Next: compare day/night active blue contrast and dock-system bar spacing, rapid 0↔3, scrolling card borders, local data on upgrade. Only then merge tested sources into main.


## 2026-11-08 — Glass UX Preview 1 (phase 1 buttons + quick profile pinch + settings footer)
- User explicitly approved the new Liquid Glass dock/opaque gradients, requested completing interface button glass, removing the clipped Settings/system gesture footer and adding a centered blurred quick profile picker triggered by two-finger inward pinch.
- **Do not modify stable business logic or wholesale-convert all buttons.** New independent branch `feature/liquid-glass-controls-quick-profiles-preview` from signed 0.4.62 Beta candidate `7bea5101e051774d4fe82a74a2395d8d184ce27e`. Source `ce0b76df9aa5dfc2550bfd56ef044e1037f8037b`. Main app source still v0.4.59; no production changes.
- Preview APK uses distinct package `com.navtasks.app.glassux`, name `NavTasks Glass UX`, code85/version `0.4.62-controls-preview1`, signed same Beta key. ZIP `https://github.com/sergbokarev95-arch/NavTasks/releases/download/glassux-preview1/NavTasks_Glass_UX_Preview1.zip` size 10,876,570 bytes SHA256 `7f129658e517e3cfacb259249f6bb087101e91fe9284cdcf47b96d6e472a8c6b`. CI `https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42440578629` SUCCESS (full unit tests, signed app, verified ZIP).
- Changed **only** `NavTasksUi.kt`, `PlanAndTasksUi.kt`, isolated app identifier/label and new `NavQuickProfileUi.kt`, `NavLiquidGlassActions.kt`, tests. Existing approved `NavLiquidGlassDock.kt`, optics and `NavSolidCardTokens.kt` unchanged, checked by CI git hashes.
- Pinch gesture on regular root screens only: initial span >=100dp, contraction >=52dp, final span <=75%; does not consume 1-finger actions. Disabled in editors/calendar/management routes. Opens crisp centered modal on 17dp-blurred background; profile selection calls same `profiles.select()` and `switchProfile()` on Task/Handover/Services/AMOS stores as ProfilesScreen; no new persistence/business algorithm.
- Settings: remove full-height navigationBarsPadding cutoff, give scrolling list weight and terminal windowInsetsBottomHeight(WindowInsets.navigationBars) + 12dp spacer. Floating '+' and Calendar: new direct Backdrop glass on API33+, old Haze fallback API31/32.
- Initial test had erroneous *expected value* for one pinch unit test; fixed in second commit; complete app CI now green.
- **Next:** user tests ZIP on HONOR alongside installed NavTasks. Create two test profiles in independent app, pinch with two fingers, check Back/outside dismiss and profile data isolation, verify Settings last card scroll and floating action glass in both themes. Do NOT claim physical test or release to main yet. Continue remaining button families separately after approval.


## 2026-11-08 — Settings footer + quick two-finger profile pinch Preview 1
- User approved Surfaces Preview2 and asked to finish floating glass buttons, fix clipped bottom area in Settings, and introduce two-finger inward pinch to open a centered blurred quick-profile selector. The buttons phase is deferred until the smaller interaction experiment is tested.
- Isolated `feature/quick-profile-pinch-settings-footer-preview` from signed 0.4.62 source `7bea5101e051774d4fe82a74a2395d8d184ce27e`. No production source merged.
- Settings footer fix: removed `navigationBarsPadding` from outer Settings column, bounded its scroll area using `weight(1f)`, placed `windowInsetsBottomHeight(WindowInsets.navigationBars)` after final settings card so content is safe and no artificial footer is painted.
- `NavQuickProfileUi.kt`: root-screen-only, non-consuming two-finger pinch (start ≥96dp, contraction ≥48dp, shrink ≥25%), no interference with editor/Settings/Calendar inner screens. A centered Dialog with neutral glass surface appears over a 12dp Compose-blurred root, existing profile name/active-check shown. Close via Back/outside. Uses exactly ProfileStore.select + TaskStore/HandoverStore/ServiceStore/WorkOrderStore.switchProfile(), as the full profile screen does.
- **Separate test package** `com.navtasks.app.glassquick`, code87, signed CI https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42441508734 SUCCESS (`:app:testDebugUnitTest`, `:app:assembleDebug`, AAPT identity and ZIP integrity). Release https://github.com/sergbokarev95-arch/NavTasks/releases/tag/glassquick-preview1 and direct signed ZIP https://github.com/sergbokarev95-arch/NavTasks/releases/download/glassquick-preview1/NavTasks_Glass_QuickProfiles_Preview1.zip, 10,869,116 bytes, SHA256 `97bf5ed69bded324d7dfe27e8f2150bd14e7a3f06d49c93caba78ccaf650f8d6`.
- User must create disposable profiles in separate app for first gesture test; existing 0.4.62 real data are not shared. Next HONOR device validation: Settings last card and system bar, night/day, pinch vs normal scrolling/swipes, backdrop blur, switching to/from Personal, Back/outside. Only after approval prepare in-place Beta package with safe rollback. Buttons '+', calendar, settings glass still NOT touched.


## 2026-11-08 — HONOR approved quick-profile gesture & Settings footer; test '+' real glass Preview2
- User confirmed `NavTasks Glass Quick Preview1` works well on HONOR ("Да все работает хорошо"): two-finger pinch quick profile selector and Settings bottom inset fix are accepted. Stable app not yet merged.
- Continued **single-component** glass-button migration, starting ONLY with floating "+" on Tasks tab. All dock/gradient card/Quick Profile/settings components unchanged.
- Isolated branch `feature/liquid-glass-plus-button-preview`, source `50c8868af2e53455d3f993956af81848802cbc25`, CI https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42442451411 SUCCESS (full JUnit and APK, ZIP extraction integrity).
- `NavLiquidGlassAddButton` uses same `rememberLayerBackdrop` as LiquidGlass dock and EXACT bar `NavLiquidDockOptics` (blur2, rim12, refraction36), same highlight/shadow/sheens. Keeps existing 54dp position/size, blue 28dp '+' glyph, haptic, task-editor opening; Haze fallback API31/32, Calendar/Settings unchanged.
- Signed separate-app ZIP https://github.com/sergbokarev95-arch/NavTasks/releases/download/glassplus-preview2/NavTasks_Glass_Plus_Button_Preview2.zip, 10,872,529 bytes, SHA256 `bd721d3d1d668972b279513a4d6be7afb5239065ef99e221bd48566b675ee4e6`, package `com.navtasks.app.glassquick` code88. Updates Preview1 test app, NOT production data.
- **NEXT:** user tests Tasks tab '+' in dark/day with text/card borders behind it, click to editor, stable scroll/refraction, existing pinch/footer; only after approval continue Calendar button, Settings buttons one by one, then prepare same-package signed in-place migration with no-uninstall higher-code recovery. Main still stable source v0.4.59 + docs.


## 2026-11-08 — 0.4.64 unified circular Liquid Glass candidate + signed 0.4.65 recovery
- User approved Glass Plus Preview2 in Day with screenshot, asked for **ONE download** due to poor internet. Specifically: keep shader/blur/refraction/lens/haptics/animations absolutely unchanged, only soften overly white DAY borders, convert the remaining relevant floating/round buttons in the same in-place build.
- Preserved approved source branch `backup/glassplus-preview2-approved` at `50c8868af2e53455d3f993956af81848802cbc25`. New feature `feature/liquid-glass-actions-0.4.64-beta`, tested commit `8040ff34abad71b2700b79af41c46f4b90a45726`. No stable application code merged to main.
- Single shader recipe shared across floating '+' and Calendar 54dp and circular Back/Settings/Search 44dp. Main root, Plan, Profiles, Task Editor, Settings, Services/Handover/AMOS (including own '+'), standalone Works. Every refractive control is a sibling outside its own LayerBackdrop source, not self-recorded. Fallback old Haze API31/32; unchanged business click handlers and Quick Profile pinch.
- Only **Day** outer .75dp brush change: softened blue-grey border (#9AAEC0 etc.) on main dock and circular actions. Night border and both themes' shader parameters, exact NavLiquidDockOptics (bar blur2/rim12/amount36, selection blur1.5/rim10/amount18), 1.30x moving lens/56dp geometry, highlights, shadow and static card gradients preserved. Data stores/profile/TaskStore exact original blob hashes guarded by CI.
- Full signed in-place Beta `com.navtasks.app.glass`, versionName0.4.64-beta/versionCode89, CI https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42444219901 SUCCESS incl full JUnit, APK signing verification and ZIP SHA integrity. ZIP https://github.com/sergbokarev95-arch/NavTasks/releases/download/v0.4.64-liquid-glass-actions-candidate/NavTasks_LiquidGlass_0.4.64_Beta_Update.zip 10,896,190 bytes, SHA256 `f046815b1f0e41d3cf93ce56c0a148ca68b0520d27d9bc3dcdeb1cdd5c3c6a8e`.
- SAFE original Haze code from 0.4.59, same package and signing at versionCode90/name0.4.65-beta, branch `feature/navtasks-0.4.65-haze-recovery`, tested `b81f572dda245a32020887861ebdf3389c3a6b51`, CI https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42444287505 SUCCESS. ZIP https://github.com/sergbokarev95-arch/NavTasks/releases/download/v0.4.65-haze-rollback/NavTasks_Stable_Haze_Rollback_0.4.65.zip 10,774,964 bytes, SHA256 `db0748c7d7dbffc9637cf9f49e57924a288ba73281db5389725bd624cebb762e`. Only install if candidate rejected.
- **Before installing:** export FULL profiles; install 0.4.64 over current real NavTasks without uninstalling. Check saved data, all round buttons in Day/Night, scroll underneath, no flicker/jank, Day border readability, original lens animation, profile pinch and Settings footer. If rejected, install signed 0.4.65 over it as a normal update; never uninstall. If rollback code90 installed, future in-place versionCode must be >=91.
- CI validates compilation, not visual HONOR experience. Await screenshots/feedback before merging to main or declaring stable. Inline form/text/icon actions left with stable static material to preserve performance/clarity, not all text widgets made optical.


## 2026-11-08 — 0.4.66 single-green completion check (HONOR visual test pending)
- User explicitly approved entire 0.4.64 all-actions Liquid Glass ("Да, вообще супер") and asked to shorten task completion: currently mint flashes twice and card is slow; desired press → clearly green first → slide DOWN promptly, not all simultaneously.
- Based on signed accepted 0.4.64 source `8040ff34abad71b2700b79af41c46f4b90a45726`. Feature `feature/task-completion-single-green-0.4.66-beta` tested commit `38919f6348e17237bf66a0102815ba3caf992f75`. Diff changes ONLY `NavTasksUi.kt` RunTileV2 visual completion, new pure `NavTaskCompletionMotion.kt` with JUnit tests, version/workflow/docs. No TaskStore/profile/AMOS/services/calendar business logic; no shader/capsule/animation dock changes.
- Previously `flash.animateTo(1,140)` then `(.22,170)` then `(.85,140)` then 150ms hold + 330ms exit with up-kick and scale swell (930ms total). New `flash.snapTo(1f)` solid green + check for 190ms, then single 250ms straight downward 48dp fade, no up motion/scale stretch (~440ms).
- Same function is used by simple task completion and last child of checklist; non-last child remains immediate, other-day confirmation still mandatory. TaskStore operation only after motion concludes, identical as before.
- Signed same-ID `com.navtasks.app.glass` 0.4.66-beta versionCode91, full CI https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42445806856 SUCCESS (tests, signed APK, identity+integrity checks). Compact ZIP https://github.com/sergbokarev95-arch/NavTasks/releases/download/v0.4.66-completion-motion-candidate/NavTasks_Completion_Check_0.4.66_Beta_Update.zip (10,896,210 bytes), SHA256 `d17dd0945f733a68d1bc2379cd0bedc8398fb41ca14cd275edfb8f24672c6a7d`. First workflow attempt built/tested but publishing got GitHub 403; retry succeeded.
- Safety recovery original stable Haze `0.4.67-beta` versionCode92 same Beta key/ID. CI https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42445698166 SUCCESS, ZIP https://github.com/sergbokarev95-arch/NavTasks/releases/download/v0.4.67-haze-rollback/NavTasks_Stable_Haze_Rollback_0.4.67.zip (10,774,966 bytes), SHA256 `3adcb68baf94d2a546fb2171f9c5aa592007316a8142207a8ba1a7aab84a5d2d`.
- User should export FULL profiles, **never uninstall** main app, install 0.4.66 over installed 0.4.64 as normal update. HONOR check simple task and final checklist item, green perceptible before movement, no double blink, animation ~440ms, all data and Liquid Glass preserved. DO NOT merge feature to main until acceptance. Recovery 0.4.67 is emergency only and returns to old Haze architecture, not 0.4.64 glass; future versionCode>=93 if installed.


## 2026-11-08 — Services current AMOS Requests filter, signed Beta 0.4.68 (HONOR pending)
- User clarified with real Services screenshot: `Работы` MUST continue showing all active service records (planned with/without registered AMOS request, unplanned with request); new `Заявки` tab MUST show ONLY current requests, from both planned and unplanned, excluding planned without request. `Закрытые` remains existing archive. This is purely an existing-list FILTER, not a new task type, module or AMOS integration.
- Exact predicate `currentServiceRequests`: `closedAt == null && completedAt == null && requestNumber.isNotBlank()` on existing `ServiceEntry`. Historical `serviceHistory` request references excluded. On completion, existing `completeServiceCycle` archives number and clears it, so request disappears automatically. No new data fields/migrations, reminders, or editing changes.
- New three compact evenly weighted chips `Работы · N`, `Заявки · N`, `Закрытые · N` in Services; existing full cards/actions remain identical; search works in selected view; upper total/soon/overdue still select Works. Empty requests message localized.
- Based on v0.4.66 accepted Glass source `38919f6348e17237bf66a0102815ba3caf992f75`; frozen branch `backup/navtasks-0.4.66-before-service-requests`; new feature `feature/service-requests-filter-0.4.68-beta` tested `16e26c0a659c9848618cf5adc849c43bffa701a9`, no merging to stable main.
- Signed same-ID Beta `com.navtasks.app.glass` v0.4.68-beta versionCode93 with unchanged Beta keystore, CI https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42447897901 SUCCESS (complete unit tests, aapt, apksigner, ZIP). ZIP https://github.com/sergbokarev95-arch/NavTasks/releases/download/v0.4.68-service-requests-candidate/NavTasks_Services_Requests_0.4.68_Beta_Update.zip (10,897,386 bytes), SHA256 `f5ae1dabbc36aa795c70ffa0b0afe93ad3ad62e8bb7987aa54edb69292d24290`.
- Same-Glass recovery (NOT old Haze!) restores exact previous 0.4.66 source with versionCode94 / 0.4.69-beta, feature `feature/navtasks-0.4.69-glass-recovery` commit `0770fdd3f5ca386cfae738f75983350fcabb25eb`, CI https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42447985380 SUCCESS. ZIP https://github.com/sergbokarev95-arch/NavTasks/releases/download/v0.4.69-glass-recovery/NavTasks_Glass_Restore_0.4.69_Beta.zip (10,896,209 bytes), SHA256 `34e9d23234d679e13b42b074dcf60007bd5e22e5a5b09d74ea893d85d2a39f41`.
- Next: user exports FULL profiles and installs code93 over existing NavTasks without uninstalling, tests count and tabs with planned WITH request, planned WITHOUT, unplanned, searches, completion clearing request number, reopening, both themes, existing true glass and quick pinch. HONOR validation required; no stable code merged until approval. If rejected install signed code94 recovery above code93 without uninstall. Future live versionCode must be >=95 if recovery is installed.


## 2026-11-08 — Rich Text & keyboard 0.4.70 (signed, HONOR acceptance pending)
- User reported no auto-capitalization after punctuation or spellcheck in search/comments; shared screenshot of WhatsApp-like toolbar B/I/U/S/color/marker. Requested proper rich text plus Android IME fix while preserving the approved Liquid Glass.
- Added sentence/autoCorrect keyboard hints to narrative fields; no autocorrection on exact AMOS identifiers or numeric fields. Keyboard's actual corrections remain under device IME control.
- WYSIWYG notes in Services, AMOS, Handover, checklist reference notes (editing and read-only in Today). Optional HTML stored alongside existing plain text: `commentHtml`/`notesHtml`. Old JSON records, full/selective profile exports and service history maintain both without rewriting TaskStore/recurrence.
- Source base v0.4.68 approved code93 `16e26c0a659c9848618cf5adc849c43bffa701a9`; backup `backup/navtasks-0.4.68-before-rich-text`; feature `feature/rich-notes-ime-0.4.70-beta` build-tested `a20ac5476084c72c51e37d10a9cb2b4f782dc07d`. CI https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42455684933 SUCCESS full JUnit+assemble/identity/signature/ZIP check.
- Same-ID signed v0.4.70-beta code95 ZIP https://github.com/sergbokarev95-arch/NavTasks/releases/download/v0.4.70-rich-notes-ime-candidate/NavTasks_RichNotes_IME_0.4.70_Beta_Update.zip (12,617,451 bytes) SHA256 `0f9de50ce81ab479e171af4234ee156e503b09841f7aa830daa503e870b6428a`. GitHub tag initially created at prior successful source `889c32f965327f96c1f61f55d30d882bf9af568d`; **final asset** explicitly uploaded by latest CI `42455684933` from source `a20ac...` using --clobber.
- Recovery v0.4.71-beta code96 restores previous exact Liquid Glass v0.4.68, not old Haze; CI https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42455220247 SUCCESS; ZIP https://github.com/sergbokarev95-arch/NavTasks/releases/download/v0.4.71-glass-recovery/NavTasks_RichNotes_Restore_0.4.71_Beta_Update.zip (10,897,385 bytes) SHA256 `f57aa1ea1e524d3a8f4916cffe3db142aa9049db3d04a087cf852e3042b0cb82`.
- **NEXT:** User exports FULL profile, installs 0.4.70 code95 over current app without uninstall; verifies RU/EN IME capital letters/correct suggestions, WYSIWYG selection and styles/save/reopen/history/export in 4 modules, day/night contrast and keyboard overlay; test original Glass, check animation, quick profiles, Services requests. HONOR acceptance pending; do not merge app feature into main until confirmed. If rejected, install signed code96 recovery over code95; avoid editing styled notes in recovery because legacy editor could drop HTML. Future in-place versionCode >=97 if recovery installed.

## 2026-11-08 — Handover Edit immediately accessible: 0.4.72 candidate signed

- HONOR screenshot showed long ECDIS Handover detail dialog without a visible edit control; original text-only Edit action was at the very bottom of scrollable content and was not discoverable in practice.
- Baseline signed 0.4.70 rich-note source `a20ac5476084c72c51e37d10a9cb2b4f782dc07d` backed up at `backup/navtasks-0.4.70-before-handover-editor`. Branch `feature/handover-edit-access-0.4.72-beta`, CI source `9565c236459b2fd07b608d8f31ded9e051f959e4`.
- **Only HandoverUi.kt UI changed**: in selected-entry dialog title, display a 44dp accessible blue pencil action beside a max-2-line title. Calls existing `openEntryEditor(sectionId, item)`, closes read-only dialog, retains existing Rich Note WYSIWYG and HandoverStore save path. Removed hidden duplicate Edit action at bottom. App/build.gradle.kts bump 0.4.72/code97; separate CI workflow.
- Full tests, Android build, beta signature, appId/version, glass/data hash guards and ZIP integrity: Actions https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42458988093 **SUCCESS**. Signed APK in ZIP https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.72-handover-edit-candidate (12,618,687 bytes). Same `com.navtasks.app.glass` package and original signing; user should export FULL profiles, install over existing app, **never uninstall**.
- In-place safety fallback `0.4.73-beta` code98 built from untouched v0.4.70 rich-note source, only version bumped, branch `feature/navtasks-0.4.73-richnotes-recovery` source `8d8033b0098c1934c102627660aecd5c1e0f1007`; Actions https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42459070578 **SUCCESS**; https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.73-handover-edit-recovery (ZIP 12,617,462 bytes). Fallback code98 upgrades over code97 if needed, without uninstall, preserving same rich HTML schema / Glass.
- **NEXT:** HONOR physical verification of edit pencil visibility, tap from long ECDIS note, saving/reopening title and rich text formatting, completion marks, day/night, keyboard/dialog controls, full profile exchange and unchanged Liquid Glass. Do not merge into `main` until user approves; stable main app source still v0.4.59 (documentation updates only). Future same-ID code >=99 if fallback98 installed.

## 2026-11-08 — Apple semantic color preview v0.4.74 signed; HONOR acceptance pending
- User inspected 10 visual concepts and chose **not** to adopt any whole scheme. Requested consistent Apple-like blue across interactive labels, check indicators, arrows and buttons; Apple orange; Notes-app-style neutral light background, **not** a redesign of dock or cards. Day screenshot background was sampled as #F4F5F7 (RGB 244,245,247).
- Branch `backup/navtasks-0.4.72-before-apple-palette` protects signed v0.4.72 source `9565c236459b2fd07b608d8f31ded9e051f959e4`. Isolated feature `feature/apple-semantic-colors-0.4.74-beta`, immutable verified release source `13b921992705efae9967f6489aa9bcfa618ab50f`. Main app source still 0.4.59, no functional merge.
- Colors: Day background pure #F4F5F7, no blue/mint glow; Night exact black. Apple system blue Day #0088FF/Night #0091FF for controls; contrast-safe blue text Day #0071E3/Night #5CB8FF; Apple system orange Day #FF8D28/Night #FF9230, text-safe warning Day #BE5300. Neutral text/muted. Calendar PARTIAL orange follows same semantic token. All existing card surfaces and roles `NavSolidCardTokens.kt`, all optical dock shader/lens/navigation sources and geometry, all data/business logic preserved. Changed only palette, page static background, text accent uses and calendar orange, plus tests/build workflow.
- First CI attempt caught failure in two contrast assertions; fixed by distinguishing icon blue (>=3:1) from text blue (>=4.5:1) and strengthening light status orange/muted, **without moving away from Apple system blue**. Some intermediate parallel runs had GitHub 403 publication errors. Recommend **only** separately tagged latest verified asset, never intermediate candidate ZIP.
- Latest accepted-by-CI source `13b921992705efae9967f6489aa9bcfa618ab50f`; full JUnit/assemble, signed same-ID versionCode99 / 0.4.74-beta, APK identity and ZIP integrity: https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42484347829 **SUCCESS**. Immutable release tag: https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.74-apple-palette-verified; ZIP 12,617,196 bytes, SHA256 `23727cfc2f7517e695c1d6486ac891439e3b8d3bd10a3ca07907244cd701fda8`. Same `com.navtasks.app.glass` signing.
- Signed exact-source rollback to v0.4.72 UI (including Handover Edit and Rich Notes) with higher code100/version 0.4.75: branch `feature/navtasks-0.4.75-prepalette-recovery`, source `f4c14f4cf2069d9be811a7ed66747af31ca74fbd`. CI https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42483665590 **SUCCESS**, release https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.75-apple-palette-recovery; ZIP 12,618,695 bytes SHA256 `067c86109c6867af5ea78b6d022e7b43faf20f85494f91c0edcc02d2912908e1`. Only install if rejected; NEVER uninstall, export FULL profiles first. Future same-ID versionCode >=101 after code100 recovery.
- Font: Apple's SF Pro license explicitly prohibits embedding in software products / non-Apple Android interfaces. User explicitly requested SF Pro; not silently substituted. Suggested OFL-licensed visually similar Inter (offline bundled) but **user approval pending**. **No font change in 0.4.74.** Cards and dock remain intentionally untouched; user will choose their style separately.
- NEXT: HONOR acceptance of v0.4.74 colors: daylight neutral #F4F5F7, all main module blue text/buttons and warning orange, day/night, status text contrast, retained card gradients, working optical dock/gesture, profiles/AMOS/tasks/calendar. Then decide Inter vs another Android-compatible font and separately redesign cards, not before.
