# CreativeFN WS — Fortnite Creative 1.0 in-game verification queue

**Status: not full-fidelity certified.** This is a working capture list, not a claim that every device is present in the current mobile Creative 1.0 inventory. Research was collected on 2026-10-13. Prioritize the current game client over any older documentation.

## What to send for each device

1. Screenshot the device name and every Customize/Device Options page. Scroll through contextual/advanced options.
2. Capture each option **with its actual default** and open selector lists so every choice is visible. If a control accepts a number/text, record range, unit, special values (Infinite, None, etc.) and whether it supports custom entry.
3. Screenshot the **Functions** and **Events** tabs, including entries that appear only after toggles or prerequisites.
4. Record any contextual conditions (e.g., setting X only appears when Y = On), player/team/class restrictions and which devices cannot be placed on ordinary Creative 1.0 islands.
5. Tell me the in-game date/build if visible. If an item is unavailable, say so; no need to hunt for it.

A short screen recording of scrolling the entire menu and opening each picker is also fine. Pictures of **3–5 devices per batch** will be enough to incorporate those faithfully and check progress.

## First batch: highest-priority gaps

| Priority | Device | Docs settings | Documentation selectors | Events | Functions | What is missing |
|---:|---|---:|---:|---:|---:|---|
| 1 | Switch | 25 | 25 | 8 | 12 | Defaults, completeness, contextual options and true menu behavior |
| 2 | Timer | 42 | 42 | 6 | 20 | Defaults, completeness, contextual options and true menu behavior |
| 3 | Mutator Zone | 27 | 27 | 4 | 4 | Defaults, completeness, contextual options and true menu behavior |
| 4 | Item Granter | 20 | 19 | 1 | 8 | Defaults, completeness, contextual options and true menu behavior |
| 6 | Conditional Button | 36 | 36 | 2 | 5 | Defaults, completeness, contextual options and true menu behavior |
| 7 | Trigger | 22 | 0 | 1 | 4 | Defaults, completeness, contextual options and true menu behavior |
| 8 | Tracker | 26 | 0 | 4 | 16 | Defaults, completeness, contextual options and true menu behavior |
| 9 | Button | 13 | 0 | 1 | 2 | Defaults, completeness, contextual options and true menu behavior |
| 10 | Barrier | 16 | 0 | 0 | 5 | Defaults, completeness, contextual options and true menu behavior |
| 11 | HUD Message | Original subset | Original subset | Original subset | Original subset | Defaults, completeness, contextual options and true menu behavior |
| 12 | Round Settings | 22 | 0 | 1 | 7 | Defaults, completeness, contextual options and true menu behavior |
| 13 | Channel | 3 | 3 | 1 | 1 | Defaults, completeness, contextual options and true menu behavior |
| 14 | Score Manager | 19 | 19 | 2 | 7 | Defaults, completeness, contextual options and true menu behavior |

## Full 208-device inventory

The companion CSV includes the full audit. The research token parser can now present 3,685 of 3,745 researched settings as documentation-derived selectors or inputs, across 194 devices; 60 lack usable picker evidence and six records have no captured settings. No device is a fully verified in-game menu. **RESEARCH_SELECTOR_PARTIAL** means a documentation-backed picker exists but its complete live menu/default is not verified; **RESEARCH_MENU_GAP** means no usable choices could be extracted; **NO_DOCUMENTED_SETTINGS** means the captured source lists no options; **BASELINE_TYPED_PARTIAL** means we have a typed original eight-device subset, not its complete menu.

