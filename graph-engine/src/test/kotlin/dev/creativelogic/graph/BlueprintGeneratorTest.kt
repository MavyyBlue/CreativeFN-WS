package dev.creativelogic.graph

import dev.creativelogic.catalog.InitialDeviceCatalog
import dev.creativelogic.model.*
import org.junit.Assert.*
import org.junit.Test

class BlueprintGeneratorTest {
    private val catalog = InitialDeviceCatalog.catalog

    @Test fun producesExactSettingsAndDirectBindingsWithoutMutatingSource() {
        val editor = GraphEditor(MechanicGraph(), catalog)
        val button = editor.add("button")
        val hud = editor.add("hud-message")
        editor.connect(button.id, DeviceEventId("onInteract"), hud.id, DeviceFunctionId("show"))
        val before = editor.graph
        val mechanic = SavedMechanic(MechanicMetadataV1("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee", "Door prompt", 1, 2), graph = before)
        val guide = BlueprintGenerator.create(mechanic, catalog)
        assertEquals(listOf("Button" to 1, "HUD Message" to 1), guide.quantities)
        assertEquals("On Interact", guide.bindings.single().eventName)
        assertEquals("Show", guide.bindings.single().functionName)
        assertTrue(guide.steps.any { "Bind Button_1" in it && "HUD Message_1" in it })
        assertTrue(guide.steps.any { "Enabled at Game Start" in it })
        assertTrue(guide.warnings.any { "not verified" in it })
        assertEquals(before, editor.graph)
    }

    @Test fun emptyGraphIsExplainedNotInvented() {
        val mechanic = SavedMechanic(MechanicMetadataV1("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee", "Empty", 1, 2))
        val guide = BlueprintGenerator.create(mechanic, catalog)
        assertTrue(guide.devices.isEmpty())
        assertTrue(guide.warnings.any { "no devices" in it })
    }
}
