package cz.frank.spacex.launches.data.repository

import androidx.paging.*
import cz.frank.spacex.launches.data.api.ILaunchesAPI
import cz.frank.spacex.launches.data.database.dao.LaunchDao
import cz.frank.spacex.launches.data.database.entity.LaunchEntity
import cz.frank.spacex.launches.domain.model.LaunchDetailModel
import cz.frank.spacex.launches.domain.model.LaunchPreviewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

interface ILaunchesRepository {
    fun pager(filters: ILaunchesFilterRepository.Filters, forceRefresh: Boolean): Flow<PagingData<LaunchPreviewModel>>
    suspend fun detailLaunch(id: String): Result<LaunchDetailModel>
}

class LaunchesRepository(
    private val launchDao: LaunchDao,
    private val launchAPI: ILaunchesAPI,
 ) : KoinComponent, ILaunchesRepository {
    @OptIn(ExperimentalPagingApi::class)
    override fun pager(filters: ILaunchesFilterRepository.Filters, forceRefresh: Boolean) = Pager(
        config = PagingConfig(pageSize = PAGE_SIZE),
        pagingSourceFactory = { launchDao.getAllLaunches() },
        remoteMediator = inject<LaunchesMediator> { parametersOf(filters, PAGE_SIZE, forceRefresh) }.value
    ).flow.map { items ->
        items.map { it.toModel() }
    }

    override suspend fun detailLaunch(id: String) = withContext(Dispatchers.IO) {
        launchAPI.specificLaunch(id).mapCatching { it.toModel() }
    }
}

fun LaunchEntity.toModel() = LaunchPreviewModel(
    id,
    name,
    image,
    rocket,
    if (upcoming) LaunchPreviewModel.State.Upcoming
    else LaunchPreviewModel.State.Launched(success)
)

fun ILaunchesAPI.LaunchDetailResponse.toModel() = LaunchDetailModel(
    id,
    flightNumber,
    name,
    if (upcoming) LaunchDetailModel.State.Upcoming else LaunchDetailModel.State.Launched(success),
    fairings.recovered,
    launchpad?.let { LaunchDetailModel.Launchpad(it.name) },
    LaunchDetailModel.Rocket(rocket.name),
    details,
    date * MILLISECONDS_IN_SECOND,
    links.youtubeId,
    links.article
)

private const val MILLISECONDS_IN_SECOND = 1000

private const val PAGE_SIZE = 50
