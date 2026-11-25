# NavTasks — следующие действия

## Beta53
- Recovery335 verified [ZIP](https://github.com/sergbokarev95-arch/NavTasks/actions/runs/45656346039/artifacts/15043651676), publication HTTP403; no duplicate run. Beta53 JVM success, signed build pending.
- Verify [Beta53 CI 45656282445](https://github.com/sergbokarev95-arch/NavTasks/actions/runs/45656282445) and [Recovery335 CI 45656346039](https://github.com/sergbokarev95-arch/NavTasks/actions/runs/45656346039), JVM, signed APK, package/version, archive and actual release. No duplicate runs.
- Device QA: both themes, root/folder search, rename and delete folder (empty and nonempty), preserve child IDs and attachments, compact detail actions, bottom editors with keyboard, save state, reader, PDF/DOCX attachment and export/import.
- Beta52 and Recovery333 verified archives; release HTTP403. Recovery335 supersedes Recovery333.

## Защитные ограничения
- Export profile before installation. Recovery335 only emergency, old code can lose newer fields when writing. Next versionCode >335 after publication.
- Stable OTA unchanged. CI does not replace device QA.
