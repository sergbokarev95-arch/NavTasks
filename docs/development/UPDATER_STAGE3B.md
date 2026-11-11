# NavTasks Updater — Stage 3B, 0.4.146 RC3

## Purpose and exact scope

This is a candidate built upon CI-passing Stage3A `0.4.144-rc2` (code169) and the HONOR-installed `0.4.142-rc1` (code167). Install full signed APKs from public stable GitHub Releases only when the installed app satisfies the explicitly tested `minSupportedVersionCode`. The main NavTasks code and stores are untouched.

- Stable feed check stays async, silent offline, and throttled after **successful** checks only. A failed offline check no longer delays discovery when connectivity returns.
- On a matching stable update, show an optional RU/EN Day/Night dialog with installed/new version, notes and Update/Later. "Later" suppresses only that version; Settings can manually reopen the dialog.
- User-initiated transfer follows only HTTPS GitHub release asset URL pinned by feed and redirects only to documented GitHub asset hosts. Streams a full APK to `cacheDir/navtasks-update` with byte limit 200MiB, total expected bytes and SHA256, bounded connect/read timeouts and cancellation cleanup. No automatic downloads.
- Android's PackageManager must parse downloaded APK, confirm `com.navtasks.app.glass`, expected release versionCode/versionName, compatible Android minSdk and no debuggable bit. Requires precisely one signing certificate matching the **currently installed APK** and the metadata certificate SHA256. SHA-256 rechecked immediately before any install request. Any missing/changed cert or archive metadata FAILS CLOSED.
- FileProvider only exposes the `navtasks-update/` private cache subdirectory. URI is `content://`, read-only temporary grant. Android standard installer decides installation; not an in-app silent package installation. If unknown-source permission is missing, Android settings opens explicitly, then user must retry Install.
- No profile JSON, TaskStore, HandoverStore, WorkOrderStore, ServiceStore, Android Auto Backup, widget/notification, navigation dock, scene/canvas optics, or rich notes changes.
- App identifier and signing key unchanged. Debug and release are separate build variants but both use `com.navtasks.app.glass` when the existing key is present.

## Testing and outstanding limitations

- Added `NavUpdateInstallerPolicyTest` and updated `NavUpdateFeedPolicyTest`; Stage3B Gradle tests, signed nondebuggable APK check and comparison to historical signers must pass before distribution. CI is NOT physical install testing.
- The public `NavTasks-Updates/channels/stable.json` still has `available:false` and no APK; therefore **the new automatic dialog and real download/install flow cannot be exercised against public stable yet**. End-to-end test requires an explicitly approved, compatible stable release or separate controlled beta test channel.
- Need HONOR/Samsung tests for no update, offline, reconnect, bad network, interrupted/damaged download, installer consent and cancel, install after skipping versions, full old data profiles migration. Never claim "any version -> any version" compatibility until snapshots and device upgrades prove it.
- Keep source and draft RC APK in PRIVATE repository. Never place release-publishing token, keystore or private data in NavTasks-Updates. No public RC promotion.

## Rollback

Previous HONOR-tested RC1 `0.4.142-rc1` code167 cannot be installed directly over code170 because Android blocks downgrade. Create a **new higher-code** rollback APK using exact RC1 application source and the same signing key before asking user to install Stage3B. Export profiles first and never uninstall.
