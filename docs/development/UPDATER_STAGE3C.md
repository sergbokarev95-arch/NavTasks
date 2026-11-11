# NavTasks Updater — Stage3C 0.4.148 RC5/code172

## Scope
- Parent is signed Stage3B commit `6afe4409a19cfc7fd1fe41f2c0d9efe273fba4e5` (0.4.146 RC3/code170); backup branch `backup/navtasks-0.4.146-before-update-preferences`.
- Move the already existing Updates Settings panel to the bottom, after both profile exchange panels and immediately before the app-version footer.
- Saved device-wide `auto_update_offers` defaults ON, independent of task reminders. OFF stops automatic startup HTTP checks AND suppresses new automatic proposal dialogs. Manual Settings check and explicit install remain available with OFF.
- Existing 24-hour throttle on successful checks, silent offline, "Later" dismissal per version, full signed APK validation, FileProvider, package/signer checks stay unchanged.
- No profile, TaskStore, Handover, AMOS, Service, widgets, alarms, Liquid Glass dock, installed ID `com.navtasks.app.glass` or signing key modifications. Public stable feed remains `available:false`.

## Validation
- Pure JVM truth table tests for OFF/ON and manual override plus source guards for stored preference and panel order.
- CI must verify all tests, nondebuggable APK with code172, historical signer parity and private draft release only. Do not claim physical acceptance from CI.
- HONOR user test after full profile export: in-place install; OFF persists across restarts; no startup offer/traffic; manual check works offline/online; previous history, widgets, AMOS, Services, Handover and glass unchanged. Samsung optional.
- Full update download/install E2E requires an approved controlled test feed; direct old-version migrations not yet validated.

## Recovery
- Before asking user to install code172, create exact code170 RC3 application-source recovery with code173+ and the same installed key/package. Existing code171 emergency rollback is too low after code172. Never uninstall or clear storage.
