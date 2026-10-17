## 2026-10-17 — Essential device communication scope (supersedes exhaustive-menu gates)

The owner clarified that device support means **accurate purpose, actual incoming Functions/outgoing Events, and only settings essential to blueprint intent or communication conditions**. Full-menu/default/contextual parity and physical gameplay reproduction are not required. The 60 missing picker domains and six no-settings records are optional research gaps unless they affect that contract. The former full capture batch is withdrawn as a prerequisite. Preserve existing settings/IDs and saved blueprints.

A receiver-only device is a valid endpoint. A Function requesting an effect does not create an outgoing Event or prove the physical result. Report structural validity, signal delivery and relevant configuration support separately from unknown gameplay. The owner's Ball Spawner example illustrates this boundary; this update adds no unverified port or gameplay behavior.

Latest live source `256d99f223a750c1cc3eada47032447154caec24` includes the previous ZIP. Android [39060899455](https://github.com/MavyyBlue/CreativeFN-WS/actions/runs/39060899455): **build PASS; launch-smoke 18/19 passed, 1 failure, 0 skipped**. Downloaded artifact `12111487409` identifies ResearchSettingUiTest: picker list absent after attempted opening. This new, unimported candidate scrolls the setting button into view and waits for the actual dialog before interacting; all selection/readback/clear assertions remain. New Android CI is pending. The clarification is documented direction, not an implemented profile/checker rewrite.

# Device support evidence — 2026-10-17

Audited live `main` at `3a0b9bc14e2c56553a095f9854786fa7226f8295`, not an older source ZIP. This document describes implemented source and remaining evidence; it does not certify Fortnite parity.

## What is actually implemented

| Area | Evidence in source | Remaining boundary |
|---|---|---|
| Catalog | Eight original typed entries plus 200 research entries | 208 entries, **207 distinct display names**; current Creative 1.0 inventory is unverified |
| Original device settings/ports | 20 settings, 7 Events, 19 Functions across the original eight | Typed subsets only; larger counts in older research CSV/JSON are not implemented legacy menus |
| Research settings/ports | 3,745 setting labels, 529 Events, 978 Functions | Documentation-derived labels; current completeness and conditional ports unverified |
| Research picker presentation | All 200 reference IDs/setting keys match the Android metadata; 3,685 settings have choice or input guidance | 60 lack usable control evidence; guidance is not a certified default, numeric range or contextual rule |
| Unknown configuration | Blank stored string means unspecified, not a Fortnite default; unknown domains have a disabled selector and builder notes | Six records have no captured settings; do not infer these devices have no Customize menu |
| Blueprint editing | Searchable/lazy settings, Events/Functions tabs, semantic binding validation, saved graph values and existing guide/overlay paths | Current run has not passed full emulator gate; no exhaustive per-device placement/edit/save/guide/overlay acceptance |
| Local communication | Legacy prototype plus mixed/research ValueSignalSession; documented event injection, bounded routing, explicit unmodeled outcomes | Not every function/setting has semantics; external event injection bypasses gameplay prerequisites |
| VFX | Trace-derived source/receiver highlights, wire signal and bubbles | Simulator-entry viewport is captured; live pan/zoom synchronization and final performance/reduced-motion acceptance remain pending |

Actual counts come from `DeviceCatalog.kt`, `creative1_reference_devices.json`, and `research_setting_choices.json`. The older verification audit also includes documentation-side menus for original devices, so it cannot be used as a count of their implemented controls. No complete, dated in-game verification set is present for the catalog.

The legacy `hud-message` entry has two options/two functions; research `hud_message` has 30 setting labels/three functions. Both display **HUD Message**. Preserve both IDs and existing documents; do not delete or silently remap either. A future consolidation requires explicit compatibility fixtures and a versioned migration.

Six records have no captured settings: Chest and Ammo Gallery, Fishing Rod Barrel, Hover Platform, Items Gallery, Radio, Sword in the Stone. Ten research types contain the 60 missing domains; see [remaining picker gaps](CREATIVE1_REMAINING_PICKER_GAPS.md). Missing picker domains are only part of the work: defaults and contextual menus are still needed for other devices too.

## Current CI and this candidate

Android [38696750855](https://github.com/MavyyBlue/CreativeFN-WS/actions/runs/38696750855) tested the audited SHA. Build/JVM/Lint/APK passed. Actual downloaded report artifact 11955191750: **18 tests, 2 failures, 0 skipped**:

- `SimulatorSceneTest.externalActionAndCollapsibleConsoleStayWithinSavedEditor`: Button not found at line 22, before simulation; lazy catalog rows are not guaranteed to be composed.
- `ResearchSettingUiTest.researchedMutatorZoneUsesDocumentedPickersNotFreeTextFields`: Pre-Game Only not found at line 31, after opening the setting; picker choices are lazy too.

The missing horizontalScroll import/callback repairs are already imported. The Mutator Zone ValueSignalUiTest passed. Emulator provisioning succeeded; this failure is not the older SDK ZIP infrastructure failure.

This unimported candidate searches the catalog before selecting Button and scrolls the tagged picker before selecting Pre-Game Only. Original simulator assertions remain. New assertions verify unspecified/selection/readback/clear. Production pickers dismiss the settings-search keyboard and search longer choice lists; they do not introduce new Fortnite settings, choices or defaults. A UI fixture exercises search/select/clear. Research limitation text is corrected to describe the existing selector-first editor.

Local validation: **13 Python tests passed**, covering importer security/integrity plus matching picker device/setting IDs and allowed evidence control types. The full Android command was attempted but Gradle could not download (`UnknownHostException: downloads.gradle.org`). This candidate is **not Android-tested or committed to GitHub**. Expected emulator suite after import: 19 tests; require the real complete passing report and both CI jobs on the imported SHA. ZIP validation is separate from CI.

## Next work — essential profiles, not exhaustive captures

For each device, audit purpose, documented incoming Functions and outgoing Events, and settings that actually affect blueprint intent or communication. Explicitly identify source-only, receiver-only or both where evidence permits. Verify missing ports or essential conditions with focused references/captures; do not request every menu or default. The previously proposed Switch/Timer/Mutator Zone capture batch is no longer a prerequisite.

The 60 missing domains and six records without captured settings are optional research gaps. They do not themselves make a device unsupported. Existing stored options and IDs remain intact. No full-menu removal or catalog migration is part of this documentation update. Simplified profile implementation and checker wording still need a source audit and focused changes.

## Preserved contract

Room schema 1, graph document format 2, catalog `0.2.0-draft`, all option/port IDs and defaults, signing, importer protections, passive Fortnite overlay, full-height editor, compact Tools fan, Undo/Redo/Save/Check, Library and blueprint guides remain intact. Further work follows the charter: authentic configuration and blueprint support first, bounded communication checks second, VFX third.
