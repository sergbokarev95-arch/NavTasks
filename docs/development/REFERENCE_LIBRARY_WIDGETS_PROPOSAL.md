# NavTasks — offline Reference library and statistical widgets
Date: 2026-11-12
Status: UPDATED APPROVED DIRECTION for widgets and shared document library, including user-owned Equipment; implementation and device acceptance pending.
Source: user discussion after acceptance of RC20. Existing accepted user device remains 0.4.166-rc20/code188.
Implementation branch: fix/rc20-pre1-handover-tasks-ui. No data schema or current library/widget actions changed by this document.

## 1. Core product rules
- Reference is an offline, read-only-first library of reference materials, not a task module.
- Never introduce checkmarks, deadlines, recurrence, notifications, task progress, or automatic task creation in Reference.
- User can create, rename, reorder and nest folders, import manuals, and compose own excerpts with source references.
- Keep only SIRE 2.0 and VIQ7 (archival) as pre-created manuals; user-generated categories and content are never bundled by the developer. Add an Equipment category with empty user-configurable inventory, plus user-defined folders. No fake/manual equipment records.
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

## 5. Widgets — direction explicitly accepted 2026-11-12, device acceptance pending
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

Decided: document binaries/manual library are shared between profiles; equipment inventory and live work links are scoped to vessel profiles, but shareable as standalone export packages. The exact first-release PDF/DOCX feature scope is planned conservatively, with fallback viewing where in-app fidelity cannot be guaranteed.

## 7. APPROVED ADDITION — Equipment reference register (2026-11-12)
A chief officer requests a user-managed hierarchy of equipment. Example: Equipment > Safety > Gas detector > manufacturer/model/unit > maintenance instructions. The application must not ship populated equipment data; every user adds their own equipment and passages. Document library is shared across profiles; each vessel profile maintains its own installed-equipment inventory so equipment installed on Vessel A never silently appears on Vessel B. Allow user-defined groups and nested folders; no fixed technical category hierarchy imposed.

### Separation of concerns — CRITICAL
- Reference equipment is NOT a duplicate AMOS/Services/Tasks scheduler. It is a library record describing how/why work is performed and linking to the existing task/work identifiers. Reference never owns completion state, reminders, due dates, notification triggers, or overdue calculations.
- Equipment card can show its linked existing monthly, quarterly, annual work items and "Open in Works / Today" actions. A work detail in Today/Works can show a contextual "Instruction" deep link to the equipment procedure; navigation must work offline.
- Procedure/maintenance instruction is independently reusable from actual scheduled jobs: title; operation type (maintenance/check/calibration/etc), optional recommended interval (one month / three months / year / custom) as descriptive source metadata ONLY; mandatory user-provided supporting source excerpt explaining purpose or steps; source title and optional document ID, section/page, edition/date; structured steps/safety cautions/comments.
- Creating a procedure or importing somebody else's equipment must NOT automatically create active recurring jobs, alter existing deadlines, create AMOS requests or schedule notifications. "Create/link task" requires a separate explicit action, using existing business UI and schedule logic; never guess the first execution date.
- Optional inventory fields: equipment name, maker, model, serial number, location, identifier/tag, vessel remarks. Avoid requiring sensitive serial data for sharing. Show linked planned jobs, without standalone checkboxes.

### Proposed minimal hierarchy and UI
Root: SIRE 2.0 | VIQ7 | Equipment | My materials | custom folders.
Equipment: freely nested collection > individual equipment items (can be flat at first). Equipment card: info tab (identity/description), "Regulations / Procedures" list, related documents and linked task/work list. Example: Gas detector > Monthly function check (manual excerpt/page) > Quarterly verification (manual excerpt) > Annual calibration (manual excerpt).
Document reading: preserve original offline PDF/DOCX; procedure opens excerpt with citation, and "Open source" goes to saved file/page when available. If the linked document is missing, keep the excerpt and citation readable, show "Original file not included" rather than losing content.
Search must index equipment name, model, location, procedure title, body excerpt, source title and user-defined tags, plus imported document text where available; results deep-link precisely to equipment/procedure/page.

### Selective interchange — explicit user requirement
- Settings > Reference > Import / Export; also Share/Export on any folder/equipment/document. User chooses one piece of equipment (e.g. Gas detector), multiple items, one folder, or entire Reference library; recipients can import to their own profile offline.
- Portable package proposed extension: `.navref` (ZIP container) with versioned manifest JSON, records for included folders/equipment/procedures/excerpts, source metadata and optional binary files. Package includes full mandatory excerpt/procedural text to function without original manual, even if the sender deliberately excludes large PDF. When included, original files retain hash/checksum; no network calls required.
- Import workflow: open package > preview exact equipment/procedures/documents and potential conflicts > choose target vessel profile (or shared library for manuals) > add, skip duplicate, or explicitly replace. Never silently delete existing items or clone active scheduled tasks.
- Stable local UUIDs and content hashes for idempotent re-import; no cross-device coupling to sender's profile/task IDs. Imported work references appear as instructional descriptions only until recipient explicitly links/creates local tasks. Imported local source references may be re-bound when the same hashed original manual exists.
- Security: validate manifest schema/version, maximum uncompressed size/file count, paths (Zip Slip prevention), duplicate names, MIME/document parsing and total storage; transactional/rollback-safe installation so corrupt archives cannot partially overwrite profiles. Never run active content/macros/executable files from imported Office/PDF.
- Sharing: copyright/licensing of commercial manuals may limit redistribution; always allow transferring self-written excerpts with citations and let user opt in to package PDFs/DOCX only when authorized to share them.

### Tests and release guardrails
- Pure tests: folder/equipment/procedure links, mandatory explanatory excerpt + source citation, no self-creating scheduled tasks, independent profile vessel inventory and shared manuals, idempotent import, conflict and duplicate detection, corrupted/oversized archive refusal, package import without optional full PDF.
- Integration: offline mode, source-page deep links, export/import one gas detector preserving three manual-based procedures but no actual task history, ensure existing tasks and Handover remain byte-for-byte unchanged across import.
- After feature development, test on HONOR and Samsung and check privacy/large file backup design before any 1.0.0 signed install.
