## 2026-10-17 — Imported scope update and actual HEAD smoke failure

The scope-clarification ZIP is **already imported** at `b820712ca98809b22d5e8d643fb8db750f433273`; older “unimported candidate” sections refer to earlier handoffs. Android [39062306268](https://github.com/MavyyBlue/CreativeFN-WS/actions/runs/39062306268) **build PASS; launch-smoke FAIL**. Downloaded report artifact `12111523054`: **19 tests, 1 failure, 0 skipped**. Emulator startup and instrumentation ran. ResearchSettingUiTest failed at line 31 inside `performScrollTo()`: the setting node was no longer in the semantics tree, before opening the dialog. This is not an unverified infrastructure diagnosis.

The new source-only correction scrolls through the stable config LazyColumn, refetches the setting button, asserts visibility/click action and invokes its actual semantics OnClick. It still requires the real dialog, choice selection, graph readback after reopening and clearing; no assertion or test is removed. This does not certify touch-coordinate scrolling. New Android execution is pending after import; no production behavior, saved-data contract or workflow changed.

Evidence annotations stay in the separate [device evidence ledger](DEVICE_EVIDENCE_LEDGER.md) pending review of actual findings; no production JSON/schema change. Lyra's Ball Spawner independent official-doc verification is next, followed by Movement Modulator and Input Trigger. Missing nonessential menus remain optional research.

A ZIP-only extraction lacks the deliberately excluded `.github` workflow. The existing bootstrap test checks that workflow's embedded importer and therefore needs a full repository checkout with the ZIP applied. Do not skip the check, package protected workflows, or mislabel a missing-file error as a product regression. Python checks and importer validation must be recorded separately from Android CI.

**Local validation for this new candidate:** 13/13 Python tests passed both in the working checkout and after applying all 134 packaged files onto a full repository copy, including the bootstrap check. ZIP checksums/source bytes match. Android Gradle was attempted again but downloads.gradle.org did not resolve; no new Android execution result.

---

## 2026-10-17 — Essential device communication scope (supersedes exhaustive-menu gates)

The owner clarified that device support means **accurate purpose, actual incoming Functions/outgoing Events, and only settings essential to blueprint intent or communication conditions**. Full-menu/default/contextual parity and physical gameplay reproduction are not required. The 60 missing picker domains and six no-settings records are optional research gaps unless they affect that contract. The former full capture batch is withdrawn as a prerequisite. Preserve existing settings/IDs and saved blueprints.

A receiver-only device is a valid endpoint. A Function requesting an effect does not create an outgoing Event or prove the physical result. Report structural validity, signal delivery and relevant configuration support separately from unknown gameplay. The owner's Ball Spawner example illustrates this boundary; this update adds no unverified port or gameplay behavior.

Latest live source `256d99f223a750c1cc3eada47032447154caec24` includes the previous ZIP. Android [39060899455](https://github.com/MavyyBlue/CreativeFN-WS/actions/runs/39060899455): **build PASS; launch-smoke 18/19 passed, 1 failure, 0 skipped**. Downloaded artifact `12111487409` identifies ResearchSettingUiTest: picker list absent after attempted opening. This new, unimported candidate scrolls the setting button into view and waits for the actual dialog before interacting; all selection/readback/clear assertions remain. New Android CI is pending. The clarification is documented direction, not an implemented profile/checker rewrite.

Older entries below are historical; this scope and the canonical charter take precedence.

---

## 2026-10-17 — Live HEAD audit and lazy-menu repair candidate

Audited GitHub `main` at `3a0b9bc14e2c56553a095f9854786fa7226f8295`. Latest Android [run 38696750855](https://github.com/MavyyBlue/CreativeFN-WS/actions/runs/38696750855) completed: **build PASS**, **launch-smoke FAIL**. Downloaded `launch-smoke-reports` artifact `11955191750` contains **18 tests, 2 failures, 0 skipped**. `SimulatorSceneTest` could not find Button in the lazy catalog; `ResearchSettingUiTest` could not find Pre-Game Only in the lazy picker. The earlier missing import/callback errors are repaired on this HEAD; `ValueSignalUiTest` passed. This is an app-test failure, not an emulator provisioning failure.

This **unimported source candidate** searches for the Button card, explicitly scrolls the setting-choice list, keeps existing simulator assertions, and adds selection/readback/clear assertions. Opening a picker dismisses the settings-search keyboard; longer finite-choice lists gain a search field. New UI regression source covers search/select/clear without allowing arbitrary text for finite choices. Research limitations text now accurately describes selectors instead of generic free-text notes.

Actual source audit: **208 stable entries, 207 distinct display names** (legacy `hud-message` and research `hud_message` both display HUD Message). The original eight expose **20 settings, 7 events, 19 functions**; 200 research entries expose **3,745 settings, 529 events, 978 functions**. All 200 reference IDs and setting keys match the Android picker evidence. **3,685** rows have choice/input guidance, **60** lack it, and six records contain no captured settings. Neither the larger documentation-audit counts for the original eight nor catalog presence proves these options/ports are all implemented. Defaults, contextual filtering, complete menus and current availability remain unverified. See [device support status](DEVICE_SUPPORT_STATUS.md) for exact boundaries and the next focused capture batch.

Validation: **13 Python importer/asset tests passed**; source diff and exported ZIP/importer checks are recorded in the handoff. Full local `./gradlew --no-daemon check lintDebug assembleDebug assembleDebugAndroidTest` could not start because `downloads.gradle.org` did not resolve. The candidate has **no new Android compile/Lint/JVM/emulator result** and is not committed to GitHub. Require both Android jobs on the newly imported SHA. No owner installation request until device support is meaningfully ready. Room, wire formats, option/port IDs/defaults, signing, overlay service, simulator engines and protected workflows are unchanged.

---

## 2026-10-15 — Android CI compile repair (pending re-run)

**Current imported source:** GitHub `main` `a8fcb83bb4d8d244f358c49c4554bac314c322d4`.
[Android run 38674385383](https://github.com/MavyyBlue/CreativeFN-WS/actions/runs/38674385383): source importer and Python importer tests passed; core-model, device-catalog, simulation-engine, and graph-engine Kotlin compilation and JVM tests passed. Android `:app:compileDebugKotlin` failed on two app UI source mistakes: an unimported Compose `horizontalScroll` modifier in `DevicePanels.kt`, and calls using trailing lambdas where a local `apply(action, restartPlayback = false)` function expects a Boolean as its final formal argument, in `ValueSignalSimulatorOverlay.kt`. Therefore APK verification and the Android `launch-smoke` job did not run.

**Corrective source bundle:** add the exact Compose foundation import and pass explicit named `action = { ... }` lambdas at every affected call site. Also make the new research-setting Android UI smoke find a large, lazily-rendered setting using the already-present search control before selecting it, without skipping or weakening the actual selection/assertion. Existing device metadata, simulation engines, graph files, signing, overlay service, Room schema, and source-of-truth document are **unchanged**. The corrective ZIP is importer/hash validated locally; Android Kotlin compilation, strict Lint, device instrumentation and phone acceptance are **not yet re-verified** and must pass in a subsequent source-imported commit. The prior successful run 38574784317 predates this candidate and cannot certify it.

---

> **Source of truth:** [`PROJECT_SOURCE_OF_TRUTH.md`](PROJECT_SOURCE_OF_TRUTH.md) governs product scope, device-fidelity standards, staged-versus-imported code, and context resets. This file is a chronological record and includes superseded status statements. Its newest source candidate is **not** automatically implemented in GitHub merely because it was packaged.

## 2026-10-14 — Comprehensive device menu audit and scalable configuration editor (source candidate, **not** full-device certification)

- User requirement: do not request phone testing until every Creative 1.0 device has a trustworthy current menu/port inventory and all device configuration and blueprint operations work; distinguish blueprint coverage from gameplay simulation.
- Research archive cross-walk: 208 catalog devices (original eight + 200 research), 3,745 setting labels, 529 new event labels and 978 new function labels. Original archive has 213 records; six out-of-scope/retired/brand-scope are excluded, and the seven existing-device matches remain stable.
- Expanded the selector metadata from 153 to **200** reference records by parsing originally string-formatted documentation option expressions; **3,685 / 3,745** extracted setting fields now have at least one documented choice or a clearly typed "enter numeric/text" cue, and **60** still lack usable picker evidence. Six research entries have no documented settings in the source. No full live-menu verification has occurred; defaults, contextual filtering, and availability require in-game capture.
- New searchable device-config Settings / Events / Functions tabs render menu rows lazily, so devices with 50–122 settings do not eagerly construct all field controls. Existing eight device options preserve their types, values, IDs, names, serialized formats, and graph behavior. Device search also matches category, event and function descriptions.
- Added ``docs/CREATIVE1_DEVICE_VERIFICATION_QUEUE.md``, full 208-record CSV, and machine-readable audit JSON; prioritize screenshots of Switch, Timer, Mutator Zone, Item Granter, Conditional Button, then original eight/Player Counter/Prop Mover and other source gaps. See capture instructions in queue.
- **Not yet full support.** The parser treats every imported value as a research suggestion, NOT a verified Fortnite default. Do not release as fully supported, alter Room/wire schema, or claim universal simulator fidelity. The prior graph, overlay, signed CI, importer and model IDs were not changed.
- Validation pending: new candidate Android Gradle, strict Lint, connected test, physical phone test **after verification**; local importer contract/checksums can be checked now.

# 2026-10-14 — Device settings and event-signal visualization (new source candidate)

**Baseline:** live GitHub `main` `739a3cc082f2bcd572a12a75fcfbcce54f34bdd0`, Android workflow [38574784317](https://github.com/MavyyBlue/CreativeFN-WS/actions/runs/38574784317) green for build and launch-smoke. The owner reports the previous research/value experience is confusing; this update addresses that feedback. **This candidate has NOT passed GitHub Android CI or owner acceptance yet.**

**Product scope:** the workshop is FIRST a Creative 1.0 blueprint planner and Fortnite overlay guide. The simulator is an *optional Event → Function communication checker*, not Fortnite world/HUD/player physics replication.

1. **Selector-first research device settings.** Adds a packaged, offline, 2026-10-13 research-options lookup extracted from the owner-provided `CreativeFN-Device-Research.zip`; 153 documentation-scope candidate IDs have at least one eligible documented choice/control. The 200 existing research device definitions, their stable option IDs and saved `TextValue` formats, original eight typed settings, catalog version `0.2.0-draft`, document V2 and Room schema stay unchanged. A sheet shows documented categorical controls as pickers, explicit 'Unspecified' for unknown defaults, number/text entry ONLY for researched pick/enter-type settings. Rows with no documented choices are read-only rather than fabricated. Previously saved arbitrary text remains displayed until changed. This is *reference UI, not certified current Fortnite exact menu or typed defaults*.
2. **Do not reject valid blueprints merely because Fortnite's world is absent.** Original eight-only graphs still use the certified prototype if its narrow modeled assumptions apply. If valid settings require unmodeled Tracker sharing/HUD modes or Barrier game phases, the UI falls back to the direct Event → Function value checker. This preserves authentic builder configurations but does **not** claim to implement player/team permissions, HUD drawing, game stages or Fortnite effects. A successful link means a locally modeled binding and supported value operation, NOT a verified physical game outcome.
3. **Clearer outcomes.** Remove manual 'Event value' UI (the event itself is injected; its internal boolean signal is not user-supplied output). Derived value changes exclude internal lastFunction/lastInputValue audit fields from summary and state display. Unmodeled operations are distinguished from safety errors in trace color and messages. The complete trace remains available for inspection/export.
4. **First event visualization for BOTH simulator paths.** Read-only, trace-driven highlights lap around emitting/receiving nodes; a cyan signal travels along the matching saved cubic graph connection, and a fading device bubble shows the event, modeled state change or an amber unsupported notice. The effects do not alter the simulator queue, blueprint document or Fortnite overlay, and play only after Run/Step. Playback is bounded to 60 recent cues for Android performance. **Current limitation:** animation geometry uses the viewport captured on entering simulation; while a test is running, subsequent canvas pan/zoom can visually misalign effects, so reenter Simulate after adjusting the view. Aligning playback with live gesture viewport is a follow-up before calling VFX polished.

**Validation performed:** targeted pure Kotlin compilation and summary-count smoke, research source-inspection, ZIP importer/manifest hash validation. Full Android Kotlin/Compose compilation, `lintDebug`, JVM and Android instrumentation tests, signing continuity and phone acceptance remain pending on the new imported commit. Do not treat these local tests as Android green.

**Phone acceptance:** select a new Mutator Zone → its 'Enabled on Phase' picker has documented choices, not a free-text entry; leave an unknown setting Unspecified; save/reopen and verify settings survive. Original eight keep typed booleans/choices, named HUD text. Queue one documented event WITHOUT typing an output, Run → watch source outline / wire orb / target outline / temporary bubble, inspect details; confirm unsupported is not a false compatibility error. Check existing overlay, Cover Flow and saved blueprint construction. Try original legacy-only graph too.

---

# 2026-10-14 — Value-test clarity, replay and event search (new source candidate)

**Verified live source:** `main` at `b35e5d49a99fdcff74784f16ce6a3ac3b24b16f8` from source-import of the previous selector repair. GitHub Android [run 38526399064](https://github.com/MavyyBlue/CreativeFN-WS/actions/runs/38526399064) **build PASSED**, including Kotlin, JVM, strict Lint, and signed debug APK. **launch-smoke failed before starting instrumentation** because `sdkmanager` tried to install Android Emulator and reported `Error on ZipFile unknown archive` (runner/SDK infrastructure). No emulator tests ran in that run and no report was generated. The prior run `38499156610` had **16/17** emulator tests passing and a targeted Compose search-box-vs-card-selector failure; that selector fix is in the current live main but was **not retested** because this later emulator never installed.

This source candidate adds Phase 5.2 **honest trace interpretation** and Phase 9 **mobile action-menu usability**:

- `ValueSignalReport` supplies exact counts of manually injected inputs, documented events, received functions, modeled state changes, unmodeled effects and safety stops; exports a plain text, ordered trace and per-device state without implying Fortnite runtime fidelity.
- `ValueScenarioReplay` replays up to **128 manual inputs** in explicit order from a fresh **volatile** simulator, running each input to idle; no Room, blueprint or Fortnite data is altered. Actions exposes Queue, Replay and Clear inputs. Replay intentionally does **not** promise to reproduce a series of interleaved Queues/Steps; each injected event runs to idle before the next.
- The event selector is now a searchable, lazily rendered list for graphs with many documented Event ports; the existing test tags/Run/Step/Reset/Queue/Output journey remain. Output offers **Copy report**, with system clipboard only and no permission/network requirement.
- New pure JVM regression tests cover truthful pending/unsupported reports, deterministic replay, safety stops and 128-action limit. Source-only staging; no Android CI results for these new changes yet.
- **Critical:** This ZIP cannot alter `.github/workflows/` (importer correctly protects workflows). The Android Emulator SDK archive problem is **not a product-code issue** and is not fixed by this ZIP. Retry GitHub Actions `launch-smoke` on a runner with a valid Emulator SDK download, or explicitly change workflow provisioning separately if failure repeats. Do **not** remove the emulator gate or claim green based on the build job alone.

**Acceptance:** import this ZIP, confirm tested head, require build and launch-smoke green, then phone-test input search, actor/value, queue/run/step, replay, copy report, unsupported warnings, and existing overlay, Library, signing, autosave, and app navigation. Existing 208 reference devices are research-backed, not verified 1:1 Fortnite device menus.

---

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
