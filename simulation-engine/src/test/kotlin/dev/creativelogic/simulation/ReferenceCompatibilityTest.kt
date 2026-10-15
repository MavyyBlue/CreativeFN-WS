package dev.creativelogic.simulation

import dev.creativelogic.catalog.InitialDeviceCatalog
import dev.creativelogic.model.*
import org.junit.Assert.*
import org.junit.Test

/** Valid non-prototype Fortnite settings must not invalidate a documented binding. */
class ReferenceCompatibilityTest {
    private val catalog = InitialDeviceCatalog.catalog
    private fun id(i: Int) = "00000000-0000-0000-0000-${i.toString().padStart(12, '0')}"
    @Test fun teamAndHudModesStillAllowSignalVerification() {
        val trigger = catalog.createInstance("trigger", "Key", id(1))
        val base = catalog.createInstance("tracker", "Goal", id(2))
        val tracker = base.copy(configuredOptions = base.configuredOptions + mapOf(
            "sharing" to DeviceOptionValue.EnumValue("Team"),
            "showOnHud" to DeviceOptionValue.EnumValue("Detailed"),
            "assignOnGameStart" to DeviceOptionValue.BooleanValue(true),
        ))
        val barrier = catalog.createInstance("barrier", "Gate", id(3)).let {
            it.copy(configuredOptions = it.configuredOptions + ("enabledDuringPhase" to DeviceOptionValue.EnumValue("Gameplay Only")))
        }
        val graph = MechanicGraph(listOf(trigger, tracker, barrier), listOf(
            LogicConnection(id(4),trigger.id,DeviceEventId("onTriggered"),tracker.id,DeviceFunctionId("incrementProgress")),
            LogicConnection(id(5),tracker.id,DeviceEventId("onComplete"),barrier.id,DeviceFunctionId("disable")),
        ))
        val s = ValueSignalSession(graph)
        s.inject(trigger.id,"onTriggered")
        s.run()
        assertEquals("1",s.values.getValue(tracker.id)["progress"])
        assertTrue(s.trace.none { it.kind == ValueSignalSession.Kind.WARNING })
        assertTrue(s.trace.any { it.kind == ValueSignalSession.Kind.FUNCTION && it.detail == "incrementProgress" })
        // Team/HUD presentation effects remain game-dependent and aren't asserted here.
    }
}
