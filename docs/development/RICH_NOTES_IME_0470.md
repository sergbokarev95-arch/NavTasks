# NavTasks 0.4.70 — spelling/IME and inline rich notes

## User request
On HONOR keyboard, first letter of prose and letters after ". " were not capitalized and autocorrect did not work even with system preference ON. User requested a compact WhatsApp-like B/I/U/S/color/marker toolbar (provided screenshot).

## Implementation and compatibility
- Base signed `v0.4.68-beta` code93 commit `16e26c0a659c9848618cf5adc849c43bffa701a9`, preserved `backup/navtasks-0.4.68-before-rich-text`. Feature source `feature/rich-notes-ime-0.4.70-beta` tested commit `a20ac5476084c72c51e37d10a9cb2b4f782dc07d`, versionCode95 same Beta ID `com.navtasks.app.glass`.
- Narratives use `KeyboardOptions(capitalization=Sentences, autoCorrectEnabled=true)`. Search/title uses this hint; numerics and AMOS IDs remain numeric/exact and exempt. Android keyboard chooses actual correction/suggestions depending on its dictionary, language and user preferences.
- `NavRichNoteUi.kt` provides compact B/I/U/strikethrough/text-color/highlight with real WYSIWYG rich-editor 1.0.0-rc14, active styles, editable full text, readable rich preview, 6000/4000 char plain caps and 24000 HTML cap.
- Target fields: Services comment, AMOS work-order comment, Handover entry notes, reusable checklist notes in Tasks/AMOS and their Today read-only notes.
- Data models: optional `commentHtml`/`notesHtml` fields added to `ServiceEntry`, `ServiceCompletion`, `WorkOrderEntry`, `CheckItem`, `HandoverEntry`, each preserving pre-existing plain comment/notes. Original JSON is preserved for export, selective import, historical service snapshots; old files load with empty HTML. No TaskStore/recurrence/AMOS service calculation change.
- New `RichNotesMigrationTest` checks legacy JSON, rich snapshot history, export JSON, escaping and validation. Unit tests use `testImplementation("org.json:json:20250517")` so Android local JVM test has a real parser. Full suite CI SUCCESS.
- Accepted Glass dock optics, selection lens, buttons, quick profile pinch, stable card tokens and TaskStore hashes guarded by CI; untouched.
- Known device acceptance needed: HONOR RU/EN keyboard behavior; bold/italic/underline/strike, text color/reset, marker while selecting fragments, rich styles survive save/reopen/export/import, dark/day color contrast, keyboard does not cover dialog save/actions, large notes scroll. Build success cannot substitute for hardware touch/IME testing.

## Verified distribution
- Workflow https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42455684933 SUCCESS; tested commit `a20ac5476084c72c51e37d10a9cb2b4f782dc07d`; `:app:testDebugUnitTest` and `:app:assembleDebug` passed. aapt validates `com.navtasks.app.glass` code95 version `0.4.70-beta`; apksigner and ZIP CRC/byte-level integrity passed.
- Same-ID signed ZIP: https://github.com/sergbokarev95-arch/NavTasks/releases/download/v0.4.70-rich-notes-ime-candidate/NavTasks_RichNotes_IME_0.4.70_Beta_Update.zip, **12,617,451 bytes**, SHA256 `0f9de50ce81ab479e171af4234ee156e503b09841f7aa830daa503e870b6428a`.
- Existing Release tag was created on successful prior compile/test commit `889c32f965327f96c1f61f55d30d882bf9af568d`; final latest signed binary asset was **replaced** by workflow 42455684933 from source `a20ac...` using `gh release upload --clobber`. Document this distinction for reproducibility.
- Fully signed rollback from exactly accepted v0.4.68 code with bumped code96: https://github.com/sergbokarev95-arch/NavTasks/releases/download/v0.4.71-glass-recovery/NavTasks_RichNotes_Restore_0.4.71_Beta_Update.zip, **10,897,385 bytes**, SHA256 `f57aa1ea1e524d3a8f4916cffe3db142aa9049db3d04a087cf852e3042b0cb82`; workflow 42455220247 SUCCESS.
- Download/install via ZIP APK *over* existing NavTasks, NEVER uninstall; full profile export first. Rollback optional only if rejected. Older build can't display new HTML but can retain unknown JSON until editing styled entry; editing saved formatted notes while rollback installed may destroy extra HTML field. Future versionCode >=97 if code96 rollback installed.
- No production app code merged to main before user validates on HONOR.
