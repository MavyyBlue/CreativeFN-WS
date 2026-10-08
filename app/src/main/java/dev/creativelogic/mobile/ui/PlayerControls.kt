package dev.creativelogic.mobile.ui

import android.content.Context
import androidx.core.content.edit
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import dev.creativelogic.model.MechanicDocumentV1

class DisplayPreferences(context: Context) {
    private val storage = context.applicationContext.getSharedPreferences("display-options", Context.MODE_PRIVATE)
    var showHeader: Boolean
        get() = storage.getBoolean("header", true)
        set(value) { storage.edit { putBoolean("header", value) } }
    var showNavigation: Boolean
        get() = storage.getBoolean("navigation", true)
        set(value) { storage.edit { putBoolean("navigation", value) } }
}

@Composable
internal fun InformationButton(title: String, body: String) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }, modifier = Modifier.semantics { contentDescription = "Information: $title" }) {
            BuilderGlyph(Glyph.Learn, MaterialTheme.colorScheme.secondary)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false },
            modifier = Modifier.widthIn(max = 280.dp).heightIn(max = 320.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(body, style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = { expanded = false }) { Text("Close") }
            }
        }
    }
}

@Composable
internal fun DisplayToggle(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().toggleable(checked, role = Role.Switch, onValueChange = onChange).padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = null)
    }
}

@Composable
internal fun RecentDrawer(documents: List<MechanicDocumentV1>, onOpen: (MechanicDocumentV1) -> Unit, onClose: () -> Unit) {
    val keyboard = LocalSoftwareKeyboardController.current
    var query by rememberSaveable { mutableStateOf("") }
    val recent = remember(documents, query) {
        documents.filter { it.mechanicMetadata.name.contains(query.trim(), ignoreCase = true) }
            .sortedWith(compareByDescending<MechanicDocumentV1> { it.mechanicMetadata.modifiedAtEpochMillis }.thenBy { it.mechanicMetadata.id })
    }
    ModalDrawerSheet(Modifier.widthIn(max = 320.dp)) {
        Row(Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Recent mechanics", Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
            IconButton(onClick = onClose, modifier = Modifier.semantics { contentDescription = "Close recent mechanics" }) {
                BuilderGlyph(Glyph.Back, MaterialTheme.colorScheme.onSurface)
            }
        }
        OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth().padding(16.dp),
            label = { Text("Find recent mechanic") }, singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }))
        LazyColumn(Modifier.weight(1f).testTag("recent-mechanics"), contentPadding = PaddingValues(bottom = 24.dp)) {
            if (recent.isEmpty()) item { Text("No matching mechanics", Modifier.padding(16.dp)) }
            items(recent, key = { it.mechanicMetadata.id }) { document ->
                NavigationDrawerItem(label = { Text(document.mechanicMetadata.name, maxLines = 2, overflow = TextOverflow.Ellipsis) },
                    selected = false, onClick = { onOpen(document) }, modifier = Modifier.padding(horizontal = 12.dp))
            }
        }
    }
}
