package dev.creativelogic.mobile

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test
import java.util.UUID

class GraphExperienceTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private fun waitForGraph() = compose.waitUntil(10_000) {
        compose.onAllNodes(hasTestTag("logic-canvas")).fetchSemanticsNodes().isNotEmpty()
    }
    private fun add(name: String) {
        compose.onNodeWithTag("graph-devices").performClick()
        compose.onNodeWithText(name).performClick()
        waitForGraph()
    }
    private fun bind(source: String, event: String, target: String, function: String) {
        fun choose(label: String, value: String) {
            compose.onNodeWithTag("choice-$label").performScrollTo().performClick()
            compose.onNode(hasText(value) and hasAnyAncestor(hasTestTag("choice-menu-$label"))).performClick()
        }
        choose("Source device", source)
        choose("Source event", event)
        choose("Target device", target)
        choose("Target function", function)
        compose.onNodeWithText("Add binding").performScrollTo().performClick()
    }
    @Test fun constructConfigureBindUndoAndReopenTriggerTrackerBarrier() {
        val name = "Graph ${UUID.randomUUID().toString().take(8)}"
        compose.onNodeWithText("New Mechanic").performClick()
        compose.onNodeWithText("Mechanic name").performTextInput(name)
        compose.onNodeWithText("Create draft").performClick()
        waitForGraph()
        add("Trigger"); add("Tracker"); add("Barrier")
        compose.onNodeWithTag("logic-canvas").assert(hasStateDescription("3 devices, 0 bindings"))
        compose.onNodeWithTag("graph-devices").performClick()
        compose.onNodeWithText("Placed").performClick()
        compose.onNodeWithText("Tracker_1").performClick()
        compose.onNodeWithText("Target Value").performScrollTo().performTextReplacement("5")
        compose.onNodeWithText("Done").performScrollTo().performClick()
        compose.onNodeWithTag("graph-bindings").performClick()
        bind("Trigger_1", "On Triggered", "Tracker_1", "Increment Progress")
        bind("Tracker_1", "On Complete", "Barrier_1", "Disable")
        compose.onNodeWithText("Close").performScrollTo().performClick()
        compose.onNodeWithTag("logic-canvas").assert(hasStateDescription("3 devices, 2 bindings"))
        compose.onNodeWithTag("graph-undo").performClick()
        compose.onNodeWithTag("logic-canvas").assert(hasStateDescription("3 devices, 1 bindings"))
        compose.onNodeWithTag("graph-redo").performClick()
        compose.onNodeWithTag("logic-canvas").assert(hasStateDescription("3 devices, 2 bindings"))
        compose.onNodeWithTag("graph-save-button").performClick()
        compose.waitUntil(10_000) { compose.onAllNodes(hasTestTag("graph-save") and hasText("Saved")).fetchSemanticsNodes().isNotEmpty() }
        compose.activityRule.scenario.recreate()
        waitForGraph()
        compose.onNodeWithTag("logic-canvas").assert(hasStateDescription("3 devices, 2 bindings"))
        compose.onNodeWithContentDescription("Back to home").performClick()
        compose.onNodeWithText("Library").performClick()
        compose.onNodeWithText("Search mechanics").performTextInput(name)
        compose.onNodeWithText("Search mechanics").performImeAction()
        val result = hasText(name) and !hasSetTextAction() and hasAnyAncestor(hasTestTag("mechanic-library"))
        compose.onNodeWithTag("mechanic-library").performScrollToNode(result)
        compose.onNode(result).performClick()
        waitForGraph()
        compose.onNodeWithTag("logic-canvas").assert(hasStateDescription("3 devices, 2 bindings"))
        compose.onNodeWithTag("graph-devices").performClick()
        compose.onNodeWithText("Placed").performClick()
        compose.onNodeWithText("Tracker_1").performClick()
        compose.onNodeWithText("Target Value").performScrollTo().assertTextContains("5")
        compose.onNodeWithText("Done").performScrollTo().performClick()
    }
}
