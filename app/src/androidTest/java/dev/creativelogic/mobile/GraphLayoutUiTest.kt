package dev.creativelogic.mobile

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test
import java.util.UUID

/** The graph stays full-height and actions must be available without persistent rows. */
class GraphLayoutUiTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun controlsAreCollapsedThenRestoreAndSimulationCanExit() {
        compose.onNodeWithText("New Mechanic").performClick()
        compose.onNodeWithText("Mechanic name").performTextInput("Canvas ${UUID.randomUUID().toString().take(8)}")
        compose.onNodeWithText("Create draft").performClick()
        compose.waitUntil(10_000) {
            compose.onAllNodes(hasTestTag("graph-tools-toggle")).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithTag("graph-devices").assertDoesNotExist()
        compose.onNodeWithTag("graph-bindings").assertDoesNotExist()
        compose.onNodeWithTag("graph-save-button").assertIsDisplayed()
        compose.onNodeWithTag("graph-health-check").assertIsDisplayed()
        compose.onNodeWithTag("graph-tools-toggle").performClick()
        compose.onNodeWithTag("graph-devices").assertIsDisplayed()
        compose.onNodeWithTag("graph-tools-toggle").performClick()
        compose.onNodeWithTag("graph-devices").assertDoesNotExist()
        compose.onNodeWithTag("graph-tools-toggle").performClick()
        compose.onNodeWithTag("graph-simulation-toggle").performClick()
        compose.onNodeWithTag("graph-simulation-toggle").assertTextEquals("Design")
        compose.onNodeWithTag("graph-simulation-toggle").performClick()
        compose.onNodeWithTag("graph-save-button").assertIsDisplayed()
    }

    @Test fun blueprintControlAppearsOnlyInsideLibrary() {
        compose.onNodeWithText("Blueprints").assertDoesNotExist()
        compose.onNodeWithText("Library").performClick()
        compose.onNodeWithTag("library-blueprints").assertIsDisplayed()
        compose.onNodeWithTag("library-blueprints").performClick()
        compose.onNodeWithText("Blueprint library").assertIsDisplayed()
        compose.onNodeWithText("Close").performClick()
    }
}
