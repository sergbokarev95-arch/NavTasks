# NavTasks — Current State

Reviewed 2026-11-08 against GitHub main and successful signed 0.4.59 release; subsequent documentation-only commits may advance main HEAD.

- Repository: sergbokarev95-arch/NavTasks (private); default and current integration branch: main.
- Current signed CI-verified release: **v0.4.59 (versionCode 81), runtime dock restored to v0.4.56; HONOR acceptance pending.** v0.4.57 and v0.4.58 were visually rejected for shimmering content; v0.4.56 is the dock renderer baseline. Debug variant adds -beta and .glass.
- Historical milestones: 0.4.46 due-only AMOS/Services; 0.4.48 notification/widget update; 0.4.50 planned/unplanned Services; 0.4.51 informational AMOS request date; 0.4.52 edge-to-edge standalone screens; 0.4.53–0.4.54 calendar/Today weekend refinement; 0.4.55–0.4.56 right-hand Add controls. 0.4.57 tests an optically refractive dock.
- Last pre-documentation main commit: 211c0ac — publication workflow for 0.4.46.
- Current release workflow .github/workflows/publish-0.4.59-apk.yml pinned to b22057ad92b0f531df12217c4ecee97150395951. Earlier signed release workflows are retained for provenance.
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

## 0.4.57 optical dock prototype (2026-11-07; source stage — NOT YET VERIFIED)
- User explicitly requested an actual glass result rather than merely a semi-transparent container, authorizing an incremental optics prototype after the Telegram/Haze audit. 0.4.56 remains the last published signed baseline until this source passes CI and release.
- Source changes limited to `NavGlass.kt`, new `NavGlassOptics.kt` (independently authored AGSL), `NavGlassOpticsTest.kt` and version in `app/build.gradle.kts`; no Kotlin/Compose/Haze dependency upgrades.
- The backdrop is still sourced from **one shared HazeState**; NavGlass splits the dock into an independently drawn Haze background, an optical effect layer, a thin glass face/edge layer, and the original animated selection/foreground. The shader is never applied to icons, click targets, or the active-pill Canvas.
- API 33+ attempts local signed-distance capsule edge refraction using RenderEffect/RuntimeShader on the **blurred Haze layer only**; API 31-32, invalid layers or known shader-construction failures fall back to Haze without refraction. No content outside the app is sampled.
- NavGlass's overly opaque stacked navigation/control overlays are reduced and blur softened to let actual content shine through, maintaining the 56dp dock, 4 tabs, 19dp right-side FABs and original critically-damped selection spring. Active capsule uses more translucent materials.
- No profile/task/service/AMOS/scheduling/notification/widget/storage changes. Unit coverage checks API/size feature gating only, not actual shader visual quality.
- GitHub main CI 42376895012 SUCCESS; signed publisher CI 42377117640 SUCCESS. Immutable source 3d2ffdeead8c607d2e6f7b43d126d18ae6593581; publishing workflow commit f41dc24810e0c8ce4e90b36cf72927b6cf4f8fc9.
- Release https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.57-glass-beta; stable-signed APK SHA-256 b8de98ae598396405ac4145e007a4ca1de629146ea14e394dd41f32200db354c; compact ZIP SHA-256 63430ee5388d5e6cf7548802f332f9a02c32dbcb13519f7f9ffd1925356d911c.
- **Not physically tested:** HONOR day/night/scroll/gesture/GPU frame-time acceptance remains pending; do not call the experimental optics visually verified.

## 0.4.58 — HONOR-reported dock size and shimmer regression (2026-11-08, source stage)
- User screenshot of v0.4.57 Handover: text behind the glass is too visible, appears to glow/shimmer while scrolling; dock background appears smaller.
- Root cause identified in NavGlass.kt: 0.4.57 moved the former padding(horizontal=1dp, vertical=2dp) **before** the backdrop/face layers. As a result, the 56dp outer container's glass was actually rendered in the 52dp inner area. 0.4.57 additionally reduced Haze blur 34dp→15dp and set a large rim 13dp/bend 5.5dp, distorting lettering over scrolling cards.
- Scoped fix: keep full-size 56dp Haze/optics/face material with 30dp round clipping; apply 1dp/2dp padding **only to the active-lens Canvas and four-tab Row**, restoring earlier selection geometry/hit targets. Haze nav blur increased to 30dp, navigation background tints slightly strengthened for legibility; non-nav floating control material unchanged.
- New AGSL params: refraction limited to a 5dp rim with maximum 1.2dp bend. Compatibility: current Haze fallback on API <33 remains. Tests enforce conservative optics values; source checks verify geometry.
- No changes to task/AMOS/service/Handover business logic, profiles, data, notifications or widgets. Source CI https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42381839037 SUCCESS; publisher CI https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42382154553 SUCCESS.
- Immutable source 4118106265361b3e28b2f4636f5c6c17def6801c, publisher workflow commit 6abcfc23221872ab021f38d60725d88b159f6b2f. Release https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.58-glass-beta; APK SHA-256 340e1851179f1ef2a1350638df3454cfdde1d279ed6bccfc15fac82a187a1914, ZIP SHA-256 382e4ce9b3cb4a3b7aac244fbf009df6a7b19ed5620c561d8997ab25337eaa42.
- **On-device HONOR test still pending:** do not claim flicker eliminated before user confirms it.

## 0.4.59 — revert unstable runtime glass optics (2026-11-08; source stage)
- HONOR user report on v0.4.58: the dock still looks like a transparent container, with text and cards under it visibly shimmering/jittering. v0.4.57 and v0.4.58 **failed physical visual acceptance**. Further alpha/blur/shader-strength tweaks are not justified without isolating the renderer.
- Safe corrective action: restore `app/src/main/java/com/navtasks/app/NavGlass.kt` **byte-for-byte from the known v0.4.56 source commit `612ba1ba215e8c1871a654acda2304f5c44c678c`**, preserving the same Haze blur, selected-lens Canvas, dock dimensions, tab click behavior and theme rendering. This specifically disconnects AGSL/RenderEffect from runtime and removes the 0.4.57/0.4.58 experimental material changes. All subsequent task, Handover, AMOS, Services and data changes remain intact.
- `NavGlassOptics.kt` and its test are retained as **inactive research files** for traceability; there is NO call to `createNavDockRefraction` in the restored `NavGlass.kt`. No model/data/profile changes or dependency upgrade.
- VersionName 0.4.59, versionCode 81. Main CI 42383033196 SUCCESS; signed publisher CI 42383289163 SUCCESS. Immutable source b22057ad92b0f531df12217c4ecee97150395951, publisher workflow 28f6245873e0613cda7f17e9d3da7e9ab1ea7612. Release https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.59-glass-beta; APK SHA-256 24dbfa7c439e95775e86abd8e064efdf119417903f2d2369390405b83090ab8e, ZIP SHA-256 454dbfd31aefe0b383d2d4ed4c1656d089fc5c079dabc2e705d8e90a1eaf1f33. HONOR visual confirmation is still pending; this release is not new refractive glass.

