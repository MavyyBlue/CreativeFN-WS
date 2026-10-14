package dev.creativelogic.graph

import dev.creativelogic.catalog.DeviceCatalog
import dev.creativelogic.model.*
import java.util.ArrayDeque

/** Read-only builder diagnostics: never edits the graph or changes simulator semantics. */
enum class DiagnosticSeverity { ERROR, WARNING, INFO }

data class BuilderDiagnostic(
    val severity: DiagnosticSeverity,
    val code: String,
    val title: String,
    val explanation: String,
    val action: String,
    val deviceId: String? = null,
    val connectionId: String? = null,
)

data class DiagnosticReport(val items: List<BuilderDiagnostic>) {
    val errors: Int get() = items.count { it.severity == DiagnosticSeverity.ERROR }
    val warnings: Int get() = items.count { it.severity == DiagnosticSeverity.WARNING }
    val canBuildGuide: Boolean get() = errors == 0
}

/**
 * Audits drafts, including imported/legacy graphs that may not meet current editor invariants.
 * IDs are intentionally retained for the UI's device inspector; no database or wire changes.
 */
object GraphDiagnostics {
    fun inspect(graph: MechanicGraph, catalog: DeviceCatalog): DiagnosticReport {
        val issues = mutableListOf<BuilderDiagnostic>()
        fun report(severity: DiagnosticSeverity, code: String, title: String,
                   explanation: String, action: String, deviceId: String? = null,
                   connectionId: String? = null) {
            issues += BuilderDiagnostic(severity, code, title, explanation, action, deviceId, connectionId)
        }
        if (graph.devices.isEmpty()) {
            report(DiagnosticSeverity.INFO, "EMPTY_GRAPH", "No devices yet",
                "This mechanic has nothing to place or connect.", "Add a device from Devices to begin.")
            return DiagnosticReport(issues)
        }
        val devicesById = graph.devices.associateBy { it.id }
        graph.devices.groupBy { it.id }.filterValues { it.size > 1 }.forEach { (id, _) ->
            report(DiagnosticSeverity.ERROR, "DUPLICATE_DEVICE_ID", "Repeated device identity",
                "Two saved devices share one internal ID; links could point to the wrong one.",
                "Re-import a clean copy or recreate one of the duplicated devices.", id)
        }
        val definitions = catalog.definitions.associateBy { it.deviceTypeId }
        graph.devices.forEach { device ->
            val definition = definitions[device.deviceTypeId]
            if (definition == null) {
                report(DiagnosticSeverity.ERROR, "UNKNOWN_DEVICE", "Unknown device: ${device.name}",
                    "${device.deviceTypeId} is not in this version of the catalog.",
                    "Use a supported device or open the blueprint in the workshop version that created it.", device.id)
                return@forEach
            }
            val expected = definition.options.associateBy { it.id }
            for (option in definition.options) {
                val value = device.configuredOptions[option.id]
                when {
                    value == null -> report(DiagnosticSeverity.ERROR, "MISSING_OPTION", "Missing setting on ${device.name}",
                        "${option.displayName} has no saved value.", "Open ${device.name}, choose ${option.displayName}, and save.", device.id)
                    !option.accepts(value) -> report(DiagnosticSeverity.ERROR, "INVALID_OPTION", "Invalid setting on ${device.name}",
                        "${option.displayName} contains a value this device does not accept.",
                        "Open ${device.name} and select a supported ${option.displayName} value.", device.id)
                }
            }
            device.configuredOptions.keys.filterNot { it in expected }.sorted().forEach { key ->
                report(DiagnosticSeverity.ERROR, "UNKNOWN_OPTION", "Unrecognized setting on ${device.name}",
                    "Saved setting '$key' isn't in this device's current catalog.",
                    "Check the source file and device version before removing or changing this setting.", device.id)
            }
            if (device.deviceTypeId == "hud-message" &&
                (device.configuredOptions["message"] as? DeviceOptionValue.TextValue)?.value?.isBlank() == true) {
                report(DiagnosticSeverity.WARNING, "BLANK_MESSAGE", "HUD message is empty",
                    "${device.name} may show nothing when Show is called.",
                    "Enter the message you want players to see.", device.id)
            }
            if (device.deviceTypeId == "tracker" &&
                (device.configuredOptions["sharing"] as? DeviceOptionValue.EnumValue)?.value?.let { it != "All" } == true) {
                report(DiagnosticSeverity.WARNING, "TRACKER_SHARING_UNSUPPORTED", "Tracker sharing not simulated",
                    "The test scene only models Sharing = All; it has no player or team progression model.",
                    "For a sandbox test use Sharing = All, or keep your selection and verify it in Fortnite.", device.id)
            }
            if (device.deviceTypeId == "tracker" &&
                (device.configuredOptions["showOnHud"] as? DeviceOptionValue.EnumValue)?.value?.let { it != "Off" } == true) {
                report(DiagnosticSeverity.WARNING, "TRACKER_HUD_UNSUPPORTED", "Tracker HUD display not simulated",
                    "The local test scene does not render tracker HUD modes.",
                    "Use Show on HUD = Off in the test scene; check the actual HUD in Fortnite.", device.id)
            }
            if (device.deviceTypeId == "barrier" &&
                (device.configuredOptions["enabledDuringPhase"] as? DeviceOptionValue.EnumValue)?.value
                    !in listOf(null, "Always", "None")) {
                report(DiagnosticSeverity.WARNING, "BARRIER_PHASE_UNSUPPORTED", "Barrier phase not simulated",
                    "The sandbox knows only Always and None; it doesn't model pre-game or gameplay-only phases.",
                    "Use Always or None for local testing, and verify other phases in Fortnite.", device.id)
            }
            if (device.deviceTypeId == "tracker" &&
                (device.configuredOptions["targetValue"] as? DeviceOptionValue.IntegerValue)?.value == 0) {
                report(DiagnosticSeverity.WARNING, "TRACKER_ZERO_TARGET", "Tracker target is zero",
                    "The prototype simulator requires a target of at least 1; it cannot run this setting.",
                    "Set ${device.name}'s Target Value to 1 or more for sandbox testing.", device.id)
            }
        }
        graph.connections.groupBy { it.id }.filterValues { it.size > 1 }.forEach { (id, _) ->
            report(DiagnosticSeverity.ERROR, "DUPLICATE_LINK_ID", "Repeated connection identity",
                "Two bindings share one internal ID.", "Recreate one repeated binding from the Bindings screen.", connectionId = id)
        }
        val byMeaning = graph.connections.groupBy {
            listOf(it.sourceDeviceId, it.sourceEventId.value, it.targetDeviceId, it.targetFunctionId.value)
        }
        byMeaning.filterValues { it.size > 1 }.values.forEach { duplicates ->
            // One message per duplicate group, not one per duplicate edge.
            report(DiagnosticSeverity.ERROR, "DUPLICATE_LINK", "Event is bound twice to the same function",
                "This connection repeats ${duplicates.size} times and may execute a function more than once.",
                "Keep one copy of the binding and remove the duplicates.", connectionId = duplicates.first().id)
        }
        graph.connections.forEach { link ->
            val source = devicesById[link.sourceDeviceId]
            val target = devicesById[link.targetDeviceId]
            if (source == null || target == null) {
                report(DiagnosticSeverity.ERROR, "MISSING_ENDPOINT", "Connection has a missing device",
                    "One end of this saved connection is no longer on the graph.",
                    "Remove the broken binding and reconnect the correct devices.", connectionId = link.id)
                return@forEach
            }
            val from = definitions[source.deviceTypeId]
            val to = definitions[target.deviceTypeId]
            if (from != null && from.events.none { it.id == link.sourceEventId }) {
                report(DiagnosticSeverity.ERROR, "UNKNOWN_EVENT", "Event no longer available",
                    "${source.name} has no '${link.sourceEventId.value}' event in this catalog.",
                    "Choose an event shown by ${source.name} and recreate the binding.", source.id, link.id)
            }
            if (to != null && to.functions.none { it.id == link.targetFunctionId }) {
                report(DiagnosticSeverity.ERROR, "UNKNOWN_FUNCTION", "Function no longer available",
                    "${target.name} has no '${link.targetFunctionId.value}' function in this catalog.",
                    "Choose a function shown by ${target.name} and recreate the binding.", target.id, link.id)
            }
        }
        fun receivesFunction(id: String, function: String) = graph.connections.any {
            it.targetDeviceId == id && it.targetFunctionId.value == function && it.sourceDeviceId in devicesById
        }
        graph.devices.forEach { device ->
            val options = device.configuredOptions
            if (device.deviceTypeId == "trigger" &&
                (options["enabledOnGameStart"] as? DeviceOptionValue.BooleanValue)?.value == false &&
                !receivesFunction(device.id, "enable")) {
                report(DiagnosticSeverity.WARNING, "TRIGGER_STAYS_DISABLED", "Trigger cannot turn on through these links",
                    "${device.name} starts disabled and has no incoming Enable binding.",
                    "Enable it at game start or connect another device's event to Enable.", device.id)
            }
            if (device.deviceTypeId == "button" &&
                (options["enabledAtGameStart"] as? DeviceOptionValue.BooleanValue)?.value == false &&
                !receivesFunction(device.id, "enable")) {
                report(DiagnosticSeverity.WARNING, "BUTTON_STAYS_DISABLED", "Button cannot turn on through these links",
                    "${device.name} starts disabled and has no incoming Enable binding.",
                    "Turn on Enabled at Game Start or add a binding to Enable.", device.id)
            }
            if (device.deviceTypeId == "tracker" &&
                (options["assignOnGameStart"] as? DeviceOptionValue.BooleanValue)?.value == false &&
                receivesFunction(device.id, "incrementProgress") && !receivesFunction(device.id, "assign")) {
                report(DiagnosticSeverity.WARNING, "TRACKER_UNASSIGNED", "Tracker may not start tracking",
                    "${device.name} receives Increment Progress but starts unassigned in this sandbox.",
                    "Set Assign on Game Start or connect an event to Assign before incrementing.", device.id)
            }
        }
        // A feedback loop is legal, but unbounded event propagation is unsafe. Warn, never reject.
        val adjacency = graph.connections.filter {
            it.sourceDeviceId in devicesById && it.targetDeviceId in devicesById
        }.groupBy { it.sourceDeviceId }
        val visited = mutableSetOf<String>()
        val active = mutableSetOf<String>()
        var hasCycle = false
        fun walk(start: String) {
            // Iterative DFS avoids overflow on large imported blueprints.
            val stack = ArrayDeque<Pair<String, Iterator<LogicConnection>>>()
            visited += start; active += start
            stack.addLast(start to adjacency[start].orEmpty().iterator())
            while (stack.isNotEmpty()) {
                val (id, edges) = stack.peekLast()
                if (!edges.hasNext()) { active -= id; stack.removeLast(); continue }
                val next = edges.next().targetDeviceId
                if (next in active) hasCycle = true
                else if (visited.add(next)) {
                    active += next; stack.addLast(next to adjacency[next].orEmpty().iterator())
                }
            }
        }
        graph.devices.forEach { if (it.id !in visited) walk(it.id) }
        if (hasCycle) report(DiagnosticSeverity.WARNING, "FEEDBACK_LOOP", "Possible event feedback loop",
            "Some connections lead back to an earlier device. This can repeatedly fire events, depending on the functions involved.",
            "Review the loop and add a stopping condition. The simulator's action/depth limits stay enabled.")
        // The catalog's NOT_SIMULATED flag is global, even when individual functions work in the test scene.
        if (graph.devices.any { definitions[it.deviceTypeId]?.simulationSupport != SimulationSupport.FULLY_SIMULATED }) {
            report(DiagnosticSeverity.INFO, "SANDBOX_SCOPE", "Fortnite behavior not fully verified",
                "The workshop can test selected event flows, not exact Creative 1.0 timing, gameplay, or multiplayer state.",
                "Use Simulate for supported settings, then test this blueprint on an actual Creative island.")
        }
        return DiagnosticReport(issues)
    }
}
