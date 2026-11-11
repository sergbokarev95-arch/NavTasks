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


## v0.4.60 in-place Exact-Lab candidate (2026-11-08)
- Latest user feedback: full-app dock previews R1/R2 do NOT look like accepted Glass Lab 0.1.5; altering blur from 2/1.5 to 5/4dp was a mistake, and positioning varied. Correct approach is exact lab visual recipe with only production bindings and original-page capture.
- Candidate feature `feature/navtasks-liquid-glass-0.4.60-candidate` source `c3cd12e37c91d789e54ae32e1b61d6d03f46ab79`, CI `42399961454` SUCCESS. Same installed Beta `com.navtasks.app.glass`, same signing key, versionCode83/versionName0.4.60-beta; full-app tests passed; no data/business changes.
- Signed release https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.60-exactlab-candidate, APK SHA256 `f360f897619c71df90eacdf52ad22db426ad17d404fabb02c270a193bd35004a`. Backup profiles before upgrade and **do not uninstall**.
- Real-device user review of exact optical look still required. Different page content/contrast naturally changes refraction visibility; no claims of pixel identity yet. Stable `main` app/release remains 0.4.59 unmodified.


## 2026-11-08 — safe rollback package ready
- Same-package signed Haze recovery APK 0.4.61-beta/versionCode84, derived from original stable 0.4.59 sources; only build version differs. CI 42400505251 SUCCESS, aapt verified `com.navtasks.app.glass` and versionCode84. https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.61-haze-rollback ; SHA256 `cc2ae62b749b1873b72f3dcf8e18ffc030daae731d005e0a53e11d7b6252309a`.
- Can update over 0.4.60 ExactLab candidate without uninstall/data reset if candidate optics unsatisfactory. Export profile backup first. Physical glass acceptance still pending; main remains 0.4.59 Haze code.


## Standalone diagnostics to isolate NavTasks Liquid Glass composition (2026-11-08)
- Installed full-app 0.4.60 Beta user says refractive output less convincing than approved Lab 0.1.5. Renderer function and optics were verified identical, but background scene and Haze layer differ.
- Independent `glasscapture-v0.4.60-diagnostic1` ZIP signed CI `42402960923` SUCCESS; package `com.navtasks.app.glassdiagnostic`, 3 modes (LIVE Haze, LIVE no Haze, Lab reference cards), unchanged dock/lens. https://github.com/sergbokarev95-arch/NavTasks/releases/tag/glasscapture-v0.4.60-diagnostic1.
- On-device comparison pending; do not infer whether source capture or scene colors are responsible until screenshots. Main source remains stable v0.4.59.


## 2026-11-08 — solid-gradient card system preview available
- User chose richer **static card materials** rather than speculative changes to approved 0.1.5 glass. Common `navGlass` now has separate opaque gradient tokens for PANEL/TASK/NESTED_TASK/CONTROL in isolated feature; selected navigation and true optical dock preserved.
- Signed standalone https://github.com/sergbokarev95-arch/NavTasks/releases/tag/glasssurfaces-preview1, CI 42404664493 SUCCESS, source 9d210be7d0dc5d4e5f3bcaa24a49a17ad0f920f7; ZIP SHA256 1c0df6d2c3aa28eeac80ef525cd94999f393af05a3173fcfc4d895a4c17739ac. Separate app `com.navtasks.app.glasssurface`, no shared user data.
- Real HONOR design acceptance pending. Other overlay/dialog surfaces not rewritten. Production main app source remains v0.4.59 and in-place 0.4.60 candidate unchanged.


## 2026-11-08 — ready signed NavTasks Liquid Glass surface candidates
- User visually approved opaque-gradient Surfaces Preview1 on HONOR (light and night), requested Night selection darker graphite and dock lower by 4dp. Preview2 `feature/liquid-glass-surface-preview2` CI 42438614953 SUCCESS, signed ZIP tag `glasssurfaces-preview2`. Visual evaluation of tweaks pending.
- Same Preview2 UI ported unchanged into in-place signed Beta candidate `0.4.62-beta` code85, package `com.navtasks.app.glass`, feature source `7bea5101e051774d4fe82a74a2395d8d184ce27e`, CI `42438818985` SUCCESS, signed https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.62-liquid-glass-surfaces-candidate. ZIP SHA256 `c2c2246e2e7aa3afe1b7dc7ac455bb03c895d35a9739e013df3f670823aa1e1a`.
- Recovery: exact stable Haze sources, same ID & signing, `0.4.63-beta` code86 signed ZIP https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.63-haze-rollback, CI 42438923548 SUCCESS, SHA256 `1663e9c472ce26635a93d4171e5e9329ac6ee7db98ea2830e3339bc762036780`. Export full profile before updates; never uninstall.
- `main` production source remains v0.4.59 Haze with documentation-only changes; user currently has 0.4.60 ExactLab candidate installed. Final signed release pending on-device acceptance; next live versionCode must exceed 86.


## 2026-11-08 — isolated Glass UX Preview 1 released for HONOR review
- Candidate v0.4.62 approved Liquid Glass + opaque gradient surfaces preserved. Follow-up requested: native glass on more buttons, Settings cutoff footer fix, and gesture-open quick profiles centered popup with underlying blur.
- Built separate package `com.navtasks.app.glassux` branch `feature/liquid-glass-controls-quick-profiles-preview`, commit `ce0b76df9aa5dfc2550bfd56ef044e1037f8037b`. CI 42440578629 SUCCESS.
- Signed ZIP (10,876,570 bytes) https://github.com/sergbokarev95-arch/NavTasks/releases/tag/glassux-preview1 ; SHA256 `7f129658e517e3cfacb259249f6bb087101e91fe9284cdcf47b96d6e472a8c6b`.
- Direct Backdrop for floating '+' / Calendar; Settings scrollbar safe end inset; guarded two-finger pinch >=100dp start, >=52dp shrink, >=25% shrink, modal with blurred background and identical existing profile switching. Stable production source not merged; HONOR device feedback pending. Header buttons/remaining controls still not migrated.


## 2026-11-08 — pinch profile and Settings footer isolated Preview1 ready
- Preview branch `feature/quick-profile-pinch-settings-footer-preview` compiled and signed CI `42441508734` SUCCESS, from accepted Surfaces 0.4.62 code. Separate package `com.navtasks.app.glassquick` versionCode87, ZIP 10.9MB https://github.com/sergbokarev95-arch/NavTasks/releases/tag/glassquick-preview1.
- Only main root handler/blur/modal and bounded Settings scrolling changed. Original LiquidGlass dock, gradient card tokens, Task/Service/Handover/AMOS logic unchanged. Plain swipe tabs not modified. Device HONOR testing pending; no main production source merge.


## 2026-11-08 — Quick profile/Settings approved; '+' Liquid Glass isolated Preview2 available
- User reports Quick Profile Preview1 works correctly on HONOR. Accepted behavior: inward 2-finger gesture, central blurred picker, store switching, Settings footer.
- New isolated APK Preview2 code88 `com.navtasks.app.glassquick`: exact-dock Backdrop optical recipe on Tasks floating '+' only, original dimensions/click and Android12 fallback. CI 42442451411 SUCCESS. Release https://github.com/sergbokarev95-arch/NavTasks/releases/tag/glassplus-preview2; SHA256 `bd721d3d1d668972b279513a4d6be7afb5239065ef99e221bd48566b675ee4e6`.
- On-device screenshot/acceptance for plus glass pending; no production app changes. Do not treat a successful build as visual acceptance. Existing main code remains stable baseline.


## 2026-11-08 — same-ID 0.4.64 Beta candidate with all primary circular actions, HONOR pending
- Full preview-derived `0.4.64-beta` code89 app `com.navtasks.app.glass` signed and zipped (10,896,190 bytes, SHA256 f046815b1f0e41d3cf93ce56c0a148ca68b0520d27d9bc3dcdeb1cdd5c3c6a8e) https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.64-liquid-glass-actions-candidate; Actions 42444219901 SUCCESS.
- Direct-scene same-lab material on main '+' / Calendar / Settings, Back/Search across Plan/Settings/Task editor/Profiles/Services/Handover/AMOS/standalone Works, '+' on Services/Handover/AMOS. Only DAY rim softened on dock/action glass. Shader/refraction/lens movement and all static card/data material untouched. Feature commit 8040ff34abad71b2700b79af41c46f4b90a45726.
- Tested no-uninstall Haze recovery code90 `0.4.65-beta`, CI42444287505 SUCCESS, ZIP https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.65-haze-rollback; SHA256 db0748c7d7dbffc9637cf9f49e57924a288ba73281db5389725bd624cebb762e. User must export FULL profile first; never uninstall main app.
- User acceptance on HONOR pending. Main production source remains stable code; docs only updated. Future versionCode>=91 after recovery.


## 2026-11-08 — new 0.4.66 Beta ready: shorter one-green completion animation
- User accepted 0.4.64 glass controls and requested faster check motion. `0.4.66-beta` code91 same installed ID and Beta signature, isolated feature `feature/task-completion-single-green-0.4.66-beta` CI42445806856 SUCCESS. ZIP 10.9MB https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.66-completion-motion-candidate, SHA256 d17dd0945f733a68d1bc2379cd0bedc8398fb41ca14cd275edfb8f24672c6a7d.
- RunTileV2 only: full solid green 190ms, 250ms downward fade without rebound/stretch; both ordinary and final checklist, same completed state/dates. All optical material and data code blob hashes frozen in CI. Device visual acceptance pending, stable main source still unchanged.
- Backup signed original Haze 0.4.67-beta code92, CI42445698166 SUCCESS, ZIP https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.67-haze-rollback, SHA256 3adcb68baf94d2a546fb2171f9c5aa592007316a8142207a8ba1a7aab84a5d2d.


