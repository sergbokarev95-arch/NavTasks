# NavTasks — действующие решения

Здесь только постоянные правила; исторические подробности доступны в [оригинальном DECISIONS.md](https://github.com/sergbokarev95-arch/NavTasks/blob/fadae2ae6ba8d8eaf81397f56f0fd0680a2f8690/docs/development/DECISIONS.md). При неясности узкого модуля проверить его актуальный код и соответствующее последнее согласование, а не делать предположения из краткого обзора.

## Разработка и выпуск
- Обязателен [AGENTS.md](../../AGENTS.md). Сохранить original signer и package, private source, public stable OTA отдельно. Beta только opt-in; stable OTA без разрешения не менять. Не сливать beta в main автоматически. Recovery — stable-functional аварийный APK с большим versionCode и оригинальной подписью; следующий выпуск выше recovery.
- Оригинальный ключ не раскрывать/не заменять. Backup до изменений, малые diff, регрессии и проверка фактической публикации, различать CI и device QA.
- Не уничтожать исторические записи, не заменять без согласия расписания, импортированные профили, чек-листы и вложения. Не требовать сети для основных функций.

## UI
- Новый визуальный язык: HONOR Calendar как референс + Apple HIG, neutral white/day и graphite/night, коралловый активный акцент #FF5533, красный destructive/overdue, отдельные семантические outcome colors. Не возвращать старые синие акценты в уже согласованные нейтральные элементы.
- Шапка основного экрана — название профиля, не NavTasks. Выбор профилей без лишней шестерёнки, быстрый pinch-switcher; личные заметки в том же стиле. Сохранить принятую анимацию Liquid Glass dock, не добавлять задержку после приземления.
- Календарь Year/Month/Day с общей стилистикой; верхний заголовок даты без дублей, отдельный Year навигационный ряд; лишние стрелки Month/Day удалены, swipes сохранены. Выходные — информационный красный, не статус просрочки. Today «Пропущенные» только при наличии; не дублировать синюю дату, красная дата в карточке сохраняется независимо от срока.
- Task editor: фон sheet night #2C2C2C, outer card #202022, nested #2A2A2C, day neutral. Единый редактор вместо верхнего переключателя частоты, рутинные категории сохраняются. Название, дата, all-day/deadline/window, повторение, напоминания, checklist, активность; missed policy в «Другие настройки». Предыдущий выбор времени и поведение должны сохраняться.

## Данные и функции
- TaskRun хранит snapshots, историю и стабильные ключи; перенос не переписывает выполненное. Частота по effective-dated rule, scope «только сейчас/все последующие» сохранять. Monthly last day учитывает длину месяца. От выполнения — отдельный режим.
- Весь день: срок до конца дня, напоминание от общего reference clock. Явный deadline overrides reference. Time window: напоминания от начала, overdue от конца; до 9 напоминаний, 0–10080 минут. Не расширять на overnight без решения о датах. Группировать совпадающие напоминания, игнорировать удалённые/завершённые/неактивные.
- AMOS/Services показывать в Today только в фактическую дату, предупреждения отдельно. Services planned/unplanned различны; внеплановая заявка без nextDate и напоминаний, дата заявки только метаданные, номер заявки не генерируется приложением. История завершения и график — отдельно.
- Виджеты RemoteViews: только поддерживаемые XML views; отдельный tap на заголовок открывает задачу, не завершает; чек-лист открывается. Иконка notification small monochrome, large оригинальная цветная.
- Handover — текст/история/форматирование, без чеков; вложенные изображения сохранять при обмене и Word/PDF export. Reference самостоятельный модуль, Inspections (SIRE 2.0/VIQ7), Equipment и My Materials; собственный обмен справочника, не смешивать с рутинными задачами. Settings: полный профиль и выборочный обмен. Личные заметки отдельны от судовых.
- Полный и выборочный обмен: сохранение идентификаторов/совместимости, вложений, добавление нового профиля; замена по совпадению имени только в соответствующем сценарии. Не заменять данные незаметно.

- Beta40: editor state refactor and ten composable sections resolved Kotlin compilation blocker. Full CI passed; retain bounded components and explicit typed state in further edits. No stable OTA change. Physical QA remains pending.

- Beta41: use a shared 48dp minimum row height and equal-width repeat cells with explicit spacing; checklist comment editing uses a scoped visible inset field and prominent heading, no changes to read-only reference behavior or other rich editors. Retain Beta40 schedule logic.

- Beta41/recovery311 verified CI and publication; physical layout/keyboard QA remains pending.
