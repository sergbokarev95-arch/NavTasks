# NavTasks Liquid Glass Lab — separate experiment

Branch: experiments/liquid-glass-backdrop. Do **not** merge directly to main.

## Why
HONOR rejected NavTasks 0.4.57 / 0.4.58 experiments because a custom AGSL shader was applied to a previously Haze-blurred dock output. It made moving text shimmer and distorted material geometry. Stable main 0.4.59 restored the old working Haze renderer.

## New approach
This laboratory is an entirely separate APK with application ID com.navtasks.glasslab, no application data, accounts, permissions, notifications or background services. It cannot overwrite the main NavTasks APK.

Uses io.github.kyant0:backdrop:1.0.6, Apache-2.0 library, in a dedicated :glasslab module. A LayerBackdrop records the unblurred scrolling screen into a GPU GraphicsLayer; drawBackdrop samples it in the correct coordinate system and applies vibrancy + blur + lens, separate from foreground text/icons. A local toggle switches optical rendering on/off; Day/Night toggle and scrollable colored cards test visibility and stability. Outer dock geometry: 56dp, 15dp horizontal page inset, four tab cells, 52dp active lens.

There is intentionally no direct Telegram GPL implementation copied or ported. Telegram remains an architectural reference for RenderNode source capture and adaptive material.

## Validation
1. GitHub Actions build :glasslab:assembleDebug and unit tests, separately from :app.
2. Install lab APK alongside production; never uninstall production NavTasks. A stable Beta signing key is used for this separate APK when available.
3. Day/Night over text-rich scrolling background. Check actual optical edge distortion, no independent text shimmer, no flicker, stable tab transitions, selected lens and full dock geometry.
4. Test fast tab switching and scroll with 60/120Hz display, assess device frame pacing and GPU jank where feasible.
5. If prototype succeeds on HONOR, propose a small integration mapping from LayerBackdrop to the NavGlass bottom bar, with Haze fallback and exact 56dp geometry. User approval required before modifying main.

## Build attempt 2 and compatible backend
- CI 42385333484 could not install platforms;android-37 (SDK Manager did not list that package). Rather than upgrading production build tools or relying on unavailable preview SDK, pinned the same Apache-2.0 Backdrop engine to version 1.0.6, whose archived Gradle module compiles against Android 36 and supports the same LayerBackdrop/drawBackdrop/lens APIs. Removed an unnecessary Compose drawRect import. Only the standalone module/workflow changed; main NavTasks 0.4.59 remains untouched.

## Current status
Lab v0.1.0 **compiled, signed and published**, physical HONOR validation still pending. Do NOT claim the final material is achieved until device testing.

## Verified 0.1.0 standalone release (2026-11-08)
- Version 0.1.0 / versionCode 1; applicationId com.navtasks.glasslab (different package from NavTasks).
- Tested source commit 512cb355d0e1a37cba22eb79e5ada7cc32294eab; branch experiments/liquid-glass-backdrop.
- CI https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42385989762 — SUCCESS, Gradle :glasslab:testDebugUnitTest and :glasslab:assembleDebug, separate stable Beta signing.
- Prerelease https://github.com/sergbokarev95-arch/NavTasks/releases/tag/glasslab-v0.1.0; APK https://github.com/sergbokarev95-arch/NavTasks/releases/download/glasslab-v0.1.0/NavTasks_LiquidGlass_Lab_0.1.0.apk.
- APK sha256:caeb1cbc7189e1aca707416f3f3660696dce1438fd546811099e8e7fcb24063c.
- Dependency: Apache-2.0 Backdrop 1.0.6 + shapes 1.2.0; Android compileSdk 36. Avoid Backdrop 2.0.0 in this repo until a stable API37 SDK & suitable AGP are available.
- The library samples the recorded scrollable content layer, not the existing NavTasks Haze layer. Refractive lens appears behind the active selection and outer bar. UI in dark/light theme with effect on/off is available for direct A/B comparison.
- No real-device inspection, frame-time measurements, or user visual acceptance yet. CI only proves compilability and packaging.


