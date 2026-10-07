# Repository working rules

Work in bounded phases; consult docs/CURRENT_STATE.md and ROADMAP.md before changing code.
Inspect HEAD, architecture documents and latest available CI evidence. Do not document
completion without implementation, tests and green Actions. Synchronize CURRENT_STATE,
ROADMAP, QA and CHANGELOG after each phase. Commit source only, never generated APKs.

Run relevant tests, full `./gradlew check`, strict lint and `assembleDebug`. Fix failures;
do not weaken checks or add undocumented suppressions. Instrumentation requires a
phone/emulator and must be recorded separately from JVM checks.

Pure models and the deterministic simulator must remain Android-free. Stable UUIDs,
semantic Event → Function ports, explicit versioned DTOs and graph-authoritative
settings/guides are mandatory. Unsupported behavior must be surfaced, never simulated
silently. Begin the catalog with Trigger, Tracker and Barrier; no decorative bulk catalog.

This is an offline passive companion. Never interact with Fortnite processes, files,
traffic, controls or screens. No AccessibilityService, capture/OCR, automation, hooks,
macros or live game-state inference. Overlay permission belongs to Phase 8 and only
follows an explicit user action, never first launch.
