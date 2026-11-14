#!/usr/bin/env bash
# One-shot, isolated Codespaces GitHub Actions self-hosted runner connectivity probe.
# Does not request, read, copy, print or use NavTasks signing secrets.
set -Eeuo pipefail
umask 077
unset NAVTASKS_BETA_KEYSTORE_PATH NAVTASKS_BETA_SIGN_PASSWORD KEYSTORE_B64
REPO="sergbokarev95-arch/NavTasks"
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
OUT="$ROOT/.codespaces-build"
STATUS="$OUT/RUNNER_STATUS.md"
mkdir -p "$OUT"
write_status() {
  printf '# Codespaces self-hosted runner probe\n\n**Status:** %s\n\n' "$1" > "$STATUS"
}
fail() {
  write_status "NOT CONNECTED"
  printf 'Reason: %s\n\nNo signing secret has been accessed. No APK has been published.\n' "$1" >> "$STATUS"
  exit 0
}
write_status "INITIALIZING"
command -v gh >/dev/null 2>&1 || fail "GitHub CLI is not available in this Codespace"
command -v curl >/dev/null 2>&1 || fail "curl is not available"
command -v python3 >/dev/null 2>&1 || fail "python3 is not available"
command -v tar >/dev/null 2>&1 || fail "tar is not available"
[[ "$(uname -s)" == "Linux" && "$(uname -m)" == "x86_64" ]] || fail "Expected Linux x86_64 machine"
# A Codespaces token is used ONLY to request the short-lived self-hosted runner
# registration token. Neither token is written into repository files or logs.
if ! gh auth status >/dev/null 2>&1; then
  fail "GitHub CLI is not authenticated inside Codespaces"
fi
REGISTRATION_TOKEN="$(gh api --method POST "repos/$REPO/actions/runners/registration-token" --jq '.token' 2>/dev/null)" \
  || fail "Codespaces credential cannot register a repository runner (Administration: write required)"
[[ -n "$REGISTRATION_TOKEN" ]] || fail "GitHub did not return a runner registration token"

RUNNER_HOME="$HOME/.navtasks-qa17-runner-probe"
mkdir -p "$RUNNER_HOME"
if [[ ! -x "$RUNNER_HOME/config.sh" ]]; then
  META="$(mktemp)"
  curl --fail --silent --show-error --location --retry 2 \
    'https://api.github.com/repos/actions/runner/releases/latest' -o "$META" \
    || fail "Unable to look up official Actions runner version"
  IFS=$'\t' read -r ASSET_URL ASSET_SHA < <(python3 - "$META" <<'PY'
import json, sys
with open(sys.argv[1], encoding="utf-8") as f:
    data = json.load(f)
for asset in data.get("assets", []):
    name = asset.get("name", "")
    digest = asset.get("digest") or ""
    if name.startswith("actions-runner-linux-x64-") and name.endswith(".tar.gz") and digest.startswith("sha256:"):
        print(asset["browser_download_url"] + "\t" + digest.removeprefix("sha256:"))
        break
PY
)
  rm -f "$META"
  [[ -n "${ASSET_URL:-}" && -n "${ASSET_SHA:-}" ]] || fail "No SHA-256-verified official runner asset"
  TMP_ARCHIVE="$(mktemp)"
  curl --fail --silent --show-error --location --retry 3 --connect-timeout 30 \
    --max-time 900 "$ASSET_URL" -o "$TMP_ARCHIVE" \
    || fail "Cannot download official GitHub runner"
  printf '%s  %s\n' "$ASSET_SHA" "$TMP_ARCHIVE" | sha256sum --check --status \
    || fail "Official runner SHA-256 mismatch"
  tar -xzf "$TMP_ARCHIVE" -C "$RUNNER_HOME" \
    || fail "Cannot extract official runner"
  rm -f "$TMP_ARCHIVE"
fi
cd "$RUNNER_HOME"
./config.sh --unattended \
  --url "https://github.com/$REPO" \
  --token "$REGISTRATION_TOKEN" \
  --name "navtasks-qa17-probe-$(hostname | cut -c1-24)" \
  --labels "navtasks-qa17-codespaces-probe" \
  --work "_work" \
  --ephemeral \
  > "$OUT/runner-registration.log" 2>&1 \
  || fail "GitHub refused runner registration (details in local runner-registration.log)"
unset REGISTRATION_TOKEN

nohup ./run.sh > "$OUT/runner.log" 2>&1 < /dev/null &
RUNNER_PID="$!"
sleep 2
kill -0 "$RUNNER_PID" 2>/dev/null \
  || fail "Runner exited unexpectedly (check local runner.log)"
write_status "REGISTERED; awaiting GitHub Actions smoke job"
cat >> "$STATUS" <<'END'
Only the private-repository, label-restricted, **secret-free** smoke job may
run on this one-shot runner. It unregisters after ONE job.

GitHub may still block Action scheduling due to account limits.
No signature or APK is generated in this probe.
END
