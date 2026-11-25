# NavTasks — следующие действия

## Beta45
- Проверить уже запущенные [Beta45 CI 45622058402](https://github.com/sergbokarev95-arch/NavTasks/actions/runs/45622058402) и [Recovery319 CI 45622112228](https://github.com/sergbokarev95-arch/NavTasks/actions/runs/45622112228), SHA, JVM tests, release build, package/version/non-debuggable/original signer и фактические APK assets. Не запускать повторно без причины.
- После успешной публикации проверить на HONOR обе темы: рутины — заголовок/число/полоса, все категории, список и редактор; AMOS — шапка/поиск, три фильтра, работы/закрытые, статусы/даты, история, кнопки, редактор.
- Services — только по отдельному согласованию.

## Защитные ограничения
- Экспортировать профиль перед установкой. Recovery319 только аварийно; старый код может не сохранять новые поля Beta40 при редактировании. Следующий versionCode >319 после публикации Recovery319.
- Stable OTA не менять. CI не заменяет device QA.
