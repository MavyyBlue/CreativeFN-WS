package dev.creativelogic.catalog

import dev.creativelogic.model.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Offline RESEARCH-ONLY Creative documentation inventory, not a verified in-game menu.
 * Entries are appended without altering the original eight stable device definitions.
 * An empty text value means the real Fortnite setting/default is NOT known: it is NOT a claim of a blank Fortnite default.
 */
object ResearchDeviceDefinitions {
    private fun pairs(record: kotlinx.serialization.json.JsonObject, field: String): List<Pair<String, String>> =
        record.getValue(field).jsonArray.map { row ->
            val pair = row.jsonArray
            pair[0].jsonPrimitive.content to pair[1].jsonPrimitive.content
        }

    fun load(): List<CreativeDeviceDefinition> {
        val input = requireNotNull(javaClass.getResourceAsStream("/creative1_reference_devices.json")) {
            "Missing bundled Creative research reference inventory"
        }
        val root = input.bufferedReader(Charsets.UTF_8).use { Json.parseToJsonElement(it.readText()).jsonObject }
        require(root.getValue("schema").jsonPrimitive.content == "1")
        return root.getValue("records").jsonArray.map { item ->
            val d = item.jsonObject
            val id = d.getValue("id").jsonPrimitive.content
            val name = d.getValue("name").jsonPrimitive.content
            val settings = pairs(d, "settings").map { (key, label) ->
                DeviceOptionDefinition(key, label, DeviceOptionKind.TEXT, DeviceOptionValue.TextValue(""))
            }
            CreativeDeviceDefinition(
                deviceTypeId = id,
                displayName = name,
                category = d.getValue("category").jsonPrimitive.content,
                options = settings,
                events = pairs(d, "events").map { (key, label) -> DeviceEventDefinition(DeviceEventId(key), label) },
                functions = pairs(d, "functions").map { (key, label) -> DeviceFunctionDefinition(DeviceFunctionId(key), label) },
                simulationSupport = SimulationSupport.NOT_SIMULATED,
                limitations = listOf(
                    "RESEARCH REFERENCE ONLY: current Creative 1.0 device availability and complete settings are not verified in-game.",
                    "Configuration fields are free-text builder notes, initially unspecified; no Fortnite defaults, bounds, or rules are asserted.",
                    "Manual event injection can test connected functions. Unmodeled functions record UNSUPPORTED instead of a fabricated gameplay effect."
                ),
                referenceUrl = d.getValue("url").jsonPrimitive.content,
            )
        }
    }
}
