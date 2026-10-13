package dev.creativelogic.mobile.blueprint

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import androidx.core.content.edit
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.creativelogic.catalog.InitialDeviceCatalog
import dev.creativelogic.graph.BlueprintGenerator
import dev.creativelogic.graph.MechanicGraphCodec
import dev.creativelogic.mobile.overlay.BlueprintOverlayService
import dev.creativelogic.model.MechanicRepository
import dev.creativelogic.model.SavedMechanic
import kotlinx.coroutines.launch
import java.util.UUID

/** Always-reachable builder entrypoint; does not change the existing graph editor navigation. */
@Composable
fun BlueprintAccess(repository: MechanicRepository, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val codec = remember { MechanicGraphCodec(InitialDeviceCatalog.catalog) }
    val documents by remember(repository) { repository.observeAll() }
        .collectAsStateWithLifecycle(initialValue = emptyList())
    var chooser by rememberSaveable { mutableStateOf(false) }
    var guideId by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingOverlayId by rememberSaveable { mutableStateOf<String?>(null) }
    var exportId by rememberSaveable { mutableStateOf<String?>(null) }
    var message by rememberSaveable { mutableStateOf<String?>(null) }
    var permissionExplanation by rememberSaveable { mutableStateOf(false) }
    fun launch(document: SavedMechanic) {
        if (!Settings.canDrawOverlays(context)) {
            pendingOverlayId = document.mechanicMetadata.id
            return
        }
        try {
            BlueprintOverlayService.launch(context, document, codec)
            chooser = false
            message = "Overlay requested. Switch to Fortnite; use the FN bubble, or Stop in its notification."
        } catch (ex: Exception) {
            message = "Unable to start overlay: ${ex.localizedMessage ?: "Android denied the request"}"
        }
    }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (!Settings.canDrawOverlays(context)) {
            pendingOverlayId = null
            message = "Overlay permission not granted. You can still use the full-screen blueprint."
        }
        else {
            documents.firstOrNull { it.mechanicMetadata.id == pendingOverlayId }?.let {
                pendingOverlayId = null
                launch(it)
            }
            // If Room has not emitted yet, the effect below waits for the document.
        }
    }
    LaunchedEffect(documents, pendingOverlayId) {
        val id = pendingOverlayId
        if (id != null && Settings.canDrawOverlays(context)) {
            documents.firstOrNull { it.mechanicMetadata.id == id }?.let {
                pendingOverlayId = null
                launch(it)
            }
        }
    }
    val importer = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch {
            try {
                val source = context.contentResolver.openInputStream(uri)?.use { stream ->
                    // Enforce a conservative memory limit for documents from untrusted providers.
                    val out = java.io.ByteArrayOutputStream()
                    val buffer = ByteArray(8192)
                    while (true) {
                        val count = stream.read(buffer)
                        if (count < 0) break
                        require(out.size() + count <= 2_000_000) { "Import exceeds 2 MB" }
                        out.write(buffer, 0, count)
                    }
                    out.toByteArray().toString(Charsets.UTF_8)
                } ?: error("File unavailable")
                val imported = codec.decode(source)
                val now = System.currentTimeMillis()
                repository.save(imported.copy(mechanicMetadata = imported.mechanicMetadata.copy(
                    id = UUID.randomUUID().toString(), name = imported.mechanicMetadata.name.take(72) + " (import)",
                    createdAtEpochMillis = now, modifiedAtEpochMillis = now,
                )))
                message = "Imported as a new mechanic."
            } catch (ex: Exception) { message = "Import failed: ${ex.localizedMessage ?: "incompatible .c1logic"}" }
        }
    }
    val exporter = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
        val document = documents.firstOrNull { it.mechanicMetadata.id == exportId }
        exportId = null
        if (uri != null && document != null) try {
            context.contentResolver.openOutputStream(uri)?.use { it.write(codec.encode(document).toByteArray(Charsets.UTF_8)) }
                ?: error("File unavailable")
            message = "Blueprint mechanic exported."
        } catch (ex: Exception) { message = "Export failed: ${ex.localizedMessage ?: "storage unavailable"}" }
    }
    Button(onClick = { chooser = true }, modifier = modifier) { Text("Blueprints") }
    if (chooser) AlertDialog(onDismissRequest = { chooser = false },
        title = { Text("Blueprint library") },
        text = {
            Column(Modifier.fillMaxWidth().heightIn(max = 450.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Save your editor changes first. Each guide uses one saved mechanic and its documented device ports.")
                OutlinedButton(onClick = { importer.launch(arrayOf("*/*")) }) { Text("Import .c1logic") }
                if (documents.isEmpty()) Text("No mechanics saved. Create one in the editor first.")
                documents.forEach { document ->
                    Column {
                        Text(document.mechanicMetadata.name, style = MaterialTheme.typography.titleSmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            TextButton(onClick = { guideId = document.mechanicMetadata.id; chooser = false }) { Text("Guide") }
                            TextButton(onClick = {
                                if (!Settings.canDrawOverlays(context)) {
                                    pendingOverlayId = document.mechanicMetadata.id
                                    chooser = false
                                    permissionExplanation = true
                                } else launch(document)
                            }) { Text("Overlay") }
                            TextButton(onClick = {
                                exportId = document.mechanicMetadata.id
                                exporter.launch("${document.mechanicMetadata.name.replace(Regex("[^a-zA-Z0-9_-]"), "_").take(45)}.c1logic")
                            }) { Text("Export") }
                        }
                        HorizontalDivider()
                    }
                }
            }
        }, confirmButton = { TextButton(onClick = { chooser = false }) { Text("Close") } })
    documents.firstOrNull { it.mechanicMetadata.id == guideId }?.let { document ->
        FullScreenBlueprint(document, onClose = { guideId = null }, onOverlay = {
            if (!Settings.canDrawOverlays(context)) {
                pendingOverlayId = document.mechanicMetadata.id
                guideId = null
                permissionExplanation = true
            } else launch(document)
        })
    }
    if (permissionExplanation) AlertDialog(onDismissRequest = {
        permissionExplanation = false; pendingOverlayId = null
    }, title = { Text("Allow floating blueprints?") },
        text = { Text("Android's Draw Over Other Apps permission is needed to show a small FN reference window above Fortnite. The overlay never reads or controls the game. A persistent foreground notification lets you stop it, and some devices or games may still block it.") },
        confirmButton = { TextButton(onClick = {
            permissionExplanation = false
            try {
                permission.launch(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    "package:${context.packageName}".toUri()))
            } catch (ex: Exception) {
                pendingOverlayId = null
                message = "Android could not open overlay settings."
            }
        }) { Text("Open Android Settings") } },
        dismissButton = { TextButton(onClick = {
            permissionExplanation = false; pendingOverlayId = null
        }) { Text("Not now") } })
    message?.let { alert ->
        AlertDialog(onDismissRequest = { message = null }, title = { Text("CreativeFN WS") },
            text = { Text(alert) }, confirmButton = { TextButton(onClick = { message = null }) { Text("OK") } })
    }
}

