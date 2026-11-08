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
