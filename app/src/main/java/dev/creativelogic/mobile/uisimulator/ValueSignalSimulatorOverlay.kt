package dev.creativelogic.mobile.uisimulator

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import dev.creativelogic.catalog.InitialDeviceCatalog
import dev.creativelogic.model.MechanicGraph
import dev.creativelogic.simulation.ValueSignalSession

/** Explicit manual event/value tester for research-reference graphs. Never reads Fortnite. */
@Composable
fun ValueSignalSimulatorOverlay(graph: MechanicGraph) {
    val catalog = InitialDeviceCatalog.catalog
    val choices = remember(graph) { graph.devices.flatMap { device ->
        catalog.definition(device.deviceTypeId).events.map { event ->
            Triple(device.id, event.id.value, "${device.name} · ${event.displayName}")
        }
    } }
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
    if (engine == null) {
        Surface(Modifier.padding(12.dp).fillMaxWidth(), shape = MaterialTheme.shapes.large) {
            Text("Value test unavailable: ${attempt.exceptionOrNull()?.message ?: "Check graph configuration"}",Modifier.padding(12.dp))
        }
        return
    }
    fun apply(action: () -> Unit) {
        try { action(); revision++; message = null }
        catch (e: IllegalArgumentException) { message = e.message ?: "Check your input" }
    }
    val trace = remember(revision,engine) { engine.trace }
    val states = remember(revision,engine) { engine.values }
    Box(Modifier.fillMaxSize()) {
        Surface(Modifier.align(Alignment.TopCenter).fillMaxWidth().padding(8.dp),
            shape = MaterialTheme.shapes.medium, tonalElevation = 6.dp) {
            Column(Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                Text("VALUE TEST · manual events, not Fortnite gameplay",style=MaterialTheme.typography.labelSmall,
                    color=MaterialTheme.colorScheme.secondary)
                Row(horizontalArrangement=Arrangement.spacedBy(4.dp), verticalAlignment=Alignment.CenterVertically) {
                    TextButton(onClick={apply { engine.run() }},modifier=Modifier.testTag("sim-run")){Text("Run")}
                    TextButton(onClick={apply { engine.step() }},modifier=Modifier.testTag("sim-step")){Text("Step")}
                    TextButton(onClick={apply { engine.reset() }},modifier=Modifier.testTag("sim-reset")){Text("Reset")}
                    Text("Queue ${engine.pending}", style=MaterialTheme.typography.labelSmall)
                }
                if(engine.halted) Text("Safety limit reached. Inspect bindings and Reset.",
                    color=MaterialTheme.colorScheme.error,style=MaterialTheme.typography.labelSmall)
                message?.let { Text(it,color=MaterialTheme.colorScheme.error,style=MaterialTheme.typography.labelSmall) }
            }
        }
        Column(Modifier.align(Alignment.CenterEnd).padding(end=8.dp,top=100.dp),
            horizontalAlignment=Alignment.End, verticalArrangement=Arrangement.spacedBy(6.dp)) {
            if(showActions) {
                Surface(Modifier.widthIn(max=280.dp).heightIn(max=350.dp).testTag("sim-actions-menu"),
                    shape=MaterialTheme.shapes.large,shadowElevation=6.dp) {
                    Column(Modifier.verticalScroll(rememberScrollState()).padding(10.dp),
                        verticalArrangement=Arrangement.spacedBy(8.dp)) {
                        Text("INJECT DOCUMENTED EVENT",style=MaterialTheme.typography.labelMedium)
                        Text("An injected event skips physical conditions. Only supported function effects change values.",
                            style=MaterialTheme.typography.labelSmall)
                        Box {
                            OutlinedButton(onClick={chooseOpen=true}, enabled=choices.isNotEmpty(),
                                modifier=Modifier.fillMaxWidth().testTag("value-sim-choice")) {
                                Text(choices.getOrNull(choice)?.third ?: "No events in graph", maxLines=2)
                            }
                            DropdownMenu(expanded=chooseOpen,onDismissRequest={chooseOpen=false},
                                modifier=Modifier.heightIn(max=300.dp).widthIn(max=290.dp)) {
                                choices.forEachIndexed { index, item ->
                                    DropdownMenuItem(text={Text(item.third,style=MaterialTheme.typography.bodySmall)},
                                        onClick={choice=index;chooseOpen=false})
                                }
                            }
                        }
                        OutlinedTextField(actor,{actor=it.take(50)},label={Text("Actor (scenario label)")},
                            singleLine=true,modifier=Modifier.fillMaxWidth())
                        OutlinedTextField(payload,{payload=it.take(80)},label={Text("Event value")},
                            singleLine=true,modifier=Modifier.fillMaxWidth())
                        Button(onClick={choices.getOrNull(choice)?.let { (id,event,_) ->
                            apply { engine.inject(id,event,actor,payload) }
                        }}, enabled=choices.isNotEmpty() && actor.isNotBlank(),
                            modifier=Modifier.fillMaxWidth().testTag("value-sim-inject")) { Text("Queue event") }
                        Text("Tap Run or Step to propagate Event → Function bindings.",
                            style=MaterialTheme.typography.labelSmall)
                    }
                }
            }
            FilledTonalButton(onClick={showActions=!showActions}, modifier=Modifier.testTag("sim-actions-toggle")) {
                Text(if(showActions) "Close actions" else "Actions +")
            }
        }
        Surface(Modifier.align(Alignment.BottomStart).fillMaxWidth(.88f).padding(start=8.dp,bottom=8.dp),
            shape=MaterialTheme.shapes.medium,shadowElevation=5.dp) {
            Column(Modifier.padding(10.dp)) {
                TextButton(onClick={showOutput=!showOutput},modifier=Modifier.testTag("sim-output")) {
                    Text(if(showOutput) "Hide values and trace ▲" else "Values / trace (${trace.size}) ▼")
                }
                if(showOutput) {
                    Column(Modifier.heightIn(max=235.dp).verticalScroll(rememberScrollState()),
                        verticalArrangement=Arrangement.spacedBy(4.dp)) {
                        graph.devices.forEach { d ->
                            val state=states[d.id].orEmpty()
                            Text("${d.name}: ${if(state.isEmpty()) "no modeled state" else state.entries.joinToString { "${it.key}=${it.value}" }}",
                                style=MaterialTheme.typography.labelSmall)
                        }
                        HorizontalDivider()
                        trace.takeLast(100).asReversed().forEach { entry ->
                            val device=graph.devices.firstOrNull { it.id == entry.deviceId }?.name ?: entry.deviceId
                            Text("${entry.kind} · $device: ${entry.detail}" +
                                if(entry.value.isNotEmpty()) " [${entry.value}]" else "",
                                style=MaterialTheme.typography.labelSmall,
                                color=if(entry.kind == ValueSignalSession.Kind.UNSUPPORTED || entry.kind == ValueSignalSession.Kind.WARNING)
                                    MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }
    }
}
