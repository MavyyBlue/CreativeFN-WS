# Current state — blueprint and Android overlay source candidate, 2026-10-13

## Authoritative prior certified baseline

`main` commit `1bcc006427d8bbdc88a926119f49c9f76398d316` successfully completed GitHub Actions Android run [38338236226](https://github.com/MavyyBlue/CreativeFN-WS/actions/runs/38338236226), including `build` and `launch-smoke`. The owner installed and accepted the Phase 3.2 simulator on an actual Android phone. Existing Room schema 1, device IDs, graph document format 2, catalog version 0.2.0-draft and fixed signing infrastructure remain intact.

## New, NOT YET CERTIFIED source candidate

- Adds `BlueprintGenerator` as a pure JVM read-only generator: devices and counts, per-device saved options, direct Event → Function labels, steps, warnings, and game-fidelity caveats. Pure JVM unit tests cover source integrity, bindings and empty graphs.
- Adds an in-app Blueprints access point to existing searchable mechanics, a full-screen construction guide with saved checklist progress and user-started overlay entrypoint.
- Adds local `.c1logic` export/import via the Android Storage Access Framework. Imports get new mechanic IDs and do not overwrite existing documents. Format is existing `MechanicGraphCodec` version 2 JSON; old catalog 0.1.0-draft documents remain migrated by existing decoder. Imported input limit 2 MB; no file/storage/network permissions.
- Adds Android `TYPE_APPLICATION_OVERLAY` with a nonexported special-use foreground service; permission obtained only by choosing Overlay. Notification offers Stop. Bubble can drag, expand/collapse; Steps/Devices/Links/Graph tabs share snapshot from the same saved document; graph has touch zoom/pan. Positions/checklist/current step persisted; no background simulation or polling.
- Existing editor, Room schema, `.c1logic` wire, catalog and deterministic simulator are not replaced.

## Evidence and unverified boundaries

The above is **source-only** work requiring an importer validation, real Gradle Android build/lint/instrumentation, and actual phone + Fortnite overlay acceptance. This document intentionally does not assert success of any of those new gates. The development environment used to prepare the package cannot run the Android SDK build; check GitHub Actions after mobile upload. A functioning Android overlay cannot be inferred from passing JVM tests alone, and Fortnite or OEM policies can suppress third-party overlays.

Check install signing identity before replacing the working APK. Do not regenerate secrets or require an uninstall by default. New code has permissions limited to overlay and foreground service and does not access the game.
