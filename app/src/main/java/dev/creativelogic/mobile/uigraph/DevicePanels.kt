package dev.creativelogic.mobile.uigraph

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.creativelogic.catalog.InitialDeviceCatalog
import dev.creativelogic.model.*
import dev.creativelogic.mobile.ui.InformationButton
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DevicePalette(graph: MechanicGraph, onDismiss: () -> Unit, onAdd: (String) -> Unit, onConfigure: (String) -> Unit) {
    var placed by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Row(Modifier.padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Devices", Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
            TextButton(onClick = onDismiss) { Text("Close") }
        }
        Row(Modifier.padding(horizontal = 20.dp)) {
            FilterChip(selected = !placed, onClick = { placed = false }, label = { Text("Add") })
            Spacer(Modifier.width(12.dp))
            FilterChip(selected = placed, onClick = { placed = true }, label = { Text("Placed") })
        }
        OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth().padding(20.dp), label = { Text("Search devices") }, singleLine = true)
        LazyColumn(Modifier.fillMaxWidth().heightIn(max = 400.dp).testTag("device-palette"), contentPadding = PaddingValues(20.dp)) {
            if (placed) items(graph.devices.filter { it.name.contains(query, true) }, key = { it.id }) { d ->
                TextButton(onClick = { onConfigure(d.id) }, modifier = Modifier.fillMaxWidth()) { Text(d.name) }
            } else items(InitialDeviceCatalog.catalog.definitions.filter { it.displayName.contains(query, true) }, key = { it.deviceTypeId }) { d ->
                OutlinedCard(onClick = { onAdd(d.deviceTypeId) }, modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(d.displayName, Modifier.weight(1f))
                        Text("REFERENCE ONLY", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }
    }
}

@Composable
internal fun ChoiceButton(label: String, value: String?, choices: List<Pair<String, String>>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }, enabled = choices.isNotEmpty(), modifier = Modifier.fillMaxWidth().testTag("choice-$label")) { Text(value ?: label) }
        DropdownMenu(expanded, { expanded = false }, modifier = Modifier.heightIn(max = 280.dp).testTag("choice-menu-$label")) {
            choices.forEach { (id, name) -> DropdownMenuItem(text = { Text(name) }, onClick = { expanded = false; onSelect(id) }) }
        }
    }
}

private fun input(value: DeviceOptionValue): String = when (value) {
    is DeviceOptionValue.BooleanValue -> value.value.toString()
    is DeviceOptionValue.IntegerValue -> value.value.toString()
    is DeviceOptionValue.DurationValue -> BigDecimal.valueOf(value.milliseconds, 3).stripTrailingZeros().toPlainString()
    is DeviceOptionValue.EnumValue -> value.value
    is DeviceOptionValue.TextValue -> value.value
}
private fun optionValue(option: DeviceOptionDefinition, text: String): DeviceOptionValue? = try {
    when (option.kind) {
        DeviceOptionKind.BOOLEAN -> text.toBooleanStrictOrNull()?.let { DeviceOptionValue.BooleanValue(it) }
        DeviceOptionKind.INTEGER -> text.toIntOrNull()?.let { DeviceOptionValue.IntegerValue(it) }
        DeviceOptionKind.DURATION -> text.toBigDecimalOrNull()?.multiply(BigDecimal(1000))?.longValueExact()?.let { DeviceOptionValue.DurationValue(it) }
        DeviceOptionKind.ENUM -> DeviceOptionValue.EnumValue(text)
        DeviceOptionKind.TEXT -> DeviceOptionValue.TextValue(text)
    }?.takeIf(option::accepts)
} catch (_: ArithmeticException) { null }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DeviceConfiguration(device: CreativeDeviceInstance, onDismiss: () -> Unit, onSave: (CreativeDeviceInstance) -> Unit,
    onDuplicate: () -> Unit, onDelete: () -> Unit, onEvent: (DeviceEventId) -> Unit, onFunction: (DeviceFunctionId) -> Unit) {
    val def = InitialDeviceCatalog.catalog.definition(device.deviceTypeId)
    var name by rememberSaveable(device.id) { mutableStateOf(device.name) }
    var notes by rememberSaveable(device.id) { mutableStateOf(device.builderNotes.orEmpty()) }
    var fields by rememberSaveable(device.id) { mutableStateOf(device.configuredOptions.mapValues { input(it.value) }) }
    val values = def.options.associate { it.id to optionValue(it, fields.getValue(it.id)) }
    val valid = name.isNotBlank() && values.values.all { it != null }
    fun save() { if (valid) onSave(device.copy(name = name.trim(), builderNotes = notes.takeIf { it.isNotBlank() }, configuredOptions = values.mapValues { it.value!! })) }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).imePadding().padding(horizontal = 20.dp).padding(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(def.displayName, Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
                InformationButton(def.displayName, def.limitations.joinToString("\n\n"))
                TextButton(onClick = { save(); onDismiss() }, enabled = valid) { Text("Done") }
            }
            Text("REFERENCE ONLY", color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.labelSmall)
            OutlinedTextField(name, { name = it.take(80) }, Modifier.fillMaxWidth(), label = { Text("Device name") }, isError = name.isBlank(), singleLine = true)
            def.options.forEach { option ->
                val text = fields.getValue(option.id)
                val change: (String) -> Unit = { fields = fields + (option.id to it) }
                when (option.kind) {
                    DeviceOptionKind.BOOLEAN -> Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(option.displayName, Modifier.weight(1f)); Switch(text == "true", { change(it.toString()) })
                    }
                    DeviceOptionKind.ENUM -> Column {
                        Text(option.displayName, style = MaterialTheme.typography.labelMedium)
                        ChoiceButton(option.displayName, text, option.choices.map { it to it }, change)
                    }
                    else -> OutlinedTextField(text, change, Modifier.fillMaxWidth(),
                        label = { Text(option.displayName + if (option.kind == DeviceOptionKind.DURATION) " (seconds)" else "") },
                        isError = values[option.id] == null, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = when (option.kind) {
                            DeviceOptionKind.INTEGER -> KeyboardType.Number
                            DeviceOptionKind.DURATION -> KeyboardType.Decimal
                            else -> KeyboardType.Text
                        }))
                }
            }
            OutlinedTextField(notes, { notes = it }, Modifier.fillMaxWidth(), label = { Text("Builder notes") }, minLines = 2)
            if (def.events.isNotEmpty()) {
                Text("Events", style = MaterialTheme.typography.titleSmall)
                def.events.forEach { e -> TextButton(onClick = { save(); onEvent(e.id) }, enabled = valid) { Text(e.displayName) } }
            }
            Text("Functions", style = MaterialTheme.typography.titleSmall)
            def.functions.forEach { f -> TextButton(onClick = { save(); onFunction(f.id) }, enabled = valid) { Text(f.displayName) } }
            HorizontalDivider()
            Row {
                TextButton(onClick = { save(); onDuplicate() }, enabled = valid) { Text("Duplicate") }
                TextButton(onClick = onDelete) { Text("Delete device", color = MaterialTheme.colorScheme.error) }
            }
        }
    }
}
