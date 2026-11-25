# NavTasks — точка продолжения

Обновлено 2026-11-25. Канонический контекст: `docs/navtasks-development`. [Регламент](../../AGENTS.md).

## Прямо сейчас
- Пользователь завершил этап «Сегодня» на Beta42; переходим к «Работы», сначала только главный экран.
- Beta43/code314: `feature/navtasks-1.2.0-beta43-works-hub`, SHA 99e6297a72d738511d0d5525d902c104a56c8869, [CI 45569665465](https://github.com/sergbokarev95-arch/NavTasks/actions/runs/45569665465) in_progress, job 139052913697. Компактные нейтральные карточки, коралловые иконки, обычные счётчики серые, просрочки красные, скоро оранжевые только при ненулевом значении; убран вводный баннер. Маршруты и расчёты прежние.
- Recovery315: `qa/navtasks-1.1.0-recovery-code315-for-beta43`, SHA ae0e75e70e10911919e3830a811a5d7d20f19a44, [CI 45569708596](https://github.com/sergbokarev95-arch/NavTasks/actions/runs/45569708596) in_progress, job 139053056957.
- Резервная ветка `backup/navtasks-beta42-before-works-hub`. Последняя подтверждённая [Beta42](https://github.com/sergbokarev95-arch/NavTasks/releases/download/v1.2.0-beta.42/NavTasks_1.2.0-beta.42.apk) code312; stable OTA 1.1.0/code230 неизменна.
- Следующее действие: проверить оба CI и фактические APK, затем пользователь проверяет Works hub в обеих темах. Внутренние экраны AMOS, Services и рутинных задач не трогать до согласования.

## Короткий промт для нового чата
> Продолжай NavTasks. Через подключённый GitHub открой sergbokarev95-arch/NavTasks, ветку docs/navtasks-development. Прочитай AGENTS.md и актуальные HANDOFF, CURRENT_STATE, NEXT_TASKS, DECISIONS. Проверь фактические ветки, Actions и Releases. Продолжи незавершённую работу без повторного запуска, показывай этапы в чате, доводи согласованную разработку до проверенного APK и рабочей ссылки. Не меняй stable OTA.

История до реорганизации: [неизменяемый архив пяти документов](https://github.com/sergbokarev95-arch/NavTasks/tree/fadae2ae6ba8d8eaf81397f56f0fd0680a2f8690/docs/development). Не воспринимать старые статусы как текущие.
