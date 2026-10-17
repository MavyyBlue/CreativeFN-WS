## 2026-10-17 — Actual failures and new acceptance gate

At `3a0b9bc`, Android run 38696750855 passed build/JVM/Lint/APK; downloaded artifact 11955191750 reports **18 tests / 2 failures / 0 skipped**. Button was outside the composed catalog rows; Pre-Game Only was outside the composed picker rows. The Mutator Zone value test passed. Earlier Kotlin errors are resolved.

The unimported candidate uses device search and explicit choice-list scrolling without dropping original simulator assertions. Research-setting coverage now asserts initially unspecified, selection, graph readback after reopening, and clearing. A new isolated UI fixture checks long-list search and finite-choice controls. Expected connected suite grows to **19** tests. Require the actual full result, not that expected count alone.

Local importer and picker-evidence contracts: **13 Python tests passed**. Full Android Gradle command was attempted but failed downloading Gradle (`UnknownHostException: downloads.gradle.org`); no local Android compile, Lint, JVM or emulator certification. After import, require build and launch-smoke on the expanded SHA. Check source ZIP hashes/required files/path protections separately. No weakening, skipped smoke job, data migration or signing change. Keep physical phone acceptance separate and deferred until device support is meaningfully ready. [Coverage and captures](docs/DEVICE_SUPPORT_STATUS.md).

---

## 2026-10-14 — Comprehensive device menu audit and scalable configuration editor (source candidate, **not** full-device certification)

- User requirement: do not request phone testing until every Creative 1.0 device has a trustworthy current menu/port inventory and all device configuration and blueprint operations work; distinguish blueprint coverage from gameplay simulation.
- Research archive cross-walk: 208 catalog devices (original eight + 200 research), 3,745 setting labels, 529 new event labels and 978 new function labels. Original archive has 213 records; six out-of-scope/retired/brand-scope are excluded, and the seven existing-device matches remain stable.
- Expanded the selector metadata from 153 to **200** reference records by parsing originally string-formatted documentation option expressions; **3,685 / 3,745** extracted setting fields now have at least one documented choice or a clearly typed "enter numeric/text" cue, and **60** still lack usable picker evidence. Six research entries have no documented settings in the source. No full live-menu verification has occurred; defaults, contextual filtering, and availability require in-game capture.
- New searchable device-config Settings / Events / Functions tabs render menu rows lazily, so devices with 50–122 settings do not eagerly construct all field controls. Existing eight device options preserve their types, values, IDs, names, serialized formats, and graph behavior. Device search also matches category, event and function descriptions.
- Added ``docs/CREATIVE1_DEVICE_VERIFICATION_QUEUE.md``, full 208-record CSV, and machine-readable audit JSON; prioritize screenshots of Switch, Timer, Mutator Zone, Item Granter, Conditional Button, then original eight/Player Counter/Prop Mover and other source gaps. See capture instructions in queue.
- **Not yet full support.** The parser treats every imported value as a research suggestion, NOT a verified Fortnite default. Do not release as fully supported, alter Room/wire schema, or claim universal simulator fidelity. The prior graph, overlay, signed CI, importer and model IDs were not changed.
- Validation pending: new candidate Android Gradle, strict Lint, connected test, physical phone test **after verification**; local importer contract/checksums can be checked now.

## 2026-10-14 — Picker/configuration and trace-VFX acceptance (source-only)

Baseline tested predecessor: `739a3cc082f2bcd572a12a75fcfbcce54f34bdd0`, Android workflow 38574784317 successful. This source candidate requires independent `build` AND `launch-smoke` gates.

- Verify research options JSON survives Android asset packaging: Mutator Zone → Enabled on Phase must open a selection list (None/All/Pre-Game Only/Gameplay Only). Its options must remain unspecified until explicitly chosen; re-open after save. A setting with no documented choices must not quietly invent a control/default.
- Original eight DeviceConfiguration option kind controls unchanged; named HUD message is intentionally a text setting, not manual simulation output.
- Ensure new Actions no longer prompts for an Event value. Test a Mutator Zone → Tracker → Barrier link with target 1 and assigned Tracker. Trace should distinguish emitted events, received functions, modeled state updates and unsupported outcomes.
- Test legacy-only Trigger → Tracker → Barrier VFX and mixed-reference graph VFX; two routes intentionally retain their established deterministic engines.
- Test Tracker Sharing=Team and Show on HUD=Detailed, Barrier Gameplay Only: valid blueprint should open fallback value tester, NOT a modal 'incompatible' screen. Team/HUD behavior is NOT simulated or certified; no fake rejection or success.
- Inspect VFX on run/step/reset, repeated cues, fanout, 60+ cues, very wide graphs, portrait/landscape, unusual zoom, battery impact. First candidate captures viewport at test entry; pan/zoom during test may misalign effects—must sync live viewport before final VFX acceptance.
- Test Level1 Room saved records, `.c1logic`, guide/list/overlay, APK signature, catalog ID backward compatibility, source importer and launch-smoke Compose tests. Do not promote an SDK/emulator infrastructure failure as a passed gate.

