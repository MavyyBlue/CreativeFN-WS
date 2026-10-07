"""Creative Logic Mobile's source-only ZIP contract and importer."""
import hashlib
import json
from pathlib import Path, PurePosixPath
import stat
import zipfile

BUNDLE = "creative-logic-mobile-source.zip"
MANIFEST = "creative-logic-mobile-bundle.json"
PRODUCT = "Creative Logic Mobile"
MAX_SOURCE_BYTES = 16 * 1024 * 1024
MAX_ENTRIES = 1000
REQUIRED = {"settings.gradle.kts", "app/build.gradle.kts", "gradlew", "docs/CURRENT_STATE.md"}


def validate_path(name):
    path = PurePosixPath(name)
    if (not name or "\\" in name or ":" in name or path.is_absolute()
            or any(part in ("", ".", "..") for part in name.split("/"))):
        raise ValueError(f"Invalid source path: {name}")
    if path.parts[0] in {".git", ".github", ".gradle", ".kotlin"}:
        raise ValueError(f"Protected source path: {name}")
    if ("build" in path.parts or name == "local.properties"
            or name.endswith((".apk", ".aab", ".jks", ".keystore", ".p12"))):
        raise ValueError(f"Not portable source: {name}")
    return path


def apply_bundle(bundle, root):
    """Validate the entire source overlay before writing any destination file."""
    root = Path(root).resolve()
    with zipfile.ZipFile(bundle) as archive:
        entries = archive.infolist()
        names = [entry.filename for entry in entries]
        if (len(entries) > MAX_ENTRIES or len(names) != len(set(names))
                or sum(entry.file_size for entry in entries) > MAX_SOURCE_BYTES):
            raise ValueError("Bundle size/entry/duplicate limit exceeded")
        if MANIFEST not in names:
            raise ValueError("Missing Creative Logic source manifest")
        for entry in entries:
            mode = entry.external_attr >> 16
            if entry.is_dir() or (stat.S_IFMT(mode) not in (0, stat.S_IFREG)):
                raise ValueError(f"Not a regular source file: {entry.filename}")
            validate_path(entry.filename)
        manifest = json.loads(archive.read(MANIFEST))
        if manifest.get("formatVersion") != 1 or manifest.get("product") != PRODUCT:
            raise ValueError("Unsupported source bundle contract")
        files = manifest.get("files")
        if not isinstance(files, dict) or set(files) != set(names) - {MANIFEST}:
            raise ValueError("Source manifest does not match ZIP members")
        if not REQUIRED.issubset(files):
            raise ValueError("Missing foundation source files")
        pending = []
        for name, digest in files.items():
            relative = validate_path(name)
            target = root.joinpath(*relative.parts)
            # Reject existing symlinks, even when they currently point within the repo.
            cursor = root
            for part in relative.parts:
                cursor = cursor / part
                if cursor.is_symlink():
                    raise ValueError(f"Symlink destination: {name}")
            if root not in target.resolve().parents:
                raise ValueError(f"Source escapes checkout: {name}")
            if target.exists() and not target.is_file():
                raise ValueError(f"Source destination is not a file: {name}")
            content = archive.read(name)
            if hashlib.sha256(content).hexdigest() != digest:
                raise ValueError(f"Source checksum mismatch: {name}")
            pending.append((target, content, name == "gradlew"))
        for target, content, executable in pending:
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_bytes(content)
            target.chmod(0o755 if executable else 0o644)
    return len(pending)
