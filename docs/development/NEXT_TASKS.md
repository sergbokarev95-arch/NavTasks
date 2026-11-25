# NavTasks — следующие действия

## Beta55 / Recovery339
- Beta55/code338: `feature/navtasks-1.2.0-beta55-equipment-actions-layout`, SHA 1495b438bc8ddb9e8996596fee40d433b02a50b9, [CI](https://github.com/sergbokarev95-arch/NavTasks/actions/runs/45675435302) in_progress. Title gets its own row (up to three lines), neutral Export button and edit/add controls in separate row; visible centered destructive folder button only in rename popup. No delete inside folder. Data and exchange unchanged. Backup `backup/navtasks-beta54-before-equipment-actions-layout`.
- Recovery339: `qa/navtasks-1.1.0-recovery-code339-for-beta55`, SHA 1feee2c9748b28d9f7db792571854ab31c9cd767, [CI](https://github.com/sergbokarev95-arch/NavTasks/actions/runs/45675542756) in_progress. Same stable logic as Recovery337, bumped code only. No stable OTA change.
- Continue polling these exact runs. No duplicate builds. Neither APK is verified or published yet.
- After success verify JVM tests, signed APK, package/version, signer and archived artifact; inspect release asset and digest. If publishing fails but build/signing passes, use verified ZIP; do not imply release exists.
- Device QA in both themes: centered visible delete in rename popup only, folder preservation on delete, long equipment titles, export button and separate edit/add row, attachment and search. Export profile before installing. Recovery emergency only.
- Previous verified Beta54: [APK](https://github.com/sergbokarev95-arch/NavTasks/releases/download/v1.2.0-beta.54/NavTasks_1.2.0-beta.54.apk). Stable OTA unchanged.
