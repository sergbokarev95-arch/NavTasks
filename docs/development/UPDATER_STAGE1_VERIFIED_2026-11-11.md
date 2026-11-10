# NavTasks GitHub in-app updater — Stage 1 verified (2026-11-11)

**Private repository**: `sergbokarev95-arch/NavTasks`; no repository privacy/access, keys, source license, data schema, Manifest or core Android components changed. The separate public binary-only channel is agreed but not created at this stage.

## Verified candidate
- Source 0.4.140 SHA `e812bdbda1e1bf72d10dbfd6c57f425322f6747d`, backup branch `backup/navtasks-0.4.140-before-release-candidate`.
- Branch `feature/navtasks-signed-release-candidate-0.4.142` HEAD `6c080c5d894b4756f645f72b87623a37ab79a74a`.
- Build `assembleRelease` **not debuggable**, name `0.4.142-rc1`, code167, id `com.navtasks.app.glass`, min SDK31, same signing key alias. CI: https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42873932892 SUCCESS.
- Private draft release https://github.com/sergbokarev95-arch/NavTasks/releases/tag/untagged-6b6bfb5b28f19db6920f; binary `NavTasks_0.4.142_RC1_Signed_Release.apk` (28435669 bytes) plus SHA-256.
- Run downloaded real signed `v0.4.118/code143` and `v0.4.140/code165` binaries from PRIVATE GitHub Releases, checked pkg and SHA256 signing certificate equivalence to new RC APK. APK signature, checksum and version checks pass.
- `NavReleaseCompatibilityTest.kt` covers synthetic old `navtasks-full-profile-v1` (missing newer AMOS/rich-note fields), Personal-only notes, and rejection of dangling historical task references. Existing JVM suite also passed.
- No runtime feature changed. This RC **DOES NOT contain an in-app updater yet**.

## Verified rollback
- `feature/navtasks-0.4.143-release-rc-recovery` HEAD `0b6c3d94fc613431c94e09a435879e65aef0ec82`, code168 / 0.4.143-beta, same package/key, exact previous 0.4.140 app source. CI: https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42874025230 SUCCESS.
- PRIVATE draft https://github.com/sergbokarev95-arch/NavTasks/releases/tag/untagged-bfab878ad7a9ded917d3; `NavTasks_0.4.143_Beta_Exact_0.4.140_Recovery.apk` (36477889 bytes) + SHA-256. Only for NEW failures caused by code167, in-place; if installed, next update code >=169.

## GitHub billing workaround
- Initial signed build CI42872674468 successfully compiled, ran tests and verified certs/checksum. Its final `actions/upload-artifact` failed because repo Actions artifact storage quota is full.
- Preserved all historical releases. Subsequent isolated builds used private GitHub **draft/prerelease Releases** instead, with no public package visibility and no stable updater feed.

## Validation gates NOT yet met
- No hardware/ADB emulator in-place installation across versions, no on-device post-install profile persistence comparison; do not guarantee old-to-new migration or 1.0.3 to 3.0.0.
- Require safe full profile export to external file, then optional device checks using backup/test profile: install RC1/code167 over code165, then verify tasks/history/checklists, Handover/notes/rich HTML, AMOS/Services completion histories, settings, notifications, widgets, month rollovers, offline reopening, day/night, dock. Repeat for code143 baseline where feasible.
- Android `allowBackup=true` remains and privacy policy requires separate choice.
- Stage2 public binary-only repo and feed, Stage3 updater service + FileProvider, settings UI and stable-only release selection are NOT yet implemented.
