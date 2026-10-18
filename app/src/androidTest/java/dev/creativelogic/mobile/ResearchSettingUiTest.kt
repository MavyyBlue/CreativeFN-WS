package dev.creativelogic.mobile

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.semantics.SemanticsActions
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
        compose.onNodeWithTag("setting-Enabled-on-Phase").assertTextContains("Unspecified · choose setting")
        openPhasePicker()
        // Dialog choices are lazy too, especially on the 320 x 640 CI emulator.
        compose.onNodeWithTag("setting-choice-list").performScrollToNode(hasText("Pre-Game Only"))
        compose.onNodeWithText("Pre-Game Only").performClick()
        compose.onNodeWithTag("setting-Enabled-on-Phase").assertTextContains("Pre-Game Only")
        compose.onNodeWithText("Done").performClick()
        // Reopen the device to verify the selected reference value reached the graph.
        compose.onNodeWithTag("graph-tools-toggle").performClick()
        compose.onNodeWithTag("graph-devices").performClick()
        compose.onNodeWithText("Placed").performClick()
        compose.onNodeWithText("Mutator Zone_1").performClick()
        compose.onNodeWithTag("config-setting-search").performTextReplacement("Enabled on Phase")
        compose.onNodeWithTag("setting-Enabled-on-Phase").assertTextContains("Pre-Game Only")
        openPhasePicker()
        compose.onNodeWithTag("setting-choice-list").performScrollToNode(hasText("Not specified in blueprint"))
        compose.onNodeWithText("Not specified in blueprint").performClick()
        compose.onNodeWithTag("setting-Enabled-on-Phase").assertTextContains("Unspecified · choose setting")
        compose.onNodeWithText("Done").performClick()
    }

    private fun openPhasePicker() {
        // Scroll through the stable lazy container, not an item that scrolling can dispose.
        compose.onNodeWithTag("device-config-content")
            .performScrollToNode(hasTestTag("setting-Enabled-on-Phase"))
        // Invoke the real accessibility click after refetching the composed item. This test
        // exercises picker/save behavior; it is not a pixel-coordinate gesture test.
        compose.onNodeWithTag("setting-Enabled-on-Phase").assertIsDisplayed().assertHasClickAction()
            .performSemanticsAction(SemanticsActions.OnClick) { it() }
        compose.waitUntil(5_000) {
            compose.onAllNodes(hasTestTag("setting-choice-list")).fetchSemanticsNodes().isNotEmpty()
        }
    }
}
