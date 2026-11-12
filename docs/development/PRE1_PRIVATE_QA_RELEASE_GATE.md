# NavTasks — private device QA release gate (not production)

Status: pre-install checklist, NOT approval to install or publish.
Date: 2026-11-13.

## Protected baseline
- Accepted user installation: **0.4.166-rc20 / versionCode 188** on HONOR.
- Baseline protected GitHub branch: `backup/navtasks-0.4.166-rc20-before-pre1-polish` at `3bfa259379cfb393a6e4e35703d7e15e1f088989`.
- Current RC20-derived development branch: `fix/rc20-pre1-handover-tasks-ui`.
- Public stable channel **MUST remain available:false**.
- Never uninstall, clear data or change `com.navtasks.app.glass` or the existing signing key. Do not touch main for QA.
- Previous privately compiled recovery RC21/code189 must NOT be installed over a future higher-code QA build as Android blocks downgrade. Build a higher-code recovery artifact from the accepted RC20 source before a real-device QA update, ideally QA code190 + recovery code191. Confirm both carry the historic signer and same package ID. Do not distribute either until verified.
- Export user's current working profile data from RC20 before test install. Existing profile JSON does not include new independent Reference manuals/equipment stores.

## Gate A — machine validation (mandatory)
- [ ] Latest feature commit has SUCCESS (not cancelled) GitHub Actions JVM tests + private signed release build + app id and original certificate SHA-256 check.
- [ ] No unit failures, no warnings treated as errors, no changes to TaskStore recurrence, AMOS/Services business logic, notifications, Handover data stores, dock or shared update mechanism without consent.
- [ ] Code diff and regression matrix reviewed. Strictly no task/AMOS/Services/Today links inside Reference schema/UI.
- [ ] QA artifact workflow uploads a downloadable signed APK for private device testing without stable channel publication, and checks package `com.navtasks.app.glass`, release nondebuggable, signer, versionCode >189 and expected versionName.
- [ ] Recovery artifact signed with same key, versionCode higher than QA, based on accepted RC20 source only; must be available before QA installation.

## Gate B — high-impact Reference QA (HONOR)
- [ ] Reference opens SIRE 2.0, VIQ7, My materials and Equipment with correct Day/Night HIG geometry, navigation, keyboard and safe dock insets.
- [ ] Import large original PDF, 500+ pages; offline, pinches/zoom/pages/search, portrait/landscape, no OOM or excessive battery usage. Cross-device older Android compatibility: on Android 31–34, current native PDF view works but text search is unimplemented. Do not falsely claim parity.
- [ ] Save DOCX and inspect Cyrillic headings, paragraphs, tables/illustrations: current internal Word reading is simplified, not pixel-exact. Full original can be backed up unchanged. External original-open fallback remains a possible future improvement.
- [ ] Create, edit, search and delete excerpt, including root folder null case and nested folders; no silent move after global search. Imported excerpt links are textual citations, NOT task links.
- [ ] Export .navlib with original PDF/DOCX bytes and all shared excerpts/folders; restore offline and compare SHA-256, folder hierarchy and original file opening.
- [ ] Export equipment .navref single-card and catalog; restore into other vessel profile without task recreation, duplicate overwrite or missing manual citation.
- [ ] Restore archive cancel/back while running: busy UI blocks unsafe navigation, verified staging retained until copy finishes or cleanup safely concludes. Crash remnants over 7 days cleaned, unrelated files untouched. Staging under noBackupFilesDir.
- [ ] Force close/reopen, low disk, bad ZIP, corrupt PDFs/DOCX, night/day, RU/EN, profile switching and screen rotation.

## Gate C — app-wide regression (HONOR and Samsung)
- [ ] Today, recurrence, Weekly/Monthly/One-off, missed counts, Today completed stack, swipe/selection, dates across month and year boundary.
- [ ] AMOS, Services due dates and request numbers, history, closures, calendar, overdue, saved user data.
- [ ] Notifications summary and specified task-time behavior unchanged; app startup update prompt toggle and manual check remain correct (public feed stays OFF).
- [ ] 2×2 and 4×2 widgets render as approved, show today completion and tomorrow count when done, remain strictly read-only, update across local midnight, reboot and switching profiles.
- [ ] Handover search occurrence navigation, collapsible notes, rich editor selection handles, save and unchanged HTML on reopening.
- [ ] Private install over RC20 preserves ALL existing profiles and entries; no back button/keyboard/dock overlay regressions.

## Gate D — promotion
- First private QA build is **not** 1.0.0 and is **not** a stable feed release.
- Source code passing CI does not prove on-device acceptance. Only user-confirmed HONOR (then Samsung) QA can clear device gates.
- Stable 1.0.0 needs separate user approval after all major gates and migration export/import pass.
