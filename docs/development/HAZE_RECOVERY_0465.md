# Haze recovery code 90 for optional in-place rollback from 0.4.64

- **Recovery only**: unmodified stable 0.4.59 Haze code, **only** versionCode=90/versionName=0.4.65-beta. Derived from earlier proven code86 recovery branch `38988ec5bc931a11ab2a5fdb8c1d59b42f9a6532`.
- Same `com.navtasks.app.glass` package and signed betaStable key as installed app/candidate code89; safe in-place upgrade over 0.4.64 to prior stable Haze without uninstalling.
- Complete profile export required before either update; do not uninstall, never force-downgrade code.
- Signed APK in compact ZIP; checks SHA256 archive integrity and aapt package/version, apksigner verification, unchanged stable NavGlass.kt and MainActivity.kt hashes.
- Do not install rollback by default; only if new material is rejected or a regression occurs on HONOR.
