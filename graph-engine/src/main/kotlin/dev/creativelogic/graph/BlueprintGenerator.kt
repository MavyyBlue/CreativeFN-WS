package dev.creativelogic.graph

import dev.creativelogic.catalog.DeviceCatalog
import dev.creativelogic.model.*

/** Shared, Android-independent, read-only construction reference. Never claims live Fortnite fidelity. */
data class BlueprintOption(val name: String, val value: String)
data class BlueprintDevice(
    val id: String, val name: String, val type: String,
    val graphX: Float, val graphY: Float, val options: List<BlueprintOption>,
    val notes: String?, val source: String,
)
data class BlueprintBinding(
    val sourceId: String, val targetId: String,
    val sourceName: String, val eventName: String, val targetName: String, val functionName: String,
)
data class Blueprint(
    val mechanicId: String, val name: String, val quantities: List<Pair<String, Int>>,
    val devices: List<BlueprintDevice>, val bindings: List<BlueprintBinding>,
    val steps: List<String>, val warnings: List<String>,
    val folders: List<BlueprintFolderV1> = emptyList(),
)

object BlueprintGenerator {
    fun create(mechanic: SavedMechanic, catalog: DeviceCatalog): Blueprint {
        val diagnostics = mutableListOf<String>()
        // Reuse the editor health rules in exported construction instructions.
        // These do not mutate or automatically repair the saved graph.
        GraphDiagnostics.inspect(mechanic.graph, catalog).items
            .filter { it.severity != DiagnosticSeverity.INFO }
            .forEach { issue -> diagnostics += "${issue.severity}: ${issue.title}. ${issue.action}" }
        try { validateGraph(mechanic.graph, catalog) }
        catch (ex: IllegalArgumentException) { diagnostics += "Invalid graph: ${ex.message ?: "review devices and connections"}" }
        val byId = mechanic.graph.devices.associateBy { it.id }
        val mapped = mechanic.graph.devices.map { instance ->
            val definition = catalog.definitions.firstOrNull { it.deviceTypeId == instance.deviceTypeId }
            if (definition == null) diagnostics += "Unknown device type ${instance.deviceTypeId}; verify manually."
            if (definition?.simulationSupport != SimulationSupport.FULLY_SIMULATED) {
                diagnostics += "${instance.name}: sandbox behavior is not verified against live Fortnite Creative 1.0."
            }
            if (definition != null) diagnostics += definition.limitations.map { "${instance.name}: $it" }
            val options = definition?.options?.map { option ->
                val chosen = instance.configuredOptions[option.id]
                if (chosen == null || !option.accepts(chosen)) diagnostics += "${instance.name}: ${option.displayName} requires manual verification."
                BlueprintOption(option.displayName, chosen?.let(::format) ?: "[unspecified]")
            } ?: instance.configuredOptions.map { (key, value) -> BlueprintOption(key, format(value)) }
            BlueprintDevice(instance.id, instance.name, definition?.displayName ?: instance.deviceTypeId,
                instance.graphX, instance.graphY, options, instance.builderNotes, definition?.referenceUrl.orEmpty())
        }
        val connections = mechanic.graph.connections.mapNotNull { connection ->
            val from = byId[connection.sourceDeviceId]
            val to = byId[connection.targetDeviceId]
            val sourcePort = from?.let { catalog.definitions.firstOrNull { def -> def.deviceTypeId == it.deviceTypeId } }
                ?.events?.firstOrNull { it.id == connection.sourceEventId }
            val targetPort = to?.let { catalog.definitions.firstOrNull { def -> def.deviceTypeId == it.deviceTypeId } }
                ?.functions?.firstOrNull { it.id == connection.targetFunctionId }
            if (from == null || to == null || sourcePort == null || targetPort == null) {
                diagnostics += "Unresolved binding ${connection.id}; inspect device IDs and port availability."
                null
            } else BlueprintBinding(from.id, to.id, from.name, sourcePort.displayName, to.name, targetPort.displayName)
        }
        val quantities = mapped.groupingBy { it.type }.eachCount().toList().sortedBy { it.first }
        val steps = buildList {
            add("Place the required devices in your Creative 1.0 island. Rename each device to match this blueprint where possible.")
            mapped.forEach { device ->
                add("Place ${device.type} named ${device.name}.")
                device.options.forEach { option -> add("On ${device.name}, verify and set ${option.name} = ${option.value}.") }
                if (!device.notes.isNullOrBlank()) add("Builder note for ${device.name}: ${device.notes}")
            }
            connections.forEach { link ->
                add("Bind ${link.sourceName} → ${link.eventName} to ${link.targetName} → ${link.functionName} using Direct Event Binding.")
            }
            add("Test the finished setup in Fortnite Creative 1.0. The workshop simulator cannot verify in-game effects, timing, or multiplayer behavior.")
        }
        if (mapped.isEmpty()) diagnostics += "This mechanic has no devices. Add devices before building."
        return Blueprint(mechanic.mechanicMetadata.id, mechanic.mechanicMetadata.name,
            quantities, mapped, connections, steps, diagnostics.distinct(), mechanic.folders)
    }

    private fun format(value: DeviceOptionValue): String = when (value) {
        is DeviceOptionValue.BooleanValue -> if (value.value) "On / Enabled" else "Off / Disabled"
        is DeviceOptionValue.IntegerValue -> value.value.toString()
        is DeviceOptionValue.DurationValue -> "${value.milliseconds} ms (${value.milliseconds / 1000.0} s; confirm Fortnite increments)"
        is DeviceOptionValue.EnumValue -> value.value
        is DeviceOptionValue.TextValue -> value.value
    }
}