## 2026-11-08 — Requests filter for Services signed v0.4.68 candidate
- User wants third `Заявки` filter in Services alongside Works and Closed. It is only active, not completed `ServiceEntry` with `requestNumber.isNotBlank()`, regardless of planned/unplanned. Existing store & history untouched.
- New v0.4.68-beta code93 same-ID signed ZIP 10,897,386B SHA256 f5ae1dabbc36aa795c70ffa0b0afe93ad3ad62e8bb7987aa54edb69292d24290, CI42447897901 SUCCESS, release https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.68-service-requests-candidate.
- Prior Glass v0.4.66 exact-source recovery v0.4.69-beta code94 CI42447985380 SUCCESS, ZIP SHA256 34e9d23234d679e13b42b074dcf60007bd5e22e5a5b09d74ea893d85d2a39f41, release https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.69-glass-recovery. Separate optional only if needed, full data preservation on in-place upgrade.
- Device HONOR validation still pending; main app source unchanged, main documentation-only updates.


## 2026-11-08 — signed orthography and WYSIWYG notes candidate 0.4.70
- Source `a20ac5476084c72c51e37d10a9cb2b4f782dc07d`, branch `feature/rich-notes-ime-0.4.70-beta`; from previous v0.4.68 signed working source.
- IME sentence capitalization/autocorrect hints for prose and titles/search, exact AMOS ID/number exempt. Rich editor (B/I/U/S/color/highlight) on notes of Services, AMOS, Handover, checklist, with optional HTML field alongside unmodified plain comment. CI42455684933 SUCCESS, signed same-ID code95 ZIP 12,617,451 bytes SHA256 0f9de50ce81ab479e171af4234ee156e503b09841f7aa830daa503e870b6428a https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.70-rich-notes-ime-candidate.
- Recovery v0.4.71 code96 exact prior 0.4.68 Glass signed, CI42455220247 SUCCESS, SHA256 f57aa1ea1e524d3a8f4916cffe3db142aa9049db3d04a087cf852e3042b0cb82 https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.71-glass-recovery.
- Device checks still pending. Stable main app CODE unchanged; main is docs-only continuation.

## 2026-11-08 — Signed v0.4.72 visible Handover Edit candidate; HONOR confirmation pending
- Source `feature/handover-edit-access-0.4.72-beta` commit `9565c236459b2fd07b608d8f31ded9e051f959e4`; original v0.4.70 backed up. Only HandoverUi selected-entry dialog now has accessible blue 44dp edit pencil next to item title (instead of buried scroll-footer Edit), plus version and dedicated CI. Existing rich editor, comments/HTML save, Liquid Glass and all stores unchanged.
- Signed beta versionCode97 / versionName0.4.72-beta; Actions 42458988093 SUCCESS. GitHub prerelease https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.72-handover-edit-candidate ZIP 12,618,687 bytes.
- Same-rich-notes optional code98 fallback 0.4.73-beta, Actions 42459070578 SUCCESS, https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.73-handover-edit-recovery ZIP 12,617,462 bytes. **Do not uninstall or downgrade**. User exports FULL profiles first.
- Next: user installs candidate and validates edit visibility, prefill/save long note and formatting, all existing Handover marks, quick profile and day/night. Phone acceptance NOT yet reported. Main application source remains 0.4.59; docs-only updates.

## 2026-11-08 — Apple palette v0.4.74 Beta CI signed; device approval pending
- User requested one consistent Apple system blue + orange across app, Notes screenshot gray #F4F5F7 Day background, neutral gray/black text; all cards and dock to be designed/kept separately. Branch `feature/apple-semantic-colors-0.4.74-beta`, source `13b921992705efae9967f6489aa9bcfa618ab50f`, main app code still 0.4.59.
- Day/ Night blue `#0088FF/#0091FF` on icons/controls, contrast-safe text blue `#0071E3/#5CB8FF`, orange `#FF8D28/#FF9230`, Day small-text orange `#BE5300`; background Day `#F4F5F7` flat and Night unchanged true black. Existing card tokens and dock optics/shader unchanged. Font remains original: SF Pro licensing does not permit Android embedding, open-licensed Inter is proposed and awaiting confirmation.
- Actions 42484347829 SUCCESS tests/build/signed ZIP; https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.74-apple-palette-verified code99 12,617,196B SHA256 `23727cfc2f7517e695c1d6486ac891439e3b8d3bd10a3ca07907244cd701fda8`. Earlier intermediate attempts failed contrast tests or Github release 403; do not distribute earlier tag.
- Optional higher-code v0.4.75 code100 recovery exact previous 0.4.72 functional source, Actions 42483665590 SUCCESS, https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.75-apple-palette-recovery. Real-device acceptance not yet reported; main source not merged.

## 2026-11-09 — Selected controls Apple Blue/White v0.4.76 Beta (CI verified, phone pending)
- Based on signed 0.4.74 palette, feature `feature/apple-blue-selection-0.4.76-beta` commit `6285489663cf2a1b0bc8d2fd2d285e2d1e8ab077`; versionCode101. `GlassRole.SELECTED` now opaque Apple Blue #0071E3 with white text/icons (4.7:1), including Services/AMOS counts and request chips, Settings, Today, Tasks, Plan today, template selection, quick profile, Rich Note formatting. Ordinary action blues and cards retained. Dock optics unchanged. No font changes.
- Successful full CI `42490690406`, signed same-ID ZIP https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.76-apple-blue-selection-verified (12,616,447 bytes, SHA256 `acf67e4a4ff621f583143bb4f9b7abcd21486a2ba27e7945d79b73c44e9c6556`).
- Optional exact 0.4.74 UI safety recovery `0.4.77-beta` code102 `cb25d2fa654fa745c93b3aed502cb21fcd4447a8`, CI `42490737993` SUCCESS, https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.77-blue-selection-recovery. Full profile backup then in-place install, NEVER uninstall. HONOR visual approval not yet reported. Main source remains 0.4.59, not merged.

## 2026-11-09 — Selected card palettes 02/09 signed v0.4.78 Beta (await HONOR visual check)
- User chose Day #02 Slate Mist and Night #09 Smoke Slate from previously delivered ZIP mockups. Day only canvas + native bars/window become `#E6E8EC`, prior light cards/gradients retained. Night true black page with matte neutral outer panels `#1D2126`, tasks/controls `#282D33`, border `#40464D`, primary text `#F6F7F8`, muted `#AAAFB5`. Bright blue nav interactions and selected `#0071E3` / white content unchanged.
- Isolated feature `feature/card-palette-day02-night09-0.4.78-beta`, source SHA `b763e09efcb86874d50c506eb18b904c02150f7f`, signed same-ID versionCode103, CI 42495153837 SUCCESS full tests/build/identity/ZIP. Release https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.78-day02-night09-verified ZIP 12,616,447B sha256 `5225c639a182bee27adc27a3ecfeec83a8f4be9e750b58b9a4f27251affcbeaf`.
- Optional exact-source fallback 0.4.79 code104 based on previously signed v0.4.76, CI 42495228148 SUCCESS, https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.79-card-palette-recovery. Install over current app only if visual rejected. **Never uninstall**, export FULL profiles first.
- All Liquid Glass dock optics/layout, app logic/stores, Handover editor and Inter/SF typography status unaffected. Main application code remains 0.4.59; documentation updated only. User has not yet verified 0.4.78 on HONOR.

## 2026-11-09 — HIG polish signed 0.4.80, device test pending
- User accepted 0.4.78 Day02/Night09 colors. Latest feature is `feature/navtasks-hig-polish-0.4.80-beta` at SHA `acac8768678df359ed0cebefc09def712f5d3ac1`, same app ID and Beta key versionCode105. Changes scoped to typography of NavTasks header, dock tap scroll resets (per-tab state key, not optical dock changes), compact cards/checklist rows, consistent destructive red UI labels, neutral Night09/Day02 dialog surfaces and native little popup colors. No SF Pro; font family remains original.
- **True small popup Liquid Glass remains unimplemented:** separate DropdownMenu window has no access to dock's sampled Backdrop. Needs a separate in-scene overlay laboratory; DO NOT fake by transparency or risk optical dock performance.
- Actions 42498478208 SUCCESS (tests/build/signing/ZIP) https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.80-hig-polish-verified ZIP 12,620,028 B SHA256 `e1421d5b2daa0a3673fbd9bdfccffdf015b64a0e299c1d2fe4fe6c67a7650b04`; HONOR physical acceptance pending.
- Optional code106 `v0.4.81-hig-polish-recovery` exact pre-polish 0.4.78 UI, CI42498384092 SUCCESS https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.81-hig-polish-recovery. Never uninstall or clear local profiles. Main code remains 0.4.59, docs only.

## 2026-11-09 — v0.4.82 UX fixes signed, HONOR test pending
- User tested 0.4.80 on HONOR and showed failures: active dock retap didn't scroll/reset, expanded child cards too large/square, count separated from title, pink deletion colors, no search autofocus/keyboard, merged Handover Delete+Cancel.
- Root-cause fix to **both** dock onSelect implementations removes `if (!active)` guard. Today now explicitly resets date and independently scrolls task LazyColumn/date chips to item 0 on direct dock tap. Compact rounded 46dp subtasks with 44dp checks; inline 0/5 count. Shared NavGlassSearchField requests focus/IME; Handover footer uses individually framed 44dp buttons; semantic destructive systemRed #FF3B30 Day/#FF453A Night across UI, high-contrast smaller danger statuses separate. Existing glass optics, task stores and Day02/Night09 cards unchanged.
- Branch `feature/navtasks-ux-fixes-0.4.82-beta`, source SHA `bffd96d1799392aaeefa82e90e93c7abdf75d97b`; code107 signed same package. CI42502226588 SUCCESS unit tests+assemble+signing. Release https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.82-ux-fixes-verified ZIP 12,623,984B SHA256 `949d5321c02be4639d1fc5b62e8d166daef8dc90d429ad9921b0fdf11bca327f`. Physical HONOR review pending.
- Optional safe no-uninstall recovery exact original v0.4.80 source only version bumped v0.4.83 code108; CI42502302586 SUCCESS, release https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.83-ux-recovery ZIP 12,620,022B SHA256 `a92f492a2953a30cfc32fca046fe4f384c1dced5048e8b4caaa92e9a6469d069`. Never uninstall; export FULL profiles first.
- Main app source unmerged (v0.4.59); true optical little-menu glass and SF Pro font decisions still separate/unimplemented.

