package dev.creativelogic.simulation

import dev.creativelogic.model.MechanicGraph

/**
 * Phase 5.2: explain the value-only test without implying Fortnite gameplay fidelity.
 * This is an immutable projection of an existing deterministic session, never another engine.
 */
data class ValueSignalSummary(
    val injectedInputs: Int,
    val emittedEvents: Int,
    val receivedFunctions: Int,
    val changedValues: Int,
    val unmodeledEffects: Int,
    val safetyWarnings: Int,
    val modeledDevices: Int,
    val queuedActions: Int,
    val stopped: Boolean,
) {
    val hasUnknownOutcomes get() = unmodeledEffects > 0 || safetyWarnings > 0
}

object ValueSignalReport {
    fun summarize(session: ValueSignalSession): ValueSignalSummary {
        val entries = session.trace
        fun count(kind: ValueSignalSession.Kind): Int = entries.count { it.kind == kind }
        return ValueSignalSummary(
            injectedInputs = count(ValueSignalSession.Kind.INPUT),
            emittedEvents = count(ValueSignalSession.Kind.EVENT),
            receivedFunctions = count(ValueSignalSession.Kind.FUNCTION),
            changedValues = count(ValueSignalSession.Kind.VALUE),
            unmodeledEffects = count(ValueSignalSession.Kind.UNSUPPORTED),
            safetyWarnings = count(ValueSignalSession.Kind.WARNING),
            modeledDevices = session.values.count { it.value.isNotEmpty() },
            queuedActions = session.pending,
            stopped = session.halted,
        )
    }

    fun explain(summary: ValueSignalSummary): String = when {
        summary.stopped -> "Safety stop. Inspect circular bindings, reduce the scenario, then Reset. Results beyond the stop are not calculated."
        summary.queuedActions > 0 -> "${summary.queuedActions} action(s) are still queued. Run or Step before interpreting the final outcome."
        summary.injectedInputs == 0 -> "No events injected. Choose a documented event in Actions, then Run."
        summary.unmodeledEffects > 0 -> "${summary.changedValues} value change(s); ${summary.unmodeledEffects} received function(s) have unmodeled effects. Inject the corresponding real-world outcome event explicitly if appropriate."
        summary.changedValues == 0 -> "Events were processed but no modeled device values changed. Check bindings and the received-function trace."
        else -> "${summary.changedValues} modeled value change(s) from ${summary.injectedInputs} injected event(s). Verify real game trigger conditions in Fortnite."
    }

    /** Portable plain-text evidence for a builder. No permissions or network access. */
    fun export(graph: MechanicGraph, session: ValueSignalSession): String {
        val summary = summarize(session)
        val names = graph.devices.associate { it.id to it.name }
        val text = StringBuilder()
        text.appendLine("CreativeFN WS - value test report")
        text.appendLine("Manual event model only; NOT a Fortnite gameplay or fidelity test.")
        text.appendLine("Inputs=${summary.injectedInputs}, events=${summary.emittedEvents}, functions=${summary.receivedFunctions}, value changes=${summary.changedValues}, unmodeled=${summary.unmodeledEffects}, warnings=${summary.safetyWarnings}, pending=${summary.queuedActions}")
        text.appendLine(explain(summary))
        text.appendLine("\nDevice values:")
        graph.devices.forEach { device ->
            val values = session.values[device.id].orEmpty()
            text.appendLine("${device.name} [${device.deviceTypeId}]: " +
                if (values.isEmpty()) "no modeled state" else values.toSortedMap().entries.joinToString { "${it.key}=${it.value}" })
        }
        text.appendLine("\nEvent and function trace (recorded order):")
        session.trace.forEachIndexed { index, entry ->
            val target = names[entry.deviceId] ?: entry.deviceId
            text.appendLine("${index + 1}. ${entry.kind} | $target | ${entry.detail} | actor=${entry.actor} | value=${entry.value}")
        }
        return text.toString()
    }
}
