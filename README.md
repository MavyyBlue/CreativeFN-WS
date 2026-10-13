# Creative Logic Mobile

Native Android companion for builders of Fortnite Creative 1.0. Prototype device
logic locally, then use recorded settings and bindings to rebuild it manually.
This project is independent of Epic Games and does not connect to Fortnite.

**Current app: Phase 2 graph editor (0.0.4), with a Phase 3 JVM runtime-core
candidate.** Create local mechanics,
place and configure Trigger/Tracker/Barrier, connect Events to Functions, move
nodes, pan/zoom, undo/redo and reopen saved graphs. Home starts with New Mechanic;
contextual information, hideable bars and Recent keep controls compact.
All devices remain Reference Only; a new explicitly experimental JVM engine
provides virtual-time Trigger/Tracker/Barrier tests but is not wired to a working
simulator screen. The graph editor passed Android CI 38311622832; the new
runtime candidate requires a separate green CI. Phone acceptance remains a
separate gate. The overlay and full Three-Key Vault are not yet available.
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

## Signing and installation safety

A permanent signing certificate has been established and backed up privately;
see docs/CURRENT_STATE.md. Do not regenerate it or place keys/secrets in this
repository. The existing Android CI still produces debug-signed APKs; the
installed debug app may have an incompatible signing identity. Source-only
runtime changes do not require installation. Plan verified release signing and
data-safe install continuity separately before any replacement.

## Phone upload and APK download

Use the [mobile delivery guide](docs/MOBILE_DELIVERY.md): install the two workflow
files once, upload the source ZIP at repository root, then extract the APK from the
successful Android run’s GitHub Actions artifact. A workspace path or temporary
external APK URL is not the supported mobile distribution flow.


**Phase 3.2 prototype candidate:** The graph editor now has a local **Simulate** test scene with manual external actions, collapsible output and five more Epic-documented reference devices. These are **not yet Fortnite-fidelity-certified**, and running the simulator never reads or controls the Fortnite app. See `FORTNITE_FIDELITY.md` for supported/unsupported behavior. Existing save data uses a backwards-compatible catalog migration; the device remains offline.
