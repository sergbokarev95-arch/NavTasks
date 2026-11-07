# NavTasks — Current State

Reviewed 2026-11-07 against GitHub main and successful 0.4.48 release; subsequent documentation-only commits may advance main HEAD.

- Repository: sergbokarev95-arch/NavTasks (private); default and current integration branch: main.
- Current source under validation: 0.4.49, versionCode 71, app/build.gradle.kts. Last published and CI-verified release remains 0.4.48 until the 0.4.49 publisher succeeds. Debug variant adds -beta and .glass.
- Previous stable source: 0.4.46 at 59f6a77 (due-only AMOS/Services and widget visibility); 0.4.47 source update pending CI: actual app icon on notifications, red missed chips, widget layout and navigation.
- Last pre-documentation main commit: 211c0ac — publication workflow for 0.4.46.
- Current release workflow: .github/workflows/publish-0.4.48-apk.yml; release v0.4.48-glass-beta (APK and ZIP), pinned to 2968dcce377f531b4bc3a57166cb8e0b62d1e1de. Previous v0.4.47 release marked superseded.
- GitHub Actions Build NavTasks Android APK run 42254823995 completed successfully on 2026-11-06. CI runs unit tests and assembles debug APK. Physical-device validation is not established by CI.
- Main modules: Today, Plan/calendar, Tasks and recurrence, profiles, import/export, handover, Services, AMOS work orders, Works agenda, notifications and two home widgets. Implementation is Kotlin/Jetpack Compose, with RemoteViews XML widgets.
- Notification implementation: app/src/main/java/com/navtasks/app/NavNotifications.kt; icon app/src/main/res/drawable/navtasks_notification.xml.
- Widget implementation: app/src/main/java/com/navtasks/app/NavWidgets.kt and app/src/main/res/layout/navtasks_widget_{compact,wide}.xml.
- The repository README.md and CI_README.md contain legacy 0.2-era statements; use Gradle, current workflow and this document for current state.
- On 2026-11-07 user approved original-color notification icon, adaptive widgets and restrained red Missed chips. 0.4.48 CI and release build passed; HONOR/Samsung on-device visual testing remains outstanding.

- 0.4.47 source touches notification icon/builder, widgets and Today presentation only; no profile or task data schema changes. Verify the GitHub main head and the next passing CI run before installing.

- 0.4.48 correction: wide widget no longer contains unsupported android.widget.Space; uses supported LinearLayout padding. The 0.4.47 release is marked Superseded/do not install; 0.4.48 is published and preferred.

## Verified 0.4.48 delivery
- Source commit: 2968dcce377f531b4bc3a57166cb8e0b62d1e1de.
- Publish-workflow commit: 0b77bce58613dbf828b17b8e6dc5ae1a081ecb34.
- Main CI: https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42332014351 — success (RemoteViews guard, unit tests, APK).
- Publisher CI: https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42332255873 — success (unit tests, stable-signed APK, ZIP, releases).
- Release: https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.48-glass-beta
- Asset SHA-256 (APK): 55f1b97fb7c31fefc990ef3904efb4bf617aaf06eecc0538275e914728b52c04.
- Device testing has NOT been completed.

## 0.4.49 Today Missed chip (source stage)
- User requested on 2026-11-07: the Today Missed tile must occupy **zero width at count=0** and appear as red only at count>0; weekday strip expands left automatically.
- Implementation: conditional AnimatedVisibility with 180 ms horizontal expansion/shrinkage; includes both 105 dp tile and 7 dp gap so there is no residual spacing. An empty backlog closes the Missed view automatically.
- Scope: NavTasksUi.kt, existing Today grouping unit-test file, version bump. No change to home widgets, notification logic, storage, profiles, AMOS, or schedules.
- Pending: source CI, signed release, and HONOR device verification.
