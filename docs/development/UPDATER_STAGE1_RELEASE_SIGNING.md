# NavTasks release signing Stage 1 — draft RC1 (2026-11-11)

## Purpose and strict scope
This branch inherits the signed 0.4.140 app code at commit `e812bdbda1e1bf72d10dbfd6c57f425322f6747d` without any changes to application runtime data stores, UI components, background scheduling, notifications/widgets or Liquid Glass.

- Exact old source protected in `backup/navtasks-0.4.140-before-release-candidate`.
- Gradle changes ONLY: versionCode 167, versionName 0.4.142; configure `release` as a non-debuggable APK with existing package `com.navtasks.app.glass`, existing `betaStable` signing config and "-rc1" suffix.
- Existing debug Beta configuration and same-keystore source retained. Never generate/rekey/swap certificate.
- Dedicated private Actions workflow restores existing signing secrets, runs unit tests with synthetic old-schema full-profile-v1 fixtures, builds release APK, validates non-debuggable/package/minSdk/version, and downloads historical signed v0.4.118/code143 and v0.4.140/code165 from private Releases to compare actual APK signing certificate SHA-256 fingerprints. If a certificate mismatches, CI must fail. Also outputs APK checksum.
- APK is uploaded as a PRIVATE Actions artifact, not GitHub Releases and NOT public distribution. No in-app updater in this stage. No new repository or permission changes.
- Unit tests are synthetic and do not establish real-user profile migration from every old version. A signed, matching package and higher versionCode are necessary but not sufficient to guarantee an in-place upgrade. Physical HONOR/Samsung install-over-old with exported FULL profiles, task/history/AMOS/Services/Handover/comments/attachments/settings and process-death checks are mandatory before 1.0.0.
- Android allowBackup=true is unchanged; privacy policy remains to be agreed separately.

## Results
Pending CI and device validation.
