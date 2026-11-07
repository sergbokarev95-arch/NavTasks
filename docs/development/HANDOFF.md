# NavTasks — New Chat Handoff

1. Connect GitHub account sergbokarev95-arch; open private repo sergbokarev95-arch/NavTasks.
2. Read all five docs/development/*.md files first. Confirm main HEAD, app/build.gradle.kts versionName/versionCode, recent commits, GitHub Releases, Actions and relevant feature branches. If GitHub is newer, update these documents before acting.
3. Source: app/src/main/java/com/navtasks/app/; resources: app/src/main/res/; tests: app/src/test/java/com/navtasks/app/.
4. Current stage: **v0.4.58 CI-verified signed hotfix published, HONOR visual confirmation pending.** v0.4.57 was visually rejected (scrolling text shimmer and 52dp glass), v0.4.56 is the pre-optics rollback reference. Handover Add and other UI checks remain open unless user confirms. User still needs to visually verify notification icon and responsive widgets on HONOR. Keep 0.4.46 AMOS due-only Today logic.
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
- New AGSL files NavGlassOptics.kt / NavGlassOpticsTest.kt remain for reference but the running NavGlass must no longer call them. VersionName 0.4.59, versionCode 81, same Beta signing. CI and signed release pending at source stage.
- Next: verify CI and release, record immutable source/publisher/run/assets and hashes, then user tests scrolling Handover/Today content under dock + tabs in day/night. If still shimmers after reverting renderer, isolate `hazeSource` / Haze composition. No shader tweaks without approved isolated test plan. We are **not** claiming authentic glass was achieved.
