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

## NEXT — MagicOS full-color SMALL icon D/E validation (v0.4.100) — 2026-11-10
- [x] Read actual HONOR A/B/C screenshot: C LargeIcon shows on right, group header blue B icon; grouping prevents reliable A/B isolation.
- [x] Keep verified v0.4.98 production B release separate. Avoid same version number collision by naming the new lab v0.4.100/code125, based on immutable v0.4.96 source and isolated branch.
- [x] Add D white/gray/Apple-blue SmallIcon full tile and E adaptive launcher SmallIcon; single preview ID, UI buttons A/B/D/E, one active at a time, no actual delivery changes. CI42638976702 passed, signed https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.100-single-icon-lab-verified.
- [ ] If installing candidate, export FULL profiles, install OVER beta without uninstalling; trigger D separately and screenshot its LEFT avatar + status; trigger E separately and screenshot, preferably with MagicOS notification icon style Multicolor if available. Do NOT open multiple test notifications together. Check no accidental white-square status-bar icon.
- [ ] Compare D/E with production B candidate on a new real notification; user chooses the final approach. Preserve old note/editor/services/schedule and app data.
- [ ] Safe return version 0.4.101/code126 exact v0.4.96 app is in CI https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42639230106 SUCCESS, signed release https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.101-single-icon-recovery is now verified and should be used ONLY to restore the prior app if needed. If code126 installed, next code>=127.

## NEXT — validate color adaptive notification tile v0.4.102/code127 (2026-11-10)

- [x] User selected full-color premium tile like Amnezia, NOT blue-only B and NOT duplicate LargeIcon C.
- [x] Preserve original v0.4.96 source backup and isolate v0.4.102/code127 (code above v0.4.100/0.4.101), without changing task/profile/service/AMOS stores or Liquid Glass.
- [x] White Slate Mist Apple Blue adaptive icon as actual HONOR SmallIcon, other OEMs mono, no LargeIcon. Settings single real-builder preview (clears old A/B/C); no reminder timing changes.
- [x] Signed JUnit/assemble/APK/ZIP CI42642525997 SUCCESS https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.102-honor-adaptive-icon-verified.
- [x] Higher-code signed exact v0.4.96 app recovery v0.4.103/code128 CI42642766235 SUCCESS https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.103-adaptive-icon-recovery.
- [ ] Export FULL profiles. Install new 0.4.102 over existing beta without uninstall/clearing data.
- [ ] On HONOR, Settings > Notifications > Проверить иконку уведомления. Capture individual notification (not grouped A/B/C), expanded and collapsed; check left tile against Amnezia and status-bar glyph. Evaluate actual MagicOS multicolor mode if available. Do NOT claim success without screenshot.
- [ ] Check real scheduled task/AMOS/Service notifications, grouping and all regular screens/dock/profile data unaffected.
- [ ] If adaptive SmallIcon white tile is tinted/blank or ugly on device, use signed v0.4.103 recovery and investigate system rendering next. After code128 recovery, future code>=129.

## NEXT — Mobile NavTasks 1.0.0 preparation agreed 2026-11-10

- [x] Scope limited before 1.0.0: dock press/hold; move Tasks UI to Work hub (alongside AMOS, Services); Today Calendar-left, Add Task-right with original Daily/Weekly/Monthly/One-off; replace dock Tasks with Reference containing ONLY empty SIRE 2.0 + VIQ7 archive tiles; thorough regression then stable 1.0.0.
- [x] Explicitly DEFER full Reference/SIRE 2.0/VIQ7/manuals/PDF population and Windows desktop + sync until AFTER 1.0.0. No data migration for hypothetical future cloud.
- [x] Save v0.4.102 original to `backup/navtasks-0.4.102-before-dock-press`; source 0.4.104/code129 `4e8d737854d5625271ca6665cd35e009dd5ac828`; JUnit/full APK/signature/ZIP CI42653872788 SUCCESS https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.104-dock-press-verified.
- [x] Safe in-place rollback v0.4.105/code130 exact v0.4.102 application source, CI42654015439 SUCCESS https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.105-dock-press-recovery. No uninstall/clear.
- [ ] User FULL profile export and physical HONOR check of 0.4.104: hold current tab (1.15x), hold a different tab (single lens slides to finger), release (1.0x matte or travel to chosen tab), rapid taps, cancels, left/right edge alignment, light/dark over text, haptics, old Back/scroll resets. Do not call phone-tested until confirmed.
- [ ] After dock acceptance: separately move **TasksScreenV2** entry into `WorksHubScreen` as Routines without moving `TaskStore` data or changing TaskEditor/TaskRun/notifications/history/profile serialization. Preserve tasks count/status and Today/Calendar integration.
- [ ] Separately adjust Today bottom actions: Plan/calendar LEFT, Add(+) RIGHT invoking the SAME TaskEditor screen, default frequency visible at top and existing choices Day/Week/Month/One-off. Test keyboard/back, no bottom overlap and both themes.
- [ ] Replace fourth dock tab label/icon Tasks -> Reference; initial Reference page ONLY styled SIRE 2.0 and VIQ7 (archival) tiles with Coming Later, no broken links or hidden data.
- [ ] End-to-end regression: Handover Personal notes rich editing, task recurrence, weekly/monthly move, overdue, AMOS/Services dates and history, profile exchange, notifications/digest icon, widgets, Plan statuses, all themes, Samsung/HONOR. Preserve all existing features and stable version before signing 1.0.0.
- [ ] After 1.0.0 workstreams: populate Reference gradually with docs/inspection content and separate Windows+offline-first sync architecture.

## NEXT — Verify 0.4.106 dock quick response on physical HONOR (2026-11-10)

- [x] Preserve v0.4.104 in backup branch and isolate versionCode131 candidate.
- [x] Eliminate press release -> click transient retarget by stable clicked destination, preserve canceled gestures and selected tab as fallback, speed critically damped travel without changing original shader/material or navigation semantics.
- [x] New JUnit for fast tap destination, cancellation, edge bounds, hold/travel/idle scales; signed assemble/ZIP CI42657916308 SUCCESS, https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.106-dock-response-verified.
- [x] Prepare exact v0.4.104 application fallback as v0.4.107/code132, CI42658060669 SUCCESS, https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.107-dock-response-recovery.
- [ ] User export FULL profiles and install verified v0.4.106 over v0.4.104. Never uninstall or clear.
- [ ] HONOR: rapidly tap distant dock tabs, press/hold current and unselected tabs, release, cancel, check extreme left/right, day/night optical text, matte, haptics and navigation reset. Ensure no pause, bounce or flash. If still delayed, user feedback before additional tuning.
- [ ] Once accepted continue 1.0.0 scope: Tasks UI within Work; Today Calendar left/Add right; Reference tab ONLY SIRE 2.0 and VIQ7 placeholder tiles; test and sign. Content/Windows after 1.0.0.

## NEXT — Classic Liquid Glass acceptance and NavTasks 1.0.0 (2026-11-10)

- [x] User rejected long-press enlargement and changed dock travel speed/smoothness of v0.4.104/v0.4.106. Restore precise v0.4.102 position/stretch/matte springs (550/600/700), selected-only target, 1.30x moving lens, and original backdrop/56dp geometry.
- [x] Add only a restrained 1.06x lens pulse triggered by a click of the ALREADY selected dock tab (85ms rise/155ms return); no pulse on holding or switching tabs; preserve haptics/reset callback.
- [x] Signed CI-verified feature v0.4.108/code133 source `9c7145e53959cffc11f0e8500c6c6ca317ef9252`, full unit tests/APK signature/ZIP integrity CI42660693061 SUCCESS https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.108-classic-glass-retap-verified.
- [x] Safe exact v0.4.102 app recovery v0.4.109/code134 with NO pulse and original dock, CI42660907817 SUCCESS https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.109-classic-dock-recovery. Do not uninstall or clear; export FULL profiles.
- [ ] HONOR test switching across all four tabs for previous smooth feel; long hold must NOT grow lens, repeated tap on active tab gives tiny quick pulse; both themes, haptics, root reset, no flicker. Acceptance only on user's report.
- [ ] After accepted, next isolated navigation stage: expose Tasks UI under Works next to AMOS/Services (leave TaskStore intact); Today Calendar-left/+ Add-right, existing TaskEditor; replace Tasks dock with Reference (ONLY SIRE 2.0 and archival VIQ7 empty tiles). Full regression, stable v1.0.0. Populate documents and Windows synchronization only AFTER 1.0.0.

## NEXT — 0.4.110 acceptance, followed by 1.0.0 mobile navigation (2026-11-10)

