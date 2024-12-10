package cz.frank.spacex

import android.content.Context
import androidx.paging.*
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import cz.frank.spacex.launches.data.database.dao.LaunchDao
import cz.frank.spacex.launches.data.database.entity.LaunchEntity
import cz.frank.spacex.launches.data.repository.ILaunchesFilterRepository
import cz.frank.spacex.launches.data.repository.LaunchesMediator
import cz.frank.spacex.launches.data.repository.toEntity
import cz.frank.spacex.main.data.SpaceXDatabase
import cz.frank.spacex.sharedtests.FakeLaunchAPI
import cz.frank.spacex.sharedtests.FakeRefreshDao
import cz.frank.spacex.sharedtests.FakeRemoteKeyDao
import cz.frank.spacex.sharedtests.LaunchFactory
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalPagingApi
@RunWith(AndroidJUnit4::class)
class LaunchMediatorUnitTest {
    private val mockFactory = LaunchFactory()
    private val mockApi = FakeLaunchAPI()

    private lateinit var db: SpaceXDatabase
    private lateinit var dao: LaunchDao


    private val filters = ILaunchesFilterRepository.Filters(
        isUpcomingSelected = true,
        isLaunchedSelected = true,
        rockets = setOf(),
        query = ""
    )


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
        val remoteMediator = launchesMediator(pageSize)
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
        val remoteMediator = launchesMediator(pageSize)
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
        val remoteMediator = launchesMediator(pageSize)
        val pagingState = PagingState<Int, LaunchEntity>(
            listOf(),
            null,
            PagingConfig(10),
            10
        )
        val result = remoteMediator.load(LoadType.REFRESH, pagingState)
        assertTrue(result is RemoteMediator.MediatorResult.Error)
    }

    @Test
    fun appendLoadReturnsErrorResultWhenErrorOccurs() = runTest {
        val pageSize = 10
        (0..<pageSize).forEach { id ->
            mockApi.addLaunches(mockFactory.createLaunch(id))
        }
        mockApi.failureMessage = FakeLaunchAPI.FailureMessage(10, "Throw refresh failure")
        val remoteMediator = launchesMediator(pageSize)
        val pagingState = PagingState<Int, LaunchEntity>(
            listOf(),
            null,
            PagingConfig(10),
            10
        )
        val result = remoteMediator.load(LoadType.REFRESH, pagingState)
        assertTrue(result is RemoteMediator.MediatorResult.Success)

        val resultAppend = remoteMediator.load(LoadType.APPEND, pagingState)
        assertTrue(resultAppend is RemoteMediator.MediatorResult.Success)
    }

    @Test
    fun firstAppendReturnsSuccessAndSignalsEndOfPaginationReached() = runTest {
        val pageSize = 10
        val launches = (0..<2*pageSize).map { id ->
            mockFactory.createLaunch(id)
        }
        for (launch in launches) {
            mockApi.addLaunches(launch)
        }
        val remoteMediator = launchesMediator(pageSize)

        // Refresh
        val pagingState = PagingState(
            listOf(PagingSource.LoadResult.Page(launches.take(10).map { it.toEntity() }, prevKey = null, nextKey = 1)),
            10,
            PagingConfig(10),
            10
        )
        val result = remoteMediator.load(LoadType.REFRESH, pagingState)
        assertTrue(result is RemoteMediator.MediatorResult.Success)


        // First append
        val resultAppend = remoteMediator.load(LoadType.APPEND, pagingState)
        assertTrue(resultAppend is RemoteMediator.MediatorResult.Success)
        assertTrue((resultAppend as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
    }


    @Test
    fun secondAppendSignalsEndOfPaginationReached() = runTest {
        val pageSize = 10
        val launches = (0..<3*pageSize).map { id ->
            mockFactory.createLaunch(id)
        }
        for (launch in launches) {
            mockApi.addLaunches(launch)
        }
        val remoteMediator = launchesMediator(pageSize)

        val pagingState = PagingState(
            listOf(PagingSource.LoadResult.Page(launches.take(10).map { it.toEntity() }, prevKey = null, nextKey = 1)),
            10,
            PagingConfig(10),
            10
        )
        val result = remoteMediator.load(LoadType.REFRESH, pagingState)
        assertTrue(result is RemoteMediator.MediatorResult.Success)


        // First append

        val resultAppend = remoteMediator.load(LoadType.APPEND, pagingState)
        assertTrue(resultAppend is RemoteMediator.MediatorResult.Success)
        assertTrue(!(resultAppend as RemoteMediator.MediatorResult.Success).endOfPaginationReached)

        // Second append
        val resultAppendSecond = remoteMediator.load(LoadType.APPEND, pagingState)
        assertTrue(resultAppendSecond is RemoteMediator.MediatorResult.Success)
        assertTrue((resultAppendSecond as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
    }

    private fun launchesMediator(pageSize: Int) = LaunchesMediator(
        db,
        dao,
        mockApi,
        FakeRemoteKeyDao(),
        FakeRefreshDao(),
        filters,
        pageSize,
        false
    )
}

