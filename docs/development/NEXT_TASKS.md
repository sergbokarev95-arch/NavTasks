# NavTasks — следующие действия

## Beta56 / Recovery341
- Beta56/code340: `feature/navtasks-1.2.0-beta56-my-materials-redesign`, SHA 0fca656f6bee9da4fb703b3d6e0981944254a60f, [CI](https://github.com/sergbokarev95-arch/NavTasks/actions/runs/45678448425) in_progress. My Materials uses neutral cards, folder pencil and delete in rename popup, shared bottom sheet for excerpts with dirty/valid save, neutral dialogs and blur, top-pull search; hub adds online library in-development notice. Empty-folder protection and storage/reader/export/import logic retained. Backup `backup/navtasks-beta55-before-my-materials-redesign`.
- Recovery341: `qa/navtasks-1.1.0-recovery-code341-for-beta56`, SHA e95652545a048381c9e08a6815aa3a0951331d78, [CI](https://github.com/sergbokarev95-arch/NavTasks/actions/runs/45678454822) in_progress. Same stable logic, bumped version and corresponding test expectations only.
- Continue polling both exact runs. No duplicate builds. APK verification pending. Stable OTA untouched.
- Verify full JVM tests, signed APK, package/version, original signer, archive and publication; inspect release asset digest. Recovery publication failures: download verified ZIP, check hash.
- Device QA: main reference status, root and nested folders, folder edit/delete (empty only), PDF/DOCX import and open, excerpt editor and reader, settled top swipe and magnifier, search result navigation, light/dark. No stable OTA changes.
