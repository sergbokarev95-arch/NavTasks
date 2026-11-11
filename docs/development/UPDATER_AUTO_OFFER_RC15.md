# NavTasks release QA: automatic offer at launch

The user rightly clarified their request concerns **new-NavTasks-version offers**, NOT task notification reminders.

## Root cause
Production automatic startup polls `stable.json` only; TEST1/TEST2 were manual checks of separate `test.json`. Since stable feed is intentionally `available:false`, startup offers were never exercised.

## Safe isolated QA mechanism
Signed `0.4.160-rc15` code182, same installed Android package `com.navtasks.app.glass` and unchanged signer. Its Settings card adds default-OFF `Тестировать предложение при запуске` switch, visible ONLY when version name ends precisely in `-rc15`. Existing `Предлагать обновления` must ALSO be on. On next app launch, checks only TEST feed, uses separate 24h successful-check throttle, previously HONOR-accepted weak-net retry, and opens existing dialogue automatically for higher compatible TEST3/code184. `Позже` defers only that TEST version, independent from stable `Позже`; manual TEST check bypasses throttle.

**No public feed changes or release authority.** Public `stable.json` remains disabled. To test physically, a new signed compatible TEST3/code184 and separate recovery code185+ require explicit public test publication approval. Release 1.0.0 MUST exclude QA controls and TEST manual button; the accepted stable feed path and optional prompt flow remain. Task notifications and stores unchanged.

## QA acceptance
Cold launch with both switches ON shows TEST3; Later then relaunch no repeat same version; manual test opens same version; OFF suppresses startup; no internet startup silent; normal stable check unaffected; full profile data retained. After passing, prepare production-only code>=186 with no tester UI, full regression and separate stable release approval.
