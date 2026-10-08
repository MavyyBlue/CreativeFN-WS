package dev.creativelogic.graph

import dev.creativelogic.catalog.DeviceCatalog
import dev.creativelogic.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

fun validateGraph(graph: MechanicGraph, catalog: DeviceCatalog) {
    require(graph.devices.map { it.id }.distinct().size == graph.devices.size) { "Duplicate device ID" }
    require(graph.connections.map { it.id }.distinct().size == graph.connections.size) { "Duplicate binding ID" }
    require(graph.connections.map { listOf(it.sourceDeviceId, it.sourceEventId.value, it.targetDeviceId, it.targetFunctionId.value) }.distinct().size == graph.connections.size) { "Duplicate binding" }
    graph.devices.forEach(catalog::validate)
    val ids = graph.devices.map { it.id }.toSet()
    graph.connections.forEach {
        require(it.sourceDeviceId in ids && it.targetDeviceId in ids) { "Binding points to a missing device" }
        catalog.validateBinding(it, graph.devices)
    }
}

/** Immutable snapshots; a gesture is committed once, never once per pointer frame. */
class GraphEditor(initial: MechanicGraph, private val catalog: DeviceCatalog, private val historyLimit: Int = 50) {
    private val mutable = MutableStateFlow(initial.also { validateGraph(it, catalog) })
    val state = mutable.asStateFlow()
    val graph get() = state.value
    private val past = ArrayDeque<MechanicGraph>()
    private val future = ArrayDeque<MechanicGraph>()
    val canUndo get() = past.isNotEmpty()
    val canRedo get() = future.isNotEmpty()
    init { require(historyLimit > 0) }
    private fun change(transform: (MechanicGraph) -> MechanicGraph) {
        val next = transform(graph)
        validateGraph(next, catalog)
        if (next == graph) return
        past.addLast(graph)
        if (past.size > historyLimit) past.removeFirst()
        future.clear()
        mutable.value = next
    }
    fun add(typeId: String): CreativeDeviceInstance {
        val type = catalog.definition(typeId)
        var number = 1
        val names = graph.devices.map { it.name }.toSet()
        while ("${type.displayName}_$number" in names) number++
        val index = graph.devices.size
        val device = catalog.createInstance(typeId, "${type.displayName}_$number").copy(graphX = 24f + index % 3 * 280f, graphY = 24f + index / 3 * 240f)
        change { it.copy(devices = it.devices + device) }
        return device
    }
    fun update(device: CreativeDeviceInstance) {
        require(graph.devices.any { it.id == device.id }) { "Device does not exist" }
        change { g -> g.copy(devices = g.devices.map { if (it.id == device.id) device else it }) }
    }
    fun move(id: String, x: Float, y: Float) = update(graph.devices.single { it.id == id }.copy(graphX = x, graphY = y))
    fun duplicate(id: String): CreativeDeviceInstance {
        val original = graph.devices.single { it.id == id }
        val copy = original.copy(id = UUID.randomUUID().toString(), name = original.name + " copy", graphX = original.graphX + 28, graphY = original.graphY + 28)
        change { it.copy(devices = it.devices + copy) }
        return copy
    }
    fun deleteDevice(id: String) = change { g -> g.copy(devices = g.devices.filterNot { it.id == id }, connections = g.connections.filterNot { it.sourceDeviceId == id || it.targetDeviceId == id }) }
    fun connect(source: String, event: DeviceEventId, target: String, function: DeviceFunctionId): LogicConnection {
        val connection = LogicConnection(UUID.randomUUID().toString(), source, event, target, function)
        change { it.copy(connections = it.connections + connection) }
        return connection
    }
    fun deleteConnection(id: String) = change { it.copy(connections = it.connections.filterNot { c -> c.id == id }) }
    fun viewport(viewport: GraphViewport) { mutable.value = graph.copy(viewport = viewport) }
    fun undo() {
        if (!canUndo) return
        future.addLast(graph)
        mutable.value = past.removeLast().copy(viewport = graph.viewport)
    }
    fun redo() {
        if (!canRedo) return
        past.addLast(graph)
        mutable.value = future.removeLast().copy(viewport = graph.viewport)
    }
}