@Composable
private fun FullScreenBlueprint(document: SavedMechanic, onClose: () -> Unit, onOverlay: () -> Unit) {
    val context = LocalContext.current
    val blueprint = remember(document) { BlueprintGenerator.create(document, InitialDeviceCatalog.catalog) }
    val progress = remember(blueprint.mechanicId) { BlueprintProgress(context, blueprint.mechanicId) }
    var checked by remember(blueprint.mechanicId) { mutableStateOf(progress.checked()) }
    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxSize().padding(8.dp), color = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.large) {
            Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(blueprint.name, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                    TextButton(onClick = onClose) { Text("Close") }
                }
                Button(onClick = onOverlay, modifier = Modifier.fillMaxWidth()) { Text("Open Blueprint Overlay") }
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Devices required", style = MaterialTheme.typography.titleMedium)
                    blueprint.quantities.forEach { (name, count) -> Text("$count × $name") }
                    Text("Configuration", style = MaterialTheme.typography.titleMedium)
                    blueprint.devices.forEach { device ->
                        Text("${device.name} (${device.type})", style = MaterialTheme.typography.titleSmall)
                        device.options.forEach { Text("  ${it.name}: ${it.value}") }
                        device.notes?.takeIf { it.isNotBlank() }?.let { Text("Note: $it") }
                    }
                    Text("Direct Event Bindings", style = MaterialTheme.typography.titleMedium)
                    if (blueprint.bindings.isEmpty()) Text("No bindings saved.")
                    blueprint.bindings.forEach { Text("${it.sourceName} / ${it.eventName} → ${it.targetName} / ${it.functionName}") }
                    Text("Build checklist", style = MaterialTheme.typography.titleMedium)
                    blueprint.steps.forEachIndexed { index, step ->
                        Row(verticalAlignment = Alignment.Top) {
                            Checkbox(checked = index in checked, onCheckedChange = { enabled ->
                                checked = if (enabled) checked + index else checked - index
                                progress.save(checked)
                            })
                            Text("${index + 1}. $step", Modifier.padding(top = 10.dp))
                        }
                    }
                    Text("Unverified behavior / warnings", style = MaterialTheme.typography.titleMedium)
                    blueprint.warnings.forEach { Text("• $it", color = MaterialTheme.colorScheme.error) }
                    Text("Reference-only. Compare exact option wording and runtime behavior inside Fortnite before relying on it.")
                }
            }
        }
    }
}

class BlueprintProgress(context: Context, mechanicId: String) {
    private val prefs = context.applicationContext.getSharedPreferences("blueprint-progress", Context.MODE_PRIVATE)
    private val key = "checked:$mechanicId"
    fun checked(): Set<Int> = prefs.getString(key, "").orEmpty().split(',').mapNotNull { it.toIntOrNull() }.toSet()
    fun save(checked: Set<Int>) { prefs.edit { putString(key, checked.sorted().joinToString(",")) } }
}
