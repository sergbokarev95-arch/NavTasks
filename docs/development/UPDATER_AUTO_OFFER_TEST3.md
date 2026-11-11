# Signed TEST3 target for RC15 startup automatic-offer QA
- Parent exact `a89443fe54c4d5e37ea542f6b33add6bfc86e095` is the RC15 QA candidate code182.
- TEST3 full APK changes ONLY Gradle version to `0.4.162-test3`/code184 and the version-assertion test; same app ID `com.navtasks.app.glass`, same original signer and business stores, widgets, Glass and update engine.
- CI requires full unit regressions, historical signer parity and creates PRIVATE DRAFT only. No test/stable feed mutation; public TEST2 remains until explicitly approved TEST3 publication.
- After explicit public test release approval, TEST3 metadata must use versionCode184, minSupportedVersionCode182, pinned `test-v0.4.162/NavTasks_0.4.162-test3.apk`, actual SHA256, size, and signer cert. Must publish signed asset before switching test.json. Never update stable.json.
- Physical QA: RC15/code182 installed in place over code180 after FULL backup; enable both auto-offers and RC15-only test switch; cold launch shows TEST3 offer; Later defers same code, manual test still offers; OFF blocks startup; Android installer must be explicit and data retained. Recovery with code>184 required.
