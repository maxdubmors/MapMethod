package dev.stekl0.mapmethod

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationInstrumentedTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun homeFieldSurvivesRoundTripAndSecondFieldIsDisposedAfterBack() {
        composeRule.onNodeWithTag("home-test-field").performTextInput("home value")
        composeRule.onNodeWithTag("open-second-button").performClick()
        composeRule.onNodeWithText("Second screen").assertIsDisplayed()

        composeRule.onNodeWithTag("second-test-field").performTextInput("second value")
        composeRule.onNodeWithTag("second-back-button").performClick()
        composeRule.onNodeWithTag("home-test-field").assertTextEquals("home value")

        composeRule.onNodeWithTag("open-second-button").performClick()
        composeRule.onNodeWithTag("second-test-field").assertTextEquals("")
    }

    @Test
    fun systemBackReturnsToHome() {
        composeRule.onNodeWithTag("open-second-button").performClick()
        composeRule.activityRule.scenario.onActivity { activity ->
            activity.onBackPressedDispatcher.onBackPressed()
        }

        composeRule.onNodeWithText("Home screen").assertIsDisplayed()
    }

    @Test
    fun activityRecreationRestoresDestinationAndFields() {
        composeRule.onNodeWithTag("home-test-field").performTextInput("home value")
        composeRule.onNodeWithTag("open-second-button").performClick()
        composeRule.onNodeWithTag("second-test-field").performTextInput("second value")

        composeRule.activityRule.scenario.recreate()

        composeRule.onNodeWithText("Second screen").assertIsDisplayed()
        composeRule.onNodeWithTag("second-test-field").assertTextEquals("second value")
        composeRule.onNodeWithTag("second-back-button").performClick()
        composeRule.onNodeWithTag("home-test-field").assertTextEquals("home value")
    }

    @Test
    fun systemBackAtHomeLeavesTheActivity() {
        var isFinishing = false
        composeRule.activityRule.scenario.onActivity { activity ->
            activity.onBackPressedDispatcher.onBackPressed()
            isFinishing = activity.isFinishing
        }

        assertTrue(isFinishing)
    }
}