## Build attempt 1
- Initial CI 42385094225 failed on AAR metadata: Backdrop 2.0.0 requires compileSdk 37, whereas the new lab was set to 36. The correction changes only the lab compileSdk and workflow Android SDK install to 37; production :app stays compileSdk 36. Gradle plugin 8.13.2 may warn about untested SDK 37; await CI before accepting.

## Build attempt 3
- CI 42385611901 passed Android 36 dependency metadata checks and compiled until Kotlin reported missing Capsule symbol. In the archived 1.0.6 upstream catalog, Capsule is supplied by the separately published io.github.kyant0:shapes:1.2.0 library, not exported as a transitive compile API of Backdrop. Add this direct dependency in the lab module only. No production app changes.

## 0.1.1 — card-border refraction experiment (CI VERIFIED; HONOR pending)
- HONOR screenshots of 0.1.0: strong text/glyph distortion but straight one-pixel card borders across dock; baseline blur 8dp smooths the edges before lensing.
- Only lab code changes: capture remains direct LayerBackdrop, no experimental work in production main. Preserve 56dp bar / 52dp selection and four tabs.
- Reduce outer pre-lens blur 8dp → 2dp and change lens height/amount 24/24dp → 12/36dp; active lens blur 4dp → 1.5dp and 18/12dp → 10/18dp. These are conservative laboratory parameters requiring HONOR acceptance, not a guarantee of optical quality.
- Add subtle surface reflection + fine edge contour in Glass ON only. Glass OFF matte baseline unchanged.
- Move statusBarsPadding outside the scrolling content and follow Day/Night system icon contrast.
- Add pure optical-bounds regression tests. 0.1.0 remains in its release and in backup/glasslab-0.1.0-before-optics. Do not merge into main.
- Test: compare a straight 1dp card border as it *enters, crosses and exits* the top/bottom rim while scrolling, in both themes; check text does not shimmer, pill geometry and four-tab taps. Physical result pending.

### Verified 0.1.1 delivery
- Isolated source commit: f3ef5353dac9dc703bcb811c224c78e7e212fe74; versionCode 2; versionName 0.1.1; package com.navtasks.glasslab.
- GitHub Actions: https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42388053998 SUCCESS, Gradle :glasslab:testDebugUnitTest and :glasslab:assembleDebug.
- Separate signed prerelease: https://github.com/sergbokarev95-arch/NavTasks/releases/tag/glasslab-v0.1.1; APK NavTasks_LiquidGlass_Lab_0.1.1.apk.
- APK SHA-256: 26c5a418e395a83c1c3733e77158e501d2ba5b950a88ed2e5d59ffbd9a5f1d69 (GitHub Actions sha256sum).
- HONOR acceptance outstanding: observe actual curvature of light card outlines under Glass ON, both themes, and check scrolling for text shimmer/jank. Compilation and unit tests do not prove visual quality.
- main remains d2792ab504610d0ddf02738f573db0ca0206041d with production NavTasks 0.4.59 / versionCode 81; no app-module changes. Previous lab prerelease 0.1.0 and backup branch retained.

## 0.1.2 — production dock icons and mobile selection motion (2026-11-08)
- User HONOR day/night screenshots **accept** the visible glass/refraction of 0.1.1: card borders now visibly bend under the 56dp outer dock. Moving shimmer has not been independently measured; keep testing.
- Precisely copy the four glyph stroke paths from stable main NavGlassGlyph into isolated GlassLabDockGlyph: today/check, work case, handover notebook, tasks/checklist. Do not import production :app module or modify NavTasks source.
- Match production dock colors: selected icon/label night #329CFF and day #087DFA, inactive icon text night #F2F8FF day #172B42 at 89% alpha; inactive labels night #AFC4D8 and day #5F768D; 24dp glyph and 10sp label.
- Lens animation follows the stable NavTasks critically damped positional spring (stiffness 550), material spring (700), and up-to-10dp horizontal stretch (600); keep a 2dp selected capsule inset and 52dp baseline height. During travel glass stays optically visible, after settling the active lens becomes matte neutral grey. The full dock keeps the approved Backdrop optics.
- Preserve existing Glass ON/OFF, Day/Night, direct source capture, scrollable cards, and no access to production data. Add model regression tests for tab identities and bounded stretch.
- Release signed isolated Glass Lab 0.1.2 only after GitHub CI success. **Do not merge into main yet**: first verify live switching on HONOR, rapid retargeting, light/dark, border refraction during scroll, clip/overflow at first and last tabs and that the resting selection is neutral grey.


