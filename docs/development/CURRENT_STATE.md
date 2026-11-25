# NavTasks — текущее состояние

Снимок на 2026-11-25; всегда сверять с GitHub. Репозиторий sergbokarev95-arch/NavTasks (фактическая видимость по GitHub API на 2026-11-25: public; ранее в документации указан private, решение о видимости не изменялось); public OTA sergbokarev95-arch/NavTasks-Updates. Пакет com.navtasks.app.glass. Android Kotlin/Jetpack Compose, JDK17, SDK36, Gradle8.13. Виджеты RemoteViews. Исходники app/src/main/java/com/navtasks/app/, тесты app/src/test/java/com/navtasks/app/, ресурсы app/src/main/res/.

## Каналы и версия
| Канал | Версия / code | Состояние |
|---|---|---|
| Stable OTA | 1.1.0 / 230 | Не изменять без согласия |
| Последняя проверенная beta | 1.2.0-beta.44 / 316 | CI success, подписанный APK опубликован |
| Recovery для beta44 | 1.1.0 / 317 | CI success, подписанный APK опубликован |
| Recovery для beta43 | 1.1.0 / 315 | CI success, подписанный APK опубликован |
| Recovery для beta42 | 1.1.0 / 313 | CI success, подписанный APK опубликован |
| Recovery для beta41 | 1.1.0 / 311 | CI success, подписанный APK опубликован |
| Recovery для beta40 | 1.1.0 / 309 | CI success, подписанный APK опубликован |

Рабочий исходник Beta44: `feature/navtasks-1.2.0-beta44-routines-style`, `f85ac5eefe07f18d540d7e567a9bf72687567993`. Main не база beta; docs-ветка не исходник сборки.

## Активный этап
- Сегодня завершён. Beta44 — рутинные задачи в новом стиле, CI success, device QA pending. AMOS и сервис не изменены.

