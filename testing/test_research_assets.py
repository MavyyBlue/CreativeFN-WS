"""Check the Android picker's evidence maps against the actual catalog resource."""
import json
from pathlib import Path
import unittest


class ResearchAssetTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        root = Path(__file__).resolve().parents[1]
        cls.catalog = json.loads((root / "device-catalog/src/main/resources/creative1_reference_devices.json").read_text())
        cls.picker = json.loads((root / "app/src/main/assets/research_setting_choices.json").read_text())

    def test_picker_evidence_targets_existing_device_and_setting_ids(self):
        self.assertEqual(1, self.catalog["schema"])
        self.assertEqual(1, self.picker["schema"])
        records = {row["id"]: row for row in self.catalog["records"]}
        self.assertEqual(len(self.catalog["records"]), len(records))
        self.assertEqual(set(records), set(self.picker["records"]))
        for device_id, guidance in self.picker["records"].items():
            with self.subTest(device=device_id):
                keys = [key for key, _ in records[device_id]["settings"]]
                self.assertEqual(len(keys), len(set(keys)))
                self.assertTrue(set(guidance).issubset(keys), "Picker evidence references an unknown setting")

    def test_custom_entry_requires_a_supported_evidence_type(self):
        for device_id, guidance in self.picker["records"].items():
            for setting_id, row in guidance.items():
                with self.subTest(device=device_id, setting=setting_id):
                    # p retains the extraction's provenance note; it is not a picker choice.
                    self.assertTrue(set(row).issubset({"c", "e", "p"}))
                    if "p" in row:
                        self.assertIsInstance(row["p"], str)
                    self.assertIn(row.get("e", ""), ("", "number", "text"))
                    choices = row.get("c", [])
                    self.assertIsInstance(choices, list)
                    self.assertTrue(all(isinstance(choice, str) and choice.strip() for choice in choices))
                    self.assertEqual(len(choices), len(set(choices)))


if __name__ == "__main__":
    unittest.main()
