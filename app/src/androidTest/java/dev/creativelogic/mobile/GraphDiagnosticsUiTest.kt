package dev.creativelogic.mobile

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test
import java.util.UUID

/** Phase 5.1 health-check must remain reachable after consolidating graph tools. */
class GraphDiagnosticsUiTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun healthCheckAndOpenDeviceSupportBuilderRepairs() {
        compose.onNodeWithText("New Mechanic").performClick()
        compose.onNodeWithText("Mechanic name").performTextInput("Diagnostics ${UUID.randomUUID().toString().take(8)}")
        compose.onNodeWithText("Create draft").performClick()
        compose.waitUntil(10_000) {
            compose.onAllNodes(hasTestTag("graph-tools-toggle")).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithTag("graph-tools-toggle").performClick()
        compose.onNodeWithTag("graph-devices").performClick()
        compose.onNodeWithText("HUD Message").performClick()
        compose.onNodeWithTag("graph-health-check").performClick()
        compose.onNodeWithText("Mechanic health check").assertIsDisplayed()
        compose.onNodeWithText("0 errors · 0 warnings · local checks only").assertIsDisplayed()
        compose.onNodeWithText("Close").performClick()
        compose.onNodeWithTag("graph-tools-toggle").performClick()
        compose.onNodeWithTag("graph-devices").performClick()
        compose.onNodeWithText("Placed").performClick()
        compose.onNodeWithText("HUD Message_1").performClick()
        compose.onNodeWithText("Message").performScrollTo().performTextReplacement(" ")
        compose.onNodeWithText("Done").performScrollTo().performClick()
        compose.onNodeWithTag("graph-health-check").performClick()
        compose.onNodeWithText("WARNING: HUD message is empty").assertIsDisplayed()
        compose.onNodeWithTag("diagnostic-open-device").performScrollTo().performClick()
        compose.onNodeWithText("HUD Message").assertIsDisplayed()
    }
}