## Подтверждённые артефакты
- Beta44: [CI 45572098541](https://github.com/sergbokarev95-arch/NavTasks/actions/runs/45572098541) SUCCESS, source f85ac5eefe07f18d540d7e567a9bf72687567993, [APK](https://github.com/sergbokarev95-arch/NavTasks/releases/download/v1.2.0-beta.44/NavTasks_1.2.0-beta.44.apk), 37248846 bytes, SHA256 c012f717b6f67625d3a9fc10765a7b3c7ad650f7a852c51b2ac043e5f75615cd. JVM tests, release build, original signer/package/version/non-debuggable verified. Device QA pending.
- Recovery317: [CI 45572145523](https://github.com/sergbokarev95-arch/NavTasks/actions/runs/45572145523) SUCCESS, source 0f7100eee77b27a9e88ec559af9340af2913bcbc, [APK](https://github.com/sergbokarev95-arch/NavTasks/releases/download/v1.1.0-recovery-code317/NavTasks_1.1.0-recovery-code317.apk), 37068614 bytes, SHA256 34d696f7f9c22c5dfc95aaf8ca7632c1b21cb82423bb8f29692402c680613597. Stable OTA unchanged.
- Beta43: [CI 45569665465](https://github.com/sergbokarev95-arch/NavTasks/actions/runs/45569665465) SUCCESS, source 99e6297a72d738511d0d5525d902c104a56c8869, [APK](https://github.com/sergbokarev95-arch/NavTasks/releases/download/v1.2.0-beta.43/NavTasks_1.2.0-beta.43.apk), 37248846 bytes, SHA256 8ae1006f72929a0b4648fca736b7226f8e19d037d1f7d8be1a82e0e2a7f8e243. JVM tests 1m24s, release 2m31s. Original signer/package/version/non-debuggable verified. Works hub only; device QA pending.
- Recovery315: [CI 45569708596](https://github.com/sergbokarev95-arch/NavTasks/actions/runs/45569708596) SUCCESS, source ae0e75e70e10911919e3830a811a5d7d20f19a44, [APK](https://github.com/sergbokarev95-arch/NavTasks/releases/download/v1.1.0-recovery-code315/NavTasks_1.1.0-recovery-code315.apk), 37068614 bytes, SHA256 f8548ef5ed55b4f2176e28d69623090b53e7cb0f9c2ec0982f291ef7f6009169. Stable OTA unchanged.
- Beta42: [CI 45567527725](https://github.com/sergbokarev95-arch/NavTasks/actions/runs/45567527725) SUCCESS, source 396c87bb4b23d2a97cb7d7aceb1532ade1064f6e, [APK](https://github.com/sergbokarev95-arch/NavTasks/releases/download/v1.2.0-beta.42/NavTasks_1.2.0-beta.42.apk), 37248846 bytes, SHA256 7a8e0af0066448697862a6ffadc8671a643f8b9872d8d3256c0cefebcf5f0450. JVM tests 1m20s, release 2m23s. Original signer/package/version/non-debuggable verified. Shared compact editor metrics, missed-policy grid, active switch and scoped checklist; device QA pending.
- Recovery313: [CI 45567569802](https://github.com/sergbokarev95-arch/NavTasks/actions/runs/45567569802) SUCCESS, source 66303fbb8b3be55b8fac82a75ccc922c0b8da521, [APK](https://github.com/sergbokarev95-arch/NavTasks/releases/download/v1.1.0-recovery-code313/NavTasks_1.1.0-recovery-code313.apk), 37068614 bytes, SHA256 4983be4b288c8bae4b38995833f1a7b38adfeede7ca2acbbc7b80e616a69a668. Stable OTA unchanged.
- Beta41: [CI 45565768088](https://github.com/sergbokarev95-arch/NavTasks/actions/runs/45565768088) SUCCESS, source 511d887a91f9a71c1c9f1f0acc58cbf78d61b781, [APK](https://github.com/sergbokarev95-arch/NavTasks/releases/download/v1.2.0-beta.41/NavTasks_1.2.0-beta.41.apk), 37248846 bytes, SHA256 e144d34ecb2c7800fe81492af9d100c371a06d69bb7185c515d39bb1026f4583. JVM tests 1m32s, release 2m44s. Original signer/package/version/non-debuggable verified. Visual-only changes: 48dp minimum rows, 2-column repeat grid, checklist inset rich text field with placeholder and heading. Device QA pending.
- Recovery311: [CI 45565827169](https://github.com/sergbokarev95-arch/NavTasks/actions/runs/45565827169) SUCCESS, source 6bc78cbc8d39ada5976c73506f9e82a94d25b4e8, [APK](https://github.com/sergbokarev95-arch/NavTasks/releases/download/v1.1.0-recovery-code311/NavTasks_1.1.0-recovery-code311.apk), 37068614 bytes, SHA256 971d162672d4a63ded035b4dace087e76d51d9d0327a45ee140df2f57651ccb5. Stable OTA unchanged.
- Beta39: [CI 45548098247](https://github.com/sergbokarev95-arch/NavTasks/actions/runs/45548098247), source 89c4bdfd89a908d4340b51ed5b43d3d929e9be1d; [APK](https://github.com/sergbokarev95-arch/NavTasks/releases/download/v1.2.0-beta.39/NavTasks_1.2.0-beta.39.apk), 37183310 bytes, SHA256 9aefff9748ee3d6c5dfb9a8126684050abd6e51e843b5df80dd41ab92aa3c86d.
- Recovery309: [CI 45557631091](https://github.com/sergbokarev95-arch/NavTasks/actions/runs/45557631091), source 8821534775bc58b109915acd1907ce00cadf9214; [APK](https://github.com/sergbokarev95-arch/NavTasks/releases/download/v1.1.0-recovery-code309/NavTasks_1.1.0-recovery-code309.apk), 37068614 bytes, SHA256 29315f1a687ab3557d2e0137a0421062bcdf4e180b3ab3ae39a98b5dbb2133cc.
- Beta40: [CI 45563258589](https://github.com/sergbokarev95-arch/NavTasks/actions/runs/45563258589), job 139031805786, SUCCESS. Source 5f646eefd0c0e39038485b8f0e7808078d6892e2. Full JVM tests (1m28s), signed release build (2m8s), package/version/non-debuggable/original signer verification and publication passed. [APK](https://github.com/sergbokarev95-arch/NavTasks/releases/download/v1.2.0-beta.40/NavTasks_1.2.0-beta.40.apk), 37232462 bytes, SHA256 86bc88b4efb1c424381464f040a57155b995a63af74b7591f7313c8b984cd326. First attempt Kotlin OOM; second compileDebugKotlin timeout; editor split into explicit state and ten sections resolved CI blocker. Device QA pending.
- Backup до Beta40: backup/navtasks-beta39-before-event-editor (fc5fac15b37879321028ff3f6d5a14e11a816093). Оригинальный signer SHA256 0cb8a3c691e3fb2198682dff8c226aec710e77c7b8fded7d260a92f359a49c54.

## Приложение
- Основные вкладки Сегодня, Работы, Хэндовер, Справочник; календарь доступен из Сегодня. Работы: AMOS, Services, Рутинные задачи. Личный профиль — личные заметки.
- Рутинные задачи: DAILY/WEEKLY/MONTHLY/EXTRA; календарное расписание либо отсчёт от выполнения. Переносы, пропуски, история и независимые checklist snapshots.
- Beta39: поднятый фон редактора #2C2C2C в night, карточки #202022, остальной экран чёрный, дневной стиль без изменения.
- Beta40 (CI-verified signed APK, device QA pending): единый редактор с названием, all-day/deadline/time window, повторением и автоматической категорией, до 9 напоминаний, чек-листом, активностью и скрытыми настройками пропусков. Новые JSON поля совместимы со старыми задачами; напоминания для окна отсчитываются от начала, просрочка — от конца. Интервалы через полночь не реализованы. Существующие ограничения изменения частоты сохранены. На старой версии новые поля могут потеряться при сохранении.
- Полный/выборочный перенос профилей, отдельный обмен справочником, вложения PDF/Word/изображения, поиск и обновления остаются в существующих модулях. Не объявлять новые изменения проверенными на устройстве до обратной связи пользователя.

Подробные постоянные правила: DECISIONS.md; исторические версии: CHANGELOG_DEV.md и [архив](https://github.com/sergbokarev95-arch/NavTasks/tree/fadae2ae6ba8d8eaf81397f56f0fd0680a2f8690/docs/development).
