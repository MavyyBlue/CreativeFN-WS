# Simulation

Not implemented in Phase 0. The Android-free module currently reports this clearly.
The UI cannot claim Simulation Passed or expose working simulation controls.

Phase 3 builds a deterministic virtual clock and ordered event queue. Equal-time
events use a monotonic sequence number. Device state, instigator/player/team context
and explicit function execution produce a trace for every action. Persist device
configuration separately from resettable runtime state. No wall-clock dependencies,
graphics, movement, combat, weapons, physics or game client integration.

Test controls: pause, step one queued event, resume, reset and manual supported
actions. Reset restores initial state, clock, queue and trace. Trace timestamps,
event ordering and state changes must be repeatable for identical inputs.

Cycles are permitted. Enforce per-tick event, propagation-depth and session-total
caps. Pause with POSSIBLE EVENT LOOP, responsible device IDs and retained trace when
exceeded. Static validation distinguishes proven errors from warnings/information;
cycles alone are not blocking errors.

## Golden acceptance fixture: Three-Key Vault

Three one-use Triggers increment a Tracker with target 3. Completion disables a
Barrier and displays a HUD Message. Verify progress 1/2/3, barrier enabled before
third activation, disabled after third, exactly one success event, and no progress
from retriggering any consumed Trigger. Expected trace fixtures are required in
Phase 3; none exist in the foundation build.
