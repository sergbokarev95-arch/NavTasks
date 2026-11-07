# NavTasks — Current State

Verified against GitHub main at 211c0ac59b11e4e1cde2abe2361ffd01155bc895 (2026-11-07 review).

- Repository: sergbokarev95-arch/NavTasks (private); default and current integration branch: main.
- Source version in new code: 0.4.47, versionCode 69, app/build.gradle.kts. Debug variant adds -beta to versionName and .glass to applicationId.
- Previous stable source: 0.4.46 at 59f6a77 (due-only AMOS/Services and widget visibility); 0.4.47 source update pending CI: actual app icon on notifications, red missed chips, widget layout and navigation.
- Last pre-documentation main commit: 211c0ac — publication workflow for 0.4.46.
- Release workflow: .github/workflows/publish-0.4.46-apk.yml; tag v0.4.46-glass-beta and APK/ZIP names defined there. Its checkout is pinned to 59f6a77, so documentation commits do not change released code.
- GitHub Actions Build NavTasks Android APK run 42254823995 completed successfully on 2026-11-06. CI runs unit tests and assembles debug APK. Physical-device validation is not established by CI.
- Main modules: Today, Plan/calendar, Tasks and recurrence, profiles, import/export, handover, Services, AMOS work orders, Works agenda, notifications and two home widgets. Implementation is Kotlin/Jetpack Compose, with RemoteViews XML widgets.
- Notification implementation: app/src/main/java/com/navtasks/app/NavNotifications.kt; icon app/src/main/res/drawable/navtasks_notification.xml.
- Widget implementation: app/src/main/java/com/navtasks/app/NavWidgets.kt and app/src/main/res/layout/navtasks_widget_{compact,wide}.xml.
- The repository README.md and CI_README.md contain legacy 0.2-era statements; use Gradle, current workflow and this document for current state.
- On 2026-11-07 user approved the original-color launcher icon in notifications, useful adaptive widgets, and restrained red Missed chips. Changes implemented in source; CI and HONOR device testing still required.

- 0.4.47 source touches notification icon/builder, widgets and Today presentation only; no profile or task data schema changes. Verify the GitHub main head and the next passing CI run before installing.
