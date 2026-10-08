package dev.creativelogic.graph

import dev.creativelogic.catalog.InitialDeviceCatalog
import dev.creativelogic.model.*
import org.junit.Assert.*
import org.junit.Test

class MechanicGraphCodecTest {
    private val catalog = InitialDeviceCatalog.catalog
    private val codec = MechanicGraphCodec(catalog)
    private val metadata = MechanicMetadataV1("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee", "Vault", 100, 200)
    private fun graph(): SavedMechanic {
        val e = GraphEditor(MechanicGraph(), catalog)
        val a = e.add("trigger"); val b = e.add("tracker"); val c = e.add("barrier")
        e.update(a.copy(builderNotes = "鍵 🗝️", configuredOptions = a.configuredOptions + ("timesCanTrigger" to DeviceOptionValue.IntegerValue(1))))
        e.connect(a.id, DeviceEventId("onTriggered"), b.id, DeviceFunctionId("incrementProgress"))
        e.connect(b.id, DeviceEventId("onComplete"), c.id, DeviceFunctionId("disable"))
        e.viewport(GraphViewport(-100f, 20f, 0.75f))
        return SavedMechanic(metadata, graph = e.graph, notes = "Mechanic notes")
    }
    @Test fun populatedGraphRoundTripsEverySettingBindingAndViewport() {
        val original = graph()
        assertEquals(original, codec.decode(codec.encode(original)))
    }
    @Test fun emptyVersionOneDraftMigratesWithoutLosingMetadataAndNotes() {
        val old = MechanicDocumentV1(mechanicMetadata = metadata, notes = "Old notes")
        val migrated = codec.decode(MechanicDocumentCodec.encode(old))
        assertEquals(metadata, migrated.mechanicMetadata); assertEquals(old.notes, migrated.notes)
        assertTrue(migrated.graph.devices.isEmpty())
        assertEquals(migrated, codec.decode(codec.encode(migrated)))
    }
    @Test(expected = IllegalArgumentException::class) fun futureSchemaIsNeverSilentlyAccepted() { codec.decode(codec.encode(graph()).replace("\"formatVersion\":2", "\"formatVersion\":3")) }
    @Test(expected = IllegalArgumentException::class) fun danglingBindingCannotBeSaved() { val original = graph(); codec.encode(original.copy(graph = original.graph.copy(devices = original.graph.devices.drop(1)))) }
    @Test(expected = IllegalArgumentException::class) fun legacyPopulatedReservedFieldsRemainRejected() {
        val old = MechanicDocumentCodec.encode(MechanicDocumentV1(mechanicMetadata = metadata)).replace("\"devices\":[]", "\"devices\":[{}]")
        codec.decode(old)
    }
}
