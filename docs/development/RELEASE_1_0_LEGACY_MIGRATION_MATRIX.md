# NavTasks 1.0.0 — historical profile compatibility matrix

Date: 2026-11-12. **QA-only; no source-app, package, signing-key, or public-feed changes.**

## Device baseline and privacy
- The user accepted the signed `0.4.164-rc18` code186 on HONOR; all profiles, task records, AMOS, Services and Handover remained intact. Protected APK/source release `v0.4.164-rc18-integrated-private`; exact accepted source SHA `e1a5f8b0912fa882ceba6ea8901ddd610e2a2c5f`.
- User explicitly chose to KEEP Android Auto Backup as a secondary safety net; do not change `allowBackup` or cloud/D2D policy. Manual full-profile export remains the primary user-controlled backup.
- This branch adds only schema-faithful **synthetic** historical-profile JSON test fixtures, a JVM migration matrix test, documentation and read-only CI. These are **NOT** real user backup files or captured historic APK installations; no personal data/credentials included.

## Historical repository source references and fixture matrix

| Historical source | Export schema and feature set | Fixture(s) | QA expectation |
|---|---|---|---|
| 0.3.1 `4a7cd30c6a991139184553078eb0dbf00aca1ae7` | `navtasks-full-profile-v1`: profile, tasks, history, Handover, lastVisit; no services or AMOS | `v0_3_1_vessel.json`, `v0_3_1_personal.json` | Preserve IDs, task checklist and completed run, postpone date, marked/date-stamped Handover and personal notes; missing newer lists are empty |
| 0.4.0 `46e030c42a4cc0ec0a51ad88fbdafc897e080850` | v1 + Services with month periodicity; no AMOS | `v0_4_0_vessel.json` | Preserve 60-month radar service schedule and checklist journal; no invented AMOS |
| 0.4.44 `f6692f3a64ca777c0f58729557dac5c6d217e944` | v1 + Services request/history and AMOS work order execution history | `v0_4_44_vessel.json` | Preserve previous requisition in service history and closed AMOS cycles, repeat recurrence, comments and IDs |

These fixtures are hand-authored from the **fields actually emitted by each historical exporter's `toJson`**, with deterministic dates, IDs and no private real-world data. App's current `ProfileTransfer.parse` must successfully parse each and re-export/re-import it without changing its resulting `ProfileSnapshot`.

## Acceptance and safeguards
1. Verify profile kind, ID, last visit date, tasks, completed/checklist history and Handover in all older formats.
2. Verify older `services` and `workOrders` omitted from JSON map to valid empty lists, NOT fictional entries.
3. Verify archived AMOS and Services histories/numbered requisitions survive double round-trip.
4. Malformed/unknown schema, duplicate task IDs, invalid Handover titles and missing task history references must cause parse failure, not partial acceptance.
5. Direct version skipping is conditional on published `minSupportedVersionCode`, correct signing certificate/package, and real successful migration; synthetic test floors alone do not authorize arbitrary skipping of 1.0.x->3.0.0.
6. Verify old pre-profile preference migration code is copy-only by source contract; REAL on-device migration requires separate emulator/spare-device dry run.
7. Build only original signed, non-debuggable `0.4.164-rc18`/code186 in read-only CI, matching cert, `stable.json available:false`. No release/upload/feed edits.

## Still outstanding before user-approved production stable 1.0.0
- Actual captured old profile export from a willing tester (redacted/carefully scoped) and/or instrumented independent device scenario, not user's active data.
- Full old-version APK upgrade on an isolated spare/emulator where Android's real private SharedPreferences migration is tested, preserving user state.
- Samsung platform validation (installer permissions, widgets, notifications, reboot/timezone, keyboard, blur fallback).
- Final functional and Apple HIG visual review, light/dark and bottom insets. Fix only reproduced issues; preserve accepted Liquid Glass dock.
- Test-only update UI absent in production stable, while normal optional update offer and POST-install user-facing acknowledgment remain explicitly tested.
- Explicit authorization before enabling the public stable feed or releasing `1.0.0`.
