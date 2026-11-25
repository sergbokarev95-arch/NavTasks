# NavTasks — точка продолжения

Обновлено 2026-11-26. Канонический контекст: `docs/navtasks-development`. [Регламент](../../AGENTS.md).

## Прямо сейчас
- Beta50/code328: `feature/navtasks-1.2.0-beta50-editor-request-fixes`, SHA fb6d139eb9106dcae7f1e9da6c4a6ad818120729, [CI 45645900538](https://github.com/sergbokarev95-arch/NavTasks/actions/runs/45645900538) attempt 1 JVM/build/signer/artifact success, release HTTP 403; attempt 2 queued. [Verified APK ZIP artifact](https://github.com/sergbokarev95-arch/NavTasks/actions/runs/45645900538/artifacts/15038495580). Единая dirty+valid галочка (рутины, AMOS, Services), начальная нормализация rich text не считается правкой. Отступ и отсутствие лишнего разделителя в заявке. Отдельная дата AMOS для плановых и внеплановых, кнопка X очистки, дата сохраняется в истории. Плановые сроки/предупреждения неизменны. Старые planned с номером без даты остаются без даты, legacy unplanned использует прежний lastDate. Внеплановый черновик без номера можно сохранять, выполнить без номера нельзя. Signed APK artifact upload before release.
- Recovery329: `qa/navtasks-1.1.0-recovery-code329-for-beta50`, SHA 7895fc9f250cd2feeb7b21699b12b1f71ac1b06a, [CI 45645966445](https://github.com/sergbokarev95-arch/NavTasks/actions/runs/45645966445) SUCCESS. [APK](https://github.com/sergbokarev95-arch/NavTasks/releases/download/v1.1.0-recovery-code329/NavTasks_1.1.0-recovery-code329.apk), 37068614 bytes, SHA256 cd317420d1f3017332e1c271e38a4d5f3888b68c707a6a8a040b6e331c6a5c43. Emergency only.
- Backup `backup/navtasks-beta49-before-editor-request-fixes`. Последняя опубликованная beta в Releases пока Beta49/code326; Recovery329 опубликована. Stable OTA 1.1.0/code230 unchanged.
- Следующее действие: проверить оба CI и фактические релизы/артефакты; не запускать дубль. Device QA: галочка при открытии/изменении/откате, rich comments, плановая заявка с датой/без даты, очистка, внеплановый черновик, история и экспорт. Экспорт профиля до установки. Recovery не поддерживает новое поле даты.

## Короткий промт для нового чата
> Продолжай NavTasks. Через подключённый GitHub открой sergbokarev95-arch/NavTasks, ветку docs/navtasks-development. Прочитай AGENTS.md и актуальные HANDOFF, CURRENT_STATE, NEXT_TASKS, DECISIONS. Проверь фактические ветки, Actions и Releases. Продолжи незавершённую работу без повторного запуска, показывай этапы в чате, доводи согласованную разработку до проверенного APK и рабочей ссылки. Не меняй stable OTA.

История до реорганизации: [неизменяемый архив пяти документов](https://github.com/sergbokarev95-arch/NavTasks/tree/fadae2ae6ba8d8eaf81397f56f0fd0680a2f8690/docs/development). Не воспринимать старые статусы как текущие.
