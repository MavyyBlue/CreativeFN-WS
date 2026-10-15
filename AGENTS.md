# CreativeFN WS — agent working rules

**MANDATORY FIRST READ:** [`docs/PROJECT_SOURCE_OF_TRUTH.md`](docs/PROJECT_SOURCE_OF_TRUTH.md). It is the owner's canonical product definition, priority order, known unknowns, completion rules, and fresh-context handoff. On conflict, follow the owner's latest explicit direction; amend the charter.

CreativeFN WS is a **Fortnite Creative 1.0 native Android BLUEPRINT workshop**, not UEFN or a Fortnite game simulator. Priority: **accurate device menus/ports and saved blueprint + overlay** → bounded honest Event → Function checking → trace-driven VFX. Do not substitute catalog breadth or fabricated simulated outcomes for genuine support. **The owner works entirely on an Android phone** and prefers not to test partial device-support candidates.

Before code edits inspect live `main`, latest matching GitHub Actions, README, ROADMAP, CURRENT_STATE, and the actual code. Separate implemented source, green CI, phone acceptance, research hypotheses, and pending ZIPs. Source import only, never generated APKs or signing keys. Run targeted tests, `./gradlew check`, strict lint and `assembleDebug` through Android CI. Require `build` **and** `launch-smoke`; phone acceptance is a separate gate. Never suppress or remove tests to turn builds green. Synchronize the charter, CURRENT_STATE, ROADMAP, QA and CHANGELOG with truthful changes.

Keep pure models and simulators Android-free; preserve UUIDs, semantic event/function ports, serialized formats and persisted data. Unsupported or unverified behavior must be explicit. For missing Fortnite settings, request the owner's focused in-game screenshots/video instead of inventing defaults. Preserve the existing working graph editor, Cover Flow navigation, folders, guides and passive foreground overlay.

Never inspect/control Fortnite processes, files, traffic or screens; no AccessibilityService, screen capture, automation, hooks or macros. The floating overlay remains user-started, passive, and on-device. Keep established signing/privacy/security intact.
