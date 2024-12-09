package cz.frank.spacex

import android.content.Context
import androidx.paging.*
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import cz.frank.spacex.launches.data.RocketsDataSource
import cz.frank.spacex.launches.data.api.ILaunchesAPI
import cz.frank.spacex.launches.data.database.dao.IRefreshDao
import cz.frank.spacex.launches.data.database.dao.IRemoteKeyDao
import cz.frank.spacex.launches.data.database.dao.LaunchDao
import cz.frank.spacex.launches.data.database.entity.LaunchEntity
import cz.frank.spacex.launches.data.repository.ILaunchesFilterRepository
import cz.frank.spacex.launches.data.repository.LaunchesMediator
import cz.frank.spacex.main.data.SpaceXDatabase
import cz.frank.spacex.shared.data.PaginatedResponse
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalPagingApi
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class LaunchMediatorUnitTest {
    private val mockFactory = LaunchFactory()
    private val mockApi = FakeLaunchAPI()

    private lateinit var db: SpaceXDatabase
    private lateinit var dao: LaunchDao


    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, SpaceXDatabase::class.java).build()
        dao = db.launchesDao()
    }

    @After
    fun tearDown() {
        db.clearAllTables()
        mockApi.failureMessage = null
        mockApi.clearLaunches()
    }

    @Test
    fun refreshLoadReturnsSuccessResultWhenMoreDataIsPresent() = runTest {
        val pageSize = 10
        (0..pageSize).forEach { id ->
            mockApi.addLaunches(mockFactory.createLaunch(id))
        }


        val filters = ILaunchesFilterRepository.Filters(
            isUpcomingSelected = true,
            isLaunchedSelected = true,
            rockets = setOf(),
            query = ""
        )
        val remoteMediator = LaunchesMediator(
            db,
            dao,
            mockApi,
            FakeRemoteKeyDao(),
            FakeRefreshDao(),
            filters,
            pageSize
        )
        val pagingState = PagingState<Int, LaunchEntity>(
            listOf(),
            null,
            PagingConfig(pageSize),
            10
        )
        val result = remoteMediator.load(LoadType.REFRESH, pagingState)
        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertFalse((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
    }

    @Test
    fun refreshLoadSuccessAndEndOfPaginationWhenNoMoreData() = runTest {
        val pageSize = 10
        (0..<pageSize).forEach { id ->
            mockApi.addLaunches(mockFactory.createLaunch(id))
        }
        val filters = ILaunchesFilterRepository.Filters(
            isUpcomingSelected = true,
            isLaunchedSelected = true,
            rockets = setOf(),
            query = ""
        )
        val remoteMediator = LaunchesMediator(
            db,
            dao,
            mockApi,
            FakeRemoteKeyDao(),
            FakeRefreshDao(),
            filters,
            pageSize
        )
        val pagingState = PagingState<Int, LaunchEntity>(
            listOf(),
            null,
            PagingConfig(10),
            10
        )
        val result = remoteMediator.load(LoadType.REFRESH, pagingState)
        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertTrue((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
    }

    @Test
    fun refreshLoadReturnsErrorResultWhenErrorOccurs() = runTest {
        mockApi.failureMessage = FakeLaunchAPI.FailureMessage(0, "Throw refresh failure")
        val pageSize = 10
        val filters = ILaunchesFilterRepository.Filters(
            isUpcomingSelected = true,
            isLaunchedSelected = true,
            rockets = setOf(),
            query = ""
        )
        val remoteMediator = LaunchesMediator(
            db,
            dao,
            mockApi,
            FakeRemoteKeyDao(),
            FakeRefreshDao(),
            filters,
            pageSize
        )
        val pagingState = PagingState<Int, LaunchEntity>(
            listOf(),
            null,
            PagingConfig(10),
            10
        )
        val result = remoteMediator.load(LoadType.REFRESH, pagingState)
        assertTrue(result is RemoteMediator.MediatorResult.Error)
    }
}

class FakeLaunchAPI : ILaunchesAPI {
    var failureMessage: FailureMessage? = null

    private val launches: MutableList<ILaunchesAPI.LaunchPreviewResponse> = mutableListOf()

    fun addLaunches(launch: ILaunchesAPI.LaunchPreviewResponse) {
        this.launches.add(launch)
    }

    fun clearLaunches() {
        launches.clear()
    }

    override suspend fun allLaunches(
        filters: ILaunchesFilterRepository.Filters,
        page: Int,
        pageSize: Int
    ): Result<PaginatedResponse<ILaunchesAPI.LaunchPreviewResponse>> {

        val maxIndex = (page*pageSize)-1
        val sliceRange = (page - 1) * pageSize..maxIndex

        println("Page $page")
        println("Max index $maxIndex")
        println("Slice range $sliceRange")
        println("Launches size ${launches.count()}")

        failureMessage?.let {
            if (it.index in sliceRange) {
                return Result.failure(Exception(it.message))
            }
        }

        val filterRocketNames = filters.rockets.map { filterRocketId -> RocketsDataSource.rockets.find { it.id == filterRocketId }!! }.map { it.name }.toSet()

        val filtered = launches
            .filter { if (!filters.isUpcomingSelected) !it.upcoming else true }
            .filter { if (!filters.isLaunchedSelected) it.upcoming else true }
            .filter { if (filters.rockets.isNotEmpty()) it.rocket.name in filterRocketNames else true  }


        val hasNextPage = maxIndex + 1 < filtered.count()

        return Result.success(
            PaginatedResponse(
                docs = filtered.slice(sliceRange),
                page = page,
                hasNextPage = hasNextPage,
                nextPage = if (hasNextPage) page + 1 else null,
            )
        )
    }


    override suspend fun specificLaunch(id: String): Result<ILaunchesAPI.LaunchDetailResponse> {
        TODO("Not yet implemented")
    }

    data class FailureMessage(val index: Int, val message: String)
}

class FakeRemoteKeyDao: IRemoteKeyDao {
    override val defaultPageToLoad: Int = 1
    private var _nextPageToLoad = defaultPageToLoad
    override val nextPageToLoad: Flow<Int> = flow { _nextPageToLoad }

    override suspend fun updateNextPage(page: Int) {
        _nextPageToLoad = page
    }
}

class FakeRefreshDao: IRefreshDao {
    private var _timeOfLastRefreshInMillis: Long? = null

    override val timeOfLastRefreshInMillis: Flow<Long?> = flow { _timeOfLastRefreshInMillis }

    override suspend fun changeTimeOfLastUpdate(millis: Long?) {
        _timeOfLastRefreshInMillis = millis
    }
}

class LaunchFactory {
    fun createLaunch(id: Int): ILaunchesAPI.LaunchPreviewResponse {
        return ILaunchesAPI.LaunchPreviewResponse(
            id.toString(),
            "Name",
            links = ILaunchesAPI.LaunchPreviewResponse.Links(patch = ILaunchesAPI.LaunchPreviewResponse.Links.Patch(null)),
            rocket = ILaunchesAPI.LaunchPreviewResponse.Rocket("Falcon1"),
            upcoming = false,
            success = true
        )
    }
}