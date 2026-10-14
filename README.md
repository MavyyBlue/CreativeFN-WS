# CreativeFN WS / Creative Logic Mobile

An **offline Android workshop** for building Fortnite Creative **1.0** device-mechanic plans on a phone. Create a logic graph, configure devices, wire Event → Function ports, test a deliberately bounded deterministic sandbox, save the mechanism and produce step-by-step construction blueprints. A user-started, passive Android floating FN overlay lets you consult Steps, Links/folders and an expandable Graph while Fortnite is open. It never inspects or controls Fortnite.

**Latest owner-accepted APK baseline:** [GitHub Actions run 38474555365](https://github.com/MavyyBlue/CreativeFN-WS/actions/runs/38474555365), commit `9a05ebbd84f1b17bca8fe454ec25bff02e3ffd35`. Both Android `build` and `launch-smoke` passed; the owner confirmed the Cover Flow navigator and inline graph-node expansion above Fortnite on a real phone. **New Phase 5.1 health-check source is an uncertified candidate** until its own import, full Android CI and phone tests finish.

## Mobile builder flow

Create or open a mechanic → use **Devices** and **Bindings** → tap **Check mechanic** for actionable configuration and wiring findings → fix the indicated devices → **Simulate** only supported scenarios with Actions / Step / Run / Pause / Reset and trace/state output → Save → Blueprints → Guide/checklist → Overlay → approve permission → return to Fortnite → tap the FN bubble. The overlay supports separate orientation preferences, movable/resizable panel, Cover Flow Steps/Links/Graph navigation, graph pinch/drag/Fit, one-hop connection focus and scrollable expansion within tapped nodes. Stop via the foreground notification. `.c1logic` import/export uses Android's document picker.

Eight installed device types: Trigger, Tracker, Barrier, HUD Message, Button, Round Settings, Creature Manager and Elimination Manager. The catalog is a reference subset, **not full Fortnite game emulation**; the simulator has specific option restrictions. The health check distinguishes unusable sandbox configurations from actual graph errors and explicitly calls out unverified fidelity. Warnings are not proof a real Fortnite device is wrong. Consult [diagnostics](docs/DIAGNOSTICS_V1.md), [fidelity notes](FORTNITE_FIDELITY.md) and [current state](docs/CURRENT_STATE.md).

## Build and delivery

JDK 21, Android SDK API 37, Build Tools 37.0.0, Gradle 9.6.0, AGP 9.4.1 and Kotlin 2.2.10 (confirm live version files). Use `./gradlew check lintDebug assembleDebug assembleDebugAndroidTest`; Android `warningsAsErrors` remains enabled. GitHub source import and Android CI are documented in [mobile delivery](docs/MOBILE_DELIVERY.md). Source bundles include hashes and required foundation paths, and do not include `.github`, generated artifacts or keystores. Compare imported commit, CI jobs, APK signer and on-phone acceptance independently.

Room schema 1, graph document version 2, stable device identities, folder metadata and the existing signing setup remain unchanged in Phase 5.1. No account, cloud dependency, invasive permissions, AccessibilityService or gameplay automation is required. See the [roadmap](ROADMAP.md), [QA](QA.md), [architecture](ARCHITECTURE.md) and [overlay constraints](OVERLAY.md).
