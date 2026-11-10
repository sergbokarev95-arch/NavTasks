# RC1 rollback plan — 0.4.143 recovery

- Code is EXACT accepted 0.4.140 application source from SHA e812bdbda1e1bf72d10dbfd6c57f425322f6747d. Only Gradle `versionCode` 168 and `versionName` 0.4.143 are changed; original debug Beta build produces the same `com.navtasks.app.glass` and signer.
- This is an emergency IN-PLACE fallback if experimental non-debuggable 0.4.142/code167 reveals new defects. Never uninstall or clear application data. Export full profiles if possible before installing RC1 and rollback.
- This recovery intentionally returns to the 0.4.140 behavior, not new 0.4.142 functionality. It does not cure any known old 0.4.140 bug.
- Private Actions artifact only. Not stable or public GitHub Release.
- After installing code168, any subsequent same-package update must use versionCode >=169.
- CI state pending.
