package dev.creativelogic.mobile.overlay

import dev.creativelogic.graph.Blueprint

/** Read-only on-canvas node details. No changes to saved graph coordinates or binding semantics. */
internal object InlineNodeCards {
    const val COMPACT_WIDTH = 176f
    const val COMPACT_HEIGHT = 66f
    const val EXPANDED_WIDTH = 260f
    const val HEADER_HEIGHT = 66f
    const val MAX_BODY_HEIGHT = 238f
    const val LINE_HEIGHT = 19f
    const val INNER_PADDING = 11f
    const val FOOTER_HEIGHT = 19f

    enum class Style { SECTION, OPTION, CONNECTION, NOTE }
    data class Entry(val text: String, val style: Style)

    fun entries(blueprint: Blueprint, id: String): List<Entry> {
        val device = blueprint.devices.firstOrNull { it.id == id } ?: return emptyList()
        val connections = blueprint.bindings.filter { it.sourceId == id || it.targetId == id }
        return buildList {
            add(Entry("SAVED SETTINGS", Style.SECTION))
            if (device.options.isEmpty()) add(Entry("No saved settings", Style.NOTE))
            device.options.forEach { add(Entry("${it.name}: ${it.value}", Style.OPTION)) }
            device.notes?.takeIf { it.isNotBlank() }?.let { add(Entry("Note: $it", Style.NOTE)) }
            add(Entry("CONNECTIONS (${connections.size})", Style.SECTION))
            if (connections.isEmpty()) add(Entry("No connections", Style.NOTE))
            connections.forEach { link ->
                val message = if (link.sourceId == id) {
                    "Sends: ${link.eventName} → ${link.targetName} / ${link.functionName}"
                } else {
                    "Receives: ${link.sourceName} / ${link.eventName} → ${link.functionName}"
                }
                add(Entry(message, Style.CONNECTION))
            }
            add(Entry("Verify exact options inside Fortnite.", Style.NOTE))
        }
    }

    /** Topmost expanded card wins over any compact node it temporarily overlaps. */
    fun hitTest(
        blueprint: Blueprint, expandedId: String?, expandedHeight: Float,
        sceneX: Float, sceneY: Float,
    ): String? {
        val selected = blueprint.devices.firstOrNull { it.id == expandedId }
        if (selected != null && sceneX >= selected.graphX && sceneX <= selected.graphX + EXPANDED_WIDTH &&
            sceneY >= selected.graphY && sceneY <= selected.graphY + expandedHeight) return selected.id
        return blueprint.devices.asReversed().firstOrNull { device ->
            sceneX >= device.graphX && sceneX <= device.graphX + COMPACT_WIDTH &&
                sceneY >= device.graphY && sceneY <= device.graphY + COMPACT_HEIGHT
        }?.id
    }
}
