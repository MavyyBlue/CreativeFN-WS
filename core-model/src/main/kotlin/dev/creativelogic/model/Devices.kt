package dev.creativelogic.model

import java.util.UUID

@JvmInline value class DeviceEventId(val value: String) { init { require(value.isNotBlank()) } }
@JvmInline value class DeviceFunctionId(val value: String) { init { require(value.isNotBlank()) } }
data class DeviceEventDefinition(val id: DeviceEventId, val displayName: String)
data class DeviceFunctionDefinition(val id: DeviceFunctionId, val displayName: String)
enum class SimulationSupport { FULLY_SIMULATED, PARTIALLY_SIMULATED, NOT_SIMULATED }
enum class DeviceOptionKind { BOOLEAN, INTEGER, DURATION, ENUM, TEXT }
sealed interface DeviceOptionValue {
    data class BooleanValue(val value: Boolean) : DeviceOptionValue
    data class IntegerValue(val value: Int) : DeviceOptionValue
    data class DurationValue(val milliseconds: Long) : DeviceOptionValue
    data class EnumValue(val value: String) : DeviceOptionValue
    data class TextValue(val value: String) : DeviceOptionValue
}
data class DeviceOptionDefinition(
    val id: String, val displayName: String, val kind: DeviceOptionKind,
    val defaultValue: DeviceOptionValue, val minimum: Long? = null, val maximum: Long? = null,
    val choices: List<String> = emptyList(),
) {
    fun accepts(value: DeviceOptionValue): Boolean = when (kind) {
        DeviceOptionKind.BOOLEAN -> value is DeviceOptionValue.BooleanValue
        DeviceOptionKind.INTEGER -> value is DeviceOptionValue.IntegerValue && inRange(value.value.toLong())
        DeviceOptionKind.DURATION -> value is DeviceOptionValue.DurationValue && value.milliseconds >= 0 && inRange(value.milliseconds)
        DeviceOptionKind.ENUM -> value is DeviceOptionValue.EnumValue && value.value in choices
        DeviceOptionKind.TEXT -> value is DeviceOptionValue.TextValue
    }
    private fun inRange(value: Long) = (minimum == null || value >= minimum) && (maximum == null || value <= maximum)
}
data class CreativeDeviceDefinition(
    val deviceTypeId: String, val displayName: String, val category: String,
    val options: List<DeviceOptionDefinition>, val events: List<DeviceEventDefinition>,
    val functions: List<DeviceFunctionDefinition>, val simulationSupport: SimulationSupport,
    val limitations: List<String>, val referenceUrl: String,
)
data class CreativeDeviceInstance(
    val id: String, val deviceTypeId: String, val name: String,
    val graphX: Float = 0f, val graphY: Float = 0f,
    val configuredOptions: Map<String, DeviceOptionValue>, val builderNotes: String? = null,
) {
    init {
        require(UUID.fromString(id).toString() == id) { "Canonical device UUID required" }
        require(deviceTypeId.isNotBlank() && name.isNotBlank())
        require(graphX.isFinite() && graphY.isFinite())
    }
}
data class LogicConnection(
    val id: String, val sourceDeviceId: String, val sourceEventId: DeviceEventId,
    val targetDeviceId: String, val targetFunctionId: DeviceFunctionId,
) {
    init { listOf(id, sourceDeviceId, targetDeviceId).forEach { require(UUID.fromString(it).toString() == it) } }
}
