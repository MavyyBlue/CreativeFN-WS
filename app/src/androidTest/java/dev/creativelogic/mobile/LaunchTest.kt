package dev.creativelogic.mobile

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
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
            compose.onAllNodes(hasText("Logic canvas")).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Logic canvas").assertIsDisplayed()
        compose.onNodeWithText(draftName).assertIsDisplayed()
        compose.onNodeWithContentDescription("Back to home").performClick()
        compose.onNodeWithText("Library").performClick()
        compose.onNodeWithText("Search mechanics").performTextInput(draftName)
        compose.onNodeWithText("Search mechanics").performImeAction()
        val resultCard = hasText(draftName) and !hasSetTextAction()
        compose.onNodeWithTag("mechanic-library").performScrollToNode(resultCard)
        compose.onNode(resultCard).assertIsDisplayed().performClick()
        compose.onNodeWithText("Logic canvas").assertIsDisplayed()
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
