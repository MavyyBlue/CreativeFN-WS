# Roadmap

Phases advance only after implementation, tests, green GitHub Actions evidence,
and owner phone acceptance for relevant Android interactions. Remote CI and device
acceptance are separate gates.

| Phase | Scope | Status as of 2026-10-12 |
| --- | --- | --- |
| 0 | Android foundation, storage, tests, CI and docs | Implemented; CI and earlier phone baseline accepted |
| 1 | Typed Trigger, Tracker and Barrier catalog | Prototype catalog implemented; Fortnite fidelity unverified |
| 2 | Touch editor, semantic bindings, undo/redo, autosave | Implemented; Android CI 38311622832 green at 105ce159, physical phone graph acceptance outstanding |
| 3 | Virtual clock, deterministic execution, trace, stepping, loop controls, golden vault | Experimental Android-free runtime *candidate in this source bundle*; 11 targeted local JVM tests passed, remote CI pending; UI and HUD Message absent |
| 4 | Additional devices verified one by one | Not started |
| 5 | Validation and debugging UX | Not started |
| 6 | Folders, thumbnails, search and .c1logic files | Basic draft Library/search exists; portable library milestone not started |
| 7 | Generated build guides | Not started |
| 8 | Passive Android overlay | Not started |
| 9 | Performance, recreation and phone polish | Not started |

**Next step:** import and certify this Phase 3 core slice with the existing source
ZIP → Android Actions workflow. No APK replacement is required: the Kotlin runtime
is not yet connected to a user-facing simulator. After green CI, add a minimal
in-app Run/Step/Reset view using only explicitly supported prototype semantics.
HUD Message must be added and verified before the *full* Three-Key Vault fixture
can pass. The current fixture proves the three Triggers → Tracker → Barrier core
only, with no fabricated HUD device or claimed Fortnite fidelity.

Deferred beyond MVP: cloud/community sharing, collaboration, ratings, comments,
server catalog updates, scene view, graph components, advanced team/round simulation,
interactive tutorials and tablet-specific UI.
