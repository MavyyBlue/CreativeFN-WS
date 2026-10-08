package dev.creativelogic.graph

import dev.creativelogic.catalog.DeviceCatalog
import dev.creativelogic.model.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

enum class GraphSaveStatus { SAVED, PENDING, SAVING, FAILED }

/** Serialized writes always read the newest graph after taking the write lock. */
class GraphDraftSession(
    val document: SavedMechanic, catalog: DeviceCatalog, private val repository: MechanicRepository,
    private val appVersion: String, private val now: () -> Long = System::currentTimeMillis,
) {
    val editor = GraphEditor(document.graph, catalog)
    private val mutableStatus = MutableStateFlow(GraphSaveStatus.SAVED)
    val saveStatus = mutableStatus.asStateFlow()
    private val writeLock = Mutex()
    private var saved = document.graph
    fun markDirty() {
        if (editor.graph != saved && mutableStatus.value != GraphSaveStatus.SAVING && mutableStatus.value != GraphSaveStatus.FAILED) mutableStatus.value = GraphSaveStatus.PENDING
    }
    suspend fun saveLatest(): Boolean = writeLock.withLock {
        val snapshot = editor.graph
        if (snapshot == saved) { mutableStatus.value = GraphSaveStatus.SAVED; return@withLock true }
        mutableStatus.value = GraphSaveStatus.SAVING
        try {
            repository.save(document.copy(appVersion = appVersion, graph = snapshot,
                mechanicMetadata = document.mechanicMetadata.copy(modifiedAtEpochMillis = now())))
            saved = snapshot
            mutableStatus.value = if (editor.graph == snapshot) GraphSaveStatus.SAVED else GraphSaveStatus.PENDING
            true
        } catch (cancelled: CancellationException) {
            mutableStatus.value = GraphSaveStatus.PENDING
            throw cancelled
        } catch (_: Exception) {
            mutableStatus.value = GraphSaveStatus.FAILED
            false
        }
    }
}
