package dev.creativelogic.mobile.uigraph

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import android.content.Intent
import androidx.core.net.toUri
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
            } else items(InitialDeviceCatalog.catalog.definitions.filter { definition ->
                definition.displayName.contains(query, true) || definition.category.contains(query, true) ||
                    definition.events.any { it.displayName.contains(query, true) } ||
                    definition.functions.any { it.displayName.contains(query, true) }
            }, key = { it.deviceTypeId }) { d ->
                OutlinedCard(onClick = { onAdd(d.deviceTypeId) }, modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(d.displayName, style = MaterialTheme.typography.titleSmall)
                            Text("${d.category} · ${d.options.size} settings · ${d.events.size} events · ${d.functions.size} functions",
                                style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("DOCS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
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
    val context = LocalContext.current
    val legacy = device.deviceTypeId in setOf("trigger", "tracker", "barrier", "hud-message",
        "button", "round-settings", "creature-manager", "elimination-manager")
    val research = rememberResearchSettingChoices(device.deviceTypeId)
    var name by rememberSaveable(device.id) { mutableStateOf(device.name) }
    var notes by rememberSaveable(device.id) { mutableStateOf(device.builderNotes.orEmpty()) }
    var fields by rememberSaveable(device.id) { mutableStateOf(device.configuredOptions.mapValues { input(it.value) }) }
    var tab by rememberSaveable(device.id) { mutableStateOf("Settings") }
    var query by rememberSaveable(device.id) { mutableStateOf("") }
    val values = def.options.associate { it.id to optionValue(it, fields.getValue(it.id)) }
    val valid = name.isNotBlank() && values.values.all { it != null }
    fun save() { if (valid) onSave(device.copy(name = name.trim(),
        builderNotes = notes.takeIf { it.isNotBlank() }, configuredOptions = values.mapValues { it.value!! })) }
    val visibleSettings = def.options.filter { option ->
        option.displayName.contains(query, ignoreCase = true) || fields[option.id]?.contains(query, ignoreCase = true) == true
    }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.fillMaxWidth().fillMaxHeight(.87f).imePadding().padding(horizontal = 18.dp).padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(def.displayName, Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
                InformationButton(def.displayName, def.limitations.joinToString("\n\n"))
                TextButton(onClick = { save(); onDismiss() }, enabled = valid) { Text("Done") }
            }
            Text(if (legacy) "ORIGINAL CREATIVE REFERENCE · PARTIAL OPTIONS"
                 else "CREATIVE 1.0 RESEARCH · MENU / DEFAULTS NOT YET VERIFIED",
                style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
            OutlinedTextField(name, { name = it.take(80) }, Modifier.fillMaxWidth(),
                label = { Text("Device name") }, isError = name.isBlank(), singleLine = true)
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Settings", "Events", "Functions").forEach { option ->
                    FilterChip(selected = tab == option, onClick = { tab = option }, label = {
                        Text(when (option) {
                            "Settings" -> "Settings (${def.options.size})"
                            "Events" -> "Events (${def.events.size})"
                            else -> "Functions (${def.functions.size})"
                        }, style = MaterialTheme.typography.labelSmall)
                    })
                }
            }
            if (tab == "Settings") {
                OutlinedTextField(query, { query = it.take(80) }, Modifier.fillMaxWidth().testTag("config-setting-search"),
                    label = { Text("Find a setting") }, singleLine = true)
            }
            // Large devices have 50–120 settings. Lazy composition avoids rendering hundreds of
            // editing controls/dialogs simultaneously on a constrained Android phone.
            LazyColumn(Modifier.fillMaxWidth().weight(1f).testTag("device-config-content"),
                contentPadding = PaddingValues(bottom = 10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                when (tab) {
                    "Settings" -> {
                        if (def.options.isEmpty()) item {
                            Text("No configurable settings found in the current source. Check the in-game menu.")
                        }
                        if (visibleSettings.isEmpty() && def.options.isNotEmpty()) item { Text("No matching setting") }
                        items(visibleSettings, key = { it.id }) { option ->
                            val stored = fields.getValue(option.id)
                            val change: (String) -> Unit = { fields = fields + (option.id to it) }
                            if (!legacy && option.kind == DeviceOptionKind.TEXT) {
                                ReferenceSettingRow(option.displayName, stored, research[option.id], change)
                            } else when (option.kind) {
                                DeviceOptionKind.BOOLEAN -> Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Text(option.displayName, Modifier.weight(1f)); Switch(stored == "true", { change(it.toString()) })
                                }
                                DeviceOptionKind.ENUM -> Column {
                                    Text(option.displayName, style = MaterialTheme.typography.labelMedium)
                                    ChoiceButton(option.displayName, stored, option.choices.map { it to it }, change)
                                }
                                else -> OutlinedTextField(stored, change, Modifier.fillMaxWidth(),
                                    label = { Text(option.displayName + if (option.kind == DeviceOptionKind.DURATION) " (seconds)" else "") },
                                    isError = values[option.id] == null, singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = when(option.kind) {
                                        DeviceOptionKind.INTEGER -> KeyboardType.Number
                                        DeviceOptionKind.DURATION -> KeyboardType.Decimal
                                        else -> KeyboardType.Text
                                    }))
                            }
                        }
                        item {
                            OutlinedTextField(notes, { notes = it }, Modifier.fillMaxWidth(),
                                label = { Text("Builder notes / in-game differences") }, minLines = 2)
                        }
                    }
                    "Events" -> {
                        if (def.events.isEmpty()) item { Text("No event ports documented. This does not prove the device has none in Fortnite.") }
                        items(def.events, key = { it.id.value }) { event ->
                            OutlinedButton(onClick = { save(); onEvent(event.id) }, enabled = valid,
                                modifier = Modifier.fillMaxWidth()) { Text(event.displayName) }
                        }
                    }
                    else -> {
                        if (def.functions.isEmpty()) item { Text("No callable function ports documented. Verify in-game before relying on this.") }
                        items(def.functions, key = { it.id.value }) { function ->
                            OutlinedButton(onClick = { save(); onFunction(function.id) }, enabled = valid,
                                modifier = Modifier.fillMaxWidth()) { Text(function.displayName) }
                        }
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TextButton(onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, def.referenceUrl.toUri())) },
                    modifier = Modifier.weight(1f)) { Text("Epic device reference", style = MaterialTheme.typography.labelSmall) }
                TextButton(onClick = { save(); onDuplicate() }, enabled = valid) { Text("Duplicate") }
                TextButton(onClick = onDelete) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            }
        }
    }
}
