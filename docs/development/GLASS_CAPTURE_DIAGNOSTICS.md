# Glass capture diagnostics (isolated app, 2026-11-08)

Purpose: compare identical optical material over the same target app's Haze scene, no-Haze scene and copied Glass Lab 0.1.5 scene.
Mode ЭКРАН: real NavTasks page, Haze source enabled; БЕЗ HAZE: same page with Haze source off; ЭТАЛОН: same Lab 0.1.5 gradient/border/card typographic geometry and strings.
Modes render inside one existing LayerBackdrop recorder; selector and NavLiquidGlassBottomBar render as siblings outside it.
Dock and optics are unchanged from candidate commit c3cd12e37c91d789e54ae32e1b61d6d03f46ab79 and verified by Git hash in CI.
Safety: separate debug suffix .glassdiagnostic; no production data shared; no change to stable main. ZIP contains signed APK, installed alongside NavTasks.
Test: dark and light; scroll labels and card borders beneath dock. If reference matches original Lab, content/background accounts for much of difference. If Haze-off affects same real scene, inspect capture ordering. If reference does not match Lab, investigate root/layer differences.
Do not edit production task cards or shader based on assumptions. Await phone screenshots.


## Verified CI + download
- Source commit: eb504f145b8a4e0cf8612cd9e814b8c6c6a1ab8f (diagnostic selector + package, CI credential syntax correction).
- CI run https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42402960923 SUCCESS: app unit tests, signed APK, aapt package verification, ZIP integrity check.
- Separate app ID com.navtasks.app.glassdiagnostic / 0.4.60-capture-diagnostic1 (versionCode 83).
- Compressed ZIP https://github.com/sergbokarev95-arch/NavTasks/releases/download/glasscapture-v0.4.60-diagnostic1/NavTasks_Glass_Capture_Diagnostics_0.4.60.zip (10,865,575 bytes); unpacked signed APK 31,360,544 bytes.
- ZIP SHA-256 969f570d1feab063be5e3ac657e18e839c8767cad30d8a9fbb4893814383d9bf. CI verified inner APK checksum equals its original signed artifact.
- Physical HONOR results PENDING. User must compare ЭКРАН, БЕЗ HAZE, ЭТАЛОН with same theme/card position, scrolling edges/text and lens motion. DO NOT conclude Haze is the cause from source code alone.
- Main app version 0.4.60 candidate remains installed independently; stable main source 0.4.59 unchanged.