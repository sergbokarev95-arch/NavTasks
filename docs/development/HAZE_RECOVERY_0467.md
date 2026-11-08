# Recovery after 0.4.66 — original stable Haze, code92

- Unchanged original stable Haze v0.4.59 task/card/profile data logic, same signed `com.navtasks.app.glass`; versionCode92/versionName0.4.67-beta is the ONLY app change from prior independently tested recovery code90 `b81f572dda245a32020887861ebdf3389c3a6b51`.
- Install over 0.4.66 code91 only if the new visual check behavior or app has a regression; never uninstall. Full profile export before installing beta.
- Distinct tagged compact recovery ZIP, app CI validates original source hash, APK identity, signature, full tests and archive.
- Subsequent Beta version codes must exceed 92 if this recovery is installed.
