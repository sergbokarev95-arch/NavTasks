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
