package dev.creativelogic.mobile

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test
import java.util.UUID

/** A new Creative documentation reference can enter the manual value scenario path. */
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
        compose.onNodeWithText("Mutator Zone", substring = false).performClick()
        compose.onNodeWithTag("graph-tools-toggle").performClick()
        compose.onNodeWithTag("graph-simulation-toggle").performClick()
        compose.onNodeWithText("VALUE TEST · manual events, not Fortnite gameplay").assertIsDisplayed()
        compose.onNodeWithTag("sim-actions-toggle").performClick()
        compose.onNodeWithTag("value-sim-choice").assertIsDisplayed()
        compose.onNodeWithTag("value-sim-inject").performClick()
        compose.onNodeWithTag("sim-run").performClick()
        compose.onNodeWithTag("sim-output").performClick()
        compose.onNodeWithText("Mutator Zone_1: no modeled state",substring=false).assertIsDisplayed()
    }
}
