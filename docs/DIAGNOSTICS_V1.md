# Phase 5.1 — builder diagnostics contract

## Scope

`GraphDiagnostics.inspect` is a pure Android-free, **read-only** inspection of a `MechanicGraph` and the exact catalog supplied by its caller. It never mutates device settings, port bindings, viewport, Room rows, folder membership or simulator actions. It supports graphs read from older/imported sources that may not meet the current editor's strict mutations.

Results have a stable `code`, `DiagnosticSeverity` (`ERROR`, `WARNING`, `INFO`), a readable title, an explanation and a concrete action. When possible, `deviceId` or `connectionId` points to the affected node/edge. The graph editor shows a small always-available summary button and an on-demand scrollable checklist; **Open device** navigates to settings. Guide generation includes the same actionable ERROR/WARNING messages. The copy does not claim that sandbox-incompatible settings are invalid Fortnite settings.

## Current checks

| Codes | Why it matters |
|---|---|
| `DUPLICATE_DEVICE_ID`, `DUPLICATE_LINK_ID`, `DUPLICATE_LINK` | Protect object identity and avoid accidental repeated function calls. Multiple distinct targets from one event are valid fan-out. |
| `UNKNOWN_DEVICE`, `MISSING_OPTION`, `INVALID_OPTION`, `UNKNOWN_OPTION` | Older/broken imports can reference data the current catalog can't validate. Avoid silently dropping unknown saved options. |
| `MISSING_ENDPOINT`, `UNKNOWN_EVENT`, `UNKNOWN_FUNCTION` | A binding points to missing devices or a removed/unsupported semantic port. |
| `BLANK_MESSAGE`, `TRIGGER_STAYS_DISABLED`, `BUTTON_STAYS_DISABLED`, `TRACKER_UNASSIGNED` | Likely builder mistakes with in-editor fixes; warnings, not automatic mutation. |
| `TRACKER_ZERO_TARGET`, `TRACKER_SHARING_UNSUPPORTED`, `TRACKER_HUD_UNSUPPORTED`, `BARRIER_PHASE_UNSUPPORTED` | Precise current **prototype simulation** limits; other Fortnite options may be valid and still need in-game checks. |
| `FEEDBACK_LOOP` | Potential loop in the directed graph. Warning only; device-specific semantics may make it harmless. Existing simulator caps are not changed. |
| `SANDBOX_SCOPE`, `EMPTY_GRAPH` | Informational notices, not failures. |

A local build guide may still contain manual steps despite ERROR findings, but the associated warnings state what must be corrected before relying on it. This is intentionally not a new document format, no automatic repair or graph migration.

## Device-fidelity position and research

This slice changes **no** catalog definition and **no** simulation behavior. The two owner-provided 2026-10-13 research ZIPs document useful potential expansions and unresolved defaults/semantics; those source-derived contracts require official Epic source spot-checks and Creative 1.0 in-game fixtures before activation. For example, Epic's current Trigger page documents Enable/Disable/Reset Times Triggered/Trigger and On Triggered; Barrier has supported Enable/Disable plus ignore-list functions and no events. These are only source/reference facts, not evidence of tested simulator parity.

## Next boundary

Phase 5.2: trace interpretation in everyday builder language, testing concrete timing/counter cases, user-accessible wiring suggestions and per-device option completeness. Phase 4 selective devices should follow evidence-backed typed model extensions rather than filling missing defaults by guessing. Keep warning wording versioned if device semantics change.
