package dev.creativelogic.catalog

import dev.creativelogic.model.*
import org.junit.Assert.*
import org.junit.Test
import java.util.UUID

class DeviceCatalogTest {
    private val catalog = InitialDeviceCatalog.catalog
    private fun rejects(action: () -> Unit) {
        try { action(); fail("Expected rejection") } catch (_: IllegalArgumentException) { }
    }
    @Test fun catalogHasUniqueTypedPortsAndHonestVersionMetadata() {
        assertEquals(listOf("trigger", "tracker", "barrier"), catalog.definitions.map { it.deviceTypeId })
        assertEquals(1, catalog.catalogSchemaVersion)
        assertNull(catalog.lastVerifiedDate); assertNull(catalog.fortniteReferenceVersion)
        catalog.definitions.forEach { d ->
            assertEquals(SimulationSupport.NOT_SIMULATED, d.simulationSupport)
            assertTrue(d.limitations.isNotEmpty()); assertTrue(d.referenceUrl.startsWith("https://dev.epicgames.com/"))
            catalog.validate(catalog.createInstance(d.deviceTypeId, d.displayName))
        }
        assertEquals(DeviceEventId("onTriggered"), catalog.definition("trigger").events.single().id)
        assertTrue(catalog.definition("barrier").events.isEmpty())
        rejects { catalog.copy(catalogSchemaVersion = 2) }
        rejects { catalog.copy(definitions = catalog.definitions + catalog.definitions.first()) }
    }
    @Test fun defaultsAndModifiedOptionsRoundTripForEveryInitialDevice() {
        val codec = DeviceInstanceCodec(catalog)
        catalog.definitions.forEach { d ->
            val original = catalog.createInstance(d.deviceTypeId, "My ${d.displayName}").copy(graphX = -10f, graphY = 5.5f, builderNotes = "Keep this name")
            assertEquals(original, codec.decode(codec.encode(original)))
        }
        val modified = catalog.createInstance("trigger", "Key").let { it.copy(configuredOptions = it.configuredOptions + mapOf(
            "timesCanTrigger" to DeviceOptionValue.IntegerValue(1), "resetDelay" to DeviceOptionValue.DurationValue(1500), "triggeredByPlayer" to DeviceOptionValue.BooleanValue(false))) }
        assertEquals(modified, codec.decode(codec.encode(modified)))
        val tracker = catalog.createInstance("tracker", "Objective").let { it.copy(configuredOptions = it.configuredOptions + ("sharing" to DeviceOptionValue.EnumValue("Team"))) }
        assertEquals(tracker, codec.decode(codec.encode(tracker)))
    }
    @Test fun badConfigurationIsRejectedWithoutCoercion() {
        val trigger = catalog.createInstance("trigger", "Key")
        rejects { catalog.validate(trigger.copy(configuredOptions = trigger.configuredOptions - "timesCanTrigger")) }
        rejects { catalog.validate(trigger.copy(configuredOptions = trigger.configuredOptions + ("extra" to DeviceOptionValue.BooleanValue(true)))) }
        rejects { catalog.validate(trigger.copy(configuredOptions = trigger.configuredOptions + ("timesCanTrigger" to DeviceOptionValue.TextValue("1")))) }
        rejects { catalog.validate(trigger.copy(configuredOptions = trigger.configuredOptions + ("resetDelay" to DeviceOptionValue.DurationValue(-1)))) }
        val tracker = catalog.createInstance("tracker", "Goal")
        rejects { catalog.validate(tracker.copy(configuredOptions = tracker.configuredOptions + ("sharing" to DeviceOptionValue.EnumValue("Unknown")))) }
        rejects { trigger.copy(id = "invalid") }; rejects { trigger.copy(graphX = Float.NaN) }
        rejects { catalog.createInstance("unverified-device", "Unknown") }
    }
    @Test fun semanticBindingsRejectWrongAndMissingPorts() {
        val trigger = catalog.createInstance("trigger", "Key")
        val tracker = catalog.createInstance("tracker", "Goal")
        val binding = LogicConnection(UUID.randomUUID().toString(), trigger.id, DeviceEventId("onTriggered"), tracker.id, DeviceFunctionId("incrementProgress"))
        catalog.validateBinding(binding, listOf(trigger, tracker))
        rejects { catalog.validateBinding(binding.copy(sourceEventId = DeviceEventId("enable")), listOf(trigger, tracker)) }
        rejects { catalog.validateBinding(binding.copy(targetFunctionId = DeviceFunctionId("onComplete")), listOf(trigger, tracker)) }
        try { catalog.validateBinding(binding, listOf(trigger)); fail("Missing device accepted") } catch (_: IllegalStateException) { }
        rejects { catalog.validateBinding(binding, listOf(trigger, tracker, tracker)) }
    }
    @Test fun wireVersionsAndMalformedValuesRequireExplicitMigrationOrRejection() {
        val codec = DeviceInstanceCodec(catalog)
        val source = codec.encode(catalog.createInstance("trigger", "Key"))
        rejects { codec.decode(source.replace("\"formatVersion\":1", "\"formatVersion\":2")) }
        rejects { codec.decode(source.replace("0.1.0-draft", "future")) }
        rejects { codec.decode(source.replace("\"value\":true", "\"value\":\"true\"")) }
        rejects { codec.decode(source.replace("\"type\":\"boolean\"", "\"type\":\"unknown\"")) }
    }
    @Test fun textOptionWireValuePreservesUnicode() {
        val definition = catalog.definition("trigger").copy(options = listOf(DeviceOptionDefinition("note", "Note", DeviceOptionKind.TEXT, DeviceOptionValue.TextValue("鍵 🗝️"))))
        val textCatalog = catalog.copy(definitions = listOf(definition))
        val original = textCatalog.createInstance("trigger", "Text")
        val codec = DeviceInstanceCodec(textCatalog)
        assertEquals(original, codec.decode(codec.encode(original)))
    }
}
