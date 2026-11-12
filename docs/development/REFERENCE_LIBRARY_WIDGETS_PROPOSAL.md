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

Decided: document binaries/manual library are shared between profiles; equipment inventory is kept in its chosen vessel profile and can be shared independently as a standalone export package. There are NO links to tasks, work orders, Services, AMOS or Today; all procedures are reference content only. The first-release PDF/DOCX feature scope stays conservative, with fallback viewing when full fidelity cannot be guaranteed.

## 7. APPROVED ADDITION — Equipment reference register (2026-11-12, corrected to full independence)
A chief officer requests a user-managed hierarchy of equipment. Example: Equipment > Safety > Gas detector > manufacturer/model/unit > maintenance instructions. The application must not ship populated equipment data; every user adds their own equipment and passages. Document library is shared across profiles; each vessel profile maintains its own installed-equipment inventory so equipment installed on Vessel A never silently appears on Vessel B. Allow user-defined groups and nested folders; no fixed technical category hierarchy imposed.

### Strict isolation — USER REQUIREMENT, NO CROSS-MODULE LINKS
- Reference equipment contains ONLY informational procedures and source excerpts, fully independent of all task-management modules. It has no link/reference IDs to Tasks, AMOS, Services, Works or Today; no completion state, reminders, due dates, notification triggers or overdue calculations.
- Equipment card shows its own descriptions of monthly, quarterly or annual maintenance/check procedures and their cited manuals, but NEVER lists actual scheduled tasks and NEVER provides navigation into Tasks/AMOS/Services/Today. Users open and search the Reference manually when needed.
- Procedure/maintenance instruction is entirely standalone reference content: title; operation type (maintenance/check/calibration/etc), optional recommended interval (month / quarter / year / custom) as descriptive text ONLY; supporting source excerpt explaining purpose or steps; source title and optionally document/page/edition; structured steps, safety cautions and comments. The interval is not a deadline or schedule.
- Creating, editing, viewing, importing or exporting Reference equipment MUST NOT create, link, update or modify any tasks, recurring jobs, AMOS requests, Services entries, reminders, schedules or due dates. No 'Create task', 'Link task', 'Open in Today/Works', or 'Instruction' shortcut from Tasks to Reference, now or as an implicit later feature.
- Optional inventory fields: equipment name, maker, model, serial number, location, identifier/tag, vessel remarks. Avoid requiring sensitive serial data for sharing. Show linked planned jobs, without standalone checkboxes.

### Proposed minimal hierarchy and UI
Root: SIRE 2.0 | VIQ7 | Equipment | My materials | custom folders.
Equipment: freely nested collection > individual equipment items (can be flat at first). Equipment card: identity/description, standalone 'Procedures' list, documents and excerpts ONLY. Example: Gas detector > Monthly function check (manual excerpt/page) > Quarterly verification (manual excerpt) > Annual calibration (manual excerpt). No task list, no task identifiers, no tracking of what was completed.
Document reading: preserve original offline PDF/DOCX; procedure opens excerpt with citation, and "Open source" goes to saved file/page when available. If the linked document is missing, keep the excerpt and citation readable, show "Original file not included" rather than losing content.
Search must index equipment name, model, location, procedure title, body excerpt, source title and user-defined tags, plus imported document text where available; results deep-link precisely to equipment/procedure/page.

### Selective interchange — explicit user requirement
- Settings > Reference > Import / Export; also Share/Export on any folder/equipment/document. User chooses one piece of equipment (e.g. Gas detector), multiple items, one folder, or entire Reference library; recipients can import to their own profile offline.
- Portable package proposed extension: `.navref` (ZIP container) with versioned manifest JSON, records for included folders/equipment/procedures/excerpts, source metadata and optional binary files. Package includes full mandatory excerpt/procedural text to function without original manual, even if the sender deliberately excludes large PDF. When included, original files retain hash/checksum; no network calls required.
- Import workflow: open package > preview exact equipment/procedures/documents and potential conflicts > choose target vessel profile (or shared library for manuals) > add, skip duplicate, or explicitly replace. Never silently delete existing items. The portable Reference format contains no task/run/completion history.
- Stable local UUIDs and content hashes for idempotent re-import; no cross-device coupling to sender's profiles, job IDs or work items. Portable content consists only of equipment records, informational procedures, citations, excerpts and optional source files. Local references to included manuals can be rebound by file hash.
- Security: validate manifest schema/version, maximum uncompressed size/file count, paths (Zip Slip prevention), duplicate names, MIME/document parsing and total storage; transactional/rollback-safe installation so corrupt archives cannot partially overwrite profiles. Never run active content/macros/executable files from imported Office/PDF.
- Sharing: copyright/licensing of commercial manuals may limit redistribution; always allow transferring self-written excerpts with citations and let user opt in to package PDFs/DOCX only when authorized to share them.

### Tests and release guardrails
- Pure tests: folder-to-equipment and procedure-to-source relationships, excerpt + citation validation, zero task/work IDs in Reference schema, no scheduler mutations, independent vessel inventory and shared manuals, idempotent imports, duplicate/conflict handling, safe corrupt/oversized archive rejection, import without an optional large PDF.
- Integration: offline mode, document-page navigation ONLY within Reference, export/import a gas detector with three described procedures but without any task records, and assert existing Tasks, AMOS, Services, Today, notifications and Handover remain unchanged.
- After feature development, test on HONOR and Samsung and check privacy/large file backup design before any 1.0.0 signed install.

## 8. OVERRIDING USER CLARIFICATION — 2026-11-12: ABSOLUTELY NO TASK/REFERENCE LINKS
- User explicitly rejected previous assistant suggestion to connect Reference with Tasks, Services, AMOS, Today or Works. Treat earlier linkage proposals as **CANCELLED**, not deferred work.
- The user manually enters and maintains actual due work in Services/AMOS separately. When looking for instructions, they independently open the Reference, select equipment, and search/view the procedure with source excerpt. No cross-navigation or automatic context matching.
- Suggested monthly/quarterly/annual intervals inside Reference are explanatory manual facts, not reminders, deadlines or actual task records.
- Reference interchange includes just standalone equipment details, procedures, documents/excerpts and citations. Never import tasks or reminders, nor create hidden links, even when the other user also has NavTasks.
- No schema field for linked taskId, workOrderId, serviceId, runKey, scheduleId or Today deep link. Search links only to Reference items/pages.
