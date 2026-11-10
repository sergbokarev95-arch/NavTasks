# NavTasks 1.0.0 — mobile release plan (2026-11-10)

## Baseline and status
- User: installed 0.4.118 on HONOR, reports preliminary stability under routine use.
- New functional candidate: signed 0.4.120-beta/code145, source `ff674baee245ba1fc85902da8d3238b220f05aba`, CI42750924441 SUCCESS and official GitHub Release. Profile-aware notification/widget cold/warm deep links only.
- Recovery: exact previous 0.4.118 application source signed as 0.4.121-beta/code146, source `662a8781144c2c092ffa4959f556744fa2634e38`, CI42751054359 SUCCESS. Install only on rejection; no uninstall/wipe.
- `main` app source remains 0.4.59, docs-only; do not accidentally base 1.0.0 on main. The installed code is on signed feature branches.
- Accepted and frozen: 0.4.108 classic Liquid Glass dock spring/refraction/retap pulse, current visual palettes, Work -> Routines/AMOS/Services, Today Calendar left/Add right, Reference SIRE 2.0/VIQ7 placeholder tiles only.
- OUT OF SCOPE before 1.0.0: inspection/PDF/reference content, Windows desktop, LAN/cloud sync and any destructive migration.

## Release blockers, in priority order
1. Real HONOR acceptance of 0.4.120: two vessel profiles, notification task/AMOS link from another active profile, cold and warm start, repeated same tap, missed widget link, unknown/deleted profile, Back/navigation state; preserve checklists/history.
2. End-to-end 0.4.118/0.4.120 regression: AMOS/Service closed notification suppression, independent widget daily refresh with notifications disabled, exact alarm grant/denial, reboot, dark/light, last-card clearance, import/export, rich notes, Handover.
3. Package identity and signature. Current installed/debug Beta: `com.navtasks.app.glass` signed with stable Beta signing config. Default Gradle release variant: `com.navtasks.app` and no same-key release signing. Publishing this default release cannot update the installed app. Proposed path, **not yet approved/implemented**: final *non-debuggable* release with the EXISTING `com.navtasks.app.glass` package and matching signer, higher versionCode, verified in-place upgrade and retained full profiles. Never uninstall or clear app data to test. Do not expose key material.
4. Privacy/Auto Backup: current Manifest `android:allowBackup="true"`; explicitly decide if Android-managed backup of ship task/maintenance/notes data should remain enabled. The app already has manual FULL profile JSON export/import. Do not switch this setting silently.
5. Data/load resilience and QA: large task/run history; years since last visit; month/leap/year transitions; multiple vessels and Personal; process death; timezone; keyboard and focus; Android physical/instrumented tests (currently absent). Samsung independent acceptance where possible.

## Recommended safe execution
- Keep each change as an isolated signed Beta with exact source SHA, dedicated passing CI, and a higher-code in-place fallback built from previous accepted app sources.
- Preserve `TaskStore`, `ProfileStore`, `HandoverStore`, `ServiceStore`, `WorkOrderStore` schemas, import/export and 0.4.108 Glass optics absent a focused bug with regression proof.
- Never label a candidate `stable 1.0.0` before on-device testing and explicit approval. Keep developer documentation in sync with the actually signed and published source.
