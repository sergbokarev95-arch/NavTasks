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


## Immediate — HONOR verification of full-app 0.4.60 Exact-Lab candidate (2026-11-08)
- [x] Preserve stable `backup/navtasks-0.4.59-before-liquid-glass`; port approved Glass Lab 0.1.5 material/lens/body and exact dock spacing without R2 blur edits to feature `feature/navtasks-liquid-glass-0.4.60-candidate`.
- [x] Keep original 0.4.59 Beta package/signing, bump code 81→83 to allow in-place upgrade; CI 42399961454 SUCCESS including app tests; signed candidate https://github.com/sergbokarev95-arch/NavTasks/releases/download/v0.4.60-exactlab-candidate/NavTasks_Glass_0.4.60_ExactLab_Beta.apk.
- [ ] **User must export complete profile backup before upgrading existing NavTasks**; do NOT uninstall the installed app. Confirm task/profile data remain on upgrade.
- [ ] Test on HONOR: glass look vs accepted Lab 0.1.5, card-border refraction (different dark UI background will affect apparent strength), text not competing with button labels, dark/light, smooth scroll/no shimmer, proportional active lens overhang, all tab/navigation/back and bottom FABs.
- [ ] Only after visual and functional acceptance, consider merging into `main` and then making other action buttons use a reusable material one by one. Stable 0.4.59 release is unchanged.


## Safe installation path for ExactLab 0.4.60 candidate
- [x] Complete signed in-place recovery Haze `0.4.61`/versionCode84, from stable 0.4.59 source only; CI `42400505251` SUCCESS, aapt package/version verified; https://github.com/sergbokarev95-arch/NavTasks/releases/download/v0.4.61-haze-rollback/NavTasks_Stable_Haze_Rollback_0.4.61.apk.
- [ ] Before candidate 0.4.60 installation, export complete profile backup. DO NOT uninstall NavTasks. Install signed 0.4.60 candidate as an update, test look/performance/data in Day/Night.
- [ ] If rejected, install signed Haze recovery 0.4.61 OVER 0.4.60, not as a downgrade; confirm data preserved.
- [ ] Only after HONOR approval merge the exact material into main; action buttons follow individually. Original 0.4.59 source stays protected.


## Priority: HONOR compare unchanged dock across three backdrops
- [x] Create isolated diagnostic variant of full NavTasks with same Backdrop and same Glass Lab 0.1.5 dock, switch modes outside capture: ЭКРАН, БЕЗ HAZE, ЭТАЛОН.
- [x] CI 42402960923 SUCCESS including original dock SHA gate and all app tests; small signed ZIP https://github.com/sergbokarev95-arch/NavTasks/releases/download/glasscapture-v0.4.60-diagnostic1/NavTasks_Glass_Capture_Diagnostics_0.4.60.zip.
- [ ] User installs separate `NavTasks Glass Check`, tests all three in night/day; scrolls text and bright card borders through dock and records screenshots at analogous positions, reports shimmer/performance.
- [ ] Diagnose: ЭТАЛОН matches Lab → target scenes/colors; БЕЗ HAZE differs from ЭКРАН on same content → investigate source capture/Haze; ЭТАЛОН differs from Lab → investigate root composition/device density.
- [ ] Only then design minimal controlled correction with tests. Do not merge speculative changes into production or touch existing task/card business logic.


## Current priority — HONOR test opaque card surfaces Preview 1 (2026-11-08)
- [x] User confirmed Live Haze and Live no Haze glass nearly indistinguishable, Lab reference looks much better. Adopt option 1: role-specific, opaque HIG-style card gradients/edges in shared navGlass; no changes to screen geometry or task state logic.
- [x] Preserve original `NavLiquidGlassDock.kt`, `NavLiquidDockOptics.kt`, `NavTasksUi.kt`; isolated `feature/liquid-glass-surface-preview` CI 42404664493 SUCCESS; signed compact ZIP https://github.com/sergbokarev95-arch/NavTasks/releases/download/glasssurfaces-preview1/NavTasks_LiquidGlass_Surfaces_Preview1.zip.
- [ ] User installs separate `NavTasks Glass Surface` (no production data mutation); if needed imports copy of profile; checks Today, monthly task list under dock, nested tasks, calendar, Works/AMOS/Services/Handover and profiles for attractive Day/Night surfaces, labels/status contrast, scrolling, refracted borders and absence of flickering.
- [ ] If accepted, plan safe in-place signed update (versionCode > current candidate83, and rollback with higher version), without modifying business logic. If not, tune just tokens centrally. Do not roll out optical glass to all inputs/cards.


## Next: verify and promote Liquid Glass + opaque gradient surfaces
- [x] User accepted static card materials `glasssurfaces-preview1` in both themes; requested darker settled grey lens in Night and dock lowered 4dp.
- [x] Isolated Preview2 with exactly two scoped tweaks; full app CI 42438614953 SUCCESS, signed ZIP https://github.com/sergbokarev95-arch/NavTasks/releases/download/glasssurfaces-preview2/NavTasks_LiquidGlass_Surfaces_Preview2.zip; same separate package `com.navtasks.app.glasssurface`.
- [x] Prepared signed in-place 0.4.62-beta code85 candidate with Preview2 exact UI; CI 42438818985 SUCCESS, ZIP https://github.com/sergbokarev95-arch/NavTasks/releases/download/v0.4.62-liquid-glass-surfaces-candidate/NavTasks_LiquidGlass_0.4.62_Beta_Update.zip.
- [x] Prepared signed no-uninstall Haze recovery 0.4.63-beta code86 from stable 0.4.59 code, CI 42438923548 SUCCESS, ZIP https://github.com/sergbokarev95-arch/NavTasks/releases/download/v0.4.63-haze-rollback/NavTasks_Stable_Haze_Rollback_0.4.63.zip.
- [ ] User visually tests Preview2 on HONOR: Day/Night, darker graphite lens improves active blue, 4dp lower dock spacing to Android gesture bar, proportional lens overflow and card-edge refraction. This must be checked on device (CI does not establish visual acceptance).
- [ ] After user approval, export full profile first; user can install candidate 0.4.62 over existing 0.4.60, without uninstall. Verify all profiles/tasks/AMOS/Services/widgets/notifications and swipes. If rejected, recovery v0.4.63 code86 upgrades over it to stable Haze (without uninstall). Any future in-place app version code ≥87.
- [ ] Merge into main only after app acceptance; continue glass material to '+' / calendar / settings one component at a time, do not globally convert ordinary cards into expensive shader surfaces.


