# NavTasks — точка продолжения

Обновлено 2026-11-26. Канонический контекст: `docs/navtasks-development`. [Регламент](../../AGENTS.md).

## Прямо сейчас
- Beta51/code330: `feature/navtasks-1.2.0-beta51-handover-redesign`, SHA 6d38f5e6d68296e80a9fbadf9a2625002137a9a2, [CI 45650863007](https://github.com/sergbokarev95-arch/NavTasks/actions/runs/45650863007) запущен, APK пока не подтверждён. Handover redesign: plain heading/count/divider, fixed Export beside search, neutral accordion cards and nested entries, high-contrast search, blurred neutral dialogs, full-screen note editor with dirty-state coral save check, neutral rich-text surface and photo action. Data/export formats unchanged; Personal behavior retained.
- Recovery331: `qa/navtasks-1.1.0-recovery-code331-for-beta51`, SHA 6c2f4be054878bb0f2b371d891d5949bc48772f5, [CI 45650510826](https://github.com/sergbokarev95-arch/NavTasks/actions/runs/45650510826) запущен. Ref update initially failed GraphQL/timeout; checked unchanged ref/no run before retry; succeeded.
- Backup `backup/navtasks-beta50-before-handover-redesign`. Previous Beta50/code328 [APK](https://github.com/sergbokarev95-arch/NavTasks/releases/download/v1.2.0-beta.50/NavTasks_1.2.0-beta.50.apk), CI 45645900538 attempt 2 SUCCESS, size 37297998 bytes, SHA256 58e156b493c0264e5c20d3f59170c7abe7d373dc35ffd9d07680c9cd2879c92d. Recovery329 published. Stable OTA 1.1.0/code230 unchanged.
- Следующее действие: проверить оба CI и реальные APK; не запускать дубль. Device QA: обе темы Handover, счётчик/разделитель, экспорт сверху, поиск и раскрытие, попапы, редактирование/фото/форматирование, Word/PDF, Personal regression. Экспорт профиля перед установкой.

## Короткий промт для нового чата
> Продолжай NavTasks. Через подключённый GitHub открой sergbokarev95-arch/NavTasks, ветку docs/navtasks-development. Прочитай AGENTS.md и актуальные HANDOFF, CURRENT_STATE, NEXT_TASKS, DECISIONS. Проверь фактические ветки, Actions и Releases. Продолжи незавершённую работу без повторного запуска, показывай этапы в чате, доводи согласованную разработку до проверенного APK и рабочей ссылки. Не меняй stable OTA.

История до реорганизации: [неизменяемый архив пяти документов](https://github.com/sergbokarev95-arch/NavTasks/tree/fadae2ae6ba8d8eaf81397f56f0fd0680a2f8690/docs/development). Не воспринимать старые статусы как текущие.
