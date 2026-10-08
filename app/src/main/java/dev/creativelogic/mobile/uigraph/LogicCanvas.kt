package dev.creativelogic.mobile.uigraph

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import dev.creativelogic.catalog.InitialDeviceCatalog
import dev.creativelogic.model.*
import kotlin.math.*

private const val NODE_WIDTH = 224f
private const val PORT_TOP = 80f
private const val PORT_ROW = 28f
private fun definition(d: CreativeDeviceInstance) = InitialDeviceCatalog.catalog.definition(d.deviceTypeId)
private fun nodeHeight(d: CreativeDeviceInstance) = PORT_TOP + max(definition(d).functions.size, definition(d).events.size) * PORT_ROW + 16f
private fun port(d: CreativeDeviceInstance, event: Boolean, index: Int) = Offset(d.graphX + if (event) NODE_WIDTH - 8f else 8f, d.graphY + PORT_TOP + index * PORT_ROW)
private data class Hit(val device: CreativeDeviceInstance, val event: DeviceEventId? = null, val function: DeviceFunctionId? = null)
private fun hit(graph: MechanicGraph, point: Offset, zoom: Float): Hit? {
    for (d in graph.devices.asReversed()) {
        val def = definition(d)
        val candidates = def.events.mapIndexed { i, e -> port(d, true, i) to Hit(d, event = e.id) } +
            def.functions.mapIndexed { i, f -> port(d, false, i) to Hit(d, function = f.id) }
        candidates.minByOrNull { (it.first - point).getDistance() }?.let {
            if ((it.first - point).getDistance() <= min(22f / zoom, 28f)) return it.second
        }
        if (Rect(d.graphX, d.graphY, d.graphX + NODE_WIDTH, d.graphY + nodeHeight(d)).contains(point)) return Hit(d)
    }
    return null
}

