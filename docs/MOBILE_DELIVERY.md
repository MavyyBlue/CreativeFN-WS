# Mobile source upload and APK download

This adapts the delivery structure of MavyyBlue/Local-Yuki, inspected at commit
`8e075d36827287e09f199e6a8ac4f6eb8d6dad39`. Its successful Android Actions run
37559206413 follows a successful source-import run. This project uses its own
manifest, importer and tests; no Local Yuki code, signing material or app integration
is reused.

## One-time setup on GitHub

The connected integration currently cannot publish repository/workflow changes:
GitHub returns HTTP 403. Browser commits under the owner's account are independent
of that integration. Nothing below requires a token or password to be pasted here.

1. Extract `creative-logic-mobile-delivery.zip` on the phone.
2. In `MavyyBlue/CreativeFN-WS`, create `.github/workflows/android.yml` using the
   supplied file's contents. Commit on main.
3. Create `.github/workflows/mobile-source-import.yml` using the other supplied
   workflow file. Commit on main. Workflows belong at these exact paths.
4. GitHub → repository root → Add file → Upload files. Upload the inner
   `creative-logic-mobile-source.zip` as-is and commit on main. Do not upload the
   outer delivery ZIP, any APK, signing material or an extracted source directory.
5. Actions → **Creative Logic Mobile source import**. Wait for success and note its
   expanded commit. The Android workflow runs after the import workflow completes.
6. Actions → **Android**. Confirm its tested source matches the imported commit and
   both build and launch-smoke jobs succeed. The smoke job checks out the build's
   exact SHA. An unrelated later run does not certify a different source bundle.
7. On that run's page, open **Artifacts → creative-logic-mobile-debug.apk**.
   GitHub downloads artifacts as a ZIP. Extract it in Android's Files app, then open
   the enclosed `creative-logic-mobile-debug.apk` to install. The ZIP is not an APK.

Artifact contents include the APK, its SHA-256, tested commit and signing certificate.
This early build uses Android debug signing, not a stable release key. Different
fresh CI runners can generate different debug keys, so APK update continuity is not
promised yet. Production release signing remains the separately planned v* workflow.

If Android reports an installation block, enable installation from the browser/Files
app used to open the APK. Android 10/API 29 or newer is required. Installation or
phone acceptance is only passed after the APK actually launches on the device.

## Later source uploads

Upload a new `creative-logic-mobile-source.zip` at the repository root. The importer
checks product/version, required files, file checksums, total size, duplicates and
regular-file paths before overlaying. It protects Git/workflow paths, rejects
symlinks/traversal/generated APKs/keystores and preserves unrelated existing files.
It removes the uploaded wrapper, commits the expanded source, and records that SHA.
The importer has no deletion mechanism in format 1.

Export tracked source locally:

```sh
python3 scripts/create_mobile_bundle.py /path/to/creative-logic-mobile-source.zip
python3 -m unittest discover -s testing -v
```

Workflow files are excluded from source bundles and must be installed separately.
If their contents change later, install those reviewed changes through GitHub too.
The importer embeds the tested Python implementation so it can bootstrap an empty
repository containing only README and the workflows.
