package dev.creativelogic.graph

import dev.creativelogic.catalog.InitialDeviceCatalog
import dev.creativelogic.model.*
import org.junit.Assert.*
import org.junit.Test

class GraphEditorTest {
    private val catalog = InitialDeviceCatalog.catalog
    private fun editor() = GraphEditor(MechanicGraph(), catalog)
    private fun rejects(action: () -> Unit) { try { action(); fail("Invalid graph accepted") } catch (_: IllegalArgumentException) { } }
    @Test fun triggerTrackerBarrierHasSemanticBindingsAndStableState() {
        val e = editor(); val trigger = e.add("trigger"); val tracker = e.add("tracker"); val barrier = e.add("barrier")
        e.connect(trigger.id, DeviceEventId("onTriggered"), tracker.id, DeviceFunctionId("incrementProgress"))
        e.connect(tracker.id, DeviceEventId("onComplete"), barrier.id, DeviceFunctionId("disable"))
        assertEquals(3, e.graph.devices.size); assertEquals(2, e.graph.connections.size)
        validateGraph(e.graph, catalog)
    }
    @Test fun deleteUndoRedoRestoresDeviceOptionsPositionsAndBindingsAtomically() {
        val e = editor(); val a = e.add("trigger"); val b = e.add("tracker")
        e.move(a.id, -30f, 100f)
        e.connect(a.id, DeviceEventId("onTriggered"), b.id, DeviceFunctionId("incrementProgress"))
        val before = e.graph
        e.deleteDevice(a.id); assertTrue(e.graph.connections.isEmpty())
        e.undo(); assertEquals(before, e.graph)
        e.redo(); assertEquals(listOf(b), e.graph.devices)
        e.undo(); e.move(b.id, 5f, 7f); assertFalse(e.canRedo)
    }
    @Test fun duplicationHasIndependentIdAndDoesNotCloneBindings() {
        val e = editor(); val a = e.add("trigger")
        e.connect(a.id, DeviceEventId("onTriggered"), a.id, DeviceFunctionId("trigger"))
        val copy = e.duplicate(a.id)
        assertNotEquals(a.id, copy.id); assertEquals(a.configuredOptions, copy.configuredOptions)
        assertEquals(1, e.graph.connections.size)
        e.update(copy.copy(name = "Second", builderNotes = "Notes"))
        assertEquals(a, e.graph.devices.first())
    }
    @Test fun wrongPortsAndDuplicateBindingsCannotMutateHistory() {
        val e = editor(); val a = e.add("trigger"); val b = e.add("tracker"); val old = e.graph
        rejects { e.connect(a.id, DeviceEventId("enable"), b.id, DeviceFunctionId("onComplete")) }
        assertEquals(old, e.graph)
        e.connect(a.id, DeviceEventId("onTriggered"), b.id, DeviceFunctionId("incrementProgress"))
        val connected = e.graph
        rejects { e.connect(a.id, DeviceEventId("onTriggered"), b.id, DeviceFunctionId("incrementProgress")) }
        assertEquals(connected, e.graph)
    }
    @Test fun viewportDoesNotConsumeUndoAndIsRetainedAcrossUndo() {
        val e = editor(); e.add("trigger"); val viewport = GraphViewport(40f, -30f, 1.5f)
        e.viewport(viewport); e.undo()
        assertTrue(e.graph.devices.isEmpty()); assertEquals(viewport, e.graph.viewport)
        e.redo(); assertEquals(viewport, e.graph.viewport)
        rejects { GraphViewport(zoom = Float.NaN) }; rejects { GraphViewport(zoom = 4f) }
    }
    @Test fun configurationIsValidatedBeforeChange() {
        val e = editor(); val a = e.add("tracker"); val old = e.graph
        rejects { e.update(a.copy(configuredOptions = a.configuredOptions + ("targetValue" to DeviceOptionValue.IntegerValue(-1)))) }
        assertEquals(old, e.graph)
        e.update(a.copy(name = "Vault tracker", configuredOptions = a.configuredOptions + ("targetValue" to DeviceOptionValue.IntegerValue(5))))
        e.undo(); assertEquals(old, e.graph)
    }
}
