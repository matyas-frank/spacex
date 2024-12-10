package cz.frank.spacex
import androidx.paging.testing.asSnapshot
import cz.frank.spacex.launches.data.RocketsDataSource
import cz.frank.spacex.launches.data.repository.LaunchesFilterRepository
import cz.frank.spacex.launches.domain.model.LaunchPreviewModel
import cz.frank.spacex.launches.ui.search.LaunchSearchViewModel
import cz.frank.spacex.sharedtests.FakeLaunchesFilterDao
import cz.frank.spacex.sharedtests.FakeLaunchesRepository
import cz.frank.spacex.sharedtests.FakeRefreshDao
import cz.frank.spacex.sharedtests.LaunchFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertTrue

@Suppress("OPT_IN_USAGE")
class LaunchesSearchViewModelUnitTest {
    private val filterDao = FakeLaunchesFilterDao()
    private val filterRepo: LaunchesFilterRepository = LaunchesFilterRepository(
        filterDao = filterDao,
        refreshDao = FakeRefreshDao()
    )
    private val launchFactory: LaunchFactory = LaunchFactory()
    private val launchesRepository = FakeLaunchesRepository()
    private lateinit var vm: LaunchSearchViewModel

    @Before
    fun vmSetup() {
        Dispatchers.setMain(Dispatchers.Default)
        vm = LaunchSearchViewModel(
            filterRepo,
            launchesRepository
        )
    }

    @Test
    fun scrollTest(): Unit = runTest {
        val preload = 20
        val totalItems = 200
        launchesRepository.setLaunches(launchFactory.createLaunches(0..<totalItems))
        val snapshot = vm.pager.asSnapshot {
            scrollTo(totalItems - preload)
        }
        assertContentEquals(
            expected = launchesRepository.launches,
            actual = snapshot
        )
    }

    @Test
    fun upcomingOnly(): Unit = runTest {
        launchesRepository.setLaunches(launchFactory.createLaunches(0..100))
        filterRepo.toggleLaunchedSelected()
        val snapshot = vm.pager.asSnapshot {
            scrollTo(100)
        }

        assertTrue { snapshot.all { it.state is LaunchPreviewModel.State.Upcoming } }
    }

    @Test
    fun launchedOnly(): Unit = runTest {
        launchesRepository.setLaunches(launchFactory.createLaunches(0..100))
        filterRepo.toggleUpcomingSelected()
        val snapshot = vm.pager.asSnapshot {
            scrollTo(100)
        }
        assertTrue { snapshot.all { it.state is LaunchPreviewModel.State.Launched } }
    }

    @Test
    fun queryOnly(): Unit = runTest {
        launchesRepository.setLaunches(launchFactory.createLaunches(0..100))
        val query = "FalconSat"
        filterRepo.setQuery(query)
        val snapshot = vm.pager.asSnapshot {
            scrollTo(100)
        }
        assertTrue { snapshot.all { it.title.contains(query)} }
    }

    @Test
    fun rocketsOnly(): Unit = runTest {
        launchesRepository.setLaunches(launchFactory.createLaunches(0..100))
        val rocketIds = setOf(RocketsDataSource.rockets.first().id)
        filterDao.setRocketsIds(rocketIds)
        val filterRocketNames = RocketsDataSource.rockets
            .filter { it.id in rocketIds }
            .map { it.name }
            .toSet()
        val snapshot = vm.pager.asSnapshot {
            scrollTo(100)
        }
        assertTrue { snapshot.all { it.rocket in filterRocketNames } }
    }

    @Test
    fun queryOnlyAndLaunched(): Unit = runTest {
        launchesRepository.setLaunches(launchFactory.createLaunches(0..100))
        val query = "FalconSat"
        filterRepo.setQuery(query)
        filterRepo.toggleUpcomingSelected()
        val snapshot = vm.pager.asSnapshot {
            scrollTo(100)
        }
        assertTrue { snapshot.all { it.title.contains(query) && it.state is LaunchPreviewModel.State.Launched} }
    }

    @Test
    fun queryOnlyAndUpcoming(): Unit = runTest {
        launchesRepository.setLaunches(launchFactory.createLaunches(0..100))
        val query = "FalconSat2"
        filterRepo.setQuery(query)
        filterRepo.toggleLaunchedSelected()
        val snapshot = vm.pager.asSnapshot {
            scrollTo(100)
        }
        assertTrue { snapshot.all { it.title.contains(query) && it.state is LaunchPreviewModel.State.Upcoming } }
    }

    @Test
    fun queryOnlyAndRockets(): Unit = runTest {
        launchesRepository.setLaunches(launchFactory.createLaunches(0..100))
        val query = "FalconSat2"
        filterRepo.setQuery(query)
        val rocketIds = setOf(RocketsDataSource.rockets.first().id)
        filterDao.setRocketsIds(rocketIds)
        val filterRocketNames = RocketsDataSource.rockets
            .filter { it.id in rocketIds }
            .map { it.name }
            .toSet()
        val snapshot = vm.pager.asSnapshot {
            scrollTo(100)
        }
        assertTrue { snapshot.all { it.title.contains(query) && it.rocket in filterRocketNames } }
    }

    @Test
    fun queryAndRocketsAndUpcoming(): Unit = runTest {
        launchesRepository.setLaunches(launchFactory.createLaunches(0..100))
        val query = "FalconSat2"
        filterRepo.setQuery(query)
        filterRepo.toggleLaunchedSelected()
        val rocketIds = setOf(RocketsDataSource.rockets.first().id)
        filterDao.setRocketsIds(rocketIds)
        val filterRocketNames = RocketsDataSource.rockets
            .filter { it.id in rocketIds }
            .map { it.name }
            .toSet()
        val snapshot = vm.pager.asSnapshot {
            scrollTo(100)
        }
        assertTrue { snapshot.all {
            it.title.contains(query) && it.rocket in filterRocketNames && it.state is LaunchPreviewModel.State.Upcoming
        } }
    }

    @Test
    fun queryAndRocketsAndLaunched(): Unit = runTest {
        launchesRepository.setLaunches(launchFactory.createLaunches(0..100))
        val query = "FalconSat2"
        filterRepo.setQuery(query)
        filterRepo.toggleUpcomingSelected()
        val rocketIds = setOf(RocketsDataSource.rockets.first().id)
        filterDao.setRocketsIds(rocketIds)
        val filterRocketNames = RocketsDataSource.rockets
            .filter { it.id in rocketIds }
            .map { it.name }
            .toSet()
        val snapshot = vm.pager.asSnapshot {
            scrollTo(100)
        }
        assertTrue {
            snapshot.all {
                it.title.contains(query)
                        && it.rocket in filterRocketNames
                        && it.state is LaunchPreviewModel.State.Launched
            }
        }
    }

    @After
    fun tearDown() {
        launchesRepository.clearLaunches()
        filterDao.clearFilters()
    }
}


