# Architecture

## Current foundation

Single activity with Compose and state-based navigation. Saveable screen and
selected mechanic ID survive activity recreation. Lifecycle-aware Flow collection
stops consuming database updates when the UI is inactive. Coroutine writes run
through a constructor-injected repository; Room rejects main-thread database I/O.
The application owns a lazily constructed Room database. There is no DI framework.

Dependency direction: `app → device-catalog → core-model; app → core-model`, `app → simulation-engine → core-model`.
All JVM modules use no Android or Compose classes. The simulator module currently
advertises unavailability only; it contains no runtime behavior.

| Logical boundary | Initial location | Status |
| --- | --- | --- |
| app | app module | activity, navigation, theme, composition root |
| core-model | core-model module | repository, empty-document DTO, typed device domain |
| device-catalog | device-catalog JVM module | typed versioned reference definitions and instance codec |
| graph-engine | app / graphengine | reserved; Phase 2, extract pure graph operations |
| simulation-engine | simulation-engine module | reserved; runtime in Phase 3 |
| mechanic-storage | app / mechanicstorage | Room schema 1, JSON adapter |
| overlay | app / overlay | reserved; Phase 8 |
| ui-graph | app / uigraph | reserved; Phase 2 |
| ui-library | app / uilibrary | reserved; Phase 6 |
| ui-simulator | app / uisimulator | reserved; Phase 3 |
| testing | JVM test source sets, app androidTest | codec, repository, launch smoke |

## Storage contract

`MechanicDocumentV1` is a dedicated wire DTO, not a serialized runtime class. It
contains format/app/catalog versions, UUID metadata, notes and reserved graph fields.
Phase 0 accepts empty graphs only and explicitly rejects populated graphs or unknown
format versions. Typed device/binding/layout DTOs must replace the reserved JSON
objects before Phase 2; retain versioning and add tested migration dispatch when needed.
Portable import/export is not implemented. No destructive Room migration fallback
is used. Exported Room schemas are source-controlled.

## Planned runtime

Graph data is authoritative for simulation inputs and generated guides. Runtime
state will belong to SimulationSession, isolated from persistent configuration.
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
All empty-graph restrictions and Android-free simulation boundaries remain intact.

## Player presentation controls

DisplayPreferences persists header/navigation flags in private SharedPreferences.
View options always remains reachable when both bars are hidden. Contextual
InformationButton uses a dismissible anchored popup, with explanatory text absent
until requested. ModalNavigationDrawer owns Recent search/scroll/selection; the
Recent destination is offered only after observed saved data is nonempty. No
recent cards are rendered on Home. Catalog reference screens use the same typed
definitions tested on the JVM. Only brief support badges remain inline.
