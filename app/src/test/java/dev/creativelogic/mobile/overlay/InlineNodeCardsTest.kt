package dev.creativelogic.mobile.overlay

import dev.creativelogic.graph.Blueprint
import dev.creativelogic.graph.BlueprintBinding
import dev.creativelogic.graph.BlueprintDevice
import dev.creativelogic.graph.BlueprintOption
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InlineNodeCardsTest {
    private val a = BlueprintDevice("trigger", "Trigger_1", "Trigger", 0f, 0f,
        listOf(BlueprintOption("Reset Delay", "0 ms")), "Use once", "")
    private val b = BlueprintDevice("hud", "HUD_1", "HUD Message", 300f, 0f,
        emptyList(), null, "")
    private val c = BlueprintDevice("tracker", "Tracker_1", "Tracker", 300f, 140f,
        emptyList(), null, "")
    private val guide = Blueprint("test", "Demo", emptyList(), listOf(a, b, c),
        listOf(BlueprintBinding("trigger", "hud", "Trigger_1", "On Triggered", "HUD_1", "Show"),
            BlueprintBinding("tracker", "trigger", "Tracker_1", "On Complete", "Trigger_1", "Trigger")),
        emptyList(), emptyList())

    @Test fun expandedContentNamesExactSettingsAndBothDirections() {
        val entries = InlineNodeCards.entries(guide, "trigger").map { it.text }
        assertTrue(entries.contains("Reset Delay: 0 ms"))
        assertTrue(entries.contains("Note: Use once"))
        assertTrue(entries.contains("Sends: On Triggered → HUD_1 / Show"))
        assertTrue(entries.contains("Receives: Tracker_1 / On Complete → Trigger"))
        assertTrue(entries.contains("CONNECTIONS (2)"))
    }

    @Test fun expandedNodeReceivesHitBeforeItsCompactNeighbors() {
        // Expansion from x=0 to x=260, while the compact card is only 176 wide.
        assertEquals("trigger", InlineNodeCards.hitTest(guide, "trigger", 280f, 248f, 180f))
        assertEquals("hud", InlineNodeCards.hitTest(guide, "trigger", 280f, 320f, 20f))
        assertEquals(null, InlineNodeCards.hitTest(guide, null, 66f, 248f, 180f))
    }

    @Test fun emptyOrMissingDevicesHaveNoInventedConnections() {
        assertTrue(InlineNodeCards.entries(guide, "missing").isEmpty())
        val messages = InlineNodeCards.entries(guide, "hud").map { it.text }
        assertTrue(messages.any { it.contains("Receives: Trigger_1 / On Triggered → Show") })
        assertFalse(messages.any { it.startsWith("Sends:") })
    }
}
