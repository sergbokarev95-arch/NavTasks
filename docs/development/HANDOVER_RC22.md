# NavTasks RC22 — nested Handover note cards and search navigation

- Baseline: HONOR-installed RC20/code188, app-source 3bfa259379cfb393a6e4e35703d7e15e1f088989.
- New isolated RC22/code190 (0.4.168-rc22); unchanged com.navtasks.app.glass signing identity.
- Vessel Handover alone: each entry title becomes a compact accordion row with a chevron. Tap row to expand/collapse; tap revealed note content to launch the unchanged full rich-note editor. For empty notes, expanded row displays an editable placeholder. Personal notes retain the previous always-visible, direct-to-editor behavior.
- During Handover search, only an actively focused note-body hit auto-expands its entry. Match navigation moves the selected match near the top of the LazyColumn with a 42dp inset, even if it was already visible; header stays fixed and other screens retain their previous visibility-only scrolling.
- Pure JVM tests cover entry toggling and visible/off-screen search deltas. No model/schema, saved rich HTML, search occurrence count, recurrence, AMOS/Services, updater, backup policy, widgets, Glass dock or public release-channel changes.
- Before any phone install, sign and verify original certificate, run all JVM tests, ensure public stable.json remains available:false, and prepare a strictly higher versionCode191 exact-RC20-source recovery signed in PRIVATE only.
- Device QA: full profile export; install in place; expand/collapse Navigation entries; click body to edit and save rich note; switch profiles including Personal; search six 'работа' matches and check exact active hit near viewport top with keyboard shown, fixed bar, both day/night, last entry and bottom dock; confirm data remain.
- Status: implementation proposed in separate feature branch; user physical acceptance pending.
