#!/usr/bin/env python3
"""Offline-only generation and validation for a NavTasks stable update manifest.

No GitHub credentials, Android runtime code, network requests or publishing.
Release CI MUST check the APK package/version/signature independently with
aapt/apksigner before calling this utility. SHA-256 alone is not authentication.
"""
from __future__ import annotations

import argparse
import hashlib
import json
import re
from datetime import datetime, timezone
from pathlib import Path

PUBLIC_REPO = "sergbokarev95-arch/NavTasks-Updates"
PACKAGE = "com.navtasks.app.glass"
MIN_SDK = 31
_VERSION = re.compile(r"(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)\Z")
_SHA256 = re.compile(r"[0-9a-fA-F]{64}\Z")


def sha256_file(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for block in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(block)
    return digest.hexdigest()


def build_stable_manifest(
    *, apk_path: Path, version_name: str, version_code: int,
    min_supported_version_code: int, signer_sha256: str,
    notes_ru: str, notes_en: str,
    previous: dict | None = None, released_at: str | None = None,
) -> dict:
    """Validate release metadata without writing to any user-data storage."""
    if not _VERSION.fullmatch(version_name):
        raise ValueError("Stable versionName must be X.Y.Z, without beta/RC suffix")
    if isinstance(version_code, bool) or not isinstance(version_code, int) or version_code <= 0:
        raise ValueError("versionCode must be a positive integer")
    if (isinstance(min_supported_version_code, bool)
            or not isinstance(min_supported_version_code, int)
            or not (1 <= min_supported_version_code <= version_code)):
        raise ValueError("Explicit tested minimum compatible versionCode is required")
    if not _SHA256.fullmatch(signer_sha256):
        raise ValueError("APK signing certificate SHA-256 digest must be 64 hex chars")
    for label, note in (("ru", notes_ru), ("en", notes_en)):
        if not isinstance(note, str) or not (1 <= len(note.strip()) <= 4000):
            raise ValueError(f"Missing or oversized {label} release notes")
    if previous is not None:
        if previous.get("schemaVersion") != 1 or previous.get("channel") != "stable":
            raise ValueError("Unrecognized prior stable channel metadata")
        if previous.get("available") is True:
            old = previous.get("versionCode")
            if not isinstance(old, int) or isinstance(old, bool) or version_code <= old:
                raise ValueError("Cannot publish same or older versionCode")
        elif previous.get("available") is not False:
            raise ValueError("Previous channel availability is invalid")
    apk_path = Path(apk_path)
    if not apk_path.is_file() or apk_path.stat().st_size <= 0:
        raise ValueError("A complete non-empty APK is required")
    tag = f"v{version_name}"
    asset = f"NavTasks_{version_name}.apk"
    timestamp = released_at or datetime.now(timezone.utc).isoformat(timespec="seconds").replace("+00:00", "Z")
    if not re.fullmatch(r"\d{4}-\d\d-\d\dT\d\d:\d\d:\d\dZ", timestamp):
        raise ValueError("releasedAt must be an ISO-8601 UTC timestamp")
    return {
        "schemaVersion": 1,
        "channel": "stable",
        "available": True,
        "applicationId": PACKAGE,
        "versionName": version_name,
        "versionCode": version_code,
        "minSdk": MIN_SDK,
        "minSupportedVersionCode": min_supported_version_code,
        "releasedAt": timestamp,
        "apk": {
            "url": f"https://github.com/{PUBLIC_REPO}/releases/download/{tag}/{asset}",
            "sizeBytes": apk_path.stat().st_size,
            "sha256": sha256_file(apk_path),
            "signerSha256": signer_sha256.lower(),
        },
        "releaseNotes": {"ru": notes_ru.strip(), "en": notes_en.strip()},
    }


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--apk", type=Path, required=True)
    parser.add_argument("--version-name", required=True)
    parser.add_argument("--version-code", type=int, required=True)
    parser.add_argument("--min-supported-version-code", type=int, required=True,
                        help="Must have independently passed migration/install tests")
    parser.add_argument("--signer-sha256", required=True)
    parser.add_argument("--notes-ru-file", required=True, type=Path)
    parser.add_argument("--notes-en-file", required=True, type=Path)
    parser.add_argument("--previous", required=True, type=Path)
    parser.add_argument("--output", required=True, type=Path)
    args = parser.parse_args()
    previous = json.loads(args.previous.read_text(encoding="utf-8"))
    manifest = build_stable_manifest(
        apk_path=args.apk, version_name=args.version_name,
        version_code=args.version_code,
        min_supported_version_code=args.min_supported_version_code,
        signer_sha256=args.signer_sha256,
        notes_ru=args.notes_ru_file.read_text(encoding="utf-8"),
        notes_en=args.notes_en_file.read_text(encoding="utf-8"),
        previous=previous,
    )
    args.output.write_text(json.dumps(manifest, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")


if __name__ == "__main__":
    main()
