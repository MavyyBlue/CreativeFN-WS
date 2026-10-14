# Research catalog emulator regression: targeted selector repair (2026-10-13)

Source baseline: live GitHub `main` `5ffcc894194a045b2b3df17444dc14c6bbb771b0`, imported from the broad research/value-signal bundle. Android Actions [run 38499156610](https://github.com/MavyyBlue/CreativeFN-WS/actions/runs/38499156610) **failed overall**, despite the **build** job (JVM tests, strict Android Lint and APK assembly) passing. Emulator `launch-smoke` executed 17 instrumentation tests: **16 passed, 1 failed**. The downloaded `launch-smoke-reports` artifact identifies `ValueSignalUiTest.researchDeviceHasManualEventActionWithoutChangingOriginalScene` failing at its `Mutator Zone` selection: Compose matched both the editable `Search devices` field (containing the query) and the device card. This is an ambiguous test selector, not an observed simulator failure. The original runtime portion of that single test was not reached.

This source-only repair updates **only that instrumentation matcher** to select a clickable, non-editable device entry. All production Kotlin, original and new device definitions, simulation rules, Room data, graph formats, Android UI, floating overlay, permissions, signing, Gradle versions, and workflows remain byte-for-byte unchanged. The package also records the failed evidence in docs and carries the four unmodified importer-required foundation paths. The failed CI run does **not** certify the new 208-device expansion.

New acceptance gate: import the corrective ZIP, verify the expanded source SHA, and require BOTH Android `build` and `launch-smoke` green on that SHA, with 17/17 emulator tests (or the full actual suite count). Then verify signing compatibility and test device search, Value Test, save/reopen and the floating overlay on the owner's Android phone. This patch is locally ZIP/importer-validated but the corrected selector has **not** been executed on an Android emulator yet.

---

# Current state — CreativeFN WS (2026-10-13)

## Research catalog + manual value signals — source candidate, 2026-10-13

**Verified predecessor:** live `main` `b531f0b70215b5bb7a445f2c9c35c89d515d6bc9`, Android [run 38495461646](https://github.com/MavyyBlue/CreativeFN-WS/actions/runs/38495461646) green; owner reports the full-height graph, tool fan, Cover Flow app navigation and Library Blueprints work on a real phone. The *new research/device/value-testing change is not yet CI or phone certified*.

- Adds **200 reference-only Creative documentation device definitions** to the existing eight, built from the owner's `CreativeFN-Device-Research.zip` `creative1_device_catalog.json` (scope classified `CREATIVE_DOCUMENTATION_SCOPE`, excluding 3 UEFN-only, 2 brand-restricted and 1 retired entry; four old device IDs and three existing devices lacking proposed IDs are preserved rather than duplicated). Totals: **208** catalog devices; the 200 new entries contain **3,745** extracted setting labels, **529** event ports and **978** function ports. Settings use explicitly unspecified free-text values, **not** inferred in-game defaults, types, or ranges; duplicates in upstream research are de-duplicated by stable proposed internal key. Event/function IDs and labels are research-derived, documented names; check them in-game before publication.
- Adds a **manual event-and-value simulation path** for graphs containing any research device. Actions chooses a placed device's documented event, actor, and free-form test value; Run/Step executes deterministic direct connections and shows input, event, function, value, unsupported and loop-limit traces. Supports existing Tracker completion and Barrier/Trigger/HUD/enable-disable values; Channel relay, Switch turn-on/off, narrow Timer start/pause/reset/explicit-complete, Teleporter request and End Game request; unmodeled real-world effects show UNSUPPORTED and can be followed by a user-declared output event. The original eight-only `PrototypeSimulationSession` and its golden traces are **unchanged**.
- Keeps the wire contract catalog version `0.2.0-draft` while only **adding** new internal device IDs and explicitly unspecified text options; no old option/port IDs or serialized types change, and existing `.c1logic`/Room records require no migration. Before promoting this unverified reference dataset to verified menus, use a separately versioned catalog migration rather than rewriting old saved values.
- Does **not** claim full current menu coverage, runtime fidelity or that every exposed function has simulated semantics. The first research archive has 207 documentation-scope records, not 207 verified Creative inventory devices. References include official Epic documentation URLs and recorded research provenance; live Creative checks remain necessary.
- No Android permission, service, overlay visual, Room schema, Gradle workflow, signing or importer change. Strict `warningsAsErrors` remains enabled. Standalone Kotlin harness: four value-engine tests passed; this is **not** Gradle/Android compilation. The Android instrumentation smoke added here awaits CI.

Next gate: source import SHA, GitHub Android build/Lint/JVM/launch-smoke, signing identity, and physical phone check of a Mutator Zone → Tracker → Barrier graph, new device search/configuration/save/blueprint/overlay and the manual event tester. Test long option sheets and large research catalogs for UI responsiveness.

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
