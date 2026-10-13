# Roadmap — CreativeFN WS

Implementation status is based on source inspection and local checks. **Only GitHub Actions plus owner phone checks certify Android behavior**; simulator behavior is NOT 1:1 Fortnite certified.

| Phase | Scope | Source status (2026-10-13) |
|---|---|---|
| 0 | Android, Room, CI, mobile source importer | Certified through CI run 38338236226 |
| 1 | Device catalog (8 scoped devices) | Established, live Fortnite fidelity pending |
| 2 | Canvas, direct bindings, history, autosave | Established, CI certified |
| 3.1–3.2 | Deterministic sandbox, manual test actions, trace and state inspection | Phase 3.2 certified CI and owner accepted on Android |
| 4 | Expanded catalog, live verification | Deferred: only 8 existing devices; no fabricated support |
| 5 | Validation, actionable debug | Graph invariant checks and simulator trace already exist; comprehensive UX not completed |
| 6 | Local mechanics, search, portable import/export, folders | Existing persistent searchable Library and .c1logic import/export; per-mechanic device folders (candidate), Library-wide folders and thumbnails deferred |
| 7 | Fortnite construction guide | New shared guide generator with device quantities, configured values, bindings, sequence, checklist, caveats; **first release CI passed; additional phone checks pending** |
| 8 | Android system overlay | New explicitly started foreground service and TYPE_APPLICATION_OVERLAY bubble/panel with drag, graph zoom/pan, step persistence, notification Stop; **Android CI green for previous build; owner confirms compact landscape Graph renders; new Links/focus features CI pending** |
| 9 | UX/lifecycle hardening | Initial sizing, step/bubble persistence and passive lifecycle; interactive overlay device inspector + connection focus candidate; profiling pending |

## Immediate Android acceptance gate

Import this source ZIP, confirm GitHub Actions build and emulator jobs pass, then check signature continuity before install. On phone: save graph → Blueprints → Guide → overlay permission → Fortnite → bubble → floating Cover Flow Steps/Links/Graph navigation → collapse/reopen/Stop → return and edit. Record whether Fortnite or OEM suppresses the overlay. No separate signing workflow changes. Do not call an uncertified overlay a confirmed working release.

## Real-device feedback / next focused acceptance

GitHub Android run `38435563960` (commit `6efa671`) is **green**, and the owner demonstrated the overlay above Fortnite in landscape with working Steps, Devices and Links. Phase 8 is partially phone-accepted, not fully completed: the graph was blank, and the expanded window consumed most of the game view. The compact candidate passed Android CI `38441055829` at commit `ac754adf`; the owner then confirmed its visible nodes, link and slider controls over Fortnite. Next validate the new Links directory, direct-connection focus, folders, return-to-editor saving and outside-touch pass-through on actual Android. Do not mark Phase 8 complete until that evidence arrives.

## Next phone gate: connection explorer and graph focus

Owner screenshots after the compact sizing correction confirm the graph, nodes, wire and width/height sliders above Fortnite. New patch (CI pending): Steps/Links/Graph navigation; Links device explorer + named, non-destructive organizational folders; graph tap-to-inspect and long-press one-hop focus; double-tap blank to clear. Folder storage is additive inside the existing format-2 `.c1logic` document and Room schema 1 stays unchanged. Verify folder name keyboard focus returns Fortnite touch behavior, export/reimport and edit-save folder retention. Do not mark this candidate certified before Android CI and separate phone evidence.

## Full-bleed Cover Flow UI candidate (2026-10-13)

The immediate certified prior baseline is `363dde7c` (Android workflow `38458909239` passed). The owner supplied a real-phone Fortnite screenshot with functioning compact graph, direct bindings and selected-node inspector. This new source candidate keeps Steps / Links / Graph and the separate saved device-folder metadata, but moves navigation outside the dark graph panel to a three-card, 3D angled, swipeable Cover Flow-style carousel in its own bounded Android overlay window. A small contextual `⋯` menu preserves manual Size, Hide/Show navigation, Collapse and Stop. A device inspector appears over the full-bleed graph without reducing node canvas height; graph focus and Fit are retained. Existing Room/simulator/overlay permissions/signing untouched. Android build/lint, emulator gesture tests, and Fortnite phone confirmation are still required for this candidate.

## Inline node expansion (Android CI pending)

Graph no longer creates a separate settings bottom sheet. Tapping a device draws a larger version of that device at the same saved graph coordinates, including wrapped options, builder notes and exact incoming/outgoing binding labels. Tap again to collapse; other device taps change selection. Large device configurations are scrolled inside the card (not with the graph pan gesture), and the camera adjusts only enough to keep the card readable in the current overlay size. One-hop long-press focus, blank double-tap focus clear, Fit/pinch/drag, Steps/Links, folder metadata and Cover Flow persist. Node sizes are view-only. Source baseline is CI-green `5d15dc96` and new Android acceptance is pending.
