package cz.frank.spacex

import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import cz.frank.spacex.crew.data.CrewRepository
import cz.frank.spacex.crew.data.ICrewApi
import cz.frank.spacex.crew.ui.search.CrewSearchViewModel
import cz.frank.spacex.launches.data.RocketsDataSource
import cz.frank.spacex.launches.data.database.dao.ILaunchesFilterDao
import cz.frank.spacex.launches.data.database.dao.IRefreshDao
import cz.frank.spacex.launches.data.database.dao.IRemoteKeyDao
import cz.frank.spacex.launches.data.repository.*
import cz.frank.spacex.launches.ui.filter.LaunchFilterViewModel
import cz.frank.spacex.launches.ui.filter.rocket.LaunchFilterRocketViewModel
import cz.frank.spacex.launches.ui.search.LaunchSearchViewModel
import cz.frank.spacex.main.data.SpaceXDatabase
import cz.frank.spacex.main.di.spaceXModule
import cz.frank.spacex.main.ui.MainActivity
import cz.frank.spacex.sharedtests.*
import org.junit.Rule
import org.junit.Test
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

class SpaceXAppUITest {
    private val testModule = module {
        single {
            val context = ApplicationProvider.getApplicationContext<Context>()
            Room.inMemoryDatabaseBuilder(context, SpaceXDatabase::class.java).build()
        }
        single { get<SpaceXDatabase>().launchesDao() }
        singleOf(::FakeRemoteKeyDao) bind IRemoteKeyDao::class
        singleOf(::FakeRefreshDao) bind IRefreshDao::class
        singleOf(::FakeLaunchesFilterDao) bind ILaunchesFilterDao::class

        singleOf(::FakeLaunchesRepository) bind ILaunchesRepository::class
        singleOf(::LaunchesFilterRocketRepository) bind ILaunchesFilterRocketRepository::class
        singleOf(::LaunchesFilterRepository) bind ILaunchesFilterRepository::class
        factory { (filters: ILaunchesFilterRepository.Filters, pageSize: Int, forceRefresh: Boolean) ->
            LaunchesMediator(get(), get(), get(), get(), get(), filters, pageSize, forceRefresh)
        }
        viewModelOf(::LaunchSearchViewModel)
        viewModelOf(::LaunchFilterViewModel)
        viewModelOf(::LaunchFilterRocketViewModel)

        singleOf(::FakeCrewApi) bind ICrewApi::class
        singleOf(::CrewRepository)

        viewModelOf(::CrewSearchViewModel)
    }

    init {
        unloadKoinModules(spaceXModule)
        loadKoinModules(testModule)
    }

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
