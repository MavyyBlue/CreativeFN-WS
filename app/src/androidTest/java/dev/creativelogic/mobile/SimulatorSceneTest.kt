package dev.creativelogic.mobile

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test
import java.util.UUID

/** In-app Phase 3.2 sandbox smoke; NOT a Fortnite fidelity assertion. */
class SimulatorSceneTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun externalActionAndCollapsibleConsoleStayWithinSavedEditor() {
        compose.onNodeWithText("New Mechanic").performClick()
        compose.onNodeWithText("Mechanic name").performTextInput("Test Scene ${UUID.randomUUID().toString().take(6)}")
        compose.onNodeWithText("Create draft").performClick()
        compose.waitUntil(10_000) {
            compose.onAllNodes(hasTestTag("logic-canvas")).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithTag("graph-tools-toggle").performClick()
        compose.onNodeWithTag("graph-devices").performClick()
        compose.onNodeWithText("Button").performClick()
        compose.onNodeWithTag("logic-canvas").assert(hasStateDescription("1 devices, 0 bindings"))
        compose.onNodeWithTag("graph-tools-toggle").performClick()
        compose.onNodeWithTag("graph-simulation-toggle").performClick()
        compose.onNodeWithText("TEST SCENE · Not Fortnite-verified").assertIsDisplayed()
        compose.onNodeWithTag("sim-actions-toggle").performClick()
        compose.onNodeWithText("Interact · Button_1").performClick()
        compose.onNodeWithTag("sim-step").performClick()
        compose.onNodeWithTag("sim-run").performClick()
        compose.onNodeWithText("Output (3) ▼").performClick()
        compose.onNodeWithText("Button_1: enabled · 1 presses").assertIsDisplayed()
        compose.onNodeWithTag("sim-reset").performClick()
        compose.onNodeWithTag("graph-simulation-toggle").performClick()
        compose.onNodeWithTag("graph-tools-toggle").performClick()
        compose.onNodeWithTag("graph-devices").assertIsDisplayed()
        compose.onNodeWithTag("logic-canvas").assert(hasStateDescription("1 devices, 0 bindings"))
    }
}
