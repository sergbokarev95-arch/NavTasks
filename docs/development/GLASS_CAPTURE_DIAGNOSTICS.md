# Glass capture diagnostics (isolated app, 2026-11-08)

Purpose: compare identical optical material over the same target app's Haze scene, no-Haze scene and copied Glass Lab 0.1.5 scene.
Mode ЭКРАН: real NavTasks page, Haze source enabled; БЕЗ HAZE: same page with Haze source off; ЭТАЛОН: same Lab 0.1.5 gradient/border/card typographic geometry and strings.
Modes render inside one existing LayerBackdrop recorder; selector and NavLiquidGlassBottomBar render as siblings outside it.
Dock and optics are unchanged from candidate commit c3cd12e37c91d789e54ae32e1b61d6d03f46ab79 and verified by Git hash in CI.
Safety: separate debug suffix .glassdiagnostic; no production data shared; no change to stable main. ZIP contains signed APK, installed alongside NavTasks.
Test: dark and light; scroll labels and card borders beneath dock. If reference matches original Lab, content/background accounts for much of difference. If Haze-off affects same real scene, inspect capture ordering. If reference does not match Lab, investigate root/layer differences.
Do not edit production task cards or shader based on assumptions. Await phone screenshots.
