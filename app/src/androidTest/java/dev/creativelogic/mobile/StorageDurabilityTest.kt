package dev.creativelogic.mobile

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import dev.creativelogic.mobile.mechanicstorage.MechanicDatabase
import dev.creativelogic.mobile.mechanicstorage.RoomMechanicRepository
import dev.creativelogic.model.MechanicDocumentV1
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
        val document = MechanicDocumentV1(mechanicMetadata = MechanicMetadataV1(
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