## 2026-11-09 — 0.4.84 Beta signed (animated dock, search focus dismissal, Apple accents), HONOR pending
- Installed 0.4.82 user feedback: search caret/IME persists while interacting with results, selected dock tab return snaps, pink weekend colors in Today/Plan, blue question-mark reference ring. New isolated `feature/navtasks-ux-polish-0.4.84-beta`, source `f3496ee38ce9bdb1948e55a41f4c93f777a2c744`; same-ID Beta code109. Active tab retaps now animate list scroll instead of regenerating tab state; separate smooth tick by each dock tab, Today both axes and local current date; cross-tab animation unchanged. Shared result gesture dismisses focus/IME without consuming interactions or erasing query. Both Today and Plan weekends + calendar incomplete red Day #FF3B30/Night #FF453A; reference '?' glyph/ring orange Day #FF8D28/Night #FF9230.
- CI 42505065303 SUCCESS full tests, assemble, APK signature, release https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.84-animated-dock-verified ZIP 12,628,769B SHA256 `e352b506d6fb7028d3e65f8d7297611b07ba9eff2c0928888420d73df44ccff0`. First run failed only stale `CalendarVisualRulesTest` old pink assertions; corrected before final release. Real HONOR acceptance pending. Main app source unchanged.
- Optional identical pre-update v0.4.82 code + only version bump v0.4.85/code110 safe recovery CI42504767028 SUCCESS https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.85-ux-polish-recovery. Never uninstall or clear data. Optical dock geometry, color palette Day02/Night09, all stores/business behavior, notes and font unchanged.

## 2026-11-09 — Smart reminder 0.4.86 Beta signed; HONOR acceptance pending
- User approved default reminder clock for "due by end of day" tasks separate from actual overdue deadline, explicit task time priority, per-task enable/interval, grouped same-instant Task/AMOS/Services notification, independent missed-task digest. Previously chosen digest clock seeds new default clock on upgrade; prefs keys `reminder_hour`, `reminder_minute`, `group_reminders`. End-of-day overdue stays after 23:59, untouched TaskStore logic. Existing AMOS/Service lead-warning windows preserved, reference time replaces their 09:00.
- `feature/navtasks-smart-reminders-0.4.86-beta`, release source `7ca695a08822bc3e90ab5af8e0686a8ce7d7a350`, code111. Full CI 42509116834 SUCCESS signed ZIP 12,638,673 B SHA256 `be8446f52792900af9bb8167013c578d500888b58e89ebed552d19db378b6f64`: https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.86-smart-reminders-verified. Android HONOR physical delivery not yet tested. Main source unchanged.
- Optional exact prior source recovery v0.4.87 code112 CI42508828509 SUCCESS https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.87-smart-reminders-recovery, same signing and profile data. Never uninstall; export FULL profiles first. Future code >=113 if recovery installed.

## 2026-11-09 — native single notification icon 0.4.88 signed, HONOR acceptance pending
- Following 0.4.86, user screenshot shows HONOR showing two unlike notification icons: original custom `navtasks_notification` traced symbol and a manually attached launcher `LargeIcon`. Android's template moves small to right when a large icon exists. 0.4.88 now uses `setSmallIcon(R.drawable.navtasks_icon_monochrome)`, EXACT original launcher monochrome XML resource, with no `setLargeIcon`. No changes to launcher icon or notification scheduling/data. The system controls monochrome rendering.
- Branch `feature/navtasks-native-notification-icon-0.4.88-beta` SHA `af717226fe6f3f9ad2308ac66c6e4e4ac00ee7dd`, signed same-package code113, CI42545659634 SUCCESS https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.88-native-icon-verified ZIP 12,637,798B SHA256 `2dc09c3999ba239ea5f44dee59f4bddd84fca51d43c181c2df49de8864ebe59d`; real HONOR alert review pending. Main source unchanged.

## 2026-11-09 — Native notification logo 0.4.88 Beta signed; HONOR acceptance pending
- HONOR user noted oversized colored app icon and duplicate small custom drawn badge on NavTasks notifications. Root cause was optional LargeIcon from manually rasterized launcher + independent hand-traced SmallIcon. Fixed in 0.4.88 code113: remove LargeIcon entirely, SmallIcon now directly reuses `R.drawable.navtasks_icon_monochrome` from official adaptive launcher asset. Android draws a single standard app notification icon, style/tint system-controlled. Launcher image and optics unmodified.
- Verified source `feature/navtasks-native-notification-icon-0.4.88-beta` SHA `af717226fe6f3f9ad2308ac66c6e4e4ac00ee7dd`; CI42545659634 SUCCESS. Release https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.88-native-icon-verified ZIP 12,637,798B SHA256 `2dc09c3999ba239ea5f44dee59f4bddd84fca51d43c181c2df49de8864ebe59d`. Android/HONOR physical verification pending; main app code unchanged.

## 2026-11-09 — Services AMOS request badge Soft Orange v0.4.90 Beta, device acceptance pending
- Selected user's option 1 for AMOS request buttons in Services only: Day peach `#FFF1DE`/border `#F4C48B`/text `#AE5800`; Night warm dark graphite `#403227`/border `#74583C`/text `#FFBF79`. New `ServiceAmosRequestButton` in `ServicesUi.kt` only; original request editor callback, actions, 44dp hit zone, filters, all data/business logic and Liquid Glass dock unchanged.
- Signed branch `feature/navtasks-amos-badge-peach-0.4.90-beta` SHA `90c9b693295a7b95304edac63e70e0979b939765`; code115, CI42556397343 SUCCESS, https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.90-amos-peach-verified ZIP 12,640,865B SHA256 `a431855f096d05e38bde36d4197df9546ea430aa1e3c9dad5727c0ecc331df40`. Physical HONOR Day/Night approval pending.
- Optional exact previous v0.4.88 source recovery v0.4.91 code116 CI42556403359 SUCCESS https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.91-amos-peach-recovery. Only install if current visuals rejected, always backup FULL profiles; never uninstall.
- Main app source not merged. Next test normal filled AMOS request badge, empty request badge, both themes and preserved edit on tap. Current service request logic remains unchanged.

## 2026-11-09 — 0.4.92 service cycle dates, request badge and notification icon verified by CI; HONOR pending
- Source: isolated `feature/navtasks-service-date-amos-icon-0.4.92-beta` at `b7242279aafa8d2c2c4ce5832f5952361419cac7`, same signed Beta package versionCode117. Base/backup exact 0.4.90 `90c9b693295a7b95304edac63e70e0979b939765`. Main app source remains 0.4.59, docs only.
- Fix planned editor's fictitious lastDate (next.minusDays(1)) by letting user select real last-service date; period now computes `last.plusMonths(periodMonths)` including 60mo -> five years. Manual next-date selection remains supported; no automatic changes to previously saved incorrect dates and no schema changes. Request-only unplanned dates and history unchanged.
- Missing AMOS number: transparent badge with only amber outline (label neutral). Existing AMOS number: original Soft Orange filled material. Dedicated notification-only 24dp white NavTasks stacked-card/check vector replaces translucent launcher monochrome icon in SmallIcon; still no LargeIcon. Launcher/Dock untouched.
- CI https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42561492881 SUCCESS tests/compile/signature/ZIP; https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.92-service-dates-icon-verified, 12,643,114B signed ZIP. Optional same-ID code118 0.4.93 **exact old 0.4.90 app code** recovery CI42561496089 SUCCESS at https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.93-service-date-icon-recovery. No uninstall, preserve profiles.
- Await HONOR screenshots/data validation: re-edit already wrong Gyro Sphere #1 record to actual 2024-04-25 + 60mo -> 2029-04-25; creation of new planned record; manual due override; Day/Night AMOS badges; NEW notification icon in status/lock/shade; app reminders, other modules and profile persistence unaffected. Phone acceptance not established.

## 2026-11-09 — 0.4.94 Beta R2 signed/CI verified; device acceptance pending

- Latest functional candidate `feature/navtasks-notes-visual-0.4.94-beta` SHA `0c96a9aaec41765feee46ae1dd8ba3f195253217`, code119, https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.94-notes-polish-r2-verified; CI https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42615079776 SUCCESS full JUnit, assemble, signed APK package/version checks and ZIP integrity. Earlier R1 release is superseded (Handover note delete was unavailable after editing change); R2 restores it in the separate note actions dialog with confirmation. Stable main app source remains version0.4.59, docs-only.
- Personal profile `Личное` and vessel Handover share `HandoverFullNoteEditor`: full-screen note title, RichText content with bottom format toolbar, explicit Save then close, Back cancels without saving; no new store/schema/profile migration. Tap entry note to edit, 44dp More for original complete/date, strike/reopen/delete.
- Removed hard max-height from displayed rich comments on Services, AMOS WorkOrders and Handover cards; content now fully measures rather than vanishing below 46–50dp. Compact editors elsewhere are unchanged.
- Plan outcome dot now means completed/partial/missed only; scheduled-only and AMOS/Service presence no longer draw unexplained blue dots. Day agenda still lists existing entries. Year scheduled-only blue coloring toned down, weekend/system red retained.
- Services: last service uses success green, next regular due uses accessible Apple-blue text; soon uses warning orange, overdue danger red. Unplanned AMOS date presentation unchanged.
- Notification only: refined 24dp transparent monochrome task-card/check vector with system blue accent hint, no LargeIcon duplication. OS chooses actual notification tile/background/tint; full white/blue/grey branding cannot be guaranteed from a SmallIcon.
- Safe higher-code recovery `0.4.95-beta` code120 is exact older 0.4.92 app source except version, SHA `f8b83519b4d2b673def9d1dc43851699a38c3cc8`; signed CI https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42614534271 SUCCESS, https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.95-notes-recovery. Export FULL profiles; no uninstall.
- Pending visual/input acceptance from HONOR: Personal and Handover rich formatting/edit/delete, long text overflow and keyboard, Calendar status semantics, Services warning colors, newly emitted notification icon, data preservation and dock. Do not call phone-tested until user confirms.

