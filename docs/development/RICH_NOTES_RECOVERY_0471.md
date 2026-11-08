# Safe visual rollback after Rich Notes v0.4.70

- Derived from exact accepted 0.4.68 source `16e26c0a659c9848618cf5adc849c43bffa701a9` (Services Requests, Liquid Glass, quick profiles, 1-green check).
- Only versionCode=96/versionName=0.4.71-beta and CI added.
- Same ID/key `com.navtasks.app.glass`, installs in place over code95; never uninstall.
- Existing plain notes are preserved. Rich HTML metadata added by 0.4.70 still exists in the JSON file and later new apps can read it; this old version ignores metadata, so while rollback is installed avoid editing formatted notes, which can replace rich fields.
- Export full profiles before any upgrade/rollback.
