# NavTasks — Next Tasks

## Current stage — v0.4.59 signed dock stability rollback published, HONOR confirmation pending

**Highest priority now:** user should test v0.4.59, which has byte-identical v0.4.56 NavGlass.kt and no runtime AGSL. Test Handover/Today scrolling before further glass research. v0.4.57 and 0.4.58 failed HONOR visual acceptance.

2026-11-07 user observed that the floating Add (+) controls should sit on the right edge consistently. In the repo, Today calendar is on the right but Tasks, Services and AMOS Add buttons were on the left.

- Update exactly three floating Add wrappers: NavTasksUi.kt (Tasks tab), ServicesUi.kt (Services), WorkOrdersUi.kt (AMOS). BottomStart/start 19dp → BottomEnd/end 19dp.
- Keep the existing floating + appearance, callback and safe Android navigation-bar inset, all bottom offsets (83dp above dock, 18dp on standalone pages), contents and interactions.
- No other FABs/dock buttons or calendar position changed; no data, profile, task, schedule or notification changes.
- Completed: 0.4.55/versionCode 77 main CI 42369659340 SUCCESS, publisher CI 42369873080 SUCCESS, release https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.55-glass-beta, source 401564269c574271b1e3c22c356989fe2d94eb58, APK SHA-256 156b24d04dcbb496e8480ce463423e430651d40fe412e42b2f9202ace07229be.
- After release, HONOR dark/light checks: + on right for Tasks, Services and AMOS; calendar shortcut still on right in Today; bottom buttons do not obscure critical interactions or gesture navigation; editor opens normally.

## Previous release
- 0.4.54 signed, source 7e04c01a67b5979ae2b02dbf9ad1be6ccf21d16f, GitHub Actions verified. Today weekends now match Plan.
- Pending visual checks from earlier releases remain recorded in CURRENT_STATE.md and release history.

## Verified release evidence (0.4.55)
- Source 401564269c574271b1e3c22c356989fe2d94eb58; publisher workflow commit aa4ed6b8f4140e7f78364819a8a8f1998e0ab279; release https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.55-glass-beta.
- Main CI https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42369659340 SUCCESS; signed release https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42369873080 SUCCESS; APK digest sha256:156b24d04dcbb496e8480ce463423e430651d40fe412e42b2f9202ace07229be.
- **Not physically verified:** user HONOR screenshot/tap test of right-hand + controls in Tasks, Services and AMOS, in both themes, while preserving platform gesture insets and all creation flows. Do not close until user confirms.

## Latest user finding — Handover (+) left (2026-11-07)
- Screenshot shows Handover creation (+) still at lower-left, unlike moved Tasks / Services / AMOS controls (0.4.55). Handover has separate HandoverUi.kt implementation.
- Source change in HandoverUi.kt only: BottomStart + start 19dp -> BottomEnd + end 19dp. Keep embedded-in-tab bottom=83dp, standalone bottom=18dp, navigationBarsPadding(), and action openSectionEditor(null).
- Completed: versionCode 78 / versionName 0.4.56; main CI 42373593478 SUCCESS, publisher CI 42373854191 SUCCESS, signed APK and ZIP released from 612ba1ba215e8c1871a654acda2304f5c44c678c.
- HONOR check: + right-aligned above dock in Handover and still opens new section; no overlap with Android gesture bar. Verify in both themes. Previous 0.4.55 changes to other screens remain untouched.

### Release confirmation and remaining device check
- Source 612ba1ba215e8c1871a654acda2304f5c44c678c; publisher workflow 49ae8ce49b6dfc90cfd094a17c07bfd815faf2a0. Latest release https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.56-glass-beta; APK SHA-256 f01d6f88729a9227ee7813e281f733c711cfab42b621f9f9a557eb3e244cb69b.
- GitHub CI https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42373593478 SUCCESS; signed publish https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42373854191 SUCCESS.
- On HONOR: Handover + visible at the lower-right above dock, matches Tasks position, opens New Section, works in light/dark and during Android gesture navigation. Device verification pending; retain feedback for next iteration.

## Proposed future work — Telegram-inspired glass (research only, not authorized for implementation)
- See [GLASS_TECH_AUDIT.md](GLASS_TECH_AUDIT.md) for pinned Telegram/NavTasks source references, comparison, API gates, and risks.
- Stage 0: HONOR screenshots + frame-time baseline over scrolling content, light/dark; do not judge solely over empty black areas.
- Stage 1 proposal: A/B tune Haze + navGlass opacity/tints/edge in NavGlass.kt, keeping dock size, Haze source, spring geometry, accessibility and gesture logic unchanged. **Await approval before coding.**
- Stage 2 only if needed: independent API33+ refractive shader prototype with Haze fallback and performance testing, without copying GPL-2.0 code. Explicit separate approval required.
- Existing 0.4.56 Handover/right-side Add HONOR acceptance remains pending and unaffected.

## Current experiment — 0.4.57 actual dock refraction (2026-11-07)
- User reported the 0.4.56 dock still looked like a translucent container, not glass. An incremental **refraction prototype** was implemented in a new independent AGSL shader, rather than copying Telegram GPL-2.0 code or migrating the entire app to Haze 2.
- **Completed:** main CI 42376895012 SUCCESS (unit tests and APK), signed publishing CI 42377117640 SUCCESS, source 3d2ffdeead8c607d2e6f7b43d126d18ae6593581, APK SHA-256 b8de98ae598396405ac4145e007a4ca1de629146ea14e394dd41f32200db354c; release https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.57-glass-beta. Unit gating passed; real optical appearance still needs phone validation.
- **HONOR visual acceptance mandatory:** test (a) Day over bright cards, (b) Night over content with distinct boundaries, (c) empty black Today (should not be claimed as refraction proof), (d) scrolling behind dock, (e) fast tab taps + swipes, (f) active pill symmetry and readability, (g) no white/blue startup flash, stale blur, flicker or shader crash.
- Performance criteria: compare old 0.4.56 to 0.4.57 on hardware, 60/120Hz frame pacing and touch response. If regression, prefer small shader-strength/material adjustments or revert only NavGlass changes, never app data.
- Compatibility: Android 12/12L uses existing Haze; Android 13+ uses optics only if RenderEffect initialization succeeds. Ensure the fallback material still looks acceptable.
- Original navigation behavior, tab positions, source capture, controls and all Handover/Services/AMOS improvements must remain intact.

## 0.4.57 published evidence / on-device check
- Immutable source 3d2ffdeead8c607d2e6f7b43d126d18ae6593581; publisher workflow f41dc24810e0c8ce4e90b36cf72927b6cf4f8fc9; signed APK release https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.57-glass-beta.
- Main CI https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42376895012 SUCCESS; publisher CI https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42377117640 SUCCESS.
- APK SHA-256 b8de98ae598396405ac4145e007a4ca1de629146ea14e394dd41f32200db354c; ZIP SHA-256 63430ee5388d5e6cf7548802f332f9a02c32dbcb13519f7f9ffd1925356d911c.
- User should make full profile backup first. Install over existing Beta without deleting data. Screenshots/video requested: scrolling colourful cards under dock in daylight, dark with contrasting cards, rapid tab movement; check no white/blue flash, frame drops, frozen source texture or icon distortion. If poor, issue higher-version targeted optics-only hotfix; do not delete user data or downgrade by uninstalling.

## 0.4.58 hotfix (2026-11-08)
- [x] Audit 0.4.56 ↔ 0.4.57 geometry: outer glass incorrectly shrunk to 52dp by material-before-content padding. Restore material at 56dp, move 1dp horizontal/2dp vertical inset onto selection Canvas and Row only.
- [x] Source stage: blur nav background 15→30dp with modest extra tint to suppress sharp scrolling text; restrict optical rim 13→5dp and inward bend 5.5→1.2dp. Keep refractive feature on API33+ and fallback on older devices.
- [x] Regression unit test bounds refractive rim and bend; no dependencies updated; version 0.4.58/versionCode 80.
- [x] Main CI (42381839037) unit tests and APK SUCCESS; signed publisher CI (42382154553) SUCCESS. Immutable source 4118106265361b3e28b2f4636f5c6c17def6801c, publisher workflow 6abcfc23221872ab021f38d60725d88b159f6b2f; release https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.58-glass-beta; APK sha256:340e1851179f1ef2a1350638df3454cfdde1d279ed6bccfc15fac82a187a1914.
- [ ] HONOR visual test: Handover scrolls behind dock without bright flickering letters; dock outer height/width and active-pill spacing matches 0.4.56; check rapid taps, both themes and empty/content-heavy backgrounds.
- Preserve 0.4.56 and 0.4.57 GitHub releases; avoid uninstall/downgrade of data-bearing app. If optical distortion persists after 0.4.58, disable only shader in a higher-version hotfix and keep safe Haze, rather than rewriting navigation.

