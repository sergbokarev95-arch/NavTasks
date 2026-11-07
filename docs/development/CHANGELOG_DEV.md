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
