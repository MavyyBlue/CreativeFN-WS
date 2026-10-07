# Device model

Phase 0 has **no device definitions and no supported device behavior**. Do not infer
support from the presence of planned package boundaries.

Phase 1 defines CreativeDeviceDefinition, CreativeDeviceInstance,
DeviceOptionDefinition/Value, DeviceEventDefinition and DeviceFunctionDefinition.
Every graph entity receives a stable UUID. Type IDs, option IDs and semantic port
IDs remain stable across catalog revisions. Connections bind source Event IDs to
target Function IDs only. Never accept untyped node-to-node lines.

The versioned catalog includes catalogSchemaVersion, fortniteReferenceVersion,
lastVerifiedDate, source references and simulation limitations. Until actual
reference verification occurs, use unavailable/unverified metadata rather than a
fabricated Fortnite version or verification date.

Support badges: FULLY SIMULATED, PARTIALLY SIMULATED, REFERENCE ONLY. Unsupported
behavior must report NOT_SIMULATED; modeled subsets report PARTIALLY_SIMULATED with
explicit limitations. FULL means tested fidelity within the documented logic scope.

Start with Trigger, Tracker and Barrier. Then implement Button, Switch, Timer,
Player Counter, HUD Message, Item Granter, Class Selector, End Game and Player Spawn
Pad individually, including settings, context semantics and tests. Do not add the
entire catalog as decorative nodes.
