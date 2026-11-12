# NavTasks pre-1.0.0 — profile index protection (isolated change)

**Status:** candidate only; GitHub CI and physical device acceptance required. The user has confirmed full data preservation in HONOR `0.4.162-test3`/code184; this branch is NOT installed or published.

## Safety baseline
- Accepted TEST3 signed source SHA `cd63c8325ac0e89e251c056597584a587a9f9e42`, protected in `backup/navtasks-0.4.162-test3-honor-accepted`.
- Isolated branch `feature/navtasks-pre1-profile-index-recovery`: only `ProfileStore.kt`, pure `NavProfileIndexRecoveryTest.kt`, this note and no-publish QA workflow change.
- Private profile data stays under individual `navtasks-profile-<id>` SharedPreferences, untouched. We do not clear/export/recreate user task history, AMOS, Services, Handover or profile IDs.
- Previous issue: an initialized malformed profile index was caught, silently became an empty list, and default ship/personal placeholders were then eligible for persistence on `saveIndex`, hiding references to real profile data.

## New behavior
1. Parse the primary index strictly: nonempty JSON array; valid existing `NavProfile` records; unique IDs; bounded maximum 1000 entries / 512KiB. **Do not substitute defaults** once a prior index is present.
2. Maintain an **independent private SharedPreferences** index safety copy `navtasks-profiles-safety-v1`, holding only validated profile IDs/titles/kinds and active selection — no tasks or histories. Seed it on healthy first launch of existing installations.
3. Any profile index mutation persists the complete prospective validated safety copy **before** the primary index write, using synchronous `commit()`; failure aborts the mutation. Normal deletion still deletes only the explicitly user-confirmed individual profile after saving the new index.
4. On malformed/missing primary index, restore the validated safety list. On a lost `initialized` flag but still-valid primary or backup, restore without resetting IDs. If **both** primary and safety copy are damaged, fail closed without writing defaults or clearing personal data. A later UI recovery notice should explain this rare fatal corruption case instead of suggesting uninstall.
5. Current profiles always take priority over possibly stale backup. Active selection falls back to an existing verified ID. The main profile format, task stores, business logic, update subsystem and Liquid Glass dock are unchanged.

## Tests
- Pure JVM tests verify primary precedence, recovery on absent/malformed/duplicate index, no verified backup => fail closed, validation of invalid IDs/types/large JSON, safety-first atomic index write ordering and absence of unintended `clear()`.
- Full existing JVM regression suite plus signed nondebuggable same-code184 APK assembly and installed-cert comparison. The no-publish CI workflow allows only this isolated change; it must not create public releases or edit `stable.json` / `test.json`.
- CI is not equivalent to real device data acceptance. A future high-code signed RC must be installed in-place only AFTER full user profile export and live-data comparison. Future code must exceed installed code184, and current source of recovery RC17/code185 must be considered before selecting a new app code.

## Remaining release gates
- Weak-network stable feed improvement already independently CI-validated on `feature/navtasks-pre1-stable-feed-resilience`, but not merged/tested with this change.
- Explicit Android OS backup / cloud restore privacy policy; real old profile migration; production 1.0.0 TEST-only control hiding; Samsung notification/widget/installer checks; complete Apple HIG and Liquid Glass visual pass; final stable release only upon separate user acceptance.
