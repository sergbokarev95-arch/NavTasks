# Emergency recovery if 0.4.62 Liquid Glass Surfaces Candidate is rejected

This branch compiles the **unaltered stable v0.4.59 Haze implementation** (including all task, Service, AMOS, Handover, profile and notification logic), with ONLY versionCode 86 / versionName 0.4.63-beta.

- Installed Beta app ID `com.navtasks.app.glass` and original `betaStable` signing key; can update over 0.4.62 candidate code85 without uninstalling, preserving local tasks/profile.
- Source based on previously CI-verified 0.4.61 Haze recovery `8158574bfac16ab9bc7baa648c4a7a2533cb906c`; dock + MainActivity exact blob SHA checked in workflow; no new glass code.
- ZIP contains signed original-size APK, archive checked by unzip and SHA256 comparison; published directly into Releases (Actions artifacts quota is exhausted).
- Before trying candidate 0.4.62, export a full profile backup. NEVER uninstall to roll back. This update is the restoration route.
- No production main branch changes.
