package cz.frank.spacex.launches.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import cz.frank.spacex.launches.data.api.ILaunchesAPI
import cz.frank.spacex.launches.data.database.dao.IRefreshDao
import cz.frank.spacex.launches.data.database.dao.IRemoteKeyDao
import cz.frank.spacex.launches.data.database.dao.LaunchDao
import cz.frank.spacex.launches.data.database.entity.LaunchEntity
import cz.frank.spacex.main.data.SpaceXDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalPagingApi::class)
class LaunchesMediator(
    private val database: SpaceXDatabase,
    private val launchDao: LaunchDao,
    private val networkService: ILaunchesAPI,
    private val remoteKeyDao: IRemoteKeyDao,
    private val refreshDao: IRefreshDao,
    private val filters: ILaunchesFilterRepository.Filters,
    private val pageSize: Int,
) : RemoteMediator<Int, LaunchEntity>() {
    override suspend fun initialize(): InitializeAction {
        val timeOfLastRefreshInMillis = refreshDao.timeOfLastRefreshInMillis.first()
        return if (
            timeOfLastRefreshInMillis == null ||
            Clock.System.now().toEpochMilliseconds() >= timeOfLastRefreshInMillis + MAX_CACHED_TIME.inWholeMilliseconds
            ) InitializeAction.LAUNCH_INITIAL_REFRESH
        else InitializeAction.SKIP_INITIAL_REFRESH
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, LaunchEntity>
    ): MediatorResult {
        val loadKey = when (loadType) {
            LoadType.REFRESH -> remoteKeyDao.defaultPageToLoad
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> {
                val lastItem = state.lastItemOrNull()
                if (lastItem == null && state.anchorPosition == null) {
                    return MediatorResult.Success(endOfPaginationReached = false)
                }
                if (lastItem == null) {
                    return MediatorResult.Success(endOfPaginationReached = true)
                }
                remoteKeyDao.nextPageToLoad.first()
            }
        }
        val result = withContext(Dispatchers.IO) {
            networkService.allLaunches(filters, page = loadKey, pageSize = pageSize)
        }
        return result.fold(
            onSuccess = { response ->
                database.withTransaction {
                    if (loadType == LoadType.REFRESH) {
                        refreshDao.changeTimeOfLastUpdate(Clock.System.now().toEpochMilliseconds())
                        launchDao.deleteAllLaunches()
                    }
                    response.nextPage?.let {
                        remoteKeyDao.updateNextPage(response.nextPage)
                    }
                    launchDao.insertAllLaunches(*response.docs.map { it.toEntity() }.toTypedArray())
                }
                MediatorResult.Success(endOfPaginationReached = !response.hasNextPage)
            },
            onFailure = { MediatorResult.Error(it) }
        )
    }

    private companion object {
        val MAX_CACHED_TIME = 20.seconds
    }
}

fun ILaunchesAPI.LaunchPreviewResponse.toEntity() =
    LaunchEntity(id, name, links.patch.small, upcoming, success, rocket.name)