## 0.4.58 release proof and next device criteria
- Stable Beta-signed release https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.58-glass-beta from immutable source 4118106265361b3e28b2f4636f5c6c17def6801c, publisher workflow 6abcfc23221872ab021f38d60725d88b159f6b2f; versionCode 80. Main CI https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42381839037 SUCCESS; publisher https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42382154553 SUCCESS.
- APK sha256:340e1851179f1ef2a1350638df3454cfdde1d279ed6bccfc15fac82a187a1914; compact ZIP sha256:382e4ce9b3cb4a3b7aac244fbf009df6a7b19ed5620c561d8997ab25337eaa42.
- User should install over 0.4.57 **without removing the app** after full profile backup. Test scrolling text behind the Handover dock, outer glass full-height, active-pill symmetry, rapid tabs, Day/Night. A screenshot alone cannot prove absence of temporal flicker; report scrolling observations or short screen recording.
- If refraction still causes shimmering, next step is AGSL-off Haze fallback in a **new higher-version** hotfix, not another untested large rewrite.

## Immediate priority — 0.4.59 stable dock fallback (2026-11-08)
- User tested v0.4.58: text/cards beneath the dock still shimmer and jitter; the dock remains visually a transparent container. Mark the v0.4.57/0.4.58 optics prototypes as **rejected on HONOR**.
- Exact NavGlass.kt reverted from pre-optics v0.4.56 `612ba1ba` rather than further tuning the shader. Experimental optics module remains in repository but is **not referenced by the running dock**. VersionName 0.4.59, versionCode 81; no other product features or user data affected.
- Completed: main CI 42383033196 SUCCESS and signed publish CI 42383289163 SUCCESS. Publication workflow validates byte-identical stable NavGlass.kt and checks AGSL is not referenced. Source b22057ad92b0f531df12217c4ecee97150395951, release https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.59-glass-beta, APK SHA-256 24dbfa7c439e95775e86abd8e064efdf119417903f2d2369390405b83090ab8e.
- Next: HONOR visual test (both themes, moving text, active lens, gestures) before closing this issue.
- HONOR check: previous familiar dock geometry, no shimmering on scroll, normal tabs, dark/light and smooth rapid switching. If still shimmering, isolate Haze source/rendering rather than restore AGSL. Do not claim true-glass effect has been achieved.
- Research phase afterward, separate from release: investigate correct dynamic backdrop capture and whether refractive effect can sample a stable preblurred texture without modifying foreground or changing dock dimensions. No coding until baseline verified and a measurable plan agreed.

## v0.4.59 signed release evidence
- Immutable source b22057ad92b0f531df12217c4ecee97150395951; signed publishing commit 28f6245873e0613cda7f17e9d3da7e9ab1ea7612; release https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.59-glass-beta; versionCode 81. Main CI 42383033196 SUCCESS; publisher 42383289163 SUCCESS.
- APK sha256:24dbfa7c439e95775e86abd8e064efdf119417903f2d2369390405b83090ab8e; ZIP sha256:454dbfd31aefe0b383d2d4ed4c1656d089fc5c079dabc2e705d8e90a1eaf1f33.
- Full profile export, install in place without uninstalling, scroll text-rich content beneath the dock and check flicker, touch, day/night and the four tabs. On-device results pending.
- Only after stable baseline acceptance, explore glass in an isolated rendering test harness with a reliable preblurred source and frame metrics. The current effect is **not** Telegram-style glass.

## New priority: physical test of separate Liquid Glass Lab 0.1.0 (2026-11-08)
- [x] Create independent `:glasslab` application in branch `experiments/liquid-glass-backdrop`, using actual scene capture + Backdrop refraction, distinct package. `main` stable NavTasks v0.4.59 unmodified.
- [x] GitHub build CI 42385989762 SUCCESS, tested commit `512cb355d0e1a37cba22eb79e5ada7cc32294eab`, experimental prerelease https://github.com/sergbokarev95-arch/NavTasks/releases/tag/glasslab-v0.1.0, APK https://github.com/sergbokarev95-arch/NavTasks/releases/download/glasslab-v0.1.0/NavTasks_LiquidGlass_Lab_0.1.0.apk (SHA-256 caeb1cbc7189e1aca707416f3f3660696dce1438fd546811099e8e7fcb24063c).
- [ ] Install **Lab APK alongside** NavTasks; use Glass ON/OFF and Day/Night toggles. Scroll text-rich cards under 56dp dock; evaluate physical refraction, letter stability/no shimmer, border glints, capsule movement, contrast, and frame pacing. Video preferable for detecting shimmer.
- [ ] If accepted, design a minimal integration into NavGlass (exact geometry, four-tab semantics, Haze fallback and data untouched) on a separate PR/branch; only merge after user authorizes. If rejected, tune test module only; never change main blindly.
- Build history: Backdrop 2.0.0 needs SDK37 which current GH runner cannot install. Backdrop 1.0.6 compiles on Android36 (Kotlin compatible), with direct `shapes:1.2.0` dependency. This is why Lab uses 1.0.6.


## Next priority after signed Glass Lab 0.1.2 (2026-11-08)
- [x] Copy exact 0.4.59 four dock icon paths and blue/inactive text colors into separate Lab module.
- [x] Keep 0.1.1 optical card-border refraction, and add a spring-travelling 10dp-stretch lens that settles into matte grey.
- [x] CI 42389729911 SUCCESS; signed standalone https://github.com/sergbokarev95-arch/NavTasks/releases/tag/glasslab-v0.1.2, source 049836c2bd05bbd3378162e174a46f2ae249c240.
- [ ] HONOR test: animated tap sequences 0→3, 3→0, rapid taps, press feedback, actual icons/colors, equal insets, no overflow; glass-to-neutral-matte resting state in both Day/Night; cards/edges still refract without shimmering while scrolling.
- [ ] Only after user confirms, design minimal integration into NavGlass/NavTasksUi using one direct Backdrop source, preserving main v0.4.59 baseline, existing touch/swipe/back rules, task/AMOS/profile data, signed APK update path and fallback strategy.
- Lab rollback branch `backup/glasslab-0.1.1-before-nav-animation`. Do not merge experimental code into main based on CI alone.


## Current: HONOR test of standalone 0.1.3 bigger lens (2026-11-08)
- [x] Create backup `backup/glasslab-0.1.2-before-28dp-stretch`, raise Lab spring horizontal max extension to 28dp (from 10dp), update regression/CI/release version.
- [x] CI 42390942917 SUCCESS; signed APK https://github.com/sergbokarev95-arch/NavTasks/releases/download/glasslab-v0.1.3/NavTasks_LiquidGlass_Lab_0.1.3.apk; source 9f3ac3c8574d539141dd828d76c9c08da28fbe6f.
- [ ] User HONOR accept/reject: does lens visibly widen enough while moving, keep geometry inside 56dp dock including endpoints, settle back to grey cleanly, and avoid shimmer in light/dark themes?
- [ ] If accepted, consider production integration as a separately reviewed change. Keep main v0.4.59 data, settings, AMOS, services, notifications and dock behavior unchanged until approval.


## Highest-priority next step — HONOR check Glass Lab 0.1.4
- [x] Replace rejected 28dp horizontal-only stretch with equal-axis 1.30× enlargement of the entire travelling active capsule. Ensure lens height exceeds 56dp dock (67.6dp), center it, and allow out-of-bounds composition; no change to outer dock source capture.
- [x] Preserve 0.1.3 backup `backup/glasslab-0.1.3-before-proportional-scale`; CI 42392699540 SUCCESS; signed experimental https://github.com/sergbokarev95-arch/NavTasks/releases/tag/glasslab-v0.1.4.
- [ ] User checks on HONOR: moving lens rises above and drops below main dock, then shrinks into the familiar 52dp matte-grey selection; verify 0→3/3→0 switching, first/last boundaries, non-distorted 24dp glyphs and text, day/night and scrolling edge refraction.
- [ ] If and only if visually accepted, plan isolated branch/PR for main app migration, with stable 0.4.59 backup and unchanged task/AMOS/data logic. Current app source unchanged.


## Current: HONOR acceptance of Glass Lab 0.1.5 (2026-11-08)
- [x] Move optical 56dp backing from parent modifier into background sibling; selected capsule with 1.30× scale can draw beyond outer bar; add CI structure guard.
- [x] CI 42393384091 SUCCESS and signed standalone APK https://github.com/sergbokarev95-arch/NavTasks/releases/download/glasslab-v0.1.5/NavTasks_LiquidGlass_Lab_0.1.5.apk.
- [ ] On HONOR confirm top AND bottom protrusion while moving, even near first/last tab, 52dp neutral-grey rest, stable glyphs, no shimmer and card-border refraction in both themes.
- [ ] Production integration only after visual approval; main NavTasks remains 0.4.59.


