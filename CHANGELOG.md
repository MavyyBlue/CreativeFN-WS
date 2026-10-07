# Changelog

## 0.0.1 — Phase 0 foundation (2026-10-07)

- Single-activity Compose home, navigation, theme and clearly labeled editor shell.
- Native Android API 29 minimum and API 37 compile/target; Gradle Kotlin DSL.
- Separate Android-free core-model and simulation-engine JVM modules.
- Constructor-injected Room repository, Flow collection and coroutine saves.
- Explicit versioned empty-mechanic JSON contract with unsupported content rejection.
- JVM tests, launch/storage instrumentation checks and Android CI artifact workflow.
- Architecture, model, simulator, overlay, QA and phase evidence documentation.

Graph editing, catalogs, simulation, completed library tools and overlays are not
implemented. Foundation validation status is maintained in docs/CURRENT_STATE.md.

Local validation: clean full checks passed (5 JVM tests), lint zero issues, debug
APK and test APK built. Signature/API metadata verification passed. CI/emulator
execution pending; no physical-phone validation is claimed.

Publication blocker: GitHub integration rejects repository writes with HTTP 403.
Foundation remains locally validated, pending remote CI and emulator acceptance.

## Mobile delivery repair — 2026-10-07

- Adapted Local Yuki’s source ZIP → importer → expanded commit → Actions APK
  artifact structure using a separate Creative Logic Mobile contract.
- Added source-only packaging, validated/protected import and 11 importer tests.
- Linked successful imports to Android CI; recorded source, APK hash and certificate.
- Added precise phone upload/extract/install instructions. No app behavior changed.
- GitHub write integration still returns 403; browser setup/import and CI pending.
