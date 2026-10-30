# Android build on GitHub Actions

Open Actions > Build NavTasks Android APK > Run workflow. After the run is green, download artifact `NavTasks-Glass-0.2.1-debug-apk` from the workflow run.

Notes:
- Debug APK is signed by the GitHub runner's ephemeral key. Its signing key may differ between runs and from earlier NavTasks beta releases. Updating an existing install may fail. Export your data before uninstalling or changing app signature.
- App ID for debug: `com.navtasks.app.beta`. Release builds are NOT automatically generated.
- Workflow requires GitHub Actions enabled and an available runner. This CI config has not yet been executed.
