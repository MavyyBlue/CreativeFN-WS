# Simulation — Phase 3 foundation candidate

## What exists in this source bundle

`simulation-engine` now contains `PrototypeSimulationSession`, an Android-free,
explicitly **experimental rules sandbox**. It consumes the existing typed
`MechanicGraph`, validates devices and Event → Function bindings against the
versioned catalog, and holds runtime state apart from saved configuration. No
Room schema change, app UI route, Android permission, or Fortnite integration is
introduced. `SimulationAvailability.isImplemented` intentionally remains false:
no builder-facing verified simulator is delivered by this first slice.

Implemented deterministic controls: `trigger(deviceId, actorId, afterMillis)`,
`pause()`, `step()` (one queued action even when paused), `resume()` (drain until
idle/safety stop), `advanceTo(millis)` (idle only), `reset()`. The virtual clock
starts at zero; only scheduled actions or explicit idle advance change it.
At equal timestamps a monotonically increasing insertion sequence breaks ties.
Trace entries record time, index, device, input/event/function/state change or
ignored action, details, and actor ID. No real clock or Android APIs are read.

Runtime types: Trigger enabled/count/cooldown; Tracker assigned/progress/completed;
Barrier enabled. State lives in the session and is not saved back to the graph.
On a successful Trigger firing, `onTriggered` dispatches bound functions. Tracker
increment/complete fires `onComplete` **once per completion**; Barrier enable/
disable mutates its separate runtime state. Other catalog port names are accepted
only where corresponding prototype behavior is implemented. An actor ID is
carried through traces and bindings but **no per-player/team tracking** is
modeled; the rules intentionally reject unsupported configurations.

Prototype supported configuration subset:
- Trigger: enabledOnGameStart, triggeredByPlayer (manual input gate),
  timesCanTrigger (0 means unlimited), resetDelay (virtual milliseconds);
  functions enable/disable/resetTimesTriggered/trigger.
- Tracker: `sharing=All`, `showOnHud=Off`, `targetValue>=1`, assignOnGameStart;
  functions assign/incrementProgress/resetProgress/complete/remove.
- Barrier: `enabledDuringPhase=Always` or `None`; functions enable/disable.

This is **not** a claim that these are Epic's current gameplay semantics,
per-player instigation, true phase transitions, collision, physics, display,
or timing. The catalog stays NOT_SIMULATED / REFERENCE ONLY pending fidelity
verification. Unsupported types/options fail before execution instead of being
silently treated as simulated. Adding full semantics requires verified references,
new tests and explicit catalog/UX review.

## Safety and determinism

Cycles are allowed, but each action is bounded by defaults of 256 processed
operations per virtual millisecond, 64 propagation levels, 10,000 session actions,
and 4,096 pending actions. Caps are configurable for tests. On exceed, the
session enters `POSSIBLE_EVENT_LOOP`, preserves the existing trace/queue, and
records involved device IDs. The owner must Reset to run again. Processed order
and snapshots are repeatable with the same graph, UUIDs and scheduled inputs.

## Acceptance fixtures

`simulation-engine/src/test/resources/fixtures/three-key-vault-core.trace` is a
literal expected trace for **three one-use Triggers → Tracker target 3 → Barrier
disabled**. Tests verify progress 1/2/3, barrier enabled until completion,
one `onComplete`, non-retriggerable used Triggers, strict trace equality,
ordering, pause/step/resume/reset, actor propagation, delayed input, cooldown,
loop guards, validation failures and disabled/unassigned behavior.

The complete Three-Key Vault specification additionally requires **HUD Message**
on success. That type and its function/event semantics do not yet exist in the
catalog; the full golden scenario is **NOT complete**. A subsequent bounded
catalog+runtime slice must add HUD Message with a carefully labeled prototype
behavior and extend the fixture. UI controls and real-phone acceptance likewise
remain pending. Golden tests do not establish Fortnite fidelity.

Remote Android/JVM CI for this candidate has **not** run at time of packaging.
Inspect the exact imported SHA and Android jobs before marking it accepted.
