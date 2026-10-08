package dev.creativelogic.catalog

import dev.creativelogic.model.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*

/** Explicit provisional wire contract; never serialize domain or runtime objects. */
@Serializable data class DeviceInstanceDtoV1(
    val formatVersion: Int = 1, val catalogVersion: String,
    val id: String, val deviceTypeId: String, val name: String,
    val graphX: Float, val graphY: Float, val options: Map<String, DeviceOptionDtoV1>, val notes: String?,
)
@Serializable data class DeviceOptionDtoV1(val type: String, val value: JsonPrimitive)

class DeviceInstanceCodec(private val catalog: DeviceCatalog) {
    private val json = Json { encodeDefaults = true }
    fun encode(instance: CreativeDeviceInstance): String {
        catalog.validate(instance)
        return json.encodeToString(toDto(instance))
    }
    fun toDto(instance: CreativeDeviceInstance): DeviceInstanceDtoV1 {
        catalog.validate(instance)
        return DeviceInstanceDtoV1(catalogVersion = catalog.catalogVersion,
            id = instance.id, deviceTypeId = instance.deviceTypeId, name = instance.name,
            graphX = instance.graphX, graphY = instance.graphY,
            options = instance.configuredOptions.mapValues { (_, value) -> encodeOption(value) }, notes = instance.builderNotes)
    }
    fun decode(source: String): CreativeDeviceInstance {
        return fromDto(json.decodeFromString<DeviceInstanceDtoV1>(source))
    }
    fun fromDto(dto: DeviceInstanceDtoV1): CreativeDeviceInstance {
        require(dto.formatVersion == 1) { "Unsupported device format" }
        require(dto.catalogVersion == catalog.catalogVersion) { "Unsupported catalog version; migration required" }
        return CreativeDeviceInstance(dto.id, dto.deviceTypeId, dto.name, dto.graphX, dto.graphY,
            dto.options.mapValues { (_, value) -> decodeOption(value) }, dto.notes).also(catalog::validate)
    }
    private fun encodeOption(value: DeviceOptionValue) = when (value) {
        is DeviceOptionValue.BooleanValue -> DeviceOptionDtoV1("boolean", JsonPrimitive(value.value))
        is DeviceOptionValue.IntegerValue -> DeviceOptionDtoV1("integer", JsonPrimitive(value.value))
        is DeviceOptionValue.DurationValue -> DeviceOptionDtoV1("durationMilliseconds", JsonPrimitive(value.milliseconds))
        is DeviceOptionValue.EnumValue -> DeviceOptionDtoV1("enum", JsonPrimitive(value.value))
        is DeviceOptionValue.TextValue -> DeviceOptionDtoV1("text", JsonPrimitive(value.value))
    }
    private fun decodeOption(dto: DeviceOptionDtoV1): DeviceOptionValue {
        val v = dto.value
        return when (dto.type) {
            "boolean" -> { require(!v.isString); DeviceOptionValue.BooleanValue(requireNotNull(v.booleanOrNull)) }
            "integer" -> { require(!v.isString); DeviceOptionValue.IntegerValue(requireNotNull(v.intOrNull)) }
            "durationMilliseconds" -> { require(!v.isString); DeviceOptionValue.DurationValue(requireNotNull(v.longOrNull)) }
            "enum" -> { require(v.isString); DeviceOptionValue.EnumValue(v.content) }
            "text" -> { require(v.isString); DeviceOptionValue.TextValue(v.content) }
            else -> throw IllegalArgumentException("Unsupported option type: ${dto.type}")
        }
    }
}
