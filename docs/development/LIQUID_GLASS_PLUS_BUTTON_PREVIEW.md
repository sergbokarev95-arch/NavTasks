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