## Standalone Liquid Glass Lab 0.1.0 (2026-11-08; experimental branch ONLY)
- Production NavTasks remains **v0.4.59**, with original 0.4.56 Haze dock. No production code, dependency, gradle setting, package ID or data changed.
- New separate branch `experiments/liquid-glass-backdrop`; source commit `512cb355d0e1a37cba22eb79e5ada7cc32294eab`; new module `:glasslab` exists only on this experimental branch, package `com.navtasks.glasslab` (separately installable).
- Refractive renderer uses Apache-2.0 `io.github.kyant0:backdrop:1.0.6` with `shapes:1.2.0`, capturing **unblurred scrollable content** into a GPU-backed LayerBackdrop and then applying vibrancy, blur and lens in a separate dock. Unlike v0.4.57/0.4.58, this does NOT attempt to refract an existing Haze effect output.
- CI https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42385989762 SUCCESS; experimental prerelease https://github.com/sergbokarev95-arch/NavTasks/releases/tag/glasslab-v0.1.0; APK SHA-256 caeb1cbc7189e1aca707416f3f3660696dce1438fd546811099e8e7fcb24063c.
- **Not tested on HONOR yet.** Do not integrate into production or claim final glass achieved until Day/Night and moving-content acceptance; see experimental branch `docs/development/GLASS_LAB.md`.


## 0.1.2 separate glass laboratory handoff (2026-11-08)
- User HONOR screenshots confirmed v0.1.1 clearly refracts card borders in Day and Night; requested exact NavTasks icons/color and animated expanding/sliding lens that settles to neutral grey.
- Updated only standalone `:glasslab` sources in `experiments/liquid-glass-backdrop`; original production `app` module remains 0.4.59 / versionCode 81.
- Lab 0.1.2 source `049836c2bd05bbd3378162e174a46f2ae249c240`; CI `42389729911` SUCCESS (tests + signed APK); prerelease https://github.com/sergbokarev95-arch/NavTasks/releases/tag/glasslab-v0.1.2. APK SHA-256 `fdeb481c91252db2c1e39f7ed02acba4541b2488f3a94bcc6d640b4f64a785c9`.
- Physical acceptance of new animation **not yet confirmed**. Next: test 4 tabs and Day/Night on HONOR, then plan minimal non-destructive integration into production.


## 0.1.3 independent Liquid Glass Lab tested by CI (2026-11-08)
- Requested change: motion-only `GlassLabDockTabs.maxStretchDp` 10f → 28f. No production code, lens height, direct Backdrop capture, glyph colors, data or navigation changed.
- Source `9f3ac3c8574d539141dd828d76c9c08da28fbe6f`, CI https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42390942917 SUCCESS; signed pre-release https://github.com/sergbokarev95-arch/NavTasks/releases/tag/glasslab-v0.1.3; APK SHA256 `8f0b7e18cce69c007ff8a3f4e73a40da1affbdaffca3507420a72626ab6f83c9`.
- HONOR visual evaluation of the larger lens pending; `main` application version remains NavTasks 0.4.59/versionCode 81.


## Standalone Glass Lab 0.1.4 (2026-11-08)
- After HONOR rejected Lab 0.1.3's horizontal-only lens stretch, trial 0.1.4 uses a uniform 1.30x scale for width and height: 52dp → 67.6dp active lens centered over 56dp bar, protruding ~5.8dp top/bottom in motion. Existing dynamic refraction, 4 glyphs, animations and matte grey settled state retained.
- Tested commit `0ca6d8874a6c6febe86cb874b81ba0b846e087fe`, GitHub Actions `42392699540` SUCCESS, signed https://github.com/sergbokarev95-arch/NavTasks/releases/tag/glasslab-v0.1.4; actual HONOR rendering **not yet verified**.
- Separate `experiments/liquid-glass-backdrop` module/package only. Main NavTasks 0.4.59 source / versionCode 81 unchanged.


## Lab 0.1.5 standalone compositor correction (2026-11-08)
- HONOR v0.1.4: lens grows but top/bottom clipped. Lab 0.1.5 separates backing and lens into siblings and retains direct Backdrop, 1.30× uniform scale, 56dp outer dock and production glyphs.
- Source `ea412201936a8a178c657b77bda7a3347b878f06`, CI https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42393384091 SUCCESS; signed https://github.com/sergbokarev95-arch/NavTasks/releases/tag/glasslab-v0.1.5. Physical user validation pending. No main NavTasks code changes.


## 2026-11-08 — NavTasks Glass Dock Preview R1 (separate package)
- Based on stable main NavTasks 0.4.59/versionCode 81. New experimental branch `feature/liquid-glass-dock-preview`, source `51f04825196093b70288bc078fe0874abf1d2723`, CI 42397184202 SUCCESS including complete app unit tests and APK signing.
- Signed standalone **full app** test https://github.com/sergbokarev95-arch/NavTasks/releases/tag/dockpreview-v0.4.59-r1; app ID `com.navtasks.app.glasspreview`, APK hash `84f0609f7ce37d4818dcc3e4ec66fb1d8360af21d1c93b8dfd32a9ffaba87faa`. Preview has separate data from stable app.
- Only primary bottom dock uses direct original-content Backdrop 1.0.6 with 0.1.5 optics and 1.30x unclipped lens. Existing Haze dock retained on Android 12/12L and as recovery. Floating buttons remain unchanged.
- Stable production app/release 0.4.59 untouched. HONOR visual/performance acceptance of preview pending before product integration and control rollout.


## 2026-11-08 — full-app Glass Dock Preview R2 available for test
- Preview R1 confirmed real glass in full NavTasks, but user feedback: background text too sharp under lower dock. Experimentally blur only at outer bar 5dp (was 2dp), moving/selected lens 4dp (was 1.5dp), preserve all lens() parameters and geometry.
- Preview branch `feature/liquid-glass-dock-preview`, source `0a5e91f2d5b10b811f4a763d922a683115b0a1cb`; GitHub Actions 42398375900 SUCCESS; signed standalone https://github.com/sergbokarev95-arch/NavTasks/releases/tag/dockpreview-v0.4.59-r2. APK SHA256 `f8380c7b492220a79256fcc71bfe95e9e5fa217ecd5ad13eb9002c945202f236`.
- Test package `com.navtasks.app.glasspreview`, versionCode82. Old R1 preserved. User visual acceptance pending. Main production code/version v0.4.59 remains untouched.

## 2026-11-12 — Signed no-publish pre-1.0.0 UI stage (HONOR pending)
- HONOR device installed `0.4.166-rc20`/code188; user saw successful update receipt. Original application source SHA `3bfa259379cfb393a6e4e35703d7e15e1f088989` protected at `backup/navtasks-0.4.166-rc20-before-pre1-polish`.
- Working branch `fix/rc20-pre1-handover-tasks-ui` implements only presentation improvements: blue outlined check-update button, disabled tasks visibly struck and stable-sorted last, nested Handover vessel notes collapsed by default with click-to-expand and click-to-edit, search-note auto-expansion and scroll-to-top targeting, independent editor text/highlight palettes including black/white. Personal notes display expanded as before; no model migration or changes to saved HTML, task recurrence, widgets, notifications, updater transport, dock or signer.
- GitHub Actions https://github.com/sergbokarev95-arch/NavTasks/actions/runs/43211482395 `SUCCESS`: JUnit full suite, signed nondebuggable `com.navtasks.app.glass` code188 verified with historical signing cert; build only, NO APK publication, NO user phone install, stable feed untouched.
- UNRESOLVED: RichTextEditor selection handles drag unreliably on HONOR; not fixed by palette enhancement. Editor library upgrade would require Kotlin/Compose compatibility analysis and device regression. Handover new collapse/search/palette UX not yet approved on HONOR. Stable 1.0.0 not authorized.

