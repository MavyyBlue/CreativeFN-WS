package dev.creativelogic.graph

import dev.creativelogic.catalog.InitialDeviceCatalog
import dev.creativelogic.model.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class BlueprintFoldersTest {
    private val catalog = InitialDeviceCatalog.catalog
    private val codec = MechanicGraphCodec(catalog)
    private fun document(): SavedMechanic {
        val editor = GraphEditor(MechanicGraph(), catalog)
        val trigger = editor.add("trigger")
        val hud = editor.add("hud-message")
        editor.connect(trigger.id, DeviceEventId("onTriggered"), hud.id, DeviceFunctionId("show"))
        return SavedMechanic(MechanicMetadataV1(UUID.randomUUID().toString(), "Folder test", 10L, 20L), graph = editor.graph)
    }
    @Test fun folderRoundTripsThroughPortableMechanicWithoutChangingGraph() {
        val plain = document()
        val ids = plain.graph.devices.map { it.id }
        val folder = BlueprintFolderV1(UUID.randomUUID().toString(), "Victory Sequence", ids)
        val organized = plain.copy(folders = listOf(folder))
        val restored = codec.decode(codec.encode(organized))
        assertEquals(organized, restored)
        assertEquals(plain.graph, restored.graph)
        assertEquals(folder, BlueprintGenerator.create(restored, catalog).folders.single())
    }
    @Test fun oldFormatTwoDocumentsWithoutFoldersStillOpen() {
        val ungrouped = document()
        val oldJson = codec.encode(ungrouped).replace(",\"folders\":[]", "")
        val restored = codec.decode(oldJson)
        assertTrue(restored.folders.isEmpty())
        assertEquals(ungrouped, restored)
    }
    @Test(expected = IllegalArgumentException::class)
    fun overlappingMembershipCannotBeSaved() {
        val plain = document()
        val root = plain.graph.devices.first().id
        val folders = listOf(
            BlueprintFolderV1(UUID.randomUUID().toString(), "First", listOf(root)),
            BlueprintFolderV1(UUID.randomUUID().toString(), "Second", listOf(root)),
        )
        codec.encode(plain.copy(folders = folders))
    }
    @Test(expected = IllegalArgumentException::class)
    fun folderReferencingDeletedDeviceIsRejected() {
        val plain = document()
        codec.encode(plain.copy(folders = listOf(BlueprintFolderV1(UUID.randomUUID().toString(), "Missing", listOf(UUID.randomUUID().toString())))))
    }
}
