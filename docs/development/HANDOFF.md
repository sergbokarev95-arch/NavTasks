# NavTasks — точка продолжения

Обновлено 2026-11-25. Актуальный контекст ведётся в `docs/navtasks-development`. [Регламент](../../AGENTS.md) обязателен.

## Прямо сейчас
- Beta 40 (1.2.0-beta.40, code 308): согласованный календарный редактор задачи, исходники в `feature/navtasks-1.2.0-beta40-event-editor`, точный запущенный SHA `763a2b69e578b785551a6bc9417f3d12e5600403`.
- [Actions Beta40](https://github.com/sergbokarev95-arch/NavTasks/actions/runs/45560588828) — новый запуск на SHA 763a2b69e578b785551a6bc9417f3d12e5600403, in_progress. Старый run 45557597680 отменён по согласованию, журнал подтвердил Kotlin OutOfMemoryError (GC overhead limit exceeded) при compileReleaseKotlin. Новый workflow разделяет test/build, 4 GiB heap, in-process, max-workers=1. APK пока не подтверждён.
- Recovery 309: [готовый APK](https://github.com/sergbokarev95-arch/NavTasks/releases/download/v1.1.0-recovery-code309/NavTasks_1.1.0-recovery-code309.apk), [CI](https://github.com/sergbokarev95-arch/NavTasks/actions/runs/45557631091) SUCCESS. SHA источника 8821534775bc58b109915acd1907ce00cadf9214.
- Последняя подтверждённая Beta: [39](https://github.com/sergbokarev95-arch/NavTasks/releases/download/v1.2.0-beta.39/NavTasks_1.2.0-beta.39.apk), code306. Стабильная OTA 1.1.0/code230 не изменена.
- Следующее действие: проверить Beta40, при успехе проверить asset и обновить CURRENT_STATE/NEXT_TASKS/HANDOFF/CHANGELOG_DEV; при ошибке разобрать лог и исправить. После публикации — тест на HONOR. До этого не начинать Beta41.

## Короткий промт для нового чата
> Продолжай NavTasks. Через подключённый GitHub открой sergbokarev95-arch/NavTasks, ветку docs/navtasks-development. Прочитай AGENTS.md и актуальные HANDOFF, CURRENT_STATE, NEXT_TASKS, DECISIONS. Проверь фактические ветки, Actions и Releases. Продолжи незавершённую работу без повторного запуска, показывай этапы в чате, доводи согласованную разработку до проверенного APK и рабочей ссылки. Не меняй stable OTA.

История до реорганизации: [неизменяемый архив пяти документов](https://github.com/sergbokarev95-arch/NavTasks/tree/fadae2ae6ba8d8eaf81397f56f0fd0680a2f8690/docs/development). Не воспринимать старые статусы как текущие.
