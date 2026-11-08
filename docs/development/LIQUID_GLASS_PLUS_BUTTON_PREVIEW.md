# Liquid Glass floating + button — Preview 2

## Context
- User validated HONOR Quick Profile Preview1 gesture, profile switching, background blur and Settings footer ("Да все работает хорошо").
- Next incremental objective: apply accepted 0.4.62 Liquid Glass dock optics to exactly ONE floating action: '+' (Tasks tab).
- Based on \`feature/quick-profile-pinch-settings-footer-preview\` accepted preview source \`8b7a29383e2875481092256931413d973edafc8e\`.

## Changes
- Introduce \`NavLiquidGlassAddButton\` in a dedicated Kotlin source file. Same \`rememberLayerBackdrop\` source as the dock, SAME \`vibrancy / blur / lens\` effects, Highlight and outer shadow, sheen, optical border. Reuse \`NavLiquidDockOptics.bar\` directly (2dp blur, 12dp refractive rim height, 36dp displacement). Retain 54dp size, 28dp '+' glyph, current theme blue, haptic click and editor opening.
- Only Tasks (+) uses this on Android API>=33. Android 12/12L retains current unchanged \`NavGlassAddButton\` Haze implementation. Today calendar and Settings icons remain untouched for next separate passes.
- Quick profile pinch and Settings footer changes remain exactly as HONOR-approved Preview1. All task/service/AMOS/store/notification data and profile-switching logic unchanged.
- Isolated app ID \`com.navtasks.app.glassquick\`, upgrade from Quick Preview1 versionCode 87 to Preview2 code88. No production installed app/data affected.
- CI compiles unit tests and signed APK, validates unchanged dock/card/haze sources, compresses signed artifact as small ZIP and publishes only as an isolated prerelease.
- HONOR test: tab Tasks; scroll card borders and text under circular '+' button, check true visible refraction at rim, smooth text with no shimmer, plus foreground always crisp and blue, tap opens task editor as before, Day/Night, quick profile gesture and Settings footer remain correct.
- No main-app merge and no new optical treatment on other controls before physical acceptance.


## Verified release / device acceptance gate
- User approved previous Quick Profile Preview1 on HONOR ("Да все работает хорошо"), including pinch switch and Settings footer.
- CI https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42442451411 SUCCESS; source `50c8868af2e53455d3f993956af81848802cbc25`. Full `:app:testDebugUnitTest`, `:app:assembleDebug`, APK package/identity and ZIP extraction-integrity checks passed.
- Signed ZIP https://github.com/sergbokarev95-arch/NavTasks/releases/download/glassplus-preview2/NavTasks_Glass_Plus_Button_Preview2.zip (10,872,529 bytes). SHA256 `bd721d3d1d668972b279513a4d6be7afb5239065ef99e221bd48566b675ee4e6`.
- Isolated app `com.navtasks.app.glassquick`, versionCode 88, versionName `0.4.62-glassplus-preview2`; updates previous Preview1, but NOT installed main app.
- New true glass '+' physical HONOR test pending. Do not port this button to production or convert Calendar/Settings buttons until device confirms refraction/legibility/press behavior in both themes.
