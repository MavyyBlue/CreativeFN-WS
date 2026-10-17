# CreativeFN WS — Project source of truth

**This is the canonical project charter and context-reset handoff.** Review it before making changes. It defines the owner's intent and completion criteria, **not** proof that a feature has been implemented. For implemented behavior, inspect live code at `main`; for certified behavior, check GitHub Actions and the owner's separate phone acceptance. If a newer direct owner instruction conflicts with this document, follow the owner and update this charter accordingly.

**Repository:** `MavyyBlue/CreativeFN-WS` — <https://github.com/MavyyBlue/CreativeFN-WS> — `main`.
**Product:** CreativeFN WS (internal name Creative Logic Mobile), native **Android-only**, phone-only development and distribution via GitHub Actions. The owner does **not** have a PC.
**Snapshot of verified live source at charter creation, 2026-10-14:** `739a3cc082f2bcd572a12a75fcfbcce54f34bdd0`; GitHub Android Actions [run 38574784317](https://github.com/MavyyBlue/CreativeFN-WS/actions/runs/38574784317): both `build` and `launch-smoke` **PASS**. This is CI evidence, not Creative 1.0 menu-fidelity certification. The comprehensive source ZIP being delivered with this charter was **not yet imported/CI tested** at the moment of writing. Check the HEAD and actual CI again after import.

## Current implementation evidence — 2026-10-17 (supersedes snapshot status)

Live `main` is `3a0b9bc14e2c56553a095f9854786fa7226f8295`. Android run **38696750855** passed build/JVM/Lint/APK assembly and failed launch-smoke: downloaded report **16/18 passed, 2 failed, 0 skipped**. Failures are lazy catalog Button selection and lazy research picker Pre-Game Only selection; the old Kotlin compile errors are already fixed and the Mutator Zone value test passed. The earlier comprehensive picker/menu candidate is now imported, but is not fully CI-certified.

The next ZIP is a separate, unimported repair/usability candidate. It adds explicit test search/scroll, checks chosen value readback/clearing, dismisses the settings-search keyboard when opening a picker, and searches long choice lists. Local Python importer/asset suite: **13 passed**. Android validation is pending: the local Gradle distribution download was blocked by DNS. Preserve both CI jobs and wait for device-support readiness before requesting installation.

Recounted implementation: original eight **20 options / 7 events / 19 functions**; research 200 **3,745 options / 529 events / 978 functions**. Picker metadata matches all 200 reference IDs. **60** research setting domains are missing, six records have no settings captured, and defaults/context visibility remain unverified. **208 entries represent 207 distinct display names** because `hud-message` and `hud_message` overlap. Preserve both IDs pending a separately tested versioned consolidation; do not silently merge/delete saved devices. Older audit CSV/JSON counts describe research inventory, not all implemented legacy menus. [DEVICE_SUPPORT_STATUS.md](DEVICE_SUPPORT_STATUS.md) records this distinction and the small next capture batch.

## 1. North star — blueprint creator first

We are making a **mobile visual planning, troubleshooting, and construction companion for Fortnite Creative 1.0**. We are **not** making UEFN, a Fortnite clone/emulator, a fully simulated Fortnite game engine, or an app that accesses Fortnite's processes or controls.

The main user workflow is:

1. **Design** a Creative 1.0 mechanic with actual devices, native-feeling configuration, arranged graph nodes and proper **Event → Function** bindings.
2. **Optionally verify communication:** trigger manually declared external scenarios in the app; check that supported event/function signals reach the intended targets, obey verified relevant enable/team/class/phase rules and produce only *defensible modeled* state changes. This is a compact diagnostic tool, not the core product.
3. **Save and organize** device graph, settings, bindings and layout as a blueprint. Support folders, construction steps/checklist, readable links, import/export and persistence.
4. **Build inside Fortnite** on the same Android phone while viewing that saved blueprint through the passive, compact floating overlay. The overlay is a reference, not an automation or live connection to Fortnite.

**If a development idea competes with blueprint accuracy/overlay usability, prioritize the blueprint.** No new phases or speculative simulator expansion until the owner agrees.

## 2. Explicit owner UX decisions

- **Device configuration feels like the in-game Creative 1.0 device UI.** Dropdowns and selection menus for finite choices, toggles for booleans, constrained controls for numbers/durations, appropriate text/asset selectors, contextual options. Never use an arbitrary text field to fake a selector when evidence is missing. Keep the original eight devices' stable settings and recognizable editor behavior.
- **Never invent** Fortnite options, defaults, min/max, context visibility, support claims, events or functions. Research labels and suggested options are **not verified current menus**. Record the source and evidence level of each. Unknowns must remain unverified and be requested from the owner.
- **Builder input is not simulator output.** Do **not** ask a builder to type an arbitrary "output value". In simulation the builder declares an event/scenario (e.g. Interact, Player Entered Zone, Game Start), and the graph's event/function/state logic determines the outcome.
- **No false environment incompatibilities.** A graph is not wrong because our local verifier doesn't model Fortnite world physics, HUD visuals, teams, actors, NPCs, or game phases. A team/class/phase/enable restriction is blocking only when the relevant documented condition and current scenario are modeled and demonstrably mismatched. Unknown results = "not modeled / verify in Fortnite", not a failed binding or fake success.
- **Distinguish four outcomes:** (a) documented/structurally valid binding; (b) function signal actually delivered in local test; (c) modeled, defensible state effect; (d) actual Fortnite result (requires in-game testing). Never equate (b) or (c) with (d).
- **Graph-first verification visualization:** source node's outlined glow makes one lap; a glowing orb travels along each *actual saved* wire; receiver's outline laps; a temporary output/dialogue bubble above the relevant device shows the real trace result and fades. Distinguish delivered, changed, blocked, and unsupported. Keep console/trace available as optional advanced output. Visuals are driven by deterministic simulation traces, never by fabricated successful effects; align under pan/zoom, respect performance and reduced motion.
- Preserve owner-accepted interface direction: full-height graph under header; upper-left collapsing Tools fan (Devices / Bindings / Fit / Simulate), compact right-side Undo/Redo/Save/Check; hideable Cover Flow-style in-app navigation outside Editor; Library-based Blueprints entry; resizable and orientation-aware Fortnite floating overlay with Steps/Links/Graph, pan/zoom/Fit and expandable scrollable nodes.

## 3. Device support means five separate kinds of evidence

**Never announce "all 208 devices fully supported" on the strength of catalog count.** Track each dimension explicitly:

| Dimension | Completion criterion |
|---|---|
| Inventory | Confirm currently in Fortnite Creative **1.0**, with excluded UEFN-only, retired, brand-limited, or otherwise out-of-scope devices distinguished |
| Configuration | Complete settings/choices/defaults/conditional visibility with exact controls and correctly persisted values; match in-game menu and saved blueprint/overlay readback |
| Ports/bindings | Correct device-specific Events, Functions, source/target validation, semantic IDs, saved bindings and fanout |
| Local verification | Propagation and supported prerequisites/effects modeled honestly; unsupported/world-dependent effects flagged, not silently fabricated |
| Blueprint delivery | Place, edit, connect, rename, duplicate, save/reopen, folder/guide/overlay and `.c1logic` import/export all work, including large devices |

**Live `main` at the charter snapshot:** eight original typed reference devices (**Trigger, Tracker, Barrier, HUD Message, Button, Round Settings, Creature Manager, Elimination Manager**) plus 200 additive Creative documentation/reference definitions, **208 catalog entries total**. The live researched-device definitions were still loaded as generic `TEXT` options with unspecified defaults by `ResearchDeviceDefinitions.kt`. These entries had labels and scoped documented ports but did not have certified complete settings menus or complete Fortnite simulation semantics. Real total device inventory may vary with Fortnite updates.

**Prepared comprehensive ZIP candidate (separate from the live snapshot):** current research crosswalk covers 200 additional reference records and 3,745 extracted setting labels. Parser-backed picker guidance was reconstructed for **3,685** setting labels; **60 missing picker/value domains across 10 device types** remain according to the candidate audit, plus **six reference records with zero captured settings**. Choice guidance is **not** proof of current live default values or contextual filtering. The ZIP includes:

- `docs/CREATIVE1_DEVICE_VERIFICATION_AUDIT.csv`
- `docs/CREATIVE1_DEVICE_VERIFICATION_SUMMARY.json`
- `docs/CREATIVE1_DEVICE_VERIFICATION_QUEUE.md`
- `docs/CREATIVE1_REMAINING_PICKER_GAPS.md` and `.csv`
- `app/src/main/assets/research_setting_choices.json`

On first use after import, **audit the actual new HEAD** and amend the counts; do not automatically promote this candidate to complete/phone-accepted. The candidate's UI picker metadata is separate from the older serialized option types, protecting existing `.c1logic` files while compatibility is evaluated.

## 4. Owner-assisted in-game research

The owner **explicitly volunteered** to capture missing settings directly inside Fortnite on the phone. When blocked by incomplete evidence, create a **small device-specific verification list** rather than make guesses or demand every device at once. Start from the candidate's `CREATIVE1_REMAINING_PICKER_GAPS.md`, and also verify completeness beyond listed picker gaps: current in-game availability, first-load defaults, contextual menus, Events/Functions and any missing menu rows. Initial high-value examples: **Switch, Timer, Mutator Zone, Item Granter, Conditional Button and Day Sequence**.

Ask for screenshots or a screen recording that shows (1) device name, (2) initial settings and default choice, (3) all dropdown values, (4) settings revealed/hidden when a related choice changes, and (5) Events and Functions. If current device has no customize menu or was removed, record that explicitly. Record a version/date and source per verified finding where possible. Do not infer the absence of a feature from an incomplete recording.

## 5. Implementation priorities and stopping rules

**First:** complete Creative 1.0 device support for the **blueprint** experience, using real settings, typed controls, complete documented ports, safe persistence, large-menu performance and pass-through to overlay/guide. The user explicitly prefers **not to install/phone-test successive partial device-support builds**. Continue desktop-free local/JVM/CI checks and request targeted research rather than repeatedly asking for installation.

**Second:** polish Event → Function communication verification across all catalog device categories. Keep the old deterministic `PrototypeSimulationSession` golden scenarios while cautiously unifying user-facing behavior with the research/mixed-graph `ValueSignalSession`. Build required-world-state rules only when evidenced; avoid blanket "incompatible" errors due to simulation limitations. No manual arbitrary output values.

**Third:** finish the trace-linked visual effects (node outline lap, traveling wire signal, receiver highlight, short fading output bubble, fanout, blocked/unsupported indication, alignment under transforms, performance and reduced motion). The animation is subordinate to correct logic and stable blueprint editing.

**Only after these** should we jointly plan more features/phases. Full gameplay/3D engine simulation is out of scope.

## 6. Technical and safety invariants

- Owner only has an Android phone. Deliver signed-installable APKs using the established GitHub Actions workflow, and source updates through `creative-logic-mobile-source.zip`, existing importer, manifest and SHA-256 checks. No PC-only commands for the owner.
- Preserve repository `main` and installed app continuity, signing certificate/key confidentiality, Android package ID, existing Room database, document/graph serialization, UUIDs, Events/Functions, user settings, foreground overlay service and permissions. For any real change to stored option **type/key/default**, use a versioned migration with regression tests; do not silently reset user blueprints.
- No interaction with Fortnite processes, data, network, or UI; no AccessibilityService, Fortnite screen capture or gameplay macros. Overlay must be passive and user-started.
- The source importer deliberately rejects `.github` workflow files, private signing keys, generated APKs, and unsafe paths. Workflow changes require a distinct authorized GitHub operation. Never put sensitive credentials into ZIPs, docs or conversation.
- Before modifying source, check live GitHub HEAD and current CI, not just an older ZIP. Rebase/recreate any candidate if HEAD has changed.
- Quality gate: actual source-import SHA/path/checksum validation; Kotlin/JVM unit tests and golden traces; strict Android Lint, Android build, emulator instrumentation (`build` and `launch-smoke` both green on the tested commit), signature continuity, then separate owner phone acceptance when the owner is ready. State uncertainty if any gate is not run.
- Prior red-build causes: `ValueSignalUiTest` ambiguous Compose text selector for `Mutator Zone`; select by stable semantics/test tag/clickability rather than matching an editable search field. Another run failed during Android Emulator SDK ZIP installation before tests; distinguish infrastructure failures from app regressions. Strict Lint previously rejected custom canvas clipping without appropriate AndroidX helpers. Do not weaken/remove tests to manufacture green.

## 7. Context reset protocol

**At the start of every new chat, Codex session, or major development pass:**

1. Read `AGENTS.md`, then **this file**; inspect `README.md`, `ROADMAP.md`, `docs/CURRENT_STATE.md` for supporting historical detail (not authority over the charter).
2. Read current GitHub `main` HEAD and corresponding recent workflow run. Compare with any pending comprehensive ZIP and determine what is **already committed** versus **generated but not imported**.
3. Inspect actual catalog/resource/configuration/simulator/graph/overlay code. Verify the live support matrix and pending in-game footage/gaps. Ask for evidence instead of guessing.
4. Work in the priority order in §5. Keep changes compatible and testable; distinguish proven CI/phone acceptance from unverified candidates.
5. When owner decisions, support status, or CI gates change, **update this charter and relevant QA/roadmap/docs** in the same import or commit when possible.

Suggested new-conversation opener:

> Continue CreativeFN WS from `MavyyBlue/CreativeFN-WS` `main`. First read `AGENTS.md` and `docs/PROJECT_SOURCE_OF_TRUTH.md`, then inspect live HEAD/CI. This is an **Android-only Fortnite Creative 1.0 blueprint workshop**, not UEFN and not a Fortnite simulator. Prioritize full documented device configuration/ports and blueprint usage, then accurate bounded Event → Function checks, then trace-driven signal VFX. Do not confuse catalog entries with verified menus, or a generated source ZIP with imported/CI-tested code. Preserve the installed app, signing, data and overlay. Report implemented vs unverified and request focused Fortnite screenshots when settings are missing.

**Evidence precedence:** current direct owner request/approval → live repository and matching CI/device evidence for implementation → this product charter and its updated decisions → older plans/research/chat summaries.

## 8. Charter maintenance

**2026-10-14:** Created canonical source of truth based on the owner's repeated blueprint-first clarifications, preference for completed device support before phone testing, offer to provide in-game evidence, and source/C I audit. Document creation does **not** certify any new device or simulated behavior. Keep this section current whenever the project's actual tested state changes.

**2026-10-17:** Re-audited imported HEAD and downloaded CI report, corrected stale pending-repair status, documented actual legacy/reference coverage and the HUD Message overlap, and recorded the separate unimported lazy-menu repair candidate. No newly verified Fortnite menus or gameplay behavior.
