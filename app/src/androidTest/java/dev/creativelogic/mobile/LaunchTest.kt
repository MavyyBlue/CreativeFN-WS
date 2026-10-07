package dev.creativelogic.mobile

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class LaunchTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    @Test fun homeExplainsTheCurrentBuild() {
        compose.onNodeWithText("CREATIVE LOGIC").assertIsDisplayed()
        compose.onNodeWithText("New Mechanic").assertIsDisplayed()
    }
    @Test fun createMechanicOpensTheEditorShellAndReturnsToHome() {
        compose.onNodeWithText("New Mechanic").performClick()
        compose.waitUntil(timeoutMillis = 10_000) {
            compose.onAllNodes(androidx.compose.ui.test.hasText("Logic canvas"))
                .fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Logic canvas").assertIsDisplayed()
        compose.onNodeWithText("Back to home").performClick()
        compose.onNodeWithText("Recent Mechanics").assertIsDisplayed()
    }
}