## Next device test: Glass UX Preview 1 (2026-11-08)
- [x] Prepare separately installed signed `NavTasks Glass UX` APK and compact ZIP, CI 42440578629 SUCCESS: https://github.com/sergbokarev95-arch/NavTasks/releases/download/glassux-preview1/NavTasks_Glass_UX_Preview1.zip.
- [ ] On HONOR create ≥2 TEMPORARY profiles in separate app, use two-finger inward pinch from regular Today/Tasks tab, ensure popup appears centered with background blurred; tap changes profile and restores corresponding tasks/Handover/Services/AMOS. Back/tap outside dismisses; one-finger swipes still work; no false openings while scrolling.
- [ ] In Settings, scroll all the way to last panel; it should clear the system gesture line without black/coloured cut-off rectangle or persistent empty footer. Android OS gesture line itself is NOT hidden.
- [ ] Check floating '+' and Calendar now use direct-scene optical Liquid Glass with unchanged action semantics on Android13+; dark/day and fast scroll have no flicker.
- [ ] After device approval, progressively adapt header circle buttons, other shared button families, dialogs and popovers, one family at a time; keep same signed Beta package for later in-place migration with incremented versionCode and tested recovery. Do not edit existing app data or stable main prior to acceptance.


## Highest priority: HONOR test quick profile gesture + Settings footer Preview1
- [x] Add non-consuming, root-only two-finger pinch and centered profile selector using original five-store select/switch contract.
- [x] Fix Settings layout's extra navigation footer with bounded scroll + bottom-only system navigation safe-area.
- [x] Signed isolated ZIP CI `42441508734` SUCCESS; https://github.com/sergbokarev95-arch/NavTasks/releases/download/glassquick-preview1/NavTasks_Glass_QuickProfiles_Preview1.zip.
- [ ] User installs separate `NavTasks Glass Quick`, creates 1-2 disposable vessel profiles, tests two-finger pinch in Today/Work/Handover/Tasks, switch to/from Personal and another vessel, checks one-finger scrolling/swipe tabs and Back/outside dismissal.
- [ ] User tests Settings bottom/footer in Day and Night: last panel remains scrollable, no clipped navy region above Android gesture bar.
- [ ] After approval create same-ID signed in-place version with code greater than all installed/recovery versions (existing 0.4.63 code86 recovery, preview separate code87); prepare higher-code stable Haze fallback, full profile export first, never uninstall.
- [ ] Then adapt each circular '+' / Calendar / Settings action to reusable true Liquid Glass in its own incremental tests. Do not blanket change all buttons or profile logic.


## Next: verify first true-glass floating action (+) on HONOR
- [x] HONOR user accepted Quick Profile pinch and Settings footer Preview1.
- [x] Add direct Backdrop/Liquid Glass to ONLY Tasks '+' action in separate test app, retaining 54dp, 28dp blue glyph, haptics and editor action, unchanged Haze fallback.
- [x] Signed CI 42442451411 SUCCESS and small ZIP https://github.com/sergbokarev95-arch/NavTasks/releases/download/glassplus-preview2/NavTasks_Glass_Plus_Button_Preview2.zip (10.9MB).
- [ ] Test on HONOR: scroll Task card title/border under '+', compare refractive rim and blue glyph with dock in day/night; tap opens editor, normal scrolling and Quick Profile gestures unaffected.
- [ ] Only after success, adapt calendar circular shortcut, then Settings icons, dialogs/other controls in small independent changes. Do not globally mutate task UI, profile stores or existing workflows.
- [ ] Later prepare signed same-package upgrade to installed NavTasks, back up full profile first, maintain rollback with higher version code, never uninstall or merge unverified code.


## Next: HONOR acceptance of ONE full 0.4.64 ZIP (no repeated preview downloads)
- [x] User liked Glass Plus Day; requested all relevant round/floating buttons in one app and only softer white day rim.
- [x] Freeze exact shader/lens/animation and data sources; share original Backdrop across circle buttons, soften DAY rim only. Signed in-place 0.4.64 code89 ZIP, CI 42444219901 SUCCESS: https://github.com/sergbokarev95-arch/NavTasks/releases/download/v0.4.64-liquid-glass-actions-candidate/NavTasks_LiquidGlass_0.4.64_Beta_Update.zip.
- [x] Same-ID no-uninstall Haze recovery 0.4.65 code90 ZIP, CI42444287505 SUCCESS: https://github.com/sergbokarev95-arch/NavTasks/releases/download/v0.4.65-haze-rollback/NavTasks_Stable_Haze_Rollback_0.4.65.zip.
- [ ] User exports FULL profile first, installs signed 0.4.64 update over installed NavTasks without uninstalling. Check task/profile/AMOS/Services/Handover/history, settings/footer, pinch, all buttons action semantics, Day/Night border contrast and scrolling refraction/no flicker.
- [ ] If rejected, install 0.4.65 recovery over 0.4.64 without uninstalling. Future in-place app code >=91 after code90 rollback.
- [ ] Upon HONOR approval only, promote accepted feature source to stable `main` and archive tagged release. Continue refining inline controls only as needed, preserving design, not adding shader to every text/row control.


