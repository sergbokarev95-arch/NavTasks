# Liquid Glass card surface adaptation — isolated preview 1

## Why
- HONOR A/B/C Glass Capture diagnostics confirmed visually that ЭКРАН and БЕЗ HAZE were indistinguishable while the Lab reference with opaque gradients/borders showed rich refraction. Haze's mere presence was not the cause.
- Most real static NavTasks cards use the shared `Modifier.navGlass(p, role)` with translucent fills (~47–52% in dark theme). A black background flattens their tonal contrast when sampled through the Backdrop dock.
- The target is not turning all cards into refractive glass, which would be slow and hard to read. Instead ordinary content surfaces become opaque with gentle HIG-inspired tonal gradients and slightly clearer 1dp borders. The true glass is reserved for overlay dock/FABs.

## Scope
- Branch: `feature/liquid-glass-surface-preview` based on tested 0.4.60 candidate source `c3cd12e37c91d789e54ae32e1b61d6d03f46ab79`.
- Central `navGlass()` enhancement for roles PANEL, TASK, NESTED_TASK, CONTROL, both light/dark. Gradient from left to right, optical borders from top to bottom; role-specific muted tones.
- NAVIGATION and SELECTED use exactly the previous transparent recipe (do not rewrite current dock/active state). Colors for warnings/overdue/completed remain on top. Card dimensions, text, icons, inputs and business logic unchanged.
- No modifications to `NavTasksUi.kt`, `NavLiquidGlassDock.kt`, `NavLiquidDockOptics.kt`; CI tests their Git blob SHAs.
- Separate test app `com.navtasks.app.glasssurface`, clearly marked `NavTasks Glass Surface`, signed ZIP. No real app database is changed. Preview has its own profile storage. User can import copy if needed.
- This is first stage only: visually compare Day/Night cards, actual scroll through the dock, all four tabs; no permanent main release before user approval. Static controls can be tuned separately if density is excessive.


## Verified delivery — Preview 1 (2026-11-08)
- Source commit `9d210be7d0dc5d4e5f3bcaa24a49a17ad0f920f7`; CI https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42404664493 SUCCESS (`:app:testDebugUnitTest`, `:app:assembleDebug`).
- Actual signed package inspected by aapt: `com.navtasks.app.glasssurface`, `0.4.60-surface-preview1` versionCode 83.
- Signed APK verified within ZIP; ZIP byte size `10,856,702` (raw APK `31,327,764`); ZIP SHA-256 `1c0df6d2c3aa28eeac80ef525cd94999f393af05a3173fcfc4d895a4c17739ac`.
- Direct link https://github.com/sergbokarev95-arch/NavTasks/releases/download/glasssurfaces-preview1/NavTasks_LiquidGlass_Surfaces_Preview1.zip
- CI confirmed that original `NavTasksUi.kt`, `NavLiquidGlassDock.kt` and `NavLiquidDockOptics.kt` are unchanged. Backend/task/AMOS/service/profile data logic unchanged.
- Await user HONOR acceptance in light/dark: tonal variation and card borders beneath dock, readable glyphs/labels, small controls, completed/overdue state accents. Verify smooth scroll and selected lens overhang.
- This is an **independently installed preview** and will not share profiles with the installed 0.4.60. `main` is still 0.4.59 application source. No stable release promotion.
