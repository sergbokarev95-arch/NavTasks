# NavTasks — следующие действия

## Beta49
- Проверить уже запущенные [Beta49 CI 45640816693](https://github.com/sergbokarev95-arch/NavTasks/actions/runs/45640816693) и [Recovery327 CI 45640883230](https://github.com/sergbokarev95-arch/NavTasks/actions/runs/45640883230), JVM, release, оригинальный signer/package/version, фактические assets. Не запускать дубль.
- На HONOR проверить обе темы Services: поиск и фильтры, работы/заявки/закрытые, карточки, историю, номер заявки, попапы, bottom sheet (Back/drag/keyboard), planned/unplanned, даты, собственный интервал, предупреждение, rich comments, сохранение и выполнение.
- Проверить, что внеплановые заявки не приобрели срок/уведомления, а плановые сохраняют ручные даты и пересчёт от фактического выполнения.

## Защитные ограничения
- Экспортировать профиль перед установкой. Recovery327 только аварийно; старый код может не сохранять новые поля Beta40 при редактировании. Следующий versionCode >327 после публикации Recovery327.
- Stable OTA не менять. CI не заменяет device QA.
