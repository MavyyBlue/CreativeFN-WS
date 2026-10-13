# Device model

Phase 1 introduces pure Kotlin domain types in core-model and a separate JVM
device-catalog module. There are three strongly typed local definitions: Trigger,
Tracker and Barrier. UI reads this catalog rather than declaring device settings.

Catalog metadata: schema 1, catalog version 0.1.0-draft. Fortnite reference version
and lastVerifiedDate are null; current live behavior/options/defaults are not yet
verified. Epic device documentation URLs are recorded as references. The modeled
options are deliberately small prototype subsets. Every device is NOT_SIMULATED
and displays REFERENCE ONLY; no runtime behavior is advertised.

DeviceOptionValue has Boolean, Integer, Duration (milliseconds), Enum and Text
variants. Definitions validate types, ranges and enum choices. Creating an instance
copies prototype defaults; configured keys must exactly match the definition.
Instances have canonical UUIDs, finite graph coordinates, name/type/options/notes.
Runtime state will be session-owned in Phase 3, separate from saved configuration.

DeviceEventId and DeviceFunctionId are distinct Kotlin types. LogicConnection has
its own UUID and source Event → target Function only. Catalog validation rejects
missing devices, duplicate IDs and incorrect ports. Cycles are not banned here.
This port validation does not replace Phase 5 graph diagnostics.

DeviceInstanceDtoV1/DeviceOptionDtoV1 are explicit wire DTOs. The codec validates
before encoding and after decoding. Unknown wire/catalog versions, malformed values
and unsupported device types require rejection/migration rather than coercion.
No domain/runtime implementation classes are serialized as a storage contract.
Phase 2 uses explicit MechanicDocumentDtoV2/BindingDtoV2/ViewportDtoV2. Legacy
MechanicDocumentV1 remains strict for migration of previously empty drafts. Graph
validation rejects duplicate semantic bindings and preserves intentional cycles.

Six JVM tests cover all three definitions, defaults/modified settings, options,
metadata, semantic bindings and DTO round trips including Unicode text. Simulation
fidelity tests begin in Phase 3; wire/schema tests do not establish runtime support.
Before claiming Phase 1 complete: finish reference verification, run new green CI,
and document the exact supported settings. Expand MVP devices individually later.

## Phase 3 experimental runtime boundary (2026-10-12)

The new JVM-only `PrototypeSimulationSession` implements an explicit bounded
rules subset for these reference devices. It does not change catalog metadata,
version, `NOT_SIMULATED` support labels or storage DTOs. Fortnite defaults,
timing and multiplayer semantics remain unverified. Prototype runtime rejects
unsupported Individual/Team tracker sharing, non-Off HUD presentation, game
phase-only barrier modes, and zero target. HUD Message is not yet defined.
See SIMULATION.md for the precise sandbox contract and test evidence.

## Phase 3.2 reference extension (uncertified source candidate)

Catalog `0.2.0-draft` contains eight entries: the original Trigger/Tracker/Barrier and five newly selected Epic devices: HUD Message, Button, Round Settings, Creature Manager, Elimination Manager. Only documented direct-event-binding ports are exposed; the supported option subset is explicitly small. Existing 0.1.0-draft graph-2/device DTOs for old devices migrate without destructive database changes. Full Fortnite behaviors are **NOT** represented, and `SimulationSupport.NOT_SIMULATED` remains correct; local test-scene exercises are not 1:1 claims. See `FORTNITE_FIDELITY.md` and `SIMULATION.md`.
