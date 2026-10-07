package dev.creativelogic.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import java.util.UUID

/** Explicit storage contract. Domain device classes will be separate in Phase 1. */
@Serializable
data class MechanicDocumentV1(
    val formatVersion: Int = 1,
    val appVersion: String = "0.0.1",
    val catalogVersion: String = "unavailable",
    val mechanicMetadata: MechanicMetadataV1,
    val devices: List<JsonObject> = emptyList(),
    val bindings: List<JsonObject> = emptyList(),
    val layout: JsonObject = JsonObject(emptyMap()),
    val notes: String = "",
)

@Serializable
data class MechanicMetadataV1(
    val id: String,
    val name: String,
    val createdAtEpochMillis: Long,
    val modifiedAtEpochMillis: Long,
)

object MechanicDocumentCodec {
    private val json = Json { encodeDefaults = true }

    fun encode(document: MechanicDocumentV1): String {
        validate(document)
        return json.encodeToString(document)
    }

    fun decode(source: String): MechanicDocumentV1 =
        json.decodeFromString<MechanicDocumentV1>(source).also(::validate)

    private fun validate(document: MechanicDocumentV1) {
        require(document.formatVersion == 1) { "Unsupported mechanic format: ${document.formatVersion}" }
        require(UUID.fromString(document.mechanicMetadata.id).toString() == document.mechanicMetadata.id) {
            "Mechanic ID must be a canonical UUID"
        }
        require(document.mechanicMetadata.name.isNotBlank()) { "Mechanic name is required" }
        // These fields are reserved, never silently discard content we cannot yet interpret.
        require(document.devices.isEmpty() && document.bindings.isEmpty() && document.layout.isEmpty()) {
            "Device graphs are not supported by the Phase 0 storage contract"
        }
    }
}
