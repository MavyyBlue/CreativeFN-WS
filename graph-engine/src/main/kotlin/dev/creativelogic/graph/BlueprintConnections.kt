package dev.creativelogic.graph

/** Read-only graph relations for blueprint navigation. Never executes device functions. */
object BlueprintConnections {
    fun directlyConnected(blueprint: Blueprint, rootId: String): Set<String> {
        if (blueprint.devices.none { it.id == rootId }) return emptySet()
        val ids = mutableSetOf(rootId)
        blueprint.bindings.forEach { binding ->
            if (binding.sourceId == rootId) ids.add(binding.targetId)
            if (binding.targetId == rootId) ids.add(binding.sourceId)
        }
        return ids
    }
    fun touching(blueprint: Blueprint, deviceId: String): List<BlueprintBinding> =
        blueprint.bindings.filter { it.sourceId == deviceId || it.targetId == deviceId }
}
