package dev.creativelogic.mobile.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dev.creativelogic.mobile.uigraph.GraphWorkspaceViewModel
import dev.creativelogic.mobile.uigraph.GraphEditorScreen
import dev.creativelogic.mobile.blueprint.BlueprintAccess
import dev.creativelogic.mobile.BuildConfig
import dev.creativelogic.model.SavedMechanic
import dev.creativelogic.model.MechanicMetadataV1
import dev.creativelogic.model.MechanicRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.util.UUID

private enum class Destination(val label: String, val glyph: Glyph) {
    Home("Home", Glyph.Home), Recent("Recent", Glyph.Recent), Library("Library", Glyph.Library), Learn("Learn", Glyph.Learn),
    Devices("Devices", Glyph.Graph), Editor("Editor", Glyph.Graph),
}

@Composable
fun CreativeLogicApp(repository: MechanicRepository, preferences: DisplayPreferences) {
    var showHeader by rememberSaveable { mutableStateOf(preferences.showHeader) }
    var showNavigation by rememberSaveable { mutableStateOf(preferences.showNavigation) }
    var displayOptions by rememberSaveable { mutableStateOf(false) }
    val drawer = rememberDrawerState(DrawerValue.Closed)
    val workspace: GraphWorkspaceViewModel = viewModel(factory = remember(repository) { viewModelFactory { initializer { GraphWorkspaceViewModel(repository) } } })
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, workspace) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_STOP) workspace.flushInBackground() }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
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
    val navigate: (Destination) -> Unit = { next ->
        keyboard?.hide()
        scope.launch { if (destination != Destination.Editor || workspace.flush()) route = next.name }
    }
    val openNew = { pendingId = UUID.randomUUID().toString(); name = ""; error = null; creating = true }
    val openDocument: (SavedMechanic) -> Unit = {
        keyboard?.hide()
        val document = it
        scope.launch {
            if (destination != Destination.Editor || workspace.flush()) {
                selectedId = document.mechanicMetadata.id
                route = Destination.Editor.name
                drawer.close()
            }
        }
    }
    val saveDraft = {
        if (name.isNotBlank() && !saving) {
            saving = true
            error = null
            scope.launch {
                try {
                    val now = System.currentTimeMillis()
                    val id = pendingId.ifBlank { UUID.randomUUID().toString().also { pendingId = it } }
                    repository.save(SavedMechanic(appVersion = BuildConfig.VERSION_NAME, mechanicMetadata = MechanicMetadataV1(
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
    BackHandler(destination != Destination.Home && !creating && !drawer.isOpen) { navigate(Destination.Home) }
    BackHandler(drawer.isOpen) { scope.launch { drawer.close() } }
    CreativeTheme {
        ModalNavigationDrawer(drawerState = drawer, gesturesEnabled = documents.isNotEmpty() && destination != Destination.Editor, drawerContent = {
            RecentDrawer(documents, onOpen = openDocument, onClose = { keyboard?.hide(); scope.launch { drawer.close() } })
        }) {
        Scaffold(
            // The former permanent corner FAB obstructed the editor; options now live in the header.
            // When the header is hidden, keep a small recovery control so both bars can be restored.
            floatingActionButton = {
                if (!showHeader) SmallFloatingActionButton(onClick = { keyboard?.hide(); displayOptions = true },
                    modifier = Modifier.semantics { contentDescription = "View options" }) {
                    BuilderGlyph(Glyph.Menu, MaterialTheme.colorScheme.onPrimaryContainer)
                }
            },
            topBar = {
                if (showHeader) {
                    if (destination == Destination.Editor) EditorHeader(
                        documents.find { it.mechanicMetadata.id == selectedId }?.mechanicMetadata?.name ?: "Your mechanic",
                        onBack = { navigate(Destination.Home) }, onOptions = { displayOptions = true },
                    ) else BrandHeader(onOptions = { displayOptions = true })
                }
            },
            bottomBar = {
                // The graph editor owns all available space under its header.
                if (showNavigation && destination != Destination.Editor) {
                    WorkshopNavigation(
                        destinations = Destination.entries.filter {
                            it != Destination.Editor && it != Destination.Devices &&
                                (it != Destination.Recent || documents.isNotEmpty())
                        },
                        selected = destination,
                        recentOpen = drawer.isOpen,
                        onChoose = { item ->
                            keyboard?.hide()
                            if (item == Destination.Recent) scope.launch { drawer.open() } else navigate(item)
                        },
                    )
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
                        Destination.Home -> HomeScreen(openNew)
                        Destination.Recent -> HomeScreen(openNew)
                        Destination.Library -> LibraryScreen(documents, repository, openNew, openDocument)
                        Destination.Learn -> LearnScreen { navigate(Destination.Devices) }
                        Destination.Devices -> DeviceReferenceScreen()
                        Destination.Editor -> GraphEditorScreen(documents.find { it.mechanicMetadata.id == selectedId }, workspace)
                    }
                }
            }
        }
        }
        if (displayOptions) AlertDialog(
            onDismissRequest = { displayOptions = false }, title = { Text("View options") },
            text = { Column(Modifier.verticalScroll(rememberScrollState())) {
                DisplayToggle("Show header", showHeader) { showHeader = it; preferences.showHeader = it }
                DisplayToggle("Show navigation", showNavigation) { showNavigation = it; preferences.showNavigation = it }
                HorizontalDivider()
                Destination.entries.filter { it != Destination.Editor && (it != Destination.Recent || documents.isNotEmpty()) }.forEach { item ->
                    TextButton(onClick = {
                        displayOptions = false
                        if (item == Destination.Recent) scope.launch { drawer.open() } else navigate(item)
                    }, modifier = Modifier.fillMaxWidth()) { Text(item.label) }
                }
            } },
            confirmButton = { TextButton(onClick = { displayOptions = false }) { Text("Done") } },
        )
        if (creating) AlertDialog(
            onDismissRequest = { if (!saving) creating = false },
            icon = { BuilderGlyph(Glyph.Graph, MaterialTheme.colorScheme.primary) },
            title = { Row(verticalAlignment = Alignment.CenterVertically) { Text("Name your mechanic", Modifier.weight(1f));
                InformationButton("New mechanic", "A mechanic is a small system of devices working together. Name your idea, then add devices and bind their Events to Functions. Simulation is not available yet.") } },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
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

/** Compact Cover Flow-inspired bar, no full-screen overlay or game integration. */
@Composable
private fun WorkshopNavigation(destinations: List<Destination>, selected: Destination, recentOpen: Boolean,
    onChoose: (Destination) -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surfaceContainerLow, tonalElevation = 3.dp) {
        Row(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 8.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
            destinations.forEachIndexed { index, item ->
                val active = if (item == Destination.Recent) recentOpen else selected == item
                Surface(onClick = { onChoose(item) }, modifier = Modifier.weight(1f).height(54.dp)
                    .graphicsLayer {
                        rotationY = if (active) 0f else if (index % 2 == 0) 9f else -9f
                        scaleY = if (active) 1f else .89f
                        cameraDistance = 14f * density
                    }.testTag("navigation-${item.name.lowercase()}"),
                    color = if (active) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = MaterialTheme.shapes.small,
                    tonalElevation = if (active) 6.dp else 1.dp) {
                    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center) {
                        BuilderGlyph(item.glyph, if (active) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant, Modifier.size(19.dp))
                        Text(item.label, maxLines = 1, style = MaterialTheme.typography.labelSmall,
                            color = if (active) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun BrandHeader(onOptions: () -> Unit) {
    Row(Modifier.fillMaxWidth().statusBarsPadding().padding(start = 20.dp, end = 10.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.primaryContainer) {
            BuilderGlyph(Glyph.Graph, MaterialTheme.colorScheme.primary, Modifier.padding(10.dp))
        }
        Text("CREATIVE LOGIC", Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
        InformationButton("Creative Logic", "Plan device mechanics for Creative and rebuild them by hand. Your graphs stay on this device. This workshop provides scoped value-based test actions, not a live Fortnite emulator.")
        ViewOptionsButton(onOptions)
    }
}

@Composable
private fun ViewOptionsButton(onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.semantics { contentDescription = "View options" }) {
        BuilderGlyph(Glyph.Menu, MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun EditorHeader(name: String, onBack: () -> Unit, onOptions: () -> Unit) {
    Row(Modifier.fillMaxWidth().statusBarsPadding().padding(start = 6.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack, modifier = Modifier.semantics { contentDescription = "Back to home" }) {
            BuilderGlyph(Glyph.Back, MaterialTheme.colorScheme.onSurface)
        }
        Text(name, modifier = Modifier.weight(1f).padding(start = 4.dp).testTag("editor-title"),
            style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        ViewOptionsButton(onOptions)
    }
}

@Composable
private fun HomeScreen(onNew: () -> Unit) {
    Row(Modifier.widthIn(max = 760.dp).fillMaxWidth().padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
        Button(onClick = onNew, modifier = Modifier.weight(1f).heightIn(min = 52.dp)) {
            BuilderGlyph(Glyph.Add, MaterialTheme.colorScheme.onPrimary)
            Spacer(Modifier.width(8.dp)); Text("New Mechanic")
        }
        InformationButton("New mechanic", "Start with a name for your idea. Add and configure devices, then connect their Events to Functions. Your saved mechanics appear in Recent and Library. Simulation is not available yet.")
    }
}

@Composable
private fun LibraryScreen(documents: List<SavedMechanic>, repository: MechanicRepository, onNew: () -> Unit, onOpen: (SavedMechanic) -> Unit) {
    val keyboard = LocalSoftwareKeyboardController.current
    var query by rememberSaveable { mutableStateOf("") }
    val matching = remember(documents, query) { documents.filter { it.mechanicMetadata.name.contains(query.trim(), ignoreCase = true) } }
    LazyColumn(Modifier.testTag("mechanic-library").widthIn(max = 760.dp).fillMaxSize(),
        contentPadding = PaddingValues(24.dp, 24.dp, 24.dp, 96.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Text("Your mechanics", style = MaterialTheme.typography.headlineMedium) }
        item {
            OutlinedTextField(value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth(),
                label = { Text("Search mechanics") }, singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
                leadingIcon = { BuilderGlyph(Glyph.Search, MaterialTheme.colorScheme.onSurfaceVariant) })
        }
        item {
            Button(onClick = onNew, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                BuilderGlyph(Glyph.Add, MaterialTheme.colorScheme.onPrimary)
                Spacer(Modifier.width(8.dp)); Text("New Mechanic")
            }
        }
        // Blueprint entrypoint is now part of the Library, rather than a permanent global FAB.
        item(key = "blueprints") { BlueprintAccess(repository, Modifier.fillMaxWidth().testTag("library-blueprints")) }
        if (matching.isEmpty()) item {
            EmptyState(if (query.isBlank()) "Your library starts here" else "No matching mechanics",
                if (query.isBlank()) "Create a named draft for your next Creative idea." else "Try a different name or clear your search.", Glyph.Library)
        } else items(matching, key = { it.mechanicMetadata.id }) { MechanicRow(it, onOpen) }
    }
}

@Composable
private fun MechanicRow(document: SavedMechanic, onOpen: (SavedMechanic) -> Unit) {
    Card(onClick = { onOpen(document) }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
        Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.secondaryContainer) {
                BuilderGlyph(Glyph.Graph, MaterialTheme.colorScheme.secondary, Modifier.padding(12.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(document.mechanicMetadata.name, style = MaterialTheme.typography.titleMedium,
                    maxLines = 2, overflow = TextOverflow.Ellipsis)
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
        InformationButton(title, body)
    }
}

@Composable
private fun LearnScreen(onDevices: () -> Unit) {
    LazyColumn(Modifier.testTag("learn-concepts").widthIn(max = 760.dp).fillMaxSize(), contentPadding = PaddingValues(24.dp, 24.dp, 24.dp, 96.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)) {
        item { Text("Device logic", style = MaterialTheme.typography.headlineMedium) }
        item { ConceptCard("Events", "An event is a signal a device sends, such as a button being interacted with.") }
        item { ConceptCard("Functions", "A function is an action a device can receive, such as disabling a barrier.") }
        item { ConceptCard("Bindings", "Direct Event Binding links a device’s event to another device’s function.") }
        item { TextButton(onClick = onDevices) { Text("Device Reference") } }
    }
}

@Composable
private fun ConceptCard(title: String, body: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
        InformationButton(title, body)
    }
}
