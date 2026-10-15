package dev.creativelogic.simulation

import dev.creativelogic.catalog.InitialDeviceCatalog
import dev.creativelogic.model.*
import org.junit.Assert.*
import org.junit.Test

class SignalVisualPlanTest {
    private val cat = InitialDeviceCatalog.catalog
    private fun id(i: Int) = "00000000-0000-0000-0000-${i.toString().padStart(12,'0')}"
    @Test fun visuallyFollowActualBindingAndStateChange() {
        val a = cat.createInstance("trigger", "A", id(1))
        val b = cat.createInstance("barrier", "B", id(2))
        val graph = MechanicGraph(listOf(a,b),listOf(LogicConnection(id(3),a.id,DeviceEventId("onTriggered"),b.id,DeviceFunctionId("disable"))))
        val session = ValueSignalSession(graph)
        session.inject(a.id,"onTriggered")
        session.run()
        val cues = SignalVisualPlan.from(graph,session.trace)
        assertTrue(cues.any { it.sourceDeviceId == a.id && it.targetDeviceId == b.id && it.targetFunctionId == "disable" })
        assertTrue(cues.any { it.sourceDeviceId == b.id && it.label == "enabled = false" })
        assertTrue(cues.none { it.unsupported })
    }
    @Test fun noImaginaryWireAndUnsupportedVisualizedDifferently() {
        val b = cat.createInstance("barrier", "B", id(1))
        val graph = MechanicGraph(listOf(b))
        val entries = listOf(ValueSignalSession.Entry(ValueSignalSession.Kind.EVENT,b.id,"ghost","p",""),
            ValueSignalSession.Entry(ValueSignalSession.Kind.UNSUPPORTED,b.id,"no semantics","p",""))
        val cues = SignalVisualPlan.from(graph,entries)
        assertTrue(cues.all { it.targetDeviceId == null })
        assertTrue(cues.last().unsupported)
    }
}