## Next: HONOR verify 0.4.66 new single-green check (no additional features)
- [x] User approved complete Liquid Glass circular button rollout in 0.4.64.
- [x] Build 0.4.66-beta code91 same-ID: solid green190ms then 250ms downward 48dp fade, eliminate double flashes/up-kick/stretch; CI 42445806856 SUCCESS. https://github.com/sergbokarev95-arch/NavTasks/releases/download/v0.4.66-completion-motion-candidate/NavTasks_Completion_Check_0.4.66_Beta_Update.zip
- [x] Emergency stable Haze same-ID recovery 0.4.67-beta code92, CI 42445698166 SUCCESS. https://github.com/sergbokarev95-arch/NavTasks/releases/download/v0.4.67-haze-rollback/NavTasks_Stable_Haze_Rollback_0.4.67.zip
- [ ] User exports full profile and installs 0.4.66 over 0.4.64 without uninstall. Test ordinary task check and last subcheck, other-date confirmation, visual green first and downward motion after, no duplicate flash. Test smooth day/night and original Liquid Glass.
- [ ] If accepted, promote verified feature source to stable main; update documentation and version policy. If regression, recovery Haze code92 can upgrade without uninstall but returns to older non-Liquid-Glass interface. Future same-package version code>=93 after recovery.


## Next: verify Services Requests filter in v0.4.68 on HONOR
- [x] Add compact third tab `Заявки · N` showing both planned and unplanned with current saved AMOS number, excluding planned without, completed/historical/closed. Keep Works and Closed, all service card actions/search/counters.
- [x] Unit tests for planned/unplanned, blank, closed, completed, historical and restored; full CI 42447897901 SUCCESS. Small signed same-app ZIP https://github.com/sergbokarev95-arch/NavTasks/releases/download/v0.4.68-service-requests-candidate/NavTasks_Services_Requests_0.4.68_Beta_Update.zip.
- [x] Safe signed recovery of previous v0.4.66 Liquid Glass (not Haze!) as code94 v0.4.69 CI42447985380 SUCCESS https://github.com/sergbokarev95-arch/NavTasks/releases/download/v0.4.69-glass-recovery/NavTasks_Glass_Restore_0.4.69_Beta.zip.
- [ ] User exports full profile, installs v0.4.68 code93 over working app (never uninstall), verifies `Работы` count unchanged, `Заявки` correctly counts both work types with numbers, `Закрытые` unchanged, search and button actions work, updates after serviced/closed, Night/Day Liquid Glass stable.
- [ ] If approved promote tested feature to production main and tag; if regression, install v0.4.69 code94 over it. Future same package versionCode>=95 after recovery.


## Next: HONOR accept rich text editor 0.4.70 / keyboard inputs
- [x] Preserve all Liquid Glass source/material/optics and TaskStore; isolate branch, freeze 0.4.68 baseline, add IME sentence auto-capitalization/autocorrect in text and explicitly disable in AMOS identifiers.
- [x] Add compact WYSIWYG B/I/U/S/colors/marker for notes Services/AMOS/Handover/checklists and optional HTML with existing plain fields; full JSON history/export regression tests. Signed v0.4.70 code95 CI42455684933 SUCCESS https://github.com/sergbokarev95-arch/NavTasks/releases/download/v0.4.70-rich-notes-ime-candidate/NavTasks_RichNotes_IME_0.4.70_Beta_Update.zip.
- [x] Signed same-Glass original 0.4.68 recovery as v0.4.71 code96 CI42455220247 SUCCESS https://github.com/sergbokarev95-arch/NavTasks/releases/download/v0.4.71-glass-recovery/NavTasks_RichNotes_Restore_0.4.71_Beta_Update.zip.
- [ ] User exports full profiles first and installs code95 without uninstall. Test HONOR keyboard with Russian/English after ". " and initial entry, selection & B/I/U/S/color/highlight, saved/reopen/backup preserving formatting, long-text scrolling, dark/day styling, dialog keyboard and search; all prior Glass/check/Handover/Services Requests functional regression.
- [ ] Only if accepted merge app source into main and mark stable. If rejected, install code96 over it; avoid editing styled notes while old editor installed; future code>=97 if rollback was used.

## Next: HONOR accept direct Handover Edit access 0.4.72
- [x] Diagnose 0.4.70 Handover long-detail dialog: Edit was buried at bottom of a scrollable action list; visible edit was not discoverable.
- [x] Back up rich-notes baseline, add 44dp accessible blue edit pencil in detail dialog title, reuse original editor and save functions, remove duplicate scroll-footer Edit. No store/schema/Glass changes.
- [x] Signed same-ID 0.4.72 code97 candidate, full CI 42458988093 SUCCESS, https://github.com/sergbokarev95-arch/NavTasks/releases/download/v0.4.72-handover-edit-candidate/NavTasks_Handover_Edit_0.4.72_Beta_Update.zip.
- [x] Higher-code safe 0.4.73 code98 exact-original-rich-notes fallback, full CI 42459070578 SUCCESS, https://github.com/sergbokarev95-arch/NavTasks/releases/download/v0.4.73-handover-edit-recovery/NavTasks_Handover_Restore_0.4.73_Beta_Update.zip.
- [ ] Export full profiles; install 0.4.72 over installed Beta without uninstalling. On HONOR open ECDIS with a long note and tap pencil at top-right. Edit both title and WYSIWYG text, save/reopen, verify original completion mark/history, both themes, keyboard and profile switching.
- [ ] If accepted, merge tested feature to main in a carefully reviewed promotion. If rejected, in-place update to fallback98 with intact data, do not uninstall. Future same-ID code >=99 after rollback.

