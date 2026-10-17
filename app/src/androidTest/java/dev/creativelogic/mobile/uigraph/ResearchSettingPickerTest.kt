package dev.creativelogic.mobile.uigraph

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** UI fixtures exercise selector behavior, not Fortnite menu fidelity. */
class ResearchSettingPickerTest {
    @get:Rule val compose = createComposeRule()

    @Test fun longFiniteMenuCanSearchSelectAndClearWithoutInventingCustomValues() {
        val stored = mutableStateOf("")
        compose.setContent {
            MaterialTheme {
                ReferenceSettingRow("Fixture choice", stored.value,
                    ResearchSettingChoices((1..12).map { "Choice $it" }, "")) { stored.value = it }
            }
        }
        compose.onNodeWithTag("setting-Fixture-choice").performClick()
        compose.onNodeWithTag("setting-choice-search").performTextInput("Unknown choice")
        compose.onNodeWithTag("setting-choice-list").performScrollToNode(hasText("No matching documented choice"))
        compose.onNodeWithText("No matching documented choice").assertIsDisplayed()
        compose.runOnIdle { assertEquals("", stored.value) }
        compose.onNodeWithTag("setting-choice-search").performTextReplacement("Choice 12")
        compose.onNodeWithTag("setting-choice-list").performScrollToNode(hasText("Choice 12"))
        compose.onNodeWithText("Device text setting").assertDoesNotExist()
        compose.onNode(hasText("Choice 12", substring = false) and hasClickAction() and !hasSetTextAction()).performClick()
        compose.runOnIdle { assertEquals("Choice 12", stored.value) }
        compose.onNodeWithTag("setting-Fixture-choice").assertTextContains("Choice 12")
        compose.onNodeWithTag("setting-Fixture-choice").performClick()
        // Opening again clears the search, and clearing a blueprint value is always available.
        compose.onNodeWithTag("setting-choice-search").assert(hasText("", substring = false))
        compose.onNodeWithText("Not specified in blueprint").performClick()
        compose.runOnIdle { assertEquals("", stored.value) }
        compose.onNodeWithTag("setting-Fixture-choice").assertTextContains("Unspecified · choose setting")
    }
}
