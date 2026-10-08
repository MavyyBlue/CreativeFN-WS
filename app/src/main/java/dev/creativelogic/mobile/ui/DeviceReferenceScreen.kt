package dev.creativelogic.mobile.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.creativelogic.catalog.InitialDeviceCatalog
import dev.creativelogic.model.DeviceOptionValue

@Composable
internal fun DeviceReferenceScreen() {
    val catalog = InitialDeviceCatalog.catalog
    var selectedType by rememberSaveable { mutableStateOf<String?>(null) }
    LazyColumn(Modifier.widthIn(max = 760.dp).fillMaxSize(), contentPadding = PaddingValues(24.dp, 24.dp, 24.dp, 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Device reference", Modifier.weight(1f), style = MaterialTheme.typography.headlineMedium)
                InformationButton("Device reference", "Browse the first three device references. These are prototype settings; current Fortnite options and defaults have not been verified. They cannot be placed or simulated yet.")
            }
        }
        items(catalog.definitions, key = { it.deviceTypeId }) { device ->
            Card(onClick = { selectedType = device.deviceTypeId }) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(device.displayName, style = MaterialTheme.typography.titleMedium)
                        Text(device.category, style = MaterialTheme.typography.bodySmall)
                    }
                    Text("REFERENCE ONLY", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                }
            }
        }
    }
    selectedType?.let { type ->
        val device = catalog.definition(type)
        AlertDialog(onDismissRequest = { selectedType = null },
            title = { Row(verticalAlignment = Alignment.CenterVertically) {
                Text(device.displayName, Modifier.weight(1f))
                InformationButton(device.displayName, device.limitations.joinToString("\n\n"))
            } },
            text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("REFERENCE ONLY", color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.labelSmall)
                Text("Prototype settings", style = MaterialTheme.typography.titleSmall)
                device.options.forEach { Text("${it.displayName}: ${optionLabel(it.defaultValue)}") }
                if (device.events.isNotEmpty()) {
                    HorizontalDivider(); Text("Events", style = MaterialTheme.typography.titleSmall)
                    device.events.forEach { Text(it.displayName) }
                }
                HorizontalDivider(); Text("Functions", style = MaterialTheme.typography.titleSmall)
                device.functions.forEach { Text(it.displayName) }
            } },
            confirmButton = { TextButton(onClick = { selectedType = null }) { Text("Done") } },
        )
    }
}

private fun optionLabel(value: DeviceOptionValue): String = when (value) {
    is DeviceOptionValue.BooleanValue -> if (value.value) "On" else "Off"
    is DeviceOptionValue.IntegerValue -> value.value.toString()
    is DeviceOptionValue.DurationValue -> "${value.milliseconds / 1000.0} s"
    is DeviceOptionValue.EnumValue -> value.value
    is DeviceOptionValue.TextValue -> value.value
}
