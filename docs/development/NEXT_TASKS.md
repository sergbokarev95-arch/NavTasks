# NavTasks — следующие действия

## Beta51
- Проверить [Beta51 CI 45650324334](https://github.com/sergbokarev95-arch/NavTasks/actions/runs/45650324334) и [Recovery331 CI 45650510826](https://github.com/sergbokarev95-arch/NavTasks/actions/runs/45650510826), JVM, release, оригинальный signer/package/version, APK assets. Не запускать дубль.
- На устройстве проверить обе темы Handover: заголовок/счётчик/разделитель, экспорт сверху независимо от списка и поиска, разделы и записи, поиск и scroll-to-match, создание/редактирование/удаление, rich formatting, фото, dirty save, Word/PDF. Проверить Personal regression и предыдущие правки Beta50.

## Защитные ограничения
- Экспорт профиля перед установкой. Recovery331 только аварийно; старый код может потерять новые поля при перезаписи. Следующий versionCode >331 после публикации.
- Stable OTA не менять. CI не заменяет device QA.
