package dev.creativelogic.catalog

import dev.creativelogic.model.*
import org.junit.Assert.*
import org.junit.Test
import java.util.UUID

class EpicPortReferenceTest {
    private val catalog = InitialDeviceCatalog.catalog
    @Test fun namedPortsMatchTheDocumentedRestrictedSet() {
        fun events(type: String) = catalog.definition(type).events.map { it.id.value }
        fun functions(type: String) = catalog.definition(type).functions.map { it.id.value }
        assertEquals(listOf("onInteract"), events("button"))
        assertEquals(listOf("onRoundStart"), events("round-settings"))
        assertEquals(listOf("onMatchingCreatureTypeEliminated"), events("creature-manager"))
        assertEquals(listOf("onEliminated", "onElimination"), events("elimination-manager"))
        assertTrue(events("hud-message").isEmpty())
        assertEquals(listOf("show", "hide"), functions("hud-message"))
        catalog.definitions.forEach { def ->
            assertTrue(def.limitations.isNotEmpty())
            assertEquals(SimulationSupport.NOT_SIMULATED, def.simulationSupport)
            assertNotNull(catalog.createInstance(def.deviceTypeId, def.displayName))
        }
    }
    @Test fun newPortsAreValidatedAgainstDeviceTypes() {
        val button = catalog.createInstance("button", "B")
        val hud = catalog.createInstance("hud-message", "H")
        val correct = LogicConnection(UUID.randomUUID().toString(), button.id, DeviceEventId("onInteract"), hud.id, DeviceFunctionId("show"))
        catalog.validateBinding(correct, listOf(button, hud))
        try {
            catalog.validateBinding(correct.copy(targetFunctionId = DeviceFunctionId("invented")), listOf(button, hud))
            fail("Unknown invented function was accepted")
        } catch (_: IllegalArgumentException) { }
    }
}
