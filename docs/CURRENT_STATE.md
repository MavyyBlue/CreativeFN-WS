# Current state — 2026-10-12

## Authoritative repository baseline

Branch `main`: `105ce159ce23e939d51695903e17337df9cce89f`
(as confirmed against GitHub before this new source bundle).
Android [run 38311622832](https://github.com/MavyyBlue/CreativeFN-WS/actions/runs/38311622832)
PASSED both build and emulator `launch-smoke` jobs at that SHA. The historical
Phase 2 save-button selector failure is repaired and no longer blocks CI.
Owner has an older working 0.0.4 app installed. This latest candidate has **no
recorded owner phone acceptance** for all graph gestures; do not replace or
uninstall the installed app without deliberate upgrade/signature/data planning.

## Implemented and pending

Phase 0 foundation: native Kotlin/Compose Android API 29–37, Room database schema 1,
CI build and emulator checks, source-ZIP importer. Phase 1: versioned typed
prototype catalog (Trigger, Tracker, Barrier); fidelity to Fortnite unverified.
Phase 2: persisted semantic graph format 2, canvas placement/drag/pan/zoom,
settings, Event → Function bindings, undo/redo, debounce/flush save, local drafts,
searchable Library and Recent. The latest CI passes; physical phone graph
acceptance and performance checks are outstanding.

**New source-bundle candidate** begins Phase 3: the Android-independent
`PrototypeSimulationSession` offers ordered virtual-time actions, runtime state,
trace, manual Trigger inputs, pause/step/resume/reset, deterministic bindings,
and bounded-loop detection. Eleven targeted runtime tests passed locally with a
Kotlin/JVM stand-in harness. A checked-in core Three-Key Vault trace verifies
three one-use Triggers reaching Tracker progress 3 and disabling Barrier.
**Remote CI for this source bundle has not run.** The complete golden scenario
needs HUD Message (not in the catalog), and user-facing simulation UI does not
yet exist. `SimulationAvailability.isImplemented` stays false.

All three devices remain `NOT_SIMULATED` / REFERENCE ONLY in the UI and catalog;
experimental test rules are not verified Fortnite mechanics. No storage schema,
permissions, app version (0.0.4), signing workflows or installed-user-data change.
Other roadmap items (expanded catalog, diagnostics, portable .c1logic library,
build guide and passive overlay) remain future milestones.

## Next acceptance step

Import `creative-logic-mobile-source.zip` using the existing phone-browser
GitHub source importer, and inspect both Android build and launch-smoke jobs for
the **new expanded commit SHA**. Do not install the generated debug APK yet;
it may use a different signing identity from the working installation.
After CI is green: implement a small, clearly labeled prototype Run/Step/Reset
screen on saved graphs, then HUD Message and the full golden trace in its own
bounded slice. Phone-only acceptance should check pan/pinch, settings, bindings,
save/reopen, rotation and data preservation without sacrificing the existing app.

## Signing boundary

The permanent certificate fingerprint is
`83:C5:00:19:5D:70:5A:AF:21:9B:66:D9:22:BB:D9:6D:72:ED:F1:FA:3D:A3:2D:09:13:35:D7:FB:F4:62:AC:34`.
Recovery validation [38310945846](https://github.com/MavyyBlue/CreativeFN-WS/actions/runs/38310945846)
succeeded. Signed-preview [38311876816](https://github.com/MavyyBlue/CreativeFN-WS/actions/runs/38311876816)
failed the APK certificate comparison. Cause remains unverified. No key or secret
is included in this bundle; signing repair is deferred unless it blocks an
accepted app delivery. Historical CI/debug builds do not guarantee in-place APK
updates. Retain the installed application and its local data.
