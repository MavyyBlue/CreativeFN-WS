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
