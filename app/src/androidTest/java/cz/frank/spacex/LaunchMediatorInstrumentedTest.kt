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

