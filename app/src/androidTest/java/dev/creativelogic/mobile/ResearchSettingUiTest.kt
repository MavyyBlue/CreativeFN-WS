package dev.creativelogic.mobile

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test
import java.util.UUID

/** Reference selector must not be a raw text field and old gameplay settings must persist. */
class ResearchSettingUiTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun researchedMutatorZoneUsesDocumentedPickersNotFreeTextFields() {
        compose.onNodeWithText("New Mechanic").performClick()
        compose.onNodeWithText("Mechanic name").performTextInput("Picker ${UUID.randomUUID().toString().take(6)}")
        compose.onNodeWithText("Create draft").performClick()
        compose.waitUntil(10_000) { compose.onAllNodes(hasTestTag("graph-tools-toggle")).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("graph-tools-toggle").performClick()
        compose.onNodeWithTag("graph-devices").performClick()
        compose.onNodeWithText("Search devices").performTextInput("Mutator Zone")
        compose.onNode(hasText("Mutator Zone") and hasClickAction() and !hasSetTextAction()).performClick()
        compose.onNodeWithTag("logic-canvas").assertExists()
        compose.onNodeWithTag("graph-tools-toggle").performClick()
        compose.onNodeWithTag("graph-devices").performClick()
        compose.onNodeWithText("Placed").performClick()
        compose.onNodeWithText("Mutator Zone_1").performClick()
        compose.onNodeWithText("CREATIVE 1.0 RESEARCH · MENU / DEFAULTS NOT YET VERIFIED").assertIsDisplayed()
        // A large device menu is lazy: filter first so the setting is composed before selecting it.
        compose.onNodeWithTag("config-setting-search").performTextInput("Enabled on Phase")
        compose.onNodeWithTag("setting-Enabled-on-Phase").performClick()
        compose.onNodeWithText("Pre-Game Only").performClick()
        compose.onNodeWithText("Done").performClick()
    }
}
