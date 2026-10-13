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

## Lint repair after first blueprint CI (2026-10-13)

The importer successfully expanded the first blueprint package into commit
`4db801a378b91ff05d3d62825fe8f50d0446fc68`. Android [run 38433588379](https://github.com/MavyyBlue/CreativeFN-WS/actions/runs/38433588379)
compiled the source and passed all 45 JVM/unit tests (1 app, 4 core-model, 8 device-catalog,
16 graph-engine including 2 blueprint tests, and 16 simulation-engine). It failed
at `:app:lintDebug` with nine findings in the newly introduced Android overlay and
blueprint entrypoint; `launch-smoke` was skipped. **That run was red, not certified.**

The follow-up ZIP changes only the reported lint issues: six KTX replacements
(SharedPreferences edit, URI parse and color parse), KTX-managed canvas transform,
a reused graph-node `RectF`, and the programmatic View's standard Context
constructor. Node lookup is also prepared outside `onDraw`, and device label
substrings are drawn without allocating temporary strings. Strict lint remains
enabled and signing, manifest permissions, Room schema, simulation rules and
catalog definitions are unchanged. This is a **new, unverified CI candidate**;
Android build/lint/emulator and physical Fortnite overlay acceptance remain open.

## Post-install owner test: Fortnite overlay confirmed; usability defects found (2026-10-13)

Follow-up source import commit `6efa6717592b4e0bf63a87bcc2f958b005baa3a8` has successful Android CI [38435563960](https://github.com/MavyyBlue/CreativeFN-WS/actions/runs/38435563960). Owner installed the APK and provided screenshots of the expanded real Android overlay above Fortnite in landscape: FN bubble was present; Steps, Devices and Links tabs worked. Owner reported an empty Graph tab and an oversized panel obstructing gameplay. This **does not** certify the full blueprint experience yet.

The next source-only candidate provides a compact quarter-screen landscape default, adjustable width and height, orientation-specific saved positions/sizes, a Tabs hide/show control, and a refitted graph with a visible device count and Fit graph button. Android render and pure Kotlin sizing regression tests are included. No backend or schema changes. CI, emulator graphic test and a repeat Fortnite phone check of this new candidate are pending; previous green evidence applies only to `6efa671`.

## Follow-up: interactive Links directory and Connection Focus (candidate, 2026-10-13)

Live GitHub `main` inspected at `ac754adfd1b44b5b6bc34413e109fa3af664d7d8`; the owner has now confirmed that the compact Graph view renders two device nodes and one wire above Fortnite in landscape, including adjustable window sizing. These are user-observed phone results, not new GitHub CI evidence for this candidate.

The new patch includes a 3-tab overlay (Steps/Links/Graph), per-node option and direct binding inspector, one-hop visual long-press focus cleared by a blank-space double-tap, grouped device explorer with named folders, folder rename/ungroup, and portable v1 folder metadata as an optional field of the existing format-2 mechanic JSON. Existing Room schema, simulator, catalog, Android permissions and signing workflows are unchanged. Graph editor saves merge latest folder metadata to avoid accidentally deleting groups after returning from Fortnite. Regression tests cover adjacency non-transitivity and folder round-trip/validation. The compact baseline at `ac754adf` passed Android CI run `38441055829` and its overlay graph was observed working in Fortnite. GitHub Android build/lint/emulator and physical-device gesture/keyboard acceptance are still required for this **new** source candidate.