## Next: on-device full NavTasks dock preview acceptance (2026-11-08)
- [x] Stable main snapshot backup `backup/navtasks-0.4.59-before-liquid-glass`; separate feature `feature/liquid-glass-dock-preview`.
- [x] Port accepted Lab 0.1.5 dock only; add original-page LayerBackdrop separate from Haze; leave old NavGlassBottomBar for API31/32; keep tasks/AMOS/Handover/profiles/data and floating buttons unchanged.
- [x] Complete app CI `42397184202` SUCCESS and standalone signed https://github.com/sergbokarev95-arch/NavTasks/releases/download/dockpreview-v0.4.59-r1/NavTasks_LiquidGlass_Dock_Preview_0.4.59.apk.
- [ ] User HONOR test with real full-app content in light and dark: swift 0→3/3→0 taps, 1.30x lens visibly protruding above/below 56dp dock, text/border distortion only under lens (no flicker), exact glyphs/blue colors, grey rest, interactions/tap targets/scroll/swipes/back, frame pacing.
- [ ] If approved, choose controlled stable in-place update approach and extend shared glass **first** to '+', calendar and settings, one component at a time with tests and fallback. Do NOT claim full integration or change production release until approval.


## Immediate next: HONOR test Glass Dock Preview R2 soft background (2026-11-08)
- [x] HONOR R1 issue: task text behind glass still readable and interfering with labels; adjust only backdrop pre-lens blur bar 2→5dp, selection 1.5→4dp in feature branch.
- [x] Backup R1 `backup/dockpreview-r1-before-soft-blur` and signed R2 https://github.com/sergbokarev95-arch/NavTasks/releases/download/dockpreview-v0.4.59-r2/NavTasks_LiquidGlass_Dock_Preview_R2.apk; CI 42398375900 SUCCESS.
- [ ] User HONOR test in day/night with task titles and card borders scrolling under dock: text should be sufficiently blurred so the four button captions stay easy to read; no new ghosting/shimmer; lens growth 1.30x and grey rest unchanged.
- [ ] After approval, plan integration + reusable glass for circular '+' / calendar / settings one at a time, preserving all existing controls/data. Main stays stable 0.4.59 until signed migration approved.

## NEXT — 2026-11-12 user-approved full pre-1.0.0 polish, phase 1 CI PASS
- [x] Record RC20/code188 installed on HONOR and success receipt appeared. Still verify no repeat after subsequent cold start.
- [x] Back up exact RC20 baseline to `backup/navtasks-0.4.166-rc20-before-pre1-polish`; keep user's installation unchanged.
- [x] On separate feature branch implement compact nested Handover vessel entries (tap row to expand/tap text to edit), maintain Personal notes, auto-expand search hits, top-align active result; inactive task strikethrough/sort and blue manual update button; independent text/highlight swatches with black/white. No business/storage edits.
- [x] Test: `43211482395` signed full JVM+APK identity `SUCCESS`, NO publish, no package install.
- [ ] Fix HONOR RichTextEditor selection-handle dragging after isolated dependency compatibility/source investigation. Do NOT claim palette alone fixes selection; guard HTML/formatting and keep existing Kotlin/Compose versions unless retested.
- [ ] When a future user-approved higher-version signed QA build is ready, verify nested Handover: closed/open/empty, text tap editor, Personal notes unchanged, repeated search hits and title notes, last search occurrence top-alignment, day/night; palette text and marker persistence, keyboard/selection.
- [ ] Decide new NavTasks adaptive/monochrome launcher icon with user, redesign responsive RemoteViews widgets and theme accents; prepare functional Reference scaffold, defer SIRE/VIQ content after stable 1.0.0.
- [ ] Complete recurrence, AMOS, Services, Handover, notifications/widgets, profile/import, data safety, Samsung/old-version compatibility and Apple HIG glass geometry audit before independent signed 1.0.0 candidate. Keep public `stable.json available:false`; candidate code >189; never publish or install same-code188 development APK.

## NEXT — 2026-11-12 selection compatibility + widget polish stage
- [x] Compare upstream Rich Editor RC14 vs 1.0.0 and official Kotlin/Compose compatibility; upstream changelog includes drag-selection stabilization. Do not swap library on user's accepted build blindly.
- [x] Create isolated `experiment/navtasks-editor-selection-1.0.0-compat` with Kotlin 2.4.0 and Rich Editor 1.0.0; compilation/signing/regressions CI #43214672555 SUCCESS. Additional synthetic saved HTML and selection-only regressions CI #43214884950 SUCCESS.
- [ ] Verify physically on HONOR that newly compiled editor actually fixes finger-drag selection without format damage, IME/toolbar regressions or altered HTML. Keep experimental branch separate until accepted.
- [x] Stage widget visual cleanup only: NavTasks system blue accent in both modes and top-aligned RemoteViews rows, no click/store changes; add JVM accent test.
- [ ] Confirm UI signed CI #43215461681, then physical HONOR dark/light widget sizing/safe click/test after future higher-code signed QA build (installed RC20 code188; exact fallback RC21 code189 exists).
- [ ] Larger widget concepts and adaptive launcher icon still need design review and user sign-off. Reference placeholders may be prepared for content; SIRE/VIQ documents after 1.0.0. Stable 1.0.0 only following broad functional and visual audits and explicit approval.

## 2026-11-12 — Reference equipment and statistics widgets approved direction
- [x] User accepts 2×2 / 4×2 statistics-only widgets, no on-widget checkmarks, Today completed/total + remaining, show Tomorrow count after all today's jobs done, NavTasks/Apple visual style. Legacy turquoise removal/top alignment is only a preliminary visual step; NOT the finished statistical widget.
- [x] User accepts shared manual library with default SIRE 2.0 and VIQ7; all additional manuals/excerpts/equipment data supplied and managed by end users.
- [x] Equipment reference design corrected by explicit user instruction: independently browsable nested equipment folders, equipment identity, informative maintenance/check procedures (monthly/quarterly/yearly as source text), with manual excerpts/citations; users MANUALLY open Reference to consult a procedure. Absolutely NO linkage to Tasks, AMOS, Services, Works or Today; no deadlines, completion state, reminders, task IDs or cross-module deep links.
- [ ] Implement isolated Reference storage and selected-item offline export/import (.navref with schema, excerpts, optional original PDFs/DOCX, checksums, duplicate/conflict resolution). Shared files, vessel-profile inventory. Never import/activate somebody else's scheduled jobs automatically.
- [ ] Implement Equipment card/procedure browser, PDF/source links and standalone Reference search index; leave pre-created equipment data empty. Do NOT implement task link mapping or any cross-module integration.
- [ ] Implement 2×2 and 4×2 read-only stats widgets; remove WIDGET_TOGGLE behavior from widget surface, update launcher provider dimensions and background. Test midnight boundary, empty task days, 100%-done Today, tomorrow count, Missed, profile changes, HONOR/Samsung.
- [ ] Run signed no-publish CI and device acceptance, maintain stable 1.0.0 publication as separate explicit user decision.
- Document: docs/development/REFERENCE_LIBRARY_WIDGETS_PROPOSAL.md


## 2026-11-12 — User correction: Reference must be completely independent
- Previous assistant-proposed integration of Reference equipment with Tasks, AMOS, Services, Works, Today and job schedules was explicitly REJECTED. Do not implement relationships, buttons, deep links, task IDs, completion states, due dates, overdue status, reminders or import of job data in Reference. The user separately enters actual scheduled work in existing AMOS/Services. When a procedure is needed they manually open Reference, find equipment and consult the cited manual/excerpt. Any intervals in Reference are descriptive, not executable schedules. Shared manuals and selective export/import of reference-only equipment remain approved. See REFERENCE_LIBRARY_WIDGETS_PROPOSAL.md.

## NEXT — Statistics widgets + Equipment foundation build/acceptance
- [x] Stage no-checkbox 2×2 and 4×2 RemoteViews, Today and Tomorrow progress/status, blue, missed display; remove WIDGET_TOGGLE logic and keep click-only-to-Today.
- [x] Add pure unit tests for stats, completed/empty days, tomorrow, no task mutations, RU/EN labels and theme colors; confirm calculation uses read-only `dayCounts`.
- [x] Stage independent ReferenceEquipment/ReferenceProcedure/ReferenceSource and folder hierarchy, per-profile atomic JSON store, safe standalone .navref equipment ZIP (manifest+excerpt+source, without full PDF), duplicate preview, no task IDs.
- [x] Add unit tests for catalog roundtrip, required excerpt/source, archive tampering/zip limits, duplicate IDs and cycles; harden failed-open read.
- [ ] Verify CI `43228912162` fully passes (including signing and source tests); diagnose/fix if not.
- [ ] Install only an eventual higher-code original-key signed candidate after explicit user approval; HONOR/Samsung test widget 2×2/4×2 geometry, nighttime contrast, Tomorrow after Today all completed, midnight/profile/restart refresh, title overflow; no taps complete tasks.
- [ ] Build independent Reference user-facing Equipment hierarchy/CRUD, manual file storage with library export/import, .navref Settings chooser and conflict preview, PDF/DOCX viewer/search. The standalone one-equipment archive foundation is NOT the final import UI.
- [ ] Run full regression and separately approve stable 1.0.0; keep public stable feed off.

### Current verification — 2026-11-12
- [x] Confirmed signed, no-publish CI #43228912162 SUCCESS for code commit aa67869 (all JVM tests, original signer, nondebuggable app identity).
- [ ] HONOR/Samsung acceptance of 2×2/4×2 launcher widgets and later Reference equipment UI/import remains outstanding. No APK download yet. All original task, AMOS, Services, notifications, Handover records preserved.

