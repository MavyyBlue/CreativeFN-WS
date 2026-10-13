package dev.creativelogic.graph

import dev.creativelogic.catalog.*
import dev.creativelogic.model.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import java.util.UUID

@Serializable data class BindingDtoV2(val id: String, val sourceDeviceId: String, val sourceEventId: String, val targetDeviceId: String, val targetFunctionId: String)
@Serializable data class ViewportDtoV2(val panX: Float = 0f, val panY: Float = 0f, val zoom: Float = 1f)
@Serializable data class MechanicDocumentDtoV2(
    val formatVersion: Int = 2, val appVersion: String, val catalogVersion: String,
    val mechanicMetadata: MechanicMetadataV1, val devices: List<DeviceInstanceDtoV1>,
    val bindings: List<BindingDtoV2>, val viewport: ViewportDtoV2, val notes: String,
    // Optional within document format 2: legacy documents have no folders field.
    val folders: List<BlueprintFolderV1> = emptyList(),
)

/** A folder contains stable device IDs; it does not add or reroute any event connection. */
fun validateBlueprintFolders(folders: List<BlueprintFolderV1>, graph: MechanicGraph) {
    val deviceIds = graph.devices.map { it.id }.toSet()
    require(folders.map { it.id }.distinct().size == folders.size) { "Duplicate folder ID" }
    val occupied = mutableSetOf<String>()
    folders.forEach { folder ->
        require(folder.folderFormatVersion == 1) { "Unsupported folder format" }
        require(UUID.fromString(folder.id).toString() == folder.id) { "Invalid folder ID" }
        require(folder.name.isNotBlank() && folder.name.length <= 60) { "Folder name must be 1–60 characters" }
        require(folder.deviceIds.isNotEmpty()) { "Empty folder" }
        require(folder.deviceIds.distinct().size == folder.deviceIds.size) { "Repeated folder device" }
        require(folder.deviceIds.all { it in deviceIds }) { "Folder references missing device" }
        require(folder.deviceIds.all { occupied.add(it) }) { "Device belongs to multiple folders" }
    }
}

class MechanicGraphCodec(private val catalog: DeviceCatalog) {
    private val json = Json { encodeDefaults = true }
    private val instances = DeviceInstanceCodec(catalog)
    private fun validate(mechanic: SavedMechanic) {
        require(UUID.fromString(mechanic.mechanicMetadata.id).toString() == mechanic.mechanicMetadata.id)
        require(mechanic.mechanicMetadata.name.isNotBlank())
        require(mechanic.catalogVersion == catalog.catalogVersion) { "Unsupported catalog version" }
        validateGraph(mechanic.graph, catalog)
        validateBlueprintFolders(mechanic.folders, mechanic.graph)
    }
    fun encode(mechanic: SavedMechanic): String {
        validate(mechanic)
        val viewport = mechanic.graph.viewport
        return json.encodeToString(MechanicDocumentDtoV2(appVersion = mechanic.appVersion, catalogVersion = mechanic.catalogVersion,
            mechanicMetadata = mechanic.mechanicMetadata, devices = mechanic.graph.devices.map(instances::toDto),
            bindings = mechanic.graph.connections.map { BindingDtoV2(it.id, it.sourceDeviceId, it.sourceEventId.value, it.targetDeviceId, it.targetFunctionId.value) },
            viewport = ViewportDtoV2(viewport.panX, viewport.panY, viewport.zoom), notes = mechanic.notes,
            folders = mechanic.folders))
    }
    fun decode(source: String): SavedMechanic {
        val version = json.parseToJsonElement(source).jsonObject.getValue("formatVersion").jsonPrimitive.int
        if (version == 1) {
            val old = MechanicDocumentCodec.decode(source)
            return SavedMechanic(old.mechanicMetadata, old.appVersion, catalog.catalogVersion, notes = old.notes)
        }
        require(version == 2) { "Unsupported mechanic format: $version" }
        val dto = json.decodeFromString<MechanicDocumentDtoV2>(source)
        require(dto.catalogVersion == catalog.catalogVersion || dto.catalogVersion == "0.1.0-draft") {
            "Catalog migration required"
        }
        require(dto.devices.all { it.catalogVersion == dto.catalogVersion }) { "Mismatched device catalog versions" }
        val graph = MechanicGraph(dto.devices.map(instances::fromDto), dto.bindings.map { LogicConnection(it.id, it.sourceDeviceId, DeviceEventId(it.sourceEventId), it.targetDeviceId, DeviceFunctionId(it.targetFunctionId)) },
            GraphViewport(dto.viewport.panX, dto.viewport.panY, dto.viewport.zoom))
        return SavedMechanic(dto.mechanicMetadata, dto.appVersion, catalog.catalogVersion, graph, dto.notes, dto.folders).also(::validate)
    }
}
