# Roadmap — CreativeFN WS

Status is based on implementation evidence. A green Android build and a separate owner device check certify an APK, not parity with Fortnite Creative 1.0.

| Phase | Scope | Status at 2026-10-13 |
|---|---|---|
| 0 | Android, Room, CI, mobile source importer | Established; current baseline run [38474555365](https://github.com/MavyyBlue/CreativeFN-WS/actions/runs/38474555365) green |
| 1 | Eight-device draft catalog, semantic ports | Established; real Fortnite fidelity incomplete |
| 2 | Touch graph canvas, bindings, history, autosave | Established; phone-accepted |
| 3 | Deterministic prototype, manual actions, traces | Established, bounded by explicit unsupported settings |
| 4 | Source-backed fidelity and selective additional devices | In progress; no new types this slice; live Creative 1.0 comparisons required |
| 5.1 | Pre-build configuration and graph diagnostics | **Source candidate**, pending importer, full Android CI, phone acceptance |
| 5.2 | Friendly runtime trace interpretation and debugging | Next bounded slice |
| 6 | Saved mechanics, search, folders, `.c1logic` | Per-mechanic device folders and import/export established; Library-wide folders/previews deferred |
| 7 | Construction guide and checklist | Established; Phase 5.1 warnings candidate pending acceptance |
| 8 | Floating Fortnite reference overlay | **Owner phone-accepted at `9a05ebbd`**: Cover Flow, inline expanding nodes, pan/pinch, Fit, focus, Steps/Links |
| 9 | Lifecycle, large graphs, profile and UX acceptance | Pending further measurement |

## Phase 5.1 immediate acceptance gate

Import `creative-logic-mobile-source.zip`; record the new expanded commit. Confirm the Android CI `build` and `launch-smoke` jobs run on that same source and pass strict Lint. Check installed-certificate continuity before APK update. On the phone, make a Button → HUD mechanic; **Check mechanic** should have no error/warning but show the general fidelity info. Set HUD Message text blank; inspect warning and **Open device**. Create an unassigned Tracker fed by a Button; confirm assignment advice. Turn on unsupported individual sharing; verify the warning explicitly says the test scene can't represent it, rather than claiming the setting is invalid in Fortnite. Make a Trigger → Trigger feedback cycle; check that it warns, while simulator limits remain intact. Save, generate a blueprint and verify warnings survive, then open the existing overlay above Fortnite and confirm navigation, node expansion, folder links and touch pass-through remain intact.

No Room migration, no new Android permissions, no changed overlay source, no new event/function IDs, no simulator semantics or signing changes. Do not label the Phase 5.1 package certified until all new gates are complete.

## Next researched candidates

Research waves (owner-provided, 2026-10-13) prioritize Channel, Switch, Timer, End Game and related devices. These are **candidates only**. Audit each against current Epic Creative 1.0 pages, model required world/player/time state honestly, lock unknown defaults and name conflicts pending live evidence, and add isolated deterministic tests before exposing any new device. Do not paste the proposed JSON registry directly into production. Future work includes Library-wide mechanic folders, previews, `.c1logic` backward compatibility, graph-scale tests and resource profiling.
