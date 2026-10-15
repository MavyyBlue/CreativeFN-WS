package dev.creativelogic.mobile.uisimulator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.creativelogic.model.MechanicGraph
import dev.creativelogic.simulation.*

/** A local, graph-attached TEST SCENE. No Fortnite or device interaction. */
@Composable
fun PrototypeSimulatorOverlay(graph: MechanicGraph, modifier: Modifier = Modifier) {
    val attempt = remember(graph) { runCatching { PrototypeSimulationSession(graph) } }
    val engine = attempt.getOrNull()
    var revision by remember(graph) { mutableIntStateOf(0) }
    var actionsOpen by remember(graph) { mutableStateOf(false) }
    var outputOpen by remember(graph) { mutableStateOf(false) }
    var message by remember(graph) { mutableStateOf<String?>(null) }
    var playback by remember(graph) { mutableStateOf<List<SignalVisualCue>>(emptyList()) }
    val trace = remember(engine, revision) { engine?.trace.orEmpty() }
    val states = remember(engine, revision) { engine?.states.orEmpty() }
    fun execute(action: () -> Unit) {
        if (engine == null) return
        try {
            val previous = engine.trace.size
            action()
            playback = SignalVisualPlan.fromPrototype(graph, engine.trace.drop(previous))
            revision++; message = null
        } catch (error: IllegalArgumentException) { message = error.message ?: "Unsupported action" }
    }
    if (engine == null) {
        // A supported blueprint may use Fortnite options that the narrow prototype
        // does not reproduce (teams, HUD modes, game phases). That is NOT an invalid
        // binding. Fall back to manual Event → Function tracing instead of blocking it.
        ValueSignalSimulatorOverlay(graph)
        return
    }
    Box(modifier.fillMaxSize()) {
        SignalTraceVfx(graph, playback)
        Column(Modifier.align(Alignment.TopCenter).fillMaxWidth().padding(8.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Surface(shape = MaterialTheme.shapes.medium, tonalElevation = 5.dp) {
                Column(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp)) {
                    Text("TEST SCENE · Not Fortnite-verified", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TextButton(onClick = { execute { engine.resume() } }, modifier = Modifier.testTag("sim-run")) { Text("Run") }
                        TextButton(onClick = { execute { engine.step() } }, modifier = Modifier.testTag("sim-step")) { Text("Step") }
                        TextButton(onClick = { execute { engine.pause() } }) { Text("Pause") }
                        TextButton(onClick = { execute { engine.reset() } }, modifier = Modifier.testTag("sim-reset")) { Text("Reset") }
                    }
                    Text("t=${engine.clockMillis}ms · ${engine.status} · queue ${engine.pendingEvents}",
                        style = MaterialTheme.typography.labelSmall)
                    if (engine.status == SimulationStatus.POSSIBLE_EVENT_LOOP)
                        Text("Possible event loop. Reset to continue.", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error)
                    message?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error) }
                }
            }
        }
        Column(Modifier.align(Alignment.CenterEnd).padding(end = 8.dp, top = 95.dp), horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(5.dp)) {
            if (actionsOpen) {
                Surface(shape = MaterialTheme.shapes.large, shadowElevation = 6.dp,
                    modifier = Modifier.widthIn(max = 228.dp).heightIn(max = 300.dp).testTag("sim-actions-menu")) {
                    Column(Modifier.verticalScroll(rememberScrollState()).padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("EXTERNAL ACTIONS", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary)
                        ActionEntry("Game Start / Round 1") { execute { engine.startGame() } }
                        ActionEntry("Player Eliminated · P2 by P1") { execute { engine.playerEliminated() } }
                        ActionEntry("Creature Eliminated · Fiend") { execute { engine.creatureEliminated() } }
                        graph.devices.filter { it.deviceTypeId == "trigger" }.forEach { device ->
                            ActionEntry("Trigger · ${device.name}") { execute { engine.trigger(device.id, "player-1") } }
                        }
                        graph.devices.filter { it.deviceTypeId == "button" }.forEach { device ->
                            ActionEntry("Interact · ${device.name}") { execute { engine.interactButton(device.id) } }
                        }
                        Text("Queue an action, then Run or Step. These are manually declared scenarios, not detected gameplay.",
                            style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            FilledTonalButton(onClick = { actionsOpen = !actionsOpen }, modifier = Modifier.testTag("sim-actions-toggle")) {
                Text(if (actionsOpen) "Close actions" else "Actions +")
            }
        }
        Surface(modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth(.87f).padding(start = 8.dp, bottom = 8.dp)
            .testTag("sim-output"), shape = MaterialTheme.shapes.medium, shadowElevation = 5.dp) {
            Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { outputOpen = !outputOpen }, modifier = Modifier.weight(1f)) {
                        Text(if (outputOpen) "Hide output ▲" else "Output (${trace.size}) ▼", maxLines = 1)
                    }
                    Text("${states.size} devices", style = MaterialTheme.typography.labelSmall)
                }
                if (outputOpen) Column(Modifier.heightIn(max = 215.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    graph.devices.forEach { device ->
                        Text("${device.name}: ${describe(states[device.id])}", style = MaterialTheme.typography.labelSmall)
                    }
                    HorizontalDivider()
                    if (trace.isEmpty()) Text("No actions yet. Open Actions, select a scenario, then Run or Step.",
                        style = MaterialTheme.typography.bodySmall)
                    trace.takeLast(100).asReversed().forEach { entry ->
                        Text("[${entry.timeMillis}ms] ${entry.kind} · ${graph.devices.find { it.id == entry.deviceId }?.name ?: entry.deviceId}: ${entry.detail}",
                            style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionEntry(label: String, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = 4.dp)) {
        Text(label, modifier = Modifier.fillMaxWidth(), maxLines = 2, overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelMedium)
    }
}

private fun describe(state: DeviceRuntimeState?): String = when (state) {
    is TriggerRuntime -> "${if (state.enabled) "ready" else "disabled"} · used ${state.triggerCount}"
    is TrackerRuntime -> "${state.progress} progress · ${if (state.completed) "complete" else if (state.assigned) "assigned" else "unassigned"}"
    is BarrierRuntime -> if (state.enabled) "enabled" else "disabled"
    is HudMessageRuntime -> if (state.visible) "message visible" else "message hidden"
    is ButtonRuntime -> "${if (state.enabled) "enabled" else "disabled"} · ${state.interactions} presses"
    is RoundSettingsRuntime -> if (state.enabled) "enabled" else "disabled"
    is CreatureManagerRuntime -> if (state.enabled) "enabled" else "disabled"
    is EliminationManagerRuntime -> if (state.enabled) "enabled" else "disabled"
    else -> "unavailable"
}