## 2026-11-13 — Reference equipment UI acceptance gate
- [x] Add standalone equipment folders, equipment & procedure UI, source citations and descriptive intervals, search by procedure/excerpt/manual text; create/edit/delete with confirmation.
- [x] Add .navref one-equipment Android SAF export/import in equipment screen and Settings, with preview, duplicate detection, add copy or explicit replace, no automatic tasks and no serial numbers by default.
- [x] Add pure operations and JVM tests for folder cycle protection, equipment/procedure ID retention, archive-only semantics and text search.
- [ ] Confirm signed no-publish CI `43246741027` conclusion, fix compile/runtime issues until SUCCESS. No device acceptance yet.
- [ ] HONOR device acceptance using future signed higher-code build (>189): app navigation/back/search keyboard, large snippets, form save/reopen, new folder/nested folder, corrupted storage alert, export+import same/another vessel, duplicate conflict, zero AMOS/Services side effects, RU/EN and day/night.
- [ ] Add shared manual PDF/DOCX file store, import/view/search, reference folder manager and excerpts not tied to equipment; selective full-folder/library .navref packaging with size/security and library backup rules. Until implemented do NOT imply PDFs/DOCX are imported or included in single-equipment archive.
- [ ] Continue icon approval, rich editor handle-drag device check, complete app regression and separately approve stable 1.0.0.

## NEXT — Post Reference UI implementation
- [x] Initial Reference Equipment UI and Settings one-card import/export signed no-publish #43246741027 SUCCESS.
- [x] Fix equipment folder-title search and Back ordering and add pure test.
- [ ] Verify updated signed CI #43248144577 SUCCESS. Do not claim physically accepted.
- [ ] Provide all-library .navref (folders/equipment/excerpts and optional original manual files) export/restore and safe migration tests; current profile JSON excludes Reference catalog, which is a significant pre-1.0.0 release risk.
- [ ] Implement shared offline PDF/DOCX import and viewers and global Reference excerpt management. Separate from task scheduling. Verify large file storage limits, hash, backup and offline search.

## NEXT — 2026-11-13 catalog safety checkpoint
- [x] Standalone full EQUIPMENT catalog ZIP backup and restore: all nested folders, devices, manual-cited procedures and serials (explicit warning); checksum/size/schema validation and merge-copies preserving existing entries. Does NOT contain PDF/DOCX files yet.
- [x] Add pure tests of catalog archive integrity, nested structure, clone identity, corrupt/bomb/zip-slip rejection and no task identifiers.
- [ ] Verify complete signed CI #43250406239 and isolated PDF dependency test #43251052670 before accepting code. Regressions must pass without changing installed RC20.
- [ ] Shared offline PDF/DOCX document storage, user-managed manual folders, efficient search/indexing, readable PDF viewer using compatible approach (AndroidX PDF beta evaluated separately), DOCX basic reader, personal excerpts, standalone whole-library backup WITH file bytes and restore (including files where permitted).
- [ ] Device/launcher acceptance on HONOR and Samsung before RC >189. Explicit user sign-off required for 1.0.0 publication.

### CI results and PDF compatibility — 2026-11-13
- [x] Signed nonpublishing CI #43250406239 SUCCESS for full equipment-only catalog backup and restoration (all JVM regressions, original signer/package).
- [ ] Verify experimental AndroidX PDF #43251678681 after SDK extension 19. Initial attempt #43251052670 FAILED due AAR metadata requiring compileSdkExtension19; no change to accepted code or end-user package.
- [ ] Before UI PDF import, build shared original-file store and FULL Reference library archival export with hashes and file bytes; verify rollback and offline restore. Only then expose PDF/DOCX import to users so documents are portable.

## NEXT — shared PDF/DOCX manual milestone, 2026-11-13
- [x] Add isolated shared manual document schema and folder validation, safe file names and checksums; metadata title/filename search. NO full-text PDF search yet.
- [x] Add private original-file import backend (streams bytes with 250MB cap and SHA256, verifies PDF/DOCX package, atomic catalog); no UI until portable archive works.
- [x] PDF library beta compile proof #43251678681 SUCCESS with SDK extension 19, but no HONOR reader acceptance. Never merge experimental version blindly.
- [ ] Confirm feature-source CI #43252927005; fix failures if any.
- [ ] Implement bounded-streaming whole-manual-library ZIP export+restore including original file bytes, hashes, and folder structure; check multi-GB/zip bomb limits and corrupt package rollback. Maintain equipment full catalog separately until unified Reference backup is designed.
- [ ] Add SAF import and library screens, PDF/Word offline reading and document search; device QA on HONOR and Samsung; no changes to tasks/business logic.

## NEXT — 2026-11-13 original-document backup gate
- [x] Implement bounded streaming .navlib archive of shared manual originals (PDF/DOCX) and metadata, SHA integrity and ZIP-safe staging.
- [x] Restore hashed binary blobs then commit shared catalog atomically, merge remapped folder/document IDs rather than replace; preserve existing data on corrupt archive.
- [x] Add pure JVM roundtrip/staging security tests for exact PDF/DOCX bytes, nested folders, missing/corrupt/unknown files.
- [ ] Confirm signed CI #43253904642 SUCCESS, repair if failures.
- [ ] Add user-facing SAF manual import and .navlib backup UI on IO dispatcher, full PDF reader with document search, basic local DOCX viewer, comprehensive Reference search and custom excerpts.
- [ ] After complete in-app full Reference backup, test replacing phone and retaining manuals + equipment catalogs; export formats are intentionally separate until unified export is accepted. No 1.0.0 release without on-device QA.

### Latest source-test gate — 2026-11-13
- [ ] Verify signed full JVM/source CI #43254879474 after shared manual ZIP backup + hash/collision validation. Do not use any unchecked APK for phone.
- [ ] After CI pass, integrate OFFLINE manual picker/reader and .navlib export/import into the Reference UI with IO-dispatched work and safe error handling. PDF beta01 proved compile only on isolated branch and still requires device acceptance.

## NEXT — 2026-11-13 UI + reader acceptance
- [x] Fix prior test-only compile failure from deprecated temporary-folder creation.
- [x] Stage Reference main navigation to SIRE 2.0/VIQ7/My materials with independent folder/file import list and shared original-file store.
- [x] Stage user-initiated PDF/DOCX document picker, streaming offline import and .navlib full-file archive backup/preview/restore on Dispatchers.IO.
- [x] Stage native internal PDF page/zoom reader and API35+ text search, simplified offline DOCX heading/paragraph reader; preserve original bytes.
- [x] Add DOCX no-external-entity and header/paragraph regression tests.
- [ ] Check latest signed CI #43258709724 until full JVM+APK identity SUCCESS, fix errors, then document exactly which reader features are available.
- [ ] Replace inert library search result arrows with actual first/next hit scrolling; root search should deep-open matching document, not only category. Add general user snippets/excerpts with independent sources. PDF on Android31-34 needs dependable full text search fallback or compatible AndroidX beta viewer path.
- [ ] Security/visual QA: 250MB manual import, PDF 500+ pages, corrupt PDF/DOCX, scanned PDF, locked file, device low disk, archive restore across profiles, HONOR & Samsung Day/Night spacing/scroll/navigation. User-installed RC20 not changed; 1.0.0 publication requires separate approval.

## NEXT — 2026-11-13 manual library UI + excerpts stage
- [x] Add standalone user excerpts in SIRE/VIQ/My materials (title/body required, optional citation/page/edition). Keep informational only; no Tasks/AMOS/Services links.
- [x] Excerpt CRUD/read/search in shared Reference screen, include in .navlib archive & non-destructive restore; allow loading older v1 catalog without excerpt field while rejecting unknown task fields.
- [x] Add regression for citations, old schema, invalid pages, cross-section folder, excerpt-only ZIP backup and remapped folder IDs.
- [ ] Confirm complete signed no-publish CI #43260910863 PASS, repair compile/tests if needed; do not say ready until verified.
- [ ] HONOR/Samsung device tests: import large PDF, DOCX, standalone excerpts, folders, search arrows, source fields, every nav/back gesture, save/reopen/reboot, archive import to spare device, day/night; PDF pages and large Word docs must scroll above dock.
- [ ] PDF on Android31-34 needs real text-search fallback; current native Page.searchText requires API35+. Full PDF text index and precise search highlights not yet implemented; scanned PDF OCR later. Rich DOCX tables/images not yet supported; original bytes preserved.
- [ ] Before stable 1.0.0: full global functionality review incl recurrence/AMOS/Services/Handover/notifications/widgets, icon/widget appearance acceptance, release signing code>189 & separate explicit user approval.

