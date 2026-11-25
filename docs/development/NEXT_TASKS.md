# NavTasks — следующие действия

## Beta57 / Recovery343
- Beta57/code342: `feature/navtasks-1.2.0-beta57-settings-redesign`, SHA 0bcf96aa9f1dbd63bae87f9b35f316430c94ddce, [CI](https://github.com/sergbokarev95-arch/NavTasks/actions/runs/45682550550) in_progress. Settings: shared neutral cards, equal language/theme choices with coral outline, shared coral switches, reminder/digest value rows, neutral profile exchange and selective exchange controls/dialogs, update buttons. Existing optical Back and all persistence/transfer/update logic retained. Backup `backup/navtasks-beta56-before-settings-redesign`.
- Recovery343: `qa/navtasks-1.1.0-recovery-code343-for-beta57`, SHA 95bbcea418a9ea777904192e7a57733db1cd15d7, [CI](https://github.com/sergbokarev95-arch/NavTasks/actions/runs/45682555680) in_progress. Stable logic retained, version and version guard expectations only.
- Continue polling exact runs, no duplicates. APK verification pending. Stable OTA untouched.
- Verify full JVM tests, release APK, package/version, original signer, archive and publication. Compare release asset digest with build log. If publication fails retrieve verified artifact, no unnecessary rerun.
- Device QA: language/theme selection, day/night, toggles and reminder/digest wheels, profile selector and full/selected transfer, destructive confirmations, manual update check; ensure previous data is preserved. No stable OTA without approval.
