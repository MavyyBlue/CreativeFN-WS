# Changelog

## 2026-10-13 — Practical blueprint workflow (uncertified candidate)

- Retain Phase 3.2 editor, deterministic test scene, Room persistence and 8-device reference catalog.
- Generate construction instructions and manual-verification caveats directly from saved mechanics; same generator drives full-screen and overlay references.
- Open a stored blueprint from an always-available Blueprints entrypoint and manage checklist progress.
- Add user-initiated Android overlay foreground service and drawable FN bubble with expandable device/steps/links/graph tabs, draggable windows and graph pan/zoom; notification Stop.
- Add portable `.c1logic` import/export with duplicate-safe IDs and input size guard.
- Keep source candidate unverified until CI and Android/Fortnite device acceptance. No signing or Room schema changes.

## Uncertified lint-fix candidate — 2026-10-13
- First blueprint upload imported as `4db801a`; Android run `38433588379` failed strictly on nine overlay/blueprint Lint errors; all 45 JVM/unit tests passed; emulator skipped.
- Follow-up addresses the exact nine Lint findings without suppressing checks or changing build/signing/database/simulation infrastructure.
- Reuses graph draw bounds and computes node mapping outside drawing; uses Android KTX URI/color/preferences/canvas helpers and a standard View constructor.
- Requires a fresh Android CI run and separate phone/Fortnite confirmation; no success claim yet.

## Compact landscape overlay and graph repair — 2026-10-13 (new CI candidate)
- Owner confirmed that the previous overlay runs above Fortnite on Android with working Steps/Devices/Links tabs; Graph was blank, and the expanded window blocked much of the game.
- Default expanded overlay now starts at about 52% × 52% of a landscape display (roughly one-quarter screen area) and retains a taller readable portrait setting. Width and height can be adjusted with the in-overlay Size controls and reset to compact defaults; values and window positions are stored per orientation.
- Replaces large stock toolbar/nav buttons with smaller touch targets, adds a persistent Tabs hide/show toggle and collapses verbose verification warnings behind an on-demand control.
- Reworks graph auto-fit using actual view-pixel geometry rather than multiplying device coordinates by density twice, makes zoom focus follow pinch gestures, and provides a Fit graph action; includes a graph-render instrumentation regression test and pure JVM geometry tests.
- Existing Room schema, saved documents, catalog, service permission/special-use declaration, simulator and signing remain untouched. Changes require new Android CI and phone retest; no claim of success for this new candidate.