### Verified 0.1.2 lab publication (2026-11-08)
- Isolated source: `049836c2bd05bbd3378162e174a46f2ae249c240`; lab versionName 0.1.2, versionCode 3, same independent `com.navtasks.glasslab` package and Beta signing.
- Main GitHub Actions: https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42389729911 SUCCESS — `:glasslab:testDebugUnitTest` and `:glasslab:assembleDebug`; sign/publish SUCCESS.
- Separate pre-release: https://github.com/sergbokarev95-arch/NavTasks/releases/tag/glasslab-v0.1.2
- Signed APK: https://github.com/sergbokarev95-arch/NavTasks/releases/download/glasslab-v0.1.2/NavTasks_LiquidGlass_Lab_0.1.2.apk
- Signed APK SHA-256: `fdeb481c91252db2c1e39f7ed02acba4541b2488f3a94bcc6d640b4f64a785c9` from CI log.
- Physical HONOR test still pending for new moving-and-grey lens; previous 0.1.1 optics received positive screenshot feedback. Check day/night, four tab icons/contrast, 0→3 and 3→0 rapid taps, dynamic stretch, settling into matte grey, border refraction and shimmering on scroll.
- Source backup before UI changes: `backup/glasslab-0.1.1-before-nav-animation`. Production `main` still NavTasks 0.4.59; no app code migration yet. Preserve staged acceptance before integration.
 
## Glass Lab 0.1.3 — larger travelling selection (2026-11-08)
- User provided visual reference of a lens that expands to roughly 1.3–1.45× its resting tab width, and authorized the specific first experiment of 28dp horizontal expansion.
- One optical-motion parameter only: `GlassLabDockTabs.maxStretchDp` 10f → 28f. Existing critically damped positional/material/stretch springs, first/last-tab position clamp, 56dp bar, 52dp baseline selection, icon strokes/colors, and approved 0.1.1 refraction are unchanged.
- Guard unit test now asserts exact 28dp and an upper bound of 32dp; this is a parameter regression test, **not** proof of good phone animation or absence of clipping.
- Standalone package `com.navtasks.glasslab`, lab versionCode 4 / versionName 0.1.3; signed prerelease should be published only after a passing lab-only CI. Stable main NavTasks 0.4.59 remains untouched.
- Backup before change: `backup/glasslab-0.1.2-before-28dp-stretch`; previous 0.1.2 prerelease retained.
- HONOR test: swipe/tap 0→3 and 3→0 repeatedly while looking at first/last endpoints; ensure the widened lens does not overlap outside dock, settle grey behavior is still legible, labels/icons stay crisp, and moving card edges do not shimmer.


### Verified 0.1.3 delivery
- Immutable source commit `9f3ac3c8574d539141dd828d76c9c08da28fbe6f`; separate package `com.navtasks.glasslab`, versionCode 4/versionName 0.1.3.
- CI https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42390942917 SUCCESS: `:glasslab:testDebugUnitTest`, `:glasslab:assembleDebug` and signed prerelease publishing.
- Signed lab APK: https://github.com/sergbokarev95-arch/NavTasks/releases/download/glasslab-v0.1.3/NavTasks_LiquidGlass_Lab_0.1.3.apk
- GitHub release https://github.com/sergbokarev95-arch/NavTasks/releases/tag/glasslab-v0.1.3
- APK SHA-256 `8f0b7e18cce69c007ff8a3f4e73a40da1affbdaffca3507420a72626ab6f83c9` from CI.
- Acceptance: physical HONOR motion and outer-edge clipping tests **pending**. Only the stretch parameter changed from 10dp to 28dp; main app remains 0.4.59. Preserve backup and older 0.1.2.
