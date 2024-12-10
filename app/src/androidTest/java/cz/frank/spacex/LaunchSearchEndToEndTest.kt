package cz.frank.spacex

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import cz.frank.spacex.launches.data.RocketsDataSource
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

    @Test
    fun ensureFilterRocketAllCheckboxWorks() {
        composeTestRule.onNodeWithTag("NavigateToFilters").assertIsDisplayed().performClick()
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.launches_filter_rockets)).assertIsDisplayed().performClick()
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.launches_filter_rockets)).assertIsDisplayed()

        val totalRocketsCount =  RocketsDataSource.rockets.count()

        composeTestRule.onAllNodesWithTag("RocketCheckbox", useUnmergedTree = true)
            .assertCountEquals(totalRocketsCount)
            .assertAll(isToggleable())
            .filter(isOn())
            .assertCountEquals(totalRocketsCount)

        composeTestRule.onNodeWithTag("AllCheckbox")
            .assertIsToggleable()
            .assertIsOn()
            .performClick()
            .assertIsOff()

        composeTestRule.onAllNodesWithTag("RocketCheckbox", useUnmergedTree = true)
            .assertCountEquals(totalRocketsCount)
            .assertAll(isToggleable())
            .apply { onFirst().assertIsOn() }
            .filter(isOn())
            .assertCountEquals(1)

        composeTestRule.onNodeWithTag("AllCheckbox")
            .assertIsToggleable()
            .assertIsOff()
            .performClick()
            .assertIsOn()

        composeTestRule.onAllNodesWithTag("RocketCheckbox", useUnmergedTree = true)
            .assertCountEquals(totalRocketsCount)
            .assertAll(isToggleable())
            .filter(isOn())
            .assertCountEquals(totalRocketsCount)
    }

    @Test
    fun ensureAtLeastOneRocketIsSelected() {
        composeTestRule.onNodeWithTag("NavigateToFilters").assertIsDisplayed().performClick()
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.launches_filter_rockets)).assertIsDisplayed().performClick()
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.launches_filter_rockets)).assertIsDisplayed()

        val totalRocketsCount =  RocketsDataSource.rockets.count()

        composeTestRule.onAllNodesWithTag("RocketCheckbox", useUnmergedTree = true)
            .assertCountEquals(totalRocketsCount)
            .assertAll(isToggleable())
            .filter(isOn())
            .assertCountEquals(totalRocketsCount)

        composeTestRule.onNodeWithTag("AllCheckbox")
            .assertIsToggleable()
            .assertIsOn()
            .performClick()
            .assertIsOff()


        composeTestRule.onAllNodesWithTag("RocketCheckbox", useUnmergedTree = true)
            .assertCountEquals(totalRocketsCount)
            .assertAll(isToggleable())
            .onFirst()
            .assertIsOn()
            .performClick()
            .assertIsOn()
    }

    @Test
    fun ensureRocketBadgeIsVisibleOnMainFilterScreen() {
        composeTestRule.onNodeWithTag("NavigateToFilters").assertIsDisplayed().performClick()

        composeTestRule.onNodeWithTag("RocketBadge", useUnmergedTree = true).assertIsNotDisplayed()

        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.launches_filter_rockets)).assertIsDisplayed().performClick()
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.launches_filter_rockets)).assertIsDisplayed()

        val totalRocketsCount =  RocketsDataSource.rockets.count()

        composeTestRule.onAllNodesWithTag("RocketCheckbox", useUnmergedTree = true)
            .assertCountEquals(totalRocketsCount)
            .assertAll(isToggleable())
            .filter(isOn())
            .assertCountEquals(totalRocketsCount)

        composeTestRule.onNodeWithTag("AllCheckbox")
            .assertIsToggleable()
            .assertIsOn()
            .performClick()
            .assertIsOff()

        composeTestRule.onNodeWithTag("NavigateBack").performClick()
        composeTestRule.onNodeWithTag("RocketBadge", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun ensureBadgeIsDisplayedOnMainLaunchSearchWhenAnyFilterIsActive() {
        composeTestRule.onNodeWithTag("AnyActiveFilterBadge", useUnmergedTree = true).assertIsNotDisplayed()
        composeTestRule.onNodeWithTag("NavigateToFilters").assertIsDisplayed().performClick()
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.launches_filter_chip_upcoming)).assertIsSelectable().assertIsSelected().performClick().assertIsNotSelected()
        composeTestRule.onNodeWithTag("NavigateBack").performClick()
        composeTestRule.onNodeWithTag("AnyActiveFilterBadge", useUnmergedTree = true).assertIsDisplayed()
    }
}