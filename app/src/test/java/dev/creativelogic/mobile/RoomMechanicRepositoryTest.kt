package dev.creativelogic.mobile

import dev.creativelogic.mobile.mechanicstorage.MechanicDao
import dev.creativelogic.mobile.mechanicstorage.MechanicEntity
import dev.creativelogic.mobile.mechanicstorage.RoomMechanicRepository
import dev.creativelogic.model.MechanicDocumentV1
import dev.creativelogic.model.MechanicMetadataV1
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class RoomMechanicRepositoryTest {
    @Test fun repositoryPersistsTheExplicitJsonContract() = runBlocking {
        val rows = MutableStateFlow<List<MechanicEntity>>(emptyList())
        val dao = object : MechanicDao {
            override fun observeAll() = rows
            override suspend fun save(entity: MechanicEntity) { rows.value = listOf(entity) }
        }
        val repository = RoomMechanicRepository(dao)
        val document = MechanicDocumentV1(mechanicMetadata = MechanicMetadataV1(
            "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee", "First mechanic", 1, 2,
        ))
        repository.save(document)
        assertEquals(document, repository.observeAll().first().single())
        assertEquals(document.mechanicMetadata.id, rows.value.single().id)
    }
}