## 2026-11-09 — notification identity A/B/C diagnostic v0.4.96, signed CI passed; HONOR pending

- User rejected blue-grey MagicOS notification icon shown by 0.4.94; compared other apps with multicolored icons. System SmallIcon uses monochrome alpha/tint and `Notification.Builder.setColor` is not a custom full-color app tile. The OS owns the notification view; avoid blanket custom layouts and speculative repaint.
- Protected accepted candidate v0.4.94 R2; new branch `feature/navtasks-notification-icon-lab-0.4.96-beta`, source `13b68b5e38fafa2edbce4de10f972c4e49186f4c`, code121. A dedicated `NavNotificationIconLab` sends A (monochrome SmallIcon), B (transparent colored SmallIcon), C (branded white/gray/blue LargeIcon with normal SmallIcon), from a single Settings test button. User can clear the three tagged IDs. Isolated channel/notifications do not change real notification builders, scheduling, data, preferences, other screens, visual dock or launcher.
- GitHub Actions https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42624280583 **SUCCESS** full JUnit/app assemble/APK signed identity and ZIP integrity. https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.96-icon-lab-verified signed ZIP `NavTasks_Notification_Icon_Lab_0.4.96_Beta_Update.zip` size 12,659,518 bytes. Initial failed CI42623795142 was repaired: framework Notification.Builder does not expose Compat-only setSilent; now uses LOW-importance test channel. No failed candidate was released.
- Safe in-place recovery 0.4.97-beta code122 SHA `37fcb530f8a3f30f25aa7cb2561d0a8c73c351ca` uses exact previous 0.4.94 R2 application sources plus only Gradle version bump; CI42623919524 SUCCESS, https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.97-icon-lab-recovery. Never uninstall or clear user data, export full profiles first. main still v0.4.59 app.
- Await actual HONOR screenshot of A/B/C from notification shade, optionally repeat in MagicOS 11 Multicolor icon style if supported. Only after device comparison select production icon, avoid duplicate LargeIcon on user alerts. If recovery installed, future same-package versionCode >=123.

## 2026-11-10 — latest validated in-place Beta is 0.4.98/code123; physical reminder pending

- Physical HONOR screenshot of isolated 0.4.96 A/B/C: blue transparent-color `SmallIcon` represented application at left; full-color `LargeIcon` from variant C appears at right only in expanded notification, not as the primary app identity. Decision to promote only the **tested B resource**, not LargeIcon and not a speculative solid-background small icon.
- `0.4.98-beta` source SHA `0e1df2acb24e879554a02f3133bcac510d380981` branch `feature/navtasks-notification-color-0.4.98-beta` is based directly on signed 0.4.94 R2. Only `NavNotifications.kt` small-icon resource, one color vector, `build.gradle.kts` and feature CI workflow change. No A/B/C test screen; no changes to notification scheduling, task/AMOS/service/profile stores, full-screen Personal/Handover rich editor, full-height comments, calendar or dock. Signed CI42638360527 SUCCESS, release https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.98-color-notification-icon-verified, in-place versionCode123 and same app ID/signature. Needs real HONOR production notification screenshot before acceptance.
- Safe rollback `0.4.99-beta` code124 source SHA `b863a414f87fe2b3cabed315d3dbafcf4bbe108b` is exact 0.4.94 R2 application code (just version bump), signed CI42638428451 SUCCESS, https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.99-notification-recovery. If installed requires next app code>=125. Export FULL profiles first, never uninstall.
- Stable main app code still 0.4.59. Independent parallel test branches with same 0.4.98 number must not be conflated with production B candidate.

## 2026-11-10 — D full-color SmallIcon experiment released; no device approval yet
- The B colored SmallIcon displayed in blue at left, but did not prove a full-color *tile*. C branded LargeIcon is right-hand when expanded. New D-only test from previous v0.4.96 specifically supplies a **complete color tile as SmallIcon** and omits LargeIcon; old A/B/C can be restored with another button.
- D branch `feature/navtasks-notification-icon-d-0.4.98-beta` SHA `e31b90e6e53c44cecd25df8838b1acd9c611c84e`, signed CI42638778449 SUCCESS https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.98-icon-d-verified, ZIP 12,659,527 bytes, code123. Real notification system `NavNotifications.kt`, scheduling/profiles/dock unchanged. CAUTION: another independent *production B* release also bears 0.4.98/code123; do not confuse by version.
- Recovery version 0.4.99/code124 exact prior 0.4.96 code (version bump only), SHA `628c1e5a7d70f534148ff318c8248ecc7c214464` signed CI42638844435 SUCCESS https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.99-icon-d-recovery. Pending user's HONOR screenshot of D and explicit approval before assigning to production.

## 2026-11-10 — 0.4.100 single-icon HONOR diagnostic signed, real rendering pending
- Screenshot confirmed v0.4.96 A/B/C previews auto-group; C LargeIcon is on right of expanded row as expected; the header's blue B group icon does not prove independent B/C full-color tiles. Keep previously signed v0.4.98/code123 **production B** candidate separate: https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.98-color-notification-icon-verified.
- One-at-a-time A/B/D/E test in v0.4.100/code125 on separate branch `feature/navtasks-notification-single-icon-0.4.98-beta` at SHA `f1641add4e0045cd101e92404be98edbf258a19f`: A old mono, B blue outline, D white/Slate Mist/Apple blue full-color vector supplied as SmallIcon, E adaptive launcher icon supplied as SmallIcon, no LargeIcon on these four. Settings produces only ONE temporary notification at a time with buttons. The actual NavNotifications.kt SHA `869e614a8bfcfa11b85db39462cbd67c92c69bd6`, state stores, reminders, AMOS/Services and dock optical sources are untouched.
- CI42638976702 SUCCESS signed APK/ZIP https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.100-single-icon-lab-verified. Required HONOR D/E individual screenshots to choose which method actually works; Android framework may mask/tint/replace either resource. Stable main remains app v0.4.59 (only docs).
- A safety restore from exact v0.4.96 application code with versionCode126 is prepared as 0.4.101 `feature/navtasks-0.4.101-single-icon-recovery` SHA `399ff0ba14d9b5e4e9697eedabcd62c419a95f12`; CI42639230106 SUCCESS; signed release https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.101-single-icon-recovery before recommending rollback. Never uninstall or clear data. If rollback code126 installed, subsequent code >=127.

## 2026-11-10 — v0.4.102 / code127 HONOR full-color adaptive notification icon (CI VERIFIED, HONOR pending)

- User explicitly wants a full-color app tile on the left, as in Amnezia, after A/B/C screenshots. C appeared on the right because it used `LargeIcon`; B colored contour was not the requested full tile. Parallel experiment 0.4.100/code125 one-at-a-time D/E remains separate, not conflated with the current test.
- Current signed candidate https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.102-honor-adaptive-icon-verified, SHA `2a503734f6718dbbb0fbaf71ebad7da3634e8610`, branch `feature/navtasks-full-color-notification-0.4.98-beta`; full GitHub Actions CI42642525997 SUCCESS; package `com.navtasks.app.glass`, versionName 0.4.102-beta/versionCode127, same signing key. HONOR only: `navtasks_notification_adaptive` is a real `AdaptiveIconDrawable` as production `SmallIcon` with white background, gray cards, Apple Blue borders and check. No `LargeIcon`. Other manufacturers retain previous tested mono small icon. New Settings one-shot preview uses the SAME production builder, replaces grouped A/B/C test; preview can be cleared. All schedules/data/optical dock unchanged.
- Device result not yet available: Android/OEM may mask/tint despite AOSP adaptive-icon exception. Ask user screenshot to compare with Amnezia before accepting.
- Recovery https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.103-adaptive-icon-recovery, code128/name0.4.103, SHA `13f071ec799829fc440d3efc9459257340b0ee79`, exact v0.4.96 application code except Gradle version; signed CI42642766235 SUCCESS. No uninstall/clear, backup FULL profiles. main app code still 0.4.59.

## 2026-11-10 — Mobile 1.0.0 scope frozen; 0.4.104 dock press Beta CI success, device pending

