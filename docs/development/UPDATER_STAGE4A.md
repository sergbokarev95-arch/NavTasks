# Stage4A — opt-in test update channel, NavTasks 0.4.150-rc7/code174

Source is accepted Stage3C RC5 commit `493ebf271003434ab013488d9d3d6f488a82f872`; preserved as `backup/navtasks-0.4.148-before-test-channel`.

- Adds an isolated manual-only `channels/test.json` endpoint, separate from stable. Never polled at startup, never used for automatic stable prompts or the normal 24-hour throttle.
- Only pinned `test-vX.Y.Z` prerelease APK `NavTasks_X.Y.Z-test1.apk` is supported, with explicit compatible floor, signed same installed NavTasks certificate, code, SHA256, size limit, RU/EN notes.
- Existing full signed APK downloader and native Android installer preserved. No business state, widget, notifications, dock or Glass modifications.
- Public repo writes failed through current GitHub integration (HTTP 403). A GitHub App permission update or narrowly scoped Actions secret `NAVTASKS_UPDATES_PUBLISH_TOKEN` restricted to PUBLIC `NavTasks-Updates` will be required. Never paste secret in chat. Public stable remains `available:false` and NO test binaries are public before publication is verified.
- RC7 signed CI and phone test pending. Before user installation, prepare signed old RC5 source fallback with higher code175.
