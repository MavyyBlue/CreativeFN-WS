# CreativeFN WS / Creative Logic Mobile

An **offline Android workshop** for building Fortnite Creative **1.0** device-mechanic plans on a phone. Create a logic graph, configure devices, wire Event → Function ports, test a deliberately bounded deterministic sandbox, save the mechanism and produce step-by-step construction blueprints. A user-started, passive Android floating FN overlay lets you consult Steps, Links/folders and an expandable Graph while Fortnite is open. It never inspects or controls Fortnite.

**Latest certified predecessor:** [GitHub Actions run 38495461646](https://github.com/MavyyBlue/CreativeFN-WS/actions/runs/38495461646), commit `b531f0b70215b5bb7a445f2c9c35c89d515d6bc9`; owner confirms the decluttered editor is phone-accepted. The following broad research catalog and value-signal source package is **not yet CI- or phone-certified**.

## Mobile builder flow

Create or open a mechanic → tap **Tools** in the upper-left graph corner for **Devices**, **Bindings**, **Fit** or **Simulate** → tap **Check** in the right rail for actionable configuration and wiring findings → fix the indicated devices → **Simulate** only supported scenarios with Actions / Step / Run / Pause / Reset and trace/state output → Save → Blueprints → Guide/checklist → Overlay → approve permission → return to Fortnite → tap the FN bubble. The overlay supports separate orientation preferences, movable/resizable panel, Cover Flow Steps/Links/Graph navigation, graph pinch/drag/Fit, one-hop connection focus and scrollable expansion within tapped nodes. Stop via the foreground notification. `.c1logic` import/export uses Android's document picker.

Eight legacy device types remain stable: Trigger, Tracker, Barrier, HUD Message, Button, Round Settings, Creature Manager and Elimination Manager. The new source candidate adds 200 labeled documentation-scope REFERENCE-ONLY device records (208 catalog entries total) and a manual Actions event/value tester for graphs using any new device; it does not imply Fortnite-complete in-game menus or implemented functions for every device. The catalog is a reference subset, **not full Fortnite game emulation**; the simulator has specific option restrictions. The health check distinguishes unusable sandbox configurations from actual graph errors and explicitly calls out unverified fidelity. Warnings are not proof a real Fortnite device is wrong. Consult [diagnostics](docs/DIAGNOSTICS_V1.md), [fidelity notes](FORTNITE_FIDELITY.md) and [current state](docs/CURRENT_STATE.md).

## Build and delivery

JDK 21, Android SDK API 37, Build Tools 37.0.0, Gradle 9.6.0, AGP 9.4.1 and Kotlin 2.2.10 (confirm live version files). Use `./gradlew check lintDebug assembleDebug assembleDebugAndroidTest`; Android `warningsAsErrors` remains enabled. GitHub source import and Android CI are documented in [mobile delivery](docs/MOBILE_DELIVERY.md). Source bundles include hashes and required foundation paths, and do not include `.github`, generated artifacts or keystores. Compare imported commit, CI jobs, APK signer and on-phone acceptance independently.

Room schema 1, graph document version 2, stable device identities, folder metadata and the existing signing setup remain unchanged in Phase 5.1. No account, cloud dependency, invasive permissions, AccessibilityService or gameplay automation is required. See the [roadmap](ROADMAP.md), [QA](QA.md), [architecture](ARCHITECTURE.md) and [overlay constraints](OVERLAY.md).

## Full-height editor and Library navigation candidate (2026-10-13)

The editor graph now occupies the space directly below its header, with no bottom navigation or permanent action rows. The left Tools fan closes after choosing an action; compact right-side controls keep Undo, Redo, Save and Check available. Elsewhere, a hideable Cover Flow-inspired four-card navigation replaces the standard tab bar. Blueprints is now located within **Library**, below **New Mechanic**, instead of on every screen. All existing blueprint services, data and simulation behaviors remain unchanged. Await full Android CI and separate phone confirmation.

## Research reference expansion + value-test Actions (source candidate)

Search **Devices** to place research-backed Creative documentation entries. Their setting names and Event/Function port labels come from the October 13 research archive and official Epic URLs, but default values and precise setting type constraints are deliberately left **unspecified**. Enter values as free-text building notes and confirm actual choices inside Creative. Unsupported or missing source ports remain unconnected rather than fabricated.

When a graph contains any new reference device, **Simulate** opens the new **VALUE TEST** scene. Expand **Actions +**, select a placed device event, enter an actor and test value, and **Queue event**. Tap **Run** or **Step**. Output shows calculated values and explicit **UNSUPPORTED** markers where a received function has no reliable model. Injection bypasses the triggering game's conditions on purpose. Existing all-legacy graphs keep their original simulator and golden trace behavior.

This is a source-only implementation awaiting Android CI, emulator and real-device confirmation. See `docs/CURRENT_STATE.md` for exact research counts and test boundaries.
