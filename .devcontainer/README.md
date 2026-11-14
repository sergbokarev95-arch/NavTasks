# NavTasks QA17: one-time Codespaces environment check

This experimental configuration lives on an independent infrastructure branch.
It does not touch the app source, package ID, Gradle configuration,
profile data, key material or public stable release channel.

## Mobile-friendly workflow

1. Stop the existing Codespace "studious-fishstick" to conserve quota.
2. Open this infrastructure branch in GitHub and create a **new** Codespace
   (2-core is enough for an initial attempt; actual success is not verified).
3. The devcontainer postCreateCommand installs JDK 17, Gradle 8.13 and
   the checksum-pinned Android SDK CLI with Android API 36, extension 19 and
   Build Tools 35.0.0, then runs QA17 tests and compiles an **UNSIGNED**
   release candidate. This setup is automatic only when a Codespace is
   created or its container is rebuilt.
4. Read .codespaces-build/STATUS.md in the file explorer. Details are in
   .codespaces-build/*.log. Stop the Codespace afterward.

Reopening the old Codespace does not automatically fetch or apply this new
devcontainer configuration. The pilot requires a fresh Codespace from this
branch (or a manual branch switch followed by rebuild).

## Critical safety boundary

GitHub Actions retains the ONLY approved production/Beta signing key.
GitHub Codespaces does **not** automatically receive those Actions Secrets.
This setup performs unsigned compilation only; no verified/installable QA17
APK is generated. Never install the unsigned output over the data-bearing
NavTasks app. The original signing certificate and higher versionCode must
be verified in a separate authorized signing stage. Do not share passwords,
keystore files or authentication tokens in chats or repository content.

This is a preparatory experiment, not proof that Gradle, Google downloads,
Codespaces quota or the Android build will pass in the live environment.
