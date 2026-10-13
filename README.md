# CreativeFN WS / Creative Logic Mobile

An offline Android companion for manually constructing Fortnite Creative 1.0 device logic. The original Phase 3.2 graph editor, Room Library and deterministic TEST SCENE remain unchanged. All Fortnite game behavior is a scoped experiment, not a 1:1 emulator.

**2026-10-13 source candidate — pending Android CI and device verification.** The new **Blueprints** control accesses saved mechanics from any application screen. Save the graph, open **Guide** for a device bill of materials, exact saved option values, bindings and checklist; choose **Overlay** to request Draw Over Other Apps and start a small passive FN reference bubble with Steps, Devices, Links and zoomable Graph. A persistent notification has Stop Overlay. `.c1logic` import/export uses the local Android document picker and preserves the original graph document JSON format.

How to use: Create → Devices and Bindings → Simulate → Save → Blueprints → Guide → Overlay → approve permission → return to Fortnite → tap FN bubble. Collapse and reopen without leaving Fortnite. Settings and checkbox progress refer to the frozen saved graph; save and launch the overlay again for newly edited changes. The overlay is subject to OEM and Fortnite restrictions that require phone testing. It does not inspect or control the game.

Build and CI: JDK 21, Android SDK API 37 and Gradle 9.6 wrapper. Use `./gradlew check lintDebug assembleDebug assembleDebugAndroidTest`. Android emulator jobs and physical phone tests are separate acceptance gates. See [current state](docs/CURRENT_STATE.md), [roadmap](ROADMAP.md), [overlay boundaries](OVERLAY.md), [fidelity ledger](FORTNITE_FIDELITY.md), and [mobile delivery](docs/MOBILE_DELIVERY.md).

No account, internet connection, cloud service, file-storage permission, AccessibilityService or background simulation is used. Do not regenerate existing signing keys. The existing Room schema 1 is not migrated; the update should install over a matching signed APK, but check actual certificate identity before replacing the installed app.
