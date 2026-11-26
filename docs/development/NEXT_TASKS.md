# NavTasks — следующие действия

## Stable 1.2.0 / Recovery347
- Owner explicitly approved stable release 1.2.0/code346 from Beta58. Source branch `release/navtasks-1.2.0-code346`, SHA 085ed444f7133d67566f76014270a4f6c60a56ab, [CI](https://github.com/sergbokarev95-arch/NavTasks/actions/runs/45754884332) in_progress. App logic identical to Beta58, version suffix removed and version guards updated. Main not merged.
- Recovery347: `qa/navtasks-1.1.0-recovery-code347-for-stable120`, SHA 78ae92177ea44d70ed2a3094db5e64744a3c29c3, [CI](https://github.com/sergbokarev95-arch/NavTasks/actions/runs/45754888571) in_progress. Existing stable 1.1.0 logic, original signer, higher versionCode.
- Public OTA still 1.1.0/code230. Only after source APK and recovery verified, publish exact SHA-pinned stable APK to NavTasks-Updates and activate stable.json via guarded workflow. Preserve test channel and previous releases. Backups: source `backup/navtasks-beta58-before-stable-1.2.0`, `backup/navtasks-main-before-stable-1.2.0`; OTA `backup/stable-1.1.0-before-1.2.0`.
- Check both exact runs; verify original signer, app ID, version, non-debuggable APK, digest, asset and archive. Do not duplicate runs.
- Public publisher must download exact signed APK and check hash/size/signature, publish non-prerelease, then atomically activate stable.json. Verify published asset and feed. Test feed unchanged.
- No automatic merge into main. Future versionCode >347 if recovery installed.
