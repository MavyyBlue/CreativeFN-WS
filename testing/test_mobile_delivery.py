import hashlib
import json
from pathlib import Path
import stat
import sys
import tempfile
import unittest
import zipfile

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "scripts"))
from mobile_delivery import MANIFEST, PRODUCT, REQUIRED, apply_bundle


class MobileDeliveryTests(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name) / "repo"
        self.root.mkdir()
        self.bundle = Path(self.temp.name) / "source.zip"
        self.source = {name: b"source" for name in REQUIRED}
        self.source["README.md"] = b"Creative Logic Mobile"

    def write_bundle(self, source=None, product=PRODUCT, override=None):
        source = self.source if source is None else source
        manifest = {"formatVersion": 1, "product": product,
                    "files": {name: hashlib.sha256(content).hexdigest() for name, content in source.items()}}
        if override:
            override(manifest)
        with zipfile.ZipFile(self.bundle, "w") as archive:
            for name, content in source.items():
                archive.writestr(name, content)
            archive.writestr(MANIFEST, json.dumps(manifest))

    def reject(self):
        with self.assertRaises(ValueError):
            apply_bundle(self.bundle, self.root)
        self.assertFalse((self.root / "settings.gradle.kts").exists())

    def test_complete_overlay_preserves_unrelated_files_and_workflows(self):
        protected = self.root / ".github/workflows/owner.yml"
        protected.parent.mkdir(parents=True)
        protected.write_text("unchanged")
        (self.root / "owner-notes.txt").write_text("keep")
        self.write_bundle()
        self.assertEqual(len(self.source), apply_bundle(self.bundle, self.root))
        self.assertEqual(b"source", (self.root / "settings.gradle.kts").read_bytes())
        self.assertEqual("unchanged", protected.read_text())
        self.assertEqual("keep", (self.root / "owner-notes.txt").read_text())
        self.assertTrue((self.root / "gradlew").stat().st_mode & stat.S_IXUSR)

    def test_parent_traversal_is_rejected_before_writes(self):
        self.write_bundle({**self.source, "../outside": b"no"})
        self.reject()

    def test_workflow_replacement_is_rejected(self):
        self.write_bundle({**self.source, ".github/workflows/android.yml": b"no"})
        self.reject()

    def test_generated_apk_is_rejected(self):
        self.write_bundle({**self.source, "app-debug.apk": b"no"})
        self.reject()

    def test_checksum_mismatch_is_rejected_before_writes(self):
        self.write_bundle(override=lambda value: value["files"].update({"README.md": "bad"}))
        self.reject()

    def test_wrong_product_is_rejected(self):
        self.write_bundle(product="Another application")
        self.reject()

    def test_missing_foundation_is_rejected(self):
        self.write_bundle({"README.md": b"incomplete"})
        self.reject()

    def test_duplicate_zip_member_is_rejected(self):
        self.write_bundle()
        with zipfile.ZipFile(self.bundle, "a") as archive:
            archive.writestr("README.md", b"duplicate")
        self.reject()

    def test_symlink_destination_is_rejected(self):
        (self.root / "docs").symlink_to(Path(self.temp.name), target_is_directory=True)
        self.write_bundle()
        self.reject()

    def test_symlink_archive_member_is_rejected(self):
        self.write_bundle()
        with zipfile.ZipFile(self.bundle, "a") as archive:
            entry = zipfile.ZipInfo("link")
            entry.create_system = 3
            entry.external_attr = (stat.S_IFLNK | 0o777) << 16
            archive.writestr(entry, "../outside")
        self.reject()

    def test_bootstrap_embeds_the_tested_importer(self):
        root = Path(__file__).resolve().parents[1]
        workflow = (root / ".github/workflows/mobile-source-import.yml").read_text()
        embedded = workflow.split("        run: |\n", 1)[1]
        embedded = "\n".join(line[10:] for line in embedded.splitlines()) + "\n"
        self.assertTrue(embedded.startswith((root / "scripts/mobile_delivery.py").read_text()))
        compile(embedded, "bootstrap importer", "exec")


if __name__ == "__main__":
    unittest.main()
