# Roadmap — CreativeFN WS

Advance phases only after implementation + full GitHub Actions evidence; Android UX also requires owner phone checks. Fortnite fidelity requires separate in-game comparison.

| Phase | Scope | Verified status (2026-10-12) |
|---|---|---|
| 0 | Android foundation, storage, CI | Established |
| 1 | Typed Creative 1.0 catalog | 3 original prototypes implemented; 5 new documentation-grounded reference entries in Phase 3.2 candidate; live fidelity pending |
| 2 | Graph editor, bindings, undo/redo, autosave | Android CI 38311622832 green; owner has used app; detailed gesture/performance acceptance open |
| 3.1 | Deterministic runtime core | CI 38316188825 green at 4ce239c; newly installed APK launched on phone, UI unchanged by design |
| 3.2 | Test scene, external actions, collapsible console, HUD golden, catalog expansion | **Source candidate in preparation**; runtime harness passed, full Android CI + phone acceptance pending |
| 4 | Incrementally verify expanded devices in Fortnite | Not completed: added reference entries are not certified device simulations |
| 5 | Full diagnostics | Not started |
| 6 | Folders, thumbnails, portable .c1logic import/export | Basic drafts/search exist; portable delivery not started |
| 7 | Generated build guides | Not started |
| 8 | Passive Android reference overlay | Not started |
| 9 | Profiling/mobile polish | Not started |

Next gate: import Phase 3.2 source ZIP, verify the **expanded tested SHA** and both Android CI jobs. Then real-phone test the Simulate / Design toggle and actions/console against a newly saved graph without uninstalling the existing app. No signing workflow/key changes belong in this slice. Device port provenance and unsupported configurations: [FORTNITE_FIDELITY.md](FORTNITE_FIDELITY.md).
