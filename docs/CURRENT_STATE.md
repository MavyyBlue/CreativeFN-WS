# Current state — Phase 3.2 source candidate, 2026-10-12

## Certified baseline

GitHub branch `main` at `4ce239cdf3483d0867fb1600812f9587128f3132` certified by Android [run 38316188825](https://github.com/MavyyBlue/CreativeFN-WS/actions/runs/38316188825). Both `build` (Gradle tests/lint/APK) and `launch-smoke` (10 emulator tests) passed. Previous Phase 2 CI [38311622832](https://github.com/MavyyBlue/CreativeFN-WS/actions/runs/38311622832) also passed. This is **prior to importing the new Phase 3.2 ZIP**.

Owner uninstalled the older debug-signed app and installed the new Phase 3.1 debug APK; the app launched and looked identical, as expected for a backend-only change. Removing the older app may have cleared its Room data. Do **not** require further uninstalls; check signing continuity/data before a future install. User's current Library data should be treated as unknown until checked on-device.

## Uncertified changes delivered in this Phase 3.2 source candidate

- Adds five Epic-documented reference devices: HUD Message, Button, Round Settings, Creature Manager, and Elimination Manager. Existing Trigger/Tracker/Barrier remain. The 0.2.0-draft catalog exposes only modelled subsets with official functions/events; full options, game physics, inventory and multiplayer are not simulated.
- Legacy 0.1.0-draft graph format-2 documents migrate in-memory to catalog 0.2.0-draft on decode; existing Room schema 1 is unchanged. A new regression test covers the migration. New drafts use catalog 0.2.0-draft.
- Extends Android-free deterministic session with Game Start (first Round Start), Button Interact, Player Eliminated and Fiend Creature Eliminated actions; precise target routing, actor trace, safety caps and runtime-only device states.
- Adds a **Simulate / Design** test-scene toggle inside the graph editor, a fixed collapsible Actions menu, Run/Step/Pause/Reset, collapsible Output pane showing trace and states, and explicit rejection of unsupported graph settings. Saved graph contents are never mutated by simulation.
- HUD Message supports the literal Three-Key Vault trace with a single Show on Tracker completion. The new fixture is `three-key-vault-phase32.trace`.

**Evidence:** the original eleven engine tests plus five new scenario tests (16/16) passed in a local standalone Kotlin/JVM harness, independent of Android. This does *not* establish Gradle/lint/Compose success, emulator success, true Fortnite Creative 1.0 fidelity, or phone acceptance. Source bundle importer integrity checks are required before upload. All normal GitHub checks remain the gate.

**Fidelity boundary:** documentation-grounded ports are NOT automatically Fortnite-verbatim runtime semantics. Official reference links, supported subsets, and in-game evidence required for eventual 1:1 certification are in `FORTNITE_FIDELITY.md`. `SimulationSupport.NOT_SIMULATED` remains on each catalog definition, and simulated activities are visibly marked TEST SCENE / not Fortnite-verified.

**Device/signing boundary:** no new permissions, background workers, app IDs, key material, signing workflows or Room schema changes. Existing app version 0.0.4 is retained for this import. Permanent signing identity remains separate from current debug APK; never regenerate its key. The owner must not uninstall the current working app just to test this candidate.

## Next step

Upload only `creative-logic-mobile-source.zip` at repository root via the existing mobile importer. Verify imported SHA, Android build test/lint/signature and emulator tests. Inspect logs on failure. **Before installing the candidate**, compare installed/produced certificate and assess in-place upgrade or a reversible data-safe path; do not imply GitHub green equals a safe APK upgrade. Next certify the simulator scene on phone. Update these documents with actual CI evidence after running.
