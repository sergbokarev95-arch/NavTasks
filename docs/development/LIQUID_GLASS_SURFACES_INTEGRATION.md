# NavTasks 0.4.62 Beta Liquid Glass surfaces — integration candidate

- Source: accepted `glasssurfaces-preview1`, visual finish Preview 2 (`67932237e875edc0eec507fde957147dc0c8d3e4`), and **only** app ID/version restored for in-place Beta update.
- App identity: `com.navtasks.app.glass`, versionName `0.4.62-beta`, versionCode 85; same `betaStable` signing key. Same local data if upgraded **without uninstall**.
- Two final polish tweaks: night selected capsule 0xFF3F4B58 alpha 0.82; dock bottom margin 8dp→4dp. Day theme and blue glyph unchanged. All solid gradients, all original lab refraction optics, 56dp geometry and animations unchanged.
- CI checks exact component SHA parity with separate Preview 2, runs full app unit suite, checks APK identity, signs and compresses ZIP for weak internet; releases candidate without touching main.
- Safety: production main 0.4.59 source unchanged; backup stable source `backup/navtasks-0.4.59-before-liquid-glass`. **Do not ask user to install until a same-ID stable Haze rollback APK with versionCode86 is signed and published.** Export full profile first; no uninstall.
- Physical HONOR test of final adjustment still pending. Only after acceptance should we consider merging to main and final stable publication.
