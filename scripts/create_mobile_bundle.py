"""Export tracked source only; workflows are installed separately via GitHub's UI."""
import hashlib
import json
from pathlib import Path
import subprocess
import sys
import zipfile
from mobile_delivery import BUNDLE, MANIFEST, PRODUCT, validate_path

root = Path(__file__).resolve().parent.parent
output = Path(sys.argv[1]) if len(sys.argv) > 1 else root.parent / BUNDLE
source_commit = subprocess.check_output(["git", "rev-parse", "HEAD"], cwd=root, text=True).strip()
names = subprocess.check_output(["git", "ls-files", "-z"], cwd=root).decode().split("\0")
files = {}
for name in sorted(filter(None, names)):
    if name.startswith(".github/"):
        continue
    validate_path(name)
    files[name] = (root / name).read_bytes()
manifest = {
    "formatVersion": 1, "product": PRODUCT, "sourceCommit": source_commit,
    "files": {name: hashlib.sha256(content).hexdigest() for name, content in files.items()},
}
with zipfile.ZipFile(output, "w", zipfile.ZIP_DEFLATED) as archive:
    for name, content in files.items():
        archive.writestr(name, content)
    archive.writestr(MANIFEST, json.dumps(manifest, indent=2) + "\n")
print(output)
print(f"SHA-256: {hashlib.sha256(output.read_bytes()).hexdigest()}")
