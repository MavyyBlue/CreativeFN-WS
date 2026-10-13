package dev.creativelogic.model

import kotlinx.serialization.Serializable

/** Configuration only. Runtime simulation state belongs to a later session. */
data class GraphViewport(val panX: Float = 0f, val panY: Float = 0f, val zoom: Float = 1f) {
    init { require(panX.isFinite() && panY.isFinite() && zoom.isFinite() && zoom in 0.1f..3f) }
}
data class MechanicGraph(
    val devices: List<CreativeDeviceInstance> = emptyList(),
    val connections: List<LogicConnection> = emptyList(),
    val viewport: GraphViewport = GraphViewport(),
)

/** Organizational metadata only. Never alters the executable device graph. */
@Serializable
data class BlueprintFolderV1(
    val id: String,
    val name: String,
    val deviceIds: List<String>,
    val folderFormatVersion: Int = 1,
)

data class SavedMechanic(
    val mechanicMetadata: MechanicMetadataV1,
    val appVersion: String = "0.0.4", val catalogVersion: String = "0.2.0-draft",
    val graph: MechanicGraph = MechanicGraph(), val notes: String = "",
    val folders: List<BlueprintFolderV1> = emptyList(),
)
