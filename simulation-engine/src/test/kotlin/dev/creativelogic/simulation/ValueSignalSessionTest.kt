package dev.creativelogic.simulation

import dev.creativelogic.catalog.InitialDeviceCatalog
import dev.creativelogic.model.*
import org.junit.Assert.*
import org.junit.Test

class ValueSignalSessionTest {
    private val catalog = InitialDeviceCatalog.catalog
    private fun id(i: Int) = "00000000-0000-0000-0000-${i.toString().padStart(12,'0')}"
    private fun dev(i: Int, type: String) = catalog.createInstance(type,type,id(i))
    private fun edge(i:Int, a:CreativeDeviceInstance, e:String,b:CreativeDeviceInstance,f:String) =
        LogicConnection(id(i),a.id,DeviceEventId(e),b.id,DeviceFunctionId(f))
    private fun tracker(i:Int) = dev(i,"tracker").copy(configuredOptions =
        dev(i,"tracker").configuredOptions + ("assignOnGameStart" to DeviceOptionValue.BooleanValue(true)) +
            ("targetValue" to DeviceOptionValue.IntegerValue(1)))

    @Test fun injectedZoneEventCanReachTrackerAndDisableBarrier() {
        val zone=dev(1,"mutator-zone"); val tracker=tracker(2);val barrier=dev(3,"barrier")
        val graph=MechanicGraph(listOf(zone,tracker,barrier),listOf(
            edge(10,zone,"on_player_entering_zone_send_event_to",tracker,"incrementProgress"),
            edge(11,tracker,"onComplete",barrier,"disable")))
        val session=ValueSignalSession(graph)
        session.inject(zone.id,"on_player_entering_zone_send_event_to","player-1","inside")
        session.run()
        assertEquals("1",session.values.getValue(tracker.id)["progress"])
        assertEquals("true",session.values.getValue(tracker.id)["completed"])
        assertEquals("false",session.values.getValue(barrier.id)["enabled"])
        assertFalse(session.halted)
        val trace=session.trace
        assertEquals(ValueSignalSession.Kind.INPUT,trace.first().kind)
        assertTrue(trace.any { it.kind==ValueSignalSession.Kind.EVENT && it.detail=="onComplete" })
        val second=ValueSignalSession(graph)
        second.inject(zone.id,"on_player_entering_zone_send_event_to","player-1","inside")
        second.run();assertEquals(trace,second.trace)
        session.reset();assertEquals("true",session.values.getValue(barrier.id)["enabled"])
        assertTrue(session.trace.isEmpty())
    }

    @Test fun unsupportedFunctionsAreLabeledAndCanBeFollowedByManualEvent() {
        val zone=dev(1,"mutator-zone");val granter=dev(2,"item-granter");val tracker=tracker(3)
        val graph=MechanicGraph(listOf(zone,granter,tracker),listOf(
            edge(10,zone,"on_player_entering_zone_send_event_to",granter,"grant_item_when_receiving_from"),
            edge(11,granter,"on_item_granted_send_event_to",tracker,"incrementProgress")))
        val s=ValueSignalSession(graph)
        s.inject(zone.id,"on_player_entering_zone_send_event_to")
        s.run()
        assertEquals("0",s.values.getValue(tracker.id)["progress"])
        assertTrue(s.trace.any { it.kind==ValueSignalSession.Kind.UNSUPPORTED && "grant_item" in it.detail })
        s.inject(granter.id,"on_item_granted_send_event_to","player-1","1")
        s.run()
        assertEquals("1",s.values.getValue(tracker.id)["progress"])
    }

    @Test fun incorrectEventIdsAreRejectedAndOnlyActualGraphConnectionsRun() {
        val zone=dev(1,"mutator-zone")
        val s=ValueSignalSession(MechanicGraph(listOf(zone)))
        try { s.inject(zone.id,"imaginary_event"); fail("Invented event accepted") }
        catch (_: IllegalArgumentException) { }
        s.inject(zone.id,"on_player_entering_zone_send_event_to")
        assertEquals(1,s.pending)
        assertTrue(s.step())
        assertEquals(0,s.pending)
        assertEquals(2,s.trace.size)
    }

    @Test fun feedbackLoopHaltsAtConfiguredSafetyLimit() {
        val trigger=dev(1,"trigger")
        val graph=MechanicGraph(listOf(trigger),listOf(edge(10,trigger,"onTriggered",trigger,"trigger")))
        val s=ValueSignalSession(graph,maxActions=12,maxDepth=24,maxPending=12)
        s.inject(trigger.id,"onTriggered")
        s.run()
        assertTrue(s.halted)
        assertEquals(0,s.pending)
        assertEquals(ValueSignalSession.Kind.WARNING,s.trace.last().kind)
    }
}
