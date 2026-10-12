# Architecture

## Current foundation

Single activity with Compose and state-based navigation. Saveable screen and
selected mechanic ID survive activity recreation. Lifecycle-aware Flow collection
stops consuming database updates when the UI is inactive. Coroutine writes run
through a constructor-injected repository; Room rejects main-thread database I/O.
The application owns a lazily constructed Room database. There is no DI framework.

Dependency direction: `app → graph-engine → device-catalog → core-model`,
`app → simulation-engine → core-model`.
All JVM modules use no Android or Compose classes. The simulator module now has an opt-in experimental prototype runtime, but
UI-facing `SimulationAvailability.isImplemented` remains false until an accepted
simulation experience exists.

| Logical boundary | Initial location | Status |
| --- | --- | --- |
| app | app module | activity, navigation, theme, composition root |
| core-model | core-model module | repository, empty-document DTO, typed device domain |
| device-catalog | device-catalog JVM module | typed versioned reference definitions and instance codec |
| graph-engine | graph-engine JVM module | immutable edits/history, graph DTO codec and save coordination |
| simulation-engine | simulation-engine module | experimental deterministic JVM engine + golden core trace; Phase 3 UI/HUD pending |
| mechanic-storage | app / mechanicstorage | Room schema 1, JSON adapter |
| overlay | app / overlay | reserved; Phase 8 |
| ui-graph | app / uigraph | touch Canvas, palette, configuration/bindings sheets, workspace ViewModel |
| ui-library | app / uilibrary | reserved; Phase 6 |
| ui-simulator | app / uisimulator | reserved; UI not wired in Phase 3 engine slice |
| testing | JVM test source sets, app androidTest | codec, repository, launch smoke |

## Storage contract

`MechanicGraphCodec` dispatches explicit versioned wire DTOs. Format 2 contains
metadata, DeviceInstanceDtoV1 options, BindingDtoV2 and ViewportDtoV2. Domain and
runtime classes are never serialized directly. Legacy format 1 is accepted only
for the previously supported empty graphs and migrates to the domain model.
Unknown formats/catalogs and invalid graph semantics are rejected. Room schema 1
continues to store this JSON in its existing blob column; no destructive fallback
or column migration is needed. Portable import/export remains Phase 6.

GraphEditor applies validated immutable snapshots with 50 undo entries. Camera
changes persist but are not undo entries. Device deletion removes incident bindings;
duplication copies settings with a new UUID and no copied bindings. An activity-
scoped ViewModel preserves session/history across rotation. GraphDraftSession uses
a mutex and captures the newest graph after obtaining it, so overlapping saves
cannot overwrite newer snapshots out of order. Writes debounce 500 ms, flush on
navigation/ON_STOP and report failure with manual retry; failed writes keep edits.
One custom Compose Canvas paints nodes/ports/wires. Gesture previews stay local
until release, keeping database writes and history out of pointer-move loops.

## Prototype runtime and future integration

Graph data is authoritative for simulation inputs and generated guides. The
Android-free `PrototypeSimulationSession` now owns resettable Trigger/Tracker/Barrier
runtime states, queue, virtual time, trace, and loop guards. It consumes the
typed graph without mutating persistent configuration; it rejects unsupported
player/team, HUD and phase modes rather than guessing. Its catalog remains
REFERENCE ONLY, and no Compose simulator controls are wired yet. The full
Three-Key Vault depends on a future HUD Message type.
A compact immutable model will be constructed once for the passive overlay.
The app will suspend simulation in background; the overlay must never start it.
Future scene view may reference the same device IDs, without duplicating settings.

## Foundation presentation refinement

CreativeTheme centralizes color/shape/typography tokens. BuilderGlyph uses small
local Canvas strokes with labels supplied by semantic controls. Typed destinations
retain saveable route/selection. AnimatedContent handles finite 120–220 ms screen
transitions; there are no infinite animation clocks. Named draft creation uses the
existing repository, with synchronous input validation and explicit asynchronous
feedback. Library search is local presentation filtering of the observed drafts.
Android-free simulation boundaries remain intact; format 2 now supports populated graphs.

## Player presentation controls

DisplayPreferences persists header/navigation flags in private SharedPreferences.
View options always remains reachable when both bars are hidden. Contextual
InformationButton uses a dismissible anchored popup, with explanatory text absent
until requested. ModalNavigationDrawer owns Recent search/scroll/selection; the
Recent destination is offered only after observed saved data is nonempty. No
recent cards are rendered on Home. Catalog reference screens use the same typed
definitions tested on the JVM. Only brief support badges remain inline.