## 2026-11-12 — Post-RC20 editor compatibility and widget alignment work
- Real HONOR RC20/code188 stays accepted baseline with one-time receipt seen; no new installation. New feature branch `fix/rc20-pre1-handover-tasks-ui` remains private, not merged to main or released.
- Native widget touch behavior untouched. Only `NavWidgets.kt` foreground accent switches legacy teal to semantic Day/Night blue and compact/wide RemoteViews XML move task rows to top rather than vertically centering them within flexible widget height. No Kotlin TaskStore, notification or dock changes. New pure JVM accent gate added; latest UI signed CI #43215461681 IN PROGRESS, do not call it passed until verified.
- Selection-handle drag fix upstream requires editor 1.0.0; safe isolated Kotlin2.4.0/editor1.0.0 experiment from same UI source has signed full build CI #43214672555 SUCCESS and extra synthetic old-HTML/selection-only tests CI #43214884950 SUCCESS. The actual HONOR touch-handling issue is still UNVERIFIED, and editor library upgrade is NOT in the working feature branch. App source data formats unchanged. Stable feed OFF.

## 2026-11-12 — User-approved feature scope extension (design only)
- Statistical widgets: user approved 2×2 and 4×2 no-checkbox layouts, Today progress/remaining and Tomorrow count when Today is 100% done. Existing branch code still uses old tappable RemoteViews rows; approved redesign NOT implemented yet.
- Reference (corrected): initial empty SIRE 2.0, VIQ7 and Equipment categories; shared manuals, user-created folders/excerpts and equipment records, with equipment inventory per vessel profile. Procedures have manual excerpts/citations and purely descriptive periodicity. NO links to Tasks/AMOS/Services/Works/Today, no scheduling, status tracking, reminders or notifications. Users manually consult Reference when they need to check how/why maintenance is done. Selective standalone export/import of equipment with excerpts and optional files; no task data in packages.
- UX and storage requirements tracked in docs/development/REFERENCE_LIBRARY_WIDGETS_PROPOSAL.md. At this commit no runtime Reference storage/UI or widget-logic changes for new approval; installed HONOR RC20/code188 remains untouched.


## 2026-11-12 — User correction: Reference must be completely independent
- Previous assistant-proposed integration of Reference equipment with Tasks, AMOS, Services, Works, Today and job schedules was explicitly REJECTED. Do not implement relationships, buttons, deep links, task IDs, completion states, due dates, overdue status, reminders or import of job data in Reference. The user separately enters actual scheduled work in existing AMOS/Services. When a procedure is needed they manually open Reference, find equipment and consult the cited manual/excerpt. Any intervals in Reference are descriptive, not executable schedules. Shared manuals and selective export/import of reference-only equipment remain approved. See REFERENCE_LIBRARY_WIDGETS_PROPOSAL.md.

## 2026-11-12 — Statistics widgets and reference model staged (device acceptance pending)
- Replaced launcher widget UI in feature branch: two fixed target grid footprints 2×2 and 4×2; no in-widget task checkbox, completion event or pending intent. Tap opens Today only; show current-day count/progress, remaining, then Tomorrow total when current day complete or empty; wide widget shows at most two text-only remaining items. App date rollover hooks were already present. Neutral dark/light background and NavTasks blue; physical HONOR/Samsung acceptance not yet performed.
- Added new completely independent Reference equipment backend (models, hierarchy, per-profile atomic catalog, one-equipment .navref ZIP export/verified import), with citation-backed instructions and descriptive periodicity. No Reference tasks, schedule IDs, AMOS, Services, Today links or notifications. UI currently still only SIRE/VIQ placeholders. No actual PDF/DOCX viewer, global library storage, import picker or Settings exchange yet.
- Full signed/no-publish CI `43228912162` started for code commit `aa67869...`; do not call passed until completed. Older intermediate jobs cancelled by concurrency. No user data changed or installed binary distributed; stable available:false.

### CI verification outcome — 2026-11-12
- Previously marked pending run #43228912162 **COMPLETED SUCCESS** for all JVM tests, signed APK assembly and original package/signature verification. No release, feed update or installation. Both stat widgets and Reference backend are staged, not HONOR accepted.

## 2026-11-13 — Reference Equipment interface staged but unaccepted
- Feature branch `fix/rc20-pre1-handover-tasks-ui` now includes source UI for vessel equipment library: folder hierarchy, create/edit/delete equipment, procedures with required source+excerpt, read-only procedure reader, text search, Android document picker export/import of one self-contained .navref, import conflict preview and Settings import/export panel. Existing Reference SIRE 2.0/VIQ7 placeholders are retained.
- No Reference linkage to AMOS, Services, tasks, Today, scheduling, reminders, or task-run state; source-only interval metadata. Personal and vessel task JSON formats untouched. Per-vessel equipment JSON in independent app-private directory; common PDF/DOCX library not yet implemented.
- New UI source stage pending CI `43246741027`, no real HONOR test or release; stable feed remains disabled and installed RC20 unchanged.

## 2026-11-13 — UX search follow-up and backup limitation
- Initial new Reference UI code/Settings exchange signed CI #43246741027 SUCCESS. Subsequent narrow changes improve folder-title search and Back ordering while adding no tasks/business logic; latest CI #43248144577 pending. Physical HONOR acceptance still outstanding.
- Reference equipment uses independent app-private storage and IS NOT part of old 10-MB Full Profile JSON exports. Individual .navref one-equipment exchange works at backend/UI level; complete library+manuals export/backup is an outstanding pre-release gate.

## 2026-11-13 — Reference catalog backup stage
- Working feature branch now includes explicit .navref equipment-catalog backup & non-destructive restore in Settings; all device folders and cited procedures roundtrip and keep per-profile separation. This does NOT back up future manual PDF/DOCX binaries. No profile JSON or existing TaskStore/AMOS/Services update. Run #43250406239 in progress at latest check, avoid asserting completed.
- Independent experimental PDF viewer dependency branch `experiment/navtasks-reference-pdf-viewer-beta01` running #43251052670, not merged. Original installed RC20 and stable manifest remain unchanged.

### Verified 2026-11-13
- Equipment catalog archive CI #43250406239 SUCCESS. The work branch compiled and passed signed no-publish gate; release feed remains off. PDF compatibility in isolated lab: first build failed at AAR metadata (compileSdkExtension19), retest #43251678681 pending; no production source change.

## 2026-11-13 — Shared manuals staging, no user-visible import yet
- `ReferenceManualModels.kt` / `ReferenceManualStore.kt` create independent shared PDF/DOCX metadata + safe original-file storage, not wired into ReferenceHub UI. No binary-inclusive archive or reader yet. Store physically under app files not cache; existing equipment profile JSON unchanged.
- PDF beta01 dependency CI isolated #43251678681 SUCCESS after compileSdkExtension19. Work branch dependencies remain unchanged. Main CI #43252927005 pending; release channel off. HONOR still signed RC20/code188.

## 2026-11-13 — Complete original-manual transport backend staged (CI pending)
- Reference shared PDF/DOCX metadata, original-file streaming copy/sha validation, and `ReferenceManualArchive` .navlib byte-inclusive export with staged verification/restoration have been added. Manual reader/SAF UI still NOT wired; no user manuals were imported.
- Archive scope: shared original manual files and nested folders; per-vessel Equipment catalog backup is separate .navref. Neither ever includes Tasks, AMOS, Services or notification state. Last manual transport source CI #43253904642 pending; released RC20 untouched.

### Latest staged code — 2026-11-13
- Manual originals and .navlib backup/verified restore are backend only, now hardened against duplicate-SHA inconsistent size and corrupt existing hash-named blobs. Latest CI #43254879474 pending. No SAF import UI added and no installed app changes.

