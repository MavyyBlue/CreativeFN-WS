package dev.creativelogic.mobile.uigraph

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.creativelogic.catalog.InitialDeviceCatalog
import dev.creativelogic.graph.*
import dev.creativelogic.mobile.uisimulator.PrototypeSimulatorOverlay
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
    var showDiagnostics by rememberSaveable { mutableStateOf(false) }
    var toolsExpanded by rememberSaveable { mutableStateOf(false) }
    val diagnostics = remember(graph.devices, graph.connections) { GraphDiagnostics.inspect(graph, InitialDeviceCatalog.catalog) }
    // Capture a fixed configuration when entering the test scene; no runtime data is stored.
    var simulatorGraph by remember { mutableStateOf<MechanicGraph?>(null) }
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
    // The graph is full-bleed beneath the app header. Floating controls do not consume
    // layout height or alter saved device coordinates / viewport persistence.
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceContainerLow)
        .onSizeChanged { canvasSize = Size(it.width.toFloat(), it.height.toFloat()) }) {
        if (simulatorGraph == null) {
            LogicCanvas(graph, source, { selected = it; toolsExpanded = false }, { context = it; toolsExpanded = false },
                ::selectEvent, ::connect,
                { id, x, y -> change { session.editor.move(id, x, y) } },
                { session.editor.viewport(it); session.markDirty() })
        } else {
            LogicCanvas(graph, null, { }, { }, { _, _ -> }, { _, _ -> },
                { _, _, _ -> }, { session.editor.viewport(it); session.markDirty() }, readOnly = true)
            PrototypeSimulatorOverlay(simulatorGraph!!)
        }
        // A compact dial opens into a fan; choosing anything closes it again.
        GraphToolDial(
            expanded = toolsExpanded, inSimulation = simulatorGraph != null,
            canFit = graph.devices.isNotEmpty(),
            onToggle = { toolsExpanded = !toolsExpanded },
            onDevices = { toolsExpanded = false; palette = true },
            onBindings = { toolsExpanded = false; bindings = true },
            onFit = { toolsExpanded = false; fit() },
            onSimulation = {
                toolsExpanded = false
                simulatorGraph = if (simulatorGraph == null) graph.copy() else null
                palette = false; bindings = false; selected = null
                context = null; source = null; event = null
            },
            modifier = Modifier.align(Alignment.TopStart)
                .then(if (simulatorGraph != null) Modifier.padding(top = 112.dp) else Modifier),
        )
        if (simulatorGraph == null) {
            Column(Modifier.align(Alignment.TopEnd).padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.End) {
                FilledTonalButton(onClick = { scope.launch { session.saveLatest() } },
                    modifier = Modifier.testTag("graph-save-button").heightIn(min = 40.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)) {
                    Text(when (status) {
                        GraphSaveStatus.SAVED -> "Saved"
                        GraphSaveStatus.SAVING -> "Saving…"
                        else -> "Save"
                    }, style = MaterialTheme.typography.labelSmall)
                }
                FilledTonalButton(onClick = { change { session.editor.undo() } },
                    enabled = session.editor.canUndo, modifier = Modifier.testTag("graph-undo").heightIn(min = 40.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)) {
                    Text("Undo", style = MaterialTheme.typography.labelSmall)
                }
                FilledTonalButton(onClick = { change { session.editor.redo() } },
                    enabled = session.editor.canRedo, modifier = Modifier.testTag("graph-redo").heightIn(min = 40.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)) {
                    Text("Redo", style = MaterialTheme.typography.labelSmall)
                }
                FilledTonalButton(onClick = { toolsExpanded = false; showDiagnostics = true },
                    modifier = Modifier.testTag("graph-health-check").heightIn(min = 40.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)) {
                    Text(when {
                        diagnostics.errors > 0 -> "Check !${diagnostics.errors}"
                        diagnostics.warnings > 0 -> "Check ${diagnostics.warnings}"
                        else -> "Check"
                    }, style = MaterialTheme.typography.labelSmall,
                        color = if (diagnostics.errors > 0) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
        }
        if (status == GraphSaveStatus.FAILED && simulatorGraph == null) {
            Surface(Modifier.align(Alignment.BottomCenter).padding(bottom = 64.dp),
                color = MaterialTheme.colorScheme.errorContainer, shape = MaterialTheme.shapes.small) {
                Text("Couldn't save. Tap Save to retry.", Modifier.padding(9.dp),
                    style = MaterialTheme.typography.labelSmall)
            }
        }
        if (source != null && simulatorGraph == null) {
            Surface(Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp, start = 12.dp, end = 12.dp),
                shape = MaterialTheme.shapes.small, tonalElevation = 5.dp) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 10.dp)) {
                    Text("Event selected → choose a function", style = MaterialTheme.typography.labelSmall)
                    TextButton(onClick = { source = null; event = null }) { Text("Cancel") }
                }
            }
        }
        SnackbarHost(snackbars, modifier = Modifier.align(Alignment.BottomCenter))
    }
    if (showDiagnostics) AlertDialog(onDismissRequest = { showDiagnostics = false },
        title = { Text("Mechanic health check") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()).testTag("graph-diagnostics"),
                verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("${diagnostics.errors} errors · ${diagnostics.warnings} warnings · local checks only",
                    style = MaterialTheme.typography.labelMedium)
                if (diagnostics.items.isEmpty()) Text("No issues found in supported configuration checks. Test in Fortnite before publishing.")
                diagnostics.items.forEach { issue ->
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("${issue.severity}: ${issue.title}", style = MaterialTheme.typography.titleSmall,
                            color = if (issue.severity == DiagnosticSeverity.ERROR) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.onSurface)
                        Text(issue.explanation, style = MaterialTheme.typography.bodySmall)
                        Text("Try: ${issue.action}", style = MaterialTheme.typography.bodySmall)
                        val id = issue.deviceId
                        if (id != null && graph.devices.any { it.id == id }) {
                            TextButton(onClick = { selected = id; showDiagnostics = false },
                                modifier = Modifier.testTag("diagnostic-open-device")) { Text("Open device") }
                        }
                    }
                    HorizontalDivider()
                }
            }
        },
        confirmButton = { TextButton(onClick = { showDiagnostics = false }) { Text("Close") } })
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