- [x] User confirmed original Liquid Glass smoothness with one-shot active-tab pulse v0.4.108 — **freeze dock**.
- [x] Back up v0.4.108 at `backup/navtasks-0.4.108-before-routines-in-work`.
- [x] New Work > Routine tasks card, uses same TaskStore/TasksScreenV2/TaskEditor and four categories, no data changes; Back and profile-scoped list state; leave Tasks tab temporarily for comparison.
- [x] Full JUnit, signed APK/version/ZIP CI42663503311 SUCCESS https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.110-routines-in-work-verified, code135.
- [x] Exact accepted v0.4.108 app-source recovery 0.4.111/code136 CI42663232108 SUCCESS https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.111-work-routines-recovery, no uninstall.
- [ ] HONOR: install in-place AFTER FULL profile export; Work > Routine tasks opens and filters Day/Week/Month/One-off; create/edit/delete, subtasks, notes, move/overdue, history and reminders; Back returns Work, scroll persists after editor, old Tasks dock and Work entry show identical tasks, AMOS/Services/Today/Handover untouched. Check both themes and unchanged accepted dock animation. Only after confirmation proceed.
- [ ] Second isolated stage: Today Calendar floating button LEFT, Add task (+) RIGHT using SAME TaskEditor; frequency selector at top for Daily/Weekly/Monthly/One-off; remove Tasks dock tab replacing with Reference/Справочник, ONLY two polished non-clickable Coming Later tiles SIRE 2.0 and archival VIQ7; no inspection/PDF database, content, sync.
- [ ] Final UI/interaction/theme/notifications/widgets/import/export regression and stable mobile 1.0.0 after HONOR and Samsung tests. Reference content/manuals and Windows app/LAN/cloud sync remain AFTER 1.0.0.

## NEXT — 0.4.112 HONOR acceptance and mobile 1.0.0 final polish (2026-11-10)

- [x] Preserve accepted 0.4.110 source in backup branch; isolate next 0.4.112/code137.
- [x] Today Calendar left + Task Add right using same floating glass/Haze components; frequency selector Day/Week/Month/One-off first in existing TaskEditor, no data migration.
- [x] Dock fourth Tasks tab -> `Справочник`/Reference, matching glyph both new/old docks, only two SIRE2.0 + VIQ7 archive **noninteractive** tiles; old Tasks screen remains via Work > Routine tasks; remove Work's outdated Inspections dummy tile.
- [x] Update parity test to new fourth-tab symbol while retaining Lab optics numeric checks; new ReferenceNavigationTest. Signed JUnit/assemble/version/ZIP CI42668433844 SUCCESS https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.112-reference-today-verified; intermediate failed CI builds not released.
- [x] Safe signed higher-code 0.4.113/code138 original 0.4.110 application rollback CI42667375817 SUCCESS https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.113-reference-recovery; in-place only, full profile export, never uninstall.
- [ ] HONOR real-device: install 0.4.112 over existing without clearing app; test Calendar LEFT, Add RIGHT with same 54dp glass in day/night, first-frequency selector for all four types; task add/edit/history/subtasks/overdue/notifications unchanged. Work contains exactly Routines/AMOS/Services; SIRE/VIQ7 Reference tiles are legible and not clickable; dock 0.4.108 smoothness unchanged; all swipes/Back, local profile, widgets, import/export unaffected.
- [ ] After acceptance final pre-1.0.0 audit: UI geometry, rich note editor, services/AMOS workflow, notifications and widgets, accessibility/keyboard/scroll/performance and Samsung/HONOR checks; release v1.0.0 only with agreed user sign-off.
- [ ] After v1.0.0 only: SIRE 2.0/VIQ7/reference document content, manuals and Windows offline-first client/sync.

## NEXT — Accept safe scrolling v0.4.114 on HONOR; final 1.0.0 audit

- [x] User screenshot: Today final task blocked by left floating Calendar; Handover last expanded section partly behind Add. Root: fixed LazyColumn bottom padding 125/126dp misses system navigation gesture inset + action offset/size.
- [x] Preserve previous accepted 0.4.112 in backup branch; change ONLY Today and Handover list padding to shared `NavFloatingActionSafeArea` formula (embedded = nav inset +83+54+20dp, standalone=18+54+20dp). Add unit tests for several nav insets and modules already adequate.
- [x] Protected all model stores, schedules, notification builder, dock spring, optical buttons and FAB positions with CI guards. Signed build+all JUnit CI42673108761 SUCCESS, v0.4.114/code139 https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.114-bottom-safe-area-verified. Earlier stale Handover blob safety-guard errors fixed before publishing.
- [x] Exact v0.4.112 source rollback v0.4.115/code140 signed CI42672965626 SUCCESS https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.115-bottom-safe-area-recovery; never uninstall/clear.
- [ ] HONOR: install v0.4.114 in place after FULL profile export; scroll Today down until last task and its entire checkbox lie above Calendar/+, check/expand last task. Verify missed view/other days and completed accordion. Handover last/expanded section and Personal notes no overlap with Add. Day/night, gestures, no absurd blank area, accepted 0.4.108 dock intact.
- [ ] Only after sign-off: focused final 1.0.0 polish/regression and signed release readiness across Work tasks/AMOS/Services, calendar, rich notes, profile exchange, notifications, widgets, both themes and Samsung/HONOR. SIRE2.0/VIQ7 content and desktop/sync AFTER 1.0.0.

## NEXT — 0.4.118 physical acceptance and remaining 1.0.0 release gates (2026-11-10)

- [x] Backup 0.4.114 exact source before pre-release changes, preserve all data and Liquid Glass.
- [x] P0: block storage writes if TaskStore or Handover persisted JSON damaged, validate data before full import/export, add tests; signed v0.4.116/code141 CI42741799999 SUCCESS https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.116-data-guard-verified.
- [x] P1: closed AMOS/Services no longer scheduled/grouped/delivered reminders; widgets update daily even with task notifications disabled, exact-alarm permission-granted replan; full tests; signed 0.4.118/code143 CI42742957197 SUCCESS https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.118-notification-widget-verified.
- [x] P2: safe bottom padding for edge-to-edge Services and AMOS and dynamic installed version in Settings; signed in 0.4.118.
- [x] Higher-version exact previous 0.4.114 application source recovery 0.4.119/code144 CI42743079436 SUCCESS https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.119-pre-release-recovery; never uninstall or clear user data.
- [ ] HONOR/Samsung device testing with FULL profile export first: preserve history/notes after update, full import/export, both themes, last Today/AMOS/Service/Handover cards, group and archived notifications, widgets next local midnight with app notifications disabled, exact-alarm permission, cold/warm routes and app background/reboot. Source CI is NOT enough.
- [ ] Separate smallest-fix Beta for `profileId` routing of notification/widget deep links across profiles; do not mutate other ship records.
- [ ] Decide/check stable 1.0.0 APK package/signature migration from `com.navtasks.app.glass` debug to intended final package without losing user data.
- [ ] Decide Android allowBackup/privacy policy and build manual/automated device regression; load/history/performance profiling.
- [ ] Only after signoff: signed final v1.0.0 mobile release. SIRE/VIQ content and Windows/cloud sync postponed until AFTER 1.0.0. Full audit `RELEASE_READINESS_AUDIT_2026-11-10.md`.

## NEXT — 0.4.120 device acceptance and final mobile 1.0.0 gates (2026-11-10)
- [x] HONOR user installed 0.4.118 and reports preliminary stability; continue targeted regression.
- [x] Save immutable 0.4.118 at `backup/navtasks-0.4.118-before-profile-deeplink`.
- [x] Use intent `profileId` for Task/AMOS/Missed/Today navigation, single serialized cold/warm/repeated tap routing, unknown profile ignored, no cross-profile history edits. JUnit tests, signed 0.4.120/code145 CI42750924441 SUCCESS, https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.120-profile-deeplinks-verified.
- [x] Exact previous 0.4.118 app-source recovery version 0.4.121/code146 CI42751054359 SUCCESS, https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.121-profile-deeplink-recovery. No uninstall/wipe.
- [ ] HONOR real-device: export FULL profiles before in-place 0.4.120 install; from active ship A click a pending Task notification of ship B with cold start and running app, verify ship B selected and exact task/date/checklist; repeat tap; then an AMOS event, a widget task title/checklist and missed chip; return via Back. Test deleted/stale profile and old same-profile intent, no wrong ship data changed.
- [ ] Full device regression: both themes, date/week/month/year boundaries, Work/AMOS/Services completion and history, notes/formatting, Handover, exports/imports, profile switches, widgets at next local midnight with notifications off, grouped/closed alerts, exact-alarm permission, device reboot/locale/timezone, last-card tap clearance, smooth v0.4.108 dock. Include Samsung where possible.
- [ ] Agree safe `1.0.0` release-package strategy: current Beta package `com.navtasks.app.glass` and beta stable signing; default release `com.navtasks.app` cannot update in place. Prefer production non-debuggable release variant retaining installed package + signing; verify on-device update and data. DO NOT silently switch package, signature, or clear data.
- [ ] Decide Android system Auto Backup policy (`allowBackup=true` today) for maritime notes/privacy; never silently change it. Stress-test large histories and multi-profile startup; add Android UI/instrumented testing where feasible.
- [ ] Only after confirmation: publish stable signed 1.0.0. SIRE 2.0 / archival VIQ7 content and Windows / sync remain POST-1.0.0. See `docs/development/RELEASE_PLAN_1.0.0.md`.