- Verified source `4e8d737854d5625271ca6665cd35e009dd5ac828`, branch `feature/navtasks-dock-press-0.4.104-beta`, signed 0.4.104-beta/code129, same package/signing key, CI42653872788 SUCCESS: tests, compiled APK, verified signature/package/version and ZIP https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.104-dock-press-verified (12,660,787 bytes). Backup exact v0.4.102 original `backup/navtasks-0.4.102-before-dock-press`.
- Single selected glass capsule now responds to pressed tab through shared interaction sources. Held press 1.15x (no timeout), moving 1.30x, idle 1x; press on another tab moves lens to finger then on release navigates as before. Original icons, backdrop refraction, matte, 56dp geometry, selection callbacks, Back/swipes and all stores unchanged. Pure JUnit covers scale state and dimensions; physical HONOR hold/cancel/edge/light/dark tests outstanding.
- Higher-code safety recovery `0.4.105-beta` code130 is exactly v0.4.102 application sources with version bump, commit `8ded1a842e5a866f69ce3a976e4f30e8d09237e1`, CI42654015439 SUCCESS, https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.105-dock-press-recovery. Do not uninstall. Future code>=131 if recovery installed.
- User-approved **before 1.0.0**: Work hub contains Tasks + AMOS + Services; Today Calendar bottom-left and new Task (+) bottom-right; same editor and four frequency options; replace dock Tasks tab with Reference, SIRE 2.0 and archival VIQ7 **placeholders only**, no PDF/contents yet. Final UI, profiles, widgets, notifications, release audit afterward. Reference content, manuals, SIRE questions and Windows PC client/sync **after 1.0.0**. Move UI only, preserve TaskStore and frequency/history/import/export.
- Prior HONOR notification preview: left color tile is now functional, user finds it not quite premium but deprioritized compared with 1.0.0. main application v0.4.59 unchanged; docs-only.

## 2026-11-10 — 0.4.106 dock responsiveness, CI success; HONOR acceptance pending

- Follow-up on user report that 0.4.104 lens moved with a delay: state bridge `clickedTab` with `remember(selected)` removes pointer-release -> onClick -> parent-selection transient target reversal; `lensTargetIndex` prefers pressed/committed/selected. Position+scale springs faster (1100 press/800 rest), remain critically damped, keep 1.15x held/1.30x travel, previous shader and 56dp geometry. Only two dock sources, tests and version/build changed; all tasks/AMOS/services/data/icon/notifications untouched.
- Feature source `feature/navtasks-dock-response-0.4.106-beta` commit `b553603d5891d79920f3d140c15cb590bab2b63b`; versionName `0.4.106-beta` / versionCode **131**, same app package/signing. GitHub Actions https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42657916308 **SUCCESS**: full JUnit, assemble, signed APK/version/package and ZIP integrity. Signed https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.106-dock-response-verified with `NavTasks_Dock_Response_0.4.106_Beta_Update.zip`, 12,661,108 bytes. Device HONOR gesture/visual acceptance PENDING.
- Safe in-place recovery branch `feature/navtasks-0.4.107-dock-response-recovery` SHA `eb47d72b63987b328a282e27d5ee7548f66aab3c`, exact previous 0.4.104 application source except Gradle version bumped to 0.4.107/code132 plus CI workflow. Action https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42658060669 status **SUCCESS** as of documentation write; signed release https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.107-dock-response-recovery. Never uninstall, clear data or overwrite profile stores. After installing code132 future updates must use code>=133.

## 2026-11-10 — 0.4.108 signed; restored classic dock movement, HONOR approval pending

- User explicitly rejected 0.4.104–0.4.106 held-lens magnification and fast/non-smooth movement. From exact accepted v0.4.102 source, restore `NavLiquidGlassDock.kt` 550f slide spring, 600f moving stretch, 700f matte, original selected-only travel and 1.30x transit. No pressedTab/isHolding/clickedTab retarget logic. Add only a subtle 1.06x retap-selected lens pulse after onClick (85ms/155ms Animatable); long hold never expands lens. Preserve 56dp dock, 52dp resting lens, shader, haptic/button actions, data and all store source files.
- Candidate 0.4.108-beta/code133 SHA `9c7145e53959cffc11f0e8500c6c6ca317ef9252`, CI42660693061 SUCCESS full signed APK/ZIP tests, https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.108-classic-glass-retap-verified. Device acceptance pending. Protected original `backup/navtasks-0.4.102-original-smooth-dock`.
- Recovery exact old v0.4.102 application source 0.4.109-beta/code134 SHA `1053c4731333f0db7d403aaab84505722c45a845`, CI42660907817 SUCCESS https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.109-classic-dock-recovery, no pulse, all previous functionality untouched. Never uninstall/clear storage. If recovery installed, next code>=135. main app source still v0.4.59.

## 2026-11-10 — 0.4.110 signed; Work > Routine tasks entry, device acceptance pending

- User accepted exact original classic dock animation v0.4.108; freeze Liquid Glass code. Initial navigation stage 0.4.110-beta/code135 commit `1e33de3c3479caff39c28363b88cc477e56ea6d9` branch `feature/navtasks-work-routines-0.4.110-beta`. GitHub Actions https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42663503311 SUCCESS, signed APK/ZIP and package/version verified, https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.110-routines-in-work-verified, 12,670,340B. Source backup accepted v0.4.108 `backup/navtasks-0.4.108-before-routines-in-work`.
- Work hub presents Routine tasks tile before AMOS and Services, count of original TaskStore templates. Opens `RoutinesModuleScreen`: full screen with Back, original `TasksScreenV2` four categories, original task editor and floating Add, profile-specific saveable state preserves scroll across editor. Old Tasks tab temporarily retained so user can verify both routes show identical data before changing the dock.
- Business stores and exports/imports unchanged; TaskStore, PlanAndTasksUi, original dock, notifications, AMOS, Services, Handover untouched. The only modified app files are `NavTasksUi.kt`, `WorksHubUi.kt`, new `RoutinesModuleScreen.kt`, Gradle version and CI.
- Safe version-only rollback 0.4.111/code136 SHA `780f29ff23f56256378adaa2c7015df776a31aa1`, CI42663232108 SUCCESS, https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.111-work-routines-recovery; exact v0.4.108 app and no uninstall. If fallback installed later versionCode>=137. Next: HONOR tests and then Today Calendar left/Add right + replace fourth dock with Reference SIRE2.0 and VIQ7 placeholders only; no content/desktop until after stable 1.0.0.

## 2026-11-10 — 0.4.112 Beta Today actions + Reference shell, signed CI SUCCESS, HONOR pending

- Source `b38e6f75fde7a51c92a865dd441a6afed5be81b8` branch `feature/navtasks-reference-today-actions-0.4.112-beta` from accepted 0.4.110 backed up. `0.4.112-beta` versionCode137, same signed `com.navtasks.app.glass`. Full JUnit, assembled APK, signature/version/ZIP integrity CI42668433844 SUCCESS; https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.112-reference-today-verified, 12,676,558B.
- Today glass Calendar bottom LEFT, Add task bottom RIGHT, same 54dp components and backdrop in dark/light and Haze fallback. Add uses existing TaskEditor; frequency chooser `День / Неделя / Месяц / Разовая` appears ABOVE title (presentation only, same Frequency enums/logic).
- Fourth dock tab changed to Reference, with new thin custom book glyph and SIRE2.0 + VIQ7 (archive) non-interactive placeholder cards, no inspection data. Original Tasks list remains via Work > Routine tasks. Removed old inert Inspections placeholder from Works; no stored data touched. Protected 0.4.108 dock springs/matte/optics and all stores/notifications unchanged. Saved tab3 UI reset key changed to reference to avoid Tasks scroll collision.
- Expected old `NavLiquidGlassParityTest` glyph-order assertion `tasks` failed two unshipped CI attempts; corrected to `reference` without loosening numerical optics checks. New `ReferenceNavigationTest`. Final run all tests pass, no prior failed build published.
- Safe in-place rollback exactly previous 0.4.110 app sources, v0.4.113/code138 commit `b27623c946490ba0ba2e48e8f9f842f627401f89` CI42667375817 SUCCESS https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.113-reference-recovery; no uninstall/clear. Device acceptance pending; full pre-1.0.0 regression next. Reference content and Windows desktop/sync postponed.

## 2026-11-10 — 0.4.114 safe bottom scrolling for Today/Handover

- User screenshots exposed bottom interaction bug in 0.4.112: final Today checklist card overlaps left Calendar, Handover last section overlaps floating Add. In main tabs, edge-to-edge content's old 125/126dp bottom content padding failed to account for 54dp floating action at 83dp from gesture-safe edge plus Android navigation bar inset. Standalone Handover/Personal uses navigationBarsPadding parent, but old 32dp bottom padding failed to clear its Add button at 18dp.
- Feature `feature/navtasks-bottom-safe-area-0.4.114-beta` SHA `01eb9043c853e59f48eef159ec9ff5c23c5e3d04`, version 0.4.114-beta/code139. `NavFloatingActionSafeArea.kt` computes actual tab bottom content padding = system navigationBars inset + 83+54+20dp, standalone =18+54+20dp. Only changed TodayScreenV2 and HandoverScreen LazyColumn `contentPadding.bottom`; floating buttons, dock, optical glass and business logic unchanged. Added pure unit tests incl. several inset sizes and existing other modules' clearances. Protected Base0.4.112 `backup/navtasks-0.4.112-before-bottom-safe-area`. GitHub CI42673108761 **SUCCESS** signed JUnit/assemble/APK identity+signature/ZIP integrity https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.114-bottom-safe-area-verified size12,678,463B. HONOR real interaction check pending; CI tests geometry logic only.
- First safety-guard CI attempts failed before build due stale expected HandoverUi SHA; fixed guard to verify controlled layout and all protected stores, no bad APK released. Safe exact prior 0.4.112 source in-place recovery 0.4.115/code140 SHA `ec3addbd4e56039991c593e570f4e25db8d89953`, CI42672965626 SUCCESS https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.115-bottom-safe-area-recovery. No uninstall/clear, export profiles. main app code still 0.4.59.

## 2026-11-10 — Source audit and two signed repair stages before 1.0.0

