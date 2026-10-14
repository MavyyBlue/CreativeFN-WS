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