## NEXT — HONOR acceptance of 0.4.124 UX + popup prototype, then 1.0.0 (2026-11-10)
- [x] Preserve original 0.4.120 `backup/navtasks-0.4.120-before-ux-handover`, freeze v0.4.108 dock and all Tasks/AMOS/Services/Handover stores.
- [x] 0.4.122 signed CI42757080882 SUCCESS: Handover is notes-only list (old mark/dates still stored), full-screen editor includes safe Delete, Today Completed warm banner, notification icon test control removed, small spring press on ordinary glass and floating buttons; https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.122-handover-ux-verified. Broken early function overload builds not published.
- [x] Exact previous 0.4.120 app higher-code fallback 0.4.123/code148 signed CI42756524945 SUCCESS https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.123-handover-ux-recovery.
- [x] 0.4.124 popup PILOT CI42757168394 SUCCESS https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.124-popup-frost-verified: system native window blur if OEM enabled for small modal dialogs and quick profiles, original solid fallback. DropdownMenu not changed; true dock-like lens in a separate popup not demonstrated.
- [ ] Confirm signed CI42757281433 and release for exact 0.4.122 source recovery 0.4.125/code150 (use only if popup trial rejected).
- [ ] HONOR: full profile export before in-place install 0.4.124. Inspect existing Handover Navigation notes count, card title/description and tap editor, Save/Back/Delete confirmation, long-text scroll, original order, preserved old marked notes, section rename/add, vessel and Personal; Today Completed amber day/night, Settings preview absent, button 1.022x press and 1.055x floating press, 0.4.108 dock unchanged, small dialog frosted appearance and battery-saver fallback.
- [ ] Verify cross-profile notification/widget deep links on 0.4.124; AMOS/Services warning suppression, background widgets, period recurrence, profile import/export, dark/light, keyboard, last cards, Samsung when possible.
- [ ] Decide final installed app ID/signing and Android backup privacy; add device tests/stress. Do NOT release 1.0.0 until explicit user acceptance. SIRE/VIQ content, manuals, Windows and sync AFTER 1.0.0.

## Final verified 0.4.125 recovery status — 2026-11-10

- **SIGNED CI SUCCESS** https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42757281433 for exact v0.4.122 application-source recovery v0.4.125-beta / versionCode150, source `8c075fd9f9febd26a64f0e28292cb8b77aa9b7a1`.
- Official verified recovery release: https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.125-popup-recovery, artifact `NavTasks_Original_0.4.122_Restore_0.4.125_Beta_Update.zip` (12,670,215 bytes), same installed package and Beta key. This rollback is ONLY for rejection of experimental popup blur in v0.4.124; it retains Handover UI/press/Today/Settings improvements of v0.4.122.
- User must export FULL profiles before updating. Install IN PLACE; NEVER uninstall or clear app storage. If recovery code150 installed, any future same-package update needs **versionCode >=151**. Never confuse latest numeric recovery 0.4.125 with newest functional candidate 0.4.124/code149.
- Device HONOR/Samsung visual/interaction acceptance still pending. No v1.0.0 published and no `main` application source merge.

## NEXT — HONOR acceptance of 0.4.126 and remaining 1.0.0 release work (2026-11-10)
- [x] Preserve native popup pilot 0.4.124 at `backup/navtasks-0.4.124-before-ui-polish`.
- [x] Update Handover editor spacing and move Delete centered below rich editor; note storage/content unchanged.
- [x] Work + Reference informative intro banner same warm framing as Today Completed; Handover heading amber, noninteractive.
- [x] Non-dock press and quick tap pulse with immediate click callback; Handover Add moved to persistent parent glass layer; accepted dock untouched.
- [x] 0.4.126-beta/code151 signed tests, build and ZIP success CI42763420520 https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.126-ui-polish-verified.
- [x] Exact 0.4.124 source high-code recovery 0.4.127/code152 signed CI42763533651 https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.127-ui-polish-recovery. Only on rejection, full profile export, no uninstall.
- [ ] HONOR test 0.4.126: full ECDIS note + long text and formatting, edit/save/back/delete confirmation, amber Work/Reference intro and Handover header day/night, click scaling on static and floating controls, no partial Handover '+' redraw, no dock animation regression.
- [ ] Popup blur requires separate engineering: Android OEM disabled cross-window blur cannot be solved by translucent fill; investigate same-window overlay with proper backdrop coordinates/IME/Back/semantics on one isolated prototype BEFORE altering existing production modal flow.
- [ ] Full HONOR/Samsung regression and 1.0.0 same-package same-key nondebuggable APK, privacy Auto Backup policy, device/dataset stress tests. References/Windows/sync post 1.0.0 only.

## Next after signed 0.4.128 whole glass tap correction
- [x] Backup exact 0.4.126 source `backup/navtasks-0.4.126-before-pressed-glass`.
- [x] Move ordinary glass-click animation layer BEFORE background/border/drawBackdrop chain; unchanged click/scale/haptic/dock; compiled/tested/signed 0.4.128-beta/code153 CI42768009539 SUCCESS, https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.128-pressed-glass-verified.
- [x] Handover info title in shared warm information card; AMOS clipped work-order glyph distinct from routine task checklist.
- [x] Exact 0.4.126 app-source recovery as higher 0.4.129-beta/code154 CI42768087904 SUCCESS, https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.129-pressed-glass-recovery. No uninstall/clear; full profile export first.
- [ ] Physical HONOR: 0.4.128 installed in place; short/long touch on real glass FAB and cards scales rim/material and glyph together; no jitter, offsets, clicks behind dock, perf regression. Handover banner and search adjacent; Work tasks vs AMOS vs Service icons; both themes; notification and widget routing unchanged; Handover rich notes/AMOS/Services persist. Samsung where possible.
- [ ] Popup cross-window frosted fallback: user previously reported no true glass; keep separate, don't fake backdrop by transparency. If high priority, prototype same-root Backdrop overlay without breaking IME/focus/Back/semantics.
- [ ] Production 1.0.0 package + signer same as installed Beta, non-debuggable build; backup privacy decision, full device/data boundary regression. Reference content and Windows AFTER stable 1.0.0.

## NEXT — after signed 0.4.130 crew-work and reference-search Beta (2026-11-11)
- [x] Protect exact 0.4.128 source at `backup/navtasks-0.4.128-before-crew-reference-font`.
- [x] Rename existing WorkHub/WorkOrders UI module `AMOS / By Crew`; description covers AMOS plus crew-periodic inspections, neutral existing WorkOrder badges, new model classification intentionally NOT added.
- [x] Full-screen Handover rich-note body 17sp/25sp; compact comments 14sp/20sp, no stored-HTML rewrite.
- [x] Search UI in Reference: SIRE 2.0 and VIQ7 placeholder matching, RU/EN, clear/empty search, no indexing of future content. JVM tests.
- [x] 0.4.130 code155 GitHub CI42839714152 SUCCESS and signed https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.130-crew-reference-notes-verified.
- [x] Exact previous 0.4.128 app recovery as 0.4.131 code156 CI42839880090 SUCCESS https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.131-crew-reference-recovery. Full profile export + in-place only if candidate fails; do not uninstall.
- [ ] HONOR real-device: new AMOS/By Crew overview/card labels, repeat/complete dates and history unaffected, rich note edit format and font, Reference search keyboard/filters/clear and light/dark, no changed dock, overlays, widgets or notifications. Test Samsung where possible.
- [ ] Before stable 1.0.0: nondebuggable release APK with same installed package/signing key, private-data Auto Backup policy decision, regression on boundary dates/profile swaps/large histories and notification/widget cycles. Real cross-window refraction for popups remains separate unresolved prototype; no SIRE/VIQ PDF/content or Windows/sync before 1.0.0.

