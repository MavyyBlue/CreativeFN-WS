package dev.creativelogic.simulation

import dev.creativelogic.catalog.InitialDeviceCatalog
import dev.creativelogic.model.*
import org.junit.Assert.*
import org.junit.Test

class TraceClarityTest {
    private val catalog = InitialDeviceCatalog.catalog
    private fun id(i: Int) = "00000000-0000-0000-0000-${i.toString().padStart(12, '0')}"
    @Test fun aReceivedFunctionWithoutStateEffectIsNotReportedAsTwoValueChanges() {
        val source = catalog.createInstance("mutator-zone", "Zone", id(1))
        val target = catalog.createInstance("item-granter", "Granter", id(2))
        val graph = MechanicGraph(listOf(source,target), listOf(LogicConnection(id(3),source.id,
            DeviceEventId("on_player_entering_zone_send_event_to"),target.id,DeviceFunctionId("grant_item_when_receiving_from"))))
        val session = ValueSignalSession(graph)
        session.inject(source.id, "on_player_entering_zone_send_event_to")
        session.run()
        val summary = ValueSignalReport.summarize(session)
        assertEquals(1, summary.receivedFunctions)
        assertEquals(0, summary.changedValues)
        assertTrue(summary.unmodeledEffects > 0)
        assertTrue(ValueSignalReport.export(graph,session).contains("no modeled state"))
    }
}
