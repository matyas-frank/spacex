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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.random.Random
import kotlin.test.assertContentEquals
import kotlin.test.assertTrue

@Suppress("OPT_IN_USAGE")
class LaunchesSearchViewModelUnitTest {
    private val filterRepo: FakeLaunchesFilterRepository = FakeLaunchesFilterRepository()
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
        filterRepo.changeRockets(rocketIds)
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
        filterRepo.changeRockets(rocketIds)
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
        filterRepo.changeRockets(rocketIds)
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
        filterRepo.changeRockets(rocketIds)
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
        filterRepo.clearFilters()
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

    fun clearFilters() {
        selectedRocketsId.value = setOf()
        _query.value = ""
        _isLaunchedSelected.value = true
        _isUpcomingSelected.value = true
    }
}


class LaunchFactory {
    fun createLaunches(intRange: IntRange) = intRange.map {
        createLaunch(it).toEntity().toModel()
    }

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

class FakeLaunchesRepository : ILaunchesRepository {

    var launches = listOf<LaunchPreviewModel>()
        private set

    fun setLaunches(launches: List<LaunchPreviewModel>) { this.launches = launches  }
    fun clearLaunches() { this.launches = listOf() }

    override fun pager(filters: ILaunchesFilterRepository.Filters): Flow<PagingData<LaunchPreviewModel>> {
        val filterRocketNames = filters.rockets.map { filterRocketId ->
            RocketsDataSource.rockets.find { it.id == filterRocketId }!!
        }.map { it.name }.toSet()

        val filtered = launches
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
