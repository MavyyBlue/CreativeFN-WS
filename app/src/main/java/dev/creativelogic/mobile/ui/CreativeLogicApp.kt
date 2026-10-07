package dev.creativelogic.mobile.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.creativelogic.model.MechanicDocumentV1
import dev.creativelogic.model.MechanicMetadataV1
import dev.creativelogic.model.MechanicRepository
import dev.creativelogic.simulation.SimulationAvailability
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun CreativeLogicApp(repository: MechanicRepository) {
    val documents by remember(repository) { repository.observeAll() }
        .collectAsStateWithLifecycle(initialValue = emptyList())
    var screen by rememberSaveable { mutableStateOf("home") }
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    BackHandler(screen != "home") { screen = "home" }
    MaterialTheme(colorScheme = darkColorScheme(
        primary = Color(0xFF9DE8CB), background = Color(0xFF101A22), surface = Color(0xFF1D2A35),
    )) {
        Scaffold { insets ->
            Column(Modifier.fillMaxSize().padding(insets).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("CREATIVE LOGIC", style = MaterialTheme.typography.headlineMedium)
                Text("Mobile building companion", color = MaterialTheme.colorScheme.primary)
                if (screen != "home") TextButton(onClick = { screen = "home" }) { Text("Back to home") }
                when (screen) {
                    "home", "library" -> {
                        if (screen == "home") {
                            Text("Plan Creative device logic offline. Rebuild it manually in Fortnite.")
                            Text("Foundation preview • Graph editor and simulation are under development.")
                            Button(enabled = !saving, onClick = {
                                saving = true
                                error = null
                                scope.launch {
                                    try {
                                        val now = System.currentTimeMillis()
                                        val id = UUID.randomUUID().toString()
                                        repository.save(MechanicDocumentV1(mechanicMetadata = MechanicMetadataV1(
                                            id, "Untitled Mechanic", now, now,
                                        )))
                                        selectedId = id
                                        screen = "editor"
                                    } catch (cancelled: CancellationException) {
                                        throw cancelled
                                    } catch (_: Exception) {
                                        error = "Could not save the mechanic. Try again."
                                    } finally { saving = false }
                                }
                            }) { Text(if (saving) "Creating…" else "New Mechanic") }
                            Row {
                                TextButton(onClick = { screen = "library" }) { Text("Mechanic Library") }
                                TextButton(onClick = { screen = "reference" }) { Text("Device Reference") }
                            }
                        }
                        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                        Text(if (screen == "home") "Recent Mechanics" else "Mechanic Library",
                            style = MaterialTheme.typography.titleLarge)
                        if (documents.isEmpty()) Text("Your locally saved mechanics will appear here.")
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(if (screen == "home") documents.take(5) else documents,
                                key = { it.mechanicMetadata.id }) { document ->
                                Card(onClick = { selectedId = document.mechanicMetadata.id; screen = "editor" },
                                    modifier = Modifier.fillMaxWidth()) {
                                    Column(Modifier.padding(16.dp)) {
                                        Text(document.mechanicMetadata.name)
                                        Text("Empty mechanic • Not tested", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                    "editor" -> {
                        Text(documents.find { it.mechanicMetadata.id == selectedId }
                            ?.mechanicMetadata?.name ?: "Mechanic", style = MaterialTheme.typography.titleLarge)
                        Column(Modifier.fillMaxWidth().weight(1f)
                            .background(MaterialTheme.colorScheme.surface).padding(20.dp)) {
                            Text("Logic canvas", style = MaterialTheme.typography.titleLarge)
                            Text("Device placement and semantic Event → Function binding arrive in Phase 2.")
                        }
                        Text(SimulationAvailability.explanation)
                        Text("This empty mechanic is saved locally. Overlay mode is not available yet.")
                    }
                    "reference" -> {
                        Text("Device Reference", style = MaterialTheme.typography.titleLarge)
                        Text("The versioned catalog starts with Trigger, Tracker and Barrier in Phase 1.")
                        Text("No device behavior is verified or simulated in this build.")
                    }
                }
            }
        }
    }
}
