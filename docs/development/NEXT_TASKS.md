# NavTasks — следующие действия

## Beta51
- Beta51 JVM/build/original signer/ZIP artifact success; publication HTTP 500. Draft 458337636 contains uploaded verified APK. Publish existing draft, do not duplicate. Recovery331 publication HTTP 500, retries/ref updates failed; check ref and run before further retry. Recovery329 cannot roll back over code330. Verified ZIP: https://github.com/sergbokarev95-arch/NavTasks/actions/runs/45650863007/artifacts/15041465730
- На устройстве проверить обе темы Handover: заголовок/счётчик/разделитель, экспорт сверху независимо от списка и поиска, разделы и записи, поиск и scroll-to-match, создание/редактирование/удаление, rich formatting, фото, dirty save, Word/PDF. Проверить Personal regression и предыдущие правки Beta50.

## Защитные ограничения
- Экспорт профиля перед установкой. Recovery331 только аварийно; старый код может потерять новые поля при перезаписи. Следующий versionCode >331 после публикации.
- Stable OTA не менять. CI не заменяет device QA.
