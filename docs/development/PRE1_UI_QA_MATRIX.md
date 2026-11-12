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

## 2026-11-12 — Stats-only launcher widgets (2×2 and 4×2)
- [ ] HONOR and Samsung provider picker shows exactly the two intended layouts, not a 3×2 third preset. Check host target-cell geometry and padding.
- [ ] Compact: Today count and blue progress bar, remaining label or Tomorrow count after all today's jobs are completed; no checkboxes anywhere.
- [ ] Wide: same Today total/percent with up to 2 pending names as plain TEXT, no tap-to-complete; after completion show Tomorrow count and explanatory text.
- [ ] Tap the widget body opens NavTasks Today; no completion writes from any widget surface or leftover legacy PendingIntent.
- [ ] At 00:00 new local day replaces Yesterday, Tomorrow count recomputed, unchanged schedules, profile switch and restart update the widget.
- [ ] Empty Today, completed Today, partial Today, backlog present/absent, RU plural forms and EN labels, night/day contrast on bright/complex wallpaper.
- [ ] Verify RemoteViews on two system launchers and 2x2/4x2 positions; Android host grid sizes may vary, use actual device acceptance.

## 2026-11-12 — Reference equipment foundation (backend only)
- [ ] Catalog per vessel profile, shared manuals later: changing vessel profile never silently shares its equipment inventory with another profile.
- [ ] Create nested equipment folders; refuse cycles, duplicate IDs and missing parents without erasing existing catalog.
- [ ] Manual excerpt and source title required for every procedure; period stored only as descriptive text; NO task IDs, AMOS, Services, Today links, reminders, or scheduling.
- [ ] Export one equipment to portable .navref: include instructions, citations and excerpt; exclude sender's local folder link and serial number by default; allow explicit serial inclusion.
- [ ] Reimport preview: detect same ID or probable duplicate, never overwrite automatically; corrupted/oversized/malformed ZIP fails safely, no Zip Slip.
- [ ] User-facing equipment editing, actual PDF/DOCX import/reading, selective settings import/export, and wide library package are NOT YET implemented. No real-device acceptance claimed.

## 2026-11-13 — Standalone Equipment Reference UI acceptance
- [ ] On HONOR & Samsung in Day/Night, open Reference > Equipment while active profile is Vessel. SIRE 2.0 and VIQ7 remain unchanged placeholder tiles, no fake manual content.
- [ ] Add folder, nested folder, rename; search finds nested folder by title. Back first hides keyboard/search then exits nested folder; on retap Reference dock returns to root.
- [ ] Add equipment title and optional maker/model/serial/location/notes. Reopen after app kill and profile switch; vessel inventories never bleed into another profile.
- [ ] Add, edit, read and delete cited procedure with manual title, page, section, edition, excerpt and optional step notes. Manual text is read-only in viewing mode, while interval remains informational only.
- [ ] Reject empty equipment names, excerpt, source title and bad page numbers. Check long (>4000-character) multi-paragraph excerpts with keyboard/scroll selection on HONOR.
- [ ] Search equipment metadata and procedure excerpts across folders; highlight results, next/previous arrow ordering, no overlay with dock, no invisible tapped row.
- [ ] Export one equipment .navref via in-card menu and Settings > Reference exchange; verify serial redacted by default and documents are NOT included, only citations/excerpts.
- [ ] Import .navref on second vessel profile: preview entry and procedures, add-copy, exact-ID explicit replacement, similar name conflict, corrupt ZIP warning. No tasks, service dates, notifications or AMOS changes.
- [ ] Do not claim complete protection via existing full-profile JSON export: new equipment catalog is separate app-private files. Before final 1.0.0 deliver complete Reference library export/restore (including all equipment, folders and manuals when supported) and test device migration.
- [ ] SIRE/VIQ PDF/DOCX import/view and general user manuals/excerpts not yet available. These must be implemented and regression-tested before stating the overall Reference feature complete.

## 2026-11-13 — Original-manual archive release gates
- [ ] Save shared manual files in SIRE/VIQ/My Materials; navigate nested folders; show cached originals offline after force close, profile switch, airplane mode and reboot.
- [ ] Export .navlib containing actual original PDF/DOCX bytes; import into another device/profile, verify SHA256 and exact page/table/format fidelity, and do not overwrite existing shared manuals.
- [ ] Corrupt ZIP, missing DOCX member, path traversal, altered checksum, ZIP bomb and partial file import must never modify the existing catalog.
- [ ] Validate AndroidX PDF reader search/zoom/bookmarks on HONOR and Samsung, fallback when library unavailable on older devices. Scanned PDFs without text need clear 'search unavailable' state; do not silently invoke network OCR.
- [ ] DOCX lightweight reading keeps paragraphs/headings/lists/tables where feasible and opens original externally for fidelity fallback; no macro execution or external requests.
- [ ] Separate vessel Equipment .navref and shared manuals .navlib have independent backup/restore UI; show filenames and accurate scope. Never imply usual Task Profile JSON includes both.

## 2026-11-13 — PDF/Word manual UI and exact limitations
- [ ] SIRE, VIQ7, My materials open respective shared PDF/DOCX folder lists; import PDF 100-250 MB through system picker, verify remains after force stop and switching profiles.
- [ ] Create/rename nested manuals folder, search its name, delete only empty folder, delete document record with confirmation. Verify no hidden full-profile export dependency.
- [ ] Internal PDF viewer on HONOR: 500+ page opening/page jumps/zoom without OOM, portrait/landscape, font margins, day/night, bottom safe inset. API35+ search returns correct pages/match count and never blocks UI. API31-34 fallback lacks text search and needs further design acceptance.
- [ ] Word DOCX reader headings/paragraphs and search, nonexecuted macros/external linked content, complex tables/images explicitly not pixel-accurate. Validate Cyrillic, tables, multi-column text.
- [ ] Export full .navlib original-byte package, verify intact PDF/DOCX from archive on another device, idempotent duplicate handling and folder remap, and fail-closed checksum/zip-slip/zip-bomb cases. Separate equipment .navref and shared manuals .navlib both need explicit backup on device switch.
- [ ] User-authored standalone excerpts outside equipment are not implemented yet and remain release scope.
