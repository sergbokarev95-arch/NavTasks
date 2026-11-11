# NavTasks 1.0.0 — pre-release acceptance audit

Date: 2026-11-12. **This is not a release authorization.**

## Frozen source and device baseline
- User currently runs signed `0.4.158-test2`/code180 on HONOR. User confirms in-app update and interrupted-download resume, and explicitly reports **all tasks, profiles and records remain present**.
- Source is protected by exact commit `77387fb9f819c57e5291aaeed0c3972f2d67e88f` (the tested TEST2 application code). No app classes, data models, notification schedules, glass UI, business stores or installed package/certificate are changed by this QA branch.
- Public `NavTasks-Updates/channels/test.json` continues to expose TEST2/code180; `stable.json` MUST remain `{"schemaVersion":1,"channel":"stable","available":false}`.
- Never uninstall or clear NavTasks user data. FULL export of all profiles before any subsequent APK. Future in-place app must use `com.navtasks.app.glass`, same signer and versionCode >180.

## Automated checks on isolated branch
- Entire existing `:app:testDebugUnitTest` (tasks, schedule rollover, service intervals incl. leap dates, AMOS, rich text, Handover, notifications, widgets, profile import/export, old profile schema, offline, updater integrity and 206 resume policies).
- New integrated `NavReleasePreflightGateTest`: complete vessel profile JSON round-trip twice (weekly task + checkbox history + styled notes + dated Handover + service requisition history + AMOS); old-file optional fields; rejected dangling history; direct skipped-upgrade floor/downgrade matrix; invalid HTTP 206, 416 and HTTP 200 safe restart; package/signing/installer source guards.
- Rebuild nondebuggable release APK using original CI keystore; verify installed package ID, code180 and versionName, same historical signer digest, and no production/test feed writes.
- CI output is test evidence, **not** a finished 1.0.0 APK or a device migration proof.

## Blocking open findings for final release
1. **P1 — Stable update feed weak-link behavior.** `NavUpdateChecker.check()` still has a single 3500ms connect/read attempt. Test feed retry was fixed and accepted on HONOR, but stable check retained 3.5s. For ships/offline users, design a _separate, guarded_ stable-feed retry (with manual error classification, pinned host/path, and original successful-check 24h throttle), without changing auto-off switch or task reminders. Do not copy test-channel offer UI into stable blindly.
2. **P1 — Corrupt profile index hardening.** `ProfileStore` currently does `runCatching { JSONArray(index[profiles])... }.getOrDefault(emptyList())`, then substitutes default in-memory profiles. A later `saveIndex()` could overwrite the list of accessible profile IDs after corrupt JSON, even though individual `navtasks-profile-<id>` preferences survive. Before release, implement safe read-only fault state or recoverable snapshot with a targeted test. MUST NOT reset/clear existing data or silently drop profiles.
3. **P1/P2 — Android backup privacy choice.** Manifest `android:allowBackup="true"` and no explicit backup/data-extraction rules. Need an intentional decision whether local ship data and task notes may flow to OS cloud/device-transfer backup, and migration-safe rules. Avoid flipping setting without user acceptance; manual full-profile export should remain usable.
4. **P2 — Test-only update controls visible in Settings.** User's test channel should not be a normal end-user workflow for stable 1.0.0. Plan to hide this card/route in production release build only, preserve the signed updater engine and emergency test workflow in prerelease development.
5. **P1 — Migrating across skipped versions is only synthetically tested.** A real old user-export snapshot from prior release(s) (e.g. old 0.4.x or future 1.0.3->3.0.0) needs isolated import, task/history/AMOS/service/Handover equality and in-place install on a nonproduction device/profile before lowering stable `minSupportedVersionCode`. Never promise arbitrary skipped versions before this matrix.
6. **P2 — Cross-device and life-cycle QA.** Samsung: permission flow/unknown-source install, widgets, notification icon, reboot/time-zone, exact alarms, Android keyboard and glass fallback. HONOR: airplane-mode startup, repeated cancel/retry and cache eviction, installer cancellation, search, calendar, services/AMOS, import/export, dock. Tested happy path does not cover these.
7. **P1 — Production release mechanics.** Never build stable from old `main` Android files (historically 0.4.59); selectively curate from tested signed feature source. Stable `versionName=1.0.0` and monotonic `versionCode >180`, same package/key, correct asset name and pinned URL (stable parser expects `releases/download/v1.0.0/NavTasks_1.0.0.apk`), signed APK SHA256/size/cert and release notes, publish release assets FIRST, activate public stable.json LAST after all acceptance gates and explicit approval.

## Device validation checklist
- Full export all profiles; independent file backup; restore on a test profile if possible.
- Compare all profile IDs and records: daily/weekly/monthly/one-off, checklist history, completed/missed/transfer, Personal notes, Handover rich HTML and completion date, AMOS and Services history/requisitions, search and calendar.
- Test offline and weak-network Settings update checks with auto offers ON/OFF; verify Later is per-version only. Repeated cancel/resume and installer Cancel; preserve user data and original signed package.
- Exercise notifications, widgets, boot/time-zone and light/dark dock on HONOR and ideally Samsung.
- Final visual pass: Apple HIG typography, panel rhythm, click affordances, color/contrast, safe bottom insets, smooth/consistent dock behavior. Fix only agreed deficiencies, rerun all regression tests, new signed RC, device acceptance, then separately approve 1.0.0 public stable.

## Non-goals
- No rewrite of working modules, no automatic test-channel distribution, no public production release, no speculative delta patches, and no changes to the approved Liquid Glass dock until a specific defect is reproduced.
