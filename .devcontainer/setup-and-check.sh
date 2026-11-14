#!/usr/bin/env bash
# Isolated QA17 preflight. No signing secrets, publication, or app-data changes.
set -Eeuo pipefail

cd "$(dirname "$0")/.."
ROOT="$PWD"
REPORT_DIR="$ROOT/.codespaces-build"
STATUS="$REPORT_DIR/STATUS.md"
mkdir -p "$REPORT_DIR"
PHASE="Environment preflight"

write_status() {
  printf '# NavTasks QA17 Codespaces check\n\n**Status:** %s\n\n**Stage:** %s\n\nThis is an UNSIGNED check, NOT an installable NavTasks update.\n\n' "$1" "$PHASE" > "$STATUS"
}
on_exit() {
  local code=$?
  if [[ "$code" -eq 0 ]]; then
    write_status "COMPLETED: JVM tests and compilation passed"
    cat >> "$STATUS" <<'END'
Validated: JDK 17, Gradle 8.13, Android API 36 / extension 19,
Build Tools 35.0.0, app JVM tests and unsigned release compilation.

NOT validated: historical signing certificate, in-place upgrade,
HONOR/Samsung UI or PDF exchange. No signed QA17 APK exists here.
Do not install or share this unsigned build.
END
  else
    write_status "FAILED (exit $code)"
    cat >> "$STATUS" <<'END'
Inspect the stage logs in .codespaces-build for the cause.
No application code, GitHub release or phone data was modified.
END
  fi
}
trap on_exit EXIT
write_status "RUNNING"

if [[ ! -f app/build.gradle.kts || ! -f gradle/wrapper/gradle-wrapper.properties ]]; then
  echo 'NavTasks Android project missing' >&2
  exit 2
fi
if ! grep -Eq 'versionCode[[:space:]]*=[[:space:]]*222' app/build.gradle.kts || \
   ! grep -Eq 'versionName[[:space:]]*=[[:space:]]*"0\.4\.197"' app/build.gradle.kts; then
  echo 'Expected QA17 version 0.4.197/code222; refusing other sources' >&2
  exit 3
fi

{ java -version; gradle --version; } > "$REPORT_DIR/toolchain.log" 2>&1
if ! java -version 2>&1 | head -n 1 | grep -qE 'version "17\.'; then
  echo 'JDK 17 required' >&2
  exit 4
fi
if ! gradle --version | grep -q 'Gradle 8.13'; then
  echo 'Gradle 8.13 required' >&2
  exit 5
fi

PHASE="Install isolated Android SDK"
write_status "RUNNING"
SDK_ROOT="$ANDROID_HOME"
TOOLS_DIR="$SDK_ROOT/cmdline-tools/latest"
SDKMANAGER="$TOOLS_DIR/bin/sdkmanager"
mkdir -p "$SDK_ROOT/cmdline-tools"
if [[ ! -x "$SDKMANAGER" ]]; then
  command -v curl >/dev/null && command -v unzip >/dev/null || {
    echo 'curl and unzip missing in devcontainer' >&2
    exit 6
  }
  TMP_DIR="$(mktemp -d)"
  # Exact Google command-line tools package/checksum published for Linux.
  URL='https://dl.google.com/android/repository/commandlinetools-linux-15859902_latest.zip'
  SHA256='4e4c464f145a7512b57d088ac6c278c03c9eea610886b35a5e0804e74eedf583'
  curl --fail --location --retry 3 --connect-timeout 30 --max-time 900 \
    --output "$TMP_DIR/android-tools.zip" "$URL" \
    > "$REPORT_DIR/sdk-download.log" 2>&1
  echo "$SHA256  $TMP_DIR/android-tools.zip" | sha256sum --check --status
  unzip -q "$TMP_DIR/android-tools.zip" -d "$TMP_DIR/extracted"
  [[ -f "$TMP_DIR/extracted/cmdline-tools/bin/sdkmanager" ]]
  mv "$TMP_DIR/extracted/cmdline-tools" "$TOOLS_DIR"
  rm -rf "$TMP_DIR"
fi

# Accept Android SDK terms in this explicit user-created development environment.
# Ignore 'yes' SIGPIPE only, not sdkmanager errors.
( set +o pipefail; yes | "$SDKMANAGER" --sdk_root="$SDK_ROOT" --licenses ) \
  > "$REPORT_DIR/sdk-licenses.log" 2>&1
"$SDKMANAGER" --sdk_root="$SDK_ROOT" --install \
  'platforms;android-36' 'platforms;android-36-ext19' \
  'build-tools;35.0.0' 'platform-tools' \
  > "$REPORT_DIR/sdk-install.log" 2>&1

for file in \
  "$SDK_ROOT/platforms/android-36/android.jar" \
  "$SDK_ROOT/platforms/android-36-ext19/android.jar" \
  "$SDK_ROOT/build-tools/35.0.0/aapt"; do
  [[ -s "$file" ]] || { echo "SDK file not found: $file" >&2; exit 7; }
done

PHASE="Run JVM tests and unsigned release build"
write_status "RUNNING"
# Codespaces must never receive or use the original release signing credentials.
unset NAVTASKS_BETA_KEYSTORE_PATH NAVTASKS_BETA_SIGN_PASSWORD KEYSTORE_B64
gradle :app:testDebugUnitTest :app:assembleRelease --no-daemon --max-workers=2 \
  -Dkotlin.compiler.execution.strategy=in-process \
  > "$REPORT_DIR/gradle-build.log" 2>&1

# The known QA17 release Gradle configuration produces an unsigned APK here.
# Fail closed if it unexpectedly produces the signed release filename.
if [[ -f app/build/outputs/apk/release/app-release.apk ]]; then
  echo 'Unexpected release output; signing audit required' >&2
  exit 8
fi
