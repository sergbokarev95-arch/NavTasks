# NavTasks Liquid Glass Lab — separate experiment

Branch: experiments/liquid-glass-backdrop. Do **not** merge directly to main.

## Why
HONOR rejected NavTasks 0.4.57 / 0.4.58 experiments because a custom AGSL shader was applied to a previously Haze-blurred dock output. It made moving text shimmer and distorted material geometry. Stable main 0.4.59 restored the old working Haze renderer.

## New approach
This laboratory is an entirely separate APK with application ID com.navtasks.glasslab, no application data, accounts, permissions, notifications or background services. It cannot overwrite the main NavTasks APK.

Uses io.github.kyant0:backdrop:2.0.0, Apache-2.0 library, in a dedicated :glasslab module. A LayerBackdrop records the unblurred scrolling screen into a GPU GraphicsLayer; drawBackdrop samples it in the correct coordinate system and applies vibrancy + blur + lens, separate from foreground text/icons. A local toggle switches optical rendering on/off; Day/Night toggle and scrollable colored cards test visibility and stability. Outer dock geometry: 56dp, 15dp horizontal page inset, four tab cells, 52dp active lens.

There is intentionally no direct Telegram GPL implementation copied or ported. Telegram remains an architectural reference for RenderNode source capture and adaptive material.

## Validation
1. GitHub Actions build :glasslab:assembleDebug and unit tests, separately from :app.
2. Install lab APK alongside production; never uninstall production NavTasks. A stable Beta signing key is used for this separate APK when available.
3. Day/Night over text-rich scrolling background. Check actual optical edge distortion, no independent text shimmer, no flicker, stable tab transitions, selected lens and full dock geometry.
4. Test fast tab switching and scroll with 60/120Hz display, assess device frame pacing and GPU jank where feasible.
5. If prototype succeeds on HONOR, propose a small integration mapping from LayerBackdrop to the NavGlass bottom bar, with Haze fallback and exact 56dp geometry. User approval required before modifying main.

## Current status
First lab source, pending CI and physical HONOR validation. Do NOT claim this achieves the final material until device testing.

## Build attempt 1
- Initial CI 42385094225 failed on AAR metadata: Backdrop 2.0.0 requires compileSdk 37, whereas the new lab was set to 36. The correction changes only the lab compileSdk and workflow Android SDK install to 37; production :app stays compileSdk 36. Gradle plugin 8.13.2 may warn about untested SDK 37; await CI before accepting.
