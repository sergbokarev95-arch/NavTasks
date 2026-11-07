# NavTasks — Next Tasks

## Current stage: 0.4.52 — edge-to-edge Services/AMOS source fix, CI pending

User sent HONOR screenshots 2026-11-07 showing a straight cutoff line above system navigation in Services and AMOS, unlike Today. User explicitly requested removal.

### Scoped fix

- ServicesUi.kt and WorkOrdersUi.kt: remove only navigationBarsPadding() on root scrolling Column. Retain statusBarsPadding() and hazeSource() and the navigationBarsPadding() on the floating + buttons.
- Preserve LazyColumn bottom contentPadding 106dp so last cards/buttons are scrollable to a tappable position; content can draw behind the system gesture bar.
- Version 0.4.52 / versionCode 74; no data schema changes or business logic changes.
- Verify source CI (tests + debug APK) then signed GitHub publication from immutable source; update all five docs with commit, CI run, release URLs, SHA256 and device status.

### HONOR acceptance

- Services and AMOS: scroll cards to the bottom; the container should fill behind the gesture region without a horizontal cutoff line.
- Floating + stays above Android system navigation; bottom buttons on last card can scroll above the gesture area and remain tappable.
- Test day/night and expanded/collapsed/search views; Android back gesture and scrolling unaffected.
- Preserve other screens and all previous 0.4.51 request-date validation; screenshots from user needed to confirm device result.

## Previous release

- 0.4.51 signed: https://github.com/sergbokarev95-arch/NavTasks/releases/tag/v0.4.51-glass-beta; source 651948cb0c280cd63a418d0054515f4d83102ea9; CI tests/build passed.
