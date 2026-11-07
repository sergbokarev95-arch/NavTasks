# NavTasks — Glass Rendering Technical Audit (2026-11-07)

**Status:** research only; **no application source, version, release, or signing change**. This report compares the current Android NavTasks dock with the public Telegram Android implementation. **Do not implement a new rendering engine without a separate scoped proposal and approval.**

## Verified baselines
- NavTasks GitHub: `sergbokarev95-arch/NavTasks`, main `f6cb1eb0d9a4a11072f11cc964a4d534416cb9bc`; latest app version `0.4.56` / versionCode `78`; `dev.chrisbanes.haze:haze:1.7.1`, Jetpack Compose. Signed release `v0.4.56-glass-beta`.
- Telegram Android GitHub: `DrKLO/Telegram`, master `3de37c8cfb039452be7398909461da6288babf15`. Its repository license is **GPL-2.0**; use as research/behavioral reference, **not** copy-paste material. Review license implications before incorporating any actual source or shader.
- Pinned Telegram implementation:
  - [MainTabsActivity](https://github.com/DrKLO/Telegram/blob/3de37c8cfb039452be7398909461da6288babf15/TMessagesProj/src/main/java/org/telegram/ui/MainTabsActivity.java)
  - [MainTabsLayout](https://github.com/DrKLO/Telegram/blob/3de37c8cfb039452be7398909461da6288babf15/TMessagesProj/src/main/java/org/telegram/ui/MainTabsLayout.java)
  - [GlassTabView](https://github.com/DrKLO/Telegram/blob/3de37c8cfb039452be7398909461da6288babf15/TMessagesProj/src/main/java/org/telegram/ui/Components/glass/GlassTabView.java)
  - [BlurredBackgroundDrawableRenderNode](https://github.com/DrKLO/Telegram/blob/3de37c8cfb039452be7398909461da6288babf15/TMessagesProj/src/main/java/org/telegram/ui/Components/blur3/drawable/BlurredBackgroundDrawableRenderNode.java)
  - [BlurredBackgroundDrawableViewFactory](https://github.com/DrKLO/Telegram/blob/3de37c8cfb039452be7398909461da6288babf15/TMessagesProj/src/main/java/org/telegram/ui/Components/blur3/BlurredBackgroundDrawableViewFactory.java)
  - [LiquidGlassEffect](https://github.com/DrKLO/Telegram/blob/3de37c8cfb039452be7398909461da6288babf15/TMessagesProj/src/main/java/org/telegram/ui/Components/blur3/LiquidGlassEffect.java)
  - [AGSL shader](https://github.com/DrKLO/Telegram/blob/3de37c8cfb039452be7398909461da6288babf15/TMessagesProj/src/main/res/raw/liquid_glass_shader.agsl)
  - [GlassEngine](https://github.com/DrKLO/Telegram/blob/3de37c8cfb039452be7398909461da6288babf15/TMessagesProj/src/main/java/org/telegram/utils/glass/GlassEngine.java) — general library pipeline; do not infer it is directly driving the MainTabsActivity dock.
- NavTasks sources: `app/src/main/java/com/navtasks/app/NavGlass.kt` and `NavTasksUi.kt` at `f6cb1eb0d9a4a11072f11cc964a4d534416cb9bc`.

## Current NavTasks rendering, verified against source
1. The page's scrollable content is marked `hazeSource(hazeState)`, and the dock and adjacent action controls use `navFrostedOverlay(...)` with `hazeEffect`, a `34.dp` blur, mild noise and tint. This **is real backdrop blur**, not a static screenshot or opaque glass simulation.
2. A second `navGlass` overlay applies a relatively opaque fill and a border gradient on top. Multiple alpha layers can suppress the visibility of the blurred background, particularly in Day mode (dock Haze background approx alpha 0x88 and additional navigation fill approx alpha 0x7A). Perceived opacity varies with stacking; do not treat alpha values as final measured appearance.
3. The four-tab active lens is drawn in a Compose Canvas with interpolated top/middle/bottom gradients and edge strokes. It slides via `animateFloatAsState` springs (dampingRatio = 1, stiffness 550 for translation), and switches toward matte material when at rest. It **does not sample or optically refract** the image behind itself.
4. Each main tab remains a Compose selectable target with haptics and the established swipe/navigation state machine. Dock outer geometry is 56dp high, with careful 1/2dp inner clearances and 15dp outer margins; preserve these unless a specific device geometry issue is proved.
5. A perfectly uniform dark/black area under a glass dock gives blur/refraction little visual information: compare rendering over **actual scrolling cards** in both light and dark themes, not only an empty black Today screen.

## Telegram implementation, verified against source
1. `MainTabsActivity` selects a RenderNode-backed content source on Android 12+ (API 31+), otherwise a themed color source; links the source's coordinate system to actual view positions. It explicitly enables the optional Liquid Glass effect only when `LiteMode.FLAG_LIQUID_GLASS` permits it.
2. `BlurredBackgroundDrawableViewFactory` allows the liquid effect only on Android 13+ (API 33+) and when the drawable is the RenderNode variant. Therefore, the Android visual stack **does not promise identical refractive rendering on all OS versions**, even if the app supports many versions and vendor skins.
3. `LiquidGlassEffect` uses an Android `RuntimeShader` attached as a `RenderEffect` to a RenderNode. The shader calculates a signed-distance rounded-rectangle boundary, a surface normal and refracted texture coordinates, then samples the source image. Parameters include boundary radii, thickness, refraction intensity and refraction index. This is a true pixel-displacement optical treatment, not merely blur or a gradient.
4. `BlurredBackgroundDrawableRenderNode` keeps separate RenderNodes for material/source and finished drawing; the enhanced effect is gated to API 33+. `MainTabsActivity` also updates its display list when page content changes.
5. `MainTabsLayout` uses `SpringAnimation` for selected-tab position and an optional press/drag interaction. Its low-bouncy spring and long-press scrubbing are UX decisions **not automatically suitable** for NavTasks. `GlassTabView` draws tab-specific selection treatment and animated icons; this is distinct from the material/refraction pipeline.
6. `GlassEngine` provides invalidation/position monitoring in Telegram's wider glass implementation. It is **not evidence by itself** that every displayed Telegram glass component is re-rendered through that engine.

## Gap and risk analysis
- **Real gap:** missing local refraction in NavTasks, not missing backdrop blur.
- **Likely visual issue:** layered Haze background + `navGlass` fill + active-lens gradients can appear matte or tinted, hiding the very content they are meant to reveal.
- **Do not lift Telegram Java or AGSL source** into the user's app without appropriate license review and explicit approval. A separately authored, independently tested Compose/Android shader could implement the same broad optical principle.
- **Compose integration is nontrivial:** the runtime shader needs an input texture/render layer with the correct content and clip bounds. Applying `RenderEffect` blindly to the Haze lens risks shader effects on the *wrong pixels*, extra offscreen passes, poor contrast or frame-time spikes. Research/design first.
- **Android fallback:** keep current Haze material on older or low-end devices. Optional shader route only for suitable API/GPU levels with an explicit compatibility/performance fallback. An OS version alone does not guarantee smooth rendering.
- **Input/accessibility:** preserve selectable semantics, haptic controls, tab swipe priority, system Back, icons, touch targets and reduced-motion/accessibility behavior.

## Proposed *unapproved* incremental implementation
**Stage 0 — baseline (no source change):** capture actual HONOR Day/Night dock while cards scroll behind it; record regular and rapid tab changes; compare 60/120Hz frame timings, contrast and gesture responsiveness. Also test at least one non-HONOR device when available.

**Stage 1 — low-risk material tuning:** keep the existing `HazeState`, blur, layout dimensions, tab-selection mechanics and code structure. In `NavGlass.kt` only, tune stacked overlay opacities and neutral tint/edge highlights under controlled A/B screenshot comparison. Ensure the lens stays readable on both light and dark backgrounds. Avoid adding any GPU shader in this stage. Test + revert if contrast or geometry worsens.

**Stage 2 — optional refraction prototype, separate change:** only with user approval, prototype an independently authored API 33+ refractive renderer using a controlled content layer. Strictly retain the Haze fallback and protect rapid tab switching, scrolling and screen changes. Benchmark frame times, GPU/render thread, memory, overdraw, white flash, black flash and contrast. **Do not merge** unless it is measurably smoother or visually better on HONOR and stays acceptable elsewhere.

**Stage 3 — optional animation polish:** consider slightly different spring dynamics only after the material is stable. Do not copy Telegram's bouncy long-press selection just because the implementation exists. Honor Apple's disciplined geometry and perceived continuity.

## Acceptance gates for any future change
- No business/model/profile/AMOS/service/task data changes; only `NavGlass` rendering and, if separately approved, narrowly scoped integrations.
- Keep 4 tabs and their click, swipe and accessibility behavior.
- No docking jumps, border clipping, white/blue flashes, stale blur or duplicated active-pill shadows.
- Compare Night and Day, empty and content-filled background, scrolling and repeated rapid tab selections.
- Maintain smooth interaction at the device's active refresh rate: at 60Hz a frame budget is ~16.7 ms; at 120Hz ~8.3 ms. Use real traces; the budgets are not promises of achieved performance.
- CI/unit tests cannot validate optical quality: HONOR screenshots and physical-device acceptance required.
- Update this report and the five development handoff docs only after an approved implementation and verified release.

## Recommendation
**Do Stage 0, then Stage 1 first.** There is already a working realtime blur stack. Reducing excessive tint/material opacity and strengthening selective edge definition is a smaller and safer candidate than importing or rebuilding Telegram's GPU pipeline. Introduce an optional refractive shader only if these corrections are demonstrably insufficient.

**Implementation status: NONE. This is an analysis-only decision brief.**

## First approved prototype — 0.4.57 (2026-11-07; pending CI/physical verification)
The user asked to move beyond translucency and achieve actual glass. Rather than importing GPL-2.0 Telegram code or upgrading all existing Haze 1.x usages to Haze 2, we introduced an independently written API33+ capsule-distance AGSL sampler (`NavGlassOptics.kt`). It applies a narrow inward texture-coordinate bend to a separate already-blurred Haze layer. The final face/edge, selected Canvas and all glyphs are drawn afterwards, so UI controls are not distorted. Existing API31/32 Haze fallback remains; construction failures fall back to it.
A small material adjustment also reduces previously stacked high-opacity tints and shortens blur. Preserve original 56dp layout, critically-damped selection and four-tab navigation. **No runtime appearance or performance claim is warranted until testing on HONOR.**
