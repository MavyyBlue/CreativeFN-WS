package dev.creativelogic.mobile.uigraph

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.creativelogic.catalog.InitialDeviceCatalog
import dev.creativelogic.graph.GraphDraftSession
import dev.creativelogic.mobile.BuildConfig
import dev.creativelogic.model.*
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class GraphWorkspaceViewModel(private val repository: MechanicRepository) : ViewModel() {
    private val mutable = MutableStateFlow<GraphDraftSession?>(null)
    val session = mutable.asStateFlow()
    private val opening = Mutex()
    private var saving: Job? = null
    @OptIn(FlowPreview::class)
    suspend fun open(document: SavedMechanic) = opening.withLock {
        if (mutable.value?.document?.mechanicMetadata?.id == document.mechanicMetadata.id) return@withLock
        if (mutable.value?.saveLatest() == false) return@withLock
        saving?.cancel()
        val next = GraphDraftSession(document, InitialDeviceCatalog.catalog, repository, BuildConfig.VERSION_NAME)
        mutable.value = next
        saving = viewModelScope.launch {
            next.editor.state.onEach { next.markDirty() }.debounce(500).collect { next.saveLatest() }
        }
    }
    suspend fun flush() = mutable.value?.saveLatest() ?: true
    fun flushInBackground() { viewModelScope.launch { flush() } }
}
