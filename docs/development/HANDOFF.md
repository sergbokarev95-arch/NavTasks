# NavTasks — точка продолжения

Обновлено 2026-11-25. Канонический контекст: `docs/navtasks-development`. [Регламент](../../AGENTS.md).

## Прямо сейчас
- Пользователь проверил Beta44 и попросил убрать плашку списка рутинных задач, начать редизайн AMOS / By Crew. Beta45/code318: `feature/navtasks-1.2.0-beta45-amos-style`, SHA e559b52ed07a396da9d15c0de7d4adae842936a5, [CI 45622459049](https://github.com/sergbokarev95-arch/NavTasks/actions/runs/45622459049) запущен, результат и публикация ещё не подтверждены.
- Recovery319: `qa/navtasks-1.1.0-recovery-code319-for-beta45`, SHA 69cc98c70dd4f081a50b6a1876c6768f0bbce9ba, [CI 45622669663](https://github.com/sergbokarev95-arch/NavTasks/actions/runs/45622669663) запущен, результат и публикация ещё не подтверждены.
- Резервная ветка `backup/navtasks-beta44-before-amos-style` (Beta44 source f85ac5eefe07f18d540d7e567a9bf72687567993). Последние проверенные APK пока Beta44/code316 и Recovery317 (см. CURRENT_STATE). Stable OTA 1.1.0/code230 неизменна.
- Следующее действие: проверить оба уже запущенных CI и фактические релизы, не запускать дубль. После CI — визуальная проверка рутины и AMOS в обеих темах; Services не менять до согласования. Экспорт профиля перед установкой.

## Короткий промт для нового чата
> Продолжай NavTasks. Через подключённый GitHub открой sergbokarev95-arch/NavTasks, ветку docs/navtasks-development. Прочитай AGENTS.md и актуальные HANDOFF, CURRENT_STATE, NEXT_TASKS, DECISIONS. Проверь фактические ветки, Actions и Releases. Продолжи незавершённую работу без повторного запуска, показывай этапы в чате, доводи согласованную разработку до проверенного APK и рабочей ссылки. Не меняй stable OTA.

История до реорганизации: [неизменяемый архив пяти документов](https://github.com/sergbokarev95-arch/NavTasks/tree/fadae2ae6ba8d8eaf81397f56f0fd0680a2f8690/docs/development). Не воспринимать старые статусы как текущие.
