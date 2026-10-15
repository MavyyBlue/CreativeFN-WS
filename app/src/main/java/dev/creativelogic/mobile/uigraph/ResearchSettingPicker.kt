package dev.creativelogic.mobile.uigraph

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import kotlinx.serialization.json.*

/** Presentation-only official-documentation options. Never rewrites the saved V2 graph schema. */
internal data class ResearchSettingChoices(val choices: List<String>, val entry: String)

@Composable
internal fun rememberResearchSettingChoices(typeId: String): Map<String, ResearchSettingChoices> {
    val context = LocalContext.current
    val all = remember(context) {
        runCatching {
            val source = context.assets.open("research_setting_choices.json").bufferedReader().use { it.readText() }
            val root = Json.parseToJsonElement(source).jsonObject
            check(root.getValue("schema").jsonPrimitive.int == 1)
            root.getValue("records").jsonObject
        }.getOrDefault(JsonObject(emptyMap()))
    }
    return remember(all, typeId) {
        all[typeId]?.jsonObject?.mapValues { (_, raw) ->
            val row = raw.jsonObject
            ResearchSettingChoices(row["c"]?.jsonArray?.map { it.jsonPrimitive.content }.orEmpty(),
                row["e"]?.jsonPrimitive?.content.orEmpty())
        }.orEmpty()
    }
}

/** Selector-first UX. A custom value is allowed only where the source describes a pick/enter control. */
@Composable
internal fun ReferenceSettingRow(label: String, stored: String, guidance: ResearchSettingChoices?, onChange: (String) -> Unit) {
    var chooser by remember { mutableStateOf(false) }
    var custom by remember(stored) { mutableStateOf(stored) }
    val selectable = guidance != null && (guidance.choices.isNotEmpty() || guidance.entry.isNotEmpty())
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        OutlinedButton(onClick = { chooser = true }, enabled = selectable, modifier = Modifier.fillMaxWidth()
            .testTag("setting-${label.replace(' ', '-')}") ) {
            Text(when {
                stored.isNotBlank() -> stored
                selectable -> "Unspecified · choose setting"
                else -> "Choices not verified in documentation"
            }, modifier = Modifier.fillMaxWidth())
        }
        if (!selectable) Text("Use builder notes until options are documented; no default is assumed.",
            style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    if (chooser && guidance != null) {
        val allowCustom = guidance.entry.isNotBlank()
        val validCustom = when (guidance.entry) {
            "number" -> custom.trim().toDoubleOrNull()?.isFinite() == true
            else -> custom.isNotBlank()
        }
        AlertDialog(onDismissRequest = { chooser = false }, title = { Text(label) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Reference options · Fortnite's exact current menu and default may differ.",
                        style = MaterialTheme.typography.bodySmall)
                    LazyColumn(Modifier.heightIn(max = 280.dp)) {
                        item { TextButton(onClick = { onChange(""); chooser = false }, modifier = Modifier.fillMaxWidth()) {
                            Text("Not specified in blueprint", modifier = Modifier.fillMaxWidth())
                        } }
                        items(guidance.choices) { choice ->
                            TextButton(onClick = { onChange(choice); chooser = false }, modifier = Modifier.fillMaxWidth()) {
                                Text(choice, modifier = Modifier.fillMaxWidth())
                            }
                        }
                    }
                    if (allowCustom) {
                        OutlinedTextField(custom, { custom = it.take(120) }, modifier = Modifier.fillMaxWidth(),
                            singleLine = true, label = { Text(if (guidance.entry == "number") "Custom numeric setting" else "Device text setting") },
                            keyboardOptions = KeyboardOptions(keyboardType = if (guidance.entry == "number") KeyboardType.Decimal else KeyboardType.Text))
                        TextButton(onClick = { onChange(custom.trim()); chooser = false }, enabled = validCustom) {
                            Text("Use custom setting")
                        }
                    }
                }
            }, confirmButton = { TextButton(onClick = { chooser = false }) { Text("Close") } })
    }
}