## Next: HONOR test Apple color palette candidate v0.4.74 (separate from card design)
- [x] Find semantic token mismatch and screenshot exact gray background pixel #F4F5F7.
- [x] Apply Apple HIG system blue Day/Night on actions (#0088FF/#0091FF), orange (#FF8D28/#FF9230), contrasting text variants (#0071E3/#5CB8FF and warning Day #BE5300), neutral text and flat Day background. Convert calendar partial-orange indicator. Preserve Liquid Glass dock optical files and `NavSolidCardTokens` unmodified.
- [x] Test default vs high-contrast text colors with `ApplePaletteTest` and `UiStatusContrastTest`, full JUnit/assemble; final signed v0.4.74 code99 workflow 42484347829 SUCCESS. Only distribute `v0.4.74-apple-palette-verified` https://github.com/sergbokarev95-arch/NavTasks/releases/download/v0.4.74-apple-palette-verified/NavTasks_Apple_Palette_0.4.74_Beta_Update.zip.
- [x] Same-ID optional v0.4.75 code100 exact v0.4.72 UI recovery, CI 42483665590 SUCCESS https://github.com/sergbokarev95-arch/NavTasks/releases/download/v0.4.75-apple-palette-recovery/NavTasks_Palette_Restore_0.4.75_Beta_Update.zip.
- [ ] User exports FULL profiles, installs v0.4.74 over current beta without uninstall; visually checks Day neutral gray #F4F5F7, button/icon/text blue, status orange, Calendar partial orange, day/night legibility and no changes to dock optical behavior, card material, app data or controls.
- [ ] SF Pro cannot be legally bundled for Android under Apple's published license. Ask user to approve open-licensed Inter or choose another legal SF-like family; **do not falsely label replacement as SF Pro**.
- [ ] Once device approves palette, choose new card surfaces separately as user requested. Promote source to `main` only after approval. If rejected install code100 higher-code recovery (never uninstall); future code >=101 after recovery.

## NEXT: verify 0.4.76 white-on-solid-Apple-blue selected states on HONOR
- [x] Screenshot-based audit: Settings language / theme, Today day chips, Services work/request filters, summary counts & filed AMOS request, AMOS, profile picker, Tasks/Plan, Rich Editor toolbar, selective export.
- [x] Back up verified signed 0.4.74 source at `backup/navtasks-0.4.74-before-apple-blue-selection`; scoped selection styling in `feature/apple-blue-selection-0.4.76-beta`. `NavPalette.selectionFill=#0071E3`, white foreground >=4.5:1 in both themes, normal Apple system blue still #0088FF/#0091FF.
- [x] Guard unchanged Liquid Glass optics, dock, static cards, stores; add `AppleBlueSelectionTest`. CI `42490690406` SUCCESS and signed same-ID v0.4.76/code101 ZIP https://github.com/sergbokarev95-arch/NavTasks/releases/download/v0.4.76-apple-blue-selection-verified/NavTasks_Apple_Blue_Selection_0.4.76_Beta_Update.zip.
- [x] Prepare optional exact former 0.4.74 UI fallback v0.4.77/code102, CI `42490737993` SUCCESS; https://github.com/sergbokarev95-arch/NavTasks/releases/download/v0.4.77-blue-selection-recovery/NavTasks_Blue_Selection_Restore_0.4.77_Beta_Update.zip. DO NOT use unless needed; no uninstall.
- [ ] User exports full profiles and installs 0.4.76 over current Beta; verifies white selected labels in Settings, Today, Services/AMOS (including populated AMOS request tile), Task frequency, calendar current day, selected profile and rich-text formatting; checks readability and dock remained identical in both themes.
- [ ] If accepted, promote feature carefully to main after explicit approval. Card materials redesigned separately later; Inter font decision remains pending user approval. If rollback v0.4.77 installed, future APK code>=103.

## NEXT: physical acceptance of v0.4.78 Day02 / Night09 chosen card palettes
- [x] Compare chosen concept `02.html` Day Slate Mist and `09.html` Night Smoke Slate to real screenshot; record exact HEX tokens.
- [x] Protect immutable v0.4.76 code101 source in backup; scoped feature `feature/card-palette-day02-night09-0.4.78-beta`.
- [x] Day only background -> `#E6E8EC`; synchronize native Activity pre-Compose window/system bar colors. Keep all light card material tokens unchanged.
- [x] Night true-black background, neutral flat `#1D2126` outer and `#282D33` inner cards, uniform quiet border `#40464D`, white/muted text tones. Preserve optical glass dock/lens, selected solid Apple Blue/white, semantic success/orange/red, all business/profile/data logic.
- [x] Add tests locking exact card/night HEX and optical roles, test updated legibility; final CI 42495153837 SUCCESS, signed same-ID 0.4.78 code103 ZIP https://github.com/sergbokarev95-arch/NavTasks/releases/download/v0.4.78-day02-night09-verified/NavTasks_Cards_02_09_0.4.78_Beta_Update.zip.
- [x] Prepare optional higher-code same-ID 0.4.79 code104 exact pre-card v0.4.76 rollback, CI42495228148 SUCCESS; https://github.com/sergbokarev95-arch/NavTasks/releases/download/v0.4.79-card-palette-recovery/NavTasks_Cards_Restore_0.4.79_Beta_Update.zip. Do not install unless needed.
- [ ] Export FULL profiles, install v0.4.78 in place on HONOR, verify Today, Plan, Tasks, Handover, AMOS/Services, settings Day/Night colors; native status/nav bars equal #E6E8EC in Day, no blue gradient at night; selected blue/white and optical dock unchanged, no lost profiles.
- [ ] Only after explicit HONOR approval consider promoting tested code; do not modify stable main application. Font SF Pro license vs Inter alternatives unresolved; do not bundle unapproved alternative. If 0.4.79 rollback installed, next same-ID APK versionCode >=105.

## NEXT: HONOR UI polish 0.4.80 review and separate tiny-popup glass Lab
- [x] Protect original v0.4.78 source, isolate `feature/navtasks-hig-polish-0.4.80-beta`.
- [x] NavTasks shared larger bold brand across Today, Plan, Handover, Work Hub, Services, AMOS.
- [x] Dock tap per-target-tab UI refresh on both active and different tabs, zero change to dock optic source; Back/swipes remain original; test pure key semantics.
- [x] Compact Today and nested checklist/Handover/Plan/Tasks/AMOS/Services cards via spacing changes only; don't shrink 44dp controls.
- [x] Consistent `p.dangerText` in Services, AMOS, Plan delete, anchored popover delete actions.
- [x] Neutral opaque night graphite `navDialogSurface` and lighter Day02 background for ALL large dialogs; native small menus neutral, not fake glass.
- [x] Full CI 42498478208 SUCCESS signed same-ID 0.4.80 code105 ZIP https://github.com/sergbokarev95-arch/NavTasks/releases/download/v0.4.80-hig-polish-verified/NavTasks_HIG_Polish_0.4.80_Beta_Update.zip.
- [x] Optional identical prior 0.4.78 source code106 rollback, CI42498384092 SUCCESS https://github.com/sergbokarev95-arch/NavTasks/releases/download/v0.4.81-hig-polish-recovery/NavTasks_HIG_Restore_0.4.81_Beta_Update.zip.
- [ ] Export FULL profiles; install v0.4.80 in place without uninstalling. On HONOR verify dock tap of Today while scrolled, switching among all four tabs, back navigation/swipe preserved; tap Today's seven-day selector, GMDSS nested checklist, Handover sections, task templates, Services/AMOS Delete and modals, profile/time picker, day/night. Check no misclicks from reduced nested row heights and no lost task data.
- [ ] If approved consider promoting safe code after review; otherwise install code106 recovery (not downgrade/uninstall). If 0.4.81 installed future versionCode >=107.
- [ ] Separately prototype **real small anchored popup glass**: Compose DropdownMenu uses another window and cannot directly share same-scene LayerBackdrop. Trial in-scene clipped, z-ordered overlay sharing app's original source, with controlled background blur and no optical flicker; maintain backdrop and tap-away semantics, and test performance on HONOR and Samsung before touching stable navigation. User approval required. Current 0.4.80 only matches neutral colors.
- [ ] Typography SF Pro licensing / Inter alternative remains unresolved; do not bundle/rename font without user decision.

## NEXT: HONOR acceptance of 0.4.82 active dock tap and compact checklist
- [x] Preserve v0.4.80 exact-source backup before modifying.
- [x] Fix root selected-tab click blocker in both optical and legacy dock callback handlers (do not redesign dock); keep per-tab reset keys and scroll/date state.
- [x] Add explicit item-0 scroll on both Today task list and 7-day chips, current local date reset on Today dock tap.
- [x] Inline styled completed count `0/5` after title and remove empty subtitle line for checklist task cards; round and compact child rows to 16dp/46dp without changing 44dp check targets.
- [x] Autofocus shared search field and show IME after opening Handover, AMOS, Services search.
- [x] Separate Handover Delete/Cancel/Save buttons into individually rounded buttons with 8dp gaps; no shared combined surface.
- [x] Make destructive labels exact Apple systemRed Day #FF3B30, Night #FF453A; contrast-safe danger/status text remains separate.
- [x] CI42502226588 SUCCESS full tests/Android signed build, verified in-place 0.4.82/code107: https://github.com/sergbokarev95-arch/NavTasks/releases/download/v0.4.82-ux-fixes-verified/NavTasks_UX_Fixes_0.4.82_Beta_Update.zip.
- [x] CI42502302586 SUCCESS rollback exact previous 0.4.80 code108: https://github.com/sergbokarev95-arch/NavTasks/releases/download/v0.4.83-ux-recovery/NavTasks_UX_Restore_0.4.83_Beta_Update.zip. Only if user rejects fix.
- [ ] Export FULL profiles, install v0.4.82 on HONOR over existing app (never uninstall) and tap selected Today at scrolled list and later dates; verify current date, first date chip and first task shown, then test other tabs and Back/swipe retention.
- [ ] Open Handover, Services and AMOS search: cursor and keyboard must appear immediately. Test editing ECDIS note with independent Delete/Cancel/Save labels, no overlap in RU/EN. Check expanded GMDSS weekly 0/5 inline and compact rounded subrows in Day/Night.
- [ ] Check no data loss, all completion/history/recurrence unchanged and dock glass refraction motion unchanged. If rejected install code108 recovery over existing app without clearing data (future versionCode >=109).
- [ ] After explicit user approval, consider separate small-popup optical glass experiment; DO NOT fake translucency or alter approved dock. SF Pro/Inter font decision unresolved.

## NEXT — HONOR acceptance of 0.4.84
- [x] Back up signed 0.4.82 exact-source UI; isolate 0.4.84 code109.
- [x] Pointer Initial-pass helper for Handover/AMOS/Services result lists to remove search cursor and hide keyboard on result touches/scrolls, without consuming event or losing query; preserve autofocus on opening search.
- [x] Active dock taps no longer invalidate SaveableStateProvider; smooth animated `animateScrollToItem(0)` on Today two axes, Handover, Works, Tasks; reset date/current Today and transient subviews, preserve original horizontal cross-tab animation and glass lens.
- [x] Use Apple systemRed across Today and Plan weekends and incomplete-status marker; Apple systemOrange on note-reference glyph and circular border; retain all other status semantics.
- [x] Update legacy calendar colors test and add semantic accent tests; first CI42504708801 failed only stale expected pink, final CI42505065303 SUCCESS. Signed 0.4.84 code109 ZIP: https://github.com/sergbokarev95-arch/NavTasks/releases/download/v0.4.84-animated-dock-verified/NavTasks_Animated_Dock_0.4.84_Beta_Update.zip.
- [x] Prepare optional higher-code exact 0.4.82 UI 0.4.85 code110 recovery CI42504767028 SUCCESS: https://github.com/sergbokarev95-arch/NavTasks/releases/download/v0.4.85-ux-polish-recovery/NavTasks_Animated_Restore_0.4.85_Beta_Update.zip (do NOT install except after rejection).
- [ ] Export full profiles; install 0.4.84 OVER existing NavTasks on HONOR. Verify active Today scroll from deep list/future chip animates to current day and first task; Works, Tasks, Handover active taps also smooth; cross-tab swipe/Back unaffected.
- [ ] In Handover/AMOS/Services type search and then touch/drag results: caret loses focus/keyboard hides immediately but query remains and list works; tapping search field again focuses. Test Day/Night weekends and Plan incomplete dot in Apple red; question-mark note icon/ring orange. Confirm all data/glass preserved.
- [ ] Only after explicit physical acceptance consider merging tested sources; main still unchanged. If recovery code110 used, future package builds must use versionCode >=111. Tiny-popup genuine optical glass lab and SF Pro/font licensing remain separate work.

## NEXT — physically validate smart reminders 0.4.86 before next feature
- [x] Protect approved 0.4.84 SHA and isolate new Beta branch.
- [x] Separate reference reminder time from missed digest time; preserve existing digest hour as migration seed. Add simultaneous grouping switch.
- [x] Schedule 0-minute At time, 15min/60min/1440min from reference if deadlineTime null, else explicit task hour; do not alter TaskStore overdue definition.
- [x] Replace AMOS/Service fixed 09:00 warning hour with reference time; preserve lead windows and request-only exclusions, group same-instant eligible Tasks/AMOS/Services into one notification with count/titles and per-event dedup.
- [x] Fix inverted first Service warning notification guard and repair former silently marked delivered keys once, without changing ServiceStore records.
- [x] Decouple seven-day rolling reminder horizon from optional missed digest using separate 00:05 ROLL AlarmManager trigger.
- [x] Unit tests for end-of-day anchor, all 4 intervals, explicit time priority, postponed date and minute grouping; final Android CI 42509116834 success. Same package signed code111: https://github.com/sergbokarev95-arch/NavTasks/releases/download/v0.4.86-smart-reminders-verified/NavTasks_Smart_Reminders_0.4.86_Beta_Update.zip.
- [x] Prepare optional exact prior 0.4.84 recovery code112 CI42508828509 success: https://github.com/sergbokarev95-arch/NavTasks/releases/download/v0.4.87-smart-reminders-recovery/NavTasks_Reminders_Restore_0.4.87_Beta_Update.zip. Do not install unless 0.4.86 rejected.
- [ ] Export FULL profiles and install 0.4.86 in place on HONOR, never uninstall. Validate reference clock migrated from old digest setting, selected clock adjustable, digest independently adjustable/disableable, grouping toggle, task reminders independent of missed digest.
- [ ] For a practical test set reference to 2–5 minutes ahead, create 2 uncompleted tasks due end of day with At time notifications: expect exactly one grouped 2-event notification; check clicking, timestamps and duplicate suppression. Then test explicit task time overrides reference, offsets 15min/hour/day, per-task disabled switch, service/AMOS warning if available. Restore preferred clock afterward.
- [ ] Verify delivery when app is backgrounded and after reboot/time-zone change. Android exact alarms require permission; system idle may delay inexact events.
- [ ] No main app merge until user approval. If rollback v0.4.87 installed, versionCode must exceed112. True optical little-popup glass/SF Pro licensing remain separate.

## NEXT — validate single native NavTasks notification icon 0.4.88 on HONOR
- [x] Preserve exact signed 0.4.86 source backup.
- [x] Remove manually traced notification small symbol and redundant branded `LargeIcon`; reuse existing unchanged adaptive launcher monochrome drawable as the only system notification icon.
- [x] CI 42545659634 SUCCESS, same-package signed 0.4.88 code113 https://github.com/sergbokarev95-arch/NavTasks/releases/download/v0.4.88-native-icon-verified/NavTasks_Native_Notification_Icon_0.4.88_Beta_Update.zip.
- [ ] Export full profiles; install over old NavTasks without uninstall or wiping. Remove old notifications from shade. Trigger NEW task or grouped notification and verify native single icon on HONOR MagicOS 11 in both lock screen and status/notification shade, with no right-side duplicate logo.
- [ ] Check the new small icon visually recognizable at status-bar size, and original home-screen icon unchanged. Native Android applies monochrome/tint; do not promise color launcher logo in status-bar.
- [ ] Confirm 0.4.86 reminder timing/grouping, notices, AMOS, Services, backup/restore, dock and user records work unchanged. If rejected, optional exact v0.4.86 rollback code114 branch `feature/navtasks-0.4.89-native-icon-recovery` (confirm CI and release first). Never uninstall; if code114 installed, future app versionCode >=115.
- [ ] Only after physical user acceptance consider merging; main application code still untouched.

## NEXT — HONOR notification icon 0.4.88 and 0.4.86 smart reminder verification
- [x] Diagnose extra hand-traced SmallIcon plus duplicated hand-rasterized launcher LargeIcon in NavNotifications. Correct app notification identity: setSmallIcon official adaptive monochrome asset only. Leave launcher/dock untouched.
- [x] CI42545659634 SUCCESS signed versionCode113 Beta ZIP: https://github.com/sergbokarev95-arch/NavTasks/releases/download/v0.4.88-native-icon-verified/NavTasks_Native_Notification_Icon_0.4.88_Beta_Update.zip.
- [ ] Export FULL profiles and install v0.4.88 OVER existing NavTasks; never uninstall. Trigger at least one real task or grouped reminder, inspect small status-bar and full notification card on HONOR MagicOS. Expect one official monochrome app symbol, no oversized second cyan image/corner badge. OEM coloring and glass launcher treatment not guaranteed.
- [ ] Recheck 0.4.86 smart reminder grouped schedule/zero interval/AMOS+Services from this version. Only after user acceptance consider promoting; main remains unchanged.

## NEXT — HONOR review of AMOS badge Day/Night color v0.4.90
- [x] Protect exact verified 0.4.88 signed source. Scope AMOS request badge only, not global selected controls.
- [x] Day option 1 soft peach fill #FFF1DE, border #F4C48B, readable amber text #AE5800; night graphite amber #403227, stroke #74583C, text #FFBF79.
- [x] Preserve same requestTarget editor callback, labels and 44dp hit target; leave global `ServiceOption`, filters, dock optics, service/AMOS/task data and scheduling untouched.
- [x] Unit test both semantic palettes and unchanged Apple blue; CI42556397343 SUCCESS signed 0.4.90 beta code115: https://github.com/sergbokarev95-arch/NavTasks/releases/download/v0.4.90-amos-peach-verified/NavTasks_AMOS_Peach_0.4.90_Beta_Update.zip.
- [x] Prepare optional exact v0.4.88 UI recovery beta 0.4.91 code116 CI42556403359 SUCCESS https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.91-amos-peach-recovery; DO NOT install unless user rejects 0.4.90.
- [ ] Export FULL profiles, install 0.4.90 over existing app on HONOR without uninstall; inspect Services request badges on planned/unplanned cards in both Day/Night, including unfilled request. Tap to edit and verify request number/date and save behavior unchanged.
- [ ] Confirm ordinary blue filter/buttons remain blue, red delete remains red, and Liquid Glass dock unchanged. Do not merge into main or mark accepted until user's physical device test.
- [ ] If recovery code116 installed, subsequent updates must use versionCode >=117. Genuine little-menu glass and pending 0.4.86 smart reminder / 0.4.88 notification icon acceptance remain separate.

## NEXT — HONOR acceptance of service date + AMOS outline + notification icon 0.4.92
- [x] Preserve immutable signed 0.4.90 source in backup branch and isolate 0.4.92 versionCode117.
- [x] Fix fabricated planned lastDate and independent period/nextDate UI: show actual last service selector and calculate `LocalDate.plusMonths` on interval or last-date change; retain manual next-due override. Never migrate existing incorrect legacy records silently.
- [x] Outline-only (transparent fill, neutral text) empty AMOS request button in Services; existing numbered requests keep warm day/night fill.
- [x] Dedicated notification-only opaque white 24dp NavTasks layered-card/check vector, sole SmallIcon. Keep launcher icon and all reminder scheduling unchanged.
- [x] Tests for 2024-04-25 + 60mo => 2029-04-25, leap/month-end, invalid zero interval, filled/outline badge; CI42561492881 **SUCCESS** signed Android APK/versionCode117 and ZIP. Release https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.92-service-dates-icon-verified.
- [x] Safe higher-code rollback to original exact 0.4.90 app logic/UI: 0.4.93/code118, CI42561496089 SUCCESS, https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.93-service-date-icon-recovery.
- [ ] Export FULL profiles then install **0.4.92 over** current NavTasks on HONOR; NEVER uninstall or clear storage. Edit existing Gyro Sphere #1 with last service 25 April 2024 and select 60mo; expected next due 25 April 2029, not 26 April 2024. Also test new planned service, 6/12/60mo, manual next override and re-edit persistence.
- [ ] Check no regression for unplanned AMOS request date/number, service completion/history, notifications and task calendars. In Services Day/Night compare numbered AMOS filled vs empty outline/neutral text, test tap+save.
- [ ] Clear previous alerts, trigger a NEW notification; inspect app icon on MagicOS lock screen, shade and status bar. Expect one recognizable monochrome task-card mark (OEM chooses coloring/tile); no second duplicated LargeIcon. Device result cannot be inferred from CI.
- [ ] Only promote app feature after user device acceptance. If optional rollback code118 installed, next versionCode >=119. Keep glass optical dock unchanged.

## NEXT — 0.4.94 R2 HONOR acceptance (2026-11-09)

- [x] Preserve exact 0.4.92 source in backup; isolate versionCode119 candidate without changing main application code.
- [x] Full-screen formatted note editor for **both** Personal and vessel Handover. Save/Back behavior; keep old plain/HTML/profile fields, Handover marks, Delete via More.
- [x] Remove fixed 46–50dp clipping in Handover, Services and AMOS list cards; test/source review.
- [x] Hide blue planned-work dots in month Plan; retain only success/partial/missed indicators and day agenda.
- [x] Planned Service last green/next blue; keep warnings and overdue in orange/red.
- [x] Replace notification small icon with refined monochrome glyph and system accent hint, no duplicate LargeIcon.
- [x] JUnit, Gradle assemble, signed APK/ZIP release 0.4.94 R2 CI42615079776 SUCCESS https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.94-notes-polish-r2-verified; mark R1 obsolete after missing deletion action corrected.
- [x] Exact prior application recovery 0.4.95/code120 signed CI42614534271 SUCCESS https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.95-notes-recovery.
- [ ] Export **FULL** profile backup, install 0.4.94 R2 atop current Beta on HONOR without uninstall/clear storage.
- [ ] Verify new/edit Personal notes and vessel Handover notes, WYSIWYG formatting, title, Save then close/reopen, Back without saving, no keyboard overlap; for Handover test More > dated/undated Complete, Strike/Reopen, confirmed Delete.
- [ ] Test >4 lines and long rich comment in Handover/Service/AMOS cards, both themes, scroll and card geometry; no text lost, no overlap with action buttons.
- [ ] Check month grid blue dots gone while green/orange/red outcome dots and legend align, Day details and AMOS/Service schedule still available.
- [ ] Check planned normal Services last-date green, next-date blue in Day/Night; due-soon warning orange and overdue danger red still correct; AMOS itself unchanged.
- [ ] Trigger a **new** NavTasks notification on MagicOS lock/shade/status, evaluate real monochrome system icon; launcher app icon remains unchanged. Device decides tile/background, do not promise a fully colored SmallIcon.
- [ ] Confirm reminders, smart grouping, data, profile switches/import/export, Liquid Glass dock, normal app and Back navigation intact. Only after user feedback accept and plan the next update.

## NEXT — HONOR compare 0.4.96 notification icon A/B/C (2026-11-09)

- [x] Preserve stable signed 0.4.94 R2 source and create isolated feature versionCode121 with three test notification styles only.
- [x] Test A native monochrome, B colored transparent blue mark, C full white/Slate Mist/Apple-blue tile as LargeIcon; protect all production NavNotifications, scheduling, profile stores, UI dock/launcher.
- [x] Add Settings test button + clear action; one low-priority test channel and three tagged IDs, no preference/data migrations.
- [x] CI fix system Notification.Builder unsupported setSilent; final full JUnit/build/signature/ZIP tests SUCCESS 42624280583, https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.96-icon-lab-verified. No earlier broken artifact released.
- [x] Exact previous 0.4.94 R2 recovery versionCode122 (0.4.97) passed CI42623919524, https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.97-icon-lab-recovery; no uninstall.
- [ ] Export FULL profiles and install verified v0.4.96 in place on HONOR (never uninstall/clear). In Settings > Notifications tap `Сравнить значки уведомлений`.
- [ ] Screenshot A, B and C in notification shade (and lock screen if useful). Note blue/white/gray rendering and whether variant C shows duplicate small badge on right. Optional: Settings > Notifications & status bar > Status bar > Notification icon style > Multicolor if your MagicOS build offers it.
- [ ] Clear test notifications. Verify actual tasks/AMOS/Services reminders still show original icon and all scheduling, profile data, complete actions, dock intact.
- [ ] Choose production behavior ONLY with actual HONOR result; if A/B/C is unsatisfactory, inspect system/OEM rendering rather than adding more arbitrary icon glyphs. Main app code still stable 0.4.59. If rollback 0.4.97/code122 used, future code >=123.

## NEXT — confirm real notifications with 0.4.98 B color glyph, 2026-11-10

- [x] Capture MagicOS 11 A/B/C notification comparison on HONOR. B's blue icon appears left in grouped view; C's white branded LargeIcon is shown at the right side of expanded view and cannot substitute for left identity.
- [x] Create isolated **production** 0.4.98/code123 directly from tested 0.4.94 R2, using only B color SmallIcon resource, no LargeIcon, no experiment UI. Existing reminders and stores untouched.
- [x] Full tests, signed APK/ZIP CI42638360527 SUCCESS; verified release https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.98-color-notification-icon-verified.
- [x] Preserve exact 0.4.94 R2 app as higher-code in-place rollback 0.4.99/code124, CI42638428451 SUCCESS, https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.99-notification-recovery.
- [ ] Export FULL profiles; install 0.4.98 over current Beta on HONOR without uninstall. Do not confuse with independent 0.4.98 test branches/releases.
- [ ] Trigger one fresh REAL notification from task, service or AMOS; screenshot collapsed/expanded shade, lock and status if possible. Does left NavTasks blue icon look good without right duplicate? Existing A/B/C test controls intentionally removed from the production candidate.
- [ ] Verify notification timing, grouping, missed-task digest, tap actions, all profile data, Handover/Personal full-screen notes and Liquid Glass; then accept or revise icon only.
- [ ] If regression, use 0.4.99 recovery in place; future same package versionCode >=125 after recovery.

## NEXT — verify standalone D colored small icon on HONOR, 2026-11-10
- [x] Inspect HONOR screenshot: B colored small mark is at left, C LargeIcon branded tile at right; B has not been approved as complete visual.
- [x] Isolate D test from original A/B/C: one fully colored tile supplied as SmallIcon, no LargeIcon, no actual notification change.
- [x] CI42638778449 SUCCESS, signed https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.98-icon-d-verified (0.4.98/code123). CI42638844435 SUCCESS, exact v0.4.96 app recovery https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.99-icon-d-recovery (0.4.99/code124).
- [ ] Export FULL profile, install D-tag build, Settings > Notifications > `Проверить D — цветная иконка слева`.
- [ ] Send screenshot of the **single D** collapsed and expanded; check left visual against premium white/Slate-Mist/Apple-blue target and status bar small badge. Device may monochrome-mask fully opaque SmallIcon; test result determines whether viable.
- [ ] Only after explicit user acceptance change production SmallIcon in a separate new versionCode >=125 if recovery ever installed; preserve normal reminder behavior. Avoid confusion with separately created production B 0.4.98/code123 release.
