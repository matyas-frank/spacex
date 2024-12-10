package cz.frank.spacex

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
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

    @Test
    fun ensureAtLeastOneLaunchedVsUpcomingIsSelected() {
        composeTestRule.onNodeWithTag("NavigateToFilters").assertIsDisplayed().performClick()

        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.launches_filter_chip_upcoming)).assertIsSelectable().assertIsSelected().performClick().assertIsNotSelected()
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.launches_filter_chip_launched)).assertIsSelectable().assertIsSelected().performClick().assertIsSelected()
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.launches_filter_chip_upcoming)).assertIsSelectable().assertIsNotSelected().performClick().assertIsSelected()

        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.launches_filter_chip_launched)).assertIsSelectable().assertIsSelected().performClick().assertIsNotSelected()
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.launches_filter_chip_upcoming)).assertIsSelectable().assertIsSelected().performClick().assertIsSelected()
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.launches_filter_chip_launched)).assertIsSelectable().assertIsNotSelected().performClick().assertIsSelected()
    }
}