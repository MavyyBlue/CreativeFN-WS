package dev.creativelogic.mobile

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test

class LaunchTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun homeExplainsTheCurrentBuild() {
        compose.onNodeWithText("CREATIVE LOGIC").assertIsDisplayed()
        compose.onNodeWithText("New Mechanic").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Home").assertIsDisplayed()
        compose.onNodeWithText("Library").assertIsDisplayed()
        compose.onNodeWithText("Learn").assertIsDisplayed()
    }

    @Test fun namedDraftPersistsThroughEditorAndLibrarySearch() {
        compose.onNodeWithText("New Mechanic").performScrollTo().performClick()
        compose.onNodeWithText("Create draft").assertIsNotEnabled()
        compose.onNodeWithText("Mechanic name").performTextInput("Vault UX acceptance")
        compose.onNodeWithText("Create draft").performClick()
        compose.waitUntil(timeoutMillis = 10_000) {
            compose.onAllNodes(hasText("Logic canvas")).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Logic canvas").assertIsDisplayed()
        compose.onNodeWithText("Vault UX acceptance").assertIsDisplayed()
        compose.onNodeWithContentDescription("Back to home").performClick()
        compose.onNodeWithText("Library").performClick()
        compose.onNodeWithText("Search mechanics").performTextInput("Vault UX acceptance")
        compose.onNodeWithText("Vault UX acceptance").performScrollTo().performClick()
        compose.onNodeWithText("Logic canvas").assertIsDisplayed()
    }

    @Test fun draftNameSurvivesActivityRecreation() {
        compose.onNodeWithText("New Mechanic").performScrollTo().performClick()
        compose.onNodeWithText("Mechanic name").performTextInput("Persistent idea")
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("Persistent idea").assertIsDisplayed()
        compose.onNodeWithText("Cancel").performClick()
        compose.onNodeWithText("CREATIVE LOGIC").assertIsDisplayed()
    }

    @Test fun beginnerReferenceExplainsSemanticPorts() {
        compose.onNodeWithText("Learn").performClick()
        compose.onNodeWithText("Events").assertIsDisplayed()
        compose.onNodeWithText("Functions").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Bindings").performScrollTo().assertIsDisplayed()
    }
}