## NEXT — acceptance of 0.4.132 idle-top swipe Search (2026-11-11)
- [x] Protect signed 0.4.130 code155 as `backup/navtasks-0.4.130-before-pull-search`.
- [x] Identify four actual search-enabled lists: Handover, AMOS / By Crew, Services, Reference; do not add gesture elsewhere.
- [x] Passive pointer observer and guarded release-only predicate, zero consumed events; original magnifier/focus works; unit tests for steady pull vs mid-scroll, fling, overswipe, diagonal, short flick and multitouch.
- [x] Failed initial CI import corrected before publishing; 0.4.132 code157 signed CI42845269986 SUCCESS, https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.132-pull-to-search-verified.
- [x] Exact 0.4.130 app-source rollback 0.4.133 code158 signed CI42844676421 SUCCESS, https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.133-pull-search-recovery. Install only if needed, full export, no uninstall; next versionCode>=159 if recovery installed.
- [ ] HONOR 0.4.132: scroll deeply down, fling up to the top and overswipe in same motion (must NOT search), interrupt inertia with new touch (must NOT search), wait 200ms and fresh intentional down >=90dp >150ms (must open), rotate/Back/re-enter, keyboard focus and search exit, all four screens, both themes and short/long lists. Check normal scrolling, tapping item cards, dock glass/animation and no jank. Samsung if possible.
- [ ] Finish same app ID/signature non-debuggable production 1.0.0 packaging, Auto Backup privacy policy, full offline/profile/import history and next-day alarm/widget regression. Popups real glass remains separate research; full SIRE2/VIQ7 content and Windows/sync after stable Android 1.0.0.

## NEXT — device acceptance for 0.4.134 staged Search Back and softer motion
- [x] Preserve source 0.4.132 at `backup/navtasks-0.4.132-before-search-back-animation`.
- [x] Common Search AnimatedVisibility in Handover, AMOS/By Crew, Services, Reference; no modification to pull-search or work stores.
- [x] IME-aware child BackHandler: visible keyboard gets first system Back; after IME hidden, Search closes/reset query; next Back navigates. JVM policy tests. Inherited CI grep corrected after first failed preflight.
- [x] Signed 0.4.134/code159 CI42849635847 SUCCESS https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.134-search-back-motion-verified.
- [x] Exact 0.4.132 app recovery 0.4.135/code160 CI42849425894 SUCCESS https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.135-search-back-recovery.
- [ ] Real HONOR test: open via magnifier and deliberate pull, immediate keyboard, first Back only hides IME, second collapses Search but stays on SAME screen, third Back navigates. Test after list-touch keyboard dismissal, empty and nonempty query, fast repeated gestures, all four screens, light/night. Verify visual entry/exit pacing, scrolling/focus, 0.4.108 dock, profile data and notifications unchanged. Samsung if possible.
- [ ] Continue final 1.0.0 gates: nondebuggable APK matching currently installed package/key, backup privacy/Auto Backup policy, data/alerts/widgets/large histories QA, real small popup Glass separately. No SIRE/VIQ full content/Windows until stable 1.0.0.

## NEXT — acceptance of 0.4.136 orange markers and result card navigation
- [x] Back up 0.4.134 source at `backup/navtasks-0.4.134-before-search-highlight`.
- [x] Shared orange search text marks; count as result cards rather than word occurrence; up/down buttons wrap, disabled at 0, accessible descriptions in RU/EN.
- [x] Implement consistent AMOS / By Crew, Services title filtering/highlighting and scrolling including closed/filter variants; Reference existing SIRE/VIQ placeholders; Handover sections/entries/note text with nested BringIntoView.
- [x] JUnit tests, signed CI42855571669 SUCCESS https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.136-search-results-verified (code161).
- [x] Exact previous 0.4.134 app recovery as v0.4.137/code162 CI42855687113 SUCCESS https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.137-search-results-recovery. Install only if new version rejected, with full profile export and no uninstall.
- [ ] HONOR: query 'ECDIS' in Handover, query phrase in long note, next/previous through different nested entries/sections, 0 results disabled arrows, multiple occurrences in a single note counted as 1 entry; switch closed/requests filter in AMOS/Services; Reference 'SIRE'/'VIQ'; check keyboard and Back chain. Verify day/night warm marker readability and no dock jitter/overlapping search controls; Samsung if possible.
- [ ] Final 1.0.0: non-debuggable same-package/key signed APK update, Android Auto Backup privacy policy, data/import/export/history/alarm/widget regression, real popup glass independent proof. SIRE/VIQ full content and Windows/sync after mobile stable.

## NEXT — HONOR acceptance of 0.4.138 occurrence search and Personal note names
- [x] Backup precise 0.4.136 source at `backup/navtasks-0.4.136-before-occurrence-search`.
- [x] Personal ONLY: allow empty title when body nonblank, auto default `Заметка N` / `Note N` using current profile's maximal auto-title number, save via unchanged HandoverStore. Vessel Handover still title-required.
- [x] Two-character trimmed search minimum in Handover, AMOS/By Crew, Services, Reference, with no filtering/highlighting/counter below threshold.
- [x] Handover per-occurrence positions in section titles, item titles and note body, one active marker and glyph-rect BringIntoView on next/previous. Reference occurrence count in existing placeholders only. AMOS/Service remain per-matching-title card.
- [x] Compact count/arrows moved INSIDE shared search field without altering search animations, staged Back, keyboard focus or dock.
- [x] Unit tests for five occurrences across 2 notes, Cyrillic, repeated positions and wrap, Reference fields, threshold, auto naming. Signed 0.4.138/code163 CI42861072394 SUCCESS https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.138-occurrence-search-verified.
- [x] Signed same-source backup 0.4.139/code164 CI42860828496 SUCCESS https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.139-occurrence-recovery. Only on regression; full export, in-place only.
- [ ] HONOR: test unnamed Personal note creation with body, proper generated title and no title requirement in Personal; mandatory title still in vessel Handover. Query 'Работа' shows all five individual text occurrences in sample, active arrow scrolls among repeated words in same long note (not just same card), Back/IME/keyboard no regressions, compact inline controls fit and accessible, one-letter no highlighting, day/night, Reference SIRE/VIQ only. Samsung if possible.
- [ ] 1.0.0 release: same app ID/key nondebuggable APK, Auto Backup privacy, full offline data/profile/exchange/history/notification/widget and large-note regression, popups real glass pilot. SIRE/VIQ full content & Windows after stable Android 1.0.0.

## NEXT — on-device 0.4.140 check (2026-11-11)
- [x] Exact source 0.4.138 safeguarded at `backup/navtasks-0.4.138-before-personal-search-fix`.
- [x] Identify deterministic Personal-only false TaskStore corruption warning: reload writes empty runs without templates, next reload strict require rejects; separate from Handover notes. Skip task storage and writes for PERSONAL only; strict vessel validation remains.
- [x] Replace Handover nested word BringIntoViewRequester with LazyListState animateScrollBy using TextLayoutResult/positionInWindow and safe viewport margins; no root/keyboard scroll request. Added `NavPersonalAndSearchViewportFixTest` covering policy and scroll boundaries.
- [x] Signed candidate 0.4.140/code165 CI42865643443 SUCCESS https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.140-personal-search-fix-verified; first compile failed missing density scope then fixed in follow-up, unverified build never released.
- [x] Original 0.4.138 code recovery as 0.4.141/code166 CI42865051674 SUCCESS https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.141-personal-search-recovery. Only for *new* regressions, not a fix for old warning. Full backups/in-place no uninstall.
- [ ] HONOR: open/close/reenter Personal profile with earlier `runs=[]` key, no warning; existing Notes remain, auto `Заметка N` works; no global storage modifications. Query 'Работа' in long Handover note: 5 occurrences; step all hits keyboard open and closed; verify top brand/header/search bar stays fixed and the selected match comes into list viewport, with no sudden jumps. Also day/night, re-enter sections, AMOS/Services search, widgets, dock.
- [ ] Production 1.0.0 gates: same installed package/signature NON-debuggable APK, Android backup/privacy, full offline profile/import/history and alarms/widgets/device regression. Real popup optical Glass research separate; reference content and Windows after mobile 1.0.0.

