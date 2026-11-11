# NavTasks — Stage 2: public binary-only channel (2026-11-11)

## Completed
- User explicitly approved a SEPARATE public repository for APKs and metadata only, keeping the development repo private. Created [sergbokarev95-arch/NavTasks-Updates](https://github.com/sergbokarev95-arch/NavTasks-Updates) as PUBLIC; verified it is separate from PRIVATE \`sergbokarev95-arch/NavTasks\`.
- Public \`README.md\`, restrictive \`.gitignore\` (only metadata/README in git) and \`channels/stable.json\` were committed. The stable manifest is **inactive**: \`{"schemaVersion":1,"channel":"stable","available":false}\`. No APK, source files, signing secrets, drafts, prereleases, recovery builds or personal data published.
- The user installed signed non-debuggable \`0.4.142-rc1\` code167 on HONOR and reported the application works. This confirms basic in-place installation, NOT exhaustive on-device validation of every historical data schema.
- Existing source RC1 commit \`6c080c5d894b4756f645f72b87623a37ab79a74a\` protected at \`backup/navtasks-0.4.142-before-update-channel\`. Stage 2 isolated feature branch \`feature/navtasks-updates-stage2-channel\`, without modifying application code, package, version, key, Manifest, notifications, widgets or dock.
- Offline-only utility \`tools/navtasks_update_feed.py\` generates deterministic per-version stable metadata after APK validation by the release workflow. It enforces \`X.Y.Z\` stable version naming, strictly increasing positive \`versionCode\`, matching package \`com.navtasks.app.glass\`, \`minSdk=31\`, required explicitly tested \`minSupportedVersionCode\`, APK SHA-256/size, signing certificate digest, URL constrained to the public release repository and nonempty RU/EN release notes. Synthetic tests cover bad versions, downgrade/recovery, missing notes, incompatible floor and hash validation. This utility **never** touches user data, signs APKs or publishes remotely.
- Workflow \`.github/workflows/navtasks-updates-stage2-validate.yml\` runs tests and checks that public stable.json is anonymous and inactive, enforcing exact preservation of RC1 app code.

## Security and distribution contract
1. Build and test signed release APK in PRIVATE NavTasks using SAME \`com.navtasks.app.glass\` and existing signing key. Do NOT distribute an APK with a different signer or applicationId.
2. Before first real stable publication, verify in-place upgrade and data preservation on stored snapshots from several historic versions; decide explicitly which old versionCodes are supported and set \`minSupportedVersionCode\` accordingly. App versions below this floor must not be offered auto-update.
3. Separate manual stable release approval from normal RC/Beta/recovery production. GitHub source release must NOT be draft/prerelease and must have a proper stable semantic version and full APK. Never infer stable from highest numeric versionCode (e.g. recovery codes 168).
4. Confirm APK package/version/minSdk with Android \`aapt\`, APK signing certificate against two known signed historical APKs with \`apksigner\`, and APK hash before copying only the verified APK and checksum to the public release.
5. Public distribution release tag \`vX.Y.Z\`; APK asset \`NavTasks_X.Y.Z.apk\`; a matching \`.sha256\` file; bilingual release notes. Publish the verified public release FIRST, THEN atomically update \`channels/stable.json\` as the last operation so a failed upload never advertises a broken APK.
6. Never put a token in the APK or public metadata. \`GITHUB_TOKEN\` from private source GitHub Actions cannot publish to a different repository. A tightly scoped GitHub App installation token or **fine-grained PAT** restricted to public \`NavTasks-Updates\` with Contents Read/Write is required in PRIVATE repo Actions secret \`NAVTASKS_UPDATES_PUBLISH_TOKEN\`. User must configure this via GitHub UI; never paste credentials in chat. Do not enable the publishing workflow until the secret and manual approvals are ready.
7. Metadata SHA-256 protects against accidental corruption, **not publisher authenticity** on its own. Android PackageManager and a pinned app signing certificate MUST verify the APK independently before the installer is opened.
8. Public source repository cannot store private code, profile exports, keystore/cert private keys or internal CI credentials. The public channel contains only APK binaries in Releases and metadata.
9. Installed NavTasks remains entirely offline. Future updater networking is asynchronous, voluntary and outside task/notification/database code.

## Still pending; do not claim done
- No stable release APK is published; the public channel intentionally says \`available=false\`.
- No cross-repository publisher credential is configured, no production promotion executed. Stage 2 code validates the manifest but does not publish.
- No Android update checker/download/installer or UI exists yet. The in-app updater is Stage 3.
- Full direct upgrade from old versions with real archived data is not proven; source-level tests cannot guarantee real-device preservation.
- Before stable 1.0.0: preserve existing signing key, test profile export/restore, app storage data, histories, notes, AMOS, Services, reminders and widgets on HONOR/Samsung, plus safety on failed migrations; revisit \`allowBackup=true\` privacy.
