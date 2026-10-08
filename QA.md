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
requested. Graph placement/bindings are now available in the Phase 2 candidate.

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
and do not continuously animate in idle/background. That historical UX check preceded the Phase 2 canvas.
Hardware frame-rate and phone responsiveness require real-device acceptance.

UX candidate: all 5 JVM tests and strict lint passed; debug app and instrumented
test APKs assembled. Local connected UI execution failed before running app
tests: the unaccelerated API 29 emulator timed out fetching ADB properties and
was rejected as unknown API level. This is an environment failure, not evidence
that the candidate UI tests pass. Lowering emulator resolution allowed a rendered
portrait home-screen inspection (API 29, 480×854): readable hero/CTA/preview and
three navigation destinations, with no overlap. Property-fetch timeouts remained
on retry. Run interaction tests in Android CI and check on a phone.


## UX smoke repair

CI 37702966712 on 6749da9 passed the build job and executed all 5 Android
instrumentation tests. Three passed. Library selection matched both the editable
search field and the mechanic card. Learn's child `performScrollTo` left Bindings
out of view. Repaired tests scroll the tagged LazyColumn with `performScrollToNode`,
then retain `assertIsDisplayed` for each concept/result. Card selection excludes
`hasSetTextAction`, making the result independent of the search input's value.
Search IME hides the keyboard before scrolling and opening the saved draft.

Full local Gradle check/lintDebug/app and test APK assembly passed after repair.
All 5 JVM tests and 11 importer tests passed; both workflows pass actionlint.
Local test-APK installation on the unaccelerated API 29 emulator exceeded
240 seconds during dex compilation. No repaired UI pass is claimed.
Instrumentation and new green CI must be recorded separately when executed.


## Player controls and Phase 1 catalog candidate

Baseline Android run 37705777016 on 611434a passed including repaired smoke tests.
New Android acceptance tests cover popup visibility/dismissal, hiding/restoring both
bars across recreation, Recent sidebar search/selection and Tracker reference ports.
Draft/search/storage tests are retained and updated for the simplified Home.
JVM catalog tests reject invalid settings/ports/UUIDs and future schemas/catalogs,
and round-trip all five value kinds. Reference badges do not establish simulation.

On a phone: hide both bars, force-stop/reopen, restore them through View options;
check compact/landscape/large-font popups and sidebar dismissal/search/long lists.
Home should contain no inline developmental explanation or Recent list.
Instrumented execution and green CI for this candidate must be recorded separately.

Final candidate: 11 JVM tests, strict lint, app/test APK builds, APK signature
verification and 11 importer tests passed. Local API 29 software-emulator install
exceeded 300 seconds; Android System UI showed an unresponsive-system dialog.
Rendered-app and updated interaction checks remain pending; no UI pass is claimed.


## Closed-sidebar selector repair — 2026-10-08

Run 37725293030 on 9f8aac4: build passed, 8/9 Android tests passed. Popup,
bar hide/restore/recreation, sidebar selection and device-reference tests all
passed. Draft reopening failed because its name occurs in both the editor
header and retained offscreen drawer content. The editor title now has a stable
semantic tag, and Library/Recent result matchers require the corresponding
list ancestor. Text equality, visibility and navigation assertions are retained;
editor title is checked again after reopening from Library. New CI is required.

Post-repair local full Gradle check/lintDebug/app and test APK assembly passed,
along with 11 JVM tests, 11 importer tests and workflow syntax validation.


## Phase 2 graph candidate — 2026-10-08

Baseline f305c5d / Android run 37726479431 passed all nine Android tests; owner
accepted the installed app and requested continuation. Phase 2 adds 13 JVM tests:
validated add/move/options/delete/bind/undo/redo, independent UUID duplication,
non-undoable viewport, populated format-2 round trip, legacy migration, unsupported
version/dangling endpoint rejection, ordered overlapping writes and save-failure
retry. Total: 24 JVM tests passed. Full Gradle check, strict lintDebug, app/test APK
assembly, 11 importer tests and both unchanged workflows' actionlint passed.

Ten Android tests compile; candidate execution is pending new CI. New graph flow
creates/configures Trigger → Tracker → Barrier, checks typed bindings and undo/redo,
saves, recreates and reopens from Library, then verifies the configured target.
Dropdown matchers require their menu ancestor to avoid matching an already selected
device in another control. Existing scoped editor/Library/Recent smoke assertions
remain. Storage durability compares a populated graph and viewport after closing
and reopening Room. Compilation is not instrumented execution.

Phone acceptance: create those three devices; drag Tracker; pan, pinch and Fit;
edit target/notes/name; connect an event to a function; reject an invalid attempt;
duplicate/delete a device and undo/redo; remove a binding; wait for Saved, force-
stop/reopen and compare layout/settings/bindings. Rotate with pending edits and
verify state; hide bars and restore with View options. Test landscape, large fonts
and long names. Dismissing settings discards uncommitted fields; Done applies them.
Measure 50/100 nodes and 200 connections before claiming rendering targets.
Simulation behavior must not be inferred from this editor milestone.