## NEXT — 0.4.142 RC1 in-place compatibility acceptance, then updater stage 2
- [x] Preserve 0.4.140 functional source `e812bdb` at `backup/navtasks-0.4.140-before-release-candidate`.
- [x] Configure NONDEBUGGABLE release candidate v0.4.142-rc1/code167 with same installed `com.navtasks.app.glass` and existing stable Beta signer; no business/Glass/Manifest changes.
- [x] CI42873932892 SUCCESS: unit regressions + synthetic early full-profile-v1 import, signed release packaging, non-debuggable, manifest/version checks, and REAL historical signer comparison against v0.4.118/code143 and v0.4.140/code165.
- [x] Private draft Release https://github.com/sergbokarev95-arch/NavTasks/releases/tag/untagged-6b6bfb5b28f19db6920f, signed NavTasks_0.4.142_RC1_Signed_Release.apk and SHA256; initial Actions artifact quota error resolved without deleting history.
- [x] Higher versionCode168 exact previous app fallback CI42874025230 SUCCESS https://github.com/sergbokarev95-arch/NavTasks/releases/tag/untagged-bfab878ad7a9ded917d3; private draft, only on new RC regression.
- [ ] HONOR (and Samsung if possible): FIRST export FULL profiles and save local copy; install code167 over code165 WITHOUT uninstall/clear; confirm existing profile IDs, Personal notes, Handover rich HTML, tasks/history/checklists, AMOS/Services completion history, comments, settings, notifications/widgets, dates, gestures, dock optics, process restart, offline. Do NOT test on sole irreplaceable data without a backup.
- [ ] Compare actual old exports and representative profile snapshots before/after; test direct in-place from earlier signed package 0.4.118/code143 as well; migration from 1.0.3 to future 3.0.0 remains unverified until versioned migration tests/fixtures introduced.
- [ ] Stage2: create/approve separate PUBLIC binary-only GitHub repository, safe release feed manifest, SHA256, RU notes, stable-only updater strategy; keep main sources private. No access changes to existing repo.
- [ ] Stage3+: implement offline-silent updater service, download, FileProvider installer, opt-in settings UI, release channel, security tests. 1.0.0 still blocked on full device/privacy/regression acceptance.

## NEXT — NavTasks updater after Stage 2 channel creation
- [x] Keep `NavTasks` PRIVATE; create separate PUBLIC https://github.com/sergbokarev95-arch/NavTasks-Updates (binary/metadata only), verify visibility and anonymous access.
- [x] Initialize `channels/stable.json` with `available:false`; zero APKs/releases publicly until stable approval.
- [x] Freeze RC1 app at `backup/navtasks-0.4.142-before-update-channel`, prepare isolated manifest builder, unit tests, no-app-change dry-run CI and guarded manual publisher at `feature/navtasks-updates-stage2-channel` SHA `d3a81382`. CI #42940531172 SUCCESS.
- [ ] Before live cross-repo promotion, user provisions `NAVTASKS_UPDATES_PUBLISH_TOKEN` fine-grained Contents:Read/Write scoped ONLY to public updates repo in PRIVATE NavTasks Actions secrets. No tokens in chat or APK. Verify publish workflow environment/manual approval gate and move tested workflow to suitable trusted release branch.
- [ ] Finalize stable-v1.0.0 data migration matrix including genuine historical exported profiles, backup/privacy and actual HONOR/Samsung upgrades. Source stable release must be reviewed, NOT draft/RC/recovery; increasing code>=169. Only then publish FULL signed APK, SHA and RU/EN notes into public Releases, with stable.json updated LAST.
- [ ] Stage3 Android: optional asynchronous periodic update checker, download retry/progress, SHA + signer & package verification, min compatible code guard, secure FileProvider/content URI, Android unknown-app-install permission and installer, Settings UI, offline silence, 24h throttling/"Позже". Do not alter dock, work stores, alarms, widgets or Liquid Glass.
- [ ] Instrumented device checks: good/no/offline/net-drop/corrupt/later/cancel, in-place profile+history+Handover+AMOS+Services+notes retention, jump across skipped releases and migration failure with no loss.

## NEXT — Stage3B/Stage4 updater device acceptance and safe release
- [x] Freeze Stage3A 0.4.144 read-only code at `backup/navtasks-0.4.144-before-apk-installer`; new private updater branch separate from main.
- [x] Implement voluntary popup/Settings, RU/EN, version+changelog, Later per-release suppression, 24h interval on SUCCESS (offline retries possible), full APK progress/cancel/retry, max 200MiB, restricted HTTPS release redirect, SHA-256, installed signing certificate + archive package/version/minSdk, FileProvider, unknown sources Android flow.
- [x] Signed non-debuggable private RC3 code170 CI42957053616 SUCCESS, https://github.com/sergbokarev95-arch/NavTasks/releases/tag/untagged-319403b5abaf1b1cf691; business stores/dock unchanged by CI.
- [x] Signed exact accepted 0.4.142 source fallback RC4 code171 CI42957177681 SUCCESS, https://github.com/sergbokarev95-arch/NavTasks/releases/tag/untagged-8428ad48f7af4074bcc0; only if regressions and after backup.
- [ ] HONOR: export FULL profiles, in-place install 0.4.146 RC3, verify all data/widgets/notification/Glass/AMOS/Service/Handover unchanged. Settings Updates on offline and with internet when stable feed says unavailable. Samsung if possible.
- [ ] End-to-end with explicitly approved controlled test channel/sample signed APK: actual candidate detection, Later persistence, download progress/cancel/bad checksum/bad signer, redirect, unknown app source permission, installer cancel, install/update over 0.4.146+ with confirmed retained data. No fake "passed" on device.
- [ ] Prepare real old-profile migration matrix and explicitly tested minSupportedVersionCode; publisher secret only scoped to public binary repo, manual source release approval, public stable feed LAST; do not publish RC/recovery as stable.
- [ ] Future first stable code>=172 if recovery code171 installed; preserve same package/signature. Security/privacy Android Auto Backup, direct-skipped-version tests and production 1.0.0 acceptance remain open.

## NEXT — updater Stage3C 0.4.148 RC5 HONOR acceptance / Stage4 safe transport
- [x] Preserve exact signed 0.4.146 RC3 application source at `backup/navtasks-0.4.146-before-update-preferences`.
- [x] Settings Updates placed AFTER full and selective exchange panels, directly BEFORE app version; persisted default-ON `auto_update_offers` suppresses startup network check + offer when OFF; manual Settings check and explicit install remain accessible.
- [x] Pure JVM ON/OFF/manual/Later truth-table tests and source-order guards, signed nondebuggable `com.navtasks.app.glass` code172, historical signer parity; CI42966615760 SUCCESS. Private draft https://github.com/sergbokarev95-arch/NavTasks/releases/tag/untagged-959074696eb80d37012f.
- [x] Exact previous RC3 application-source recovery signed code173/0.4.149 RC6, CI42966290343 SUCCESS, private draft https://github.com/sergbokarev95-arch/NavTasks/releases/tag/untagged-01723f74215b01af7d0f.
- [ ] HONOR: FULL profile export and backup, in-place RC5 installation over RC3 without uninstall/clearing app data; verify updates panel position, toggle persists after app restart, OFF means no startup network request/modal, manual offline/online check works, ON checks subject to 24-hour successful-check throttle; same-version Later suppression.
- [ ] HONOR/Samsung: confirm profiles, Personal/Handover notes, Tasks, AMOS/By Crew, Services, widgets, alarms, theme/Glass, import/export and completion history are intact.
- [ ] Controlled private TEST CHANNEL (not public stable) for full end-to-end version discovery, bad network, interrupted and corrupt download, optional installer permission/Cancel/Retry, and real in-place data retention. Public `stable.json` MUST remain `available:false` until approved stable. Never publish private RC/rollback to public stable.
- [ ] For poor maritime connectivity, consider separately reviewed HTTPS Range/resumable signed-full-APK transfer, with final SHA256+signer revalidation and safe retry. Currently download restarts on interruption. Delta patch architecture deferred; version jumping must remain possible with full APK.
- [ ] Compatible minSupportedVersionCode and migration matrix using real old profile snapshots, data privacy/Auto Backup decision, release 1.0.0 physical acceptance. If code173 rollback installed, future code>=174.

## NEXT — 2026-11-11 HONOR acceptance of RC5 + controlled updater E2E
- [x] User installed code172 `0.4.148-rc5` over installed NavTasks and reported all application data/UI still present, with newly added toggle working. This closes basic device acceptance of the settings move/toggle; retains unknowns below.
- [ ] Verify preference persistence after process kill/reboot, actual absence of background HTTP with OFF, manual check online/offline, automatic ON check throttle and Later across future candidate; user has not expressly reported all network cases.
- [ ] Stage4 prepare gated test-channel schema and signed full-APK publication process in isolation. No RC/recovery file in public stable, no change to `stable.json` (`available:false`). Seek explicit approval BEFORE making any test APK publicly retrievable.
- [ ] Controlled test on HONOR of candidate detection/download/cancel/retry, installer permissions/system consent, signature/hash mismatch fail-closed, direct update over skipped versions with full profile/history retention. Samsung if possible.
- [ ] Evaluate resumable HTTPS Range/If-Range full signed APK with bounded cache/size and SHA256+installed certificate recheck after full assembly; never rely on delta patches for basic upgrades.

