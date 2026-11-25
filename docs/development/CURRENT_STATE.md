# NavTasks — текущее состояние

Снимок на 2026-11-25; всегда сверять с GitHub. Репозиторий sergbokarev95-arch/NavTasks (private); public OTA sergbokarev95-arch/NavTasks-Updates. Пакет com.navtasks.app.glass. Android Kotlin/Jetpack Compose, JDK17, SDK36, Gradle8.13. Виджеты RemoteViews. Исходники app/src/main/java/com/navtasks/app/, тесты app/src/test/java/com/navtasks/app/, ресурсы app/src/main/res/.

## Каналы и версия
| Канал | Версия / code | Состояние |
|---|---|---|
| Stable OTA | 1.1.0 / 230 | Не изменять без согласия |
| Последняя проверенная beta | 1.2.0-beta.39 / 306 | CI success, подписанный APK опубликован |
| Beta в работе | 1.2.0-beta.40 / 308 | CI in_progress при проверке |
| Recovery для beta40 | 1.1.0 / 309 | CI success, подписанный APK опубликован |

Рабочий исходник Beta40: `feature/navtasks-1.2.0-beta40-event-editor`, `fadae2ae6ba8d8eaf81397f56f0fd0680a2f8690`. Main до документационного обновления: 908c5d3c9df9840473af882491020401da6bc86f; не база новой beta. Документационная ветка не является исходником новой сборки.

## Подтверждённые артефакты
- Beta39: [CI 45548098247](https://github.com/sergbokarev95-arch/NavTasks/actions/runs/45548098247), source 89c4bdfd89a908d4340b51ed5b43d3d929e9be1d; [APK](https://github.com/sergbokarev95-arch/NavTasks/releases/download/v1.2.0-beta.39/NavTasks_1.2.0-beta.39.apk), 37183310 bytes, SHA256 9aefff9748ee3d6c5dfb9a8126684050abd6e51e843b5df80dd41ab92aa3c86d.
- Recovery309: [CI 45557631091](https://github.com/sergbokarev95-arch/NavTasks/actions/runs/45557631091), source 8821534775bc58b109915acd1907ce00cadf9214; [APK](https://github.com/sergbokarev95-arch/NavTasks/releases/download/v1.1.0-recovery-code309/NavTasks_1.1.0-recovery-code309.apk), 37068614 bytes, SHA256 29315f1a687ab3557d2e0137a0421062bcdf4e180b3ab3ae39a98b5dbb2133cc.
- Beta40: [CI 45557597680](https://github.com/sergbokarev95-arch/NavTasks/actions/runs/45557597680), job 139013303032. Не считать готовой до успешного run и опубликованного asset. Workflow .github/workflows/navtasks-1.2.0-beta40-event-editor.yml: timeout 50 минут. При проверке лог активного job недоступен. Сборка не отменена; причина долгого выполнения неизвестна.
- Backup до Beta40: backup/navtasks-beta39-before-event-editor (fc5fac15b37879321028ff3f6d5a14e11a816093). Оригинальный signer SHA256 0cb8a3c691e3fb2198682dff8c226aec710e77c7b8fded7d260a92f359a49c54.

## Приложение
- Основные вкладки Сегодня, Работы, Хэндовер, Справочник; календарь доступен из Сегодня. Работы: AMOS, Services, Рутинные задачи. Личный профиль — личные заметки.
- Рутинные задачи: DAILY/WEEKLY/MONTHLY/EXTRA; календарное расписание либо отсчёт от выполнения. Переносы, пропуски, история и независимые checklist snapshots.
- Beta39: поднятый фон редактора #2C2C2C в night, карточки #202022, остальной экран чёрный, дневной стиль без изменения.
- Beta40 (исходники, не верифицированный APK): единый редактор с названием, all-day/deadline/time window, повторением и автоматической категорией, до 9 напоминаний, чек-листом, активностью и скрытыми настройками пропусков. Новые JSON поля совместимы со старыми задачами; напоминания для окна отсчитываются от начала, просрочка — от конца. Интервалы через полночь не реализованы. Существующие ограничения изменения частоты сохранены. На старой версии новые поля могут потеряться при сохранении.
- Полный/выборочный перенос профилей, отдельный обмен справочником, вложения PDF/Word/изображения, поиск и обновления остаются в существующих модулях. Не объявлять новые изменения проверенными на устройстве до обратной связи пользователя.

Подробные постоянные правила: DECISIONS.md; исторические версии: CHANGELOG_DEV.md и [архив](https://github.com/sergbokarev95-arch/NavTasks/tree/fadae2ae6ba8d8eaf81397f56f0fd0680a2f8690/docs/development).
