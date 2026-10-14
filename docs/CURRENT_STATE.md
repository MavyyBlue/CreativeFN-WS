# Current state — CreativeFN WS (2026-10-13)

## Editor declutter source candidate — 2026-10-13

Live `main` audited at `a6384c23e3a1cc1725688baa42e792444b5ff8ce`. Android workflow [run 38490221585](https://github.com/MavyyBlue/CreativeFN-WS/actions/runs/38490221585) passed on this commit, and the owner confirms the Phase 5.1 health check is green and phone-accepted. This is the **new certified predecessor**, superseding the Phase 5.1 pending status below. The current editor/navigation refactor is **source-only and requires a NEW Android CI and phone check**.

- Replace the standard bottom navigation with a compact four-card, angled Cover Flow-inspired in-app bar, still controlled by the saved Show navigation preference. The Editor omits the bottom bar entirely, leaving the canvas full-height under its existing title/header.
- Remove the old permanent editor toolbars. A small top-left Tools toggle reveals Devices, Bindings, Fit and Simulate in a fan; choosing an option collapses the fan. While simulating, the same anchor returns to Design. The right tool rail retains Undo, Redo, Save/Saved and Phase 5.1 Check, with status and binding prompts only when needed.
- Move the existing Blueprints entrypoint from a global floating button into the Library, immediately after New Mechanic. This preserves the chooser, `.c1logic` import/export, guides, checklists and overlay permission/launch functions.
- Relocate View options into the header; when the header is toggled off, a recovery button remains available. The navigation bar remains independently togglable.
- No changes to Room schema, graph/doc formats, device catalog, simulation rules, event-function IDs, overlay foreground service, permissions, signed workflow, Gradle dependencies or importer.
- Updated Android UI tests cover collapsed controls, tool actions, simulator exit, Library Blueprints entry, preferences and existing editing workflows. Tests have not yet executed in GitHub CI for this candidate.

Phone acceptance after green CI: confirm edge-to-edge canvas in portrait and landscape, tool fan fits small screens, pan/pinch outside fixed buttons, press each fan option, Undo/Redo, Save, Check, full Library Blueprints/Overlay journey, hide/restore header/nav, and verify floating Fortnite overlay independently unchanged.


## Latest certified Android + phone baseline

GitHub `main` was inspected at **`9a05ebbd84f1b17bca8fe454ec25bff02e3ffd35`**. GitHub Actions Android run [38474555365](https://github.com/MavyyBlue/CreativeFN-WS/actions/runs/38474555365) passed both `build` and `launch-smoke`. The owner installed the corresponding APK and confirmed on an actual Android phone, over Fortnite, that the compact Cover Flow navigator and **inline expanding, scrollable graph nodes** work. This supersedes earlier candidate/pending descriptions throughout repository history.

The working base includes eight reference devices, Room schema 1, graph document format 2, `.c1logic` exchange, deterministic Phase 3.2 prototype testing, saved blueprints/checklists, named folders, one-hop Connection Focus, an Android foreground-service overlay, and orientation-specific panel geometry. The device catalog is `0.2.0-draft`. Simulation remains limited and **not Fortnite 1:1 certified**. Do not infer new certification for modified code from the baseline run.

## Phase 5.1 diagnostic health check — subsequently CI- and phone-accepted

This source package adds a read-only `GraphDiagnostics` pass that examines the graph without changing it. It provides stable codes, error/warning/info severity, explanation, concrete repair advice and relevant device IDs. It flags missing or invalid options, unknown device/option/ports, missing link endpoints, duplicate link identities/meanings, blank HUD messages, dormant Button/Trigger devices, unassigned Tracker progress, Tracker target zero, unsupported sandbox Tracker sharing/HUD and Barrier phase modes, and possible event feedback loops. A single scope notice explains that game-fidelity has not been verified.

The Android graph editor receives a compact nonintrusive **Check mechanic** indicator. Tap to see findings and tap **Open device** to change the relevant saved configuration. Generated construction guides also carry actionable warning/error messages so builders see the same problems during rebuild. A new pure-JVM regression suite covers healthy/corrupted graphs, fanout, cycles, dormant devices, tracker issues, simulator limitations, guide propagation and long acyclic graphs.

The entire slice preserves Room/database version, graph/JSON formats, device IDs, catalog definitions, simulation execution, overlay code, UI navigation, Android manifest/permissions, build dependencies and signing infrastructure. This package does **not** expand the catalog or claim to verify Fortnite behavior.

## Phase 5.1 historical pre-certification validation

The repository is not locally cloned because outbound GitHub access is unavailable in the build container. Live source was audited through the connected GitHub repository API. A targeted standalone `kotlinc` harness may compile the new diagnostics against structurally equivalent model stubs, but does not substitute for a Gradle multi-module build or Android Lint. The source-import manifest and checksum validation run locally against `scripts/mobile_delivery.py` before handoff.

**Resolved:** Phase 5.1 source import and Android build/launch-smoke subsequently passed at `a6384c23` (run `38490221585`); the owner confirms a successful phone check. The current editor declutter candidate remains unverified. Signing-certificate compatibility must still be checked for any newly installed APK.

## Known boundaries and next work

The prototype only accepts Tracker `sharing=All`, `showOnHud=Off`, `targetValue>=1` and Barrier `enabledDuringPhase=Always/None`, even though the editor can configure other documented choices. The health check informs rather than silently normalizes these. An event-cycle warning is conservative, not proof that a loop will occur; actual event depth, action and queue caps are unchanged. Native Fortnite physics, real multiplayer, HUD and world effects require actual Creative island verification.

Next: phone validation of the health-check control, selective Epic-documented port/setting corrections backed by observed Creative 1.0 fixtures, practical trace explanations, broader graph-scale testing, and Phase 9 lifecycle profiling. The two owner-provided Creative 1.0 research archives are research/reference inputs, **not** live Fortnite validation; their proposed Channel, Switch, Timer and other devices are deliberately not registered in this slice.
