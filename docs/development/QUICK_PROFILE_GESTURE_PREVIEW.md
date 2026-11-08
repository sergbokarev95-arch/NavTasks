# NavTasks Quick Profile Pinch + Settings Footer Preview

- Isolated test package `com.navtasks.app.glassquick` (versionCode 87, 0.4.62-quickprofile-preview1) built from signed 0.4.62 main-app candidate. Original app `com.navtasks.app.glass` and its actual task/profile data are untouched.
- Correct Settings layout: full screen no artificial bottom navigationBarsPadding; weighted scrolling content, bottom-only safe-area inset after final panel, preserving visibility of last panel without a clipped footer.
- On root screens only, a two-finger pinch inward triggers a centered quick profile picker with blurred original page behind it. Requires initial finger span >=96dp, inward motion >=48dp and current span at most 75% of the initial one. Uses PointerEventPass.Initial, does not consume pointer events and ignores ordinary single-finger gestures; disabled inside Settings, editor, Calendar, Services, Work Orders and other subpages.
- Picker shows live profiles from ProfileStore, active checked blue. Existing selected-profile contract maintained: ProfileStore.select, TaskStore.switchProfile, HandoverStore.switchProfile, ServiceStore.switchProfile, WorkOrderStore.switchProfile. Resets tab/day history on change, no data migration or deletion. Back/tap outside dismisses.
- Popup backdrop blur is Compose Modifier.blur(12dp) around background app subtree only, not blurred text in popup, with a separate Dialog translucent neutral glass surface and system dim.
- User needs to create one or two disposable vessel profiles in this **isolated test app** before trying switching; real NavTasks data not shared.
- HONOR acceptance: Settings last item visible and no blue clipped footer, two-finger pinch in Today/Work/Handover/Tasks, normal one-finger scrolling and swipe navigation unaffected, smooth full-screen blur only with dialog, current profile selection works, personal/vessel isolation preserved, dark/day.
- Next stage: real optical buttons (+, Calendar, Settings) one by one after user approves. Not merged to main.


## GitHub Actions verification
- Tested source commit `cc1abcbac68e3efb4801ab8685167fbdc2715344`; CI https://github.com/sergbokarev95-arch/NavTasks/actions/runs/42441508734 SUCCESS, all app unit tests and signed `:app:assembleDebug`.
- Published separate prerelease https://github.com/sergbokarev95-arch/NavTasks/releases/tag/glassquick-preview1.
- Signed ZIP https://github.com/sergbokarev95-arch/NavTasks/releases/download/glassquick-preview1/NavTasks_Glass_QuickProfiles_Preview1.zip, 10,869,116 bytes; SHA256 `97bf5ed69bded324d7dfe27e8f2150bd14e7a3f06d49c93caba78ccaf650f8d6`.
- AAPT verified package `com.navtasks.app.glassquick` versionCode 87. Inner APK byte-for-byte archive check passed.
- Device gesture, popup blur, footer visuals and profile switch still require physical HONOR acceptance. Stable main source and installed user's 0.4.62 remain unchanged. Buttons phase not yet started.