## NEXT — Stage4A/B test publisher gate and full HONOR updater E2E
- [x] User approved ONLY public test prerelease (not stable); created public `channels/test.json` `available:false`; verified stable `available:false` and no public releases.
- [x] Private signed test-channel checker RC7/code174 CI42972399842 SUCCESS; manual Settings-only, no automatic test requests. Backup exact RC5 at `backup/navtasks-0.4.148-before-test-channel`.
- [x] Exact accepted RC5 app fallback RC8/code175 signed CI42972475636 SUCCESS.
- [x] Signed test-target full APK `0.4.152-test1`/code176 CI42972731198 SUCCESS with exact RC7 application code, size28,501,761 bytes SHA256 `43843af02acbab67606a12ba711254db8c38d148e6f4455bd52f73fb5dbd4ed1`. Private draft only.
- [x] Guarded private-to-public prerelease publisher workflow staged; #42973725649 stopped safely due absent `NAVTASKS_UPDATES_PUBLISH_TOKEN`.
- [ ] User (GitHub UI ONLY): create fine-grained PAT with Contents Read/Write restricted to `sergbokarev95-arch/NavTasks-Updates`; add PRIVATE source repo Actions secret named `NAVTASKS_UPDATES_PUBLISH_TOKEN`; never paste token or key into chat.
- [ ] After token provision, make fresh workflow-only push to `feature/navtasks-updater-stage4b-test-apk-0.4.152`; require signed CI SHA, signer history, public release `test-v0.4.152` and exact asset verification; update ONLY public `channels/test.json` last, stable:false unchanged.
- [ ] Verify signed exact RC7 app recovery code177 CI42974037324; do not recommend test target installation without verified higher-code backup.
- [ ] HONOR: export full profile backups, install RC7/code174 in place, explicitly press test channel, confirm test target code176 shown, Later/Cancel, streaming, permissions/system installer, full data/history/widgets/dock untouched. Samsung optional. Code177 rollback if code176 regression, never uninstall.
- [ ] Test bad checksums/signers, network interruption, offline, test channel unavailable and direct-skipped-version old snapshots; no claim all version jumps work until migration matrix tested. Implement resumable full APK separately.

## NEXT — Stage4 release safety checkpoint (2026-11-11)
- [x] Create exact RC7/code174 app-source emergency restore as RC9/code177, signed CI42974037324 SUCCESS, private draft https://github.com/sergbokarev95-arch/NavTasks/releases/tag/untagged-7d0c70ad8ede887bd8b6; full profile backup and IN-PLACE install only.
- [ ] Create PUBLIC-repo-scoped fine-grained PAT in GitHub UI; store as PRIVATE `NavTasks` Actions secret `NAVTASKS_UPDATES_PUBLISH_TOKEN` (no credentials in chat). Trigger fresh publisher run after this; do not publish from failed run.
- [ ] Only then verify PUBLIC `test-v0.4.152` prerelease APK and `channels/test.json available:true`; check `channels/stable.json available:false` unchanged and SHA/certificate metadata. Then request HONOR RC7 installation and controlled E2E test.
- [ ] After code177 emergency restore, future upgrades require code>=178.

## NEXT — Stage4 test channel published, HONOR end-to-end acceptance
- [x] Fine-grained public-only repository publishing secret added by user in private GitHub Actions. No token in app/metadata/chat.
- [x] Signed TEST1/code176 published as PUBLIC prerelease `test-v0.4.152` with APK and .sha256. Source Actions run42978780819 SUCCESS, verified signed full APK against historical signer, public re-download SHA, release asset size and metadata. `test.json` active `available:true`, `minSupportedVersionCode=174`. PUBLIC stable feed still `available:false`.
- [ ] User export FULL profiles, then install signed PRIVATE RC7/code174 `0.4.150-rc7` over current RC5/code172 without uninstall, preserving app package/signer. Draft: https://github.com/sergbokarev95-arch/NavTasks/releases/tag/untagged-439aaa4ee6cb45e7a7f8. Test Settings now contains `Проверить тестовый канал` separately from ordinary manual check.
- [ ] User press `Проверить тестовый канал`, expect TEST1/code176, test Later and manual re-open, then Update opt-in APK download/progress, Android unknown-source permission/install consent, verify final version/data/profile notes, AMOS/Services, widgets, Handover, task history, dock. Full on-device E2E NOT yet observed.
- [ ] If regression, consider PRIVATE signed exact RC7 recovery `0.4.153-rc9`/code177, CI42974037324 SUCCESS; backup FULL profiles before any in-place install; never uninstall/erase data. After installing code177 fallback future update code>=178.
- [ ] Separate adverse-network/redirect/truncated/corrupt/signer-fail/installer-cancel tests, Samsung, compatibility floor/migration snapshots. Stabilization and production stable 1.0.0 gated on physical acceptance; NEVER publish prerelease on stable.

## NEXT — HONOR manual test feed retest after weak-network fix1/code175
- [x] User RC7/code174 screenshot: manual test feed "недоступен"; browser screenshot: public test.json is reachable/valid. Root cause cannot be deduced from old generic catch; suspect 3.5s timeouts, but distinguish by new diagnostics.
- [x] RC7 backup branch `backup/navtasks-0.4.150-rc7-before-weak-network-fix`.
- [x] Only test feed isolated read with 12s/20s timeout, 3 bounded retry attempts, strict same-path HTTPS redirect handling, classified RU/EN error messages. Unit tests adapted for changed call; source diff restricted, no business/Glass changes; signed CI42984926537 SUCCESS.
- [x] PRIVATE signed release `0.4.150-rc7-fix1`/code175, APK `NavTasks_0.4.150_RC7_Fix1_WeakNet_Private.apk`, https://github.com/sergbokarev95-arch/NavTasks/releases/tag/untagged-c61cfbfa7436013ce386.
- [ ] HONOR FULL profile backup and in-place code174 -> code175, use manual `Проверить тестовый канал`, report specific error if any. If TEST1/code176 found, check Later, download progress, cancel/retry, Android installer and data retention. Phone acceptance NOT YET verified.
- [ ] If regression, exact prior RC7 app-source recovery code177 signed run42974037324 SUCCESS. Do not install code175 RC8 recovery over same-code fix; future code>=178 if 177 installed. No uninstall.
- [ ] Weak maritime internet full-APK resume is separate later stage; public stable remains `available:false`. Public TEST1 code176 unchanged; no new public release authorized/published.

## NEXT — 2026-11-11 HAPPY-PATH HONOR UPDATER E2E USER ACCEPTANCE
- [x] On HONOR, signed private `0.4.150-rc7-fix1` code175 detected publicly published `0.4.152-test1` code176 via manual test channel. User screenshots: offer dialog with installed/available version, post-download `Установить`, and new installed version in Settings. User reports `Обновление прошло`.
- [x] Public test channel remains isolated enabled for code176; public stable feed remains `available:false`. No new public assets/tags authorized by this acceptance.
- [ ] Confirm data after update: profiles/IDs, tasks/completions, Personal/Handover rich notes, AMOS, Services and work histories, widgets, notifications, schedule/alarms, custom dock/Glass and full profile export/import. User has not yet explicitly reported all.
- [ ] Explicit adverse cases: airplane mode/offline feed, download cancel/retry, broken network mid-APK, HTTP redirects, truncated/oversize file, corrupted SHA/signature, unknown-sources permission and installer cancel. Download currently starts again after interruption; design bounded resumable full APK separately.
- [ ] Validate direct skipped-version migration on old real profile snapshots, Samsung, signed code/version lineage and auto-update preference. Keep public stable off until production full acceptance. Phone currently code176; do not deploy any code<=176 over it.

