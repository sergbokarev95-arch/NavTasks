# Completion check: one visible green pulse then immediate downward exit

- Source base: HONOR user-accepted `0.4.64-beta` code89, commit `8040ff34abad71b2700b79af41c46f4b90a45726`. Only `RunTileV2` visual timing/graphics and a pure token/test change, no TaskStore logic change.
- User feedback: current card blinks green twice and slides away too slowly; after tap it should become solid green first, remain clearly observable, then quickly move DOWN. Avoid simultaneous color-and-movement.
- Old: green flash animated to 1, down to .22, up to .85 (140+170+140ms), delay150ms, exit330ms with up-kick and scale swell (~930ms).
- New: solid green via `flash.snapTo(1f)`, hold `190ms` without motion, then one `250ms` downward fade, 48dp; no rebound/stretch. Both ordinary completion and final checklist tick go through same helper, 440ms total. Non-final checklist changes, confirmations on other dates, completed stack, data persistence and recurrence remain unchanged.
- User specifically asked to preserve the perfected Liquid Glass shader, all round buttons, dock, optics, border, Quick Profile gesture and insets; guard with SHA checks in CI.
- Full signed same-ID Beta update `com.navtasks.app.glass` versionCode91/versionName0.4.66-beta (higher than v0.4.65 recovery code90), built in feature branch; compact release ZIP. Never uninstall; export full profile before installing.
- Prepare same-ID same-signed Haze rollback code92 separately before asking to install. Only HONOR test can approve real animation timing.
