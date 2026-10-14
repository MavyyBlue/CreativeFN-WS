package dev.creativelogic.graph

import dev.creativelogic.catalog.InitialDeviceCatalog
import dev.creativelogic.model.*
import org.junit.Assert.*
import org.junit.Test
import java.util.UUID

class GraphDiagnosticsTest {
    private val catalog = InitialDeviceCatalog.catalog
    private fun device(type: String, name: String) = catalog.createInstance(type, name)
    private fun link(from: CreativeDeviceInstance, event: String, to: CreativeDeviceInstance, function: String) =
        LogicConnection(UUID.randomUUID().toString(), from.id, DeviceEventId(event), to.id, DeviceFunctionId(function))
    private fun codes(graph: MechanicGraph) = GraphDiagnostics.inspect(graph, catalog).items.map { it.code }

    @Test fun healthyGraphHasNoErrorsOrWarningsAndDoesNotChangeSource() {
        val button = device("button", "Start")
        val hud = device("hud-message", "Welcome")
        val graph = MechanicGraph(listOf(button, hud), listOf(link(button, "onInteract", hud, "show")))
        val copy = graph.copy()
        val report = GraphDiagnostics.inspect(graph, catalog)
        assertEquals(0, report.errors)
        assertEquals(0, report.warnings)
        assertTrue(report.canBuildGuide)
        assertTrue(report.items.any { it.code == "SANDBOX_SCOPE" && it.severity == DiagnosticSeverity.INFO })
        assertEquals(copy, graph)
    }

    @Test fun malformedImportedGraphProducesSpecificRepairInstructions() {
        val source = device("button", "Start")
        val broken = device("tracker", "Goal").copy(configuredOptions = mapOf(
            "targetValue" to DeviceOptionValue.IntegerValue(-2),
            "unknownOption" to DeviceOptionValue.TextValue("oops"),
        ))
        val missingId = UUID.randomUUID().toString()
        val graph = MechanicGraph(listOf(source, broken), listOf(
            LogicConnection(UUID.randomUUID().toString(), source.id, DeviceEventId("onInteract"), missingId, DeviceFunctionId("complete")),
            link(source, "unknownEvent", broken, "unknownFunction"),
        ))
        val report = GraphDiagnostics.inspect(graph, catalog)
        assertTrue(report.errors >= 6)
        assertTrue(report.items.all { it.title.isNotBlank() && it.explanation.isNotBlank() && it.action.isNotBlank() })
        assertTrue(report.items.any { it.code == "MISSING_OPTION" && it.deviceId == broken.id })
        assertTrue(report.items.any { it.code == "INVALID_OPTION" })
        assertTrue(report.items.any { it.code == "UNKNOWN_OPTION" })
        assertTrue(report.items.any { it.code == "MISSING_ENDPOINT" })
        assertTrue(report.items.any { it.code == "UNKNOWN_EVENT" })
        assertTrue(report.items.any { it.code == "UNKNOWN_FUNCTION" })
        assertFalse(report.canBuildGuide)
    }

    @Test fun duplicateSemanticConnectionsAreFlaggedButNormalFanoutIsNot() {
        val button = device("button", "Start")
        val first = device("barrier", "First")
        val second = device("barrier", "Second")
        val one = link(button, "onInteract", first, "disable")
        val duplicate = one.copy(id = UUID.randomUUID().toString())
        val extra = link(button, "onInteract", second, "disable")
        assertTrue("DUPLICATE_LINK" in codes(MechanicGraph(listOf(button, first, second), listOf(one, duplicate, extra))))
        assertFalse("DUPLICATE_LINK" in codes(MechanicGraph(listOf(button, first, second), listOf(one, extra))))
    }

    @Test fun possibleLoopIsWarningAndNotAnError() {
        val one = device("trigger", "One")
        val two = device("trigger", "Two")
        val graph = MechanicGraph(listOf(one, two), listOf(
            link(one, "onTriggered", two, "trigger"), link(two, "onTriggered", one, "trigger")))
        val report = GraphDiagnostics.inspect(graph, catalog)
        assertTrue("FEEDBACK_LOOP" in report.items.map { it.code })
        assertEquals(0, report.errors)
        assertTrue(report.canBuildGuide)
        assertEquals(DiagnosticSeverity.WARNING, report.items.first { it.code == "FEEDBACK_LOOP" }.severity)
    }