## NEXT — 2026-11-11 user confirmed all data after test update; Stage5 resume QA
- [x] User explicitly confirmed all app data exists after HONOR in-app signed code175 -> code176 update; no profile/data loss reported.
- [x] Preserve accepted full app source code176 SHA639fab15d02a41c1dfecb5852f6eeb2977db4c72 at `backup/navtasks-0.4.152-test1-after-honor-e2e`.
- [x] Prepare isolated Stage5 private code178 resume candidate (new .part identity; exact 206 Range acceptance; 200 reset; bound, retry; final signer checks unchanged) plus pure JUnit tests and CI on `feature/navtasks-updater-stage5-resumable-0.4.156-rc11`.
- [x] Prepare private exact successful source fallback code179 on `feature/navtasks-updater-0.4.157-rc12-exact-test1-recovery`, signed CI verification still required.
- [ ] Confirm BOTH CI success and signed nondebuggable APK historical-signer parity before suggesting any installation. No actual user phone test of code178 yet.
- [ ] Controlled resume E2E requires newer compatible signed public test target code>=180; request separate user permission before publishing ANY new APK or modifying public `channels/test.json`. Existing TEST1 code176 is lower than RC11 code178.
- [ ] Interrupted Wi-Fi/airplane mode, Range-supported/ignored behaviors, tampered/truncated payload, retry/cancel and cache eviction tests on HONOR; preserve full existing data each time. Samsung and old-version jump migration remain independent.
- [ ] Final production 1.0.0 release requires security/privacy, onboarding and full regression; stable channel remains off and source private.

## NEXT — Stage5 TEST2 code180 published; HONOR resumable APK acceptance
- [x] User approved PUBLIC TEST2 prerelease, no stable publishing.
- [x] Final signed private RC11 `0.4.156-rc11` code178 with isolated resumable download, strict HTTP 206 Content-Range/200 fallback, bounded .part identity and retries, full SHA-256 and installed signer, plus accepted weak-network feed tests and exact TEST2 parser. CI43014544611 SUCCESS, PRIVATE draft https://github.com/sergbokarev95-arch/NavTasks/releases/tag/untagged-5f3610d2ad235a19d13f.
- [x] Signed TEST2 `0.4.158-test2` code180 CI43014737847 SUCCESS, private source SHA77387fb9f819c57e5291aaeed0c3972f2d67e88f. Guarded public publisher CI43016024100 SUCCESS. https://github.com/sergbokarev95-arch/NavTasks-Updates/releases/tag/test-v0.4.158, `test.json available:true` min floor178, exact sha/size/URL, `stable.json available:false`.
- [x] Exact previously HONOR-accepted TEST1 app-source signed recovery code181 `0.4.159-rc14`, CI43014926117 SUCCESS, private draft https://github.com/sergbokarev95-arch/NavTasks/releases/tag/untagged-c1480f6830ca77f5a74e.
- [ ] HONOR: FULL profile backup, install exact test2-ready RC11 code178 OVER current code176 (not old RC11 draft); no uninstall/clear data. Confirm Settings version and data. Manually check TEST2 channel (offer code180, floor178).
- [ ] Start user-initiated download, interrupt or tap Cancel after nonzero progress, restore connection/tap Update. Check whether progress resumes from saved prefix (GitHub HTTP 206) vs resets safely if host returns 200; monitor offline/timeouts and verify final full signed APK, Android install and all records unchanged.
- [ ] Exercise repeated cancel, Android system installer Cancel/retry, stale/tampered/oversize response, cache eviction and Samsung; automated unit policies are NOT physical device proof.
- [ ] If RC11/code178 or TEST2/code180 regression: backup FULL profiles, use exact prior accepted source signed code181 RC14 as IN-PLACE recovery, no uninstall; future APK versionCode >=182. Recovery should not be routinely installed.
- [ ] Test skipped-version migrations from real old profiles, auto-update OFF behavior across restarts, release signing/metadata security, privacy backup policy before approving production stable 1.0.0. Source repo remains private, stable channel disabled.

## NEXT — 2026-11-12 HONOR resume PASS; remaining release gates
- [x] User opted-in and performed RC11/code178 -> TEST2/code180 in-app update. User explicitly confirmed a mid-download interruption and subsequent restart continued from saved portion. Settings screenshot shows final `0.4.158-test2`. Mark real-device **resumable signed APK happy-path PASS**; no claim of packet-level HTTP206 tracing.
- [x] CI and gated isolated public TEST2 code180 release verified; `channels/test.json available:true` min floor178, `channels/stable.json available:false`.
- [ ] Ask user whether all tasks, profiles, completion history, notes/Handover, AMOS/Services, notifications/widgets and Glass are intact after TEST2 (previous TEST1 was accepted).
- [ ] Additional failure-mode verification without risking production data: repeated cancel, full airplane-mode cut and resume, server responds 200 to Range, malformed 206/Content-Range, cache eviction, bad signature and SHA, installer cancel/retry. Pure unit tests cover safety conditions, physical tests pending.
- [ ] Test direct version skipping and migration with preserved full profile snapshots from older builds; Samsung; long offline period; persistent auto-offers toggle; import/export; Android backup privacy. No stable 1.0.0 until accepted.
- [ ] Preserve exact tested app, avoid unnecessary changes to business models and Liquid Glass; current HONOR version code180; future APK over it must code>=181 (if emergency code181 installed, next>=182). Recovery `0.4.159-rc14`/code181 available privately but only for a regression.

## NEXT — 2026-11-12 FINAL TESTS, POLISH, THEN APPROVED 1.0.0
- [x] User verified TEST2/code180 in-place update + resume and confirmed ALL data present on HONOR.
- [x] Freeze exact tested source commit `77387fb9f819c57e5291aaeed0c3972f2d67e88f` at `backup/navtasks-0.4.158-test2-honor-accepted`; stable feed stays OFF.
- [ ] Review signed preflight Actions run 43038747378 at https://github.com/sergbokarev95-arch/NavTasks/actions/runs/43038747378; branch `audit/navtasks-1.0.0-preflight-code180` has ONLY test/doc/workflow changes.
- [ ] P1: improve stable update-feed retry for weak networks without touching 24h successful-check throttle, opted-out auto offers, or signed APK installation; isolate and test.
- [ ] P1: protect against corrupted profile index silently falling back to defaults then overwriting previously accessible profile IDs; preserve all preference data and test fail-closed/recovery.
- [ ] Decide Android cloud backup policy for private task/profile data (`allowBackup=true` without explicit rules) and test preservation/safe restore.
- [ ] Test actual old profile snapshots and skipped version floor/migration on separate test profile/device; Samsung permission/notifications/widgets/Glass and HONOR airplane-mode/reboot/installer-cancel. Avoid repeatedly reinstalling user-active profile when tests can use test environment.
- [ ] Final independent UI/UX pass: both themes, date pickers/plan, service/AMOS overdue and history, rich notes, dock optics/animation, bottom safe area, UI stress at Android font scale; fix only reproduced bugs and rerun regressions.
- [ ] Production stable 1.0.0 needs same package/cert, monotonic versionCode >180, proper signed APK, SHA256, release/metadata first-last transaction, release notes, explicit user approval. Do not build stable from old source-main 0.4.59 or expose tester-only UI. NEVER enable stable before device acceptance.

## NEXT — 2026-11-12 automated signed preflight PASS; targeted hardening and final device QA
- [x] Preflight branch `audit/navtasks-1.0.0-preflight-code180` CI43038747378 SUCCESS: unchanged tested app source, synthetic full-profile data roundtrip across task/history/Handover/AMOS/Services, legacy/invalid import rejection, Range and skipped code floor safety, signed nondebuggable same cert code180 and stable channel OFF.
- [ ] Fix stable feed 3500ms single-attempt weak-network problem with bounded retry and explicit manual diagnostics; do not change automatic opt-out or successful 24h throttle. Isolate and verify CI before touching phone.
- [ ] Fail-safe ProfileStore index load on damaged JSON; prevent default placeholder list becoming permanent overwrite of profile IDs, preserving original profile preference files. Write reproducible tests, not broad storage refactor.
- [ ] Decide cloud/transfer Android backup allowed data under `allowBackup=true`, implement explicit rules if needed without removing manual full export.
- [ ] Remove/hide public-facing TEST-only manual channel control in actual production build (retain internal opt-in tester path as needed); final HIG/Glass/dark/light review.
- [ ] On HONOR offline/online update switch/relaunch, notification scheduling across reboot/timezone, widgets, search/calendar, exact service/AMOS history and template/full-profile import; run Samsung smoke and old export version-jump matrix on separate test profile/device.
- [ ] After targeted fixes: full CI + signed RC same app ID/cert and versionCode>180, full export before on-phone install, independent UI/functional acceptance; separate approval for stable 1.0.0 and only then publish public APK assets followed by enabling stable feed last.

