package dev.creativelogic.mobile.uisimulator

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.creativelogic.catalog.InitialDeviceCatalog
import dev.creativelogic.model.MechanicGraph
import dev.creativelogic.simulation.ManualScenarioSignal
import dev.creativelogic.simulation.ValueScenarioReplay
import dev.creativelogic.simulation.ValueSignalReport
import dev.creativelogic.simulation.ValueSignalSession

/** Reference-only event/value testing. Physical Fortnite conditions are never inferred. */
@Composable
fun ValueSignalSimulatorOverlay(graph: MechanicGraph) {
    val catalog = InitialDeviceCatalog.catalog
    val choices = remember(graph) {
        graph.devices.flatMap { device ->
            catalog.definition(device.deviceTypeId).events.map { event ->
                Triple(device.id, event.id.value, "${device.name} · ${event.displayName}")
            }
        }
    }
    val attempt = remember(graph) { runCatching { ValueSignalSession(graph) } }
    val engine = attempt.getOrNull()
    var revision by remember(graph) { mutableIntStateOf(0) }
    var showActions by rememberSaveable(graph) { mutableStateOf(false) }
    var showOutput by rememberSaveable(graph) { mutableStateOf(false) }
    var chooseOpen by remember { mutableStateOf(false) }
    var choice by rememberSaveable(graph) { mutableIntStateOf(0) }
    var actor by rememberSaveable(graph) { mutableStateOf("player-1") }
    var payload by rememberSaveable(graph) { mutableStateOf("true") }
    var message by remember(graph) { mutableStateOf<String?>(null) }
    val history = remember(graph) { mutableStateListOf<ManualScenarioSignal>() }
    val context = LocalContext.current

    if (engine == null) {
        Surface(Modifier.padding(12.dp).fillMaxWidth(), shape = MaterialTheme.shapes.large) {
            Text("Value test unavailable: ${attempt.exceptionOrNull()?.message ?: "Check graph configuration"}", Modifier.padding(12.dp))
        }
        return
    }
    fun apply(action: () -> Unit) {
        try { action(); revision++; message = null }
        catch (e: IllegalArgumentException) { message = e.message ?: "Check your input" }
    }
    fun queueEvent() {
        val selected = choices.getOrNull(choice) ?: return
        if (history.size >= ValueScenarioReplay.MAX_SIGNALS) {
            message = "Scenario limit (${ValueScenarioReplay.MAX_SIGNALS} inputs). Clear the list to start another."
            return
        }
        val input = ManualScenarioSignal(selected.first, selected.second, actor, payload)
        apply { engine.inject(input.deviceId, input.eventId, input.actor, input.value); history.add(input) }
    }
    val trace = remember(revision, engine) { engine.trace }
    val states = remember(revision, engine) { engine.values }
    val summary = remember(revision, engine) { ValueSignalReport.summarize(engine) }

    Box(Modifier.fillMaxSize()) {
        Surface(Modifier.align(Alignment.TopCenter).fillMaxWidth().padding(8.dp),
            shape = MaterialTheme.shapes.medium, tonalElevation = 6.dp) {
            Column(Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                Text("VALUE TEST · manual events, not Fortnite gameplay",
                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { apply { engine.run() } }, modifier = Modifier.testTag("sim-run")) { Text("Run") }
                    TextButton(onClick = { apply { engine.step() } }, modifier = Modifier.testTag("sim-step")) { Text("Step") }
                    TextButton(onClick = { apply { engine.reset() } }, modifier = Modifier.testTag("sim-reset")) { Text("Reset") }
                    Text("Queue ${engine.pending}", style = MaterialTheme.typography.labelSmall)
                }
                if (engine.halted) Text("Safety limit reached. Inspect bindings and Reset.",
                    color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                message?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall) }
            }
        }
        Column(Modifier.align(Alignment.CenterEnd).padding(end = 8.dp, top = 100.dp),
            horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (showActions) {
                Surface(Modifier.widthIn(max = 285.dp).heightIn(max = 360.dp).testTag("sim-actions-menu"),
                    shape = MaterialTheme.shapes.large, shadowElevation = 6.dp) {
                    Column(Modifier.verticalScroll(rememberScrollState()).padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("INJECT DOCUMENTED EVENT", style = MaterialTheme.typography.labelMedium)
                        Text("Events are manually declared; game conditions are not tested.",
                            style = MaterialTheme.typography.labelSmall)
                        OutlinedButton(onClick = { chooseOpen = true }, enabled = choices.isNotEmpty(),
                            modifier = Modifier.fillMaxWidth().testTag("value-sim-choice")) {
                            Text(choices.getOrNull(choice)?.third ?: "No events in graph", maxLines = 2,
                                overflow = TextOverflow.Ellipsis)
                        }
                        Button(onClick = { queueEvent() }, enabled = choices.isNotEmpty() && actor.isNotBlank(),
                            modifier = Modifier.fillMaxWidth().testTag("value-sim-inject")) { Text("Queue event") }
                        OutlinedTextField(actor, { actor = it.take(50) }, label = { Text("Actor (scenario label)") },
                            singleLine = true, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(payload, { payload = it.take(80) }, label = { Text("Event value") },
                            singleLine = true, modifier = Modifier.fillMaxWidth())
                        HorizontalDivider()
                        Text("SCENARIO INPUTS (${history.size})", style = MaterialTheme.typography.labelSmall)
                        Text("Replay runs each saved input in order from a clean test state; it does not save into your blueprint.",
                            style = MaterialTheme.typography.labelSmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            TextButton(onClick = { apply { ValueScenarioReplay.replay(engine, history.toList()) }; showOutput = true },
                                enabled = history.isNotEmpty(), modifier = Modifier.testTag("value-sim-replay")) {
                                Text("Replay")
                            }
                            TextButton(onClick = { history.clear(); message = null }, enabled = history.isNotEmpty(),
                                modifier = Modifier.testTag("value-sim-clear-history")) { Text("Clear inputs") }
                        }
                        history.takeLast(12).forEachIndexed { index, input ->
                            val name = graph.devices.firstOrNull { it.id == input.deviceId }?.name ?: "Unknown"
                            Text("${history.size - minOf(12, history.size) + index + 1}. $name · ${input.eventId} = ${input.value}",
                                style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
            FilledTonalButton(onClick = { showActions = !showActions }, modifier = Modifier.testTag("sim-actions-toggle")) {
                Text(if (showActions) "Close actions" else "Actions +")
            }
        }
        Surface(Modifier.align(Alignment.BottomStart).fillMaxWidth(.88f).padding(start = 8.dp, bottom = 8.dp),
            shape = MaterialTheme.shapes.medium, shadowElevation = 5.dp) {
            Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = { showOutput = !showOutput }, modifier = Modifier.testTag("sim-output")) {
                    Text(if (showOutput) "Hide values and trace ▲" else "Values / trace (${trace.size}) ▼")
                }
                if (showOutput) {
                    Column(Modifier.heightIn(max = 238.dp).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text("${summary.injectedInputs} injected · ${summary.receivedFunctions} functions · " +
                            "${summary.changedValues} values · ${summary.unmodeledEffects} unmodeled",
                            style = MaterialTheme.typography.labelMedium, modifier = Modifier.testTag("value-sim-summary"))
                        Text(ValueSignalReport.explain(summary), style = MaterialTheme.typography.bodySmall)
                        OutlinedButton(onClick = {
                            val text = ValueSignalReport.export(graph, engine)
                            val manager = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                            manager?.setPrimaryClip(ClipData.newPlainText("CreativeFN WS value test", text))
                            message = if (manager != null) "Report copied" else "Clipboard unavailable"
                        }, modifier = Modifier.testTag("value-sim-copy-report")) { Text("Copy report") }
                        graph.devices.forEach { d ->
                            val state = states[d.id].orEmpty()
                            Text("${d.name}: ${if (state.isEmpty()) "no modeled state" else state.entries.joinToString { "${it.key}=${it.value}" }}",
                                style = MaterialTheme.typography.labelSmall)
                        }
                        HorizontalDivider()
                        trace.takeLast(100).asReversed().forEach { entry ->
                            val device = graph.devices.firstOrNull { it.id == entry.deviceId }?.name ?: entry.deviceId
                            Text("${entry.kind} · $device: ${entry.detail}" +
                                if (entry.value.isNotEmpty()) " [${entry.value}]" else "",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (entry.kind == ValueSignalSession.Kind.UNSUPPORTED || entry.kind == ValueSignalSession.Kind.WARNING)
                                    MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }
    }
    if (chooseOpen) {
        var query by remember { mutableStateOf("") }
        val matches = remember(query, choices) {
            choices.withIndex().filter { it.value.third.contains(query.trim(), ignoreCase = true) }
        }
        AlertDialog(onDismissRequest = { chooseOpen = false },
            title = { Text("Choose documented event") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = query, onValueChange = { query = it },
                        label = { Text("Search device or event") }, singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("value-sim-event-search"))
                    LazyColumn(Modifier.heightIn(max = 300.dp).testTag("value-sim-event-results")) {
                        itemsIndexed(matches, key = { _, item -> item.index }) { _, item ->
                            TextButton(onClick = { choice = item.index; chooseOpen = false },
                                modifier = Modifier.fillMaxWidth()) {
                                Text(item.value.third, modifier = Modifier.fillMaxWidth(),
                                    style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    if (matches.isEmpty()) Text("No matching documented events.", style = MaterialTheme.typography.bodySmall)
                }
            }, confirmButton = {
                TextButton(onClick = { chooseOpen = false }) { Text("Close") }
            })
    }
}
