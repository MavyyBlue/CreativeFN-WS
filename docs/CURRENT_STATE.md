# Current state

Current phase: **Phase 1 catalog foundation and player-facing UX candidate**.

Latest fully validated commit: `611434a97673acc8e21905dbb48eac4597d0e429`.
Latest validated Android CI: [37705777016](https://github.com/MavyyBlue/CreativeFN-WS/actions/runs/37705777016), success, including the repaired smoke tests.
Owner installed/launched that APK and accepted its visual direction, then requested
less explanatory clutter, controllable bars and a separate Recent sidebar.
The new candidate needs its own green CI and phone acceptance.

Implemented foundation: Kotlin/Compose API 29–37, named drafts persisted with
Room/explicit JSON DTO, Android-free model/simulator boundaries, source importer,
strict lint, APK artifacts and Android instrumentation.

Phase 1 candidate: new Android-free device-catalog module. Versioned strongly typed
Trigger, Tracker and Barrier definitions; five option kinds, typed Event/Function
IDs, UUID device instances, semantic bindings, option/port validation and explicit
versioned instance DTO/codec. Six JVM tests cover defaults and modified options,
round trips, invalid IDs/types/settings, semantic binding rejection, metadata and
future-version rejection. Fortnite reference version/date remain null: these are
unverified prototype subsets/defaults with source references, not fidelity claims.

Supported simulated devices: **none**. Partial devices: **none**.
Reference-only catalog devices: **Trigger, Tracker, Barrier**.
Device runtime simulation has not begun. No device placement, graph persistence,
folders, portable mechanic import/export, generated guide or overlay yet. Existing
empty-graph Room/JSON data remains readable; the new instance wire DTO is separate.

Player UX candidate: Home starts with New Mechanic without a hero container or
recent list. Contextual information opens a dismissible floating popup. Header and
navigation visibility are persisted, with always-accessible View options to restore
both or navigate. Recent appears after a draft exists and opens a searchable,
scrolling sidebar ordered by modification time. Device Reference is catalog-driven.

Validation: final local full Gradle check, strict lint and app/test APK assembly
passed. Local Android install exceeded 300 seconds on the unaccelerated API 29
emulator; its Android System UI became unresponsive, blocking visual/interaction
checks. Updated UI execution is pending CI/phone validation.
Both unchanged workflows passed
actionlint; all 11 importer tests passed.
11 JVM tests (including 6 catalog tests); 11 importer tests. Android tests cover
popups, both-bar hiding/restoration/recreation, recent sidebar selection and catalog
reference, alongside existing draft/search/recreation/storage smoke coverage.
No new green remote CI is claimed.

Next task: finish candidate checks, import source ZIP through the existing workflow,
obtain green Android CI and phone acceptance. Phase 1 reference verification remains
open; Phase 2 adds device placement, options and graph persistence. GitHub writes
previously returned 403; browser source upload remains the proven delivery path.