- Current signed acceptance candidate **0.4.118-beta/code143**, branch `feature/navtasks-notification-widget-0.4.118-beta`, SHA `6ee8978d27ce52ce75e12794d05f3886d4652f9e`, GitHub Actions 42742957197 SUCCESS https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.118-notification-widget-verified; physical HONOR and Samsung acceptance not yet run. Isolated, not merged into main app code, so main still v0.4.59.
- First verified stage 0.4.116-beta/code141 source `928398730d0f414a63f96abb1f306662f0f893bb` CI42741799999 SUCCESS https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.116-data-guard-verified: strict JSON decoder and read-only protection on malformed task/runs/Handover data; guarded full profile export/import and tests. No model schema migration.
- Second stage inherits guard, fixes closed AMOS/Services scheduled/grouped/delivered alerts, independent widget next-day roll when notifications OFF, exact-alarm permission-granted broadcast, edge-to-edge bottom content clearance for last AMOS/Service cards, removes stale Settings build number; tests.
- Safe exact previous app source recovery 0.4.119-beta/code144 SHA `d93536f525a8c934b3bb2ae90e3a7e5547a8d0e7` CI42743079436 SUCCESS https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.119-pre-release-recovery, use only in case of failure, in-place after full profile backup. Next versionCode >=145 if rollback installed.
- Remaining release gates detailed in `docs/development/RELEASE_READINESS_AUDIT_2026-11-10.md`: in-place stable package/signature, profile-targeted notification deep links, privacy/Auto Backup policy, load/stress/performance tests, real-device UI and OEM tests. No new features until 1.0.0. Do not modify dock or app data schema.

## 2026-11-10 — 0.4.120 signed deep-link Beta and 0.4.121 safe fallback
- User reports installed 0.4.118 stable in routine use, but notification/widget edge cases not comprehensively tested.
- 0.4.120-beta/code145 source `ff674baee245ba1fc85902da8d3238b220f05aba`, branch `feature/navtasks-profile-deeplinks-0.4.120-beta`, CI42750924441 SUCCESS, signed https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.120-profile-deeplinks-verified.
- Single immutable request from MainActivity, profile resolution before destination lookup, shared Quick Profile store-switch path, safe handling of non-existing profiles, explicit per-tap serial, all Today/Task/AMOS/Missed widget routes. Kept production notification builders, widget update code, TaskStore/ServiceStore/WorkOrderStore/ProfileStore storage and accepted Glass motion identical to v0.4.118. JVM route tests only; **not** proof of HONOR cold/warm behavior.
- Exact v0.4.118 application recovery as 0.4.121-beta/code146, source `662a8781144c2c092ffa4959f556744fa2634e38`, CI42751054359 SUCCESS and signed https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.121-profile-deeplink-recovery. Full profile export first; install in place, do not uninstall. Following rollback require code>=147.
- `main` remains docs-only with stable older app source v0.4.59. Release 1.0.0 packaging and privacy choices are unresolved. Next: real-device deep-link/profile test, then independent production release build strategy and full HONOR/Samsung regression. No SIRE/VIQ material or Windows/sync before mobile 1.0.0.

## 2026-11-10 — signed Handover notebook 0.4.122 and popup frost pilot 0.4.124
- User requested pre-v1.0 polish after preliminary 0.4.118 stability and reviewed an actual HONOR Handover screenshot. Signed candidate 0.4.122-beta/code147 source `25449f02983e97050190745b38e9636816c41ad3`, CI42757080882 SUCCESS https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.122-handover-ux-verified; Handover entries act as reading/editing notes only (no visible completion / More), title and expanded text, delete safely inside full-screen editor, stored legacy completion status preserved; Today Completed amber like Tasks info label; Settings preview icon button removed; subtle glass card and floating button press response, core glass dock untouched. Initial compile regressions were caught and resolved BEFORE release.
- Exact 0.4.120 app fallback 0.4.123-beta/code148 SHA `64669fe4e0f87ec3c8bdfa088df952c9fd3d8762`, CI42756524945 SUCCESS, signed https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.123-handover-ux-recovery.
- Independent 0.4.124-beta/code149 native Android API31+ dialog frosted-glass preview SHA `e478e4d0c8e5794382f7755da8ab63f5f9e7ed30`, CI42757168394 SUCCESS https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.124-popup-frost-verified. Small modal dialogs and quick profile get system window-background blur and NavTasks rim/card when OEM permits, opaque fallback otherwise; native dropdown menus remain original because cross-window Liquid Glass refraction is not correct. This is NOT the optical dock engine. Protected original v0.4.122 at `backup/navtasks-0.4.122-before-popup-blur`.
- Higher-code exact v0.4.122 application recovery 0.4.125/code150 branch `feature/navtasks-0.4.125-popup-recovery` SHA `8c075fd9f9febd26a64f0e28292cb8b77aa9b7a1`; CI42757281433 pending final verification at documentation time.
- Neither v0.4.122 nor v0.4.124 has been physically accepted on HONOR/Samsung. Main app source remains 0.4.59. Next test both themes, long notes/delete, all controls and popup blur on HONOR, confirm 0.4.125 rollback; then original release gates package/signature, privacy and stress/reliability tests. No SIRE/VIQ content or Windows/sync before 1.0.0.

## Final verified 0.4.125 recovery status — 2026-11-10

- **SIGNED CI SUCCESS** https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42757281433 for exact v0.4.122 application-source recovery v0.4.125-beta / versionCode150, source `8c075fd9f9febd26a64f0e28292cb8b77aa9b7a1`.
- Official verified recovery release: https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.125-popup-recovery, artifact `NavTasks_Original_0.4.122_Restore_0.4.125_Beta_Update.zip` (12,670,215 bytes), same installed package and Beta key. This rollback is ONLY for rejection of experimental popup blur in v0.4.124; it retains Handover UI/press/Today/Settings improvements of v0.4.122.
- User must export FULL profiles before updating. Install IN PLACE; NEVER uninstall or clear app storage. If recovery code150 installed, any future same-package update needs **versionCode >=151**. Never confuse latest numeric recovery 0.4.125 with newest functional candidate 0.4.124/code149.
- Device HONOR/Samsung visual/interaction acceptance still pending. No v1.0.0 published and no `main` application source merge.

## 2026-11-10 — Latest signed UI candidate v0.4.126/code151; HONOR review pending
- Source `7ebb2ba2be24925c9d507be4f9d5acb040c559b1` on isolated `feature/navtasks-ui-polish-0.4.126-beta`, signed CI42763420520 SUCCESS, https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.126-ui-polish-verified. Based on 0.4.124 popup pilot backed up before modifications. Existing accepted 0.4.108 dock optics/spring and all business/data stores unchanged.
- Handover editor note starts closer to title, Delete at centered bottom; Work and Reference info banners in warm Today Completed palette; Handover section label orange; non-dock clickables get 125ms short-tap 1.04/1.075 scale feedback; Handover Add now parent-overlay, avoiding nested tab reassembly.
- Exact 0.4.124 application recovery signed as 0.4.127/code152, CI42763533651 SUCCESS, https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.127-ui-polish-recovery. Install only if new candidate rejected, always full profile backup and no uninstall/wipe.
- Unresolved OEM popup real glass: 0.4.124 uses optional native Window background blur with opaque fallback, not dock lens. User reports visually no glass on HONOR; no claim of a fix. Physical HONOR/Samsung tests, package/signature strategy, Android Auto Backup/privacy and final 1.0.0 readiness pending.

## 2026-11-10 — Latest functional candidate 0.4.128/code153
- Signed CI42768009539 SUCCESS. Source `31e44f0b7e39759744bb09a1dc5a7e77f638dc86` on `feature/navtasks-pressed-glass-0.4.128-beta`. Verified release https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.128-pressed-glass-verified.
- Whole visual glass surface/rim now scale together: outermost `Modifier.graphicsLayer().then(this)` in `glassClickable`, preserving prior 0.4.126 short pulse and immediate onClick. Handover section amber info-card, AMOS distinctive work-order pictogram; all Task/Handover/AMOS/Service/Profile stores and accepted dock optics unchanged. HONOR acceptance **PENDING**; CI success is not physical proof.
- Exact preceding 0.4.126 app higher-version recovery as 0.4.129/code154 SHA `ac4e6a154c84f47091326a837751e45cddab1c7d`, CI42768087904 SUCCESS, https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.129-pressed-glass-recovery. Full profile export + install over current APK only on rejection. Next versionCode>=155 after recovery.
- Popup native frost on HONOR remains visually inconclusive; no claim of real dock-equivalent refraction. Release 1.0.0 not yet ready; same-ID release signing, privacy/backup, HONOR/Samsung regression pending.

## 2026-11-11 — Functional 0.4.130 Beta/code155 (signed CI; device approval pending)
- From 0.4.128 baseline (saved at `backup/navtasks-0.4.128-before-crew-reference-font`) commit `1ef9cc4a568157007deee265d54d5830485989ff`, signed CI42839714152 SUCCESS, release https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.130-crew-reference-notes-verified.
- AMOS work and crew checks share existing WorkOrderStore: module UI title `AMOS / By Crew`, clearer description/search/card/history/editor strings; NO model/type/classification field was added and no data/logic altered. Full-page Handover editor body 17sp, 25sp line, compact comments remain 14sp, 20sp. Reference adds local search over two empty SIRE2.0/VIQ7 placeholder cards only (no PDF/index/content).
- Exact 0.4.128 application fallback as signed 0.4.131/code156 `bdec8647813f420f9af4825f0609990c82819751`, CI42839880090 SUCCESS https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.131-crew-reference-recovery. Use only if 0.4.130 regression, in-place with FULL profile export, no uninstall. If installed, future code>=157.
- Device HONOR acceptance, Samsung regression, nondebuggable same-package/signing, Android Auto Backup privacy, and real glass popup exploration are **OPEN**. Main application code remains unchanged (old main baseline); release 1.0.0 NOT yet ready.

