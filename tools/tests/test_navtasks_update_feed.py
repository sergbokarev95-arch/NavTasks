"""No network and no APK publication: validate feed's immutable contract."""
import hashlib
import sys
import tempfile
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from navtasks_update_feed import build_stable_manifest, sha256_file


class StableFeedTests(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.apk = Path(self.temp.name) / "local-test.apk"
        self.apk.write_bytes(b"synthetic-fixture-not-a-real-apk")
        self.args = dict(
            apk_path=self.apk, version_name="1.0.0",
            version_code=169, min_supported_version_code=165,
            signer_sha256="A" * 64,
            notes_ru="Исправления и улучшения.", notes_en="Fixes and improvements.",
            previous={"schemaVersion": 1, "channel": "stable", "available": False},
            released_at="2026-11-11T01:00:00Z",
        )

    def build(self, **changes):
        return build_stable_manifest(**(self.args | changes))

    def test_initial_channel_must_explicitly_be_inactive(self):
        data = self.build()
        self.assertTrue(data["available"])
        self.assertEqual("com.navtasks.app.glass", data["applicationId"])
        self.assertEqual(31, data["minSdk"])
        self.assertEqual(169, data["versionCode"])
        self.assertEqual(165, data["minSupportedVersionCode"])
        self.assertEqual(hashlib.sha256(self.apk.read_bytes()).hexdigest(), data["apk"]["sha256"])
        self.assertEqual(self.apk.stat().st_size, data["apk"]["sizeBytes"])
        self.assertEqual("a" * 64, data["apk"]["signerSha256"])
        self.assertEqual("https://github.com/sergbokarev95-arch/NavTasks-Updates/releases/download/v1.0.0/NavTasks_1.0.0.apk", data["apk"]["url"])

    def test_refuses_any_beta_rc_and_bad_versions(self):
        for name in ("1.0.0-beta", "0.4.142-rc1", "1.2", "1.0.1/../../file", "v1.0.0", "01.0.0"):
            with self.subTest(name=name), self.assertRaises(ValueError):
                self.build(version_name=name)

    def test_refuses_recovery_and_downgrade_by_version_code(self):
        previous = {"schemaVersion": 1, "channel": "stable",
                    "available": True, "versionCode": 169}
        for code in (167, 168, 169):
            with self.subTest(code=code), self.assertRaises(ValueError):
                self.build(version_code=code, previous=previous,
                           min_supported_version_code=165)
        self.assertEqual(170, self.build(version_code=170, previous=previous)["versionCode"])

    def test_compatibility_floor_is_explicit_and_bounded(self):
        for floor in (0, 170, -1, True):
            with self.subTest(floor=floor), self.assertRaises(ValueError):
                self.build(min_supported_version_code=floor)

    def test_unauthorized_or_empty_metadata_is_rejected(self):
        with self.assertRaises(ValueError):
            self.build(signer_sha256="wrong")
        with self.assertRaises(ValueError):
            self.build(notes_ru="  ")
        with self.assertRaises(ValueError):
            self.build(previous={"schemaVersion": 1, "channel": "beta", "available": False})
        with self.assertRaises(ValueError):
            self.build(previous={"schemaVersion": 1, "channel": "stable", "available": "false"})
        self.apk.write_bytes(b"")
        with self.assertRaises(ValueError):
            self.build()

    def test_file_hash_function_matches_standard_sha256(self):
        self.assertEqual(hashlib.sha256(self.apk.read_bytes()).hexdigest(),
                         sha256_file(self.apk))


if __name__ == "__main__":
    unittest.main()
