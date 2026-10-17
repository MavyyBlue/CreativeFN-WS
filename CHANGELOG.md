## 2026-10-17 — Essential device communication scope (supersedes exhaustive-menu gates)

The owner clarified that device support means **accurate purpose, actual incoming Functions/outgoing Events, and only settings essential to blueprint intent or communication conditions**. Full-menu/default/contextual parity and physical gameplay reproduction are not required. The 60 missing picker domains and six no-settings records are optional research gaps unless they affect that contract. The former full capture batch is withdrawn as a prerequisite. Preserve existing settings/IDs and saved blueprints.

A receiver-only device is a valid endpoint. A Function requesting an effect does not create an outgoing Event or prove the physical result. Report structural validity, signal delivery and relevant configuration support separately from unknown gameplay. The owner's Ball Spawner example illustrates this boundary; this update adds no unverified port or gameplay behavior.

Latest live source `256d99f223a750c1cc3eada47032447154caec24` includes the previous ZIP. Android [39060899455](https://github.com/MavyyBlue/CreativeFN-WS/actions/runs/39060899455): **build PASS; launch-smoke 18/19 passed, 1 failure, 0 skipped**. Downloaded artifact `12111487409` identifies ResearchSettingUiTest: picker list absent after attempted opening. This new, unimported candidate scrolls the setting button into view and waits for the actual dialog before interacting; all selection/readback/clear assertions remain. New Android CI is pending. The clarification is documented direction, not an implemented profile/checker rewrite.

Older entries below are historical; this scope and the canonical charter take precedence.

---

## 2026-10-17 — Lazy-menu CI repair and research picker usability (unimported)

- Audited live `3a0b9bc` and Android 38696750855: build passed; actual emulator report 16/18 passed. Earlier Kotlin repairs are already imported.
- Search for the Button card and scroll the lazy choice list in the two failing tests; retain simulator assertions and add chosen-value graph readback/clearing checks.
- Dismiss the settings-search keyboard when opening a picker; add search for choice lists longer than eight. Add an isolated long-menu UI regression and two evidence-asset contract tests.
- Correct research limitation text and document actual counts, missing menus and duplicate HUD Message representations without altering stable IDs or saved formats.
- Local Python: 13 passed. Full Android checks blocked by unavailable Gradle download; candidate needs import and fresh CI. No new Fortnite fidelity or phone acceptance claim.

---

## 2026-10-14 — Comprehensive device menu audit and scalable configuration editor (source candidate, **not** full-device certification)

- User requirement: do not request phone testing until every Creative 1.0 device has a trustworthy current menu/port inventory and all device configuration and blueprint operations work; distinguish blueprint coverage from gameplay simulation.
- Research archive cross-walk: 208 catalog devices (original eight + 200 research), 3,745 setting labels, 529 new event labels and 978 new function labels. Original archive has 213 records; six out-of-scope/retired/brand-scope are excluded, and the seven existing-device matches remain stable.
- Expanded the selector metadata from 153 to **200** reference records by parsing originally string-formatted documentation option expressions; **3,685 / 3,745** extracted setting fields now have at least one documented choice or a clearly typed "enter numeric/text" cue, and **60** still lack usable picker evidence. Six research entries have no documented settings in the source. No full live-menu verification has occurred; defaults, contextual filtering, and availability require in-game capture.
- New searchable device-config Settings / Events / Functions tabs render menu rows lazily, so devices with 50–122 settings do not eagerly construct all field controls. Existing eight device options preserve their types, values, IDs, names, serialized formats, and graph behavior. Device search also matches category, event and function descriptions.
- Added ``docs/CREATIVE1_DEVICE_VERIFICATION_QUEUE.md``, full 208-record CSV, and machine-readable audit JSON; prioritize screenshots of Switch, Timer, Mutator Zone, Item Granter, Conditional Button, then original eight/Player Counter/Prop Mover and other source gaps. See capture instructions in queue.
- **Not yet full support.** The parser treats every imported value as a research suggestion, NOT a verified Fortnite default. Do not release as fully supported, alter Room/wire schema, or claim universal simulator fidelity. The prior graph, overlay, signed CI, importer and model IDs were not changed.
- Validation pending: new candidate Android Gradle, strict Lint, connected test, physical phone test **after verification**; local importer contract/checksums can be checked now.

## 2026-10-14 — Picker-first blueprint configuration and first trace VFX (source candidate)

- Preserved stable 208-device catalog, `0.2.0-draft` wire/option IDs and Room. Added local research-backed selectors for 153 candidate devices rather than always displaying free-text device settings. Unknown defaults and undocumented options remain unspecified; numeric/text entry only for documented pick/enter controls.
- Removed manual Event value field in the new test Actions, reduced misleading audit-only value counts in reports, and kept unmodeled effects distinct from actual safety warnings.
- The legacy narrow simulator now falls back to Event → Function verification for valid non-modeled team/HUD/game-phase configurations rather than blocking blueprint testing.
- Added bound, trace-derived node perimeter highlights, signal orbs along saved connections and fading output bubbles for the old and expanded tester. Geometry is based on entry viewport; live viewport sync remains deferred.
- Added research settings UI and JVM compatibility/visual trace/report tests. Source-only/local harness checks; **new Android CI + physical phone acceptance pending**. Overlay and signing untouched.

## 2026-10-14 — Test report, manual scenario replay, and large-catalog event search (CI candidate)

- Checked run `38526399064`: build green; emulator setup failed with `Error on ZipFile unknown archive` before connected tests launched; selector repair still awaits emulator confirmation.
- Added user-readable, plain-text-copyable value test report distinguishing input injection, received functions, modeled changes, unsupported effects and loop stops. No claim of Fortnite gameplay simulation.
- Added bounded, deterministic, volatile multi-input replay (up to 128 inputs) and searchable event selection with a lazy list. The original 208-device reference catalog and narrow eight-device prototype remain untouched.
- Added pure JVM tests for report, unsupported paths, replay ordering, limit handling and safety-stop summaries; not yet certified by Android CI.
- Kept Room, `.c1logic`, device ports/defaults, graph engine, overlay service, signing, Gradle and `.github` files untouched. The emulator SDK download corruption is an independent CI infrastructure issue; the source-only ZIP cannot repair `.github` provisioning.

## 2026-10-13 — Focused UI-test selector correction (CI pending)

- Investigated [Android run 38499156610](https://github.com/MavyyBlue/CreativeFN-WS/actions/runs/38499156610) and its actual downloaded launch-smoke report at expanded commit `5ffcc894194a045b2b3df17444dc14c6bbb771b0`. Android build/Lint/JVM/package job **passed**; 16 of 17 emulator instrumentation tests passed; one failed during device search selection.
- Updated `ValueSignalUiTest` to distinguish the clickable Mutator Zone device card from the editable search field containing the same phrase. Subsequent simulation/assertion steps remain intact.
- No production source, reference device catalog, simulator, overlay, schema, signing, dependency, importer or workflow changes. The corrected test still needs remote Android CI and owner phone acceptance before any certification claim.

## 2026-10-13 — Research catalog and value-signal testing (CI candidate)

- Based on live green `b531f0b7` (Android run `38495461646`) and owner phone acceptance of editor declutter.
- Append 200 reference-only device definitions from Creative documentation research (208 including eight stable legacy types). Bundle 3,745 named configurable text fields, 529 documented event labels and 978 function labels from those 200 records. No inferred official defaults; no UEFN-only, retired or branded-only records.
- Add manual value/event Actions path with actor and input value, deterministic binding execution, targeted boolean/progress state changes, unsupported-operation trace and loop bounds; preserve old eight-device simulator.
- Add standalone value-engine tests, new Android UI source test and research catalog validation. No change to Room schema, graph wire version, overlay service, signing or Gradle workflows. Source package NOT YET Android CI- or phone-certified.

## 2026-10-13 — Full-height builder workspace (new source candidate)

- Starting from owner-accepted commit `a6384c23e3a1cc1725688baa42e792444b5ff8ce` / Android green run [38490221585](https://github.com/MavyyBlue/CreativeFN-WS/actions/runs/38490221585). This source package has **no new Android CI certification** yet.
- Remove Editor bottom nav and stacked Graph control rows; canvas fills from the app header to the system content boundary.
- Add a fixed upper-left, collapsing Tools fan (Devices/Bindings/Fit/Simulate; Design return in simulation); keep Undo/Redo/Save and Check on a compact fixed right rail.
- Replace full-width stock NavigationBar with a compact Cover Flow-inspired four-card navigation; retain per-user show/hide preference and explicit View options recovery when header hidden.
- Move Blueprints into Library immediately after New Mechanic. Preserve import/export, guides, progress, permissions and foreground overlay service unchanged.
- Update Compose instrumentation source to exercise changed access paths and layout, with no changes to Room, ports, simulation logic, event ordering, saved formats, overlay or build/signing settings.

# Changelog — CreativeFN WS

## 2026-10-13 — Phase 5.1 mechanic health check (subsequently certified at a6384c23)

- Verified live main SHA `9a05ebbd84f1b17bca8fe454ec25bff02e3ffd35` as the owner-accepted Android baseline ([run 38474555365](https://github.com/MavyyBlue/CreativeFN-WS/actions/runs/38474555365); both `build` and `launch-smoke` green).
- Added a read-only, Android-free graph diagnostics engine with stable severity/codes, friendly explanation and suggested corrective actions: invalid/missing settings, unknown/duplicated ports and links, disconnected endpoints, blank HUD messages, disabled Trigger/Button inputs, unassigned Tracker, limited prototype settings and feedback-loop warnings.
- Added in-editor **Check mechanic** summary/inspect/Open device workflow; shared warnings now appear in the generated blueprint. No data or simulator state is modified by inspection.
- Added pure-JVM regression tests for valid/corrupted graphs, repeated bindings vs fan-out, cycles, dormant devices, tracker limitations, blueprint integration and larger acyclic graphs.
- Kept the existing eight-device catalog, Room schema 1, version-2 wire formats, overlay UI/service, fixed signing, Android permissions, Lint policy and Gradle dependencies untouched.
- Initially pending, this diagnostic slice subsequently passed Android `build` and `launch-smoke` on `a6384c23` (run `38490221585`), and the owner reports a successful phone acceptance.

## 2026-10-13 — Inline expanded device-node baseline (CERTIFIED)

- The previous inline-expansion candidate `d7999c5c101cd123132ee3d1877ee024bd699789` failed strict Lint on manual `Canvas.save / clipRect / restore` (`UseKtx`) in Android run 38473346190. Replacing the clip with AndroidX `withClip` preserved rendering semantics and allowed the subsequent successful run.
- **Commit `9a05ebbd84f1b17bca8fe454ec25bff02e3ffd35`, run 38474555365: build and launch-smoke passed; owner subsequently installed and accepted the in-place expandable node in Fortnite on Android.**
- Graph nodes expand on tap to show options, notes and bindings, and collapse on retap; content scrolls within the node while graph pan, pinch, Fit and one-hop connection focus persist.

## Earlier 2026-10-13 milestones and correction evidence

- Cover Flow navigation moved Steps/Links/Graph below the main blueprint panel, restoring full-bleed graph space. CI passed for Cover Flow predecessor `5d15dc9683ccb1b885471afbdb0ba1ffd0aa60bd`, run 38468092556. Earlier run 38466392646 had failed `ClickableViewAccessibility`; a custom accessible label with `performClick()` corrected it.
- The three-view graph/Links/folder follow-up added named non-destructive device folders, detailed binding explorer and direct-neighbor focus. Per-device folder metadata was added non-destructively to the existing version-2 mechanism document.
- The compact overlay corrected a blank graph and oversized landscape window reported on the phone; run 38441055829 passed at `ac754adf` and owner confirmed visible nodes/wires and resizing above Fortnite. Earlier expanded overlay run 38435563960 at `6efa671` was green but had a blank graph and excessive panel size.
- Blueprint generator, guide checklist, `.c1logic` local import/export and Android passive overlay were introduced without Room migration. First candidate imported at `4db801a` and failed run 38433588379 at Android Lint (nine issues) despite 45 JVM tests; focused KTX, drawing allocation and custom View repairs followed.
- Phase 3.2 deterministic scenario simulator, eight reference devices, graph edit/undo/redo, Room library and mobile-source importer were established in earlier green commits. Historical detailed diffs and reports remain available in GitHub commit history.
