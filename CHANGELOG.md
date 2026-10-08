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

## 0.0.2 — Foundation UX polish (2026-10-07)

- New lime/lavender theme, clearer typography, spacing, cards and lightweight icons.
- Home/Library/Learn navigation with short transitions, safe insets and scrolling.
- Named draft creation with blank-name protection, asynchronous save and retry feedback.
- Search saved draft names and learn events/functions/bindings in plain language.
- Explicit preview badges and an improved canvas placeholder; no simulation claim.
- Existing saved data/schema retained; new documents record the actual app version.
- New UI acceptance coverage; full local Gradle tests/lint/APK checks passed.
- Baseline 8805c77 / Android run 37697700991 passed and owner installed/launched.
  The new UX candidate requires its own CI and phone acceptance.


## UX launch-smoke repair — 2026-10-07

- Diagnosed run 37702966712: build passed; 3/5 Android tests passed.
- Library tests distinguish saved cards from editable search text.
- Learn/Library tests scroll lazy lists directly and keep visibility assertions.
- Library Search keyboard action dismisses the keyboard.
- Full local Gradle checks, strict lint, debug/test APK builds and importer tests
  pass. New green CI remains required; no workflow checks were disabled.
