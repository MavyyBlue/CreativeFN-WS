## 2026-10-17 — Blueprint-first continuation

Live `3a0b9bc` passed build but failed 2/18 emulator tests in Android run 38696750855. The separate unimported candidate fixes lazy-menu targeting and adds keyboard dismissal and long-choice search; Android rerun is pending. **Priority 1 remains device menu/port completeness**, using the concrete [support audit](docs/DEVICE_SUPPORT_STATUS.md) and focused Switch/Timer/Mutator Zone captures. Next come honest communication verification and live-viewport trace VFX. Do not interpret old exhaustive simulation/live-menu gates as the product scope or request repeated partial APK installations. Catalog count is 208 entries, not 208 verified distinct devices.

---

## 2026-10-14 — Comprehensive device menu audit and scalable configuration editor (source candidate, **not** full-device certification)

- User requirement: do not request phone testing until every Creative 1.0 device has a trustworthy current menu/port inventory and all device configuration and blueprint operations work; distinguish blueprint coverage from gameplay simulation.
- Research archive cross-walk: 208 catalog devices (original eight + 200 research), 3,745 setting labels, 529 new event labels and 978 new function labels. Original archive has 213 records; six out-of-scope/retired/brand-scope are excluded, and the seven existing-device matches remain stable.
- Expanded the selector metadata from 153 to **200** reference records by parsing originally string-formatted documentation option expressions; **3,685 / 3,745** extracted setting fields now have at least one documented choice or a clearly typed "enter numeric/text" cue, and **60** still lack usable picker evidence. Six research entries have no documented settings in the source. No full live-menu verification has occurred; defaults, contextual filtering, and availability require in-game capture.
- New searchable device-config Settings / Events / Functions tabs render menu rows lazily, so devices with 50–122 settings do not eagerly construct all field controls. Existing eight device options preserve their types, values, IDs, names, serialized formats, and graph behavior. Device search also matches category, event and function descriptions.
- Added ``docs/CREATIVE1_DEVICE_VERIFICATION_QUEUE.md``, full 208-record CSV, and machine-readable audit JSON; prioritize screenshots of Switch, Timer, Mutator Zone, Item Granter, Conditional Button, then original eight/Player Counter/Prop Mover and other source gaps. See capture instructions in queue.
- **Not yet full support.** The parser treats every imported value as a research suggestion, NOT a verified Fortnite default. Do not release as fully supported, alter Room/wire schema, or claim universal simulator fidelity. The prior graph, overlay, signed CI, importer and model IDs were not changed.
- Validation pending: new candidate Android Gradle, strict Lint, connected test, physical phone test **after verification**; local importer contract/checksums can be checked now.

## 2026-10-14 — Blueprint-first reprioritization

1. Configuration UX is the priority: **implemented as source candidate** picker-first documented device controls with explicit unknown defaults; actual Fortnite menu capture and source QA remain open.
2. Communication simulation: **source candidate** removes user-entered event output values, corrects audit counts, and permits valid unmodeled team/HUD/phase configurations to fall back to Event→Function tracing. Source-backed behavior coverage, actor-specific team conditions and real Fortnite testing are still open; do not claim all 208 devices simulated.
3. Visual feedback: **first candidate** deterministic trace-driven node glow/connection orb/output bubble for legacy and reference testers; live viewport sync, accessibility/animation preference and device profiling remain before full certification.
4. Resume next phase planning only after Android CI, real phone acceptance and comparison against real Fortnite Creative 1.0 menus.

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
