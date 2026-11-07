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

- 2026-11-07 — 0.4.51 VERIFIED RELEASE: source 651948cb0c280cd63a418d0054515f4d83102ea9; publisher commit 48c55cd46d37bb66df7d10fc57cab801e488f694; main CI 42354150215 SUCCESS, signed publisher CI 42354348234 SUCCESS. Release https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.51-glass-beta (versionCode 73), APK SHA-256 ea58622fad66204e812dfdaea6ed7e8546ed98e672c6764481dd91f35ae8e4c3; user HONOR acceptance outstanding.

- 2026-11-07 — 0.4.52 source stage (CI pending): removed redundant navigation-bar safe-area padding from standalone Services and AMOS scrolling Columns, retaining it on floating Add buttons and retaining 106dp list trailing content padding; edge-to-edge visual parity with Today. No business logic or data schema changes. Bumped to versionCode 74. CI/release/device checks pending.

- 2026-11-07 — 0.4.52 SIGNED RELEASE: remove root navigationBarsPadding() from ServicesUi.kt and WorkOrdersUi.kt while retaining floating Add safe inset and bottom 106dp content padding. No models, business logic or other screens changed. Source d38aa26ddeb8ac3fe5f568dddb1c551463fc5a4d; publisher workflow f448c0f5225c6415d3cf414b1edcf2dff2a3bd8f. Main CI 42356909189 SUCCESS, publisher CI 42357102513 SUCCESS; release https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.52-glass-beta; APK SHA-256 e05d0691f0d0320c288e6b466507adc160cad2c13fd83422656daf9726eea3fb. HONOR visual confirmation pending.

- 2026-11-07 — 0.4.53 source stage, CI pending: monthly calendar tiles switched from blue-outlined Columns to subtly tinted Boxes, centered 16sp numerals, bottom-aligned status dots and coral weekend labels/numerals. Pure regression tests keep weekend and dot semantics intact. VersionCode 75; no business logic/schema changes.

- 2026-11-07 — 0.4.53 SIGNED RELEASE: subtly filled month calendar with no bright blue outlines, weekend coral labels/numerals, fixed-center 16sp day numbers, bottom-anchored existing status dots. No persistent data or scheduling changes. Source 81cce3e29b74454f6329a124f9589570a9cb2722; publish workflow e556ab3b4993d7a877e8885bf6d5045bae136505; CI 42365740578 SUCCESS, publisher CI 42365940441 SUCCESS. Release https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.53-glass-beta; APK SHA-256 c7dd50a87bd36e9efaf7ddfcdc4ef55c40dff7be645314e536f7b373e16d7b73. Physical HONOR verification pending.

- 2026-11-07 — 0.4.54 source stage (CI pending): Today DayChip supports weekend coral caption/date using Plan's existing weekend detector and shared theme color; Today date font 15 -> 16sp. The selected-day glass and date selection remain unchanged. CalendarVisualRulesTest covers both palette variants. No data/model changes; versionCode 76. Commit and checks to record after CI.

- 2026-11-07 — 0.4.54 SIGNED RELEASE: add muted Sat/Sun top and numeral color to Today 7-day chips, shared with Plan; increase Today date number to 16sp. Blue selected backing preserved; no persisted data or business logic changes. Immutable source 7e04c01a67b5979ae2b02dbf9ad1be6ccf21d16f; publication workflow 8ff1fc48a05c00a5635e3da8ca9ce330467f8cb0. Main CI 42366929632 SUCCESS, publisher CI 42367156700 SUCCESS. Release https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.54-glass-beta; APK SHA-256 f3b2ab0a8c74836469875ba8e0c25dd711ea3cf58f67ccee4c1f5a2325cb63ac. Device appearance not yet checked.

- 2026-11-07 — 0.4.55 source stage (CI pending): reposition only Add (+) floating wrappers from BottomStart/start 19dp to BottomEnd/end 19dp in Tasks, Services and AMOS; preserve callbacks, bottom offsets and Android gesture insets. No data/business logic changes. VersionCode 77.

- 2026-11-07 — 0.4.55 SIGNED RELEASE: moved three floating Add button wrappers to BottomEnd/end19dp, retained exact vertical offsets and callbacks. Source 401564269c574271b1e3c22c356989fe2d94eb58; publisher workflow aa4ed6b8f4140e7f78364819a8a8f1998e0ab279; source CI 42369659340 SUCCESS, signed release CI 42369873080 SUCCESS. Release https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.55-glass-beta, APK SHA-256 156b24d04dcbb496e8480ce463423e430651d40fe412e42b2f9202ace07229be. HONOR UI/tap verification pending.
