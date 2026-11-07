# Stable Haze rollback 0.4.61 — safety counterpart for 0.4.60 candidate

This **is not** the new glass: it recompiles the exact stable 0.4.59 application code and assets with versionCode=84/versionName=0.4.61-beta. This version is necessary because Android normally rejects downgrades from versionCode=83 to 81 while keeping data.

- Built from stable main docs-only commit `77d35e04ea3f1bb20e2976f2efe27ae41dbe3a2e`, with only `app/build.gradle.kts` version bump and standalone CI workflow.
- Same `com.navtasks.app.glass` and same `betaStable` signing key. No task, data, Service, AMOS, widget, navigation, icon, theme or NavGlass renderer modifications.
- Source SHA verified for `NavGlass.kt` and `MainActivity.kt` against stable 0.4.59.
- Use ONLY if in-place candidate 0.4.60 Glass Lab optics are visually or functionally rejected. Upgrade over the existing app, never uninstall. Export a complete profile before any test.
- The stable `0.4.59` release and baseline branch `backup/navtasks-0.4.59-before-liquid-glass` remain intact.