### Current next actions after CI #43260910863 — 2026-11-13
- [x] Full signed nonpublishing CI #43260910863 SUCCESS for PDF/DOCX library, standalone excerpts, equipment and widgets.
- [ ] Audit Reference reader search/navigation and PDF performance with large 500+ page manuals. Global query currently indexes document metadata and user excerpts only, not every page of all PDFs. Plan off-UI-thread full text indexing and older Android 31-34 fallback without rewriting existing Tasks.
- [ ] Verify .navlib shared manuals+excerpts and .navref vessel equipment as TWO separate backup scopes; assess whether Settings needs a combined backup before stable release, and document correct user-facing migration instructions.
- [ ] Prepare once-only signed private device QA candidate (versionCode >189, same package/signer), but do NOT publish or request user to install until source and visual QA gates are ready. Keep accepted RC20 rollback branch.
- [ ] HONOR and Samsung physical testing: widgets 2x2/4x2, light/dark, offline manual imports and backup recovery, PDF page zoom/find, DOCX readability, keyboard/selection, task recurrence, AMOS, Services, Handover, notification timing; approve 1.0.0 only after user confirmation.

## NEXT — 2026-11-13 targeted quality audit and private QA build
- [x] Protect active original-manual archive restore from Cancel/Back and stage disposal; test crash staging cleanup.
- [x] Abort stale PDF full-text scans on query changes; no old results after a new query.
- [x] Prevent excerpt edit-after-search from silently changing its folder; add JVM test for root-null and existing folder cases.
- [ ] Confirm signed CI #43265305900 after all 3 fixes and pure regression tests; inspect failure logs and patch only if needed.
- [ ] Stress test on HONOR: large PDFs, archive export/import/back-pressure, cancellation/low disk, night/day reader, 2×2/4×2 widgets. Samsung older Android PDF search and text extraction still unresolved.
- [ ] Global Reference search currently covers metadata/user excerpts, not full PDF contents; implement safe full-text index or clarify supported search scope before 1.0.0. DOCX simplified reader is not a pixel-exact Office renderer.
- [ ] Only after reliable private build & full functional checks, prepare original-key code>189 QA APK (not stable release). Keep rollback and live profile backup.

### Quality-first versioned QA build gate — 2026-11-13
- [x] Preserve true RC20 original source in separate `qa/navtasks-rc20-recovery-code191` with ONLY code191 and private artifact workflow; no main/stable changes.
- [ ] Confirm recovery CI #43266688902 PASS, certificate SHA and package and APK artifact visible; test version metadata before using.
- [ ] Confirm latest feature source CI #43265672215 PASS, correct any compiler/test failure, review final diff.
- [ ] ONLY THEN create QA code190 from verified feature SHA with same historic signer/package and private artifact upload workflow. Never enable public stable feed. Ensure current RC20 profile export before installing test APK and provide recovery191 artifact in case of failures.
- [ ] Real HONOR and Samsung device checks remain mandatory: current widgets, Reference offline import/export, big PDFs, DOCX, Handover text selection and task recurrence/notifications. PDF older Android full text search not yet completed.

### QA APK delivery status — 2026-11-13
- [x] Source code CI #43265672215 SUCCESS, no TaskStore/AMOS/Services changes in quality audit.
- [x] Prepared private QA code190 and original-RC20 recovery code191 branches/workflows; version-only source diff and original-key/signature checks.
- [x] Identified GitHub Actions artifact quota error (not code failure); switched isolated workflows to PRIVATE DRAFT GitHub release assets, never stable channel.
- [ ] Confirm private draft CI #43269056417 recovery and #43269120575 QA both SUCCESS; verify Release status DRAFT, attached APK size, original package/cert and version codes. Do not supply installation links unless assets actually exist.
- [ ] After verified, give user direct private-draft APK links and one concise backup/install/recovery procedure; ask for physical HONOR testing, keep RC20 profiles backup.

### QA handoff status — 2026-11-13
- [x] CI source full PASS #43265672215; QA artifact #43269120575 and recovery artifact #43269056417 both SUCCESS with original cert/identity and private draft assets verified. Stable feed available:false.
- [ ] Guide user to download BOTH private QA code190 and fallback code191, export installed RC20 profile and install QA190 in-place; do NOT uninstall or clear data.
- [ ] Collect HONOR on-device QA: existing profile tasks, AMOS/Services, Handover, notifications, Today and 2x2/4x2 stats widgets, Reference SIRE/VIQ PDF/Word/excerpts/cited Equipment, library .navlib and equipment .navref backup/restore, search/zoom/save, Night/Day.
- [ ] Fix visual/function regressions from user screenshots before future higher-code QA; maintain recovery code hierarchy. Test Samsung separately. Do not publish 1.0.0 automatically.

### User-visible PDF QA defects — 2026-11-13
- [x] User installed QA190 on HONOR, preexisting data retained.
- [x] Reproduce from screenshot: PDF reader constrained under dock & app header, page small/horizontally clipped and search hit counter without visible page markers.
- [x] Stage full-screen edge-to-edge reader that covers dock and app chrome, fits page width without losing aspect, supports touch pinch, pan and button zoom, and draws live rectangles on actual PDF native text-match coordinates with selected result scroll.
- [x] Add pure JVM tests for aspect/canvas coordinates, maximum bitmap memory and zoom scaling.
- [ ] Verify final source CI #43277871872 SUCCESS, fix failures. No screen or physical rendering claim until tested on HONOR.
- [ ] Produce PRIVATE QA signed original-key code>=192 update atop QA190, plus retain rollback recovery191; request user profile backup before next install. No public/stable release.
- [ ] On HONOR check multiple PDF formats (1/2/8+ pages), 100% entire width, 2–4x zoom, two-axis pan, location-aware search highlight and next/prev results even on same page, Back/keyboard, light/dark and full-screen visibility (dock must not cover PDF).
- [ ] Samsung older Android <35 PDF text search unsupported in current native path. Scanned PDF requires OCR and has no text matches. Keep these explicit.

## 2026-11-13 — priority: user-supplied HONOR QA2 screenshots
- [x] Stage widget SYSTEM day/night color resilience using RemoteViews.setColorInt(light,night) and drawable-night resources. Removed ineffective CONFIGURATION_CHANGED manifest action (Android requires runtime registration); small/wide typography enlarged and small widget uses weighted vertical spacing. Physical MagicOS day/night launcher recolor still NOT verified; launcher controls cell aspect ratio.
- [x] Stage deferred PDF raster regeneration until the end of pinch, avoid replay of scroll-to-result on every zoom frame. Existing reader remains single-page fallback.
- [x] Add PDF/DOCX attachment inside equipment procedure, preview/replace/unlink, original stored in shared .navlib library, ref keyed by SHA+MIME rather than remapped document ID. Preserve attachment on edit. .navref remains metadata-only; UI warns .navlib binary backup also needed.
- [x] Add attachment model/backward-compat/archive tests.
- [ ] Confirm signed main source CI #43367074167 PASS; repair if it fails. Do not give user installer yet.
- [ ] Confirm separate AndroidX native-PDF experiment CI #43367575751 PASS for extension19 and actual page reader integration; only merge if verified. For older Samsung with extension<13, native Compose fallback remains supported but lacks continuous search.
- [ ] Test HONOR with screenshots: automatic OS/app day/night recolor of EXISTING 2x2/4x2 widgets, readability/wrapping and launcher dimensions; test PDF 2-page checklist continuous scrolling, search, selection, zoom gestures, direct file preview from Radar X Band procedure. Reopen/reboot/import/export .navlib/.navref to prove persistence.
- [ ] Target next private QA code>=194 with original signer/package and a recovery code higher than QA; user retains QA2/code192 and recovery193 until approved. No stable update feed change.

### NEXT — 2026-11-13, native PDF merged into feature source
- [x] Native AndroidX PDF viewer experiment tested in isolation: #43367575751 SUCCESS.
- [x] Integrate only native PDF viewer + SDK extension and CI installation (not full experiment branch merge), leave old Compose fallback on unsupported devices and preserve unrelated main source commits.
- [ ] Verify full signed main CI #43368601723 (original package/cert/JVM tests and SDK ext19). Fix if failure.
- [ ] Confirm automatic Day/Night on already-placed MagicOS 2x2/4x2 widgets, their typography, launcher-tall proportions; older Samsung compatibility.
- [ ] Verify true continuous native PDF with 2-page SMS checklist and 500-page manuals: pinch, search and highlighted hits, text selection and scroll position, status/nav bars, file reopening, plus DOCX still using isolated original reader.
- [ ] Test equipment procedure original PDF/DOCX attach/replace/unlink, old JSON compatibility, export .navref metadata + separate .navlib original-byte restore; ensure broken/missing attachment produces error but retains citation/procedure.
- [ ] Prepare separately signed private QA versionCode>=194 and ORIGINAL-RC20-based safe recovery code greater than QA before requesting HONOR test. Do not publish stable update feed or user installation without confirmed test gate.

