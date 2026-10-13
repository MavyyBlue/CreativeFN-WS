# Fortnite Creative 1.0 fidelity ledger — Phase 3.2 candidate (2026-10-12)

**This build is NOT 1:1 certified.** Ports below follow Epic's published direct-event-binding terminology; runtime behavior remains a deliberately scoped, offline scenario model until each case is compared with a live Fortnite Creative 1.0 island. No connection to the Fortnite client is made.

| Device | Verified documented event/function names | Test-scene subset | NOT simulated / evidence still needed |
|---|---|---|---|
| HUD Message | functions **Show**, **Hide** (Epic also documents Clear Layer); no events | Message text; Show/Hide; immediate Show on Round Start | HUD styling, timed duration, queue/layers, recipient filters, Clear Layer, on-screen Fortnite look |
| Button | **On Interact** event; **Enable**, **Disable** | External manual interaction, initially enabled, optional one use | proximity, teams/classes, interact time, delay/cooldown, audio and visuals |
| Round Settings | **On Round Start** event | Game Start simulates only Round 1, for All/1 devices | subsequent rounds, inventories, rewards, round winner and all other overrides |
| Creature Manager | **On a Matching Creature Type Is Eliminated** event; **Enable**, **Disable** | Manual Fiend-eliminated input for enabled Fiend manager | actual spawning, combat, damage, different creature types, priority between managers |
| Elimination Manager | **On Eliminated**, **On Elimination** events; **Enable**, **Disable** (Epic also documents On Item Picked Up) | Manual player-2 eliminated by player-1; Players Only, enabled check, self-elimination filter | inventory registration, drops, team/class filters, penalties, true instigator context, other target types |
| Trigger, Tracker, Barrier | Existing Phase 1 draft ports | Legacy Phase 3.1 explicit rules | Fortnite live default options/timing/player/team behavior |

Official Epic source pages consulted on 2026-10-12:
- HUD Message: https://dev.epicgames.com/documentation/en-us/fortnite/using-hud-message-devices-in-fortnite-creative
- Button: https://dev.epicgames.com/documentation/en-us/fortnite/using-button-devices-in-fortnite-creative
- Round Settings: https://dev.epicgames.com/documentation/en-us/fortnite/using-round-settings-devices-in-fortnite-creative
- Creature Manager: https://dev.epicgames.com/documentation/en-us/fortnite/using-creature-manager-devices-in-fortnite-creative
- Elimination Manager: https://dev.epicgames.com/documentation/en-us/fortnite/using-elimination-manager-devices-in-fortnite-creative

Notes:
- A menu entry **Game Start / Round 1** represents a manually declared first-round scenario. It is not a device event named Game Start. `onRoundStart` is sourced only from Round Settings devices.
- **Player Eliminated** is distinct from **Creature Eliminated**. The former routes `onEliminated` for victim and `onElimination` for eliminator via Elimination Manager; the latter routes the documented matching-creature event through Creature Manager. Actor propagation is sandbox trace metadata, NOT Fortnite instigator certification.
- Only implemented settings appear in the UI. Features that cannot be represented without a physics/game-engine model are excluded, not silently assumed.
- Final 1:1 claims require a per-device fixture built inside Fortnite Creative 1.0, version/date noted, screenshots of real settings/bindings, recorded input sequence, expected effects and side-by-side trace checks. Discrepancies must disable claims or cause scoped corrections.
- Existing docs/metadata fields `fortniteReferenceVersion` and `lastVerifiedDate` stay null until actual in-game verification. Documentation consultation alone does not constitute this.