## 2026-11-13 — PDF/DOCX manual UI + native readers in branch, CI pending
- Full shared manual original-file storage/streaming .navlib backup now has SAF import/export UI and Reference category navigation (SIRE 2.0, VIQ7, My materials) in staged code. One file import verifies PDF/DOCX signatures and uses private non-cache originals; shared across all NavTasks profiles. Existing Equipment catalog independent and vessel-scoped.
- PDF internal renderer uses native one-page-at-a-time Android PdfRenderer, zoom/page navigation, API35+ native text search. Android 31-34 have page view only pending compatible full-search fallback; DOCX local simplified paragraph/heading reader only. These are NOT physical-device tested.
- Signed nonpublish CI #43258709724 pending after additional DOCX regression test. Previous CI #43254879474 failure was deprecated createTempDir only; corrected. Stable feed OFF and installed RC20 unchanged.

## 2026-11-13 — Reference offline library staged with user excerpts
- Source includes SIRE/VIQ/My materials shared PDF/DOCX offline file import with safe .navlib full original-byte export/restore, folder management, native PDF reader/page navigation (API35+ text search), simplified DOCX paragraph/heading reader and user-defined excerpts with optional manual citation. All independent of vessel profile Task/AMOS/Services; equipment remains per vessel with its separate .navref exchange.
- Source-only CI #43258709724 succeeded before excerpt changes; current complete CI #43260910863 pending. No HONOR or Samsung UX acceptance, no release. Older Android pdf search fallback and rich rendering require more work before final 1.0.0.

### Verified 2026-11-13 — source CI completed
- #43260910863 SUCCESS for complete Reference manuals+standalone excerpts UI/source as of commit 8ac9b86; all tests and signed private release identity verification passed. Subsequent changes limited to documentation. This is NOT proof of app UI acceptance on HONOR/Samsung, PDF search older Android, or 1.0.0 release readiness.

## 2026-11-13 — Pre-QA stability pass after library feature work
- Since signed success #43260910863 (source 8ac9b86), narrow fixes only in Reference manual archive staging lifecycle, abandoned crash staging cleanup, cancellable stale PDF searches and original excerpt-folder preservation; tests added for stage cleanup and root-folder edit.
- Source-branch signed CI #43265305900 PENDING, not accepted until PASS. User's installed RC20/code188, signed baseline and public stable feed OFF. No new app installed, no data migrated.

### 2026-11-13 — Recovery-first private QA staging
- Reference-only safety polish code HEAD `d804884` undergoing signed CI #43265672215. Source code changes: noninterruptible staged restores with non-cache/no-backup temp path and abortable large-PDF searches; no existing task business changes. User-facing installed RC20/code188 intact.
- Branch `qa/navtasks-rc20-recovery-code191` from original accepted source contains ONLY versionCode191/release suffix and private artifact CI #43266688902 pending. Intended downgrade-safe fallback after future QA code190 install. Neither artifacts nor test APK yet delivered.

### 2026-11-13 — first safe private QA install set ready for device testing
- Candidate `0.4.170-qa1` code190 private DRAFT APK and original-RC20-based fallback code191 private DRAFT APK both built with original cert, release non-debuggable, identical package, JVM tests PASS and artifact asset bytes present. CI #43269120575 / #43269056417 SUCCESS. Stable feed OFF.
- No device installs yet. User should download both and export existing profile before updating; Android downgrade to code188 is blocked after QA190 so use code191 recovery if required. QA is NOT stable 1.0.0.

### 2026-11-13 — PDF full-screen correction staged for user screenshot findings
- On installed QA190 HONOR, imported PDF originals and page navigation work; document is squeezed under app title/dock, cropped horizontally in some cases and search matches not highlighted. User explicitly expects full-screen reader.
- Staged Reference-only full-screen Dialog reader, fit-width page geometry, bounded memory, pinch/zoom/scroll, per-match native API35 PDF coordinate highlighting/auto-scroll. DOCX also opens full screen, but simplified text mode unchanged; minSdk31 PDF search fallback still pending.
- Source CI #43277871872 pending. New private QA device APK must have code>=192 (device already on 190; recovery code191 existing). No code190 binary modification. Stable feed off.

## 2026-11-13 — HONOR QA2 user feedback / ongoing fixes
- QA2/code192 installed. Day screenshots show widgets 2x2 stretched by MagicOS launcher, poor typographic density and stale color after OS theme switch; PDF still single-page fullscreen dialog with pinch lag; user requests file attachment inside equipment procedure.
- Main branch source adds widget dual day/night colors and qualifier backgrounds, bigger/adaptive small+wide text, deferred PDF raster generation after pinch, standalone attached PDF/DOCX per equipment procedure linked to shared originals by SHA and backed up in .navlib (not .navref). JVM regression `ReferenceProcedureAttachmentTest.kt` added. Full main signed CI #43367074167 pending.
- Experimental, isolated `experiment/navtasks-native-pdf-fullscreen-nov13`: AndroidX PDF Viewer beta01 true continuous scroll/search/zoom in separate full-window Activity; extension>=13 check and existing Compose fallback. CI #43367575751 pending, not merged to working branch.
- User's stable app data unchanged; no new APK issued after QA2, recovery code193 remains available, stable feed available:false. Next build code>=194 with fresh downgrade-safe fallback first.

### 2026-11-13 — Native full-screen PDF reader staged, main CI pending
- Isolated native AndroidX PDF dependency and actual fullscreen reader passed #43367575751. Components now selectively present in main feature branch: original PDF SHA lookup, native AppCompat fullscreen fragment with continuous scroll/search/selection, fallback to existing Compose reader on Android S extension<13, library and equipment procedure links. Main CI #43368601723 pending. Previous widget/attachment source CI #43367074167 SUCCESS. No installable new QA created.

## 2026-11-13 — QA5 HONOR acceptance and QA6 continuous PDF staged
- HONOR owner confirms QA5/code198 external chooser + internal preview work. Source SHA 229f7f1; no further approved changes to the QA5 source.
- QA6 code200 branch `feature/navtasks-qa6-continuous-pdf-code200` adds a Compose LazyColumn continuous vertical PDF reader using existing framework PdfRenderer and max 2.4 MP bitmap per composed page. Prior single-page PDF reader with search, zoom and match highlights stays accessible with a mode switch; experimental crashing AndroidX PDF Activity stays off. No change to Reference archives/originals or other business modules.
- Code200 signed private QA CI #43386652027 and exact-QA5 recovery code201 CI #43386746405 are running as of documentation update. Compilation and on-device acceptance are NOT yet claimed. Stable metadata unchanged OFF.

### 2026-11-13 — VERIFIED PRIVATE QA6 and exact QA5 recovery signed CI PASS
- QA6/code200 signed workflow https://github.com/sergbokarev95-arch/NavTasks/actions/runs/43386652027 **SUCCESS** from source commit `d96d53d5202d23c22f742ba037cbf10fc17f3df9`. All JVM tests and release build passed, package `com.navtasks.app.glass`, version `0.4.175-qa6` / code200, original signing certificate SHA256 `0cb8a3c691e3fb2198682dff8c226aec710e77c7b8fded7d260a92f359a49c54`, not debuggable. PRIVATE DRAFT `untagged-3c5d969aa52139c820aa`, APK `NavTasks-0.4.175-qa6-code200.apk` 36,507,284 bytes, SHA256 `85d3aa508a10214b96607cbd46377d39a9e600425a027544e735e77575890904`.
- Exact accepted QA5 application-source emergency recovery/code201 signed https://github.com/sergbokarev95-arch/NavTasks/actions/runs/43386746405 **SUCCESS** from `f8ee25eb6c37117d18d27b673a2fa66ba4289e8a`. Compare to QA5 code198 shows ONLY version metadata, static version test expectations, separate private workflow; no business/UI changes. PRIVATE DRAFT `untagged-9398ca4278c5b9a53985`, APK `NavTasks-0.4.176-qa5-recovery-code201.apk` 36,474,520 bytes, SHA256 `2da72f4e471ad10d0d2f7e39232b7be1d2b5b998fe12bd92c1d99ea2fd0c38fc`; original cert/package verified.
- PUBLIC stable feed still OFF. **No physical HONOR QA6 acceptance yet.** User should export FULL profiles plus separate .navlib and .navref as appropriate, download both files, install ONLY code200 over currently accepted QA5/code198 (no uninstall or data clearing), check horizontal vs vertical/pinch/zoom/search fallback, PDF and DOCX, "Open with", large PDFs, memory/crashes and application data. Recovery201 is ONLY if QA6 regressions emerge. If code201 is installed, subsequent versionCode must be >201.

