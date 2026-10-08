package dev.creativelogic.mobile.uigraph

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.creativelogic.graph.*
import dev.creativelogic.mobile.ui.InformationButton
import dev.creativelogic.model.*
import kotlinx.coroutines.launch
import kotlin.math.*

@Composable
fun GraphEditorScreen(document: SavedMechanic?, workspace: GraphWorkspaceViewModel) {
    LaunchedEffect(document?.mechanicMetadata?.id) { if (document != null) workspace.open(document) }
    val session by workspace.session.collectAsStateWithLifecycle()
    val active = session?.takeIf { it.document.mechanicMetadata.id == document?.mechanicMetadata?.id }
    if (active == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
    } else key(active.document.mechanicMetadata.id) { GraphEditorContent(active) }
}

@Composable
private fun GraphEditorContent(session: GraphDraftSession) {
    val graph by session.editor.state.collectAsStateWithLifecycle()
    val status by session.saveStatus.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val snackbars = remember { SnackbarHostState() }
    var palette by rememberSaveable { mutableStateOf(false) }
    var bindings by rememberSaveable { mutableStateOf(false) }
    var selected by rememberSaveable { mutableStateOf<String?>(null) }
    var context by rememberSaveable { mutableStateOf<String?>(null) }
    var source by rememberSaveable { mutableStateOf<String?>(null) }
    var event by rememberSaveable { mutableStateOf<String?>(null) }
    var canvasSize by remember { mutableStateOf(Size.Zero) }
    val density = LocalDensity.current.density
    fun change(action: () -> Unit) {
        try { action(); session.markDirty() }
        catch (error: IllegalArgumentException) { scope.launch { snackbars.showSnackbar(error.message ?: "This binding is not valid.") } }
    }
    fun fit() {
        if (graph.devices.isEmpty() || canvasSize.width <= 0) return
        val left = graph.devices.minOf { it.graphX }; val top = graph.devices.minOf { it.graphY }
        val width = graph.devices.maxOf { it.graphX } - left + 248f
        val height = graph.devices.maxOf { it.graphY } - top + 264f
        val zoom = min((canvasSize.width / density - 32f) / width, (canvasSize.height / density - 32f) / height).coerceIn(0.1f, 1.5f)
        session.editor.viewport(GraphViewport(16f - left * zoom, 16f - top * zoom, zoom))
    }
    fun selectEvent(id: String, eventId: DeviceEventId) { source = id; event = eventId.value; selected = null }
    fun connect(id: String, function: DeviceFunctionId) {
        if (source == null || event == null) { scope.launch { snackbars.showSnackbar("Choose an event first.") }; return }
        change { session.editor.connect(source!!, DeviceEventId(event!!), id, function); source = null; event = null }
        selected = null
    }
    LaunchedEffect(graph.devices) { if (graph.devices.none { it.id == source }) { source = null; event = null } }
    Column(Modifier.fillMaxSize().padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Logic canvas", Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
            InformationButton("Logic canvas", "Drag a device to move it. Drag empty space to pan; pinch to zoom. Tap a device for settings, or hold it for actions. Tap an Event then a Function to bind them, or use Bindings. These devices are reference-only: simulation is not available yet.")
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = { palette = true }, modifier = Modifier.testTag("graph-devices")) { Text("Devices") }
            TextButton(onClick = { bindings = true }, modifier = Modifier.testTag("graph-bindings")) { Text("Bindings") }
            TextButton(onClick = { scope.launch { session.saveLatest() } }, modifier = Modifier.testTag("graph-save-button")) {
                Text(when (status) { GraphSaveStatus.SAVED -> "Saved"; GraphSaveStatus.SAVING -> "Saving…"; else -> "Save" }, Modifier.testTag("graph-save"))
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = { change { session.editor.undo() } }, enabled = session.editor.canUndo, modifier = Modifier.testTag("graph-undo")) { Text("Undo") }
            TextButton(onClick = { change { session.editor.redo() } }, enabled = session.editor.canRedo, modifier = Modifier.testTag("graph-redo")) { Text("Redo") }
            TextButton(onClick = { fit() }, enabled = graph.devices.isNotEmpty()) { Text("Fit") }
        }
        if (status == GraphSaveStatus.FAILED) Text("Couldn’t save. Tap Save to retry.", color = MaterialTheme.colorScheme.error)
        if (source != null) Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Event selected → choose a function", Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
            TextButton(onClick = { source = null; event = null }) { Text("Cancel") }
        }
        Box(Modifier.fillMaxWidth().weight(1f).clip(MaterialTheme.shapes.large).background(MaterialTheme.colorScheme.surfaceContainerLow)
            .onSizeChanged { canvasSize = Size(it.width.toFloat(), it.height.toFloat()) }) {
            LogicCanvas(graph, source, { selected = it }, { context = it }, ::selectEvent, ::connect,
                { id, x, y -> change { session.editor.move(id, x, y) } }, { session.editor.viewport(it); session.markDirty() })
        }
        SnackbarHost(snackbars)
    }
    if (palette) DevicePalette(graph, { palette = false }, { type -> change { session.editor.add(type) }; palette = false }, { selected = it; palette = false })
    if (bindings) BindingPanel(graph, { bindings = false }, { a, e, b, f -> change { session.editor.connect(a, e, b, f) } }, { change { session.editor.deleteConnection(it) } })
    graph.devices.find { it.id == selected }?.let { device -> DeviceConfiguration(device, { selected = null }, { change { session.editor.update(it) } },
        { change { val copy = session.editor.duplicate(device.id); selected = copy.id } },
        { change { session.editor.deleteDevice(device.id) }; selected = null }, { selectEvent(device.id, it) }, { connect(device.id, it) }) }
    graph.devices.find { it.id == context }?.let { device -> AlertDialog(onDismissRequest = { context = null },
        title = { Text(device.name) }, text = { Column {
            TextButton(onClick = { selected = device.id; context = null }) { Text("Configure") }
            TextButton(onClick = { change { session.editor.duplicate(device.id) }; context = null }) { Text("Duplicate") }
            TextButton(onClick = { change { session.editor.deleteDevice(device.id) }; context = null }) { Text("Delete device") }
        } }, confirmButton = { TextButton(onClick = { context = null }) { Text("Close") } }) }
}
