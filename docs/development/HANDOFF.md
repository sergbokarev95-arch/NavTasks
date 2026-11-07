# NavTasks — New Chat Handoff

1. Connect GitHub account sergbokarev95-arch; open private repo sergbokarev95-arch/NavTasks.
2. Read all five docs/development/*.md files first. Confirm main HEAD, app/build.gradle.kts versionName/versionCode, recent commits, GitHub Releases, Actions and relevant feature branches. If GitHub is newer, update these documents before acting.
3. Source: app/src/main/java/com/navtasks/app/; resources: app/src/main/res/; tests: app/src/test/java/com/navtasks/app/.
4. Current stage: 0.4.49 published and CI-verified; device verification of Today Missed zero-hide is pending. 0.4.48 remains the prior stable version. User still needs to visually verify notification icon and responsive widgets on HONOR. Keep 0.4.46 AMOS due-only Today logic.
5. Build: JDK 17, Android SDK 36, Gradle 8.13; `gradle :app:testDebugUnitTest --no-daemon` then `gradle :app:assembleDebug --no-daemon`. GitHub workflow .github/workflows/android-debug.yml uses stable beta signing secrets; do not disclose or overwrite signing keys. Signed release workflows use pinned source commits and publish APK/ZIP to GitHub Releases.
6. Debug build uses applicationId suffix .glass and versionName suffix -beta. Avoid replacing production application or clearing user data.
7. Preserve profiles, task recurrence, completion history, service/AMOS scheduling, notifications, import/export, existing widgets and navigation. Use small diffs and regression tests.
8. For each completed stage: code review, tests, CI evidence, publish APK only when verified, then update CURRENT_STATE, NEXT_TASKS, DECISIONS, CHANGELOG_DEV and HANDOFF with actual commit/release/test evidence.
9. Do not claim physical HONOR/Samsung tests based solely on GitHub Actions success.

## Short continuation prompt

«Продолжай NavTasks из GitHub sergbokarev95-arch/NavTasks. Сначала прочитай docs/development/HANDOFF.md и остальные четыре документа, проверь main, последнюю версию, коммиты и CI. Работай точечно, не ломай данные и не меняй код до согласования текущей задачи. После успешного этапа обнови документацию в GitHub».

## 0.4.48 visual verification after build

- Lock screen / notification drawer: original color app icon, not generic square checkmark; status bar Android small icon remains monochrome.
- Today: missed 0 neutral; missed >0 red glyph/border/count without breaking date row or night mode.
- Widgets: narrow 180dp, wide 300dp, wide >=350dp, portrait/landscape, dark/light; list capacity, text clipping and tap behavior.
- Red missed widget pill should open Today > Missed from both cold start and running app. If it does not, fix the deep link without changes to task data.

- Do NOT distribute 0.4.47: additional code review found an unsupported Space view in the large widget XML. The corrected release target is 0.4.48 / versionCode 70 and must have its own passing CI and GitHub release. Preserve this explanation for provenance.

## Latest verified handoff (2026-11-07)

- Latest release: https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.48-glass-beta — v0.4.48-glass-beta (versionCode 70).
- Immutable released source: 2968dcce377f531b4bc3a57166cb8e0b62d1e1de. Latest publisher workflow commit: 0b77bce58613dbf828b17b8e6dc5ae1a081ecb34.
- Main test/build: https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42332014351 SUCCESS. Publisher: https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42332255873 SUCCESS.
- v0.4.47 is marked obsolete in releases due to an unsupported wide-widget Space. Never recommend installing it.
- Next action: receive HONOR screenshots/feedback on notification identity, Today Missed red glass, compact/wide widget sizes, missed deep link, and 13 Nov AMOS timing. Device result unknown until reported.
- Restart prompt: «Открой GitHub sergbokarev95-arch/NavTasks, прочитай docs/development/HANDOFF.md и CURRENT_STATE.md, проверь последнюю ветку/релиз, затем продолжи с тестов 0.4.48 на HONOR. Сохраняй все изменения и handoff в GitHub».

## 0.4.49 continuation (2026-11-07)

- User accepted: Today Missed chip must disappear entirely if missedCount=0. Existing 0.4.48 kept a neutral 0 chip; remove it and the 7dp gap only from Today, not home widget.
- Implementation touches NavTasksUi.kt and TodayFrequencyGroupingTest, bumps versionCode to 71 / versionName 0.4.49; published with successful CI (details below).
- Verify 0->1, 1->0 and returning from Missed backlog, screen width, dates scrolling, day/night, and no AMOS effects.

## Latest verified release — 0.4.49 (2026-11-07)
- Release: https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.49-glass-beta, APK/ZIP, versionCode 71, stable Beta signing.
- Immutable source: 41ae92386e1459099a6c1c3c069a987c5de04203; publication workflow commit 5dedee07a6f14d95f4730e52ee5fef62da6f8096.
- Main CI: https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42335111965 SUCCESS.
- Publisher CI: https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42335349500 SUCCESS.
- Only UI affected: Today Missed control hides at zero, 180 ms horizontal enter/exit red pill at positive count, date row reclaims space; empty Missed mode automatically closes. Tests cover zero and positive counts.
- No profile/data/AMOS/widget/notification changes in this release.
- User HONOR screenshot/test pending. Inspect the 0↔1 missed transition, day/night, date-row fill/scroll and Back navigation. Do not claim phone testing from CI alone.
- Recovery prompt: «Продолжи NavTasks из GitHub sergbokarev95-arch/NavTasks. Прочитай docs/development/HANDOFF.md и CURRENT_STATE.md; проверь main, последнее v0.4.49-glass-beta и CI. Текущая задача — проверка исчезающей плашки Пропущенные на HONOR. Работай точечно и обновляй docs/development после этапа».
