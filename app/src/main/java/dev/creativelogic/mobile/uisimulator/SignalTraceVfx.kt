package dev.creativelogic.mobile.uisimulator

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.creativelogic.catalog.InitialDeviceCatalog
import dev.creativelogic.model.CreativeDeviceInstance
import dev.creativelogic.model.MechanicGraph
import dev.creativelogic.simulation.SignalVisualCue
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

/** Purely decorative, low-cost sequential playback of verified *local* trace entries.
 * Does not drive the simulator or claim that Fortnite performed any world action.
 */
@Composable
internal fun SignalTraceVfx(graph: MechanicGraph, cues: List<SignalVisualCue>, modifier: Modifier = Modifier) {
    var current by remember(graph) { mutableStateOf<SignalVisualCue?>(null) }
    val phase = remember(graph) { Animatable(0f) }
    LaunchedEffect(cues) {
        current = null
        for (cue in cues.takeLast(60)) {
            current = cue
            phase.snapTo(0f)
            phase.animateTo(1f, tween(durationMillis = if (cue.targetDeviceId == null) 550 else 850))
            delay(140)
        }
        current = null
    }
    val cue = current ?: return
    val src = graph.devices.firstOrNull { it.id == cue.sourceDeviceId } ?: return
    val dest = graph.devices.firstOrNull { it.id == cue.targetDeviceId }
    val p = phase.value
    val glow = if (cue.unsupported) Color(0xFFFFB86D) else Color(0xFF28DDF9)
    val density = androidx.compose.ui.platform.LocalDensity.current.density
    val viewport = graph.viewport
    fun location(d: CreativeDeviceInstance): Offset = Offset(
        (d.graphX * viewport.zoom + viewport.panX) * density,
        (d.graphY * viewport.zoom + viewport.panY) * density)
    fun bounds(d: CreativeDeviceInstance) = Size(224f * viewport.zoom * density,
        (96f + max(InitialDeviceCatalog.catalog.definition(d.deviceTypeId).functions.size,
            InitialDeviceCatalog.catalog.definition(d.deviceTypeId).events.size) * 28f) * viewport.zoom * density)
    fun edge(d: CreativeDeviceInstance, event: Boolean, portName: String?): Offset {
        val def = InitialDeviceCatalog.catalog.definition(d.deviceTypeId)
        val index = if (event) def.events.indexOfFirst { it.id.value == portName } else
            def.functions.indexOfFirst { it.id.value == portName }
        return location(d) + Offset((if (event) 216f else 8f) * viewport.zoom * density,
            (80f + index.coerceAtLeast(0) * 28f) * viewport.zoom * density)
    }
    val sourceOrigin = location(src)
    val sourceSize = bounds(src)
    val arrival = dest?.let { location(it) }
    val targetSize = dest?.let { bounds(it) }
    // Curves match LogicCanvas's documented cubic Bezier construction.
    val start = if (dest != null) edge(src,true,cue.sourceEventId) else Offset.Zero
    val finish = if (dest != null) edge(dest,false,cue.targetFunctionId) else Offset.Zero
    val bend = max(48f * viewport.zoom * density,abs(finish.x - start.x)/2f)
    Canvas(modifier.fillMaxSize().testTag("sim-signal-vfx")) {
        fun outline(origin: Offset, dimensions: Size, progress: Float) {
            drawRoundRect(glow.copy(alpha = .22f), topLeft = origin, size = dimensions,
                style = Stroke(width = 4.dp.toPx()))
            // One moving highlight lap around the outer bounds of the graph device.
            val width = dimensions.width
            val height = dimensions.height
            val perimeter = 2 * (width + height)
            val distance = ((progress % 1f + 1f) % 1f) * perimeter
            val point = when {
                distance < width -> Offset(origin.x + distance, origin.y)
                distance < width + height -> Offset(origin.x + width, origin.y + distance - width)
                distance < width * 2 + height -> Offset(origin.x + width - (distance - width - height),origin.y + height)
                else -> Offset(origin.x,origin.y + height - (distance - (width * 2 + height)))
            }
            drawCircle(glow.copy(alpha = .18f), radius = 13.dp.toPx(), center = point)
            drawCircle(glow, radius = 4.dp.toPx(), center = point)
        }
        if (dest == null || p < .23f) outline(sourceOrigin,sourceSize,(p / .23f).coerceAtMost(1f))
        if (dest != null) {
            if (p >= .23f && p <= .75f) {
                val t = ((p - .23f) / .52f).coerceIn(0f,1f)
                val u = 1f - t
                val c1 = Offset(start.x + bend,start.y)
                val c2 = Offset(finish.x - bend,finish.y)
                val pos = start * (u*u*u) + c1 * (3f*u*u*t) + c2 * (3f*u*t*t) + finish * (t*t*t)
                drawCircle(glow.copy(alpha = .19f),radius = 15.dp.toPx(),center = pos)
                drawCircle(glow.copy(alpha = .9f),radius = 5.dp.toPx(),center = pos)
            }
            if (p >= .75f && arrival != null && targetSize != null)
                outline(arrival,targetSize,((p - .75f) / .25f).coerceIn(0f,1f))
        }
    }
    val labelDevice = dest ?: src
    val bubbleOrigin = location(labelDevice)
    val bubbleAlpha = if (p < .72f && dest != null) 0f else ((1f - p) / .25f).coerceIn(0f,1f)
    if (bubbleAlpha > .02f) {
        Box(modifier.fillMaxSize(),contentAlignment = Alignment.TopStart) {
            Surface(Modifier.offset { IntOffset(bubbleOrigin.x.roundToInt(), (bubbleOrigin.y - 40 * density).coerceAtLeast(2f).roundToInt()) }
                .graphicsLayer { alpha = bubbleAlpha }.widthIn(max = 210.dp), color = if (cue.unsupported) MaterialTheme.colorScheme.errorContainer
                else MaterialTheme.colorScheme.secondaryContainer, shape = MaterialTheme.shapes.small,
                shadowElevation = 4.dp) {
                Text(cue.label.take(95), Modifier.padding(horizontal=8.dp,vertical=5.dp),
                    fontSize = 11.sp, color = if(cue.unsupported) MaterialTheme.colorScheme.onErrorContainer
                    else MaterialTheme.colorScheme.onSecondaryContainer)
            }
        }
    }
}
