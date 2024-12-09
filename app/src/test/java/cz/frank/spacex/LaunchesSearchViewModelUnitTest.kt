package cz.frank.spacex

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.testing.asPagingSourceFactory
import androidx.paging.testing.asSnapshot
import cz.frank.spacex.launches.data.RocketsDataSource
import cz.frank.spacex.launches.data.api.ILaunchesAPI
import cz.frank.spacex.launches.data.repository.ILaunchesFilterRepository
import cz.frank.spacex.launches.data.repository.ILaunchesRepository
import cz.frank.spacex.launches.data.repository.toEntity
import cz.frank.spacex.launches.data.repository.toModel
import cz.frank.spacex.launches.domain.model.LaunchDetailModel
import cz.frank.spacex.launches.domain.model.LaunchPreviewModel
import cz.frank.spacex.launches.ui.search.LaunchSearchViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Test
import kotlin.random.Random
import kotlin.test.assertContentEquals
import kotlin.test.assertTrue

class LaunchesSearchViewModelTest {
    private lateinit var filterRepo: FakeLaunchesFilterRepository
    private lateinit var launchFactory: LaunchFactory
    private lateinit var vm: LaunchSearchViewModel

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup() {
        filterRepo = FakeLaunchesFilterRepository()
        launchFactory = LaunchFactory()
        Dispatchers.setMain(Dispatchers.Default)
        vm = LaunchSearchViewModel(
            filterRepo,
            FakeLaunchesRepository(launchFactory)
        )
    }

    @Test
    fun scrollTest(): Unit = runTest {
        launchFactory.createLaunches(0..100)
        val items = vm.pager
        val snapshot = items.asSnapshot {
            scrollTo(100)
        }
        assertContentEquals(
            expected = launchFactory.launches,
            actual = snapshot
        )
    }

    @Test
    fun upcomingOnly(): Unit = runTest {
        launchFactory.createLaunches(0..100)
        val items = vm.pager
        filterRepo.toggleLaunchedSelected()
        val snapshot = items.asSnapshot {
            scrollTo(100)
        }

        assertTrue { snapshot.all { it.state is LaunchPreviewModel.State.Upcoming } }
    }

    @Test
    fun launchedOnly(): Unit = runTest {
        launchFactory.createLaunches(0..100)
        val items = vm.pager
        filterRepo.toggleUpcomingSelected()
        val snapshot = items.asSnapshot {
            scrollTo(100)
        }
        assertTrue { snapshot.all { it.state is LaunchPreviewModel.State.Launched } }
    }

    @Test
    fun queryOnly(): Unit = runTest {
        launchFactory.createLaunches(0..100)
        val items = vm.pager
        val query = "FalconSat"
        filterRepo.setQuery(query)
        val snapshot = items.asSnapshot {
            scrollTo(100)
        }
        assertTrue { snapshot.all { it.title.contains(query)} }
    }

    @Test
    fun rocketsOnly(): Unit = runTest {
        launchFactory.createLaunches(0..100)
        val items = vm.pager
        val rocketIds = setOf(RocketsDataSource.rockets.first().id)
        filterRepo.changeRockets(rocketIds)
        val filterRocketNames = rocketIds.map { filterRocketId ->
            RocketsDataSource.rockets.find { it.id == filterRocketId }!!
        }.map { it.name }.toSet()
        val snapshot = items.asSnapshot {
            scrollTo(100)
        }
        assertTrue { snapshot.all { it.rocket in filterRocketNames } }
    }

    @Test
    fun queryOnlyAndLaunched(): Unit = runTest {
        launchFactory.createLaunches(0..100)
        val items = vm.pager
        val query = "FalconSat"
        filterRepo.setQuery(query)
        filterRepo.toggleUpcomingSelected()
        val snapshot = items.asSnapshot {
            scrollTo(100)
        }
        assertTrue { snapshot.all { it.title.contains(query) && it.state is LaunchPreviewModel.State.Launched} }
    }

    @Test
    fun queryOnlyAndUpcoming(): Unit = runTest {
        launchFactory.createLaunches(0..100)
        val items = vm.pager
        val query = "FalconSat2"
        filterRepo.setQuery(query)
        filterRepo.toggleLaunchedSelected()
        val snapshot = items.asSnapshot {
            scrollTo(100)
        }
        assertTrue { snapshot.all { it.title.contains(query) && it.state is LaunchPreviewModel.State.Upcoming } }
    }

    @Test
    fun queryOnlyAndRockets(): Unit = runTest {
        launchFactory.createLaunches(0..100)
        val items = vm.pager
        val query = "FalconSat2"
        filterRepo.setQuery(query)
        val rocketIds = setOf(RocketsDataSource.rockets.first().id)
        filterRepo.changeRockets(rocketIds)
        val filterRocketNames = rocketIds.map { filterRocketId ->
            RocketsDataSource.rockets.find { it.id == filterRocketId }!!
        }.map { it.name }.toSet()
        val snapshot = items.asSnapshot {
            scrollTo(100)
        }
        assertTrue { snapshot.all { it.title.contains(query) && it.rocket in filterRocketNames } }
    }

