# Stage 3A — read-only GitHub stable-feed discovery

- Isolated feature branch based on accepted RC1 0.4.142 code167. New 0.4.144-rc2 code169 is a development-only candidate; **do not install unsigned release**. Preserve installed app, signing key, app ID and all local data.
- Adds only INTERNET permission; asynchronous 3.5-second timeouts, 24-hour auto-throttle, silent offline failure. No background service, no task store access, no change to business logic or glass.
- Checks public stable.json, versionCode, explicit minSupportedVersionCode, Android minSdk, exact applicationId and pinned public APK URL, hash/signer shape; rejects beta/draft/incompatible/downgrade feeds.
- Settings gets Updates panel with installed version, last successful check, manual Check, notes and compatibility status. **NO download, installer, update dialog or APK publication yet.** Later stages must validate actual APK signer against installed certificate before install.
- The public stable feed remains available:false. Actual successful automatic availability, on-device checks and end-to-end upgrade remain untested until a real stable APK is published.
