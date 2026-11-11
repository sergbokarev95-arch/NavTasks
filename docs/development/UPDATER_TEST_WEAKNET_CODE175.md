# NavTasks — manual TEST feed weak-network diagnostics: 0.4.150-rc7-fix1 / code175
Source: exact user-installed RC7/code174 at SHA `536fd3c1608456e029617787cd9be9c9d9a4e39d`, backup `backup/navtasks-0.4.150-rc7-before-weak-network-fix`.

## Incident
User's HONOR RC7/code174 displayed "Тестовый канал недоступен" repeatedly, while its browser successfully loaded `channels/test.json`, channel test available true, code176/floor174. Existing RC7 test checker used connect/read 3.5 seconds, disabled redirects, caught all errors identically. There was no proof of JSON-parser failure: parser source matches published metadata.

## Surgical code fix
- No change to stable check, stable.json, application data, task handling, notifications, Amos/Services, Handover or Glass.
- Only `checkTest` fetch block now uses isolated `NavTestFeedNetwork.read()` on IO dispatcher; 12s connect/20s read, up to 3 bounded attempts on timeouts, ordinary IO, HTTP429/5xx; no retry of 4xx or invalid redirects/TLS errors.
- Strict same-origin HTTPS redirect guard anchored to full literal raw test.json path, 32KiB maximum response.
- User-facing localized diagnostics: timeout/DNS/TLS/HTTP status/invalid response/unsafe redirect/other IO, without exposing certificate, token, raw stack or private URLs.
- JUnit tests for URL pinning, retry policy, error taxonomy and unchanged stable checker.
- Version `0.4.150-rc7-fix1` code175 **deliberately** between installed RC7/code174 and already public TEST1/code176; avoids publishing a second test binary or changing its manifest. This code is different from emergency RC8/code175, so do not install RC8 over this fix. Existing emergency exact RC7 source RC9/code177 signed CI42974037324 SUCCESS is the safe fallback if needed. Future post-fallback versionCode>=178.

## Required verification
- CI signed nondebuggable APK, Android package ID `com.navtasks.app.glass`, original certificate vs historical APKs, JUnit tests, private DRAFT release only. No public release of this fix; stable remains available:false.
- After user full profile export, install in place over RC7/code174, launch Settings test button, expect TEST1 code176. If fail, capture new explicit diagnostic code. If succeed, test Later, user-initiated download, Android package installer and data persistence.
