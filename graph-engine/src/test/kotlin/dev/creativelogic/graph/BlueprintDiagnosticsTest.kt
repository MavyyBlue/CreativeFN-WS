package dev.creativelogic.graph

import dev.creativelogic.catalog.InitialDeviceCatalog
import dev.creativelogic.model.*
import org.junit.Assert.*
import org.junit.Test

class BlueprintDiagnosticsTest {
    @Test fun warningsInConstructionGuideExplainRepairWithoutAlteringOriginal() {
        val catalog = InitialDeviceCatalog.catalog
        val tracker = catalog.createInstance("tracker", "Quest").let {
            it.copy(configuredOptions = it.configuredOptions +
                ("sharing" to DeviceOptionValue.EnumValue("Individual")) +
                ("targetValue" to DeviceOptionValue.IntegerValue(0)))
        }
        val graph = MechanicGraph(devices = listOf(tracker))
        val document = SavedMechanic(
            mechanicMetadata = MechanicMetadataV1("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee", "Quest logic", 1, 2),
            graph = graph,
        )
        val blueprint = BlueprintGenerator.create(document, catalog)
        assertTrue(blueprint.warnings.any { "Tracker sharing not simulated" in it })
        assertTrue(blueprint.warnings.any { "Tracker target is zero" in it })
        assertEquals(graph, document.graph)
    }
}
