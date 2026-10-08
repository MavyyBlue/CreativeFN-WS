package dev.creativelogic.mobile

import androidx.room.Room
import dev.creativelogic.graph.GraphEditor
import dev.creativelogic.catalog.InitialDeviceCatalog
import dev.creativelogic.model.*
import androidx.test.platform.app.InstrumentationRegistry
import dev.creativelogic.mobile.mechanicstorage.MechanicDatabase
import dev.creativelogic.mobile.mechanicstorage.RoomMechanicRepository
import dev.creativelogic.model.SavedMechanic
import dev.creativelogic.model.MechanicMetadataV1
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class StorageDurabilityTest {
    @Test fun recordSurvivesDatabaseReopen() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val filename = "durability-test.db"
        context.deleteDatabase(filename)
        val editor = GraphEditor(MechanicGraph(), InitialDeviceCatalog.catalog)
        val a = editor.add("trigger"); val b = editor.add("tracker"); val c = editor.add("barrier")
        editor.connect(a.id, DeviceEventId("onTriggered"), b.id, DeviceFunctionId("incrementProgress"))
        editor.connect(b.id, DeviceEventId("onComplete"), c.id, DeviceFunctionId("disable"))
        editor.move(b.id, -30f, 90f)
        editor.viewport(GraphViewport(60f, 20f, 0.7f))
        val document = SavedMechanic(graph = editor.graph, mechanicMetadata = MechanicMetadataV1(
            "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee", "Durable mechanic", 1, 2,
        ))
        try {
            val firstDatabase = Room.databaseBuilder(context, MechanicDatabase::class.java, filename).build()
            try {
                RoomMechanicRepository(firstDatabase.mechanics()).save(document)
            } finally { firstDatabase.close() }
            val reopenedDatabase = Room.databaseBuilder(context, MechanicDatabase::class.java, filename).build()
            try {
                assertEquals(document, RoomMechanicRepository(reopenedDatabase.mechanics()).observeAll().first().single())
            } finally { reopenedDatabase.close() }
        } finally { context.deleteDatabase(filename) }
    }
}