@Composable
internal fun LogicCanvas(
    graph: MechanicGraph, pendingSource: String?, onConfigure: (String) -> Unit, onContext: (String) -> Unit,
    onEvent: (String, DeviceEventId) -> Unit, onFunction: (String, DeviceFunctionId) -> Unit,
    onMove: (String, Float, Float) -> Unit, onViewport: (GraphViewport) -> Unit,
) {
    var viewport by remember { mutableStateOf(graph.viewport) }
    LaunchedEffect(graph.viewport) { viewport = graph.viewport }
    val configure by rememberUpdatedState(onConfigure)
    val context by rememberUpdatedState(onContext)
    val eventAction by rememberUpdatedState(onEvent)
    val functionAction by rememberUpdatedState(onFunction)
    val moveAction by rememberUpdatedState(onMove)
    val viewportAction by rememberUpdatedState(onViewport)
    var moving by remember { mutableStateOf<CreativeDeviceInstance?>(null) }
    val currentGraph by rememberUpdatedState(graph)
    val paint = remember { Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL) } }
    val body = Color(0xFF1A1B23); val border = Color(0xFF48495C); val green = Color(0xFFB4F578); val purple = Color(0xFFB7B1FF)
    Canvas(Modifier.fillMaxSize().testTag("logic-canvas").semantics {
        contentDescription = "Creative device graph"
        stateDescription = "${graph.devices.size} devices, ${graph.connections.size} bindings"
        customActions = graph.devices.map { d -> CustomAccessibilityAction("Configure ${d.name}") { onConfigure(d.id); true } }
    }.pointerInput(Unit) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            val start = down.position
            val startView = viewport
            val startWorld = (start / density - Offset(viewport.panX, viewport.panY)) / viewport.zoom
            val selected = hit(currentGraph, startWorld, viewport.zoom)
            var dragged = false
            var transformed = false
            var longPressed = false
            var displacement = Offset.Zero
            var last = start
            var pressed = true
            var elapsed = 0L
            while (pressed) {
                val event = if (!dragged && !transformed && !longPressed && selected != null)
                    withTimeoutOrNull((viewConfiguration.longPressTimeoutMillis - elapsed).coerceAtLeast(1)) { awaitPointerEvent() }
                else awaitPointerEvent()
                if (event == null) { context(selected!!.device.id); longPressed = true; continue }
                elapsed = event.changes.maxOf { it.uptimeMillis } - down.uptimeMillis
                pressed = event.changes.any { it.pressed }
                val active = event.changes.count { it.pressed }
                if (active > 1) {
                    transformed = true; moving = null
                    val centroid = event.calculateCentroid(useCurrent = false) / density
                    val newZoom = (viewport.zoom * event.calculateZoom()).coerceIn(0.1f, 3f)
                    val ratio = newZoom / viewport.zoom
                    val pan = centroid + (Offset(viewport.panX, viewport.panY) - centroid) * ratio + event.calculatePan() / density
                    viewport = GraphViewport(pan.x, pan.y, newZoom)
                    event.changes.forEach { it.consume() }
                } else if (active == 1 && !longPressed) {
                    val position = event.changes.first { it.pressed }.position
                    displacement += position - last; last = position
                    if (displacement.getDistance() > viewConfiguration.touchSlop) dragged = true
                    if (dragged) {
                        if (!transformed && selected != null && selected.event == null && selected.function == null) {
                            moving = selected.device.copy(graphX = selected.device.graphX + displacement.x / density / viewport.zoom,
                                graphY = selected.device.graphY + displacement.y / density / viewport.zoom)
                        } else if (!transformed) {
                            viewport = startView.copy(panX = startView.panX + displacement.x / density, panY = startView.panY + displacement.y / density)
                        } else {
                            val pan = event.calculatePan() / density
                            viewport = viewport.copy(panX = viewport.panX + pan.x, panY = viewport.panY + pan.y)
                        }
                        event.changes.forEach { it.consume() }
                    }
                }
            }
            moving?.let { moveAction(it.id, it.graphX, it.graphY) }; moving = null
            if (viewport != startView) viewportAction(viewport)
            if (!dragged && !transformed && !longPressed) selected?.let {
                when {
                    it.event != null -> eventAction(it.device.id, it.event)
                    it.function != null -> functionAction(it.device.id, it.function)
                    else -> configure(it.device.id)
                }
            }
        }
    }) {
        val step = 24.dp.toPx() * viewport.zoom
        if (step >= 8f) {
            var x = (viewport.panX * density % step + step) % step
            while (x < size.width) {
                var y = (viewport.panY * density % step + step) % step
                while (y < size.height) { drawCircle(border.copy(alpha = .45f), 1f, Offset(x, y)); y += step }
                x += step
            }
        }
        val devices = graph.devices.map { if (moving?.id == it.id) moving!! else it }
        val byId = devices.associateBy { it.id }
        val pixelsPerDp = density
        withTransform({ translate(viewport.panX * pixelsPerDp, viewport.panY * pixelsPerDp); scale(viewport.zoom, viewport.zoom, Offset.Zero) }) {
            graph.connections.forEach { c ->
                val source = byId.getValue(c.sourceDeviceId); val target = byId.getValue(c.targetDeviceId)
                val a = port(source, true, definition(source).events.indexOfFirst { it.id == c.sourceEventId }) * density
                val b = port(target, false, definition(target).functions.indexOfFirst { it.id == c.targetFunctionId }) * density
                val bend = max(48f * density, abs(b.x - a.x) / 2)
                val path = Path().apply { moveTo(a.x, a.y); cubicTo(a.x + bend, a.y, b.x - bend, b.y, b.x, b.y) }
                drawPath(path, purple, style = androidx.compose.ui.graphics.drawscope.Stroke(2.dp.toPx()))
                drawLine(purple, b - Offset(8.dp.toPx(), 5.dp.toPx()), b, 2.dp.toPx())
                drawLine(purple, b - Offset(8.dp.toPx(), -5.dp.toPx()), b, 2.dp.toPx())
            }
            devices.forEach { d ->
                val origin = Offset(d.graphX, d.graphY) * density; val def = definition(d)
                drawRoundRect(body, origin, Size(NODE_WIDTH * density, nodeHeight(d) * density), CornerRadius(14.dp.toPx()))
                drawRoundRect(if (d.id == pendingSource) green else border, origin, Size(NODE_WIDTH * density, nodeHeight(d) * density), CornerRadius(14.dp.toPx()), style = androidx.compose.ui.graphics.drawscope.Stroke(1.5.dp.toPx()))
                fun text(value: String, x: Float, y: Float, sizeDp: Float, color: Color, maxWidth: Float) {
                    paint.textSize = sizeDp * density; paint.color = color.toArgb()
                    var label = value
                    while (paint.measureText(label) > maxWidth * density && label.length > 1) label = label.dropLast(1)
                    if (label != value) label = label.dropLast(1) + "…"
                    drawContext.canvas.nativeCanvas.drawText(label, (d.graphX + x) * density, (d.graphY + y) * density, paint)
                }
                text(d.name, 14f, 24f, 16f, Color.White, 196f)
                text("${def.displayName} · Reference only", 14f, 44f, 11f, purple, 196f)
                text("FUNCTIONS", 14f, 62f, 9f, purple, 98f); text("EVENTS", 132f, 62f, 9f, green, 78f)
                def.functions.forEachIndexed { i, f ->
                    drawCircle(purple, 5.dp.toPx(), port(d, false, i) * density)
                    text(f.displayName, 18f, PORT_TOP + i * PORT_ROW + 4f, 11f, Color(0xFFE1DDFF), 94f)
                }
                def.events.forEachIndexed { i, e ->
                    drawCircle(green, 5.dp.toPx(), port(d, true, i) * density)
                    text(e.displayName, 122f, PORT_TOP + i * PORT_ROW + 4f, 11f, Color(0xFFD3F6BA), 88f)
                }
            }
        }
    }
}
