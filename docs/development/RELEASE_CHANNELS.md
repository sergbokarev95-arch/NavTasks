# NavTasks release channels — stable and private beta testing

Last updated: 2026-11-15.

## Stable
- Production app package: `com.navtasks.app.glass`; original signer SHA256 `0cb8a3c691e3fb2198682dff8c226aec710e77c7b8fded7d260a92f359a49c54`. Never rotate signer, reinstall or reset user data merely for an update.
- First public stable: `1.0.0`, versionCode `226`. Built and fully JVM-tested by `navtasks-1.0.0-stable-code226.yml`. No `-rc` or `-beta` suffix in the released APK.
- Stable update feed: `https://raw.githubusercontent.com/sergbokarev95-arch/NavTasks-Updates/main/channels/stable.json`. It is separate from any QA/test metadata. `NavUpdateChecker.parseStableFeed` strictly pins the APK to `https://github.com/sergbokarev95-arch/NavTasks-Updates/releases/download/v<version>/NavTasks_<version>.apk`. Do not edit that guard to use untrusted download locations.
- The stable feed is enabled only *after* copying and verifying the signed APK and SHA-256 in `NavTasks-Updates`; `available:false` until then. Updating is always optional, with `Обновить / Позже` and manual Settings check.
- Never publish an update with a versionCode lower than any installed stable/test or recovery candidate. Code `225` is a QA17-source fallback for RC1, so stable code starts at `226`.
- Stable updates are released when verified bugs or improvements warrant them; there is no fixed monthly schedule. User may choose the public version number. A direct jump from older signed versions to the latest stable is permitted if format migration and version compatibility are tested.

## Experimental betas
- Future pre-releases use an isolated beta branch and signed APK installed *only* by selected testers. Example: testing changes for `1.0.1` with `1.0.1-beta.3`, then `1.0.1-beta.4`. Public `1.0.1` must have a larger versionCode than every preceding beta that its testers should update in place.
- The current legacy QA test feed is NOT a generic beta feed yet: `NavTestUpdateFeed` accepts historical `-test1` to `-test3`, while `NavUpdateQa` enables QA controls only for `-rc15`. Before introducing the new beta channel, extend these QA-only parsers and UI gates in a *separate tested change*. Do not expose beta update prompts in stable `1.0.0`.
- Stable devices read only stable metadata. Testers get beta APK links, and beta update discovery can be introduced with separate `channels/test.json` metadata and explicit opt-in. No beta should ever set `channels/stable.json` to a prerelease.
- Always retain one accepted signed build as recovery, export all profiles and independent manuals before riskier beta installation, and verify with HONOR + Samsung.

## Reference 1.0.0 UI freeze
- Settings retains only full-profile transfer and selective exchange.
- Reference is independent of Today, Works, Handover. It contains exactly three root cards: Inspections (SIRE 2.0 and VIQ7 inside), Equipment, My Materials.
- The `Импорт и экспорт` action is centered **after** the last Reference card and matches the existing Handover footer action geometry, border, typography. Import/export itself retains original-PDF/DOCX packaging, per-file verification, offline library and equipment catalog backups.

## 2026-11-15 — actual public 1.0.0 publication and pending shared update feed
- Signed release `https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v1.0.0` passed full CI `43608609581`, package `com.navtasks.app.glass`, versionCode `226`, versionName `1.0.0`, original signer SHA256 `0cb8a3c691e3fb2198682dff8c226aec710e77c7b8fded7d260a92f359a49c54`. Public asset `NavTasks_1.0.0.apk`, 37035842 bytes, SHA256 `ac5038175b5dd21d9de3a5ae04f398d53e2d927fe970e2e2c533548986b186d2`.
- **Updates channel NOT YET ENABLED**: `NavTasks-Updates/channels/stable.json` still has `available:false`. ChatGPT GitHub App installation `177097839` currently allows only `NavTasks` and `SmartVessel-Report`. Write attempt to `NavTasks-Updates` failed 403 `Resource not accessible by integration`. User must add `NavTasks-Updates` to that same GitHub App's selected repositories at `https://github.com/settings/installations/177097839`. Never claim automatic updates live until publish a signed APK to Updates/releases/tag/v1.0.0, verify hash, and only then set stable.json available true.
- Existing installs read the old `NavTasks-Updates` feed and pin to `NavTasks-Updates` release URLs. Do NOT silently switch URLs in the app; doing so leaves old installed builds unable to discover the first stable update and changes a proven security guard.
- Keep beta testing separate: stable releases by verified improvements, not monthly; for changes after stable 1.0.0 prefer `1.0.1-beta.3`, `1.0.1-beta.4` for beta testing, with monotonically increasing versionCode, then `1.0.1` stable above all beta codes. Current legacy test parser must be extended separately before automated beta discoverability can be claimed.
