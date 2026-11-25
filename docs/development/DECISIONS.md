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

- Beta42: owner requests coherent spacing across app. Apply shared NavEventLayout to task editor first: compact card vertical padding 4dp, horizontal 14dp, 48dp minimum rows, 10dp card gap, 8dp internal and 12dp group spacing. Missed policy same 2-column grid as recurrence; active switch same row as all-day; scoped checklist compact mode. Keep minimum touch targets and no logic changes. Do not globally change unrelated screens without visual review.

- Beta42/recovery313 verified CI and release publication. Device visual QA pending; apply shared spacing to further agreed UI changes rather than independent hardcoded paddings.

- Beta43: пользователь завершил «Сегодня» и начал «Работы» с главного экрана. Использовать общие matte surfaces и размеры; три компактные карточки, коралловые иконки, серые обычные счётчики, красный overdue и оранжевый soon только при ненулевых значениях. Убран дублирующий вводный баннер. Маршруты, модели, расчёты и внутренние экраны неизменны.

- Beta43/recovery315 verified CI and published APKs. Device visual QA pending.

- Beta44: Routine tasks use shared calendar segmented control (neutral lens, coral active ink), neutral header without NavTasks, compact matte cards, neutral list count and coral checklist count; existing Liquid Glass back/add unchanged. Preserve effective frequency, ordering, disabled strikethrough, stable keys, editing and data. No unrelated screen changes.

- Beta44/recovery317 verified CI and release publication; device visual QA pending.

- Beta45: по замечаниям пользователя убрать плашку заголовка рутин, использовать текст/число/линию в стиле Today. AMOS list: neutral header and search, matte cards, compact status filters with coral selection rim and semantic status ink, common calendar segmented control for active/closed. Preserve callbacks, filtering, recurrence, history and stable keys; Services unchanged. Shared AMOS chips and editor text inputs also neutral; no editor behavior changes.

- Beta46: пользователь запросил контрастный поиск AMOS в дневной теме и единый новый стиль попапов. Scoped highContrastField opt-in; opaque neutral alert surfaces, neutral/coral action ink (permanent deletion red), 4dp blur of underlying AMOS page. Editor: visual-only matte styling, equal choice grid, neutral rich note editor, scrollable content and separate aligned footer; retain existing fields, validation, save and scheduling. Full editor structural redesign like routine events deferred to subsequent agreement. No Services/global palette changes.

- Beta47: пользователь согласовал полноценную перестройку AMOS editor. Reuse routine event visual language and slide-up sheet (94% height, top handle, X/check actions, separate scrollable content), same row/card metrics and choice dialog. Preserve AMOS recurrence presets and custom months 1–1200; a single day-based warning, not routine multi-reminder schema. Completion date receives calendar glyph and chevron; «Выполнено» action. Preserve data/save/history/shift logic, stable OTA and Services unchanged.

- Beta48: согласовано использовать coral только для активных действий и настроек AMOS editor (валидная галочка, настроенное повторение/предупреждение, активные инструменты комментария); остальные значения нейтральны. Scoped inset comment field and toolbar, actual placeholder, transparent indicators, smaller explanatory spacing; preserve rich text/HTML, scheduling and neutral defaults in other editors. No Services/stable OTA change.

- Beta49: пользователь согласовал Services redesign по образцу AMOS Beta48. Neutral matte cards, high-contrast search, coral selection, shared tabs, neutral popups/blur, explicit completion date and «Выполнено», bottom editor with X/check, grouped date rows, interval/reminder choice lists, rich comment inset. Retain amber AMOS request badge as semantic status; unplanned dates remain informational, warnings remain months for planned maintenance, history and request semantics unchanged. Other modules/stable OTA unchanged.

- Beta50: согласовано dirty+valid для save checkmark во всех трёх event/work/service editors: disabled neutral for unchanged or invalid; coral for valid changes, including rich text. AMOS requisition date is separate optional metadata on ServiceEntry and ServiceCompletion, never shifts planned maintenance schedule. Legacy planned date unknown; unplanned fallback to old lastDate; new request defaults to today and can be edited. X clears current number/date only; unplanned without number can be saved as draft, never completed until number set. Preserve archived cycles and selective/full profile JSON. Archive signed beta APK before release for recovery of transient publication failure.

- Beta51: vessel Handover redesign. Plain section heading/count and separator; export pinned beside search (user delegated position). Neutral outer/nested surfaces, high-contrast search, blurred neutral dialogs, full-page note editor coral dirty save and neutral rich-text/photo action. Export all sections as before, preserve accordion/search/notes/photos/export and Personal flow.

- Beta52: Reference hub and inspections redesign only. SIRE 2.0/VIQ7 inspection categories visible as coral «В разработке», library routes closed (including legacy restored state); do not remove existing files or expose inaccessible document search hits. Equipment and personal library routes remain available; existing independent import/export below categories restyled. Online library is not implemented in this beta.
