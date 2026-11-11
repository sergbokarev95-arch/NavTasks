# Stage4B — signed target test update APK, 0.4.152-test1 / code176

- Exact preceding Stage4A application source `536fd3c1608456e029617787cd9be9c9d9a4e39d` plus Gradle version changes ONLY. Signed full non-debuggable APK and private CI required before any public test publication.
- Package is still `com.navtasks.app.glass` and original Beta signing key. VersionCode176, versionName0.4.152-test1. In-app RC7 tester (code174) can request `channels/test.json` manually.
- Target public prerelease (not created by this workflow): `test-v0.4.152`, asset `NavTasks_0.4.152-test1.apk`, metadata `channel:test` and compatible floor174. Public stable.json MUST remain unchanged, available:false.
- Public repo GitHub connector writes currently fail 403. Publisher requires separate tightly scoped private Actions secret and verification. No private source, data, keystore or tokens in public.
- On-phone E2E pending. On successful in-place install code176, rollback needs separately signed exact RC7 code177 or later; don't uninstall.
