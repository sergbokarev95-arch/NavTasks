# NavTasks — Stage5 resumable full APK transport, 0.4.156 RC11/code178

## Status and authoritative baseline
- User accepted real HONOR in-app update signed `0.4.150-rc7-fix1`/code175 to publicly distributed `0.4.152-test1`/code176 and confirmed ALL app data remains present.
- Immutable accepted application-code SHA `639fab15d02a41c1dfecb5852f6eeb2977db4c72`, backup branch `backup/navtasks-0.4.152-test1-after-honor-e2e`. Stage5 branch `feature/navtasks-updater-stage5-resumable-0.4.156-rc11` adds only offline update transport changes.
- Release build code178, `0.4.156-rc11`, same signed `com.navtasks.app.glass`, nondebuggable, original key.

## Deliberate exact changes
- Within `NavUpdateInstaller.download` only: preserve bounded .part file in private `cacheDir/navtasks-update/` if weak internet fails or the user cancels; bind .part identity to pinned candidate versionCode/versionName/expected SHA256/size/URL. Android can still evict cache.
- Request HTTP `Range: bytes=N-` after a partial. Accept append only for exact `206 Content-Range bytes N-END/TOTAL`. If server ignores Range and sends HTTP 200, safely truncate/restart instead of appending full body. Reject invalid/HTTP errors and disallowed HTTPS hosts/redirects.
- Three bounded attempts with network timeouts and backoff; size limited to signed expected bytes (<=200MiB). Report progress from previously downloaded bytes. Never install partial or unverified APK.
- On complete download, original full SHA-256, installed package/version and **installed Android signer equality** checks are unchanged, and verified once more immediately before Android system installer. Corrupt completed content deleted, no silent install. All downloads still user-initiated; no task/profile/store changes.
- Explicit `Later` and cancel never perform background network transfers. Saved partial occupies cache storage only; full app data unaffected.
- Add pure JUnit tests for 200 restart, exact 206 append, mismatch/oversize/416 reject, candidate identity pinning, and preserved signer validation. CI checks private signed APK and baseline certificate. NO public stable or test feed modifications.

## Acceptance gates not yet complete
- Signed CI must pass before exposing private RC11. Create higher-code **exact accepted 0.4.152-test1 application-source recovery code>=179** and verify signer before asking user to install RC11.
- Real resume needs a newer explicitly approved public TEST APK `versionCode >178` with a compatible floor; existing public TEST1/code176 cannot install over RC11. Do NOT publish a new public TEST release or modify test/stable channel without separate user consent.
- On HONOR: simulate interrupted download, manual resume showing nonzero progress, data remains intact, invalid Content-Range and mismatch fail closed; test Android installer cancel and Samsung later.

## Preserve previously HONOR-accepted weak-network test feed fix1
- `0.4.152-test1` code176 app source predates HONOR-accepted `0.4.150-rc7-fix1` code175 manual feed retry. Without carrying fix1 forward, next weak-link test feed check could regress.
- Stage5 therefore also copies ONLY previously signed/validated updater-only test feed networking and RU/EN error messages from commit `b2bf22674d68ed4026ef781d03243d1414ce8f63`: `NavTestFeedNetwork.kt`, `NavUpdateChecker.kt`, `PlanAndTasksUi.kt`, their respective policy/unit tests. No task/storage/notification/Glass source is modified.
- Test-only HTTPS read is bounded (12 s connect/20 s read, 3 attempts, pinned path/redirect, 32 KiB), stable feed logic untouched. CI tests verify this behavior, in addition to full APK resume.
