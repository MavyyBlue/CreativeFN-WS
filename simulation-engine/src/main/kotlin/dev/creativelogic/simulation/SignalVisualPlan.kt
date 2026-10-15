package dev.creativelogic.simulation

import dev.creativelogic.model.MechanicGraph

/** Derived from the ACTUAL deterministic trace, not guessed world actions. */
data class SignalVisualCue(
    val sourceDeviceId: String,
    val targetDeviceId: String?,
    val sourceEventId: String?,
    val targetFunctionId: String?,
    val label: String,
    val unsupported: Boolean = false,
)

object SignalVisualPlan {
    fun from(graph: MechanicGraph, trace: List<ValueSignalSession.Entry>): List<SignalVisualCue> {
        val result = mutableListOf<SignalVisualCue>()
        for (entry in trace.take(300)) {
            when (entry.kind) {
                ValueSignalSession.Kind.INPUT -> result += SignalVisualCue(entry.deviceId, null, null, null, "Injected event")
                ValueSignalSession.Kind.EVENT -> {
                    val links = graph.connections.filter { it.sourceDeviceId == entry.deviceId && it.sourceEventId.value == entry.detail }
                    if (links.isEmpty()) result += SignalVisualCue(entry.deviceId, null, null, null, "${entry.detail} · no recipients")
                    else links.forEach { result += SignalVisualCue(entry.deviceId, it.targetDeviceId, entry.detail,
                        it.targetFunctionId.value, "${entry.detail} → ${it.targetFunctionId.value}") }
                }
                ValueSignalSession.Kind.VALUE -> if (!entry.detail.startsWith("lastFunction = ") &&
                    !entry.detail.startsWith("lastInputValue = "))
                    result += SignalVisualCue(entry.deviceId, null, null, null, entry.detail)
                ValueSignalSession.Kind.UNSUPPORTED -> result += SignalVisualCue(entry.deviceId, null, null, null,
                    "Effect not modeled", unsupported = true)
                ValueSignalSession.Kind.WARNING -> result += SignalVisualCue(entry.deviceId, null, null, null,
                    "Propagation stopped", unsupported = true)
                ValueSignalSession.Kind.FUNCTION -> Unit // Corresponding graph edge was already captured from EVENT.
            }
        }
        return result.take(200)
    }
    fun fromPrototype(graph: MechanicGraph, trace: List<SimulationTraceEntry>): List<SignalVisualCue> {
        val result = mutableListOf<SignalVisualCue>()
        for (entry in trace.take(300)) {
            when (entry.kind) {
                TraceKind.INPUT -> result += SignalVisualCue(entry.deviceId, null, null, null, "Injected action")
                TraceKind.EVENT -> {
                    val links = graph.connections.filter { it.sourceDeviceId == entry.deviceId && it.sourceEventId.value == entry.detail }
                    if (links.isEmpty()) result += SignalVisualCue(entry.deviceId,null,null,null,"${entry.detail} · no recipients")
                    else links.forEach { result += SignalVisualCue(entry.deviceId,it.targetDeviceId,
                        entry.detail,it.targetFunctionId.value,"${entry.detail} → ${it.targetFunctionId.value}") }
                }
                TraceKind.STATE_CHANGE -> result += SignalVisualCue(entry.deviceId,null,null,null,entry.detail)
                TraceKind.IGNORED -> result += SignalVisualCue(entry.deviceId,null,null,null,"Condition blocked action",unsupported = true)
                TraceKind.WARNING -> result += SignalVisualCue(entry.deviceId,null,null,null,"Propagation stopped",unsupported = true)
                TraceKind.FUNCTION -> Unit
            }
        }
        return result.take(200)
    }
}