    @Test fun disabledTriggerAndButtonNeedInboundEnable() {
        val trigger = device("trigger", "Entrance").copy(configuredOptions =
            deviceOptions("trigger") + ("enabledOnGameStart" to DeviceOptionValue.BooleanValue(false)))
        val button = device("button", "Gate").copy(configuredOptions =
            deviceOptions("button") + ("enabledAtGameStart" to DeviceOptionValue.BooleanValue(false)))
        val graph = MechanicGraph(listOf(trigger, button))
        assertTrue("TRIGGER_STAYS_DISABLED" in codes(graph))
        assertTrue("BUTTON_STAYS_DISABLED" in codes(graph))
        val wired = graph.copy(connections = listOf(
            link(trigger, "onTriggered", button, "enable"), link(button, "onInteract", trigger, "enable")))
        assertFalse("TRIGGER_STAYS_DISABLED" in codes(wired))
        assertFalse("BUTTON_STAYS_DISABLED" in codes(wired))
    }

    @Test fun trackerIncrementWithoutAssignmentIsActionable() {
        val button = device("button", "Start")
        val tracker = device("tracker", "Progress")
        val graph = MechanicGraph(listOf(button, tracker), listOf(link(button, "onInteract", tracker, "incrementProgress")))
        assertTrue("TRACKER_UNASSIGNED" in codes(graph))
        val fixed = graph.copy(connections = graph.connections + link(button, "onInteract", tracker, "assign"))
        assertFalse("TRACKER_UNASSIGNED" in codes(fixed))
    }

    @Test fun unsupportedPrototypeSettingsClearlyPointToInGameVerification() {
        val tracker = device("tracker", "Per-player goal").let { it.copy(configuredOptions = it.configuredOptions +
            ("sharing" to DeviceOptionValue.EnumValue("Individual")) +
            ("showOnHud" to DeviceOptionValue.EnumValue("Detailed")) +
            ("targetValue" to DeviceOptionValue.IntegerValue(0))) }
        val barrier = device("barrier", "Lobby").let { it.copy(configuredOptions = it.configuredOptions +
            ("enabledDuringPhase" to DeviceOptionValue.EnumValue("Pre-Game Only"))) }
        val result = GraphDiagnostics.inspect(MechanicGraph(listOf(tracker, barrier)), catalog)
        assertTrue(result.items.any { it.code == "TRACKER_SHARING_UNSUPPORTED" })
        assertTrue(result.items.any { it.code == "TRACKER_HUD_UNSUPPORTED" })
        assertTrue(result.items.any { it.code == "TRACKER_ZERO_TARGET" })
        assertTrue(result.items.any { it.code == "BARRIER_PHASE_UNSUPPORTED" })
        assertEquals(0, result.errors)
    }

    @Test fun blankHudAndUnknownDeviceAreDifferentSeverities() {
        val hud = device("hud-message", "Notice").let { it.copy(configuredOptions = it.configuredOptions +
            ("message" to DeviceOptionValue.TextValue("  "))) }
        val unknown = device("button", "Legacy").copy(deviceTypeId = "old-device")
        val result = GraphDiagnostics.inspect(MechanicGraph(listOf(hud, unknown)), catalog)
        assertEquals(DiagnosticSeverity.WARNING, result.items.first { it.code == "BLANK_MESSAGE" }.severity)
        assertEquals(DiagnosticSeverity.ERROR, result.items.first { it.code == "UNKNOWN_DEVICE" }.severity)
    }

    @Test fun largeGraphCycleCheckIsIterative() {
        val devices = (0..1200).map { device("button", "B$it") }
        val edges = devices.zipWithNext().map { (a, b) -> link(a, "onInteract", b, "enable") }
        val result = GraphDiagnostics.inspect(MechanicGraph(devices, edges), catalog)
        assertFalse(result.items.any { it.code == "FEEDBACK_LOOP" })
        assertEquals(0, result.errors)
    }

    private fun deviceOptions(type: String) = device(type, "Reference").configuredOptions
}