## 2026-11-13 — HONOR accepted QA6; widgets and blue reader action QA7 staged
- Owner verified signed QA6 `0.4.175-qa6` code200 on HONOR: vertical continuous PDF reading and existing external chooser work. Do not rewrite or replace accepted QA6 reader.
- Explicit user-approved remaining 1.0.0 scope: (1) finish PDF [HONOR accepted], (2) minimal visual/automatic theme widget polish and filled Apple-blue `Открыть через`, (3) rich-text selection and redesigned app icon, (4) second Android device check, (5) signed final release after full regression and separate approval. No new feature scope.
- Isolated branch `feature/navtasks-qa7-widget-polish-code202` adds: two redesigned compact/wide RemoteViews XMLs with refined neutral typography, margins, progress bars, red missed pill, current/next visual hierarchy; fixes Russian "Осталась 1 задача"; fills the existing manual "Открыть через" button with the already-established Apple-blue selection color + white text, keeping ACTION_VIEW/FileProvider read-only and all PDF logic.
- Correct Android platform limitation: `Intent.ACTION_CONFIGURATION_CHANGED` cannot be received by a manifest-registered receiver (Android official API); use process-live `NavTasksApplication` dynamic registration and explicit light/dark background drawable on every widget refresh, plus Android system periodic `APPWIDGET_UPDATE` floor 30 minutes when process is dead. Preserve in-app theme selection, RemoteViews setColorInt day/night foreground palette, existing tasks/routines and no launcher completion control. This **must** be visually verified on HONOR with existing placed widgets while app is background/killed; do not claim instantaneous changes on every OEM when process is dead.
- QA7 source + original-signer private APK code202 CI `43389549409` started; separate exact accepted QA6 application-source (source SHA `d96d53d5202d23c22f742ba037cbf10fc17f3df9`) higher-code203 emergency rollback CI `43389554466` started. Both are PRIVATE DRAFT only; first validate CI pass, signer digest, versionCode and assets. Public stable feed stays `available:false`.
- HONOR QA: export FULL profiles and .navlib original manuals, download both verified APKs before installing anything, install ONLY QA7/code202 in-place over QA6/code200; check 2×2 and 4×2 on launcher, automatic System Day/Night change WITHOUT readding widget, geometry on this device, widgets' single tap opens NavTasks without completing tasks, missed badge only when backlog exists, zero/complete/tomorrow behaviors, PDF/docx "Открыть через" action, profiles and navigation untouched. Use code203 only on regression, never uninstall or clear data. Samsung separately for 1.0.0.

### 2026-11-13 — VERIFIED QA7 private signed APK + exact QA6 recovery203
- User accepted QA6/code200 PDF on HONOR before this stage; no change to rendering, FileProvider or document model in QA7.
- Final corrected QA7 signed workflow `43390512011` **SUCCESS**: JVM unit tests (including `NavWidgetPolishTest`), release build, original certificate SHA256 `0cb8a3c691e3fb2198682dff8c226aec710e77c7b8fded7d260a92f359a49c54`, nondebuggable app `com.navtasks.app.glass`, versionName `0.4.177-qa7`, versionCode `202`. Source `05bc1a985b099527dc03bfd9610cde9fcf89a561`. PRIVATE DRAFT https://github.com/sergbokarev95-arch/NavTasks/releases/tag/untagged-b308b6cb4354ac863e8a, exact APK `NavTasks-0.4.177-qa7-code202.apk` size `36,507,536` bytes SHA256 `32f94f68ca3a8c36ded7c7401682a47bfe0f8b2a0180fc3ea12651c383fdc2fb`.
- Original QA6 application-source emergency recovery/code203 workflow `43390517112` **SUCCESS**: same original package/certificate, nondebuggable, version `0.4.178-qa6-recovery` code203. Source `301e825374e6633c87dbbe2238b175a51e15bb2c`. PRIVATE DRAFT https://github.com/sergbokarev95-arch/NavTasks/releases/tag/untagged-b13bd3745245aa843011, exact APK `NavTasks-0.4.178-qa6-recovery-code203.apk` size `36,507,288` bytes SHA256 `df0da5a77abf1182126e493b3616b9739c4e0fc94d3f494c076cf2fa87053bd9`. Compare from QA6 accepted `d96d53d5` shows ONLY version/test/workflow diff, no app logic.
- First workflow attempt had an incorrectly reused QA6 **release tag**, despite passing signature/tests. Those PRIVATE older drafts MUST NOT BE LINKED or INSTALLED. Correctly tagged and separately verified final releases above supersede them.
- PUBLIC `NavTasks-Updates/channels/stable.json` verified `available:false`. No public stable, no merge to main, no live data migration. **HONOR physical QA7 pending**: export full profiles + .navlib, download both final APK files, install ONLY code202 over current QA6/code200 IN PLACE; test both widget sizes already placed and System Day/Night transitions (including backgrounded app), no widget-triggered completion, text/percent/missed, blue Open with PDF/DOCX, cold restart and data persistence. Use code203 only after regression; never uninstall/clear data. Apple HIG visual acceptance requires real screenshot review, CI pass alone is insufficient.

## 2026-11-13 — Widget QA8 code204 staged after HONOR QA7 theme mismatch
- QA7/code202 NOT visually accepted: Night widget background not dark enough and stale night card background remained when Day foreground colors switched; new widget showed Day correctly. Percent 84% floated small in upper header.
- QA8: four explicit RemoteViews layout IDs and single atomic day/night color selection on *full* update, near-black #F8000000 night drawable, 16sp percent beside bar. Retains 2x2, 4x2, existing read-only behavior, task counts, QA6 PDF and Blue Open with.
- Signed CI #43410980825 running; exact QA7 source signed code205 recovery CI #43410986526 running. Physical HONOR theme switching and layouts still unverified. Stable OFF; main unmodified.

