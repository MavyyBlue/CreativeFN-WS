## 2026-10-14 update — after source catalog expansion

- **Phase 4 reference breadth:** 208 devices (eight original plus 200 research-backed), **catalogue/reference only**. Actual live Creative 1.0 menus and unmodeled functions remain verification work; not complete Fortnite fidelity.
- **Phase 5.2 trace interpretation:** source candidate now shows input, event/function, modeled value, unsupported and safety counts, state report and plain-text export. Pending Android CI and phone acceptance.
- **Phase 5.3 value scenario composition:** source candidate now supports bounded (128) repeatable in-memory event sequences with per-event Run-to-idle semantics. Persistent scenario templates/automatic Fortnite conditions are **not** implemented.
- **Phase 9 usability:** lazy searchable event picker for many researched ports and compact report controls. More profiling, large-options UX, font-size/landscape testing remain open.
- **CI gate:** run `38526399064` built successfully, but emulator setup failed while unzipping Android Emulator SDK; no connected tests ran. Retry emulator on healthy SDK supply. Workflows are protected from the upload ZIP and were not modified.

> **Gate update, 2026-10-13:** Research catalog + value-test source was imported at `5ffcc894` but **is not certified**: run [38499156610](https://github.com/MavyyBlue/CreativeFN-WS/actions/runs/38499156610) had `build` PASS / `launch-smoke` FAIL (16/17 emulator tests passed). `ValueSignalUiTest` selected an ambiguous Mutator Zone text match. A test-selector-only corrective source ZIP is ready; full Android and owner phone acceptance remain pending. No device fidelity claim changes.

## Expanded documentation catalog and manual value test (uncertified source)

At green predecessor `b531f0b7` (Android run `38495461646`, owner acceptance reported), add 200 research-scope device entries without changing the eight old IDs/options. Source evidence is reference-only. The 207 Creative-doc-scope items included 7 preexisting titles/IDs; 200 additional types are placed in the device palette (208 total). All 200 use unspecified text setting values, documented port labels and official-source links; no unverified Fortnite defaults are manufactured. For mixed/new graphs, manually inject source events and inspect value changes/unsupported deliveries through deterministic graph routing. Legacy-only graphs keep the existing engine. CI/Lint/emulator, signing and phone acceptance pending. Subsequent work: targeted semantics per function, formal catalog-version migration, larger graph/option editor profiling.

# Roadmap — CreativeFN WS

Status is based on implementation evidence. A green Android build and a separate owner device check certify an APK, not parity with Fortnite Creative 1.0.

| Phase | Scope | Status at 2026-10-13 |
|---|---|---|
| 0 | Android, Room, CI, mobile source importer | Established; current predecessor run [38490221585](https://github.com/MavyyBlue/CreativeFN-WS/actions/runs/38490221585) green |
| 1 | Eight-device draft catalog, semantic ports | Established; real Fortnite fidelity incomplete |
| 2 | Touch graph canvas, bindings, history, autosave | Established; phone-accepted |
| 3 | Deterministic prototype, manual actions, traces | Established, bounded by explicit unsupported settings |
| 4 | Source-backed fidelity and selective additional devices | **Research reference expansion source candidate:** 200 additive reference-only devices, not verified live menus; live Creative 1.0 comparisons required |
| 5.1 | Pre-build configuration and graph diagnostics | **Certified** at `a6384c23`; Android run 38490221585 green and owner phone-accepted |
| 5.2 | Friendly runtime trace interpretation and debugging | Value-signal Actions source candidate: manual injected events + actor/value, deterministic routing and unsupported markers |
| 6 | Saved mechanics, search, folders, `.c1logic` | Per-mechanic device folders and import/export established; Library-wide folders/previews deferred |
| 7 | Construction guide and checklist | Established, including Phase 5.1 diagnostic warnings (owner accepted) |
| 8 | Floating Fortnite reference overlay | **Owner phone-accepted at `9a05ebbd`**: Cover Flow, inline expanding nodes, pan/pinch, Fit, focus, Steps/Links |
| 9 | Lifecycle, large graphs, profile and UX acceptance | Editor declutter **owner phone-accepted** at `b531f0b7`; profiling pending |

## Phase 5.1 previously completed acceptance

The owner reports the diagnostics editor control works, and Android `build` and `launch-smoke` passed on `a6384c23` ([run 38490221585](https://github.com/MavyyBlue/CreativeFN-WS/actions/runs/38490221585)). Preserve these behaviors during the following navigation refactor. For the new source candidate, repeat CI and phone testing separately; no previous green run certifies this UI revision.

## Next researched candidates

Research waves (owner-provided, 2026-10-13) prioritize Channel, Switch, Timer, End Game and related devices. These are **candidates only**. Audit each against current Epic Creative 1.0 pages, model required world/player/time state honestly, lock unknown defaults and name conflicts pending live evidence, and add isolated deterministic tests before exposing any new device. Do not paste the proposed JSON registry directly into production. Future work includes Library-wide mechanic folders, previews, `.c1logic` backward compatibility, graph-scale tests and resource profiling.

## UI declutter candidate after certified Phase 5.1

Baseline is `a6384c23e3a1cc1725688baa42e792444b5ff8ce` (Android run `38490221585` green and owner phone-accepted). Next source revision gives the app an angled, compact four-card navigation bar (saved toggle), a full-height editor without bottom bar, a collapsing upper-left fan of Tools / Devices / Bindings / Fit / Simulate, right-edge Undo / Redo / Save / Check controls, and a Library-only Blueprints button beneath New Mechanic. New instrumentation source covers navigation and editor access; run on actual GitHub Android jobs and validate on the phone. No device/simulator/catalog or overlay source changes. The next major planned expansion remains the researched device catalog and value-based scenario actions, with documentation/implementation support boundaries.