    @Test
    fun queryAndRocketsAndUpcoming(): Unit = runTest {
        launchFactory.createLaunches(0..100)
        val items = vm.pager
        val query = "FalconSat2"
        filterRepo.setQuery(query)
        val rocketIds = setOf(RocketsDataSource.rockets.first().id)
        filterRepo.changeRockets(rocketIds)
        filterRepo.toggleLaunchedSelected()
        val filterRocketNames = rocketIds.map { filterRocketId ->
            RocketsDataSource.rockets.find { it.id == filterRocketId }!!
        }.map { it.name }.toSet()
        val snapshot = items.asSnapshot {
            scrollTo(100)
        }
        assertTrue { snapshot.all {
            it.title.contains(query) && it.rocket in filterRocketNames && it.state is LaunchPreviewModel.State.Upcoming
        } }
    }

    @Test
    fun queryAndRocketsAndLaunched(): Unit = runTest {
        launchFactory.createLaunches(0..100)
        val items = vm.pager
        val query = "FalconSat2"
        filterRepo.setQuery(query)
        val rocketIds = setOf(RocketsDataSource.rockets.first().id)
        filterRepo.changeRockets(rocketIds)
        filterRepo.toggleUpcomingSelected()
        val filterRocketNames = rocketIds.map { filterRocketId ->
            RocketsDataSource.rockets.find { it.id == filterRocketId }!!
        }.map { it.name }.toSet()
        val snapshot = items.asSnapshot {
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
}

class FakeLaunchesFilterRepository : ILaunchesFilterRepository {
    private val _query =  MutableStateFlow("")
    override val query: Flow<String> = _query

    private val _isLaunchedSelected = MutableStateFlow(true)
    override val isLaunchedSelected: Flow<Boolean> = _isLaunchedSelected

    private val _isUpcomingSelected = MutableStateFlow(true)
    override val isUpcomingSelected: Flow<Boolean> = _isUpcomingSelected

    private val selectedRocketsId = MutableStateFlow<Set<String>>(setOf())

    private val _rocketsCount = selectedRocketsId.map { it.count() }
    override val rocketsCount: Flow<Int> = _rocketsCount

    fun changeRockets(rockets: Set<String>) {
        selectedRocketsId.value = rockets
    }

    override val isAnyFilterActive: Flow<Boolean> = combine(
        isLaunchedSelected,
        isUpcomingSelected,
        selectedRocketsId
    ) { launched, upcoming, rockets ->
        !launched || !upcoming || rockets.isNotEmpty()
    }

    override val allFilters: Flow<ILaunchesFilterRepository.Filters> = combine(
        isUpcomingSelected,
        isLaunchedSelected,
        selectedRocketsId,
        query,
    ) { upcoming, launched, selectedRockets, query ->
        ILaunchesFilterRepository.Filters(upcoming, launched, selectedRockets, query)
    }

    override suspend fun setQuery(query: String) {
        _query.value = query
    }

    override suspend fun toggleLaunchedSelected() {
        _isLaunchedSelected.update { !it }
    }

    override suspend fun toggleUpcomingSelected() {
        _isUpcomingSelected.update { !it }
    }
}


class LaunchFactory {
    val launches = mutableListOf<LaunchPreviewModel>()

    fun createLaunches(intRange: IntRange) = launches.addAll(intRange.map {
        createLaunch(it).toEntity().toModel()
    })

    private fun createLaunch(id: Int): ILaunchesAPI.LaunchPreviewResponse {
        return ILaunchesAPI.LaunchPreviewResponse(
            id.toString(),
            listOf("FalconSat", "FalconSat2").random(),
            links = ILaunchesAPI.LaunchPreviewResponse.Links(
                patch = ILaunchesAPI.LaunchPreviewResponse.Links.Patch(null)
            ),
            rocket = ILaunchesAPI.LaunchPreviewResponse.Rocket(RocketsDataSource.rockets.random().name),
            upcoming = Random.nextBoolean(),
            success = Random.nextBoolean()
        )
    }
}

class FakeLaunchesRepository(private val launchFactory: LaunchFactory) : ILaunchesRepository {

    override fun pager(filters: ILaunchesFilterRepository.Filters): Flow<PagingData<LaunchPreviewModel>> {
        val filterRocketNames = filters.rockets.map { filterRocketId ->
            RocketsDataSource.rockets.find { it.id == filterRocketId }!!
        }.map { it.name }.toSet()

        val filtered = launchFactory.launches
            .filter { if (!filters.isUpcomingSelected) it.state !is LaunchPreviewModel.State.Upcoming  else true }
            .filter { if (!filters.isLaunchedSelected) it.state is LaunchPreviewModel.State.Upcoming else true }
            .filter { if (filters.rockets.isNotEmpty()) it.rocket in filterRocketNames else true  }
            .filter { if (filters.query.isNotBlank()) it.title.contains(filters.query) else true }


        return Pager(PagingConfig(10), pagingSourceFactory = filtered.asPagingSourceFactory()).flow
    }

    override suspend fun detailLaunch(id: String): Result<LaunchDetailModel> {
        TODO("Not yet implemented")
    }
}
