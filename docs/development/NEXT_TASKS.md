# NavTasks — следующие действия

## Beta49
- Recovery327 SUCCESS и APK опубликован. Beta49 attempt 1 passed JVM/build/signer but release publication HTTP 403; same run 45640816693 attempt 2 started without changing code or token. Проверить attempt 2 и фактическую публикацию; не запускать дубль.
- На HONOR проверить обе темы Services: поиск и фильтры, работы/заявки/закрытые, карточки, историю, номер заявки, попапы, bottom sheet (Back/drag/keyboard), planned/unplanned, даты, собственный интервал, предупреждение, rich comments, сохранение и выполнение.
- Проверить, что внеплановые заявки не приобрели срок/уведомления, а плановые сохраняют ручные даты и пересчёт от фактического выполнения.

## Защитные ограничения
- Экспортировать профиль перед установкой. Recovery327 только аварийно; старый код может не сохранять новые поля Beta40 при редактировании. Следующий versionCode >327 после публикации Recovery327.
- Stable OTA не менять. CI не заменяет device QA.
