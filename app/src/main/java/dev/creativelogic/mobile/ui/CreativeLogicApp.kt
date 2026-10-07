package dev.creativelogic.mobile.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.creativelogic.mobile.BuildConfig
import dev.creativelogic.model.MechanicDocumentV1
import dev.creativelogic.model.MechanicMetadataV1
import dev.creativelogic.model.MechanicRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.util.UUID

private enum class Destination(val label: String, val glyph: Glyph) {
    Home("Home", Glyph.Home), Library("Library", Glyph.Library), Learn("Learn", Glyph.Learn),
    Editor("Editor", Glyph.Graph),
}

@Composable
fun CreativeLogicApp(repository: MechanicRepository) {
    val documents by remember(repository) { repository.observeAll() }
        .collectAsStateWithLifecycle(initialValue = emptyList())
    var route by rememberSaveable { mutableStateOf(Destination.Home.name) }
    val destination = Destination.entries.firstOrNull { it.name.equals(route, ignoreCase = true) } ?: Destination.Home
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingId by rememberSaveable { mutableStateOf("") }
    var creating by rememberSaveable { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val keyboard = LocalSoftwareKeyboardController.current
    val openNew = { pendingId = UUID.randomUUID().toString(); name = ""; error = null; creating = true }
    val openDocument: (MechanicDocumentV1) -> Unit = {
        keyboard?.hide()
        selectedId = it.mechanicMetadata.id
        route = Destination.Editor.name
    }
    val saveDraft = {
        if (name.isNotBlank() && !saving) {
            saving = true
            error = null
            scope.launch {
                try {
                    val now = System.currentTimeMillis()
                    val id = pendingId.ifBlank { UUID.randomUUID().toString().also { pendingId = it } }
                    repository.save(MechanicDocumentV1(appVersion = BuildConfig.VERSION_NAME, mechanicMetadata = MechanicMetadataV1(
                        id, name.trim(), now, now,
                    )))
                    selectedId = id
                    keyboard?.hide()
                    creating = false
                    route = Destination.Editor.name
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    error = "Couldn’t save your draft. Please try again."
                } finally { saving = false }
            }
        }
    }
    BackHandler(destination != Destination.Home && !creating) { route = Destination.Home.name }
    CreativeTheme {
        Scaffold(
            topBar = {
                if (destination == Destination.Editor) EditorHeader(
                    documents.find { it.mechanicMetadata.id == selectedId }?.mechanicMetadata?.name ?: "Your mechanic",
                    onBack = { route = Destination.Home.name },
                ) else BrandHeader()
            },
            bottomBar = {
                if (destination != Destination.Editor) NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainerLow) {
                    Destination.entries.filter { it != Destination.Editor }.forEach { item ->
                        NavigationBarItem(selected = destination == item,
                            onClick = { keyboard?.hide(); route = item.name },
                            icon = { BuilderGlyph(item.glyph, if (destination == item)
                                MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant) },
                            label = { Text(item.label) })
                    }
                }
            },
        ) { insets ->
            AnimatedContent(targetState = destination,
                modifier = Modifier.fillMaxSize().padding(insets),
                transitionSpec = {
                    (fadeIn(tween(180)) + slideInHorizontally(tween(220)) { it / 16 }) togetherWith
                        (fadeOut(tween(120)) + slideOutHorizontally(tween(180)) { -it / 16 })
                }, label = "Screen transition") { screen ->
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                    when (screen) {
                        Destination.Home -> HomeScreen(documents, openNew, openDocument,
                            onLibrary = { route = Destination.Library.name }, onLearn = { route = Destination.Learn.name })
                        Destination.Library -> LibraryScreen(documents, openNew, openDocument)
                        Destination.Learn -> LearnScreen()
                        Destination.Editor -> EditorScreen()
                    }
                }
            }
        }
        if (creating) AlertDialog(
            onDismissRequest = { if (!saving) creating = false },
            icon = { BuilderGlyph(Glyph.Graph, MaterialTheme.colorScheme.primary) },
            title = { Text("Name your mechanic") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("A mechanic is a small system of devices working together. Start with the idea you want to build.")
                    OutlinedTextField(value = name, onValueChange = { if (!saving) name = it.take(80) },
                        label = { Text("Mechanic name") }, placeholder = { Text("e.g. Three-Key Vault") },
                        singleLine = true, enabled = !saving, modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { saveDraft() }))
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            },
            confirmButton = { TextButton(onClick = { saveDraft() }, enabled = name.isNotBlank() && !saving) {
                Text(if (saving) "Saving…" else "Create draft")
            } },
            dismissButton = { TextButton(onClick = { creating = false }, enabled = !saving) { Text("Cancel") } },
        )
    }
}