## 2026-11-13 — QA3 delivery gate
- [x] Signed main source #43369146008 SUCCESS on SHA 72bda3 after widget manifest correction.
- [x] Fork clean QA3 branch `qa/navtasks-pre1-quality-code194-v2` directly from verified SHA; only version metadata/test/workflow diff (no stale fork, no task logic changes).
- [x] Repair recovery195 versionCode-only test assertion (previous #43368918818 failed 1 test expecting code188; correction commit 9fa25b2).
- [ ] Check recovery195 CI #43370110246 and QA3 CI #43370322277 FULL SUCCESS, confirm private draft binaries, package/cert, APK sizes and SHA256. If failures, investigate and patch.
- [ ] Give user verified private APK194 AND higher-code recovery195 ONLY after both pass; instruct QA2 full profile backup and in-place install, never uninstall or clear data. Stable feed remains OFF.
- [ ] Physical HONOR test: system day/night auto recolor existing widgets, 2x2/4x2 text, native continuous PDF scrolling/zoom/search and PDF equipment attachments, .navlib and .navref backup. Samsung test before 1.0.0.

## 2026-11-13 — HIGH PRIORITY: QA3 PDF opening crashes on HONOR
- [x] Isolate common fault entry point: both library and equipment open new `ReferencePdfActivity` (AndroidX beta), prior QA2 Compose reader worked. **No device stacktrace yet; don't claim exact cause**.
- [x] Stage source fail-closed `NAV_NATIVE_PDF_QA_APPROVED=false` and JUnit routing regression. Preserve manual binaries, metadata, attachment links, tasks and settings.
- [x] Fork private QA4 code196 from hotfix source and higher-code original RC20 recovery197; both remain private draft, stable feed off.
- [ ] Confirm source CI #43373096595, QA4 #43373276093, recovery197 #43373284523 success; verify original cert/package/non-debuggable plus exact APK assets/digests. NEVER give unverified installer.
- [ ] QA4 on HONOR: open existing PDF from My Materials / nested SMS folder / Radar check attachment; navigation, page/zoom/search, Back, light/dark and no crash. Preserve data. Previous viewer only one-page-at-a-time; continuous reader still unresolved.
- [ ] Obtain actual AndroidRuntime logcat of QA3 native viewer crash (if available) before re-enabling beta. Use safe isolated continuous reader development, don't ship as a blind switch.
- [ ] Address remaining widget details after PDF is stable: verify existing widgets day/night toggle on MagicOS without re-adding and typography/layout 2x2/4x2 across launcher dimensions. Test on Samsung. No user-facing stable 1.0.0 until approved.

### QA4 restored-Activity safety gate
- [x] Add early `ReferencePdfActivity.onCreate` guard against restoring native beta fragment from QA3 task stack after in-place update.
- [x] Verify recovery197 CI #43373284523 SUCCESS and uploaded PRIVATE DRAFT APK with original package/certificate, 28.5 MB SHA256 eebcbda91fa29e2b4cf4422b32d155f39dc9edfc866330bd062a83e2964c48c2.
- [ ] Check FINAL QA4 restoreguard workflow #43374148590 SUCCESS and verify its NEW private tag/asset. **DO NOT distribute first QA4 draft** (the earlier #43373276093 passed before restore guard).
- [ ] Install QA4 code196 in-place over user QA3 code194 after profile backup, check PDF from both library and equipment procedure and preserve originals; recovery197 retained but not installed unless necessary. PDF still single-page in QA4.

### QA4 gate completed / physical device check next
- [x] Final signed private QA4 #43374148590 SUCCESS and verified new restoreguard private DRAFT code196 original cert/package. URL and SHA in HANDOFF.md. Old unguarded draft MUST NOT be used.
- [x] Signed private RC20 recovery197 #43373284523 SUCCESS; larger version code197 and original RC20 logic. Stable feed still OFF.
- [ ] User installs code196 over existing QA3 code194 after exports and keeps recovery197 offline; **DO NOT uninstall or delete data**. Verify My Materials PDF, SMS nested PDF, PDF in Radar check attachment, Back, search, repeated opens, and data persistence on restart. Report any remaining force-close.
- [ ] Only AFTER crash-free HONOR acceptance, develop a fault-contained continuous PDF reader and revisit widget automatic day/night + 2x2/4x2 geometry, grammar 'Осталось 1 задача' -> 'Осталась 1 задача'. QA4 fallback is one-page-at-a-time; no claim native beta continuous reader works.

## 2026-11-13 — Hybrid manual preview + "Open with"
- [x] Add "Открыть через / Open with" action to both internal PDF/DOCX preview headers: shared library documents and equipment procedure attachment.
- [x] Use scoped nonexported Android FileProvider and ACTION_VIEW system chooser with temporary read-only URI permissions; correct original display name and MIME; no wildcard roots or user profile access. Preserve internal preview and all existing data.
- [x] Add regression tests for safe MIME mapping, narrow path whitelist, no EDIT/WRITE grant/file scheme, both UI entry points.
- [ ] Verify latest full source CI #43381674656 (or successor if commits race); inspect and fix any build/test failures before QA packaging.
- [ ] HONOR + Samsung device QA: PDF in SMS folder, attached Radar check PDF, DOCX, chooser availability, external app reading, back to NavTasks, missing handler feedback, no crash/data mutation. If a cloud app is selected, it may copy the file outside NavTasks: inform the user.
- [ ] Keep QA4 (code196) crash-free reader and recovery197 intact; next private QA must versionCode >=198, higher-code recovery first, original package/certificate and explicit user test. DO NOT publish 1.0.0/stable feed.

## NEXT — 2026-11-13 user-locked final pre-1.0.0 scope, QA6 gate
- [x] QA5/code198 real HONOR user acceptance: Open with PDF/DOCX and internal preview work.
- [x] Stage isolated lazy vertical PDF QA6/code200; preserve original single-page search reader, native AndroidX beta crash gate OFF, external chooser, all data and business rules.
- [x] Stage higher code201 exact QA5-source emergency rollback with separate private CI.
- [ ] Verify QA6 signed CI #43386652027 and recovery CI #43386746405 SUCCESS, complete unit tests, non-debuggable package com.navtasks.app.glass, historical certificate, correct code200/code201 and private draft APK digests. Do not distribute on CI failure.
- [ ] HONOR test with full profile + separate .navlib export first; download both verified APKs; install ONLY QA6 in place. Check continuous vertical pages, memory on large documents, pinch/zoom, search and fallback, PDF attachments, DOCX, external chooser, day/night, Back/restart and no data loss. Emergency code201 ONLY after failure.
- [ ] After PDF device acceptance: polish widgets 2x2/4x2 geometry and automatic theme; fix rich editor text selection; approve new icon; test on another Android device (Samsung when available); final functional/visual/data/migration smoke and signed 1.0.0 acceptance. No new features. Keep public stable available:false until approval.

### QA6 signed private build/recovery verified; HONOR acceptance NEXT
- [x] CI #43386652027 QA6/code200 SUCCESS and PRIVATE draft APK confirmed SHA256 85d3aa508a10214b96607cbd46377d39a9e600425a027544e735e77575890904.
- [x] CI #43386746405 exact accepted QA5-source recovery/code201 SUCCESS and PRIVATE draft APK confirmed SHA256 2da72f4e471ad10d0d2f7e39232b7be1d2b5b998fe12bd92c1d99ea2fd0c38fc.
- [ ] HONOR owner test: export profile and manual library, download both files, install ONLY QA6/code200 in place; compare continuous scroll/pinch to paged fallback/search, long manual stress, "Open with" PDF/DOCX, data retention. No release acceptance until owner reports.
- [ ] Once QA6 accepted, proceed with widgets, text selection, app icon, second-device qualification and final 1.0.0 (separate approval). Keep stable.json available:false.

## NEXT — QA7 widget polish + blue Open with, 2026-11-13
- [x] QA6/code200 HONOR physically accepted, PDF now vertical by default with optional page search.
- [x] QA7/code202 isolated edits: layouts, compact/wide color & geometry, red missed pill, Russian singular remaining grammar, dynamic app-process widget config registration, 30min host refresh, filled Apple-blue external chooser action.
- [x] Separate exact user-accepted QA6-source versionCode203 recovery branch created with signing workflow.
- [ ] Verify full signed CI #43389549409 / recovery #43389554466 success, original certificate, package name, no debug, private draft APK byte digest; report actual status.
- [ ] HONOR in-place QA7 only after profile + .navlib export and BOTH APKs downloaded, auto Day/Night reflow of existing widgets (app open/background), screen geometry both sizes, task counts/zero/missed/tomorrow, click-through safety, PDF/DOCX chooser, no data loss. If theme change while process fully killed stays stale, document OEM behavior and do not claim instant success.
- [ ] After HONOR acceptance move to rich text selection and new app icon, then second-device test and final regression/release approval. Public stable OFF.

### QA7 signed CI done; next HONOR device check
- [x] Corrected private QA7/code202 signed release CI #43390512011 SUCCESS, original signer verified, APK SHA256 32f94f68ca3a8c36ded7c7401682a47bfe0f8b2a0180fc3ea12651c383fdc2fb, draft untagged-b308b6cb4354ac863e8a.
- [x] Corrected higher-code203 exact accepted QA6-source recovery CI #43390517112 SUCCESS; SHA256 df0da5a77abf1182126e493b3616b9739c4e0fc94d3f494c076cf2fa87053bd9; draft untagged-b13bd3745245aa843011. Ignore older erroneous private draft tags.
- [ ] HONOR export FULL profile + separate .navlib, download both, install ONLY QA7/code202 in place, verify existing 2×2 / 4×2 widgets, System light/dark without widget reinstall, legibility, missed/remaining/tomorrow, tapped widget does not complete tasks, blue Open with PDF/DOCX, saved data.
- [ ] If failure, use code203 fallback without uninstall, after data export; next Android versionCode must exceed 203. No stable publication until owner screenshot QA. Then only rich-text selection + new icon, second device, final 1.0.0 signed QA and approval.

## NEXT — QA8 HONOR widget acceptance gate, then locked 1.0.0 scope
- [x] Create isolated QA8/code204 with explicit light/dark layouts per 2x2 and 4x2 to prevent stale night background on Day text, move 84% beside 5dp progress bar at 16sp, near-black night surface.
- [x] Preserve QA7/source as code205 emergency rollback and isolated CI.
- [ ] CI #43410980825 and recovery #43410986526 PASS all JVM tests/signature/non-debuggable/version/package and create unique PRIVATE draft tags + APK digests.
- [ ] BEFORE install: export full profiles plus .navlib, download both verified APKs, install ONLY QA8/code204 over installed QA7/code202, no uninstall/clear data.
- [ ] HONOR: both existing widget placements, app in System and explicit Day/Night, switch Day->Night->Day multiple times without deleting widget; check background AND type change together, near-black night, readable light, progress percent 16sp beside bar, missed badge and counts, app launch via widget, data retention. Check process foreground/background; if fully killed OEM defers redraw it can take until next host update; do not claim instant until verified.
- [ ] Once accepted: rich-text selection, new app icon, second-device Android/Samsung test, final signed 1.0.0 regression/release user approval. No new features.

### Signed QA8 handoff / HONOR acceptance NEXT
- [x] QA8/code204 CI #43410980825 SUCCESS (original signer, tests, release). APK SHA256 `5ec4befdc91855c3cadc1c3e477e1c3d60ec9f833762450a3ba65206c408c5ea`, private draft `untagged-83651a9e71bcdfeb423c`.
- [x] Exact QA7-source recovery/code205 CI #43410986526 SUCCESS, no application code drift. SHA256 `3887d64ed7750b4a3b6ff40338b3f84d1dccee1aa4c523ca491a6adaabcf1cc8`, private draft `untagged-b15372b28147997a4a7a`.
- [x] stable feed verified still `available:false`, main untouched.
- [ ] HONOR: export full profile + .navlib, download both APKs, install ONLY QA8/code204 IN PLACE. Confirm existing 2×2 and 4×2 auto-swap background AND text from Day->Night->Day without readding, near-black night, 16sp wide % beside progress bar, all task/missed figures, clickable widget opens app, blue Open with PDF, profiles intact.
- [ ] If broken, install exact-QA7 source recovery205 IN PLACE, never clear data. User must approve QA8 visually before text-selection and icon stage; then Samsung and signed 1.0.0 after QA.

## NEXT — finish widget alpha and move to remaining release gates
- [x] Change QA9 dark widget fill from almost opaque black `#F8000000` to subtle translucent `#E0000000` in isolated branch; preserve QA8 visual geometry, click targets, day/night design, all app logic.
- [x] Confirm limits: Android requires dynamic CONFIGURATION_CHANGED registration (already implemented) and updatePeriodMillis minimum 30 minutes (already configured). No always-on poll; acceptable limitation if killed process prevents instantaneous change.
- [x] Stage exact QA8-source private signed recovery code207 before QA9 install.
- [ ] Verify signed QA9 code206 CI #43413317881 and recovery code207 CI #43413325570 pass, package/cert/version, private APK digest.
- [ ] Device: export full profile and .navlib, download BOTH verified assets, install only QA9/code206 without uninstall, inspect black transparency and contrast; check actual theme switch and repaint after opening app, PDF/blue chooser still works. User can accept OEM delay without more widget iterations.
- [ ] Then rich-text selection and new app icon in separate QA branch; second-device test; 1.0.0 full regression and explicit approval.

### Signed QA9 builds verified; user device acceptance NEXT
- [x] QA9/code206 CI #43413317881 PASS, APK SHA256 `2bdd3ab64f41dbbb8d8a40ed8734c3f6036837cd631b8f287562d529261b30f3`, private draft `untagged-0b3cac527bf65bd1e333`.
- [x] Exact QA8 source recovery/code207 CI #43413325570 PASS, APK SHA256 `6a35caacd46bbd9d301f4d26cddd6ee64d7af6014ef656e2797c46a078768121`, private draft `untagged-0652f546156cce7cf29a`.
- [x] Public stable `available:false`, main unchanged. Background-killed immediate OS Night->Day updates documented best-effort limitation.
- [ ] HONOR: back up full profile and .navlib, download BOTH verified APKs, install ONLY code206 in place; check slightly translucent black in both already-placed widget sizes, readability, no data loss, blue Open with and PDF remain unaffected. Recovery code207 only if regression.
- [ ] Next rich text selection and new icon; another Android phone; final complete regression + authorized signed 1.0.0 release, no new features.

## NEXT — QA10 selection handles before icon
- [x] Diagnose user's exact 1-word-per-handle-drag bug to Compose Rich Editor 1.0.0-rc14; upstream v1.0.0 fixes Android handle selection PR #746/#748.
- [x] Isolate stable Rich Editor v1.0.0 version bump in QA10/code208 without touching NavRichNoteEditor body, HTML formats or business data; guard test.
- [x] Stage QA9 exact app-source code209 emergency recovery.
- [ ] Verify compatibility and JVM tests/signing from QA10 CI #43416622629 and recovery CI #43416631675. Any new Kotlin/Compose requirement is a risk to explicitly evaluate before doing a broad migration.
- [ ] Download private signed APK and higher-code209 recovery only after verification; full profiles + .navlib export; install QA10 IN PLACE, never uninstall/clear data.
- [ ] HONOR user selection tests: drag over several words/lines continuously; select all, copy, cut, paste; apply B/I/U/color to multiword region; Save and reopen an EXISTING formatted ECDIS note; test new Russian note and keyboard; confirm profiles and tasks unchanged.
- [ ] After user accepts selection, next only new NavTasks icon, second Android device, final signed 1.0.0 QA and authorized publication.

### QA10 signed build ready — HONOR actual handle-drag validation NEXT
- [x] Upstream stable Rich Editor 1.0.0 includes fix for Android handle-drag selection; no Kotlin/Compose project upgrades needed to compile.
- [x] QA10/code208 original-signer private signed CI #43416622629 SUCCESS; APK SHA256 `3b6535acd2330687fe2aab764175735c8b3f9667fb869e5d3aa3bcef15c64cd0`, private draft untagged-267c5792eb36c2f2cda1.
- [x] Exact QA9-source higher-code209 recovery original-signer CI #43416631675 SUCCESS; APK SHA256 `a9eabf4118c0b6144e3eae6dfca30a3bbef70f06137ad8c0466e29a97a2ac35c`, private draft untagged-c53c550e91a0dd2bd59f.
- [x] Public stable feed `available:false`; main untouched.
- [ ] HONOR: backup full profile + .navlib, download both, install ONLY code208 in place, double-tap an existing Handover ECDIS word, drag ONE handle across several words AND multiple lines in ONE continuous gesture, check select all/copy/cut/paste and formatting B/I/U/color, Save/reopen original formatted notes and tasks. If regression, install recovery209 in place; no data clearing.
- [ ] After user explicitly accepts selection behavior: standalone new NavTasks app icon (requires visual approval), second Android device (Samsung), complete regression and authorized 1.0.0 release. No other feature scope.

## NEXT — QA11 launcher icon device verification, then release gates
- [x] QA10 rich text selection verified on HONOR by user.
- [x] Isolated approved icon into two-part Android adaptive icon (default system-blue background + floating compass/cards foreground). North arrow orange, three checklist dots, center blue check, four compass markers. Android monochrome layer provided.
- [x] Add source-based `export_magic_glass.py` to create package-mapped **fully transparent** PNG and a non-installable overlay ZIP for existing HONOR MagicOS `.hnt`; no ability to automatically swap OEM theme via manifest.
- [x] Stage signed QA11/code210 and exact QA10-source code211 recovery workflows.
- [ ] CI #43422885390 and #43422895671 PASS JVM tests, package/cert/nondebug, release and exported transparent PNG checks; verify SHA-256 and links. No APK sharing until validated.
- [ ] HONOR: full profile + .navlib backups, download both APKs, install ONLY QA11/code210 IN PLACE; verify blue default adaptive icon and no regressions. Check static/adaptive clipping and launcher updates without deleting app. Transparent MagicOS theme overlay needs active HNT theme merging and on-device confirmation (current theme has NOT been uploaded).
- [ ] Another Android phone (Samsung ideally): normal colored icon, monochrome-themed icon, UI/notifications/widgets/backup/PDF, compatibility and no data loss.
- [ ] Complete release candidate QA (all existing modules, older-version update/migrations, signing key, stable update metadata), get explicit owner approval and publish 1.0.0. No new feature scope.

### QA11 signed builds verified; next HONOR / Samsung and release acceptance
- [x] Approved icon design implemented in existing Android adaptive icon layers and test suite.
- [x] Signed QA11/code210 CI #43423620341 PASS, APK SHA256 `44358f77552a3029d7c8bf9d743fdb317a65fa426ae6e982f9b5f085284c7538`, private draft `untagged-76ac11a0e661458f4c20`.
- [x] Full-color MagicOS transparent package PNG 512×512 SHA256 `f05d2a62ea129438dcee45f2071f7ad2ba6381eb020f0c9a73e559c06d25102d` and icon-only overlay ZIP exported and attached; NOT automatically selected from APK. Needs merge into user's current Magic Glass HNT, not installed as a new theme.
- [x] Exact QA10 source signed recovery/code211 CI #43422895671 PASS, SHA256 `7059ba88d448407381ae4b1e532018316b2542e4f412ed6b72592f5b07c3bb2c`, private draft `untagged-f3ab03b41aa9b6f9f7c4`.
- [x] Original cert/package/nondebuggable verified; stable.json `available:false`; main untouched.
- [ ] HONOR owner: export full profiles + separate .navlib, download signed QA11 and emergency code211, install ONLY QA11/code210 in place, verify normal blue launcher icon and functional app; no removal/data clearing. For true Liquid Glass without blue tile, safely merge named PNG into existing HNT; current theme file NOT provided and therefore not modified. Owner screenshot acceptance required.
- [ ] Verify on another device (Samsung): colored adaptive icon, monochrome-themed icon, launcher, widgets, task/AMOS/Service/Handover/notices, notes, PDF, backups and network update. Record findings.
- [ ] Final 1.0.0 full regression including direct long-version updates/restore, original signing, versionCode above any installed recovery, stable channel rollout only after explicit approval.

## NEXT — QA15 code218 PDF focal-point release-candidate gate
- [x] QA12 signed CI PASS: name-based safe profile import and selected Service request number. Distinct vessel profiles with same `ship-default` ID must add, not replace. AMOS request/reference roundtrip covered.
- [x] QA13 signed CI PASS: `.navref` v2 includes full PDF/DOCX originals; SHA-256/size verified before import, legacy v1 warns.
- [x] QA14 signed CI PASS: read-only Handover DOCX and PDF exports for successors using Word/iOS/PC.
- [x] QA15 code218 isolated: pinch focal scroll geometry for single-page and continuous PDF, selects touched LazyList page rather than firstVisibleItemIndex; unit tests added. Higher-code219 exact QA14 source recovery isolated.
- [ ] Verify QA15 signed CI #43472051215 and recovery #43472078535 success, correct app ID/com.navtasks.app.glass, same historical signing certificate, versionCode218/219 and private draft APK SHA-256. No rollout on failure.
- [ ] HONOR owner: export FULL profile + separate .navlib, download both APKs, install ONLY QA15 in place, pan one finger (must NEVER zoom), pinch at upper-right/lower-right/left of page (focus preserved), long continuous PDF multiple pages, paged search and external chooser, no crash or data loss.
- [ ] HONOR ↔ Samsung: 2 differently named vessel profiles with shared default ID (must ADD, not replace), same-name confirmed replace, personal unaffected; service and AMOS request roundtrip; equipment .navref with real PDF/DOCX original; Handover DOCX/PDF opens on Android, iPhone/PC with Cyrillic. Test offline, preserve source backups.
- [ ] Final independent code/security/regression audit and user sign-off before 1.0.0. main and public stable channel stay unchanged until approval.
- [ ] AFTER 1.0.0 only: opt-in curated shared seafarer reference catalog with downloadable content, Handover photo attachments.

### QA15 PRIVATE CI PASS — next HONOR & Samsung signoff
- [x] Signed QA15/code218 CI #43472806069 SUCCESS (original key/package, all JVM tests and release); SHA256 `2f974f9f5680820467495ecd377d99d47d3112c0140bc36b7115712e8dc2521a`, draft `untagged-8910b70a567452e8a658`. First CI failed restricted Compose scope, fixed; only successful artifact is distributable.
- [x] QA14 exact-source recovery/code219 CI #43472078535 SUCCESS, app logic untouched; SHA256 `f772bc175f8d0dc2268de8a3639cb41360439287b30b30f3f78db868cb19a471`, draft `untagged-fb68cac5e5480e56d2e4`.
- [x] Main untouched, public stable feed `available:false`.
- [ ] HONOR FULL profile backup and separate .navlib; download both verified APKs; install ONLY code218 without uninstall/clear. Pinch/scroll PDF multiple pages, paged mode with search, swipe one finger and landscape, compare original old behavior.
- [ ] Samsung ↔ HONOR roundtrip: 2 different-named vessel profiles same `ship-default` ID; same-name confirmed replacement only; preserve personal; selected AMOS/Service requests; equipment card with embedded PDF/DOCX opens on recipient; Handover DOCX/PDF opens on Android/Windows/iOS (Cyrillic).
- [ ] User signs off, then independent full audit and signed 1.0.0 candidate (versionCode >219 if any rollback installed); stable metadata publish only after explicit approval.

## NEXT — 2026-11-15 pre-stable RC1 device and release gate
- [x] QA17/code222 original signed build CI success; owner installed and reports original PDF now transfers; QA16/code223 signed recovery saved.
- [x] Isolate final settings/reference UI from QA17: global Settings retains Full Profile Transfer and Selective Exchange only. Standalone Reference: exactly 3 root categories (Inspections -> SIRE 2.0 / VIQ7, Equipment, My Materials), with an independently accessible Import/Export route. Preserve Reference v2 PDFs, old v1 warning, profile/AMOS/Service data models and docks.
- [x] Add source-level navigation/regression tests; prepare version 1.0.0-rc1 code224 with original signer workflow and private DRAFT only.
- [ ] Verify RC1 signed CI full JVM suite, exact application ID, historical signer and versionCode224; if error fix only isolated fault before device QA.
- [ ] Owner: export complete profile + separate `.navlib`, install signed RC1 in place; verify Settings only two transfers, Reference three categories, SIRE2 and VIQ7 navigation/search, Equipment import/export with original PDF/DOCX in HONOR and Samsung, My Materials intact, day/night and bottom insets, Handover DOCX/PDF, tasks/AMOS/Services/profile data.
- [ ] Independent final production regression and source/history security review after repository visibility made public; only then update main and publish signed stable 1.0.0 + `NavTasks-Updates/channels/stable.json` with owner release sign-off.

- [ ] Confirm separately signed exact QA17-source emergency fallback code225 CI PASS before RC1 owner install; do NOT use lower code223 as rollback from RC1/code224. Stable 1.0.0 requires code >=226 after any recovery.

### 1.0.0 RC1 CI completion — physical acceptance NEXT
- [x] RC1 code224 original cert/JVM/release/private-draft CI `43606021649` PASS, SHA256 `20c5dc654694f097201d6f931278efca45e081f54d724e2aa1c0a25981104ddd`.
- [x] Exact QA17 app-source higher-code225 recovery original cert/JVM/release/private-draft CI `43606025556` PASS, SHA256 `1d25699c1d2b7b98460b50ce0ad0deff4d86d9ba5769d87a8d71b2ac08e75897`.
- [ ] HONOR: export FULL profiles and `.navlib`, save recovery APK, install ONLY RC1 code224 in place (do not uninstall / reset data), verify only two transfer blocks in Settings and three Reference root cards with SIRE/VIQ7 INSIDE Inspections, Equipment import/export, My Materials, profile/task/AMOS/Services/Handover preservation, theme, docking and search.
- [ ] HONOR↔Samsung: PDF/DOCX equipment originals still open after transfer in both export routes; inspect full/restricted imports, Work order request values, Handover DOCX/PDF Cyrillic, widgets and notifications; no data loss.
- [ ] Audit public repository history/metadata and all 1.0.0 release safety checks; prepare PRODUCTION 1.0.0 code >=226, publish signed APK to `NavTasks-Updates` expected pinned URL and enable stable.json ONLY after user's RC1 approval and final verified signature/hash. Continue optional monthly releases, no forced updates.

## 2026-11-15 — first OTA acceptance check (1.0.0 -> 1.0.1)
- [x] Create exact v1.0.0-derived minimal 1.0.1 release (code227), remove visible Glass in Settings footer, preserve package/signing key/data.
- [x] Run all 396 JVM tests and verify APK package/certificate/release ID: `43613297380` SUCCESS.
- [x] Mirror exact original-signed 1.0.1 APK to NavTasks-Updates release, checksum match; activate stable update metadata only after verification: `43613796958` SUCCESS.
- [ ] On user HONOR and friend device initially with 1.0.0: Settings -> Check for updates while online, verify 1.0.1 offered, choose Download/Install via system APK installer, verify saved profile/tasks/Handover/Reference PDF still exist, Settings shows NavTasks 1.0.1 without "Glass".
- [ ] Confirm start-up auto-update offer with toggle ON (check throttled by previous successful network check); manual check available independently. Do not assume instant startup offer if last check was recent.
- [ ] If accepted: start isolated next beta branch and implement opt-in beta channel for user-selected future naming (legacy parser currently handles older test1-3, not new beta.N). Stable users must remain on stable feed.
