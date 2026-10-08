# Current state

Current phase: **Phase 2 graph editor candidate (0.0.4)**.

Latest fully validated commit: `f305c5ddb17249fd5b446a68ebf6fd3f77795d8a`.
Latest validated Android CI: [37726479431](https://github.com/MavyyBlue/CreativeFN-WS/actions/runs/37726479431), success, including all nine Android tests.
Owner installed/launched that APK, accepted the player UX and requested the next phase.
These links describe the baseline, not the new graph candidate. Phase 2 requires
its own green CI and phone acceptance before completion.

Implemented: Kotlin/Compose API 29–37, Room, versioned typed catalog, contextual
information, persisted hideable bars, conditional searchable Recent sidebar,
Library/search, source importer and strict Android CI. New graph editor supports
placing Trigger/Tracker/Barrier, dragging nodes, one-finger pan, pinch zoom, Fit,
configuration sheets with typed controls, renaming/notes, duplication/deletion,
Event → Function port connections, binding list/removal, bounded undo/redo and
autosave. Invalid settings, endpoints and ports are rejected; intentional cycles
are allowed. The canvas uses one drawing layer; accessibility configuration actions
and the Placed device list provide alternatives to tapping nodes.

New Android-free graph-engine module owns immutable graph edits, history and
serialized save coordination. Explicit graph format 2 preserves devices, options,
UUID bindings and viewport. Existing empty format-1 drafts migrate on read without
losing metadata/notes. Room database schema remains 1 because its JSON blob column
is unchanged. Future formats/catalogs are rejected rather than silently coerced.

Supported simulated devices: **none**. Partial devices: **none**.
Reference-only devices: **Trigger, Tracker, Barrier**. Catalog schema 1/version
0.1.0-draft remains unverified; Fortnite reference version/date are null. Prototype
subsets/defaults are not fidelity claims. Simulation, trace/stepping, full static
diagnostics, folders, portable mechanic import/export, guides and overlay remain
unimplemented. This graph editor is a milestone toward the simulator, not the MVP.

Local validation (2026-10-08): full Gradle check, strict lintDebug, debug app and
instrumentation APK assembly passed; 24 JVM tests and 11 Python importer tests
passed. Both unchanged workflows pass actionlint. Ten Android tests compile,
including a new configure/connect/undo/reopen flow and populated Room durability.
Candidate run [37792525064](https://github.com/MavyyBlue/CreativeFN-WS/actions/runs/37792525064)
on `e2e6e6808433890d4559d8f2e4ff6eafe194d62d`: build passed, 9/10 Android
tests passed. Graph interaction progressed through configuration/bindings/undo/redo
but timed out matching the child graph-save tag. TextButton merges its label into
the parent; the repaired selector targets graph-save-button plus Saved and retains
visibility, recreation/reopen, binding-count and persisted-setting assertions.
No repaired Android pass is claimed; new CI remains required. Local software-emulator
installation previously timed out without hardware acceleration; no candidate
phone/performance results are claimed.

Known limitations: three reference devices only; configuration applies on Done
or a device action, dismissing the sheet discards uncommitted fields. History is
session-local and survives rotation, but not process death/reopening another
mechanic. Autosave is debounced 500 ms and flushed on navigation/background;
abrupt termination during an unfinished write can lose the most recent edit.
100-node/200-binding frame-rate, landscape/large-font touch behavior and full
process-recreation acceptance still need device measurements.

Next task: upload the source ZIP through the existing importer, obtain green
Android CI and verify touch/persistence on a phone. Fix observed Phase 2 failures
before advancing to Phase 3's deterministic runtime/golden Three-Key Vault.
Reference fidelity verification remains open. GitHub write integration previously
returned 403; the owner's browser upload remains the proven delivery path.

Post-repair local full Gradle check/lintDebug/app and test APK assembly passed;
24 JVM tests, 11 importer tests, workflow syntax and APK signature checks passed.
Repaired Android execution remains pending a new CI run.