## 2026-11-11 — newest functional Beta 0.4.132/code157: guarded pull-to-search
- Source `e1781fd712da5457595bd2909713a7beb7fb0b9c` on `feature/navtasks-pull-search-0.4.132-beta` based on signed 0.4.130; CI42845269986 SUCCESS and release https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.132-pull-to-search-verified. Failed first CI42844541870 (unsupported import) was fixed and never released.
- Shared non-consuming passive gesture for Handover, AMOS / By Crew, Equipment Services and Reference LazyColumns only: starts already settled at absolute top after >=200ms rest, first meaningful movement downward, >=90dp pull, >=150ms duration, <=45% horizontal deviation, one finger, release at top. No trigger during inertia or same gesture reaching top from further down. Existing search action/text focus untouched. Pure JVM predicate tests; real device behavior not yet user-accepted.
- Exact 0.4.130 app recovery as 0.4.133/code158 signed CI42844676421 SUCCESS https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.133-pull-search-recovery. Use only if 0.4.132 rejected, full-profile export and in-place, no uninstall. After recovery next versionCode>=159.
- Old business/data stores, notification/widget code, accepted dock/refraction, and 0.4.130 crew works, full-page notes 17sp and Reference placeholder search retained.
- Still release blockers: HONOR physical gesture QA, Samsung, final non-debuggable signed SAME package/key, Android Auto Backup privacy, full regression; true Glass popup effect unresolved. `main` app source remains old, docs-only.

## 2026-11-11 — Latest functional signed candidate 0.4.134/code159
- User approved overall 0.4.132 pull-down Search but requested softer field animation and keyboard -> Search -> page Back order.
- 0.4.134 commit `c51a321edb335ac2d53ad5db693ddef8c909517b` from 0.4.132 on `feature/navtasks-search-back-motion-0.4.134-beta`; signed CI42849635847 SUCCESS, https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.134-search-back-motion-verified. Shared NavAnimatedSearchField (260ms reveal/230ms hide; fade), BackHandler only when Search open and IME hidden; original magnifier, auto-focus, 0.4.132 settled-top gesture, filtering and stores untouched. Physical HONOR test pending.
- Exact previous 0.4.132 application source fallback as 0.4.135/code160, SHA `765eef00fa1b8a95d7295f5c65e3c7c0f9ffd183`, CI42849425894 SUCCESS https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.135-search-back-recovery. Use only if rejected; full profile export and in-place update, no uninstall/clear. After fallback future code>=161.
- `main` contains developer docs/old app source; don't build 1.0.0 from main. Pending full HONOR/Samsung Back/IME navigation regression, same-package same-key NON-debuggable signed release, Auto Backup privacy decision, and separate popup true Glass investigation. No new reference contents or Windows before mobile 1.0.0.

## 2026-11-11 — Latest signed functional candidate 0.4.136/code161
- `feature/navtasks-search-results-0.4.136-beta` SHA `8941f93ae2359324dd4b95b8d3e425a95e80a50e` from 0.4.134, CI42855571669 SUCCESS https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.136-search-results-verified. Search now displays orange highlighted substrings on matching cards, a current/total result-CARD counter and up/down wrap navigation. Handover: section titles + nested entry titles and notes; AMOS/Services title-only (same filters), Reference two placeholders only. Saved HTML/rich content unchanged; focused note briefly rendered as marked plain text only in search. Existing search animation, keyboard/Back, pull search unchanged.
- Exact prior 0.4.134 source fallback `feature/navtasks-0.4.137-search-results-recovery`, SHA `ef637cefd0aa7bd4fd0296e409148ac23034c184`, code162, CI42855687113 SUCCESS https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.137-search-results-recovery. Only if candidate fails device testing; full profile export and in-place install, never uninstall.
- HONOR hardware verification needed for keyboard, navigating long nested Handover notes, highlight in light/night, selected filter/count changes and no double-trigger. Main app code unchanged; production 1.0.0 package/signing, Android Auto Backup privacy, full regression and popup true Glass remain open.

## 2026-11-11 — Latest signed functional candidate 0.4.138/code163 (device acceptance pending)
- Candidate `feature/navtasks-occurrence-search-0.4.138-beta`, HEAD `7891159e7f1b0d2399a6b75b3d27e08f3e33e1bb`, signed GitHub CI42861072394 SUCCESS, https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.138-occurrence-search-verified. Auto-name untitled Personal notes from profile sections without store migration; vessel Handover title still required. Handover and Reference search now count per-occurrence including repeats inside one long note, have per-match active orange marker and arrow navigation; Handover uses TextLayoutResult rectangle for intended exact-line BringIntoView. Counter and arrows inside input, query threshold 2 characters on all 4 search screens; AMOS/Services continue title-only card search. No business/dock/notification changes. Physical HONOR verification needed.
- Exact previous 0.4.136 app fallback as signed 0.4.139/code164, SHA `2527c32fa09f96441343e2e83768b4e046bfb30c`, CI42860828496 SUCCESS https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.139-occurrence-recovery. Use only if new build rejected, in place after FULL profile export. Next code>=165 after fallback.
- Stable 1.0.0 not released. HONOR/Samsung QA, same installed package/key nondebuggable signature, Android backup/privacy, data edge-case regression and actual popup optical Glass remain open. Main app source is old and should not be considered current build.

## 2026-11-11 — latest CI-verified 0.4.140/code165, physical HONOR acceptance pending
- `feature/navtasks-personal-search-fix-0.4.140-beta` SHA `e812bdbda1e1bf72d10dbfd6c57f425322f6747d`, signed CI42865643443 SUCCESS, release https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.140-personal-search-fix-verified. Fix notes-only Personal false "profile protected" dialog: TaskStore no longer materializes/decodes/writes task runs in PERSONAL, while strict vessel guards intact. This avoids empty `runs=[]` / no `templates` false corruption from prior TaskStore.refresh(). Saved personal note preferences untouched.
- Handover exact-match navigation now uses LazyListState.animateScrollBy via measured glyph/window/list coordinates instead of propagating BringIntoView to the top window; expected to keep the search field pinned while keyboard stays open. All existing hit counting, colors, auto-named Personal notes, rich HTML, search filtering, IME Back, docks, business data/alarms/widgets unchanged. Needs physical HONOR test.
- Exact prior 0.4.138 source recovery signed as 0.4.141/code166 `f1a7035fadec28ab46a836f151c61dcfcc832fe6` CI42865051674 SUCCESS https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.141-personal-search-recovery; old warning/scroll bug included, so only if new regression, in-place with backups.
- No stable 1.0.0 yet; production signing/debuggable, backup privacy, end-to-end device regression and true popup Liquid Glass remain open. `main` has docs; active app code lives in feature branch.

## 2026-11-11 — updater stage 1: production-type signing verified, physical in-place test pending
- Signed NONDEBUGGABLE RC1 0.4.142-rc1/code167 from exact 0.4.140 source `e812bdb`, branch `feature/navtasks-signed-release-candidate-0.4.142` SHA `6c080c5d894b4756f645f72b87623a37ab79a74a`, CI42873932892 SUCCESS https://github.com/sergbokarev95-arch/NavTasks/releases/tag/untagged-6b6bfb5b28f19db6920f. APK `NavTasks_0.4.142_RC1_Signed_Release.apk`; **com.navtasks.app.glass**, same signing certificate as REAL v0.4.118 and v0.4.140 APKs, confirmed by CI; source/business/Manifest/dock unchanged. Private draft/pre-release, not stable or public, no network/updater code added.
- Emergency exact original 0.4.140 app source rollback as 0.4.143-beta/code168, branch `feature/navtasks-0.4.143-release-rc-recovery` SHA `0b6c3d94fc613431c94e09a435879e65aef0ec82`, CI42874025230 SUCCESS https://github.com/sergbokarev95-arch/NavTasks/releases/tag/untagged-bfab878ad7a9ded917d3. Install over RC only on NEW regression, never uninstall/clear; next versionCode >=169 after recovery.
- First RC CI build and certificate checks passed but Actions upload quota blocked distribution, re-run uses private GitHub draft release; no assets/history cleaned up. Real HONOR/Samsung in-place retention + archived old data migrations unverified; Android Auto Backup/privacy and production 1.0.0 remain unresolved. No separate PUBLIC binary repo created yet.

## 2026-11-11 — Updates infrastructure Stage 2 verified, stable channel intentionally inactive
- PUBLIC `sergbokarev95-arch/NavTasks-Updates` https://github.com/sergbokarev95-arch/NavTasks-Updates established; source `NavTasks` remains PRIVATE. Only README, .gitignore, `channels/stable.json` `available:false`, and NO APK or public GitHub release. Public channel initialized commit `5e537d5220c6556415e2d41b02441a3e6adb42d1`.
- User installed in-place signed nondebuggable `0.4.142-rc1` (code167) on HONOR and reported app works; full old-profile migration verification remains outstanding. Private emergency `0.4.143` code168 preserved.
- Dedicated branch `feature/navtasks-updates-stage2-channel` SHA `d3a81382d1cdd6f4521a31cfbba81f8d8233901d` off `6c080c5d894b4756f645f72b87623a37ab79a74a`; app source unchanged. Stable manifest validator, Python tests, public feed smoke check and manual gated publisher staged; CI #42940531172 SUCCESS. Publisher is not enabled in default main, no secret configured and no public stable APK. The mobile update checker/downloader/installer/UI is NOT implemented yet. Next versionCode >=169.

