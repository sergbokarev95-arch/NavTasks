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

## November 10 Handover / popup UX stage
- Signed 0.4.122-beta/code147 `25449f02983e97050190745b38e9636816c41ad3` (CI42757080882 SUCCESS) turns Handover into a notebook with no completion/strike/more UI, retains old stored flags and existing profile history; delete through full-screen editor. Today Completed warm colored like task-list info; notification icon preview removed; ordinary glass controls have small press scale, dock shader untouched. https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.122-handover-ux-verified.
- Signed experimental 0.4.124-beta/code149 `e478e4d0c8e5794382f7755da8ab63f5f9e7ed30` (CI42757168394 SUCCESS) extends 0.4.122 with Android-native popup window-background blur on supported devices and safe solid fallback. This is not the dock's exact refraction and does not change native DropdownMenu; require HONOR/Samsung screenshots/behavior verification. https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.124-popup-frost-verified.
- Recovery exact v0.4.120 app 0.4.123/code148 CI42756524945 SUCCESS signed https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.123-handover-ux-recovery. Exact v0.4.122 app rollback candidate 0.4.125/code150 CI42757281433 pending at documentation write. Preserve all user data and avoid uninstalling.
- Next release readiness checks unchanged: physical 0.4.124 review and profile-aware notification routing, both themes, AMOS/Services, Handover long notes, widgets/background alarms; in-place production signing/package path, Auto Backup privacy, real device stress and explicit acceptance. Do not fill SIRE 2.0/VIQ7 or start Windows/sync until after mobile 1.0.0.

## Final verified 0.4.125 recovery status — 2026-11-10

- **SIGNED CI SUCCESS** https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42757281433 for exact v0.4.122 application-source recovery v0.4.125-beta / versionCode150, source `8c075fd9f9febd26a64f0e28292cb8b77aa9b7a1`.
- Official verified recovery release: https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.125-popup-recovery, artifact `NavTasks_Original_0.4.122_Restore_0.4.125_Beta_Update.zip` (12,670,215 bytes), same installed package and Beta key. This rollback is ONLY for rejection of experimental popup blur in v0.4.124; it retains Handover UI/press/Today/Settings improvements of v0.4.122.
- User must export FULL profiles before updating. Install IN PLACE; NEVER uninstall or clear app storage. If recovery code150 installed, any future same-package update needs **versionCode >=151**. Never confuse latest numeric recovery 0.4.125 with newest functional candidate 0.4.124/code149.
- Device HONOR/Samsung visual/interaction acceptance still pending. No v1.0.0 published and no `main` application source merge.

## 2026-11-10 — UI candidate after physical feedback
- Signed 0.4.126-beta/code151 `7ebb2ba2be24925c9d507be4f9d5acb040c559b1`, CI42763420520 SUCCESS, https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.126-ui-polish-verified. Handover editor geometry/Delete; informational warm Work/Reference banners and Handover heading; quick-tap feedback; parent Handover Add. Prior 0.4.124 saved; exact recovery v0.4.127/code152 CI42763533651 SUCCESS https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.127-ui-polish-recovery.
- **Not yet phone-approved:** HONOR visual, Handover gesture/touch/edit and long note retention, dock, alerts/profiles; Samsung. Cross-window native blur still cannot guarantee dock lens in small popups; requires separated same-root proof if pursued. Final 1.0.0 signing/package, privacy and device testing remain gating.

## 2026-11-10 — Follow-up UI candidate: whole glass press v0.4.128
- Functional signed 0.4.128-beta/code153 SHA `31e44f0b7e39759744bb09a1dc5a7e77f638dc86`, CI42768009539 SUCCESS, https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.128-pressed-glass-verified. Correct Compose modifier ordering to scale actual glass material, Handover section matching information card and distinct AMOS Work Order icon. Stores and original dock optics frozen.
- Recovery exact previous 0.4.126 app as 0.4.129/code154 SHA `ac4e6a154c84f47091326a837751e45cddab1c7d`, CI42768087904 SUCCESS, https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.129-pressed-glass-recovery. Only on rejection; no uninstall, full profile backup first.
- Both are CI-verified, **not** real-device accepted. HONOR/Samsung checks, release-package identity/signature, privacy/Auto Backup, comprehensive test and unresolved optical popups block stable 1.0.0.

## 2026-11-11 — Pre-release signed candidate 0.4.130
- Source `1ef9cc4a568157007deee265d54d5830485989ff`, signed GitHub CI42839714152 SUCCESS, release https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.130-crew-reference-notes-verified (code155). Existing AMOS/crew work UI now transparently labeled without changing recurrence/history; full page notes 17/25sp; Reference section search for SIRE2.0/VIQ7 placeholders only. Preserve approved dock and all data.
- Exact previous source 0.4.128 signed higher-code 0.4.131/code156 recovery `bdec8647813f420f9af4825f0609990c82819751`, CI42839880090 SUCCESS https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.131-crew-reference-recovery.
- Real HONOR/Samsung review and package/signature release plan, backup privacy policy, full regression and Glass popup optics still open. **Do not call 1.0.0 stable** until all gates reviewed.

## 2026-11-11 — New pre-release candidate 0.4.132: rested-top pull Search
- Signed 0.4.132-beta/code157 SHA `e1781fd712da5457595bd2909713a7beb7fb0b9c`, CI42845269986 SUCCESS, https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.132-pull-to-search-verified. Adds a passive down-pull search gesture solely to four screens with existing magnifier, with >=200ms idle, >=90dp pull, release-only and strict rejection of ongoing fling/middle-of-list overswipe/diagonal/two-finger gestures. No data/notification/widget/dock changes.
- Exact previous 0.4.130 code recovered as signed v0.4.133/code158 SHA `6ad32064f80e36b9e77b558361b02328a2a02c67`, CI42844676421 SUCCESS, https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.133-pull-search-recovery. Optional and in-place only with full-profile backup.
- Real HONOR/Samsung gesture QA still pending, as are stable 1.0.0 nondebuggable same-package/signer configuration, Android Auto Backup privacy, full regression and separate true Glass popup research. Do not call 1.0.0 stable yet.
