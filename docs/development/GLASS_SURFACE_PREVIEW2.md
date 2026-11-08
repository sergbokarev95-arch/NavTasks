# Liquid Glass Surfaces Preview 2 — visual finish (2026-11-08)

## User acceptance and scope
- HONOR screenshots of Preview 1 confirmed dark and day cards/optical dock are visually good. Two final requests: the inactive **selected capsule** on dark theme is too pale, making the blue active glyph seem dull; and dock sits slightly too high above Android gesture navigation.
- Build from the exact signed Preview 1 source `9d210be7d0dc5d4e5f3bcaa24a49a17ad0f920f7`, preserved by release tag `glasssurfaces-preview1`. This is a separate app `com.navtasks.app.glasssurface`, not a production upgrade.
- Make **two and only two visual changes**:
  1. Dark theme **resting selected-lens surface** `Color(0xFF616B77).copy(alpha=.86f)` → `Color(0xFF3F4B58).copy(alpha=.82f)`; same highlight, lens/refraction, animated grey transition, active blue #329CFF, icon, glow and day theme.
  2. Bottom dock placement from `padding(horizontal=15.dp, vertical=8.dp)` to `padding(horizontal=15.dp).padding(top=8.dp,bottom=4.dp)`; moves whole capsule 4dp closer to system gesture bar. Dock remains 56dp, outer optics and selection overhang unchanged. Android system safe insets remain.
- All **solid** PANEL/TASK/NESTED_TASK/CONTROL card gradients/borders from Preview 1 remain unchanged, including both themes; preserve NavTasks business logic/screens/widgets/profiles unchanged.
- Physical HONOR visual validation needed: exact night selected capsule contrast, unchanged blue glyph visibility, edge-to-edge tab spacing, safe gap to system gesture bar, 0→3 movement and top/bottom lens overhang, day theme and scrolling border refraction.
- CI compiles/tests full app, guards four card token files, same independent test package/signing, publishes small ZIP containing signed APK. Only after user approves Preview 2 should in-place production migration be prepared.
