package cz.frank.spacex.launches.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.map
import cz.frank.spacex.launches.data.api.ILaunchesAPI
import cz.frank.spacex.launches.data.database.dao.LaunchDao
import cz.frank.spacex.launches.ui.detail.LaunchDetailModel
import cz.frank.spacex.launches.ui.search.LaunchPreviewModel
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

class LaunchesRepository(
    private val launchDao: LaunchDao,
    private val launchAPI: ILaunchesAPI,
 ) : KoinComponent {

    @OptIn(ExperimentalPagingApi::class)
    val pager = Pager(
        config = PagingConfig(pageSize = PAGE_SIZE),
        pagingSourceFactory = { launchDao.getAllLaunches() },
        remoteMediator = inject<LaunchesMediator> { parametersOf(PAGE_SIZE) }.value
    ).flow.map { items ->
        items.map {
            LaunchPreviewModel(
                it.id,
                it.name,
                it.image,
                it.rocket,
                if (it.upcoming) LaunchPreviewModel.State.Upcoming
                else LaunchPreviewModel.State.Launched(it.success)
            )
        }
    }

    suspend fun detailLaunch(id: String) = launchAPI.specificLaunch(id).mapCatching { it.toModel() }
}

fun ILaunchesAPI.LaunchDetailResponse.toModel() = LaunchDetailModel(
    id,
    flightNumber,
    name,
    if (upcoming) LaunchDetailModel.State.Upcoming else LaunchDetailModel.State.Launched(success),
    fairings.recovered,
    launchpad?.let { LaunchDetailModel.Launchpad(it.name) },
    LaunchDetailModel.Rocket(rocket.name),
    details,
    date * UNIX_TO_EPOCH_MULTIPLIER,
    links.youtubeId,
    links.article
)

private const val UNIX_TO_EPOCH_MULTIPLIER = 1000

private const val PAGE_SIZE = 50
