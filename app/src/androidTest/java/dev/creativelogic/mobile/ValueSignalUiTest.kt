package dev.creativelogic.mobile

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test
import java.util.UUID

/** Search a reference device, inject a manual input and inspect an honest value report. */
class ValueSignalUiTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun researchDeviceHasManualEventActionWithoutChangingOriginalScene() {
        compose.onNodeWithText("New Mechanic").performClick()
        compose.onNodeWithText("Mechanic name").performTextInput("Zone ${UUID.randomUUID().toString().take(8)}")
        compose.onNodeWithText("Create draft").performClick()
        compose.waitUntil(10_000) { compose.onAllNodes(hasTestTag("graph-tools-toggle")).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("graph-tools-toggle").performClick()
        compose.onNodeWithTag("graph-devices").performClick()
        compose.onNodeWithText("Search devices").performTextInput("Mutator Zone")
        // The text field itself also contains Mutator Zone: only the result card is clickable without an edit action.
        compose.onNode(hasText("Mutator Zone", substring = false) and hasClickAction() and !hasSetTextAction()).performClick()
        compose.onNodeWithTag("graph-tools-toggle").performClick()
        compose.onNodeWithTag("graph-simulation-toggle").performClick()
        compose.onNodeWithText("VALUE TEST · manual events, not Fortnite gameplay").assertIsDisplayed()
        compose.onNodeWithTag("sim-actions-toggle").performClick()
        compose.onNodeWithTag("value-sim-choice").assertIsDisplayed()
        compose.onNodeWithTag("value-sim-inject").performClick()
        compose.onNodeWithTag("sim-run").performClick()
        compose.onNodeWithTag("sim-output").performClick()
        compose.onNodeWithText("Mutator Zone_1: no modeled state", substring = false).assertIsDisplayed()
        compose.onNodeWithTag("value-sim-summary").assertExists()
        compose.onNodeWithTag("value-sim-copy-report").assertExists()
        // The manually entered sequence can be repeated without touching persisted blueprint data.
        compose.onNodeWithTag("value-sim-replay").assertExists()
    }
}
