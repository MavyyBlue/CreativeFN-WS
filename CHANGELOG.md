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