@Composable
private fun BrandHeader() {
    Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.primaryContainer) {
            BuilderGlyph(Glyph.Graph, MaterialTheme.colorScheme.primary, Modifier.padding(10.dp))
        }
        Column(Modifier.weight(1f)) {
            Text("CREATIVE LOGIC", style = MaterialTheme.typography.titleMedium)
            Text("Create something clever.", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        BadgeLabel("PREVIEW")
    }
}

@Composable
private fun EditorHeader(name: String, onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack, modifier = Modifier.semantics { contentDescription = "Back to home" }) {
            BuilderGlyph(Glyph.Back, MaterialTheme.colorScheme.onSurface)
        }
        Column(Modifier.weight(1f).padding(horizontal = 8.dp)) {
            Text(name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("Saved on this device", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        BadgeLabel("DRAFT")
    }
}

@Composable
private fun BadgeLabel(text: String) {
    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer) {
        Text(text, Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
    }
}

@Composable
private fun HomeScreen(documents: List<MechanicDocumentV1>, onNew: () -> Unit,
    onOpen: (MechanicDocumentV1) -> Unit, onLibrary: () -> Unit, onLearn: () -> Unit) {
    LazyColumn(Modifier.widthIn(max = 760.dp).fillMaxSize(), contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)) {
        item {
            Surface(shape = MaterialTheme.shapes.extraLarge, color = MaterialTheme.colorScheme.surfaceContainer) {
                Column(Modifier.background(Brush.linearGradient(listOf(
                    MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.surfaceContainer,
                ))).padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("MADE FOR CREATIVE BUILDERS", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary)
                    Text("Your next mechanic\nstarts with an idea.", style = MaterialTheme.typography.headlineLarge)
                    Text("Plan device logic. Keep your settings together. Rebuild it by hand in Creative.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Button(onClick = onNew, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                        shape = MaterialTheme.shapes.medium) {
                        BuilderGlyph(Glyph.Add, MaterialTheme.colorScheme.onPrimary)
                        Spacer(Modifier.width(8.dp)); Text("New Mechanic")
                    }
                }
            }
        }
        item { PreviewNotice() }
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Recent Mechanics", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                TextButton(onClick = onLibrary) { Text("View all") }
            }
        }
        if (documents.isEmpty()) item {
            EmptyState("A home for your ideas", "Create your first draft and find it here whenever you’re ready to continue.", Glyph.Library)
        } else items(documents.take(5), key = { it.mechanicMetadata.id }) { MechanicRow(it, onOpen) }
        item {
            OutlinedCard(onClick = onLearn, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    BuilderGlyph(Glyph.Learn, MaterialTheme.colorScheme.secondary)
                    Column(Modifier.weight(1f)) {
                        Text("New to device logic?", style = MaterialTheme.typography.titleMedium)
                        Text("Start with events, functions and bindings.", color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall)
                    }
                    BuilderGlyph(Glyph.Arrow, MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun PreviewNotice() {
    Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surfaceContainerLow) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BuilderGlyph(Glyph.Learn, MaterialTheme.colorScheme.secondary)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Planning preview", style = MaterialTheme.typography.titleSmall)
                Text("Save named drafts today. Device editing, simulation and the build overlay are coming next.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun LibraryScreen(documents: List<MechanicDocumentV1>, onNew: () -> Unit, onOpen: (MechanicDocumentV1) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    val matching = remember(documents, query) { documents.filter { it.mechanicMetadata.name.contains(query.trim(), ignoreCase = true) } }
    LazyColumn(Modifier.widthIn(max = 760.dp).fillMaxSize(), contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text("Your mechanics", style = MaterialTheme.typography.headlineMedium)
            Text("Ideas worth keeping, all on your device.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            OutlinedTextField(value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth(),
                label = { Text("Search mechanics") }, singleLine = true,
                leadingIcon = { BuilderGlyph(Glyph.Search, MaterialTheme.colorScheme.onSurfaceVariant) })
        }
        item {
            Button(onClick = onNew, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                BuilderGlyph(Glyph.Add, MaterialTheme.colorScheme.onPrimary)
                Spacer(Modifier.width(8.dp)); Text("New Mechanic")
            }
        }
        if (matching.isEmpty()) item {
            EmptyState(if (query.isBlank()) "Your library starts here" else "No matching mechanics",
                if (query.isBlank()) "Create a named draft for your next Creative idea." else "Try a different name or clear your search.", Glyph.Library)
        } else items(matching, key = { it.mechanicMetadata.id }) { MechanicRow(it, onOpen) }
    }
}

@Composable
private fun MechanicRow(document: MechanicDocumentV1, onOpen: (MechanicDocumentV1) -> Unit) {
    Card(onClick = { onOpen(document) }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
        Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.secondaryContainer) {
                BuilderGlyph(Glyph.Graph, MaterialTheme.colorScheme.secondary, Modifier.padding(12.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(document.mechanicMetadata.name, style = MaterialTheme.typography.titleMedium,
                    maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text("Draft · 0 devices · Not tested", color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall)
            }
            BuilderGlyph(Glyph.Arrow, MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun EmptyState(title: String, body: String, glyph: Glyph) {
    Column(Modifier.fillMaxWidth().padding(vertical = 24.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        BuilderGlyph(glyph, MaterialTheme.colorScheme.secondary, Modifier.size(36.dp))
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

@Composable
private fun LearnScreen() {
    LazyColumn(Modifier.widthIn(max = 760.dp).fillMaxSize(), contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)) {
        item {
            Text("Small devices.\nBig possibilities.", style = MaterialTheme.typography.headlineLarge)
            Text("Device logic is a conversation: one device announces something, another responds.",
                color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
        }
        item { ConceptCard("01", "Events", "Something happened.", "An event is a signal a device sends, such as a button being interacted with.") }
        item { ConceptCard("02", "Functions", "Do something next.", "A function is an action a device can receive, such as disabling a barrier.") }
        item { ConceptCard("03", "Bindings", "Connect the conversation.", "Direct Event Binding links a device’s event to another device’s function.") }
        item { PreviewNotice() }
        item { Text("The device reference and playable examples will appear as their behavior is verified.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable
private fun ConceptCard(number: String, title: String, subtitle: String, body: String) {
    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainer) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(number, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(subtitle, color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.titleSmall)
            Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun EditorScreen() {
    val dots = MaterialTheme.colorScheme.outlineVariant
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(Modifier.fillMaxWidth().weight(1f).clip(MaterialTheme.shapes.extraLarge)
            .background(MaterialTheme.colorScheme.surfaceContainerLow), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                val spacing = 24.dp.toPx()
                var x = spacing
                while (x < size.width) {
                    var y = spacing
                    while (y < size.height) { drawCircle(dots, 1.dp.toPx(), Offset(x, y)); y += spacing }
                    x += spacing
                }
            }
            Surface(Modifier.padding(20.dp).widthIn(max = 400.dp), shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainer, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                Column(Modifier.verticalScroll(rememberScrollState()).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    BuilderGlyph(Glyph.Graph, MaterialTheme.colorScheme.primary, Modifier.size(40.dp))
                    Text("Logic canvas", style = MaterialTheme.typography.titleLarge)
                    Text("Your idea has a place to grow.", style = MaterialTheme.typography.titleSmall)
                    Text("Device placement and connections are coming next. For now, your named draft is saved and ready for later.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            }
        }
        Text("Draft saved locally · Device tools coming soon", style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.align(Alignment.CenterHorizontally))
    }
}
