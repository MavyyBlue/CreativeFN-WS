## 2026-10-17 — Current source and CI

Audited `main` at `3a0b9bc`: Android [38696750855](https://github.com/MavyyBlue/CreativeFN-WS/actions/runs/38696750855) **build passed; launch-smoke failed (16/18 passed)**. Earlier Kotlin repairs are imported. A new, unimported candidate corrects lazy-menu test targeting, improves long-choice pickers, and checks selection/readback/clear. Its 13 Python tests pass; Android validation awaits source import/CI because the local Gradle download is unreachable.

There are 208 catalog entries, including two HUD Message representations; menus/defaults/context remain unverified. [Current device support](docs/DEVICE_SUPPORT_STATUS.md) supersedes older coverage assumptions below. No new APK installation is requested yet.

---

## 2026-10-14 — Comprehensive device menu audit and scalable configuration editor (source candidate, **not** full-device certification)

- User requirement: do not request phone testing until every Creative 1.0 device has a trustworthy current menu/port inventory and all device configuration and blueprint operations work; distinguish blueprint coverage from gameplay simulation.
- Research archive cross-walk: 208 catalog devices (original eight + 200 research), 3,745 setting labels, 529 new event labels and 978 new function labels. Original archive has 213 records; six out-of-scope/retired/brand-scope are excluded, and the seven existing-device matches remain stable.
- Expanded the selector metadata from 153 to **200** reference records by parsing originally string-formatted documentation option expressions; **3,685 / 3,745** extracted setting fields now have at least one documented choice or a clearly typed "enter numeric/text" cue, and **60** still lack usable picker evidence. Six research entries have no documented settings in the source. No full live-menu verification has occurred; defaults, contextual filtering, and availability require in-game capture.
- New searchable device-config Settings / Events / Functions tabs render menu rows lazily, so devices with 50–122 settings do not eagerly construct all field controls. Existing eight device options preserve their types, values, IDs, names, serialized formats, and graph behavior. Device search also matches category, event and function descriptions.
- Added ``docs/CREATIVE1_DEVICE_VERIFICATION_QUEUE.md``, full 208-record CSV, and machine-readable audit JSON; prioritize screenshots of Switch, Timer, Mutator Zone, Item Granter, Conditional Button, then original eight/Player Counter/Prop Mover and other source gaps. See capture instructions in queue.
- **Not yet full support.** The parser treats every imported value as a research suggestion, NOT a verified Fortnite default. Do not release as fully supported, alter Room/wire schema, or claim universal simulator fidelity. The prior graph, overlay, signed CI, importer and model IDs were not changed.
- Validation pending: new candidate Android Gradle, strict Lint, connected test, physical phone test **after verification**; local importer contract/checksums can be checked now.

## 2026-10-14 — Blueprint settings and signal visualization

The priority of CreativeFN WS is **plan → configure authentic Creative 1.0 device choices → connect Event/Function bindings → save blueprint → consult floating overlay while building in Fortnite**. The optional simulator checks **communication**, not world physics or a 1:1 Fortnite runtime. Research device settings now use source-backed selector dialogs where documented; unknown defaults are unspecified, never silently invented. Event values are **derived**, not filled into Actions. Trace-guided node highlights, moving wire signals, and fading bubbles offer an intuitive view of communication, with the detailed console still available. This is new Android source awaiting CI, not an accepted APK. Some Fortnite option menus and physical effects remain unverified. Motion VFX uses the viewport at test entry; if you pan or zoom during testing, exit and reopen the simulator to realign before running another test.

## Value testing (2026-10-14 candidate)

CreativeFN WS plans direct Creative 1.0 device bindings. Reference-only device entries are **not** a certified copy of every current Fortnite settings menu. In Value Test, open **Actions +**, choose/search a documented event, supply an actor, then **Queue event** and **Run** or **Step**. The event is **injected**: collision, gameplay prerequisites, team/player state and other physical conditions were not simulated. The values output identifies modeled state transitions and explicitly labels unsupported function effects.

**Replay** repeats the recorded manual inputs in order from a reset, processing each fully before the next. **Clear inputs** empties only the in-memory test scenario. **Copy report** puts a plain-text state/trace report on the Android clipboard; it is not uploaded or saved in the blueprint. The scenario and trace disappear when leaving the test scene. Runtime remains limited; test the real device settings and mechanics inside Fortnite before publishing.

CI status: current predecessor `b35e5d49` built successfully in run `38526399064`; emulator runner could not install its SDK ZIP, so smoke tests have **not** passed for that predecessor. The new candidate needs fresh Android build, strict Lint, emulator smoke, fixed signing check and phone acceptance.

> **CI correction (2026-10-13):** Android run [38499156610](https://github.com/MavyyBlue/CreativeFN-WS/actions/runs/38499156610) compiled and passed Lint/JVM checks, but 1 of 17 emulator UI tests failed because the Mutator Zone search field and device card shared the same text matcher. The next source ZIP fixes only that test selector; it does not modify production or certify the device expansion. Await new green build + launch-smoke and phone acceptance.

# CreativeFN WS / Creative Logic Mobile

An **offline Android workshop** for building Fortnite Creative **1.0** device-mechanic plans on a phone. Create a logic graph, configure devices, wire Event → Function ports, test a deliberately bounded deterministic sandbox, save the mechanism and produce step-by-step construction blueprints. A user-started, passive Android floating FN overlay lets you consult Steps, Links/folders and an expandable Graph while Fortnite is open. It never inspects or controls Fortnite.

**Latest certified predecessor:** [GitHub Actions run 38495461646](https://github.com/MavyyBlue/CreativeFN-WS/actions/runs/38495461646), commit `b531f0b70215b5bb7a445f2c9c35c89d515d6bc9`; owner confirms the decluttered editor is phone-accepted. The following broad research catalog and value-signal source package is **not yet CI- or phone-certified**.

## Mobile builder flow

Create or open a mechanic → tap **Tools** in the upper-left graph corner for **Devices**, **Bindings**, **Fit** or **Simulate** → tap **Check** in the right rail for actionable configuration and wiring findings → fix the indicated devices → **Simulate** only supported scenarios with Actions / Step / Run / Pause / Reset and trace/state output → Save → Blueprints → Guide/checklist → Overlay → approve permission → return to Fortnite → tap the FN bubble. The overlay supports separate orientation preferences, movable/resizable panel, Cover Flow Steps/Links/Graph navigation, graph pinch/drag/Fit, one-hop connection focus and scrollable expansion within tapped nodes. Stop via the foreground notification. `.c1logic` import/export uses Android's document picker.

Eight legacy device types remain stable: Trigger, Tracker, Barrier, HUD Message, Button, Round Settings, Creature Manager and Elimination Manager. The new source candidate adds 200 labeled documentation-scope REFERENCE-ONLY device records (208 catalog entries total) and a manual Actions event tester for graphs using any new device; it does not imply Fortnite-complete in-game menus or implemented functions for every device. The catalog is a reference subset, **not full Fortnite game emulation**; the simulator has specific option restrictions. The health check distinguishes unusable sandbox configurations from actual graph errors and explicitly calls out unverified fidelity. Warnings are not proof a real Fortnite device is wrong. Consult [diagnostics](docs/DIAGNOSTICS_V1.md), [fidelity notes](FORTNITE_FIDELITY.md) and [current state](docs/CURRENT_STATE.md).

## Build and delivery

JDK 21, Android SDK API 37, Build Tools 37.0.0, Gradle 9.6.0, AGP 9.4.1 and Kotlin 2.2.10 (confirm live version files). Use `./gradlew check lintDebug assembleDebug assembleDebugAndroidTest`; Android `warningsAsErrors` remains enabled. GitHub source import and Android CI are documented in [mobile delivery](docs/MOBILE_DELIVERY.md). Source bundles include hashes and required foundation paths, and do not include `.github`, generated artifacts or keystores. Compare imported commit, CI jobs, APK signer and on-phone acceptance independently.

Room schema 1, graph document version 2, stable device identities, folder metadata and the existing signing setup remain unchanged in Phase 5.1. No account, cloud dependency, invasive permissions, AccessibilityService or gameplay automation is required. See the [roadmap](ROADMAP.md), [QA](QA.md), [architecture](ARCHITECTURE.md) and [overlay constraints](OVERLAY.md).

## Full-height editor and Library navigation candidate (2026-10-13)

The editor graph now occupies the space directly below its header, with no bottom navigation or permanent action rows. The left Tools fan closes after choosing an action; compact right-side controls keep Undo, Redo, Save and Check available. Elsewhere, a hideable Cover Flow-inspired four-card navigation replaces the standard tab bar. Blueprints is now located within **Library**, below **New Mechanic**, instead of on every screen. All existing blueprint services, data and simulation behaviors remain unchanged. Await full Android CI and separate phone confirmation.

## Research reference expansion + value-test Actions (source candidate)

Search **Devices** to place research-backed Creative documentation entries. Their setting names and Event/Function port labels come from the October 13 research archive and official Epic URLs, but default values and precise setting type constraints are deliberately left **unspecified**. Use documented selectors or numeric/text controls where evidence exists; unknown choices stay unavailable. Confirm current menus inside Creative. Unsupported or missing source ports remain unconnected rather than fabricated.

When a graph contains any new reference device, **Simulate** opens the new **VALUE TEST** scene. Expand **Actions +**, select a placed device event, choose an actor, and **Queue event**. Tap **Run** or **Step**. Output shows calculated values and explicit **UNSUPPORTED** markers where a received function has no reliable model. Injection bypasses the triggering game's conditions on purpose. Existing all-legacy graphs keep their original simulator and golden trace behavior.

This is a source-only implementation awaiting Android CI, emulator and real-device confirmation. See `docs/CURRENT_STATE.md` for exact research counts and test boundaries.
