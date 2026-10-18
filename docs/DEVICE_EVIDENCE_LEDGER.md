# Device communication evidence ledger

## Recording contract — 2026-10-17

Record evidence per capability or essential condition, not a blanket confidence score for an entire device. This ledger is a research staging area; entries are not automatically production contracts or CI evidence. Existing catalog JSON and serialized device data remain unchanged.

For each researched device, provide:

- Exact device name, existing catalog ID, plain-language purpose and documented Creative 1.0 scope.
- Exact incoming Function and outgoing Event labels, with individual source links.
- Only settings relevant to blueprint intent or signal activation/filtering/propagation.
- Whether an Event follows a Function under documented conditions, requires external gameplay interaction, or has an unknown cause. Function delivery alone does not prove a physical result or imply another Event.
- Source title/URL, access date, publication/version when available, and a concise supporting paraphrase. Distinguish official documentation, observed in-game evidence and inference.
- Status: **documented**, **observed**, **partial/conflicting**, or **unknown**. A missing section is not proof that a device has no ports. State known scope/version limits.

Suggested per-capability table:

| Device / existing ID | Capability or essential condition | Exact label/value | Trigger/prerequisite | Evidence URL + locator | Access date/version | Status / uncertainty |
|---|---|---|---|---|---|---|

No new Fortnite capability findings have been entered in this revision. Do not interpret this empty table as a negative port inventory.

## Assigned research, pending results

1. **Ball Spawner:** independently establish purpose and the complete documented incoming/outgoing port inventory. Verify whether any spawn-result event exists; neither assume one nor assert absence from incomplete documentation. Separate physical ball interaction from local signal delivery. The owner's example defines our model boundary, not a sourced API contract.
2. **Movement Modulator:** exact device/port identity, activation events/functions, and only conditions affecting signal activation; movement physics remains external.
3. **Input Trigger:** input-driven Events, incoming Functions and essential input/player filters; distinguish a user input from a Function signal.

Lyra returns findings through Mavyy's relay. Review each proposed correction against the current source IDs before implementation. Do not commit research guesses or silently rename/remap saved ports.

## Future structured evidence

After this format is reviewed against real findings, consider a separate machine-readable sidecar keyed by existing device and port/setting IDs, with schema/version validation. Do not add speculative fields to production device JSON or change saved-document contracts merely to attach research confidence.
