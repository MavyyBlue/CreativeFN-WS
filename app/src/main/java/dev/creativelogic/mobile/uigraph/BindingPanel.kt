package dev.creativelogic.mobile.uigraph

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.creativelogic.catalog.InitialDeviceCatalog
import dev.creativelogic.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BindingPanel(graph: MechanicGraph, onDismiss: () -> Unit, onConnect: (String, DeviceEventId, String, DeviceFunctionId) -> Unit, onDelete: (String) -> Unit) {
    var source by rememberSaveable { mutableStateOf<String?>(null) }
    var event by rememberSaveable { mutableStateOf<String?>(null) }
    var target by rememberSaveable { mutableStateOf<String?>(null) }
    var function by rememberSaveable { mutableStateOf<String?>(null) }
    val catalog = InitialDeviceCatalog.catalog
    val sourceDevice = graph.devices.find { it.id == source }
    val targetDevice = graph.devices.find { it.id == target }
    val sourceDef = sourceDevice?.let { catalog.definition(it.deviceTypeId) }
    val targetDef = targetDevice?.let { catalog.definition(it.deviceTypeId) }
    fun label(d: CreativeDeviceInstance) = if (graph.devices.count { it.name == d.name } > 1) "${d.name} · ${d.id.take(4)}" else d.name
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Bindings", style = MaterialTheme.typography.titleLarge)
            ChoiceButton("Source device", sourceDevice?.let(::label), graph.devices.filter { catalog.definition(it.deviceTypeId).events.isNotEmpty() }.map { it.id to label(it) }) { source = it; event = null }
            ChoiceButton("Source event", sourceDef?.events?.find { it.id.value == event }?.displayName, sourceDef?.events.orEmpty().map { it.id.value to it.displayName }) { event = it }
            ChoiceButton("Target device", targetDevice?.let(::label), graph.devices.map { it.id to label(it) }) { target = it; function = null }
            ChoiceButton("Target function", targetDef?.functions?.find { it.id.value == function }?.displayName, targetDef?.functions.orEmpty().map { it.id.value to it.displayName }) { function = it }
            Button(onClick = {
                onConnect(source!!, DeviceEventId(event!!), target!!, DeviceFunctionId(function!!))
                source = null; event = null; target = null; function = null
            }, enabled = sourceDevice != null && targetDevice != null && event != null && function != null,
                modifier = Modifier.fillMaxWidth()) { Text("Add binding") }
            HorizontalDivider()
            graph.connections.forEach { c ->
                val a = graph.devices.single { it.id == c.sourceDeviceId }; val b = graph.devices.single { it.id == c.targetDeviceId }
                val e = catalog.definition(a.deviceTypeId).events.single { it.id == c.sourceEventId }
                val f = catalog.definition(b.deviceTypeId).functions.single { it.id == c.targetFunctionId }
                Row {
                    Text("${label(a)} · ${e.displayName}\n→ ${label(b)} · ${f.displayName}", Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                    TextButton(onClick = { onDelete(c.id) }) { Text("Remove") }
                }
            }
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Close") }
        }
    }
}