### QA8 signed private CI VERIFIED — 2026-11-13
- User-installed baseline QA7/code202 remained intact, and QA8/code204 was built from `1dfd6eaeec1bb78d568ad9518ff45d3002d5c280` using original signing key/certificate, appId `com.navtasks.app.glass`, non-debuggable `0.4.179-qa8` / versionCode `204`. All JVM tests (including regression for four theme layouts) and :app:assembleRelease passed, GitHub Actions #43410980825 **SUCCESS**.
- Correct PRIVATE DRAFT QA8: https://github.com/sergbokarev95-arch/NavTasks/releases/tag/untagged-83651a9e71bcdfeb423c, asset `NavTasks-0.4.179-qa8-code204.apk` size `36,512,148` bytes, SHA256 `5ec4befdc91855c3cadc1c3e477e1c3d60ec9f833762450a3ba65206c408c5ea`.
- Exact installed QA7 application source protected as signed higher-code205 emergency recovery in branch `qa/navtasks-qa7-recovery-code205`: code difference from accepted SHA `05bc1a985b099527dc03bfd9610cde9fcf89a561` is ONLY version metadata/test expectation/private CI workflow, NO code changes. Signed non-debuggable release `0.4.180-qa7-recovery`/code205; workflow #43410986526 **SUCCESS**, original certificate.
- Correct PRIVATE DRAFT recovery: https://github.com/sergbokarev95-arch/NavTasks/releases/tag/untagged-b15372b28147997a4a7a, asset `NavTasks-0.4.180-qa7-recovery-code205.apk` size `36,507,544` bytes, SHA256 `3887d64ed7750b4a3b6ff40338b3f84d1dccee1aa4c523ca491a6adaabcf1cc8`.
- Final QA8 branch code files unchanged after successful build: only appended documentation; compare from build SHA confirms that. Stable channel `available:false` and main unmodified. **HONOR physical QA still pending**, so do not mark widgets visually accepted or 1.0.0 ready.
- Exact device sequence: FULL profile export and separate .navlib backup, download BOTH verified files while network available, install ONLY QA8/code204 over current QA7/code202 IN PLACE. Never uninstall or clear data. Check existing 2×2 and 4×2 widget Day->Night->Day from NavTasks settings and System settings, black background/white text together, light/black text together, 84% beside bar, missed pill and data preservation. Recovery205 ONLY on observed failure; subsequent versionCode must exceed205 if used. Afterwards text selection/icon, second device, 1.0.0 acceptance.

## 2026-11-13 — QA9 black widget translucency isolated
- QA8/code204 user screenshots confirm dark and light widget palettes are now both legible with correct 84% position. Requested slight transparency for black surface. QA9 touches ONLY `navtasks_widget_bg_dark.xml`, changes #F8000000 to #E0000000 (~88% alpha) with regression test expectation and version metadata; app otherwise exactly QA8.
- Platform conclusion: no guaranteed instant theme change on HONOR while NavTasks process is stopped; existing process-live configuration receiver + periodic host refresh at Android's 30-minute minimum + NavTasks onResume refresh are sufficient; user allows documenting limitation and proceeding. No extra background polling.
- Private signed CI #43413317881 code206 and recovery #43413325570 code207 started; signed/device acceptance pending. Public stable OFF.

### 2026-11-13 — QA9 signed final widget-alpha candidate VERIFIED
- GitHub Actions #43413317881 SUCCESS: all JVM tests and non-debuggable release assembled with original `com.navtasks.app.glass` certificate SHA256 `0cb8a3c691e3fb2198682dff8c226aec710e77c7b8fded7d260a92f359a49c54`, versionCode206, versionName `0.4.181-qa9`. Source commit `4ae888dc07ac81259e537d4dbe590cf840fb3cc9`. Private DRAFT `untagged-0b3cac527bf65bd1e333`, asset `NavTasks-0.4.181-qa9-code206.apk` 36,512,148 bytes SHA256 `2bdd3ab64f41dbbb8d8a40ed8734c3f6036837cd631b8f287562d529261b30f3`.
- Exact accepted QA8 source emergency rollback versionCode207 (`0.4.182-qa8-recovery`) GitHub Actions #43413325570 SUCCESS, original signing identity, non-debuggable, same appId. Source commit `a425d36445458aaef46d401047e0056794164252`. Private DRAFT `untagged-0652f546156cce7cf29a`, asset `NavTasks-0.4.182-qa8-recovery-code207.apk` 36,512,156 bytes SHA256 `6a35caacd46bbd9d301f4d26cddd6ee64d7af6014ef656e2797c46a078768121`. Diff from accepted QA8 code204 only version metadata/test + new workflow, NO UI/business changes.
- Final code changes after signed QA9 source commit are documentation only. Public stable.json verified `available:false`; no main merge, no public update and no device change. Device acceptance remains pending; user should export FULL profile + .navlib, download both private assets and install ONLY code206 over QA8 code204, no uninstall/data clearing. Recovery207 ONLY on regression. On success, stop widget work and move to rich-text selection and app icon, second-device QA and final signed 1.0.0 after user approval. Note background-killed instantaneous theme change may be delayed; acceptable limitation per owner.

