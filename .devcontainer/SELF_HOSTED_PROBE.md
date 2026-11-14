# NavTasks Codespaces runner scheduling probe

This infrastructure branch contains only a connectivity experiment. It
does not build or sign NavTasks or change the installed Android app.

A new Codespace created from this branch automatically tries to use its
existing GitHub identity to register an ephemeral repository self-hosted
runner. That runner is eligible for one label-restricted, secret-free
workflow job only. There are NO signing keys, passwords, tokens or other
credentials in the repository configuration.

Check .codespaces-build/RUNNER_STATUS.md through the file explorer.
- REGISTERED: registration succeeded, but the Actions job might still fail.
- NOT CONNECTED: authorization or runner setup was blocked.
Verify separately that the GitHub Actions workflow concluded SUCCESS.

Current quota restrictions might prevent scheduling even on self-hosted.
If so, do not create more Codespaces, retry indefinitely or purchase credits.

All app Kotlin, versionCode, original key, public update feed and main branch
remain untouched. No signed APK can be released until a separate reviewed
signing workflow runs and passes signature/identity verification.
