# Roadmap

Phases advance only after implementation/tests and green GitHub Actions evidence.

| Phase | Scope | Status |
| --- | --- | --- |
| 0 | Android foundation, storage wiring, tests, CI, docs | Validated baseline f305c5d / Android run 37726479431; owner phone acceptance |
| 1 | Versioned typed catalog: Trigger, Tracker, Barrier | Catalog foundation validated in baseline; Fortnite reference verification open |
| 2 | Touch graph, semantic ports, undo/redo, autosave | 9/10 candidate Android tests passed; save-button selector repair pending CI/phone acceptance |
| 3 | Deterministic simulator, trace, stepping, golden vault | Not started |
| 4 | Expand twelve MVP devices one at a time with tests | Not started |
| 5 | Validation and debugging UX | Not started |
| 6 | Library/folders, thumbnails, search, .c1logic import/export | Not started |
| 7 | Graph-derived build guide | Not started |
| 8 | Passive overlay; real phone acceptance | Not started |
| 9 | Profiling, process recreation, mobile polish | Not started |

Phase 0 is a shell and not a static diagramming substitute for the intended product.
Next task: validate the Phase 2 editor through the existing source importer and
Android CI, then confirm placement/movement/zoom/bindings and close/reopen on a
phone. Phase 3 follows with the deterministic runtime and golden vault tests.
The owner authorized graph work using explicitly reference-only prototype devices;
Fortnite fidelity verification remains open.
Device fidelity evidence is required before advertising simulation support.

Deferred beyond MVP: cloud/community sharing, collaboration, ratings, comments,
server catalog updates, scene view, graph components, advanced team/round simulation,
interactive tutorials and tablet-specific UI.
