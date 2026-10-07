# Quality assurance

## Foundation checks

- JVM codec tests: metadata/notes round trip, unknown format rejection, UUID validation,
  populated unsupported graph rejection.
- Android-module JVM test: repository converts to/from explicit JSON storage contract.
- Instrumentation smoke: install/launch on API 29+, visible app title and New Mechanic.
- Instrumentation durability test: close/reopen Room and compare saved document.
- Strict Android lint, debug APK build and instrumentation APK compilation.
- CI validates Gradle wrapper and uploads APK, tests, lint and smoke reports.

Execution evidence is recorded in docs/CURRENT_STATE.md. Written tests alone are not
execution evidence. Local checks cannot establish green remote CI or physical-phone
acceptance. Instrumentation is separate from JVM tests; it needs a device/emulator.

## Manual foundation acceptance

Install debug APK on an API 29+ phone. Launch, create an empty mechanic, return to
home, open Library, force-stop/reopen, and verify the same record remains. Rotate
while editing and verify selected record persists. Confirm no permissions are
requested. Device graph contents do not exist yet.

## Later phases

Every simulated device: initial state, functions/events, settings, disabled state,
reset, player/team context and bindings with other devices. Maintain expected traces
for Trigger→HUD, Button→Tracker→Barrier, Switch→Timer→End Game,
three Triggers→Tracker→Item Granter, Class Selector→Tracker→End Game, and the golden
Three-Key Vault. Check deterministic ordering and loop caps with intentional cycles.

Graph persistence: edit/move/connect, close/reopen and compare all settings/bindings/layout.
Portable files: export, delete, import and compare schema plus content; reject unsupported
versions without data loss. Build guides must reflect every graph setting change.
Overlay: real phone while Fortnite runs, outside-window interaction, orientation,
stop notification, collapse/restore and process recreation. Profile 50/100 nodes,
200 connections, idle CPU and memory. Record measurements rather than assumed targets.

## Dependency version advisories

Only AndroidGradlePluginVersion, GradleDependency and NewerVersionAvailable are
excluded. They report newer releases rather than defects and perform live version
lookups, making otherwise reproducible/offline checks change without source changes.
Toolchain pins deliberately pair AGP 9.4.1 (API 37 support) with its Kotlin 2.2.10
compiler and matching Compose/serialization plugins. Library upgrades belong in
bounded changes that run all checks and emulator tests. All correctness, manifest,
resource, API and Compose lint checks remain enabled; warnings are errors. No lint
baseline exists. Initial lint found missing icon and modern backup rules; both
were fixed with resources rather than suppressed.

Local clean foundation validation passed on 2026-10-07: 5 JVM tests, lint with
zero issues, debug and instrumentation APK assembly, signature verification.
That statement describes the original local foundation run; subsequent green
baseline CI is linked in CURRENT_STATE.

Remote validation blocked on 2026-10-07: git push and CLI/connector GitHub blob
writes returned HTTP 403 (Resource not accessible by integration). No CI or
emulator execution is claimed. Integration repository contents/workflow write
access is required to publish the checked-in workflow.

## Mobile delivery checks

11 Python importer tests pass: valid overlay/owner-file preservation, executable
wrapper, checksums, wrong product, incomplete source, traversal, protected workflows,
APK exclusion, duplicate ZIP members, destination/archive symlinks and bootstrap
implementation parity. Android CI executes them before Gradle checks. The APK
artifact records source SHA, APK digest and signature certificate; emulator tests
use the exact SHA checked out by the build job. The browser import and new CI
workflow remain unexecuted until owner upload; these local tests do not certify them.

Both mobile-delivery workflows pass actionlint validation. Full Gradle checks,
lint and debug app/test APK assembly passed again after the delivery-only change.

Local end-to-end import proof passed: source ZIP committed to a fresh repository,
full embedded bootstrap importer executed, wrapper removed, expanded source
committed and pushed to a local bare remote. This verifies the import sequence;
it is not execution on GitHub Actions.

## Foundation UX acceptance

Baseline Android CI 37697700991 passed on 8805c77; owner reported installed/
launched APK, then requested UX changes. New UX checks: create a named draft; blank
name disables submission; saving opens its editor; Back/Library/search reopens the
same draft; Learn explains semantic ports. Storage reopen test is retained.

Check portrait/landscape and large fonts: every screen scrolls, controls avoid
system bars, names wrap/ellipsize, creation dialog and editor placeholder can
scroll. Navigation animations last at most 220 ms, respect Android duration scale,
and do not continuously animate in idle/background. No live graph is rendered.
Hardware frame-rate and phone responsiveness require real-device acceptance.

UX candidate: all 5 JVM tests and strict lint passed; debug app and instrumented
test APKs assembled. Local connected UI execution failed before running app
tests: the unaccelerated API 29 emulator timed out fetching ADB properties and
was rejected as unknown API level. This is an environment failure, not evidence
that the candidate UI tests pass. Lowering emulator resolution allowed a rendered
portrait home-screen inspection (API 29, 480×854): readable hero/CTA/preview and
three navigation destinations, with no overlap. Property-fetch timeouts remained
on retry. Run interaction tests in Android CI and check on a phone.
