# NavTasks — Development Change Log

This log records confirmed GitHub commits, not a reconstructed full history.

- 2026-11-06 — 0.4.43 — 86a144e43a1aaa48e31abd2315edd4bf68d124a0: clarify monthly execution dates and preview upcoming due dates.
- 2026-11-06 — 0.4.44 — f6692f3a64ca777c0f58729557dac5c6d217e944: selective sharing, full-profile clarity, unified search and glass add actions.
- 2026-11-06 — 0.4.44 — fbd553e461807c40a1606af6b7226d09c96b1659: selective AMOS import identity regression fix.
- 2026-11-06 — 0.4.45 — 0e4ec8ef38e4bf24fbe8c3cba0c69393cfe4384a: notification legibility and adaptive compact widgets.
- 2026-11-06 — 0.4.46 — 59f6a77b2e8a3cccb2b70b96b7d59f123d37ebeb: AMOS/Services due-date-only Today and widget visibility.
- 2026-11-06 — 0.4.46 publication — 211c0ac59b11e4e1cde2abe2361ffd01155bc895: pinned 0.4.46 signed release workflow and ZIP.
- 2026-11-07 — documentation bootstrap: this docs/development directory; no application code changed. See Git history for resulting documentation commit(s).

Do not infer version numbers from old README.md or past chats. Check app/build.gradle.kts, branch HEAD, releases and CI first.

- 2026-11-07 — 0.4.47 source stage: original colored installed launcher icon in notification LargeIcon; stacked-card monochrome small icon; widget date/size-responsive rows and red backlog pill; Today missed red chip; widget deep-link to missed list; capacity/visibility/date regression tests. CI/release evidence pending.

- 2026-11-07 — 0.4.48 safety correction: remove unsupported android.widget.Space from wide widget in favor of supported LinearLayout padding; add GitHub CI guard for RemoteViews, versionCode 70. Supersedes 0.4.47 before user installation. CI/release pending.

- 2026-11-07 — 0.4.48 widget initialization follow-up: initial XML hides Missed pill and uses neutral Today placeholder; live RemoteViews update alone supplies real localized date and overdue count. No data changes.

- 2026-11-07 — 0.4.48 confirmed: source 2968dcce377f531b4bc3a57166cb8e0b62d1e1de; publication workflow 0b77bce58613dbf828b17b8e6dc5ae1a081ecb34. Main CI 42332014351 SUCCESS; release CI 42332255873 SUCCESS. Published https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.48-glass-beta. APK SHA-256 55f1b97fb7c31fefc990ef3904efb4bf617aaf06eecc0538275e914728b52c04. 0.4.47 release title updated to Superseded, do not install. No hardware validation yet.

- 2026-11-07 — 0.4.49 source stage (CI/release pending): fully hide Today Missed chip and spacing at zero, let 7-day selector expand; animate entry/exit, reset empty Missed mode, and add regressions. Code initially based on main 76246c05ced3b1bdbebb96e0375305b283e1db5c. Final commit and checks to record after validation.

- 2026-11-07 — 0.4.49 released: source commit 41ae92386e1459099a6c1c3c069a987c5de04203, release-workflow commit 5dedee07a6f14d95f4730e52ee5fef62da6f8096; main CI 42335111965 SUCCESS, signed publish CI 42335349500 SUCCESS. Release: https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.49-glass-beta (versionCode 71). APK SHA-256 08123ef5d64a6b7d6f1febe3725a8fe7902b47ea0ff50fc85f4b90585b5103a5. Physical device validation pending.

- 2026-11-07 — 0.4.50 source stage (CI pending): add explicit Planned/Unplanned maintenance in ServiceEntry and editor; nullable nextDate / optional history scheduledDate; AMOS request required on new unscheduled entry; no phantom due/recurrence; protect all date-based agenda/notifications/filters; preserve planned backward compatibility and request history. Tests added for unplanned completion and template exchange. VersionCode 72. See forthcoming CI/publisher commits for proof.

- 2026-11-07 — v0.4.50 PUBLISHED: source 0a3467be369d889774b653d111917d7a6606a520, publisher workflow c12e0000d04aa427fa36cb600cdd29fb10216a74; main CI 42350648226 SUCCESS, signed publisher CI 42350831779 SUCCESS. Release https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.50-glass-beta, APK SHA-256 63e814be42a5a9f3af44f3551bf5024ea16ed6c00aab3ccd127023cf00e6680d. Await HONOR device verification.

- 2026-11-07 — 0.4.51 source stage: Unplanned AMOS request date becomes editable in ServiceEditorDialog via existing NavDateWheelDialog; default today for new, saved date retained when editing. No new fields or schedule changes; date-information regression added. CI/release not yet verified.
