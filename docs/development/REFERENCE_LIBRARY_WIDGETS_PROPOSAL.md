# NavTasks — offline Reference library and statistical widgets
Date: 2026-11-12
Status: DESIGN PROPOSAL — not implemented; request final user UX sign-off before changes to app runtime.
Source: user discussion after acceptance of RC20. Existing accepted user device remains 0.4.166-rc20/code188.
Implementation branch: fix/rc20-pre1-handover-tasks-ui. No data schema or current library/widget actions changed by this document.

## 1. Core product rules
- Reference is an offline, read-only-first library of reference materials, not a task module.
- Never introduce checkmarks, deadlines, recurrence, notifications, task progress, or automatic task creation in Reference.
- User can create, rename, reorder and nest folders, import manuals, and compose own excerpts with source references.
- Keep SIRE 2.0 and VIQ7 as initial categories (VIQ7 archival), with user-defined additional categories and no fixed document scheme.
- Search across categories, titles, full-text documents where extractable, and user-created excerpts, with highlighted matches, document title, page/section context, and direct navigation.

## 2. Proposed object model (not a migration yet)
- ReferenceCollection (id, title, sort order, display config, optional parent id).
- ReferenceDocument (id, collectionId, display title, mime, original file path, size/hash, page count, detected outline, extraction/index status, optional source/version/updatedAt).
- ReferenceExcerpt (id, collectionId, title, plaintext, optional rich HTML, sourceDocumentId, sourcePage or DOCX heading/bookmark, createdAt/updatedAt, sort order).
- Search entry (stable source id, matching text snippet, PDF page index or DOCX position, offsets if reliable). Do not corrupt original files.
- Original binaries stored separately from settings/preferences and large profile JSON. Private per-app persistent files, NOT cache; hash dedup to avoid repeated import. Preview/index data can be rebuilt.
- Shared manuals by default across vessel profiles; personal excerpts scope is an open UX decision. Explicit .zip library archive import/export with manifest+binary files; verify profiles and Android Auto Backup behavior separately. Never assume huge PDFs fit the existing 10-MB profile JSON (ProfileTransfer MAX_BYTES).
- Security: local document picker; copy imported file for offline use; validate MIME/extensions, file size/zip expansion, parse failures and nested file references; no online conversion, scripts, macros or automatic remote downloads.

## 3. Readers and import
- PDF: preserve exact page layout, searchable text and PDF bookmarks when present. Prefer AndroidX PDF viewer only where Android S(API 31)+extension13 available; test HONOR and Samsung support. Provide compatible fallback for supported devices without required extension (renderer, or explicit external open if in-app search unavailable). Need individual page navigation, pinch zoom, search-highlight, location restore and PDF outline.
- Missing bookmarks: keep original intact; allow manual outline and folders, optional detection of heading typography later. Never pretend imported PDF is automatically structured if no bookmarks.
- Scanned PDF: index unavailable without OCR; clearly label. Offline OCR as separately assessed later. Do not require Play Services or internet on ship.
- DOCX: save original; extract plain searchable text and semantic headings locally; provide simplified internal reading mode (HTML rendered without remote content) with best-effort basic styles, lists, tables, images; offer open-original externally for complex layout. Old binary .doc is out of scope for initial import.
- Notes/excerpts: create by user paste/entry, optionally via selection from reader where supported; source/document/page link; existing rich editor can be considered once its selection problem is accepted on HONOR.
- Import UX: within a folder '+': New folder, Import PDF/DOCX, New excerpt. Result is a navigable file card with file metadata and index status. Do not store whole SIRE as thousands of hand-copied notes.

## 4. Proposed Reference UI
- Root: SIRE 2.0, VIQ7, My materials and user-created folders; search action and contextual Add.
- Folder: breadcrumbs or clear Back, title, searchable list of nested folders, document cards and excerpts, optional sorting (manual, name, recent). Keep initial setup simple, not a large settings screen.
- Document screen: native-feeling full-screen reader with page/section, search, outline, bookmarks; source remains untouched.
- Excerpt screen: large readable text and source citation; edit, move, delete with confirmation.
- Search: >=2 characters consistent with existing NavSearchMarkers; results grouped by document/source, each hit navigates to PDF page or specific excerpt.
- Keep Reference file and search indexing isolated from Tasks/AMOS/Services, shared dock, updates and profile preferences.

## 5. Widgets — user-approved direction, exact visual details pending
- Two launcher footprint presets ONLY: 2×2 (compact) and 4×2 (wide). Do not offer 3×2 as a default. Actual dp sizes vary by launchers (HONOR and Samsung), so layouts must be responsive.
- Statistical/summary only; no direct checkmark completion. Tap body opens NavTasks Today (or selected-day route when explicitly shown); no task mutation from widget.
- 2×2: Today label/date, big completed/total count, restrained blue progress track, 'remaining X' status. If all today's tasks completed, replace lower block with 'Tomorrow: N tasks'; do not silently replace today's completed headline without context.
- 4×2: same Today total and progress, plus a compact preview of up to 2 remaining today task names without checkboxes; when today 100% complete, replace preview with Tomorrow count and optional 1-2 next task names. A 'Missed N' semantic-red indicator only when N>0, and it must not obscure primary stats.
- Colors: only NavTasks semantic Apple blue and neutral Day/Night typography; consistent border/radii, legible backdrop, balanced inset; avoid noisy gradients and fake 3D. Wallpaper contrast must remain readable. No requirement to reproduce OS Apple blur, which Android widgets cannot guarantee on every launcher.
- Daily transitions: refresh when tasks completed/edited, profile switched, device restarted, local date rolled over; tomorrow count must refer to next local calendar day. Date and profile routing must match app.
- Current implementation: NavWidgets.kt still has a WIDGET_TOGGLE broadcast and RemoteViews '○' action; remove/disable these ONLY when user-approved redesign is implemented and checked. Existing xml compact/wide and appwidget provider min widths are not yet guaranteed to represent exact 2x2/4x2 grid footprints on all launchers.

## 6. Delivery sequence / gates
1. User agrees reader/import scope and widget behavior; document final decisions.
2. Implement document object/store and export/import with isolated tests before UI wiring; backup baseline and preserve old profile JSON untouched.
3. Implement PDF file picker/import/view/search first; validate long SIRE document navigation, bookmarks and offline behavior on HONOR; fallback on Samsung versions.
4. Implement DOCX basic extraction/reading mode with explicit original-file fallback. Avoid pretending pixel-exact Word rendering.
5. Implement excerpts, folder operations, cross-document search and user-friendly import error handling.
6. Redesign both widgets atomically (no on-widget checks), with dynamic rows and local-date rollover tests. Test on HONOR and Samsung, both themes, small/large launcher cells.
7. Run broad cross-module regression, protect signature and code>189 for installation, and publish stable 1.0.0 ONLY with separate user approval.

Open decisions: shared-vs-per-profile personal excerpts; exact initial PDF/DOCX feature scope for 1.0.0. Make a clear conservative recommendation rather than scope-creep.
