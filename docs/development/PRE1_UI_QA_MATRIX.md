# NavTasks — pre-1.0.0 UI acceptance matrix (RC20 baseline)

## Safety and boundaries
- Accepted HONOR installed baseline: signed 0.4.166-rc20 / versionCode188, package `com.navtasks.app.glass`.
- Original source protected at `backup/navtasks-0.4.166-rc20-before-pre1-polish` SHA `3bfa259379cfb393a6e4e35703d7e15e1f088989`.
- Working source branch: `fix/rc20-pre1-handover-tasks-ui`. JVM and no-publish APK CI #43215461681 SUCCESS.
- New feature APK MUST have versionCode >189 because higher-code emergency recovery RC21/189 is signed. Must preserve original signer, package, all user profile data. No deletion, reinstall or clear-data.
- Full profile export before any future install. No public stable publication without separate approval.

## Handover and Personal (both themes, Russian + English)
- [ ] Vessel main sections start collapsed; tap section opens smoothly; second tap closes.
- [ ] Vessel inner entries start as compact titles with chevrons. Tap title opens description without entering editor; tap again closes; open another entry; no history/data alteration.
- [ ] Tap expanded description opens existing fullscreen editor. Save explicitly persists rich HTML and plain text; Back discards draft.
- [ ] Empty description can still be edited; long descriptions scroll without overlaying dock or search bar.
- [ ] Personal notes remain directly readable and tappable, with automatic untitled naming unchanged.
- [ ] Global Handover search starts at >=2 characters, counts EACH occurrence; text matching in collapsed notes becomes visible. Arrows advance both within one long note and across entries. Active matching word aligns near top of list while header/search stay fixed.
- [ ] Keyboard Back hides IME before search; repeated Back closes search; later Back navigates as before.

## Rich editor, existing RC14 vs experimental 1.0.0
- [ ] Real HONOR long-press selects a word; grab start/end handles and drag both directions, including across line and paragraph boundaries.
- [ ] Selection remains stable when IME is shown/hidden, when scroll position moves, and when color/highlight toolbar is expanded.
- [ ] Bold, italic, underline and strikethrough preserve selection and survive Save/reopen.
- [ ] Text color palette includes black, white and brand accents; marker color is independent; reset does not alter unrelated styles.
- [ ] Test long note close to 4000 characters, multi-paragraph Russian and English, existing historic HTML. Compare stored plaintext and styles before/after. Never run on sole live record without full export.
- [ ] No flicker/shimmer or stale selection when tapping Save, reopening or switching profiles.
- Experimental library upgrade MUST NOT be merged solely because CI passes; touch interaction and format fidelity on actual device are separate acceptance gates.

## Tasks, widgets, settings
- [ ] Daily/Weekly/Monthly/One-off: active tasks first; inactive tasks grey/struck and last; toggling activation changes ordering without erasing history or recurrence.
- [ ] Updates: blue outlined manual check in Day/Night, offline check safe, auto-offers setting preserved, no public stable offer while stable.json unavailable.
- [ ] Compact and wide widgets in both themes: NavTasks blue rather than teal; task rows top aligned; text not clipped at small/large launcher sizes; labels readable against custom wallpaper.
- [ ] Widget task tap opens task editor, simple check toggles only requested task, checklist opens detail, Missed count hidden at zero, progress correct.
- [ ] Notifications and widgets refresh after checking/completing tasks, restarting device, date changes and switching profile.

## Final release gates
- [ ] Full profile export/import, Android Auto Backup restore if possible on spare device, actual legacy sample compatibility, Samsung package/UI/notifications/widgets.
- [ ] AMOS, Services, calendar and Today cross-module regression, bottom safe insets, Liquid Glass behavior unchanged.
- [ ] User approves final 1.0.0 build, GitHub stable manifest separately toggled only after signed build and checksum/identity verification.