| Device | State | Missing selector labels (up to six examples) |
|---|---|---|
| Switch | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Timer | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Mutator Zone | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Item Granter | RESEARCH_SELECTOR_PARTIAL | Item to Equip |
| Conditional Button | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Trigger | BASELINE_TYPED_PARTIAL | Trigger Sound, Visible in Game, Activating Team, Activating Class, Invert Class Selection, Triggered by Player… |
| Tracker | BASELINE_TYPED_PARTIAL | Stat to Track, Reset on First Spawn, Target Value, Starting Value, Valid Team, Assign on Game Start… |
| Button | BASELINE_TYPED_PARTIAL | Interact Time, Activating Team, Invert Team Selection, Allowed Class, Invert Class Selection, Times Can Trigger… |
| Barrier | BASELINE_TYPED_PARTIAL | Enabled During Phase, Barrier Material, Zone Shape, Barrier Depth, Barrier Width, Barrier Height… |
| HUD Message | BASELINE_TYPED_PARTIAL | Defaults/current-menu verification |
| Round Settings | BASELINE_TYPED_PARTIAL | Round, Override Keep Items Between Rounds, Keep Items Between Rounds, Reset Class Each Round, Wood Given Per Round, Metal Given Per Round… |
| Channel | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Score Manager | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Random Number Generator | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| End Game | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Prop Mover | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Teleporter | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Capture Area | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Team Settings & Inventory | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Class Designer | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Guard Spawner | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Creature Manager | BASELINE_TYPED_PARTIAL | Creature Type, Health, Score, Damage to Player, Damage to Environment, Movement Speed… |
| Elimination Manager | BASELINE_TYPED_PARTIAL | Enabled At Game Start, Number of Items Dropped, Valid on Self-Elimination, Target Type, Initial Movement of Item, Random Drop… |
| Item Spawner | RESEARCH_SELECTOR_PARTIAL | Initial Secondary Weapon Ammo, Spare Secondary Weapon Ammo |
| Progress Based Mesh | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Accolades | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Advanced Storm Controller | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Advanced Storm Controller Beacon | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| AI Navigation Modification | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| AI Patrol Path Node | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Air Vent | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Analytics | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Armored Battle Bus Spawner | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Armored Transport Spawner | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Ascender | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| ATK Spawner | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Attribute Evaluator | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Audio Mixer | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Audio Player | RESEARCH_SELECTOR_PARTIAL | Audio |
| Automated Turret | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Ball Spawner | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Baller Spawner | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Bank Vault | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Basic Storm Controller | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Beacon | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Big Rig Spawner | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Billboard | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Biplane Spawner | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Boat Spawner | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Bomb Flower | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Bouncer Gallery | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Campfire | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Cannon Spawner | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Capture Item Spawner | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Carryable Spawner | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Chair | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Changing Booth | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Character | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Character Device Controller | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Chest and Ammo Gallery | NO_DOCUMENTED_SETTINGS | Defaults/current-menu verification |
| Class Selector | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Class Selector UI | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Collectible Object | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Crash Pad | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Creature Placer | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Creature Spawner | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Crowd Volume | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Customizable Light | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| D-Launcher | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Damage Amplifier Powerup | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Damage Volume | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Dance Mannequin | RESEARCH_SELECTOR_PARTIAL | Custom Character Cosmetics Default, Custom Character Cosmetics Preset 2, Custom Character Cosmetics Preset 3 |
| Day Sequence | RESEARCH_SELECTOR_PARTIAL | Trigger Volume, Enable Sun Component, Intensity, Color, Enable Flare, Rotation Z… |
| Developer Profile Link | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Dirt Bike Spawner | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Disguise | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Down But Not Out | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Driftboard Spawner | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Earth Sprite | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Explosive | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Fang Spawner | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Fire Volume | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Firefly Spawner | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| First Person Camera | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Fishing Rod Barrel | NO_DOCUMENTED_SETTINGS | Defaults/current-menu verification |
| Fishing Zone | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Fixed Angle Camera | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Fixed Point Camera | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Fuel Pump | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Grind Powerup | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Grind Rail | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Hammerhead Choppa Spawner | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Healing Cactus | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Health Powerup | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Heavy Turret | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Helicopter Spawner | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Hiding Prop Gallery | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Holoscreen | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Hover Platform | NO_DOCUMENTED_SETTINGS | Defaults/current-menu verification |
| HUD Controller | RESEARCH_SELECTOR_PARTIAL | Alignment, Anchor |
| HUD Message | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Input Trigger | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Item Placer | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Item Remover | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Items Gallery | NO_DOCUMENTED_SETTINGS | Defaults/current-menu verification |
| Level Instance | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Level Loader | RESEARCH_SELECTOR_PARTIAL | Level Instance Name |
| Lock | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Map Indicator | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Matchmaking Portal | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Melee Designer | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Message Feed | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Movement Modulator | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Nitro Drifter Spawner | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Objective | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Octane Spawner | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Orbit Camera | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Patchwork Cable Splitter | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Patchwork Distortion Effect | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Patchwork Drum Player | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Patchwork Drum Sequencer | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Patchwork Echo Effect | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Patchwork FX Filter | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Patchwork FX Gain | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Patchwork Instrument Player | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Patchwork LFO Modulator | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Patchwork Music Manager | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Patchwork Note Progressor | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Patchwork Note Sequencer | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Patchwork Note Trigger | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Patchwork Omega Synthesizer | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Patchwork Song Sync | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Patchwork Speaker | RESEARCH_SELECTOR_PARTIAL | Music Event Group |
| Patchwork Step Modulator | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Patchwork Value Setter | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Perception Trigger | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Physics Boulder | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Physics Tree | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Pickup Truck Spawner | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Pinball Bumper | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Pinball Flipper | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Placeable Ledge | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Player Checkpoint | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Player Counter | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Player Marker | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Player Movement | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Player Reference | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Player Spawner | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Pop-Up Dialog | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Prop Manipulator | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Prop-O-Matic Manager | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Pulse Trigger | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Quadcrasher Spawner | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Race Checkpoint | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Race Manager | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Radio | NO_DOCUMENTED_SETTINGS | Defaults/current-menu verification |
| Real Time Clock | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Reboot Van Spawner | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Rift Point Volume | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Rocket Boost Powerup | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Rocket Racing Boost Pad | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Rocket Racing EMP Volume Hazard | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Rocket Racing Vehicle Spawner | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Roly Poly | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Save Point | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Scout Spire | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Sedan Spawner | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Sentry | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Shopping Cart Spawner | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Side Scroller Controls | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Siege Cannon | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Signal Remote Manager | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Skilled Interaction | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Skydive Volume | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Slurp Plant | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Sportbike Spawner | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Sports Car Spawner | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Stat Counter | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Stat Creator | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Stat Powerup | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Stink Flower | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Supply Drop Spawner | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Surfboard Spawner | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| SUV Spawner | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Sword in the Stone | NO_DOCUMENTED_SETTINGS | Defaults/current-menu verification |
| Tank Spawner | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Target Dummy | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Target Dummy Track | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Taxi Spawner | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Third Person Controls | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Timed Objective | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Trick Tile | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| UFO Spawner | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Vehicle Mod Box Spawner | RESEARCH_SELECTOR_PARTIAL | Paint Color, Icon, Custom List Mod 2, Custom List Mod 3, Custom List Mod 4, Custom List Mod 5… |
| Vehicle Service Station | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Vending Machine | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| VFX Creator | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| VFX Spawner | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Video Player | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Visual Effect Powerup | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Volume | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Voting Group | RESEARCH_SELECTOR_PARTIAL | Custom Widget |
| Voting Options | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| War Bus Spawner | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Water | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Weapon Mod Bench | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Wildlife Spawner | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |
| Zipline | RESEARCH_SELECTOR_PARTIAL | Defaults/current-menu verification |

## Excluded from normal Creative 1.0 device claims

Cinematic Sequence and NPC Spawner are marked UEFN-only by the source dataset; Skydome is marked retired; Track UEFN-only; Hero and LEGO Assembly are brand/island-scoped. If any are available in your regular Creative 1.0 inventory, capture the game UI to reopen them.

## Interpretation

Event and function tables are **research references** and may be incomplete even when nonempty. In-app bindings can be drawn and saved for recorded ports. Verification of game side conditions requires real in-game evidence. A working blueprint planner is not the same as an accurate simulator of gameplay.
