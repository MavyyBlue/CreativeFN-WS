package dev.creativelogic.mobile

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test
import java.util.UUID

class LaunchTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun homeExplainsTheCurrentBuild() {
        compose.onNodeWithText("CREATIVE LOGIC").assertIsDisplayed()
        compose.onNodeWithText("New Mechanic").assertIsDisplayed()
        compose.onNodeWithText("Home").assertIsDisplayed()
        compose.onNodeWithText("Library").assertIsDisplayed()
        compose.onNodeWithText("Learn").assertIsDisplayed()
    }

    @Test fun namedDraftPersistsThroughEditorAndLibrarySearch() {
        val draftName = "Vault UX ${UUID.randomUUID().toString().take(8)}"
        compose.onNodeWithText("New Mechanic").performClick()
        compose.onNodeWithText("Create draft").assertIsNotEnabled()
        compose.onNodeWithText("Mechanic name").performTextInput(draftName)
        compose.onNodeWithText("Create draft").performClick()
        compose.waitUntil(timeoutMillis = 10_000) {
            compose.onAllNodes(hasTestTag("graph-tools-toggle")).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithTag("graph-tools-toggle").assertIsDisplayed()
        compose.onNodeWithTag("editor-title").assertTextEquals(draftName).assertIsDisplayed()
        compose.onNodeWithContentDescription("Back to home").performClick()
        compose.onNodeWithText("Library").performClick()
        compose.onNodeWithText("Search mechanics").performTextInput(draftName)
        compose.onNodeWithText("Search mechanics").performImeAction()
        val resultCard = hasText(draftName) and !hasSetTextAction() and
            hasAnyAncestor(hasTestTag("mechanic-library"))
        compose.onNodeWithTag("mechanic-library").performScrollToNode(resultCard)
        compose.onNode(resultCard).assertIsDisplayed().performClick()
        compose.onNodeWithTag("graph-tools-toggle").assertIsDisplayed()
        compose.onNodeWithTag("editor-title").assertTextEquals(draftName).assertIsDisplayed()
    }

    @Test fun draftNameSurvivesActivityRecreation() {
        compose.onNodeWithText("New Mechanic").performClick()
        compose.onNodeWithText("Mechanic name").performTextInput("Persistent idea")
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("Persistent idea").assertIsDisplayed()
        compose.onNodeWithText("Cancel").performClick()
        compose.onNodeWithText("CREATIVE LOGIC").assertIsDisplayed()
    }

    @Test fun beginnerReferenceExplainsSemanticPorts() {
        compose.onNodeWithText("Learn").performClick()
        listOf("Events", "Functions", "Bindings").forEach { concept ->
            compose.onNodeWithTag("learn-concepts").performScrollToNode(hasText(concept))
            compose.onNodeWithText(concept).assertIsDisplayed()
        }
    }
}