## 2026-10-14 — Current CI triage and new source-candidate gates

- Run [38526399064](https://github.com/MavyyBlue/CreativeFN-WS/actions/runs/38526399064), commit `b35e5d49a99fdcff74784f16ce6a3ac3b24b16f8`: **build success**. **launch-smoke infrastructure failure** before emulator boot or instrumentation: Android Emulator SDK installation reported `Error on ZipFile unknown archive`; no connected-test result. Do not attribute this to Kotlin, app startup, or a new failing assertion.
- Earlier run [38499156610](https://github.com/MavyyBlue/CreativeFN-WS/actions/runs/38499156610) had 16/17 emulator tests passing; `ValueSignalUiTest` selected two nodes sharing `Mutator Zone` text. The `hasClickAction() && !hasSetTextAction()` repair is now on main but awaits full emulator retest. Do not claim certification until it passes.
- New source-only tests: `ValueSignalReportTest` for report/unsupported/queued summaries, deterministic multi-event replay, 128-input cap and a feedback-cycle safety-stop. Test `ValueSignalUiTest` continues to cover the old selector, new summary, Copy report and replay affordance without testing clipboard permissions.
- When emulator provisioning is healthy, run all 17+ instrumentation checks, including `ValueSignalUiTest`, with the SDK 29 x86_64 AVD. If SDK ZipFile download corruption repeats, address runner/SDK provisioning in the protected workflow by a separate explicitly authorized change; **do not skip launch-smoke**.
- Real device: search large event choices, queue a zone event, Run/Step, read modeled and unmodeled results, Replay input history, Copy report, inspect clipboard, and confirm graph/Library/Blueprints/Fortnite overlay unaffected.

## Value Test instrumentation failure and correction — 2026-10-13

Android run [38499156610](https://github.com/MavyyBlue/CreativeFN-WS/actions/runs/38499156610), tested imported commit `5ffcc894194a045b2b3df17444dc14c6bbb771b0`: `build` passed (including JVM tests, Android strict Lint, APK assembly); `launch-smoke` failed at `:app:connectedDebugAndroidTest`. Actual downloaded connected-test report: **17 tests / 1 failure / 0 skipped**. The failed case was `ValueSignalUiTest.researchDeviceHasManualEventActionWithoutChangingOriginalScene`, at line 20, before selecting the Mutator Zone device. The exact failure was "Expected exactly '1' node but found '2'" because both the `Search devices` editable field and the filtered `Mutator Zone` result matched the text predicate.

The corrective test now uses `hasText(..., substring=false) and hasClickAction() and !hasSetTextAction()` to identify the device card, excluding the search field. It does not weaken assertions for the subsequent scenario flow. Production source is not changed. New full emulator execution and real-device confirmation are still required; do not infer 17/17 merely from local source review.

## Broad reference catalog / value scenario candidate (awaiting CI)

Verified predecessor: `b531f0b7`, GitHub Actions run [38495461646](https://github.com/MavyyBlue/CreativeFN-WS/actions/runs/38495461646) green; owner says editor declutter works on phone. This **new** research/value testing source has not completed Android Gradle, strict Lint, launch-smoke or phone acceptance.

- Device catalog regression test preserves eight original device IDs/order, validates 200 additive reference-only definitions and round-trips configuration values. Confirm resources survive packaging. Research data includes 3,745 unique extracted setting labels, 529 events and 978 functions across new definitions.
- `ValueSignalSessionTest`: explicit zone event → assigned Tracker completion → Barrier disable; unmodeled Item Granter function followed by manually injected outcome; reject invented event names; event-cycle safety stops. Four focused tests pass under an independent `kotlinc` mock-model harness; **not** full Gradle/module integration.
- `ValueSignalUiTest`: add Mutator Zone from search, enter VALUE TEST, queue documented event, run, inspect trace. Requires actual connected instrumentation execution, not assumed.
- Regression to verify original Eight-only simulator's exact fixtures unchanged, Room documents, `.c1logic` import/export, Guide and device folders, giant options sheets, overlay launch, Cover Flow and inline card expand still work. No signing changes.
- Important support boundary: port names from documentation research are not actual user-input events; injected events deliberately bypass game conditions. Every unsupported function must be identified rather than implicitly treated as simulated. Free-text setting fields are intentionally **not** proof of actual option types/defaults.

## Editor/navigation declutter acceptance (new candidate)

Certified predecessor `a6384c23e3a1cc1725688baa42e792444b5ff8ce` passed Android CI [38490221585](https://github.com/MavyyBlue/CreativeFN-WS/actions/runs/38490221585) and owner diagnostics acceptance. **This UI source requires a separate Android build, strict Lint, emulator smoke and phone acceptance.**

- New source tests: `GraphLayoutUiTest` (collapsed Tools / Save / Check / simulation return; Library-only Blueprints), updates to `GraphExperienceTest`, `GraphDiagnosticsUiTest`, `SimulatorSceneTest`, `LaunchTest`, and `PlayerExperienceTest` for changed controls. Source tests are NOT execution evidence.
- Manually inspect portrait/landscape with minimum supported Android size/font scaling; tap all four fan options; verify full-height canvas remains pannable/zoomable outside controls and no unexpected invisible hit areas; repeat with keyboard visible.
- Verify hide/restore navigation and header, device creation/editing, Event → Function connections, Undo/Redo, save/reopen, diagnostics dialog and repairing a device, simulator enter/exit and trace/actions, search/recent, Guide, checklist, import/export, and overlay launch.
- After CI green, compare signing certificate with installed build, then check the already accepted floating Fortnite overlay separately, including pinch/scroll and pass-through.

# Quality assurance — CreativeFN WS

The accepted Android baseline is `9a05ebbd84f1b17bca8fe454ec25bff02e3ffd35`, [Actions run 38474555365](https://github.com/MavyyBlue/CreativeFN-WS/actions/runs/38474555365) (**build** and **launch-smoke** green), installed and phone-accepted above Fortnite for Cover Flow and inline expandable graph cards. Earlier documentation that said these features were still candidates is superseded. Phase 5.1 later passed Android CI run `38490221585` on `a6384c23` and was phone-accepted by the owner. The current UI candidate is separately pending.

## Automated regression matrix

- Graph editor: stable UUID identities, semantic Event → Function validation, duplicate prevention, undo/redo, autosave, persisted layout and folder metadata.
- Device catalog: exact scoped event/function IDs, documented subset of options, invalid option values rejected; no guessed live behavior.
- Simulator: deterministic same-time order, external actions, state/traces, infinite-loop and queue/depth caps, supported vs unsupported settings, reset and safe pause/step/run. Golden Three-Key Vault fixture remains in the repository.
- **New Phase 5.1:** `GraphDiagnosticsTest` covers healthy graphs, malformed legacy/imported data, missing/invalid options, orphan links, unknown ports, duplicate semantic bindings vs valid fan-out, disabled Button/Trigger, unassigned Tracker, blank HUD, unsupported Tracker sharing/HUD and Barrier phase, feedback-loop warning, and 1201-device iterative acyclic traversal. `BlueprintDiagnosticsTest` verifies actionable guide warnings and no document mutation.
- `.c1logic`: version-2 compatibility, source/destination graph consistency and non-destructive folder round trips; old versions never silently lost.
- Android instrumentation: Room durability, editor interactions and saved content, blueprint graph render, inline expanded-card bitmap render, overlay navigation; lintDebug must be green with warnings-as-errors and no broad suppression.
- Importer: Python integrity/path/required-file tests; verified SHA-256 for all package entries, no `.github`, `.git`, keystore, generated binaries, symlinks or path traversal; exact source-import contract retained.

## Phase 5.1 phone regression checks (already accepted on predecessor)

1. Create Button → HUD Message, open **Check mechanic**. Expect zero errors/warnings, plus a general sandbox-fidelity information notice. No configuration changes or toast interruption on ordinary pan/zoom.
2. Clear the HUD text. Verify `BLANK_MESSAGE` explains why, offers a fix, and **Open device** opens the correct settings sheet. Restore text and confirm warning disappears.
3. Add Button → Tracker Increment Progress. With Assign on Game Start off, expect `TRACKER_UNASSIGNED`. Wire Assign or turn on assignment; verify warning disappears. Check `targetValue=0` gives `TRACKER_ZERO_TARGET` and individual sharing triggers a sandbox limitation, not an invalid-Fortnite claim.
4. Add Barrier Pre-Game Only. Verify explicit unsupported sandbox phase warning without silently rewriting the option.
5. Build two connected Triggers that form a feedback path; confirm warning suggests a stop condition and simulated action/depth caps still operate. Confirm independent fanout does **not** trigger duplicate warnings.
6. Save, open construction guide and verify its warning text matches the saved graph. Check the overlays' Steps/Links/Graph, expandable node, scrolling details, folders and one-hop connection focus still work over Fortnite, and touches outside the panel still reach the game.
7. Close/reopen, export and reimport `.c1logic` with folders; verify IDs, options, links and checklist state don't unexpectedly disappear. Never uninstall the existing app solely to test this change.

## Evidence boundaries

Local standalone Kotlin harness validation checks newly authored source only if used; it is **not** a Gradle multi-module build, `lintDebug`, emulator test or Android phone acceptance. New tests will actually run under GitHub Actions after import. Confirm the importer expands the exact uploaded package and record the expanded SHA; verify build and launch-smoke use that SHA before APK installation. Check signing certificate continuity separately. If red, inspect full logs and fix the smallest reported cause instead of changing workflows or suppressing Lint.

Maintain Android accessibility click behavior, AndroidX KTX drawing helpers, preallocated draw objects and minSdk/targetSdk API compatibility. No changes to the existing foreground overlay service or signing credentials occur in Phase 5.1.

Historical detailed QA and original failure evidence for earlier candidates remain available in GitHub history at commits preceding the certified baseline. Live Fortnite parity requires dated/versioned in-game fixtures; Epic documentation alone is not runtime certification.
