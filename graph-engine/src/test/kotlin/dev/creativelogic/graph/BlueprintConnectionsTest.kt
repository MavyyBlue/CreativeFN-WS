package dev.creativelogic.graph

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BlueprintConnectionsTest {
    private fun device(id: String) = BlueprintDevice(id, id, "Trigger", 0f, 0f, emptyList(), null, "")
    private fun link(from: String, to: String) = BlueprintBinding(from, to, from, "On Triggered", to, "Enable")
    private val blueprint = Blueprint("mechanic", "Focus test", emptyList(),
        listOf(device("A"), device("B"), device("C"), device("D"), device("E")),
        listOf(link("A", "B"), link("C", "A"), link("B", "D"), link("B", "A")),
        emptyList(), emptyList())

    @Test fun longPressFocusOnlyIncludesDirectIncomingAndOutgoingNeighbors() {
        assertEquals(setOf("A", "B", "C"), BlueprintConnections.directlyConnected(blueprint, "A"))
        assertTrue("Must not traverse to D via B", "D" !in BlueprintConnections.directlyConnected(blueprint, "A"))
        assertEquals(setOf("E"), BlueprintConnections.directlyConnected(blueprint, "E"))
        assertTrue(BlueprintConnections.directlyConnected(blueprint, "missing").isEmpty())
    }
    @Test fun outgoingAndIncomingPreservePortDirectionAndDuplicatesAreLegitimate() {
        assertEquals(3, BlueprintConnections.touching(blueprint, "A").size)
        assertEquals(0, BlueprintConnections.touching(blueprint, "E").size)
        assertEquals("C", BlueprintConnections.touching(blueprint, "A")[1].sourceId)
    }
}