## 2026-11-11 — Stage3B signed in-app updater RC candidate, physical tests pending
- Latest updater candidate: `feature/navtasks-updater-stage3b-0.4.146-rc3` SHA `6afe4409a19cfc7fd1fe41f2c0d9efe273fba4e5`, version `0.4.146-rc3`/code170, GitHub CI42957053616 SUCCESS, signed PRIVATE draft https://github.com/sergbokarev95-arch/NavTasks/releases/tag/untagged-319403b5abaf1b1cf691. User-installed accepted base is 0.4.142-rc1/code167. CI tested package/signature/gradle and JVM; no physical full updater test yet.
- APK downloader/installer is opt-in; URL pinned to PUBLIC stable Releases with network and size caps, SHA+PackageManager signer check against already installed NavTasks; FileProvider read-only isolated cache; Android system permission/install. Day/night RU/EN popup, Settings manual button, Later suppresses only one version; offline failure silent and does not block fresh online check.
- Exact 0.4.142 application source recovery 0.4.147-rc4/code171 SHA `b9024cc0bb44de9ae18374d4d45b031ebb664b5b`, CI42957177681 SUCCESS, PRIVATE https://github.com/sergbokarev95-arch/NavTasks/releases/tag/untagged-8428ad48f7af4074bcc0; after recovery next code>=172.
- Public `NavTasks-Updates/channels/stable.json` intentionally `available:false`: no user should receive live update prompt or downloader yet. No business/state/dock code changed outside updater hooks.

## 2026-11-11 — Stage3C signed candidate 0.4.148 RC5 / fallback 0.4.149 RC6
- User HONOR-installed 0.4.146 RC3/code170 and confirmed the Updates section exists. Approved moving it immediately before version footer and a global persisted "Offer updates automatically" switch; OFF disables startup check+offer but NOT manual checking.
- Candidate `feature/navtasks-updater-stage3c-0.4.148-rc5`, application SHA `493ebf271003434ab013488d9d3d6f488a82f872`, later CI-workflow-only fix HEAD `493ebf271003434ab013488d9d3d6f488a82f872` (same commit after Kotlin fix), signed CI https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42966615760 SUCCESS. Private draft https://github.com/sergbokarev95-arch/NavTasks/releases/tag/untagged-959074696eb80d37012f with `NavTasks_0.4.148_RC5_Updater_Private.apk`, code172, 28,501,761 bytes, same package/certificate, nondebuggable.
- Signed safe previous RC3 app-source recovery v0.4.149-rc6/code173 SHA `d12ff03ff666899feda5294813de8e49e35bd524` CI https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42966290343 SUCCESS private draft https://github.com/sergbokarev95-arch/NavTasks/releases/tag/untagged-01723f74215b01af7d0f.
- Exact RC3 backup `backup/navtasks-0.4.146-before-update-preferences` SHA `6afe4409a19cfc7fd1fe41f2c0d9efe273fba4e5`; `main` is docs-only, app source stays at 0.4.59. Public stable update metadata still `available:false`, no public binary or stable 1.0.0.
- Needs HONOR real-device acceptance of RC5 preference/relaunch/manual checking and full data preservation, direct updater E2E and skipped-version tests with a controlled approved channel. Signed CI success is NOT phone testing. Rollback only in place after FULL profile export. Code173 recovery means future code>=174.

## 2026-11-11 — RC5 installed and user smoke-accepted on HONOR
- User says `0.4.148-rc5` installed, "все на месте, переключатель работает"; in-place functional/data visibility and toggle were verified by the user. Full automated/actual HTTP network suppression, persistence across reboot, repeated Later, Samsung and full profile historical migration matrix have NOT been independently tested.
- Latest tested candidate: `493ebf271003434ab013488d9d3d6f488a82f872` CI42966615760 SUCCESS, private APK draft `untagged-959074696eb80d37012f`, code172. Emergency exact RC3 source restore code173 CI42966290343 SUCCESS, draft `untagged-01723f74215b01af7d0f`.
- Next major effort controlled updater E2E: stable public feed intentionally `available:false`; no public RC promoted or released. Use an isolated channel only after explicit channel/public asset approval, preserving existing full-APK signer/package validation. Future versionCode >172 (or >=174 after recovery installation).

## 2026-11-11 — Stage4 updater: signed private test infrastructure, public publisher credential pending
- Public `NavTasks-Updates`: `channels/test.json` exists with `available:false` (commit `aac03ce`), `channels/stable.json` remains `available:false`, ZERO public APK/releases.
- Signed RC7/code174 `0.4.150-rc7` manual-only test checker CI42972399842 SUCCESS (branch HEAD `536fd3c`); exact accepted RC5 source fallback RC8/code175 CI42972475636 SUCCESS. Original user phone still reports RC5/code172; no newer phone QA.
- Signed target `0.4.152-test1`/code176 exact RC7 app source except version, private draft, SHA256 `43843af02acbab67606a12ba711254db8c38d148e6f4455bd52f73fb5dbd4ed1`, CI42972731198 SUCCESS.
- Public publisher workflow commit `699e32a` CI42973725649 FAILED PRE-SIDE-EFFECT because `NAVTASKS_UPDATES_PUBLISH_TOKEN` was absent from private source Actions secrets. Required token is fine-grained, scoped exclusively to public updates repository Contents RW, NEVER user-visible. Only after provision/verified publisher run should test manifest become available:true. Stable never touched.
- RC9/code177 exact RC7 app fallback `5ae489c` in signed CI42974037324 pending. Full E2E and user device acceptance pending, same package/cert preserved.

## 2026-11-11 — Stage4 recovery final CI result
- Code177 `0.4.153-rc9` exact previous RC7 application code backup CI42974037324 SUCCESS; PRIVATE draft https://github.com/sergbokarev95-arch/NavTasks/releases/tag/untagged-7d0c70ad8ede887bd8b6. This is verified signing/build only, NOT physical install.
- Stable/test public channels both remain `available:false` and there are still NO public Releases. Private source publisher is correctly fail-closed without `NAVTASKS_UPDATES_PUBLISH_TOKEN`; user GitHub secret setup required next. Latest user-tested phone app remains RC5/code172.

## 2026-11-11 — Test transport public prerelease live; stable OFF
- Public `NavTasks-Updates` has one verified GitHub prerelease `test-v0.4.152`, signed full APK `NavTasks_0.4.152-test1.apk` 28,501,761 bytes, code176, SHA256 `43843af02acbab67606a12ba711254db8c38d148e6f4455bd52f73fb5dbd4ed1`. Test metadata `channels/test.json`: `available:true`, `minSupportedVersionCode:174`, URL matches public release/asset size. `channels/stable.json` STILL `available:false`.
- Source CI publishing workflow 42978780819 SUCCESS after user set scoped Actions secret; source draft resolved by release/asset ID, independent historical signing certificate and re-download/checksum verified. PUBLIC repo contains no application source, profile exports or private signing key.
- Phone user-tested stable candidate remains `0.4.148-rc5`/code172. Manual test feed is implemented ONLY in signed private `0.4.150-rc7`/code174, CI42972399842 SUCCESS, draft https://github.com/sergbokarev95-arch/NavTasks/releases/tag/untagged-439aaa4ee6cb45e7a7f8. Physical in-place RC7 -> test code176 UI/install and full data migration still pending. Safe previous RC7 application source fallback code177 CI42974037324 SUCCESS.

## 2026-11-11 — Test-channel weak-network hotfix signed, awaiting HONOR retry
- User RC7/code174 test-channel check repeatedly showed "Тестовый канал недоступен"; HONOR browser loaded correct `test.json` with TEST1/code176 floor174. Old test checker timed out in 3.5s and swallowed exceptions; precise cause unproven.
- Separate source branch `feature/navtasks-updater-0.4.150-rc7-fix1-code175` SHA `b2bf22674d68ed4026ef781d03243d1414ce8f63`. Only isolated test network retry (12s/20s max three attempts), same-path safe redirects, classified localised errors, tests and code175. Signed CI42984926537 SUCCESS, PRIVATE draft https://github.com/sergbokarev95-arch/NavTasks/releases/tag/untagged-c61cfbfa7436013ce386 APK 28,518,153 bytes. Public test TEST1/code176 available:true, stable available:false.
- Needs in-place phone acceptance of fix code175 and full updater E2E. Original RC7 backup preserved. Higher-code RC9/code177 signed recovery is available; RC8/code175 should not be used over fix.

## 2026-11-11 — HONOR in-app GitHub TEST1 update passed (user)
- Verified user screenshots: `0.4.150-rc7-fix1`/code175 manual test-offer for `0.4.152-test1`/code176, download/verification reaches Install button, and Settings afterward shows `0.4.152-test1`; user confirms update succeeded. This establishes on-phone *happy-path* optional signed full-APK update end-to-end. Does not yet establish exhaustive task data/profile/history/widget preservation or download resume/bad-signer/failed-download behavior.
- `NavTasks-Updates/channels/stable.json` still `available:false`; `channels/test.json` still `available:true` for TEST1, no stable prompt published. Last tested device code176. Next install must use code>=177 (higher if 177 recovery installed).

## 2026-11-11 — User confirmed all NavTasks data survived on-phone TEST1 update; Stage5 private development
- HONOR user said "Все на месте": data and records preserved after in-app `0.4.152-test1`/code176 installation. Happy-path E2E + visible data survival PASSED.
- Accepted source 639fab15d02a41c1dfecb5852f6eeb2977db4c72 backed up `backup/navtasks-0.4.152-test1-after-honor-e2e`. Stage5 private candidate `feature/navtasks-updater-stage5-resumable-0.4.156-rc11` code178; private exact accepted-code recovery `feature/navtasks-updater-0.4.157-rc12-exact-test1-recovery` code179. Both CI/build and device QA pending.
- Scope: only resumable transport for full signed APK via bounded private cache, exact 206 Content-Range and final SHA/signing verification. Public `stable.json` remains inactive; TEST1 remains existing code176, not modified.