## NEXT — 2026-11-12 verify startup automatic update offer before 1.0.0
- [x] Root cause found: normal startup intentionally checks stable only; TEST1/2 were manual and stable feed is inactive. Task reminder notifications are NOT part of this issue.
- [x] Built signed PRIVATE RC15/code182 `0.4.160-rc15`, CI43044178187 SUCCESS. RC-only opt-in auto TEST offers with separate success throttle and Later suppression; installed source remains untouched. QA toggle must disappear from stable 1.0.x.
- [x] Built signed TEST3/code184 `0.4.162-test3` CI43044626615 SUCCESS; public TEST prerelease publisher CI43045892670 SUCCESS, `test.json available:true` code184 minFloor182, `stable.json available:false`. https://github.com/sergbokarev95-arch/NavTasks-Updates/releases/tag/test-v0.4.162
- [ ] Verify private exact accepted-code180 fallback `0.4.163-rc17` code185 CI43045311279 SUCCESS, private draft signing parity, before phone testing; never install downgrade or clear user data.
- [ ] HONOR FULL export and install rc15/code182 in place; enable both switches; cold restart should auto-show TEST3 offer code184; Later suppress same version, manual test check must reopen, disable auto offers should prevent startup; offline startup quiet. After consent, install TEST3 code184 and reconfirm all data/AMOS/Handover/Services/widgets/dock retained. Actual QA not reported yet.
- [ ] Final production release cleanup: eliminate tester-only UI and TEST channel from normal 1.0.0; fix weak stable feed network, profile-index corruption fallback, backup policy, old profile migration and Samsung/Android lifecycle; rerun signed CI and full real-device regression; user approves stable 1.0.0 separately.
## NEXT — 2026-11-12 signed RC15/test3/recovery ready for HONOR
- [x] Private RC15/code182 signed CI43044178187 SUCCESS: https://github.com/sergbokarev95-arch/NavTasks/releases/tag/untagged-a7ffbdc6242655c1ab1c.
- [x] Public test3/code184 verified and test channel active floor182; stable still OFF.
- [x] Exact accepted source emergency recovery code185 CI43045311279 SUCCESS https://github.com/sergbokarev95-arch/NavTasks/releases/tag/untagged-c3d917192472827f56b3.
- [ ] HONOR: full profile backup; install RC15 *in place*, turn on RC-only QA startup switch and existing auto-offers switch; fully close + relaunch, check *automatic* TEST3 pop-up. Test Later, manual check, toggle OFF and offline. Then optional TEST3 installation and data preservation. No actual device PASS yet.
- [ ] Subsequent production gate: stable-feed weak network, profile-index fail-safe, backup policy, tester UI removal, real old version jumps/Samsung, release/installer UI polish, final signed QA and separate stable approval.

## NEXT — 2026-11-12 HONOR automatic update popup verified
- [x] Screenshot confirms RC15/code182 showing automatic startup TEST3/code184 update popup with `Обновить` and `Позже` on Today screen. Do NOT confuse startup TEST QA popup with Android task notifications.
- [ ] Press `Позже`, cold-start again (no popup expected), confirm manual `Проверить тестовый канал` still finds TEST3; use unit/new-code test to independently prove Later per-version suppression vs 24-hour auto-check throttle. Check auto-offer toggle OFF, offline silent and re-enable. Then optionally install TEST3 with data validation.
- [x] Isolated hardened stable-feed transport CI43142231890 SUCCESS. Keep it isolated until integrated into newer signed code>184 release candidate with focused regression/device check; do not include test-only QA controls in stable 1.0.x.
- [ ] Finish corrupt-profile-index protection, backup privacy decision, skipped-version migration, HONOR and Samsung E2E, last visual/functional audit, then obtain explicit user acceptance before 1.0.0 stable publishing.

## NEXT — 2026-11-12 automatic popup and Later accepted on HONOR
- [x] RC15/code182 normal startup automatically offered public TEST3/code184, screenshot confirmed.
- [x] User confirms `Позже`/subsequent launch and manual `Проверить тестовый канал` behaved correctly; settings screenshot confirms manual popup still available. Reminder: no-repeat is also explained by independent 24h successful-check throttle, so per-version suppression should remain unit tested.
- [x] TEST3 public release `test-v0.4.162` code184 compatible floor182 with correct pinned URL/hash/signer; stable feed `available:false`; exact prior TEST2 app-source recovery RC17 code185 CI SUCCESS private.
- [ ] After full export, in-place install TEST3/code184 using Update, verify all profiles/tasks/history/AMOS/Services/Handover and settings. User currently installed RC15/code182; do not install recovery unless a regression.
- [ ] Final blockers: integrate stable-feed weak-net correction (isolated CI43142231890 SUCCESS), corrupted profile-index fail-safe, Android OS backup policy, old-version skipped upgrade migration, Samsung, final HIG/UI check, signed release candidate and explicit 1.0.0 approval.

## NEXT — 2026-11-12 TEST3 code184 installed on HONOR
- [x] User screenshot proves `0.4.162-test3` / code184 in Settings after in-app update RC15/code182; normal `Предлагать обновления` toggle still enabled. Startup offer, Later and manual reopen were already reported PASS.
- [ ] Obtain explicit user confirmation that tasks/profiles/completion history/AMOS/Services/Handover still present after TEST3; do not infer from Settings alone.
- [ ] P1 ProfileStore malformed initialized profile-index must never cause silent overwrite of profile IDs on next save; preserve old profile preference files and block risky writes or use verified recovery snapshot. Exact code in `app/src/main/java/com/navtasks/app/ProfileStore.kt` lines165–183; separate backed-up branch and pure regression tests; no mass storage rewrite.
- [ ] Integrate signed stable-feed weak-net fix after separate CI43142231890 PASS, and remove QA-only auto TEST control in production; rerun all tests with code >184 and signer parity.
- [ ] Cloud backup policy, historical profile/skip-version migration, Samsung, notification/widget lifecycle and Apple HIG visual stress review; 1.0.0 stable public release only on separate approval.

## NEXT — 2026-11-12 signed TEST3 HONOR data PASS; profile-index P1 safety QA
- [x] User confirms ALL user data exists after signed `0.4.162-test3`/code184 installation. Startup auto-offer, Later/manual reopen and in-place TEST3 update user-accepted.
- [x] Back up exact accepted source `cd63c8325ac0e89e251c056597584a587a9f9e42` at `backup/navtasks-0.4.162-test3-honor-accepted`.
- [ ] Complete no-publish CI #43166894862 and any targeted fixes for `feature/navtasks-pre1-profile-index-recovery`, new corrupt-index/validated-backup safety test matrix. Do not touch current user's code184/data.
- [ ] Integrate separately proven weak-link stable feed improvement, decide Android OS backup privacy, check historical export/skip-version migration and Samsung, finish dark/light UI and Glass audit. Produce signed candidate code >184 with original app ID/cert, QA on real devices/data then separate explicit stable 1.0.0 approval. Keep `stable.json available:false`.

## NEXT — 2026-11-12 index-safety CI complete, still NOT shipped
- [x] Full user acceptance of TEST3 code184 on HONOR including all data preservation and auto-update path.
- [x] Exact source backup `backup/navtasks-0.4.162-test3-honor-accepted`.
- [x] Isolated index-protection patch CI43166894862 SUCCESS: strict validated profile index, independent last-good copy, no default profile substitution, no deletion of user data; no publish.
- [ ] Integrate independent stable weak-network patch CI43142231890 with this safety patch in higher-code signed release candidate (code>=186, since recovery code185 exists), run full tests and verify signer, then test in-place without uninstall.
- [ ] Decide Android `allowBackup`/cloud privacy policy and validate export/restoration, old-version profile migration on separate sample, Samsung updates/widgets/notifications, final full day/night HIG audit; publish 1.0.0 stable only after explicit approval.

## NEXT — 2026-11-12 signed integrated RC18 is ready for HONOR validation
- [x] Full TEST3 code184 data accepted on HONOR, protected accepted source and known signing cert.
- [x] RC18/code186 integrates only stable feed weak-net hardening and profile-index safety, all current TEST3 unit tests restored. Signed CI43169515052 SUCCESS and PRIVATE APK draft `untagged-8410913265242890d745`. Public stable remains off; test feed TEST3 code184.
- [x] PRIVATE exact accepted TEST3-source fallback RC19/code187 signed CI43169194383 SUCCESS, draft `untagged-78d81ebbde4f0d1fba02`.
- [ ] User FULL export, then install RC18 over existing TEST3 **IN PLACE** (no uninstall). Verify profiles/task history/AMOS/Services/Handover and dock day/night. Manual stable check with internet and offline, no erroneous startup prompt while stable unavailable. Check reminders/widgets/click flows.
- [ ] If RC18 regression, in-place reinstall signed code187 RC19 exact accepted source after backup; future code >=188 if used. DO NOT install fallback routinely.
- [ ] Android OS backup privacy policy, historical skip-version migration on independent test profile, Samsung, tester-only UI gating in production, system PackageInstaller limitations/post-update acknowledgment and complete UI/UX audit before separate approved production 1.0.0 signed release.
