# Creative Logic Mobile

Native Android companion for builders of Fortnite Creative 1.0. Prototype device
logic locally, then use recorded settings and bindings to rebuild it manually.
This project is independent of Epic Games and does not connect to Fortnite.

**Current candidate: Phase 2 graph editor (0.0.4).** Create local mechanics,
place and configure Trigger/Tracker/Barrier, connect Events to Functions, move
nodes, pan/zoom, undo/redo and reopen saved graphs. Home starts with New Mechanic;
contextual information, hideable bars and Recent keep controls compact.
All devices are Reference Only: simulation and overlay are future milestones.
Local checks pass; the candidate still needs its own green CI and phone acceptance.
This is not the completed MVP.

## Build

Use JDK 21, Android SDK platform `platforms;android-37.0`, Build Tools 37.0.0,
and the checked-in Gradle 9.6.0 wrapper. AGP 9.4.1 supports API 37. Kotlin 2.2.10
matches AGP's built-in Kotlin. Minimum Android is API 29; compile/target is API 37.
Set `ANDROID_HOME` or put `sdk.dir=/your/android/sdk` in ignored `local.properties`.

```sh
./gradlew check lintDebug assembleDebug assembleDebugAndroidTest
# With an API 29+ emulator or phone connected:
./gradlew connectedDebugAndroidTest
```

APK: `app/build/outputs/apk/debug/app-debug.apk`. GitHub Actions uploads the
`creative-logic-mobile-debug.apk` artifact; binaries are never committed.
The app uses managed Kotlin code and supports ARM64 without a native ABI restriction.

## Project boundaries

`core-model/`, `device-catalog/`, `graph-engine/` and `simulation-engine/` are Android-free JVM modules. Other planned
boundaries start as packages in `app/`; see [ARCHITECTURE.md](ARCHITECTURE.md).
Constructor injection supplies the repository. No DI framework, account, network
permission, ads, analytics or telemetry is used.

See [current evidence](docs/CURRENT_STATE.md), [roadmap](ROADMAP.md),
[testing](QA.md), [device contract](DEVICE_MODEL.md), [simulation](SIMULATION.md),
and [overlay boundary](OVERLAY.md).

## Release signing

Early builds use debug signing only. Before a `v*` release, create a dedicated
release keystore outside the repository, store its encoded bytes and credentials
in GitHub Actions Secrets, and add a tag workflow running tests, lint, release
build, signing and `apksigner verify` before uploading
`Creative-Logic-Mobile-v0.1.0.apk` to GitHub Releases. Preserve the certificate
across releases. No keystore or signing credential is stored in this project.

## Phone upload and APK download

Use the [mobile delivery guide](docs/MOBILE_DELIVERY.md): install the two workflow
files once, upload the source ZIP at repository root, then extract the APK from the
successful Android run’s GitHub Actions artifact. A workspace path or temporary
external APK URL is not the supported mobile distribution flow.
