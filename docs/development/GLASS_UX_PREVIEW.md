# NavTasks Glass UX — phased action controls and pinch-to-switch experiment

## Baseline
- From signed user-tested `v0.4.62-liquid-glass-surfaces-candidate` code snapshot `7bea5101e051774d4fe82a74a2395d8d184ce27e`. Approved night graphite lens, lowered dock and solid opaque gradient cards **unchanged**.
- Isolated Android application ID `com.navtasks.app.glassux` (different local storage from installed real `com.navtasks.app.glass`); debug label `NavTasks Glass UX`.
- NO merge to production main; stable source backups and recovery 0.4.63 remain.
- User saw in Settings an awkward chopped bottom near Android gesture bar. Also wants real Liquid Glass for UI buttons, plus quick profile selection via two-finger pinch toward center.

## Scope (Preview 1)
1. Settings only: remove `navigationBarsPadding()` on full-screen scroll parent, apply `weight(1f)` to scrolling content and `WindowInsets.navigationBars` as final list spacer. Avoid reserved blank footer and make final NavPanel fully scroll above gesture area. No changes to actual settings.
2. Two-finger pinch gesture only on ordinary root app screens. Trigger when fingers begin separated by >=100dp, shrink by >=52dp and >=25%; never consume one-finger gestures or minor two-finger motions. Disable on editor, Settings, calendar, full-screen management and when picker is already open. Profile title header still offers full ProfilesScreen as an alternative.
3. The popup appears centered above a blurred (17dp) frozen-in-place UI, with dim scrim, list of available profiles, active check and dismissal outside/back. Tapping a profile follows EXACT existing select + switchProfile() sequence for task, Handover, Services and AMOS stores. Home navigation state reset matches existing ProfilesScreen onBack; no merge, rename, deletion or data migration.
4. First floating-button conversion: '+', Calendar use *direct source* Backdrop layer glass on API33+ as siblings of captured scene. The original Haze buttons remain fallback on API31/32. No change to action callbacks, hit target or positions. Other controls/settings/dialogs should be converted only after reviewing this stage.

## QA gates
- GitHub CI unit tests and signed ZIP release, Android package verified.
- On HONOR: profile selector two-finger pinch versus ordinary taps/vertical/horizontal scroll; check it does not hijack pinches in full-screen Calendar. Test profile change actually reloads tasks, AMOS, Services and Handover and doesn't touch other profile data. Use test profiles (separate app storage).
- Settings: reach last card and scroll it fully above system gesture bar; no coloured rectangle/cut-off footer; test night/day.
- Liquid action buttons: verify 'plus' opens task editor; calendar opens Plan; refraction and no shimmer on scrolling, day/night; main 56dp dock optics untouched.
- Only then consider the remaining circular header, secondary buttons, popovers and dialogs **one component family at a time**.
