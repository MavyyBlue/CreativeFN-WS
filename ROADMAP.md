# Roadmap — CreativeFN WS

Implementation status is based on source inspection and local checks. **Only GitHub Actions plus owner phone checks certify Android behavior**; simulator behavior is NOT 1:1 Fortnite certified.

| Phase | Scope | Source status (2026-10-13) |
|---|---|---|
| 0 | Android, Room, CI, mobile source importer | Certified through CI run 38338236226 |
| 1 | Device catalog (8 scoped devices) | Established, live Fortnite fidelity pending |
| 2 | Canvas, direct bindings, history, autosave | Established, CI certified |
| 3.1–3.2 | Deterministic sandbox, manual test actions, trace and state inspection | Phase 3.2 certified CI and owner accepted on Android |
| 4 | Expanded catalog, live verification | Deferred: only 8 existing devices; no fabricated support |
| 5 | Validation, actionable debug | Graph invariant checks and simulator trace already exist; comprehensive UX not completed |
| 6 | Local mechanics, search, portable import/export, folders | Existing persistent searchable Library + new .c1logic document picker import/export; folders and thumbnails deferred |
| 7 | Fortnite construction guide | New shared guide generator with device quantities, configured values, bindings, sequence, checklist, caveats; **first release CI passed; additional phone checks pending** |
| 8 | Android system overlay | New explicitly started foreground service and TYPE_APPLICATION_OVERLAY bubble/panel with drag, graph zoom/pan, step persistence, notification Stop; **Android CI green; Fortnite overlay visible, Graph and sizing follow-up unverified** |
| 9 | UX/lifecycle hardening | Initial orientation fitting, saved step/bubble location and passive lifecycle; profiling/full soak testing pending |

## Immediate Android acceptance gate

Import this source ZIP, confirm GitHub Actions build and emulator jobs pass, then check signature continuity before install. On phone: save graph → Blueprints → Guide → overlay permission → Fortnite → bubble → Steps/Devices/Links/Graph → collapse/reopen/Stop → return and edit. Record whether Fortnite or OEM suppresses the overlay. No separate signing workflow changes. Do not call an uncertified overlay a confirmed working release.

## Real-device feedback / next focused acceptance

GitHub Android run `38435563960` (commit `6efa671`) is **green**, and the owner demonstrated the overlay above Fortnite in landscape with working Steps, Devices and Links. Phase 8 is partially phone-accepted, not fully completed: the graph was blank, and the expanded window consumed most of the game view. A follow-up code candidate (CI pending) reduces landscape size, provides in-overlay per-orientation size controls and tab-bar collapse, and repairs graph fitting plus tests. Next validate graph nodes/links, drag, pinch/fit, on-screen resizing, saved size/checklist and Fortnite touch pass-through on actual Android. Do not mark Phase 8 complete until that evidence arrives.