## 2026-11-13 — QA10 selection handles upstream fix candidate
- Installed screenshot: Handover rich note ECDIS lets select one word; extending blue handle stops after each next word. Existing app Compose Rich Editor 1.0.0-rc14 upstream bug, not a general Android clipboard issue.
- Isolated QA10/code208 changes only RichEditor dependency to stable 1.0.0 (upstream #746/#748 selection fixes), app version and targeted test. Original RichTextState, HTML, Handover edit/save semantics remain unchanged.
- Original signed QA9 exact-source recovery code209 staged separately. QA10 CI #43416622629 and rollback CI #43416631675 running; not device-tested or approved. Potential Kotlin metadata mismatch under review. Stable feed OFF.

### 2026-11-13 — QA10 original-signer PRIVATE CI VERIFIED, HONOR gesture QA PENDING
- Stable upstream Compose Rich Editor `1.0.0` (previous `1.0.0-rc14`) fixes Android long-press/selection-handle drag (#730, #731), as merged PR #746 (and edge crossing #748); exact issue described by user. The app's Kotlin `2.3.21`, Compose BOM, `NavRichNoteEditor` UI, rich-state persistence and Handover format handling remained unchanged; no broad migration needed for compilation.
- Source at CI `fdcc6ffe3f8fa085db8250ff8d87135f1f903a12`; private signed QA10 `0.4.183-qa10`/code208, package `com.navtasks.app.glass`, non-debuggable, original certificate SHA256 `0cb8a3c691e3fb2198682dff8c226aec710e77c7b8fded7d260a92f359a49c54`. GitHub Actions `43416622629` SUCCESS: JVM suite and assembleRelease, signature/version verification.
- QA10 DRAFT https://github.com/sergbokarev95-arch/NavTasks/releases/tag/untagged-267c5792eb36c2f2cda1 APK `NavTasks-0.4.183-qa10-code208.apk` 36,920,262 bytes SHA256 `3b6535acd2330687fe2aab764175735c8b3f9667fb869e5d3aa3bcef15c64cd0`. Private draft only; never publish stable before device QA.
- Exact QA9 app-source emergency recovery code209 private CI `43416631675` SUCCESS; only build version/test expectation/workflow differ from original QA9 source `4ae888dc07ac81259e537d4dbe590cf840fb3cc9`; original certificate/package, `0.4.184-qa9-recovery` code209. DRAFT https://github.com/sergbokarev95-arch/NavTasks/releases/tag/untagged-c53c550e91a0dd2bd59f APK `NavTasks-0.4.184-qa9-recovery-code209.apk` 36,512,156 bytes SHA256 `a9eabf4118c0b6144e3eae6dfca30a3bbef70f06137ad8c0466e29a97a2ac35c`.
- `NavTasks-Updates/channels/stable.json` verified `available:false`, `main` not modified, no account data touched. User MUST export full profiles and .navlib, download both signed private assets then install ONLY QA10/code208 IN PLACE. Never uninstall/clear data. Check ECDIS old note drag selection across several words/lines continuously, select-all/copy/cut/paste, B/I/U/color across range, Save/reopen formatting, RU keyboard, no crash, profiles/amos/services unaffected. Recovery209 only on regression; next versionCode must exceed209 if installed.
- **Do not claim gesture bug resolved on HONOR until user physically tests it.** After confirmation, next task is new NavTasks app icon, then Samsung/other phone and final signed 1.0.0 QA/release approval.

## 2026-11-13 — QA11 approved dual-mode NavTasks launcher artwork staged
- User tested QA10 rich-note blue selection handles on HONOR and explicitly confirmed "работает идеально". Preserve the accepted stable Compose Rich Editor v1.0.0 and all existing stored notes.
- User approved icon design: system-blue/cyan gradient normal Android background; separate transparent foreground featuring one segmented compass ring with NO filled disk, orange north arrow, pale west/east/south points, two white layered task cards, three left-side checklist dots, vivid-blue check mark. No anchor or extra elements. HONOR MagicOS Liquid Glass variant must show ONLY foreground (compass/cards/check) with transparent background, not any baked-blue tile.
- Android implementation in `feature/navtasks-qa11-icon-code210`: update the three EXISTING VectorDrawable resources `navtasks_icon_background.xml`, `navtasks_icon_foreground.xml` and `navtasks_icon_monochrome.xml`. Existing adaptive `mipmap-anydpi-v26/ic_launcher.xml` binds all three, manifest and original package `com.navtasks.app.glass` unchanged. Android standard themed icon uses monochrome tint; this is different from OEM Liquid Glass.
- Technically essential: a single Android adaptive-icon definition CANNOT automatically detect an installed HONOR MagicOS Liquid Glass theme and conditionally omit only its background while keeping the full-color foreground. The opaque blue adaptive layer stays in the regular APK; a separate full-color transparent `com.navtasks.app.glass.png` is generated from the exact Android vector using `tools/icon_artwork/export_magic_glass.py`. It must be merged into user's EXISTING HONOR `icons.zip` inside Magic Glass `.hnt` theme as an icon override to keep glass background. No active `.hnt` was supplied so DO NOT fabricate an automatic switch or overwrite a theme; release also includes an icon-only overlay zip that is NOT a standalone theme.
- Original notification small icon left unchanged to avoid regression; only launcher icon assets altered; task/AMOS/Services/Handover, PDF, widgets, update logic, signing etc unchanged.
- QA11/code210 private signed workflow #43422885390 started; it runs full unit test and :app:assembleRelease, original certificate/package verification, exports transparent PNG and icon-only ZIP into PRIVATE DRAFT along with APK. Exact accepted QA10 source code211 emergency rollback branch `qa/navtasks-qa10-recovery-code211` workflow #43422895671 started. Both outcomes and digests need to be checked before distributing. No public stable feed or main merge.
- Post-CI HONOR physical QA: export full profiles + separate .navlib before in-place install; download both APKs, install ONLY QA11/code210 and check normal blue adaptive icon on launcher, app tasks/notes unchanged and app launches from shortcut. Then test transparent PNG theme overlay with existing Magic Glass `.hnt` only after safe merging/preserving existing theme; verify no blue square, all colors retained, small icon legible and badge/shortcut unaffected. APK alone cannot guarantee Magic Glass appearance. Recovery211 only for regression, do not uninstall/clear data.
- After owner accepts app icon, next second device Samsung test and final full regression/signature/backup/update QA before separately authorized 1.0.0 stable release.

### 2026-11-13 — QA11 icon + transparent HONOR theme asset CI VERIFIED
- User accepted QA10 rich text selection on HONOR. QA11 staged ONLY launcher VectorDrawable resources, version/test policy, export script and private signed workflow. No edits to note editor, business logic, notifications, widgets, AMOS, Services, Handover, updates, PDFs, or manifest.
- Signed `NavTasks 0.4.185-qa11` / versionCode210 original package `com.navtasks.app.glass` private GitHub Actions `43423620341` **SUCCESS**, original cert SHA256 `0cb8a3c691e3fb2198682dff8c226aec710e77c7b8fded7d260a92f359a49c54`, non-debuggable, all debug JVM tests and release assembly passed. Exact application source build SHA `7caf9d281c7421e3c4a4d2d17929e022dd372782`.
- Private draft: https://github.com/sergbokarev95-arch/NavTasks/releases/tag/untagged-76ac11a0e661458f4c20
- APK `NavTasks-0.4.185-qa11-code210.apk`, 36,921,170 bytes, SHA256 `44358f77552a3029d7c8bf9d743fdb317a65fa426ae6e982f9b5f085284c7538`.
- PNG `com.navtasks.app.glass.png`, transparent RGBA 512×512, 45,751 bytes, SHA256 `f05d2a62ea129438dcee45f2071f7ad2ba6381eb020f0c9a73e559c06d25102d`. Exported directly from Android VectorDrawable foreground; validated all four outer corners have zero alpha. Orange north, white compass ring, two white cards, three dots and blue check; NO blue tile in this PNG.
- Icon-only archive `NavTasks_MagicOS_LiquidGlass_Icon_Overlay.zip`, 45,025 bytes SHA256 `f0860beec20c47b8cd18890e6995812941781020cbfcb1697f70272d91671d2d`. It is NOT a standalone HONOR theme. Needs SAFE merge into the existing Magic Glass `.hnt` `icons.zip`, preserving `transparent_config.xml`, other applications' icons, wallpapers, themes and package structure. Do NOT claim APK auto-detects OEM launcher theme.
- Exact accepted QA10 app-source emergency recovery code211 GitHub Actions `43422895671` **SUCCESS**, private draft https://github.com/sergbokarev95-arch/NavTasks/releases/tag/untagged-f3ab03b41aa9b6f9f7c4; APK `NavTasks-0.4.186-qa10-recovery-code211.apk`, 36,920,262 bytes SHA256 `7059ba88d448407381ae4b1e532018316b2542e4f412ed6b72592f5b07c3bb2c`; compare original QA10 source `fdcc6ffe3f8fa085db8250ff8d87135f1f903a12` shows ONLY version/test/new private workflow; no app code change.
- Public stable.json `available:false` verified, main unmodified, no live data touched. **QA11 not HONOR device-accepted yet, and transparent layer not installed in the active MagicOS theme**. Owner must export full profiles + separate .navlib, download QA11 APK AND higher-code211 recovery before installing only QA11 in place; check launcher normal blue, theme and app persistence; optional Liquid Glass overlay separately after original HNT theme is provided/backed up. Subsequent versionCodes >211 after code211 recovery is installed.
- After icon acceptance, second device (Samsung) and final 1.0.0 release QA/approval only. No additional feature scope.

## 2026-11-14 — Pre-1.0.0 two-device debrief: release blockers and QA15 PDF gesture candidate
- HONOR+Samsung user/friend found blockers: selective service request numbers disappeared; full-profile import offered to overwrite different-named vessel due to shared `ship-default` ID; `.navref` equipment exports contained manifest but not the actual PDF/DOCX. User also requested no pan/pinch snapping PDF upper-left, and read-only Handover Word/PDF export for iPhone/PC successors. No stable release authorization while these are unresolved.
- QA12 `feature/navtasks-qa12-exchange-safety-code212` CI #43466075956 SUCCESS: selective request preserved and profile match based on same case-insensitive normalized name + kind; ID alone NEVER authorizes replacement. Exact same-name replace remains confirm-only.
- QA13 `feature/navtasks-qa13-reference-package-code214` CI #43466350524 SUCCESS: self-contained `.navref` v2 streams original PDF/DOCX with SHA validation and imports only after staging; legacy v1 without binary is warned, not falsely presented as complete. Data stores and existing modules untouched.
- QA14 `feature/navtasks-qa14-handover-documents-code216` CI #43467918514 SUCCESS: offline Handover DOCX + searchable A4 PDF export using Android CreateDocument and read-only snapshot (not editable app state). Need physical file opening on iOS/PC and user review; CI does NOT confirm compatibility on external devices.
- QA15 `feature/navtasks-qa15-pdf-pan-code218`: fixes wrong upper-left pinch focus in framework PDF paged + continuous readers. Zoom only on >=2 pressed pointers; single-finger drag remains native scroll. Focal-preserving scroll math and visible LazyList page under actual pinch centroid; regression tests cover left/right, zoom in/out, multi-page anchor and clamps. Signed CI #43472051215 RUNNING and exact QA14 source higher-code219 recovery CI #43472078535 RUNNING. Private draft only; do not claim device acceptance.
- User-approved future post-1.0.0 concepts: optional curated/shared seafaring reference library with online catalog, downloadable PDF/DOCX packages and update metadata independent of APK; Handover photos attached to entries. Should be designed separately with permission/moderation, publisher consent, licensing, and offline caching (avoid uploading confidential vessel documents). Not in release freeze.
- Final release gate: both CI PASS + original key/ID, PRIVATE APK hashes verified, full profile + separate `.navlib` backed up, QA15 installed in place on HONOR, full exchange roundtrip between two DIFFERENT profiles on Samsung, verify selected AMOS and Service request fields, `.navref` original PDF/DOCX open, Handover Word open on iPhone/PC and PDF on Android/iPhone, two-finger zoom/finger drag across PDF, old data/backup preserved. Only then final audit, main merge, versionCode beyond all installed rollback builds, signed 1.0.0 and stable feed approval.

### 2026-11-13 — QA15 signed PDF focal gesture build VERIFIED, physical QA pending
- QA15 private original-signing GitHub Actions #43472806069 **SUCCESS** (JVM unit tests, compile debug/release, APK identity, non-debuggable versionCode 218 / versionName `0.4.193-qa15` / appId `com.navtasks.app.glass`, certificate SHA256 `0cb8a3c691e3fb2198682dff8c226aec710e77c7b8fded7d260a92f359a49c54`), build source `400f13e56a20f531d081d9a08b41971440439471`. First CI #43472051215 failed due to restricted AwaitPointerEventScope attempting arbitrary suspend scrolls; fixed by composition `rememberCoroutineScope().launch` with cancel-previous focal scroll jobs and VERIFIED repeat build; never distribute failed run.
- Private **DRAFT** QA15: https://github.com/sergbokarev95-arch/NavTasks/releases/tag/untagged-8910b70a567452e8a658; `NavTasks-0.4.193-qa15-code218.apk`, 37,003,090 bytes, SHA256 `2f974f9f5680820467495ecd377d99d47d3112c0140bc36b7115712e8dc2521a`.
- Exact QA14 application source emergency recovery `qa/navtasks-qa14-recovery-code219` CI #43472078535 **SUCCESS**, same original appId/signing, non-debuggable code219 name `0.4.194-qa14-recovery`. Diff against QA14 accepted source `a8ad509106707aa16ab0322365fee8896fc67b87` shows ONLY version metadata, associated unit test version expectations and private workflow; NO functional source changes.
- Private **DRAFT** recovery: https://github.com/sergbokarev95-arch/NavTasks/releases/tag/untagged-fb68cac5e5480e56d2e4; `NavTasks-0.4.194-qa14-recovery-code219.apk`, 36,986,702 bytes, SHA256 `f772bc175f8d0dc2268de8a3639cb41360439287b30b30f3f78db868cb19a471`.
- Public stable channel confirmed `available:false`, main untouched at `908c5d3c9df9840473af882491020401da6bc86f`. No physical HONOR/Samsung tests of QA15 yet and **NO stable release**. User must export FULL profiles and separate .navlib, download both candidates while connected, install ONLY code218 IN PLACE over current QA11/code210 or whichever signed lower-code build. Do NOT uninstall/clear app data. Save recovery219 only if QA15 has a regression. If recovery installed next code must exceed219.
- Next two-device QA: different-named vessel + same shared ID imports AS NEW profile; same name confirmation replacement; selected Services/AMOS requests roundtrip; `.navref` v2 equipment carries real PDF/DOCX that open on recipient; Handover DOCX/PDF open on Windows/iPhone with Cyrillic; PDF one-finger pan never zooms and 2-finger pinch around any page quadrant maintains focus in continuous AND paged mode; verify tasks/history/profile data unchanged, no crashes. Then independent final regression review and only with owner approval 1.0.0 public release.

## 2026-11-14 — QA16 HONOR/Samsung Handover export and incomplete .navref
- User submitted screenshot of export button in Handover and overlapping modal buttons in night mode; two `TextButton` actions `Word` and `Отмена` were rendered inside ONE `dismissButton` slot of `NavGlassAlertDialog`, whose single `Box` stacks children on top of each other. New isolated Handover modal has **separate**, full-width, at least 52dp tall glass Word/PDF rows and a distinct 44dp Cancel row. Toolbar `Экспорт` is now a 44dp glass control with matching blue typography. No note-export model or Handover persistence changed.
- The user's exact uploaded `Radar_X_Band.navref` was inspected byte-for-byte in the conversation: ZIP size **728 bytes**, **one entry only** (`manifest.json`), `schemaVersion:1`, metadata references `A.916(22).pdf` with a SHA-256 but contains **no actual PDF bytes**. This is a LEGACY v1 file and NOT a completed v2 archive; source alone does not establish whether a stale download, older installed APK, or alternate workflow produced it. Do NOT claim the precise human root cause without verifying the new exported file.
- QA15 code218 intended to use `ReferenceEquipmentPortableV2.export`, but that method writes directly to an Android SAF destination, and if the original cannot be found the user may be left with a partial/empty document. QA16 `feature/navtasks-qa16-export-ux-integrity-code220` changes the two equipment export triggers to the same preflight flow: generate complete v2 ZIP in a temporary private file, `stage()`/SHA/size/type-verify all original PDF/DOCX and metadata, **then** open Android CreateDocument picker with distinct `<equipment>_with_documents_v2.navref` filename. After writing, re-open the provider URI, stage and checksum-validate the saved result before displaying success. A failed copy/verification prompts a clear error and best-effort removal only of the newly created SAF document. Temporary files cleaned after completion/cancel/navigation. No Cloud repo/private data uploaded.
- Existing .navref v1 remains importable with an explicit missing-binary warning for compatibility. QA16 does not pretend to recover original PDF from old v1 file; recipient needs a newly exported v2 from sender.
- Added `NavExportUxAndPortableSafetyTest.kt` tests that require isolated Handover actions, v2 preflight and no legacy encoder UI calls. Existing `ReferenceEquipmentPortableV2Test` already covers original PDFs, SHA, tampering, and v1 warning.
- Private original-signer CI QA16 `0.4.195-qa16` versionCode **220** workflow #43552733231 and exact QA15-app-source emergency recovery `0.4.196-qa15-recovery` code**221** workflow #43552738083 have started; outcome and private release asset digests must be verified. Installed appId `com.navtasks.app.glass`; public stable OFF and main untouched.
- HONOR device test: backup entire profiles + separate .navlib, download BOTH signed APKs BEFORE installation, in-place install ONLY code220. Handover dialog in RU/EN day/night no overlap and Word/PDF still open on iPhone/PC; equipment with original A.916(22).pdf exports NEW file with suffix `_with_documents_v2.navref`, ideally larger than PDF, inspect as ZIP expecting `manifest.json` schema2 plus `originals/<sha>.pdf`, send that NEW file to Samsung, import and open PDF offline. Also test missing-original case refuses export BEFORE file chooser with comprehensible error, no empty archive; test source tasks/profiles and backups unchanged. Use emergency 221 only if verified regression; never uninstall or clear data.
- The user requested no release until these two-device checks and final independent audit; remaining scope fixed. Future shared reference catalog/photos remain after 1.0.0.
