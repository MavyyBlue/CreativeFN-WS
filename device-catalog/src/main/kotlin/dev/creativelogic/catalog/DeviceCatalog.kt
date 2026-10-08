package dev.creativelogic.catalog

import dev.creativelogic.model.*
import java.util.UUID

/** Versioned, offline draft reference. Fidelity verification belongs before simulation. */
data class DeviceCatalog(
    val catalogSchemaVersion: Int, val catalogVersion: String,
    val fortniteReferenceVersion: String?, val lastVerifiedDate: String?,
    val definitions: List<CreativeDeviceDefinition>,
) {
    init {
        require(catalogSchemaVersion == 1) { "Unsupported catalog schema" }
        require(catalogVersion.isNotBlank())
        require(definitions.map { it.deviceTypeId }.distinct().size == definitions.size)
        definitions.forEach { d ->
            require(d.deviceTypeId.isNotBlank() && d.displayName.isNotBlank())
            require(d.options.map { it.id }.distinct().size == d.options.size)
            require(d.events.map { it.id }.distinct().size == d.events.size)
            require(d.functions.map { it.id }.distinct().size == d.functions.size)
            require(d.options.all { it.id.isNotBlank() && it.accepts(it.defaultValue) })
            require(d.simulationSupport == SimulationSupport.FULLY_SIMULATED || d.limitations.isNotEmpty())
        }
    }
    fun definition(typeId: String) = definitions.singleOrNull { it.deviceTypeId == typeId }
        ?: throw IllegalArgumentException("Unknown device type: $typeId")
    fun createInstance(typeId: String, name: String, id: String = UUID.randomUUID().toString()): CreativeDeviceInstance {
        val d = definition(typeId)
        return CreativeDeviceInstance(id, typeId, name, configuredOptions = d.options.associate { it.id to it.defaultValue })
    }
    fun validate(instance: CreativeDeviceInstance) {
        val d = definition(instance.deviceTypeId)
        require(instance.configuredOptions.keys == d.options.map { it.id }.toSet()) { "Missing or unknown option" }
        require(d.options.all { it.accepts(instance.configuredOptions.getValue(it.id)) }) { "Invalid option type or value" }
    }
    fun validateBinding(connection: LogicConnection, instances: List<CreativeDeviceInstance>) {
        require(instances.map { it.id }.distinct().size == instances.size) { "Duplicate device IDs" }
        val source = instances.singleOrNull { it.id == connection.sourceDeviceId } ?: error("Missing source device")
        val target = instances.singleOrNull { it.id == connection.targetDeviceId } ?: error("Missing target device")
        require(definition(source.deviceTypeId).events.any { it.id == connection.sourceEventId }) { "Unknown source event" }
        require(definition(target.deviceTypeId).functions.any { it.id == connection.targetFunctionId }) { "Unknown target function" }
    }
}

object InitialDeviceCatalog {
    private fun bool(id: String, name: String, default: Boolean) = DeviceOptionDefinition(id, name, DeviceOptionKind.BOOLEAN, DeviceOptionValue.BooleanValue(default))
    private fun integer(id: String, name: String, default: Int) = DeviceOptionDefinition(id, name, DeviceOptionKind.INTEGER, DeviceOptionValue.IntegerValue(default), minimum = 0)
    private fun enum(id: String, name: String, default: String, vararg choices: String) = DeviceOptionDefinition(id, name, DeviceOptionKind.ENUM, DeviceOptionValue.EnumValue(default), choices = choices.toList())
    private fun event(id: String, name: String) = DeviceEventDefinition(DeviceEventId(id), name)
    private fun function(id: String, name: String) = DeviceFunctionDefinition(DeviceFunctionId(id), name)
    private fun device(id: String, name: String, category: String, options: List<DeviceOptionDefinition>, events: List<DeviceEventDefinition>, functions: List<DeviceFunctionDefinition>, path: String) = CreativeDeviceDefinition(
        id, name, category, options, events, functions, SimulationSupport.NOT_SIMULATED,
        listOf("Reference only. Simulation is not available yet.", "These are prototype settings, not a verified copy of every Fortnite option or default."),
        "https://dev.epicgames.com/documentation/en-us/fortnite-creative/$path-in-fortnite-creative",
    )
    val catalog = DeviceCatalog(1, "0.1.0-draft", null, null, listOf(
        device("trigger", "Trigger", "Logic", listOf(
            bool("enabledOnGameStart", "Enabled on Game Start", true), bool("triggeredByPlayer", "Triggered by Player", true),
            integer("timesCanTrigger", "Times Can Trigger", 0),
            DeviceOptionDefinition("resetDelay", "Reset Delay", DeviceOptionKind.DURATION, DeviceOptionValue.DurationValue(0), minimum = 0),
        ), listOf(event("onTriggered", "On Triggered")), listOf(function("enable", "Enable"), function("disable", "Disable"), function("resetTimesTriggered", "Reset Times Triggered"), function("trigger", "Trigger")), "using-trigger-devices"),
        device("tracker", "Tracker", "Objectives", listOf(
            integer("targetValue", "Target Value", 3), enum("sharing", "Sharing", "Individual", "Individual", "Team", "All"),
            enum("showOnHud", "Show on HUD", "Off", "Off", "Detailed", "List", "Both"), bool("assignOnGameStart", "Assign on Game Start", false),
        ), listOf(event("onComplete", "On Complete")), listOf(function("assign", "Assign"), function("incrementProgress", "Increment Progress"), function("resetProgress", "Reset Progress"), function("complete", "Complete"), function("remove", "Remove")), "using-tracker-devices"),
        device("barrier", "Barrier", "World", listOf(
            enum("enabledDuringPhase", "Enabled During Phase", "Always", "None", "Always", "Pre-Game Only", "Gameplay Only"),
        ), emptyList(), listOf(function("enable", "Enable"), function("disable", "Disable")), "using-barrier-devices"),
    ))
}
