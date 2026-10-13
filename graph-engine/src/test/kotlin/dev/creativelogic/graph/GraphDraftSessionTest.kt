package dev.creativelogic.graph

import dev.creativelogic.catalog.InitialDeviceCatalog
import dev.creativelogic.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.*
import org.junit.Test
import java.util.UUID

class GraphDraftSessionTest {
    private val initial = SavedMechanic(MechanicMetadataV1("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee", "Vault", 1, 2))
    @Test fun overlappingFlushesPersistNewestGraphInOrder() = runBlocking {
        val entered = CompletableDeferred<Unit>(); val release = CompletableDeferred<Unit>()
        val writes = mutableListOf<SavedMechanic>()
        val repo = object : MechanicRepository {
            override fun observeAll() = MutableStateFlow(emptyList<SavedMechanic>())
            override suspend fun save(document: SavedMechanic) {
                if (writes.isEmpty()) { entered.complete(Unit); release.await() }
                writes += document
            }
        }
        val session = GraphDraftSession(initial, InitialDeviceCatalog.catalog, repo, "test") { 10 }
        session.editor.add("trigger")
        val first = launch { session.saveLatest() }; entered.await()
        session.editor.add("tracker")
        val second = launch { session.saveLatest() }; release.complete(Unit)
        first.join(); second.join()
        assertEquals(listOf(1, 2), writes.map { it.graph.devices.size })
        assertEquals(session.editor.graph, writes.last().graph)
        assertEquals(GraphSaveStatus.SAVED, session.saveStatus.value)
        session.saveLatest(); assertEquals(2, writes.size)
    }
    @Test fun failedSaveKeepsGraphForExplicitRetry() = runBlocking {
        var fail = true; var saved: SavedMechanic? = null
        val repo = object : MechanicRepository {
            override fun observeAll() = MutableStateFlow(emptyList<SavedMechanic>())
            override suspend fun save(document: SavedMechanic) { if (fail) error("Storage failure"); saved = document }
        }
        val s = GraphDraftSession(initial, InitialDeviceCatalog.catalog, repo, "test")
        s.editor.add("trigger"); assertFalse(s.saveLatest())
        assertEquals(GraphSaveStatus.FAILED, s.saveStatus.value); assertEquals(1, s.editor.graph.devices.size)
        fail = false; assertTrue(s.saveLatest()); assertEquals(s.editor.graph, saved!!.graph)
    }
    @Test fun editorSavePreservesFoldersCreatedByOverlayWhileActivityWasBackgrounded() = runBlocking {
        val editor = GraphEditor(MechanicGraph(), InitialDeviceCatalog.catalog)
        val device = editor.add("trigger")
        val base = initial.copy(graph = editor.graph)
        val latest = MutableStateFlow(listOf(base))
        val repo = object : MechanicRepository {
            override fun observeAll() = latest
            override suspend fun save(document: SavedMechanic) { latest.value = listOf(document) }
        }
        val session = GraphDraftSession(base, InitialDeviceCatalog.catalog, repo, "test") { 10 }
        val folder = BlueprintFolderV1(UUID.randomUUID().toString(), "My Trigger", listOf(device.id))
        latest.value = listOf(base.copy(folders = listOf(folder))) // overlay changed Room after session opened
        session.editor.add("barrier")
        assertTrue(session.saveLatest())
        assertEquals(listOf(folder), latest.value.single().folders)
        assertEquals(2, latest.value.single().graph.devices.size)
    }
}
