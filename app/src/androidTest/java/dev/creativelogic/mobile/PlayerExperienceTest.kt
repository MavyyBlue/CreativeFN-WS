package dev.creativelogic.mobile

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test
import java.util.UUID

class PlayerExperienceTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun contextualInformationOpensAndClosesWithoutClutteringHome() {
        val explanation = "Start with a name for your idea. Add and configure devices, then connect their Events to Functions. Your saved mechanics appear in Recent and Library. Simulation is not available yet."
        compose.onNodeWithText(explanation).assertDoesNotExist()
        compose.onNodeWithContentDescription("Information: New mechanic").performClick()
        compose.onNodeWithText(explanation).assertIsDisplayed()
        compose.onNodeWithText("Close").performClick()
        compose.onNodeWithText(explanation).assertDoesNotExist()
        compose.onNodeWithText("Recent mechanics").assertIsNotDisplayed()
    }

    @Test fun bothBarsCanBeHiddenAndRestoredAcrossRecreation() {
        compose.onNodeWithContentDescription("View options").performClick()
        compose.onNodeWithText("Show header").performClick()
        compose.onNodeWithText("Show navigation").performClick()
        compose.onNodeWithText("Done").performClick()
        compose.onNodeWithText("CREATIVE LOGIC").assertDoesNotExist()
        compose.onNodeWithText("Library").assertDoesNotExist()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("CREATIVE LOGIC").assertDoesNotExist()
        compose.onNodeWithText("Library").assertDoesNotExist()
        compose.onNodeWithText("New Mechanic").assertIsDisplayed()
        compose.onNodeWithContentDescription("View options").performClick()
        compose.onNodeWithText("Show header").performClick()
        compose.onNodeWithText("Show navigation").performClick()
        compose.onNodeWithText("Done").performClick()
        compose.onNodeWithText("CREATIVE LOGIC").assertIsDisplayed()
        compose.onNodeWithText("Library").assertIsDisplayed()
    }

    @Test fun recentSidebarOpensSavedDraftAndClosesOnSelection() {
        val name = "Recent ${UUID.randomUUID().toString().take(8)}"
        compose.onNodeWithText("New Mechanic").performClick()
        compose.onNodeWithText("Mechanic name").performTextInput(name)
        compose.onNodeWithText("Create draft").performClick()
        compose.waitUntil(10_000) { compose.onAllNodes(hasText("Logic canvas")).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithContentDescription("Back to home").performClick()
        compose.waitUntil(10_000) { compose.onAllNodes(hasText("Recent")).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Recent").performClick()
        compose.onNodeWithText("Recent mechanics").assertIsDisplayed()
        compose.onNodeWithText("Find recent mechanic").performTextInput(name)
        val result = hasText(name) and !hasSetTextAction() and
            hasAnyAncestor(hasTestTag("recent-mechanics"))
        compose.onNodeWithTag("recent-mechanics").performScrollToNode(result)
        compose.onNode(result).performClick()
        compose.onNodeWithText("Logic canvas").assertIsDisplayed()
        compose.onNodeWithText("Recent mechanics").assertIsNotDisplayed()
    }

    @Test fun deviceReferenceShowsCatalogSettingsAndSemanticPorts() {
        compose.onNodeWithText("Learn").performClick()
        compose.onNodeWithText("Device Reference").performClick()
        compose.onNodeWithText("Tracker").performClick()
        compose.onNodeWithText("Target Value: 3").assertIsDisplayed()
        compose.onNodeWithText("On Complete").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Done").performClick()
    }
}
