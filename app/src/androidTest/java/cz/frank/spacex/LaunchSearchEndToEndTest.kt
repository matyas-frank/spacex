package cz.frank.spacex

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import cz.frank.spacex.main.ui.MainActivity
import org.junit.Rule
import org.junit.Test

class LaunchSearchEndToEndTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun switchBetweenSections() {
        composeTestRule.onNodeWithTag("ToggleDrawerLaunches").assertIsDisplayed().performClick()
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.drawer_crew)).assertIsDisplayed().performClick()
        composeTestRule.onNodeWithTag("ToggleDrawerCrew").assertIsDisplayed().performClick()
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.drawer_launches)).assertIsDisplayed().performClick()
        composeTestRule.onNodeWithTag("ToggleDrawerLaunches").assertIsDisplayed()
    }
}