# NavTasks — точка продолжения

Обновлено 2026-11-25. Актуальный контекст ведётся в `docs/navtasks-development`. [Регламент](../../AGENTS.md) обязателен.

## Прямо сейчас
- Beta41/code310: по скриншотам HONOR исправлены выравнивание строк, равная сетка повторения и заметное поле комментария. Branch `feature/navtasks-1.2.0-beta41-editor-polish`, SHA 511d887a91f9a71c1c9f1f0acc58cbf78d61b781, [CI](https://github.com/sergbokarev95-arch/NavTasks/actions/runs/45565768088) in_progress. Recovery311 branch `qa/navtasks-1.1.0-recovery-code311-for-beta41`, SHA 6bc78cbc8d39ada5976c73506f9e82a94d25b4e8, [CI](https://github.com/sergbokarev95-arch/NavTasks/actions/runs/45565827169) in_progress. Backup `backup/navtasks-beta40-before-editor-polish`.

- Beta 40 (1.2.0-beta.40, code 308): согласованный календарный редактор задачи, исходники в `feature/navtasks-1.2.0-beta40-event-editor`, точный запущенный SHA `5f646eefd0c0e39038485b8f0e7808078d6892e2`.
- [Actions Beta40](https://github.com/sergbokarev95-arch/NavTasks/actions/runs/45563258589), job 139031805786, source 5f646eefd0c0e39038485b8f0e7808078d6892e2: SUCCESS. Full JVM tests, signed release build, original signer/package/version and publication passed. [Beta40 APK](https://github.com/sergbokarev95-arch/NavTasks/releases/download/v1.2.0-beta.40/NavTasks_1.2.0-beta.40.apk), 37232462 bytes, SHA256 86bc88b4efb1c424381464f040a57155b995a63af74b7591f7313c8b984cd326. Device QA pending.
- Recovery 309: [готовый APK](https://github.com/sergbokarev95-arch/NavTasks/releases/download/v1.1.0-recovery-code309/NavTasks_1.1.0-recovery-code309.apk), [CI](https://github.com/sergbokarev95-arch/NavTasks/actions/runs/45557631091) SUCCESS. SHA источника 8821534775bc58b109915acd1907ce00cadf9214.
- Последняя подтверждённая Beta: 40/code308. Стабильная OTA 1.1.0/code230 не изменена.
- Следующее действие: завершить и проверить Beta41 и Recovery311; затем пользователь проверяет визуальные изменения на HONOR. Не менять stable OTA.

## Короткий промт для нового чата
> Продолжай NavTasks. Через подключённый GitHub открой sergbokarev95-arch/NavTasks, ветку docs/navtasks-development. Прочитай AGENTS.md и актуальные HANDOFF, CURRENT_STATE, NEXT_TASKS, DECISIONS. Проверь фактические ветки, Actions и Releases. Продолжи незавершённую работу без повторного запуска, показывай этапы в чате, доводи согласованную разработку до проверенного APK и рабочей ссылки. Не меняй stable OTA.

История до реорганизации: [неизменяемый архив пяти документов](https://github.com/sergbokarev95-arch/NavTasks/tree/fadae2ae6ba8d8eaf81397f56f0fd0680a2f8690/docs/development). Не воспринимать старые статусы как текущие.
